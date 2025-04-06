package com.bintianqi.owndroid

import android.content.Context
import android.os.Binder
import android.os.Build
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner
import kotlinx.coroutines.flow.MutableStateFlow

class Privilege(
    val device: Boolean = false, // Device owner
    val profile: Boolean = false, // Profile owner
    val dhizuku: Boolean = false,
    val work: Boolean = false, // Work profile
    val org: Boolean = false, // Organization-owned work profile
    val affiliated: Boolean = false
) {
    val primary = Binder.getCallingUid() / 100000 == 0 // Primary user
}

val myPrivilege = MutableStateFlow(Privilege())

fun updatePrivilege(context: Context) {
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val profile = context.isProfileOwner
    val work = profile && Build.VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)
    myPrivilege.value = Privilege(
        device = context.isDeviceOwner,
        profile = profile,
        dhizuku = SharedPrefs(context).dhizuku,
        work = work,
        org = work && Build.VERSION.SDK_INT >= 30 && dpm.isOrganizationOwnedDeviceWithManagedProfile,
        affiliated = Build.VERSION.SDK_INT >= 28 && dpm.isAffiliatedUser
    )
}

