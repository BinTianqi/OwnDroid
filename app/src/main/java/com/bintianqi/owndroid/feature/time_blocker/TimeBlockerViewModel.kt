package com.bintianqi.owndroid.feature.time_blocker

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TimeBlockerViewModel(
    val application: MyApplication,
    val ph: PrivilegeHelper,
    val repo: TimeBlockerRepository,
    val toastChannel: ToastChannel,
    val privilegeState: StateFlow<PrivilegeStatus>
) : ViewModel() {

    val rulesState = MutableStateFlow(emptyList<BlockRule>())

    init {
        refreshRules()
    }

    fun refreshRules() {
        viewModelScope.launch(Dispatchers.IO) {
            rulesState.value = repo.getRules()
        }
    }

    fun addRule(rule: BlockRule) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.addRule(rule)
            refreshRules()
        }
    }

    fun updateRule(rule: BlockRule) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateRule(rule)
            refreshRules()
        }
    }

    fun deleteRule(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteRule(id)
            refreshRules()
        }
    }

    fun toggleRuleEnabled(rule: BlockRule) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateRule(rule.copy(enabled = !rule.enabled))
            refreshRules()
        }
    }

    fun hasUsageStatsPermission(): Boolean {
        val appOps = application.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            application.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun openUsageAccessSettings() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        application.startActivity(intent)
    }

    fun isServiceRunning(): Boolean {
        return TimeBlockerService.isRunning
    }

    fun startService() {
        TimeBlockerService.start(application)
    }

    fun stopService() {
        TimeBlockerService.stop(application)
    }
}
