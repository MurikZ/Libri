package com.libri.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
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

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE users ADD COLUMN isSynced INTEGER NOT NULL DEFAULT 1")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE books ADD COLUMN coverImageUri TEXT")
                db.execSQL("ALTER TABLE books ADD COLUMN fragment TEXT")
            }
        }
    }
}
