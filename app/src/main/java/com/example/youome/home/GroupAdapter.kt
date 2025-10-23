package com.example.youome.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.example.youome.data.model.GroupUiModel

class GroupAdapter(
    private val items: List<HomeRecyclerViewItems>,
    private val onGroupClick: (GroupUiModel) -> Unit = {},
    private val onCreateGroupClick: () -> Unit = {}
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val VIEW_TYPE_GROUP = 0
        private const val VIEW_TYPE_CREATE_GROUP = 1
    }

    class GroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val groupName: TextView = itemView.findViewById(R.id.group_name)
        val debtSummary: TextView = itemView.findViewById(R.id.debt_summary)
        val memberCount: TextView = itemView.findViewById(R.id.member_count)
    }

    class CreateGroupViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is HomeRecyclerViewItems.GroupItem -> VIEW_TYPE_GROUP
            is HomeRecyclerViewItems.CreateGroupItem -> VIEW_TYPE_CREATE_GROUP
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_GROUP -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_group_card, parent, false)
                GroupViewHolder(view)
            }
            VIEW_TYPE_CREATE_GROUP -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_create_group_card, parent, false)
                CreateGroupViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is HomeRecyclerViewItems.GroupItem -> {
                val groupHolder = holder as GroupViewHolder
                val group = item.group
                
                groupHolder.groupName.text = group.name
                groupHolder.debtSummary.text = group.debtSummary
                groupHolder.memberCount.text = group.memberCount.toString()
                
                // Set click listener
                groupHolder.itemView.setOnClickListener {
                    onGroupClick(group)
                }
                
                // Set debt summary color based on whether you're owed or owe money
                val textColor = if (group.isOwed) {
                    groupHolder.itemView.context.getColor(android.R.color.holo_green_dark)
                } else {
                    groupHolder.itemView.context.getColor(android.R.color.holo_red_dark)
                }
                groupHolder.debtSummary.setTextColor(textColor)
            }
            is HomeRecyclerViewItems.CreateGroupItem -> {
                val createGroupHolder = holder as CreateGroupViewHolder
                
                // Set click listener for create group
                createGroupHolder.itemView.setOnClickListener {
                    onCreateGroupClick()
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size
}
