package com.example.youome.data.model

data class GroupUiModel(
    val id: String,
    val name: String,
    val debtSummary: String,
    val memberCount: Int,
    val debtAmount: Double = 0.0,
    val isOwed: Boolean = true
)
