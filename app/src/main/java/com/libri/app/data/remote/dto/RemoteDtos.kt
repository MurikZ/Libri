package com.libri.app.data.remote.dto

data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(
    val email: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null
)

data class LoginResponse(
    val token: String,
    val userId: Long,
    val role: String,
    val firstName: String,
    val lastName: String,
    val email: String
)

data class AuthorRemoteDto(
    val id: Long,
    val firstName: String,
    val lastName: String
)

data class BookRemoteDto(
    val id: Long,
    val title: String,
    val isbn: String?,
    val publicationYear: Int?,
    val publisher: String?,
    val description: String?,
    val authors: List<AuthorRemoteDto>?,
    val totalInstances: Int,
    val availableInstances: Int,
    val status: String?
)

data class BooksResponse(
    val books: List<BookRemoteDto>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int
)

data class CreateBookRequest(
    val title: String,
    val isbn: String? = null,
    val publicationYear: Int? = null,
    val publisher: String? = null,
    val description: String? = null,
    val authorIds: List<Long>? = null,
    val instanceCount: Int = 1
)

data class LoanRemoteDto(
    val id: Long,
    val userId: Long,
    val userFullName: String,
    val bookInstanceId: Long,
    val inventoryNumber: String,
    val bookId: Long,
    val bookTitle: String,
    val loanDate: String,
    val dueDate: String,
    val returnDate: String?,
    val status: String,
    val overdue: Boolean
)

data class BorrowRequest(val userId: Long, val bookInstanceId: Long)

data class ReturnResponse(
    val loan: LoanRemoteDto,
    val fine: FineRemoteDto?
)

data class FineRemoteDto(
    val id: Long,
    val userId: Long,
    val userFullName: String,
    val loanId: Long,
    val amount: Double,
    val paid: Boolean,
    val calculatedDate: String,
    val reason: String?
)

data class ReservationRemoteDto(
    val id: Long,
    val userId: Long,
    val bookId: Long,
    val bookTitle: String,
    val reservationDate: String,
    val expiryDate: String,
    val status: String
)

data class CreateReservationRequest(val userId: Long, val bookId: Long)
