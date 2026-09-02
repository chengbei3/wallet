package com.wallet.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.wallet.data.model.LauncherIconStyle

object LauncherIconManager {

    private const val TAG = "LauncherIconManager"

    private val aliasMap = mapOf(
        LauncherIconStyle.DEFAULT to ".LauncherDefault",
        LauncherIconStyle.FOREST to ".LauncherForest",
        LauncherIconStyle.OCEAN to ".LauncherOcean",
        LauncherIconStyle.SUNSET to ".LauncherSunset"
    )

    fun applyIcon(context: Context, style: LauncherIconStyle) {
        val appContext = context.applicationContext
        val packageManager = appContext.packageManager
        val packageName = appContext.packageName
        val targetAlias = aliasMap[style] ?: return

        runCatching {
            val targetComponent = componentName(packageName, targetAlias)
            packageManager.setComponentEnabledSetting(
                targetComponent,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP
            )

            aliasMap
                .filterValues { it != targetAlias }
                .forEach { (_, alias) ->
                    packageManager.setComponentEnabledSetting(
                        componentName(packageName, alias),
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP
                    )
                }
        }.onFailure {
            Log.e(TAG, "Failed to apply launcher icon: $style", it)
        }
    }

    private fun componentName(packageName: String, alias: String): ComponentName {
        return ComponentName(packageName, "$packageName$alias")
    }
}
