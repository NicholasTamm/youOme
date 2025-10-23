package com.example.youome.data.utils

object CurrencyUtils {
    
    // Currency symbols mapping (ordered as specified)
    private val currencySymbols = mapOf(
        "CAD" to "$",  // Canadian Dollar - using standard dollar symbol
        "USD" to "$",  // US Dollar
        "EUR" to "€",  // Euro
        "GBP" to "£",  // British Pound
        "AUD" to "$",  // Australian Dollar - using standard dollar symbol
        "CNY" to "¥",  // Chinese Yuan
        "JPY" to "¥",  // Japanese Yen
        "CHF" to "CHF" // Swiss Franc
    )
    
    /**
     * Get currency symbol from currency code
     */
    fun getSymbol(currencyCode: String): String {
        return currencySymbols[currencyCode.uppercase()] ?: "$"
    }
    
    /**
     * Format amount with currency symbol
     */
    fun formatAmount(amount: Double, currencyCode: String): String {
        val symbol = getSymbol(currencyCode)
        return "$symbol${String.format("%.2f", amount)}"
    }
}
