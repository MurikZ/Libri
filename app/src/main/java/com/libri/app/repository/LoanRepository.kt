package com.libri.app.repository

import com.libri.app.data.dao.BookDao
import com.libri.app.data.dao.BookInstanceDao
import com.libri.app.data.dao.FineDao
import com.libri.app.data.dao.LoanDao
import com.libri.app.data.entity.BookStatus
import com.libri.app.data.entity.FineEntity
import com.libri.app.data.entity.LoanEntity
import com.libri.app.data.entity.LoanStatus
import com.libri.app.domain.model.Loan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoanRepository @Inject constructor(
    private val loanDao: LoanDao,
    private val bookInstanceDao: BookInstanceDao,
    private val bookDao: BookDao,
    private val fineDao: FineDao
) {
    fun getActiveLoans(userId: Long): Flow<List<Loan>> =
        loanDao.getActiveLoansForUser(userId).map { it.map { l -> l.toDomain() } }

    fun getLoanHistory(userId: Long): Flow<List<Loan>> =
        loanDao.getLoanHistoryForUser(userId).map { it.map { l -> l.toDomain() } }

    fun getAllActiveLoans(): Flow<List<Loan>> =
        loanDao.getAllActiveLoans().map { it.map { l -> l.toDomain() } }

    suspend fun canBorrow(userId: Long): Boolean = loanDao.countActiveLoans(userId) < 5

    suspend fun issueBookToUser(userId: Long, bookId: Long, durationDays: Int = 30): Result<Unit> {
        if (!canBorrow(userId)) return Result.failure(Exception("Достигнут лимит выдач (5 книг)"))
        val instance = bookInstanceDao.findAvailableInstance(bookId)
            ?: return Result.failure(Exception("Нет доступных экземпляров"))
        return createLoan(userId, instance.id, durationDays)
    }

    suspend fun createLoan(userId: Long, bookInstanceId: Long, durationDays: Int = 30): Result<Unit> {
        val instance = bookInstanceDao.findById(bookInstanceId)
            ?: return Result.failure(Exception("Экземпляр не найден"))
        if (instance.status != BookStatus.AVAILABLE) {
            return Result.failure(Exception("Экземпляр недоступен"))
        }
        val today = LocalDate.now()
        loanDao.insert(
            LoanEntity(
                userId = userId,
                bookInstanceId = bookInstanceId,
                loanDate = today,
                dueDate = today.plusDays(durationDays.toLong()),
                status = LoanStatus.ACTIVE
            )
        )
        bookInstanceDao.update(instance.copy(status = BookStatus.ON_LOAN))
        return Result.success(Unit)
    }

    suspend fun returnBook(loanId: Long): Result<Unit> {
        val loan = loanDao.findById(loanId)
            ?: return Result.failure(Exception("Выдача не найдена"))
        val today = LocalDate.now()
        loanDao.update(loan.copy(returnDate = today, status = LoanStatus.RETURNED))
        bookInstanceDao.findById(loan.bookInstanceId)?.let {
            bookInstanceDao.update(it.copy(status = BookStatus.AVAILABLE))
        }
        if (today.isAfter(loan.dueDate)) {
            val days = ChronoUnit.DAYS.between(loan.dueDate, today)
            fineDao.insert(
                FineEntity(
                    userId = loan.userId,
                    loanId = loanId,
                    amount = days * 5.0,
                    reason = "Просрочка на $days ${dayWord(days)}",
                    calculatedDate = today
                )
            )
        }
        return Result.success(Unit)
    }

    private suspend fun LoanEntity.toDomain(): Loan {
        val instance = bookInstanceDao.findById(bookInstanceId)
        val bookWithAuthors = instance?.let { bookDao.getBookWithAuthors(it.bookId) }
        val today = LocalDate.now()
        val overdue = status == LoanStatus.ACTIVE && today.isAfter(dueDate)
        val days = if (overdue) ChronoUnit.DAYS.between(dueDate, today) else 0L
        return Loan(
            id = id,
            bookInstanceId = bookInstanceId,
            bookId = instance?.bookId ?: 0L,
            bookTitle = bookWithAuthors?.book?.title ?: "Неизвестная книга",
            bookAuthors = bookWithAuthors?.authors?.map { "${it.firstName} ${it.lastName}" } ?: emptyList(),
            loanDate = loanDate,
            dueDate = dueDate,
            returnDate = returnDate,
            status = if (overdue) LoanStatus.OVERDUE else status,
            isOverdue = overdue,
            daysOverdue = days
        )
    }

    private fun dayWord(days: Long): String = when {
        days % 100 in 11..19 -> "дней"
        days % 10 == 1L -> "день"
        days % 10 in 2..4 -> "дня"
        else -> "дней"
    }
}
