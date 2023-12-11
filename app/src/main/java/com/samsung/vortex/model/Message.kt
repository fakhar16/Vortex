package com.samsung.vortex.model

data class Message(
    val messageId: String = "",
    val message: String = "",
    val type: String = "",
    val from: String = "",
    val to: String = "",
    val time: Long = 0,
    var feeling: Int = 0,
    var starred: String = "",
    var isUnread: Boolean = true,
    var caption: String = "",
    var fileName: String = "",
    var fileSize: String = "",
    var isSong: Boolean = false) {
    override fun toString(): String {
        return "Message(message='$message', type='$type')"
    }
}
