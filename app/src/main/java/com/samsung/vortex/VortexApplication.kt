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
        lateinit var starMessagesDatabaseReference: DatabaseReference
        lateinit var imageUrlDatabaseReference: DatabaseReference

        lateinit var userProfilesImagesReference: StorageReference
        lateinit var imageStorageReference: StorageReference

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
        starMessagesDatabaseReference = firebaseDatabase.getReference(getString(R.string.STARRED_MESSAGES))
        imageUrlDatabaseReference = firebaseDatabase.getReference(getString(R.string.IMAGE_URL_USED_BY_USERS))
    }

    private fun initializeStorageReferences() {
        userProfilesImagesReference = FirebaseStorage.getInstance().reference.child(getString(R.string.PROFILE_IMAGES))
        imageStorageReference = FirebaseStorage.getInstance().reference.child(application.applicationContext.getString(R.string.IMAGE_FILES))
    }
}