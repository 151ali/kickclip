package com.kickclip.app.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Dark Theme Color Tokens
val DarkBackdrop = Color(0xFF050506)
val SheetSurface = Color(0xFF141416)
val InputSurface = Color(0xFF1C1C20)
val CardBorder = Color(0xFF26262D)

// Accent Colors
val AccentCoral = Color(0xFFFF5A36)
val AccentCyan = Color(0xFF34E4D0)

// Text Colors
val TextPrimary = Color(0xFFF4F4F6)
val TextSecondary = Color(0xFF8E8E93)
val TextPlaceholder = Color(0xFF5A5A65)
val TextError = Color(0xFFFF453A)

// Accent Gradient for handle bar and status badge
val AccentGradient = Brush.horizontalGradient(
    colors = listOf(AccentCoral, AccentCyan)
)
