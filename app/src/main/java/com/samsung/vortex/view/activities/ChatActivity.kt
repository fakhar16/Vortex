package com.samsung.vortex.view.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.webkit.URLUtil
import android.widget.RelativeLayout
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.presenceDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.adapters.MessagesAdapter
import com.samsung.vortex.databinding.ActivityChatBinding
import com.samsung.vortex.databinding.CustomChatBarBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.utils.WhatsappLikeProfilePicPreview
import com.samsung.vortex.viewmodel.MessageViewModel
import com.squareup.picasso.Picasso

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var customChatBarBinding: CustomChatBarBinding
    private lateinit var messageReceiverId: String

    private lateinit var viewModel: MessageViewModel

    private lateinit var adapter: MessagesAdapter

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

    private fun initializeFields() {
//        binding.recordView.activity = this
//        binding.recordView.callback = this
        messageReceiverId = intent.extras!!.getString(getString(R.string.VISIT_USER_ID))!!
        initToolBar()
        setupViewModel()
        setupRecyclerView()
//        setupAttachmentBottomSheetMenu()
        handleButtonClicks()
        checkIfSearchMessageTriggered()
        initProgressBar()
//        handleMessageEditTextListener()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[MessageViewModel::class.java]
        viewModel.init(currentUser!!.uid, messageReceiverId)
        viewModel.getMessage()?.observe(this) {
            adapter.notifyDataSetChanged()
            scrollToMessage()
        }
    }

    private fun setupRecyclerView() {
        binding.userMessageList.layoutManager = LinearLayoutManager(this)
        adapter = MessagesAdapter(this@ChatActivity, viewModel.messages!!.value!!, currentUser!!.uid, messageReceiverId)
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
    }

    private fun scrollToMessage() {
        if (adapter.itemCount != 0) {
            if (intent.hasExtra(getString(R.string.MESSAGE_ID))) {
                val position: Int = adapter.getItemPosition(intent.getStringExtra(getString(R.string.MESSAGE_ID)))
                intent.removeExtra(getString(R.string.MESSAGE_ID))
                binding.userMessageList.smoothScrollToPosition(position)
//                binding.userMessageList.postDelayed({
//                    binding.userMessageList.findViewHolderForAdapterPosition(position)
//                        ?.itemView?.findViewById(R.id.my_linear_layout).setBackgroundTintList(ContextCompat.getColorStateList(this@ChatActivity, R.color.colorPrimary))
//                    Handler(Looper.getMainLooper()).postDelayed({
//                        Objects.requireNonNull(binding.userMessageList.findViewHolderForAdapterPosition(position)).itemView?.findViewById(R.id.my_linear_layout).setBackgroundTintList(null)
//                    }, 500)
//                }, 200)
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
//                filter(newText)
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
                }
//                else if (binding.expandedVideo.cardView.getVisibility() == View.VISIBLE) {
//                    Objects.requireNonNull(binding.expandedVideo.video.getPlayer()).release()
//                    binding.userMessageList.isClickable = true
//                    WhatsappLikeProfilePicPreview.dismissVideoPreview()
//                } else if (binding.layoutSmily.visibility == View.VISIBLE) {
//                    hideSmileyLayout()
//                }
                    else {
                    finish()
                }
            }
        })
    }

    private fun handleButtonClicks() {
        binding.sendMessageBtn.setOnClickListener { sendMessage() }
//        binding.camera.setOnClickListener { view -> cameraButtonClicked() }
//        binding.attachMenu.setOnClickListener { view -> showAttachmentMenu() }
//        customChatBarBinding.voiceCall.setOnClickListener { view ->
//            Toast.makeText(
//                this,
//                receiver.getName(),
//                Toast.LENGTH_SHORT
//            ).show()
//        }
//        customChatBarBinding.videoCall.setOnClickListener { view -> createVideoCall() }
        customChatBarBinding.userImage.setOnClickListener {
            val file = Utils.getImageOffline(receiver.image, receiver.uid)
            WhatsappLikeProfilePicPreview.zoomImageFromThumb(customChatBarBinding.userImage, binding.expandedImage.cardView, binding.expandedImage.image, binding.chatToolBar.root.rootView, file)
        }
//        customChatBarBinding.userInfo.setOnClickListener { view -> sendUserToProfileActivity() }
        binding.smilies.setOnClickListener { smileyButtonClicked() }
        binding.emojiPickerView.setOnEmojiPickedListener { emojiViewItem ->
            binding.messageInputText.append(emojiViewItem.emoji)
        }
    }

    private fun sendMessage() {
        val message = binding.messageInputText.text.toString()
        if (URLUtil.isValidUrl(message)) {
//            FirebaseUtils.sendURLMessage(message, currentUser!!.uid, receiver.uid)
        } else {
            FirebaseUtils.sendMessage(message, currentUser!!.uid, receiver.uid)
        }
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
        Utils.hideKeyboard(this)
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

//    private fun filter(text: String) {
//        val filteredList: ArrayList<Message> = ArrayList()
//        for (item in viewModel.getMessage()?.value!!) {
//            if (text.lowercase(Locale.getDefault()) in item.message.lowercase(Locale.ROOT)) {
//                filteredList.add(item)
//            }
//        }
//        if (filteredList.isNotEmpty()) {
//            messagesAdapter.filterList(filteredList)
//        } else {
//            messagesAdapter.filterList(ArrayList())
//        }
//    }

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
}