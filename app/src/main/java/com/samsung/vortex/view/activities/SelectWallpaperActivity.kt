package com.samsung.vortex.view.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import com.samsung.vortex.R
import com.samsung.vortex.adapters.ChatWallpaperAdapter
import com.samsung.vortex.databinding.ActivitySelectWallpaperBinding

class SelectWallpaperActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectWallpaperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val type: String = intent.getStringExtra(getString(R.string.selected_type))!!

        initToolBar(type)
        backButtonCallBack()
        setupGridView(type)
    }

    private fun setupGridView(type: String) {
        val imageIds = ArrayList<Int>()
        when (type) {
            getString(R.string.bright) -> {
                imageIds.add(R.drawable.bright1)
                imageIds.add(R.drawable.bright2)
                imageIds.add(R.drawable.bright3)
            }
            getString(R.string.dark) -> {
                imageIds.add(R.drawable.dark1)
                imageIds.add(R.drawable.dark2)
            }
            getString(R.string.solids) -> {
                imageIds.add(R.drawable.dark2)
            }
        }
        binding.wallpaperGrid.adapter = ChatWallpaperAdapter(this, imageIds)
    }

    private fun initToolBar(type: String) {
        setSupportActionBar(binding.toolBar.mainAppBar)
        supportActionBar!!.title = type.uppercase()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    private fun backButtonCallBack() {
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                finish()
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}