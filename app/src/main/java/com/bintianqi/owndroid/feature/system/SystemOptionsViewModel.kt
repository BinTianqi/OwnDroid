package com.bintianqi.owndroid.feature.system

import android.os.Build.VERSION
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.MyShortcut
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ShortcutUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class SystemOptionsViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val settingsRepo: SettingsRepository,
    val privilegeState: StateFlow<PrivilegeStatus>
) : ViewModel() {
    val optionsState = MutableStateFlow(SystemOptionsStatus())

    fun getSystemOptionsStatus() = ph.safeDpmCall {
        val privilege = privilegeState.value
        optionsState.value = SystemOptionsStatus(
            cameraDisabled = dpm.getCameraDisabled(null),
            screenCaptureDisabled = dpm.getScreenCaptureDisabled(null),
            statusBarDisabled = if (VERSION.SDK_INT >= 34 &&
                privilege.run { device || (profile && affiliated) }
            )
                dpm.isStatusBarDisabled else false,
            autoTimeEnabled = if (VERSION.SDK_INT >= 30 && (privilege.device || privilege.org))
                dpm.getAutoTimeEnabled(dar) else false,
            autoTimeZoneEnabled = if (VERSION.SDK_INT >= 30 && (privilege.device || privilege.org))
                dpm.getAutoTimeZoneEnabled(dar) else false,
            autoTimeRequired = if (VERSION.SDK_INT < 30) dpm.autoTimeRequired else false,
            masterVolumeMuted = dpm.isMasterVolumeMuted(dar),
            backupServiceEnabled =
                if (VERSION.SDK_INT >= 26) dpm.isBackupServiceEnabled(dar) else false,
            btContactSharingDisabled = if (privilege.work)
                dpm.getBluetoothContactSharingDisabled(dar) else false,
            commonCriteriaMode =
                if (VERSION.SDK_INT >= 30 && (privilege.device || privilege.org))
                    dpm.isCommonCriteriaModeEnabled(dar)
                else false,
            usbSignalEnabled = if (VERSION.SDK_INT >= 31) dpm.isUsbDataSignalingEnabled else false,
            canDisableUsbSignal =
                if (VERSION.SDK_INT >= 31) dpm.canUsbDataSignalingBeDisabled() else false,
            stayOnWhilePluggedIn =
                Settings.Global.getInt(
                    application.contentResolver, Settings.Global.STAY_ON_WHILE_PLUGGED_IN
                ) != 0
        )
    }

    fun setCameraDisabled(disabled: Boolean) = ph.safeDpmCall {
        dpm.setCameraDisabled(dar, disabled)
        ShortcutUtils.setShortcut(application, settingsRepo, MyShortcut.DisableCamera, !disabled)
        optionsState.update { it.copy(cameraDisabled = dpm.getCameraDisabled(null)) }
    }

    fun setScreenCaptureDisabled(disabled: Boolean) = ph.safeDpmCall {
        dpm.setScreenCaptureDisabled(dar, disabled)
        optionsState.update {
            it.copy(screenCaptureDisabled = dpm.getScreenCaptureDisabled(null))
        }
    }

    fun setStatusBarDisabled(disabled: Boolean) = ph.safeDpmCall {
        val result = dpm.setStatusBarDisabled(dar, disabled)
        if (result) optionsState.update { it.copy(statusBarDisabled = disabled) }
    }

    @RequiresApi(30)
    fun setAutoTimeEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.setAutoTimeEnabled(dar, enabled)
        optionsState.update { it.copy(autoTimeEnabled = dpm.getAutoTimeEnabled(dar)) }
    }

    @RequiresApi(30)
    fun setAutoTimeZoneEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.setAutoTimeZoneEnabled(dar, enabled)
        optionsState.update {
            it.copy(autoTimeZoneEnabled = dpm.getAutoTimeZoneEnabled(dar))
        }
    }

    @Suppress("DEPRECATION")
    fun setAutoTimeRequired(required: Boolean) = ph.safeDpmCall {
        dpm.setAutoTimeRequired(dar, required)
        optionsState.update { it.copy(autoTimeRequired = dpm.autoTimeRequired) }
    }

    fun setMasterVolumeMuted(muted: Boolean) = ph.safeDpmCall {
        dpm.setMasterVolumeMuted(dar, muted)
        ShortcutUtils.setShortcut(application, settingsRepo, MyShortcut.Mute, !muted)
        optionsState.update { it.copy(masterVolumeMuted = dpm.isMasterVolumeMuted(dar)) }
    }

    @RequiresApi(26)
    fun setBackupServiceEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.setBackupServiceEnabled(dar, enabled)
        optionsState.update {
            it.copy(backupServiceEnabled = dpm.isBackupServiceEnabled(dar))
        }
    }

    fun setBtContactSharingDisabled(disabled: Boolean) = ph.safeDpmCall {
        dpm.setBluetoothContactSharingDisabled(dar, disabled)
        optionsState.update {
            it.copy(btContactSharingDisabled = dpm.getBluetoothContactSharingDisabled(dar))
        }
    }

    @RequiresApi(30)
    fun setCommonCriteriaModeEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.setCommonCriteriaModeEnabled(dar, enabled)
        optionsState.update {
            it.copy(commonCriteriaMode = dpm.isCommonCriteriaModeEnabled(dar))
        }
    }

    @RequiresApi(31)
    fun setUsbSignalEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.isUsbDataSignalingEnabled = enabled
        optionsState.update { it.copy(usbSignalEnabled = dpm.isUsbDataSignalingEnabled) }
    }

    fun setStayOnWhilePluggedIn(status: Boolean) = ph.safeDpmCall {
        dpm.setGlobalSetting(
            dar, Settings.Global.STAY_ON_WHILE_PLUGGED_IN, if (status) "15" else "0"
        )
        optionsState.update { it.copy(stayOnWhilePluggedIn = status) }
    }

    fun getGlobalSettings(): Map<String, Boolean> {
        return globalSettings.associate {
            it.setting to (Settings.Global.getInt(application.contentResolver, it.setting, 0) == 1)
        }
    }

    fun setGlobalSetting(name: String, status: Boolean) = ph.safeDpmCall {
        dpm.setGlobalSetting(dar, name, if (status) "1" else "0")
    }

    fun getSecureSettings(): Map<String, Boolean> {
        return secureSettings.associate {
            it.setting to (Settings.Secure.getInt(application.contentResolver, it.setting, 0) == 1)
        }
    }

    fun setSecureSetting(name: String, status: Boolean) = ph.safeDpmCall {
        dpm.setSecureSetting(dar, name, if (status) "1" else "0")
    }
}
