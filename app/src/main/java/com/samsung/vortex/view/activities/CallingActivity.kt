package com.samsung.vortex.view.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.videoUserDatabaseReference
import com.samsung.vortex.databinding.ActivityCallingBinding
import com.samsung.vortex.utils.Utils.Companion.INCOMING_CALL_NOTIFICATION_ID
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.webrtc.CallActivity
import com.squareup.picasso.Picasso

class CallingActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallingBinding

     override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupProfileInfo()
        handleButtonClicks()
    }

    private fun setupProfileInfo() {
        binding.profileName.text = intent.getStringExtra(getString(R.string.NAME))
        Picasso.get().load(intent.getStringExtra(getString(R.string.IMAGE))).placeholder(R.drawable.profile_image).into(binding.profileImage)
    }

    private fun handleButtonClicks() {
        binding.btnReject.setOnClickListener {
            videoUserDatabaseReference.child(
                intent.getStringExtra(getString(R.string.FRIEND_USER_NAME))!!).setValue(null)
            NotificationManagerCompat.from(applicationContext).cancel(INCOMING_CALL_NOTIFICATION_ID)
            finish()
        }
        binding.btnAccept.setOnClickListener {
            val intent = Intent(this@CallingActivity, CallActivity::class.java)
            intent.putExtra(getString(R.string.CALL_ACCEPTED), true)
            intent.putExtra(getString(R.string.CALLER), currentUser!!.uid)
            startActivity(intent)
            finish()
        }
    }
}