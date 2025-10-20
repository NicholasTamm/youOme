package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    
    // QUERY operations
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getCurrentExpensesByGroup(groupId: String): List<Expense>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getExpensesByGroup(groupId: String): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE groupId = :groupId AND paidBy = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getExpensesByGroupAndUser(groupId: String, userId: String): Flow<List<Expense>>
    
    @Query("SELECT * FROM expenses WHERE expenseId = :expenseId")
    suspend fun getExpenseById(expenseId: String): Expense?
    
    @Query("SELECT * FROM expenses WHERE paidBy = :userId AND isDeleted = 0 ORDER BY createdAt DESC")
    fun getExpensesByUser(userId: String): Flow<List<Expense>>
    
    @Query("SELECT SUM(amount) FROM expenses WHERE groupId = :groupId AND isDeleted = 0")
    suspend fun getTotalExpenseAmountByGroup(groupId: String): Double?
    
    // INSERT operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenses(expenses: List<Expense>)
    
    // UPDATE operations
    @Update
    suspend fun updateExpense(expense: Expense)
    
    @Query("UPDATE expenses SET isDeleted = 1 WHERE expenseId = :expenseId")
    suspend fun markExpenseAsDeleted(expenseId: String)
    
    // DELETE operations
    @Delete
    suspend fun deleteExpense(expense: Expense)
}
