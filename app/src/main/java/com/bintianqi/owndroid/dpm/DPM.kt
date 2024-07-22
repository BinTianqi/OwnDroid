package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.app.admin.IDevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.app.admin.WifiSsidPolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build.VERSION
import androidx.activity.ComponentActivity.CONTEXT_IGNORE_SECURITY
import androidx.activity.ComponentActivity.DEVICE_POLICY_SERVICE
import androidx.activity.result.ActivityResultLauncher
import com.bintianqi.owndroid.PackageInstallerReceiver
import com.bintianqi.owndroid.Receiver
import com.bintianqi.owndroid.backToHomeStateFlow
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.Dhizuku.binderWrapper
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.IOException
import java.io.InputStream

var selectedPermission = MutableStateFlow("")
lateinit var createManagedProfile: ActivityResultLauncher<Intent>
lateinit var addDeviceAdmin: ActivityResultLauncher<Intent>

val Context.isDeviceOwner: Boolean
    get() {
        val sharedPref = getSharedPreferences("data", Context.MODE_PRIVATE)
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isDeviceOwnerApp(
            if(sharedPref.getBoolean("dhizuku", false)) {
                Dhizuku.getOwnerPackageName()
            } else {
                "com.bintianqi.owndroid"
            }
        )
    }

val Context.isProfileOwner: Boolean
    get() {
        val dpm = getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        return dpm.isProfileOwnerApp("com.bintianqi.owndroid")
    }

val Context.isDeviceAdmin: Boolean
    get() {
        return getDPM().isAdminActive(getReceiver())
    }

val Context.dpcPackageName: String
    get() {
        val sharedPref = getSharedPreferences("data", Context.MODE_PRIVATE)
        return if(sharedPref.getBoolean("dhizuku", false)) {
            Dhizuku.getOwnerPackageName()
        } else {
            "com.bintianqi.owndroid"
        }
    }

fun DevicePolicyManager.isOrgProfile(receiver: ComponentName): Boolean {
    return VERSION.SDK_INT >= 30 && this.isProfileOwnerApp("com.bintianqi.owndroid") && isManagedProfile(receiver) && isOrganizationOwnedDeviceWithManagedProfile
}

@Throws(IOException::class)
fun installPackage(context: Context, inputStream: InputStream) {
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)
    val out = session.openWrite("COSU", 0, -1)
    val buffer = ByteArray(65536)
    var c: Int
    while(inputStream.read(buffer).also{c = it}!=-1) { out.write(buffer, 0, c) }
    session.fsync(out)
    inputStream.close()
    out.close()
    val intent = Intent(context, PackageInstallerReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
    session.commit(pendingIntent)
}

@SuppressLint("PrivateApi")
fun binderWrapperDevicePolicyManager(appContext: Context): DevicePolicyManager? {
    try {
        val context = appContext.createPackageContext(Dhizuku.getOwnerComponent().packageName, CONTEXT_IGNORE_SECURITY)
        val manager = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val field = manager.javaClass.getDeclaredField("mService")
        field.isAccessible = true
        val oldInterface = field[manager] as IDevicePolicyManager
        if (oldInterface is DhizukuBinderWrapper) return manager
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IDevicePolicyManager.Stub.asInterface(newBinder)
        field[manager] = newInterface
        return manager
    } catch (e: Exception) {
        dhizukuErrorStatus.value = 1
    }
    return null
}

fun Context.getDPM(): DevicePolicyManager {
    val sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE)
    if(sharedPref.getBoolean("dhizuku", false)) {
        if (!Dhizuku.isPermissionGranted()) {
            dhizukuErrorStatus.value = 2
            backToHomeStateFlow.value = true
            return this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        }
        return binderWrapperDevicePolicyManager(this) ?: this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
    } else {
        return this.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }
}

fun Context.getReceiver(): ComponentName {
    val sharedPref = this.getSharedPreferences("data", Context.MODE_PRIVATE)
    return if(sharedPref.getBoolean("dhizuku", false)) {
        Dhizuku.getOwnerComponent()
    } else {
        ComponentName(this, Receiver::class.java)
    }
}

val dhizukuErrorStatus = MutableStateFlow(0)

fun Context.resetDevicePolicy() {
    val dpm = getDPM()
    val receiver = getReceiver()
    RestrictionData.getAllRestrictions(this).forEach {
        dpm.clearUserRestriction(receiver, it)
    }
    dpm.accountTypesWithManagementDisabled?.forEach {
        dpm.setAccountManagementDisabled(receiver, it, false)
    }
    if (VERSION.SDK_INT >= 30) {
        dpm.setConfiguredNetworksLockdownState(receiver, false)
        dpm.setAutoTimeZoneEnabled(receiver, true)
        dpm.setAutoTimeEnabled(receiver, true)
        dpm.setCommonCriteriaModeEnabled(receiver, false)
        try {
            val frp = FactoryResetProtectionPolicy.Builder().setFactoryResetProtectionEnabled(false).setFactoryResetProtectionAccounts(listOf())
            dpm.setFactoryResetProtectionPolicy(receiver, frp.build())
        } catch(_: Exception) {}
        dpm.setUserControlDisabledPackages(receiver, listOf())
    }
    if (VERSION.SDK_INT >= 33) {
        dpm.minimumRequiredWifiSecurityLevel = DevicePolicyManager.WIFI_SECURITY_OPEN
        dpm.wifiSsidPolicy = null
    }
    if (VERSION.SDK_INT >= 28) {
        dpm.getOverrideApns(receiver).forEach { dpm.removeOverrideApn(receiver, it.id) }
        dpm.setKeepUninstalledPackages(receiver, listOf())
    }
    dpm.setCameraDisabled(receiver, false)
    dpm.setScreenCaptureDisabled(receiver, false)
    dpm.setMasterVolumeMuted(receiver, false)
    try {
        if(VERSION.SDK_INT >= 31) dpm.isUsbDataSignalingEnabled = true
    } catch (_: Exception) { }
    if (VERSION.SDK_INT >= 23) {
        dpm.setPermissionPolicy(receiver, DevicePolicyManager.PERMISSION_POLICY_PROMPT)
        dpm.setSystemUpdatePolicy(receiver, SystemUpdatePolicy.createAutomaticInstallPolicy())
    }
    if (VERSION.SDK_INT >= 24) {
        dpm.setAlwaysOnVpnPackage(receiver, null, false)
        dpm.setPackagesSuspended(receiver, arrayOf(), false)
    }
    dpm.setPermittedInputMethods(receiver, null)
    dpm.setPermittedAccessibilityServices(receiver, null)
    packageManager.getInstalledApplications(0).forEach {
        if (dpm.isUninstallBlocked(receiver, it.packageName)) dpm.setUninstallBlocked(receiver, it.packageName, false)
    }
    if (VERSION.SDK_INT >= 26) {
        dpm.setRequiredStrongAuthTimeout(receiver, 0)
        dpm.clearResetPasswordToken(receiver)
    }
    if (VERSION.SDK_INT >= 31) {
        dpm.requiredPasswordComplexity = DevicePolicyManager.PASSWORD_COMPLEXITY_NONE
    }
    dpm.setKeyguardDisabledFeatures(receiver, 0)
    dpm.setMaximumTimeToLock(receiver, 0)
    dpm.setPasswordExpirationTimeout(receiver, 0)
    dpm.setMaximumFailedPasswordsForWipe(receiver, 0)
    dpm.setPasswordHistoryLength(receiver, 0)
    if (VERSION.SDK_INT < 31) {
        dpm.setPasswordQuality(receiver, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED)
    }
    dpm.setRecommendedGlobalProxy(receiver, null)
}
