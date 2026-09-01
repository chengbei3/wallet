package com.wallet.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.wallet.data.model.Account
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import com.wallet.data.model.CurrencyType
import com.wallet.data.model.UserProfile
import com.wallet.data.model.WallpaperPage
import com.wallet.data.repository.WalletRepository
import kotlinx.coroutines.flow.StateFlow

class WalletViewModel : ViewModel() {

    private val repository = WalletRepository()

    val accounts: StateFlow<List<Account>> = repository.accounts
    val transactions: StateFlow<List<Transaction>> = repository.transactions
    val userProfile: StateFlow<UserProfile> = repository.userProfile

    val totalAssets: Double
        get() = repository.totalAssets

    val monthlyIncome: Double
        get() = repository.getMonthlyIncome()

    val monthlyExpense: Double
        get() = repository.getMonthlyExpense()

    fun addTransaction(
        amount: Double,
        type: TransactionType,
        category: String,
        note: String,
        accountId: String
    ) {
        repository.addTransaction(amount, type, category, note, accountId)
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

    fun updateWallpaper(page: WallpaperPage, uri: String?) {
        repository.updateWallpaper(page, uri)
    }

    fun updateExchangeRate(rate: Double) {
        repository.updateExchangeRate(rate)
    }
}
