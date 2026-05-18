package com.example.mototrack2java.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightPalette = lightColors(
    primary = Color(0xFF034E81),
    primaryVariant = Color(0xFF013758),
    secondary = Color(0xFFE6795B),
    background = Color(0xFFF7F9FB),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFF18232E),
    onSurface = Color(0xFF18232E)
)

private val DarkPalette = darkColors(
    primary = Color(0xFF4B9CD5),
    primaryVariant = Color(0xFF1F6A9C),
    secondary = Color(0xFFE6795B),
    background = Color(0xFF101820),
    surface = Color(0xFF17212B),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color(0xFFE7EEF5),
    onSurface = Color(0xFFE7EEF5)
)

@Composable
fun MotoTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colors = if (darkTheme) DarkPalette else LightPalette,
        content = content
    )
}

val Colors.success: Color
    get() = Color(0xFF2E7D32)
