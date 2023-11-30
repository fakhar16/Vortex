package com.samsung.vortex.view.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.samsung.vortex.R
import com.samsung.vortex.databinding.FragmentSettingsBinding
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.view.activities.SettingsActivity
import com.samsung.vortex.view.activities.StarMessageActivity
import com.squareup.picasso.Picasso

class SettingsFragment : Fragment() {
    private lateinit var binding: FragmentSettingsBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        handleItemsClick()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupProfileInfo()
    }

    private fun handleItemsClick() {
        binding.profileInfo.setOnClickListener {
            val intent = Intent(context, SettingsActivity::class.java)
            startActivity(intent)
        }
        binding.starMessages.setOnClickListener {
            val intent = Intent(context, StarMessageActivity::class.java)
            startActivity(intent)
        }
    }

    private fun setupProfileInfo() {
        binding.userName.text = currentUser?.name
        if (currentUser?.status?.isNotEmpty() == true) {
            binding.userStatus.text = currentUser?.status
        }
        if (currentUser?.image?.isNotEmpty() == true) {
            Picasso.get().load(Utils.getImageOffline(currentUser!!.image, currentUser!!.uid)).placeholder(R.drawable.profile_image).into(binding.userImage)
        }
    }
}