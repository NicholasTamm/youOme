package com.example.youome.home

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.youome.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreateNewExpenseActivity : AppCompatActivity() {

    private lateinit var descriptionInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var paidBySpinner: Spinner
    private lateinit var splitBetweenSpinner: Spinner
    private lateinit var createExpenseButton: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_expense)
        
        setupViews()
        setupCategoryDropdown()
        setupPaidByDropdown()
        setupSplitBetweenDropdown()
        setupClickListeners()
    }

    private fun setupViews() {
        descriptionInput = findViewById(R.id.description_input)
        amountInput = findViewById(R.id.amount_input)
        categorySpinner = findViewById(R.id.category_spinner)
        paidBySpinner = findViewById(R.id.paid_by_spinner)
        splitBetweenSpinner = findViewById(R.id.split_between_spinner)
        createExpenseButton = findViewById(R.id.create_group_button) // Keep same ID for now
    }

    private fun setupCategoryDropdown() {
        val categories = resources.getStringArray(R.array.categories)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        categorySpinner.adapter = adapter
        categorySpinner.setSelection(5) // Set "Other" as default
    }

    private fun setupPaidByDropdown() {
        // For now, use sample users. In a real app, you'd fetch from database
        val sampleUsers = listOf(
            "John Doe",
            "Jane Smith", 
            "Mike Johnson",
            "Sarah Wilson"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sampleUsers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paidBySpinner.adapter = adapter
        paidBySpinner.setSelection(0) // Set first user as default
    }

    private fun setupSplitBetweenDropdown() {
        // For now, use sample users. In a real app, you'd fetch from database
        val sampleUsers = listOf(
            "All Members",
            "John Doe",
            "Jane Smith", 
            "Mike Johnson",
            "Sarah Wilson"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, sampleUsers)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        splitBetweenSpinner.adapter = adapter
        splitBetweenSpinner.setSelection(0) // Set "All Members" as default
    }

    private fun setupClickListeners() {
        createExpenseButton.setOnClickListener {
            createExpense()
        }
    }

    private fun createExpense() {
        val description = descriptionInput.text.toString().trim()
        val amountText = amountInput.text.toString().trim()
        
        if (description.isEmpty()) {
            Toast.makeText(this, "Please enter expense description", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (amountText.isEmpty()) {
            Toast.makeText(this, "Please enter expense amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        val amount = try {
            amountText.toDouble()
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (amount <= 0) {
            Toast.makeText(this, "Amount must be greater than 0", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get selected values
        val selectedGroupId = intent.getStringExtra("group_id") ?: "1"
        val selectedGroupName = intent.getStringExtra("group_name") ?: "Unknown Group"
        val selectedCategory = categorySpinner.selectedItem.toString()
        val selectedPaidBy = paidBySpinner.selectedItem.toString()
        val selectedSplitBetween = if (splitBetweenSpinner.selectedItem.toString() == "All Members") "ALL" else splitBetweenSpinner.selectedItem.toString()
        
        // TODO: In a real app, you would:
        // 1. Use the selectedGroupId to create the expense
        // 2. Get the actual user ID from the selectedPaidBy
        // 3. Get actual user IDs from the selectedSplitBetween
        // 4. Call the expense repository to create the expense
        
        Toast.makeText(this, "Expense created for $selectedGroupName: $description - $amount\nPaid by: $selectedPaidBy\nSplit between: $selectedSplitBetween", Toast.LENGTH_LONG).show()
        
        // For now, just finish the activity
        finish()
    }
}