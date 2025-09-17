package com.bintianqi.owndroid

import android.app.Application
import android.app.PendingIntent
import android.app.admin.PackagePolicy
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.Privilege.DAR
import com.bintianqi.owndroid.Privilege.DPM
import com.bintianqi.owndroid.dpm.AppStatus
import com.bintianqi.owndroid.dpm.getPackageInstaller
import com.bintianqi.owndroid.dpm.isValidPackageName
import com.bintianqi.owndroid.dpm.parsePackageInstallerMessage
import com.bintianqi.owndroid.dpm.permissionList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

class MyViewModel(application: Application): AndroidViewModel(application) {
    val PM = application.packageManager
    val theme = MutableStateFlow(ThemeSettings(SP.materialYou, SP.darkTheme, SP.blackTheme))
    fun changeTheme(newTheme: ThemeSettings) {
        theme.value = newTheme
        SP.materialYou = newTheme.materialYou
        SP.darkTheme = newTheme.darkTheme
        SP.blackTheme = newTheme.blackTheme
    }

    val chosenPackage = Channel<String>(1, BufferOverflow.DROP_LATEST)

    val installedPackages = MutableStateFlow(emptyList<AppInfo>())
    val refreshPackagesProgress = MutableStateFlow(0F)
    fun refreshPackageList() {
        viewModelScope.launch(Dispatchers.IO) {
            installedPackages.value = emptyList()
            val apps = PM.getInstalledApplications(getInstalledAppsFlags)
            apps.forEachIndexed { index, info ->
                installedPackages.update {
                    it + getAppInfo(info)
                }
                refreshPackagesProgress.value = (index + 1).toFloat() / apps.size
            }
        }
    }
    fun getAppInfo(info: ApplicationInfo) =
        AppInfo(info.packageName, info.loadLabel(PM).toString(), info.loadIcon(PM), info.flags)
    fun getAppInfo(name: String): AppInfo {
        return try {
            getAppInfo(PM.getApplicationInfo(name, getInstalledAppsFlags))
        } catch (_: PackageManager.NameNotFoundException) {
            AppInfo(name, "???", Color.Transparent.toArgb().toDrawable(), 0)
        }
    }

    val suspendedPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(24)
    fun getSuspendedPackaged() {
        val packages = PM.getInstalledApplications(getInstalledAppsFlags).filter {
            DPM.isPackageSuspended(DAR, it.packageName)
        }
        suspendedPackages.value = packages.map { getAppInfo(it) }
    }
    @RequiresApi(24)
    fun setPackageSuspended(name: String, status: Boolean): Boolean {
        val result = DPM.setPackagesSuspended(DAR, arrayOf(name), status)
        getSuspendedPackaged()
        return result.isEmpty()
    }

    val hiddenPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getHiddenPackages() {
        viewModelScope.launch {
            hiddenPackages.value = PM.getInstalledApplications(getInstalledAppsFlags).filter {
                DPM.isApplicationHidden(DAR, it.packageName)
            }.map { getAppInfo(it) }
        }
    }
    fun setPackageHidden(name: String, status: Boolean): Boolean {
        val result = DPM.setApplicationHidden(DAR, name, status)
        getHiddenPackages()
        return result
    }

    // Uninstall blocked packages
    val ubPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getUbPackages() {
        viewModelScope.launch {
            ubPackages.value = PM.getInstalledApplications(getInstalledAppsFlags).filter {
                DPM.isUninstallBlocked(DAR, it.packageName)
            }.map { getAppInfo(it) }
        }
    }
    fun setPackageUb(name: String, status: Boolean) {
        DPM.setUninstallBlocked(DAR, name, status)
        getUbPackages()
    }

    // User control disabled packages
    val ucdPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(30)
    fun getUcdPackages() {
        ucdPackages.value = DPM.getUserControlDisabledPackages(DAR).map {
            getAppInfo(it)
        }
    }
    @RequiresApi(30)
    fun setPackageUcd(name: String, status: Boolean) {
        DPM.setUserControlDisabledPackages(
            DAR,
            ucdPackages.value.map { it.name }.run { if (status) plus(name) else minus(name) }
        )
        getUcdPackages()
    }

    val packagePermissions = MutableStateFlow(emptyMap<String, Int>())
    @RequiresApi(23)
    fun getPackagePermissions(name: String) {
        if (name.isValidPackageName) {
            packagePermissions.value = permissionList().associate {
                it.permission to DPM.getPermissionGrantState(DAR, name, it.permission)
            }
        } else {
            packagePermissions.value = emptyMap()
        }
    }
    @RequiresApi(23)
    fun setPackagePermission(name: String, permission: String, status: Int): Boolean {
        val result = DPM.setPermissionGrantState(DAR, name, permission, status)
        getPackagePermissions(name)
        return result
    }

