package com.samsung.vortex.utils.bottomsheethandler

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.samsung.vortex.R

class MessageBottomSheetHandler {
    companion object {
        fun start(context: Context) {
            val dialog = BottomSheetDialog(context)
            val view = View.inflate(context, R.layout.message_bottom_sheet_layout, null)

            dialog.setContentView(view)
            dialog.setCanceledOnTouchOutside(false)
            (view.parent as View).setBackgroundColor(Color.TRANSPARENT)

            //cancel button
            dialog.findViewById<Button>(R.id.cancel)!!.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
        }
    }
}