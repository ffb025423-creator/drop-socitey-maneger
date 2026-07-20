package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = PrimaryRed,
    secondary = CrimsonAccent,
    background = AppBackground,
    surface = SecondaryBackground,
    surfaceVariant = CardBackground,
    onPrimary = TextWhite,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextWhite,
    error = ErrorRed
  )

private val LightColorScheme =
  lightColorScheme(
    primary = PrimaryRed,
    secondary = CrimsonAccent,
    background = Color(0xFFFAFAFA),
    surface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFEBEBEB),
    onPrimary = Color.White,
    onBackground = Color(0xFF0F0F0F),
    onSurface = Color(0xFF0F0F0F),
    onSurfaceVariant = Color(0xFF333333),
    error = ErrorRed
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
