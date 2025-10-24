package com.example.youome.rankings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.youome.data.dao.DebtDao
import com.example.youome.data.dao.UserDao
import com.example.youome.data.database.YouOmeDatabase
import com.example.youome.data.model.DebtRankingModel
import kotlinx.coroutines.launch

class RankingViewModel(application: Application) : AndroidViewModel(application) {

    private val debtDao: DebtDao = YouOmeDatabase.getDatabase(application).debtDao()
    private val userDao: UserDao = YouOmeDatabase.getDatabase(application).userDao()

    private val _debtRankings = MutableLiveData<List<DebtRankingModel>>()
    val debtRankings: LiveData<List<DebtRankingModel>> = _debtRankings

    init {
        refreshRankings()
    }

    fun refreshRankings() {
        viewModelScope.launch {
            try {
                Log.d("RankingViewModel", "Refreshing debt rankings...")
                calculateDebtRankings()
            } catch (e: Exception) {
                Log.e("RankingViewModel", "Error refreshing rankings: ${e.message}")
                e.printStackTrace()
                _debtRankings.postValue(emptyList())
            }
        }
    }

    private suspend fun calculateDebtRankings() {
        try {
            val currentUser = userDao.getCurrentUser()
            val allDebts = debtDao.getAllDebts()
            
            val userDebtMap = mutableMapOf<String, Double>()
            val userNames = mutableMapOf<String, String>()
            val userCurrencies = mutableMapOf<String, String>()
            
            currentUser?.let { user ->
                userDebtMap[user.userId] = 0.0
                userNames[user.userId] = user.displayName
                userCurrencies[user.userId] = "USD"
            }
            
            allDebts.forEach { debt ->
                if (!userDebtMap.containsKey(debt.debtorId)) {
                    userDebtMap[debt.debtorId] = 0.0
                }
                if (!userDebtMap.containsKey(debt.creditorId)) {
                    userDebtMap[debt.creditorId] = 0.0
                }
                
                userDebtMap[debt.debtorId] = userDebtMap[debt.debtorId]!! - debt.amount
                userDebtMap[debt.creditorId] = userDebtMap[debt.creditorId]!! + debt.amount
                
                userCurrencies[debt.debtorId] = debt.currency
                userCurrencies[debt.creditorId] = debt.currency
                
                val debtor = userDao.getUserById(debt.debtorId)
                val creditor = userDao.getUserById(debt.creditorId)
                debtor?.let { userNames[debt.debtorId] = it.displayName }
                creditor?.let { userNames[debt.creditorId] = it.displayName }
            }
            
            val allRankings = userDebtMap.map { (userId, debtAmount) ->
                DebtRankingModel(
                    userId = userId,
                    userName = userNames[userId] ?: "Unknown User",
                    totalDebt = debtAmount,
                    currency = userCurrencies[userId] ?: "USD"
                )
            }
            
            val rankings = allRankings.filter { it.totalDebt <= 0 }
                .sortedBy { it.totalDebt }
            
            _debtRankings.postValue(rankings)
            
        } catch (e: Exception) {
            Log.e("RankingViewModel", "Error calculating debt rankings: ${e.message}")
            _debtRankings.postValue(emptyList())
        }
    }
}
