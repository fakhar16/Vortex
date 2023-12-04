package com.samsung.vortex.repository.interfaces

import androidx.lifecycle.MutableLiveData
import com.samsung.vortex.model.User

interface IChatsRepository {
    fun getChats() : MutableLiveData<ArrayList<User>>
    fun getUnreadChats(): MutableLiveData<java.util.ArrayList<User>>
}