package com.example.youome.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.youome.R
import com.example.youome.data.model.GroupUiModel
import java.util.Calendar

class HomeFragment : Fragment() {

    private lateinit var groupAdapter: GroupAdapter
    private lateinit var homeRecyclerViewItems: MutableList<HomeRecyclerViewItems>
    private lateinit var greetingText: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.home, container, false)
        
        setupRecyclerView(view)
        setupGreeting(view)
        setupSampleData()
        
        return view
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
        
        // TODO: Replace "John" with actual user name from user data/preferences
        val userName = "John"
        greetingText.text = "$greeting, $userName!"
    }

    private fun setupSampleData() {
        // Add some sample groups for demonstration
        val sampleGroups = listOf(
            GroupUiModel(
                id = "1",
                name = "Weekend Trip",
                debtSummary = "Others owe Me: $25.50",
                memberCount = 4,
                debtAmount = 25.50,
                isOwed = true
            ),
            GroupUiModel(
                id = "2", 
                name = "Dinner with Friends",
                debtSummary = "You owe: $15.75",
                memberCount = 6,
                debtAmount = 15.75,
                isOwed = false
            ),
            GroupUiModel(
                id = "3",
                name = "Office Lunch",
                debtSummary = "Others owe Me: $8.25",
                memberCount = 8,
                debtAmount = 8.25,
                isOwed = true
            ),
            GroupUiModel(
                id = "4",
                name = "Movie Night",
                debtSummary = "Settled",
                memberCount = 3,
                debtAmount = 0.0,
                isOwed = true
            ),
            GroupUiModel(
                id = "4",
                name = "Movie Night",
                debtSummary = "Settled",
                memberCount = 3,
                debtAmount = 0.0,
                isOwed = true
            ),
            GroupUiModel(
                id = "4",
                name = "Movie Night",
                debtSummary = "Settled",
                memberCount = 3,
                debtAmount = 0.0,
                isOwed = true
            ),
        )
        
        // Convert groups to HomeRecyclerViewItems.GroupItem and add create group item
        homeRecyclerViewItems.addAll(sampleGroups.map { HomeRecyclerViewItems.GroupItem(it) })
        homeRecyclerViewItems.add(HomeRecyclerViewItems.CreateGroupItem)
        
        groupAdapter.notifyDataSetChanged()
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
