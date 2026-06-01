package com.libri.app.repository

import com.libri.app.data.dao.UserDao
import com.libri.app.data.entity.UserEntity
import com.libri.app.data.entity.UserRole
import com.libri.app.data.remote.api.AuthApi
import com.libri.app.data.remote.dto.LoginRequest
import com.libri.app.data.remote.dto.RegisterRequest
import com.libri.app.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authApi: AuthApi,
    private val userDao: UserDao,
    private val sessionManager: SessionManager
) {
    val currentUserId: Flow<Long?> = sessionManager.userId
    val currentUserRole: Flow<UserRole?> = sessionManager.userRole
    val isOfflineSession: Flow<Boolean> = sessionManager.isOffline

    suspend fun login(email: String, password: String): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        val onlineResult = runCatching {
            val response = authApi.login(LoginRequest(normalizedEmail, password))
            val role = runCatching { UserRole.valueOf(response.role) }.getOrDefault(UserRole.READER)
            val entity = UserEntity(
                id = response.userId,
                email = response.email,
                passwordHash = hashPassword(password),
                firstName = response.firstName,
                lastName = response.lastName,
                role = role,
                registrationDate = LocalDate.now(),
                isSynced = true
            )
            userDao.insertOrReplace(entity)
            sessionManager.saveSession(
                userId = response.userId,
                role = role,
                token = response.token,
                firstName = response.firstName,
                lastName = response.lastName,
                email = response.email
            )
            entity
        }
        if (onlineResult.isSuccess) return onlineResult

        // оффлайн: ищем пользователя в локальной БД
        return runCatching {
            val local = userDao.login(normalizedEmail, hashPassword(password))
                ?: error("Неверный email или пароль")
            sessionManager.saveSession(
                userId = local.id,
                role = local.role,
                token = "offline_${local.id}",
                firstName = local.firstName,
                lastName = local.lastName,
                email = local.email
            )
            local
        }
    }

    suspend fun register(
        email: String,
        password: String,
        firstName: String,
        lastName: String,
        phone: String?,
        city: String? = null,
        role: UserRole = UserRole.READER
    ): Result<UserEntity> {
        val normalizedEmail = email.trim().lowercase()
        val onlineResult = runCatching {
            val response = authApi.register(
                RegisterRequest(
                    email = normalizedEmail,
                    password = password,
                    firstName = firstName.trim(),
                    lastName = lastName.trim(),
                    phone = phone?.takeIf { it.isNotBlank() }
                )
            )
            val userRole = runCatching { UserRole.valueOf(response.role) }.getOrDefault(UserRole.READER)
            val entity = UserEntity(
                id = response.userId,
                email = response.email,
                passwordHash = hashPassword(password),
                firstName = response.firstName,
                lastName = response.lastName,
                role = userRole,
                registrationDate = LocalDate.now(),
                isSynced = true
            )
            userDao.insertOrReplace(entity)
            sessionManager.saveSession(
                userId = response.userId,
                role = userRole,
                token = response.token,
                firstName = response.firstName,
                lastName = response.lastName,
                email = response.email
            )
            entity
        }
        if (onlineResult.isSuccess) return onlineResult

        // оффлайн: сохраняем локально
        return runCatching {
            if (userDao.findByEmail(normalizedEmail) != null) {
                error("Email уже зарегистрирован")
            }
            val entity = UserEntity(
                id = 0,
                email = normalizedEmail,
                passwordHash = hashPassword(password),
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                phone = phone?.takeIf { it.isNotBlank() },
                role = role,
                registrationDate = LocalDate.now(),
                city = city?.takeIf { it.isNotBlank() },
                isSynced = false
            )
            val localId = userDao.insert(entity)
            val saved = entity.copy(id = localId)
            sessionManager.saveSession(
                userId = localId,
                role = role,
                token = "offline_$localId",
                firstName = firstName.trim(),
                lastName = lastName.trim(),
                email = normalizedEmail
            )
            saved
        }
    }

    suspend fun logout() = sessionManager.clearSession()

    suspend fun getCurrentUser(id: Long): User? = userDao.findById(id)?.toDomain()

    fun hashPassword(password: String): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        return digest.digest(password.toByteArray()).joinToString("") { "%02x".format(it) }
    }

    fun getAllReaders(): Flow<List<User>> = userDao.getAllReaders().map { list ->
        list.map { it.toDomain() }
    }

    private fun UserEntity.toDomain() = User(
        id = id,
        email = email,
        firstName = firstName,
        lastName = lastName,
        phone = phone,
        role = role,
        registrationDate = registrationDate,
        city = city,
        isSynced = isSynced
    )
}
