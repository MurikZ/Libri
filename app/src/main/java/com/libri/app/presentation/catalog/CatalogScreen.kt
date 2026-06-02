package com.libri.app.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.libri.app.data.entity.BookStatus
import com.libri.app.data.entity.UserRole
import com.libri.app.domain.model.Book
import com.libri.app.presentation.auth.textFieldColors
import com.libri.app.presentation.theme.Background
import com.libri.app.presentation.theme.OnBackground
import com.libri.app.presentation.theme.Primary
import com.libri.app.presentation.theme.StatusAvailableBg
import com.libri.app.presentation.theme.StatusAvailableText
import com.libri.app.presentation.theme.StatusOnLoanBg
import com.libri.app.presentation.theme.StatusOnLoanText
import com.libri.app.presentation.theme.StatusOverdueBg
import com.libri.app.presentation.theme.StatusOverdueText
import com.libri.app.presentation.theme.StatusReservedBg
import com.libri.app.presentation.theme.StatusReservedText
import com.libri.app.presentation.theme.Surface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogScreen(
    navController: NavController,
    userId: Long,
    userRole: UserRole,
    viewModel: CatalogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Каталог книг", style = MaterialTheme.typography.titleLarge)
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        floatingActionButton = {
            if (userRole != UserRole.READER) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = Primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Добавить книгу", tint = Color.White)
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Поиск по названию...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = Primary) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = textFieldColors()
            )

            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    CatalogFilter.ALL to "Все",
                    CatalogFilter.AVAILABLE to "Доступные",
                    CatalogFilter.ON_LOAN to "Выданные",
                    CatalogFilter.RESERVED to "Забронированные"
                )
                items(filters) { (filter, label) ->
                    FilterChip(
                        selected = uiState.activeFilter == filter,
                        onClick = { viewModel.onFilterChange(filter) },
                        label = { Text(label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Primary)
                }
            } else if (uiState.books.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📚", fontSize = 48.sp)
                        Spacer(Modifier.height(8.dp))
                        Text("Книг не найдено", color = OnBackground.copy(alpha = 0.6f))
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.books, key = { it.id }) { book ->
                        BookCard(
                            book = book,
                            onClick = { navController.navigate("book_detail/${book.id}") }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, desc, year, isbn, publisher, authorsRaw, coverUri, fragment ->
                viewModel.addBook(title, desc, year, isbn, publisher, authorsRaw, coverUri, fragment)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun BookCard(book: Book, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCoverPlaceholder(title = book.title, coverUri = book.coverImageUri, modifier = Modifier.size(56.dp, 72.dp))

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = book.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.authors.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground.copy(alpha = 0.7f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = book.publicationYear.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground.copy(alpha = 0.5f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                BookStatusChip(status = book.status)
            }
        }
    }
}

@Composable
fun BookCoverPlaceholder(title: String, coverUri: String? = null, modifier: Modifier = Modifier) {
    if (!coverUri.isNullOrBlank()) {
        AsyncImage(
            model = coverUri,
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = modifier.clip(RoundedCornerShape(6.dp))
        )
        return
    }

    val colors = listOf(
        listOf(Color(0xFF8B7355), Color(0xFFA0522D)),
        listOf(Color(0xFF6D5A42), Color(0xFF8B7355)),
        listOf(Color(0xFF7B6B4A), Color(0xFF9B8B6A)),
        listOf(Color(0xFF5C4A32), Color(0xFF7B6250))
    )
    val palette = colors[title.hashCode().and(0x7FFFFFFF) % colors.size]

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Brush.verticalGradient(palette)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title.firstOrNull()?.uppercase() ?: "?",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp
        )
    }
}

@Composable
fun BookStatusChip(status: BookStatus) {
    val (bg, textColor, label) = when (status) {
        BookStatus.AVAILABLE -> Triple(StatusAvailableBg, StatusAvailableText, "Доступна")
        BookStatus.ON_LOAN -> Triple(StatusOnLoanBg, StatusOnLoanText, "Выдана")
        BookStatus.RESERVED -> Triple(StatusReservedBg, StatusReservedText, "Забронирована")
        BookStatus.DAMAGED -> Triple(StatusOverdueBg, StatusOverdueText, "Повреждена")
        BookStatus.LOST -> Triple(StatusOverdueBg, StatusOverdueText, "Утеряна")
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(label, color = textColor, fontSize = 11.sp, fontWeight = FontWeight.Medium)
    }
}
