package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    
    // QUERY operations
    @Query("SELECT * FROM groups ORDER BY name ASC")
    fun getAllGroups(): Flow<List<Group>>
    
    @Query("SELECT * FROM groups WHERE groupId = :groupId")
    suspend fun getGroupById(groupId: String): Group?
    
    @Query("SELECT * FROM groups WHERE name LIKE :searchQuery ORDER BY name ASC")
    fun searchGroups(searchQuery: String): Flow<List<Group>>
    
    @Query("""
        SELECT g.* FROM groups g 
        INNER JOIN group_members gm ON g.groupId = gm.groupId 
        WHERE gm.userId = :userId 
        ORDER BY g.name ASC
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
    
    // DELETE operations
    @Delete
    suspend fun deleteGroup(group: Group)
    
    @Query("DELETE FROM groups WHERE groupId = :groupId")
    suspend fun deleteGroupById(groupId: String)
    
    @Query("DELETE FROM groups")
    suspend fun deleteAll()
}
