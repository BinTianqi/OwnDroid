package com.bintianqi.owndroid

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.bintianqi.owndroid.dpm.UserOperationType
import com.bintianqi.owndroid.dpm.doUserOperationWithContext

class ShortcutsReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val action = intent.action?.removePrefix("com.bintianqi.owndroid.action.")
            val key = SP.shortcutKey
            val requestKey = intent?.getStringExtra("key")
            if (action != null && key != null && requestKey == key) {
                when (action) {
                    "LOCK" -> Privilege.DPM.lockNow()
                    "DISABLE_CAMERA" -> {
                        val state = Privilege.DPM.getCameraDisabled(Privilege.DAR)
                        Privilege.DPM.setCameraDisabled(Privilege.DAR, !state)
                        ShortcutUtils.setShortcut(this, MyShortcut.DisableCamera, state)
                    }
                    "MUTE" -> {
                        val state = Privilege.DPM.isMasterVolumeMuted(Privilege.DAR)
                        Privilege.DPM.setMasterVolumeMuted(Privilege.DAR, !state)
                        ShortcutUtils.setShortcut(this, MyShortcut.Mute, state)
                    }
                    "USER_RESTRICTION" -> {
                        val state = intent?.getBooleanExtra("state", false)
                        val id = intent?.getStringExtra("restriction")
                        if (state == null || id == null) return
                        if (state) {
                            Privilege.DPM.addUserRestriction(Privilege.DAR, id)
                        } else {
                            Privilege.DPM.clearUserRestriction(Privilege.DAR, id)
                        }
                        ShortcutUtils.updateUserRestrictionShortcut(this, id, !state, false)
                    }
                    "USER_OPERATION" -> {
                        val typeName = intent.getStringExtra("operation") ?: return
                        val type = UserOperationType.valueOf(typeName)
                        val serial = intent.getIntExtra("serial", -1)
                        if (serial == -1) return
                        doUserOperationWithContext(this, type, serial, false)
                    }
                }
                Log.d(TAG, "Received intent: $action")
                showOperationResultToast(true)
            } else {
                showOperationResultToast(false)
            }
        } catch(e: Exception) {
            e.printStackTrace()
        } finally {
            finish()
        }
    }
    companion object {
        private const val TAG = "ShortcutsReceiver"
    }
}
