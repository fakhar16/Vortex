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
import com.samsung.vortex.adapters.DocMessagesAdapter
import com.samsung.vortex.databinding.FragmentDocsBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.viewmodel.DocMessageViewModel
import com.samsung.vortex.viewmodel.DocViewModelFactory

class DocsFragment : Fragment {
    private lateinit var binding: FragmentDocsBinding
    private lateinit var adapter: DocMessagesAdapter
    private lateinit var receiverId: String
    private lateinit var viewModel: DocMessageViewModel

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
        binding = FragmentDocsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupViewModel()
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.docsMessagesList.layoutManager = LinearLayoutManager(context)
        adapter = DocMessagesAdapter(requireContext(), viewModel.getDocMessageWithReceiver()!!.value!!)
        binding.docsMessagesList.addItemDecoration(DividerItemDecoration(binding.docsMessagesList.context, DividerItemDecoration.VERTICAL))
        binding.docsMessagesList.adapter = adapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this, DocViewModelFactory(receiverId))[DocMessageViewModel::class.java]
        viewModel.getDocMessageWithReceiver()!!.observe(viewLifecycleOwner) {
            adapter.notifyDataSetChanged()
            updateMediaMessageLayout()
        }
    }

    private fun updateMediaMessageLayout() {
        if (adapter.itemCount == 0) {
            binding.noDocsMessageLayout.visibility = View.VISIBLE
            userDatabaseReference.child(receiverId)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val receiver: User = snapshot.getValue(User::class.java)!!
                            binding.noDocsDesc.text = String.format("Tap + to share documents with %s", receiver.name)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        } else {
            binding.noDocsMessageLayout.visibility = View.GONE
        }
    }
}