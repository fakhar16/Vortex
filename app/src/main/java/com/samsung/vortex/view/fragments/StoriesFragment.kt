package com.samsung.vortex.view.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.samsung.vortex.R
import com.samsung.vortex.adapters.StatusAdapter
import com.samsung.vortex.databinding.FragmentStoriesBinding
import com.samsung.vortex.model.UserStatus
import com.samsung.vortex.repository.StatusRepositoryImpl
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.currentUser
import com.samsung.vortex.view.activities.CameraxActivity
import com.samsung.vortex.viewmodel.StatusViewModel
import com.squareup.picasso.Picasso
import java.util.Locale

class StoriesFragment : Fragment() {
    private lateinit var binding: FragmentStoriesBinding
    private lateinit var statusAdapter: StatusAdapter
    private lateinit var viewModel: StatusViewModel
    private val imagePickActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            Utils.showLoadingBar(requireActivity(), binding.progressbar.root)
            val data = result.data
            if (data != null && data.data != null) {
                StatusRepositoryImpl.getInstance()!!.uploadStatus(data.data!!, currentUser!!, binding.progressbar.root, requireActivity())
            }
        }
    }
    private val imageCaptureActivityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data!!
                val fileUri = Uri.parse(data.getStringExtra(getString(R.string.IMAGE_URI)))
                val bitmap: Bitmap

                val source = ImageDecoder.createSource(requireContext().contentResolver, fileUri)
                bitmap = ImageDecoder.decodeBitmap(source)
                val matrix = Matrix()
                matrix.preRotate(0f)
                val finalBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                val os = requireContext().contentResolver.openOutputStream(fileUri)
                finalBitmap.compress(Bitmap.CompressFormat.PNG, 100, os!!)
                Utils.showLoadingBar(requireActivity(), binding.progressbar.root)
                StatusRepositoryImpl.getInstance()!!.uploadStatus(fileUri, currentUser!!, binding.progressbar.root, requireActivity())
            }
        }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = FragmentStoriesBinding.inflate(inflater, container, false)
        initProgressBarDetails()
        loadUserInfo()
        setupViewModel()
        setupRecyclerView()
        handleItemsClick()
        return binding.root
    }

    private fun loadUserInfo() {
        Picasso.get().load(Utils.getImageOffline(currentUser!!.image, currentUser!!.uid)).placeholder(R.drawable.profile_image).into(binding.image)
    }

    @SuppressLint("SetTextI18n")
    private fun initProgressBarDetails() {
        binding.progressbar.dialogTitle.text = "Uploading Image"
        binding.progressbar.dialogDescription.text = "Please wait, while we are uploading your image..."
    }

    private fun setupRecyclerView() {
        statusAdapter = StatusAdapter(requireContext(), viewModel.getUserStatues()!!.value!!)
        binding.statusList.layoutManager = LinearLayoutManager(context)
        binding.statusList.addItemDecoration(DividerItemDecoration(binding.statusList.context, DividerItemDecoration.VERTICAL))
        binding.statusList.adapter = statusAdapter
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[StatusViewModel::class.java]
        viewModel.getUserStatues()!!.observe(viewLifecycleOwner) { statusAdapter.notifyDataSetChanged() }
    }

    private fun handleItemsClick() {
        binding.btnStatus.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            imagePickActivityResultLauncher.launch(intent)
        }
        binding.addStatus.setOnClickListener { cameraButtonClicked() }
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

    private fun cameraButtonClicked() {
        val intent = Intent(requireContext(), CameraxActivity::class.java)
        intent.putExtra(getString(R.string.IS_FROM_STORIES), true)
        imageCaptureActivityResultLauncher.launch(intent)
    }

    private fun filter(text: String) {
        val filteredList: ArrayList<UserStatus> = ArrayList()
        for (item in viewModel.getUserStatues()!!.value!!) {
            if (item.name.lowercase(Locale.ROOT).contains(text.lowercase(Locale.getDefault()))) {
                filteredList.add(item)
            }
        }
        if (filteredList.isNotEmpty()) {
            statusAdapter.filterList(filteredList)
        } else {
            statusAdapter.filterList(ArrayList())
        }
    }
}