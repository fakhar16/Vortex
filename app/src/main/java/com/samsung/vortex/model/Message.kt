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
    var isUnread: Boolean = true) {
    override fun toString(): String {
        return "Message(messageId='$messageId', message='$message', type='$type', from='$from', to='$to', time=$time, feeling=$feeling, starred='$starred', isUnread=$isUnread)"
    }
}
