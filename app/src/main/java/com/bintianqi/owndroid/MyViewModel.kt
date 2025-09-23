package com.bintianqi.owndroid

import android.app.ActivityOptions
import android.app.Application
import android.app.PendingIntent
import android.app.admin.DeviceAdminInfo
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.InstallSystemUpdateCallback
import android.app.admin.FactoryResetProtectionPolicy
import android.app.admin.PackagePolicy
import android.app.admin.SystemUpdateInfo
import android.app.admin.SystemUpdatePolicy
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build.VERSION
import android.os.HardwarePropertiesManager
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
import com.bintianqi.owndroid.dpm.ACTIVATE_DEVICE_OWNER_COMMAND
import com.bintianqi.owndroid.dpm.AppStatus
import com.bintianqi.owndroid.dpm.CaCertInfo
import com.bintianqi.owndroid.dpm.DelegatedAdmin
import com.bintianqi.owndroid.dpm.DeviceAdmin
import com.bintianqi.owndroid.dpm.FrpPolicyInfo
import com.bintianqi.owndroid.dpm.HardwareProperties
import com.bintianqi.owndroid.dpm.PendingSystemUpdateInfo
import com.bintianqi.owndroid.dpm.SystemOptionsStatus
import com.bintianqi.owndroid.dpm.SystemUpdatePolicyInfo
import com.bintianqi.owndroid.dpm.delegatedScopesList
import com.bintianqi.owndroid.dpm.getPackageInstaller
import com.bintianqi.owndroid.dpm.isValidPackageName
import com.bintianqi.owndroid.dpm.parsePackageInstallerMessage
import com.bintianqi.owndroid.dpm.permissionList
import com.bintianqi.owndroid.dpm.temperatureTypes
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.concurrent.Executors

class MyViewModel(application: Application): AndroidViewModel(application) {
    val myRepo = getApplication<MyApplication>().myRepo
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

