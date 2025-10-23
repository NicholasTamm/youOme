package com.example.youome.home

import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.Observer
import com.example.youome.R
import com.example.youome.data.entities.User
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class CreateNewExpenseActivity : AppCompatActivity() {

    private lateinit var descriptionInput: TextInputEditText
    private lateinit var amountInput: TextInputEditText
    private lateinit var categorySpinner: Spinner
    private lateinit var paidBySpinner: Spinner
    private lateinit var splitBetweenSpinner: Spinner
    private lateinit var createExpenseButton: MaterialButton
    private lateinit var groupDetailsViewModel: GroupDetailsViewModel
    
    private var groupId: String = ""
    private var groupName: String = ""
    private var groupMembers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_expense)
        
        // Initialize ViewModel
        groupDetailsViewModel = ViewModelProvider(this)[GroupDetailsViewModel::class.java]
        
        // Get group data from intent
        groupId = intent.getStringExtra("group_id") ?: "1"
        groupName = intent.getStringExtra("group_name") ?: "Unknown Group"
        
        setupViews()
        setupCategoryDropdown()
        observeViewModel()
        setupClickListeners()
        
        // Load group members
        groupDetailsViewModel.loadGroupData(groupId)
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

    private fun observeViewModel() {
        // Observe group members
        groupDetailsViewModel.groupMembers.observe(this, Observer { members ->
            Log.d("CreateNewExpenseActivity", "Received ${members.size} group members")
            groupMembers = members
            setupPaidByDropdown()
            setupSplitBetweenDropdown()
        })
    }

    private fun setupPaidByDropdown() {
        if (groupMembers.isEmpty()) {
            Log.w("CreateNewExpenseActivity", "No group members available yet")
            return
        }
        
        val memberNames = groupMembers.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, memberNames)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        paidBySpinner.adapter = adapter
        paidBySpinner.setSelection(0) // Set first member as default
    }

    private fun setupSplitBetweenDropdown() {
        if (groupMembers.isEmpty()) {
            Log.w("CreateNewExpenseActivity", "No group members available yet")
            return
        }
        
        val splitOptions = listOf("All Members") + groupMembers.map { it.displayName }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, splitOptions)
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
        
        if (groupMembers.isEmpty()) {
            Toast.makeText(this, "Group members not loaded yet. Please try again.", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Get selected values
        val selectedCategory = categorySpinner.selectedItem.toString()
        val selectedPaidBy = paidBySpinner.selectedItem.toString()
        val selectedSplitBetween = splitBetweenSpinner.selectedItem.toString()
        
        // Find user IDs
        val paidByUser = groupMembers.find { it.displayName == selectedPaidBy }
        if (paidByUser == null) {
            Toast.makeText(this, "Selected payer not found", Toast.LENGTH_SHORT).show()
            return
        }
        
        val splitByUserIds = if (selectedSplitBetween == "All Members") {
            groupMembers.map { it.userId }
        } else {
            val splitByUser = groupMembers.find { it.displayName == selectedSplitBetween }
            if (splitByUser == null) {
                Toast.makeText(this, "Selected split member not found", Toast.LENGTH_SHORT).show()
                return
            }
            listOf(splitByUser.userId)
        }
        
        Log.d("CreateNewExpenseActivity", "Creating expense: $description for group: $groupName")
        Log.d("CreateNewExpenseActivity", "Paid by: $selectedPaidBy, Split between: $selectedSplitBetween")
        
        // Use ViewModel to create the expense
        groupDetailsViewModel.createExpense(
            groupId = groupId,
            description = description,
            amount = amount,
            category = selectedCategory,
            paidByUserId = paidByUser.userId,
            splitByUserIds = splitByUserIds
        )
        
        Toast.makeText(this, "Expense '$description' created successfully!", Toast.LENGTH_LONG).show()
        
        // Finish the activity and return to group details
        finish()
    }
}