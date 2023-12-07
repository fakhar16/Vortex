package com.samsung.vortex.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.adapters.MediaMessagesAdapter
import com.samsung.vortex.databinding.FragmentMediaBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.viewmodel.MediaMessageViewModel
import com.samsung.vortex.viewmodel.viewmodelfactory.MediaViewModelFactory


class MediaFragment : Fragment {
    private lateinit var binding: FragmentMediaBinding
    private lateinit var adapter: MediaMessagesAdapter
    private lateinit var receiverId: String
    private lateinit var viewModel: MediaMessageViewModel

    constructor() {
        // Required empty public constructor
    }

    constructor(receiverId: String) {
        this.receiverId = receiverId
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentMediaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupViewModel()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.mediaMessagesList.layoutManager = GridLayoutManager(
            context, 3
        )
        adapter = MediaMessagesAdapter(requireContext(), viewModel.getMediaMessageWithReceiver()!!.value!!)
        binding.mediaMessagesList.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, MediaViewModelFactory(receiverId))[MediaMessageViewModel::class.java]
        viewModel.getMediaMessageWithReceiver()!!.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
            updateMediaMessageLayout()
        }
    }

    private fun updateMediaMessageLayout() {
        if (adapter.itemCount == 0) {
            binding.noMediaMessageLayout.visibility = View.VISIBLE
            userDatabaseReference.child(receiverId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val receiver: User = snapshot.getValue(User::class.java)!!
                            binding.noMediaDesc.text = java.lang.String.format("Tap + to share media with %s", receiver.name)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            binding.noMediaMessageLayout.visibility = View.GONE
        }
    }
}