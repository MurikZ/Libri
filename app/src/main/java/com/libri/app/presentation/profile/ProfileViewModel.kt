package com.libri.app.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libri.app.domain.model.Fine
import com.libri.app.domain.model.User
import com.libri.app.repository.AuthRepository
import com.libri.app.repository.FineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val fines: List<Fine> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null
) {
    val isOffline: Boolean get() = user?.isSynced == false
}

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val fineRepository: FineRepository
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    private val _fines = MutableStateFlow<List<Fine>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ProfileUiState> = combine(
        _user, _fines, _isLoading, _message
    ) { user, fines, loading, message ->
        ProfileUiState(user, fines, loading, message)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfileUiState()
    )

    fun init(userId: Long) {
        viewModelScope.launch {
            _user.value = authRepository.getCurrentUser(userId)
            _isLoading.value = false
        }
        viewModelScope.launch {
            fineRepository.getFines(userId).collect { _fines.value = it }
        }
    }

    fun payFine(fineId: Long) {
        viewModelScope.launch {
            fineRepository.payFine(fineId)
                .onSuccess { showMessage("Штраф оплачен") }
        }
    }

    private fun showMessage(msg: String) {
        _message.update { msg }
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _message.update { null }
        }
    }
}
