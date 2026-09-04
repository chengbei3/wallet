package com.wallet.data.model

data class UserProfile(
    val nickname: String = "用户",
    val email: String = "",
    val currency: String = "CNY",
    val darkMode: Boolean = true,
    val themeColor: AppThemeColor = AppThemeColor.BLACK,
    val notificationsEnabled: Boolean = true,
    val usdToCnyRate: Double = 7.25,
    val wallpaperOverlayAlpha: Float = 0.55f,
    val cardBackgroundAlpha: Float = 0.92f,
    val tabBarOverlayAlpha: Float = 0f,
    val fontScale: FontScale = FontScale.NORMAL,
    val splashImageUri: String? = null,
    val customAppIconUri: String? = null,
    val avatarUri: String? = null,
    val launcherIconStyle: LauncherIconStyle = LauncherIconStyle.DEFAULT,
    val openAddTransactionOnStart: Boolean = true,
    val accountingWallpaperUri: String? = null,
    val assetsWallpaperUri: String? = null,
    val profileWallpaperUri: String? = null,
    val accountingOverlayAlpha: Float = 0.55f,
    val categoryAccountBindings: Map<String, String> = emptyMap()
)
