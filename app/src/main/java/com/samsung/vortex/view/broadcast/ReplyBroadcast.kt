package com.samsung.vortex.view.broadcast

import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.fcm.FCMNotificationService
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils.Companion.INCOMING_MESSAGE_NOTIFICATION_ID

class ReplyBroadcast: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        handleDirectReply(context, intent)
    }

    private fun handleDirectReply(context: Context, intent: Intent) {
        val remoteReply = RemoteInput.getResultsFromIntent(intent)
        val messageReceiverId = intent.extras!!.getString(VortexApplication.application.applicationContext.getString(R.string.VISIT_USER_ID))
        val messageSenderId = intent.extras!!.getString(VortexApplication.application.applicationContext.getString(R.string.CURRENT_USER_ID))
        if (remoteReply != null) {
            val message =
                remoteReply.getCharSequence(FCMNotificationService.KEY_TEXT_REPLY).toString()
            FirebaseUtils.sendMessage(message, messageSenderId!!, messageReceiverId!!)
            NotificationManagerCompat.from(context).cancel(INCOMING_MESSAGE_NOTIFICATION_ID)
        }
    }
}