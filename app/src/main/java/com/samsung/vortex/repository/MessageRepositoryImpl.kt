package com.samsung.vortex.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.starMessagesDatabaseReference
import com.samsung.vortex.model.Message
import com.samsung.vortex.repository.interfaces.IMessageRepository
import com.samsung.vortex.utils.Utils.Companion.currentUser

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

    private var mStarredMessages = ArrayList<Message>()
    var starMessages = MutableLiveData<ArrayList<Message>>()

    private var mStarredMessagesWithReceiver = ArrayList<Message>()
    var starMessagesWithReceiver = MutableLiveData<ArrayList<Message>>()

    override fun getMessages(sender: String, receiver: String): MutableLiveData<ArrayList<Message>> {
        mMessages = ArrayList()
        loadMessages(sender, receiver)
        messages.value = mMessages
        return messages
    }

    override fun getStarredMessages(): MutableLiveData<ArrayList<Message>> {
        mStarredMessages = ArrayList()
        loadStarMessages()
        starMessages.value = mStarredMessages
        return starMessages
    }

    override fun getStarredMessagesMatchingReceiver(): MutableLiveData<ArrayList<Message>> {
        mStarredMessagesWithReceiver = ArrayList()
        loadStarMessagesWithReceiver()
        starMessagesWithReceiver.value = mStarredMessagesWithReceiver
        return starMessagesWithReceiver
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

    private fun loadStarMessages() {
        starMessagesDatabaseReference
            .child(currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mStarredMessages.clear()
                    if (snapshot.exists()) {
                        for (snapshot1 in snapshot.children) {
                            val message = snapshot1.getValue(Message::class.java)
                            mStarredMessages.add(message!!)
                        }
                    }
                    starMessages.postValue(mStarredMessages)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadStarMessagesWithReceiver() {
        starMessagesDatabaseReference
            .child(currentUser!!.uid)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mStarredMessagesWithReceiver.clear()
                    if (snapshot.exists()) {
                        for (snapshot1 in snapshot.children) {
                            val message = snapshot1.getValue(Message::class.java)
                            mStarredMessagesWithReceiver.add(message!!)
                        }
                        val temp: ArrayList<Message> = ArrayList(mStarredMessagesWithReceiver)
                        mStarredMessagesWithReceiver.clear()
                        for (message in messages.value!!) {
                            for (tempMessage in temp) {
                                if (tempMessage.messageId == message.messageId)
                                    mStarredMessagesWithReceiver.add(message)
                            }
                        }
                    }
                    starMessagesWithReceiver.postValue(mStarredMessagesWithReceiver)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}