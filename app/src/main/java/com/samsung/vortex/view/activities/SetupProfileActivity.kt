package com.samsung.vortex.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userProfilesImagesReference
import com.samsung.vortex.databinding.ActivitySetupProfileBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import java.io.File

class SetupProfileActivity : AppCompatActivity() {
    private lateinit var currentUserId: String
    private lateinit var binding: ActivitySetupProfileBinding
    private var currentUser: User? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySetupProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = FirebaseAuth.getInstance().uid!!

        initializeFields()
        retrieveUserImage()
        handleItemsClick()
    }

    private fun handleItemsClick() {
        binding.editProfileImage.setOnClickListener { Crop.pickImage(this@SetupProfileActivity) }
        updateProfileNameLimitTextOnFocus()
    }

    private fun updateProfileNameLimitTextOnFocus() {
        binding.setUserName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val remainingLimit: Int = 25 - binding.setUserName.text?.length!!
                binding.profileNameLimiter.visibility = View.VISIBLE
                binding.profileNameLimiter.text = remainingLimit.toString()
            } else {
                binding.profileNameLimiter.visibility = View.GONE
            }
        }
        binding.setUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                val remainingLimit = 25 - editable.length
                binding.profileNameLimiter.text = remainingLimit.toString()
            }
        })
    }

    private fun retrieveUserImage() {
        userDatabaseReference.child(currentUserId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    currentUser = snapshot.getValue(User::class.java)
                    if (snapshot.hasChild(getString(R.string.IMAGE))) {
                        Picasso.get().load(currentUser?.image)
                            .placeholder(R.drawable.profile_image).into(binding.setProfileImage)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun beginCrop(source: Uri?) {
        val destination = Uri.fromFile(File(cacheDir, getString(R.string.CROPPED)))
        Crop.of(source, destination).asSquare().start(this)
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == RESULT_OK) {
            val resultUri: Uri = Crop.getOutput(result)
            saveUserProfileImageToFireBaseStorage(resultUri)
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserProfileImageToFireBaseStorage(resultUri: Uri) {
        val filePath: StorageReference = userProfilesImagesReference.child("$currentUserId.jpg")
        val uploadTask: UploadTask = filePath.putFile(resultUri)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                filePath.downloadUrl.addOnSuccessListener { uri ->
                    userDatabaseReference.child(currentUserId).child(getString(R.string.IMAGE))
                        .setValue(uri.toString())
                        .addOnCompleteListener { task1 ->
                            if (task1.isSuccessful) {
                                Utils.dismissLoadingBar(this@SetupProfileActivity, binding.progressbar.root)
                            } else {
                                Utils.dismissLoadingBar(this@SetupProfileActivity, binding.progressbar.root)
                                val message: String = task1.exception.toString()
                                Toast.makeText(this@SetupProfileActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Crop.REQUEST_PICK && resultCode == RESULT_OK) {
            beginCrop(data!!.data)
        } else if (requestCode == Crop.REQUEST_CROP) {
            binding.progressbar.dialogTitle.text = getString(R.string.UPDATE_PROFILE_IMAGE_TITLE)
            binding.progressbar.dialogDescription.text = getString(R.string.UPDATE_PROFILE_IMAGE_DESCRIPTION)
            Utils.showLoadingBar(this@SetupProfileActivity, binding.progressbar.root)
            handleCrop(resultCode, data)
        }
    }

    private fun initializeFields() {
        setSupportActionBar(binding.settingsToolbar.mainAppBar)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
        supportActionBar!!.setDisplayShowCustomEnabled(true)
        supportActionBar!!.title = "Edit Profile"
    }

    private fun updateProfileName() {
        val setUserName: String = binding.setUserName.text.toString()
        val profileMap = HashMap<String, Any>()
        profileMap[getString(R.string.NAME)] = setUserName
        userDatabaseReference.child(currentUserId).updateChildren(profileMap)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    sendUserToMainActivity()
                    Toast.makeText(this@SetupProfileActivity, "Profile Updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    val message: String = task.exception.toString()
                    Toast.makeText(this@SetupProfileActivity, "Error: $message", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun sendUserToMainActivity() {
        val mainIntent = Intent(this@SetupProfileActivity, MainActivity::class.java)
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(mainIntent)
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.edit_profile_menu, menu)
        menu.findItem(R.id.done).isEnabled = false
        binding.setUserName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun afterTextChanged(editable: Editable) {
                if (editable.isNotEmpty()) {
                    if (!menu.findItem(R.id.done).isEnabled) {
                        menu.findItem(R.id.done).isEnabled = true
                    }
                } else {
                    menu.findItem(R.id.done).isEnabled = false
                }
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.done) {
            updateProfileName()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}