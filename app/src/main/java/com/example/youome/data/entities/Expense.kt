package com.example.youome.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = Group::class,
            parentColumns = ["groupId"],
            childColumns = ["groupId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["paidBy"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = false)
    val expenseId: String,
    val groupId: String,
    val description: String,
    val amount: Double,
    val paidBy: String,
    val splitBy: String, // Who the expense is split between (comma-separated user IDs or "ALL")
    val category: String = "General",
    val currency: String = "USD",
    val createdAt: Long = System.currentTimeMillis()
)
