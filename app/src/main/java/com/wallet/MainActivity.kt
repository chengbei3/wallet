package com.wallet

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.wallet.ui.theme.WalletTheme
import com.wallet.ui.viewmodel.WalletViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            splashScreen.setOnExitAnimationListener { splashView ->
                splashView.remove()
            }
        }
        enableEdgeToEdge()
        setContent {
            val viewModel: WalletViewModel = viewModel()
            val profile by viewModel.userProfile.collectAsState()
            val isDarkTheme = profile.darkMode || isSystemInDarkTheme()

            WalletTheme(
                darkTheme = isDarkTheme,
                fontScale = profile.fontScale,
                themeColor = profile.themeColor
            ) {
                WalletApp(viewModel = viewModel)
            }
        }
    }
}
