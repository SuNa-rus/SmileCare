package com.example.dental.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

// Медицинская палитра: глубокий синий, бледно-голубой фон, мятный акцент
val Blue = Color(0xFF0B4FB3)
val BlueDark = Color(0xFF083A87)
val Sky = Color(0xFFDCEAFB)
val BgColor = Color(0xFFEAF2FC)
val Mint = Color(0xFF1EE3B0)
val MintDark = Color(0xFF0FA97F)
val Ink = Color(0xFF10294D)
val Muted = Color(0xFF7B8CA6)
val Coral = Color(0xFFFF5C73)
val Amber = Color(0xFFF5A524)
val Violet = Color(0xFF7C5CFF)

private val Scheme = lightColorScheme(
    primary = Blue, onPrimary = Color.White, secondary = Mint, onSecondary = Ink,
    background = BgColor, onBackground = Ink, surface = Color.White, onSurface = Ink,
    surfaceVariant = Sky, onSurfaceVariant = Muted, outline = Color(0xFFC5D5EA),
    primaryContainer = Sky, onPrimaryContainer = Blue, error = Coral,
    surfaceContainerLowest = Color.White, surfaceContainerLow = Color.White, surfaceContainer = Color.White,
    surfaceContainerHigh = Color.White, surfaceContainerHighest = Sky
)

@Composable
fun DentalTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = Scheme) {
        CompositionLocalProvider(LocalContentColor provides Ink, content = content)
    }
}
