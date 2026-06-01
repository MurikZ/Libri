package com.libri.app.presentation.librarian

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.libri.app.domain.model.Loan
import com.libri.app.presentation.catalog.BookCoverPlaceholder
import com.libri.app.presentation.theme.Background
import com.libri.app.presentation.theme.ErrorColor
import com.libri.app.presentation.theme.OnBackground
import com.libri.app.presentation.theme.Primary
import com.libri.app.presentation.theme.StatusOverdueBg
import com.libri.app.presentation.theme.Surface
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibrarianScreen(
    currentUserId: Long,
    viewModel: LibrarianViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.message, uiState.error) {
        (uiState.message ?: uiState.error)?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Управление", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { IssueLoanSection(viewModel) }
            item { ReturnLoanSection(loans = uiState.activeLoans, onReturn = viewModel::returnLoan) }
            item { CatalogManagementSection(books = uiState.books.take(20), onDelete = viewModel::deleteBook) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IssueLoanSection(viewModel: LibrarianViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var readerExpanded by remember { mutableStateOf(false) }
    var bookExpanded by remember { mutableStateOf(false) }
    var selectedReaderId by remember { mutableLongStateOf(0L) }
    var selectedBookId by remember { mutableLongStateOf(0L) }
    var selectedReaderName by remember { mutableStateOf("Выберите читателя") }
    var selectedBookTitle by remember { mutableStateOf("Выберите книгу") }
    var selectedDays by remember { mutableIntStateOf(30) }

    SectionCard(title = "Выдача книги") {
        ExposedDropdownMenuBox(
            expanded = readerExpanded,
            onExpandedChange = { readerExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedReaderName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Читатель") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(readerExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = readerExpanded, onDismissRequest = { readerExpanded = false }) {
                uiState.readers.forEach { reader ->
                    DropdownMenuItem(
                        text = { Text("${reader.fullName} (${reader.email})") },
                        onClick = {
                            selectedReaderId = reader.id
                            selectedReaderName = reader.fullName
                            readerExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        ExposedDropdownMenuBox(
            expanded = bookExpanded,
            onExpandedChange = { bookExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedBookTitle,
                onValueChange = {},
                readOnly = true,
                label = { Text("Книга") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(bookExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                shape = RoundedCornerShape(12.dp)
            )
            ExposedDropdownMenu(expanded = bookExpanded, onDismissRequest = { bookExpanded = false }) {
                uiState.books.filter { it.availableInstances > 0 }.forEach { book ->
                    DropdownMenuItem(
                        text = { Text("${book.title} (доступно: ${book.availableInstances})") },
                        onClick = {
                            selectedBookId = book.id
                            selectedBookTitle = book.title
                            bookExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        Text("Срок выдачи", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(7, 14, 21, 30).forEach { days ->
                FilterChip(
                    selected = selectedDays == days,
                    onClick = { selectedDays = days },
                    label = { Text("$days дн.") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Primary,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    )
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (selectedReaderId > 0 && selectedBookId > 0) {
                    viewModel.issueLoan(selectedReaderId, selectedBookId, selectedDays)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = androidx.compose.ui.graphics.Color.White),
            enabled = selectedReaderId > 0 && selectedBookId > 0
        ) {
            Text("Выдать книгу", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun ReturnLoanSection(loans: List<Loan>, onReturn: (Long) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))

    SectionCard(title = "Приём возврата (${loans.size} активных)") {
        if (loans.isEmpty()) {
            Text("Нет активных выдач", color = OnBackground.copy(0.5f), modifier = Modifier.padding(8.dp))
        } else {
            loans.take(10).forEach { loan ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .background(
                            if (loan.isOverdue) StatusOverdueBg.copy(0.3f) else Surface,
                            RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(loan.bookTitle, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            "Срок: ${loan.dueDate.format(formatter)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (loan.isOverdue) ErrorColor else OnBackground.copy(0.6f)
                        )
                    }
                    Button(
                        onClick = { onReturn(loan.id) },
                        colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = androidx.compose.ui.graphics.Color.White),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) { Text("Вернуть", fontSize = 12.sp,color = Color.White) }
                }
            }
        }
    }
}

@Composable
fun CatalogManagementSection(books: List<com.libri.app.domain.model.Book>, onDelete: (Long) -> Unit) {
    SectionCard(title = "Каталог (${books.size} книг)") {
        books.forEach { book ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BookCoverPlaceholder(title = book.title, modifier = Modifier.size(40.dp, 52.dp))
                Spacer(Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(book.title, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(book.authors.joinToString(", "), style = MaterialTheme.typography.bodySmall,
                        color = OnBackground.copy(0.6f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = { onDelete(book.id) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = ErrorColor)
                }
            }
        }
    }
}

@Composable
fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}
