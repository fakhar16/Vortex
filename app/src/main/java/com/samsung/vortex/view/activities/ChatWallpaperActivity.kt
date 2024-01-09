package com.samsung.vortex.view.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.databinding.ActivityChatWallpaperBinding
import com.samsung.vortex.utils.Utils.Companion.currentUser

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

    override fun onStart() {
        super.onStart()
        removeCustomWallpaper()
    }

    private fun removeCustomWallpaper() {
        VortexApplication.chatBgDatabaseReference
            .child(currentUser!!.uid)
            .child(ChatActivity.receiver.uid)
            .child(getString(R.string.IMAGE_ID))
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        binding.removeCustomWallpaper.visibility = View.VISIBLE
                    } else {
                        binding.removeCustomWallpaper.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    private fun handleItemClick() {
        binding.bright.setOnClickListener { sendUserToSelectWallpaper(getString(R.string.bright)) }
        binding.dark.setOnClickListener { sendUserToSelectWallpaper(getString(R.string.dark)) }
        binding.solid.setOnClickListener { sendUserToSelectWallpaper(getString(R.string.solids)) }
        binding.removeCustomWallpaper.setOnClickListener { removeCustomWallpaperFromFirebase() }
    }

    private fun removeCustomWallpaperFromFirebase() {
        VortexApplication.chatBgDatabaseReference
            .child(currentUser!!.uid)
            .child(ChatActivity.receiver.uid)
            .removeValue()
            .addOnCompleteListener {
                binding.removeCustomWallpaper.visibility = View.GONE
            }
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