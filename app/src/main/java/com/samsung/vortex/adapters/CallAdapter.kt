package com.samsung.vortex.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.databinding.ItemCallLogBinding
import com.samsung.vortex.model.CallLog
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.view.activities.ProfileActivity
import com.squareup.picasso.Picasso

class CallAdapter(var context: Context, private var callLogs : ArrayList<CallLog>)
    : RecyclerView.Adapter<CallAdapter.CallViewHolder>() {
    inner class CallViewHolder(var binding: ItemCallLogBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CallViewHolder {
        val binding = ItemCallLogBinding.inflate(LayoutInflater.from(context), parent, false)
        return CallViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return callLogs.size
    }

    override fun onBindViewHolder(holder: CallViewHolder, position: Int) {
        val callLog = callLogs[position]
        if (currentUser!!.uid == callLog.from) {
            holder.binding.callInfo.setOnClickListener {
                openUserAndCallInfo(callLog.to)
            }
            userDatabaseReference.child(callLog.to)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)!!
                            updateCallLogDetails(holder, user, callLog)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })
        } else {
            holder.binding.callInfo.setOnClickListener {
                openUserAndCallInfo(callLog.from)
            }
            userDatabaseReference.child(callLog.from)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val user = snapshot.getValue(User::class.java)!!
                            updateCallLogDetails(holder, user, callLog)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }

                })

        }
    }

    private fun openUserAndCallInfo(receiverId: String) {
        val intent = Intent(context, ProfileActivity::class.java)
        intent.putExtra(context.getString(R.string.RECEIVER_ID), receiverId)
        context.startActivity(intent)

    }

    private fun updateCallLogDetails(holder: CallViewHolder, user: User, callLog: CallLog) {
        holder.binding.callerName.text = user.name
        holder.binding.callTime.text = Utils.getDateTimeString(callLog.time)
        Picasso.get().load(Utils.getImageOffline(user.image, user.uid))
            .into(holder.binding.callerImage)
        when (callLog.type) {
            VortexApplication.application.getString(R.string.MISSED_CALL) -> {
                holder.binding.callTypeIcon.setImageResource(R.drawable.baseline_missed_video_call_24)
                holder.binding.callType.text = VortexApplication.application.getText(R.string.MISSED_CALL)
            }

            VortexApplication.application.getString(R.string.INCOMING) -> {
                holder.binding.callTypeIcon.setImageResource(R.drawable.baseline_call_received_24)
                holder.binding.callType.text = VortexApplication.application.getText(R.string.INCOMING)
            }

            VortexApplication.application.getString(R.string.OUTGOING) -> {
                holder.binding.callTypeIcon.setImageResource(R.drawable.baseline_call_made_24)
                holder.binding.callType.text = VortexApplication.application.getText(R.string.OUTGOING)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<CallLog>) {
        callLogs = filterList
        notifyDataSetChanged()
    }

//    private fun updateUnreadMessageListForUser(user: User, holder: ChatAdapter.ChatViewHolder) {
//        var unreadMessageCount = 0
//        VortexApplication.messageDatabaseReference
//            .child(Utils.currentUser!!.uid)
//            .child(user.uid)
//            .addValueEventListener(object : ValueEventListener {
//                override fun onDataChange(snapshot: DataSnapshot) {
//                    if (snapshot.exists()) {
//                        for (child in snapshot.children) {
//                            val message: Message = child.getValue(Message::class.java)!!
//                            if (message.isUnread) {
//                                unreadMessageCount++
//                            }
//                        }
//                        if (unreadMessageCount != 0) {
//                            holder.binding.unreadMessageCount.visibility = View.VISIBLE
//                            holder.binding.unreadMessageCount.text = unreadMessageCount.toString()
//                        } else {
//                            holder.binding.unreadMessageCount.visibility = View.GONE
//                        }
//                    }
//                }
//
//                override fun onCancelled(error: DatabaseError) {}
//            })
//    }
}