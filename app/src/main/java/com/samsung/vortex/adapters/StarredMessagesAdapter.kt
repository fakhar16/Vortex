package com.samsung.vortex.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.databinding.ItemStarMessageBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.utils.bottomsheethandler.MessageBottomSheetHandler
import com.samsung.vortex.view.activities.ChatActivity
import com.samsung.vortex.view.activities.StarMessageActivity
import com.squareup.picasso.Picasso

class StarredMessagesAdapter(var context: Context, private var messageList: ArrayList<Message>)
    :
    RecyclerView.Adapter<StarredMessagesAdapter.StarredMessagesViewHolder>() {
    inner class StarredMessagesViewHolder(var binding: ItemStarMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StarredMessagesViewHolder {
        val binding = ItemStarMessageBinding.inflate(LayoutInflater.from(context), parent, false)
        return StarredMessagesViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: StarredMessagesViewHolder, position: Int) {
        val message: Message = messageList[position]

        var user: User?

        if (message.from == currentUser!!.uid) {
            user = currentUser as User
            bindMessageDetails(holder, message, user)
        } else {
            userDatabaseReference.child(message.from)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            user = snapshot.getValue(User::class.java)
                            bindMessageDetails(holder, message, user!!)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        holder.binding.lowerInfo.setOnClickListener {
            val intent = Intent(context, ChatActivity::class.java)
            val visitUserId: String = if (message.from == currentUser!!.uid) message.to else message.from
            intent.putExtra(context.getString(R.string.VISIT_USER_ID), visitUserId)
            intent.putExtra(context.getString(R.string.MESSAGE_ID), message.messageId)
            context.startActivity(intent)
        }

        var clickedMessage: View = holder.binding.myLinearLayout
        if (message.type == context.getString(R.string.IMAGE)) {
            clickedMessage = holder.binding.image
        } else if (message.type == context.getString(R.string.VIDEO)) {
            clickedMessage = holder.binding.videoPlayPreview
        }

        clickedMessage.setOnLongClickListener {
            MessageBottomSheetHandler.start(context, message, messageList, 0, holder.binding.star.visibility)
            true
        }
    }

    private fun bindMessageDetails(
        holder: StarredMessagesViewHolder,
        message: Message,
        user: User
    ) {
        holder.binding.messageTime.text = Utils.getTimeString(message.time)
        holder.binding.messageDate.text = Utils.getDateString(message.time)
        Picasso.get().load(Utils.getImageOffline(user.image, user.uid)).placeholder(R.drawable.profile_image).into(holder.binding.userImage)
        holder.binding.userName.text = if (user.uid == currentUser!!.uid) "You" else user.name

        holder.binding.myLinearLayout.background =
            if (user.uid == currentUser!!.uid) ContextCompat.getDrawable(context, R.drawable.sender_messages_layout)
            else ContextCompat.getDrawable(context, R.drawable.receiver_messages_layout)

        when (message.type) {
            context.getString(R.string.TEXT) -> {
                holder.binding.message.text = message.message
            }
            context.getString(R.string.IMAGE) -> {
                holder.binding.message.visibility = View.GONE
                holder.binding.image.visibility = View.VISIBLE
                Picasso.get().load(Utils.getImageOffline(message.message, message.messageId)).placeholder(R.drawable.profile_image).into(holder.binding.image)
                holder.binding.image.setOnClickListener {
                    (context as StarMessageActivity).showImagePreview(holder.binding.image, Utils.getImageOffline(message.message, message.messageId))
                }
            }
            context.getString(R.string.VIDEO) -> {
                holder.binding.message.visibility = View.GONE
                holder.binding.image.visibility = View.VISIBLE
                holder.binding.videoPlayPreview.visibility = View.VISIBLE
                Glide.with(context).load(message.message).centerCrop()
                    .placeholder(R.drawable.baseline_play_circle_outline_24).into(holder.binding.image)
                holder.binding.videoPlayPreview.setOnClickListener {
                    (context as StarMessageActivity).showVideoPreview(holder.binding.image, message.message)
                }
            }

            VortexApplication.application.getString(R.string.URL) -> {
                val linkedText = String.format("<a href=\"%s\">%s</a> ", message.message, message.message)
                holder.binding.message.text = Html.fromHtml(linkedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                holder.binding.message.movementMethod = LinkMovementMethod.getInstance()
                holder.binding.message.setLinkTextColor(Color.BLUE)
            }
        }
//        else if (message.type.equals(context.getString(R.string.PDF_FILES))) {
//            holder.binding.message.visibility = View.GONE
//            holder.binding.image.visibility = View.VISIBLE
//            holder.binding.image.setImageResource(R.drawable.baseline_file_present_24)
//            holder.binding.image.setOnClickListener { view ->
//                context.startActivity(
//                    PdfViewerActivity.Companion.launchPdfFromUrl(
//                        context,
//                        message.message,
//                        message.getFilename(),
//                        "",
//                        true
//                    )
//                )
//            }
//        } else if (message.type.equals(context.getString(R.string.AUDIO_RECORDING))) {
//            val file_path: String =
//                (ApplicationClass.application.getApplicationContext().getFilesDir()
//                    .getPath() + "/" + message.getMessageId()).toString() + ".3gp"
//            val file = File(file_path)
//            holder.binding.audioFileDuration.setText(Utils.getDuration(file))
//            holder.binding.message.visibility = View.GONE
//            holder.binding.audioRecordingLayout.visibility = View.VISIBLE
//            holder.binding.playRecording.setOnClickListener { view ->
//                isRecordingPlaying = !isRecordingPlaying
//                if (isRecordingPlaying) {
//                    holder.binding.playRecording.setImageResource(R.drawable.baseline_pause_24)
//                    Utils.playAudioRecording(file.path)
//                    Utils.updateAudioDurationUI(
//                        Utils.getDurationLong(file),
//                        holder.binding.audioFileDuration,
//                        holder.binding.playRecording,
//                        holder.binding.audioSeekBar
//                    )
//                } else {
//                    Utils.countDownTimer.cancel()
//                    holder.binding.playRecording.setImageResource(R.drawable.baseline_play_arrow_24)
//                    Utils.stopPlayingRecording()
//                    holder.binding.audioFileDuration.setText(Utils.getDuration(file))
//                    holder.binding.audioSeekBar.progress = 0
//                }
//            }
//        } else if (message.type.equals(context.getString(R.string.CONTACT))) {
//            holder.binding.message.visibility = View.GONE
//            holder.binding.contactLayout.visibility = View.VISIBLE
//            holder.binding.viewContact.visibility = View.VISIBLE
//            ApplicationClass.contactsDatabaseReference.child(message.message)
//                .addValueEventListener(object : ValueEventListener {
//                    override fun onDataChange(snapshot: DataSnapshot) {
//                        val list: MutableList<String?> = java.util.ArrayList()
//                        if (snapshot.exists()) {
//                            for (child in snapshot.children) {
//                                list.add(child.getValue(String::class.java))
//                            }
//                            holder.binding.contactName.text = list[1]
//                            Picasso.get().load(list[0]).into(holder.binding.contactImage)
//                        }
//                    }
//
//                    override fun onCancelled(error: DatabaseError) {}
//                })
//            holder.binding.viewContact.setOnClickListener {
//                val intent = Intent(context, SendContactActivity::class.java)
//                intent.putExtra("contactId", message.message)
//                intent.putExtra("IsViewContact", true)
//                context.startActivity(intent)
//            }
//        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: java.util.ArrayList<Message>) {
        messageList = filterList
        notifyDataSetChanged()
    }
}