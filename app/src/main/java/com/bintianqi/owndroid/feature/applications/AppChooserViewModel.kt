package com.bintianqi.owndroid.feature.applications

import android.content.Context
import android.content.RestrictionsManager
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.getAppInfo
import com.bintianqi.owndroid.utils.getInstalledAppsFlags
import com.bintianqi.owndroid.utils.searchInString
import com.bintianqi.owndroid.utils.transformAppRestrictionEntryList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppChooserViewModel(val application: MyApplication, val ph: PrivilegeHelper) : ViewModel() {
    val packagesState = MutableStateFlow(emptyList<AppChooserEntry>())
    val progressState = MutableStateFlow(0F)

    fun refreshPackageList() {
        viewModelScope.launch(Dispatchers.IO) {
            packagesState.value = emptyList()
            val apps = application.packageManager.getInstalledApplications(getInstalledAppsFlags)
            apps.forEachIndexed { index, info ->
                packagesState.update {
                    it + getAppStatus(info.packageName)
                }
                progressState.value = (index + 1).toFloat() / apps.size
            }
        }
    }

    private fun getAppStatus(packageName: String): AppChooserEntry {
        val appInfo = getAppInfo(application.packageManager, packageName)
        val rm = application.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
        var hasMc = false
        var mcModified = false
        var suspended = false
        var hidden = false
        var ub = false
        var ucd = false
        var mdd = false
        try {
            ph.safeDpmCall {
                val bundle = dpm.getApplicationRestrictions(dar, packageName)
                val entries = rm.getManifestRestrictions(packageName)
                if (entries != null) {
                    hasMc = true
                    val restrictions = transformAppRestrictionEntryList(entries, bundle)
                    if (restrictions.any { !it.isNull() }) mcModified = true
                }
            }
        } catch (_: Exception) {}
        try {
            ph.safeDpmCall {
                if (Build.VERSION.SDK_INT >= 24) {
                    suspended = dpm.isPackageSuspended(dar, packageName)
                }
                hidden = dpm.isApplicationHidden(dar, packageName)
                ub = dpm.isUninstallBlocked(dar, packageName)
                if (Build.VERSION.SDK_INT >= 30) {
                    ucd = packageName in dpm.getUserControlDisabledPackages(dar)
                }
                if (Build.VERSION.SDK_INT >= 28) {
                    mdd = packageName in dpm.getMeteredDataDisabledPackages(dar)
                }
            }
        } catch (_: Exception) {}
        return AppChooserEntry(appInfo, hasMc, mcModified, suspended, hidden, ub, ucd, mdd)
    }

    fun onPackageRemoved(name: String) {
        packagesState.update { list ->
            list.filter { it.info.name != name }
        }
    }

    fun filterApp(app: AppChooserEntry, filter: AppChooserFilter, query: String): Boolean {
        return (filter.userApps == filter.systemApps ||
                (filter.userApps && app.info.flags and ApplicationInfo.FLAG_SYSTEM == 0) ||
                (filter.systemApps && app.info.flags and ApplicationInfo.FLAG_SYSTEM != 0)) &&
                (!filter.hasMc || app.hasMc) && (!filter.mcModified || app.mcModified) &&
                (filter.suspended == filter.notSuspended || (filter.suspended && app.suspended)
                        || (filter.notSuspended && !app.suspended)) &&
                (filter.hidden == filter.notHidden || (filter.hidden && app.hidden) ||
                        (filter.notHidden && !app.hidden)) &&
                (filter.ub == filter.notUb || (filter.ub && app.ub) || (filter.notUb && !app.ub)) &&
                (filter.ucDisabled == filter.ucNotDisabled || (filter.ucDisabled && app.ucd) ||
                        (filter.ucNotDisabled && !app.ucd)) &&
                (filter.mdDisabled == filter.mdNotDisabled || (filter.mdDisabled && app.mdd) ||
                        (filter.mdNotDisabled && !app.mdd)) &&
                (query.isEmpty() || searchInString(query, app.info.name) ||
                        searchInString(query, app.info.label)) &&
                (!filter.installed || app.info.flags and ApplicationInfo.FLAG_INSTALLED != 0)
    }

    fun updateAppState(name: String) {
        if (name.isEmpty()) return
        packagesState.update { list ->
            list.map {
                if (it.info.name == name) {
                    getAppStatus(name)
                } else it
            }
        }
    }
}
