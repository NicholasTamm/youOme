package com.example.youome.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText

class CreateNewGroupActivity : AppCompatActivity() {

    private lateinit var groupNameInput: TextInputEditText
    private lateinit var currencySpinner: Spinner
    private lateinit var memberInput: TextInputEditText
    private lateinit var addMemberButton: MaterialButton
    private lateinit var createGroupButton: MaterialButton
    private lateinit var membersRecyclerView: RecyclerView
    
    private val membersList = mutableListOf<String>()
    private lateinit var membersAdapter: MembersAdapter
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_new_group)
        
        // Initialize ViewModel
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        
        setupViews()
        setupCurrencyDropdown()
        setupMembersRecyclerView()
        setupClickListeners()
    }

    private fun setupViews() {
        groupNameInput = findViewById(R.id.group_name_input)
        currencySpinner = findViewById(R.id.currency_spinner)
        memberInput = findViewById(R.id.member_input)
        addMemberButton = findViewById(R.id.add_member_button)
        createGroupButton = findViewById(R.id.create_group_button)
        membersRecyclerView = findViewById(R.id.members_recycler_view)
    }

    private fun setupCurrencyDropdown() {
        val currencies = resources.getStringArray(R.array.currencies)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencies)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        currencySpinner.adapter = adapter
        currencySpinner.setSelection(0) // Set "USD" as default
    }

    private fun setupMembersRecyclerView() {
        membersAdapter = MembersAdapter(membersList) { memberToRemove ->
            membersList.remove(memberToRemove)
            membersAdapter.notifyDataSetChanged()
        }
        
        membersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CreateNewGroupActivity)
            adapter = membersAdapter
        }
    }

    private fun setupClickListeners() {
        addMemberButton.setOnClickListener {
            addMember()
        }
        
        createGroupButton.setOnClickListener {
            createGroup()
        }
    }

    private fun addMember() {
        val memberText = memberInput.text.toString().trim()
        
        if (memberText.isEmpty()) {
            Toast.makeText(this, "Please enter a member email or phone", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (membersList.contains(memberText)) {
            Toast.makeText(this, "Member already added", Toast.LENGTH_SHORT).show()
            return
        }
        
        membersList.add(memberText)
        membersAdapter.notifyDataSetChanged()
        memberInput.text?.clear()
    }

    private fun createGroup() {
        val groupName = groupNameInput.text.toString().trim()
        
        if (groupName.isEmpty()) {
            Toast.makeText(this, "Please enter group name", Toast.LENGTH_SHORT).show()
            return
        }
        
        if (membersList.isEmpty()) {
            Toast.makeText(this, "Please add at least one member", Toast.LENGTH_SHORT).show()
            return
        }
        
        val selectedCurrency = currencySpinner.selectedItem.toString()
        
        Log.d("CreateNewGroupActivity", "Creating group: $groupName with currency: $selectedCurrency")
        Log.d("CreateNewGroupActivity", "Members: ${membersList.joinToString(", ")}")
        
        // Use ViewModel to create the group
        homeViewModel.createGroup(groupName, selectedCurrency, membersList)
        
        Toast.makeText(this, "Group '$groupName' created successfully!", Toast.LENGTH_LONG).show()
        
        // Finish the activity and return to home
        finish()
    }

    // Adapter for members RecyclerView
    private class MembersAdapter(
        private val members: List<String>,
        private val onRemoveMember: (String) -> Unit
    ) : RecyclerView.Adapter<MembersAdapter.MemberViewHolder>() {

        class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val chip: Chip = itemView as Chip
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
            val chip = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_member_chip, parent, false) as Chip
            return MemberViewHolder(chip)
        }

        override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
            val member = members[position]
            holder.chip.text = member
            
            holder.chip.setOnCloseIconClickListener {
                onRemoveMember(member)
            }
        }

        override fun getItemCount(): Int = members.size
    }
}