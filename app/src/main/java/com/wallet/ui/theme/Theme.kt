package com.wallet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.wallet.data.model.FontScale

private val DarkColorScheme = darkColorScheme(
    primary = Green80,
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Green80,
    secondary = GreenGrey80,
    tertiary = Teal80,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = Color(0xFF2A2A2A),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8)
)

private val LightColorScheme = lightColorScheme(
    primary = Green40,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF1B5E20),
    secondary = GreenGrey40,
    tertiary = Teal40,
    background = LightBackground,
    surface = Color.White,
    surfaceVariant = Color(0xFFE8F0E9),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F)
)

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: FontScale = FontScale.NORMAL,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography(fontScale),
        content = content
    )
}
