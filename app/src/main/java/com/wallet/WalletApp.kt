package com.wallet

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wallet.ui.components.TransparentBarDefaults
import com.wallet.ui.components.WallpaperBackground
import com.wallet.ui.navigation.Screen
import com.wallet.ui.navigation.bottomNavItems
import com.wallet.ui.screen.accounting.AccountingScreen
import com.wallet.ui.screen.assets.AssetsScreen
import com.wallet.ui.screen.profile.ProfileScreen
import com.wallet.ui.viewmodel.WalletViewModel

@Composable
fun WalletApp(viewModel: WalletViewModel = viewModel()) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val profile by viewModel.userProfile.collectAsState()
    val accountingWallpaperOnly by viewModel.accountingWallpaperOnly.collectAsState()

    val wallpaperUri = when (currentRoute) {
        Screen.Accounting.route -> profile.accountingWallpaperUri
        Screen.Assets.route -> profile.assetsWallpaperUri
        Screen.Profile.route -> profile.profileWallpaperUri
        else -> null
    }

    WallpaperBackground(
        wallpaperUri = wallpaperUri,
        overlayAlpha = profile.wallpaperOverlayAlpha
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                val hideBottomBar = currentRoute == Screen.Accounting.route && accountingWallpaperOnly
                if (!hideBottomBar) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface.copy(
                            alpha = profile.tabBarOverlayAlpha
                        ),
                        tonalElevation = 0.dp,
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        bottomNavItems.forEach { screen ->
                            val selected = currentRoute == screen.route
                            NavigationBarItem(
                                selected = selected,
                                onClick = {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                                        contentDescription = screen.title
                                    )
                                },
                                label = { Text(screen.title) },
                                colors = TransparentBarDefaults.navigationBarItemColors()
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Accounting.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Accounting.route) {
                    AccountingScreen(viewModel = viewModel)
                }
                composable(Screen.Assets.route) {
                    AssetsScreen(viewModel = viewModel)
                }
                composable(Screen.Profile.route) {
                    ProfileScreen(viewModel = viewModel)
                }
            }
        }
    }
}
