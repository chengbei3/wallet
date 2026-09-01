package com.wallet.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.wallet.data.model.FontScale

private val BaseTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp
    ),
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 24.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 17.sp,
        lineHeight = 26.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        lineHeight = 18.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

fun scaledTypography(fontScale: FontScale): Typography {
    val scale = fontScale.multiplier
    return Typography(
        displayLarge = BaseTypography.displayLarge.scaled(scale),
        displayMedium = BaseTypography.displayMedium.scaled(scale),
        displaySmall = BaseTypography.displaySmall.scaled(scale),
        headlineLarge = BaseTypography.headlineLarge.scaled(scale),
        headlineMedium = BaseTypography.headlineMedium.scaled(scale),
        headlineSmall = BaseTypography.headlineSmall.scaled(scale),
        titleLarge = BaseTypography.titleLarge.scaled(scale),
        titleMedium = BaseTypography.titleMedium.scaled(scale),
        titleSmall = BaseTypography.titleSmall.scaled(scale),
        bodyLarge = BaseTypography.bodyLarge.scaled(scale),
        bodyMedium = BaseTypography.bodyMedium.scaled(scale),
        bodySmall = BaseTypography.bodySmall.scaled(scale),
        labelLarge = BaseTypography.labelLarge.scaled(scale),
        labelMedium = BaseTypography.labelMedium.scaled(scale),
        labelSmall = BaseTypography.labelSmall.scaled(scale)
    )
}

private fun TextStyle.scaled(multiplier: Float): TextStyle {
    return copy(
        fontSize = fontSize * multiplier,
        lineHeight = lineHeight * multiplier
    )
}

val Typography = BaseTypography
