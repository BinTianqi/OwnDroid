package com.bintianqi.owndroid

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.bintianqi.owndroid.feature.users.UserOperationType
import com.bintianqi.owndroid.utils.doUserOperationWithContext
import com.bintianqi.owndroid.utils.MyShortcut
import com.bintianqi.owndroid.utils.ShortcutUtils
import com.bintianqi.owndroid.utils.showOperationResultToast

class ShortcutsReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myApp = application as MyApplication
        val context = this
        val sr = myApp.container.settingsRepo
        val ph = myApp.container.privilegeHelper
        val settings = sr.data.shortcut
        try {
            val action = intent.action?.removePrefix("com.bintianqi.owndroid.action.")
            val requestKey = intent?.getStringExtra("key")
            if (action != null && settings.enabled && requestKey == settings.key) {
                ph.safeDpmCall {
                    when (action) {
                        "LOCK" -> dpm.lockNow()
                        "DISABLE_CAMERA" -> {
                            val state = dpm.getCameraDisabled(dar)
                            dpm.setCameraDisabled(dar, !state)
                            ShortcutUtils.setShortcut(context, sr, MyShortcut.DisableCamera, state)
                        }

                        "MUTE" -> {
                            val state = dpm.isMasterVolumeMuted(dar)
                            dpm.setMasterVolumeMuted(dar, !state)
                            ShortcutUtils.setShortcut(context, sr, MyShortcut.Mute, state)
                        }

                        "USER_RESTRICTION" -> {
                            val state = intent?.getBooleanExtra("state", false)
                            val id = intent?.getStringExtra("restriction")
                            if (state == null || id == null) return@safeDpmCall
                            if (state) {
                                dpm.addUserRestriction(dar, id)
                            } else {
                                dpm.clearUserRestriction(dar, id)
                            }
                            ShortcutUtils.updateUserRestrictionShortcut(
                                context, sr, id, !state, false
                            )
                        }

                        "USER_OPERATION" -> {
                            val typeName = intent.getStringExtra("operation") ?: return@safeDpmCall
                            val type = UserOperationType.valueOf(typeName)
                            val serial = intent.getIntExtra("serial", -1)
                            if (serial == -1) return@safeDpmCall
                            doUserOperationWithContext(context, ph.dpm, ph.dar, type, serial, false)
                        }
                    }
                }
                Log.d(TAG, "Received intent: $action")
                showOperationResultToast(true)
            } else {
                showOperationResultToast(false)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            finish()
        }
    }

    companion object {
        private const val TAG = "ShortcutsReceiver"
    }
}
