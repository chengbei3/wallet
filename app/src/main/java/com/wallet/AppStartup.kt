package com.wallet

import android.content.Context
import com.wallet.data.local.WalletPreferences
import com.wallet.data.model.LauncherIconStyle
import com.wallet.notification.NotificationScheduler
import com.wallet.util.LauncherIconManager
import java.util.concurrent.Executors

object AppStartup {

    private val executor = Executors.newSingleThreadExecutor()

    fun runDeferred(context: Context) {
        val appContext = context.applicationContext
        executor.execute {
            runCatching {
                val preferences = WalletPreferences(appContext)
                val profile = preferences.loadProfile()
                if (profile.launcherIconStyle == LauncherIconStyle.CUSTOM &&
                    profile.customAppIconUri != null
                ) {
                    LauncherIconManager.applyCustomIcon(appContext, profile.customAppIconUri)
                } else {
                    LauncherIconManager.applyIcon(appContext, profile.launcherIconStyle)
                }
                if (preferences.notificationsEnabled) {
                    NotificationScheduler.schedule(appContext)
                }
            }
        }
    }
}
