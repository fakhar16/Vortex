package com.samsung.vortex.repository.interfaces

import androidx.lifecycle.MutableLiveData
import com.samsung.vortex.model.CallLog

interface ICallsRepository {
    fun getCallLogs() : MutableLiveData<ArrayList<CallLog>>
}