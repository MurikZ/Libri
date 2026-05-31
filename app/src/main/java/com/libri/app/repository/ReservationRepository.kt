package com.libri.app.repository

import com.libri.app.data.dao.BookDao
import com.libri.app.data.dao.BookInstanceDao
import com.libri.app.data.dao.ReservationDao
import com.libri.app.data.entity.BookStatus
import com.libri.app.data.entity.ReservationEntity
import com.libri.app.data.entity.ReservationStatus
import com.libri.app.data.remote.api.ReservationApi
import com.libri.app.data.remote.dto.CreateReservationRequest
import com.libri.app.domain.model.Reservation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReservationRepository @Inject constructor(
    private val reservationApi: ReservationApi,
    private val reservationDao: ReservationDao,
    private val bookDao: BookDao,
    private val bookInstanceDao: BookInstanceDao
) {
    fun getActiveReservations(userId: Long): Flow<List<Reservation>> = flow {
        try {
            val remote = reservationApi.getUserReservations(userId)
            emit(remote
                .filter { it.status == "PENDING" || it.status == "ACTIVE" }
                .map { dto ->
                    Reservation(
                        id = dto.id,
                        bookId = dto.bookId,
                        bookTitle = dto.bookTitle,
                        bookAuthors = emptyList(),
                        reservationDate = LocalDate.parse(dto.reservationDate),
                        expiryDate = LocalDate.parse(dto.expiryDate),
                        status = runCatching { ReservationStatus.valueOf(dto.status) }
                            .getOrDefault(ReservationStatus.PENDING)
                    )
                })
        } catch (_: Exception) {
            reservationDao.getActiveReservationsForUser(userId)
                .map { it.map { r -> r.toDomain() } }
                .collect { emit(it) }
        }
    }

    fun getAllReservations(userId: Long): Flow<List<Reservation>> =
        reservationDao.getAllReservationsForUser(userId).map { it.map { r -> r.toDomain() } }

    suspend fun findUserReservationForBook(userId: Long, bookId: Long): ReservationEntity? =
        reservationDao.findUserReservationForBook(userId, bookId)

    suspend fun reserve(userId: Long, bookId: Long): Result<Unit> = runCatching {
        reservationApi.createReservation(CreateReservationRequest(userId, bookId))
        Unit
    }

    suspend fun cancelReservation(reservationId: Long): Result<Unit> = runCatching {
        reservationApi.cancelReservation(reservationId)
        reservationDao.findById(reservationId)?.let {
            reservationDao.update(it.copy(status = ReservationStatus.CANCELLED))
        }
        Unit
    }

    private suspend fun ReservationEntity.toDomain(): Reservation {
        val bookWithAuthors = bookDao.getBookWithAuthors(bookId)
        return Reservation(
            id = id,
            bookId = bookId,
            bookTitle = bookWithAuthors?.book?.title ?: "Неизвестная книга",
            bookAuthors = bookWithAuthors?.authors?.map { "${it.firstName} ${it.lastName}" } ?: emptyList(),
            reservationDate = reservationDate,
            expiryDate = expiryDate,
            status = status
        )
    }
}
