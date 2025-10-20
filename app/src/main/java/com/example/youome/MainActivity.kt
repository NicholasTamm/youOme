package com.example.youome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.youome.home.HomeFragment
import com.example.youome.rankings.RankingsFragment
import com.example.youome.analytics.AnalyticsFragment
import com.example.youome.settings.SettingsFragment

class MainActivity : AppCompatActivity() {
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var homeFragment: HomeFragment
    private lateinit var rankingsFragment: RankingsFragment
    private lateinit var analyticsFragment: AnalyticsFragment
    private lateinit var settingsFragment: SettingsFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        
        // Initialize fragments
        homeFragment = HomeFragment()
        rankingsFragment = RankingsFragment()
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
                        .replace(R.id.fragment_container, rankingsFragment)
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
}