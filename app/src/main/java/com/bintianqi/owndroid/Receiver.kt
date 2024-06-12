package com.bintianqi.owndroid

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.*
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner

class Receiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val receiver = ComponentName(context, this::class.java)
        if(dpm.isAdminActive(receiver) || isProfileOwner(dpm) || isDeviceOwner(dpm)){
            Toast.makeText(context, context.getString(R.string.onEnabled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, R.string.onDisabled, Toast.LENGTH_SHORT).show()
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Toast.makeText(context, R.string.create_work_profile_success, Toast.LENGTH_SHORT).show()
    }

}

class PackageInstallerReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        val toastText = when(intent.getIntExtra(EXTRA_STATUS,999)){
            STATUS_PENDING_USER_ACTION->R.string.status_pending_action
            STATUS_SUCCESS->R.string.success
            STATUS_FAILURE->R.string.failed
            STATUS_FAILURE_BLOCKED->R.string.status_fail_blocked
            STATUS_FAILURE_ABORTED->R.string.status_fail_aborted
            STATUS_FAILURE_INVALID->R.string.status_fail_invalid
            STATUS_FAILURE_CONFLICT->R.string.status_fail_conflict
            STATUS_FAILURE_STORAGE->R.string.status_fail_storage
            STATUS_FAILURE_INCOMPATIBLE->R.string.status_fail_incompatible
            STATUS_FAILURE_TIMEOUT->R.string.status_fail_timeout
            else->R.string.unknown
        }
        Log.e("OwnDroid", intent.getIntExtra(EXTRA_STATUS,999).toString())
        val text = context.getString(R.string.app_installer_status) + context.getString(toastText)
        if(toastText!=999){Toast.makeText(context, text, Toast.LENGTH_SHORT).show()}
    }
}
