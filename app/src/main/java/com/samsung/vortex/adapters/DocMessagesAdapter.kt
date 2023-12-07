package com.samsung.vortex.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.rajat.pdfviewer.PdfViewerActivity.Companion.launchPdfFromUrl
import com.samsung.vortex.databinding.ItemMediaDocBinding
import com.samsung.vortex.model.Message

class DocMessagesAdapter(private val context: Context, private var messageList: ArrayList<Message>) :
    RecyclerView.Adapter<DocMessagesAdapter.DocMessagesViewHolder>() {

    inner class DocMessagesViewHolder(var binding: ItemMediaDocBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DocMessagesViewHolder {
        val inflater = LayoutInflater.from(context)
        val layoutBinding: ItemMediaDocBinding = ItemMediaDocBinding.inflate(inflater, parent, false)
        return DocMessagesViewHolder(layoutBinding)
    }

    override fun onBindViewHolder(holder: DocMessagesViewHolder, position: Int) {
        val message: Message = messageList[position]
        holder.binding.fileName.text = message.fileName
        holder.binding.fileLayout.setOnClickListener {
            context.startActivity(launchPdfFromUrl(context, message.message, message.fileName, "", true))
        }
        holder.binding.fileSize.text = message.fileSize
    }

    override fun getItemCount(): Int {
        return messageList.size
    }
}
