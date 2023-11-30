package com.samsung.vortex.utils

import android.app.Activity
import android.util.Log
import android.view.View
import android.view.WindowManager
import com.google.firebase.storage.FirebaseStorage
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.model.User
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.coroutines.suspendCoroutine

class Utils {
    companion object {
        var currentUser: User? = null
        const val TAG = "Console"

        fun showLoadingBar(activity: Activity, view: View) {
            view.visibility = View.VISIBLE
            activity.window.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        }

        fun dismissLoadingBar(activity: Activity, view: View) {
            view.visibility = View.GONE
            activity.window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }

        fun getImageOffline(image: String, imageId: String) : File {
            val filePath: String = VortexApplication.application.applicationContext.filesDir.path + "/" + imageId + ".jpg"
            val file = File(filePath)

            if (!file.exists()) {
                runBlocking {
                    FirebaseStorage.getInstance().getReferenceFromUrl(image).getFile(file).await()
                }
            }
            return file
        }
    }
}