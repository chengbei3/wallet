package com.wallet.util

import java.math.BigDecimal
import java.math.RoundingMode

object ExchangeRates {

    private const val SCALE = 2

    fun normalize(rate: Double): Double {
        return BigDecimal(rate.toString())
            .setScale(SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }

    fun format(rate: Double): String {
        return BigDecimal(rate.toString())
            .setScale(SCALE, RoundingMode.HALF_UP)
            .toPlainString()
    }

    fun roundMoney(amount: Double): Double {
        return BigDecimal(amount.toString())
            .setScale(SCALE, RoundingMode.HALF_UP)
            .toDouble()
    }
}
