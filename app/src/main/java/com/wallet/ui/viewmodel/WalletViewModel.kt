package com.wallet.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.wallet.data.model.Account
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.PeriodStatistics
import com.wallet.data.model.StatisticsPeriod
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.data.model.UserProfile
import com.wallet.data.model.LauncherIconStyle
import com.wallet.data.model.WallpaperPage
import com.wallet.data.repository.WalletRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WalletViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WalletRepository(application.applicationContext)

    val accounts: StateFlow<List<Account>> = repository.accounts
    val transactions: StateFlow<List<Transaction>> = repository.transactions
    val userProfile: StateFlow<UserProfile> = repository.userProfile

    private val _accountingWallpaperOnly = MutableStateFlow(false)
    val accountingWallpaperOnly: StateFlow<Boolean> = _accountingWallpaperOnly.asStateFlow()

    fun setAccountingWallpaperOnly(enabled: Boolean) {
        _accountingWallpaperOnly.value = enabled
    }

    val totalAssets: Double
        get() = repository.totalAssets

    val monthlyIncome: Double
        get() = repository.getMonthlyIncome()

    val monthlyExpense: Double
        get() = repository.getMonthlyExpense()

    fun getStatistics(period: StatisticsPeriod, year: Int, month: Int): PeriodStatistics {
        return repository.getStatistics(period, year, month)
    }

    fun exportBackupJson(): String {
        return repository.exportBackupJson()
    }

    fun importBackupJson(json: String, replaceExisting: Boolean): WalletRepository.ImportResult {
        return repository.importBackupJson(json, replaceExisting)
    }

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        accountId: String,
        excludeFromStats: Boolean = false
    ) {
        repository.addTransaction(amount, type, category, note, accountId, excludeFromStats)
    }

    fun deleteTransaction(transaction: Transaction) {
        repository.deleteTransaction(transaction)
    }

    fun getTotalAssets(usdToCnyRate: Double): Double {
        return repository.calculateTotalAssets(accounts.value, usdToCnyRate)
    }

    fun addAccount(name: String, balance: Double, currency: CurrencyType) {
        repository.addAccount(name, balance, currency)
    }

    fun updateAccount(account: Account) {
        repository.updateAccount(account)
    }

    fun deleteAccount(accountId: String) {
        repository.deleteAccount(accountId)
    }

    fun updateProfile(profile: UserProfile) {
        repository.updateProfile(profile)
    }

    fun updateLauncherIconStyle(style: LauncherIconStyle) {
        repository.updateLauncherIconStyle(style)
    }

    fun updateCustomLauncherIcon(uri: String) {
        repository.updateCustomLauncherIcon(uri)
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        repository.setNotificationsEnabled(enabled)
    }

    fun updateWallpaper(page: WallpaperPage, uri: String?) {
        repository.updateWallpaper(page, uri)
    }

    fun updateExchangeRate(rate: Double) {
        repository.updateExchangeRate(rate)
    }
}
