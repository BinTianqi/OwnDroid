package com.bintianqi.owndroid.feature.provisioning

class ProvisioningParams(
    val imei: String?, val serial: String?, val modes: List<Int>
)

class ProvisioningOptions(
    val skipEncryption: Boolean
)
