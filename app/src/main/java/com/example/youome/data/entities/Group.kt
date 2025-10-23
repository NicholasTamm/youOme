package com.example.youome.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = false)
    val groupId: String,
    val name: String,
    val currency: String = "USD",
    val category: String = "Other"
)
