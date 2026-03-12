package com.bintianqi.owndroid.feature.system

class CaCertInfo(
    val hash: String,
    val serialNumber: String,
    val issuer: String,
    val subject: String,
    val issuedTime: Long,
    val expiresTime: Long,
    val bytes: ByteArray
)
