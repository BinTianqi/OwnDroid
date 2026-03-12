package com.bintianqi.owndroid.feature.system

import android.app.admin.DevicePolicyManager.InstallSystemUpdateCallback
import android.app.admin.SystemUpdateInfo
import android.app.admin.SystemUpdatePolicy
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow

class SystemUpdateViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val toastChannel: ToastChannel
) : ViewModel() {
    val policyState = MutableStateFlow(SystemUpdatePolicyUiState())

    fun getPolicy() = ph.safeDpmCall {
        val policy = dpm.systemUpdatePolicy
        if (policy != null) {
            policyState.value = SystemUpdatePolicyUiState(
                SystemUpdatePolicyType.entries.find { it.id == policy.policyType }!!,
                policy.installWindowStart.toString(), policy.installWindowEnd.toString()
            )
        }
    }

    fun setPolicy(info: SystemUpdatePolicyUiState) {
        policyState.value = info
    }

    fun applyPolicy() = ph.safeDpmCall {
        val info = policyState.value
        val policy = when (info.type) {
            SystemUpdatePolicyType.Automatic -> SystemUpdatePolicy.createAutomaticInstallPolicy()
            SystemUpdatePolicyType.Windowed ->
                SystemUpdatePolicy.createWindowedInstallPolicy(info.start.toInt(), info.end.toInt())
            SystemUpdatePolicyType.Postpone -> SystemUpdatePolicy.createPostponeInstallPolicy()
            else -> null
        }
        dpm.setSystemUpdatePolicy(dar, policy)
        toastChannel.sendStatus(true)
    }

    val pendingUpdateState = MutableStateFlow(PendingSystemUpdateInfo())

    @RequiresApi(26)
    fun getPendingUpdate() = ph.safeDpmCall {
        val update = dpm.getPendingSystemUpdate(dar)
        pendingUpdateState.value = PendingSystemUpdateInfo(
            update != null, update?.receivedTime ?: 0,
            update?.securityPatchState == SystemUpdateInfo.SECURITY_PATCH_STATE_TRUE
        )
    }

    @RequiresApi(29)
    fun installUpdate(uri: Uri, callback: (String) -> Unit) = ph.safeDpmCall {
        val callback = object : InstallSystemUpdateCallback() {
            override fun onInstallUpdateError(errorCode: Int, errorMessage: String) {
                super.onInstallUpdateError(errorCode, errorMessage)
                val errDetail = when (errorCode) {
                    UPDATE_ERROR_BATTERY_LOW -> R.string.battery_low
                    UPDATE_ERROR_UPDATE_FILE_INVALID -> R.string.update_file_invalid
                    UPDATE_ERROR_INCORRECT_OS_VERSION -> R.string.incorrect_os_ver
                    UPDATE_ERROR_FILE_NOT_FOUND -> R.string.file_not_exist
                    else -> R.string.unknown_error
                }
                callback(application.getString(errDetail) + "\n$errorMessage")
            }
        }
        dpm.installSystemUpdate(dar, uri, application.mainExecutor, callback)
    }
}
