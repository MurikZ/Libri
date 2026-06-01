package com.libri.app.domain.model

import com.libri.app.data.entity.BookStatus
import com.libri.app.data.entity.LoanStatus
import com.libri.app.data.entity.ReservationStatus
import com.libri.app.data.entity.UserRole
import java.time.LocalDate

data class User(
    val id: Long,
    val email: String,
    val firstName: String,
    val lastName: String,
    val phone: String?,
    val role: UserRole,
    val registrationDate: LocalDate,
    val city: String?,
    val isSynced: Boolean = true
) {
    val fullName: String get() = "$firstName $lastName"
    val initials: String get() = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}"
}

data class Book(
    val id: Long,
    val title: String,
    val authors: List<String>,
    val description: String?,
    val publicationYear: Int,
    val isbn: String,
    val publisher: String?,
    val availableInstances: Int,
    val totalInstances: Int,
    val status: BookStatus
)

data class Loan(
    val id: Long,
    val bookInstanceId: Long,
    val bookId: Long,
    val bookTitle: String,
    val bookAuthors: List<String>,
    val loanDate: LocalDate,
    val dueDate: LocalDate,
    val returnDate: LocalDate?,
    val status: LoanStatus,
    val isOverdue: Boolean,
    val daysOverdue: Long
)

data class Reservation(
    val id: Long,
    val bookId: Long,
    val bookTitle: String,
    val bookAuthors: List<String>,
    val reservationDate: LocalDate,
    val expiryDate: LocalDate,
    val status: ReservationStatus
)

data class Fine(
    val id: Long,
    val loanId: Long,
    val amount: Double,
    val reason: String,
    val paid: Boolean,
    val calculatedDate: LocalDate
)
