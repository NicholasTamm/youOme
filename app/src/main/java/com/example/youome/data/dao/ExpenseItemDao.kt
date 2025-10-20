package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.ExpenseItem
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseItemDao {
    
    // QUERY operations
    @Query("SELECT * FROM expense_items ei JOIN expenses e ON ei.expenseId = e.expenseId WHERE e.groupId = :groupId")
    suspend fun getCurrentExpenseItemsByGroup(groupId: String): List<ExpenseItem>
    
    @Query("SELECT * FROM expense_items WHERE expenseId = :expenseId")
    suspend fun getCurrentExpenseItemsByExpense(expenseId: String): List<ExpenseItem>
    
    @Query("SELECT * FROM expense_items WHERE expenseId = :expenseId")
    fun getExpenseItemsByExpense(expenseId: String): Flow<List<ExpenseItem>>
    
    @Query("SELECT * FROM expense_items WHERE userId = :userId")
    fun getExpenseItemsByUser(userId: String): Flow<List<ExpenseItem>>
    
    @Query("SELECT * FROM expense_items WHERE userId = :userId AND isSettled = 0")
    fun getUnsettledExpenseItemsByUser(userId: String): Flow<List<ExpenseItem>>
    
    @Query("SELECT SUM(amount) FROM expense_items WHERE userId = :userId AND isSettled = 0")
    suspend fun getTotalUnsettledAmountByUser(userId: String): Double?
    
    // INSERT operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItem(expenseItem: ExpenseItem)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpenseItems(expenseItems: List<ExpenseItem>)
    
    // UPDATE operations
    @Update
    suspend fun updateExpenseItem(expenseItem: ExpenseItem)
    
    @Query("UPDATE expense_items SET isSettled = 1, settledAt = :settledAt WHERE expenseId = :expenseId AND userId = :userId")
    suspend fun markExpenseItemAsSettled(expenseId: String, userId: String, settledAt: Long = System.currentTimeMillis())
    
    // DELETE operations
    @Delete
    suspend fun deleteExpenseItem(expenseItem: ExpenseItem)
}
