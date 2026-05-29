package com.gnome.launcher.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// GNOME color palette
object GnomeColors {
    val Background = Color(0xFF1E1E2E)         // Dark base
    val Surface = Color(0xFF313244)             // Card/panel surface
    val SurfaceVariant = Color(0xFF45475A)      // Elevated surface
    val TopBar = Color(0xFF11111B)              // Top bar (near-black)
    val Accent = Color(0xFF89B4FA)              // GNOME blue accent
    val AccentSecondary = Color(0xFFCBA6F7)     // Purple accent
    val TextPrimary = Color(0xFFCDD6F4)         // Main text
    val TextSecondary = Color(0xFFA6ADC8)       // Subtitle text
    val TextDisabled = Color(0xFF585B70)        // Disabled text
    val Divider = Color(0xFF45475A)
    val SearchBar = Color(0xFF313244)
    val Overlay = Color(0x99000000)
    val IconOverlay = Color(0x22CDD6F4)
}

private val GnomeDarkColorScheme = darkColorScheme(
    primary = GnomeColors.Accent,
    secondary = GnomeColors.AccentSecondary,
    background = GnomeColors.Background,
    surface = GnomeColors.Surface,
    surfaceVariant = GnomeColors.SurfaceVariant,
    onPrimary = Color(0xFF1E1E2E),
    onBackground = GnomeColors.TextPrimary,
    onSurface = GnomeColors.TextPrimary,
    onSurfaceVariant = GnomeColors.TextSecondary,
)

@Composable
fun GnomeLauncherTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GnomeDarkColorScheme,
        content = content
    )
}
