package com.example.youome.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = false)
    val userId: String,
    val username: String,
    val email: String,
    val displayName: String,
    val phoneNumber: String? = null,
    val profileImageUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isCurrentUser: Boolean = false
)
