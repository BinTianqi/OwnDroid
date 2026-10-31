package com.bintianqi.owndroid

import android.app.Application
import android.os.Build.VERSION
import com.bintianqi.owndroid.utils.DhizukuException
import com.bintianqi.owndroid.utils.NotificationUtils
import com.bintianqi.owndroid.utils.getPrivilegeStatus
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MyApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        container = AppContainer(this)
        val ph = container.privilegeHelper
        val ps = container.privilegeState
        try {
            ps.value = getPrivilegeStatus(ph.dpm, ph.dar, ph.dhizuku)
        } catch (e: DhizukuException) {
            ps.value = getPrivilegeStatus(ph.myDpm, ph.myDar, false)
            if (ps.value.activated) { // Device owner transferred from Dhizuku
                container.settingsRepo.update { it.privilege.dhizuku = false }
                ph.dhizuku = false
            } else {
                container.dhizukuErrorState.value = e.reason
            }
        }
        NotificationUtils.createChannels(this)

        // The time blocker is meant to be always-on: restart its service if enabled
        // rules exist and the user never explicitly stopped it (e.g. after the system
        // killed the process). Boot is handled separately by TimeBlockerBootReceiver.
        if (VERSION.SDK_INT >= 24) {
            try {
                val repo = container.timeBlockerRepo
                val enabledByUser = container.settingsRepo.data.timeBlockerServiceEnabled
                if (enabledByUser && repo.getEnabledRules().isNotEmpty() &&
                    !com.bintianqi.owndroid.feature.time_blocker.TimeBlockerService.isRunning
                ) {
                    com.bintianqi.owndroid.feature.time_blocker.TimeBlockerService.start(this)
                }
            } catch (e: Exception) {
                android.util.Log.e("MyApplication", "Failed to auto-start time blocker", e)
            }
        }
    }
}
