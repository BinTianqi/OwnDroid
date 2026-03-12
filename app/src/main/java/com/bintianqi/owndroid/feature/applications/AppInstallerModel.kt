package com.bintianqi.owndroid.feature.applications

import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import kotlinx.serialization.Serializable

@Serializable
data class SessionParamsOptions(
    val mode: Int = PackageInstaller.SessionParams.MODE_FULL_INSTALL,
    val keepOriginalEnabledSetting: Boolean = false,
    val noKill: Boolean = false,
    val location: Int = PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY,
)
