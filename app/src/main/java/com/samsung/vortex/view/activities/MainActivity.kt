package com.samsung.vortex.view.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessaging
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.presenceDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.databinding.ActivityMainBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils.Companion.TAG
import com.samsung.vortex.utils.Utils.Companion.currentUser

class MainActivity : AppCompatActivity() {

    private var mAuth: FirebaseAuth? = null
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        mAuth = FirebaseAuth.getInstance()

        checkIfUserIsLogined()

        val navController: NavController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_bottom_navigation)

        NavigationUI.setupWithNavController(binding.navView, navController)
        if (FirebaseAuth.getInstance().uid != null) {
            acquireFcmRegistrationToken()
            setupCurrentUser()
        }
    }

    private fun setupCurrentUser() {
        FirebaseAuth.getInstance().uid?.let {
            userDatabaseReference.child(it)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) currentUser = snapshot.getValue(User::class.java)!!
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }
                })
        }
    }

    private fun acquireFcmRegistrationToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token: String = task.result
                val map: MutableMap<String, Any> =
                    HashMap()
                map[getString(R.string.TOKEN)] = token
                FirebaseAuth.getInstance().uid?.let {
                    userDatabaseReference
                        .child(it)
                        .updateChildren(map)
                }
            } else {
                Log.i(TAG, "acquireFcmRegistrationToken: task failed")
            }
        }
    }

    private fun checkIfUserIsLogined() {
        val currentUser: FirebaseUser? = mAuth?.currentUser
        if (currentUser == null) {
            sendUserToWelcomeActivity()
        } else {
            verifyUserExistence()
        }
    }

    private fun verifyUserExistence() {
        val currentUserId: String = mAuth?.currentUser?.uid!!
        userDatabaseReference.child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.child(getString(R.string.NAME)).exists()) {
                        sendUserToSettingsActivity()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    override fun onResume() {
        super.onResume()
        presenceDatabaseReference
            .child(FirebaseAuth.getInstance().uid!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        presenceDatabaseReference
            .child(FirebaseAuth.getInstance().uid!!)
            .setValue("Offline")
    }

    private fun sendUserToWelcomeActivity() {
        val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        finish()
    }

    private fun sendUserToSettingsActivity() {
        val settingsIntent = Intent(this@MainActivity, SetupProfileActivity::class.java)
        settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        settingsIntent.putExtra(
            getString(R.string.PHONE_NUMBER),
            intent.getStringExtra(getString(R.string.PHONE_NUMBER))
        )
        startActivity(settingsIntent)
    }
}