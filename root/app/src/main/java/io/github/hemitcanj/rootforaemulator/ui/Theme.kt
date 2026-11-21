package io.github.hemitcanj.rootforaemulator.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val RootGreen = Color(0xFF3DDC84)
val RootBg = Color(0xFF0B1210)
val RootSurface = Color(0xFF14201B)
val RootSurfaceHigh = Color(0xFF1E2E27)
val RootText = Color(0xFFE8F4EE)
val RootMuted = Color(0xFF8FA99D)
val RootYellow = Color(0xFFFFC857)
val RootRed = Color(0xFFFF6B6B)
val RootBlue = Color(0xFF7BD8FF)

private val RootColorScheme = darkColorScheme(
    primary = RootGreen,
    onPrimary = Color(0xFF00210F),
    primaryContainer = Color(0xFF0D3B25),
    onPrimaryContainer = RootGreen,
    secondary = RootBlue,
    onSecondary = Color(0xFF001E29),
    background = RootBg,
    onBackground = RootText,
    surface = RootSurface,
    onSurface = RootText,
    surfaceVariant = RootSurfaceHigh,
    onSurfaceVariant = RootMuted,
    outline = Color(0xFF385247),
    error = RootRed,
    onError = Color(0xFF2A0000)
)

@Composable
fun RootTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = RootColorScheme,
        typography = Typography(),
        content = content
    )
}
