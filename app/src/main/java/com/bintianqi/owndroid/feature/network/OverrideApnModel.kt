package com.bintianqi.owndroid.feature.network

import android.annotation.SuppressLint
import android.os.Build.VERSION
import android.telephony.TelephonyManager
import android.telephony.data.ApnSetting

enum class ApnMenu {
    None, ApnType, AuthType, Protocol, RoamingProtocol, NetworkType, MvnoType, OperatorNumeric
}

data class ApnType(val id: Int, val name: String, val requiresApi: Int = 0)

@SuppressLint("InlinedApi")
val apnTypes = listOf(
    ApnType(ApnSetting.TYPE_DEFAULT, "Default"),
    ApnType(ApnSetting.TYPE_MMS, "MMS"),
    ApnType(ApnSetting.TYPE_SUPL, "SUPL"),
    ApnType(ApnSetting.TYPE_DUN, "DUN"),
    ApnType(ApnSetting.TYPE_HIPRI, "HiPri"),
    ApnType(ApnSetting.TYPE_FOTA, "FOTA"),
    ApnType(ApnSetting.TYPE_IMS, "IMS"),
    ApnType(ApnSetting.TYPE_CBS, "CBS"),
    ApnType(ApnSetting.TYPE_IA, "IA"),
    ApnType(ApnSetting.TYPE_EMERGENCY, "Emergency"),
    ApnType(ApnSetting.TYPE_MCX, "MCX", 29),
    ApnType(ApnSetting.TYPE_XCAP, "XCAP", 30),
    ApnType(ApnSetting.TYPE_VSIM, "VSIM", 31),
    ApnType(ApnSetting.TYPE_BIP, "BIP", 31),
    ApnType(ApnSetting.TYPE_ENTERPRISE, "Enterprise", 33),
    ApnType(ApnSetting.TYPE_RCS, "RCS", 35),
    ApnType(ApnSetting.TYPE_OEM_PAID, "OEM paid"),
    ApnType(ApnSetting.TYPE_OEM_PRIVATE, "OEM private")
).filter { VERSION.SDK_INT >= it.requiresApi }

class ApnProtocol(val id: Int, val text: String, val requiresApi: Int = 28)

@Suppress("InlinedApi")
val apnProtocols = listOf(
    ApnProtocol(ApnSetting.PROTOCOL_IP, "IPv4"),
    ApnProtocol(ApnSetting.PROTOCOL_IPV6, "IPv6"),
    ApnProtocol(ApnSetting.PROTOCOL_IPV4V6, "IPv4/IPv6"),
    ApnProtocol(ApnSetting.PROTOCOL_PPP, "PPP"),
    ApnProtocol(ApnSetting.PROTOCOL_NON_IP, "Non-IP", 29),
    ApnProtocol(ApnSetting.PROTOCOL_UNSTRUCTURED, "Unstructured", 29)
)

class ApnAuthType(val id: Int, val text: String)

@Suppress("InlinedApi")
val apnAuthTypes = listOf(
    ApnAuthType(ApnSetting.AUTH_TYPE_NONE, "None"),
    ApnAuthType(ApnSetting.AUTH_TYPE_PAP, "PAP"),
    ApnAuthType(ApnSetting.AUTH_TYPE_CHAP, "CHAP"),
    ApnAuthType(ApnSetting.AUTH_TYPE_PAP_OR_CHAP, "PAP/CHAP")
)

data class ApnNetworkType(val id: Int, val text: String, val requiresApi: Int = 0)

@Suppress("InlinedApi", "DEPRECATION")
val apnNetworkTypes = listOf(
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_LTE, "LTE"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSPAP, "HSPA+"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EHRPD, "CDMA - eHRPD"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_B, "CDMA - EvDo rev. B"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_A, "CDMA - EvDo rev. A"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_0, "CDMA - EvDo rev. 0"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_1xRTT, "CDMA - 1xRTT"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_NR, "NR", 29)
).filter { VERSION.SDK_INT >= it.requiresApi }

@Suppress("InlinedApi")
enum class ApnMvnoType(val id: Int, val text: String) {
    SPN(ApnSetting.MVNO_TYPE_SPN, "SPN"),
    IMSI(ApnSetting.MVNO_TYPE_IMSI, "IMSI"),
    GID(ApnSetting.MVNO_TYPE_GID, "GID"),
    ICCID(ApnSetting.MVNO_TYPE_ICCID, "ICCID")
}

data class ApnConfig(
    val enabled: Boolean,
    val name: String,
    val apn: String,
    val proxy: String,
    val port: Int?,
    val username: String,
    val password: String,
    val apnType: Int,
    val mmsc: String,
    val mmsProxy: String,
    val mmsPort: Int?,
    val authType: Int,
    val protocol: Int,
    val roamingProtocol: Int,
    val networkType: Int,
    val profileId: Int?,
    val carrierId: Int?,
    val mtuV4: Int?,
    val mtuV6: Int?,
    val mvno: ApnMvnoType,
    val operatorNumeric: String,
    val persistent: Boolean,
    val alwaysOn: Boolean,
    val id: Int = -1
)
