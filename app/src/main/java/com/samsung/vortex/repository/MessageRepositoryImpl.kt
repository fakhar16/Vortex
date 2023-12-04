package com.samsung.vortex.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.model.Message
import com.samsung.vortex.repository.interfaces.IMessageRepository

class MessageRepositoryImpl : IMessageRepository{
    companion object {
        private var instance: MessageRepositoryImpl? = null

        fun getInstance(): MessageRepositoryImpl {
            if(instance == null)
                instance = MessageRepositoryImpl()
            return instance!!
        }
    }

    private var mMessages = ArrayList<Message>()
    var messages = MutableLiveData<ArrayList<Message>>()

    override fun getMessages(sender: String, receiver: String): MutableLiveData<ArrayList<Message>> {
        mMessages = ArrayList()
        loadMessages(sender, receiver)
        messages.value = mMessages
        return messages
    }

    override fun getStarredMessages(): MutableLiveData<ArrayList<Message>> {
        TODO("Not yet implemented")
    }

    override fun getStarredMessagesMatchingReceiver(): MutableLiveData<ArrayList<Message>> {
        TODO("Not yet implemented")
    }

    override fun getMediaMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>> {
        TODO("Not yet implemented")
    }

    override fun getDocMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>> {
        TODO("Not yet implemented")
    }

    override fun getLinksMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>> {
        TODO("Not yet implemented")
    }

    private fun loadMessages(messageSenderId: String, messageReceiverId: String) {
        messageDatabaseReference
            .child(messageSenderId)
            .child(messageReceiverId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mMessages.clear()
                    if (snapshot.exists()) {
                        for (snapshot1 in snapshot.children) {
                            val message = snapshot1.getValue(Message::class.java)
                            mMessages.add(message!!)
                        }
                    }
                    messages.postValue(mMessages)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}