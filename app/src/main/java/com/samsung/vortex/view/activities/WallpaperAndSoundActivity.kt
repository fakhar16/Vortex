package com.samsung.vortex.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.OnBackPressedCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.databinding.ActivityWallpaperAndSoundBinding
import com.samsung.vortex.utils.Utils

class WallpaperAndSoundActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWallpaperAndSoundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWallpaperAndSoundBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolBar()
        backButtonCallBack()
        handleItemClicks()
    }

    override fun onStart() {
        super.onStart()
        updateBackgroundImage()
    }

    private fun updateBackgroundImage() {
        VortexApplication.chatBgDatabaseReference
            .child(Utils.currentUser!!.uid)
            .child(ChatActivity.receiver.uid)
            .child(getString(R.string.IMAGE_ID))
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val imageId = snapshot.getValue(String::class.java)
                        binding.selectedChatBg.setImageResource(resources.getIdentifier(imageId, "drawable", packageName))
                        Log.i(Utils.TAG, "onDataChange: $imageId")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun handleItemClicks() {
        binding.layoutWallpaper.setOnClickListener {
            sendUserToChatWallPaperActivity()
        }
    }

    private fun sendUserToChatWallPaperActivity() {
        val intent = Intent(this@WallpaperAndSoundActivity, ChatWallpaperActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.enter, R.anim.exit)
    }

    private fun initToolBar() {
        setSupportActionBar(binding.toolBar.mainAppBar)
        supportActionBar!!.title = "Wallpaper & Sound"
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