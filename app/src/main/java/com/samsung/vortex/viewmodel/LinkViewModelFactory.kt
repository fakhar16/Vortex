package com.samsung.vortex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LinkViewModelFactory (private val receiver: String = ""): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = LinksMessageViewModel(receiver) as T
}