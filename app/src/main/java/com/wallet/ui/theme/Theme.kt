package com.wallet.ui.theme

import android.app.Activity
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.wallet.data.model.AppThemeColor
import com.wallet.data.model.FontScale

private val blackColorScheme = darkColorScheme(
    primary = Color(0xFFECECEC),
    onPrimary = Color(0xFF000000),
    primaryContainer = Color(0xFF1A1A1A),
    onPrimaryContainer = Color(0xFFF5F5F5),
    secondary = Color(0xFFB0B0B0),
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF141414),
    onSecondaryContainer = Color(0xFFE0E0E0),
    tertiary = Color(0xFF9E9E9E),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFF101010),
    onTertiaryContainer = Color(0xFFF0F0F0),
    background = Color(0xFF000000),
    onBackground = Color(0xFFE8E8E8),
    surface = Color(0xFF0A0A0A),
    onSurface = Color(0xFFE8E8E8),
    surfaceVariant = Color(0xFF161616),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF6E6E6E),
    outlineVariant = Color(0xFF3D3D3D),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

fun walletColorScheme(
    @Suppress("UNUSED_PARAMETER") themeColor: AppThemeColor = AppThemeColor.BLACK,
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true
): ColorScheme = blackColorScheme

@Composable
fun WalletTheme(
    @Suppress("UNUSED_PARAMETER") darkTheme: Boolean = true,
    fontScale: FontScale = FontScale.NORMAL,
    @Suppress("UNUSED_PARAMETER") themeColor: AppThemeColor = AppThemeColor.BLACK,
    content: @Composable () -> Unit
) {
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = blackColorScheme,
        typography = scaledTypography(fontScale),
        content = content
    )
}
