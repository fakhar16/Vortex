package com.samsung.vortex.bottomsheethandler

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.FirebaseUtils
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
            messageDatabaseReference.child(currentUser!!.uid)
                .child(receiver)
                .addValueEventListener(object: ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val message = child.getValue(Message::class.java)
                                FirebaseUtils.deleteMessage(message!!)
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                    }

                })

            messageDatabaseReference.child(currentUser!!.uid)
                .child(context.getString(R.string.LAST_MESSAGE_WITH_) + receiver)
                .removeValue()
        }
    }
}