package com.samsung.vortex.view.fragments

import android.annotation.SuppressLint
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.samsung.vortex.R
import com.samsung.vortex.adapters.CallAdapter
import com.samsung.vortex.databinding.FragmentCallBinding
import com.samsung.vortex.model.CallLog
import com.samsung.vortex.viewmodel.CallViewModel

class CallFragment : Fragment() {
    private lateinit var viewModel: CallViewModel
    private lateinit var binding: FragmentCallBinding
    private lateinit var adapter: CallAdapter

    private var isEditCallLogs = false

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
        viewModel.getCallLogs().observe(viewLifecycleOwner) { adapter.notifyDataSetChanged() }

    }

    private fun setupRecyclerView(viewModel: CallViewModel) {
        binding.callList.layoutManager = LinearLayoutManager(context)
        adapter = CallAdapter(requireContext(), viewModel.getCallLogs().value!! )
        binding.callList.addItemDecoration(DividerItemDecoration(binding.callList.context, DividerItemDecoration.VERTICAL))
        binding.callList.adapter = adapter
    }

    @SuppressLint("ResourceAsColor")
    private fun handleItemsClick() {
        binding.missedCalls.setOnClickListener {
            binding.allCalls.backgroundTintList = context?.resources?.getColorStateList(R.color.white, requireContext().theme)
            binding.missedCalls.backgroundTintList = context?.resources?.getColorStateList(R.color.gray, requireContext().theme)
            binding.allCalls.setTypeface(null, Typeface.NORMAL)
            binding.missedCalls.setTypeface(null, Typeface.BOLD)

            val missedCallList = ArrayList<CallLog>()
            for (call in viewModel.getCallLogs().value!!) {
                if (call.type == getString(R.string.MISSED_CALL)) {
                    missedCallList.add(call)
                }
            }

            adapter.filterCalls(missedCallList)
        }

        binding.allCalls.setOnClickListener {
            binding.missedCalls.backgroundTintList = context?.resources?.getColorStateList(R.color.white, requireContext().theme)
            binding.allCalls.backgroundTintList = context?.resources?.getColorStateList(R.color.gray, requireContext().theme)
            binding.allCalls.setTypeface(null, Typeface.BOLD)
            binding.missedCalls.setTypeface(null, Typeface.NORMAL)

            adapter.filterCalls(viewModel.getCallLogs().value!!)
        }

        binding.editCallLogs.setOnClickListener { editCallLogs() }
    }

    @SuppressLint("SetTextI18n")
    private fun editCallLogs() {
        isEditCallLogs = !isEditCallLogs

        if (isEditCallLogs) {
            binding.clearCallLogs.visibility = View.VISIBLE
            binding.editCallLogs.text = "Done"
            adapter.showDeleteButton()
        } else {
            binding.clearCallLogs.visibility = View.INVISIBLE
            binding.editCallLogs.text = "Edit"
            adapter.hideDeleteButton()
        }
    }
}