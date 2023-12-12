package com.samsung.vortex.repository

import androidx.lifecycle.MutableLiveData
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.VortexApplication.Companion.callLogsDatabaseReference
import com.samsung.vortex.model.CallLog
import com.samsung.vortex.repository.interfaces.ICallsRepository
import com.samsung.vortex.utils.Utils.Companion.currentUser

class CallsRepositoryImpl : ICallsRepository{
    private val mCallLogs = ArrayList<CallLog>()
    var callLogsLiveData = MutableLiveData<ArrayList<CallLog>>()

    companion object {
        private var instance: CallsRepositoryImpl? = null

        fun getInstance(): CallsRepositoryImpl {
            if(instance == null)
                instance = CallsRepositoryImpl()
            return instance!!
        }
    }

    override fun getCallLogs(): MutableLiveData<ArrayList<CallLog>> {
        if (mCallLogs.size == 0) loadCallLogs()
        callLogsLiveData.value = mCallLogs
        return callLogsLiveData
    }


    private fun loadCallLogs() {
        callLogsDatabaseReference.child(currentUser!!.uid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists() && snapshot.value != null) {
                    mCallLogs.clear()
                    for (dataSnapshot in snapshot.children) {
                        val callLog = dataSnapshot.getValue(CallLog::class.java)!!
                        mCallLogs.add(callLog)
                    }
                    mCallLogs.reverse()
                    callLogsLiveData.postValue(mCallLogs)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }
}