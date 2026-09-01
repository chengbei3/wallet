package com.wallet.data.model

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val note: String = "",
    val accountId: String,
    val timestamp: Long = System.currentTimeMillis()
)
