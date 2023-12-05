package com.samsung.vortex.view.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.samsung.vortex.R
import com.samsung.vortex.adapters.CameraXViewPagerAdapter
import com.samsung.vortex.databinding.ActivityCameraxBinding

class CameraxActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCameraxBinding
    private var tabsName = arrayOf("Photo", "Video")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCameraxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var tabCount = 2

        if (intent.getBooleanExtra(getString(R.string.IS_FROM_STORIES), false)) tabCount = 1


        val adapter = CameraXViewPagerAdapter(this, tabCount)
        binding.viewPager.adapter = adapter

        TabLayoutMediator(
            binding.tabs, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            tab.text = tabsName[position]
        }.attach()
    }
}