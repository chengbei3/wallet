package com.wallet.data.model

enum class WallpaperPage(val title: String) {
    ACCOUNTING("记账"),
    ASSETS("资产"),
    PROFILE("我的")
}

fun UserProfile.pageTabBarAlpha(page: WallpaperPage): Float = when (page) {
    WallpaperPage.ACCOUNTING -> accountingPageTabAlpha
    WallpaperPage.ASSETS -> assetsPageTabAlpha
    WallpaperPage.PROFILE -> profilePageTabAlpha
}

fun UserProfile.withPageTabBarAlpha(page: WallpaperPage, alpha: Float): UserProfile = when (page) {
    WallpaperPage.ACCOUNTING -> copy(accountingPageTabAlpha = alpha)
    WallpaperPage.ASSETS -> copy(assetsPageTabAlpha = alpha)
    WallpaperPage.PROFILE -> copy(profilePageTabAlpha = alpha)
}
