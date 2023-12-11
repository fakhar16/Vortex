package com.samsung.vortex.repository

import android.app.Activity
import android.net.Uri
import android.view.View
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.storiesDatabaseReference
import com.samsung.vortex.model.Status
import com.samsung.vortex.model.User
import com.samsung.vortex.model.UserStatus
import com.samsung.vortex.repository.interfaces.IStatusRepository
import com.samsung.vortex.utils.Utils
import java.util.Date

class StatusRepositoryImpl : IStatusRepository {
    companion object {
        private var instance: StatusRepositoryImpl? = null
        fun getInstance(): StatusRepositoryImpl? {
            if (instance == null) {
                instance = StatusRepositoryImpl()
            }
            return instance
        }
    }
    
    private val mUserStatuses: ArrayList<UserStatus> = ArrayList()
    var userStatuses: MutableLiveData<ArrayList<UserStatus>> = MutableLiveData<ArrayList<UserStatus>>()
    
    override fun getStatuses(): MutableLiveData<ArrayList<UserStatus>> {
        if (mUserStatuses.size == 0) loadStatuses()
        userStatuses.setValue(mUserStatuses)
        return userStatuses
    }

    override fun uploadStatus(data: Uri, user: User, dialog: View, activity: Activity) {
        val date = Date()
        val storage = FirebaseStorage.getInstance()
        val reference = storage.reference.child(VortexApplication.application.getString(R.string.STATUS)).child(date.time.toString() + "")
        reference.putFile(data)
            .addOnCompleteListener { task: Task<UploadTask.TaskSnapshot?> ->
                if (task.isSuccessful) {
                    reference.downloadUrl
                        .addOnSuccessListener { uri: Uri ->
                            val userStatus = UserStatus()
                            userStatus.name = user.name
                            userStatus.profileImage = user.image
                            userStatus.lastUpdated = date.time
                            val obj = HashMap<String, Any>()
                            obj[VortexApplication.application.getString(R.string.NAME)] = userStatus.name
                            obj[VortexApplication.application.getString(R.string.PROFILE_IMAGE)] = userStatus.profileImage
                            obj[VortexApplication.application.getString(R.string.LAST_UPDATED)] = userStatus.lastUpdated
                            val imageUrl = uri.toString()
                            val status = Status(imageUrl, userStatus.lastUpdated)
                            storiesDatabaseReference
                                .child(FirebaseAuth.getInstance().uid!!)
                                .updateChildren(obj)
                            storiesDatabaseReference
                                .child(FirebaseAuth.getInstance().uid!!)
                                .child(VortexApplication.application.getString(R.string.STATUSES))
                                .push()
                                .setValue(status)
                            Utils.dismissLoadingBar(activity, dialog)
                        }
                }
            }
    }

    private fun loadStatuses() {
        storiesDatabaseReference
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        mUserStatuses.clear()
                        for (dataSnapshot in snapshot.children) {
                            val status = UserStatus()
                            status.name =
                                dataSnapshot.child(VortexApplication.application.getString(R.string.NAME)).getValue(String::class.java)!!
                            status.profileImage =
                                dataSnapshot.child(VortexApplication.application.getString(R.string.PROFILE_IMAGE)).getValue(String::class.java)!!
                            status.lastUpdated = dataSnapshot.child(VortexApplication.application.getString(R.string.LAST_UPDATED)).getValue(Long::class.java)!!
                            val statuses: ArrayList<Status> = ArrayList()
                            for (statusSnapShot in dataSnapshot.child(VortexApplication.application.getString(R.string.STATUSES)).children) {
                                val sampleStatus: Status? =
                                    statusSnapShot.getValue(Status::class.java)
                                statuses.add(sampleStatus!!)
                            }
                            status.statuses = statuses
                            mUserStatuses.add(status)
                        }
                        userStatuses.postValue(mUserStatuses)
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}
