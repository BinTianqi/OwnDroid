package com.bintianqi.owndroid.feature.system

import android.provider.Settings
import com.bintianqi.owndroid.R

data class SystemOptionsStatus(
    val cameraDisabled: Boolean = false,
    val screenCaptureDisabled: Boolean = false,
    val statusBarDisabled: Boolean = false,
    val autoTimeEnabled: Boolean = true,
    val autoTimeZoneEnabled: Boolean = true,
    val autoTimeRequired: Boolean = true,
    val masterVolumeMuted: Boolean = false,
    val backupServiceEnabled: Boolean = false,
    val btContactSharingDisabled: Boolean = false,
    val commonCriteriaMode: Boolean = false,
    val usbSignalEnabled: Boolean = true,
    val canDisableUsbSignal: Boolean = true,
    val stayOnWhilePluggedIn: Boolean = false
)

class GlobalSetting(val icon: Int, val name: Int, val setting: String) // also for secure settings

val globalSettings = listOf(
    //GlobalSetting(R.drawable.cell_tower_fill0, R.string.data_roaming, Settings.Global.DATA_ROAMING),
    GlobalSetting(R.drawable.adb_fill0, R.string.enable_adb, Settings.Global.ADB_ENABLED),
    GlobalSetting(R.drawable.usb_fill0, R.string.enable_usb_mass_storage,
        Settings.Global.USB_MASS_STORAGE_ENABLED),
    GlobalSetting(R.drawable.wifi_password_fill0, R.string.lockdown_admin_configured_network,
        Settings.Global.WIFI_DEVICE_OWNER_CONFIGS_LOCKDOWN)
)

val secureSettings = listOf(
    GlobalSetting(R.drawable.light_off_fill0, R.string.skip_first_use_hints,
        Settings.Secure.SKIP_FIRST_USE_HINTS)
)
