package com.wallet

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
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

    Scaffold(
        bottomBar = {
            NavigationBar {
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
                        label = { Text(screen.title) }
                    )
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
