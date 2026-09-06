package com.kickclip.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = AccentCoral,
    secondary = AccentCyan,
    background = DarkBackdrop,
    surface = SheetSurface,
    surfaceVariant = InputSurface,
    onPrimary = TextPrimary,
    onSecondary = DarkBackdrop,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)

@Composable
fun KickclipTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
