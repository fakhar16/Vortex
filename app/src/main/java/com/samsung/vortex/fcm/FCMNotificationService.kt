package com.samsung.vortex.fcm

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.RingtoneManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.Utils.Companion.ACTION_REJECT_CALL
import com.samsung.vortex.utils.Utils.Companion.INCOMING_CALL_CHANNEL_ID
import com.samsung.vortex.utils.Utils.Companion.INCOMING_CALL_NOTIFICATION_ID
import com.samsung.vortex.utils.Utils.Companion.INCOMING_MESSAGE_NOTIFICATION_ID
import com.samsung.vortex.utils.Utils.Companion.MESSAGE_CHANNEL_ID
import com.samsung.vortex.utils.Utils.Companion.TYPE_DISCONNECT_CALL_BY_OTHER_USER
import com.samsung.vortex.utils.Utils.Companion.TYPE_DISCONNECT_CALL_BY_USER
import com.samsung.vortex.utils.Utils.Companion.TYPE_MESSAGE
import com.samsung.vortex.utils.Utils.Companion.TYPE_VIDEO_CALL
import com.samsung.vortex.view.activities.CallingActivity
import com.samsung.vortex.view.activities.ChatActivity
import com.samsung.vortex.view.broadcast.HungUpBroadcast
import com.samsung.vortex.view.broadcast.ReplyBroadcast
import com.samsung.vortex.webrtc.CallActivity
import com.squareup.picasso.Picasso
import java.io.IOException

@SuppressLint("MissingFirebaseInstanceTokenRefresh")
class FCMNotificationService: FirebaseMessagingService() {
    companion object {
        const val KEY_TEXT_REPLY = "text_reply"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data: Map<String, String> = remoteMessage.data

        when (data[VortexApplication.application.applicationContext.getString(R.string.TYPE)]) {
            TYPE_MESSAGE -> {
                showMessageNotification(data)
                updateMessageStatus(data[VortexApplication.application.getString(R.string.SENDER_ID)]!!, data[VortexApplication.application.getString(R.string.RECEIVER_ID)]!!)
            }
            TYPE_VIDEO_CALL -> {
                try {
                    showVideoCallNotification(data)
                } catch (e: IOException) {
                    throw RuntimeException(e)
                }
            }
            TYPE_DISCONNECT_CALL_BY_USER -> {
                NotificationManagerCompat.from(VortexApplication.application.applicationContext)
                    .cancel(INCOMING_CALL_NOTIFICATION_ID)
            }
            TYPE_DISCONNECT_CALL_BY_OTHER_USER -> {
                applicationContext.sendBroadcast(Intent(ACTION_REJECT_CALL))
            }
        }
    }