    // Metered data disabled packages
    val mddPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(28)
    fun getMddPackages() {
        mddPackages.value = DPM.getMeteredDataDisabledPackages(DAR).map { getAppInfo(it) }
    }
    @RequiresApi(28)
    fun setPackageMdd(name: String, status: Boolean): Boolean {
        val result = DPM.setMeteredDataDisabledPackages(
            DAR, mddPackages.value.map { it.name }.run { if (status) plus(name) else minus(name) }
        )
        getMddPackages()
        return result.isEmpty()
    }

    // Keep uninstalled packages
    val kuPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(28)
    fun getKuPackages() {
        kuPackages.value = DPM.getKeepUninstalledPackages(DAR)?.map { getAppInfo(it) } ?: emptyList()
    }
    @RequiresApi(28)
    fun setPackageKu(name: String, status: Boolean) {
        DPM.setKeepUninstalledPackages(
            DAR, kuPackages.value.map { it.name }.run { if (status) plus(name) else minus(name) }
        )
        getKuPackages()
    }

    // Cross profile packages
    val cpPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(30)
    fun getCpPackages() {
        cpPackages.value = DPM.getCrossProfilePackages(DAR).map { getAppInfo(it) }
    }
    @RequiresApi(30)
    fun setPackageCp(name: String, status: Boolean) {
        DPM.setCrossProfilePackages(
            DAR,
            cpPackages.value.map { it.name }.toSet().run { if (status) plus(name) else minus(name) }
        )
        getCpPackages()
    }

    // Cross-profile widget providers
    val cpwProviders = MutableStateFlow(emptyList<AppInfo>())
    fun getCpwProviders() {
        cpwProviders.value = DPM.getCrossProfileWidgetProviders(DAR).map { getAppInfo(it) }
    }
    fun setCpwProvider(name: String, status: Boolean): Boolean {
        val result = if (status) {
            DPM.addCrossProfileWidgetProvider(DAR, name)
        } else {
            DPM.removeCrossProfileWidgetProvider(DAR, name)
        }
        getCpwProviders()
        return result
    }

    @RequiresApi(28)
    fun clearAppData(name: String, callback: (Boolean) -> Unit) {
        DPM.clearApplicationUserData(DAR, name, Executors.newSingleThreadExecutor()) { _, result ->
            callback(result)
        }
    }

