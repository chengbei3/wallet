package com.wallet.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.wallet.data.model.AppThemeColor
import com.wallet.data.model.FontScale

private val sharedDarkBase = darkColorScheme(
    onPrimary = Color(0xFF003910),
    onSecondary = Color(0xFF1B3321),
    onTertiary = Color(0xFF003731),
    onBackground = Color(0xFFE8E8E8),
    onSurface = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFFBDBDBD),
    outline = Color(0xFF6E6E6E),
    outlineVariant = Color(0xFF3D3D3D),
    error = Color(0xFFCF6679),
    onError = Color(0xFF000000)
)

private val sharedLightBase = lightColorScheme(
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    outlineVariant = Color(0xFFCAC4D0),
    error = Color(0xFFB3261E),
    onError = Color.White
)

fun walletColorScheme(themeColor: AppThemeColor, darkTheme: Boolean): ColorScheme {
    return if (darkTheme) darkSchemeFor(themeColor) else lightSchemeFor(themeColor)
}

fun themePreviewColor(themeColor: AppThemeColor): Color = when (themeColor) {
    AppThemeColor.WHITE -> Color(0xFFFAFAFA)
    AppThemeColor.BLACK -> Color(0xFF121212)
    AppThemeColor.GREEN -> Color(0xFF2E7D32)
    AppThemeColor.BLUE -> Color(0xFF1565C0)
}

private fun lightSchemeFor(themeColor: AppThemeColor): ColorScheme = when (themeColor) {
    AppThemeColor.WHITE -> sharedLightBase.copy(
        primary = Color(0xFF212121),
        primaryContainer = Color(0xFFEEEEEE),
        onPrimaryContainer = Color(0xFF212121),
        secondary = Color(0xFF616161),
        secondaryContainer = Color(0xFFE0E0E0),
        onSecondaryContainer = Color(0xFF212121),
        tertiary = Color(0xFF424242),
        tertiaryContainer = Color(0xFFF5F5F5),
        onTertiaryContainer = Color(0xFF212121),
        background = Color(0xFFFFFFFF),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFF3F3F3)
    )

    AppThemeColor.BLACK -> sharedLightBase.copy(
        primary = Color(0xFF000000),
        primaryContainer = Color(0xFFE0E0E0),
        onPrimaryContainer = Color(0xFF000000),
        secondary = Color(0xFF424242),
        secondaryContainer = Color(0xFFD6D6D6),
        onSecondaryContainer = Color(0xFF1A1A1A),
        tertiary = Color(0xFF212121),
        tertiaryContainer = Color(0xFFEEEEEE),
        onTertiaryContainer = Color(0xFF000000),
        background = Color(0xFFF2F2F2),
        surface = Color(0xFFFAFAFA),
        surfaceVariant = Color(0xFFE8E8E8)
    )

    AppThemeColor.GREEN -> sharedLightBase.copy(
        primary = Green40,
        primaryContainer = Color(0xFFC8E6C9),
        onPrimaryContainer = Color(0xFF1B5E20),
        secondary = GreenGrey40,
        secondaryContainer = Color(0xFFDCE8DF),
        onSecondaryContainer = Color(0xFF1B3321),
        tertiary = Teal40,
        tertiaryContainer = Color(0xFFB2DFDB),
        onTertiaryContainer = Color(0xFF004D40),
        background = LightBackground,
        surface = Color.White,
        surfaceVariant = Color(0xFFE8F0E9)
    )

    AppThemeColor.BLUE -> sharedLightBase.copy(
        primary = Color(0xFF1565C0),
        primaryContainer = Color(0xFFBBDEFB),
        onPrimaryContainer = Color(0xFF0D47A1),
        secondary = Color(0xFF455A64),
        secondaryContainer = Color(0xFFCFD8DC),
        onSecondaryContainer = Color(0xFF263238),
        tertiary = Color(0xFF0277BD),
        tertiaryContainer = Color(0xFFB3E5FC),
        onTertiaryContainer = Color(0xFF01579B),
        background = Color(0xFFF5F9FF),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFE3F2FD)
    )
}

private fun darkSchemeFor(themeColor: AppThemeColor): ColorScheme = when (themeColor) {
    AppThemeColor.WHITE -> sharedDarkBase.copy(
        primary = Color(0xFFF5F5F5),
        onPrimary = Color(0xFF121212),
        primaryContainer = Color(0xFF424242),
        onPrimaryContainer = Color(0xFFF5F5F5),
        secondary = Color(0xFFBDBDBD),
        secondaryContainer = Color(0xFF383838),
        onSecondaryContainer = Color(0xFFE8E8E8),
        tertiary = Color(0xFFE0E0E0),
        tertiaryContainer = Color(0xFF2C2C2C),
        onTertiaryContainer = Color(0xFFF5F5F5),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF2C2C2C)
    )

    AppThemeColor.BLACK -> sharedDarkBase.copy(
        primary = Color(0xFFECECEC),
        onPrimary = Color(0xFF000000),
        primaryContainer = Color(0xFF1A1A1A),
        onPrimaryContainer = Color(0xFFF5F5F5),
        secondary = Color(0xFFB0B0B0),
        secondaryContainer = Color(0xFF141414),
        onSecondaryContainer = Color(0xFFE0E0E0),
        tertiary = Color(0xFF9E9E9E),
        tertiaryContainer = Color(0xFF101010),
        onTertiaryContainer = Color(0xFFF0F0F0),
        background = Color(0xFF000000),
        surface = Color(0xFF0A0A0A),
        surfaceVariant = Color(0xFF161616)
    )

    AppThemeColor.GREEN -> sharedDarkBase.copy(
        primary = Green80,
        primaryContainer = Color(0xFF1B5E20),
        onPrimaryContainer = Color(0xFFC8E6C9),
        secondary = GreenGrey80,
        secondaryContainer = Color(0xFF2E4A35),
        onSecondaryContainer = Color(0xFFD4E4D8),
        tertiary = Teal80,
        tertiaryContainer = Color(0xFF004D40),
        onTertiaryContainer = Color(0xFFB2DFDB),
        background = DarkBackground,
        surface = DarkSurface,
        surfaceVariant = Color(0xFF2A2A2A)
    )

    AppThemeColor.BLUE -> sharedDarkBase.copy(
        primary = Color(0xFF90CAF9),
        onPrimary = Color(0xFF0D47A1),
        primaryContainer = Color(0xFF1565C0),
        onPrimaryContainer = Color(0xFFE3F2FD),
        secondary = Color(0xFF90A4AE),
        secondaryContainer = Color(0xFF263238),
        onSecondaryContainer = Color(0xFFCFD8DC),
        tertiary = Color(0xFF4FC3F7),
        tertiaryContainer = Color(0xFF01579B),
        onTertiaryContainer = Color(0xFFB3E5FC),
        background = Color(0xFF0D1117),
        surface = Color(0xFF151B24),
        surfaceVariant = Color(0xFF1E2A38)
    )
}

@Composable
fun WalletTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    fontScale: FontScale = FontScale.NORMAL,
    themeColor: AppThemeColor = AppThemeColor.GREEN,
    content: @Composable () -> Unit
) {
    val colorScheme = walletColorScheme(themeColor, darkTheme)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
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
