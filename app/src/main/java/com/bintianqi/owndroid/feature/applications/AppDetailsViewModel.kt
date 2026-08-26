package com.bintianqi.owndroid.feature.applications

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PermissionInfo
import android.net.Uri
import android.os.Build.VERSION
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.getAppInfo
import com.bintianqi.owndroid.utils.getInstalledAppsFlags
import com.bintianqi.owndroid.utils.plusOrMinus
import com.bintianqi.owndroid.utils.uninstallPackage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppDetailsViewModel(
    val packageName: String, val application: MyApplication, val ph: PrivilegeHelper,
    val privilegeState: StateFlow<PrivilegeStatus>, val toastChannel: ToastChannel
) : ViewModel() {
    val appInfo = MutableStateFlow<AppInfo?>(null)
    val detailedAppInfo = MutableStateFlow(DetailedAppInfo())
    val uiState = MutableStateFlow(AppDetailsUiState())

    fun getInfo() {
        viewModelScope.launch(Dispatchers.IO) {
            val pkgInfo = application.packageManager.getPackageInfo(
                packageName, getInstalledAppsFlags
            )
            appInfo.value = getAppInfo(application.packageManager, packageName)
            detailedAppInfo.value = DetailedAppInfo(pkgInfo.versionName ?: "", pkgInfo.versionCode)
            getStatus()
        }
    }

    fun viewAppDetails(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        intent.data = Uri.fromParts("package", packageName, null)
        try {
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    private fun getStatus() = ph.safeDpmCall {
        uiState.value = AppDetailsUiState(
            if (VERSION.SDK_INT >= 24) dpm.isPackageSuspended(dar, packageName) else false,
            dpm.isApplicationHidden(dar, packageName),
            dpm.isUninstallBlocked(dar, packageName),
            if (VERSION.SDK_INT >= 30) packageName in dpm.getUserControlDisabledPackages(dar)
            else false,
            if (VERSION.SDK_INT >= 28) packageName in dpm.getMeteredDataDisabledPackages(dar)
            else false,
            if (VERSION.SDK_INT >= 28 && privilegeState.value.device)
                dpm.getKeepUninstalledPackages(dar)?.contains(packageName) == true
            else false
        )
    }

    @RequiresApi(24)
    fun setSuspended(status: Boolean) = ph.safeDpmCall {
        try {
            dpm.setPackagesSuspended(dar, arrayOf(packageName), status)
            uiState.update { it.copy(suspend = dpm.isPackageSuspended(dar, packageName)) }
        } catch (_: Exception) {
        }
    }

    fun setHidden(status: Boolean) = ph.safeDpmCall {
        dpm.setApplicationHidden(dar, packageName, status)
        uiState.update { it.copy(hide = dpm.isApplicationHidden(dar, packageName)) }
    }

    fun setUninstallBlocked(status: Boolean) = ph.safeDpmCall {
        dpm.setUninstallBlocked(dar, packageName, status)
        uiState.update { it.copy(uninstallBlocked = dpm.isUninstallBlocked(dar, packageName)) }
    }

    @RequiresApi(30)
    fun setUserControlDisabled(state: Boolean) = ph.safeDpmCall {
        dpm.setUserControlDisabledPackages(
            dar,
            dpm.getUserControlDisabledPackages(dar).plusOrMinus(state, packageName)
        )
        uiState.update {
            it.copy(userControlDisabled = packageName in dpm.getUserControlDisabledPackages(dar))
        }
    }

    @RequiresApi(28)
    fun setMeteredDataDisabled(state: Boolean) = ph.safeDpmCall {
        dpm.setMeteredDataDisabledPackages(
            dar,
            dpm.getMeteredDataDisabledPackages(dar).plusOrMinus(state, packageName)
        )
        uiState.update {
            it.copy(meteredDataDisabled = packageName in dpm.getMeteredDataDisabledPackages(dar))
        }
    }

    @RequiresApi(28)
    fun setKeepUninstalled(state: Boolean) = ph.safeDpmCall {
        dpm.setKeepUninstalledPackages(
            dar,
            (dpm.getKeepUninstalledPackages(dar) ?: emptyList()).plusOrMinus(state, packageName)
        )
        uiState.update {
            it.copy(
                keepUninstalled = dpm.getKeepUninstalledPackages(dar)?.contains(packageName) == true
            )
        }
    }

    val permissionsState = MutableStateFlow(emptyMap<PermissionItem, Int>())

    fun getPermissions() {
        viewModelScope.launch(Dispatchers.IO) {
            getPermissionsInternal()
        }
    }

    private fun getPermissionsInternal() {
        val pm = application.packageManager
        val allPermissions = mutableListOf<PermissionInfo>()
        pm.getAllPermissionGroups(0).forEach {
            allPermissions += pm.queryPermissionsByGroup(it.name, 0)
        }
        val requestedPermissions = application.packageManager.getPackageInfo(
            packageName, PackageManager.GET_PERMISSIONS or getInstalledAppsFlags
        ).requestedPermissions ?: emptyArray()
        val actualPermissions = allPermissions.filter {
            it.protectionLevel and PermissionInfo.PROTECTION_DANGEROUS != 0 &&
                    it.name in requestedPermissions
        }.map {
            PermissionItem(it.name, it.loadLabel(pm).toString(), getIconForPermission(it.name))
        }
        ph.safeDpmCall {
            permissionsState.value = actualPermissions.associateWith {
                dpm.getPermissionGrantState(dar, packageName, it.id)
            }
        }
    }

    fun setPermission(permission: String, status: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            ph.safeDpmCall {
                val result = dpm.setPermissionGrantState(dar, packageName, permission, status)
                if (result) {
                    getPermissions()
                } else {
                    toastChannel.sendStatus(false)
                }
            }
        }
    }

    @RequiresApi(28)
    fun clearData(callback: () -> Unit) = ph.safeDpmCall {
        dpm.clearApplicationUserData(dar, packageName, application.mainExecutor) { _, result ->
            callback()
            toastChannel.sendStatus(result)
        }
    }

    fun uninstall(callback: (String?) -> Unit) {
        uninstallPackage(application, ph, packageName, callback)
    }
}
