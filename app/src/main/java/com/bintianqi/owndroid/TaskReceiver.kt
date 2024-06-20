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
        Log.d("OwnDroid", ("TaskReceiver: pkgName: " + intent.component?.packageName))
        Log.d("OwnDroid", ("TaskReceiver: pkg: " + intent.`package`))
        val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        if(sharedPref.getString("AutomationApp", "") != intent.component?.packageName) return
        val category = intent.getStringExtra("category")
        if(category == "app") {
            val action = intent.getStringExtra("action")
            if(action == "suspend") {
                val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
                val receiver = ComponentName(context,Receiver::class.java)
                val app = intent.getStringExtra("app")
                val mode = intent.getBooleanExtra("mode", false)
                if(VERSION.SDK_INT >= 24) {
                    dpm.setPackagesSuspended(receiver, arrayOf(app), mode)
                } else {
                    Log.d("OwnDroid", "unsupported")
                }
            } else {
                Log.d("OwnDroid", "unknown action")
            }
        } else {
            Log.d("OwnDroid", "unknown category")
        }
    }
}
