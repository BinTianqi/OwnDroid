package com.binbin.androidowner.dpm

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher


lateinit var getCaCert: ActivityResultLauncher<Intent>
lateinit var createManagedProfile: ActivityResultLauncher<Intent>
lateinit var getApk: ActivityResultLauncher<Intent>
lateinit var getUserIcon: ActivityResultLauncher<Intent>

var userIconUri: Uri? = null
var apkUri: Uri? = null
var caCert = byteArrayOf()

fun isDeviceOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isDeviceOwnerApp("com.binbin.androidowner")
}

fun isProfileOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isProfileOwnerApp("com.binbin.androidowner")
}

