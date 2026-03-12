package com.bintianqi.owndroid.feature.network

import android.app.admin.WifiSsidPolicy
import android.net.wifi.WifiConfiguration
import com.bintianqi.owndroid.R

class WifiInfo(
    val id: Int,
    val ssid: String,
    val hiddenSsid: Boolean?,
    val bssid: String,
    val macRandomization: WifiMacRandomization?,
    val status: WifiStatus,
    val security: WifiSecurity?,
    val password: String,
    val ipMode: IpMode?,
    val ipConf: IpConf?,
    val proxyMode: ProxyMode?,
    val proxyConf: ProxyConf?
)

enum class AddNetworkMenu {
    None, Status, Security, MacRandomization, Ip, Proxy, HiddenSSID
}

@Suppress("InlinedApi", "DEPRECATION")
enum class WifiMacRandomization(val id: Int, val text: Int) {
    None(WifiConfiguration.RANDOMIZATION_NONE, R.string.none),
    Persistent(WifiConfiguration.RANDOMIZATION_PERSISTENT, R.string.persistent),
    NonPersistent(WifiConfiguration.RANDOMIZATION_NON_PERSISTENT, R.string.non_persistent),
    Auto(WifiConfiguration.RANDOMIZATION_AUTO, R.string.auto)
}

@Suppress("InlinedApi", "DEPRECATION")
enum class WifiSecurity(val id: Int, val text: Int) {
    Open(WifiConfiguration.SECURITY_TYPE_OPEN, R.string.wifi_security_open),
    Psk(WifiConfiguration.SECURITY_TYPE_PSK, R.string.wifi_security_psk)
}

@Suppress("DEPRECATION")
enum class WifiStatus(val id: Int, val text: Int) {
    Current(WifiConfiguration.Status.CURRENT, R.string.current),
    Enabled(WifiConfiguration.Status.ENABLED, R.string.enabled),
    Disabled(WifiConfiguration.Status.DISABLED, R.string.disabled)
}

class IpConf(val address: String, val gateway: String, val dns: List<String>)

class ProxyConf(val host: String, val port: Int, val exclude: List<String>)

enum class IpMode(val text: Int) {
    Dhcp(R.string.wifi_mode_dhcp), Static(R.string.static_str)
}

enum class ProxyMode(val text: Int) {
    None(R.string.none), Http(R.string.http)
}

class SsidPolicy(
    val type: SsidPolicyType = SsidPolicyType.None,
    val list: List<String> = emptyList()
)

@Suppress("InlinedApi")
enum class SsidPolicyType(val id: Int, val text: Int) {
    None(-1, R.string.none),
    Whitelist(WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST, R.string.whitelist),
    Blacklist(WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST, R.string.blacklist)
}