    private fun updateMessageStatus(sender: String, receiver: String) {
        VortexApplication.messageDatabaseReference
            .child(sender)
            .child(receiver)
            .orderByKey()
            .limitToLast(1)
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val message = child.getValue(Message::class.java)!!
                            message.status = VortexApplication.application.getString(R.string.DELIVERED)
                            VortexApplication.messageDatabaseReference
                                .child(sender)
                                .child(receiver)
                                .child(message.messageId)
                                .setValue(message)
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }

            })
    }

    @Throws(IOException::class)
    private fun showMessageNotification(data: Map<String, String>) {
        val bitmap = Picasso.get().load(data[VortexApplication.application.applicationContext.getString(R.string.ICON)]).get()
        val senderId = data[VortexApplication.application.applicationContext.getString(R.string.SENDER_ID)]
        val receiverId = data[VortexApplication.application.applicationContext.getString(R.string.RECEIVER_ID)]
        val title = data[VortexApplication.application.applicationContext.getString(R.string.TITLE)]
        val message = data[VortexApplication.application.applicationContext.getString(R.string.MESSAGE)]

        val intent = Intent(applicationContext, ChatActivity::class.java)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra(VortexApplication.application.applicationContext.getString(R.string.VISIT_USER_ID), senderId)
        intent.putExtra(VortexApplication.application.applicationContext.getString(R.string.CURRENT_USER_ID), receiverId)

        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val channel = NotificationChannel(MESSAGE_CHANNEL_ID, "Message Notification", NotificationManager.IMPORTANCE_HIGH)
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)

        //Direct Reply Intent
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY).setLabel("Reply").build()
        val replyIntent = Intent(VortexApplication.application.applicationContext, ReplyBroadcast::class.java)
        replyIntent.putExtra(VortexApplication.application.applicationContext.getString(R.string.VISIT_USER_ID), senderId)
        replyIntent.putExtra(VortexApplication.application.applicationContext.getString(R.string.CURRENT_USER_ID), receiverId)
        val replyPendingIntent = PendingIntent.getBroadcast(VortexApplication.application.applicationContext, 0, replyIntent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_MUTABLE)
        val action: NotificationCompat.Action =
            NotificationCompat.Action.Builder(R.drawable.icon, "Reply", replyPendingIntent)
                .addRemoteInput(remoteInput).build()
        val notification: NotificationCompat.Builder =
            NotificationCompat.Builder(this, MESSAGE_CHANNEL_ID)
                .setContentTitle(title)
                .setContentText(message)
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(bitmap)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .addAction(action)
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(INCOMING_MESSAGE_NOTIFICATION_ID, notification.build())
    }

    @Throws(IOException::class)
    private fun showVideoCallNotification(data: Map<String, String>) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        val audioAttr = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .setLegacyStreamType(AudioManager.STREAM_RING)
            .build()
        val notificationChannel = NotificationChannelCompat.Builder(INCOMING_CALL_CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH)
            .setName("Incoming calls")
            .setDescription("Incoming audio and video call alerts")
            .setSound(soundUri, audioAttr)
            .build()
        val title =
            data[VortexApplication.application.getString(R.string.TITLE)]
        val icon =
            data[VortexApplication.application.getString(R.string.ICON)]
        val receiverId = data[VortexApplication.application.getString(R.string.RECEIVER_ID)]
        val senderId = data[VortexApplication.application.getString(R.string.SENDER_ID)]
        val bitmap = Picasso.get().load(icon).get()
        val largeIcon = Icon.createWithBitmap(bitmap)

        //Accept call intents
        val answerIntent = Intent(applicationContext, CallActivity::class.java)
        answerIntent.putExtra(VortexApplication.application.getString(R.string.CALL_ACCEPTED), true)
        answerIntent.putExtra(VortexApplication.application.getString(R.string.CALLER), receiverId)
        val answerPendingIntent = PendingIntent.getActivity(applicationContext, 0, answerIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        //Reject call intents
        val rejectIntent = Intent(applicationContext, HungUpBroadcast::class.java)
        rejectIntent.putExtra(VortexApplication.application.getString(R.string.RECEIVER_ID), receiverId)
        rejectIntent.putExtra(VortexApplication.application.getString(R.string.SENDER_ID), senderId)
        val rejectPendingIntent = PendingIntent.getBroadcast(applicationContext, 0, rejectIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        //Show incoming call full screen intents
        val showIncomingCallIntent = Intent(applicationContext, CallingActivity::class.java)
        showIncomingCallIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        showIncomingCallIntent.putExtra(VortexApplication.application.getString(R.string.IMAGE), icon)
        showIncomingCallIntent.putExtra(VortexApplication.application.getString(R.string.NAME), title)
        showIncomingCallIntent.putExtra(VortexApplication.application.getString(R.string.FRIEND_USER_NAME), receiverId)
        val showIncomingCallPendingIntent = PendingIntent.getActivity(applicationContext, 0, showIncomingCallIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // Create a new call with the user as caller.
        val incomingCaller = Person.Builder()
            .setName(title)
            .setIcon(largeIcon)
            .setImportant(true)
            .build()

        // Create a call style notification for an incoming call.
        val builder: Notification.Builder = Notification.Builder(VortexApplication.application.applicationContext, INCOMING_CALL_CHANNEL_ID)
            .setSmallIcon(Icon.createWithResource(VortexApplication.application.applicationContext, R.drawable.icon))
            .setContentTitle("Incoming call")
            .setContentText("Whatsapp video call")
            .setStyle(Notification.CallStyle.forIncomingCall(incomingCaller, rejectPendingIntent, answerPendingIntent))
            .setContentIntent(showIncomingCallPendingIntent)
            .setFullScreenIntent(showIncomingCallPendingIntent, true)
            .addPerson(incomingCaller)
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_CALL)
        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(notificationChannel)
        NotificationManagerCompat.from(applicationContext)
            .notify(INCOMING_CALL_NOTIFICATION_ID, builder.build())
    }
}