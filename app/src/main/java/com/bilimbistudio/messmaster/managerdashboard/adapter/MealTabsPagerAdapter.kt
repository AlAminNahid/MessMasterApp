package com.bilimbistudio.messmaster.managerdashboard.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.bilimbistudio.messmaster.managerdashboard.FragmentBazarList
import com.bilimbistudio.messmaster.managerdashboard.FragmentMealList

class MealTabsPagerAdapter(hostFragment: Fragment) : FragmentStateAdapter(hostFragment) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> FragmentMealList()
        else -> FragmentBazarList()
    }
}
