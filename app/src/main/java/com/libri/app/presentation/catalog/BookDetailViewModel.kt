package com.libri.app.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libri.app.data.entity.ReservationEntity
import com.libri.app.domain.model.Book
import com.libri.app.domain.model.Loan
import com.libri.app.repository.BookRepository
import com.libri.app.repository.FineRepository
import com.libri.app.repository.LoanRepository
import com.libri.app.repository.ReservationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BookDetailUiState(
    val book: Book? = null,
    val isLoading: Boolean = true,
    val reservation: ReservationEntity? = null,
    val activeLoans: List<Loan> = emptyList(),
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class BookDetailViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val reservationRepository: ReservationRepository,
    private val loanRepository: LoanRepository,
    private val fineRepository: FineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookDetailUiState())
    val uiState: StateFlow<BookDetailUiState> = _uiState.asStateFlow()

    fun loadBook(bookId: Long, userId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val book = bookRepository.getBook(bookId)
            val reservation = reservationRepository.findUserReservationForBook(userId, bookId)
            _uiState.update { it.copy(book = book, isLoading = false, reservation = reservation) }
        }
    }

    fun reserve(userId: Long, bookId: Long) {
        viewModelScope.launch {
            reservationRepository.reserve(userId, bookId)
                .onSuccess {
                    val reservation = reservationRepository.findUserReservationForBook(userId, bookId)
                    val book = bookRepository.getBook(bookId)
                    _uiState.update { it.copy(reservation = reservation, book = book, message = "Книга забронирована") }
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun cancelReservation(userId: Long, bookId: Long) {
        viewModelScope.launch {
            val reservation = reservationRepository.findUserReservationForBook(userId, bookId) ?: return@launch
            reservationRepository.cancelReservation(reservation.id)
                .onSuccess {
                    val book = bookRepository.getBook(bookId)
                    _uiState.update { it.copy(reservation = null, book = book, message = "Бронь отменена") }
                }
                .onFailure { e -> _uiState.update { it.copy(error = e.message) } }
        }
    }

    fun clearMessages() = _uiState.update { it.copy(message = null, error = null) }
}
