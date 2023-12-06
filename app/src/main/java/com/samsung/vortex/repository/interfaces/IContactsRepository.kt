package com.samsung.vortex.repository.interfaces

import androidx.lifecycle.MutableLiveData
import com.samsung.vortex.model.User

interface IContactsRepository {
    fun getContacts(): MutableLiveData<ArrayList<User>>
    fun getContactsWithUnreadChats(): MutableLiveData<ArrayList<User>>
}