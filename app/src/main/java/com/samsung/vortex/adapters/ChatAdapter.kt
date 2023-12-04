package com.samsung.vortex.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.databinding.UsersDisplayLayoutBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.view.activities.ChatActivity
import com.squareup.picasso.Picasso

class ChatAdapter(var context: Context, private var chats : ArrayList<User>)
    : RecyclerView.Adapter<ChatAdapter.ChatViewHolder>() {

    inner class ChatViewHolder(var binding: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = UsersDisplayLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        val user = chats[position]
        holder.binding.userProfileName.text = user.name
        Picasso.get().load(Utils.getImageOffline(user.image, user.uid)).placeholder(R.drawable.profile_image).into(holder.binding.usersProfileImage)

        messageDatabaseReference
            .child(currentUser!!.uid)
            .child(context.getString(R.string.LAST_MESSAGE_WITH_) + user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        val lastMsg = snapshot.child(context.getString(R.string.LAST_MESSAGE_DETAILS)).getValue(String::class.java)
                        val lastMsgTime = snapshot.child(context.getString(R.string.LAST_MESSAGE_TIME)).getValue(Long::class.java)
                        holder.binding.userProfileStatus.text = lastMsg
                        holder.binding.userLastSeenTime.text = Utils.getDateTimeString(lastMsgTime!!)
                    } else {
                        holder.binding.userProfileStatus.text = context.getString(R.string.TAP_TO_CHAT)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })

        holder.itemView.setOnClickListener {
            val chatIntent = Intent(context, ChatActivity::class.java)
            chatIntent.putExtra(context.getString(R.string.VISIT_USER_ID), user.uid)
            context.startActivity(chatIntent)
        }

        updateUnreadMessageListForUser(user, holder)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<User>) {
        chats = filterList
        notifyDataSetChanged()
    }

    private fun updateUnreadMessageListForUser(user: User, holder: ChatAdapter.ChatViewHolder) {
        var unreadMessageCount = 0
        messageDatabaseReference
            .child(currentUser!!.uid)
            .child(user.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val message: Message = child.getValue(Message::class.java)!!
                            if (message.isUnread) {
                                unreadMessageCount++
                            }
                        }
                        if (unreadMessageCount != 0) {
                            holder.binding.unreadMessageCount.visibility = View.VISIBLE
                            holder.binding.unreadMessageCount.text = unreadMessageCount.toString()
                        } else {
                            holder.binding.unreadMessageCount.visibility = View.GONE
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}