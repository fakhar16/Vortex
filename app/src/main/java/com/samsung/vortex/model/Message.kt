package com.samsung.vortex.model

data class Message(
    val messageId: String = "",
    val message: String = "",
    val type: String = "",
    val from: String = "",
    val to: String = "",
    val time: Long = 0,
    val feeling: Int = 0,
    val starred: String = "",
    val isUnread: Boolean = false) {
}
