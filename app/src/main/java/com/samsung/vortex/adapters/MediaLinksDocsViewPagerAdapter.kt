package com.samsung.vortex.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.samsung.vortex.view.fragments.DocsFragment
import com.samsung.vortex.view.fragments.LinksFragment
import com.samsung.vortex.view.fragments.MediaFragment

class MediaLinksDocsViewPagerAdapter(
    fragmentActivity: FragmentActivity,
    private val receiverId: String
) :
    FragmentStateAdapter(fragmentActivity) {
    override fun createFragment(position: Int): Fragment {
        when (position) {
            0 -> return MediaFragment(receiverId)
            1 -> return LinksFragment(receiverId)
            2 -> return DocsFragment(receiverId)
        }
        return MediaFragment(receiverId)
    }

    override fun getItemCount(): Int {
        return 3
    }
}