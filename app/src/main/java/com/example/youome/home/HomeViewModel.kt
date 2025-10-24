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
import com.example.youome.data.dao.DebtDao
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
    private val debtDao: DebtDao = YouOmeDatabase.getDatabase(application).debtDao()

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

    private fun calculateBalanceForCurrentUser(currentUser: User?) {
        if (currentUser == null) {
            _balanceSummaryModel.postValue(null)
            return
        }
        
        viewModelScope.launch {
            try {
                val userId = currentUser.userId
                val unsettledDebtsOwedToUser = debtDao.getTotalOwedToAmount(userId) ?: 0.0
                val unsettledDebtsUserOwes = debtDao.getTotalOwedAmount(userId) ?: 0.0
                val totalBalance = unsettledDebtsOwedToUser - unsettledDebtsUserOwes
                
                val allUnsettledDebts = debtDao.getAllUnsettledDebtsByUser(userId).first()
                val groupDebts = mutableMapOf<String, Double>()
                
                allUnsettledDebts.forEach { debt ->
                    val groupId = debt.groupId
                    if (!groupDebts.containsKey(groupId)) {
                        groupDebts[groupId] = 0.0
                    }
                    
                    if (debt.creditorId == userId) {
                        groupDebts[groupId] = groupDebts[groupId]!! + debt.amount
                    } else if (debt.debtorId == userId) {
                        groupDebts[groupId] = groupDebts[groupId]!! - debt.amount
                    }
                }
                
                val largestDebtGroup = groupDebts.maxByOrNull { abs(it.value) }
                val largestDebtGroupId = largestDebtGroup?.key ?: ""
                
                val groupName = if (largestDebtGroupId.isNotEmpty()) {
                    try {
                        val group = groupDao.getGroupById(largestDebtGroupId)
                        group?.name ?: "Unknown Group"
                    } catch (e: Exception) {
                        "Group $largestDebtGroupId"
                    }
                } else {
                    "No Groups"
                }
                
                val balanceSummary = BalanceSummaryModel(
                    totalBalance = totalBalance,
                    currency = "USD",
                    mostSignificantGroup = groupName
                )
                
                _balanceSummaryModel.postValue(balanceSummary)
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error calculating balance: ${e.message}")
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
                val currentUser = userDao.getCurrentUser()
                if (currentUser == null) {
                    _groups.postValue(emptyList())
                    return@launch
                }
                
                val groups = groupDao.getAllGroups().first()
                val groupUiModels = groups.map { group ->
                    GroupUiModel(
                        id = group.groupId,
                        name = group.name,
                        debtSummary = "Loading...",
                        memberCount = 0,
                        debtAmount = 0.0,
                        isOwed = false
                    )
                }
                
                _groups.postValue(groupUiModels)
                calculateDetailedGroupData(groups, currentUser.userId)
                
            } catch (e: Exception) {
                Log.e("HomeViewModel", "Error fetching groups: ${e.message}")
                _groups.postValue(emptyList())
            }
        }
    }

    fun refreshGroups() {
        getGroups()
    }

    fun createGroup(groupName: String, currency: String, memberNames: List<String>) {
        viewModelScope.launch {
            try {
                val currentUser = userDao.getCurrentUser()
                if (currentUser == null) {
                    return@launch
                }

                val groupId = "group_${UUID.randomUUID().toString().substring(0, 8)}"
                val newGroup = Group(
                    groupId = groupId,
                    name = groupName,
                    currency = currency
                )

                groupDao.insertGroup(newGroup)
                val groupMembers = mutableListOf<GroupMember>()
                groupMembers.add(GroupMember(
                    groupId = groupId,
                    userId = currentUser.userId
                ))

                val usersToCreate = mutableListOf<User>()
                memberNames.forEach { memberName ->
                    val memberUserId = "user_${UUID.randomUUID().toString().substring(0, 8)}"
                    val newUser = User(
                        userId = memberUserId,
                        displayName = memberName,
                        email = null,
                        isCurrentUser = false
                    )
                    
                    usersToCreate.add(newUser)
                    groupMembers.add(GroupMember(
                        groupId = groupId,
                        userId = memberUserId
                    ))
                }
                
                userDao.insertUsers(usersToCreate)
                groupMemberDao.insertGroupMembers(groupMembers)
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
            // Get unsettled debts for this group and user
            val unsettledDebtsOwedToUser = debtDao.getTotalOwedToAmountForGroup(currentUserId, groupId) ?: 0.0
            val unsettledDebtsUserOwes = debtDao.getTotalOwedAmountForGroup(currentUserId, groupId) ?: 0.0
            
            // Calculate net balance from debts
            val netBalance = unsettledDebtsOwedToUser - unsettledDebtsUserOwes
            
            val (summary, amount, isOwed) = when {
                netBalance > 0.01 -> Triple("Others owe Me: $${String.format("%.2f", netBalance)}", netBalance, true)
                netBalance < -0.01 -> Triple("You owe: $${String.format("%.2f", -netBalance)}", -netBalance, false)
                else -> Triple("Settled", 0.0, true)
            }
            
            Log.d("HomeViewModel", "Group $groupId debt summary from debts: $summary")
            Triple(summary, amount, isOwed)
            
        } catch (e: Exception) {
            Log.e("HomeViewModel", "Error calculating debt summary for group $groupId: ${e.message}")
            Triple("Error", 0.0, false)
        }
    }

}