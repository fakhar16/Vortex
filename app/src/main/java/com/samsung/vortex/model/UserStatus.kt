package com.samsung.vortex.model

data class UserStatus(var name: String = "", var profileImage: String = "", var lastUpdated: Long = 0, var statuses: ArrayList<Status>? = null)
