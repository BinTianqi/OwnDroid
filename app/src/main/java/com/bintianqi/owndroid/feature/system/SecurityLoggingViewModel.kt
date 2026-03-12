package com.bintianqi.owndroid.feature.system

import android.app.admin.SecurityLog
import android.net.Uri
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.collections.isNotEmpty

class SecurityLoggingViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val repo: SecurityLoggingRepository,
    val toastChannel: ToastChannel
) : ViewModel() {
    val enabledState = MutableStateFlow(false)

    @RequiresApi(24)
    fun getEnabled() = ph.safeDpmCall {
        enabledState.value = dpm.isSecurityLoggingEnabled(dar)
    }

    @RequiresApi(24)
    fun setEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.setSecurityLoggingEnabled(dar, enabled)
        enabledState.value = enabled
    }

    val countState = MutableStateFlow(0L)

    fun getCount() {
        countState.value = repo.getSecurityLogsCount()
    }

    val exportingState = MutableStateFlow(false)

    fun exportLogs(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            exportingState.value = true
            application.contentResolver.openOutputStream(uri)?.use {
                repo.exportSecurityLogs(it)
            }
            exportingState.value = false
            toastChannel.sendStatus(true)
        }
    }

    fun deleteLogs() {
        repo.deleteSecurityLogs()
        countState.value = 0
    }

    var preRebootSecurityLogs = emptyList<SecurityLog.SecurityEvent>()

    @RequiresApi(24)
    fun getPreRebootSecurityLogs(callback: () -> Unit) {
        if (preRebootSecurityLogs.isNotEmpty()) callback()
        val result = try {
            val logs = ph.myDpm.retrievePreRebootSecurityLogs(ph.myDar)
            if (!logs.isNullOrEmpty()) {
                preRebootSecurityLogs = logs
                true
            } else false
        } catch (_: SecurityException) {
            false
        }
        if (!result) toastChannel.sendStatus(false)
    }

    @RequiresApi(24)
    fun exportPreRebootSecurityLogs(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            exportingState.value = true
            application.contentResolver.openOutputStream(uri)!!.use {
                repo.exportPRSecurityLogs(preRebootSecurityLogs, it)
            }
            exportingState.value = false
            toastChannel.sendStatus(true)
        }
    }
}
