package com.bintianqi.owndroid.feature.system

data class FrpPolicyInfo(
    val supported: Boolean = false,
    val usePolicy: Boolean = false,
    val enabled: Boolean = false,
    val accounts: List<String> = emptyList()
)

class DeviceInfo(
    val financed: Boolean = false,
    val dpmrh: String? = null,
    val storageEncryptionStatus: Int = 0,
    val deviceIdAttestationSupported: Boolean = false,
    val uniqueDeviceAttestationSupported: Boolean = false,
    val activeAdmins: List<String> = emptyList()
)
