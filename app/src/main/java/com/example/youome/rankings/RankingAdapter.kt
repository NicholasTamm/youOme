package com.example.youome.rankings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.example.youome.data.model.DebtRankingModel
import com.example.youome.data.utils.CurrencyUtils

class RankingAdapter(
    private var rankings: List<DebtRankingModel>
) : RecyclerView.Adapter<RankingAdapter.RankingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RankingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ranking_card, parent, false)
        return RankingViewHolder(view)
    }

    override fun onBindViewHolder(holder: RankingViewHolder, position: Int) {
        val ranking = rankings[position]
        val actualRank = position + 4 // Since top 3 are shown separately
        holder.bind(ranking, actualRank)
    }

    override fun getItemCount(): Int = rankings.size

    fun updateRankings(newRankings: List<DebtRankingModel>) {
        rankings = newRankings
        notifyDataSetChanged()
    }

    inner class RankingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val rankText: TextView = itemView.findViewById(R.id.ranking_rank)
        private val nameText: TextView = itemView.findViewById(R.id.ranking_name)
        private val amountText: TextView = itemView.findViewById(R.id.ranking_amount)

        fun bind(ranking: DebtRankingModel, rank: Int) {
            rankText.text = "${rank}th"
            nameText.text = ranking.userName
            // Display debt amount as positive (what they owe)
            val debtAmount = -ranking.totalDebt
            amountText.text = CurrencyUtils.formatAmount(debtAmount, ranking.currency)
            amountText.setTextColor(itemView.context.getColor(R.color.red)) // Always red for debtors
        }
    }
}
