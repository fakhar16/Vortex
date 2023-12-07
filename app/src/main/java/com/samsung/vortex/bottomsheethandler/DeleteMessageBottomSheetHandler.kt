package com.samsung.vortex.bottomsheethandler

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.samsung.vortex.R
import com.samsung.vortex.adapters.MessagesAdapter.Companion.ITEM_RECEIVE
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils.Companion.currentUser

class DeleteMessageBottomSheetHandler {
    companion object {
        fun start(context: Context, message: Message, messages: ArrayList<Message>, VIEW_TYPE: Int) {
            val dialog = BottomSheetDialog(context)
            val view = View.inflate(context, R.layout.delete_message_bottom_sheet_layout, null)

            dialog.setContentView(view)
            dialog.setCanceledOnTouchOutside(false)
            (view.parent as View).setBackgroundColor(Color.TRANSPARENT)

            if (VIEW_TYPE == ITEM_RECEIVE) {
                dialog.findViewById<TextView>(R.id.delete_for_everyone)!!.visibility = View.GONE
            }

            //delete for everyone
            dialog.findViewById<TextView>(R.id.delete_for_everyone)!!.setOnClickListener {
                FirebaseUtils.deleteMessageForEveryone(message)
                updateLastMessage(messages, message)
                dialog.dismiss()
            }

            //delete for me
            dialog.findViewById<TextView>(R.id.delete_for_me)!!.setOnClickListener {
                FirebaseUtils.deleteMessage(message)
                updateLastMessage(messages, message)
                dialog.dismiss()
            }


            //cancel button
            dialog.findViewById<Button>(R.id.cancel)!!.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }

        private fun updateLastMessage(userMessageList: ArrayList<Message>, message: Message) {
            // if user removed last message in the list
            if (userMessageList.indexOf(message) == userMessageList.size - 1 && userMessageList.size >= 2)
                FirebaseUtils.updateLastMessage(userMessageList[userMessageList.size - 2])
            //if user removed the only message
            if (userMessageList.size == 1) FirebaseUtils.removeLastMessages(message.from, message.to)

            //if deleted message is starred
            if (message.starred.contains(":" + currentUser!!.uid) || message.starred == "starred")
                FirebaseUtils.deleteStarredMessage(message.messageId)
        }
    }
}