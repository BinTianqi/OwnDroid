package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.util.Log
import androidx.activity.ComponentActivity

class TaskReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.getStringExtra("action")
        val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val receiver = ComponentName(context,Receiver::class.java)
        val app = intent.getStringExtra("app")
        if(action == "suspend") {
            dpm.setPackagesSuspended(receiver, arrayOf(app), true)
        } else if(action == "unsuspend") {
            dpm.setPackagesSuspended(receiver, arrayOf(app), false)
        } else {
            Log.d("OwnDroid", "unknown action")
        }
    }
}
