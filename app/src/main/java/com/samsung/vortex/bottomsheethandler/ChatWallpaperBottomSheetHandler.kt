package com.samsung.vortex.bottomsheethandler

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.view.activities.ChatActivity
import com.samsung.vortex.view.activities.SelectWallpaperActivity

class ChatWallpaperBottomSheetHandler {
    companion object {
        @SuppressLint("StaticFieldLeak")
        private lateinit var bottomSheetDialog: BottomSheetDialog

        fun start(context: Context, imageId: String) {
            val contentView = View.inflate(context, R.layout.chat_wallpaper_bottom_sheet_layout, null)
            bottomSheetDialog = BottomSheetDialog(context)
            bottomSheetDialog.setContentView(contentView)

            BottomSheetBehavior.from(contentView.parent as  View).state = BottomSheetBehavior.STATE_EXPANDED

            bottomSheetDialog.show()

            (bottomSheetDialog.findViewById<ImageView>(R.id.bg) as ImageView).setImageResource(context.resources.getIdentifier(imageId, "drawable", context.packageName))

            bottomSheetDialog.findViewById<TextView>(R.id.cancel)!!.setOnClickListener { bottomSheetDialog.dismiss() }
            bottomSheetDialog.findViewById<TextView>(R.id.set)!!.setOnClickListener {
                VortexApplication
                    .chatBgDatabaseReference
                    .child(com.samsung.vortex.utils.Utils.currentUser!!.uid)
                    .child(ChatActivity.receiver.uid)
                    .child(context.getString(R.string.IMAGE_ID))
                    .setValue(imageId)

                bottomSheetDialog.dismiss()

                (context as SelectWallpaperActivity).finish()
            }
        }
    }
}