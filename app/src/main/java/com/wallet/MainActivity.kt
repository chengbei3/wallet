package com.wallet

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallet.ui.screen.splash.SplashScreen
import com.wallet.ui.theme.WalletTheme
import com.wallet.ui.viewmodel.WalletViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: WalletViewModel = viewModel()
            val profile by viewModel.userProfile.collectAsState()
            var showSplash by remember { mutableStateOf(true) }

            val isDarkTheme = if (profile.darkMode) true else isSystemInDarkTheme()

            WalletTheme(
                darkTheme = isDarkTheme,
                fontScale = profile.fontScale
            ) {
                if (showSplash) {
                    SplashScreen(
                        splashImageUri = profile.splashImageUri,
                        customAppIconUri = profile.customAppIconUri,
                        onFinished = { showSplash = false }
                    )
                } else {
                    WalletApp(viewModel = viewModel)
                }
            }
        }
    }
}
