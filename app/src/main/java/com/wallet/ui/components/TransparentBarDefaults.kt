package com.wallet.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

object TransparentBarDefaults {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun AppTopAppBar(
        title: @Composable () -> Unit,
        modifier: Modifier = Modifier,
        containerAlpha: Float = 0f,
        actions: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit = {}
    ) {
        val containerColor = MaterialTheme.colorScheme.surface.copy(alpha = containerAlpha)
        TopAppBar(
            title = title,
            actions = actions,
            modifier = modifier,
            windowInsets = WindowInsets(0, 0, 0, 0),
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = containerColor,
                scrolledContainerColor = containerColor,
                navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                titleContentColor = MaterialTheme.colorScheme.onBackground,
                actionIconContentColor = MaterialTheme.colorScheme.onBackground
            )
        )
    }

    @Composable
    fun topAppBarColors() = TopAppBarDefaults.topAppBarColors(
        containerColor = Color.Transparent,
        scrolledContainerColor = Color.Transparent,
        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
        titleContentColor = MaterialTheme.colorScheme.onBackground,
        actionIconContentColor = MaterialTheme.colorScheme.onBackground
    )

    @Composable
    fun navigationBarItemColors() = NavigationBarItemDefaults.colors(
        selectedIconColor = MaterialTheme.colorScheme.primary,
        selectedTextColor = MaterialTheme.colorScheme.primary,
        unselectedIconColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        unselectedTextColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)
    )
}
