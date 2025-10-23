package com.example.youome.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.example.youome.data.model.ExpenseUiModel

class ExpenseAdapter(
    private val expenses: List<ExpenseUiModel>
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense_card, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
    }

    override fun getItemCount(): Int = expenses.size

    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val descriptionText: TextView = itemView.findViewById(R.id.expense_description)
        private val amountText: TextView = itemView.findViewById(R.id.expense_amount)
        private val paidByValue: TextView = itemView.findViewById(R.id.paid_by_value)
        private val splitBetweenValue: TextView = itemView.findViewById(R.id.split_between_value)
        private val categoryValue: TextView = itemView.findViewById(R.id.category_value)
        private val dateText: TextView = itemView.findViewById(R.id.expense_date)

        fun bind(expense: ExpenseUiModel) {
            descriptionText.text = expense.description
            amountText.text = "${expense.currency}${String.format("%.2f", expense.amount)}"
            paidByValue.text = expense.paidBy
            splitBetweenValue.text = expense.splitBetween.joinToString(", ")
            categoryValue.text = expense.category
            dateText.text = expense.createdAt
        }
    }
}
