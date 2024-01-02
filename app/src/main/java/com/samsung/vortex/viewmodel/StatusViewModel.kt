package com.samsung.vortex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.vortex.model.UserStatus
import com.samsung.vortex.repository.StatusRepositoryImpl

class StatusViewModel: ViewModel() {

    private var userStatuses: MutableLiveData<ArrayList<UserStatus>>? = null

    fun getUserStatues(): LiveData<ArrayList<UserStatus>>? {
        return userStatuses
    }

    init {
        if (userStatuses == null)
            userStatuses = StatusRepositoryImpl.getInstance()!!.getStatuses()
    }
}