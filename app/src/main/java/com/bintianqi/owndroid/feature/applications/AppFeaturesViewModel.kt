package com.bintianqi.owndroid.feature.applications

import android.app.admin.PackagePolicy
import android.content.pm.PackageManager
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

class AppFeaturesViewModel(
    val application: MyApplication, val ph: PrivilegeHelper,
    val privilegeState: StateFlow<PrivilegeStatus>,
    val toastChannel: ToastChannel
) : ViewModel() {
    val pm = application.packageManager!!
    
    val suspendedPackages = MutableStateFlow(emptyList<AppInfo>())

    @RequiresApi(24)
    fun getSuspendedPackaged() = ph.safeDpmCall {
        val packages = pm.getInstalledApplications(getInstalledAppsFlags).filter {
            dpm.isPackageSuspended(dar, it.packageName)
        }
        suspendedPackages.value = packages.map { getAppInfo(pm, it) }
    }

    @RequiresApi(24)
    fun setPackageSuspended(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        dpm.setPackagesSuspended(dar, packages.toTypedArray(), status)
        getSuspendedPackaged()
    }

    val hiddenPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getHiddenPackages() = ph.safeDpmCall {
        hiddenPackages.value = pm.getInstalledApplications(getInstalledAppsFlags).filter {
            dpm.isApplicationHidden(dar, it.packageName)
        }.map { getAppInfo(pm, it) }
    }

    fun setPackageHidden(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        for (name in packages) {
            dpm.setApplicationHidden(dar, name, status)
        }
        getHiddenPackages()
    }

    // Uninstall blocked packages
    val ubPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getUbPackages() = ph.safeDpmCall {
        ubPackages.value = pm.getInstalledApplications(getInstalledAppsFlags).filter {
            dpm.isUninstallBlocked(dar, it.packageName)
        }.map { getAppInfo(pm, it) }
    }

    fun setPackageUb(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        for (name in packages) {
            dpm.setUninstallBlocked(dar, name, status)
        }
        getUbPackages()
    }

    // User control disabled packages
    val ucdPackages = MutableStateFlow(emptyList<AppInfo>())

    @RequiresApi(30)
    fun getUcdPackages() = ph.safeDpmCall {
        ucdPackages.value = dpm.getUserControlDisabledPackages(dar).distinct().map {
            getAppInfo(pm, it)
        }
    }

    @RequiresApi(30)
    fun setPackageUcd(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        dpm.setUserControlDisabledPackages(
            dar,
            ucdPackages.value.map { it.name }.plusOrMinus(status, packages)
        )
        getUcdPackages()
    }

    val permissionPackagesState = MutableStateFlow(emptyList<Pair<AppInfo, Int>>())

    fun getPermissionPackages(permission: String) {
        viewModelScope.launch(Dispatchers.IO) {
            permissionPackagesState.value = emptyList()
            ph.safeDpmCall {
                permissionPackagesState.value = pm.getInstalledPackages(
                    getInstalledAppsFlags or PackageManager.GET_PERMISSIONS
                ).filter {
                    it.requestedPermissions?.contains(permission) ?: false
                }.map {
                    getAppInfo(pm, it.packageName) to
                            dpm.getPermissionGrantState(dar, it.packageName, permission)
                }
            }
        }
    }

    fun setPackagePermission(
        packageName: String, permission: String, state: Int
    ) = ph.safeDpmCall {
        val result = dpm.setPermissionGrantState(dar, packageName, permission, state)
        if (result) {
            getPermissionPackages(permission)
        } else {
            toastChannel.sendStatus(false)
        }
    }

    @RequiresApi(28)
    fun clearStorage(packageName: String, callback: () -> Unit) = ph.safeDpmCall {
        dpm.clearApplicationUserData(dar, packageName, application.mainExecutor) { _, result ->
            callback()
            toastChannel.sendStatus(result)
        }
    }

    fun uninstallApp(packageName: String, callback: (String?) -> Unit) {
        uninstallPackage(application, ph, packageName, callback)
    }

    // Metered data disabled packages
    val mddPackages = MutableStateFlow(emptyList<AppInfo>())

    @RequiresApi(28)
    fun getMddPackages() = ph.safeDpmCall {
        mddPackages.value =
            dpm.getMeteredDataDisabledPackages(dar).distinct().map { getAppInfo(pm, it) }
    }

    @RequiresApi(28)
    fun setPackageMdd(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        dpm.setMeteredDataDisabledPackages(
            dar, mddPackages.value.map { it.name }.plusOrMinus(status, packages)
        )
        getMddPackages()
    }

    // Keep uninstalled packages
    val kuPackages = MutableStateFlow(emptyList<AppInfo>())

    @RequiresApi(28)
    fun getKuPackages() = ph.safeDpmCall {
        kuPackages.value =
            dpm.getKeepUninstalledPackages(dar)?.distinct()?.map { getAppInfo(pm, it) } ?: emptyList()
    }

    @RequiresApi(28)
    fun setPackageKu(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        dpm.setKeepUninstalledPackages(
            dar, kuPackages.value.map { it.name }.plusOrMinus(status, packages)
        )
        getKuPackages()
    }

    // Cross profile packages
    val cpPackages = MutableStateFlow(emptyList<AppInfo>())

    @RequiresApi(30)
    fun getCpPackages() = ph.safeDpmCall {
        cpPackages.value = dpm.getCrossProfilePackages(dar).map { getAppInfo(pm, it) }
    }

    @RequiresApi(30)
    fun setPackageCp(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        dpm.setCrossProfilePackages(
            dar,
            cpPackages.value.map { it.name }.toSet().run {
                if (status) plus(packages) else minus(packages)
            }
        )
        getCpPackages()
    }

    // Cross-profile widget providers
    val cpwProviders = MutableStateFlow(emptyList<AppInfo>())
    fun getCpwProviders() = ph.safeDpmCall {
        cpwProviders.value =
            dpm.getCrossProfileWidgetProviders(dar).distinct().map { getAppInfo(pm, it) }
    }

    fun setCpwProvider(packages: List<String>, status: Boolean) = ph.safeDpmCall {
        for (name in packages) {
            if (status) {
                dpm.addCrossProfileWidgetProvider(dar, name)
            } else {
                dpm.removeCrossProfileWidgetProvider(dar, name)
            }
        }
        getCpwProviders()
    }

    @RequiresApi(28)
    fun installExistingApp(name: String) = ph.safeDpmCall {
        val result = dpm.installExistingPackage(dar, name)
        toastChannel.sendStatus(result)
    }

    // Credential manager policy
    val cmPolicyState = MutableStateFlow(-1)
    val cmPackagesState = MutableStateFlow(emptyList<AppInfo>())

    @RequiresApi(34)
    fun getCmPolicy() = ph.safeDpmCall {
        val policy = dpm.credentialManagerPolicy
        if (policy != null) {
            cmPackagesState.value = policy.packageNames.distinct().map { getAppInfo(pm, it) }
        }
        cmPolicyState.value = policy?.policyType ?: -1
    }

    fun setCmPolicy(type: Int) {
        cmPolicyState.value = type
    }

    fun setCmPackage(packages: List<String>, status: Boolean) {
        cmPackagesState.update {
            updateAppInfoList(it, packages, status)
        }
    }

    @RequiresApi(34)
    fun applyCmPolicy() = ph.safeDpmCall {
        val type = cmPolicyState.value
        dpm.credentialManagerPolicy = if (type != -1 && cmPackagesState.value.isNotEmpty()) {
            PackagePolicy(type, cmPackagesState.value.map { it.name }.toSet())
        } else null
        getCmPolicy()
    }

    fun updateAppInfoList(
        origin: List<AppInfo>, input: List<String>, status: Boolean
    ): List<AppInfo> {
        return if (status) {
            origin + input.map { getAppInfo(pm, it) }
        } else {
            origin.filter { it.name !in input }
        }
    }

    // Permitted input method
    val pimAllowAll = MutableStateFlow(true)
    val pimPackages = MutableStateFlow(emptyList<AppInfo>())

    fun getPimPolicy() = ph.safeDpmCall {
        val packages = dpm.getPermittedInputMethods(dar)
        pimAllowAll.value = packages == null
        if (packages != null) pimPackages.value = packages.distinct().map { getAppInfo(pm, it) }
    }

    fun setPimAllowAll(state: Boolean) {
        pimAllowAll.value = state
    }

    fun setPimPackage(packages: List<String>, status: Boolean) {
        pimPackages.update {
            updateAppInfoList(it, packages, status)
        }
    }

    fun applyPimPolicy() = ph.safeDpmCall {
        val result = dpm.setPermittedInputMethods(
            dar, if (pimAllowAll.value) null else pimPackages.value.map { it.name }
        )
        toastChannel.sendStatus(result)
        getPimPolicy()
    }

    // Permitted accessibility services
    val pasAllowAll = MutableStateFlow(true)
    val pasPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getPasPolicy() = ph.safeDpmCall {
        val packages = dpm.getPermittedAccessibilityServices(dar)
        pasAllowAll.value = packages == null
        if (packages != null) pasPackages.value = packages.distinct().map { getAppInfo(pm, it) }
    }

    fun setPasAllowAll(state: Boolean) {
        pasAllowAll.value = state
    }

    fun setPasPackage(packages: List<String>, status: Boolean) {
        pasPackages.update {
            updateAppInfoList(it, packages, status)
        }
    }

    fun applyPasPolicy() = ph.safeDpmCall {
        val result = dpm.setPermittedAccessibilityServices(
            dar, if (pasAllowAll.value) null else pasPackages.value.map { it.name }
        )
        toastChannel.sendStatus(result)
        getPasPolicy()
    }

    fun enableSystemApp(name: String) = ph.safeDpmCall {
        dpm.enableSystemApp(dar, name)
    }

    @RequiresApi(34)
    fun setDefaultDialer(name: String) = ph.safeDpmCall {
        val result = try {
            dpm.setDefaultDialerApplication(name)
            true
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            false
        }
        toastChannel.sendStatus(result)
    }
}
