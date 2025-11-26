package com.bintianqi.owndroid

import android.accounts.Account
import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.Application
import android.app.KeyguardManager
import android.app.PendingIntent
import android.app.admin.DeviceAdminInfo
import android.app.admin.DeviceAdminReceiver
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.InstallSystemUpdateCallback
import android.app.admin.FactoryResetProtectionPolicy
import android.app.admin.IDevicePolicyManager
import android.app.admin.PackagePolicy
import android.app.admin.PreferentialNetworkServiceConfig
import android.app.admin.SecurityLog
import android.app.admin.SystemUpdateInfo
import android.app.admin.SystemUpdatePolicy
import android.app.admin.WifiSsidPolicy
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.RestrictionEntry
import android.content.RestrictionsManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.IpConfiguration
import android.net.LinkAddress
import android.net.ProxyInfo
import android.net.StaticIpConfiguration
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiSsid
import android.os.Binder
import android.os.Build.VERSION
import android.os.Bundle
import android.os.HardwarePropertiesManager
import android.os.UserHandle
import android.os.UserManager
import android.telephony.data.ApnSetting
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.Privilege.DAR
import com.bintianqi.owndroid.Privilege.DPM
import com.bintianqi.owndroid.dpm.ACTIVATE_DEVICE_OWNER_COMMAND
import com.bintianqi.owndroid.dpm.ApnAuthType
import com.bintianqi.owndroid.dpm.ApnConfig
import com.bintianqi.owndroid.dpm.ApnMvnoType
import com.bintianqi.owndroid.dpm.ApnProtocol
import com.bintianqi.owndroid.dpm.AppGroup
import com.bintianqi.owndroid.dpm.AppRestriction
import com.bintianqi.owndroid.dpm.AppStatus
import com.bintianqi.owndroid.dpm.CaCertInfo
import com.bintianqi.owndroid.dpm.CreateUserResult
import com.bintianqi.owndroid.dpm.CreateWorkProfileOptions
import com.bintianqi.owndroid.dpm.DelegatedAdmin
import com.bintianqi.owndroid.dpm.DeviceAdmin
import com.bintianqi.owndroid.dpm.FrpPolicyInfo
import com.bintianqi.owndroid.dpm.HardwareProperties
import com.bintianqi.owndroid.dpm.IntentFilterDirection
import com.bintianqi.owndroid.dpm.IntentFilterOptions
import com.bintianqi.owndroid.dpm.IpMode
import com.bintianqi.owndroid.dpm.KeyguardDisableConfig
import com.bintianqi.owndroid.dpm.KeyguardDisableMode
import com.bintianqi.owndroid.dpm.NetworkStatsData
import com.bintianqi.owndroid.dpm.NetworkStatsTarget
import com.bintianqi.owndroid.dpm.PasswordComplexity
import com.bintianqi.owndroid.dpm.PendingSystemUpdateInfo
import com.bintianqi.owndroid.dpm.PreferentialNetworkServiceInfo
import com.bintianqi.owndroid.dpm.PrivateDnsConfiguration
import com.bintianqi.owndroid.dpm.PrivateDnsMode
import com.bintianqi.owndroid.dpm.ProxyMode
import com.bintianqi.owndroid.dpm.ProxyType
import com.bintianqi.owndroid.dpm.QueryNetworkStatsParams
import com.bintianqi.owndroid.dpm.RecommendedProxyConf
import com.bintianqi.owndroid.dpm.RpTokenState
import com.bintianqi.owndroid.dpm.SsidPolicy
import com.bintianqi.owndroid.dpm.SsidPolicyType
import com.bintianqi.owndroid.dpm.SystemOptionsStatus
import com.bintianqi.owndroid.dpm.SystemUpdatePolicyInfo
import com.bintianqi.owndroid.dpm.UserIdentifier
import com.bintianqi.owndroid.dpm.UserInformation
import com.bintianqi.owndroid.dpm.UserOperationType
import com.bintianqi.owndroid.dpm.WifiInfo
import com.bintianqi.owndroid.dpm.WifiSecurity
import com.bintianqi.owndroid.dpm.WifiStatus
import com.bintianqi.owndroid.dpm.activateOrgProfileCommand
import com.bintianqi.owndroid.dpm.delegatedScopesList
import com.bintianqi.owndroid.dpm.doUserOperationWithContext
import com.bintianqi.owndroid.dpm.getPackageInstaller
import com.bintianqi.owndroid.dpm.handlePrivilegeChange
import com.bintianqi.owndroid.dpm.isValidPackageName
import com.bintianqi.owndroid.dpm.parsePackageInstallerMessage
import com.bintianqi.owndroid.dpm.runtimePermissions
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
import kotlinx.coroutines.withContext
import java.net.InetAddress
import java.security.MessageDigest
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.Executors
import kotlin.reflect.jvm.jvmErasure

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
    fun getDisplayDangerousFeatures(): Boolean {
        return SP.displayDangerousFeatures
    }
    fun getShortcutsEnabled(): Boolean {
        return SP.shortcuts
    }
    fun setDisplayDangerousFeatures(state: Boolean) {
        SP.displayDangerousFeatures = state
    }
    fun setShortcutsEnabled(enabled: Boolean) {
        SP.shortcuts = enabled
        ShortcutUtils.setAllShortcuts(application, enabled)
    }
    fun getAppLockConfig(): AppLockConfig {
        val passwordHash = SP.lockPasswordHash
        return AppLockConfig(passwordHash?.ifEmpty { null }, SP.biometricsUnlock, SP.lockWhenLeaving)
    }
    fun setAppLockConfig(config: AppLockConfig) {
        if (config.password == null) {
            SP.lockPasswordHash = ""
        } else if (!config.password.isEmpty()) {
            SP.lockPasswordHash = config.password.hash()
        }
        SP.biometricsUnlock = config.biometrics
        SP.lockWhenLeaving = config.whenLeaving
    }
    fun getApiEnabled(): Boolean {
        return SP.apiKeyHash?.isNotEmpty() ?: false
    }
    fun setApiKey(key: String) {
        SP.apiKeyHash = if (key.isEmpty()) "" else key.hash()
    }
    val enabledNotifications = MutableStateFlow(emptyList<Int>())
    fun getEnabledNotifications() {
        val list = SP.notifications?.split(',')?.mapNotNull { it.toIntOrNull() }
        enabledNotifications.value = list ?: NotificationType.entries.map { it.id }
    }
    fun setNotificationEnabled(type: NotificationType, enabled: Boolean) {
        enabledNotifications.update { list ->
            if (enabled) list.plus(type.id) else list.minus(type.id)
        }
        SP.notifications = enabledNotifications.value.joinToString(",") { it.toString() }
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
    fun onPackageRemoved(name: String) {
        installedPackages.update { list ->
            list.filter { it.name != name }
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
        hiddenPackages.value = PM.getInstalledApplications(getInstalledAppsFlags).filter {
            DPM.isApplicationHidden(DAR, it.packageName)
        }.map { getAppInfo(it) }
    }
    fun setPackageHidden(name: String, status: Boolean): Boolean {
        val result = DPM.setApplicationHidden(DAR, name, status)
        getHiddenPackages()
        return result
    }

    // Uninstall blocked packages
    val ubPackages = MutableStateFlow(emptyList<AppInfo>())
    fun getUbPackages() {
        ubPackages.value = PM.getInstalledApplications(getInstalledAppsFlags).filter {
            DPM.isUninstallBlocked(DAR, it.packageName)
        }.map { getAppInfo(it) }
    }
    fun setPackageUb(name: String, status: Boolean) {
        DPM.setUninstallBlocked(DAR, name, status)
        getUbPackages()
    }

    // User control disabled packages
    val ucdPackages = MutableStateFlow(emptyList<AppInfo>())
    @RequiresApi(30)
    fun getUcdPackages() {
        ucdPackages.value = DPM.getUserControlDisabledPackages(DAR).distinct().map {
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
            packagePermissions.value = runtimePermissions.associate {
                it.id to DPM.getPermissionGrantState(DAR, name, it.id)
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
        mddPackages.value = DPM.getMeteredDataDisabledPackages(DAR).distinct().map { getAppInfo(it) }
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
        kuPackages.value = DPM.getKeepUninstalledPackages(DAR)?.distinct()?.map { getAppInfo(it) } ?: emptyList()
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
        cpwProviders.value = DPM.getCrossProfileWidgetProviders(DAR).distinct().map { getAppInfo(it) }
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
                    if (statusExtra == PackageInstaller.STATUS_SUCCESS) {
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
            cmPackages.value = policy.packageNames.distinct().map { getAppInfo(it) }
            policy.policyType
        } ?: -1
    }
    fun setCmPackage(name: String, status: Boolean) {
        cmPackages.update { list ->
            if (status) list + getAppInfo(name) else list.filter { it.name != name }
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
            pimPackages.value = packages?.distinct()?.map { getAppInfo(it) } ?: emptyList()
            packages == null
        }
    }
    fun setPimPackage(name: String, status: Boolean) {
        pimPackages.update { packages ->
            if (status) packages + getAppInfo(name) else packages.filter { it.name != name }
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
            pasPackages.value = packages?.distinct()?.map { getAppInfo(it) } ?: emptyList()
            packages == null
        }
    }
    fun setPasPackage(name: String, status: Boolean) {
        pasPackages.update { packages ->
            if (status) packages + getAppInfo(name) else packages.filter { it.name != name }
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
            if (VERSION.SDK_INT >= 28 && Privilege.status.value.device)
                DPM.getKeepUninstalledPackages(DAR)?.contains(name) == true
            else false
        )
    }
    // Application details
    @RequiresApi(24)
    fun adSetPackageSuspended(name: String, status: Boolean) {
        try {
            DPM.setPackagesSuspended(DAR, arrayOf(name), status)
            appStatus.update { it.copy(suspend = DPM.isPackageSuspended(DAR, name)) }
        } catch (_: Exception) {}
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

    val appRestrictions = MutableStateFlow(emptyList<AppRestriction>())

    @RequiresApi(23)
    fun getAppRestrictions(name: String) {
        val rm = application.getSystemService(RestrictionsManager::class.java)
        try {
            val bundle = DPM.getApplicationRestrictions(DAR, name)
            appRestrictions.value = rm.getManifestRestrictions(name)?.mapNotNull {
                transformRestrictionEntry(it)
            }?.map {
                if (bundle.containsKey(it.key)) {
                    when (it) {
                        is AppRestriction.BooleanItem -> it.value = bundle.getBoolean(it.key)
                        is AppRestriction.StringItem -> it.value = bundle.getString(it.key)
                        is AppRestriction.IntItem -> it.value = bundle.getInt(it.key)
                        is AppRestriction.ChoiceItem -> it.value = bundle.getString(it.key)
                        is AppRestriction.MultiSelectItem -> it.value = bundle.getStringArray(it.key)
                    }
                }
                it
            } ?: emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(23)
    fun setAppRestrictions(name: String, item: AppRestriction) {
        viewModelScope.launch(Dispatchers.IO) {
            val bundle = transformAppRestriction(
                appRestrictions.value.filter { it.key != item.key }.plus(item)
            )
            DPM.setApplicationRestrictions(DAR, name, bundle)
            getAppRestrictions(name)
        }
    }

    @RequiresApi(23)
    fun clearAppRestrictions(name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            DPM.setApplicationRestrictions(DAR, name, Bundle())
            getAppRestrictions(name)
        }
    }

    fun transformRestrictionEntry(e: RestrictionEntry): AppRestriction? {
        return when (e.type) {
            RestrictionEntry.TYPE_INTEGER ->
                AppRestriction.IntItem(e.key, e.title, e.description, null)
            RestrictionEntry.TYPE_STRING ->
                AppRestriction.StringItem(e.key, e.title, e.description, null)
            RestrictionEntry.TYPE_BOOLEAN ->
                AppRestriction.BooleanItem(e.key, e.title, e.description, null)
            RestrictionEntry.TYPE_CHOICE -> AppRestriction.ChoiceItem(e.key, e.title,
                e.description, e.choiceEntries, e.choiceValues, null)
            RestrictionEntry.TYPE_MULTI_SELECT -> AppRestriction.MultiSelectItem(e.key, e.title,
                e.description, e.choiceEntries, e.choiceValues, null)
            else -> null
        }
    }

    fun transformAppRestriction(list: List<AppRestriction>): Bundle {
        val b = Bundle()
        for (r in list) {
            when (r) {
                is AppRestriction.IntItem -> r.value?.let { b.putInt(r.key, it) }
                is AppRestriction.StringItem -> r.value?.let { b.putString(r.key, it) }
                is AppRestriction.BooleanItem -> r.value?.let { b.putBoolean(r.key, it) }
                is AppRestriction.ChoiceItem -> r.value?.let { b.putString(r.key, it) }
                is AppRestriction.MultiSelectItem -> r.value?.let { b.putStringArray(r.key, r.value) }
            }
        }
        return b
    }

    val appGroups = MutableStateFlow(emptyList<AppGroup>())
    init {
        getAppGroups()
    }
    fun getAppGroups() {
        appGroups.value = myRepo.getAppGroups()
    }
    fun setAppGroup(id: Int?, name: String, apps: List<String>) {
        myRepo.setAppGroup(id, name, apps)
        getAppGroups()
    }
    fun deleteAppGroup(id: Int) {
        myRepo.deleteAppGroup(id)
        appGroups.update { group ->
            group.filter { it.id != id }
        }
    }

    @RequiresApi(24)
    fun reboot() {
        DPM.reboot(DAR)
    }
    @RequiresApi(24)
    fun requestBugReport(): Boolean {
        return try {
            DPM.requestBugreport(DAR)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    @SuppressLint("PrivateApi")
    @RequiresApi(24)
    fun getOrgName(): String {
        return try {
            DPM.getOrganizationName(DAR)?.toString() ?: ""
        } catch (_: Exception) {
            try {
                val method = DevicePolicyManager::class.java.getDeclaredMethod(
                    "getDeviceOwnerOrganizationName"
                )
                method.isAccessible = true
                (method.invoke(DPM) as CharSequence).toString()
            } catch (_: Exception) {
                ""
            }
        }
    }
    @RequiresApi(24)
    fun setOrgName(name: String) {
        DPM.setOrganizationName(DAR, name)
    }
    @RequiresApi(31)
    fun setOrgId(id: String): Boolean {
        return try {
            DPM.setOrganizationId(id)
            true
        } catch (_: IllegalStateException) {
            false
        }
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
        ShortcutUtils.setShortcut(application, MyShortcut.DisableCamera, !disabled)
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
        ShortcutUtils.setShortcut(application, MyShortcut.Mute, !muted)
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
            hardwareProperties.value = properties
            delay(hpRefreshInterval)
        }
    }
    @RequiresApi(28)
    fun setTime(time: Long, useCurrentTz: Boolean): Boolean {
        val offset = if (useCurrentTz) {
            ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds * 1000L
        } else 0L
        return DPM.setTime(DAR, time - offset)
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
    fun startLockTaskMode(packageName: String, activity: String): Boolean {
        if (!DPM.isLockTaskPermitted(packageName)) {
            val list = lockTaskPackages.value.map { it.name } + packageName
            DPM.setLockTaskPackages(DAR, list.toTypedArray())
            getLockTaskPackages()
        }
        val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
        val intent = if(activity.isNotEmpty()) {
            Intent().setComponent(ComponentName(packageName, activity))
        } else PM.getLaunchIntentForPackage(packageName)
        if (intent != null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            application.startActivity(intent, options.toBundle())
            return true
        } else {
            return false
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
    val selectedCaCert = MutableStateFlow<CaCertInfo?>(null)
    fun getCaCerts() {
        installedCaCerts.value = DPM.getInstalledCaCerts(DAR).mapNotNull { parseCaCert(it) }
    }
    fun selectCaCert(cert: CaCertInfo) {
        selectedCaCert.value = cert
    }
    fun parseCaCert(uri: Uri) {
        try {
            application.contentResolver.openInputStream(uri)?.use {
                selectedCaCert.value = parseCaCert(it.readBytes())
            }
        } catch(e: Exception) {
            e.printStackTrace()
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
                cert.notBefore.time, cert.notAfter.time, bytes
            )
        } catch (e: CertificateException) {
            e.printStackTrace()
            null
        }
    }
    fun installCaCert(): Boolean {
        val result =  DPM.installCaCert(DAR, selectedCaCert.value!!.bytes)
        if (result) getCaCerts()
        return result
    }
    fun uninstallCaCert() {
        DPM.uninstallCaCert(DAR, selectedCaCert.value!!.bytes)
        getCaCerts()
    }
    fun uninstallAllCaCerts() {
        DPM.uninstallAllUserCaCerts(DAR)
        getCaCerts()
    }
    fun exportCaCert(uri: Uri) {
        application.contentResolver.openOutputStream(uri)?.use {
            it.write(selectedCaCert.value!!.bytes)
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
    fun getSecurityLoggingEnabled(): Boolean {
        return DPM.isSecurityLoggingEnabled(DAR)
    }
    @RequiresApi(24)
    fun setSecurityLoggingEnabled(enabled: Boolean) {
        DPM.setSecurityLoggingEnabled(DAR, enabled)
    }
    fun exportSecurityLogs(uri: Uri, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            application.contentResolver.openOutputStream(uri)?.use {
                myRepo.exportSecurityLogs(it)
            }
            withContext(Dispatchers.Main) {
                callback()
            }
        }
    }
    fun getSecurityLogsCount(): Int {
        return myRepo.getSecurityLogsCount().toInt()
    }
    fun deleteSecurityLogs() {
        myRepo.deleteSecurityLogs()
    }
    var preRebootSecurityLogs = emptyList<SecurityLog.SecurityEvent>()
    @RequiresApi(24)
    fun getPreRebootSecurityLogs(): Boolean {
        if (preRebootSecurityLogs.isNotEmpty()) return true
        return try {
            val logs = DPM.retrievePreRebootSecurityLogs(DAR)
            if (logs != null && logs.isNotEmpty()) {
                preRebootSecurityLogs = logs
                true
            } else false
        } catch (_: SecurityException) {
            false
        }
    }
    @RequiresApi(24)
    fun exportPreRebootSecurityLogs(uri: Uri, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val stream = application.contentResolver.openOutputStream(uri) ?: return@launch
            myRepo.exportPRSecurityLogs(preRebootSecurityLogs, stream)
            stream.close()
            withContext(Dispatchers.Main) { callback() }
        }
    }

    @RequiresApi(24)
    fun isCreatingWorkProfileAllowed(): Boolean {
        return DPM.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
    }
    fun activateDoByShizuku(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            useShizuku(application) { service ->
                try {
                    val result = IUserService.Stub.asInterface(service)
                        .execute(ACTIVATE_DEVICE_OWNER_COMMAND)
                    if (result == null || result.getInt("code", -1) != 0) {
                        callback(false, null)
                    } else {
                        Privilege.updateStatus()
                        handlePrivilegeChange(application)
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
                if (result.isSuccess) {
                    Privilege.updateStatus()
                    handlePrivilegeChange(application)
                }
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
        handlePrivilegeChange(application)
        callback(true, null)
    }
    fun activateDhizukuMode(callback: (Boolean, String?) -> Unit) {
        fun onSucceed() {
            SP.dhizuku = true
            Privilege.initialize(application)
            handlePrivilegeChange(application)
            callback(true, null)
        }
        if (Dhizuku.init(application)) {
            if (Dhizuku.isPermissionGranted()) {
                onSucceed()
            } else {
                Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                    override fun onRequestPermission(grantResult: Int) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) onSucceed()
                        else callback(false, application.getString(R.string.dhizuku_permission_not_granted))
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
        viewModelScope.launch(Dispatchers.IO) {
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
        viewModelScope.launch(Dispatchers.IO) {
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
    val userRestrictions = MutableStateFlow(emptyMap<String, Boolean>())
    @RequiresApi(24)
    fun getUserRestrictions() {
        val bundle = DPM.getUserRestrictions(DAR)
        userRestrictions.value = bundle.keySet().associateWith { bundle.getBoolean(it) }
    }
    fun setUserRestriction(name: String, state: Boolean): Boolean {
        return try {
            if (state) {
                DPM.addUserRestriction(DAR, name)
            } else {
                DPM.clearUserRestriction(DAR, name)
            }
            userRestrictions.update { it.plus(name to state) }
            ShortcutUtils.updateUserRestrictionShortcut(application, name, !state, true)
            true
        } catch (_: SecurityException) {
            false
        }
    }
    fun createUserRestrictionShortcut(id: String): Boolean {
        return ShortcutUtils.setUserRestrictionShortcut(
            application, id, userRestrictions.value[id] ?: true
        )
    }
    fun createWorkProfile(options: CreateWorkProfileOptions): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        if (VERSION.SDK_INT >= 23) {
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
                MyAdminComponent
            )
        } else {
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,
                application.packageName
            )
        }
        if (options.migrateAccount && VERSION.SDK_INT >= 22) {
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE,
                Account(options.accountName, options.accountType)
            )
            if (VERSION.SDK_INT >= 26) {
                intent.putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION,
                    options.keepAccount
                )
            }
        }
        if (VERSION.SDK_INT >= 24) {
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION,
                options.skipEncrypt
            )
        }
        if (VERSION.SDK_INT >= 33) {
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE, options.offline)
        }
        return intent
    }
    fun activateOrgProfileByShizuku(callback: (Boolean) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            var succeed = false
            useShizuku(application) { service ->
                val result = IUserService.Stub.asInterface(service).execute(activateOrgProfileCommand)
                succeed = result?.getInt("code", -1) == 0
                callback(succeed)
            }
            if (succeed) Privilege.updateStatus()
        }
    }
    @RequiresApi(30)
    fun getPersonalAppsSuspendedReason(): Int {
        return DPM.getPersonalAppsSuspendedReasons(DAR)
    }
    @RequiresApi(30)
    fun setPersonalAppsSuspended(suspended: Boolean) {
        DPM.setPersonalAppsSuspended(DAR, suspended)
    }
    @RequiresApi(30)
    fun getProfileMaxTimeOff(): Long {
        return DPM.getManagedProfileMaximumTimeOff(DAR)
    }
    @RequiresApi(30)
    fun setProfileMaxTimeOff(time: Long) {
        DPM.setManagedProfileMaximumTimeOff(DAR, time)
    }
    fun addCrossProfileIntentFilter(options: IntentFilterOptions) {
        val filter = IntentFilter(options.action)
        if (options.category.isNotEmpty()) filter.addCategory(options.category)
        if (options.mimeType.isNotEmpty()) filter.addDataType(options.mimeType)
        val flags = when(options.direction) {
            IntentFilterDirection.ToManaged -> DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED
            IntentFilterDirection.ToParent -> DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
            IntentFilterDirection.Both -> DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED or
                    DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
        }
        DPM.addCrossProfileIntentFilter(DAR, filter, flags)
    }

    val UM = application.getSystemService(Context.USER_SERVICE) as UserManager
    @RequiresApi(28)
    fun getLogoutEnabled(): Boolean {
        return DPM.isLogoutEnabled
    }
    @RequiresApi(28)
    fun setLogoutEnabled(enabled: Boolean) {
        DPM.setLogoutEnabled(DAR, enabled)
    }
    fun getUserInformation(): UserInformation {
        val uh = Binder.getCallingUserHandle()
        return UserInformation(
            if (VERSION.SDK_INT >= 24) UserManager.supportsMultipleUsers() else false,
            if (VERSION.SDK_INT >= 31) UserManager.isHeadlessSystemUserMode() else false,
            if (VERSION.SDK_INT >= 23) UM.isSystemUser else false,
            if (VERSION.SDK_INT >= 34) UM.isAdminUser else false,
            if (VERSION.SDK_INT >= 25) UM.isDemoUser else false,
            if (VERSION.SDK_INT >= 23) UM.getUserCreationTime(uh) else 0,
            if (VERSION.SDK_INT >= 28) DPM.isLogoutEnabled else false,
            if (VERSION.SDK_INT >= 28) DPM.isEphemeralUser(DAR) else false,
            if (VERSION.SDK_INT >= 28) DPM.isAffiliatedUser else false,
            UM.getSerialNumberForUser(uh)
        )
    }
    @Suppress("PrivateApi")
    @RequiresApi(28)
    fun getUserIdentifiers(): List<UserIdentifier> {
        return DPM.getSecondaryUsers(DAR)?.mapNotNull {
            try {
                val field = UserHandle::class.java.getDeclaredField("mHandle")
                field.isAccessible = true
                UserIdentifier(field.get(it) as Int, UM.getSerialNumberForUser(it))
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        } ?: emptyList()
    }
    fun doUserOperation(type: UserOperationType, id: Int, isUserId: Boolean): Boolean {
        return doUserOperationWithContext(application, type, id, isUserId)
    }
    fun createUserOperationShortcut(type: UserOperationType, id: Int, isUserId: Boolean): Boolean {
        val serial = if (isUserId && VERSION.SDK_INT >= 24) {
            UM.getSerialNumberForUser(UserHandle.getUserHandleForUid(id * 100000))
        } else id
        return ShortcutUtils.setUserOperationShortcut(application, type, serial.toInt())
    }
    fun getUserOperationResultText(code: Int): Int {
        return when (code) {
            UserManager.USER_OPERATION_SUCCESS -> R.string.success
            UserManager.USER_OPERATION_ERROR_UNKNOWN -> R.string.unknown_error
            UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE-> R.string.fail_managed_profile
            UserManager.USER_OPERATION_ERROR_MAX_RUNNING_USERS -> R.string.limit_reached
            UserManager.USER_OPERATION_ERROR_CURRENT_USER -> R.string.fail_current_user
            else -> R.string.unknown
        }
    }
    @RequiresApi(24)
    fun createUser(name: String, flags: Int, callback: (CreateUserResult) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val uh = DPM.createAndManageUser(DAR, name, DAR, null, flags)
                if (uh == null) {
                    callback(CreateUserResult(R.string.failed))
                } else {
                    callback(CreateUserResult(R.string.succeeded, UM.getSerialNumberForUser(uh)))
                }
            } catch (e: Exception) {
                e.printStackTrace()
                if (VERSION.SDK_INT >= 28 && e is UserManager.UserOperationException) {
                    callback(CreateUserResult(getUserOperationResultText(e.userOperationResult)))
                } else {
                    callback(CreateUserResult(R.string.error))
                }
            }
        }
    }
    val affiliationIds = MutableStateFlow(emptyList<String>())
    @RequiresApi(26)
    fun getAffiliationIds() {
        affiliationIds.value = DPM.getAffiliationIds(DAR).toList()
    }
    @RequiresApi(26)
    fun setAffiliationId(id: String, state: Boolean) {
        val newList = affiliationIds.value.run { if (state) plus(id) else minus(id) }
        DPM.setAffiliationIds(DAR, newList.toSet())
        affiliationIds.value = newList
    }
    fun setProfileName(name: String) {
        DPM.setProfileName(DAR, name)
    }
    @RequiresApi(23)
    fun setUserIcon(bitmap: Bitmap) {
        DPM.setUserIcon(DAR, bitmap)
    }
    @RequiresApi(28)
    fun getUserSessionMessages(): Pair<String, String> {
        return (DPM.getStartUserSessionMessage(DAR)?.toString() ?: "") to
                (DPM.getEndUserSessionMessage(DAR)?.toString() ?: "")
    }
    @RequiresApi(28)
    fun setStartUserSessionMessage(message: String?) {
        DPM.setStartUserSessionMessage(DAR, message)
    }
    @RequiresApi(28)
    fun setEndUserSessionMessage(message: String?) {
        DPM.setEndUserSessionMessage(DAR, message)
    }
    @RequiresApi(28)
    fun logoutUser(): Int {
        return getUserOperationResultText(DPM.logoutUser(DAR))
    }

    val WM = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
    // Lockdown admin configured networks
    @RequiresApi(30)
    fun getLanEnabled(): Boolean {
        return DPM.hasLockdownAdminConfiguredNetworks(DAR)
    }
    @RequiresApi(30)
    fun setLanEnabled(state: Boolean) {
        DPM.setConfiguredNetworksLockdownState(DAR, state)
    }
    fun setWifiEnabled(enabled: Boolean): Boolean {
        return WM.setWifiEnabled(enabled)
    }
    fun disconnectWifi(): Boolean {
        return WM.disconnect()
    }
    fun reconnectWifi(): Boolean {
        return WM.reconnect()
    }
    @RequiresApi(24)
    fun getWifiMac(): String? {
        return DPM.getWifiMacAddress(DAR)
    }
    val configuredNetworks = MutableStateFlow(emptyList<WifiInfo>())
    fun getConfiguredNetworks() {
        configuredNetworks.value = WM.configuredNetworks.distinctBy { it.networkId }.map { conf ->
            WifiInfo(
                conf.networkId, conf.SSID.removeSurrounding("\""), null, conf.BSSID ?: "", null,
                WifiStatus.entries.find { it.id == conf.status }!!, null, "", null, null, null, null
            )
        }
    }
    fun enableNetwork(id: Int): Boolean {
        return WM.enableNetwork(id, false)
    }
    fun disableNetwork(id: Int): Boolean {
        return WM.disableNetwork(id)
    }
    fun removeNetwork(id: Int): Boolean{
        return WM.removeNetwork(id)
    }
    fun setWifi(info: WifiInfo): Boolean {
        val conf = WifiConfiguration()
        conf.SSID = "\"" + info.ssid + "\""
        info.hiddenSsid?.let { conf.hiddenSSID = it }
        if (VERSION.SDK_INT >= 30) info.security?.let { conf.setSecurityParams(it.id) }
        if (info.security == WifiSecurity.Psk) conf.preSharedKey = info.password
        if (VERSION.SDK_INT >= 33) info.macRandomization?.let { conf.macRandomizationSetting = it.id }
        if (VERSION.SDK_INT >= 33 && info.ipMode != null) {
            val ipConf = if (info.ipMode == IpMode.Static && info.ipConf != null) {
                val constructor = LinkAddress::class.constructors.find {
                    it.parameters.size == 1 && it.parameters[0].type.jvmErasure == String::class
                }
                val address = constructor!!.call(info.ipConf.address)
                val staticIpConf = StaticIpConfiguration.Builder()
                    .setIpAddress(address)
                    .setGateway(InetAddress.getByName(info.ipConf.gateway))
                    .setDnsServers(info.ipConf.dns.map { InetAddress.getByName(it) })
                    .build()
                IpConfiguration.Builder().setStaticIpConfiguration(staticIpConf).build()
            } else null
            conf.setIpConfiguration(ipConf)
        }
        if (VERSION.SDK_INT >= 26 && info.proxyMode != null) {
            val proxy = if (info.proxyMode == ProxyMode.Http) {
                info.proxyConf?.let {
                    ProxyInfo.buildDirectProxy(it.host, it.port, it.exclude)
                }
            } else null
            conf.httpProxy = proxy
        }
        val result = if (info.id != -1) {
            conf.networkId = info.id
            WM.updateNetwork(conf)
        } else {
            WM.addNetwork(conf)
        }
        if (result != -1) {
            when (info.status) {
                WifiStatus.Current -> WM.enableNetwork(result, true)
                WifiStatus.Enabled -> WM.enableNetwork(result, false)
                WifiStatus.Disabled -> WM.disableNetwork(result)
            }
        }
        return result != -1
    }
    @RequiresApi(33)
    fun getMinimumWifiSecurityLevel(): Int {
        return DPM.minimumRequiredWifiSecurityLevel
    }
    @RequiresApi(33)
    fun setMinimumWifiSecurityLevel(level: Int) {
        DPM.minimumRequiredWifiSecurityLevel = level
    }
    @RequiresApi(33)
    fun getSsidPolicy(): SsidPolicy {
        val policy = DPM.wifiSsidPolicy
        return SsidPolicy(
            SsidPolicyType.entries.find { it.id == policy?.policyType } ?: SsidPolicyType.None,
            policy?.ssids?.map { it.bytes.decodeToString() } ?: emptyList()
        )
    }
    @RequiresApi(33)
    fun setSsidPolicy(policy: SsidPolicy) {
        val newPolicy = if (policy.type != SsidPolicyType.None) {
            WifiSsidPolicy(
                policy.type.id, policy.list.map { WifiSsid.fromBytes(it.encodeToByteArray()) }.toSet()
            )
        } else null
        DPM.wifiSsidPolicy = newPolicy
    }
    @RequiresApi(24)
    fun getPackageUid(name: String): Int {
        return PM.getPackageUid(name, 0)
    }
    var networkStatsData = emptyList<NetworkStatsData>()
    @RequiresApi(23)
    fun readNetworkStats(stats: NetworkStats): List<NetworkStatsData> {
        val list = mutableListOf<NetworkStatsData>()
        while (stats.hasNextBucket()) {
            val bucket = NetworkStats.Bucket()
            stats.getNextBucket(bucket)
            list += readNetworkStatsBucket(bucket)
        }
        stats.close()
        return list
    }
    @RequiresApi(23)
    fun readNetworkStatsBucket(bucket: NetworkStats.Bucket): NetworkStatsData {
        return NetworkStatsData(
            bucket.rxBytes, bucket.rxPackets, bucket.txBytes, bucket.txPackets,
            bucket.uid, bucket.state, bucket.startTimeStamp, bucket.endTimeStamp,
            if (VERSION.SDK_INT >= 24) bucket.tag else null,
            if (VERSION.SDK_INT >= 24) bucket.roaming else null,
            if (VERSION.SDK_INT >= 26) bucket.metered else null
        )
    }
    @Suppress("NewApi")
    fun queryNetworkStats(params: QueryNetworkStatsParams, callback: (String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val nsm = application.getSystemService(NetworkStatsManager::class.java)
            try {
                val data = when (params.target) {
                    NetworkStatsTarget.Device -> listOf(readNetworkStatsBucket(
                        nsm.querySummaryForDevice(
                            params.networkType.type, null, params.startTime, params.endTime
                        )
                    ))
                    NetworkStatsTarget.User -> listOf(readNetworkStatsBucket(
                        nsm.querySummaryForUser(
                            params.networkType.type, null, params.startTime, params.endTime
                        )
                    ))
                    NetworkStatsTarget.Uid -> readNetworkStats(nsm.queryDetailsForUid(
                        params.networkType.type, null, params.startTime, params.endTime, params.uid
                    ))
                    NetworkStatsTarget.UidTag -> readNetworkStats(nsm.queryDetailsForUidTag(
                        params.networkType.type, null, params.startTime, params.endTime,
                        params.uid, params.tag
                    ))
                    NetworkStatsTarget.UidTagState -> readNetworkStats(
                        nsm.queryDetailsForUidTagState(
                            params.networkType.type, null, params.startTime, params.endTime,
                            params.uid, params.tag, params.state.id
                        )
                    )
                }
                networkStatsData = data
                withContext(Dispatchers.Main) {
                    if (data.isEmpty()) {
                        callback(application.getString(R.string.no_data))
                    } else {
                        callback(null)
                    }
                }
            } catch(e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    callback(e.message ?: "")
                }
            }
        }
    }
    fun clearNetworkStats() {
        networkStatsData = emptyList()
    }
    @RequiresApi(29)
    fun getPrivateDns(): PrivateDnsConfiguration {
        val mode = DPM.getGlobalPrivateDnsMode(DAR)
        return PrivateDnsConfiguration(
            PrivateDnsMode.entries.find { it.id == mode }, DPM.getGlobalPrivateDnsHost(DAR) ?: ""
        )
    }
    @Suppress("PrivateApi")
    @RequiresApi(29)
    fun setPrivateDns(conf: PrivateDnsConfiguration): Boolean {
        return try {
            val field = DevicePolicyManager::class.java.getDeclaredField("mService")
            field.isAccessible = true
            val dpm = field.get(DPM) as IDevicePolicyManager
            val host = if (conf.mode == PrivateDnsMode.Host) conf.host else null
            val result = dpm.setGlobalPrivateDns(DAR, conf.mode!!.id, host)
            result == DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    @RequiresApi(24)
    fun getAlwaysOnVpnPackage(): String {
        return DPM.getAlwaysOnVpnPackage(DAR) ?: ""
    }
    @RequiresApi(29)
    fun getAlwaysOnVpnLockdown(): Boolean {
        return DPM.isAlwaysOnVpnLockdownEnabled(DAR)
    }
    @RequiresApi(24)
    fun setAlwaysOnVpn(name: String?, lockdown: Boolean): Int {
        return try {
            DPM.setAlwaysOnVpnPackage(DAR, name, lockdown)
            R.string.succeeded
        } catch (_: UnsupportedOperationException) {
            R.string.unsupported
        } catch (_: PackageManager.NameNotFoundException) {
            R.string.not_installed
        }
    }
    fun setRecommendedGlobalProxy(conf: RecommendedProxyConf) {
        val info = when (conf.type) {
            ProxyType.Off -> null
            ProxyType.Pac -> {
                if (VERSION.SDK_INT >= 30 && conf.specifyPort) {
                    ProxyInfo.buildPacProxy(conf.url.toUri(), conf.port)
                } else {
                    ProxyInfo.buildPacProxy(conf.url.toUri())
                }
            }
            ProxyType.Direct -> {
                ProxyInfo.buildDirectProxy(conf.host, conf.port, conf.exclude)
            }
        }
        DPM.setRecommendedGlobalProxy(DAR, info)
    }
    // PNS: preferential network service
    @RequiresApi(31)
    fun getPnsEnabled(): Boolean {
        return DPM.isPreferentialNetworkServiceEnabled
    }
    @RequiresApi(31)
    fun setPnsEnabled(enabled: Boolean) {
        DPM.isPreferentialNetworkServiceEnabled = enabled
    }
    val pnsConfigs = MutableStateFlow(emptyList<PreferentialNetworkServiceInfo>())
    @RequiresApi(33)
    fun getPnsConfigs() {
        pnsConfigs.value = DPM.preferentialNetworkServiceConfigs.map {
            PreferentialNetworkServiceInfo(
                it.isEnabled, it.networkId, it.isFallbackToDefaultConnectionAllowed,
                if (VERSION.SDK_INT >= 34) it.shouldBlockNonMatchingNetworks() else false,
                it.excludedUids.toList(), it.includedUids.toList()
            )
        }
    }
    @RequiresApi(33)
    fun buildPnsConfig(
        info: PreferentialNetworkServiceInfo
    ): PreferentialNetworkServiceConfig {
        return PreferentialNetworkServiceConfig.Builder().apply {
            setEnabled(info.enabled)
            @Suppress("WrongConstant")
            setNetworkId(info.id)
            setFallbackToDefaultConnectionAllowed(info.allowFallback)
            if (VERSION.SDK_INT >= 34) setShouldBlockNonMatchingNetworks(info.blockNonMatching)
            setIncludedUids(info.includedUids.toIntArray())
            setExcludedUids(info.excludedUids.toIntArray())
        }.build()
    }
    @RequiresApi(33)
    fun setPnsConfig(info: PreferentialNetworkServiceInfo, state: Boolean) {
        val configs = pnsConfigs.value.run {
            if (state) plus(info) else minus(info)
        }.map { buildPnsConfig(it) }
        DPM.preferentialNetworkServiceConfigs = configs
    }
    val apnConfigs = MutableStateFlow(listOf<ApnConfig>())
    @RequiresApi(28)
    fun getApnEnabled(): Boolean {
        return DPM.isOverrideApnEnabled(DAR)
    }
    @RequiresApi(28)
    fun setApnEnabled(enabled: Boolean) {
        DPM.setOverrideApnsEnabled(DAR, enabled)
    }
    @RequiresApi(28)
    fun getApnConfigs() {
        apnConfigs.value = DPM.getOverrideApns(DAR).map {
            val proxy = if (VERSION.SDK_INT >= 29) it.proxyAddressAsString else it.proxyAddress.hostName
            val mmsProxy = if (VERSION.SDK_INT >= 29) it.mmsProxyAddressAsString else it.mmsProxyAddress.hostName
            ApnConfig(
                it.isEnabled, it.entryName, it.apnName, proxy, it.proxyPort,
                it.user, it.password, it.apnTypeBitmask, it.mmsc.toString(),
                mmsProxy, it.mmsProxyPort,
                ApnAuthType.entries.find { type -> type.id == it.authType }!!,
                ApnProtocol.entries.find { protocol -> protocol.id == it.protocol }!!,
                ApnProtocol.entries.find { protocol -> protocol.id == it.roamingProtocol }!!,
                it.networkTypeBitmask,
                if (VERSION.SDK_INT >= 33) it.profileId else 0,
                if (VERSION.SDK_INT >= 29) it.carrierId else 0,
                if (VERSION.SDK_INT >= 33) it.mtuV4 else 0,
                if (VERSION.SDK_INT >= 33) it.mtuV6 else 0,
                ApnMvnoType.entries.find { type ->  type.id == it.mvnoType }!!,
                it.operatorNumeric,
                if (VERSION.SDK_INT >= 33) it.isPersistent else true,
                if (VERSION.SDK_INT >= 35) it.isAlwaysOn else true,
                it.id
            )
        }
    }
    @RequiresApi(28)
    fun buildApnSetting(config: ApnConfig): ApnSetting? {
        val builder = ApnSetting.Builder()
        builder.setCarrierEnabled(config.enabled)
        builder.setEntryName(config.name)
        builder.setApnName(config.apn)
        if (VERSION.SDK_INT >= 29) builder.setProxyAddress(config.proxy)
        else builder.setProxyAddress(InetAddress.getByName(config.proxy))
        config.port?.let { builder.setProxyPort(it) }
        builder.setUser(config.username)
        builder.setPassword(config.password)
        builder.setApnTypeBitmask(config.apnType)
        builder.setMmsc(config.mmsc.toUri())
        if (VERSION.SDK_INT >= 29) builder.setMmsProxyAddress(config.mmsProxy)
        else builder.setMmsProxyAddress(InetAddress.getByName(config.mmsProxy))
        builder.setAuthType(config.authType.id)
        builder.setProtocol(config.protocol.id)
        builder.setRoamingProtocol(config.roamingProtocol.id)
        builder.setNetworkTypeBitmask(config.networkType)
        if (VERSION.SDK_INT >= 33) config.profileId?.let { builder.setProfileId(it) }
        if (VERSION.SDK_INT >= 29) config.carrierId?.let { builder.setCarrierId(it) }
        if (VERSION.SDK_INT >= 33) {
            config.mtuV4?.let { builder.setMtuV4(it) }
            config.mtuV6?.let { builder.setMtuV6(it) }
        }
        builder.setMvnoType(config.mvno.id)
        builder.setOperatorNumeric(config.operatorNumeric)
        if (VERSION.SDK_INT >= 33) builder.setPersistent(config.persistent)
        if (VERSION.SDK_INT >= 35) builder.setAlwaysOn(config.alwaysOn)
        return builder.build()
    }
    @RequiresApi(28)
    fun setApnConfig(config: ApnConfig): Boolean {
        val settings = buildApnSetting(config)
        if (settings == null) return false
        return if (config.id == -1) {
            DPM.addOverrideApn(DAR, settings) != -1
        } else {
            DPM.updateOverrideApn(DAR, config.id, settings)
        }
    }
    @RequiresApi(28)
    fun removeApnConfig(id: Int): Boolean {
        return DPM.removeOverrideApn(DAR, id)
    }
    @RequiresApi(26)
    fun getNetworkLoggingEnabled(): Boolean {
        return DPM.isNetworkLoggingEnabled(DAR)
    }
    @RequiresApi(26)
    fun setNetworkLoggingEnabled(enabled: Boolean) {
        DPM.setNetworkLoggingEnabled(DAR, enabled)
    }
    fun getNetworkLogsCount(): Int {
        return myRepo.getNetworkLogsCount().toInt()
    }
    fun exportNetworkLogs(uri: Uri, callback: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            application.contentResolver.openOutputStream(uri)?.use {
                myRepo.exportNetworkLogs(it)
            }
            withContext(Dispatchers.Main) { callback() }
        }
    }
    fun deleteNetworkLogs() {
        myRepo.deleteNetworkLogs()
    }

    @RequiresApi(29)
    fun getPasswordComplexity(): PasswordComplexity {
        val complexity = DPM.passwordComplexity
        return PasswordComplexity.entries.find { it.id == complexity }!!
    }
    fun isPasswordComplexitySufficient(): Boolean {
        return DPM.isActivePasswordSufficient
    }
    @RequiresApi(28)
    fun isUsingUnifiedPassword(): Boolean {
        return DPM.isUsingUnifiedPassword(DAR)
    }
    // Reset password token
    @RequiresApi(26)
    fun getRpTokenState(): RpTokenState {
        return try {
            RpTokenState(true, DPM.isResetPasswordTokenActive(DAR))
        } catch (_: IllegalStateException) {
            RpTokenState(false, false)
        }
    }
    @RequiresApi(26)
    fun setRpToken(token: String): Boolean {
        return DPM.setResetPasswordToken(DAR, token.encodeToByteArray())
    }
    @RequiresApi(26)
    fun clearRpToken(): Boolean {
        return DPM.clearResetPasswordToken(DAR)
    }
    @RequiresApi(26)
    fun createActivateRpTokenIntent(): Intent? {
        val km = application.getSystemService(KeyguardManager::class.java)
        val title = application.getString(R.string.activate_reset_password_token)
        return km.createConfirmDeviceCredentialIntent(title, "")
    }
    fun resetPassword(password: String, token: String, flags: Int): Boolean {
        return if (VERSION.SDK_INT >= 26) {
            DPM.resetPasswordWithToken(DAR, password, token.encodeToByteArray(), flags)
        } else {
            DPM.resetPassword(password, flags)
        }
    }
    @RequiresApi(31)
    fun getRequiredPasswordComplexity(): PasswordComplexity {
        val complexity = DPM.requiredPasswordComplexity
        return PasswordComplexity.entries.find { it.id == complexity }!!
    }
    @RequiresApi(31)
    fun setRequiredPasswordComplexity(complexity: PasswordComplexity) {
        DPM.requiredPasswordComplexity = complexity.id
    }
    fun getKeyguardDisableConfig(): KeyguardDisableConfig {
        val flags = DPM.getKeyguardDisabledFeatures(DAR)
        val mode = when (flags) {
            DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE -> KeyguardDisableMode.None
            DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL -> KeyguardDisableMode.All
            else -> KeyguardDisableMode.Custom
        }
        return KeyguardDisableConfig(mode, flags)
    }
    fun setKeyguardDisableConfig(config: KeyguardDisableConfig) {
        val flags = when (config.mode) {
            KeyguardDisableMode.None -> DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE
            KeyguardDisableMode.All -> DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL
            else -> config.flags
        }
        DPM.setKeyguardDisabledFeatures(DAR, flags)
    }
    fun getMaxTimeToLock(): Long {
        return DPM.getMaximumTimeToLock(DAR)
    }
    @RequiresApi(26)
    fun getRequiredStrongAuthTimeout(): Long {
        return DPM.getRequiredStrongAuthTimeout(DAR)
    }
    fun getPasswordExpirationTimeout(): Long {
        return DPM.getPasswordExpirationTimeout(DAR)
    }
    fun getMaxFailedPasswordsForWipe(): Int {
        return DPM.getMaximumFailedPasswordsForWipe(DAR)
    }
    fun getPasswordHistoryLength(): Int {
        return DPM.getPasswordHistoryLength(DAR)
    }
    fun setMaxTimeToLock(time: Long) {
        DPM.setMaximumTimeToLock(DAR, time)
    }
    @RequiresApi(26)
    fun setRequiredStrongAuthTimeout(time: Long) {
        DPM.setRequiredStrongAuthTimeout(DAR, time)
    }
    fun setPasswordExpirationTimeout(time: Long) {
        DPM.setPasswordExpirationTimeout(DAR, time)
    }
    fun setMaxFailedPasswordsForWipe(times: Int) {
        DPM.setMaximumFailedPasswordsForWipe(DAR, times)
    }
    fun setPasswordHistoryLength(length: Int) {
        DPM.setPasswordHistoryLength(DAR, length)
    }
}

data class ThemeSettings(
    val materialYou: Boolean = false,
    val darkTheme: Int = -1,
    val blackTheme: Boolean = false
)
