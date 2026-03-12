package com.bintianqi.owndroid.feature.privilege

data class DhizukuClientInfo(
    val uid: Int,
    val signature: String?,
    val permissions: List<String> = emptyList()
)
