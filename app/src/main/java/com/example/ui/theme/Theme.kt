package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = MinecraftLightGreen,
    secondary = MinecraftDiamond,
    tertiary = MinecraftGold,
    background = MinecraftDarkBg,
    surface = MinecraftCardBg,
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MinecraftGreen,
    secondary = MinecraftDiamond,
    tertiary = MinecraftGold,
    background = Color(0xFFF1F3F4),
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1E1F22),
    onSurface = Color(0xFF1E1F22)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Minecraft immersive dark theme
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
