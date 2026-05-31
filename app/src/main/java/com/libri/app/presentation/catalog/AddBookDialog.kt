package com.libri.app.presentation.catalog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.libri.app.presentation.auth.textFieldColors
import com.libri.app.presentation.theme.ErrorColor
import com.libri.app.presentation.theme.Primary

@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Int, String, String?, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var yearStr by remember { mutableStateOf("") }
    var isbn by remember { mutableStateOf("") }
    var publisher by remember { mutableStateOf("") }
    var authorsRaw by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить книгу", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                            title, description.takeIf { it.isNotBlank() },
                            year, isbn, publisher.takeIf { it.isNotBlank() }, authorsRaw
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) { Text("Добавить") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Отмена", color = Primary) }
        }
    )
}
