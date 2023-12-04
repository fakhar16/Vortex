package com.samsung.vortex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.vortex.model.Message
import com.samsung.vortex.repository.ChatsRepositoryImpl
import com.samsung.vortex.repository.MessageRepositoryImpl

class MessageViewModel : ViewModel() {
    var messages: MutableLiveData<ArrayList<Message>>? = null
    fun getMessage(): LiveData<ArrayList<Message>>? {
        return messages
    }

    fun init(sender: String, receiver: String) {
        if (messages != null) return
        messages = MessageRepositoryImpl.getInstance().getMessages(sender, receiver)
    }
}