@file:OptIn(ExperimentalMaterial3Api::class)

package com.kiprono.mamambogaqrapp.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

// 1. Theme State Manager
object AppThemeState {
    // Defaulting to false (light mode) but can be toggled by the user.
    val isDark = mutableStateOf(false)
}

// 2. Color Schemes (Customized for SmartRider)
// Using your Green/Cyan scheme:

// Light Color Scheme
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4CAF50), // Brighter, primary green
    secondary = Color(0xFF00ACC1), // Cyan secondary color
    tertiary = Color(0xFFA5D6A7),
    background = Color(0xFFF7F7F7),
    surface = Color.White,
    onPrimary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black
)

// Dark Color Scheme
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF66BB6A), // A bright, energetic green
    secondary = Color(0xFF4DB6AC), // Complementary teal
    tertiary = Color(0xFFC8E6C9),
    background = Color(0xFF121212),
    surface = Color(0xFF1E1E1E),
    onPrimary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White
)

// 3. App Theme Composable (Renamed to SmartRiderTheme)
@Composable
fun SmartRiderTheme(
    // If AppThemeState.isDark is true, use darkTheme; otherwise, check system preference.
    darkTheme: Boolean = AppThemeState.isDark.value || isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    // Assuming default Material 3 Typography for simplicity.
    val AppTypography = Typography()

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}