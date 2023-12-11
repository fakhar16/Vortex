package com.samsung.vortex.adapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.samsung.vortex.R
import com.samsung.vortex.databinding.ItemStatusBinding
import com.samsung.vortex.model.Status
import com.samsung.vortex.model.UserStatus
import com.samsung.vortex.view.activities.MainActivity
import com.squareup.picasso.Picasso
import omari.hamza.storyview.StoryView
import omari.hamza.storyview.callback.StoryClickListeners
import omari.hamza.storyview.model.MyStory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatusAdapter(var context: Context, private var userStatuses: ArrayList<UserStatus>) :
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val inflater = LayoutInflater.from(context)
        val layoutBinding: ItemStatusBinding = ItemStatusBinding.inflate(inflater, parent, false)
        return StatusViewHolder(layoutBinding)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val userStatus: UserStatus = userStatuses[position]
        if (userStatus.statuses!!.size == 0) return
        val lastStatus: Status = userStatus.statuses!![userStatus.statuses!!.size - 1]
        Picasso.get().load(lastStatus.imageUrl).placeholder(R.drawable.profile_image).into(holder.binding.image)
        holder.binding.circularStatusView.setPortionsCount(userStatus.statuses!!.size)
        holder.binding.name.text = userStatus.name
        holder.binding.status.text = SimpleDateFormat("hh:mm a", Locale.US).format(Date(userStatus.lastUpdated))
        holder.binding.statusView.setOnClickListener {
            val myStories: ArrayList<MyStory> = ArrayList()
            for (status in userStatus.statuses!!) {
                myStories.add(MyStory(status.imageUrl))
            }
            StoryView.Builder((context as MainActivity).supportFragmentManager)
                .setStoriesList(myStories) // Required
                .setStoryDuration(5000) // Default is 2000 Millis (2 Seconds)
                .setTitleText(userStatus.name) // Default is Hidden
                .setSubtitleText("") // Default is Hidden
                .setTitleLogoUrl(userStatus.profileImage) // Default is Hidden
                .setStoryClickListeners(object : StoryClickListeners {
                    override fun onDescriptionClickListener(position1: Int) {
                        //your action
                    }

                    override fun onTitleIconClickListener(position1: Int) {
                        //your action
                    }
                }) // Optional Listeners
                .build() // Must be called before calling show method
                .show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filterList(filterList: ArrayList<UserStatus>) {
        userStatuses = filterList
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return userStatuses.size
    }

    inner class StatusViewHolder(var binding: ItemStatusBinding) : RecyclerView.ViewHolder(binding.root)
}