package com.samsung.vortex.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.samsung.vortex.view.fragments.PhotoFragment
import com.samsung.vortex.view.fragments.VideoFragment

class CameraXViewPagerAdapter(fragmentActivity: FragmentActivity, private val count: Int) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return PhotoFragment()
            1 -> return VideoFragment()
        }
        return PhotoFragment()
    }

    override fun getItemCount(): Int {
        return count
    }
}
