package com.samsung.vortex.view.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.samsung.vortex.R
import com.samsung.vortex.adapters.ChatAdapter
import com.samsung.vortex.databinding.FragmentChatBinding
import com.samsung.vortex.model.User
import com.samsung.vortex.viewmodel.ChatViewModel
import java.util.Locale

class ChatFragment : Fragment() {
    private lateinit var viewModel: ChatViewModel
    private lateinit var binding: FragmentChatBinding
    private lateinit var adapter: ChatAdapter
    private var isUnreadFilterOn = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentChatBinding.inflate(inflater, container, false)
        handleItemsClick()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        setupViewModel()
        setupRecyclerView(viewModel)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]
        viewModel.getChats()!!.observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }

    }

    private fun setupRecyclerView(viewModel: ChatViewModel) {
        binding.chatsList.layoutManager = LinearLayoutManager(context)
        adapter = ChatAdapter(requireContext(), viewModel.getChats()!!.value!! )
        binding.chatsList.addItemDecoration(DividerItemDecoration(binding.chatsList.context, DividerItemDecoration.VERTICAL))
        binding.chatsList.adapter = adapter
    }

    private fun handleItemsClick() {
        binding.filter.setOnClickListener { filterList() }
        binding.unreadNoChatView.clearFilter.setOnClickListener {
            isUnreadFilterOn = false
            binding.filter.setImageResource(R.drawable.baseline_filter_list_24)
            binding.searchView.queryHint = "Search"
            adapter.filterList(viewModel.getChats()!!.value!!)
            binding.unreadNoChatView.root.visibility = View.GONE
        }
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                filter(newText)
                return true
            }
        })
    }

    private fun filterList() {
        isUnreadFilterOn = !isUnreadFilterOn
        binding.unreadNoChatView.root.visibility = View.GONE
        if (isUnreadFilterOn) {
            binding.filter.setImageResource(R.drawable.baseline_filter_list_off_24)
            binding.searchView.queryHint = "Search unread chats"
            adapter.filterList(viewModel.getUnreadChats().value!!)
            if (adapter.itemCount == 0) {
                binding.unreadNoChatView.root.visibility = View.VISIBLE
            }
        } else {
            binding.filter.setImageResource(R.drawable.baseline_filter_list_24)
            binding.searchView.queryHint = "Search"
            adapter.filterList(viewModel.getChats()!!.value!!)
        }
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<User> = ArrayList()
        if (isUnreadFilterOn) {
            for (item in viewModel.getUnreadChats().value!!)
                if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault())))
                    filteredList.add(item)
        } else {
            for (item in viewModel.getChats()!!.value!!)
                if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault())))
                    filteredList.add(item)
        }
        if (filteredList.isNotEmpty()) {
            adapter.filterList(filteredList)
        } else {
            adapter.filterList(ArrayList())
        }
    }
}