package com.libri.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LibriColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    secondary = Secondary,
    onSecondary = OnBackground,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnBackground,
    error = ErrorColor,
    onError = OnPrimary,
    surfaceVariant = Surface,
    onSurfaceVariant = OnBackground
)

@Composable
fun LibriTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LibriColorScheme,
        typography = Typography,
        content = content
    )
}
