package com.samsung.vortex.view.activities

import android.annotation.SuppressLint
import android.content.ClipboardManager
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.webkit.URLUtil
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.presenceDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.adapters.MessagesAdapter
import com.samsung.vortex.bottomsheethandler.ShareContactBottomSheetHandler
import com.samsung.vortex.databinding.ActivityChatBinding
import com.samsung.vortex.databinding.CustomChatBarBinding
import com.samsung.vortex.fcm.FCMNotificationSender
import com.samsung.vortex.interfaces.GoEditTextListener
import com.samsung.vortex.interfaces.MessageListenerCallback
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.Notification
import com.samsung.vortex.model.User
import com.samsung.vortex.swipe.MessageSwipeController
import com.samsung.vortex.swipe.SwipeControllerActions
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.FirebaseUtils.Companion.sendAudioRecording
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.TYPE_VIDEO_CALL
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.utils.Utils.Companion.getFileSize
import com.samsung.vortex.utils.Utils.Companion.getFileType
import com.samsung.vortex.utils.Utils.Companion.getFilename
import com.samsung.vortex.utils.Utils.Companion.hideKeyboard
import com.samsung.vortex.utils.Utils.Companion.hideReplyLayout
import com.samsung.vortex.utils.Utils.Companion.showLoadingBar
import com.samsung.vortex.utils.WhatsappLikeProfilePicPreview
import com.samsung.vortex.viewmodel.MessageViewModel
import com.samsung.vortex.viewmodel.viewmodelfactory.MessageViewModelFactory
import com.samsung.vortex.webrtc.CallActivity
import com.squareup.picasso.Picasso
import com.tougee.recorderview.AudioRecordView
import java.io.File
import java.util.Date
import java.util.Locale

class ChatActivity : AppCompatActivity(), MessageListenerCallback, AudioRecordView.Callback {
    private lateinit var binding: ActivityChatBinding
    private lateinit var customChatBarBinding: CustomChatBarBinding
    private lateinit var messageReceiverId: String

    private lateinit var viewModel: MessageViewModel

    private lateinit var adapter: MessagesAdapter
    private lateinit var bottomSheetDialog: BottomSheetDialog

    var quotedMessageId: String = ""

    companion object {
        lateinit var receiver: User
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeFields()
        initializeReceiver()
        backButtonPressed()
        updateStatusIndicator()
    }

    override fun onStart() {
        super.onStart()
        updateBackgroundImage()
    }

    private fun updateBackgroundImage() {
        VortexApplication.chatBgDatabaseReference
            .child(currentUser!!.uid)
            .child(messageReceiverId)
            .child(getString(R.string.IMAGE_ID))
            .addListenerForSingleValueEvent(object: ValueEventListener {
                @SuppressLint("DiscouragedApi")
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val imageId = snapshot.getValue(String::class.java)
                        binding.chatBg.setBackgroundResource(resources.getIdentifier(imageId, "drawable", packageName))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    private fun initializeFields() {
        binding.recordView.activity = this
        binding.recordView.callback = this

        messageReceiverId = intent.extras!!.getString(getString(R.string.VISIT_USER_ID))!!
        initToolBar()
        setupViewModel()
        setupRecyclerView()
        setupAttachmentBottomSheetMenu()
        handleButtonClicks()
        checkIfSearchMessageTriggered()
        initProgressBar()
        handleMessageEditTextListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, MessageViewModelFactory(currentUser!!.uid, messageReceiverId))[MessageViewModel::class.java]
        viewModel.getMessage()?.observe(this) {
            adapter.notifyDataSetChanged()
//            scrollToMessage()
        }
    }

    private fun setupRecyclerView() {
        binding.userMessageList.layoutManager = LinearLayoutManager(this)
        adapter = MessagesAdapter(this@ChatActivity, viewModel.messages!!.value!!, messageReceiverId)
        binding.userMessageList.adapter = adapter
        binding.userMessageList.addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
            if (bottom < oldBottom) {
                binding.userMessageList.postDelayed({
                    if (adapter.itemCount != 0) binding.userMessageList.smoothScrollToPosition(
                        adapter.itemCount - 1
                    )
                }, 100)
            }
        }

        val messageSwipeController = MessageSwipeController(this, object : SwipeControllerActions {
            override fun showReplyUI(position: Int) {
                hideReplyLayout(binding.replyLayout)
                val quotedMessage = adapter.getMessageAtPos(position)
                quotedMessageId = quotedMessage.messageId

                binding.messageInputText.requestFocus()
                binding.replyLayout.mainLayout.visibility = View.VISIBLE
                binding.replyLayout.replyMessage.text = quotedMessage.message
                if (currentUser!!.uid == quotedMessage.from) {
                    binding.replyLayout.username.text = getString(R.string.you)
                    binding.replyLayout.username.setTextColor(getColor(R.color.sinch_yellow))
                    binding.replyLayout.bar.setBackgroundColor(getColor(R.color.sinch_yellow))
                } else {
                    binding.replyLayout.username.text = receiver.name
                    binding.replyLayout.username.setTextColor(getColor(R.color.color_blue))
                    binding.replyLayout.bar.setBackgroundColor(getColor(R.color.color_blue))
                }

                Utils.setReplyLayoutByMessageType(this@ChatActivity, quotedMessage, binding.replyLayout)
            }
        })

        val itemTouchHelper = ItemTouchHelper(messageSwipeController)
        itemTouchHelper.attachToRecyclerView(binding.userMessageList)
    }

    private fun scrollToMessage() {
        if (adapter.itemCount != 0) {
            if (intent.hasExtra(getString(R.string.MESSAGE_ID))) {
                val position: Int = adapter.getItemPosition(intent.getStringExtra(getString(R.string.MESSAGE_ID)))
                intent.removeExtra(getString(R.string.MESSAGE_ID))
                binding.userMessageList.smoothScrollToPosition(position)

                binding.userMessageList.postDelayed({
                    binding.userMessageList.findViewHolderForAdapterPosition(position)!!
                        .itemView.findViewById<LinearLayout>(R.id.my_linear_layout).backgroundTintList = ContextCompat.getColorStateList(this@ChatActivity, R.color.colorPrimary)
                    Handler(Looper.getMainLooper()).postDelayed({
                        binding.userMessageList.findViewHolderForAdapterPosition(position)!!
                            .itemView.findViewById<LinearLayout>(R.id.my_linear_layout).backgroundTintList = null
                    }, 500)
                }, 200)
            } else {
                binding.userMessageList.smoothScrollToPosition(adapter.itemCount - 1)
            }
        }
    }

    private fun checkIfSearchMessageTriggered() {
        if (intent.getBooleanExtra(getString(R.string.SEARCH_MESSAGE), false)) {
            binding.chatToolBar.root.visibility = View.INVISIBLE
            binding.searchBar.visibility = View.VISIBLE
            binding.search.requestFocus()
        }
        binding.search.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })
        binding.cancel.setOnClickListener {
            binding.search.setQuery("", false)
            binding.chatToolBar.root.visibility = View.VISIBLE
            binding.searchBar.visibility = View.GONE
        }
    }

    private fun initProgressBar() {
        binding.progressbar.dialogTitle.text = getString(R.string.SENDING_FILE_TITLE)
        binding.progressbar.dialogDescription.text = getString(R.string.SENDING_FILE_DESCRIPTION)
    }

    private fun initToolBar() {
        setSupportActionBar(binding.chatToolBar.mainAppBar)
        val actionBar = supportActionBar
        actionBar!!.setDisplayShowCustomEnabled(true)
        actionBar.setDisplayHomeAsUpEnabled(true)
        actionBar.setDisplayShowTitleEnabled(false)
        val layoutInflater = this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
        customChatBarBinding = CustomChatBarBinding.inflate(layoutInflater)
        actionBar.customView = customChatBarBinding.root
    }

    private fun initializeReceiver() {
        userDatabaseReference
            .child(messageReceiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    receiver = snapshot.getValue(User::class.java)!!
                    updateChatBarDetails()
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun updateChatBarDetails() {
        customChatBarBinding.name.text = receiver.name
        Picasso.get().load(Utils.getImageOffline(receiver.image, receiver.uid)).placeholder(R.drawable.profile_image).into(customChatBarBinding.userImage)
    }

    private fun backButtonPressed() {
        onBackPressedDispatcher.addCallback(this, object: OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.expandedImage.cardView.visibility == View.VISIBLE) {
                    WhatsappLikeProfilePicPreview.dismissPhotoPreview()
                } else if (binding.expandedVideo.cardView.visibility == View.VISIBLE) {
                    binding.expandedVideo.video.player!!.release()
                    binding.userMessageList.isClickable = true
                    WhatsappLikeProfilePicPreview.dismissVideoPreview()
                } else if (binding.layoutSmily.visibility == View.VISIBLE) {
                    hideSmileyLayout()
                } else {
                    finish()
                }
            }
        })
    }

    private fun handleButtonClicks() {
        binding.sendMessageBtn.setOnClickListener { sendMessage() }
        binding.camera.setOnClickListener { cameraButtonClicked() }
        binding.attachMenu.setOnClickListener { showAttachmentMenu() }
        customChatBarBinding.videoCall.setOnClickListener { createVideoCall() }
        customChatBarBinding.userImage.setOnClickListener {
            val file = Utils.getImageOffline(receiver.image, receiver.uid)
            WhatsappLikeProfilePicPreview.zoomImageFromThumb(customChatBarBinding.userImage, binding.expandedImage.cardView, binding.expandedImage.image, binding.chatToolBar.root.rootView, file)
        }
        customChatBarBinding.userInfo.setOnClickListener { sendUserToProfileActivity() }
        binding.smilies.setOnClickListener { smileyButtonClicked() }
        binding.emojiPickerView.setOnEmojiPickedListener { emojiViewItem ->
            binding.messageInputText.append(emojiViewItem.emoji)
        }

        binding.replyLayout.cancel.setOnClickListener { hideReplyLayout(binding.replyLayout) }
    }

    private fun createVideoCall() {
        val notification = Notification(currentUser!!.name, "Incoming Video Call", TYPE_VIDEO_CALL, currentUser!!.image, receiver.token, currentUser!!.uid, receiver.uid)
        FCMNotificationSender.sendNotification(VortexApplication.application, notification)
        val intent = Intent(this, CallActivity::class.java)
        intent.putExtra(getString(R.string.CALLER), currentUser!!.uid)
        intent.putExtra(getString(R.string.RECEIVER), receiver.uid)
        intent.putExtra(getString(R.string.IS_CALL_MADE), true)
        startActivity(intent)
    }

    private fun setupAttachmentBottomSheetMenu() {
        val contentView = View.inflate(this@ChatActivity, R.layout.attachment_bottom_sheet_layout, null)
        bottomSheetDialog = BottomSheetDialog(this@ChatActivity)
        bottomSheetDialog.setContentView(contentView)
        (contentView.parent as View).setBackgroundColor(Color.TRANSPARENT)
    }

    private fun showAttachmentMenu() {
        bottomSheetDialog.show()

        bottomSheetDialog.findViewById<LinearLayout>(R.id.camera_btn)!!.setOnClickListener {
            cameraButtonClicked()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayout>(R.id.photo_video_attachment_btn)!!.setOnClickListener {
            attachmentButtonClicked()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayout>(R.id.document_btn)!!.setOnClickListener {
            attachDocButtonClicked()
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.findViewById<LinearLayout>(R.id.contact_btn)!!.setOnClickListener {
            contactButtonClicked()
            bottomSheetDialog.dismiss()
        }

        bottomSheetDialog.findViewById<LinearLayout>(R.id.audio_btn)!!.setOnClickListener {
            audioButtonClicked()
            bottomSheetDialog.dismiss()
        }
    }

    private fun contactButtonClicked() {
        ShareContactBottomSheetHandler.start(this, messageReceiverId)
    }

    private fun cameraButtonClicked() {
        val intent = Intent(this@ChatActivity, CameraxActivity::class.java)
        mediaCaptureResultLauncher.launch(intent)
    }

    private fun attachmentButtonClicked() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/* video/*"
        mediaPickActivityResultLauncher.launch(intent)
    }

    private fun audioButtonClicked() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"
        mediaPickActivityResultLauncher.launch(intent)
    }

    private fun attachDocButtonClicked() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        docPickActivityResultLauncher.launch(intent)
    }

    private fun sendUserToProfileActivity() {
        val intent = Intent(this, ProfileActivity::class.java)
        intent.putExtra(getString(R.string.RECEIVER_ID), receiver.uid)
        startActivity(intent)
    }

    private fun sendMessage() {
        val message = binding.messageInputText.text.toString()
        if (URLUtil.isValidUrl(message)) {
            FirebaseUtils.sendURLMessage(message, currentUser!!.uid, receiver.uid, quotedMessageId = quotedMessageId)
        } else {
            FirebaseUtils.sendMessage(message, currentUser!!.uid, receiver.uid, quotedMessageId = quotedMessageId)
        }
        hideReplyLayout(binding.replyLayout)
        binding.messageInputText.setText("")
    }

    private fun smileyButtonClicked() {
        if (binding.layoutSmily.visibility == View.GONE) showSmileyLayout() else hideSmileyLayout()
    }

    private fun hideSmileyLayout() {
        binding.layoutSmily.visibility = View.GONE
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            Utils.dipToPixels(this@ChatActivity, 60f).toInt()
        )
        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE)
        params.removeRule(RelativeLayout.ABOVE)
        binding.bottomBar.layoutParams = params
    }

    private fun showSmileyLayout() {
        hideKeyboard(this)
        binding.layoutSmily.visibility = View.VISIBLE
        val params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.MATCH_PARENT,
            Utils.dipToPixels(this@ChatActivity, 60f).toInt()
        )
        params.removeRule(RelativeLayout.ALIGN_PARENT_BOTTOM)
        params.addRule(RelativeLayout.ABOVE, R.id.layout_smily)
        binding.bottomBar.layoutParams = params
    }

    private fun updateStatusIndicator() {
        val handler = Handler(Looper.getMainLooper())
        binding.messageInputText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
            override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {
                if (binding.messageInputText.text?.isNotEmpty() == true) {
                    binding.camera.visibility = View.GONE
                    binding.recordView.visibility = View.GONE
                    binding.recordBg.visibility = View.GONE
                    binding.sendMessageBtn.visibility = View.VISIBLE
                } else {
                    binding.camera.visibility = View.VISIBLE
                    binding.recordView.visibility = View.VISIBLE
                    binding.recordBg.visibility = View.VISIBLE
                    binding.sendMessageBtn.visibility = View.GONE
                }
            }

            override fun afterTextChanged(editable: Editable) {
                presenceDatabaseReference
                    .child(currentUser!!.uid)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping, 1000)
            }

            val userStoppedTyping = Runnable {
                presenceDatabaseReference
                    .child(currentUser!!.uid)
                    .setValue("Online")
            }
        })
        presenceDatabaseReference
            .child(messageReceiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val status = snapshot.getValue(String::class.java)
                        if (status?.isNotEmpty() == true) {
                            if (status == "Offline") {
                                customChatBarBinding.status.visibility = View.GONE
                            } else {
                                customChatBarBinding.status.visibility = View.VISIBLE
                                customChatBarBinding.status.text = status
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<Message> = ArrayList()
        for (item in viewModel.getMessage()?.value!!) {
            if (text.lowercase(Locale.getDefault()) in item.message.lowercase(Locale.ROOT)) {
                filteredList.add(item)
            }
        }
        if (filteredList.isNotEmpty()) {
            adapter.filterList(filteredList)
        } else {
            adapter.filterList(ArrayList())
        }
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

    private fun prepareImageMessageForSending(fileUri: Uri, messageId: String, isImageFromClipboard: Boolean) {
        binding.capturedImage.cardView.visibility = View.VISIBLE
        Picasso.get().load(fileUri).into(binding.capturedImage.image)
        binding.capturedImage.receiverName.text = receiver.name

        binding.capturedImage.sendMessage.setOnClickListener {
            val caption = binding.capturedImage.caption.text.toString()
            binding.capturedImage.caption.setText("")
            hideKeyboard(this)
            binding.capturedImage.cardView.visibility = View.GONE

            showLoadingBar(this@ChatActivity, binding.progressbar.root)

            if (isImageFromClipboard) {
                val objMessage = Message(messageId, fileUri.toString(), getString(R.string.IMAGE), currentUser!!.uid, receiver.uid, Date().time, -1, "", true)
                FirebaseUtils.forwardImage(this@ChatActivity, objMessage, receiver.uid, caption)
            } else {
                FirebaseUtils.sendImage(this@ChatActivity, currentUser!!.uid, messageReceiverId, fileUri, caption, quotedMessageId)
                hideReplyLayout(binding.replyLayout)
            }
        }
    }

    @OptIn(UnstableApi::class) private fun prepareVideoMessageForSending(fileUri: Uri, messageId: String, isVideoFromClipboard: Boolean) {
        binding.capturedVideo.cardView.visibility = View.VISIBLE
        binding.capturedVideo.receiverName.text = receiver.name

        val player = ExoPlayer.Builder(this).build()
        binding.capturedVideo.video.setShowNextButton(false)
        binding.capturedVideo.video.setShowPreviousButton(false)
        binding.capturedVideo.video.player = player

        val mediaItem = MediaItem.fromUri(fileUri)
        player.setMediaItem(mediaItem)
        player.prepare()

        binding.capturedVideo.sendMessage.setOnClickListener {
            val caption = binding.capturedVideo.caption.text.toString()
            binding.capturedVideo.caption.setText("")
            hideKeyboard(this)
            binding.capturedVideo.cardView.visibility = View.GONE
            showLoadingBar(this@ChatActivity, binding.progressbar.root)

            if (isVideoFromClipboard) {
                val objMessage = Message(messageId, fileUri.toString(), getString(R.string.VIDEO), currentUser!!.uid, receiver.uid, Date().time, -1, "", true)
                FirebaseUtils.forwardVideo(this@ChatActivity, objMessage, receiver.uid, caption)
            } else {
                FirebaseUtils.sendVideo(this, currentUser!!.uid, messageReceiverId, fileUri, caption, quotedMessageId = quotedMessageId)
                hideReplyLayout(binding.replyLayout)
            }
        }
    }

    private fun prepareDocMessageForSending(fileUri: Uri, messageId: String, isDocFromClipboard: Boolean, fileName: String, fileSize: String) {
        binding.capturedImage.cardView.visibility = View.VISIBLE
        binding.capturedImage.image.setImageResource(R.drawable.baseline_picture_as_pdf_24)
        binding.capturedImage.receiverName.text = receiver.name
        binding.capturedImage.sendMessage.setOnClickListener {
            val caption = binding.capturedImage.caption.text.toString()
            binding.capturedImage.caption.setText("")
            hideKeyboard(this)
            binding.capturedImage.cardView.visibility = View.GONE
            showLoadingBar(this@ChatActivity, binding.progressbar.root)
            val objMessage = Message(messageId, fileUri.toString(), getString(R.string.PDF_FILES), currentUser!!.uid, receiver.uid, Date().time, -1, "", true, fileName = fileName, fileSize = fileSize)
            if (isDocFromClipboard) {
                FirebaseUtils.forwardDoc(this@ChatActivity, objMessage, receiver.uid, caption)
            } else {
                FirebaseUtils.sendDoc(this@ChatActivity, currentUser!!.uid, messageReceiverId, fileUri, fileName, fileSize, caption, quotedMessageId = quotedMessageId)
                hideReplyLayout(binding.replyLayout)
            }
        }
    }

    private val listener: GoEditTextListener = object : GoEditTextListener{
        override fun onUpdate() {
            val clipboardManager = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val primaryClipData = clipboardManager.primaryClip

            val item = primaryClipData!!.getItemAt(0)
            if (primaryClipData.itemCount > 1) {
                val messageIdItem = primaryClipData.getItemAt(1)
                val messageTypeItem = primaryClipData.getItemAt(2)

                val uri = item.uri

                binding.messageInputText.setText("")
                hideKeyboard(this@ChatActivity)

                when(messageTypeItem.text.toString()) {
                    VortexApplication.application.applicationContext.getString(R.string.IMAGE) -> {
                        prepareImageMessageForSending(uri, messageIdItem.text.toString(), true)
                        binding.capturedImage.cancel.setOnClickListener {
                            hideKeyboard(this@ChatActivity)
                            binding.capturedImage.cardView.visibility = View.GONE
                        }
                    }

                    VortexApplication.application.applicationContext.getString(R.string.VIDEO) -> {
                        prepareVideoMessageForSending(uri, messageIdItem.text.toString(), true)
                        binding.capturedVideo.cancel.setOnClickListener {
                            hideKeyboard(this@ChatActivity)
                            binding.capturedVideo.cardView.visibility = View.GONE
                        }
                    }

                    VortexApplication.application.applicationContext.getString(R.string.PDF_FILES) -> {
                        val fileNameItem = primaryClipData.getItemAt(3)
                        val fileSizeItem = primaryClipData.getItemAt(4)
                        prepareDocMessageForSending(uri, messageIdItem.text.toString(), true, fileNameItem.text.toString(), fileSizeItem.text.toString())
                    }
                }
            }
        }
    }

    private val mediaCaptureResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data != null) {
            val data = result.data
            val fileType = data!!.getStringExtra(getString(R.string.FILE_TYPE))
            if (fileType == getString(R.string.IMAGE)) {
                val fileUri = Uri.parse(data.getStringExtra(getString(R.string.IMAGE_URI)))
                val bitmap: Bitmap
                val source = ImageDecoder.createSource(contentResolver, fileUri)
                bitmap = ImageDecoder.decodeBitmap(source)
                val matrix = Matrix()
                matrix.preRotate(0f)
                val finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                val os = contentResolver.openOutputStream(fileUri)
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, os!!)

                prepareImageMessageForSending(fileUri, "", false)
                binding.capturedImage.cancel.setOnClickListener {
                    hideKeyboard(this@ChatActivity)
                    binding.capturedImage.cardView.visibility = View.GONE
                }
            } else if (fileType == getString(R.string.VIDEO)) {
                val fileUri = Uri.parse(data.getStringExtra(getString(R.string.VIDEO_URI)))
                prepareVideoMessageForSending(fileUri, "", false)
                binding.capturedVideo.cancel.setOnClickListener {
                    hideKeyboard(this@ChatActivity)
                    binding.capturedVideo.cardView.visibility = View.GONE
                }
            }
        }
    }

    private val mediaPickActivityResultLauncher: ActivityResultLauncher<Intent> = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data: Intent = result.data!!
                val fileUri = data.data
                if (getFileType(fileUri).equals("jpg")) {
                    prepareImageMessageForSending(fileUri!!, "", false)
                } else if (getFileType(fileUri).equals("mp4")) {
                    prepareVideoMessageForSending(fileUri!!, "", false)
                } else if (getFileType(fileUri).equals("mp3")) {
                    sendAudioRecording(this, currentUser!!.uid, messageReceiverId, fileUri!!, FirebaseDatabase.getInstance().reference.push().key!!, isSong = true)
                    hideReplyLayout(binding.replyLayout)
                }
            }
        }

    private val docPickActivityResultLauncher: ActivityResultLauncher<Intent> =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val data: Intent = result.data!!
                val fileUri = data.data
                prepareDocMessageForSending(fileUri!!, "", false, getFilename(this, fileUri)!!, getFileSize(fileUri)
                )
            }
        }

    fun showImagePreview(thumbView: View, url: File) {
        WhatsappLikeProfilePicPreview.zoomImageFromThumb(thumbView, binding.expandedImage.cardView, binding.expandedImage.image, binding.chatToolBar.root.rootView, url)
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun showVideoPreview(thumbView: View, url: String) {
        WhatsappLikeProfilePicPreview.zoomVideoFromThumb(thumbView, binding.expandedVideo.cardView, binding.chatToolBar.root.rootView)
        val player = ExoPlayer.Builder(this).build()
        binding.expandedVideo.video.player = player
        binding.expandedVideo.video.setShowNextButton(false)
        binding.expandedVideo.video.setShowPreviousButton(false)
        val mediaItem = MediaItem.fromUri(url)
        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()
    }

    private fun handleMessageEditTextListener() {
        binding.messageInputText.addListener(listener)
    }

    override fun onMessageSent() {
        Utils.dismissLoadingBar(this, binding.progressbar.root)
    }

    override fun onMessageSentFailed() {
        Utils.dismissLoadingBar(this, binding.progressbar.root)
    }

    private var recordedAudioFileName: String? = null
    override fun isReady(): Boolean {
        return true
    }

    override fun onRecordCancel() {
        binding.chatbar.visibility = View.VISIBLE
    }

    override fun onRecordEnd() {
        binding.chatbar.visibility = View.VISIBLE
        Utils.stopRecording()
        val filePath: String = VortexApplication.application.applicationContext.filesDir.path
        val file = File(filePath)
        val fullFileName = "$file/$recordedAudioFileName.3gp"
        sendAudioRecording(this, currentUser!!.uid, messageReceiverId, Uri.fromFile(File(fullFileName)), recordedAudioFileName!!)
        hideReplyLayout(binding.replyLayout)
    }

    override fun onRecordStart() {
        recordedAudioFileName = FirebaseDatabase.getInstance().reference.push().key
        val vibrator = (getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        binding.chatbar.visibility = View.GONE
        Utils.startRecording(recordedAudioFileName!!)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return super.onSupportNavigateUp()
    }
}