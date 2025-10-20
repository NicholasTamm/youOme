package com.example.youome.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.youome.data.dao.*
import com.example.youome.data.entities.*

@Database(
    entities = [
        User::class,
        Group::class,
        GroupMember::class,
        Expense::class,
        ExpenseItem::class,
        Debt::class
    ],
    version = 1,
    exportSchema = false
)
abstract class YouOmeDatabase : RoomDatabase() {
    
    abstract fun userDao(): UserDao
    abstract fun groupDao(): GroupDao
    abstract fun groupMemberDao(): GroupMemberDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseItemDao(): ExpenseItemDao
    abstract fun debtDao(): DebtDao
    
    companion object {
        @Volatile
        private var INSTANCE: YouOmeDatabase? = null
        
        fun getDatabase(context: Context): YouOmeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    YouOmeDatabase::class.java,
                    "youome_database"
                )
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

