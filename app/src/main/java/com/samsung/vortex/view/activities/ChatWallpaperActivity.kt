package com.samsung.vortex.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import com.samsung.vortex.R
import com.samsung.vortex.databinding.ActivityChatWallpaperBinding

class ChatWallpaperActivity : AppCompatActivity() {
    private lateinit var binding:ActivityChatWallpaperBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatWallpaperBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolBar()
        backButtonCallBack()
        handleItemClick()
    }

    private fun handleItemClick() {
        binding.bright.setOnClickListener { sendUserToSelectWallpaper(getString(R.string.bright)) }
        binding.dark.setOnClickListener { sendUserToSelectWallpaper(getString(R.string.dark)) }
        binding.solid.setOnClickListener { sendUserToSelectWallpaper(getString(R.string.solids)) }
    }

    private fun sendUserToSelectWallpaper(type: String) {
        val intent = Intent(this@ChatWallpaperActivity, SelectWallpaperActivity::class.java)
        intent.putExtra(getString(R.string.selected_type), type)
        startActivity(intent)
        overridePendingTransition(R.anim.enter, R.anim.exit)
    }

    private fun initToolBar() {
        setSupportActionBar(binding.toolBar.mainAppBar)
        supportActionBar!!.title = "Chat Wallpaper"
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