package com.example.youome.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "debts",
    primaryKeys = ["groupId", "debtorId", "creditorId"],
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
            childColumns = ["debtorId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["userId"],
            childColumns = ["creditorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("groupId"), Index("debtorId"), Index("creditorId")]
)
data class Debt(
    val groupId: String,
    val debtorId: String,
    val creditorId: String,
    val amount: Double,
    val currency: String = "USD",
    val isSettled: Boolean = false
)
