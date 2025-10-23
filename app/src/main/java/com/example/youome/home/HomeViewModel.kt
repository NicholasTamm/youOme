package com.example.youome.home

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.youome.data.dao.ExpenseDao
import com.example.youome.data.dao.UserDao
import com.example.youome.data.dao.GroupDao
import com.example.youome.data.dao.GroupMemberDao
import com.example.youome.data.database.YouOmeDatabase
import com.example.youome.data.entities.User
import com.example.youome.data.entities.Group
import com.example.youome.data.entities.GroupMember
import com.example.youome.data.model.BalanceSummaryModel
import com.example.youome.data.model.GroupUiModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.UUID
import kotlin.math.abs

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao: UserDao = YouOmeDatabase.getDatabase(application).userDao()
    private val expenseDao: ExpenseDao = YouOmeDatabase.getDatabase(application).expenseDao()
    private val groupDao: GroupDao = YouOmeDatabase.getDatabase(application).groupDao()
    private val groupMemberDao: GroupMemberDao = YouOmeDatabase.getDatabase(application).groupMemberDao()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _balanceSummaryModel = MutableLiveData<BalanceSummaryModel?>()
    val balanceSummaryModel : LiveData<BalanceSummaryModel?> = _balanceSummaryModel

    private val _groups = MutableLiveData<List<GroupUiModel>>()
    val groups: LiveData<List<GroupUiModel>> = _groups



    init {
        getCurrentUser()
        getGroups()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userDao.getCurrentUser()
                Log.d("HomeViewModel", "Current user: ${user?.displayName ?: "null"}")
                _currentUser.postValue(user)
                
                // Calculate balance once user is loaded
                calculateBalanceForCurrentUser(user)
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error getting current user: ${e.message}")
                _currentUser.postValue(null)
                _balanceSummaryModel.postValue(null)
            }
        }
    }

    // Calculate Balance summary from DB for user
    private fun calculateBalanceForCurrentUser(currentUser: User?) {
        if (currentUser == null) {
            Log.d("HomeViewModel", "No current user - setting balance to null")
            _balanceSummaryModel.postValue(null)
            return
        }
        
        viewModelScope.launch {
            try {
                val userId = currentUser.userId
                Log.d("HomeViewModel", "Calculating balance for user: ${currentUser.displayName} (ID: $userId)")
                
                // Get ALL expenses (not just ones user paid) to calculate proper balance
                val allExpenses = expenseDao.getAllExpenses()
                
                var totalOwedToYou = 0.0
                var totalYouOwe = 0.0
                
                // Map to track debt per group for finding the most significant group
                val groupDebts = mutableMapOf<String, Double>()
                
                
                allExpenses.forEach { expense ->
                    val splitMembers = expense.splitBy.split(",").map { it.trim() }
                    val splitAmount = expense.amount / splitMembers.size
                    
                    // Initialize group debt if not exists
                    if (!groupDebts.containsKey(expense.groupId)) {
                        groupDebts[expense.groupId] = 0.0
                    }
                    
                    if (expense.paidBy == userId) {
                        // Current user paid, others owe them
                        val owedToUser = expense.amount - splitAmount
                        totalOwedToYou += owedToUser
                        groupDebts[expense.groupId] = groupDebts[expense.groupId]!! + owedToUser
                    } else if (splitMembers.contains(userId)) {
                        // Current user is part of split, owes the payer
                        totalYouOwe += splitAmount
                        groupDebts[expense.groupId] = groupDebts[expense.groupId]!! - splitAmount
                    }
                }
                
                // Calculate net balance
                val totalBalance = totalOwedToYou - totalYouOwe
                
                // Find the group with the largest absolute debt
                val largestDebtGroup = groupDebts.maxByOrNull { abs(it.value) }
                val largestDebtGroupId = largestDebtGroup?.key ?: ""
                
                // Get group name for display
                val groupName = if (largestDebtGroupId.isNotEmpty()) {
                    // Get group name from database
                    try {
                        val group = groupDao.getGroupById(largestDebtGroupId)
                        group?.name ?: "Unknown Group"
                    } catch (e: Exception) {
                        "Group $largestDebtGroupId"
                    }
                } else {
                    "No Groups"
                }
                
                Log.d("HomeViewModel", "Balance calculated: $totalBalance, Most significant group: $groupName")
                
                // Create balance summary with original calculation but store group name
                val balanceSummary = BalanceSummaryModel(
                    totalBalance = totalBalance,
                    totalOwedToYou = totalOwedToYou,
                    totalYouOwe = totalYouOwe,
                    currency = "USD",
                    mostSignificantGroup = groupName
                )
                
                _balanceSummaryModel.postValue(balanceSummary)
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error calculating balance: ${e.message}")
                e.printStackTrace()
                // Handle error - set default balance
                _balanceSummaryModel.postValue(BalanceSummaryModel())
            }
        }
    }

    fun refreshBalance() {
        calculateBalanceForCurrentUser(_currentUser.value)
    }

    private fun getGroups() {
        viewModelScope.launch {
            try {
                // Get current user for debt calculations
                val currentUser = userDao.getCurrentUser()
                if (currentUser == null) {
                    _groups.postValue(emptyList())
                    return@launch
                }
                
                // Get all groups from database
                val allGroups = groupDao.getAllGroups()
                
                allGroups.collect { groups ->
                    Log.d("HomeViewModel", "Found ${groups.size} groups in database")
                    
                    // Convert database groups to UI models with basic data first
                    val groupUiModels = groups.map { group ->
                        GroupUiModel(
                            id = group.groupId,
                            name = group.name,
                            debtSummary = "Loading...", // Will be calculated separately
                            memberCount = 0, // Will be calculated separately
                            debtAmount = 0.0, // Will be calculated separately
                            isOwed = false // Will be calculated separately
                        )
                    }
                    
                    Log.d("HomeViewModel", "Converted ${groupUiModels.size} groups to UI models with basic data")
                    _groups.postValue(groupUiModels)
                    
                    // Now calculate detailed data in background
                    calculateDetailedGroupData(groups, currentUser.userId)
                }
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching groups: ${e.message}")
                e.printStackTrace()
                _groups.postValue(emptyList())
            }
        }
    }

    fun refreshGroups() {
        getGroups()
    }

    fun createGroup(groupName: String, currency: String, memberEmails: List<String>) {
        viewModelScope.launch {
            try {
                // Get current user
                val currentUser = userDao.getCurrentUser()
                if (currentUser == null) {
                    return@launch
                }

                // Generate unique group ID
                val groupId = "group_${UUID.randomUUID().toString().substring(0, 8)}"

                // Create the group
                val newGroup = Group(
                    groupId = groupId,
                    name = groupName,
                    currency = currency
                )

                // Insert the group
                groupDao.insertGroup(newGroup)

                // Create group members
                val groupMembers = mutableListOf<GroupMember>()

                // Add current user as a member
                groupMembers.add(GroupMember(
                    groupId = groupId,
                    userId = currentUser.userId
                ))

                // Add other members (for now, we'll create placeholder users)
                memberEmails.forEach { email ->
                    // In a real app, you would look up users by email or create new users
                    // For now, we'll create a placeholder user ID
                    val memberUserId = "user_${UUID.randomUUID().toString().substring(0, 8)}"

                    groupMembers.add(GroupMember(
                        groupId = groupId,
                        userId = memberUserId
                    ))
                }

                // Insert all group members
                groupMemberDao.insertGroupMembers(groupMembers)

                // Refresh groups to show the new group
                refreshGroups()

            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error creating group: ${e.message}")
            }
        }
    }

    private fun calculateDetailedGroupData(groups: List<com.example.youome.data.entities.Group>, currentUserId: String) {
        viewModelScope.launch {
            try {
                Log.d("HomeViewModel", "Calculating detailed data for ${groups.size} groups...")
                
                val updatedGroupUiModels = mutableListOf<GroupUiModel>()
                
                for (group in groups) {
                    try {
                        val memberCount = calculateMemberCount(group.groupId)
                        val debtSummary = calculateGroupDebtSummary(group.groupId, currentUserId)
                        
                        val groupUiModel = GroupUiModel(
                            id = group.groupId,
                            name = group.name,
                            debtSummary = debtSummary.first,
                            memberCount = memberCount,
                            debtAmount = debtSummary.second,
                            isOwed = debtSummary.third
                        )
                        
                        updatedGroupUiModels.add(groupUiModel)
                        Log.d("HomeViewModel", "Processed group: ${group.name} - Members: $memberCount, Debt: ${debtSummary.first}")
                        
                    } catch (e: Exception) {
                        Log.e("HomeViewModel", "Error processing group ${group.name}: ${e.message}")
                        // Add group with default values if calculation fails
                        updatedGroupUiModels.add(GroupUiModel(
                            id = group.groupId,
                            name = group.name,
                            debtSummary = "Error",
                            memberCount = 0,
                            debtAmount = 0.0,
                            isOwed = false
                        ))
                    }
                }
                
                Log.d("HomeViewModel", "Updated ${updatedGroupUiModels.size} groups with detailed data")
                _groups.postValue(updatedGroupUiModels)
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error calculating detailed group data: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun calculateMemberCount(groupId: String): Int {
        return try {
            val members = groupMemberDao.getGroupMembers(groupId).first()
            members.size
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error calculating member count for group $groupId: ${e.message}")
            0
        }
    }

    private suspend fun calculateGroupDebtSummary(groupId: String, currentUserId: String): Triple<String, Double, Boolean> {
        return try {
            // Get all expenses for this group
            val groupExpenses = expenseDao.getExpensesByGroup(groupId).first()
            
            var totalOwedToYou = 0.0
            var totalYouOwe = 0.0
            
            groupExpenses.forEach { expense ->
                val splitMembers = expense.splitBy.split(",").map { it.trim() }
                val splitAmount = expense.amount / splitMembers.size
                
                if (expense.paidBy == currentUserId) {
                    // Current user paid, others owe them
                    totalOwedToYou += expense.amount - splitAmount
                } else if (splitMembers.contains(currentUserId)) {
                    // Current user is part of split, owes the payer
                    totalYouOwe += splitAmount
                }
            }
            
            val netBalance = totalOwedToYou - totalYouOwe
            
            val (summary, amount, isOwed) = when {
                netBalance > 0.01 -> Triple("Others owe Me: $${String.format("%.2f", netBalance)}", netBalance, true)
                netBalance < -0.01 -> Triple("You owe: $${String.format("%.2f", -netBalance)}", -netBalance, false)
                else -> Triple("Settled", 0.0, true)
            }
            
            Log.d("HomeViewModel", "Group $groupId debt summary: $summary")
            Triple(summary, amount, isOwed)
            
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error calculating debt summary for group $groupId: ${e.message}")
            Triple("Error", 0.0, false)
        }
    }

}