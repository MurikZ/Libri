package com.libri.app.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.libri.app.data.entity.UserRole
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val USER_ID = longPreferencesKey("user_id")
    private val USER_ROLE = stringPreferencesKey("user_role")
    private val JWT_TOKEN = stringPreferencesKey("jwt_token")
    private val USER_FIRST_NAME = stringPreferencesKey("user_first_name")
    private val USER_LAST_NAME = stringPreferencesKey("user_last_name")
    private val USER_EMAIL = stringPreferencesKey("user_email")

    val userId: Flow<Long?> = dataStore.data.map { it[USER_ID] }
    val userRole: Flow<UserRole?> = dataStore.data.map { prefs ->
        prefs[USER_ROLE]?.let { runCatching { UserRole.valueOf(it) }.getOrNull() }
    }
    val token: Flow<String?> = dataStore.data.map { it[JWT_TOKEN] }
    val isOffline: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[JWT_TOKEN]?.startsWith("offline_") == true
    }
    val firstName: Flow<String?> = dataStore.data.map { it[USER_FIRST_NAME] }
    val lastName: Flow<String?> = dataStore.data.map { it[USER_LAST_NAME] }
    val email: Flow<String?> = dataStore.data.map { it[USER_EMAIL] }

    suspend fun saveSession(
        userId: Long,
        role: UserRole,
        token: String,
        firstName: String = "",
        lastName: String = "",
        email: String = ""
    ) {
        dataStore.edit { prefs ->
            prefs[USER_ID] = userId
            prefs[USER_ROLE] = role.name
            prefs[JWT_TOKEN] = token
            prefs[USER_FIRST_NAME] = firstName
            prefs[USER_LAST_NAME] = lastName
            prefs[USER_EMAIL] = email
        }
    }

    suspend fun clearSession() {
        dataStore.edit { it.clear() }
    }
}
