package com.samsung.vortex.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.samsung.vortex.model.CallLog
import com.samsung.vortex.repository.CallsRepositoryImpl

class CallViewModel : ViewModel() {
    private var callLogs: MutableLiveData<ArrayList<CallLog>>? = null

    fun getCallLogs(): LiveData<ArrayList<CallLog>> {
        return callLogs!!
    }

    init {
        if (callLogs == null)
            callLogs = CallsRepositoryImpl.getInstance().getCallLogs()
    }
}