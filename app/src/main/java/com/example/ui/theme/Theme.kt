package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val SleekColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF2A2D31),
    onPrimaryContainer = Color(0xFFD0BCFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    secondaryContainer = Color(0xFF44474E),
    onSecondaryContainer = Color(0xFFD0BCFF),
    tertiary = Color(0xFFEFB8C8),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE2E2E6),
    surface = Color(0xFF1B1B1F),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2A2D31),
    onSurfaceVariant = Color(0xFFC4C6D0),
    outline = Color(0xFF44474E),
    outlineVariant = Color(0xFF35363A)
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF625B71),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE8DDFF),
    onPrimaryContainer = Color(0xFF1D0061),
    secondary = Color(0xFF625B71),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE8DDFF),
    onSecondaryContainer = Color(0xFF1D0061),
    tertiary = Color(0xFF7D5260),
    background = Color(0xFFFDFBFF),
    onBackground = Color(0xFF1B1B1F),
    surface = Color(0xFFFDFBFF),
    onSurface = Color(0xFF1B1B1F),
    surfaceVariant = Color(0xFFE2E1EC),
    onSurfaceVariant = Color(0xFF45464F),
    outline = Color(0xFF767680),
    outlineVariant = Color(0xFFC7C5D0)
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Default to true to enforce the beautiful Sleek Interface dark theme!
  dynamicColor: Boolean = false, // Set to false to prioritize our custom design color scheme
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) SleekColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
