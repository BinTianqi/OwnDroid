package com.bintianqi.owndroid

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bintianqi.owndroid.feature.users.UserOperationType
import com.bintianqi.owndroid.utils.ShortcutAction
import com.bintianqi.owndroid.utils.ShortcutSystemOption
import com.bintianqi.owndroid.utils.ShortcutUtils
import com.bintianqi.owndroid.utils.doUserOperationWithContext
import com.bintianqi.owndroid.utils.showOperationResultToast

class ShortcutsReceiverActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myApp = application as MyApplication
        val sr = myApp.container.settingsRepo
        val settings = sr.data.shortcut
        try {
            val requestKey = intent?.getStringExtra("key")
            if (requestKey == settings.key) {
                if (sr.data.shortcut.enabled) {
                    runCommand()
                } else {
                    Toast.makeText(this, R.string.shortcuts_disabled, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            finish()
        }
    }

    fun runCommand() {
        val context = this
        val myApp = application as MyApplication
        val sr = myApp.container.settingsRepo
        val ph = myApp.container.privilegeHelper
        val action = ShortcutAction.entries.find { it.name == intent.getStringExtra("action") }
        val state = intent.getBooleanExtra("state", false)
        ph.safeDpmCall {
            when (action) {
                ShortcutAction.LockScreen -> dpm.lockNow()
                ShortcutAction.SystemOption -> {
                    val option = ShortcutSystemOption.entries.find {
                        it.name == intent.getStringExtra("option")
                    }
                    when (option) {
                        ShortcutSystemOption.Camera -> dpm.setCameraDisabled(dar, state)
                        ShortcutSystemOption.Mute -> dpm.setMasterVolumeMuted(dar, !state)
                        null -> Log.e(TAG, "Unknown option")
                    }
                    ShortcutUtils.updateSystemOptionShortcuts(context, sr, option!!, !state)
                }

                ShortcutAction.UserRestriction -> {
                    val id = intent.getStringExtra("restriction")!!
                    if (state) {
                        dpm.clearUserRestriction(dar, id)
                    } else {
                        dpm.addUserRestriction(dar, id)
                    }
                    ShortcutUtils.updateUserRestrictionShortcuts(context, sr, id, !state)
                }

                ShortcutAction.UserOperation -> {
                    val typeName = intent.getStringExtra("operation") ?: return@safeDpmCall
                    val type = UserOperationType.valueOf(typeName)
                    val serial = intent.getIntExtra("serial", -1)
                    if (serial == -1) return@safeDpmCall
                    doUserOperationWithContext(context, ph.dpm, ph.dar, type, serial, false)
                }

                ShortcutAction.Logout -> {
                    if (Build.VERSION.SDK_INT >= 28) {
                        val result = dpm.logoutUser(dar)
                        context.showOperationResultToast(result == 0)
                    }
                }

                null -> Log.e(TAG, "Unknown action")
            }
        }
    }

    companion object {
        private const val TAG = "ShortcutsReceiver"
    }
}
