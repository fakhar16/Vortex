package com.samsung.vortex.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.net.Uri
import android.os.CountDownTimer
import android.provider.OpenableColumns
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.databinding.ReplyLayoutBinding
import com.samsung.vortex.model.Message
import com.samsung.vortex.model.User
import com.squareup.picasso.Picasso
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.IOException
import java.sql.Timestamp
import java.text.CharacterIterator
import java.text.SimpleDateFormat
import java.text.StringCharacterIterator
import java.util.Calendar
import java.util.Date
import java.util.Locale

class Utils {
    companion object {
        var currentUser: User? = null
        const val TAG = "Console"
        const val TYPE_MESSAGE = "type_message"
        var MESSAGE_CHANNEL_ID = "MESSAGE"
        var INCOMING_CALL_NOTIFICATION_ID = 16
        var INCOMING_MESSAGE_NOTIFICATION_ID = 17

        //For audio recording
        private var recorder: MediaRecorder? = null
        private var mPlayer: MediaPlayer? =null
        var countDownTimer: CountDownTimer? = null
        var isRecordingPlaying = false

        //video calling
        const val TYPE_VIDEO_CALL: String = "type_video_call"
        const val ACTION_REJECT_CALL = "reject_call"
        const val TYPE_DISCONNECT_CALL_BY_USER = "type_disconnect_call_user"
        const val TYPE_DISCONNECT_CALL_BY_OTHER_USER = "type_disconnect_call_other_user"
        var INCOMING_CALL_CHANNEL_ID = "incoming_call"

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

        @SuppressLint("Range", "Recycle")
        fun getFilename(context: Context, uri: Uri): String? {
            var res: String? = null
            if (uri.scheme == "content") {
                val cursor = context.contentResolver.query(uri, null, null, null, null)
                try {
                    if (cursor != null && cursor.moveToFirst()) {
                        res = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    assert(cursor != null)
                    cursor!!.close()
                }
                if (res == null) {
                    res = uri.path
                    val cutIndex = res!!.lastIndexOf('/')
                    if (cutIndex != -1) {
                        res = res.substring(cutIndex + 1)
                    }
                }
            }
            return res
        }

        @SuppressLint("Recycle")
        fun getFileSize(fileUri: Uri): String {
            val fileDescriptor: AssetFileDescriptor = VortexApplication.application.applicationContext.contentResolver.openAssetFileDescriptor(fileUri, "r")!!
            val fileSize = fileDescriptor.length
            return humanReadableByteCountSI(fileSize)
        }

        @SuppressLint("DefaultLocale")
        private fun humanReadableByteCountSI(bytes: Long): String {
            var bytes1 = bytes
            if (-1000 < bytes1 && bytes1 < 1000) {
                return "$bytes1 B"
            }
            val ci: CharacterIterator = StringCharacterIterator("kMGTPE")
            while (bytes1 <= -999950 || bytes1 >= 999950) {
                bytes1 /= 1000
                ci.next()
            }
            return String.format("%.1f %cB", bytes1 / 1000.0, ci.current())
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

        fun copyDoc(uri: Uri?, message_id: String?, fileName: String?, fileSize: String?) {
            val clipboardManager = VortexApplication.application.applicationContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newRawUri(VortexApplication.application.applicationContext.getString(R.string.USER_MESSAGE_FILE), uri)
            clipData.addItem(ClipData.Item(message_id))
            clipData.addItem(ClipData.Item(VortexApplication.application.applicationContext.getString(R.string.PDF_FILES)))
            clipData.addItem(ClipData.Item(fileName))
            clipData.addItem(ClipData.Item(fileSize))
            clipboardManager.setPrimaryClip(clipData)
        }

        fun startRecording(file_name: String) {
            val filePath: String = VortexApplication.application.applicationContext.filesDir.path
            val file = File(filePath)
            if (!file.exists()) {
                file.mkdirs()
            }
            val fullFileName = "$file/$file_name.3gp"
            recorder = MediaRecorder()
            recorder!!.setAudioSource(MediaRecorder.AudioSource.MIC)
            recorder!!.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
            recorder!!.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
            recorder!!.setOutputFile(fullFileName)
            recorder!!.prepare()
            recorder!!.start()
        }

        fun stopRecording() {
            recorder!!.stop()
            recorder!!.release()
            recorder = null
        }

        fun playAudioRecording(filename: String?) {
            mPlayer = MediaPlayer()
            mPlayer!!.setDataSource(filename)
            mPlayer!!.prepare()
            mPlayer!!.start()
        }

        fun stopPlayingRecording() {
            mPlayer!!.release()
            mPlayer = null
        }

        fun isRecordingFileExist(file: File): Boolean {
            return file.exists()
        }

        fun getDuration(file: File): String {
            var durationStr: String
            try {
                MediaMetadataRetriever().use { mediaMetadataRetriever ->
                    mediaMetadataRetriever.setDataSource(file.absolutePath)
                    durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toString()
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return formatMilliSecond(durationStr.toLong())
        }

        fun getDurationLong(file: File): Long {
            var durationStr: String
            try {
                MediaMetadataRetriever().use { mediaMetadataRetriever ->
                    mediaMetadataRetriever.setDataSource(file.absolutePath)
                    durationStr = mediaMetadataRetriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION).toString()
                }
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
            return durationStr.toLong()
        }

        fun formatMilliSecond(milliseconds: Long): String {
            var finalTimerString = ""
            val secondsString: String
            val hours = (milliseconds / (1000 * 60 * 60)).toInt()
            val minutes = (milliseconds % (1000 * 60 * 60)).toInt() / (1000 * 60)
            val seconds = (milliseconds % (1000 * 60 * 60) % (1000 * 60) / 1000).toInt()
            if (hours > 0) finalTimerString = "$hours:"
            secondsString = if (seconds < 10) "0$seconds" else "" + seconds
            finalTimerString = "$finalTimerString$minutes:$secondsString"
            return finalTimerString
        }

        fun updateAudioDurationUI(duration: Long, durationText: TextView, playPause: ImageView, seekBar: SeekBar) {
            countDownTimer =
                object : CountDownTimer(duration, 1000) {
                    override fun onTick(l: Long) {
                        durationText.text = formatMilliSecond(l)
                        val seekBarValue = 100 - (l / (duration * 1.0) * 100.0).toInt()
                        seekBar.progress = seekBarValue
                    }

                    @SuppressLint("DefaultLocale")
                    override fun onFinish() {
                        durationText.text = formatMilliSecond(duration)
                        playPause.setImageResource(R.drawable.baseline_play_arrow_24)
                        stopPlayingRecording()
                        isRecordingPlaying = false
                        seekBar.progress = 0
                    }
                }.start()
        }

        private var density = 1f

        fun dp(value: Float, context: Context): Int {
            if (density == 1f) {
                checkDisplaySize(context)
            }
            return if (value == 0f) {
                0
            } else Math.ceil((density * value).toDouble()).toInt()
        }


        private fun checkDisplaySize(context: Context) {
            try {
                density = context.resources.displayMetrics.density
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun setReplyLayoutByMessageType(context: Context, quotedMessage: Message, binding: ReplyLayoutBinding) {
            when (quotedMessage.type) {
                VortexApplication.application.applicationContext.getString(R.string.IMAGE) -> {
                    binding.replyMessage.visibility = View.GONE
                    binding.replyImage.visibility = View.VISIBLE
                    Picasso.get()
                        .load(getImageOffline(quotedMessage.message, quotedMessage.messageId))
                        .placeholder(R.drawable.profile_image)
                        .into(binding.replyImage)

                }

                VortexApplication.application.getString(R.string.VIDEO) -> {
                    binding.replyMessage.visibility = View.GONE
                    binding.replyVideoImage.visibility = View.VISIBLE
                    binding.replyVideoPlayPreview.visibility = View.VISIBLE
                    Glide.with(context).load(quotedMessage.message).centerCrop()
                        .placeholder(R.drawable.baseline_play_circle_outline_24)
                        .into(binding.replyVideoImage)
                }

                context.getString(R.string.PDF_FILES) -> {
                    binding.replyMessage.visibility = View.GONE
                    binding.replyFileName.visibility = View.VISIBLE
                    binding.replyFileName.text = quotedMessage.fileName
                    binding.replyImage.visibility = View.VISIBLE
                    binding.replyImage.setImageResource(R.drawable.baseline_picture_as_pdf_24)
                }

                context.getString(R.string.CONTACT) -> {
                    binding.replyMessage.visibility = View.GONE
                    binding.replyContactLayout.visibility = View.VISIBLE
                    VortexApplication.contactsDatabaseReference.child(quotedMessage.message)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val list: MutableList<String?> = ArrayList()
                                if (snapshot.exists()) {
                                    for (child in snapshot.children) {
                                        list.add(child.getValue(String::class.java))
                                    }
                                    binding.replyContactName.text = list[1]
                                    Picasso.get().load(list[0]).into(binding.replyContactImage)
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }

                context.getString(R.string.AUDIO_RECORDING) -> {
                    if (quotedMessage.song) {
                        binding.replyAudioSenderImage.setImageResource(R.drawable.audio)
                    }

                    binding.replyAudioRecordingLayout.visibility = View.VISIBLE
                    binding.replyMessage.visibility = View.GONE
                }
            }
        }

        fun hideReplyLayout(binding: ReplyLayoutBinding) {
            binding.replyMessage.visibility = View.VISIBLE
            binding.replyImage.visibility = View.GONE
            binding.replyVideoImage.visibility = View.GONE
            binding.replyVideoPlayPreview.visibility = View.GONE
            binding.replyFileName.visibility = View.GONE
            binding.replyContactLayout.visibility = View.GONE
            binding.replyAudioRecordingLayout.visibility = View.GONE
            binding.mainLayout.visibility = View.GONE
        }
    }
}