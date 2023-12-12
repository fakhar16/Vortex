package com.samsung.vortex.model

data class CallLog(
    val from: String = "",
    val to: String = "",
    val displayName: String = "",
    val type: String = "",
    val time: Long = 0)
