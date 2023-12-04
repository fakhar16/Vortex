package com.samsung.vortex.utils.bottomsheethandler

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.samsung.vortex.R
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.Utils.Companion.copyMessage

class MessageBottomSheetHandler {
    companion object {
        fun start(context: Context, message: Message, messages: ArrayList<Message>, VIEW_TYPE: Int) {
            val dialog = BottomSheetDialog(context)
            val view = View.inflate(context, R.layout.message_bottom_sheet_layout, null)

            dialog.setContentView(view)
            dialog.setCanceledOnTouchOutside(false)
            (view.parent as View).setBackgroundColor(Color.TRANSPARENT)

            //copy button
            dialog.findViewById<LinearLayout>(R.id.copy)!!.setOnClickListener {
                when (message.type) {
                    context.getString(R.string.IMAGE) -> {
            //                    Utils.copyImage(Uri.parse(message.getMessage()), message.getMessageId())
                    }
                    context.getString(R.string.VIDEO) -> {
            //                    Utils.copyVideo(Uri.parse(message.getMessage()), message.getMessageId())
                    }
                    context.getString(R.string.PDF_FILES) -> {
            //                    Utils.copyDoc(
            //                        Uri.parse(message.getMessage()),
            //                        message.getMessageId(),
            //                        message.getFilename(),
            //                        message.getFileSize()
            //                    )
                    }
                    else -> {
                        copyMessage(message.message)
                    }
                }
                dialog.dismiss()
            }

            //delete button
            dialog.findViewById<LinearLayout>(R.id.delete)!!.setOnClickListener {
                DeleteMessageBottomSheetHandler.start(context, message, messages, VIEW_TYPE)
                dialog.dismiss()
            }

            //cancel button
            dialog.findViewById<Button>(R.id.cancel)!!.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}