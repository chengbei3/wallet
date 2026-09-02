package com.wallet.data.local

import android.content.Context
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.FontScale
import com.wallet.data.model.LauncherIconStyle
import com.wallet.data.model.Transaction
import com.wallet.data.model.UserProfile
import org.json.JSONArray
import org.json.JSONObject

class WalletPreferences(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadAccounts(): List<Account>? {
        val json = prefs.getString(KEY_ACCOUNTS, null) ?: return null
        return try {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val item = array.getJSONObject(i)
                    add(
                        Account(
                            id = item.getString("id"),
                            name = item.getString("name"),
                            balance = item.getDouble("balance"),
                            currency = CurrencyType.valueOf(item.getString("currency")),
                            icon = item.optString("icon", "account_balance_wallet")
                        )
                    )
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    fun saveAccounts(accounts: List<Account>) {
        val array = JSONArray()
        accounts.forEach { account ->
            array.put(
                JSONObject()
                    .put("id", account.id)
                    .put("name", account.name)
                    .put("balance", account.balance)
                    .put("currency", account.currency.name)
                    .put("icon", account.icon)
            )
        }
        prefs.edit().putString(KEY_ACCOUNTS, array.toString()).apply()
    }

    fun loadTransactions(): List<Transaction> {
        val json = prefs.getString(KEY_TRANSACTIONS, null) ?: return emptyList()
        return try {
            WalletDataSerializer.parseTransactions(org.json.JSONArray(json))
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun saveTransactions(transactions: List<Transaction>) {
        val json = WalletDataSerializer.transactionsToJson(transactions).toString()
        prefs.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    fun loadProfile(): UserProfile {
        val fontScaleName = prefs.getString(KEY_FONT_SCALE, FontScale.NORMAL.name)
        val iconStyleName = prefs.getString(KEY_LAUNCHER_ICON_STYLE, LauncherIconStyle.DEFAULT.name)
        return UserProfile(
            nickname = prefs.getString(KEY_NICKNAME, "用户") ?: "用户",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            currency = prefs.getString(KEY_CURRENCY, "CNY") ?: "CNY",
            darkMode = prefs.getBoolean(KEY_DARK_MODE, false),
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true),
            usdToCnyRate = prefs.getFloat(KEY_USD_TO_CNY_RATE, 7.25f).toDouble(),
            wallpaperOverlayAlpha = prefs.getFloat(KEY_WALLPAPER_OVERLAY_ALPHA, 0.55f),
            tabBarOverlayAlpha = prefs.getFloat(KEY_TAB_BAR_OVERLAY_ALPHA, 0f),
            fontScale = runCatching { FontScale.valueOf(fontScaleName!!) }.getOrDefault(FontScale.NORMAL),
            splashImageUri = prefs.getString(KEY_SPLASH_IMAGE, null),
            customAppIconUri = prefs.getString(KEY_CUSTOM_APP_ICON, null),
            avatarUri = prefs.getString(KEY_AVATAR, null),
            launcherIconStyle = runCatching {
                LauncherIconStyle.valueOf(iconStyleName!!)
            }.getOrDefault(LauncherIconStyle.DEFAULT),
            openAddTransactionOnStart = prefs.getBoolean(KEY_OPEN_ADD_ON_START, true),
            accountingWallpaperUri = prefs.getString(KEY_ACCOUNTING_WALLPAPER, null),
            assetsWallpaperUri = prefs.getString(KEY_ASSETS_WALLPAPER, null),
            profileWallpaperUri = prefs.getString(KEY_PROFILE_WALLPAPER, null)
        )
    }

    fun saveProfile(profile: UserProfile) {
        prefs.edit()
            .putString(KEY_NICKNAME, profile.nickname)
            .putString(KEY_EMAIL, profile.email)
            .putString(KEY_CURRENCY, profile.currency)
            .putBoolean(KEY_DARK_MODE, profile.darkMode)
            .putBoolean(KEY_NOTIFICATIONS_ENABLED, profile.notificationsEnabled)
            .putFloat(KEY_USD_TO_CNY_RATE, profile.usdToCnyRate.toFloat())
            .putFloat(KEY_WALLPAPER_OVERLAY_ALPHA, profile.wallpaperOverlayAlpha)
            .putFloat(KEY_TAB_BAR_OVERLAY_ALPHA, profile.tabBarOverlayAlpha)
            .putString(KEY_FONT_SCALE, profile.fontScale.name)
            .putString(KEY_SPLASH_IMAGE, profile.splashImageUri)
            .putString(KEY_CUSTOM_APP_ICON, profile.customAppIconUri)
            .putString(KEY_AVATAR, profile.avatarUri)
            .putString(KEY_LAUNCHER_ICON_STYLE, profile.launcherIconStyle.name)
            .putBoolean(KEY_OPEN_ADD_ON_START, profile.openAddTransactionOnStart)
            .putString(KEY_ACCOUNTING_WALLPAPER, profile.accountingWallpaperUri)
            .putString(KEY_ASSETS_WALLPAPER, profile.assetsWallpaperUri)
            .putString(KEY_PROFILE_WALLPAPER, profile.profileWallpaperUri)
            .apply()
    }

    fun saveTotalAssets(totalAssets: Double) {
        prefs.edit().putFloat(KEY_TOTAL_ASSETS, totalAssets.toFloat()).apply()
    }

    fun getTotalAssets(): Double {
        return prefs.getFloat(KEY_TOTAL_ASSETS, 0f).toDouble()
    }

    val notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true)

    companion object {
        private const val PREFS_NAME = "wallet_prefs"
        private const val KEY_ACCOUNTS = "accounts"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMAIL = "email"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_USD_TO_CNY_RATE = "usd_to_cny_rate"
        private const val KEY_WALLPAPER_OVERLAY_ALPHA = "wallpaper_overlay_alpha"
        private const val KEY_TAB_BAR_OVERLAY_ALPHA = "tab_bar_overlay_alpha"
        private const val KEY_FONT_SCALE = "font_scale"
        private const val KEY_SPLASH_IMAGE = "splash_image"
        private const val KEY_CUSTOM_APP_ICON = "custom_app_icon"
        private const val KEY_AVATAR = "avatar_uri"
        private const val KEY_LAUNCHER_ICON_STYLE = "launcher_icon_style"
        private const val KEY_OPEN_ADD_ON_START = "open_add_on_start"
        private const val KEY_ACCOUNTING_WALLPAPER = "accounting_wallpaper"
        private const val KEY_ASSETS_WALLPAPER = "assets_wallpaper"
        private const val KEY_PROFILE_WALLPAPER = "profile_wallpaper"
        private const val KEY_TOTAL_ASSETS = "total_assets"
    }
}
