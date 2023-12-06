package com.samsung.vortex.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.samsung.vortex.R
import com.samsung.vortex.databinding.UsersDisplayLayoutBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.bottomsheethandler.ForwardMessageBottomSheetHandler
import com.squareup.picasso.Picasso

class ContactAdapter(private val context: Context, userList: ArrayList<User>) :
    RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {
    private var userList: ArrayList<User>

    init {
        this.userList = userList
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val layoutBinding: UsersDisplayLayoutBinding = UsersDisplayLayoutBinding.inflate(inflater, parent, false)
        return ContactViewHolder(layoutBinding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val user: User = userList[position]
        holder.binding.userProfileName.text = user.name
        Picasso.get().load(Utils.getImageOffline(user.image, user.uid)).placeholder(R.drawable.profile_image).into(holder.binding.usersProfileImage)
        holder.binding.userProfileStatus.visibility = View.GONE
        val layoutParams = LinearLayout.LayoutParams(125, 125)
        holder.binding.usersProfileImage.layoutParams = layoutParams
        holder.binding.itemView.setOnClickListener {
            ForwardMessageBottomSheetHandler.forwardMessage(context, user.uid)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<User>) {
        userList = filterList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return userList.size
    }

    inner class ContactViewHolder(var binding: UsersDisplayLayoutBinding) : RecyclerView.ViewHolder(binding.root)
}