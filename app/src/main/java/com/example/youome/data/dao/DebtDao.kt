package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.Debt
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    
    // QUERY operations
    
    @Query("""
        SELECT * FROM debts 
        WHERE (debtorId = :userId OR creditorId = :userId) 
        AND isSettled = 0 
        ORDER BY lastUpdated DESC
    """)
    fun getAllUnsettledDebtsByUser(userId: String): Flow<List<Debt>>
    
    @Query("SELECT * FROM debts WHERE groupId = :groupId AND isSettled = 0")
    suspend fun getCurrentUnsettledDebtsByGroup(groupId: String): List<Debt>
    
    @Query("SELECT * FROM debts WHERE groupId = :groupId AND isSettled = 1")
    suspend fun getCurrentSettledDebtsByGroup(groupId: String): List<Debt>
    
    @Query("SELECT * FROM debts WHERE groupId = :groupId ORDER BY lastUpdated DESC")
    fun getDebtsByGroup(groupId: String): Flow<List<Debt>>
    
    @Query("SELECT * FROM debts WHERE creditorId = :userId AND isSettled = 0")
    fun getUnsettledDebtsByCreditor(userId: String): Flow<List<Debt>>
    
    @Query("SELECT * FROM debts WHERE debtorId = :userId AND isSettled = 0")
    fun getUnsettledDebtsByDebtor(userId: String): Flow<List<Debt>>
    
    @Query("SELECT SUM(amount) FROM debts WHERE creditorId = :userId AND isSettled = 0")
    suspend fun getTotalOwedToAmount(userId: String): Double?
    
    @Query("SELECT SUM(amount) FROM debts WHERE debtorId = :userId AND isSettled = 0")
    suspend fun getTotalOwedAmount(userId: String): Double?
    
    // INSERT operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: Debt)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<Debt>)
    
    // UPDATE operations
    @Update
    suspend fun updateDebt(debt: Debt)
    
    @Query("UPDATE debts SET isSettled = 1, settledAt = :settledAt WHERE groupId = :groupId AND debtorId = :debtorId AND creditorId = :creditorId")
    suspend fun settleDebt(groupId: String, debtorId: String, creditorId: String, settledAt: Long = System.currentTimeMillis())
    
    // DELETE operations
    @Delete
    suspend fun deleteDebt(debt: Debt)
    
    @Query("DELETE FROM debts WHERE groupId = :groupId")
    suspend fun deleteDebtsByGroup(groupId: String)
    
    @Query("DELETE FROM debts WHERE groupId = :groupId AND isSettled = 0")
    suspend fun deleteUnsettledDebtsByGroup(groupId: String)
}
