package com.wallet.ui.theme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

object AppCardColors {

    @Composable
    fun surface() = cardColors(MaterialTheme.colorScheme.surface)

    @Composable
    fun surfaceVariant() = cardColors(MaterialTheme.colorScheme.surfaceVariant)

    @Composable
    fun primaryContainer() = CardDefaults.cardColors(
        containerColor = themedContainerColor(MaterialTheme.colorScheme.primaryContainer),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )

    @Composable
    private fun cardColors(base: Color) = CardDefaults.cardColors(
        containerColor = themedContainerColor(base),
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    private fun themedContainerColor(base: Color): Color {
        val alpha = LocalCardBackgroundAlpha.current
        return if (alpha <= 0f) Color.Transparent else base.copy(alpha = alpha)
    }
}

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    colors: CardColors = AppCardColors.surface(),
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier,
        shape = shape,
        color = colors.containerColor,
        contentColor = colors.contentColor,
        shadowElevation = 0.dp,
        tonalElevation = 0.dp,
    ) {
        Column(content = content)
    }
}

@Composable
fun themedTextColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
fun themedSecondaryTextColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant
