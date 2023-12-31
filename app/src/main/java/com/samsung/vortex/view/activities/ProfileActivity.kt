package com.samsung.vortex.view.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.bottomsheethandler.ClearChatBottomSheetHandler
import com.samsung.vortex.databinding.ActivityProfileBinding
import com.samsung.vortex.fcm.FCMNotificationSender
import com.samsung.vortex.model.CallLog
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.Notification
import com.samsung.vortex.model.User
import com.samsung.vortex.repository.MessageRepositoryImpl
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.TYPE_VIDEO_CALL
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.utils.WhatsappLikeProfilePicPreview
import com.samsung.vortex.webrtc.CallActivity
import com.squareup.picasso.Picasso

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private lateinit var receiver: User
    private lateinit var receiverId: String
    private lateinit var starMessages: ArrayList<Message>
    private lateinit var mediaMessages: ArrayList<Message>
    private lateinit var docMessages: ArrayList<Message>
    private lateinit var linksMessages: ArrayList<Message>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        receiverId = intent.getStringExtra(getString(R.string.RECEIVER_ID))!!
        val isFromCallLog = intent.getBooleanExtra(getString(R.string.IS_FROM_CALL_LOG), false)

        starMessages = MessageRepositoryImpl.getInstance().getStarredMessagesMatchingReceiver().value!!
        mediaMessages = MessageRepositoryImpl.getInstance().getMediaMessagesMatchingReceiver(receiverId).value!!
        docMessages = MessageRepositoryImpl.getInstance().getDocMessagesMatchingReceiver(receiverId).value!!
        linksMessages = MessageRepositoryImpl.getInstance().getLinksMessagesMatchingReceiver(receiverId).value!!

        if (isFromCallLog) {
            val callLog: CallLog = intent.getSerializableExtra(getString(R.string.CALL_LOG), CallLog::class.java)!!
            binding.callDate.text = Utils.getDateString(callLog.time)
            binding.callTime.text = Utils.getTimeString(callLog.time)
            binding.callDescription.text = String.format("${callLog.type} Video call")
            binding.callLogParent.visibility = View.VISIBLE
        }

        loadUserInfo()
        initToolBar()
        handleItemsClick()
        backButtonCallBack()
    }

    private fun backButtonCallBack() {
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.cardView.visibility == View.VISIBLE) {
                    WhatsappLikeProfilePicPreview.dismissPhotoPreview()
                } else {
                    finish()
                }
            }
        })
    }

    private fun loadUserInfo() {
        userDatabaseReference.child(receiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        receiver = snapshot.getValue(User::class.java)!!
                        updateUserUI()
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    @SuppressLint("SetTextI18n")
    private fun updateUserUI() {
        binding.userName.text = receiver.name
        binding.userPhone.text = receiver.phone_number
        binding.userStatus.text = receiver.status
        Picasso.get().load(Utils.getImageOffline(receiver.image, receiver.uid)).placeholder(R.drawable.profile_image).into(binding.userImage)

        binding.blockUser.text = "Block ${receiver.name}"
        binding.reportUser.text = "Report ${receiver.name}"

        updateStarredMessageCount()
        updateMediaMessageCount()
    }

    private fun updateStarredMessageCount() {
        val starredMessageCount = starMessages.size
        binding.starredMessagesCount.text = if (starredMessageCount == 0) "None" else starredMessageCount.toString()
    }

    private fun updateMediaMessageCount() {
        val mediaMessageCount = mediaMessages.size + docMessages.size + linksMessages.size
        binding.mediaCount.text = if (mediaMessageCount == 0) "None" else mediaMessageCount.toString()
    }

    private fun initToolBar() {
        setSupportActionBar(binding.mainPageToolbar.mainAppBar)
        supportActionBar!!.title = "Contact Info"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    private fun showImagePreview(thumbView: View) {
        val file = Utils.getImageOffline(receiver.image, receiver.uid)
        WhatsappLikeProfilePicPreview.zoomImageFromThumb(
            thumbView,
            binding.cardView,
            binding.image,
            binding.container,
            file
        )
        binding.appBarLayout.visibility = View.GONE
    }

    private fun sendUserToStarActivity() {
        val intent = Intent(this, StarMessageActivity::class.java)
        intent.putExtra(getString(R.string.STAR_MESSAGE_WITH_RECEIVER), true)
        startActivity(intent)
    }

    private fun sendUserToChatActivity() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra(getString(R.string.VISIT_USER_ID), receiverId)
        intent.putExtra(getString(R.string.SEARCH_MESSAGE), true)
        startActivity(intent)
    }

    private fun handleItemsClick() {
        binding.userImage.setOnClickListener { showImagePreview(binding.userImage) }
        binding.starLayout.setOnClickListener { sendUserToStarActivity() }
        binding.search.setOnClickListener { sendUserToChatActivity() }
        binding.videoCall.setOnClickListener { createVideoCall() }
        binding.mediaLayout.setOnClickListener { sendUserToMediaLinksDocsActivity() }
        binding.clearChat.setOnClickListener { showClearChatDialog() }
        binding.contactDetailsLayout.setOnClickListener { showContactDetail() }
        binding.wallpaperLayout.setOnClickListener { sentUserToWallpaperAndSoundActivity() }
    }

    private fun sentUserToWallpaperAndSoundActivity() {
        val intent = Intent(this@ProfileActivity, WallpaperAndSoundActivity::class.java)
        intent.putExtra(getString(R.string.CONTACT_ID), receiverId)
        startActivity(intent)
        overridePendingTransition(R.anim.enter, R.anim.exit)
    }

    private fun showContactDetail() {
        val intent = Intent(this@ProfileActivity, SendContactActivity::class.java)
        intent.putExtra(getString(R.string.CONTACT_ID), receiverId)
        intent.putExtra(getString(R.string.isviewcontactfromprofile), true)
        startActivity(intent)
    }

    private fun showClearChatDialog() {
        ClearChatBottomSheetHandler.start(this, receiverId)
    }

    private fun createVideoCall() {
        val notification = Notification(currentUser!!.name, "Incoming Video Call", TYPE_VIDEO_CALL, currentUser!!.image, receiver.token, currentUser!!.uid, receiver.uid)
        FCMNotificationSender.sendNotification(VortexApplication.application.applicationContext, notification)
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra(VortexApplication.application.getString(R.string.CALLER), currentUser!!.uid)
        intent.putExtra(getString(R.string.RECEIVER), receiver.uid)
        intent.putExtra(getString(R.string.IS_CALL_MADE), true)
        startActivity(intent)
    }

    private fun sendUserToMediaLinksDocsActivity() {
        val intent = Intent(this, MediaLinksDocsActivity::class.java)
        intent.putExtra(getString(R.string.RECEIVER_ID), receiverId)
        startActivity(intent)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}