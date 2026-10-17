package com.bintianqi.owndroid

import android.app.admin.IDevicePolicyManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.Process
import android.os.ServiceManager
import android.util.Log
import androidx.annotation.Keep
import com.bintianqi.owndroid.utils.ACTIVATE_DEVICE_OWNER_COMMAND
import kotlin.system.exitProcess

@Keep
class ActivationService : IActivationService.Stub() {
    override fun execute(command: String): Bundle? {
        if (!checkUid()) return null
        try {
            val bundle = Bundle()
            val process = Runtime.getRuntime().exec(command)
            val exitCode = process.waitFor()
            bundle.putInt("code", exitCode)
            bundle.putString("output",  process.inputStream.readBytes().decodeToString())
            bundle.putString("error", process.errorStream.readBytes().decodeToString())
            return bundle
        } catch(e: Exception) {
            Log.e(TAG, null, e)
            return null
        }
    }

    override fun activateDeviceOwner(): Bundle? {
        if (!checkUid()) return null
        var bypassed = false
        Log.d(TAG, "Trying to bypass restriction")
        val getProc = Runtime.getRuntime().exec("settings get secure user_setup_complete")
        val getOutput = getProc.inputStream.readBytes().decodeToString()
        if (getOutput == "1" || getOutput == "1\n") {
            val putProc = Runtime.getRuntime().exec("settings put secure user_setup_complete 0")
            val exitCode = putProc.waitFor()
            Log.d(TAG, "Setting user_setup_complete, exit code $exitCode")
            bypassed = exitCode == 0
        } else {
            Log.w(TAG, "Get user_setup_complete, expected 1, actual $getOutput")
        }

        if (bypassed) forceUpdateSetupComplete()

        Log.d(TAG, "Setting device owner")
        val setOwnerResult = execute(ACTIVATE_DEVICE_OWNER_COMMAND)

        if (bypassed) {
            Log.d(TAG, "Restoring user_setup_complete")
            val putProc = Runtime.getRuntime().exec("settings put secure user_setup_complete 1")
            putProc.waitFor()
            forceUpdateSetupComplete()
        }
        return setOwnerResult
    }

    private fun forceUpdateSetupComplete() {
        try {
            Log.d(TAG, "Trying to force update DPMS state")
            val binder = ServiceManager.getService(Context.DEVICE_POLICY_SERVICE)
            val dpm = IDevicePolicyManager.Stub.asInterface(binder)
            if (Build.VERSION.SDK_INT >= 31) {
                dpm.forceUpdateUserSetupComplete(0)
            } else {
                dpm.forceUpdateUserSetupComplete()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to force update DPMS state", e)
        }
    }

    /** Used by Shizuku */
    override fun destroy() {
        exitProcess(0)
    }

    private fun checkUid(): Boolean {
        val uid = Process.myUid()
        return uid == 0 || uid == 2000
    }

    companion object {
        const val TAG = "ActivationService"
    }
}
