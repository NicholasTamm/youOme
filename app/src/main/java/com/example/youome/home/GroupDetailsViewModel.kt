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
import com.example.youome.data.entities.Expense
import com.example.youome.data.entities.User
import com.example.youome.data.entities.GroupMember
import com.example.youome.data.model.ExpenseUiModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import java.util.UUID
import java.text.SimpleDateFormat
import java.util.*

class GroupDetailsViewModel(application: Application) : AndroidViewModel(application) {

    private val expenseDao: ExpenseDao = YouOmeDatabase.getDatabase(application).expenseDao()
    private val userDao: UserDao = YouOmeDatabase.getDatabase(application).userDao()
    private val groupDao: GroupDao = YouOmeDatabase.getDatabase(application).groupDao()
    private val groupMemberDao: GroupMemberDao = YouOmeDatabase.getDatabase(application).groupMemberDao()

    private val _expenses = MutableLiveData<List<ExpenseUiModel>>()
    val expenses: LiveData<List<ExpenseUiModel>> = _expenses

    private val _groupMembers = MutableLiveData<List<User>>()
    val groupMembers: LiveData<List<User>> = _groupMembers

    private val _paymentSummary = MutableLiveData<String>()
    val paymentSummary: LiveData<String> = _paymentSummary

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    fun loadGroupData(groupId: String) {
        getCurrentUser()
        loadExpenses(groupId)
        loadGroupMembers(groupId)
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userDao.getCurrentUser()
                Log.d("GroupDetailsViewModel", "Current user: ${user?.displayName ?: "null"}")
                _currentUser.postValue(user)
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error getting current user: ${e.message}")
                _currentUser.postValue(null)
            }
        }
    }

    private fun loadExpenses(groupId: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupDetailsViewModel", "Loading expenses for group: $groupId")
                
                val groupExpenses = expenseDao.getExpensesByGroup(groupId).first()
                Log.d("GroupDetailsViewModel", "Found ${groupExpenses.size} expenses in database")
                
                // Get group members to map user IDs to display names
                val groupMembers = groupMemberDao.getGroupMembers(groupId).first()
                val userMap = mutableMapOf<String, String>()
                
                for (member in groupMembers) {
                    val user = userDao.getUserById(member.userId)
                    if (user != null) {
                        userMap[user.userId] = user.displayName
                    }
                }
                
                Log.d("GroupDetailsViewModel", "Created user map with ${userMap.size} users")
                
                // Convert database expenses to UI models with first names only
                val expenseUiModels = groupExpenses.map { expense ->
                    val splitUserIds = expense.splitBy.split(",").map { it.trim() }
                    val splitFirstNames = splitUserIds.map { userId -> 
                        getFirstName(userMap[userId] ?: userId)
                    }
                    
                    // Calculate user's share for this expense
                    val currentUserFirstName = getFirstName(_currentUser.value?.displayName ?: "")
                    val userShare = calculateUserShare(expense, splitUserIds, currentUserFirstName, groupMembers.size)
                    
                    ExpenseUiModel(
                        id = expense.expenseId,
                        description = expense.description,
                        amount = expense.amount,
                        currency = "$", // TODO: Get from group currency
                        paidBy = getFirstName(userMap[expense.paidBy] ?: expense.paidBy), // Use first name only
                        splitBetween = if (splitUserIds.size == groupMembers.size) {
                            listOf("All(${groupMembers.size})")
                        } else {
                            splitFirstNames
                        }, 
                        category = expense.category,
                        createdAt = formatDate(expense.createdAt),
                        groupId = expense.groupId,
                        userShare = userShare
                    )
                }
                
                _expenses.postValue(expenseUiModels)
                calculatePaymentSummary(expenseUiModels)
                
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error loading expenses: ${e.message}")
                e.printStackTrace()
                _expenses.postValue(emptyList())
            }
        }
    }

    private fun loadGroupMembers(groupId: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupDetailsViewModel", "Loading members for group: $groupId")
                
                val groupMembers = groupMemberDao.getGroupMembers(groupId).first()
                Log.d("GroupDetailsViewModel", "Found ${groupMembers.size} group members")
                
                // Get user details for each member
                val users = mutableListOf<User>()
                for (member in groupMembers) {
                    val user = userDao.getUserById(member.userId)
                    if (user != null) {
                        users.add(user)
                    }
                }
                
                _groupMembers.postValue(users)
                
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error loading group members: ${e.message}")
                e.printStackTrace()
                _groupMembers.postValue(emptyList())
            }
        }
    }


    private fun formatDate(timestamp: Long): String {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return dateFormat.format(Date(timestamp))
    }

    private fun getFirstName(fullName: String): String {
        return fullName.split(" ").firstOrNull() ?: fullName
    }

    private fun calculateUserShare(expense: Expense, splitUserIds: List<String>, currentUserFirstName: String, groupMembersCount: Int): Double {
        val splitCount = if (splitUserIds.size == groupMembersCount) {
            groupMembersCount
        } else {
            splitUserIds.size
        }
        val splitAmount = expense.amount / splitCount
        
        // Get current user's first name from the expense data
        val paidByFirstName = getFirstName(_currentUser.value?.displayName ?: "")
        val currentUserId = _currentUser.value?.userId
        
        val userShare = when {
            expense.paidBy == currentUserId -> {
                // User paid, others owe them
                expense.amount - splitAmount
            }
            splitUserIds.size == groupMembersCount -> {
                // Split between all members (including current user)
                -splitAmount
            }
            splitUserIds.contains(currentUserId) -> {
                // User is part of split, owes the payer
                -splitAmount
            }
            else -> {
                // User not involved in this expense
                0.0
            }
        }
        
        return userShare
    }

    private fun calculatePaymentSummary(expenses: List<ExpenseUiModel>) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value
                if (currentUser == null) {
                    _paymentSummary.postValue("Please log in to see payment summary")
                    return@launch
                }

                // Get group members count for "All(#)" calculations
                val groupMembersList = groupMemberDao.getGroupMembers(expenses.firstOrNull()?.groupId ?: "").first()
                val groupMembersCount = groupMembersList.size

                var totalOwed = 0.0
                var totalOwing = 0.0

                expenses.forEach { expense ->
                    val currentUserFirstName = getFirstName(currentUser.displayName)
                    
                    // Determine actual number of people in split and if current user is included
                    val actualSplitCount = if (expense.splitBetween.contains("All($groupMembersCount)")) {
                        groupMembersCount
                    } else {
                        expense.splitBetween.size
                    }
                    
                    val isCurrentUserInSplit = if (expense.splitBetween.contains("All($groupMembersCount)")) {
                        // If split between all members, current user is included
                        true
                    } else {
                        // Check if current user's first name is in the split list
                        expense.splitBetween.contains(currentUserFirstName)
                    }
                    
                    val splitAmount = expense.amount / actualSplitCount

                    // Compare by first name since ExpenseUiModel now contains first names only
                    if (expense.paidBy == currentUserFirstName) {
                        // Current user paid, others owe them
                        totalOwed += expense.amount - splitAmount
                    } else if (isCurrentUserInSplit) {
                        // Current user is part of split, owes the payer
                        totalOwing += splitAmount
                    }
                }

                val summary = when {
                    totalOwed > totalOwing -> {
                        val netAmount = totalOwed - totalOwing
                        "Others owe you: $${String.format("%.2f", netAmount)}"
                    }
                    totalOwing > totalOwed -> {
                        val netAmount = totalOwing - totalOwed
                        "You owe others: $${String.format("%.2f", netAmount)}"
                    }
                    else -> "All settled up!"
                }

                _paymentSummary.postValue(summary)

            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error calculating payment summary: ${e.message}")
                _paymentSummary.postValue("Error calculating summary")
            }
        }
    }

    fun createExpense(
        groupId: String,
        description: String,
        amount: Double,
        category: String,
        paidByUserId: String,
        splitByUserIds: List<String>
    ) {
        viewModelScope.launch {
            try {
                Log.d("GroupDetailsViewModel", "Creating expense: $description for group: $groupId")
                
                // Generate unique expense ID
                val expenseId = "expense_${UUID.randomUUID().toString().substring(0, 8)}"
                
                // Create the expense
                val newExpense = Expense(
                    expenseId = expenseId,
                    groupId = groupId,
                    description = description,
                    amount = amount,
                    paidBy = paidByUserId,
                    splitBy = splitByUserIds.joinToString(","),
                    category = category,
                    createdAt = System.currentTimeMillis()
                )
                
                // Insert the expense
                expenseDao.insertExpense(newExpense)
                Log.d("GroupDetailsViewModel", "Created expense: $expenseId")
                
                // Refresh expenses to show the new expense
                loadExpenses(groupId)
                
                Log.d("GroupDetailsViewModel", "Expense creation completed successfully")
                
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error creating expense: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    fun refreshExpenses(groupId: String) {
        loadExpenses(groupId)
    }
}
