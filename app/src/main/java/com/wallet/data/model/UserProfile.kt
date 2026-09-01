package com.wallet.data.model

data class UserProfile(
    val nickname: String = "用户",
    val email: String = "",
    val currency: String = "CNY",
    val darkMode: Boolean = false,
    val usdToCnyRate: Double = 7.25,
    val accountingWallpaperUri: String? = null,
    val assetsWallpaperUri: String? = null,
    val profileWallpaperUri: String? = null
)
