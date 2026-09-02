package com.wallet.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppCardColors {

    @Composable
    fun surface() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(
            alpha = LocalCardBackgroundAlpha.current
        ),
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    fun surfaceVariant() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
            alpha = LocalCardBackgroundAlpha.current
        ),
        contentColor = MaterialTheme.colorScheme.onSurface
    )

    @Composable
    fun primaryContainer() = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(
            alpha = LocalCardBackgroundAlpha.current
        ),
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    )
}

@Composable
fun themedTextColor(): Color = MaterialTheme.colorScheme.onSurface

@Composable
fun themedSecondaryTextColor(): Color = MaterialTheme.colorScheme.onSurfaceVariant
