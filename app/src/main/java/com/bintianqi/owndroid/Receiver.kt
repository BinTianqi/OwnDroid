package com.bintianqi.owndroid

import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner

class Receiver : DeviceAdminReceiver() {
    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        val myDpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val myComponent = ComponentName(context, this::class.java)
        if(myDpm.isAdminActive(myComponent)||isProfileOwner(myDpm)||isDeviceOwner(myDpm)){
            Toast.makeText(context, context.getString(R.string.onEnabled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, context.getString(R.string.onDisabled), Toast.LENGTH_SHORT).show()
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Toast.makeText(context, context.getString(R.string.create_work_profile_success), Toast.LENGTH_SHORT).show()
    }

}

class PackageInstallerReceiver:BroadcastReceiver(){
    override fun onReceive(context: Context, intent: Intent) {
        val toastText = when(intent.getIntExtra(EXTRA_STATUS,666)){
            STATUS_PENDING_USER_ACTION->"等待用户交互"
            STATUS_SUCCESS->context.getString(R.string.success)
            STATUS_FAILURE->context.getString(R.string.fail)
            STATUS_FAILURE_BLOCKED->"失败：被阻止"
            STATUS_FAILURE_ABORTED->"失败：被打断"
            STATUS_FAILURE_INVALID->"失败：无效"
            STATUS_FAILURE_CONFLICT->"失败：冲突"
            STATUS_FAILURE_STORAGE->"失败：空间不足"
            STATUS_FAILURE_INCOMPATIBLE->"失败：不兼容"
            STATUS_FAILURE_TIMEOUT->"失败：超时"
            else->context.getString(R.string.unknown)
        }
        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
    }
}
