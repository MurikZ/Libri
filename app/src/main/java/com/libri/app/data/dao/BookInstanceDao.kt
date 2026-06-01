package com.libri.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.libri.app.data.entity.BookInstanceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookInstanceDao {
    @Query("SELECT * FROM book_instances WHERE bookId = :bookId")
    suspend fun getInstancesForBook(bookId: Long): List<BookInstanceEntity>

    @Query("SELECT * FROM book_instances WHERE bookId = :bookId AND status = 'AVAILABLE' LIMIT 1")
    suspend fun findAvailableInstance(bookId: Long): BookInstanceEntity?

    @Query("SELECT * FROM book_instances WHERE id = :id")
    suspend fun findById(id: Long): BookInstanceEntity?

    @Query("SELECT * FROM book_instances")
    fun getAll(): Flow<List<BookInstanceEntity>>

    @Insert
    suspend fun insert(instance: BookInstanceEntity): Long

    @Update
    suspend fun update(instance: BookInstanceEntity)

    @Query("DELETE FROM book_instances WHERE id = :id")
    suspend fun deleteById(id: Long)
}
