package com.wallet.data.model

import java.util.UUID

data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val amount: Double,
    val type: TransactionType,
    val category: String,
    val note: String = "",
    val accountId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val excludeFromStats: Boolean = false,
    val isTransfer: Boolean = false,
    val relatedAccountId: String? = null,
    val counterAmount: Double = 0.0,
    val transferFee: Double = 0.0
)
