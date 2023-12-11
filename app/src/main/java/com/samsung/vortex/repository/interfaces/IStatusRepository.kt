package com.samsung.vortex.repository.interfaces

import android.app.Activity
import android.net.Uri
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.samsung.vortex.model.User
import com.samsung.vortex.model.UserStatus

interface IStatusRepository {
    fun getStatuses(): MutableLiveData<ArrayList<UserStatus>>
    fun uploadStatus(data: Uri, user: User, dialog: View, activity: Activity)
}