package com.example.youome.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "expense_items",
    primaryKeys = ["expenseId", "userId"],
    foreignKeys = [
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["expenseId"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ExpenseItem(
    val expenseId: String,
    val userId: String,
    val amount: Double,
    val isSettled: Boolean = false,
    val settledAt: Long? = null
)
