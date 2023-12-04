package com.samsung.vortex.fcm

import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.util.Log
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication
import com.samsung.vortex.model.Notification
import com.samsung.vortex.utils.Utils.Companion.TAG
import org.json.JSONObject

class FCMNotificationSender {
    companion object {
        private const val BASE_URL = " https://fcm.googleapis.com/fcm/send"

        fun sendNotification(context: Context, notification: Notification) {
            val policy = ThreadPolicy.Builder().permitAll().build()
            StrictMode.setThreadPolicy(policy)
            val queue: RequestQueue = Volley.newRequestQueue(context)

            val data = JSONObject()

            data.put(VortexApplication.application.applicationContext.getString(R.string.TITLE), notification.title)
            data.put(VortexApplication.application.applicationContext.getString(R.string.MESSAGE), notification.message)
            data.put(VortexApplication.application.applicationContext.getString(R.string.TYPE), notification.type)
            data.put(VortexApplication.application.applicationContext.getString(R.string.ICON), notification.icon)
            data.put(VortexApplication.application.applicationContext.getString(R.string.SENDER_ID), notification.senderId)
            data.put(VortexApplication.application.applicationContext.getString(R.string.RECEIVER_ID), notification.receiverId)

            val receiverJsonObject = JSONObject()
            receiverJsonObject.put(VortexApplication.application.applicationContext.getString(R.string.TO), notification.toToken)
            receiverJsonObject.put(VortexApplication.application.applicationContext.getString(R.string.DATA), data)

            val jsonObjectRequest: JsonObjectRequest = object : JsonObjectRequest(
                Method.POST, BASE_URL,
                receiverJsonObject,
                Response.Listener<JSONObject> { response: JSONObject ->
                    Log.i(TAG, "onResponse: FCM: $response") },
                Response.ErrorListener { }) {
                override fun getHeaders(): Map<String, String> {
                    val map: MutableMap<String, String> = HashMap()
                    map[VortexApplication.application.applicationContext.getString(R.string.CONTENT_TYPE)] = VortexApplication.application.applicationContext.getString(R.string.APPLICATION_JSON)
                    map[VortexApplication.application.applicationContext.getString(R.string.AUTHORIZATION)] = VortexApplication.application.applicationContext.getString(R.string.SERVER_KEY)
                    return map
                }
            }

            queue.add(jsonObjectRequest)
        }
    }
}