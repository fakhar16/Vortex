package com.samsung.vortex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.vortex.model.User
import com.samsung.vortex.repository.ChatsRepositoryImpl

class ChatViewModel : ViewModel() {
    private var users: MutableLiveData<ArrayList<User>>? = null

    fun getChats(): LiveData<ArrayList<User>>? {
        return users
    }

    fun getUnreadChats(): LiveData<ArrayList<User>> {
        return ChatsRepositoryImpl.getInstance().getUnreadChats()
    }

    init {
        if (users == null)
            users = ChatsRepositoryImpl.getInstance().getChats()
    }
}