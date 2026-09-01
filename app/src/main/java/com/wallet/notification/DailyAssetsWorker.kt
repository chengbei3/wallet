package com.wallet.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.wallet.data.local.WalletPreferences

class DailyAssetsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = WalletPreferences(applicationContext)
        if (!prefs.notificationsEnabled) {
            return Result.success()
        }

        val totalAssets = prefs.getTotalAssets()
        NotificationHelper.showDailyAssetsNotification(applicationContext, totalAssets)
        return Result.success()
    }
}
