package com.bintianqi.owndroid.dpm

import android.os.Build.VERSION
import android.os.UserManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.SwitchItem

data class Restriction(
    val id: String,
    @StringRes val name: Int,
    @DrawableRes val icon: Int
)

@Composable
fun UserRestriction(navCtrl:NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    MyScaffold(R.string.user_restriction, 0.dp, navCtrl) {
        Text(text = stringResource(R.string.switch_to_disable_feature), modifier = Modifier.padding(start = 16.dp))
        if(context.isProfileOwner) { Text(text = stringResource(R.string.profile_owner_is_restricted), modifier = Modifier.padding(start = 16.dp)) }
        if(context.isProfileOwner && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)))) {
            Text(text = stringResource(R.string.some_features_invalid_in_work_profile), modifier = Modifier.padding(start = 16.dp))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        FunctionItem(R.string.network_and_internet, icon = R.drawable.wifi_fill0) { navCtrl.navigate("UR-Internet") }
        FunctionItem(R.string.connectivity, icon = R.drawable.devices_other_fill0) { navCtrl.navigate("UR-Connectivity") }
        FunctionItem(R.string.applications, icon = R.drawable.apps_fill0) { navCtrl.navigate("UR-Applications") }
        FunctionItem(R.string.users, icon = R.drawable.account_circle_fill0) { navCtrl.navigate("UR-Users") }
        FunctionItem(R.string.media, icon = R.drawable.volume_up_fill0) { navCtrl.navigate("UR-Media") }
        FunctionItem(R.string.other, icon = R.drawable.more_horiz_fill0) { navCtrl.navigate("UR-Other") }
    }
}

@RequiresApi(24)
@Composable
fun UserRestrictionItem(restriction: Restriction) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    Box(modifier = Modifier.padding(start = 22.dp, end = 16.dp)) {
        SwitchItem(
            restriction.name, restriction.id, restriction.icon,
            { dpm.getUserRestrictions(receiver).getBoolean(restriction.id) },
            {
                try {
                    if(it) {
                        dpm.addUserRestriction(receiver, restriction.id)
                    } else {
                        dpm.clearUserRestriction(receiver, restriction.id)
                    }
                } catch(_: SecurityException) {
                    if(context.isProfileOwner) {
                        Toast.makeText(context, R.string.require_device_owner, Toast.LENGTH_SHORT).show()
                    }
                }
            }, padding = false
        )
    }
}

