package com.samsung.vortex.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.samsung.vortex.R
import com.samsung.vortex.databinding.ItemMediaMessageBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.view.activities.MediaLinksDocsActivity
import com.squareup.picasso.Picasso

class MediaMessagesAdapter(private val context: Context, private var messageList: ArrayList<Message>) :
    RecyclerView.Adapter<MediaMessagesAdapter.MediaMessagesViewHolder>() {

    inner class MediaMessagesViewHolder(var binding: ItemMediaMessageBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaMessagesViewHolder {
        val inflater = LayoutInflater.from(context)
        val layoutBinding: ItemMediaMessageBinding = ItemMediaMessageBinding.inflate(inflater, parent, false)
        return MediaMessagesViewHolder(layoutBinding)
    }

    override fun onBindViewHolder(holder: MediaMessagesViewHolder, position: Int) {
        val message: Message = messageList[position]
        when (message.type) {
            context.getString(R.string.VIDEO) -> {
                holder.binding.play.visibility = View.VISIBLE
                Glide.with(context).load(message.message).centerCrop()
                    .placeholder(R.drawable.baseline_play_circle_outline_24)
                    .into(holder.binding.image)
                holder.binding.image.setOnClickListener {
                    (context as MediaLinksDocsActivity).showVideoPreview(holder.binding.image, message.message)
                }
            }
            context.getString(R.string.IMAGE) -> {
                Picasso.get().load(Utils.getImageOffline(message.message, message.messageId)).placeholder(R.drawable.profile_image).into(holder.binding.image)
                holder.binding.image.setOnClickListener {
                    (context as MediaLinksDocsActivity).showImagePreview(holder.binding.image, Utils.getImageOffline(message.message, message.messageId))
                }
            }
            context.getString(R.string.AUDIO_RECORDING) -> {
                holder.binding.image.setImageResource(R.drawable.audio)
                holder.binding.image.setOnClickListener {
                    (context as MediaLinksDocsActivity).showAudioPreview(holder.binding.image, Utils.getImageOffline(message.message, message.messageId))
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}
