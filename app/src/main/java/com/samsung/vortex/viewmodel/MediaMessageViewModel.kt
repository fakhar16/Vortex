package com.samsung.vortex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.vortex.model.Message
import com.samsung.vortex.repository.MessageRepositoryImpl

class MediaMessageViewModel(private var receiver: String) : ViewModel() {
    private var messagesWithReceiver: MutableLiveData<ArrayList<Message>>? = null

    fun getMediaMessageWithReceiver(): LiveData<ArrayList<Message>>? {
        return messagesWithReceiver
    }

    init {
        if (messagesWithReceiver == null)
            messagesWithReceiver = MessageRepositoryImpl.getInstance().getMediaMessagesMatchingReceiver(receiver)
    }
}
