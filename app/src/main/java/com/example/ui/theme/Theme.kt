package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = Color.Black,
    secondary = AccentGreen,
    onSecondary = Color.Black,
    tertiary = CoverCyberPink,
    background = DeepBlack,
    onBackground = TextPrimary,
    surface = PremiumCharcoal,
    onSurface = TextPrimary,
    surfaceVariant = MediumGray,
    onSurfaceVariant = TextSecondary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force premium JioSaavn Dark theme always
    dynamicColor: Boolean = false, // Use our handcrafted palette instead of system colors
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
