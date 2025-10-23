package com.example.youome.home

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youome.R
import com.example.youome.data.model.BalanceSummaryModel
import com.example.youome.data.model.GroupUiModel
import com.example.youome.data.utils.CurrencyUtils
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var groupAdapter: GroupAdapter
    private lateinit var homeRecyclerViewItems: MutableList<HomeRecyclerViewItems>
    private lateinit var greetingText: TextView
    
    // Balance Summary TextViews
    private lateinit var balanceAmountText: TextView
    private lateinit var significantGroupNameText: TextView
    
    // Simple ViewModel with LiveData
    private val viewModel: HomeViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home, container, false)
        
        setupRecyclerView(view)
        setupGreeting(view)
        setupBalanceSummary(view)
        setupGroups()
        
        return view
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning from other activities
        viewModel.refreshBalance()
        viewModel.refreshGroups()
    }

    private fun setupRecyclerView(view: View) {
        val recyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.groups_view)
        
        homeRecyclerViewItems = mutableListOf()
        groupAdapter = GroupAdapter(
            items = homeRecyclerViewItems,
            onGroupClick = { group -> onGroupClick(group) },
            onCreateGroupClick = { onCreateGroupClick() }
        )
        
        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = groupAdapter
        }
    }


    private fun setupGreeting(view: View) {
        greetingText = view.findViewById(R.id.greeting_text)
        
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        
        val greeting = when (currentHour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..23 -> "Good evening"
            else -> "Good night"
        }
        
        // Observe current user from ViewModel using LiveData
        viewModel.currentUser.observe(viewLifecycleOwner, Observer { user ->
            val userName = user?.displayName ?: "User"
            greetingText.text = "$greeting, $userName!"
        })
    }

    private fun setupBalanceSummary(view: View) {
        // Find the balance summary TextViews
        balanceAmountText = view.findViewById(R.id.balance_amount)
        significantGroupNameText = view.findViewById(R.id.significant_group_name)
        
        // Observe balance summary from ViewModel using LiveData
        viewModel.balanceSummaryModel.observe(viewLifecycleOwner, Observer { balanceSummary ->
            balanceSummary?.let { balance ->
                // Update main balance amount
                val balanceText = if (balance.isSettled) {
                    "All Settled!"
                } else {
                    CurrencyUtils.formatAmount(balance.totalBalance, balance.currency)
                }
                balanceAmountText.text = balanceText
                
                // Update significant group name
                significantGroupNameText.text = balance.mostSignificantGroup
            }
        })
    }

    private fun setupGroups() {
        // Observe groups from ViewModel using LiveData
        viewModel.groups.observe(viewLifecycleOwner, Observer { groups ->
            // Clear existing items
            homeRecyclerViewItems.clear()
            
            // Add groups from database
            homeRecyclerViewItems.addAll(groups.map { HomeRecyclerViewItems.GroupItem(it) })
            
            // Add create group item at the end
            homeRecyclerViewItems.add(HomeRecyclerViewItems.CreateGroupItem)
            
            // Notify adapter of changes
            groupAdapter.notifyDataSetChanged()
        })
    }

    private fun onGroupClick(group: GroupUiModel) {
        // Navigate to group details screen
        val intent = Intent(context, GroupDetailsActivity::class.java).apply {
            putExtra("group_id", group.id)
            putExtra("group_name", group.name)
        }
        startActivity(intent)
    }
    
    private fun onCreateGroupClick() {
        // Navigate to create new group screen
        val intent = Intent(context, CreateNewGroupActivity::class.java)
        startActivity(intent)
    }
}
