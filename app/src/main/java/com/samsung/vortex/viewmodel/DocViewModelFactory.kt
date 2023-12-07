package com.samsung.vortex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class DocViewModelFactory (private val receiver: String = ""): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = DocMessageViewModel(receiver) as T
}