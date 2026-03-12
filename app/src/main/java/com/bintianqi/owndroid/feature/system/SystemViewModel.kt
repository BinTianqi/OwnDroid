package com.bintianqi.owndroid.feature.system

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.FactoryResetProtectionPolicy
import android.os.Build.VERSION
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.getAppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SystemViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val settingsRepo: SettingsRepository,
    val privilegeState: StateFlow<PrivilegeStatus>, val toastChannel: ToastChannel
) : ViewModel() {
    fun getDisplayDangerousFeatures() = settingsRepo.data.displayDangerousFeatures

    @RequiresApi(24)
    fun reboot() = ph.safeDpmCall {
        dpm.reboot(dar)
    }

    @RequiresApi(24)
    fun requestBugReport() = ph.safeDpmCall {
        val result = try {
            dpm.requestBugreport(dar)
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        toastChannel.sendStatus(result)
    }

    val orgNameState = MutableStateFlow("")

    @SuppressLint("PrivateApi")
    @RequiresApi(24)
    fun getOrgName() = ph.safeDpmCall {
        orgNameState.value = try {
            dpm.getOrganizationName(dar)?.toString() ?: ""
        } catch (_: Exception) {
            try {
                val method = DevicePolicyManager::class.java.getDeclaredMethod(
                    "getDeviceOwnerOrganizationName"
                )
                method.isAccessible = true
                (method.invoke(dpm) as CharSequence).toString()
            } catch (_: Exception) {
                ""
            }
        }
    }

    fun setOrgName(name: String) {
        orgNameState.value = name
    }

    @RequiresApi(24)
    fun applyOrgName() = ph.safeDpmCall {
        dpm.setOrganizationName(dar, orgNameState.value)
    }

    val orgIdState = MutableStateFlow("")

    fun setOrgId(id: String) {
        orgIdState.value = id
    }

    @RequiresApi(31)
    fun applyOrgId() = ph.safeDpmCall {
        val result = try {
            dpm.setOrganizationId(orgIdState.value)
            true
        } catch (_: IllegalStateException) {
            false
        }
        toastChannel.sendStatus(result)
    }

    val enrollmentSpecificIdState = MutableStateFlow("")

    @RequiresApi(31)
    fun getEnrollmentSpecificId() = ph.safeDpmCall {
        enrollmentSpecificIdState.value =
            dpm.enrollmentSpecificId.ifEmpty { application.getString(R.string.none) }
    }

    val lockScreenInfoState = MutableStateFlow("")

    @RequiresApi(24)
    fun getLockScreenInfo() = ph.safeDpmCall {
        lockScreenInfoState.value = dpm.deviceOwnerLockScreenInfo?.toString() ?: ""
    }

    fun setLockScreenInfo(text: String) {
        lockScreenInfoState.value = text
    }

    @RequiresApi(24)
    fun applyLockScreenInfo() = ph.safeDpmCall {
        dpm.setDeviceOwnerLockScreenInfo(dar, lockScreenInfoState.value)
        toastChannel.sendStatus(true)
    }

    fun setKeyguardDisabled(disabled: Boolean) = ph.safeDpmCall {
        val result = dpm.setKeyguardDisabled(dar, disabled)
        toastChannel.sendStatus(result)
    }

    fun lockScreen(evictKey: Boolean) = ph.safeDpmCall {
        if (VERSION.SDK_INT >= 26 && privilegeState.value.work) {
            dpm.lockNow(
                if (evictKey) DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY else 0
            )
        } else {
            dpm.lockNow()
        }
    }

    val defaultInputMethodState = MutableStateFlow("")

    val inputMethodList = MutableStateFlow(listOf<Pair<String, AppInfo>>())

    fun getDefaultInputMethod() = ph.safeDpmCall {
        defaultInputMethodState.value = Settings.Secure.getString(
            application.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD
        )
    }

    fun getInputMethods() = ph.safeDpmCall {
        val imm = application.getSystemService(InputMethodManager::class.java)
        val pm = application.packageManager
        inputMethodList.value = imm.inputMethodList.map {
            it.id to getAppInfo(pm, it.packageName)
        }
    }

    fun setDefaultInputMethod(id: String) = ph.safeDpmCall {
        dpm.setSecureSetting(
            dar, Settings.Secure.DEFAULT_INPUT_METHOD, id
        )
        getDefaultInputMethod()
    }

    val contentProtectionPolicyState = MutableStateFlow(0)

    @RequiresApi(35)
    fun getContentProtectionPolicy() = ph.safeDpmCall {
        contentProtectionPolicyState.value = dpm.getContentProtectionPolicy(dar)
    }

    @RequiresApi(35)
    fun setContentProtectionPolicy(policy: Int) = ph.safeDpmCall {
        dpm.setContentProtectionPolicy(dar, policy)
        getContentProtectionPolicy()
    }

    val permissionPolicyState = MutableStateFlow(0)

    fun getPermissionPolicy() = ph.safeDpmCall {
        permissionPolicyState.value = dpm.getPermissionPolicy(dar)
    }

    fun setPermissionPolicy(policy: Int) = ph.safeDpmCall {
        dpm.setPermissionPolicy(dar, policy)
        getPermissionPolicy()
    }

    val mtePolicyState = MutableStateFlow(0)

    @RequiresApi(34)
    fun getMtePolicy() = ph.safeDpmCall {
        mtePolicyState.value = dpm.mtePolicy
    }

    @RequiresApi(34)
    fun setMtePolicy(policy: Int) = ph.safeDpmCall {
        try {
            dpm.mtePolicy = policy
            mtePolicyState.value = policy
        } catch (_: UnsupportedOperationException) {
            toastChannel.sendText(R.string.unsupported)
        }
    }

    // Nearby streaming
    val nsAppPolicyState = MutableStateFlow(0)

    @RequiresApi(31)
    fun getNsAppPolicy() = ph.safeDpmCall {
        nsAppPolicyState.value = dpm.nearbyAppStreamingPolicy
    }

    @RequiresApi(31)
    fun setNsAppPolicy(policy: Int) = ph.safeDpmCall {
        dpm.nearbyAppStreamingPolicy = policy
        getNsAppPolicy()
    }

    val nsNotificationPolicyState = MutableStateFlow(0)

    @RequiresApi(31)
    fun getNsNotificationPolicy() = ph.safeDpmCall {
        nsNotificationPolicyState.value = dpm.nearbyNotificationStreamingPolicy
    }

    @RequiresApi(31)
    fun setNsNotificationPolicy(policy: Int) = ph.safeDpmCall {
        dpm.nearbyNotificationStreamingPolicy = policy
        getNsNotificationPolicy()
    }

    // Management disabled account
    val mdAccountTypes = MutableStateFlow(emptyList<String>())

    fun getMdAccountTypes() = ph.safeDpmCall {
        mdAccountTypes.value = dpm.accountTypesWithManagementDisabled?.toList() ?: emptyList()
    }

    fun setMdAccountType(type: String, disabled: Boolean) = ph.safeDpmCall {
        dpm.setAccountManagementDisabled(dar, type, disabled)
        getMdAccountTypes()
    }

    val frpPolicyState = MutableStateFlow(FrpPolicyInfo())

    @RequiresApi(30)
    fun getFrpPolicy() = ph.safeDpmCall {
        try {
            val policy = dpm.getFactoryResetProtectionPolicy(dar)
            frpPolicyState.value = FrpPolicyInfo(
                true, policy != null, policy?.isFactoryResetProtectionEnabled ?: false,
                policy?.factoryResetProtectionAccounts ?: emptyList()
            )
        } catch (_: UnsupportedOperationException) {
        }
    }

    fun setFrpPolicy(info: FrpPolicyInfo) {
        frpPolicyState.value = info
    }

    @RequiresApi(30)
    fun applyFrpPolicy() = ph.safeDpmCall {
        val info = frpPolicyState.value
        val policy = if (info.usePolicy) {
            FactoryResetProtectionPolicy.Builder()
                .setFactoryResetProtectionEnabled(info.enabled)
                .setFactoryResetProtectionAccounts(info.accounts)
                .build()
        } else null
        dpm.setFactoryResetProtectionPolicy(dar, policy)
        toastChannel.sendStatus(true)
    }

    fun wipeData(wipeDevice: Boolean, flags: Int, reason: String) = ph.safeDpmCall {
        if (wipeDevice && VERSION.SDK_INT >= 34) {
            dpm.wipeDevice(flags)
        } else {
            if (VERSION.SDK_INT >= 28 && reason.isNotEmpty()) {
                dpm.wipeData(flags, reason)
            } else {
                dpm.wipeData(flags)
            }
        }
    }

    val deviceInfoState = MutableStateFlow(DeviceInfo())

    fun getDeviceInfo() = ph.safeDpmCall {
        val ps = privilegeState.value
        deviceInfoState.value = DeviceInfo(
            if (VERSION.SDK_INT >= 34 && (ps.device || ps.org)) dpm.isDeviceFinanced else false,
            if (VERSION.SDK_INT >= 33) dpm.devicePolicyManagementRoleHolderPackage else "",
            dpm.storageEncryptionStatus,
            if (VERSION.SDK_INT >= 28) dpm.isDeviceIdAttestationSupported else false,
            if (VERSION.SDK_INT >= 30) dpm.isUniqueDeviceAttestationSupported else false,
            dpm.activeAdmins?.map { it.flattenToShortString() } ?: emptyList()
        )
    }

    val shortSupportMessageState = MutableStateFlow("")
    val longSupportMessageState = MutableStateFlow("")

    @RequiresApi(24)
    fun getSupportMessages() = ph.safeDpmCall {
        shortSupportMessageState.value = dpm.getShortSupportMessage(dar)?.toString() ?: ""
        longSupportMessageState.value = dpm.getLongSupportMessage(dar)?.toString() ?: ""
    }

    fun setShortSupportMessage(text: String) {
        shortSupportMessageState.value = text
    }

    fun setLongSupportMessage(text: String) {
        longSupportMessageState.value = text
    }

    @RequiresApi(24)
    fun applySupportMessages() = ph.safeDpmCall {
        dpm.setShortSupportMessage(dar, shortSupportMessageState.value.ifEmpty { null })
        dpm.setLongSupportMessage(dar, longSupportMessageState.value.ifEmpty { null })
        toastChannel.sendStatus(true)
    }
}
