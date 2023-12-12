package com.samsung.vortex.utils

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.audioRecordingStorageReference
import com.samsung.vortex.VortexApplication.Companion.audioRecordingUrlDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.contactsDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.docsStorageReference
import com.samsung.vortex.VortexApplication.Companion.docsUrlDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.imageStorageReference
import com.samsung.vortex.VortexApplication.Companion.imageUrlDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.starMessagesDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.videoStorageReference
import com.samsung.vortex.VortexApplication.Companion.videoUrlDatabaseReference
import com.samsung.vortex.fcm.FCMNotificationSender
import com.samsung.vortex.interfaces.MessageListenerCallback
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.Notification
import com.samsung.vortex.model.PhoneContact
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils.Companion.TYPE_MESSAGE
import com.samsung.vortex.utils.Utils.Companion.currentUser
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
                sendNotification(message, messageReceiverId, messageSenderId, TYPE_MESSAGE)
            }
        }

        fun sendAudioRecording(context: Context, messageSenderId: String, messageReceiverId: String, fileUri: Uri, messagePushId: String, isSong: Boolean = false) {
            val callback = context as MessageListenerCallback
            val messageSenderRef = context.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId
            val messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId
            val filePath: StorageReference = audioRecordingStorageReference.child("$messagePushId.3gp")
            val uploadTask: StorageTask<UploadTask.TaskSnapshot?> = filePath.putFile(fileUri)
            uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                filePath.downloadUrl
            }.addOnCompleteListener { task: Task<Uri> ->
                if (task.isSuccessful) {
                    callback.onMessageSent()
                    val downloadUrl = task.result
                    val myUrl = downloadUrl.toString()
                    val objMessage = Message(messagePushId, myUrl, context.getString(R.string.AUDIO_RECORDING), messageSenderId, messageReceiverId, Date().time, -1, "", true, isSong = isSong)
                    val messageBodyDetails: MutableMap<String, Any> = HashMap()
                    messageBodyDetails["$messageSenderRef/$messagePushId"] = objMessage
                    messageBodyDetails["$messageReceiverRef/$messagePushId"] = objMessage
                    FirebaseDatabase.getInstance().reference
                        .updateChildren(messageBodyDetails)
                    val audioRecordingUrlUserDetails: MutableMap<String, Any> = HashMap()
                    audioRecordingUrlUserDetails[messageSenderId] = true
                    audioRecordingUrlUserDetails[messageReceiverId] = true
                    audioRecordingUrlDatabaseReference
                        .child(messagePushId)
                        .updateChildren(audioRecordingUrlUserDetails)
                    updateLastMessage(objMessage)
                    if (isSong)
                        sendNotification("Sent a music file", messageReceiverId, messageSenderId, TYPE_MESSAGE)
                    else
                        sendNotification("Sent an audio message", messageReceiverId, messageSenderId, TYPE_MESSAGE)
                }
            }.addOnFailureListener { e: Exception? -> callback.onMessageSentFailed() }
        }

        fun sendContact(contact: PhoneContact, messageSenderId: String, messageReceiverId: String) {
            val messageSenderRef: String = VortexApplication.application.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId
            val messageReceiverRef: String = VortexApplication.application.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId
            val userMessageKeyRef = messageDatabaseReference
                .child(messageSenderId)
                .child(messageReceiverId)
                .push()
            val contactPushId: String = contactsDatabaseReference.push().key!!
            val messagePushId = userMessageKeyRef.key
            val objMessage = Message(messagePushId!!, contactPushId, VortexApplication.application.getString(R.string.CONTACT), messageSenderId, messageReceiverId, Date().time, -1, "", true)
            val messageBodyDetails: MutableMap<String, Any> = HashMap()
            messageBodyDetails["$messageSenderRef/$messagePushId"] = objMessage
            messageBodyDetails["$messageReceiverRef/$messagePushId"] = objMessage
            FirebaseDatabase.getInstance().reference
                .child(VortexApplication.application.getString(R.string.CONTACTS))
                .child(contactPushId)
                .setValue(contact)
            FirebaseDatabase.getInstance().reference.updateChildren(messageBodyDetails)
            updateLastMessage(objMessage)
            sendNotification("Sent a contact", messageReceiverId, messageSenderId, TYPE_MESSAGE)
        }

        fun sendURLMessage(message: String?, messageSenderId: String, messageReceiverId: String) {
            if (!TextUtils.isEmpty(message)) {
                val messageSenderRef: String = VortexApplication.application.applicationContext.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId
                val messageReceiverRef: String = VortexApplication.application.applicationContext
                    .getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId
                val userMessageKeyRef = messageDatabaseReference
                    .child(messageSenderId)
                    .child(messageReceiverId)
                    .push()
                val messagePushId = userMessageKeyRef.key
                val objMessage = Message(messagePushId!!, message!!, VortexApplication.application.applicationContext.getString(R.string.URL), messageSenderId, messageReceiverId, Date().time, -1, "", true)
                val messageBodyDetails: MutableMap<String, Any> = HashMap()
                messageBodyDetails["$messageSenderRef/$messagePushId"] = objMessage
                messageBodyDetails["$messageReceiverRef/$messagePushId"] = objMessage
                FirebaseDatabase.getInstance().reference
                    .updateChildren(messageBodyDetails)
                updateLastMessage(objMessage)
                sendNotification(message, messageReceiverId, messageSenderId, TYPE_MESSAGE)
            }
        }

        fun sendImage(context: Context, messageSenderId: String, messageReceiverId: String, fileUri: Uri, caption: String) {
            val callback: MessageListenerCallback = context as MessageListenerCallback
            val messageSenderRef = context.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId
            val messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId
            val userMessageKeyRef = messageDatabaseReference
                .child(messageSenderId)
                .child(messageReceiverId)
                .push()

            val messagePushId = userMessageKeyRef.key
            val filePath: StorageReference = imageStorageReference.child("$messagePushId.jpg")
            val uploadTask: StorageTask<UploadTask.TaskSnapshot?> = filePath.putFile(fileUri)

            uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                filePath.downloadUrl
            }.addOnCompleteListener { task: Task<Uri> ->
                if (task.isSuccessful) {
                    callback.onMessageSent()
                    val downloadUrl = task.result
                    val myUrl = downloadUrl.toString()
                    val objMessage: Message = if (caption.isEmpty()) {
                        Message(messagePushId!!, myUrl, context.getString(R.string.IMAGE), messageSenderId, messageReceiverId, Date().time, -1, "", true)
                    } else {
                        Message(messagePushId!!, myUrl, context.getString(R.string.IMAGE), messageSenderId, messageReceiverId, Date().time, -1, "", true, caption)
                    }

                    val messageBodyDetails: MutableMap<String, Any> = HashMap()
                    messageBodyDetails["$messageSenderRef/$messagePushId"] = objMessage
                    messageBodyDetails["$messageReceiverRef/$messagePushId"] = objMessage
                    FirebaseDatabase.getInstance().reference
                        .updateChildren(messageBodyDetails)

                    val imageUrlUserDetails: MutableMap<String, Any> = HashMap()
                    imageUrlUserDetails[messageSenderId] = true
                    imageUrlUserDetails[messageReceiverId] = true
                    imageUrlDatabaseReference
                        .child(messagePushId)
                        .updateChildren(imageUrlUserDetails)
                    updateLastMessage(objMessage)
                    sendNotification("Sent an image", messageReceiverId, messageSenderId, TYPE_MESSAGE)
                }
            }.addOnFailureListener { callback.onMessageSentFailed() }
        }

        fun forwardImage(context: Context, message: Message, receiver: String, caption: String) {
            val callback = context as MessageListenerCallback
            val objMessage: Message = if (caption.isEmpty()) Message(message.messageId, message.message, message.type, currentUser!!.uid, receiver, Date().time, -1, "", true)
            else Message(message.messageId, message.message, message.type, currentUser!!.uid, receiver, Date().time, -1, "", true, caption)

            val messageSenderRef = context.getString(R.string.MESSAGES) + "/" + objMessage.from + "/" + objMessage.to
            val messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + objMessage.to + "/" + objMessage.from

            val messageBodyDetails: MutableMap<String, Any> = HashMap()
            messageBodyDetails[messageSenderRef + "/" + message.messageId] = objMessage
            messageBodyDetails[messageReceiverRef + "/" + message.messageId] = objMessage

            FirebaseDatabase.getInstance().reference
                .updateChildren(messageBodyDetails)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        callback.onMessageSent()
                    }
                }
            val imageUrlUserDetails: MutableMap<String, Any> = HashMap()
            imageUrlUserDetails[currentUser!!.uid] = true
            imageUrlUserDetails[receiver] = true

            imageUrlDatabaseReference
                .child(message.messageId)
                .updateChildren(imageUrlUserDetails)

            updateLastMessage(objMessage)
            sendNotification("Sent an image", objMessage.to, objMessage.from, TYPE_MESSAGE)
        }

        fun sendVideo(context: Context, messageSenderId: String, messageReceiverId: String, fileUri: Uri, caption: String) {
            val callback = context as MessageListenerCallback
            val messageSenderRef = context.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId
            val messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId
            val userMessageKeyRef = messageDatabaseReference
                .child(messageSenderId)
                .child(messageReceiverId)
                .push()
            
            val messagePushId = userMessageKeyRef.key
            val filePath: StorageReference = videoStorageReference.child("$messagePushId.mp4")
            filePath.putFile(fileUri)
                .addOnSuccessListener { taskSnapshot: UploadTask.TaskSnapshot ->
                    val uriTask = taskSnapshot.storage.downloadUrl
                    while (!uriTask.isSuccessful);
                    callback.onMessageSent()
                    val downloadUri = uriTask.result.toString()
                    val objMessage: Message = if (caption.isEmpty()) Message(messagePushId!!, downloadUri, context.getString(R.string.VIDEO), messageSenderId, messageReceiverId, Date().time, -1, "", true) 
                    else Message(messagePushId!!, downloadUri, context.getString(R.string.VIDEO), messageSenderId, messageReceiverId, Date().time, -1, "", true, caption)
                    val messageBodyDetails: MutableMap<String, Any> = HashMap()
                    messageBodyDetails["$messageSenderRef/$messagePushId"] = objMessage
                    messageBodyDetails["$messageReceiverRef/$messagePushId"] = objMessage
                    FirebaseDatabase.getInstance().reference
                        .updateChildren(messageBodyDetails)
                    val videoUrlUserDetails: MutableMap<String, Any> = HashMap()
                    videoUrlUserDetails[messageSenderId] = true
                    videoUrlUserDetails[messageReceiverId] = true
                    videoUrlDatabaseReference
                        .child(messagePushId)
                        .updateChildren(videoUrlUserDetails)
                    updateLastMessage(objMessage)
                    sendNotification("Sent a video", messageReceiverId, messageSenderId, TYPE_MESSAGE)
                }
                .addOnFailureListener { callback.onMessageSentFailed() }
        }

        fun forwardVideo(context: Context, message: Message, receiver: String, caption: String) {
            val callback = context as MessageListenerCallback
            val objMessage: Message = if (caption.isEmpty()) Message(message.messageId, message.message, message.type, currentUser!!.uid, receiver, Date().time, -1, "", true) 
            else Message(message.messageId, message.message, message.type, currentUser!!.uid, receiver, Date().time, -1, "", true, caption)
            
            val messageSenderRef = context.getString(R.string.MESSAGES) + "/" + objMessage.from + "/" + objMessage.to
            val messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + objMessage.to + "/" + objMessage.from
            val messageBodyDetails: MutableMap<String, Any> = HashMap()
            messageBodyDetails[messageSenderRef + "/" + message.messageId] = objMessage
            messageBodyDetails[messageReceiverRef + "/" + message.messageId] = objMessage
            FirebaseDatabase.getInstance().reference
                .updateChildren(messageBodyDetails)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        callback.onMessageSent()
                    }
                }
            val videoUrlUserDetails: MutableMap<String, Any> = HashMap()
            videoUrlUserDetails[currentUser!!.uid] = true
            videoUrlUserDetails[receiver] = true
            videoUrlDatabaseReference
                .child(message.messageId)
                .updateChildren(videoUrlUserDetails)
            updateLastMessage(objMessage)
            sendNotification("Sent a video", objMessage.to, objMessage.from, TYPE_MESSAGE)
        }

        fun sendDoc(context: Context, messageSenderId: String, messageReceiverId: String, fileUri: Uri, filename: String, fileSize: String, caption: String) {
            val callback = context as MessageListenerCallback
            val messageSenderRef =
                context.getString(R.string.MESSAGES) + "/" + messageSenderId + "/" + messageReceiverId
            val messageReceiverRef =
                context.getString(R.string.MESSAGES) + "/" + messageReceiverId + "/" + messageSenderId
            val userMessageKeyRef = messageDatabaseReference
                .child(messageSenderId)
                .child(messageReceiverId)
                .push()
            val messagePushId = userMessageKeyRef.key
            val filePath: StorageReference = docsStorageReference.child("$messagePushId.pdf")
            val uploadTask: StorageTask<UploadTask.TaskSnapshot?> = filePath.putFile(fileUri)
            uploadTask.continueWithTask { task: Task<UploadTask.TaskSnapshot?> ->
                if (!task.isSuccessful) {
                    throw task.exception!!
                }
                filePath.downloadUrl
            }.addOnCompleteListener { task: Task<Uri> ->
                if (task.isSuccessful) {
                    callback.onMessageSent()
                    val downloadUrl = task.result
                    val myUrl = downloadUrl.toString()
                    val objMessage: Message = if (caption.isEmpty()) Message(messagePushId!!, myUrl, context.getString(R.string.PDF_FILES), messageSenderId, messageReceiverId, Date().time, -1, "", true, fileName = filename, fileSize = fileSize) 
                    else Message(messagePushId!!, myUrl, context.getString(R.string.PDF_FILES), messageSenderId, messageReceiverId, Date().time, -1, "", caption = caption, fileName = filename, fileSize = fileSize)
                    val messageBodyDetails: MutableMap<String, Any> = HashMap()
                    messageBodyDetails["$messageSenderRef/$messagePushId"] = objMessage
                    messageBodyDetails["$messageReceiverRef/$messagePushId"] = objMessage
                    FirebaseDatabase.getInstance().reference
                        .updateChildren(messageBodyDetails)
                    val docUrlUserDetails: MutableMap<String, Any> = HashMap()
                    docUrlUserDetails[messageSenderId] = true
                    docUrlUserDetails[messageReceiverId] = true
                    docsUrlDatabaseReference
                        .child(messagePushId)
                        .updateChildren(docUrlUserDetails)
                    updateLastMessage(objMessage)
                    sendNotification("Sent a file", messageReceiverId, messageSenderId, TYPE_MESSAGE)
                }
            }.addOnFailureListener { callback.onMessageSentFailed() }
        }

        fun forwardDoc(context: Context, message: Message, receiver: String, caption: String) {
            val callback = context as MessageListenerCallback
            val objMessage: Message = if (caption.isEmpty()) 
                Message(message.messageId, message.message, message.type, currentUser!!.uid, receiver, Date().time, -1, "", true, fileName = message.fileName, fileSize = message.fileSize)
            else 
                Message(message.messageId, message.message, message.type, currentUser!!.uid, receiver, Date().time, -1, "", true, caption = caption, fileName = message.fileName, fileSize = message.fileSize)
            
            val messageSenderRef = context.getString(R.string.MESSAGES) + "/" + objMessage.from + "/" + objMessage.to
            val messageReceiverRef = context.getString(R.string.MESSAGES) + "/" + objMessage.to + "/" + objMessage.from
            val messageBodyDetails: MutableMap<String, Any> = HashMap()
            messageBodyDetails[messageSenderRef + "/" + message.messageId] = objMessage
            messageBodyDetails[messageReceiverRef + "/" + message.messageId] = objMessage
            FirebaseDatabase.getInstance().reference
                .updateChildren(messageBodyDetails)
                .addOnCompleteListener { task: Task<Void?> ->
                    if (task.isSuccessful) {
                        callback.onMessageSent()
                    }
                }
            val docUrlUserDetails: MutableMap<String, Any> = HashMap()
            docUrlUserDetails[currentUser!!.uid] = true
            docUrlUserDetails[receiver] = true
            docsUrlDatabaseReference
                .child(message.messageId)
                .updateChildren(docUrlUserDetails)
            updateLastMessage(objMessage)
            sendNotification(
                "Sent a file",
                objMessage.to,
                objMessage.from,
                TYPE_MESSAGE
            )
        }

        fun sendNotification(message: String, receiverId: String, senderId: String, type: String) {
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

        fun updateLastMessage(message: Message) {
            val lastMsgObj: MutableMap<String, Any> = HashMap()
            lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_TIME)] = message.time
            when (message.type) {
                VortexApplication.application.applicationContext.getString(R.string.IMAGE) -> lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Photo"
                VortexApplication.application.applicationContext.getString(R.string.VIDEO) -> lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Video"
                VortexApplication.application.applicationContext.getString(R.string.PDF_FILES) -> lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "File"
                VortexApplication.application.applicationContext.getString(R.string.URL) -> lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Link"
                VortexApplication.application.applicationContext.getString(R.string.CONTACT) -> lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "Contact"
                VortexApplication.application.applicationContext.getString(R.string.AUDIO_RECORDING) -> lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = "audio"
                else -> lastMsgObj[VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_DETAILS)] = message.message
            }

            messageDatabaseReference
                .child(message.from)
                .child(VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_WITH_) + message.to)
                .updateChildren(lastMsgObj)
            messageDatabaseReference
                .child(message.to)
                .child(VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_WITH_) + message.from)
                .updateChildren(lastMsgObj)
        }
        fun updateMessageUnreadStatus(receiverId: String?) {
            messageDatabaseReference
                .child(currentUser!!.uid)
                .child(receiverId!!)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val msg = child.getValue(Message::class.java)
                                val map: MutableMap<String, Any> = HashMap()
                                map["unread"] = false
                                messageDatabaseReference
                                    .child(currentUser!!.uid)
                                    .child(receiverId)
                                    .child(msg!!.messageId)
                                    .updateChildren(map)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }

        fun removeLastMessages(sender: String, receiver: String) {
            messageDatabaseReference
                .child(sender)
                .child(VortexApplication.application.applicationContext.getString(R.string.LAST_MESSAGE_WITH_) + receiver)
                .removeValue()
        }

        fun starMessage(message: Message) {
            val starredUser: String = message.starred + ":" + currentUser!!.uid
            message.starred = VortexApplication.application.applicationContext.getString(R.string.STARRED)
            
            starMessagesDatabaseReference
                .child(currentUser!!.uid)
                .child(message.messageId)
                .setValue(message)

            messageDatabaseReference
                .child(message.from)
                .child(message.to)
                .child(message.messageId)
                .child(VortexApplication.application.applicationContext.getString(R.string.STARRED))
                .setValue(starredUser)

            messageDatabaseReference
                .child(message.to)
                .child(message.from)
                .child(message.messageId)
                .child(VortexApplication.application.applicationContext.getString(R.string.STARRED))
                .setValue(starredUser)
        }

        fun unStarMessage(message: Message) {
            val starredUser: String = message.starred.replace(":" + currentUser!!.uid, "")
            starMessagesDatabaseReference
                .child(currentUser!!.uid)
                .child(message.messageId)
                .removeValue()

            messageDatabaseReference
                .child(message.from)
                .child(message.to)
                .child(message.messageId)
                .child(VortexApplication.application.applicationContext.getString(R.string.STARRED))
                .setValue(starredUser)

            messageDatabaseReference
                .child(message.to)
                .child(message.from)
                .child(message.messageId)
                .child(VortexApplication.application.applicationContext.getString(R.string.STARRED))
                .setValue(starredUser)
        }

        fun deleteStarredMessage(message_id: String) {
            starMessagesDatabaseReference
                .child(currentUser!!.uid)
                .child(message_id)
                .removeValue()
        }

        fun deleteMessage(message: Message) {
            val from: String = currentUser!!.uid
            val to: String = if (message.from == from)
                message.to
            else
                message.from

            messageDatabaseReference
                .child(from)
                .child(to)
                .child(message.messageId)
                .removeValue()

            when (message.type) {
                VortexApplication.application.applicationContext.getString(R.string.IMAGE) -> {
        //                imageUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(currentUser!!.uid)
        //                    .removeValue()
                }
                VortexApplication.application.applicationContext.getString(R.string.VIDEO) -> {
        //                videoUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(currentUser!!.uid)
        //                    .removeValue()
                }
                VortexApplication.application.applicationContext.getString(R.string.PDF_FILES) -> {
        //                docsUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(currentUser!!.uid)
        //                    .removeValue()
                }
            }
        }

        fun deleteMessageForEveryone(message: Message) {
            messageDatabaseReference
                .child(message.from)
                .child(message.to)
                .child(message.messageId)
                .removeValue()

            messageDatabaseReference
                .child(message.to)
                .child(message.from)
                .child(message.messageId)
                .removeValue()

            when (message.type) {
                VortexApplication.application.applicationContext.getString(R.string.IMAGE) -> {
        //                imageUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(message.from)
        //                    .removeValue()
        //
        //                imageUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(message.to)
        //                    .removeValue()
        //
        //                imageUrlDatabaseReference.child(message.messageId)
        //                    .addValueEventListener(object : ValueEventListener {
        //                        override fun onDataChange(snapshot: DataSnapshot) {
        //                            if (!snapshot.exists()) {
        //                                imageStorageReference.getStorage()
        //                                    .getReferenceFromUrl(message.message).delete()
        //                            }
        //                        }
        //
        //                        override fun onCancelled(error: DatabaseError) {}
        //                    })
                }
                VortexApplication.application.applicationContext.getString(R.string.VIDEO) -> {
        //                videoUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(message.from)
        //                    .removeValue()
        //                videoUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(message.to)
        //                    .removeValue()
        //                videoUrlDatabaseReference.child(message.messageId)
        //                    .addValueEventListener(object : ValueEventListener {
        //                        override fun onDataChange(snapshot: DataSnapshot) {
        //                            if (!snapshot.exists()) {
        //                                videoStorageReference.getStorage()
        //                                    .getReferenceFromUrl(message.message).delete()
        //                            }
        //                        }
        //
        //                        override fun onCancelled(error: DatabaseError) {}
        //                    })
                }
                VortexApplication.application.applicationContext.getString(R.string.PDF_FILES) -> {
        //                docsUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(message.from)
        //                    .removeValue()
        //                docsUrlDatabaseReference
        //                    .child(message.messageId)
        //                    .child(message.to)
        //                    .removeValue()
        //                docsUrlDatabaseReference.child(message.messageId)
        //                    .addValueEventListener(object : ValueEventListener {
        //                        override fun onDataChange(snapshot: DataSnapshot) {
        //                            if (!snapshot.exists()) {
        //                                docsStorageReference.getStorage()
        //                                    .getReferenceFromUrl(message.message).delete()
        //                            }
        //                        }
        //
        //                        override fun onCancelled(error: DatabaseError) {}
        //                    })
                }
            }
        }
    }
}