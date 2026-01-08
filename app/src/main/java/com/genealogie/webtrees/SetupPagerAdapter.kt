package com.genealogie.webtrees

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SetupPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    val fragments = listOf(
        SetupUrlFragment(),
        SetupCredentialsFragment(),
        SetupTreeFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
