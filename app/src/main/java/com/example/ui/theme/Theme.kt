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

private val DarkColorScheme = darkColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.Black,
    primaryContainer = CardBgElevated,
    onPrimaryContainer = SaffronSecondary,
    secondary = SaffronSecondary,
    onSecondary = Color.Black,
    tertiary = GoldAccent,
    background = DarkBg,
    onBackground = TextPrimaryDark,
    surface = CardBg,
    onSurface = TextPrimaryDark,
    surfaceVariant = CardBgElevated,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = SaffronPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0CC),
    onPrimaryContainer = SaffronPrimary,
    secondary = SaffronSecondary,
    onSecondary = Color.White,
    tertiary = GoldAccent,
    background = LightBg,
    onBackground = TextPrimaryLight,
    surface = LightCard,
    onSurface = TextPrimaryLight,
    surfaceVariant = Color(0xFFF2ECE4),
    onSurfaceVariant = TextSecondaryLight
)

@Composable
fun LokVaaniTheme(
    darkTheme: Boolean = true, // Force dark theme by default for immersive visual premium look
    dynamicColor: Boolean = false, // Disable dynamic colors to keep our premium Saffron Sunset brand colors
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
