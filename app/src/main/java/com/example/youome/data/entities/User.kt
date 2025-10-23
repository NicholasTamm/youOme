package com.example.youome.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    val userId: String,
    val displayName: String,
    val email: String? = null,
    val isCurrentUser: Boolean = false
)
