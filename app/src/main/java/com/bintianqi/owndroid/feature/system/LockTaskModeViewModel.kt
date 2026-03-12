package com.bintianqi.owndroid.feature.system

import android.app.ActivityOptions
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.LockTaskService
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.getAppInfo
import kotlinx.coroutines.flow.MutableStateFlow

class LockTaskModeViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val toastChannel: ToastChannel
) : ViewModel() {
    val packagesState = MutableStateFlow(emptyList<AppInfo>())

    @RequiresApi(26)
    fun getPackages() = ph.safeDpmCall {
        val pm = application.packageManager
        packagesState.value = dpm.getLockTaskPackages(dar).map { getAppInfo(pm, it) }
    }

    @RequiresApi(26)
    fun setPackage(name: String, status: Boolean) = ph.safeDpmCall {
        dpm.setLockTaskPackages(
            dar,
            packagesState.value.map { it.name }
                .run { if (status) plus(name) else minus(name) }
                .toTypedArray()
        )
        getPackages()
    }

    @RequiresApi(28)
    fun startLockTaskMode(
        packageName: String, activity: String, clearTask: Boolean, showNotification: Boolean
    ) = ph.safeDpmCall {
        if (!dpm.isLockTaskPermitted(packageName)) {
            val list = packagesState.value.map { it.name } + packageName
            dpm.setLockTaskPackages(dar, list.toTypedArray())
            getPackages()
        }
        if (showNotification) {
            dpm.setLockTaskFeatures(
                dar,
                dpm.getLockTaskFeatures(dar) or
                        DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS or
                        DevicePolicyManager.LOCK_TASK_FEATURE_HOME
            )
        }
        val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
        val pm = application.packageManager
        val intent = if (activity.isNotEmpty()) {
            Intent().setComponent(ComponentName(packageName, activity))
        } else pm.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or (if (clearTask) Intent.FLAG_ACTIVITY_CLEAR_TASK else 0)
            )
            application.startActivity(intent, options.toBundle())
            if (showNotification) {
                application.startForegroundService(Intent(application, LockTaskService::class.java))
            }
        } else {
            toastChannel.sendStatus(false)
        }
    }

    val featuresState = MutableStateFlow(0)

    @RequiresApi(28)
    fun getFeatures() = ph.safeDpmCall {
        featuresState.value = dpm.getLockTaskFeatures(dar)
    }

    fun setFeatures(flags: Int) {
        featuresState.value = flags
    }

    @RequiresApi(28)
    fun applyFeatures(errorCallback: (String?) -> Unit) = ph.safeDpmCall {
        try {
            dpm.setLockTaskFeatures(dar, featuresState.value)
        } catch (e: IllegalArgumentException) {
            errorCallback(e.message)
        }
        toastChannel.sendStatus(true)
    }
}