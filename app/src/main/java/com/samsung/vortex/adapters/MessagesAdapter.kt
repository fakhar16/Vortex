package com.samsung.vortex.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.samsung.vortex.R
import com.samsung.vortex.databinding.ItemMessageBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils.Companion.getDateTimeString
import com.samsung.vortex.utils.bottomsheethandler.MessageBottomSheetHandler

class MessagesAdapter(var context: Context, private var messageList: ArrayList<Message>, var senderId: String, var receiverId: String)
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
        val message = messageList[position]

        //Setting message and time
        holder.binding.message.text = message.message
        holder.binding.messageTime.text = getDateTimeString(message.time)

        //Long click on message
        holder.binding.myLinearLayout.setOnLongClickListener {
            MessageBottomSheetHandler.start(context)
            true
        }
    }

    fun getItemPosition(message_id: String?): Int {
        for (message in messageList) {
            if (message.messageId == message_id) return messageList.indexOf(message)
        }
        return -1
    }
}