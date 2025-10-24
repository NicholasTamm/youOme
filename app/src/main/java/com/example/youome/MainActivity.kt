package com.example.youome

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.youome.data.database.YouOmeDatabase
import com.example.youome.data.utils.DatabasePopulator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.youome.home.HomeFragment
import com.example.youome.analytics.AnalyticsFragment
import com.example.youome.rankings.RankingFragment
import com.example.youome.settings.SettingsFragment
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var homeFragment: HomeFragment
    private lateinit var rankingFragment: RankingFragment
    private lateinit var analyticsFragment: AnalyticsFragment
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        // Set up database with test data and current user
        setupDatabase()

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        
        // Initialize fragments
        homeFragment = HomeFragment()
        rankingFragment = RankingFragment()
        analyticsFragment = AnalyticsFragment()
        settingsFragment = SettingsFragment()

        // Set default fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, homeFragment)
            .commit()

        // Handle bottom navigation item selection
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, homeFragment)
                        .commit()
                    true
                }
                R.id.nav_rankings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, rankingFragment)
                        .commit()
                    true
                }
                R.id.nav_analytics -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, analyticsFragment)
                        .commit()
                    true
                }
                R.id.nav_settings -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, settingsFragment)
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupDatabase() {
        lifecycleScope.launch {
            try {
                Log.d("MainActivity", "Getting database instance...")
                val database = YouOmeDatabase.getDatabase(this@MainActivity)
                Log.d("MainActivity", "Database instance obtained: ${database != null}")
                
                // Always populate the database (force refresh)
                Log.d("MainActivity", "Force populating database with fresh data")
                DatabasePopulator.seedDatabase(database)
                
                // Verify final state
                val userDao = database.userDao()
                val currentUserAfterSeed = userDao.getCurrentUser()
                Log.d("MainActivity", "Current user after seed: ${currentUserAfterSeed?.displayName ?: "null"}")
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to setup database: ${e.message}")
                e.printStackTrace()
            }
        }
    }
}