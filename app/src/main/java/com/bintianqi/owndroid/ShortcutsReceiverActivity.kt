package com.bintianqi.owndroid

import android.app.Activity
import android.os.Bundle
import android.util.Log

class ShortcutsReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            val action = intent.action?.removePrefix("com.bintianqi.owndroid.action.")
            if (action != null && SP.shortcuts) {
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
                }
                Log.d(TAG, "Received intent: $action")
                showOperationResultToast(true)
            }
        } finally {
            finish()
        }
    }
    companion object {
        private const val TAG = "ShortcutsReceiver"
    }
}
