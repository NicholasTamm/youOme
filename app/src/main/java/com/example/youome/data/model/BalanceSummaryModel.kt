package com.example.youome.data.model

data class BalanceSummaryModel(
    val totalBalance: Double = 0.0,          // Net balance (positive = others owe you, negative = you owe)

    val currency: String = "USD",
    val mostSignificantGroup: String = ""     // Group with the largest debt
) {
    // Helper properties for UI
    val isOwed: Boolean get() = totalBalance > 0
    val isOwing: Boolean get() = totalBalance < 0
    val isSettled: Boolean get() = kotlin.math.abs(totalBalance) < 0.01
}
