package com.example.youome.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.example.youome.data.model.ExpenseUiModel
import com.example.youome.data.model.GroupUiModel
import com.example.youome.data.model.PaymentRecommendation
import com.google.android.material.button.MaterialButton

class GroupDetailsActivity : AppCompatActivity() {

    private lateinit var groupNameText: TextView
    private lateinit var paymentSummaryText: TextView
    private lateinit var paymentRecommendationsText: TextView
    private lateinit var expensesRecyclerView: RecyclerView
    private lateinit var addExpenseButton: MaterialButton
    private lateinit var settleDebtButton: MaterialButton
    private lateinit var expenseAdapter: ExpenseAdapter
    private lateinit var groupDetailsViewModel: GroupDetailsViewModel
    
    private var groupId: String = ""
    private var groupName: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_details)

        // Initialize ViewModel
        groupDetailsViewModel = ViewModelProvider(this)[GroupDetailsViewModel::class.java]

        setupViews()
        setupRecyclerView()
        setupClickListeners()
        loadGroupData()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from other activities
        if (::groupDetailsViewModel.isInitialized && groupId.isNotEmpty()) {
            groupDetailsViewModel.loadGroupData(groupId)
        }
    }


    private fun setupViews() {
        groupNameText = findViewById(R.id.group_name_text)
        paymentSummaryText = findViewById(R.id.payment_summary_text)
        paymentRecommendationsText = findViewById(R.id.payment_recommendations_text)
        expensesRecyclerView = findViewById(R.id.expenses_recycler_view)
        addExpenseButton = findViewById(R.id.add_expense_button)
        settleDebtButton = findViewById(R.id.settle_debt_button)
    }

    private fun setupClickListeners() {
        addExpenseButton.setOnClickListener {
            addExpense()
        }
        
        settleDebtButton.setOnClickListener {
            showSettleDebtConfirmation()
        }
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
        groupName = intent.getStringExtra("group_name") ?: "Unknown Group"
        groupId = intent.getStringExtra("group_id") ?: "1"
        
        groupNameText.text = groupName
        
        // Load data using ViewModel
        groupDetailsViewModel.loadGroupData(groupId)
    }

    private fun observeViewModel() {
        // Observe expenses
        groupDetailsViewModel.expenses.observe(this, Observer { expenses ->
            expenseAdapter = ExpenseAdapter(expenses)
            expensesRecyclerView.adapter = expenseAdapter
        })

        // Observe payment summary
        groupDetailsViewModel.paymentSummary.observe(this, Observer { summary ->
            paymentSummaryText.text = summary
        })

        // Observe payment recommendations
        groupDetailsViewModel.paymentRecommendations.observe(this, Observer { recommendations ->
            updatePaymentRecommendations(recommendations)
        })
    }

    private fun addExpense() {
        // Navigate to create new expense screen with group information
        val expenseIntent = Intent(this, CreateNewExpenseActivity::class.java).apply {
            putExtra("group_id", groupId)
            putExtra("group_name", groupName)
        }
        startActivity(expenseIntent)
    }

    private fun showSettleDebtConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Settle All Debts")
            .setMessage("Are you sure you want to settle all outstanding debts for this group? This action cannot be undone.")
            .setPositiveButton("Settle") { _, _ ->
                settleAllDebts()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun settleAllDebts() {
        Log.d("GroupDetailsActivity", "Settling all debts for group: $groupId")
        groupDetailsViewModel.settleAllDebts(groupId)
        Toast.makeText(this, "All debts have been settled!", Toast.LENGTH_LONG).show()
    }

    private fun updatePaymentRecommendations(recommendations: List<PaymentRecommendation>) {
        if (recommendations.isEmpty()) {
            paymentRecommendationsText.visibility = android.view.View.GONE
            return
        }

        val recommendationsText = recommendations.joinToString("\n") { recommendation ->
            when {
                recommendation.fromUser == "You" -> "Pay $${String.format("%.2f", recommendation.amount)} to ${recommendation.toUser}"
                recommendation.toUser == "You" -> "Receive $${String.format("%.2f", recommendation.amount)} from ${recommendation.fromUser}"
                else -> "${recommendation.fromUser} pays $${String.format("%.2f", recommendation.amount)} to ${recommendation.toUser}"
            }
        }

        paymentRecommendationsText.text = recommendationsText
        paymentRecommendationsText.visibility = android.view.View.VISIBLE
    }
}
