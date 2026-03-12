package com.bintianqi.owndroid.feature.network

import android.app.admin.DevicePolicyManager
import com.bintianqi.owndroid.R

@Suppress("InlinedApi")
enum class PrivateDnsMode(val id: Int, val text: Int) {
    Opportunistic(DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC, R.string.automatic),
    Host(DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME, R.string.enabled)
}

enum class ProxyType(val text: Int) {
    Off(R.string.proxy_type_off), Pac(R.string.proxy_type_pac), Direct(R.string.proxy_type_direct)
}

data class RecommendedProxyConf(
    val type: ProxyType, val url: String, val host: String, val specifyPort: Boolean,
    val port: Int, val exclude: List<String>
)
