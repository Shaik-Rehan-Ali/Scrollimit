package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = WarmWoodGold,
    onPrimary = Color(0xFF1E1400),
    primaryContainer = Color(0xFF5A4004),
    onPrimaryContainer = Color(0xFFFBE1AB),
    secondary = ForestSage,
    onSecondary = Color(0xFF152A01),
    tertiary = SoftTeal,
    onTertiary = Color(0xFF001F1C),
    background = CozyObsidianBg,
    onBackground = Color(0xFFEAF0F6),
    surface = CharcoalCard,
    onSurface = Color(0xFFEAF0F6),
    surfaceVariant = Color(0xFF2C313E),
    onSurfaceVariant = Color(0xFFCCD2DF),
    error = SoftTerracotta,
    onError = Color(0xFF3F0A00)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF7E5704),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6E2C5),
    onPrimaryContainer = Color(0xFF321E01),
    secondary = ForestSage,
    onSecondary = Color.White,
    tertiary = SoftTeal,
    background = Color(0xFFFAF7F0),
    onBackground = Color(0xFF1E1B15),
    surface = Color.White,
    onSurface = Color(0xFF1E1B15),
    surfaceVariant = Color(0xFFECE1D3),
    onSurfaceVariant = Color(0xFF4E453A),
    error = SoftTerracotta,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Allow forcing custom Cozy vibe
    forceCustomTheme: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme = if (forceCustomTheme) {
        if (darkTheme) DarkColorScheme else LightColorScheme
    } else {
        if (darkTheme) DarkColorScheme else LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
