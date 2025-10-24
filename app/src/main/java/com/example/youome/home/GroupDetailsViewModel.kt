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
import com.example.youome.data.entities.Expense
import com.example.youome.data.entities.User
import com.example.youome.data.entities.GroupMember
import com.example.youome.data.entities.Debt
import com.example.youome.data.model.ExpenseUiModel
import com.example.youome.data.model.PaymentRecommendation
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
    private val debtDao: DebtDao = YouOmeDatabase.getDatabase(application).debtDao()

    private val _expenses = MutableLiveData<List<ExpenseUiModel>>()
    val expenses: LiveData<List<ExpenseUiModel>> = _expenses

    private val _groupMembers = MutableLiveData<List<User>>()
    val groupMembers: LiveData<List<User>> = _groupMembers

    private val _paymentSummary = MutableLiveData<String>()
    val paymentSummary: LiveData<String> = _paymentSummary

    private val _paymentRecommendations = MutableLiveData<List<PaymentRecommendation>>()
    val paymentRecommendations: LiveData<List<PaymentRecommendation>> = _paymentRecommendations

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
                        userShare = 0.0 // Will be calculated from debts
                    )
                }
                
                // Calculate user shares from debts for each expense
                val adjustedExpenseUiModels = calculateUserSharesFromDebts(expenseUiModels, groupId)
                
                _expenses.postValue(adjustedExpenseUiModels)
                calculatePaymentSummary(adjustedExpenseUiModels)
                calculatePaymentRecommendations(groupId)
                
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

    private suspend fun calculateUserSharesFromDebts(expenses: List<ExpenseUiModel>, groupId: String): List<ExpenseUiModel> {
        val currentUser = _currentUser.value ?: return expenses
        
        // Get all unsettled debts for this group involving the current user
        val allDebts = debtDao.getDebtsByGroup(groupId).first()
        val userDebts = allDebts.filter { debt -> 
            debt.debtorId == currentUser.userId || debt.creditorId == currentUser.userId
        }
        
        // Calculate total debt amounts per expense
        val expenseDebtMap = mutableMapOf<String, Double>()
        
        // For now, we'll distribute debts proportionally across expenses
        // This is a simplified approach - in a real app you might want to track which debts
        // correspond to which expenses more precisely
        val totalExpenseAmount = expenses.sumOf { it.amount }
        
        if (totalExpenseAmount > 0) {
            expenses.forEach { expense ->
                val expenseRatio = expense.amount / totalExpenseAmount
                val expenseDebtAmount = userDebts.sumOf { debt ->
                    when {
                        debt.creditorId == currentUser.userId -> debt.amount * expenseRatio
                        debt.debtorId == currentUser.userId -> -debt.amount * expenseRatio
                        else -> 0.0
                    }
                }
                expenseDebtMap[expense.id] = expenseDebtAmount
            }
        }
        
        // Apply debt amounts to expenses
        return expenses.map { expense ->
            val userShare = expenseDebtMap[expense.id] ?: 0.0
            expense.copy(userShare = userShare)
        }
    }

    private fun calculatePaymentSummary(expenses: List<ExpenseUiModel>) {
        viewModelScope.launch {
            try {
                val currentUser = _currentUser.value
                if (currentUser == null) {
                    _paymentSummary.postValue("Please log in to see payment summary")
                    return@launch
                }

                val groupId = expenses.firstOrNull()?.groupId ?: ""
                
                // Get unsettled debts for this group and user
                val unsettledDebtsOwedToUser = debtDao.getTotalOwedToAmountForGroup(currentUser.userId, groupId) ?: 0.0
                val unsettledDebtsUserOwes = debtDao.getTotalOwedAmountForGroup(currentUser.userId, groupId) ?: 0.0
                
                // Calculate net balance from debts
                val netOwed = unsettledDebtsOwedToUser
                val netOwing = unsettledDebtsUserOwes

                val summary = when {
                    netOwed > netOwing -> {
                        val netAmount = netOwed - netOwing
                        "Others owe you: $${String.format("%.2f", netAmount)}"
                    }
                    netOwing > netOwed -> {
                        val netAmount = netOwing - netOwed
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
                
                // Calculate and create debt entries
                createDebtEntries(newExpense)
                
                // Refresh expenses to show the new expense
                loadExpenses(groupId)
                
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error creating expense: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private fun createDebtEntries(expense: Expense) {
        viewModelScope.launch {
            try {
                val splitUserIds = expense.splitBy.split(",").map { it.trim() }
                val splitAmount = expense.amount / splitUserIds.size
                
                Log.d("GroupDetailsViewModel", "Creating debt entries for expense: ${expense.description}")
                Log.d("GroupDetailsViewModel", "Paid by: ${expense.paidBy}, Split between: ${splitUserIds.joinToString(", ")}")
                Log.d("GroupDetailsViewModel", "Split amount per person: $splitAmount")
                
                // Create debt entries for each person who owes the payer
                val debtors = splitUserIds.filter { it != expense.paidBy }
                
                if (debtors.isEmpty()) {
                    Log.d("GroupDetailsViewModel", "No debts to create - payer is the only person in split")
                } else {
                    debtors.forEach { debtorId ->
                        // Create debt entry: debtor owes the payer
                        val debt = Debt(
                            groupId = expense.groupId,
                            debtorId = debtorId,
                            creditorId = expense.paidBy,
                            amount = splitAmount,
                            currency = "USD", // TODO: Get from group currency
                            isSettled = false
                        )
                        
                        // Insert the debt
                        debtDao.insertDebt(debt)
                        Log.d("GroupDetailsViewModel", "Created debt: $debtorId owes ${expense.paidBy} $splitAmount")
                    }
                }
                
                Log.d("GroupDetailsViewModel", "Debt creation completed for expense: ${expense.description}")
                
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error creating debt entries: ${e.message}")
            }
        }
    }

    fun refreshExpenses(groupId: String) {
        loadExpenses(groupId)
    }

    fun settleAllDebts(groupId: String) {
        viewModelScope.launch {
            try {
                Log.d("GroupDetailsViewModel", "Settling all debts for group: $groupId")
                
                // Get all unsettled debts for this group
                val unsettledDebts = debtDao.getCurrentUnsettledDebtsByGroup(groupId)
                
                if (unsettledDebts.isEmpty()) {
                    Log.d("GroupDetailsViewModel", "No unsettled debts found for group: $groupId")
                    return@launch
                }
                
                Log.d("GroupDetailsViewModel", "Found ${unsettledDebts.size} unsettled debts to settle")
                
                // Settle each debt
                unsettledDebts.forEach { debt ->
                    debtDao.settleDebt(debt.groupId, debt.debtorId, debt.creditorId)
                    Log.d("GroupDetailsViewModel", "Settled debt: ${debt.debtorId} -> ${debt.creditorId} ($${debt.amount})")
                }
                
                Log.d("GroupDetailsViewModel", "All debts settled successfully for group: $groupId")
                
                // Refresh expenses to update payment summary
                loadExpenses(groupId)
                
                // Clear payment recommendations since all debts are settled
                _paymentRecommendations.postValue(emptyList())
                
            } catch (e: Exception) {
                Log.e("GroupDetailsViewModel", "Error settling debts: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    private suspend fun calculatePaymentRecommendations(groupId: String) {
        try {
            val currentUser = _currentUser.value ?: return
            
            // Get all unsettled debts for this group
            val unsettledDebts = debtDao.getCurrentUnsettledDebtsByGroup(groupId)
            
            if (unsettledDebts.isEmpty()) {
                _paymentRecommendations.postValue(emptyList())
                return
            }
            
            // Calculate net balance for each user
            val userBalances = mutableMapOf<String, Double>()
            val userNames = mutableMapOf<String, String>()
            
            unsettledDebts.forEach { debt ->
                // Initialize user balances
                if (!userBalances.containsKey(debt.debtorId)) {
                    userBalances[debt.debtorId] = 0.0
                }
                if (!userBalances.containsKey(debt.creditorId)) {
                    userBalances[debt.creditorId] = 0.0
                }
                
                // Update balances
                userBalances[debt.debtorId] = userBalances[debt.debtorId]!! - debt.amount
                userBalances[debt.creditorId] = userBalances[debt.creditorId]!! + debt.amount
                
                // Get user names
                val debtor = userDao.getUserById(debt.debtorId)
                val creditor = userDao.getUserById(debt.creditorId)
                debtor?.let { userNames[debt.debtorId] = getFirstName(it.displayName) }
                creditor?.let { userNames[debt.creditorId] = getFirstName(it.displayName) }
            }
            
            // Separate creditors (positive balance) and debtors (negative balance)
            val creditors = userBalances.filter { it.value > 0.01 }.toList().sortedByDescending { it.second }
            val debtors = userBalances.filter { it.value < -0.01 }.toList().sortedBy { it.second }
            
            // Greedy algorithm to minimize transactions
            val recommendations = mutableListOf<PaymentRecommendation>()
            val mutableCreditors = creditors.toMutableList()
            val mutableDebtors = debtors.toMutableList()
            var creditorIndex = 0
            var debtorIndex = 0
            
            while (creditorIndex < mutableCreditors.size && debtorIndex < mutableDebtors.size) {
                val creditor = mutableCreditors[creditorIndex]
                val debtor = mutableDebtors[debtorIndex]
                
                val creditorAmount = creditor.second
                val debtorAmount = -debtor.second // Make positive
                
                val transferAmount = minOf(creditorAmount, debtorAmount)
                
                if (transferAmount > 0.01) {
                    recommendations.add(
                        PaymentRecommendation(
                            fromUser = userNames[debtor.first] ?: debtor.first,
                            toUser = userNames[creditor.first] ?: creditor.first,
                            amount = transferAmount
                        )
                    )
                    
                    // Update balances
                    mutableCreditors[creditorIndex] = creditor.first to (creditorAmount - transferAmount)
                    mutableDebtors[debtorIndex] = debtor.first to -(debtorAmount - transferAmount)
                    
                    // Move to next creditor/debtor if current one is settled
                    if (mutableCreditors[creditorIndex].second <= 0.01) {
                        creditorIndex++
                    }
                    if (mutableDebtors[debtorIndex].second >= -0.01) {
                        debtorIndex++
                    }
                } else {
                    break
                }
            }
            
            // Filter recommendations to only show those involving the current user
            val currentUserRecommendations = recommendations.filter { recommendation ->
                val currentUserName = getFirstName(currentUser.displayName)
                recommendation.fromUser == currentUserName || recommendation.toUser == currentUserName
            }
            
            Log.d("GroupDetailsViewModel", "Generated ${currentUserRecommendations.size} payment recommendations")
            _paymentRecommendations.postValue(currentUserRecommendations)
            
        } catch (e: Exception) {
            Log.e("GroupDetailsViewModel", "Error calculating payment recommendations: ${e.message}")
            _paymentRecommendations.postValue(emptyList())
        }
    }
}
