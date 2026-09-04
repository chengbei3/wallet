package com.wallet.data.repository

import android.content.Context
import com.wallet.data.local.TransactionStatisticsCalculator
import com.wallet.data.local.WalletDataSerializer
import com.wallet.data.local.WalletPreferences
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.PeriodStatistics
import com.wallet.data.model.StatisticsPeriod
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.data.model.UserProfile
import com.wallet.data.model.LauncherIconStyle
import com.wallet.data.model.WallpaperPage
import com.wallet.data.model.WalletBackup
import com.wallet.notification.NotificationScheduler
import com.wallet.util.LauncherIconManager
import com.wallet.util.ExchangeRates
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

    private val _transactions = MutableStateFlow(preferences.loadTransactions())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _userProfile = MutableStateFlow(preferences.loadProfile())
    val userProfile: StateFlow<UserProfile> = _userProfile.asStateFlow()

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

    fun getStatistics(
        period: StatisticsPeriod,
        year: Int,
        month: Int
    ): PeriodStatistics {
        return TransactionStatisticsCalculator.calculate(
            transactions = _transactions.value,
            period = period,
            year = year,
            month = month
        )
    }

    fun exportBackup(): WalletBackup {
        return WalletBackup(
            accounts = _accounts.value,
            transactions = _transactions.value
        )
    }

    fun exportBackupJson(): String {
        return WalletDataSerializer.toJson(exportBackup())
    }

    sealed class ImportResult {
        data class Success(val transactionCount: Int, val accountCount: Int) : ImportResult()
        data class Error(val message: String) : ImportResult()
    }

    fun importBackupJson(json: String, replaceExisting: Boolean): ImportResult {
        return try {
            val backup = WalletDataSerializer.fromJson(json)
            if (backup.transactions.isEmpty() && backup.accounts.isEmpty()) {
                return ImportResult.Error("备份文件为空")
            }

            if (replaceExisting) {
                if (backup.accounts.isNotEmpty()) {
                    _accounts.value = backup.accounts
                }
                _transactions.value = backup.transactions.sortedByDescending { it.timestamp }
            } else {
                val existingTxIds = _transactions.value.map { it.id }.toSet()
                val newTransactions = backup.transactions.filter { it.id !in existingTxIds }
                _transactions.update { current ->
                    (current + newTransactions).sortedByDescending { it.timestamp }
                }

                val existingAccountIds = _accounts.value.map { it.id }.toSet()
                val newAccounts = backup.accounts.filter { it.id !in existingAccountIds }
                _accounts.update { it + newAccounts }
            }

            persistSnapshot()
            ImportResult.Success(
                transactionCount = _transactions.value.size,
                accountCount = _accounts.value.size
            )
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "导入失败")
        }
    }

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        accountId: String,
        excludeFromStats: Boolean = false,
        applyToBalance: Boolean = true
    ) {
        val transaction = Transaction(
            amount = amount,
            type = type,
            category = category,
            note = note,
            accountId = accountId,
            excludeFromStats = excludeFromStats
        )
        _transactions.update { listOf(transaction) + it }

        if (applyToBalance) {
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

    fun updateTransaction(updated: Transaction) {
        val existing = _transactions.value.find { it.id == updated.id } ?: return
        _transactions.update { list ->
            list.map { if (it.id == updated.id) updated else it }
                .sortedByDescending { it.timestamp }
        }
        _accounts.update { accounts ->
            accounts.map { account ->
                var balance = account.balance
                if (account.id == existing.accountId) {
                    balance -= transactionDelta(existing)
                }
                if (account.id == updated.accountId) {
                    balance += transactionDelta(updated)
                }
                if (balance != account.balance) {
                    account.copy(balance = balance)
                } else {
                    account
                }
            }
        }
        persistSnapshot()
    }

    private fun transactionDelta(transaction: Transaction): Double {
        return if (transaction.type == TransactionType.INCOME) {
            transaction.amount
        } else {
            -transaction.amount
        }
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
            if (profile.launcherIconStyle == LauncherIconStyle.CUSTOM && profile.customAppIconUri != null) {
                LauncherIconManager.applyCustomIcon(appContext, profile.customAppIconUri)
            } else {
                LauncherIconManager.applyIcon(appContext, profile.launcherIconStyle)
            }
        }
        persistSnapshot()
    }

    fun updateLauncherIconStyle(style: LauncherIconStyle) {
        val updated = _userProfile.value.copy(launcherIconStyle = style)
        _userProfile.value = updated
        preferences.saveProfile(updated)
        if (style == LauncherIconStyle.CUSTOM && updated.customAppIconUri != null) {
            LauncherIconManager.applyCustomIcon(appContext, updated.customAppIconUri)
        } else {
            LauncherIconManager.applyIcon(appContext, style)
        }
        persistSnapshot()
    }

    fun updateCustomLauncherIcon(uri: String): Boolean {
        val result = LauncherIconManager.applyCustomIcon(appContext, uri)
        if (!result.success) {
            return false
        }
        val updated = _userProfile.value.copy(
            customAppIconUri = result.localIconUri ?: uri,
            launcherIconStyle = LauncherIconStyle.CUSTOM
        )
        _userProfile.value = updated
        preferences.saveProfile(updated)
        persistSnapshot()
        return true
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
        val normalizedRate = ExchangeRates.normalize(rate)
        _userProfile.update {
            it.copy(usdToCnyRate = normalizedRate).also { profile -> preferences.saveProfile(profile) }
        }
        persistSnapshot()
    }

    fun getMonthlyIncome(): Double {
        val (year, month) = TransactionStatisticsCalculator.getCurrentYearMonth()
        return getStatistics(StatisticsPeriod.MONTH, year, month).income
    }

    fun getMonthlyExpense(): Double {
        val (year, month) = TransactionStatisticsCalculator.getCurrentYearMonth()
        return getStatistics(StatisticsPeriod.MONTH, year, month).expense
    }

    private fun persistSnapshot() {
        preferences.saveAccounts(_accounts.value)
        preferences.saveTransactions(_transactions.value)
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