    fun uninstallPackage(packageName: String, onComplete: (String?) -> Unit) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val statusExtra = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
                if(statusExtra == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                    @SuppressWarnings("UnsafeIntentLaunch")
                    context.startActivity(intent.getParcelableExtra(Intent.EXTRA_INTENT) as Intent?)
                } else {
                    context.unregisterReceiver(this)
                    if(statusExtra == PackageInstaller.STATUS_SUCCESS) {
                        onComplete(null)
                    } else {
                        onComplete(parsePackageInstallerMessage(context, intent))
                    }
                }
            }
        }
        ContextCompat.registerReceiver(
            application, receiver, IntentFilter(AppInstallerViewModel.ACTION), null,
            null, ContextCompat.RECEIVER_EXPORTED
        )
        val pi = if(VERSION.SDK_INT >= 34) {
            PendingIntent.getBroadcast(
                application, 0, Intent(AppInstallerViewModel.ACTION),
                PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_MUTABLE
            ).intentSender
        } else {
            PendingIntent.getBroadcast(application, 0, Intent(AppInstallerViewModel.ACTION), PendingIntent.FLAG_MUTABLE).intentSender
        }
        application.getPackageInstaller().uninstall(packageName, pi)
    }

    @RequiresApi(28)
    fun installExistingApp(name: String): Boolean {
        return DPM.installExistingPackage(DAR, name)
    }

    // Credential manager policy
    val cmPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(34)
    fun getCmPolicy(): Int {
        return DPM.credentialManagerPolicy?.let { policy ->
            cmPackages.value = policy.packageNames.map { getAppInfo(it) }
            policy.policyType
        } ?: -1
    }
    fun setCmPackage(name: String, status: Boolean) {
        cmPackages.update { list ->
            if (status) list + getAppInfo(name) else list.dropWhile { it.name == name }
        }
    }
    @RequiresApi(34)
    fun setCmPolicy(type: Int) {
        DPM.credentialManagerPolicy = if (type != -1 && cmPackages.value.isNotEmpty()) {
            PackagePolicy(type, cmPackages.value.map { it.name }.toSet())
        } else null
        getCmPolicy()
    }

    // Permitted input method
    val pimPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getPimPackages(): Boolean {
        return DPM.getPermittedInputMethods(DAR).let { packages ->
            pimPackages.value = packages?.map { getAppInfo(it) } ?: emptyList()
            packages == null
        }
    }
    fun setPimPackage(name: String, status: Boolean) {
        pimPackages.update { packages ->
            if (status) packages + getAppInfo(name) else packages.dropWhile { it.name == name }
        }
    }
    fun setPimPolicy(allowAll: Boolean): Boolean {
        val result = DPM.setPermittedInputMethods(
            DAR, if (allowAll) null else pimPackages.value.map { it.name })
        getPimPackages()
        return result
    }

    // Permitted accessibility services
    val pasPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getPasPackages(): Boolean {
        return DPM.getPermittedAccessibilityServices(DAR).let { packages ->
            pasPackages.value = packages?.map { getAppInfo(it) } ?: emptyList()
            packages == null
        }
    }
    fun setPasPackage(name: String, status: Boolean) {
        pasPackages.update { packages ->
            if (status) packages + getAppInfo(name) else packages.dropWhile { it.name == name }
        }
    }
    fun setPasPolicy(allowAll: Boolean): Boolean {
        val result = DPM.setPermittedAccessibilityServices(
            DAR, if (allowAll) null else pasPackages.value.map { it.name })
        getPasPackages()
        return result
    }

    fun enableSystemApp(name: String) {
        DPM.enableSystemApp(DAR, name)
    }

    val appStatus = MutableStateFlow(AppStatus(false, false, false, false, false, false))
    fun getAppStatus(name: String) {
        appStatus.value = AppStatus(
            if (VERSION.SDK_INT >= 24) DPM.isPackageSuspended(DAR, name) else false,
            DPM.isApplicationHidden(DAR, name),
            DPM.isUninstallBlocked(DAR, name),
            if (VERSION.SDK_INT >= 30) name in DPM.getUserControlDisabledPackages(DAR) else false,
            if (VERSION.SDK_INT >= 28) name in DPM.getMeteredDataDisabledPackages(DAR) else false,
            if (VERSION.SDK_INT >= 28) DPM.getKeepUninstalledPackages(DAR)?.contains(name) == true else false
        )
    }
    // Application details
    @RequiresApi(24)
    fun adSetPackageSuspended(name: String, status: Boolean) {
        DPM.setPackagesSuspended(DAR, arrayOf(name), status)
        appStatus.update { it.copy(suspend = DPM.isPackageSuspended(DAR, name)) }
    }
    fun adSetPackageHidden(name: String, status: Boolean) {
        DPM.setApplicationHidden(DAR, name, status)
        appStatus.update { it.copy(hide = DPM.isApplicationHidden(DAR, name)) }
    }
    fun adSetPackageUb(name: String, status: Boolean) {
        DPM.setUninstallBlocked(DAR, name, status)
        appStatus.update { it.copy(uninstallBlocked = DPM.isUninstallBlocked(DAR, name)) }
    }
    @RequiresApi(30)
    fun adSetPackageUcd(name: String, status: Boolean) {
        DPM.setUserControlDisabledPackages(DAR,
            DPM.getUserControlDisabledPackages(DAR).run { if (status) plus(name) else minus(name) })
        appStatus.update {
            it.copy(userControlDisabled = name in DPM.getUserControlDisabledPackages(DAR))
        }
    }
    @RequiresApi(28)
    fun adSetPackageMdd(name: String, status: Boolean) {
        DPM.setMeteredDataDisabledPackages(DAR,
            DPM.getMeteredDataDisabledPackages(DAR).run { if (status) plus(name) else minus(name) })
        appStatus.update {
            it.copy(meteredDataDisabled = name in DPM.getMeteredDataDisabledPackages(DAR))
        }
    }
    @RequiresApi(28)
    fun adSetPackageKu(name: String, status: Boolean) {
        DPM.setKeepUninstalledPackages(DAR,
            DPM.getKeepUninstalledPackages(DAR)?.run { if (status) plus(name) else minus(name) } ?: emptyList())
        appStatus.update {
            it.copy(keepUninstalled = DPM.getKeepUninstalledPackages(DAR)?.contains(name) == true )
        }
    }

    @RequiresApi(34)
    fun setDefaultDialer(name: String): Boolean {
        return try {
            DPM.setDefaultDialerApplication(name)
            true
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            false
        }
    }
}

data class ThemeSettings(
    val materialYou: Boolean = false,
    val darkTheme: Int = -1,
    val blackTheme: Boolean = false
)
