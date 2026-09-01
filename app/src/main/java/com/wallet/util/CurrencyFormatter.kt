package com.wallet.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    fun formatCny(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(Locale.CHINA)
        return format.format(amount)
    }
}
