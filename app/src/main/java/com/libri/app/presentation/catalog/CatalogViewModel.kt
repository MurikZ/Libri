package com.libri.app.presentation.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.libri.app.data.entity.BookStatus
import com.libri.app.domain.model.Book
import com.libri.app.repository.BookRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CatalogFilter { ALL, AVAILABLE, ON_LOAN, RESERVED }

data class CatalogUiState(
    val books: List<Book> = emptyList(),
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val activeFilter: CatalogFilter = CatalogFilter.ALL,
    val message: String? = null
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val bookRepository: BookRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _filter = MutableStateFlow(CatalogFilter.ALL)
    private val _message = MutableStateFlow<String?>(null)

    private val _allBooks: StateFlow<List<Book>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) bookRepository.getAllBooks() else bookRepository.searchBooks(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val uiState: StateFlow<CatalogUiState> = combine(
        _allBooks, _filter, _searchQuery, _message
    ) { books, filter, query, message ->
        val filtered = when (filter) {
            CatalogFilter.ALL -> books
            CatalogFilter.AVAILABLE -> books.filter { it.status == BookStatus.AVAILABLE }
            CatalogFilter.ON_LOAN -> books.filter { it.status == BookStatus.ON_LOAN }
            CatalogFilter.RESERVED -> books.filter { it.status == BookStatus.RESERVED }
        }
        CatalogUiState(
            books = filtered,
            isLoading = false,
            searchQuery = query,
            activeFilter = filter,
            message = message
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CatalogUiState()
    )

    fun onSearchQueryChange(query: String) = _searchQuery.update { query }
    fun onFilterChange(filter: CatalogFilter) = _filter.update { filter }

    fun addBook(
        title: String, description: String?, year: Int, isbn: String,
        publisher: String?, authorsRaw: String, coverUri: String?, fragment: String?
    ) {
        viewModelScope.launch {
            val authors = authorsRaw.split(",")
                .map { it.trim().split(" ") }
                .filter { it.size >= 2 }
                .map { it[0] to it.drop(1).joinToString(" ") }
            runCatching {
                bookRepository.addBook(title, description, year, isbn, publisher, authors, coverUri, fragment)
            }.onSuccess { showMessage("Книга «$title» добавлена") }
             .onFailure { showMessage("Ошибка: ${it.message}") }
        }
    }

    fun deleteBook(bookId: Long) {
        viewModelScope.launch {
            bookRepository.deleteBook(bookId)
            showMessage("Книга удалена")
        }
    }

    fun showMessage(msg: String) {
        _message.update { msg }
        viewModelScope.launch {
            kotlinx.coroutines.delay(3000)
            _message.update { null }
        }
    }
}
