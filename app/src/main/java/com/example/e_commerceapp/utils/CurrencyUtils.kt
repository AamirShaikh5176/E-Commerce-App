package com.example.e_commerceapp.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Centralized utility for formatting currency in Indian Rupees (INR - ₹)
 * and performing savings and discount calculations.
 */
object CurrencyUtils {
    private val inrLocale = Locale.forLanguageTag("en-IN")

    private val inrFormat: NumberFormat = NumberFormat.getCurrencyInstance(inrLocale).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }

    private val inrFormatWithDecimals: NumberFormat = NumberFormat.getCurrencyInstance(inrLocale).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 2
    }

    /**
     * Formats an amount into Indian Rupee format, e.g., ₹1,499 or ₹1,49,999.
     *
     * @param amount The numerical monetary value.
     * @param showDecimals True if decimals should be displayed (e.g., ₹1,499.50).
     */
    fun formatInr(amount: Double, showDecimals: Boolean = false): String {
        return if (showDecimals) {
            inrFormatWithDecimals.format(amount)
        } else {
            inrFormat.format(amount)
        }
    }

    /**
     * Overload for String representations of price.
     */
    fun formatInr(amountStr: String, showDecimals: Boolean = false): String {
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        return formatInr(amount, showDecimals)
    }

    /**
     * Calculates the savings in INR and percentage discount between MRP and selling price.
     *
     * @param mrpPrice The Maximum Retail Price (original price).
     * @param sellingPrice The actual discounted selling price.
     * @return Pair of (savingsAmountInRupees, discountPercentage).
     */
    fun calculateSavings(mrpPrice: Double, sellingPrice: Double): Pair<Double, Int> {
        val savings = (mrpPrice - sellingPrice).coerceAtLeast(0.0)
        val discountPercent = if (mrpPrice > 0.0 && savings > 0.0) {
            ((savings / mrpPrice) * 100).roundToInt()
        } else {
            0
        }
        return Pair(savings, discountPercent)
    }
}
