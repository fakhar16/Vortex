package com.samsung.vortex.repository

import android.annotation.SuppressLint
import android.database.Cursor
import android.provider.ContactsContract
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.messageDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.User
import com.samsung.vortex.repository.interfaces.IChatsRepository
import com.samsung.vortex.utils.Utils

class ChatsRepositoryImpl : IChatsRepository{
    private val mUsers = ArrayList<User>()
    var users = MutableLiveData<ArrayList<User>>()
    var usersWithUnreadChats = MutableLiveData<ArrayList<User>>()
    private val contactList = ArrayList<String>()

    companion object {
        private var instance: ChatsRepositoryImpl? = null

        fun getInstance(): ChatsRepositoryImpl {
            if(instance == null)
                instance = ChatsRepositoryImpl()
            return instance!!
        }
    }

    override fun getChats(): MutableLiveData<ArrayList<User>> {
        if (mUsers.size == 0) loadContacts()
        users.value = mUsers
        return users
    }

    override fun getUnreadChats(): MutableLiveData<ArrayList<User>> {
        loadContactsWitUnreadChats()
        return usersWithUnreadChats
    }

    private fun loadContacts() {
        loadContactListFromPhone()
        userDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value != null) {
                    mUsers.clear()
                    for (dataSnapshot in snapshot.children) {
                        if (dataSnapshot.hasChild(VortexApplication.application.applicationContext.getString(R.string.NAME))) {
                            val user = dataSnapshot.getValue(User::class.java)!!
                            if (user.uid != FirebaseAuth.getInstance().uid && contactList.contains(user.phone_number))
                                mUsers.add(user)
                        }
                    }
                    users.postValue(mUsers)
                    loadContactsWitUnreadChats()
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun loadContactsWitUnreadChats() {
        val finalUsers = mUsers.clone() as ArrayList<User>
        for (user in mUsers) {
            messageDatabaseReference
                .child(Utils.currentUser!!.uid)
                .child(user.uid)
                .limitToLast(1)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            for (child in snapshot.children) {
                                val message: Message = child.getValue(Message::class.java)!!
                                if (!message.isUnread) {
                                    finalUsers.remove(user)
                                }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
        usersWithUnreadChats.postValue(finalUsers)
    }

    @SuppressLint("Range")
    private fun loadContactListFromPhone() {
        @SuppressLint("Recycle") val phones: Cursor? = VortexApplication.application.applicationContext.contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null)
        if (phones != null) {
            while (phones.moveToNext()) {
                val phone = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER))
                if (phone != null) {
                    contactList.add(phone)
                }
            }
        }
    }
}