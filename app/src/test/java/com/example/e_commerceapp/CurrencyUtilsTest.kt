package com.example.e_commerceapp

import com.example.e_commerceapp.utils.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CurrencyUtilsTest {

    @Test
    fun formatInr_formatsWholeNumberWithRupeeSymbol() {
        val formatted = CurrencyUtils.formatInr(1499.0)
        // Should contain Rupee symbol ₹ and 1,499
        assertTrue(formatted.contains("₹") || formatted.contains("Rs"))
        assertTrue(formatted.contains("1,499") || formatted.contains("1499"))
    }

    @Test
    fun formatInr_handlesLargeIndianAmounts() {
        val formatted = CurrencyUtils.formatInr(149999.0)
        assertTrue(formatted.contains("1,49,999") || formatted.contains("149,999") || formatted.contains("149999"))
    }

    @Test
    fun calculateSavings_returnsCorrectSavingsAndPercentage() {
        val (savings, discountPercent) = CurrencyUtils.calculateSavings(mrpPrice = 2000.0, sellingPrice = 1499.0)
        assertEquals(501.0, savings, 0.01)
        assertEquals(25, discountPercent)
    }

    @Test
    fun calculateSavings_handlesZeroMrpGracefully() {
        val (savings, discountPercent) = CurrencyUtils.calculateSavings(mrpPrice = 0.0, sellingPrice = 100.0)
        assertEquals(0.0, savings, 0.01)
        assertEquals(0, discountPercent)
    }

    @Test
    fun calculateSavings_handlesNoDiscount() {
        val (savings, discountPercent) = CurrencyUtils.calculateSavings(mrpPrice = 500.0, sellingPrice = 500.0)
        assertEquals(0.0, savings, 0.01)
        assertEquals(0, discountPercent)
    }
}
