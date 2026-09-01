package com.wallet.data.local

import android.content.Context
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
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

    fun loadProfile(): UserProfile {
        return UserProfile(
            nickname = prefs.getString(KEY_NICKNAME, "用户") ?: "用户",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            currency = prefs.getString(KEY_CURRENCY, "CNY") ?: "CNY",
            darkMode = prefs.getBoolean(KEY_DARK_MODE, false),
            notificationsEnabled = prefs.getBoolean(KEY_NOTIFICATIONS_ENABLED, true),
            usdToCnyRate = prefs.getFloat(KEY_USD_TO_CNY_RATE, 7.25f).toDouble(),
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
        private const val KEY_NICKNAME = "nickname"
        private const val KEY_EMAIL = "email"
        private const val KEY_CURRENCY = "currency"
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_NOTIFICATIONS_ENABLED = "notifications_enabled"
        private const val KEY_USD_TO_CNY_RATE = "usd_to_cny_rate"
        private const val KEY_ACCOUNTING_WALLPAPER = "accounting_wallpaper"
        private const val KEY_ASSETS_WALLPAPER = "assets_wallpaper"
        private const val KEY_PROFILE_WALLPAPER = "profile_wallpaper"
        private const val KEY_TOTAL_ASSETS = "total_assets"
    }
}
