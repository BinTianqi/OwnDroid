package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher

var selectedPermission = ""
var applySelectedPermission = false
lateinit var createManagedProfile: ActivityResultLauncher<Intent>
lateinit var addDeviceAdmin: ActivityResultLauncher<Intent>

fun isDeviceOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isDeviceOwnerApp("com.bintianqi.owndroid")
}

fun isProfileOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isProfileOwnerApp("com.bintianqi.owndroid")
}
