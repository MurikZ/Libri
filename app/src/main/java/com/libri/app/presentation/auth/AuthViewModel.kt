package com.libri.app.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libri.app.data.entity.UserRole
import com.libri.app.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        if (email.isBlank()) { _uiState.update { it.copy(error = "Введите email") }; return }
        if (password.isBlank()) { _uiState.update { it.copy(error = "Введите пароль") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.login(email, password)
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun register(
        email: String,
        password: String,
        confirmPassword: String,
        firstName: String,
        lastName: String,
        phone: String,
        city: String,
        role: UserRole
    ) {
        if (firstName.isBlank()) { _uiState.update { it.copy(error = "Введите имя") }; return }
        if (lastName.isBlank()) { _uiState.update { it.copy(error = "Введите фамилию") }; return }
        if (email.isBlank()) { _uiState.update { it.copy(error = "Введите email") }; return }
        if (password.length < 6) { _uiState.update { it.copy(error = "Пароль минимум 6 символов") }; return }
        if (password != confirmPassword) { _uiState.update { it.copy(error = "Пароли не совпадают") }; return }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.register(email, password, firstName, lastName, phone, city, role)
                .onFailure { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
        }
    }

    fun clearError() = _uiState.update { it.copy(error = null) }
}
