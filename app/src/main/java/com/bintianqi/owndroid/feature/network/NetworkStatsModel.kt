package com.bintianqi.owndroid.feature.network

import android.app.usage.NetworkStats
import android.net.ConnectivityManager
import com.bintianqi.owndroid.R

enum class NetworkStatsMenu {
    None, Type, Target, NetworkType, StartTime, EndTime, Uid, Tag, State
}

enum class NetworkStatsType(val text: Int) { Summary(R.string.summary), Details(R.string.details) }

@Suppress("DEPRECATION")
enum class NetworkType(val type: Int, val text: Int) {
    Mobile(ConnectivityManager.TYPE_MOBILE, R.string.mobile),
    Wifi(ConnectivityManager.TYPE_WIFI, R.string.wifi),
    Bluetooth(ConnectivityManager.TYPE_BLUETOOTH, R.string.bluetooth),
    Ethernet(ConnectivityManager.TYPE_ETHERNET, R.string.ethernet),
    Vpn(ConnectivityManager.TYPE_VPN, R.string.vpn),
}

enum class NetworkStatsTarget(val text: Int, val type: NetworkStatsType, val minApi: Int = 23) {
    Device(R.string.device, NetworkStatsType.Summary),
    User(R.string.user, NetworkStatsType.Summary),
    Uid(R.string.uid, NetworkStatsType.Details),
    UidTag(R.string.uid_tag, NetworkStatsType.Details, 24),
    UidTagState(R.string.uid_tag_state, NetworkStatsType.Details, 28)
}

@Suppress("InlinedApi")
enum class NetworkStatsState(val id: Int, val text: Int) {
    All(NetworkStats.Bucket.STATE_ALL, R.string.all),
    Default(NetworkStats.Bucket.STATE_DEFAULT, R.string.default_str),
    Foreground(NetworkStats.Bucket.STATE_FOREGROUND, R.string.foreground)
}

enum class NetworkStatsUID(val uid: Int, val text: Int) {
    All(NetworkStats.Bucket.UID_ALL, R.string.all),
    Removed(NetworkStats.Bucket.UID_REMOVED, R.string.uninstalled),
    Tethering(NetworkStats.Bucket.UID_TETHERING, R.string.tethering)
}

class QueryNetworkStatsParams(
    val type: NetworkStatsType,
    val target: NetworkStatsTarget,
    val networkType: NetworkType,
    val startTime: Long,
    val endTime: Long,
    val uid: Int,
    val tag: Int,
    val state: NetworkStatsState
)

class NetworkStatsData(
    val rxBytes: Long,
    val rxPackets: Long,
    val txBytes: Long,
    val txPackets: Long,
    val uid: Int,
    val state: Int,
    val startTime: Long,
    val endTime: Long,
    val tag: Int?,
    val roaming: Int?,
    val metered: Int?
)
