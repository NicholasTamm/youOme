package com.example.youome

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class MyFragmentStateAdapter(activity: FragmentActivity, var pageList : ArrayList<Fragment>):
    FragmentStateAdapter(activity){

    override fun createFragment(position: Int): Fragment {
        return pageList[position]
    }

    override fun getItemCount(): Int {
        return pageList.size
    }
}