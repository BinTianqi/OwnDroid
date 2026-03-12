package com.bintianqi.owndroid.feature.network

class PreferentialNetworkServiceInfo(
    val enabled: Boolean = true,
    val id: Int = -1,
    val allowFallback: Boolean = false,
    val blockNonMatching: Boolean = false,
    val excludedUids: List<Int> = emptyList(),
    val includedUids: List<Int> = emptyList()
)
