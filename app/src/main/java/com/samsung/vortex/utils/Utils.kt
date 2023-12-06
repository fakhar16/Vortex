package com.samsung.vortex.utils

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import com.google.firebase.storage.FirebaseStorage
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.model.User
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.File
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Utils {
    companion object {
        var currentUser: User? = null
        const val TAG = "Console"
        const val TYPE_MESSAGE = "type_message"
        var MESSAGE_CHANNEL_ID = "MESSAGE"
        var INCOMING_MESSAGE_NOTIFICATION_ID = 17

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

        fun getDateTimeString(time: Long): String? {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            return if (isSameDay(time)) timeFormat.format(Date(time)) else {
                dateFormat.format(Date(time))
            }
        }

        fun getDateString(time: Long): String? {
            val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            return dateFormat.format(Date(time))
        }

        fun getTimeString(time: Long): String? {
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
            return timeFormat.format(Date(time))
        }

        private fun isSameDay(date1: Long): Boolean {
            val calendar1 = Calendar.getInstance()
            calendar1.time = Date(date1)
            val calendar2 = Calendar.getInstance()
            calendar2.time = Date(Timestamp(System.currentTimeMillis()).time)
            return calendar1[Calendar.YEAR] == calendar2[Calendar.YEAR] && calendar1[Calendar.MONTH] == calendar2[Calendar.MONTH] && calendar1[Calendar.DAY_OF_MONTH] == calendar2[Calendar.DAY_OF_MONTH]
        }

        fun dipToPixels(context: Context, dipValue: Float): Float {
            val metrics = context.resources.displayMetrics
            return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, metrics)
        }

        fun hideKeyboard(activity: Activity) {
            val imm = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            var view = activity.currentFocus
            if (view == null) {
                view = View(activity)
            }
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }

        fun getFileType(uri: Uri?): String? {
            val r: ContentResolver = VortexApplication.application.applicationContext.contentResolver
            val mimeTypeMap = MimeTypeMap.getSingleton()
            return mimeTypeMap.getExtensionFromMimeType(r.getType(uri!!))
        }

        fun copyMessage(message: String?) {
            val clipboardManager = VortexApplication.application.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText(VortexApplication.application.applicationContext.getString(R.string.USER_MESSAGE_TEXT), message)
            clipboardManager.setPrimaryClip(clipData)
        }

        fun copyImage(uri: Uri, message_id: String) {
            val clipboardManager = VortexApplication.application.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newRawUri(VortexApplication.application.applicationContext.getString(R.string.USER_MESSAGE_IMAGE), uri)
            clipData.addItem(ClipData.Item(message_id))
            clipData.addItem(ClipData.Item(VortexApplication.application.applicationContext.getString(R.string.IMAGE)))
            clipboardManager.setPrimaryClip(clipData)
        }

        fun copyVideo(uri: Uri?, message_id: String?) {
            val clipboardManager = VortexApplication.application.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newRawUri(VortexApplication.application.applicationContext.getString(R.string.USER_MESSAGE_VIDEO), uri)
            clipData.addItem(ClipData.Item(message_id))
            clipData.addItem(ClipData.Item(VortexApplication.application.applicationContext.getString(R.string.VIDEO)))
            clipboardManager.setPrimaryClip(clipData)
        }
    }
}