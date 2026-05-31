package com.libri.app.repository

import android.content.Context
import com.libri.app.data.dao.BookDao
import com.libri.app.data.dao.BookInstanceDao
import com.libri.app.data.dao.FineDao
import com.libri.app.data.dao.LoanDao
import com.libri.app.data.dao.ReservationDao
import com.libri.app.data.dao.UserDao
import com.libri.app.data.entity.AuthorEntity
import com.libri.app.data.entity.BookAuthorCrossRef
import com.libri.app.data.entity.BookEntity
import com.libri.app.data.entity.BookInstanceEntity
import com.libri.app.data.entity.BookStatus
import com.libri.app.data.entity.FineEntity
import com.libri.app.data.entity.LoanEntity
import com.libri.app.data.entity.LoanStatus
import com.libri.app.data.entity.ReservationEntity
import com.libri.app.data.entity.ReservationStatus
import com.libri.app.data.entity.UserEntity
import com.libri.app.data.entity.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataPreloader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userDao: UserDao,
    private val bookDao: BookDao,
    private val bookInstanceDao: BookInstanceDao,
    private val loanDao: LoanDao,
    private val reservationDao: ReservationDao,
    private val fineDao: FineDao,
    private val authRepository: AuthRepository
) {
    private val prefs = context.getSharedPreferences("libri_prefs", Context.MODE_PRIVATE)

    suspend fun preloadIfNeeded() {
        if (prefs.getBoolean("data_loaded", false)) return

        val hash = authRepository.hashPassword("123456")
        val today = LocalDate.now()

        val readerId = userDao.insert(
            UserEntity(email = "reader@lib.ru", passwordHash = hash,
                firstName = "Иван", lastName = "Петров",
                role = UserRole.READER, registrationDate = today.minusMonths(6))
        )
        userDao.insert(
            UserEntity(email = "librarian@lib.ru", passwordHash = hash,
                firstName = "Мария", lastName = "Сидорова",
                role = UserRole.LIBRARIAN, registrationDate = today.minusYears(2))
        )
        userDao.insert(
            UserEntity(email = "admin@lib.ru", passwordHash = hash,
                firstName = "Алексей", lastName = "Иванов",
                role = UserRole.ADMIN, registrationDate = today.minusYears(3))
        )

        data class BookSeed(val title: String, val year: Int, val isbn: String, val authors: List<Pair<String, String>>)

        val seedBooks = listOf(
            BookSeed("Мастер и Маргарита", 1967, "978-5-389-07985-0", listOf("Михаил" to "Булгаков")),
            BookSeed("Преступление и наказание", 1866, "978-5-04-099028-3", listOf("Фёдор" to "Достоевский")),
            BookSeed("Война и мир", 1869, "978-5-04-099028-4", listOf("Лев" to "Толстой")),
            BookSeed("1984", 1949, "978-5-17-083206-4", listOf("Джордж" to "Оруэлл")),
            BookSeed("Чистый код", 2008, "978-5-496-00487-9", listOf("Роберт" to "Мартин")),
            BookSeed("Паттерны проектирования", 1994, "978-5-496-00912-6", listOf("Эрих" to "Гамма")),
            BookSeed("Автостопом по галактике", 1979, "978-5-17-083207-1", listOf("Дуглас" to "Адамс")),
            BookSeed("Гарри Поттер и философский камень", 1997, "978-5-389-07985-1", listOf("Джоан" to "Роулинг")),
            BookSeed("Маленький принц", 1943, "978-5-389-07985-2", listOf("Антуан" to "Сент-Экзюпери")),
            BookSeed("Дюна", 1965, "978-5-389-07985-3", listOf("Фрэнк" to "Герберт"))
        )

        val bookIds = mutableListOf<Long>()
        val instanceIds = mutableListOf<Pair<Long, Long>>() // Pair(instance1Id, instance2Id)

        seedBooks.forEach { seed ->
            val bookId = bookDao.insertBook(BookEntity(title = seed.title, publicationYear = seed.year, isbn = seed.isbn))
            bookIds.add(bookId)

            seed.authors.forEach { (fn, ln) ->
                val existing = bookDao.findAuthorByName(fn, ln)
                val authorId = existing?.id ?: bookDao.insertAuthor(AuthorEntity(firstName = fn, lastName = ln))
                bookDao.insertBookAuthorCrossRef(BookAuthorCrossRef(bookId = bookId, authorId = authorId))
            }

            val id1 = bookInstanceDao.insert(BookInstanceEntity(bookId = bookId, inventoryNumber = "INV-$bookId-001", status = BookStatus.AVAILABLE))
            val id2 = bookInstanceDao.insert(BookInstanceEntity(bookId = bookId, inventoryNumber = "INV-$bookId-002", status = BookStatus.AVAILABLE))
            instanceIds.add(id1 to id2)
        }

        // Overdue loan for Мастер и Маргарита (instance 1)
        val overdueInstanceId = instanceIds[0].first
        val loanId = loanDao.insert(
            LoanEntity(
                userId = readerId,
                bookInstanceId = overdueInstanceId,
                loanDate = today.minusDays(20),
                dueDate = today.minusDays(6),
                status = LoanStatus.ACTIVE
            )
        )
        bookInstanceDao.update(BookInstanceEntity(id = overdueInstanceId, bookId = bookIds[0], inventoryNumber = "INV-${bookIds[0]}-001", status = BookStatus.ON_LOAN))

        // Active loan for Преступление и наказание (instance 1)
        val activeInstanceId = instanceIds[1].first
        loanDao.insert(
            LoanEntity(
                userId = readerId,
                bookInstanceId = activeInstanceId,
                loanDate = today.minusDays(5),
                dueDate = today.plusDays(9),
                status = LoanStatus.ACTIVE
            )
        )
        bookInstanceDao.update(BookInstanceEntity(id = activeInstanceId, bookId = bookIds[1], inventoryNumber = "INV-${bookIds[1]}-001", status = BookStatus.ON_LOAN))

        // Active reservation for Война и мир (instance 1)
        val reservedInstanceId = instanceIds[2].first
        reservationDao.insert(
            ReservationEntity(
                userId = readerId,
                bookId = bookIds[2],
                reservationDate = today,
                expiryDate = today.plusDays(3),
                status = ReservationStatus.ACTIVE
            )
        )
        bookInstanceDao.update(BookInstanceEntity(id = reservedInstanceId, bookId = bookIds[2], inventoryNumber = "INV-${bookIds[2]}-001", status = BookStatus.RESERVED))

        // Unpaid fine from overdue
        fineDao.insert(
            FineEntity(
                userId = readerId,
                loanId = loanId,
                amount = 150.0,
                reason = "Просрочка на 6 дней",
                paid = false,
                calculatedDate = today.minusDays(1)
            )
        )

        prefs.edit().putBoolean("data_loaded", true).apply()
    }
}
