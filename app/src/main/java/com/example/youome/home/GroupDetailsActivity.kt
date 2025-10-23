package com.example.youome.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
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
        expensesRecyclerView = findViewById(R.id.expenses_recycler_view)
        addExpenseButton = findViewById(R.id.add_expense_button)
    }

    private fun setupClickListeners() {
        addExpenseButton.setOnClickListener {
            addExpense()
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
    }

    private fun addExpense() {
        // Navigate to create new expense screen with group information
        val expenseIntent = Intent(this, CreateNewExpenseActivity::class.java).apply {
            putExtra("group_id", groupId)
            putExtra("group_name", groupName)
        }
        startActivity(expenseIntent)
    }
}
