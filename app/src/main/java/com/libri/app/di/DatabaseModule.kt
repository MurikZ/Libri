package com.libri.app.di

import android.content.Context
import androidx.room.Room
import com.libri.app.data.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "libri_db")
            .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
            .build()

    @Provides
    fun provideUserDao(db: AppDatabase) = db.userDao()

    @Provides
    fun provideBookDao(db: AppDatabase) = db.bookDao()

    @Provides
    fun provideBookInstanceDao(db: AppDatabase) = db.bookInstanceDao()

    @Provides
    fun provideLoanDao(db: AppDatabase) = db.loanDao()

    @Provides
    fun provideReservationDao(db: AppDatabase) = db.reservationDao()

    @Provides
    fun provideFineDao(db: AppDatabase) = db.fineDao()
}
