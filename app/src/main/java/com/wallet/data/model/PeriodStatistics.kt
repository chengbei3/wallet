package com.wallet.data.model

enum class StatisticsPeriod {
    MONTH,
    YEAR
}

data class PeriodStatistics(
    val periodLabel: String,
    val income: Double,
    val expense: Double,
    val balance: Double,
    val transactionCount: Int,
    val categoryBreakdown: List<CategoryStat>
)

data class CategoryStat(
    val category: String,
    val amount: Double,
    val type: TransactionType
)
