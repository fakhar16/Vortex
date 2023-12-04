package com.samsung.vortex.utils

import android.text.TextUtils
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.fcm.FCMNotificationSender
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.Notification
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils.Companion.TYPE_MESSAGE
import java.util.Date

class FirebaseUtils {
    companion object {
        fun sendMessage(message: String, messageSenderId: String, messageReceiverId: String) {
            if (!TextUtils.isEmpty(message)) {
                val messageSenderRef: String = VortexApplication.application.applicationContext.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId
                val messageReceiverRef: String = VortexApplication.application.applicationContext.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId
                val userMessageKeyRef: DatabaseReference = messageDatabaseReference
                    .child(messageSenderId)
                    .child(messageReceiverId)
                    .push()
                val messagePushId = userMessageKeyRef.key
                val objMessage = Message(
                    messagePushId!!,
                    message,
                    VortexApplication.application.applicationContext.getString(R.string.TEXT),
                    messageSenderId,
                    messageReceiverId,
                    Date().time,
                    -1,
                    "",
                    true
                )
                val messageBodyDetails: MutableMap<String, Any> = HashMap()
                messageBodyDetails["$messageSenderRef/$messagePushId"] = objMessage
                messageBodyDetails["$messageReceiverRef/$messagePushId"] = objMessage
                FirebaseDatabase.getInstance().reference
                    .updateChildren(messageBodyDetails)
                updateLastMessage(objMessage)
                FirebaseUtils.sendNotification(message, messageReceiverId, messageSenderId, TYPE_MESSAGE)
            }
        }

        private fun sendNotification(message: String, receiverId: String, senderId: String, type: String) {
            userDatabaseReference
                .child(receiverId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val receiver: User? = snapshot.getValue(User::class.java)
                            userDatabaseReference
                                .child(senderId)
                                .addValueEventListener(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        if (snapshot.exists()) {
                                            val sender: User = snapshot.getValue(User::class.java)!!
                                            assert(receiver != null)
                                            val notification = Notification(sender.name, message, type, sender.image, receiver!!.token, sender.uid, receiver.uid)
                                            FCMNotificationSender.sendNotification(VortexApplication.application.applicationContext, notification)
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {}
                                })
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        private fun updateLastMessage(message: Message) {
            val lastMsgObj: MutableMap<String, Any> = java.util.HashMap()
            lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_TIME)] = message.time
            if (message.type == VortexApplication.application.applicationContext.getString(R.string.IMAGE))
                lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Photo"
            else if (message.type == VortexApplication.application.applicationContext.getString(R.string.VIDEO))
                lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Video"
            else if (message.type == VortexApplication.application.applicationContext.getString(R.string.PDF_FILES))
                lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "File"
            else if (message.type == VortexApplication.application.applicationContext.getString(R.string.URL))
                lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Link"
            else if (message.type == VortexApplication.application.applicationContext.getString(R.string.CONTACT))
                lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Contact"
            else if (message.type == VortexApplication.application.applicationContext.getString(R.string.AUDIO_RECORDING))
                lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "audio"
            else
                lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = message.message

            messageDatabaseReference
                .child(message.from)
                .child(VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_WITH_) + message.to)
                .updateChildren(lastMsgObj)
            messageDatabaseReference
                .child(message.to)
                .child(VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_WITH_) + message.from)
                .updateChildren(lastMsgObj)
        }
    }
}