    @RequiresApi(24)
    fun reboot() {
        DPM.reboot(DAR)
    }
    @RequiresApi(24)
    fun requestBugReport(): Boolean {
        return DPM.requestBugreport(DAR)
    }
    @RequiresApi(24)
    fun getOrgName(): String {
        return DPM.getOrganizationName(DAR).toString()
    }
    @RequiresApi(24)
    fun setOrgName(name: String) {
        DPM.setOrganizationName(DAR, name)
    }
    @RequiresApi(31)
    fun setOrgId(id: String) {
        DPM.setOrganizationId(id)
    }
    @RequiresApi(31)
    fun getEnrollmentSpecificId(): String {
        return DPM.enrollmentSpecificId
    }
    val systemOptionsStatus = MutableStateFlow(SystemOptionsStatus())
    fun getSystemOptionsStatus() {
        val privilege = Privilege.status.value
        systemOptionsStatus.value = SystemOptionsStatus(
            cameraDisabled = DPM.getCameraDisabled(null),
            screenCaptureDisabled = DPM.getScreenCaptureDisabled(null),
            statusBarDisabled = if (VERSION.SDK_INT >= 34 &&
                privilege.run { device || (profile && affiliated) })
                DPM.isStatusBarDisabled else false,
            autoTimeEnabled = if (VERSION.SDK_INT >= 30 && privilege.run { device || org })
                DPM.getAutoTimeEnabled(DAR) else false,
            autoTimeZoneEnabled = if (VERSION.SDK_INT >= 30 && privilege.run { device || org })
                DPM.getAutoTimeZoneEnabled(DAR) else false,
            autoTimeRequired = if (VERSION.SDK_INT < 30) DPM.autoTimeRequired else false,
            masterVolumeMuted = DPM.isMasterVolumeMuted(DAR),
            backupServiceEnabled = if (VERSION.SDK_INT >= 26) DPM.isBackupServiceEnabled(DAR) else false,
            btContactSharingDisabled = if (VERSION.SDK_INT >= 23 && privilege.work)
                DPM.getBluetoothContactSharingDisabled(DAR) else false,
            commonCriteriaMode = if (VERSION.SDK_INT >= 30) DPM.isCommonCriteriaModeEnabled(DAR) else false,
            usbSignalEnabled = if (VERSION.SDK_INT >= 31) DPM.isUsbDataSignalingEnabled else false,
            canDisableUsbSignal = if (VERSION.SDK_INT >= 31) DPM.canUsbDataSignalingBeDisabled() else false
        )
    }
    fun setCameraDisabled(disabled: Boolean) {
        DPM.setCameraDisabled(DAR, disabled)
        createShortcuts(application)
        systemOptionsStatus.update { it.copy(cameraDisabled = DPM.getCameraDisabled(null)) }
    }
    fun setScreenCaptureDisabled(disabled: Boolean) {
        DPM.setScreenCaptureDisabled(DAR, disabled)
        systemOptionsStatus.update {
            it.copy(screenCaptureDisabled = DPM.getScreenCaptureDisabled(null))
        }
    }
    @RequiresApi(23)
    fun setStatusBarDisabled(disabled: Boolean) {
        val result = DPM.setStatusBarDisabled(DAR, disabled)
        if (result) systemOptionsStatus.update { it.copy(statusBarDisabled = disabled) }
    }
    @RequiresApi(30)
    fun setAutoTimeEnabled(enabled: Boolean) {
        DPM.setAutoTimeEnabled(DAR, enabled)
        systemOptionsStatus.update { it.copy(autoTimeEnabled = DPM.getAutoTimeEnabled(DAR)) }
    }
    @RequiresApi(30)
    fun setAutoTimeZoneEnabled(enabled: Boolean) {
        DPM.setAutoTimeZoneEnabled(DAR, enabled)
        systemOptionsStatus.update {
            it.copy(autoTimeZoneEnabled = DPM.getAutoTimeZoneEnabled(DAR))
        }
    }
    @Suppress("DEPRECATION")
    fun setAutoTimeRequired(required: Boolean) {
        DPM.setAutoTimeRequired(DAR, required)
        systemOptionsStatus.update { it.copy(autoTimeRequired = DPM.autoTimeRequired) }
    }
    fun setMasterVolumeMuted(muted: Boolean) {
        DPM.setMasterVolumeMuted(DAR, muted)
        createShortcuts(application)
        systemOptionsStatus.update { it.copy(masterVolumeMuted = DPM.isMasterVolumeMuted(DAR)) }
    }
    @RequiresApi(26)
    fun setBackupServiceEnabled(enabled: Boolean) {
        DPM.setBackupServiceEnabled(DAR, enabled)
        systemOptionsStatus.update {
            it.copy(backupServiceEnabled = DPM.isBackupServiceEnabled(DAR))
        }
    }
    @RequiresApi(23)
    fun setBtContactSharingDisabled(disabled: Boolean) {
        DPM.setBluetoothContactSharingDisabled(DAR, disabled)
        systemOptionsStatus.update {
            it.copy(btContactSharingDisabled = DPM.getBluetoothContactSharingDisabled(DAR))
        }
    }
    @RequiresApi(30)
    fun setCommonCriteriaModeEnabled(enabled: Boolean) {
        DPM.setCommonCriteriaModeEnabled(DAR, enabled)
        systemOptionsStatus.update {
            it.copy(commonCriteriaMode = DPM.isCommonCriteriaModeEnabled(DAR))
        }
    }
    @RequiresApi(31)
    fun setUsbSignalEnabled(enabled: Boolean) {
        DPM.isUsbDataSignalingEnabled = enabled
        systemOptionsStatus.update { it.copy(usbSignalEnabled = DPM.isUsbDataSignalingEnabled) }
    }
    @RequiresApi(23)
    fun setKeyguardDisabled(disabled: Boolean): Boolean {
        return DPM.setKeyguardDisabled(DAR, disabled)
    }
    fun lockScreen(evictKey: Boolean) {
        if (VERSION.SDK_INT >= 26 && Privilege.status.value.work) {
            DPM.lockNow(if (evictKey) DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY else 0)
        } else {
            DPM.lockNow()
        }
    }
    val hardwareProperties = MutableStateFlow(HardwareProperties())
    var hpRefreshInterval = 1000L
    fun setHpRefreshInterval(interval: Float) {
        hpRefreshInterval = (interval * 1000).toLong()
    }
    @RequiresApi(24)
    suspend fun getHardwareProperties() {
        val hpm = application.getSystemService(HardwarePropertiesManager::class.java)
        while (true) {
            val properties =  HardwareProperties(
                temperatureTypes.map { (type, _) ->
                    type to hpm.getDeviceTemperatures(type, HardwarePropertiesManager.TEMPERATURE_CURRENT).toList()
                }.toMap(),
                hpm.cpuUsages.map { it.active to it.total },
                hpm.fanSpeeds.toList()
            )
            if (properties.cpuUsages.isEmpty() && properties.fanSpeeds.isEmpty() &&
                properties.temperatures.isEmpty()) {
                break
            }
            delay(hpRefreshInterval)
        }
    }
    @RequiresApi(28)
    fun setTime(time: Long): Boolean {
        return DPM.setTime(DAR, time)
    }
    @RequiresApi(28)
    fun setTimeZone(tz: String): Boolean {
        return DPM.setTimeZone(DAR, tz)
    }
    @RequiresApi(36)
    fun getAutoTimePolicy(): Int {
        return DPM.autoTimePolicy
    }
    @RequiresApi(36)
    fun setAutoTimePolicy(policy: Int) {
        DPM.autoTimePolicy = policy
    }
    @RequiresApi(36)
    fun getAutoTimeZonePolicy(): Int {
        return DPM.autoTimeZonePolicy
    }
    @RequiresApi(36)
    fun setAutoTimeZonePolicy(policy: Int) {
        DPM.autoTimeZonePolicy = policy
    }
    @RequiresApi(35)
    fun getContentProtectionPolicy(): Int {
        return DPM.getContentProtectionPolicy(DAR)
    }
    @RequiresApi(35)
    fun setContentProtectionPolicy(policy: Int) {
        DPM.setContentProtectionPolicy(DAR, policy)
    }
    @RequiresApi(23)
    fun getPermissionPolicy(): Int {
        return DPM.getPermissionPolicy(DAR)
    }
    @RequiresApi(23)
    fun setPermissionPolicy(policy: Int) {
        DPM.setPermissionPolicy(DAR, policy)
    }
    @RequiresApi(34)
    fun getMtePolicy(): Int {
        return DPM.mtePolicy
    }
    @RequiresApi(34)
    fun setMtePolicy(policy: Int): Boolean {
        return try {
            DPM.mtePolicy = policy
            true
        } catch (_: UnsupportedOperationException) {
            false
        }
    }
    @RequiresApi(31)
    fun getNsAppPolicy(): Int {
        return DPM.nearbyAppStreamingPolicy
    }
    @RequiresApi(31)
    fun setNsAppPolicy(policy: Int) {
        DPM.nearbyAppStreamingPolicy = policy
    }
    @RequiresApi(31)
    fun getNsNotificationPolicy(): Int {
        return DPM.nearbyNotificationStreamingPolicy
    }
    @RequiresApi(31)
    fun setNsNotificationPolicy(policy: Int) {
        DPM.nearbyNotificationStreamingPolicy = policy
    }
    val lockTaskPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(26)
    fun getLockTaskPackages() {
        lockTaskPackages.value = DPM.getLockTaskPackages(DAR).map { getAppInfo(it) }
    }
    @RequiresApi(26)
    fun setLockTaskPackage(name: String, status: Boolean) {
        DPM.setLockTaskPackages(DAR,
            lockTaskPackages.value.map { it.name }
                .run { if (status) plus(name) else minus(name) }
                .toTypedArray()
        )
        getLockTaskPackages()
    }
    @RequiresApi(28)
    fun startLockTaskMode(packageName: String, activity: String): Int {
        if (!NotificationUtils.checkPermission(application)) return 0
        if (!DPM.isLockTaskPermitted(packageName)) return 1
        val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
        val intent = if(activity.isNotEmpty()) {
            Intent().setComponent(ComponentName(packageName, activity))
        } else PM.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent, options.toBundle())
            return 0
        } else {
            return 2
        }
    }
    @RequiresApi(28)
    fun getLockTaskFeatures(): Int {
        return DPM.getLockTaskFeatures(DAR)
    }
    @RequiresApi(28)
    fun setLockTaskFeatures(flags: Int): String? {
        try {
            DPM.setLockTaskFeatures(DAR, flags)
            return null
        } catch (e: IllegalArgumentException) {
            return e.message
        }
    }
    val installedCaCerts = MutableStateFlow(emptyList<CaCertInfo>())
    fun getCaCerts() {
        viewModelScope.launch {
            installedCaCerts.value = DPM.getInstalledCaCerts(DAR).mapNotNull { parseCaCert(it) }
        }
    }
    fun parseCaCert(uri: Uri): CaCertInfo? {
        return try {
            application.contentResolver.openInputStream(uri)?.use {
                parseCaCert(it.readBytes())
            }
        } catch(e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun parseCaCert(bytes: ByteArray): CaCertInfo? {
        val hash = MessageDigest.getInstance("SHA-256").digest(bytes).toHexString()
        return try {
            val factory = CertificateFactory.getInstance("X.509")
            val cert = factory.generateCertificate(bytes.inputStream()) as X509Certificate
            CaCertInfo(
                hash, cert.serialNumber.toString(16),
                cert.issuerX500Principal.name, cert.subjectX500Principal.name,
                parseDate(cert.notBefore), parseDate(cert.notAfter), bytes
            )
        } catch (e: CertificateException) {
            e.printStackTrace()
            null
        }
    }
    fun installCaCert(cert: CaCertInfo): Boolean {
        val result =  DPM.installCaCert(DAR, cert.bytes)
        if (result) getCaCerts()
        return result
    }
    fun uninstallCaCert(cert: CaCertInfo) {
        DPM.uninstallCaCert(DAR, cert.bytes)
        getCaCerts()
    }
    fun uninstallAllCaCerts() {
        DPM.uninstallAllUserCaCerts(DAR)
        getCaCerts()
    }
    fun exportCaCert(uri: Uri, cert: CaCertInfo) {
        application.contentResolver.openOutputStream(uri)?.use {
            it.write(cert.bytes)
        }
    }
    val mdAccountTypes = MutableStateFlow(emptyList<String>())
    fun getMdAccountTypes() {
        mdAccountTypes.value = DPM.accountTypesWithManagementDisabled?.toList() ?: emptyList()
    }
    fun setMdAccountType(type: String, disabled: Boolean) {
        DPM.setAccountManagementDisabled(DAR, type, disabled)
        getMdAccountTypes()
    }
    @RequiresApi(30)
    fun getFrpPolicy(): FrpPolicyInfo {
        return try {
            val policy = DPM.getFactoryResetProtectionPolicy(DAR)
            FrpPolicyInfo(
                true, policy != null, policy?.isFactoryResetProtectionEnabled ?: false,
                policy?.factoryResetProtectionAccounts ?: emptyList()
            )
        } catch (_: UnsupportedOperationException) {
            FrpPolicyInfo(false, false, false, emptyList())
        }
    }
    @RequiresApi(30)
    fun setFrpPolicy(info: FrpPolicyInfo) {
        val policy = if (info.usePolicy) {
            FactoryResetProtectionPolicy.Builder()
                .setFactoryResetProtectionEnabled(info.enabled)
                .setFactoryResetProtectionAccounts(info.accounts)
                .build()
        } else null
        DPM.setFactoryResetProtectionPolicy(DAR, policy)
    }
    fun wipeData(wipeDevice: Boolean, flags: Int, reason: String) {
        if (wipeDevice && VERSION.SDK_INT >= 34) {
            DPM.wipeDevice(flags)
        } else {
            if(VERSION.SDK_INT >= 28 && reason.isNotEmpty()) {
                DPM.wipeData(flags, reason)
            } else {
                DPM.wipeData(flags)
            }
        }
    }
    @RequiresApi(23)
    fun getSystemUpdatePolicy(): SystemUpdatePolicyInfo {
        val policy = DPM.systemUpdatePolicy
        return SystemUpdatePolicyInfo(
            policy?.policyType ?: -1, policy?.installWindowStart ?: 0, policy?.installWindowEnd ?: 0
        )
    }
    @RequiresApi(23)
    fun setSystemUpdatePolicy(info: SystemUpdatePolicyInfo) {
        val policy = when (info.type) {
            SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC -> SystemUpdatePolicy.createAutomaticInstallPolicy()
            SystemUpdatePolicy.TYPE_INSTALL_WINDOWED ->
                SystemUpdatePolicy.createWindowedInstallPolicy(info.start, info.end)
            SystemUpdatePolicy.TYPE_POSTPONE -> SystemUpdatePolicy.createPostponeInstallPolicy()
            else -> null
        }
        DPM.setSystemUpdatePolicy(DAR, policy)
    }
    @RequiresApi(26)
    fun getPendingSystemUpdate(): PendingSystemUpdateInfo {
        val update = DPM.getPendingSystemUpdate(DAR)
        return PendingSystemUpdateInfo(update != null, update?.receivedTime ?: 0,
            update?.securityPatchState == SystemUpdateInfo.SECURITY_PATCH_STATE_TRUE)
    }
    @RequiresApi(29)
    fun installSystemUpdate(uri: Uri, callback: (String) -> Unit) {
        val callback = object: InstallSystemUpdateCallback() {
            override fun onInstallUpdateError(errorCode: Int, errorMessage: String) {
                super.onInstallUpdateError(errorCode, errorMessage)
                val errDetail = when(errorCode) {
                    UPDATE_ERROR_BATTERY_LOW -> R.string.battery_low
                    UPDATE_ERROR_UPDATE_FILE_INVALID -> R.string.update_file_invalid
                    UPDATE_ERROR_INCORRECT_OS_VERSION -> R.string.incorrect_os_ver
                    UPDATE_ERROR_FILE_NOT_FOUND -> R.string.file_not_exist
                    else -> R.string.unknown_error
                }
                callback(application.getString(errDetail) + "\n$errorMessage")
            }
        }
        DPM.installSystemUpdate(DAR, uri, application.mainExecutor, callback)
    }

    @RequiresApi(24)
    fun isCreatingWorkProfileAllowed(): Boolean {
        return DPM.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
    }
    fun activateDoByShizuku(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            useShizuku(application) { service ->
                try {
                    val result = IUserService.Stub.asInterface(service)
                        .execute(ACTIVATE_DEVICE_OWNER_COMMAND)
                    if (result == null || result.getInt("code", -1) != 0) {
                        callback(false, null)
                    } else {
                        Privilege.updateStatus()
                        callback(
                            true, result.getString("output") + "\n" + result.getString("error")
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback(false, null)
                }
            }
        }
    }
    fun activateDoByRoot(callback: (Boolean, String?) -> Unit) {
        Shell.getShell { shell ->
            if(shell.isRoot) {
                val result = Shell.cmd(ACTIVATE_DEVICE_OWNER_COMMAND).exec()
                val output = result.out.joinToString("\n") + "\n" + result.err.joinToString("\n")
                Privilege.updateStatus()
                callback(result.isSuccess, output)
            } else {
                callback(false, application.getString(R.string.permission_denied))
            }
        }
    }
    @RequiresApi(28)
    fun activateDoByDhizuku(callback: (Boolean, String?) -> Unit) {
        DPM.transferOwnership(DAR, MyAdminComponent, null)
        SP.dhizuku = false
        Privilege.initialize(application)
        callback(true, null)
    }
    fun activateDhizukuMode(callback: (Boolean, String?) -> Unit) {
        fun onSucceed() {
            SP.dhizuku = true
            Privilege.initialize(application)
            callback(true, null)
        }
        if (Dhizuku.init(application)) {
            if (Dhizuku.isPermissionGranted()) {
                onSucceed()
            } else {
                Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                    override fun onRequestPermission(grantResult: Int) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) onSucceed()
                    }
                })
            }
        } else {
            callback(false, application.getString(R.string.failed_to_init_dhizuku))
        }
    }
    fun clearDeviceOwner() {
        DPM.clearDeviceOwnerApp(application.packageName)
    }
    @RequiresApi(24)
    fun clearProfileOwner() {
        DPM.clearProfileOwner(MyAdminComponent)
    }
    fun deactivateDhizukuMode() {
        SP.dhizuku = false
        Privilege.initialize(application)
    }
    val dhizukuClients = MutableStateFlow(emptyList<Pair<DhizukuClientInfo, AppInfo>>())
    fun getDhizukuClients() {
        viewModelScope.launch {
            dhizukuClients.value = myRepo.getDhizukuClients().mapNotNull {
                val packageName = PM.getNameForUid(it.uid)
                if (packageName == null) {
                    myRepo.deleteDhizukuClient(it)
                    null
                } else {
                    it to getAppInfo(packageName)
                }
            }
        }
    }
    fun getDhizukuServerEnabled(): Boolean {
        return SP.dhizukuServer
    }
    fun setDhizukuServerEnabled(status: Boolean) {
        SP.dhizukuServer = status
    }
    fun updateDhizukuClient(info: DhizukuClientInfo) {
        myRepo.setDhizukuClient(info)
        dhizukuClients.update { list ->
            val ml = list.toMutableList()
            val index = ml.indexOfFirst { it.first.uid == info.uid }
            ml[index] = info to ml[index].second
            ml
        }
    }
    @RequiresApi(24)
    fun getLockScreenInfo(): String {
        return DPM.deviceOwnerLockScreenInfo?.toString() ?: ""
    }
    @RequiresApi(24)
    fun setLockScreenInfo(text: String) {
        DPM.setDeviceOwnerLockScreenInfo(DAR, text)
    }
    val delegatedAdmins = MutableStateFlow(emptyList<DelegatedAdmin>())
    @RequiresApi(26)
    fun getDelegatedAdmins() {
        val list = mutableListOf<DelegatedAdmin>()
        delegatedScopesList.forEach { scope ->
            DPM.getDelegatePackages(DAR, scope.id)?.forEach { pkg ->
                val index = list.indexOfFirst { it.app.name == pkg }
                if (index == -1) {
                    list += DelegatedAdmin(getAppInfo(pkg), listOf(scope.id))
                } else {
                    list[index] = DelegatedAdmin(list[index].app, list[index].scopes + scope.id)
                }
            }
        }
        delegatedAdmins.value = list
    }
    @RequiresApi(26)
    fun setDelegatedAdmin(name: String, scopes: List<String>) {
        DPM.setDelegatedScopes(DAR, name, scopes)
        getDelegatedAdmins()
    }
    @RequiresApi(34)
    fun getDeviceFinanced(): Boolean {
        return DPM.isDeviceFinanced
    }
    @RequiresApi(33)
    fun getDpmRh(): String? {
        return DPM.devicePolicyManagementRoleHolderPackage
    }
    fun getStorageEncryptionStatus(): Int {
        return DPM.storageEncryptionStatus
    }
    @RequiresApi(28)
    fun getDeviceIdAttestationSupported(): Boolean {
        return DPM.isDeviceIdAttestationSupported
    }
    @RequiresApi(30)
    fun getUniqueDeviceAttestationSupported(): Boolean {
        return DPM.isUniqueDeviceAttestationSupported
    }
    fun getActiveAdmins(): String {
        return DPM.activeAdmins?.joinToString("\n") {
            it.flattenToShortString()
        } ?: application.getString(R.string.none)
    }
    @RequiresApi(24)
    fun getShortSupportMessage(): String {
        return DPM.getShortSupportMessage(DAR)?.toString() ?: ""
    }
    @RequiresApi(24)
    fun getLongSupportMessage(): String {
        return DPM.getLongSupportMessage(DAR)?.toString() ?: ""
    }
    @RequiresApi(24)
    fun setShortSupportMessage(text: String?) {
        DPM.setShortSupportMessage(DAR, text)
    }
    @RequiresApi(24)
    fun setLongSupportMessage(text: String?) {
        DPM.setLongSupportMessage(DAR, text)
    }
    val deviceAdminReceivers = MutableStateFlow(emptyList<DeviceAdmin>())
    fun getDeviceAdminReceivers() {
        viewModelScope.launch {
            deviceAdminReceivers.value = PM.queryBroadcastReceivers(
                Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                PackageManager.GET_META_DATA
            ).mapNotNull {
                try {
                    DeviceAdminInfo(application, it)
                } catch(_: Exception) {
                    null
                }
            }.filter {
                it.isVisible && it.packageName != "com.bintianqi.owndroid" &&
                        it.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }.map {
                DeviceAdmin(getAppInfo(it.packageName), it.component)
            }
        }
    }
    @RequiresApi(28)
    fun transferOwnership(component: ComponentName) {
        DPM.transferOwnership(DAR, component, null)
        Privilege.updateStatus()
    }
}

data class ThemeSettings(
    val materialYou: Boolean = false,
    val darkTheme: Int = -1,
    val blackTheme: Boolean = false
)
