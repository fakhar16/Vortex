package com.samsung.vortex.bottomsheethandler

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.utils.Utils.Companion.TAG
import com.samsung.vortex.utils.Utils.Companion.currentUser

class ClearChatBottomSheetHandler {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var dialog: BottomSheetDialog

        private lateinit var receiver: String

        fun start(context: Context, messageReceiverId: String) {
            val contentView = View.inflate(context, R.layout.clear_chat_bottom_sheet_layout, null)
            dialog = BottomSheetDialog(context)
            dialog.setContentView(contentView)
            dialog.show()
            receiver = messageReceiverId

            dialog.findViewById<ImageView>(R.id.close_clear_chat_dialog)!!.setOnClickListener {
                dialog.dismiss()
            }

            dialog.findViewById<LinearLayout>(R.id.clear_chat)!!.setOnClickListener {
                clearChat(context, receiver)
                dialog.dismiss()
            }
        }

        private fun clearChat(context: Context, receiver: String) {
            Log.i(TAG, "clearChat: ${currentUser!!.uid} : $receiver")
            messageDatabaseReference.child(currentUser!!.uid)
                .child(receiver)
                .removeValue()

            messageDatabaseReference.child(currentUser!!.uid)
                .child(context.getString(R.string.LAST_MESSAGE_WITH_) + receiver)
                .removeValue()
        }
    }
}