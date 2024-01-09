package com.samsung.vortex.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import com.samsung.vortex.R
import com.samsung.vortex.bottomsheethandler.ChatWallpaperBottomSheetHandler

class ChatWallpaperAdapter(val context: Context, private val imageIds:ArrayList<Int>): BaseAdapter() {
    override fun getCount(): Int {
        return imageIds.size
    }

    override fun getItem(position: Int): Any {
        return imageIds[position]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var listItemView: View? = convertView
        if (listItemView == null) {
            listItemView = LayoutInflater.from(context).inflate(R.layout.chat_bg_item, parent, false)
        }

        listItemView!!.findViewById<ImageView>(R.id.wallpaper).setImageResource(imageIds[position])

        listItemView.setOnClickListener {
            ChatWallpaperBottomSheetHandler.start(context, context.resources.getResourceEntryName(imageIds[position]))
        }

        return listItemView
    }
}