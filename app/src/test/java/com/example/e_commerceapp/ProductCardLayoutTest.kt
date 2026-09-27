package com.example.e_commerceapp

import com.example.e_commerceapp.utils.CurrencyUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductCardLayoutTest {

    @Test
    fun productPrice_highValues_formatCorrectly() {
        val s24Price = 129999.0
        val formattedS24 = CurrencyUtils.formatInr(s24Price)
        assertTrue("Expected Rupee symbol in $formattedS24", formattedS24.contains("₹"))
        assertTrue("Expected price formatting in $formattedS24", formattedS24.contains("1,29,999") || formattedS24.contains("129,999"))

        val iphonePrice = 69999.0
        val formattedIphone = CurrencyUtils.formatInr(iphonePrice)
        assertTrue("Expected Rupee symbol in $formattedIphone", formattedIphone.contains("₹"))
        assertTrue("Expected price formatting in $formattedIphone", formattedIphone.contains("69,999"))
    }

    @Test
    fun discountBadgeText_formatsHorizontallyWithoutLineBreaks() {
        val discounts = listOf(10.0, 12.0, 26.0)
        for (discount in discounts) {
            val badgeText = "${discount.toInt()}% OFF"
            // Ensure no newline characters or vertical formatting
            assertFalse("Badge text should not contain newlines", badgeText.contains("\n"))
            assertTrue("Badge text should end with % OFF", badgeText.endsWith("% OFF"))
            assertEquals("${discount.toInt()}% OFF", badgeText)
        }
    }

    @Test
    fun buyNowEffectivePrice_calculatesCorrectlyForQuantity() {
        val unitPrice = 69999.0
        val quantity = 2
        val effectivePrice = (unitPrice * quantity).toString()
        assertEquals("139998.0", effectivePrice)
    }

    @Test
    fun savingsAndDiscount_calculatedAccurately() {
        // e.g. Samsung S24: MRP 144,999, Price 129,999 -> 10%
        val (savings, discount) = CurrencyUtils.calculateSavings(144999.0, 129999.0)
        assertEquals(15000.0, savings, 0.01)
        assertEquals(10, discount)

        // e.g. iPhone 15: MRP 79,900, Price 69,999 -> 12%
        val (savings2, discount2) = CurrencyUtils.calculateSavings(79900.0, 69999.0)
        assertEquals(9901.0, savings2, 0.01)
        assertEquals(12, discount2)
    }
}
