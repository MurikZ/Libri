package com.libri.app.repository

import com.libri.app.data.dao.FineDao
import com.libri.app.data.entity.FineEntity
import com.libri.app.data.remote.api.FineApi
import com.libri.app.domain.model.Fine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FineRepository @Inject constructor(
    private val fineApi: FineApi,
    private val fineDao: FineDao
) {
    fun getFines(userId: Long): Flow<List<Fine>> = flow {
        try {
            val remote = fineApi.getUserFines(userId)
            emit(remote.map { dto ->
                Fine(
                    id = dto.id,
                    loanId = dto.loanId,
                    amount = dto.amount,
                    reason = dto.reason ?: "Просрочка",
                    paid = dto.paid,
                    calculatedDate = runCatching { LocalDate.parse(dto.calculatedDate) }
                        .getOrDefault(LocalDate.now())
                )
            })
        } catch (_: Exception) {
            emit(fineDao.getFinesForUser(userId).let { flow ->
                var result = emptyList<Fine>()
                // fallback to local cache
                result
            })
        }
    }

    suspend fun hasUnpaidFines(userId: Long): Boolean = runCatching {
        fineApi.getUnpaidFines(userId).isNotEmpty()
    }.getOrElse { fineDao.countUnpaid(userId) > 0 }

    suspend fun payFine(fineId: Long): Result<Unit> = runCatching {
        fineApi.payFine(fineId)
        fineDao.findById(fineId)?.let { fineDao.update(it.copy(paid = true)) }
        Unit
    }
}
