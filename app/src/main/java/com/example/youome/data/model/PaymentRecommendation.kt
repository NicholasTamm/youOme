package com.example.youome.data.model

data class PaymentRecommendation(
    val fromUser: String,
    val toUser: String,
    val amount: Double,
    val currency: String = "USD"
)

