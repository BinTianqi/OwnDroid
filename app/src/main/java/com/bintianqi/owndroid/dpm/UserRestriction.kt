package com.bintianqi.owndroid.dpm

import android.os.Build
import android.os.Bundle
import android.os.UserManager
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.myPrivilege
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.zhCN
import kotlinx.serialization.Serializable

@Serializable
data class Restriction(
    val id: String,
    @StringRes val name: Int,
    @DrawableRes val icon: Int,
    val requiresApi: Int = 0
)

@Serializable object UserRestriction

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(24)
@Composable
fun UserRestrictionScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val privilege by myPrivilege.collectAsStateWithLifecycle()
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    fun navigateToOptions(title: Int, items: List<Restriction>) {
        onNavigate(UserRestrictionOptions(title, items))
    }
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.user_restriction)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    IconButton({ onNavigate(UserRestrictionEditor) }) {
                        Icon(Icons.Default.Edit, null)
                    }
                },
                scrollBehavior = sb
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            Spacer(Modifier.padding(vertical = 2.dp))
            Text(text = stringResource(R.string.switch_to_disable_feature), modifier = Modifier.padding(start = 16.dp))
            if(privilege.profile) { Text(text = stringResource(R.string.profile_owner_is_restricted), modifier = Modifier.padding(start = 16.dp)) }
            if(privilege.work) {
                Text(text = stringResource(R.string.some_features_invalid_in_work_profile), modifier = Modifier.padding(start = 16.dp))
            }
            Spacer(Modifier.padding(vertical = 2.dp))
            FunctionItem(R.string.network, icon = R.drawable.language_fill0) {
                navigateToOptions(R.string.network, RestrictionData.internet)
            }
            FunctionItem(R.string.connectivity, icon = R.drawable.devices_other_fill0) {
                navigateToOptions(R.string.connectivity, RestrictionData.connectivity)
            }
            FunctionItem(R.string.applications, icon = R.drawable.apps_fill0) {
                navigateToOptions(R.string.applications, RestrictionData.applications)
            }
            FunctionItem(R.string.users, icon = R.drawable.account_circle_fill0) {
                navigateToOptions(R.string.users, RestrictionData.users)
            }
            FunctionItem(R.string.media, icon = R.drawable.volume_up_fill0) {
                navigateToOptions(R.string.media, RestrictionData.media)
            }
            FunctionItem(R.string.other, icon = R.drawable.more_horiz_fill0) {
                navigateToOptions(R.string.other, RestrictionData.other)
            }
        }
    }
}

@Serializable
data class UserRestrictionOptions(
    val title: Int, val items: List<Restriction>
)

