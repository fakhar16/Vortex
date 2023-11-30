package com.samsung.vortex.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.databinding.FragmentChatBinding
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.TAG
import com.squareup.picasso.Picasso

class ChatFragment : Fragment() {
    private lateinit var binding: FragmentChatBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }
}