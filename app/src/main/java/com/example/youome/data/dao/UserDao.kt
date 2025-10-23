package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    
    // QUERY operations
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    suspend fun getCurrentUser(): User?
    
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
    
    
    @Query("SELECT * FROM users WHERE displayName = :displayName")
    suspend fun getUsersByDisplayName(displayName: String): List<User>
    
    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): User?
    
    // INSERT operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
    
    // UPDATE operations
    @Update
    suspend fun updateUser(user: User)
    
    // DELETE operations
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("DELETE FROM users WHERE userId = :userId")
    suspend fun deleteUserById(userId: String)
    
    @Query("DELETE FROM users")
    suspend fun deleteAll()
}
