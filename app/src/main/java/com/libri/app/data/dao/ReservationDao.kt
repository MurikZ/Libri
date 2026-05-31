package com.libri.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.libri.app.data.entity.ReservationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReservationDao {
    @Query("SELECT * FROM reservations WHERE userId = :userId AND status IN ('PENDING', 'ACTIVE') ORDER BY reservationDate DESC")
    fun getActiveReservationsForUser(userId: Long): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE userId = :userId ORDER BY reservationDate DESC")
    fun getAllReservationsForUser(userId: Long): Flow<List<ReservationEntity>>

    @Query("SELECT * FROM reservations WHERE userId = :userId AND bookId = :bookId AND status IN ('PENDING', 'ACTIVE') LIMIT 1")
    suspend fun findUserReservationForBook(userId: Long, bookId: Long): ReservationEntity?

    @Query("SELECT * FROM reservations WHERE bookId = :bookId AND status IN ('PENDING', 'ACTIVE') LIMIT 1")
    suspend fun findActiveReservationForBook(bookId: Long): ReservationEntity?

    @Query("SELECT * FROM reservations WHERE id = :id")
    suspend fun findById(id: Long): ReservationEntity?

    @Insert
    suspend fun insert(reservation: ReservationEntity): Long

    @Update
    suspend fun update(reservation: ReservationEntity)
}