@RequiresApi(24)
@Composable
fun UserRestrictionOptionsScreen(
    data: UserRestrictionOptions, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val status = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(Unit) {
        val restrictions = dpm.getUserRestrictions(receiver)
        data.items.forEach {
            status.put(it.id, restrictions.getBoolean(it.id))
        }
    }
    MyLazyScaffold(data.title, onNavigateUp) {
        items(data.items.filter { Build.VERSION.SDK_INT >= it.requiresApi }) { restriction ->
            Row(
                Modifier.fillMaxWidth().padding(15.dp, 6.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(Modifier.fillMaxWidth(0.8F), verticalAlignment = Alignment.CenterVertically) {
                    Icon(painterResource(restriction.icon), null, Modifier.padding(start = 6.dp, end = 16.dp))
                    Column {
                        Text(stringResource(restriction.name), style = typography.titleMedium)
                        Text(
                            restriction.id, style = typography.bodyMedium,
                            color = colorScheme.onBackground.copy(alpha = 0.8F)
                        )
                    }
                }
                Switch(
                    status[restriction.id] == true,
                    {
                        if (it) {
                            dpm.addUserRestriction(receiver, restriction.id)
                        } else {
                            dpm.clearUserRestriction(receiver, restriction.id)
                        }
                        status[restriction.id] = dpm.getUserRestrictions(receiver).getBoolean(restriction.id)
                    }
                )
            }
        }
        item {
            Spacer(Modifier.padding(vertical = 30.dp))
        }
    }
}

@Suppress("InlinedApi")
object RestrictionData {
    val internet = listOf(
        Restriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, R.string.config_mobile_network, R.drawable.signal_cellular_alt_fill0),
        Restriction(UserManager.DISALLOW_CONFIG_WIFI, R.string.config_wifi, R.drawable.wifi_fill0),
        Restriction(UserManager.DISALLOW_DATA_ROAMING, R.string.data_roaming, R.drawable.network_cell_fill0, 24),
        Restriction(UserManager.DISALLOW_CELLULAR_2G, R.string.cellular_2g, R.drawable.network_cell_fill0, 34),
        Restriction(UserManager.DISALLOW_ULTRA_WIDEBAND_RADIO, R.string.ultra_wideband_radio, R.drawable.wifi_tethering_fill0, 34),
        Restriction(UserManager.DISALLOW_ADD_WIFI_CONFIG, R.string.add_wifi_conf, R.drawable.wifi_fill0, 33),
        Restriction(UserManager.DISALLOW_CHANGE_WIFI_STATE, R.string.change_wifi_state, R.drawable.wifi_fill0, 33),
        Restriction(UserManager.DISALLOW_WIFI_DIRECT, R.string.wifi_direct, R.drawable.wifi_tethering_fill0),
        Restriction(UserManager.DISALLOW_WIFI_TETHERING, R.string.wifi_tethering, R.drawable.wifi_tethering_fill0, 33),
        Restriction(UserManager.DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI, R.string.share_admin_wifi, R.drawable.share_fill0, 33),
        Restriction(UserManager.DISALLOW_NETWORK_RESET, R.string.network_reset, R.drawable.reset_wrench_fill0, 23),
        Restriction(UserManager.DISALLOW_CONFIG_TETHERING, R.string.config_tethering, R.drawable.wifi_tethering_fill0),
        Restriction(UserManager.DISALLOW_CONFIG_VPN, R.string.config_vpn, R.drawable.vpn_key_fill0),
        Restriction(UserManager.DISALLOW_CONFIG_PRIVATE_DNS, R.string.config_private_dns, R.drawable.dns_fill0, 29),
        Restriction(UserManager.DISALLOW_AIRPLANE_MODE, R.string.airplane_mode, R.drawable.airplanemode_active_fill0, 28),
        Restriction(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, R.string.config_cell_broadcasts, R.drawable.cell_tower_fill0),
        Restriction(UserManager.DISALLOW_SMS, R.string.sms, R.drawable.sms_fill0),
        Restriction(UserManager.DISALLOW_OUTGOING_CALLS, R.string.outgoing_calls, R.drawable.phone_forwarded_fill0),
        Restriction(UserManager.DISALLOW_SIM_GLOBALLY, R.string.download_esim, R.drawable.sim_card_download_fill0)
    )
    val connectivity = listOf(
        Restriction(UserManager.DISALLOW_BLUETOOTH, R.string.bluetooth, R.drawable.bluetooth_fill0, 26),
        Restriction(UserManager.DISALLOW_BLUETOOTH_SHARING, R.string.bt_share, R.drawable.bluetooth_searching_fill0, 26),
        Restriction(UserManager.DISALLOW_SHARE_LOCATION, R.string.share_location, R.drawable.location_on_fill0),
        Restriction(UserManager.DISALLOW_CONFIG_LOCATION, R.string.config_location, R.drawable.location_on_fill0, 28),
        Restriction(UserManager.DISALLOW_NEAR_FIELD_COMMUNICATION_RADIO, R.string.nfc, R.drawable.nfc_fill0, 35),
        Restriction(UserManager.DISALLOW_OUTGOING_BEAM, R.string.outgoing_beam, R.drawable.nfc_fill0, 22),
        Restriction(UserManager.DISALLOW_USB_FILE_TRANSFER, R.string.usb_file_transfer, R.drawable.usb_fill0),
        Restriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA, R.string.mount_physical_media, R.drawable.sd_card_fill0),
        Restriction(UserManager.DISALLOW_PRINTING, R.string.printing, R.drawable.print_fill0, 28)
    )
    val applications = listOf(
        Restriction(UserManager.DISALLOW_INSTALL_APPS, R.string.install_app, R.drawable.android_fill0),
        Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, R.string.install_unknown_src_globally, R.drawable.android_fill0, 29),
        Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, R.string.inst_unknown_src, R.drawable.android_fill0),
        Restriction(UserManager.DISALLOW_UNINSTALL_APPS, R.string.uninstall_app, R.drawable.delete_fill0),
        Restriction(UserManager.DISALLOW_APPS_CONTROL, R.string.apps_ctrl, R.drawable.apps_fill0),
        Restriction(UserManager.DISALLOW_CONFIG_DEFAULT_APPS, R.string.config_default_apps, R.drawable.apps_fill0, 34)
    )
    val media = listOf(
        Restriction(UserManager.DISALLOW_CONFIG_BRIGHTNESS, R.string.config_brightness, R.drawable.brightness_5_fill0, 28),
        Restriction(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT, R.string.config_scr_timeout, R.drawable.screen_lock_portrait_fill0, 28),
        Restriction(UserManager.DISALLOW_AMBIENT_DISPLAY, R.string.ambient_display, R.drawable.brightness_5_fill0, 28),
        Restriction(UserManager.DISALLOW_ADJUST_VOLUME, R.string.adjust_volume, R.drawable.volume_up_fill0),
        Restriction(UserManager.DISALLOW_UNMUTE_MICROPHONE, R.string.unmute_microphone, R.drawable.mic_fill0),
        Restriction(UserManager.DISALLOW_CAMERA_TOGGLE, R.string.camera_toggle, R.drawable.cameraswitch_fill0, 31),
        Restriction(UserManager.DISALLOW_MICROPHONE_TOGGLE, R.string.microphone_toggle, R.drawable.mic_fill0, 31)
    )
    val users = listOf(
        Restriction(UserManager.DISALLOW_ADD_USER, R.string.add_user, R.drawable.account_circle_fill0),
        Restriction(UserManager.DISALLOW_REMOVE_USER, R.string.remove_user, R.drawable.account_circle_fill0),
        Restriction(UserManager.DISALLOW_USER_SWITCH, R.string.switch_user, R.drawable.account_circle_fill0, 28),
        Restriction(UserManager.DISALLOW_ADD_MANAGED_PROFILE, R.string.create_work_profile, R.drawable.work_fill0, 26),
        Restriction(UserManager.DISALLOW_REMOVE_MANAGED_PROFILE, R.string.delete_work_profile, R.drawable.delete_forever_fill0, 26),
        Restriction(UserManager.DISALLOW_ADD_PRIVATE_PROFILE, R.string.create_private_space, R.drawable.lock_fill0, 35),
        Restriction(UserManager.DISALLOW_SET_USER_ICON, R.string.set_user_icon, R.drawable.account_circle_fill0, 24),
        Restriction(UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE, R.string.cross_profile_copy, R.drawable.content_paste_fill0),
        Restriction(UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE, R.string.share_into_managed_profile, R.drawable.share_fill0, 28),
        Restriction(UserManager.DISALLOW_UNIFIED_PASSWORD, R.string.unified_pwd, R.drawable.work_fill0, 28)
    )
    val other = listOf(
        Restriction(UserManager.DISALLOW_AUTOFILL, R.string.autofill, R.drawable.password_fill0, 26),
        Restriction(UserManager.DISALLOW_CONFIG_CREDENTIALS, R.string.config_credentials, R.drawable.android_fill0),
        Restriction(UserManager.DISALLOW_CONTENT_CAPTURE, R.string.content_capture, R.drawable.screenshot_fill0, 29),
        Restriction(UserManager.DISALLOW_CONTENT_SUGGESTIONS, R.string.content_suggestions, R.drawable.sms_fill0, 29),
        Restriction(UserManager.DISALLOW_ASSIST_CONTENT, R.string.assist_content, R.drawable.info_fill0, 35),
        Restriction(UserManager.DISALLOW_CREATE_WINDOWS, R.string.create_windows, R.drawable.web_asset),
        Restriction(UserManager.DISALLOW_SET_WALLPAPER, R.string.set_wallpaper, R.drawable.wallpaper_fill0, 24),
        Restriction(UserManager.DISALLOW_GRANT_ADMIN, R.string.grant_admin, R.drawable.security_fill0, 34),
        Restriction(UserManager.DISALLOW_FUN, R.string.`fun`, R.drawable.stadia_controller_fill0, 23),
        Restriction(UserManager.DISALLOW_MODIFY_ACCOUNTS, R.string.modify_accounts, R.drawable.manage_accounts_fill0),
        Restriction(UserManager.DISALLOW_CONFIG_LOCALE, R.string.config_locale, R.drawable.language_fill0, 28),
        Restriction(UserManager.DISALLOW_CONFIG_DATE_TIME, R.string.config_date_time, R.drawable.schedule_fill0, 28),
        Restriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS, R.string.sys_err_dialog, R.drawable.warning_fill0, 28),
        Restriction(UserManager.DISALLOW_FACTORY_RESET, R.string.factory_reset, R.drawable.android_fill0),
        Restriction(UserManager.DISALLOW_SAFE_BOOT, R.string.safe_boot, R.drawable.security_fill0, 23),
        Restriction(UserManager.DISALLOW_DEBUGGING_FEATURES, R.string.debug_features, R.drawable.adb_fill0)
    )
    fun getAllRestrictions() = internet + connectivity + media + applications + users + other
}

