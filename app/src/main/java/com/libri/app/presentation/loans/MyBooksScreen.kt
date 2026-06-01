package com.libri.app.presentation.loans

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.libri.app.domain.model.Fine
import com.libri.app.domain.model.Loan
import com.libri.app.domain.model.Reservation
import com.libri.app.presentation.catalog.BookCoverPlaceholder
import com.libri.app.presentation.theme.Background
import com.libri.app.presentation.theme.ErrorColor
import com.libri.app.presentation.theme.OnBackground
import com.libri.app.presentation.theme.Primary
import com.libri.app.presentation.theme.StatusOverdueBg
import com.libri.app.presentation.theme.StatusReservedBg
import com.libri.app.presentation.theme.Surface
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBooksScreen(
    userId: Long,
    viewModel: LoansViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(userId) { viewModel.init(userId) }
    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Мои книги", style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Spacer(Modifier.height(4.dp))
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Background,
                contentColor = Primary,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Primary
                    )
                }
            ) {
                listOf("Выдачи", "Брони", "История").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> LoansList(
                        loans = uiState.activeLoans,
                        emptyText = "Нет активных выдач"
                    )
                    1 -> ReservationsList(
                        reservations = uiState.activeReservations,
                        onCancel = viewModel::cancelReservation,
                        emptyText = "Нет активных броней"
                    )
                    2 -> LoansList(
                        loans = uiState.loanHistory,
                        emptyText = "История пуста",
                        showPayFine = false,
                        fines = uiState.fines,
                        onPayFine = viewModel::payFine
                    )
                }
            }

            if (selectedTab == 0 && uiState.fines.any { !it.paid }) {
                FinesSection(
                    fines = uiState.fines.filter { !it.paid },
                    onPayFine = viewModel::payFine
                )
            }
        }
    }
}

@Composable
fun LoansList(
    loans: List<Loan>,
    emptyText: String,
    showPayFine: Boolean = false,
    fines: List<Fine> = emptyList(),
    onPayFine: (Long) -> Unit = {}
) {
    if (loans.isEmpty()) {
        EmptyState(emptyText)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(loans, key = { it.id }) { loan ->
                LoanCard(loan = loan)
            }
        }
    }
}

@Composable
fun LoanCard(loan: Loan) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))
    val borderColor = if (loan.isOverdue) ErrorColor else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(if (loan.isOverdue) 2.dp else 0.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (loan.isOverdue) StatusOverdueBg.copy(0.5f) else Surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BookCoverPlaceholder(title = loan.bookTitle, modifier = Modifier.size(48.dp, 60.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(loan.bookTitle, fontWeight = FontWeight.Bold, maxLines = 2)
                Text(
                    loan.bookAuthors.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = OnBackground.copy(0.6f)
                )
                Spacer(Modifier.height(4.dp))
                Text("Взята: ${loan.loanDate.format(formatter)}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Срок возврата: ${loan.dueDate.format(formatter)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (loan.isOverdue) ErrorColor else OnBackground.copy(0.7f)
                )
                if (loan.isOverdue) {
                    Text(
                        "Просрочено на ${loan.daysOverdue} дн.",
                        color = ErrorColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun ReservationsList(
    reservations: List<Reservation>,
    onCancel: (Long) -> Unit,
    emptyText: String
) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))

    if (reservations.isEmpty()) {
        EmptyState(emptyText)
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(reservations, key = { it.id }) { reservation ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = StatusReservedBg.copy(0.4f)),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BookCoverPlaceholder(title = reservation.bookTitle, modifier = Modifier.size(48.dp, 60.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reservation.bookTitle, fontWeight = FontWeight.Bold, maxLines = 2)
                            Text(reservation.bookAuthors.joinToString(", "),
                                style = MaterialTheme.typography.bodySmall, color = OnBackground.copy(0.6f))
                            Spacer(Modifier.height(4.dp))
                            Text("Забронировано: ${reservation.reservationDate.format(formatter)}",
                                style = MaterialTheme.typography.bodySmall)
                            Text("Истекает: ${reservation.expiryDate.format(formatter)}",
                                style = MaterialTheme.typography.bodySmall, color = Primary)
                        }
                        Button(
                            onClick = { onCancel(reservation.id) },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorColor),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) { Text("Отменить", fontSize = 12.sp) }
                    }
                }
            }
        }
    }
}

@Composable
fun FinesSection(fines: List<Fine>, onPayFine: (Long) -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(StatusOverdueBg.copy(0.3f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text("Неоплаченные штрафы", fontWeight = FontWeight.Bold, color = ErrorColor)
        Spacer(Modifier.height(8.dp))
        fines.forEach { fine ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(fine.reason, style = MaterialTheme.typography.bodyMedium)
                    Text(fine.calculatedDate.format(formatter), style = MaterialTheme.typography.bodySmall,
                        color = OnBackground.copy(0.6f))
                }
                Spacer(Modifier.width(8.dp))
                Text("${fine.amount.toInt()} руб.", fontWeight = FontWeight.Bold, color = ErrorColor)
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = { onPayFine(fine.id) },
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) { Text("Оплатить", fontSize = 12.sp) }
            }
        }
    }
}

@Composable
private fun EmptyState(text: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📚", fontSize = 48.sp)
            Spacer(Modifier.height(8.dp))
            Text(text, color = OnBackground.copy(0.6f))
        }
    }
}
