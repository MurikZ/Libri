package com.libri.app.presentation.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.libri.app.data.entity.BookStatus
import com.libri.app.data.entity.ReservationStatus
import com.libri.app.data.entity.UserRole
import com.libri.app.presentation.auth.textFieldColors
import com.libri.app.presentation.theme.Background
import com.libri.app.presentation.theme.ErrorColor
import com.libri.app.presentation.theme.OnBackground
import com.libri.app.presentation.theme.OnPrimary
import com.libri.app.presentation.theme.Primary
import com.libri.app.presentation.theme.PrimaryVariant
import com.libri.app.presentation.theme.Surface as ThemeSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookDetailScreen(
    bookId: Long,
    userId: Long,
    userRole: UserRole,
    navController: NavController,
    viewModel: BookDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showFragment by remember { mutableStateOf(false) }
    var showAddInstance by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var inventoryNumber by remember { mutableStateOf("") }
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(bookId) { viewModel.loadBook(bookId, userId) }
    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) navController.popBackStack()
    }
    LaunchedEffect(uiState.message, uiState.error) {
        (uiState.message ?: uiState.error)?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.book?.title ?: "Книга", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            val book = uiState.book
            if (book == null) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Книга не найдена", color = OnBackground.copy(0.6f))
                }
            } else {
                val isAdmin = userRole != UserRole.READER
                val hasFragment = !book.fragment.isNullOrBlank()
                val hasReaderAction = userRole == UserRole.READER &&
                        (book.status == BookStatus.AVAILABLE && uiState.reservation == null ||
                                uiState.reservation?.status == ReservationStatus.ACTIVE)
                val showBottomBar = hasFragment || hasReaderAction || isAdmin

                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = if (showBottomBar) 88.dp else 16.dp)
                    ) {
                        // Cover
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .background(Brush.verticalGradient(listOf(PrimaryVariant, Primary)))
                        ) {
                            if (!book.coverImageUri.isNullOrBlank()) {
                                AsyncImage(
                                    model = book.coverImageUri,
                                    contentDescription = book.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.45f))
                                            )
                                        )
                                )
                            } else {
                                Column(
                                    modifier = Modifier.align(Alignment.Center),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = book.title.firstOrNull()?.uppercase() ?: "?",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 64.sp
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = book.authors.firstOrNull() ?: "",
                                        color = Color.White.copy(0.8f),
                                        fontSize = 13.sp
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                            ) {
                                BookStatusChip(status = book.status)
                            }
                        }

                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = book.authors.joinToString(", "),
                                style = MaterialTheme.typography.bodyLarge,
                                color = Primary
                            )

                            Spacer(Modifier.height(20.dp))
                            Divider(color = OnBackground.copy(0.1f))
                            Spacer(Modifier.height(16.dp))

                            InfoRow("Год издания", book.publicationYear.toString())
                            book.publisher?.let { InfoRow("Издательство", it) }
                            InfoRow("ISBN", book.isbn)
                            InfoRow("Доступно", "${book.availableInstances} из ${book.totalInstances}")

                            // Instances list for admin
                            if (isAdmin && uiState.instances.isNotEmpty()) {
                                Spacer(Modifier.height(20.dp))
                                Divider(color = OnBackground.copy(0.1f))
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "Экземпляры",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OnBackground.copy(0.6f)
                                )
                                Spacer(Modifier.height(6.dp))
                                uiState.instances.forEach { inst ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            inst.inventoryNumber,
                                            style = MaterialTheme.typography.bodySmall,
                                            modifier = Modifier.weight(1f)
                                        )
                                        val (statusColor, statusLabel) = when (inst.status) {
                                            BookStatus.AVAILABLE -> Primary to "Доступен"
                                            BookStatus.ON_LOAN -> ErrorColor to "Выдан"
                                            BookStatus.RESERVED -> OnBackground.copy(0.5f) to "Забронирован"
                                            else -> OnBackground.copy(0.4f) to inst.status.name
                                        }
                                        Text(statusLabel, style = MaterialTheme.typography.bodySmall, color = statusColor)
                                        if (inst.status == BookStatus.AVAILABLE) {
                                            IconButton(
                                                onClick = { viewModel.deleteInstance(inst.id, bookId) },
                                                colors = IconButtonDefaults.iconButtonColors(contentColor = ErrorColor)
                                            ) {
                                                Icon(Icons.Default.Delete, contentDescription = "Удалить экземпляр")
                                            }
                                        }
                                    }
                                }
                            }

                            book.description?.let { desc ->
                                Spacer(Modifier.height(20.dp))
                                Divider(color = OnBackground.copy(0.1f))
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "Описание",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = OnBackground.copy(0.85f),
                                    lineHeight = 22.sp
                                )
                            }
                        }
                    }

                    // Sticky bottom bar
                    if (showBottomBar) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                            color = Background,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                                    .navigationBarsPadding(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                if (hasFragment) {
                                    OutlinedButton(
                                        onClick = { showFragment = true },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("Читать фрагмент", color = Primary)
                                    }
                                }
                                if (isAdmin) {
                                    OutlinedButton(
                                        onClick = { showAddInstance = true },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text("+ Экземпляр", color = Primary)
                                    }
                                    Button(
                                        onClick = { showDeleteConfirm = true },
                                        modifier = Modifier.weight(1f).height(52.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = ErrorColor, contentColor = Color.White)
                                    ) {
                                        Text("Удалить", color = Color.White)
                                    }
                                } else {
                                    when {
                                        hasReaderAction && book.status == BookStatus.AVAILABLE && uiState.reservation == null -> {
                                            Button(
                                                onClick = { viewModel.reserve(userId, bookId) },
                                                modifier = Modifier.weight(1f).height(52.dp),
                                                shape = RoundedCornerShape(12.dp),
                                                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                                            ) {
                                                Text("Забронировать", fontWeight = FontWeight.SemiBold, color = Color.White)
                                            }
                                        }
                                        hasReaderAction && uiState.reservation?.status == ReservationStatus.ACTIVE -> {
                                            OutlinedButton(
                                                onClick = { viewModel.cancelReservation(userId, bookId) },
                                                modifier = Modifier.weight(1f).height(52.dp),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Text("Отменить бронь", color = ErrorColor)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add instance dialog
    if (showAddInstance) {
        AlertDialog(
            onDismissRequest = { showAddInstance = false; inventoryNumber = "" },
            title = { Text("Добавить экземпляр") },
            text = {
                OutlinedTextField(
                    value = inventoryNumber,
                    onValueChange = { inventoryNumber = it },
                    label = { Text("Инвентарный номер") },
                    placeholder = { Text("INV-001") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inventoryNumber.isNotBlank()) {
                            viewModel.addInstance(bookId, inventoryNumber)
                            inventoryNumber = ""
                            showAddInstance = false
                        }
                    },
                    enabled = inventoryNumber.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = Color.White)
                ) { Text("Добавить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showAddInstance = false; inventoryNumber = "" }) {
                    Text("Отмена", color = Primary)
                }
            }
        )
    }

    // Delete confirmation dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Удалить книгу?") },
            text = {
                Text(
                    "«${uiState.book?.title}» будет удалена вместе со всеми экземплярами. Это действие нельзя отменить.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBook(bookId)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorColor, contentColor = Color.White)
                ) { Text("Удалить", color = Color.White) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Отмена", color = Primary) }
            }
        )
    }

    // Fragment bottom sheet
    if (showFragment && uiState.book?.fragment != null) {
        ModalBottomSheet(
            onDismissRequest = { showFragment = false },
            sheetState = bottomSheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(start = 24.dp, end = 24.dp, bottom = 48.dp)
            ) {
                Text(
                    "Фрагмент",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    uiState.book!!.fragment!!,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 24.sp,
                    color = OnBackground
                )
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = OnBackground.copy(0.55f))
        Spacer(Modifier.width(16.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
