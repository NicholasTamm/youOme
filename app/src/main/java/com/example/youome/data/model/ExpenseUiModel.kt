package com.example.youome.data.model

data class ExpenseUiModel(
    val id: String,
    val description: String,
    val amount: Double,
    val currency: String,
    val paidBy: String,
    val splitBetween: List<String>,
    val category: String,
    val createdAt: String,
    val groupId: String,
    val userShare: Double = 0.0
)
