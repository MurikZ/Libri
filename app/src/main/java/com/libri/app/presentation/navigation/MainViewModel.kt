package com.libri.app.presentation.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libri.app.repository.AuthRepository
import com.libri.app.repository.DataPreloader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val dataPreloader: DataPreloader
) : ViewModel() {

    val sessionState: StateFlow<SessionState> = combine(
        authRepository.currentUserId,
        authRepository.currentUserRole
    ) { userId, role ->
        if (userId != null && role != null) SessionState.LoggedIn(userId, role)
        else SessionState.LoggedOut
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SessionState.Loading
    )

    init {
        viewModelScope.launch { dataPreloader.preloadIfNeeded() }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
