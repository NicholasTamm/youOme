package com.example.youome.data.dao

import androidx.room.*
import com.example.youome.data.entities.GroupMember
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMemberDao {
    
    // QUERY operations
    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId AND isActive = 1")
    suspend fun getGroupMemberCount(groupId: String): Int
    
    @Query("SELECT userId FROM group_members WHERE groupId = :groupId AND isActive = 1")
    fun getGroupMemberIds(groupId: String): Flow<List<String>>
    
    @Query("SELECT * FROM group_members WHERE groupId = :groupId AND isActive = 1")
    fun getGroupMembers(groupId: String): Flow<List<GroupMember>>
    
    @Query("SELECT * FROM group_members WHERE userId = :userId AND isActive = 1")
    fun getGroupsByUser(userId: String): Flow<List<GroupMember>>
    
    // INSERT operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMember(member: GroupMember)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGroupMembers(members: List<GroupMember>)
    
    // UPDATE operations
    @Update
    suspend fun updateGroupMember(member: GroupMember)
    
    @Query("UPDATE group_members SET isActive = 0 WHERE groupId = :groupId AND userId = :userId")
    suspend fun removeGroupMember(groupId: String, userId: String)
    
    // DELETE operations
    @Delete
    suspend fun deleteGroupMember(member: GroupMember)
    
    @Query("DELETE FROM group_members WHERE groupId = :groupId")
    suspend fun deleteAllGroupMembers(groupId: String)
}
