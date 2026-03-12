package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.bintianqi.owndroid.utils.hash

class ApiReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val requestKey = intent.getStringExtra("key")
        var log = "OwnDroid API request received. action: ${intent.action}"
        val myApp = context.applicationContext as MyApplication
        val key = myApp.container.settingsRepo.data.apiKeyHash
        if (key.isNotEmpty() && key == requestKey?.hash()) {
            val app = intent.getStringExtra("package")
            val permission = intent.getStringExtra("permission")
            val restriction = intent.getStringExtra("restriction")
            if (!app.isNullOrEmpty()) log += "\npackage: $app"
            if (!permission.isNullOrEmpty()) log += "\npermission: $permission"
            try {
                myApp.container.privilegeHelper.safeDpmCall {
                    @SuppressWarnings("NewApi")
                    when (intent.action?.removePrefix("com.bintianqi.owndroid.action.")) {
                        "HIDE" -> dpm.setApplicationHidden(dar, app, true)
                        "UNHIDE" -> dpm.setApplicationHidden(dar, app, false)
                        "SUSPEND" -> dpm.setPackagesSuspended(dar, arrayOf(app), true)
                        "UNSUSPEND" -> dpm.setPackagesSuspended(dar, arrayOf(app), false)
                        "ADD_USER_RESTRICTION" -> {
                            dpm.addUserRestriction(dar, restriction)
                        }

                        "CLEAR_USER_RESTRICTION" -> {
                            dpm.clearUserRestriction(dar, restriction)
                        }

                        "SET_PERMISSION_DEFAULT" -> {
                            dpm.setPermissionGrantState(
                                dar, app!!, permission!!,
                                DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
                            )
                        }

                        "SET_PERMISSION_GRANTED" -> {
                            dpm.setPermissionGrantState(
                                dar, app!!, permission!!,
                                DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
                            )
                        }

                        "SET_PERMISSION_DENIED" -> {
                            dpm.setPermissionGrantState(
                                dar, app!!, permission!!,
                                DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
                            )
                        }

                        "LOCK" -> {
                            dpm.lockNow()
                        }

                        "REBOOT" -> {
                            dpm.reboot(dar)
                        }

                        "SET_CAMERA_DISABLED" -> {
                            dpm.setCameraDisabled(dar, true)
                        }

                        "SET_CAMERA_ENABLED" -> {
                            dpm.setCameraDisabled(dar, false)
                        }

                        "SET_USB_DISABLED" -> {
                            dpm.isUsbDataSignalingEnabled = false
                        }

                        "SET_USB_ENABLED" -> {
                            dpm.isUsbDataSignalingEnabled = true
                        }

                        "SET_SCREEN_CAPTURE_DISABLED" -> {
                            dpm.setScreenCaptureDisabled(dar, true)
                        }

                        "SET_SCREEN_CAPTURE_ENABLED" -> {
                            dpm.setScreenCaptureDisabled(dar, false)
                        }

                        else -> {
                            log += "\nInvalid action"
                        }
                    }
                }
            } catch (e: Exception) {
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
