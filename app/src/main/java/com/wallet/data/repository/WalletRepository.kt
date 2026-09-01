package com.wallet.data.repository

import android.content.Context
import com.wallet.data.local.WalletPreferences
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.data.model.UserProfile
import com.wallet.notification.NotificationScheduler
import com.wallet.util.LauncherIconManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class WalletRepository(context: Context) {

    private val appContext = context.applicationContext
    private val preferences = WalletPreferences(appContext)

    private val defaultAccounts = listOf(
        Account(name = "现金", balance = 2000.0),
        Account(name = "银行卡", balance = 15000.0),
        Account(name = "支付宝", balance = 3500.0),
        Account(name = "微信", balance = 1200.0),
        Account(name = "美元账户", balance = 500.0, currency = CurrencyType.USD)
    )

    private val _accounts = MutableStateFlow(preferences.loadAccounts() ?: defaultAccounts)
    val accounts: StateFlow<List<Account>> = _accounts.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _userProfile = MutableStateFlow(preferences.loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

    init {
        persistSnapshot()
        LauncherIconManager.applyIcon(appContext, _userProfile.value.launcherIconStyle)
    }

    val totalAssets: Double
        get() = calculateTotalAssets(_accounts.value, _userProfile.value.usdToCnyRate)

    fun calculateTotalAssets(accounts: List<Account>, usdToCnyRate: Double): Double {
        return accounts.sumOf { account ->
            when (account.currency) {
                CurrencyType.CNY -> account.balance
                CurrencyType.USD -> account.balance * usdToCnyRate
            }
        }
    }

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        accountId: String
    ) {
        val transaction = Transaction(
            amount = amount,
            type = type,
            category = category,
            note = note,
            accountId = accountId
        )
        _transactions.update { listOf(transaction) + it }

        _accounts.update { accounts ->
            accounts.map { account ->
                if (account.id == accountId) {
                    val delta = if (type == TransactionType.INCOME) amount else -amount
                    account.copy(balance = account.balance + delta)
                } else {
                    account
                }
            }
        }
        persistSnapshot()
    }

    fun deleteTransaction(transaction: Transaction) {
        _transactions.update { it.filter { t -> t.id != transaction.id } }

        _accounts.update { accounts ->
            accounts.map { account ->
                if (account.id == transaction.accountId) {
                    val delta = if (transaction.type == TransactionType.INCOME) {
                        -transaction.amount
                    } else {
                        transaction.amount
                    }
                    account.copy(balance = account.balance + delta)
                } else {
                    account
                }
            }
        }
        persistSnapshot()
    }

    fun addAccount(name: String, balance: Double, currency: CurrencyType) {
        _accounts.update { it + Account(name = name, balance = balance, currency = currency) }
        persistSnapshot()
    }

    fun updateAccount(account: Account) {
        _accounts.update { accounts ->
            accounts.map { if (it.id == account.id) account else it }
        }
        persistSnapshot()
    }

    fun deleteAccount(accountId: String) {
        _accounts.update { it.filter { account -> account.id != accountId } }
        persistSnapshot()
    }

    fun updateProfile(profile: UserProfile) {
        val previous = _userProfile.value
        _userProfile.value = profile
        preferences.saveProfile(profile)
        updateNotificationSchedule(profile.notificationsEnabled, previous.notificationsEnabled)
        if (profile.launcherIconStyle != previous.launcherIconStyle) {
            LauncherIconManager.applyIcon(appContext, profile.launcherIconStyle)
        }
        persistSnapshot()
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        updateProfile(_userProfile.value.copy(notificationsEnabled = enabled))
    }

    fun updateWallpaper(page: WallpaperPage, uri: String?) {
        _userProfile.update { profile ->
            when (page) {
                WallpaperPage.ACCOUNTING -> profile.copy(accountingWallpaperUri = uri)
                WallpaperPage.ASSETS -> profile.copy(assetsWallpaperUri = uri)
                WallpaperPage.PROFILE -> profile.copy(profileWallpaperUri = uri)
            }.also { preferences.saveProfile(it) }
        }
    }

    fun updateExchangeRate(rate: Double) {
        _userProfile.update {
            it.copy(usdToCnyRate = rate).also { profile -> preferences.saveProfile(profile) }
        }
        persistSnapshot()
    }

    fun getMonthlyIncome(): Double {
        val now = System.currentTimeMillis()
        val monthStart = now - 30L * 24 * 60 * 60 * 1000
        return _transactions.value
            .filter { it.type == TransactionType.INCOME && it.timestamp >= monthStart }
            .sumOf { it.amount }
    }

    fun getMonthlyExpense(): Double {
        val now = System.currentTimeMillis()
        val monthStart = now - 30L * 24 * 60 * 60 * 1000
        return _transactions.value
            .filter { it.type == TransactionType.EXPENSE && it.timestamp >= monthStart }
            .sumOf { it.amount }
    }

    private fun persistSnapshot() {
        preferences.saveAccounts(_accounts.value)
        preferences.saveTotalAssets(totalAssets)
    }

    private fun updateNotificationSchedule(enabled: Boolean, previousEnabled: Boolean) {
        if (enabled == previousEnabled) return
        if (enabled) {
            NotificationScheduler.schedule(appContext)
        } else {
            NotificationScheduler.cancel(appContext)
        }
    }
}