object RestrictionData {
    val internet: List<Restriction> get() {
        val list = mutableListOf<Restriction>()
        list += Restriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, R.string.config_mobile_network, R.drawable.signal_cellular_alt_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_WIFI, R.string.config_wifi, R.drawable.wifi_fill0)
        if(VERSION.SDK_INT>=24) list += Restriction(UserManager.DISALLOW_DATA_ROAMING, R.string.data_roaming, R.drawable.network_cell_fill0)
        if(VERSION.SDK_INT>=34) {
            list += Restriction(UserManager.DISALLOW_CELLULAR_2G, R.string.cellular_2g, R.drawable.network_cell_fill0)
            list += Restriction(UserManager.DISALLOW_ULTRA_WIDEBAND_RADIO, R.string.ultra_wideband_radio, R.drawable.wifi_tethering_fill0)
        }
        if(VERSION.SDK_INT>=33) {
            list += Restriction(UserManager.DISALLOW_ADD_WIFI_CONFIG, R.string.add_wifi_conf, R.drawable.wifi_fill0)
            list += Restriction(UserManager.DISALLOW_CHANGE_WIFI_STATE, R.string.change_wifi_state, R.drawable.wifi_fill0)
            list += Restriction(UserManager.DISALLOW_WIFI_DIRECT, R.string.wifi_direct, R.drawable.wifi_tethering_fill0)
            list += Restriction(UserManager.DISALLOW_WIFI_TETHERING, R.string.wifi_tethering, R.drawable.wifi_tethering_fill0)
            list += Restriction(UserManager.DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI, R.string.share_admin_wifi, R.drawable.share_fill0)
        }
        if(VERSION.SDK_INT>=23) list += Restriction(UserManager.DISALLOW_NETWORK_RESET, R.string.network_reset, R.drawable.reset_wrench_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_TETHERING, R.string.config_tethering, R.drawable.wifi_tethering_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_VPN, R.string.config_vpn, R.drawable.vpn_key_fill0)
        if(VERSION.SDK_INT>=29) list += Restriction(UserManager.DISALLOW_CONFIG_PRIVATE_DNS, R.string.config_private_dns, R.drawable.dns_fill0)
        if(VERSION.SDK_INT>=28) list += Restriction(UserManager.DISALLOW_AIRPLANE_MODE, R.string.airplane_mode, R.drawable.airplanemode_active_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, R.string.config_cell_broadcasts, R.drawable.cell_tower_fill0)
        list += Restriction(UserManager.DISALLOW_SMS, R.string.sms, R.drawable.sms_fill0)
        list += Restriction(UserManager.DISALLOW_OUTGOING_CALLS, R.string.outgoing_calls, R.drawable.phone_forwarded_fill0)
        return list
    }
    val connectivity: List<Restriction> get() {
        val list = mutableListOf<Restriction>()
        if(VERSION.SDK_INT>=26) {
            list += Restriction(UserManager.DISALLOW_BLUETOOTH, R.string.bluetooth, R.drawable.bluetooth_fill0)
            list += Restriction(UserManager.DISALLOW_BLUETOOTH_SHARING, R.string.bt_share, R.drawable.bluetooth_searching_fill0)
        }
        list += Restriction(UserManager.DISALLOW_SHARE_LOCATION, R.string.share_location, R.drawable.location_on_fill0)
        if(VERSION.SDK_INT>=28) list += Restriction(UserManager.DISALLOW_CONFIG_LOCATION, R.string.config_location, R.drawable.location_on_fill0)
        if(VERSION.SDK_INT>=22) list += Restriction(UserManager.DISALLOW_OUTGOING_BEAM, R.string.outgoing_beam, R.drawable.nfc_fill0)
        list += Restriction(UserManager.DISALLOW_USB_FILE_TRANSFER, R.string.usb_file_transfer, R.drawable.usb_fill0)
        list += Restriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, R.string.mount_physical_media, R.drawable.sd_card_fill0)
        if(VERSION.SDK_INT>=28) list += Restriction(UserManager.DISALLOW_PRINTING, R.string.printing, R.drawable.print_fill0)
        return list
    }
    val applications: List<Restriction> get() {
        val list = mutableListOf<Restriction>()
        list += Restriction(UserManager.DISALLOW_INSTALL_APPS, R.string.install_app, R.drawable.android_fill0)
        if(VERSION.SDK_INT>=29) list += Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, R.string.install_unknown_src_globally, R.drawable.android_fill0)
        list += Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, R.string.inst_unknown_src, R.drawable.android_fill0)
        list += Restriction(UserManager.DISALLOW_UNINSTALL_APPS, R.string.uninstall_app, R.drawable.delete_fill0)
        list += Restriction(UserManager.DISALLOW_APPS_CONTROL, R.string.apps_ctrl, R.drawable.apps_fill0)
        if(VERSION.SDK_INT>=34) list += Restriction(UserManager.DISALLOW_CONFIG_DEFAULT_APPS, R.string.config_default_apps, R.drawable.apps_fill0)
        return list
    }
    val media: List<Restriction> get() {
        val list = mutableListOf<Restriction>()
        if(VERSION.SDK_INT>=28) {
            list += Restriction(UserManager.DISALLOW_CONFIG_BRIGHTNESS, R.string.config_brightness, R.drawable.brightness_5_fill0)
            list += Restriction(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT, R.string.config_scr_timeout, R.drawable.screen_lock_portrait_fill0)
            list += Restriction(UserManager.DISALLOW_AMBIENT_DISPLAY, R.string.ambient_display, R.drawable.brightness_5_fill0)
        }
        list += Restriction(UserManager.DISALLOW_ADJUST_VOLUME, R.string.adjust_volume, R.drawable.volume_up_fill0)
        list += Restriction(UserManager.DISALLOW_UNMUTE_MICROPHONE, R.string.unmute_microphone, R.drawable.mic_fill0)
        if(VERSION.SDK_INT>=31) {
            list += Restriction(UserManager.DISALLOW_CAMERA_TOGGLE, R.string.camera_toggle, R.drawable.cameraswitch_fill0)
            list += Restriction(UserManager.DISALLOW_MICROPHONE_TOGGLE, R.string.microphone_toggle, R.drawable.mic_fill0)
        }
        return list
    }
    val users: List<Restriction> get() {
        val list = mutableListOf<Restriction>()
        list += Restriction(UserManager.DISALLOW_ADD_USER, R.string.add_user, R.drawable.account_circle_fill0)
        list += Restriction(UserManager.DISALLOW_REMOVE_USER, R.string.remove_user, R.drawable.account_circle_fill0)
        if(VERSION.SDK_INT>=28) list += Restriction(UserManager.DISALLOW_USER_SWITCH, R.string.switch_user, R.drawable.account_circle_fill0)
        if(VERSION.SDK_INT>=24) list += Restriction(UserManager.DISALLOW_SET_USER_ICON, R.string.set_user_icon, R.drawable.account_circle_fill0)
        list += Restriction(UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE, R.string.cross_profile_copy, R.drawable.content_paste_fill0)
        if(VERSION.SDK_INT>=28) {
            list += Restriction(UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE, R.string.share_into_managed_profile, R.drawable.share_fill0)
            list += Restriction(UserManager.DISALLOW_UNIFIED_PASSWORD, R.string.unified_pwd, R.drawable.work_fill0)
        }
        return list
    }
    val other: List<Restriction> get() {
        val list = mutableListOf<Restriction>()
        if(VERSION.SDK_INT>=26) list += Restriction(UserManager.DISALLOW_AUTOFILL, R.string.autofill, R.drawable.password_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_CREDENTIALS, R.string.config_credentials, R.drawable.android_fill0)
        if(VERSION.SDK_INT>=29) {
            list += Restriction(UserManager.DISALLOW_CONTENT_CAPTURE, R.string.content_capture, R.drawable.screenshot_fill0)
            list += Restriction(UserManager.DISALLOW_CONTENT_SUGGESTIONS, R.string.content_suggestions, R.drawable.sms_fill0)
        }
        list += Restriction(UserManager.DISALLOW_CREATE_WINDOWS, R.string.create_windows, R.drawable.web_asset)
        if(VERSION.SDK_INT>=24) list += Restriction(UserManager.DISALLOW_SET_WALLPAPER, R.string.set_wallpaper, R.drawable.wallpaper_fill0)
        if(VERSION.SDK_INT>=34) list += Restriction(UserManager.DISALLOW_GRANT_ADMIN, R.string.grant_admin, R.drawable.security_fill0)
        if(VERSION.SDK_INT>=23) list += Restriction(UserManager.DISALLOW_FUN, R.string.`fun`, R.drawable.stadia_controller_fill0)
        list += Restriction(UserManager.DISALLOW_MODIFY_ACCOUNTS, R.string.modify_accounts, R.drawable.manage_accounts_fill0)
        if(VERSION.SDK_INT>=28) {
            list += Restriction(UserManager.DISALLOW_CONFIG_LOCALE, R.string.config_locale, R.drawable.language_fill0)
            list += Restriction(UserManager.DISALLOW_CONFIG_DATE_TIME, R.string.config_date_time, R.drawable.schedule_fill0)
        }
        if(VERSION.SDK_INT>=28) list += Restriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, R.string.sys_err_dialog, R.drawable.warning_fill0)
        list += Restriction(UserManager.DISALLOW_FACTORY_RESET, R.string.factory_reset, R.drawable.android_fill0)
        if(VERSION.SDK_INT>=23) list += Restriction(UserManager.DISALLOW_SAFE_BOOT, R.string.safe_boot, R.drawable.security_fill0)
        list += Restriction(UserManager.DISALLOW_DEBUGGING_FEATURES, R.string.debug_features, R.drawable.adb_fill0)
        return list
    }
    fun getAllRestrictions(): List<Restriction> =
         internet + connectivity + media + applications + users + other

}
