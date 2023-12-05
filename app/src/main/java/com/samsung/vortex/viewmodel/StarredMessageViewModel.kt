package com.samsung.vortex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.vortex.model.Message
import com.samsung.vortex.repository.MessageRepositoryImpl

class StarredMessageViewModel: ViewModel() {
    var messages: MutableLiveData<ArrayList<Message>>? = null
    var messagesWithReceiver: MutableLiveData<ArrayList<Message>>? = null

    fun getStarredMessage(): LiveData<ArrayList<Message>>? {
        return messages
    }

    fun getStarredMessageWithReceiver(): LiveData<ArrayList<Message>>? {
        return messagesWithReceiver
    }

    init {
        if (messages == null)
            messages = MessageRepositoryImpl.getInstance().getStarredMessages()

//        if (messagesWithReceiver == null)
//            messagesWithReceiver = MessageRepositoryImpl.getInstance().getStarredMessagesMatchingReceiver()
    }
}