package com.samsung.vortex.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.samsung.vortex.viewmodel.MediaMessageViewModel

class MediaViewModelFactory (private val receiver: String = ""): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = MediaMessageViewModel(receiver) as T
}