package com.example.youome.rankings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.example.youome.data.entities.User

class MemberSelectionAdapter(
    private val members: List<User>,
    private val onSelectionChanged: (List<String>) -> Unit
) : RecyclerView.Adapter<MemberSelectionAdapter.MemberViewHolder>() {

    private val selectedMembers = mutableSetOf<String>()

    class MemberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberCheckbox: CheckBox = itemView.findViewById(R.id.member_checkbox)
        val memberName: TextView = itemView.findViewById(R.id.member_name)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_member_selection, parent, false)
        return MemberViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemberViewHolder, position: Int) {
        val member = members[position]
        
        holder.memberName.text = member.displayName
        holder.memberCheckbox.isChecked = selectedMembers.contains(member.userId)
        
        holder.memberCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedMembers.add(member.userId)
            } else {
                selectedMembers.remove(member.userId)
            }
            onSelectionChanged(selectedMembers.toList())
        }
    }

    override fun getItemCount(): Int = members.size

    fun selectAll() {
        selectedMembers.clear()
        selectedMembers.addAll(members.map { it.userId })
        notifyDataSetChanged()
        onSelectionChanged(selectedMembers.toList())
    }

    fun clearSelection() {
        selectedMembers.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedMembers.toList())
    }

    fun getSelectedMembers(): List<String> = selectedMembers.toList()
}

