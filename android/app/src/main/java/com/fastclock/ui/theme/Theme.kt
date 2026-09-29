package com.fastclock.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Colors from CSS :root ──
val Bg = Color(0xFF0F1117)
val Surface = Color(0xFF1A1D27)
val Surface2 = Color(0xFF242836)
val Text = Color(0xFFE8E6E1)
val TextMuted = Color(0xFF8B8FA3)
val Accent = Color(0xFFF0A830)
val Accent2 = Color(0xFF3DD4B5)
val AccentDim = Color(0x26F0A830)
val Accent2Dim = Color(0x1F3DD4B5)
val RingTrack = Color(0xFF2A2E3D)
val RingTrackInner = Color(0xFF222633)

private val DarkColors = darkColorScheme(
    primary = Accent2,
    onPrimary = Color(0xFF0A1A16),
    primaryContainer = Accent2Dim,
    onPrimaryContainer = Accent2,
    secondary = Accent,
    onSecondary = Color(0xFF1A1200),
    secondaryContainer = AccentDim,
    onSecondaryContainer = Accent,
    background = Bg,
    onBackground = Text,
    surface = Surface,
    onSurface = Text,
    surfaceVariant = Surface2,
    onSurfaceVariant = TextMuted,
    outline = RingTrack
)

private val LightColors = lightColorScheme(
    primary = Accent2,
    secondary = Accent,
    background = Color(0xFFF5F3F0),
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF1A1D27),
    onSurface = Color(0xFF1A1D27)
)

@Composable
fun FastClockTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}