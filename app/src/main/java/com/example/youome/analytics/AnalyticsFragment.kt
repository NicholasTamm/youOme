package com.example.youome.analytics

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.youome.R
import com.example.youome.data.dao.*
import com.example.youome.data.database.YouOmeDatabase
import com.example.youome.data.entities.User
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsFragment : Fragment() {

    private lateinit var viewModel: AnalyticsViewModel
    private lateinit var totalSpentWeek: TextView
    private lateinit var expensesCountWeek: TextView
    private lateinit var avgExpenseWeek: TextView
    private lateinit var topCategoryWeek: TextView
    private lateinit var totalOwedAmount: TextView
    private lateinit var totalOwedToAmount: TextView
    private lateinit var netBalanceAmount: TextView
    private lateinit var thisWeekSpending: TextView
    private lateinit var lastWeekSpending: TextView
    private lateinit var spendingChange: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.analytics, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        viewModel = ViewModelProvider(this)[AnalyticsViewModel::class.java]
        
        totalSpentWeek = view.findViewById(R.id.total_spent_week)
        expensesCountWeek = view.findViewById(R.id.expenses_count_week)
        avgExpenseWeek = view.findViewById(R.id.avg_expense_week)
        topCategoryWeek = view.findViewById(R.id.top_category_week)
        totalOwedAmount = view.findViewById(R.id.total_owed_amount)
        totalOwedToAmount = view.findViewById(R.id.total_owed_to_amount)
        netBalanceAmount = view.findViewById(R.id.net_balance_amount)
        thisWeekSpending = view.findViewById(R.id.this_week_spending)
        lastWeekSpending = view.findViewById(R.id.last_week_spending)
        spendingChange = view.findViewById(R.id.spending_change)
        viewModel.currentUser.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                viewModel.loadAnalyticsData(user.userId)
            }
        }
        
        viewModel.spendingFacts.observe(viewLifecycleOwner) { facts ->
            facts?.let {
                totalSpentWeek.text = formatCurrency(it.totalSpent)
                expensesCountWeek.text = it.expenseCount.toString()
                avgExpenseWeek.text = formatCurrency(it.averageExpense)
                topCategoryWeek.text = it.topCategory ?: "None"
            }
        }
        
        viewModel.debtSummary.observe(viewLifecycleOwner) { summary ->
            summary?.let {
                totalOwedAmount.text = formatCurrency(it.totalOwed)
                totalOwedToAmount.text = formatCurrency(it.totalOwedTo)
                netBalanceAmount.text = formatCurrency(it.netBalance)
                
                // Set color based on net balance
                val color = if (it.netBalance >= 0) android.R.color.holo_green_dark else android.R.color.holo_red_dark
                netBalanceAmount.setTextColor(resources.getColor(color, null))
            }
        }
        
        viewModel.weeklySpending.observe(viewLifecycleOwner) { spending ->
            spending?.let {
                thisWeekSpending.text = formatCurrency(it.thisWeek)
                lastWeekSpending.text = formatCurrency(it.lastWeek)
                
                val change = if (it.lastWeek > 0) {
                    ((it.thisWeek - it.lastWeek) / it.lastWeek * 100).toInt()
                } else {
                    0
                }
                
                val changeText = if (change >= 0) "+$change%" else "$change%"
                spendingChange.text = changeText
                
                val color = if (change >= 0) android.R.color.holo_red_dark else android.R.color.holo_green_dark
                spendingChange.setTextColor(resources.getColor(color, null))
            }
        }
    }
    
    private fun formatCurrency(amount: Double): String {
        val formatter = DecimalFormat("$#,##0.00")
        return formatter.format(amount)
    }
}

data class SpendingFacts(
    val totalSpent: Double,
    val expenseCount: Int,
    val averageExpense: Double,
    val topCategory: String?
)

data class DebtSummary(
    val totalOwed: Double,
    val totalOwedTo: Double,
    val netBalance: Double
)

data class WeeklySpending(
    val thisWeek: Double,
    val lastWeek: Double
)

class AnalyticsViewModel(application: Application) : AndroidViewModel(application) {

    private val userDao: UserDao = YouOmeDatabase.getDatabase(application).userDao()
    private val expenseDao: ExpenseDao = YouOmeDatabase.getDatabase(application).expenseDao()
    private val debtDao: DebtDao = YouOmeDatabase.getDatabase(application).debtDao()

    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    private val _spendingFacts = MutableLiveData<SpendingFacts?>()
    val spendingFacts: LiveData<SpendingFacts?> = _spendingFacts

    private val _debtSummary = MutableLiveData<DebtSummary?>()
    val debtSummary: LiveData<DebtSummary?> = _debtSummary

    private val _weeklySpending = MutableLiveData<WeeklySpending?>()
    val weeklySpending: LiveData<WeeklySpending?> = _weeklySpending

    init {
        getCurrentUser()
    }

    private fun getCurrentUser() {
        viewModelScope.launch {
            try {
                val user = userDao.getCurrentUser()
                _currentUser.postValue(user)
            } catch (e: Exception) {
                _currentUser.postValue(null)
            }
        }
    }

    fun loadAnalyticsData(userId: String) {
        loadSpendingFacts(userId)
        loadDebtSummary(userId)
        loadWeeklySpending(userId)
    }

    private fun loadSpendingFacts(userId: String) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startTime = calendar.timeInMillis

                val totalSpent = expenseDao.getTotalSpendingByUserInRange(userId, startTime, endTime) ?: 0.0
                val expenseCount = expenseDao.getExpenseCountByUserInRange(userId, startTime, endTime)
                val averageExpense = if (expenseCount > 0) totalSpent / expenseCount else 0.0
                val topCategory = expenseDao.getTopCategoryByUserInRange(userId, startTime, endTime)

                val facts = SpendingFacts(
                    totalSpent = totalSpent,
                    expenseCount = expenseCount,
                    averageExpense = averageExpense,
                    topCategory = topCategory
                )
                
                _spendingFacts.postValue(facts)
            } catch (e: Exception) {
                _spendingFacts.postValue(null)
            }
        }
    }

    private fun loadDebtSummary(userId: String) {
        viewModelScope.launch {
            try {
                val totalOwed = debtDao.getTotalOwedAmount(userId) ?: 0.0
                val totalOwedTo = debtDao.getTotalOwedToAmount(userId) ?: 0.0
                val netBalance = totalOwedTo - totalOwed

                val summary = DebtSummary(
                    totalOwed = totalOwed,
                    totalOwedTo = totalOwedTo,
                    netBalance = netBalance
                )
                
                _debtSummary.postValue(summary)
            } catch (e: Exception) {
                _debtSummary.postValue(null)
            }
        }
    }

    private fun loadWeeklySpending(userId: String) {
        viewModelScope.launch {
            try {
                val calendar = Calendar.getInstance()
                val endTime = calendar.timeInMillis
                
                calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val thisWeekStart = calendar.timeInMillis
                
                val thisWeek = expenseDao.getWeeklySpendingByUser(userId, thisWeekStart, endTime) ?: 0.0
                
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                val lastWeekEnd = calendar.timeInMillis
                calendar.add(Calendar.WEEK_OF_YEAR, -1)
                val lastWeekStart = calendar.timeInMillis
                
                val lastWeek = expenseDao.getWeeklySpendingByUser(userId, lastWeekStart, lastWeekEnd) ?: 0.0

                val spending = WeeklySpending(
                    thisWeek = thisWeek,
                    lastWeek = lastWeek
                )
                
                _weeklySpending.postValue(spending)
            } catch (e: Exception) {
                _weeklySpending.postValue(null)
            }
        }
    }
}