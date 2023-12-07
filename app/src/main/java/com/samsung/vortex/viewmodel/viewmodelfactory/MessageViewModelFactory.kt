package com.samsung.vortex.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.samsung.vortex.viewmodel.DocMessageViewModel
import com.samsung.vortex.viewmodel.MessageViewModel

class MessageViewModelFactory (private val sender: String = "", private val receiver: String = ""): ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T = MessageViewModel(sender, receiver) as T
}