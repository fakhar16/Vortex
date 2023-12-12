package com.samsung.vortex.view.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.videoUserDatabaseReference
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils.Companion.INCOMING_CALL_NOTIFICATION_ID
import com.samsung.vortex.utils.Utils.Companion.TYPE_DISCONNECT_CALL_BY_OTHER_USER

class HungUpBroadcast : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val receiverId = intent.getStringExtra(
            VortexApplication.application.getString(R.string.RECEIVER_ID)
        )
        val senderId = intent.getStringExtra(
            VortexApplication.application.getString(R.string.SENDER_ID)
        )
        videoUserDatabaseReference.child(receiverId!!).setValue(null)
        NotificationManagerCompat.from(context).cancel(INCOMING_CALL_NOTIFICATION_ID)
        FirebaseUtils.sendNotification("", senderId!!, receiverId, TYPE_DISCONNECT_CALL_BY_OTHER_USER)
    }
}