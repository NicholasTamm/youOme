package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    // QUERY operations
    @Query("SELECT * FROM expenses")
    suspend fun getAllExpenses(): List<Expense>

    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAt DESC")
    suspend fun getCurrentExpensesByGroup(groupId: String): List<Expense>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId ORDER BY createdAt DESC")
    fun getExpensesByGroup(groupId: String): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND paidBy = :userId ORDER BY createdAt DESC")
    fun getExpensesByGroupAndUser(groupId: String, userId: String): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    suspend fun getExpenseById(expenseId: String): Expense?
    
    @Query("SELECT * FROM expenses WHERE paidBy = :userId ORDER BY createdAt DESC")
    fun getExpensesByUser(userId: String): Flow<List<Expense>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE groupId = :groupId")
    suspend fun getTotalExpenseAmountByGroup(groupId: String): Double?
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY createdAt DESC")
    fun getExpensesByCategory(category: String): Flow<List<Expense>>
    
    // INSERT operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)
    
    // UPDATE operations
    @Update
    suspend fun updateExpense(expense: Expense)
    
    // DELETE operations
    @Delete
    suspend fun deleteExpense(expense: Expense)
    
    @Query("DELETE FROM expenses WHERE expenseId = :expenseId")
    suspend fun deleteExpenseById(expenseId: String)
    
    @Query("DELETE FROM expenses WHERE groupId = :groupId")
    suspend fun deleteExpensesByGroup(groupId: String)
    
    @Query("DELETE FROM expenses")
    suspend fun deleteAll()
}
