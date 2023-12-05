package com.samsung.vortex.view.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.samsung.vortex.R
import com.samsung.vortex.adapters.StarredMessagesAdapter
import com.samsung.vortex.databinding.ActivityStarMessageBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.viewmodel.StarredMessageViewModel
import com.samsung.vortex.viewmodel.StarMessageViewModelFactory
import java.util.Locale

class StarMessageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStarMessageBinding
    private lateinit var adapter: StarredMessagesAdapter
    private lateinit var viewModel: StarredMessageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStarMessageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initToolBar()
        setupViewModel()
        setupRecyclerView()
        handleItemsClick()
    }

    private fun initToolBar() {
        setSupportActionBar(binding.mainPageToolbar.mainAppBar)
        supportActionBar!!.title = "Starred Messages"
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        if (intent.getBooleanExtra(getString(R.string.STAR_MESSAGE_WITH_RECEIVER), false)) {
            viewModel = ViewModelProvider(this, StarMessageViewModelFactory(true))[StarredMessageViewModel::class.java]
            viewModel.getStarredMessageWithReceiver()!!.observe(this) {
                adapter.notifyDataSetChanged()
                updateStarredMessageLayout()
            }
            adapter = StarredMessagesAdapter(this, viewModel.getStarredMessageWithReceiver()!!.value!!)
        } else {
            viewModel = ViewModelProvider(this)[StarredMessageViewModel::class.java]
            viewModel.getStarredMessage()!!.observe(this) {
                adapter.notifyDataSetChanged()
                updateStarredMessageLayout()
            }
            adapter = StarredMessagesAdapter(this, viewModel.getStarredMessage()!!.value!!)
        }
    }

    private fun updateStarredMessageLayout() {
        if (adapter.itemCount == 0) {
            binding.noStarMessageLayout.visibility = View.VISIBLE
        } else {
            binding.noStarMessageLayout.visibility = View.GONE
        }
    }

    private fun setupRecyclerView() {
        binding.starMessagesList.layoutManager = LinearLayoutManager(this)
        binding.starMessagesList.addItemDecoration(
            DividerItemDecoration(
                binding.starMessagesList.context,
                DividerItemDecoration.VERTICAL
            )
        )
        binding.starMessagesList.adapter = adapter
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
        val filteredList: ArrayList<Message> = ArrayList()
        val list: ArrayList<Message> =
            if (intent.getBooleanExtra(getString(R.string.STAR_MESSAGE_WITH_RECEIVER), false)) {
            viewModel.getStarredMessageWithReceiver()!!.value!!
        } else {
            viewModel.getStarredMessage()!!.value!!
        }
        for (item in list) {
            if (item.message.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        if (filteredList.isNotEmpty()) {
            adapter.filterList(filteredList)
        } else {
            adapter.filterList(ArrayList())
        }
    }
}