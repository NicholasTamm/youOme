package com.example.youome.rankings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.youome.R
import com.example.youome.data.model.DebtRankingModel
import com.example.youome.data.utils.CurrencyUtils

class RankingFragment : Fragment() {

    private lateinit var rankingAdapter: RankingAdapter
    private lateinit var remainingDebtorsRecyclerView: RecyclerView
    
    // Top 3 debtors UI elements
    private lateinit var firstPlaceImage: ImageView
    private lateinit var firstPlaceName: TextView
    private lateinit var firstPlaceAmount: TextView
    private lateinit var firstPlaceRank: TextView
    
    private lateinit var secondPlaceImage: ImageView
    private lateinit var secondPlaceName: TextView
    private lateinit var secondPlaceAmount: TextView
    private lateinit var secondPlaceRank: TextView
    
    private lateinit var thirdPlaceImage: ImageView
    private lateinit var thirdPlaceName: TextView
    private lateinit var thirdPlaceAmount: TextView
    private lateinit var thirdPlaceRank: TextView
    
    private val viewModel: RankingViewModel by viewModels({ requireActivity() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_ranking, container, false)
        
        setupViews(view)
        setupRecyclerView(view)
        observeViewModel()
        
        return view
    }

    override fun onResume() {
        super.onResume()
        Log.d("RankingFragment", "onResume() called - refreshing rankings")
        viewModel.refreshRankings()
    }

    private fun setupViews(view: View) {
        // Top 3 debtors
        firstPlaceImage = view.findViewById(R.id.first_place_image)
        firstPlaceName = view.findViewById(R.id.first_place_name)
        firstPlaceAmount = view.findViewById(R.id.first_place_amount)
        firstPlaceRank = view.findViewById(R.id.first_place_rank)
        
        secondPlaceImage = view.findViewById(R.id.second_place_image)
        secondPlaceName = view.findViewById(R.id.second_place_name)
        secondPlaceAmount = view.findViewById(R.id.second_place_amount)
        secondPlaceRank = view.findViewById(R.id.second_place_rank)
        
        thirdPlaceImage = view.findViewById(R.id.third_place_image)
        thirdPlaceName = view.findViewById(R.id.third_place_name)
        thirdPlaceAmount = view.findViewById(R.id.third_place_amount)
        thirdPlaceRank = view.findViewById(R.id.third_place_rank)
        
        remainingDebtorsRecyclerView = view.findViewById(R.id.remaining_debtors_recycler)
    }

    private fun setupRecyclerView(view: View) {
        rankingAdapter = RankingAdapter(emptyList())
        
        remainingDebtorsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = rankingAdapter
        }
    }

    private fun observeViewModel() {
        // Observe debt rankings
        viewModel.debtRankings.observe(viewLifecycleOwner, Observer { rankings ->
            Log.d("RankingFragment", "Rankings observer triggered with ${rankings.size} rankings")
            updateTopThreeRankings(rankings)
            updateRemainingRankings(rankings)
        })
    }

    private fun updateTopThreeRankings(rankings: List<DebtRankingModel>) {
        // Update first place (most shameful - owes the most)
        if (rankings.isNotEmpty()) {
            val first = rankings[0]
            firstPlaceImage.setImageResource(getRankingImage(1))
            firstPlaceName.text = first.userName
            // Display debt amount as positive (what they owe)
            val debtAmount = -first.totalDebt
            firstPlaceAmount.text = CurrencyUtils.formatAmount(debtAmount, first.currency)
            firstPlaceRank.text = "1st"
            firstPlaceImage.visibility = View.VISIBLE
        } else {
            firstPlaceImage.visibility = View.GONE
        }
        
        // Update second place
        if (rankings.size > 1) {
            val second = rankings[1]
            secondPlaceImage.setImageResource(getRankingImage(2))
            secondPlaceName.text = second.userName
            // Display debt amount as positive (what they owe)
            val debtAmount = -second.totalDebt
            secondPlaceAmount.text = CurrencyUtils.formatAmount(debtAmount, second.currency)
            secondPlaceRank.text = "2nd"
            secondPlaceImage.visibility = View.VISIBLE
        } else {
            secondPlaceImage.visibility = View.GONE
        }
        
        // Update third place
        if (rankings.size > 2) {
            val third = rankings[2]
            thirdPlaceImage.setImageResource(getRankingImage(3))
            thirdPlaceName.text = third.userName
            // Display debt amount as positive (what they owe)
            val debtAmount = -third.totalDebt
            thirdPlaceAmount.text = CurrencyUtils.formatAmount(debtAmount, third.currency)
            thirdPlaceRank.text = "3rd"
            thirdPlaceImage.visibility = View.VISIBLE
        } else {
            thirdPlaceImage.visibility = View.GONE
        }
    }

    private fun updateRemainingRankings(rankings: List<DebtRankingModel>) {
        // Show remaining debtors (4th place and below)
        val remainingRankings = if (rankings.size > 3) {
            rankings.subList(3, rankings.size)
        } else {
            emptyList()
        }
        
        rankingAdapter.updateRankings(remainingRankings)
    }

    private fun getRankingImage(rank: Int): Int {
        return when (rank) {
            1 -> R.drawable.ic_first_place // Gold medal
            2 -> R.drawable.ic_second_place // Silver medal
            3 -> R.drawable.ic_third_place // Bronze medal
            else -> R.drawable.ic_default_avatar
        }
    }
}
