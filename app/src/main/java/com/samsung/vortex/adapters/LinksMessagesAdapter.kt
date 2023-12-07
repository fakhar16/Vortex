package com.samsung.vortex.adapters

import android.content.Context
import android.graphics.Color
import android.text.Html
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.samsung.vortex.databinding.ItemMediaLinkBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.utils.Utils.Companion.getDateTimeString

class LinksMessagesAdapter(private val context: Context, private var messageList: ArrayList<Message>) :
    RecyclerView.Adapter<LinksMessagesAdapter.LinksMessagesViewHolder>() {

    inner class LinksMessagesViewHolder(var binding: ItemMediaLinkBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LinksMessagesViewHolder {
        val inflater = LayoutInflater.from(context)
        val layoutBinding: ItemMediaLinkBinding = ItemMediaLinkBinding.inflate(inflater, parent, false)
        return LinksMessagesViewHolder(layoutBinding)
    }

    override fun onBindViewHolder(holder: LinksMessagesViewHolder, position: Int) {
        val message: Message = messageList[position]
        val linkedText =
            String.format("<a href=\"%s\">%s</a> ", message.message, message.message)
        holder.binding.linkText.text = Html.fromHtml(linkedText, Html.FROM_HTML_MODE_LEGACY)
        holder.binding.linkText.movementMethod = LinkMovementMethod.getInstance()
        holder.binding.linkText.setLinkTextColor(Color.BLUE)
        holder.binding.linkTime.text = getDateTimeString(message.time)
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}
