package com.samsung.vortex.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
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

    private var mDocMessagesWithReceiver = ArrayList<Message>()
    var docMessagesWithReceiver = MutableLiveData<ArrayList<Message>>()

    private var mMediaMessagesWithReceiver = ArrayList<Message>()
    var mediaMessagesWithReceiver = MutableLiveData<ArrayList<Message>>()

    private var mLinksMessagesWithReceiver = ArrayList<Message>()
    var linksMessagesWithReceiver = MutableLiveData<ArrayList<Message>>()

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
        mMediaMessagesWithReceiver = ArrayList()
        loadMediaMessagesWithReceiver(receiver)
        mediaMessagesWithReceiver.value = mMediaMessagesWithReceiver
        return mediaMessagesWithReceiver
    }

    override fun getDocMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>> {
        mDocMessagesWithReceiver = ArrayList()
        loadDocMessagesWithReceiver(receiver)
        docMessagesWithReceiver.value = mDocMessagesWithReceiver
        return docMessagesWithReceiver
    }

    override fun getLinksMessagesMatchingReceiver(receiver: String): MutableLiveData<ArrayList<Message>> {
        mLinksMessagesWithReceiver = ArrayList()
        loadLinksMessagesWithReceiver(receiver)
        linksMessagesWithReceiver.value = mLinksMessagesWithReceiver
        return linksMessagesWithReceiver
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
                        if (messages.value != null) {
                            for (message in messages.value!!) {
                                for (tempMessage in temp) {
                                    if (tempMessage.messageId == message.messageId)
                                        mStarredMessagesWithReceiver.add(message)
                                }
                            }
                        }
                    }
                    starMessagesWithReceiver.postValue(mStarredMessagesWithReceiver)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadMediaMessagesWithReceiver(receiver: String) {
        messageDatabaseReference
            .child(currentUser!!.uid)
            .child(receiver)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mMediaMessagesWithReceiver.clear()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val message = child.getValue(Message::class.java)!!
                            if (message.type == VortexApplication.application.applicationContext
                                .getString(R.string.IMAGE) || message.type == VortexApplication.application.applicationContext
                                    .getString(R.string.VIDEO) || message.type == VortexApplication.application.getString(R.string.AUDIO_RECORDING))
                                mMediaMessagesWithReceiver.add(message)
                        }
                    }
                    mediaMessagesWithReceiver.postValue(mMediaMessagesWithReceiver)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadLinksMessagesWithReceiver(receiver: String?) {
        messageDatabaseReference
            .child(currentUser!!.uid)
            .child(receiver!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mLinksMessagesWithReceiver.clear()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val message = child.getValue(Message::class.java)!!
                            if (message.type == VortexApplication.application.applicationContext.getString(R.string.URL)
                            ) mLinksMessagesWithReceiver.add(
                                message
                            )
                        }
                    }
                    linksMessagesWithReceiver.postValue(mLinksMessagesWithReceiver)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun loadDocMessagesWithReceiver(receiver: String) {
        messageDatabaseReference
            .child(currentUser!!.uid)
            .child(receiver)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    mDocMessagesWithReceiver.clear()
                    if (snapshot.exists()) {
                        for (child in snapshot.children) {
                            val message = child.getValue(Message::class.java)!!
                            if (message.type == VortexApplication.application.getString(R.string.PDF_FILES)) 
                                mDocMessagesWithReceiver.add(message)
                        }
                    }
                    docMessagesWithReceiver.postValue(mDocMessagesWithReceiver)
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}