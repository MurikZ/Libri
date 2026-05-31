package com.libri.app.data.db

import androidx.room.TypeConverter
import com.libri.app.data.entity.BookStatus
import com.libri.app.data.entity.LoanStatus
import com.libri.app.data.entity.ReservationStatus
import com.libri.app.data.entity.UserRole
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let { LocalDate.parse(it) }

    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = UserRole.valueOf(value)

    @TypeConverter
    fun fromBookStatus(status: BookStatus): String = status.name

    @TypeConverter
    fun toBookStatus(value: String): BookStatus = BookStatus.valueOf(value)

    @TypeConverter
    fun fromLoanStatus(status: LoanStatus): String = status.name

    @TypeConverter
    fun toLoanStatus(value: String): LoanStatus = LoanStatus.valueOf(value)

    @TypeConverter
    fun fromReservationStatus(status: ReservationStatus): String = status.name

    @TypeConverter
    fun toReservationStatus(value: String): ReservationStatus = ReservationStatus.valueOf(value)
}
