package com.bintianqi.owndroid.feature.settings

import android.graphics.Bitmap

data class AppLockUiState(
    val password: Boolean = false,
    val totp: Boolean = false,
    val biometrics: Boolean = false,
    val whenLeaving: Boolean = false
)

data class AppLockTotpUiState(
    val otpUri: String,
    val qrCode: Bitmap
)
