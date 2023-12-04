package com.samsung.vortex.repository.interfaces

import androidx.lifecycle.MutableLiveData
import com.samsung.vortex.model.Message

interface IMessageRepository {
    fun getMessages(sender: String, receiver: String): MutableLiveData<ArrayList<Message>>
    fun getStarredMessages(): MutableLiveData<ArrayList<Message>>
    fun getStarredMessagesMatchingReceiver(): MutableLiveData<ArrayList<Message>>
    fun getMediaMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>>
    fun getDocMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>>
    fun getLinksMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>>
}