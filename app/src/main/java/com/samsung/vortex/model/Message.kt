package com.samsung.vortex.model

import com.samsung.vortex.R
import com.samsung.vortex.VortexApplication

data class Message(
    val messageId: String = "",
    val message: String = "",
    val type: String = "",
    val from: String = "",
    val to: String = "",
    val time: Long = 0,
    var feeling: Int = 0,
    var starred: String = "",
    var unread: Boolean = true,
    var caption: String = "",
    var fileName: String = "",
    var fileSize: String = "",
    var song: Boolean = false,
    var status: String = VortexApplication.application.getString(R.string.SENT),
    var quotedMessageId: String = "")
