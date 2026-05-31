package com.libri.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
import com.libri.app.data.entity.FineEntity
import com.libri.app.data.entity.LoanEntity
import com.libri.app.data.entity.ReservationEntity
import com.libri.app.data.entity.UserEntity

@Database(
    entities = [
        UserEntity::class,
        BookEntity::class,
        AuthorEntity::class,
        BookAuthorCrossRef::class,
        BookInstanceEntity::class,
        LoanEntity::class,
        ReservationEntity::class,
        FineEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun bookDao(): BookDao
    abstract fun bookInstanceDao(): BookInstanceDao
    abstract fun loanDao(): LoanDao
    abstract fun reservationDao(): ReservationDao
    abstract fun fineDao(): FineDao
}
