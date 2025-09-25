package com.example.youome

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class MainActivity : AppCompatActivity() {
    private lateinit var viewPager2: ViewPager2
    private lateinit var tabLayout: TabLayout
    private lateinit var pages: ArrayList<Fragment>
    private lateinit var homePage: HomeFragment
    private lateinit var debtPage : DebtFragment
    private lateinit var analyticsPage : AnalyticsFragment
    private lateinit var settingsPage : SettingsFragment
    public val tabNames : List<String> = listOf("Home" , "Debts" , "Analytics", "Settings")
    private lateinit var myMyFragmentStateAdapter: MyFragmentStateAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)

        viewPager2 = findViewById(R.id.viewpager)
        tabLayout = findViewById(R.id.tabs)

        homePage = HomeFragment()
        debtPage = DebtFragment()
        analyticsPage = AnalyticsFragment()
        settingsPage = SettingsFragment()


        pages = ArrayList()
        pages.add(homePage)
        pages.add(debtPage)
        pages.add(analyticsPage)
        pages.add(settingsPage)


        myMyFragmentStateAdapter = MyFragmentStateAdapter(this, pages)
        viewPager2.adapter = myMyFragmentStateAdapter

        val tabConfigurationStrategy =
            TabLayoutMediator.TabConfigurationStrategy { tab, position ->
                tab.text = tabNames[position]
            }

        val tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager2, tabConfigurationStrategy)
        tabLayoutMediator.attach()
    }
}