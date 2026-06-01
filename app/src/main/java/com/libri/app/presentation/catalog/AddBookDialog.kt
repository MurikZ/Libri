package com.libri.app.presentation.catalog

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.libri.app.presentation.auth.textFieldColors
import com.libri.app.presentation.theme.ErrorColor
import com.libri.app.presentation.theme.OnPrimary
import com.libri.app.presentation.theme.Primary
import com.libri.app.presentation.theme.Surface

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?, year: Int, isbn: String, publisher: String?, authorsRaw: String, coverUri: String?, fragment: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var yearStr by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var authorsRaw by remember { mutableStateOf("") }
    var fragment by remember { mutableStateOf("") }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri -> coverUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить книгу", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Cover picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Primary.copy(0.4f), RoundedCornerShape(12.dp))
                        .background(Surface)
                        .clickable { imagePicker.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (coverUri != null) {
                        AsyncImage(
                            model = coverUri,
                            contentDescription = "Обложка",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, null, tint = Primary)
                            Text("Выбрать обложку", color = Primary, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                OutlinedTextField(
                    value = title, onValueChange = { title = it; error = null },
                    label = { Text("Название *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = authorsRaw, onValueChange = { authorsRaw = it },
                    label = { Text("Авторы (через запятую) *") },
                    placeholder = { Text("Иван Иванов, Пётр Петров") },
                    singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp), colors = textFieldColors()
                )
                OutlinedTextField(
                    value = yearStr, onValueChange = { yearStr = it },
                    label = { Text("Год издания *") }, singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = isbn, onValueChange = { isbn = it },
                    label = { Text("ISBN *") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = publisher, onValueChange = { publisher = it },
                    label = { Text("Издательство") }, singleLine = true,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = description, onValueChange = { description = it },
                    label = { Text("Описание") }, maxLines = 3,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                OutlinedTextField(
                    value = fragment, onValueChange = { fragment = it },
                    label = { Text("Фрагмент книги") },
                    placeholder = { Text("Вставьте отрывок из книги...") },
                    maxLines = 5,
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                    colors = textFieldColors()
                )
                error?.let { Text(it, color = ErrorColor, style = MaterialTheme.typography.bodySmall) }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val year = yearStr.toIntOrNull()
                    when {
                        title.isBlank() -> error = "Введите название"
                        authorsRaw.isBlank() -> error = "Введите авторов"
                        year == null -> error = "Некорректный год"
                        isbn.isBlank() -> error = "Введите ISBN"
                        else -> onConfirm(
                            title,
                            description.takeIf { it.isNotBlank() },
                            year, isbn,
                            publisher.takeIf { it.isNotBlank() },
                            authorsRaw,
                            coverUri?.toString(),
                            fragment.takeIf { it.isNotBlank() }
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary)
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Primary) }
        }
    )
}
