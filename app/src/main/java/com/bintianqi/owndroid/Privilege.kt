package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Binder
import android.os.Build
import com.bintianqi.owndroid.dpm.binderWrapperDevicePolicyManager
import com.bintianqi.owndroid.dpm.dhizukuErrorStatus
import com.rosan.dhizuku.api.Dhizuku
import kotlinx.coroutines.flow.MutableStateFlow

object Privilege {
    fun initialize(context: Context) {
        if (SP.dhizuku) {
            Dhizuku.init(context)
            val hasPermission = try {
                Dhizuku.isPermissionGranted()
            } catch(_: Exception) {
                false
            }
            if (hasPermission) {
                val dhizukuDpm = binderWrapperDevicePolicyManager(context)
                if (dhizukuDpm != null) {
                    DPM = dhizukuDpm
                    DAR = Dhizuku.getOwnerComponent()
                    updateStatus()
                    return
                }
            }
            dhizukuErrorStatus.value = 2
        }
        DPM = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        DAR = MyAdminComponent
        updateStatus()
    }
    lateinit var DPM: DevicePolicyManager
        private set
    lateinit var DAR: ComponentName
        private set

    data class Status(
        val device: Boolean = false,
        val profile: Boolean = false,
        val dhizuku: Boolean = false,
        val work: Boolean = false,
        val org: Boolean = false,
        val affiliated: Boolean = false
    ) {
        val activated = device || profile
        val primary = Binder.getCallingUid() / 100000 == 0 // Primary user
    }
    val status = MutableStateFlow(Status())
    fun updateStatus() {
        val profile = DPM.isProfileOwnerApp(DAR.packageName)
        val work = profile && Build.VERSION.SDK_INT >= 24 && DPM.isManagedProfile(DAR)
        status.value = Status(
            device = DPM.isDeviceOwnerApp(DAR.packageName),
            profile = profile,
            dhizuku = SP.dhizuku,
            work = work,
            org = work && Build.VERSION.SDK_INT >= 30 && DPM.isOrganizationOwnedDeviceWithManagedProfile,
            affiliated = Build.VERSION.SDK_INT >= 28 && DPM.isAffiliatedUser
        )
    }
}
