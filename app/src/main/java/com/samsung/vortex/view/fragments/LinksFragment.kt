package com.samsung.vortex.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.adapters.LinksMessagesAdapter
import com.samsung.vortex.databinding.FragmentLinksBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.viewmodel.viewmodelfactory.LinkViewModelFactory
import com.samsung.vortex.viewmodel.LinksMessageViewModel

class LinksFragment : Fragment {
    private lateinit var binding: FragmentLinksBinding
    private lateinit var adapter: LinksMessagesAdapter
    private lateinit var receiverId: String
    private lateinit var viewModel: LinksMessageViewModel

    constructor() {
        // Required empty public constructor
    }

    constructor(receiverId: String) {
        this.receiverId = receiverId
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentLinksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupViewModel()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.linksMessagesList.layoutManager = LinearLayoutManager(context)
        adapter = LinksMessagesAdapter(requireContext(), viewModel.getLinksMessageWithReceiver()!!.value!!
        )
        binding.linksMessagesList.addItemDecoration(DividerItemDecoration(binding.linksMessagesList.context, DividerItemDecoration.VERTICAL))
        binding.linksMessagesList.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, LinkViewModelFactory(receiverId))[LinksMessageViewModel::class.java]
        viewModel.getLinksMessageWithReceiver()!!.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
            updateMediaMessageLayout()
        }
    }

    private fun updateMediaMessageLayout() {
        if (adapter.itemCount == 0) {
            binding.noLinksMessageLayout.visibility = View.VISIBLE
            userDatabaseReference.child(receiverId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val receiver: User = snapshot.getValue(User::class.java)!!
                            binding.noLinksDesc.text = String.format("Links you send and receive with %s will appear here", receiver.name)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            binding.noLinksMessageLayout.visibility = View.GONE
        }
    }
}