package com.samsung.vortex.model

import java.io.Serializable

data class CallLog(
    val id: String = "",
    val from: String = "",
    val to: String = "",
    val displayName: String = "",
    val type: String = "",
    val time: Long = 0) : Serializable
