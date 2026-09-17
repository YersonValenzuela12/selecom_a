package com.example.selecom.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDBEAFE),
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = Ink700,
    onSecondary = Color.White,
    secondaryContainer = Ink100,
    onSecondaryContainer = Ink900,
    background = BackgroundLight,
    onBackground = Ink900,
    surface = SurfaceLight,
    onSurface = Ink900,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = Ink700,
    outline = Ink300
)

@Composable
fun SelecomTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
