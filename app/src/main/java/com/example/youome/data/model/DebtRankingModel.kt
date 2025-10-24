package com.example.youome.data.model

data class DebtRankingModel(
    val userId: String,
    val userName: String,
    val totalDebt: Double, // Negative = owes money, Positive = owed money
    val currency: String = "USD"
)

