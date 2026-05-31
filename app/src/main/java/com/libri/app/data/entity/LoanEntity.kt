package com.libri.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "loans",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = BookInstanceEntity::class,
            parentColumns = ["id"],
            childColumns = ["bookInstanceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class LoanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(index = true) val userId: Long,
    @ColumnInfo(index = true) val bookInstanceId: Long,
    val loanDate: LocalDate,
    val dueDate: LocalDate,
    val returnDate: LocalDate? = null,
    val status: LoanStatus
)
