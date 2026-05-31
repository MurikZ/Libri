package com.libri.app.presentation.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libri.app.domain.model.Fine
import com.libri.app.domain.model.Loan
import com.libri.app.domain.model.Reservation
import com.libri.app.repository.FineRepository
import com.libri.app.repository.LoanRepository
import com.libri.app.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoansUiState(
    val activeLoans: List<Loan> = emptyList(),
    val activeReservations: List<Reservation> = emptyList(),
    val loanHistory: List<Loan> = emptyList(),
    val fines: List<Fine> = emptyList(),
    val message: String? = null
)

@HiltViewModel
class LoansViewModel @Inject constructor(
    private val loanRepository: LoanRepository,
    private val reservationRepository: ReservationRepository,
    private val fineRepository: FineRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    private var _userId: Long = 0L

    private val _loans = MutableStateFlow<List<Loan>>(emptyList())
    private val _reservations = MutableStateFlow<List<Reservation>>(emptyList())
    private val _history = MutableStateFlow<List<Loan>>(emptyList())
    private val _fines = MutableStateFlow<List<Fine>>(emptyList())

    val uiState: StateFlow<LoansUiState> = combine(
        _loans, _reservations, _history, _fines, _message
    ) { loans, reservations, history, fines, message ->
        LoansUiState(loans, reservations, history, fines, message)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LoansUiState()
    )

    fun init(userId: Long) {
        if (_userId == userId) return
        _userId = userId
        viewModelScope.launch {
            loanRepository.getActiveLoans(userId).collect { _loans.value = it }
        }
        viewModelScope.launch {
            reservationRepository.getActiveReservations(userId).collect { _reservations.value = it }
        }
        viewModelScope.launch {
            loanRepository.getLoanHistory(userId).collect { _history.value = it }
        }
        viewModelScope.launch {
            fineRepository.getFines(userId).collect { _fines.value = it }
        }
    }

    fun payFine(fineId: Long) {
        viewModelScope.launch {
            fineRepository.payFine(fineId)
                .onSuccess { showMessage("Штраф оплачен") }
                .onFailure { e -> showMessage(e.message ?: "Ошибка") }
        }
    }

    fun cancelReservation(reservationId: Long) {
        viewModelScope.launch {
            reservationRepository.cancelReservation(reservationId)
                .onSuccess { showMessage("Бронь отменена") }
                .onFailure { e -> showMessage(e.message ?: "Ошибка") }
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
