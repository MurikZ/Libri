package com.libri.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.libri.app.data.entity.LoanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LoanDao {
    @Query("SELECT * FROM loans WHERE userId = :userId AND status = 'ACTIVE' ORDER BY dueDate ASC")
    fun getActiveLoansForUser(userId: Long): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE userId = :userId ORDER BY loanDate DESC")
    fun getLoanHistoryForUser(userId: Long): Flow<List<LoanEntity>>

    @Query("SELECT * FROM loans WHERE status = 'ACTIVE' ORDER BY dueDate ASC")
    fun getAllActiveLoans(): Flow<List<LoanEntity>>

    @Query("SELECT COUNT(*) FROM loans WHERE userId = :userId AND status = 'ACTIVE'")
    suspend fun countActiveLoans(userId: Long): Int

    @Query("SELECT * FROM loans WHERE id = :id")
    suspend fun findById(id: Long): LoanEntity?

    @Query("SELECT * FROM loans WHERE bookInstanceId = :instanceId AND status = 'ACTIVE' LIMIT 1")
    suspend fun findActiveLoanForInstance(instanceId: Long): LoanEntity?

    @Insert
    suspend fun insert(loan: LoanEntity): Long

    @Update
    suspend fun update(loan: LoanEntity)
}
