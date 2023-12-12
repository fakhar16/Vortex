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
import com.samsung.vortex.adapters.CallAdapter
import com.samsung.vortex.databinding.FragmentCallBinding
import com.samsung.vortex.model.CallLog
import com.samsung.vortex.viewmodel.CallViewModel
import java.util.Locale

class CallFragment : Fragment() {
    private lateinit var viewModel: CallViewModel
    private lateinit var binding: FragmentCallBinding
    private lateinit var adapter: CallAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentCallBinding.inflate(inflater, container, false)
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
        viewModel = ViewModelProvider(this)[CallViewModel::class.java]
        viewModel.getCallLogs()!!.observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }
//        viewModel.getMissedCallLogs()!!.observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }

    }

    private fun setupRecyclerView(viewModel: CallViewModel) {
        binding.callList.layoutManager = LinearLayoutManager(context)
        adapter = CallAdapter(requireContext(), viewModel.getCallLogs().value!! )
        binding.callList.addItemDecoration(DividerItemDecoration(binding.callList.context, DividerItemDecoration.VERTICAL))
        binding.callList.adapter = adapter
    }

    private fun handleItemsClick() {
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


    private fun filter(text: String) {
//        val filteredList: ArrayList<CallLog> = ArrayList()
//        for (item in viewModel.getCallLogs()!!.value!!)
//            if (item.friendName.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault())))
//                filteredList.add(item)
//        if (filteredList.isNotEmpty()) {
//            adapter.filterList(filteredList)
//        } else {
//            adapter.filterList(ArrayList())
//        }
    }
}