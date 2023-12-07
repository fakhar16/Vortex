package com.samsung.vortex.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.samsung.vortex.viewmodel.StarredMessageViewModel

class StarMessageViewModelFactory(private val isReceiver: Boolean = false): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = StarredMessageViewModel(isReceiver) as T
}