package com.samsung.vortex.webrtc

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.VortexApplication.Companion.callLogsDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.userDatabaseReference
import com.samsung.vortex.VortexApplication.Companion.videoUserDatabaseReference
import com.samsung.vortex.databinding.ActivityCallBinding
import com.samsung.vortex.model.CallLog
import com.samsung.vortex.model.User
import com.samsung.vortex.utils.FirebaseUtils
import com.samsung.vortex.utils.Utils
import com.samsung.vortex.utils.Utils.Companion.ACTION_REJECT_CALL
import com.samsung.vortex.utils.Utils.Companion.INCOMING_CALL_NOTIFICATION_ID
import com.samsung.vortex.utils.Utils.Companion.TAG
import com.samsung.vortex.utils.Utils.Companion.TYPE_DISCONNECT_CALL_BY_USER
import com.samsung.vortex.webrtc.models.JavaScriptInterface
import com.squareup.picasso.Picasso
import java.util.Date
import java.util.UUID

class CallActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCallBinding
    private var sender: String = ""
    var receiver: String = ""
    private var uniqueId = ""

    //    Boolean isPeerConnected = false;
    private var isAudio = true
    private var isVideo = true
    private lateinit var endCallReceiver: BroadcastReceiver
    private lateinit var rejectFilter: IntentFilter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NotificationManagerCompat.from(applicationContext).cancel(INCOMING_CALL_NOTIFICATION_ID)

        rejectFilter = IntentFilter()
        rejectFilter.addAction(ACTION_REJECT_CALL)
        endCallReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == ACTION_REJECT_CALL) {
                    finish()
                }
            }
        }

        registerReceiver(endCallReceiver, rejectFilter, RECEIVER_NOT_EXPORTED)

        sender = intent.getStringExtra(VortexApplication.application.getString(R.string.CALLER))!!
        binding.endCall.setOnClickListener {
            disconnectCall()
            sendMissedCallLog()
        }
        binding.endOngoingCall.setOnClickListener {
            disconnectCall()
            sendIncomingCallLog()
        }
        binding.toggleAudioBtn.setOnClickListener {
            isAudio = !isAudio
            callJavaScriptFunction("javascript:toggleAudio('$isAudio')")
            if (isAudio) {
                binding.toggleAudioBtn.setImageResource(R.drawable.btn_unmute_normal)
            } else {
                binding.toggleAudioBtn.setImageResource(R.drawable.btn_mute_normal)
            }
        }
        binding.toggleVideoBtn.setOnClickListener {
            isVideo = !isVideo
            callJavaScriptFunction("javascript:toggleVideo('$isVideo')")
            if (isVideo) {
                binding.toggleVideoBtn.setImageResource(R.drawable.btn_video_normal)
            } else {
                binding.toggleVideoBtn.setImageResource(R.drawable.btn_video_muted)
            }
        }

        setupWebView()
        val isCallMade = intent.getBooleanExtra(VortexApplication.application.getString(R.string.IS_CALL_MADE), false)
        if (isCallMade) {
            receiver = intent.getStringExtra(VortexApplication.application.getString(R.string.RECEIVER))!!
            sendCallRequest()
            userDatabaseReference
                .child(receiver)
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists() && snapshot.value != null) {
                            val user: User? = snapshot.getValue(User::class.java)
                            binding.callerName.text = user!!.name
                            Picasso.get().load(Utils.getImageOffline(user.image, user.uid)).placeholder(R.drawable.profile_image).into(binding.callerImage)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
        }
    }

    private fun sendMissedCallLog() {
        val missedCallLog = CallLog(sender, receiver, "", getString(R.string.MISSED_CALL), Date().time)
        callLogsDatabaseReference.child(receiver).push().setValue(missedCallLog)
    }

    private fun sendIncomingCallLog() {
        val missedCallLog = CallLog(sender, receiver, "", getString(R.string.INCOMING), Date().time)
        callLogsDatabaseReference.child(receiver).push().setValue(missedCallLog)
    }

    private fun sendCallRequest() {
        videoUserDatabaseReference.child(receiver).child(VortexApplication.application.getString(R.string.INCOMING)).setValue(sender)
        sendOutGoingCallLog()
        videoUserDatabaseReference.child(receiver).child(getString(R.string.IS_AVAILABLE))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        if (snapshot.value.toString() == "true") {
                            listenForConnId()
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun sendOutGoingCallLog() {
        val outGoingCallLog = CallLog(sender, receiver, "", getString(R.string.OUTGOING), Date().time)
        callLogsDatabaseReference.child(sender).push().setValue(outGoingCallLog)
    }

    private fun listenForConnId() {
        videoUserDatabaseReference.child(receiver).child(getString(R.string.CONN_ID))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.value == null) return
                    binding.callLayout.visibility = View.GONE
                    binding.callControlLayout.visibility = View.VISIBLE
                    callJavaScriptFunction("javascript:startCall('" + snapshot.value + "')")
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    @SuppressLint("SetJavaScriptEnabled")
    fun setupWebView() {
        binding.webView.webChromeClient = object : WebChromeClient() {
            override fun onPermissionRequest(request: PermissionRequest) {
                request.grant(request.resources)
            }
        }
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.mediaPlaybackRequiresUserGesture = false
        binding.webView.addJavascriptInterface(JavaScriptInterface(), "Android")
        loadVideoCall()
    }

    private fun loadVideoCall() {
        val filePath = "file:android_asset/call.html"
        binding.webView.loadUrl(filePath)
        binding.webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                super.onPageFinished(view, url)
                initializePeer()
            }
        }
    }

    private fun initializePeer() {
        uniqueId = getUniqueId()
        callJavaScriptFunction("javascript:init('$uniqueId')")
        videoUserDatabaseReference.child(sender).child(VortexApplication.application.getString(R.string.INCOMING))
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.value != null) onCallRequest(snapshot.value.toString())
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

    private fun onCallRequest(caller: String?) {
        Log.i(TAG, "onCallRequest: ")
        if (caller == null) return
        val isCallAccepted = intent.getBooleanExtra(VortexApplication.application.getString(R.string.CALL_ACCEPTED), false)
        if (isCallAccepted) {
            binding.callLayout.visibility = View.GONE
            videoUserDatabaseReference.child(sender).child(VortexApplication.application.getString(R.string.CONN_ID)).setValue(uniqueId)
            videoUserDatabaseReference.child(sender).child(VortexApplication.application.getString(R.string.IS_AVAILABLE)).setValue(true)
            binding.callControlLayout.visibility = View.VISIBLE
        }
    }

    private fun getUniqueId(): String {
        return UUID.randomUUID().toString()
    }

    fun callJavaScriptFunction(function: String) {
        binding.webView.post { binding.webView.loadUrl(function) }
    }

    private fun disconnectCall() {
        FirebaseUtils.sendNotification("", receiver, sender, TYPE_DISCONNECT_CALL_BY_USER)
        videoUserDatabaseReference.child(receiver).setValue(null)
        callJavaScriptFunction("javascript:disconnectCall()")
        finish()
    }

    override fun onDestroy() {
        videoUserDatabaseReference.child(receiver).setValue(null)
        callJavaScriptFunction("javascript:disconnectCall()")
        super.onDestroy()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(endCallReceiver)
    }
}