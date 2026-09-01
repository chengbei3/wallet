package com.wallet.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.wallet.data.model.LauncherIconStyle

object LauncherIconManager {

    private val aliasMap = mapOf(
        LauncherIconStyle.DEFAULT to ".LauncherDefault",
        LauncherIconStyle.FOREST to ".LauncherForest",
        LauncherIconStyle.OCEAN to ".LauncherOcean",
        LauncherIconStyle.SUNSET to ".LauncherSunset"
    )

    fun applyIcon(context: Context, style: LauncherIconStyle) {
        val packageManager = context.packageManager
        aliasMap.forEach { (iconStyle, alias) ->
            val componentName = ComponentName(context.packageName, "${context.packageName}$alias")
            val state = if (iconStyle == style) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            }
            packageManager.setComponentEnabledSetting(
                componentName,
                state,
                PackageManager.DONT_KILL_APP
            )
        }
    }
}
