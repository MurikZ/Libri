package com.libri.app.repository

import com.libri.app.data.dao.BookDao
import com.libri.app.data.dao.BookInstanceDao
import com.libri.app.data.entity.*
import com.libri.app.data.remote.api.BookApi
import com.libri.app.data.remote.api.LoanApi
import com.libri.app.data.remote.dto.BookRemoteDto
import com.libri.app.data.remote.dto.BorrowRequest
import com.libri.app.data.remote.dto.CreateBookRequest
import com.libri.app.domain.model.Book
import com.libri.app.domain.model.Loan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookRepository @Inject constructor(
    private val bookApi: BookApi,
    private val loanApi: LoanApi,
    private val bookDao: BookDao,
    private val bookInstanceDao: BookInstanceDao
) {
    // Кэш → сервер (offline-first)
    fun getAllBooks(): Flow<List<Book>> = flow {
        // 1. Сразу отдаём кэш
        val cached = bookDao.getAllBooksWithAuthors().map { list -> list.map { it.toDomain() } }
        // emit из Flow — подписываемся на локальный Flow
        // Для упрощения: сначала грузим с сервера, затем кэшируем и отдаём из Room
        try {
            val remote = bookApi.getBooks(size = 100)
            remote.books.forEach { dto -> cacheBook(dto) }
        } catch (_: Exception) {
            // нет сети — работаем из кэша
        }
        // отдаём данные из Room через collect
        cached.collect { emit(it) }
    }

    fun searchBooks(query: String): Flow<List<Book>> = flow {
        try {
            val remote = bookApi.searchBooks(query)
            remote.books.forEach { dto -> cacheBook(dto) }
        } catch (_: Exception) { /* offline */ }

        bookDao.searchBooks(query).map { list -> list.map { it.toDomain() } }
            .collect { emit(it) }
    }

    suspend fun getBook(id: Long): Book? {
        return try {
            val remote = bookApi.getBook(id)
            cacheBook(remote)
            bookDao.getBookWithAuthors(id)?.toDomain()
        } catch (_: Exception) {
            bookDao.getBookWithAuthors(id)?.toDomain()
        }
    }

    suspend fun addBook(
        title: String,
        description: String?,
        publicationYear: Int,
        isbn: String,
        publisher: String?,
        authorNames: List<Pair<String, String>>
    ): Long {
        val response = bookApi.createBook(
            CreateBookRequest(
                title = title,
                isbn = isbn.takeIf { it.isNotBlank() },
                publicationYear = publicationYear,
                publisher = publisher,
                description = description,
                instanceCount = 2
            )
        )
        cacheBook(response)
        return response.id
    }

    suspend fun deleteBook(bookId: Long) {
        bookApi.deleteBook(bookId)
        val book = bookDao.getBook(bookId) ?: return
        bookDao.deleteBook(book)
    }

    suspend fun borrowBook(userId: Long, bookInstanceId: Long): Result<Unit> = runCatching {
        loanApi.borrowBook(BorrowRequest(userId, bookInstanceId))
        Unit
    }

    suspend fun getUserLoans(userId: Long): List<Loan> = runCatching {
        loanApi.getUserLoans(userId).map { it.toDomain() }
    }.getOrDefault(emptyList())

    private suspend fun cacheBook(dto: BookRemoteDto) {
        val bookEntity = BookEntity(
            id = dto.id,
            title = dto.title,
            description = dto.description,
            publicationYear = dto.publicationYear ?: 0,
            isbn = dto.isbn ?: "",
            publisher = dto.publisher
        )
        bookDao.upsertBook(bookEntity)

        dto.authors?.forEach { authorDto ->
            val authorEntity = AuthorEntity(
                id = authorDto.id,
                firstName = authorDto.firstName,
                lastName = authorDto.lastName
            )
            bookDao.upsertAuthor(authorEntity)
            bookDao.insertBookAuthorCrossRef(BookAuthorCrossRef(bookId = dto.id, authorId = authorDto.id))
        }
    }

    private suspend fun BookWithAuthors.toDomain(): Book {
        val instances = bookInstanceDao.getInstancesForBook(book.id)
        val available = instances.count { it.status == BookStatus.AVAILABLE }
        val status = when {
            available > 0 -> BookStatus.AVAILABLE
            instances.any { it.status == BookStatus.ON_LOAN } -> BookStatus.ON_LOAN
            instances.any { it.status == BookStatus.RESERVED } -> BookStatus.RESERVED
            else -> BookStatus.AVAILABLE
        }
        return Book(
            id = book.id,
            title = book.title,
            authors = authors.map { "${it.firstName} ${it.lastName}" },
            description = book.description,
            publicationYear = book.publicationYear,
            isbn = book.isbn,
            publisher = book.publisher,
            availableInstances = available,
            totalInstances = instances.size,
            status = status
        )
    }

    private fun com.libri.app.data.remote.dto.LoanRemoteDto.toDomain(): Loan {
        val loanDate = LocalDate.parse(loanDate)
        val dueDateParsed = LocalDate.parse(dueDate)
        val returnDateParsed = returnDate?.let { LocalDate.parse(it) }
        val today = LocalDate.now()
        val overdue = returnDateParsed == null && today.isAfter(dueDateParsed)
        val daysOverdue = if (overdue) ChronoUnit.DAYS.between(dueDateParsed, today) else 0L
        return Loan(
            id = id,
            bookInstanceId = bookInstanceId,
            bookId = bookId,
            bookTitle = bookTitle,
            bookAuthors = emptyList(),
            loanDate = loanDate,
            dueDate = dueDateParsed,
            returnDate = returnDateParsed,
            status = runCatching { com.libri.app.data.entity.LoanStatus.valueOf(status) }
                .getOrDefault(com.libri.app.data.entity.LoanStatus.ACTIVE),
            isOverdue = overdue,
            daysOverdue = daysOverdue
        )
    }
}
