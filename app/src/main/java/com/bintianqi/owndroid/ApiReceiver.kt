package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class ApiReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestKey = intent.getStringExtra("key")
        var log = "OwnDroid API request received. action: ${intent.action}"
        val key = SP.apiKeyHash
        if(!key.isNullOrEmpty() && key == requestKey?.hash()) {
            val app = intent.getStringExtra("package")
            val permission = intent.getStringExtra("permission")
            val restriction = intent.getStringExtra("restriction")
            if (!app.isNullOrEmpty()) log += "\npackage: $app"
            if (!permission.isNullOrEmpty()) log += "\npermission: $permission"
            try {
                @SuppressWarnings("NewApi")
                when(intent.action?.removePrefix("com.bintianqi.owndroid.action.")) {
                    "HIDE" -> Privilege.DPM.setApplicationHidden(Privilege.DAR, app, true)
                    "UNHIDE" -> Privilege.DPM.setApplicationHidden(Privilege.DAR, app, false)
                    "SUSPEND" -> Privilege.DPM.setPackagesSuspended(Privilege.DAR, arrayOf(app), true)
                    "UNSUSPEND" -> Privilege.DPM.setPackagesSuspended(Privilege.DAR, arrayOf(app), false)
                    "ADD_USER_RESTRICTION" -> { Privilege.DPM.addUserRestriction(Privilege.DAR, restriction) }
                    "CLEAR_USER_RESTRICTION" -> { Privilege.DPM.clearUserRestriction(Privilege.DAR, restriction) }
                    "SET_PERMISSION_DEFAULT" -> {
                        Privilege.DPM.setPermissionGrantState(
                            Privilege.DAR, app!!, permission!!,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
                        )
                    }
                    "SET_PERMISSION_GRANTED" -> {
                        Privilege.DPM.setPermissionGrantState(
                            Privilege.DAR, app!!, permission!!,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                        )
                    }
                    "SET_PERMISSION_DENIED" -> {
                        Privilege.DPM.setPermissionGrantState(
                            Privilege.DAR, app!!, permission!!,
                            DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
                        )
                    }
                    "LOCK" -> { Privilege.DPM.lockNow() }
                    "REBOOT" -> { Privilege.DPM.reboot(Privilege.DAR) }
                    "SET_CAMERA_DISABLED" -> {
                        Privilege.DPM.setCameraDisabled(Privilege.DAR, true)
                    }
                    "SET_CAMERA_ENABLED" -> {
                        Privilege.DPM.setCameraDisabled(Privilege.DAR, false)
                    }
                    "SET_USB_DISABLED" -> {
                        Privilege.DPM.isUsbDataSignalingEnabled = false
                    }
                    "SET_USB_ENABLED" -> {
                        Privilege.DPM.isUsbDataSignalingEnabled = true
                    }
                    "SET_SCREEN_CAPTURE_DISABLED" -> {
                        Privilege.DPM.setScreenCaptureDisabled(Privilege.DAR, true)
                    }
                    "SET_SCREEN_CAPTURE_ENABLED" -> {
                        Privilege.DPM.setScreenCaptureDisabled(Privilege.DAR, false)
                    }
                    else -> {
                        log += "\nInvalid action"
                    }
                }
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
