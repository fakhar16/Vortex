package com.samsung.vortex.view.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userProfilesImagesReference
import com.samsung.vortex.databinding.ActivitySettingsBinding
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.utils.WhatsappLikeProfilePicPreview
import com.soundcloud.android.crop.Crop
import com.squareup.picasso.Picasso
import java.io.File

class SettingsActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySettingsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolBar()
        handleItemClicks()
        updateProfileNameLimitTextOnFocus()
        backButtonPressed()
    }

    private fun backButtonPressed() {
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.expandedImage.cardView.visibility == View.VISIBLE) {
                    WhatsappLikeProfilePicPreview.dismissPhotoPreview()
                } else {
                    finish()
                }
            }
        })
    }

    private fun updateProfileNameLimitTextOnFocus() {
        binding.setUserName.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                val remainingLimit: Int = 25 - binding.setUserName.text!!.length
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

    private fun handleItemClicks() {
        binding.setUserName.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                updateProfileName()
            }
            false
        }
        binding.setProfileStatus.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                updateProfileStatus()
            }
            false
        }
        binding.editProfileImage.setOnClickListener { Crop.pickImage(this@SettingsActivity) }
        binding.setProfileImage.setOnClickListener {
            val imageFile = Utils.getImageOffline(currentUser!!.image, currentUser!!.uid)
            WhatsappLikeProfilePicPreview.zoomImageFromThumb(
                binding.setProfileImage,
                binding.expandedImage.cardView,
                binding.expandedImage.image,
                binding.toolBar.root.rootView,
                imageFile
            )
        }
    }

    private fun initToolBar() {
        setSupportActionBar(binding.toolBar.mainAppBar)
        supportActionBar!!.title = "Edit Profile"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    override fun onStart() {
        super.onStart()
        setupProfileInfo()
    }

    private fun updateProfileName() {
        if (binding.setUserName.text.toString().isEmpty()) {
            Toast.makeText(this@SettingsActivity, "Please write your user name...", Toast.LENGTH_SHORT).show()
        } else {
            val profileMap = HashMap<String, Any>()
            profileMap[getString(R.string.NAME)] = binding.setUserName.text.toString()
            userDatabaseReference.child(currentUser!!.uid).updateChildren(profileMap)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Error: " + task.exception, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun updateProfileStatus() {
        if (binding.setProfileStatus.text.toString().isEmpty()) {
            Toast.makeText(this@SettingsActivity, "Please write your user name...", Toast.LENGTH_SHORT).show()
        } else {
            val profileMap = HashMap<String, Any>()
            profileMap[getString(R.string.STATUS)] = binding.setProfileStatus.text.toString()
            userDatabaseReference.child(currentUser!!.uid).updateChildren(profileMap)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Toast.makeText(this@SettingsActivity, "Error: " + task.exception, Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun setupProfileInfo() {
        if (currentUser!!.image.isNotEmpty()) {
            Picasso.get().load(Utils.getImageOffline(currentUser!!.image, currentUser!!.uid)).placeholder(R.drawable.profile_image).into(binding.setProfileImage)
        }
        if (currentUser!!.status.isNotEmpty()) {
            binding.setProfileStatus.setText(currentUser!!.status)
        }
        if (currentUser!!.name.isNotEmpty()) {
            binding.setUserName.setText(currentUser!!.name)
            binding.tvPhone.text = currentUser!!.phone_number
        }
    }

    private fun beginCrop(source: Uri?) {
        val destination = Uri.fromFile(File(cacheDir, getString(R.string.CROPPED)))
        Crop.of(source, destination).asSquare().start(this)
    }

    private fun handleCrop(resultCode: Int, result: Intent?) {
        if (resultCode == RESULT_OK) {
            val resultUri = Crop.getOutput(result)
            saveUserProfileImageToFireBaseStorage(resultUri)
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveUserProfileImageToFireBaseStorage(resultUri: Uri) {
        val filePath: StorageReference =
            userProfilesImagesReference.child(currentUser!!.uid + ".jpg")
        val uploadTask = filePath.putFile(resultUri)
        uploadTask.addOnCompleteListener { task: Task<UploadTask.TaskSnapshot?> ->
            if (task.isSuccessful) {
                filePath.downloadUrl
                    .addOnSuccessListener { uri: Uri ->
                        userDatabaseReference.child(currentUser!!.uid)
                            .child(getString(R.string.IMAGE))
                            .setValue(uri.toString())
                            .addOnCompleteListener { task1 ->
                                if (task1.isSuccessful) {
                                    Utils.dismissLoadingBar(this@SettingsActivity, binding.progressbar.root)
                                } else {
                                    Utils.dismissLoadingBar(this@SettingsActivity, binding.progressbar.root)
                                    val message: String = task1.exception.toString()
                                    Toast.makeText(this@SettingsActivity, "Error: $message", Toast.LENGTH_SHORT).show()
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
            Utils.showLoadingBar(this@SettingsActivity, binding.progressbar.root)
            handleCrop(resultCode, data)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

//    @Deprecated("Deprecated in Java")
//    override fun onBackPressed() {
//        super.onBackPressed()
//
//    }
}