package com.libri.app.data.entity

enum class UserRole { READER, LIBRARIAN, ADMIN }

enum class BookStatus { AVAILABLE, ON_LOAN, RESERVED, DAMAGED, LOST }

enum class LoanStatus { ACTIVE, RETURNED, OVERDUE }

enum class ReservationStatus { PENDING, ACTIVE, EXPIRED, CANCELLED }
