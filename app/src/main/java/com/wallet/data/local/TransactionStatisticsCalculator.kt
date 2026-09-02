package com.wallet.data.local

import com.wallet.data.model.CategoryStat
import com.wallet.data.model.PeriodStatistics
import com.wallet.data.model.StatisticsPeriod
import com.wallet.data.model.Transaction
import com.wallet.data.model.TransactionType
import java.util.Calendar
import java.util.Locale

object TransactionStatisticsCalculator {

    fun calculate(
        transactions: List<Transaction>,
        period: StatisticsPeriod,
        year: Int,
        month: Int
    ): PeriodStatistics {
        val statsTransactions = transactions.filter { !it.excludeFromStats }

        val filtered = when (period) {
            StatisticsPeriod.MONTH -> statsTransactions.filter { tx ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                cal.get(Calendar.YEAR) == year && cal.get(Calendar.MONTH) == month
            }
            StatisticsPeriod.YEAR -> statsTransactions.filter { tx ->
                val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                cal.get(Calendar.YEAR) == year
            }
        }

        val income = filtered
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        val expense = filtered
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }

        val categoryBreakdown = filtered
            .groupBy { "${it.type.name}_${it.category}" }
            .map { (_, items) ->
                val first = items.first()
                CategoryStat(
                    category = first.category,
                    amount = items.sumOf { it.amount },
                    type = first.type
                )
            }
            .sortedByDescending { it.amount }

        val label = when (period) {
            StatisticsPeriod.MONTH -> String.format(Locale.CHINA, "%d年%d月", year, month + 1)
            StatisticsPeriod.YEAR -> String.format(Locale.CHINA, "%d年", year)
        }

        return PeriodStatistics(
            periodLabel = label,
            income = income,
            expense = expense,
            balance = income - expense,
            transactionCount = filtered.size,
            categoryBreakdown = categoryBreakdown
        )
    }

    fun getCurrentYearMonth(): Pair<Int, Int> {
        val cal = Calendar.getInstance()
        return cal.get(Calendar.YEAR) to cal.get(Calendar.MONTH)
    }
}
