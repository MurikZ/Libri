package com.libri.app.presentation.librarian

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libri.app.domain.model.Book
import com.libri.app.domain.model.Loan
import com.libri.app.domain.model.User
import com.libri.app.repository.AuthRepository
import com.libri.app.repository.BookRepository
import com.libri.app.repository.LoanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LibrarianUiState(
    val books: List<Book> = emptyList(),
    val readers: List<User> = emptyList(),
    val activeLoans: List<Loan> = emptyList(),
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class LibrarianViewModel @Inject constructor(
    private val bookRepository: BookRepository,
    private val loanRepository: LoanRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    private val _readers = MutableStateFlow<List<User>>(emptyList())
    private val _activeLoans = MutableStateFlow<List<Loan>>(emptyList())
    private val _message = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LibrarianUiState> = combine(
        _books, _readers, _activeLoans, _message, _error
    ) { books, readers, loans, message, error ->
        LibrarianUiState(books, readers, loans, message, error)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = LibrarianUiState()
    )

    init {
        viewModelScope.launch {
            bookRepository.getAllBooks().collect { _books.value = it }
        }
        viewModelScope.launch {
            authRepository.getAllReaders().collect { _readers.value = it }
        }
        viewModelScope.launch {
            loanRepository.getAllActiveLoans().collect { _activeLoans.value = it }
        }
    }

    fun issueLoan(userId: Long, bookId: Long, durationDays: Int = 30) {
        viewModelScope.launch {
            loanRepository.issueBookToUser(userId, bookId, durationDays)
                .onSuccess { showMessage("Книга выдана на $durationDays дней") }
                .onFailure { e -> showError(e.message ?: "Ошибка") }
        }
    }

    fun returnLoan(loanId: Long) {
        viewModelScope.launch {
            loanRepository.returnBook(loanId)
                .onSuccess { showMessage("Книга принята") }
                .onFailure { e -> showError(e.message ?: "Ошибка") }
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            bookRepository.deleteBook(bookId)
            showMessage("Книга удалена из каталога")
        }
    }

    private fun showMessage(msg: String) {
        _message.update { msg }
        _error.update { null }
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _message.update { null }
        }
    }

    private fun showError(msg: String) {
        _error.update { msg }
        _message.update { null }
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _error.update { null }
        }
    }
}
