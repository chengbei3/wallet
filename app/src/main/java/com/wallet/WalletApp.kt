package com.wallet

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallet.ui.components.TransparentBarDefaults
import com.wallet.ui.components.WallpaperBackground
import com.wallet.ui.navigation.Screen
import com.wallet.ui.navigation.bottomNavItems
import com.wallet.ui.screen.accounting.AccountingScreen
import com.wallet.ui.screen.assets.AssetsScreen
import com.wallet.ui.screen.profile.ProfileScreen
import com.wallet.ui.theme.LocalCardBackgroundAlpha
import com.wallet.ui.viewmodel.WalletViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun WalletApp(viewModel: WalletViewModel = viewModel()) {
    val profile by viewModel.userProfile.collectAsState()
    val accountingWallpaperOnly by viewModel.accountingWallpaperOnly.collectAsState()
    val pages = bottomNavItems
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )
    val coroutineScope = rememberCoroutineScope()
    val currentPage = pagerState.currentPage
    val currentScreen = pages[currentPage]

    val wallpaperUri = when (currentScreen) {
        Screen.Accounting -> profile.accountingWallpaperUri
        Screen.Assets -> profile.assetsWallpaperUri
        Screen.Profile -> profile.profileWallpaperUri
    }

    CompositionLocalProvider(LocalCardBackgroundAlpha provides profile.cardBackgroundAlpha) {
        WallpaperBackground(
            wallpaperUri = wallpaperUri,
            overlayAlpha = profile.wallpaperOverlayAlpha
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onBackground,
                contentWindowInsets = WindowInsets(0, 0, 0, 0),
                bottomBar = {
                    val hideBottomBar = currentScreen == Screen.Accounting && accountingWallpaperOnly
                    if (!hideBottomBar) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface.copy(
                                alpha = profile.tabBarOverlayAlpha
                            ),
                            tonalElevation = 0.dp,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            pages.forEachIndexed { index, screen ->
                                val selected = currentPage == index
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(index)
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) {
                                                screen.selectedIcon
                                            } else {
                                                screen.unselectedIcon
                                            },
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
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.padding(innerPadding),
                    beyondViewportPageCount = 0
                ) { page ->
                    when (pages[page]) {
                        Screen.Accounting -> AccountingScreen(
                            viewModel = viewModel,
                            topBarAlpha = profile.tabBarOverlayAlpha
                        )
                        Screen.Assets -> AssetsScreen(
                            viewModel = viewModel,
                            topBarAlpha = profile.tabBarOverlayAlpha
                        )
                        Screen.Profile -> ProfileScreen(
                            viewModel = viewModel,
                            topBarAlpha = profile.tabBarOverlayAlpha
                        )
                    }
                }
            }
        }
    }
}