@Serializable object UserRestrictionEditor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserRestrictionEditorScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val list = remember { mutableStateListOf<String>() }
    fun refresh() {
        val restrictions = dpm.getUserRestrictions(receiver)
        list.clear()
        list.addAll(restrictions.keySet().filter { restrictions.getBoolean(it) })
    }
    LaunchedEffect(Unit) { refresh() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit)) },
                navigationIcon = { NavIcon(onNavigateUp) }
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        LazyColumn(Modifier.fillMaxSize().padding(paddingValues)) {
            items(list, { it }) {
                Row(
                    Modifier.fillMaxWidth().padding(HorizontalPadding, 2.dp).animateItem(),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Text(it)
                    IconButton({
                        dpm.clearUserRestriction(receiver, it)
                        refresh()
                    }) {
                        Icon(Icons.Outlined.Delete, null)
                    }
                }
            }
            item {
                var input by remember { mutableStateOf("") }
                fun add() {
                    dpm.addUserRestriction(receiver, input)
                    refresh()
                    input = ""
                }
                OutlinedTextField(
                    input, { input = it }, Modifier.fillMaxWidth().padding(HorizontalPadding, 20.dp),
                    label = { Text("id") },
                    trailingIcon = {
                        IconButton(::add, enabled = input.isNotBlank()) {
                            Icon(Icons.Default.Add, null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions { add() }
                )
            }
        }
    }
}
