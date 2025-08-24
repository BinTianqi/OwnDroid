package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver

class ApiReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestKey = intent.getStringExtra("key")
        var log = "OwnDroid API request received. action: ${intent.action}\nkey: $requestKey"
        val sp = SharedPrefs(context)
        if(!sp.isApiEnabled) return
        val key = sp.apiKey
        if(!key.isNullOrEmpty() && key == requestKey) {
            val dpm = context.getDPM()
            val receiver = context.getReceiver()
            val app = intent.getStringExtra("package")
            val permission = intent.getStringExtra("permission")
            val restriction = intent.getStringExtra("restriction")
            if (!app.isNullOrEmpty()) log += "\npackage: $app"
            if (!permission.isNullOrEmpty()) log += "\npermission: $permission"
            try {
                @SuppressWarnings("NewApi")
                val ok = when(intent.action?.removePrefix("com.bintianqi.owndroid.action.")) {
                    "HIDE" -> dpm.setApplicationHidden(receiver, app, true)
                    "UNHIDE" -> dpm.setApplicationHidden(receiver, app, false)
                    "SUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), true).isEmpty()
                    "UNSUSPEND" -> dpm.setPackagesSuspended(receiver, arrayOf(app), false).isEmpty()
                    "ADD_USER_RESTRICTION" -> { dpm.addUserRestriction(receiver, restriction); true }
                    "CLEAR_USER_RESTRICTION" -> { dpm.clearUserRestriction(receiver, restriction); true }
                    "SET_PERMISSION_DEFAULT" -> {
                        dpm.setPermissionGrantState(
                            receiver, app!!, permission!!,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
                        )
                    }
                    "SET_PERMISSION_GRANTED" -> {
                        dpm.setPermissionGrantState(
                            receiver, app!!, permission!!,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                        )
                    }
                    "SET_PERMISSION_DENIED" -> {
                        dpm.setPermissionGrantState(
                            receiver, app!!, permission!!,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
                        )
                    }
                    "LOCK" -> { dpm.lockNow(); true }
                    "REBOOT" -> { dpm.reboot(receiver); true }
                    else -> {
                        log += "\nInvalid action"
                        false
                    }
                }
                log += "\nsuccess: $ok"
            } catch(e: Exception) {
                e.printStackTrace()
                val message = (e::class.qualifiedName ?: "Exception") + ": " + (e.message ?: "")
                log += "\n$message"
            }
        } else {
            log += "\nUnauthorized"
        }
        Log.d(TAG, log)
    }
    companion object {
        private const val TAG = "API"
    }
}
