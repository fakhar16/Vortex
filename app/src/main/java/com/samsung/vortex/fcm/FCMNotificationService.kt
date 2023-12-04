package com.samsung.vortex.fcm

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.RemoteInput
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.utils.Utils.Companion.INCOMING_MESSAGE_NOTIFICATION_ID
import com.samsung.vortex.utils.Utils.Companion.MESSAGE_CHANNEL_ID
import com.samsung.vortex.utils.Utils.Companion.TYPE_MESSAGE
import com.samsung.vortex.view.activities.ChatActivity
import com.samsung.vortex.view.broadcast.ReplyBroadcast
import com.squareup.picasso.Picasso
import java.io.IOException

class FCMNotificationService: FirebaseMessagingService() {
    companion object {
        const val KEY_TEXT_REPLY = "text_reply"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val data: Map<String, String> = remoteMessage.data

        val type = data[VortexApplication.application.applicationContext.getString(R.string.TYPE)]

        when (type) {
            TYPE_MESSAGE -> {
                showMessageNotification(data)
            }
//            TYPE_VIDEO_CALL -> {
//                try {
//                    showVideoCallNotification(data)
//                } catch (e: IOException) {
//                    throw RuntimeException(e)
//                }
//            }
//            TYPE_DISCONNECT_CALL_BY_USER -> {
//                NotificationManagerCompat.from(ApplicationClass.application.getApplicationContext())
//                    .cancel(INCOMING_CALL_NOTIFICATION_ID)
//                // Todo: Show missed call log here
//            }
//            TYPE_DISCONNECT_CALL_BY_OTHER_USER -> {
//                applicationContext.sendBroadcast(Intent(ACTION_REJECT_CALL))
//            }
        }
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat.from(this)
            .notify(INCOMING_MESSAGE_NOTIFICATION_ID, notification.build())
    }
}