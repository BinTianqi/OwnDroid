package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.admin.DeviceAdminReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.EXTRA_STATUS
import android.content.pm.PackageInstaller.STATUS_FAILURE
import android.content.pm.PackageInstaller.STATUS_FAILURE_ABORTED
import android.content.pm.PackageInstaller.STATUS_FAILURE_BLOCKED
import android.content.pm.PackageInstaller.STATUS_FAILURE_CONFLICT
import android.content.pm.PackageInstaller.STATUS_FAILURE_INCOMPATIBLE
import android.content.pm.PackageInstaller.STATUS_FAILURE_INVALID
import android.content.pm.PackageInstaller.STATUS_FAILURE_STORAGE
import android.content.pm.PackageInstaller.STATUS_FAILURE_TIMEOUT
import android.content.pm.PackageInstaller.STATUS_PENDING_USER_ACTION
import android.content.pm.PackageInstaller.STATUS_SUCCESS
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver
import com.bintianqi.owndroid.dpm.isDeviceAdmin
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner
import com.bintianqi.owndroid.dpm.toggleInstallAppActivity
import kotlinx.coroutines.flow.MutableStateFlow

class Receiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        context.toggleInstallAppActivity()
        if(context.isDeviceAdmin || context.isProfileOwner || context.isDeviceOwner){
            Toast.makeText(context, context.getString(R.string.onEnabled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        context.toggleInstallAppActivity()
        Toast.makeText(context, R.string.onDisabled, Toast.LENGTH_SHORT).show()
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Toast.makeText(context, R.string.create_work_profile_success, Toast.LENGTH_SHORT).show()
    }

}

val installAppDone = MutableStateFlow(false)

class PackageInstallerReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        val toastText = when(intent.getIntExtra(EXTRA_STATUS, 999)){
            STATUS_PENDING_USER_ACTION -> R.string.status_pending_action
            STATUS_SUCCESS -> R.string.success
            STATUS_FAILURE -> R.string.failed
            STATUS_FAILURE_BLOCKED -> R.string.status_fail_blocked
            STATUS_FAILURE_ABORTED -> R.string.status_fail_aborted
            STATUS_FAILURE_INVALID -> R.string.status_fail_invalid
            STATUS_FAILURE_CONFLICT -> R.string.status_fail_conflict
            STATUS_FAILURE_STORAGE -> R.string.status_fail_storage
            STATUS_FAILURE_INCOMPATIBLE -> R.string.status_fail_incompatible
            STATUS_FAILURE_TIMEOUT -> R.string.status_fail_timeout
            else -> 999
        }
        Log.e("OwnDroid", intent.getIntExtra(EXTRA_STATUS, 999).toString())
        installAppDone.value = true
        if(toastText != 999){
            val text = context.getString(R.string.app_installer_status) + context.getString(toastText)
            Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
        }
    }
}

class StopLockTaskModeReceiver: BroadcastReceiver() {
    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        val dpm = context.getDPM()
        val receiver = context.getReceiver()
        val packages = dpm.getLockTaskPackages(receiver)
        dpm.setLockTaskPackages(receiver, arrayOf())
        dpm.setLockTaskPackages(receiver, packages)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(1)
    }
}
