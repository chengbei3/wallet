package com.wallet

import android.app.Application
import com.wallet.data.local.WalletPreferences
import com.wallet.notification.NotificationHelper
import com.wallet.notification.NotificationScheduler
import com.wallet.util.LauncherIconManager

class WalletApplication : Application() {

    lateinit var preferences: WalletPreferences
        private set

    override fun onCreate() {
        super.onCreate()
        preferences = WalletPreferences(this)
        NotificationHelper.createNotificationChannel(this)
        LauncherIconManager.applyIcon(this, preferences.loadProfile().launcherIconStyle)
        if (preferences.notificationsEnabled) {
            NotificationScheduler.schedule(this)
        }
    }
}
