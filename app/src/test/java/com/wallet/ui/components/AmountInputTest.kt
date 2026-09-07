package com.wallet.ui.components

import org.junit.Assert.assertEquals
import org.junit.Test

class AmountInputTest {

    @Test
    fun appendsDigitsAndDecimal() {
        assertEquals("1", appendAmountInput("", "1"))
        assertEquals("12", appendAmountInput("1", "2"))
        assertEquals("12.", appendAmountInput("12", "."))
        assertEquals("12.3", appendAmountInput("12.", "3"))
        assertEquals("12.35", appendAmountInput("12.3", "5"))
    }

    @Test
    fun rejectsSecondDecimalAndExtraFractionDigits() {
        assertEquals("12.3", appendAmountInput("12.3", "."))
        assertEquals("12.35", appendAmountInput("12.35", "6"))
        assertEquals("0.", appendAmountInput("", "."))
    }

    @Test
    fun leadingZeroAndDoubleZero() {
        assertEquals("0", appendAmountInput("", "0"))
        assertEquals("5", appendAmountInput("0", "5"))
        assertEquals("0", appendAmountInput("0", "00"))
        assertEquals("100", appendAmountInput("1", "00"))
        assertEquals("1.00", appendAmountInput("1.", "00"))
        assertEquals("1.20", appendAmountInput("1.2", "00"))
    }

    @Test
    fun deletesLastCharacter() {
        assertEquals("12", appendAmountInput("12.", "DEL"))
        assertEquals("", appendAmountInput("1", "DEL"))
        assertEquals("", appendAmountInput("", "DEL"))
    }
}
