package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    // QUERY operations
    @Query("SELECT * FROM groups WHERE isActive = 1 ORDER BY updatedAt DESC")
    fun getAllActiveGroups(): Flow<List<Group>>
    
    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun getGroupById(groupId: String): Group?
    
    @Query("SELECT * FROM groups WHERE createdBy = :userId ORDER BY updatedAt DESC")
    fun getGroupsByCreator(userId: String): Flow<List<Group>>
    
    @Query("""
        SELECT g.* FROM groups g 
        INNER JOIN group_members gm ON g.groupId = gm.groupId 
        WHERE gm.userId = :userId AND g.isActive = 1 AND gm.isActive = 1 
        ORDER BY g.updatedAt DESC
    """)
    fun getGroupsByMember(userId: String): Flow<List<Group>>
    
    // INSERT operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroup(group: Group)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroups(groups: List<Group>)
    
    // UPDATE operations
    @Update
    suspend fun updateGroup(group: Group)
    
    @Query("UPDATE groups SET isActive = 0 WHERE groupId = :groupId")
    suspend fun deactivateGroup(groupId: String)
    
    // DELETE operations
    @Delete
    suspend fun deleteGroup(group: Group)
}
