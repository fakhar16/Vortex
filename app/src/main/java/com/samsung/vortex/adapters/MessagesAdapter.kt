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
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.rajat.pdfviewer.PdfViewerActivity
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.bottomsheethandler.MessageBottomSheetHandler
import com.samsung.vortex.databinding.ItemMessageBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.utils.Utils.Companion.getDateTimeString
import com.samsung.vortex.utils.Utils.Companion.isRecordingPlaying
import com.samsung.vortex.view.activities.ChatActivity
import com.samsung.vortex.view.activities.SendContactActivity
import com.squareup.picasso.Picasso
import java.io.File

class MessagesAdapter(var context: Context, private var messageList: ArrayList<Message>, private var receiverId: String)
    : RecyclerView.Adapter<MessagesAdapter.MessageViewHolder>(){

    inner class MessageViewHolder(var binding: ItemMessageBinding) : RecyclerView.ViewHolder(binding.root)

    companion object {
        const val ITEM_SENT = 1
        const val ITEM_RECEIVE = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        FirebaseUtils.updateMessageUnreadStatus(receiverId)

        val layoutBinding: ItemMessageBinding = ItemMessageBinding.inflate(LayoutInflater.from(context), parent, false)

        val params = layoutBinding.myLinearLayout.layoutParams as RelativeLayout.LayoutParams
        if (viewType == ITEM_RECEIVE) {
            params.addRule(RelativeLayout.ALIGN_PARENT_START)
            layoutBinding.myLinearLayout.layoutParams = params
            layoutBinding.myLinearLayout.background = ContextCompat.getDrawable(context, R.drawable.receiver_messages_layout)
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_END)
            layoutBinding.myLinearLayout.layoutParams = params
            layoutBinding.myLinearLayout.background = ContextCompat.getDrawable(context, R.drawable.sender_messages_layout)
        }

        return MessageViewHolder(layoutBinding)
    }

    override fun getItemViewType(position: Int): Int {
        val message: Message = messageList[position]
        return if (FirebaseAuth.getInstance().uid == message.from) {
            ITEM_SENT
        } else {
            ITEM_RECEIVE
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        FirebaseUtils.updateMessageSeenStatus(receiverId)
        val message = messageList[position]

        //Setting message and time
        holder.binding.message.text = message.message
        holder.binding.messageTime.text = getDateTimeString(message.time)

        if (message.from == currentUser!!.uid) {
            holder.binding.messageStatus.visibility = View.VISIBLE
            when(message.status) {
                context.getString(R.string.SENT) -> {
                    holder.binding.messageStatus.setImageResource(R.drawable.baseline_done_24)
                }
                context.getString(R.string.SEEN) -> {
                    holder.binding.messageStatus.setImageResource(R.drawable.baseline_done_all_24_seen)
                }
                context.getString(R.string.DELIVERED) -> {
                    holder.binding.messageStatus.setImageResource(R.drawable.baseline_done_all_24)
                }
            }
        }

        //Setting star visibility
        if (message.starred.contains(currentUser!!.uid)) {
            holder.binding.star.visibility = View.VISIBLE
        } else {
            holder.binding.star.visibility = View.GONE
        }

        //Long click on message
        holder.binding.myLinearLayout.setOnLongClickListener {
            MessageBottomSheetHandler.start(context, message, messageList, getItemViewType(messageList.indexOf(message)), holder.binding.star.visibility)
            true
        }
        
        when (message.type) {
            VortexApplication.application.applicationContext.getString(R.string.IMAGE) -> {
                if (message.caption.isEmpty())
                    holder.binding.message.visibility = View.GONE
                else
                    holder.binding.message.text = message.caption

                holder.binding.image.visibility = View.VISIBLE
                Picasso.get().load(Utils.getImageOffline(message.message, message.messageId)).placeholder(R.drawable.profile_image).into(holder.binding.image)

                holder.binding.image.setOnClickListener {
                    (context as ChatActivity).showImagePreview(holder.binding.image, Utils.getImageOffline(message.message, message.messageId))
                }
            }
            VortexApplication.application.getString(R.string.VIDEO) -> {
                if (message.caption.isEmpty())
                    holder.binding.message.visibility = View.GONE
                else
                    holder.binding.message.text = message.caption

                holder.binding.image.visibility = View.VISIBLE
                holder.binding.videoPlayPreview.visibility = View.VISIBLE
                Glide.with(context).load(message.message).centerCrop()
                    .placeholder(R.drawable.baseline_play_circle_outline_24)
                    .into(holder.binding.image)
                holder.binding.image.setOnClickListener {
                    (context as ChatActivity).showVideoPreview(holder.binding.image, message.message)
                }
            }
            context.getString(R.string.PDF_FILES) -> {
                //Setting file if message type is
                if (message.caption.isEmpty())
                    holder.binding.message.visibility = View.GONE
                else holder.binding.message.text = message.caption

                holder.binding.fileName.visibility = View.VISIBLE
                holder.binding.fileName.text = message.fileName
                holder.binding.image.visibility = View.VISIBLE
                holder.binding.image.setImageResource(R.drawable.baseline_picture_as_pdf_24)
                holder.binding.image.setOnClickListener {
                    context.startActivity(
                        PdfViewerActivity.Companion.launchPdfFromUrl(
                            context,
                            message.message,
                            message.fileName,
                            "",
                            true
                        )
                    )
                }
            }
            VortexApplication.application.getString(R.string.URL) -> {
                val linkedText = String.format("<a href=\"%s\">%s</a> ", message.message, message.message)
                holder.binding.message.text = Html.fromHtml(linkedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
                holder.binding.message.movementMethod = LinkMovementMethod.getInstance()
                holder.binding.message.setLinkTextColor(Color.BLUE)
            }
            context.getString(R.string.CONTACT) -> {
                holder.binding.message.visibility = View.GONE
                holder.binding.contactLayout.visibility = View.VISIBLE
                holder.binding.viewContact.visibility = View.VISIBLE
                VortexApplication.contactsDatabaseReference.child(message.message)
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val list: MutableList<String?> = ArrayList()
                            if (snapshot.exists()) {
                                for (child in snapshot.children) {
                                    list.add(child.getValue(String::class.java))
                                }
                                holder.binding.contactName.text = list[1]
                                Picasso.get().load(list[0]).into(holder.binding.contactImage)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })

                holder.binding.viewContact.setOnClickListener {
                    val intent = Intent(context, SendContactActivity::class.java)
                    intent.putExtra("contactId", message.message)
                    intent.putExtra("IsViewContact", true)
                    context.startActivity(intent)
                }
            }
            context.getString(R.string.AUDIO_RECORDING) -> {
                if (message.song) {
                    holder.binding.audioSenderImage.setImageResource(R.drawable.audio)
                }
                val filePath: String = VortexApplication.application.applicationContext.filesDir.path + "/" + message.messageId + ".3gp"
                val file = File(filePath)

                holder.binding.audioRecordingLayout.visibility = View.VISIBLE
                if (Utils.isRecordingFileExist(file)) {
                    holder.binding.audioFileDuration.text = Utils.getDuration(file)
                } else {
                    FirebaseStorage.getInstance().getReferenceFromUrl(message.message).getFile(file)
                }

                holder.binding.message.visibility = View.GONE
                holder.binding.audioFileDuration.visibility = View.VISIBLE
                holder.binding.playRecording.setOnClickListener {
                    isRecordingPlaying = !isRecordingPlaying
                    if (isRecordingPlaying) {
                        holder.binding.playRecording.setImageResource(R.drawable.baseline_pause_24)
                        Utils.playAudioRecording(file.path)
                        Utils.updateAudioDurationUI(
                            Utils.getDurationLong(file),
                            holder.binding.audioFileDuration,
                            holder.binding.playRecording,
                            holder.binding.audioSeekBar
                        )
                    } else {
                        Utils.countDownTimer!!.cancel()
                        holder.binding.playRecording.setImageResource(R.drawable.baseline_play_arrow_24)
                        Utils.stopPlayingRecording()
                        holder.binding.audioFileDuration.text = Utils.getDuration(file)
                        holder.binding.audioSeekBar.progress = 0
                    }
                }
            }
        }
    }

    fun getItemPosition(message_id: String?): Int {
        for (message in messageList) {
            if (message.messageId == message_id) return messageList.indexOf(message)
        }
        return -1
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<Message>) {
        messageList = filterList
        notifyDataSetChanged()
    }

    fun getMessageAtPos(position: Int) : Message {
        return messageList[position]
    }
}