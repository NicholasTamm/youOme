package com.example.youome.home

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.example.youome.data.model.ExpenseUiModel
import com.example.youome.data.model.GroupUiModel
import com.google.android.material.button.MaterialButton

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var groupNameText: TextView
    private lateinit var paymentSummaryText: TextView
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var addExpenseButton: MaterialButton
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        setupViews()
        setupRecyclerView()
        setupClickListeners()
        loadGroupData()
    }


    private fun setupViews() {
        groupNameText = findViewById(R.id.group_name_text)
        paymentSummaryText = findViewById(R.id.payment_summary_text)
        expensesRecyclerView = findViewById(R.id.expenses_recycler_view)
        addExpenseButton = findViewById(R.id.add_expense_button)
    }

    private fun setupClickListeners() {
        addExpenseButton.setOnClickListener {
            addExpense()
        }
    }

    private fun addExpense() {
        // Navigate to create new expense screen with group information
        val groupId = intent.getStringExtra("group_id") ?: "1"
        val groupName = intent.getStringExtra("group_name") ?: "Unknown Group"
        
        val expenseIntent = Intent(this, CreateNewExpenseActivity::class.java).apply {
            putExtra("group_id", groupId)
            putExtra("group_name", groupName)
        }
        startActivity(expenseIntent)
    }

    private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(emptyList())
        
        expensesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@GroupDetailsActivity)
            adapter = expenseAdapter
        }
    }

    private fun loadGroupData() {
        // Get group data from intent
        val groupName = intent.getStringExtra("group_name") ?: "Unknown Group"
        val groupId = intent.getStringExtra("group_id") ?: "1"
        
        groupNameText.text = groupName
        
        // Load sample expenses for this group
        val sampleExpenses = getSampleExpenses(groupId)
        expenseAdapter = ExpenseAdapter(sampleExpenses)
        expensesRecyclerView.adapter = expenseAdapter
        
        // Calculate and display payment summary
        val paymentSummary = calculatePaymentSummary(sampleExpenses)
        paymentSummaryText.text = paymentSummary
    }

    private fun getSampleExpenses(groupId: String): List<ExpenseUiModel> {
        return listOf(
            ExpenseUiModel(
                id = "1",
                description = "Dinner at Restaurant",
                amount = 45.50,
                currency = "$",
                paidBy = "John Doe",
                splitBetween = listOf("John Doe", "Jane Smith", "Mike Johnson"),
                category = "Food",
                createdAt = "Dec 15, 2024",
                groupId = groupId
            ),
            ExpenseUiModel(
                id = "2",
                description = "Gas for Road Trip",
                amount = 32.00,
                currency = "$",
                paidBy = "Jane Smith",
                splitBetween = listOf("John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson"),
                category = "Travel",
                createdAt = "Dec 14, 2024",
                groupId = groupId
            ),
            ExpenseUiModel(
                id = "3",
                description = "Hotel Room",
                amount = 120.00,
                currency = "$",
                paidBy = "Mike Johnson",
                splitBetween = listOf("John Doe", "Jane Smith", "Mike Johnson"),
                category = "Travel",
                createdAt = "Dec 13, 2024",
                groupId = groupId
            ),
            ExpenseUiModel(
                id = "4",
                description = "Groceries",
                amount = 28.75,
                currency = "$",
                paidBy = "Sarah Wilson",
                splitBetween = listOf("John Doe", "Jane Smith", "Mike Johnson", "Sarah Wilson"),
                category = "Food",
                createdAt = "Dec 12, 2024",
                groupId = groupId
            )
        )
    }

    private fun calculatePaymentSummary(expenses: List<ExpenseUiModel>): String {
        // TODO: Replace with actual user name from user data/preferences
        val currentUser = "John Doe"
        
        var totalOwed = 0.0
        var totalOwing = 0.0
        
        expenses.forEach { expense ->
            val splitAmount = expense.amount / expense.splitBetween.size
            
            if (expense.paidBy == currentUser) {
                // Current user paid, others owe them
                totalOwed += expense.amount - splitAmount
            } else if (expense.splitBetween.contains(currentUser)) {
                // Current user is part of split, owes the payer
                totalOwing += splitAmount
            }
        }
        
        return when {
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
    }
}
