package com.samsung.vortex

import android.app.Application
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class VortexApplication : Application() {
    companion object {
        lateinit var userDatabaseReference: DatabaseReference
        lateinit var presenceDatabaseReference: DatabaseReference
        lateinit var messageDatabaseReference: DatabaseReference

        lateinit var userProfilesImagesReference: StorageReference

        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this@VortexApplication
        val firebaseDatabase = FirebaseDatabase.getInstance()
        initializeDatabaseReferences(firebaseDatabase)
        initializeStorageReferences()
    }

    private fun initializeDatabaseReferences(firebaseDatabase: FirebaseDatabase) {
        userDatabaseReference = firebaseDatabase.getReference(getString(R.string.USERS))
        messageDatabaseReference = firebaseDatabase.getReference(getString(R.string.MESSAGES))
        presenceDatabaseReference = firebaseDatabase.getReference(getString(R.string.PRESENCE))
    }

    private fun initializeStorageReferences() {
        userProfilesImagesReference = FirebaseStorage.getInstance().reference.child(getString(R.string.PROFILE_IMAGES))
    }
}