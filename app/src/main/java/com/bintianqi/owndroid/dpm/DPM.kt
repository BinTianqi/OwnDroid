package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Build.VERSION
import androidx.activity.result.ActivityResultLauncher
import kotlinx.coroutines.flow.MutableStateFlow

var selectedPermission = MutableStateFlow("")
lateinit var createManagedProfile: ActivityResultLauncher<Intent>
lateinit var addDeviceAdmin: ActivityResultLauncher<Intent>

fun isDeviceOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isDeviceOwnerApp("com.bintianqi.owndroid")
}

fun isProfileOwner(dpm: DevicePolicyManager): Boolean {
    return dpm.isProfileOwnerApp("com.bintianqi.owndroid")
}

fun DevicePolicyManager.isOrgProfile(receiver: ComponentName):Boolean {
    return VERSION.SDK_INT >= 30 && isProfileOwner(this) && isManagedProfile(receiver) && isOrganizationOwnedDeviceWithManagedProfile
}
