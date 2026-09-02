package com.wallet.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.wallet.data.local.WalletPreferences
import com.wallet.util.LauncherIconManager

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != Intent.ACTION_BOOT_COMPLETED) return
        val prefs = WalletPreferences(context)
        LauncherIconManager.applyIcon(context, prefs.loadProfile().launcherIconStyle)
        if (prefs.notificationsEnabled) {
            NotificationScheduler.schedule(context)
        }
    }
}
