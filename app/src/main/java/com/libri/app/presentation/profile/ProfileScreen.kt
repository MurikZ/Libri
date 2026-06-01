package com.libri.app.presentation.profile

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.libri.app.data.entity.UserRole
import com.libri.app.domain.model.Fine
import com.libri.app.presentation.theme.Background
import com.libri.app.presentation.theme.ErrorColor
import com.libri.app.presentation.theme.LogoutColor
import com.libri.app.presentation.theme.OnBackground
import com.libri.app.presentation.theme.Primary
import com.libri.app.presentation.theme.StatusAvailableBg
import com.libri.app.presentation.theme.StatusAvailableText
import com.libri.app.presentation.theme.StatusOnLoanBg
import com.libri.app.presentation.theme.StatusOnLoanText
import com.libri.app.presentation.theme.StatusReservedBg
import com.libri.app.presentation.theme.StatusReservedText
import com.libri.app.presentation.theme.Surface
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    userId: Long,
    onLogout: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale("ru"))

    LaunchedEffect(userId) { viewModel.init(userId) }
    LaunchedEffect(uiState.message) {
        uiState.message?.let { snackbarHostState.showSnackbar(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Профиль", style = MaterialTheme.typography.titleLarge) },
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (uiState.isOffline) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.WifiOff, contentDescription = null, tint = Color(0xFFE65100))
                            Text(
                                "Аккаунт создан оффлайн и не синхронизирован с сервером.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFE65100)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                uiState.user?.let { user ->
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user.initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp
                        )
                    }

                    Spacer(Modifier.height(12.dp))
                    Text(user.fullName, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(user.email, style = MaterialTheme.typography.bodyMedium, color = OnBackground.copy(0.6f))

                    Spacer(Modifier.height(8.dp))

                    val (roleBg, roleTextColor, roleLabel) = when (user.role) {
                        UserRole.READER -> Triple(StatusAvailableBg, StatusAvailableText, "Читатель")
                        UserRole.LIBRARIAN -> Triple(StatusOnLoanBg, StatusOnLoanText, "Библиотекарь")
                        UserRole.ADMIN -> Triple(StatusReservedBg, StatusReservedText, "Администратор")
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(roleBg)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(roleLabel, color = roleTextColor, fontWeight = FontWeight.Medium)
                    }

                    Spacer(Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            ProfileInfoRow("Дата регистрации", user.registrationDate.format(formatter))
                            user.phone?.let { ProfileInfoRow("Телефон", it) }
                            user.city?.let { ProfileInfoRow("Город", it) }
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }

                if (uiState.fines.isNotEmpty()) {
                    Text(
                        "Штрафы",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Spacer(Modifier.height(8.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Surface),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            uiState.fines.forEachIndexed { index, fine ->
                                FineRow(fine = fine, onPay = { viewModel.payFine(fine.id) })
                                if (index < uiState.fines.lastIndex) Divider(color = OnBackground.copy(0.1f))
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = LogoutColor, contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Выйти", fontWeight = FontWeight.SemiBold)
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = OnBackground.copy(0.6f))
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FineRow(fine: Fine, onPay: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale("ru"))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(fine.reason, style = MaterialTheme.typography.bodyMedium)
            Text(
                fine.calculatedDate.format(formatter),
                style = MaterialTheme.typography.bodySmall,
                color = OnBackground.copy(0.6f)
            )
        }
        Text(
            "${fine.amount.toInt()} руб.",
            fontWeight = FontWeight.Bold,
            color = if (fine.paid) OnBackground.copy(0.5f) else ErrorColor
        )
        Spacer(Modifier.width(8.dp))
        if (!fine.paid) {
            TextButton(
                onClick = onPay,
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) { Text("Оплатить", color = Primary, fontSize = 12.sp) }
        } else {
            Text("Оплачен", color = OnBackground.copy(0.4f), fontSize = 12.sp)
        }
    }
}
