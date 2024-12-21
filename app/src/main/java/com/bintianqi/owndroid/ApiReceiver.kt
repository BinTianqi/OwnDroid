package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver

class ApiReceiver: BroadcastReceiver() {
    @SuppressLint("NewApi")
    override fun onReceive(context: Context, intent: Intent) {
        val sharedPrefs = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        if(sharedPrefs.getBoolean("enable_api", false)) return
        val key = sharedPrefs.getString("api_key", null)
        if(key != null && key == intent.getStringExtra("key")) {
            val dpm = context.getDPM()
            val receiver = context.getReceiver()
            val app = intent.getStringExtra("package") ?: ""
            try {
                val ok = when(intent.action) {
                    "com.bintianqi.owndroid.action.HIDE" -> dpm.setApplicationHidden(receiver, app, true)
                    "com.bintianqi.owndroid.action.UNHIDE" -> dpm.setApplicationHidden(receiver, app, false)
                    "com.bintianqi.owndroid.action.SUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), true).isEmpty()
                    "com.bintianqi.owndroid.action.UNSUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), false).isEmpty()
                    "com.bintianqi.owndroid.action.LOCK" -> { dpm.lockNow(); true }
                    else -> {
                        Log.w(TAG, "Invalid action")
                        resultData = "Invalid action"
                        false
                    }
                }
                if(!ok) resultCode = 1
            } catch(e: Exception) {
                e.printStackTrace()
                val message = (e::class.qualifiedName ?: "Exception") + ": " + (e.message ?: "")
                Log.w(TAG, message)
                resultCode = 1
                resultData = message
            }
        } else {
            Log.w(TAG, "Unauthorized")
            resultCode = 1
            resultData = "Unauthorized"
        }
    }
    companion object {
        private const val TAG = "API"
    }
}
