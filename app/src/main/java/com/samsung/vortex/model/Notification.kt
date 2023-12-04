package com.samsung.vortex.model

data class Notification(
    val title: String = "",
    val message: String = "",
    val type: String = "",
    val icon: String = "",
    val toToken: String = "",
    val senderId: String = "",
    val receiverId: String = "")
