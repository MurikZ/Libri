package com.libri.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.libri.app.data.entity.FineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FineDao {
    @Query("SELECT * FROM fines WHERE userId = :userId ORDER BY calculatedDate DESC")
    fun getFinesForUser(userId: Long): Flow<List<FineEntity>>

    @Query("SELECT COUNT(*) FROM fines WHERE userId = :userId AND paid = 0")
    suspend fun countUnpaid(userId: Long): Int

    @Query("SELECT * FROM fines WHERE id = :id")
    suspend fun findById(id: Long): FineEntity?

    @Insert
    suspend fun insert(fine: FineEntity): Long

    @Update
    suspend fun update(fine: FineEntity)
}
