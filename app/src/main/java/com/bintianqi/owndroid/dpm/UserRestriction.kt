package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build.VERSION
import android.os.UserManager
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.TopBar

data class Restriction(
    val restriction:String,
    @StringRes val name:Int,
    val desc:String,
    @DrawableRes val ico:Int
)

@Composable
fun UserRestriction(navCtrl: NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    /*val titleMap = mapOf(
        "Internet" to R.string.network_internet,
        "Connectivity" to R.string.more_connectivity,
        "Users" to R.string.users,
        "Media" to R.string.media,
        "Applications" to R.string.applications,
        "Other" to R.string.other
    )*/
    Scaffold(
        topBar = {
            /*TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.user_restrict))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )*/
            TopBar(backStackEntry,navCtrl,localNavCtrl){
                if(backStackEntry?.destination?.route=="Home"&&scrollState.maxValue>80){
                    Text(
                        text = stringResource(R.string.user_restrict),
                        modifier = Modifier.alpha((maxOf(scrollState.value-30, 0)).toFloat() / 80)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition,
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            composable(route = "Internet") { Internet() }
            composable(route = "Home") { Home(localNavCtrl,scrollState) }
            composable(route = "Connectivity") { Connectivity() }
            composable(route = "Applications") { Application() }
            composable(route = "Users") { User() }
            composable(route = "Media") { Media() }
            composable(route = "Other") { Other() }
        }
    }
}

@Composable
private fun Home(navCtrl:NavHostController, scrollState: ScrollState) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Text(
            text = stringResource(R.string.user_restrict),
            style = typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 7.dp, start = 15.dp)
        )
        Text(text = stringResource(R.string.switch_to_disable_feature), modifier = Modifier.padding(start = 15.dp))
        if(context.isProfileOwner) { Text(text = stringResource(R.string.profile_owner_is_restricted), modifier = Modifier.padding(start = 15.dp)) }
        if(context.isProfileOwner && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)))) {
            Text(text = stringResource(R.string.some_features_invalid_in_work_profile), modifier = Modifier.padding(start = 15.dp))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        SubPageItem(R.string.network_internet, "", R.drawable.wifi_fill0) { navCtrl.navigate("Internet") }
        SubPageItem(R.string.more_connectivity, "", R.drawable.devices_other_fill0) { navCtrl.navigate("Connectivity") }
        SubPageItem(R.string.applications, "", R.drawable.apps_fill0) { navCtrl.navigate("Applications") }
        SubPageItem(R.string.users, "", R.drawable.account_circle_fill0) { navCtrl.navigate("Users") }
        SubPageItem(R.string.media, "", R.drawable.volume_up_fill0) { navCtrl.navigate("Media") }
        SubPageItem(R.string.other, "", R.drawable.more_horiz_fill0) { navCtrl.navigate("Other") }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun Internet() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        for(internetItem in RestrictionData.internet()) {
            UserRestrictionItem(internetItem.restriction, internetItem.name, internetItem.desc, internetItem.ico)
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Connectivity() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        for(connectivityItem in RestrictionData.connectivity()) {
            UserRestrictionItem(connectivityItem.restriction, connectivityItem.name, connectivityItem.desc, connectivityItem.ico)
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
fun Application() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        for(applicationItem in RestrictionData.application(context)) {
            UserRestrictionItem(applicationItem.restriction, applicationItem.name, applicationItem.desc, applicationItem.ico)
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun User() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        for(userItem in RestrictionData.user()) {
            UserRestrictionItem(userItem.restriction, userItem.name, userItem.desc, userItem.ico)
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Media() {
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        for(mediaItem in RestrictionData.media()) {
            UserRestrictionItem(mediaItem.restriction, mediaItem.name, mediaItem.desc, mediaItem.ico)
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Other() {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        for(otherItem in RestrictionData.other(context)) {
            UserRestrictionItem(otherItem.restriction, otherItem.name, otherItem.desc, otherItem.ico)
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun UserRestrictionItem(
    restriction:String, itemName:Int,
    restrictionDescription:String,
    leadIcon:Int
) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    SwitchItem(
        itemName,restrictionDescription,leadIcon,
        { if(context.isDeviceOwner||context.isProfileOwner) { dpm.getUserRestrictions(receiver).getBoolean(restriction) }else{ false } },
        {
            try{
                if(it) {
                    dpm.addUserRestriction(receiver,restriction)
                }else{
                    dpm.clearUserRestriction(receiver,restriction)
                }
            }catch(e:SecurityException) {
                if(context.isProfileOwner) {
                    Toast.makeText(context, R.string.require_device_owner, Toast.LENGTH_SHORT).show()
                }
            }
        },
        context.isDeviceOwner||context.isProfileOwner
    )
}

object RestrictionData {
    fun internet(): List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        list += Restriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, R.string.config_mobile_network, "", R.drawable.signal_cellular_alt_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_WIFI, R.string.config_wifi, "", R.drawable.wifi_fill0)
        if(VERSION.SDK_INT>=24) { list += Restriction(UserManager.DISALLOW_DATA_ROAMING, R.string.data_roaming, "", R.drawable.network_cell_fill0) }
        if(VERSION.SDK_INT>=34) {
            list += Restriction(UserManager.DISALLOW_CELLULAR_2G, R.string.cellular_2g, "", R.drawable.network_cell_fill0)
            list += Restriction(UserManager.DISALLOW_ULTRA_WIDEBAND_RADIO, R.string.ultra_wideband_radio, "", R.drawable.wifi_tethering_fill0)
        }
        if(VERSION.SDK_INT>=33) {
            list += Restriction(UserManager.DISALLOW_ADD_WIFI_CONFIG, R.string.add_wifi_conf, "", R.drawable.wifi_fill0)
            list += Restriction(UserManager.DISALLOW_CHANGE_WIFI_STATE, R.string.change_wifi_state, "", R.drawable.wifi_fill0)
            list += Restriction(UserManager.DISALLOW_WIFI_DIRECT, R.string.wifi_direct, "", R.drawable.wifi_tethering_fill0)
            list += Restriction(UserManager.DISALLOW_WIFI_TETHERING, R.string.wifi_tethering, "", R.drawable.wifi_tethering_fill0)
            list += Restriction(UserManager.DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI, R.string.share_admin_wifi, "", R.drawable.share_fill0)
        }
        if(VERSION.SDK_INT>=23) { list += Restriction(UserManager.DISALLOW_NETWORK_RESET, R.string.network_reset, "", R.drawable.reset_wrench_fill0) }
        list += Restriction(UserManager.DISALLOW_CONFIG_TETHERING, R.string.config_tethering, "", R.drawable.wifi_tethering_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_VPN, R.string.config_vpn, "", R.drawable.vpn_key_fill0)
        if(VERSION.SDK_INT>=29) { list += Restriction(UserManager.DISALLOW_CONFIG_PRIVATE_DNS, R.string.config_private_dns, "", R.drawable.dns_fill0) }
        if(VERSION.SDK_INT>=28) { list += Restriction(UserManager.DISALLOW_AIRPLANE_MODE, R.string.airplane_mode, "", R.drawable.airplanemode_active_fill0) }
        list += Restriction(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, R.string.config_cell_broadcasts, "", R.drawable.cell_tower_fill0)
        list += Restriction(UserManager.DISALLOW_SMS, R.string.sms, "", R.drawable.sms_fill0)
        list += Restriction(UserManager.DISALLOW_OUTGOING_CALLS, R.string.outgoing_calls, "", R.drawable.phone_forwarded_fill0)
        return list
    }
    fun connectivity(): List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        if(VERSION.SDK_INT>=26) {
            list += Restriction(UserManager.DISALLOW_BLUETOOTH, R.string.bluetooth, "", R.drawable.bluetooth_fill0)
            list += Restriction(UserManager.DISALLOW_BLUETOOTH_SHARING, R.string.bt_share, "", R.drawable.bluetooth_searching_fill0)
        }
        list += Restriction(UserManager.DISALLOW_SHARE_LOCATION, R.string.share_location, "", R.drawable.location_on_fill0)
        if(VERSION.SDK_INT>=28) { list += Restriction(UserManager.DISALLOW_CONFIG_LOCATION, R.string.config_location, "", R.drawable.location_on_fill0) }
        if(VERSION.SDK_INT>=22) { list += Restriction(UserManager.DISALLOW_OUTGOING_BEAM, R.string.outgoing_beam, "", R.drawable.nfc_fill0) }
        list += Restriction(UserManager.DISALLOW_USB_FILE_TRANSFER, R.string.usb_file_transfer, "", R.drawable.usb_fill0)
        list += Restriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, R.string.mount_physical_media, "", R.drawable.sd_card_fill0)
        if(VERSION.SDK_INT>=28) { list += Restriction(UserManager.DISALLOW_PRINTING, R.string.printing, "", R.drawable.print_fill0) }
        return list
    }
    fun application(context: Context): List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        list += Restriction(UserManager.DISALLOW_INSTALL_APPS, R.string.install_app, "", R.drawable.android_fill0)
        if(VERSION.SDK_INT>=29) { list += Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, R.string.install_unknown_src_globally, "", R.drawable.android_fill0) }
        list += Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, R.string.inst_unknown_src, "", R.drawable.android_fill0)
        list += Restriction(UserManager.DISALLOW_UNINSTALL_APPS, R.string.uninstall_app, "", R.drawable.delete_fill0)
        list += Restriction(UserManager.DISALLOW_APPS_CONTROL, R.string.apps_ctrl, context.getString(R.string.apps_control_desc), R.drawable.apps_fill0)
        if(VERSION.SDK_INT>=34) { list += Restriction(UserManager.DISALLOW_CONFIG_DEFAULT_APPS, R.string.config_default_apps, "", R.drawable.apps_fill0) }
        return list
    }
    fun media(): List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        if(VERSION.SDK_INT>=28) {
            list += Restriction(UserManager.DISALLOW_CONFIG_BRIGHTNESS, R.string.config_brightness, "", R.drawable.brightness_5_fill0)
            list += Restriction(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT, R.string.config_scr_timeout, "", R.drawable.screen_lock_portrait_fill0)
            list += Restriction(UserManager.DISALLOW_AMBIENT_DISPLAY, R.string.ambient_display, "", R.drawable.brightness_5_fill0)
        }
        list += Restriction(UserManager.DISALLOW_ADJUST_VOLUME, R.string.adjust_volume, "", R.drawable.volume_up_fill0)
        list += Restriction(UserManager.DISALLOW_UNMUTE_MICROPHONE, R.string.unmute_microphone, "", R.drawable.mic_fill0)
        if(VERSION.SDK_INT>=31) {
            list += Restriction(UserManager.DISALLOW_CAMERA_TOGGLE, R.string.camera_toggle, "", R.drawable.cameraswitch_fill0)
            list += Restriction(UserManager.DISALLOW_MICROPHONE_TOGGLE, R.string.microphone_toggle, "", R.drawable.mic_fill0)
        }
        return list
    }
    fun user(): List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        list += Restriction(UserManager.DISALLOW_ADD_USER, R.string.add_user, "", R.drawable.account_circle_fill0)
        list += Restriction(UserManager.DISALLOW_REMOVE_USER, R.string.remove_user, "", R.drawable.account_circle_fill0)
        if(VERSION.SDK_INT>=28) { list += Restriction(UserManager.DISALLOW_USER_SWITCH, R.string.switch_user, "", R.drawable.account_circle_fill0) }
        if(VERSION.SDK_INT>=24) { list += Restriction(UserManager.DISALLOW_SET_USER_ICON, R.string.set_user_icon, "", R.drawable.account_circle_fill0) }
        list += Restriction(UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE, R.string.cross_profile_copy, "", R.drawable.content_paste_fill0)
        if(VERSION.SDK_INT>=28) {
            list += Restriction(UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE, R.string.share_into_managed_profile, "", R.drawable.share_fill0)
            list += Restriction(UserManager.DISALLOW_UNIFIED_PASSWORD, R.string.unified_pwd, "", R.drawable.work_fill0)
        }
        return list
    }
    fun other(context: Context): List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        if(VERSION.SDK_INT>=26) { list += Restriction(UserManager.DISALLOW_AUTOFILL, R.string.autofill, "", R.drawable.password_fill0) }
        list += Restriction(UserManager.DISALLOW_CONFIG_CREDENTIALS, R.string.config_credentials, "", R.drawable.android_fill0)
        if(VERSION.SDK_INT>=29) {
            list += Restriction(UserManager.DISALLOW_CONTENT_CAPTURE, R.string.content_capture, "", R.drawable.screenshot_fill0)
            list += Restriction(UserManager.DISALLOW_CONTENT_SUGGESTIONS, R.string.content_suggestions, "", R.drawable.sms_fill0)
        }
        list += Restriction(UserManager.DISALLOW_CREATE_WINDOWS, R.string.create_windows, context.getString(R.string.create_windows_desc), R.drawable.web_asset)
        if(VERSION.SDK_INT>=24) { list += Restriction(UserManager.DISALLOW_SET_WALLPAPER, R.string.set_wallpaper, "", R.drawable.wallpaper_fill0) }
        if(VERSION.SDK_INT>=34) { list += Restriction(UserManager.DISALLOW_GRANT_ADMIN, R.string.grant_admin, "", R.drawable.security_fill0) }
        if(VERSION.SDK_INT>=23) { list += Restriction(UserManager.DISALLOW_FUN, R.string.`fun`, context.getString(R.string.fun_desc), R.drawable.stadia_controller_fill0) }
        list += Restriction(UserManager.DISALLOW_MODIFY_ACCOUNTS, R.string.modify_accounts, "", R.drawable.manage_accounts_fill0)
        if(VERSION.SDK_INT>=28) {
            list += Restriction(UserManager.DISALLOW_CONFIG_LOCALE, R.string.config_locale, "", R.drawable.language_fill0)
            list += Restriction(UserManager.DISALLOW_CONFIG_DATE_TIME, R.string.config_date_time, "", R.drawable.schedule_fill0)
        }
        if(VERSION.SDK_INT>=28) { list += Restriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, R.string.sys_err_dialog, "", R.drawable.warning_fill0) }
        list += Restriction(UserManager.DISALLOW_FACTORY_RESET, R.string.factory_reset, "", R.drawable.android_fill0)
        if(VERSION.SDK_INT>=23) { list += Restriction(UserManager.DISALLOW_SAFE_BOOT, R.string.safe_boot, "", R.drawable.security_fill0) }
        list += Restriction(UserManager.DISALLOW_DEBUGGING_FEATURES, R.string.debug_features, "", R.drawable.adb_fill0)
        return list
    }
    fun getAllRestrictions(context: Context): List<String> {
        val result = mutableListOf<String>()
        internet().forEach { result.add(it.restriction) }
        connectivity().forEach { result.add(it.restriction) }
        media().forEach { result.add(it.restriction) }
        application(context).forEach { result.add(it.restriction) }
        user().forEach { result.add(it.restriction) }
        other(context).forEach { result.add(it.restriction) }
        return result
    }
}
