package com.wallet.data.model

data class WalletBackup(
    val version: Int = 1,
    val exportedAt: Long = System.currentTimeMillis(),
    val accounts: List<Account> = emptyList(),
    val transactions: List<Transaction> = emptyList()
)
