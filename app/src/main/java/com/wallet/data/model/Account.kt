package com.wallet.data.model

import java.util.UUID

data class Account(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val balance: Double,
    val currency: CurrencyType = CurrencyType.CNY,
    val icon: String = "account_balance_wallet"
)
