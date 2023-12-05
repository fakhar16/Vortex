package com.samsung.vortex.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class StarMessageViewModelFactory(private val isReceiver: Boolean = false): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = StarredMessageViewModel(isReceiver) as T
}