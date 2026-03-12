package com.bintianqi.owndroid.feature.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class NetworkLog(
    val id: Long?,
    @SerialName("package") val packageName: String,
    val time: Long,
    val type: String,
    val host: String?,
    val count: Int?,
    val addresses: List<String>?,
    val address: String?,
    val port: Int?
)
