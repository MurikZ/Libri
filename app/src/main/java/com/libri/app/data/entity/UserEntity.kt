package com.libri.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val passwordHash: String,
    val firstName: String,
    val lastName: String,
    val phone: String? = null,
    val role: UserRole,
    val registrationDate: LocalDate,
    val city: String? = null
)
