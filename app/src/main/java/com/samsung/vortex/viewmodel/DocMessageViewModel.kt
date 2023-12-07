package com.samsung.vortex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.vortex.model.Message
import com.samsung.vortex.repository.MessageRepositoryImpl

class DocMessageViewModel(private var receiver: String): ViewModel() {
    var messagesWithReceiver: MutableLiveData<ArrayList<Message>>? = null

    fun getDocMessageWithReceiver(): LiveData<java.util.ArrayList<Message>>? {
        return messagesWithReceiver
    }

    init {
        if (messagesWithReceiver == null)
            messagesWithReceiver = MessageRepositoryImpl.getInstance().getDocMessagesMatchingReceiver(receiver)
    }

}