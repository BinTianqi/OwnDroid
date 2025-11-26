package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.MTE_DISABLED
import android.app.admin.DevicePolicyManager.MTE_ENABLED
import android.app.admin.DevicePolicyManager.MTE_NOT_CONTROLLED_BY_POLICY
import android.app.admin.DevicePolicyManager.NEARBY_STREAMING_DISABLED
import android.app.admin.DevicePolicyManager.NEARBY_STREAMING_ENABLED
import android.app.admin.DevicePolicyManager.NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY
import android.app.admin.DevicePolicyManager.NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY
import android.app.admin.DevicePolicyManager.PERMISSION_POLICY_AUTO_DENY
import android.app.admin.DevicePolicyManager.PERMISSION_POLICY_AUTO_GRANT
import android.app.admin.DevicePolicyManager.PERMISSION_POLICY_PROMPT
import android.app.admin.DevicePolicyManager.WIPE_EUICC
import android.app.admin.DevicePolicyManager.WIPE_EXTERNAL_STORAGE
import android.app.admin.DevicePolicyManager.WIPE_RESET_PROTECTION_DATA
import android.app.admin.DevicePolicyManager.WIPE_SILENTLY
import android.app.admin.SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC
import android.app.admin.SystemUpdatePolicy.TYPE_INSTALL_WINDOWED
import android.app.admin.SystemUpdatePolicy.TYPE_POSTPONE
import android.content.Context
import android.net.Uri
import android.os.Build.VERSION
import android.os.HardwarePropertiesManager
import android.os.UserManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.AppInfo
import com.bintianqi.owndroid.BottomPadding
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SP
import com.bintianqi.owndroid.clickableTextField
import com.bintianqi.owndroid.formatDate
import com.bintianqi.owndroid.adaptiveInsets
import com.bintianqi.owndroid.popToast
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.ErrorDialog
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.yesOrNo
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.roundToLong

@Serializable object SystemManager

@Composable
fun SystemManagerScreen(
    vm: MyViewModel, onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit
) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    /** 1: reboot, 2: bug report, 3: org name, 4: org id, 5: enrollment specific id*/
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    MyScaffold(R.string.system, onNavigateUp, 0.dp) {
        FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(SystemOptions) }
        FunctionItem(R.string.keyguard, icon = R.drawable.screen_lock_portrait_fill0) { onNavigate(Keyguard) }
        if(VERSION.SDK_INT >= 24 && privilege.device && !privilege.dhizuku)
            FunctionItem(R.string.hardware_monitor, icon = R.drawable.memory_fill0) { onNavigate(HardwareMonitor) }
        if(VERSION.SDK_INT >= 24 && privilege.device) {
            FunctionItem(R.string.reboot, icon = R.drawable.restart_alt_fill0) { dialog = 1 }
        }
        if(VERSION.SDK_INT >= 24 && privilege.device && (VERSION.SDK_INT < 28 || privilege.affiliated)) {
            FunctionItem(R.string.bug_report, icon = R.drawable.bug_report_fill0) { dialog = 2 }
        }
        if(VERSION.SDK_INT >= 28 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.change_time, icon = R.drawable.schedule_fill0) { onNavigate(ChangeTime) }
            FunctionItem(R.string.change_timezone, icon = R.drawable.globe_fill0) { onNavigate(ChangeTimeZone) }
        }
        if (VERSION.SDK_INT >= 36 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.auto_time_policy, icon = R.drawable.schedule_fill0) { onNavigate(AutoTimePolicy) }
            FunctionItem(R.string.auto_timezone_policy, icon = R.drawable.globe_fill0) { onNavigate(AutoTimeZonePolicy) }
        }
        /*if(VERSION.SDK_INT >= 28 && (deviceOwner || profileOwner))
            FunctionItem(R.string.key_pairs, icon = R.drawable.key_vertical_fill0) { navCtrl.navigate("KeyPairs") }*/
        if(VERSION.SDK_INT >= 35 && (privilege.device || (privilege.profile && privilege.affiliated)))
            FunctionItem(R.string.content_protection_policy, icon = R.drawable.search_fill0) { onNavigate(ContentProtectionPolicy) }
        if(VERSION.SDK_INT >= 23) {
            FunctionItem(R.string.permission_policy, icon = R.drawable.key_fill0) { onNavigate(PermissionPolicy) }
        }
        if(VERSION.SDK_INT >= 34 && privilege.device) {
            FunctionItem(R.string.mte_policy, icon = R.drawable.memory_fill0) { onNavigate(MtePolicy) }
        }
        if(VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.nearby_streaming_policy, icon = R.drawable.share_fill0) { onNavigate(NearbyStreamingPolicy) }
        }
        if (VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.lock_task_mode, icon = R.drawable.lock_fill0) { onNavigate(LockTaskMode) }
        }
        FunctionItem(R.string.ca_cert, icon = R.drawable.license_fill0) { onNavigate(CaCert) }
        if(VERSION.SDK_INT >= 26 && !privilege.dhizuku && (privilege.device || privilege.org)) {
            FunctionItem(R.string.security_logging, icon = R.drawable.description_fill0) { onNavigate(SecurityLogging) }
        }
        FunctionItem(R.string.device_info, icon = R.drawable.perm_device_information_fill0) { onNavigate(DeviceInfo) }
        if(VERSION.SDK_INT >= 24 && (privilege.profile || (VERSION.SDK_INT >= 26 && privilege.device))) {
            FunctionItem(R.string.org_name, icon = R.drawable.corporate_fare_fill0) { dialog = 3 }
        }
        if(VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.org_id, icon = R.drawable.corporate_fare_fill0) { dialog = 4 }
        }
        if (VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.enrollment_specific_id, icon = R.drawable.id_card_fill0) { dialog = 5 }
        }
        if(VERSION.SDK_INT >= 24 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.lock_screen_info, icon = R.drawable.screen_lock_portrait_fill0) { onNavigate(LockScreenInfo) }
        }
        if(VERSION.SDK_INT >= 24) {
            FunctionItem(R.string.support_messages, icon = R.drawable.chat_fill0) { onNavigate(SupportMessage) }
        }
        FunctionItem(R.string.disable_account_management, icon = R.drawable.account_circle_fill0) { onNavigate(DisableAccountManagement) }
        if(VERSION.SDK_INT >= 23 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.system_update_policy, icon = R.drawable.system_update_fill0) { onNavigate(SetSystemUpdatePolicy) }
        }
        if(VERSION.SDK_INT >= 29 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.install_system_update, icon = R.drawable.system_update_fill0) { onNavigate(InstallSystemUpdate) }
        }
        if(VERSION.SDK_INT >= 30 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.frp_policy, icon = R.drawable.device_reset_fill0) { onNavigate(FrpPolicy) }
        }
        if(SP.displayDangerousFeatures && !privilege.work) {
            FunctionItem(R.string.wipe_data, icon = R.drawable.device_reset_fill0) { onNavigate(WipeData) }
        }
    }
    if((dialog == 1 || dialog == 2) && VERSION.SDK_INT >= 24) AlertDialog(
        onDismissRequest = { dialog = 0 },
        title = { Text(stringResource(if(dialog == 1) R.string.reboot else R.string.bug_report)) },
        text = { Text(stringResource(if(dialog == 1) R.string.info_reboot else R.string.confirm_bug_report)) },
        dismissButton = {
            TextButton(onClick = { dialog = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(dialog == 1) {
                        vm.reboot()
                    } else {
                        context.showOperationResultToast(vm.requestBugReport())
                    }
                    dialog = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    if(dialog in 3..5) {
        var input by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            text = {
                val focusMgr = LocalFocusManager.current
                LaunchedEffect(Unit) {
                    if (dialog == 5 && VERSION.SDK_INT >= 31) {
                        val id = vm.getEnrollmentSpecificId()
                        input = id.ifEmpty { context.getString(R.string.none) }
                    }
                    if (dialog == 3 && VERSION.SDK_INT >= 24) input = vm.getOrgName()
                }
                Column {
                    OutlinedTextField(
                        input, { input = it },
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (dialog != 3) 8.dp else 0.dp),
                        readOnly = dialog == 5,
                        label = {
                            Text(stringResource(
                                when(dialog){
                                    3 -> R.string.org_name
                                    4 -> R.string.org_id
                                    5 -> R.string.enrollment_specific_id
                                    else -> R.string.place_holder
                                }
                            ))
                        },
                        supportingText = {
                            if(dialog == 4) Text(stringResource(R.string.length_6_to_64))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                        textStyle = typography.bodyLarge
                    )
                    if(dialog == 5) Text(stringResource(R.string.info_enrollment_specific_id))
                    if(dialog == 4) Text(stringResource(R.string.info_org_id))
                }
            },
            onDismissRequest = { dialog = 0 },
            dismissButton = {
                if (dialog != 5) TextButton({ dialog = 0 }) { Text(stringResource(R.string.cancel)) }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (dialog == 3 && VERSION.SDK_INT >= 24) vm.setOrgName(input)
                        if (dialog == 4 && VERSION.SDK_INT >= 31) {
                            context.showOperationResultToast(vm.setOrgId(input))
                        }
                        dialog = 0
                    },
                    enabled = dialog != 4 || input.length in 6..64
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

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
    val canDisableUsbSignal: Boolean = true
)

@Serializable object SystemOptions

@Composable
fun SystemOptionsScreen(vm: MyViewModel, onNavigateUp: () -> Unit) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    val status by vm.systemOptionsStatus.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.getSystemOptionsStatus() }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        SwitchItem(R.string.disable_cam, status.cameraDisabled, vm::setCameraDisabled,
            R.drawable.no_photography_fill0)
        SwitchItem(R.string.disable_screen_capture, status.screenCaptureDisabled,
            vm::setScreenCaptureDisabled, R.drawable.screenshot_fill0)
        if (VERSION.SDK_INT >= 34 && privilege.run { device || (profile && affiliated) }) {
            SwitchItem(R.string.disable_status_bar, status.statusBarDisabled,
                vm::setStatusBarDisabled, R.drawable.notifications_fill0)
        }
        if (privilege.device || privilege.org) {
            if(VERSION.SDK_INT >= 30) {
                SwitchItem(R.string.auto_time, status.autoTimeEnabled, vm::setAutoTimeEnabled,
                    R.drawable.schedule_fill0)
                SwitchItem(R.string.auto_timezone, status.autoTimeZoneEnabled,
                    vm::setAutoTimeZoneEnabled, R.drawable.globe_fill0)
            } else {
                SwitchItem(R.string.require_auto_time, status.autoTimeRequired,
                    vm::setAutoTimeRequired, R.drawable.schedule_fill0)
            }
        }
        if (!privilege.work) SwitchItem(R.string.master_mute,
            status.masterVolumeMuted, vm::setMasterVolumeMuted, R.drawable.volume_off_fill0)
        if (VERSION.SDK_INT >= 26) {
            SwitchItem(R.string.backup_service, icon = R.drawable.backup_fill0,
                state = status.backupServiceEnabled, onCheckedChange = vm::setBackupServiceEnabled,
                onClickBlank = { dialog = 1 })
        }
        if (VERSION.SDK_INT >= 24 && privilege.work) {
            SwitchItem(R.string.disable_bt_contact_share, status.btContactSharingDisabled,
                vm::setBtContactSharingDisabled, R.drawable.account_circle_fill0)
        }
        if(VERSION.SDK_INT >= 30 && (privilege.device || privilege.org)) {
            SwitchItem(R.string.common_criteria_mode, icon = R.drawable.security_fill0,
                state = status.commonCriteriaMode,
                onCheckedChange = vm::setCommonCriteriaModeEnabled,
                onClickBlank = { dialog = 2 })
        }
        if (VERSION.SDK_INT >= 31 && (privilege.device || privilege.org) && status.canDisableUsbSignal) {
            SwitchItem(R.string.enable_usb_signal, status.usbSignalEnabled,
                vm::setUsbSignalEnabled, R.drawable.usb_fill0)
        }
        if (VERSION.SDK_INT >= 23 && VERSION.SDK_INT < 34) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.status_bar), style = typography.titleMedium)
                Button({
                    vm.setStatusBarDisabled(true)
                }, Modifier.padding(horizontal = 4.dp)) {
                    Text(stringResource(R.string.disable))
                }
                Button({
                    vm.setStatusBarDisabled(false)
                }) {
                    Text(stringResource(R.string.enable))
                }
            }
        }
    }
    if(dialog != 0) AlertDialog(
        text = {
            Text(stringResource(
                when(dialog) {
                    1 -> R.string.info_backup_service
                    2 -> R.string.info_common_criteria_mode
                    else -> R.string.options
                }
            ))
        },
        confirmButton = {
            TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.confirm)) }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@Serializable object Keyguard

@Composable
fun KeyguardScreen(
    setKeyguardDisabled: (Boolean) -> Boolean, lock: (Boolean) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    MyScaffold(R.string.keyguard, onNavigateUp) {
        if (VERSION.SDK_INT >= 23 && (privilege.device ||
                    (VERSION.SDK_INT >= 28 && privilege.profile && privilege.affiliated))) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { context.showOperationResultToast(setKeyguardDisabled(true)) },
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.disable))
                }
                Button(
                    onClick = { context.showOperationResultToast(setKeyguardDisabled(false)) },
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text(stringResource(R.string.enable))
                }
            }
            Notes(R.string.info_disable_keyguard)
            Spacer(Modifier.padding(vertical = 12.dp))
        }
        if(VERSION.SDK_INT >= 23) Text(text = stringResource(R.string.lock_now), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 2.dp))
        var evictKey by rememberSaveable { mutableStateOf(false) }
        Button(
            onClick = { lock(evictKey) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lock_now))
        }
        if (VERSION.SDK_INT >= 26 && privilege.work) {
            CheckBoxItem(R.string.evict_credential_encryption_key, evictKey) { evictKey = true }
            Spacer(Modifier.height(5.dp))
            Notes(R.string.info_evict_credential_encryption_key)
        }
    }
}

data class HardwareProperties(
    val temperatures: Map<Int, List<Float>> = emptyMap(),
    val cpuUsages: List<Pair<Long, Long>> = emptyList(),
    val fanSpeeds: List<Float> = emptyList()
)

@RequiresApi(24)
val temperatureTypes = mapOf(
    HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU to R.string.cpu_temp,
    HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU to R.string.gpu_temp,
    HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY to R.string.battery_temp,
    HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN to R.string.skin_temp
)

@Serializable object HardwareMonitor

@RequiresApi(24)
@Composable
fun HardwareMonitorScreen(
    hardwareProperties: StateFlow<HardwareProperties>, getHardwareProperties: suspend () -> Unit,
    setRefreshInterval: (Float) -> Unit,
    onNavigateUp: () -> Unit
) {
    val properties by hardwareProperties.collectAsStateWithLifecycle()
    var refreshInterval by rememberSaveable { mutableFloatStateOf(1F) }
    val refreshIntervalMs = (refreshInterval * 1000).roundToLong()
    LaunchedEffect(Unit) {
        getHardwareProperties()
    }
    MyScaffold(R.string.hardware_monitor, onNavigateUp) {
        Text(stringResource(R.string.refresh_interval), Modifier.padding(top = 8.dp, bottom = 4.dp),
            style = typography.titleLarge)
        Slider(refreshInterval, {
            refreshInterval = it
            setRefreshInterval(it)
        }, valueRange = 0.5F..2F, steps = 14)
        Text("${refreshIntervalMs}ms")
        Spacer(Modifier.padding(vertical = 10.dp))
        properties.temperatures.forEach { tempMapItem ->
            Text(stringResource(temperatureTypes[tempMapItem.key]!!), style = typography.titleLarge, modifier = Modifier.padding(vertical = 4.dp))
            if(tempMapItem.value.isEmpty()) {
                Text(stringResource(R.string.unsupported))
            } else {
                tempMapItem.value.forEachIndexed { index, temp ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(index.toString(), style = typography.titleMedium, modifier = Modifier.padding(start = 8.dp, end = 12.dp))
                        Text(if(temp == HardwarePropertiesManager.UNDEFINED_TEMPERATURE) stringResource(R.string.undefined) else temp.toString())
                    }
                }
            }
            Spacer(Modifier.padding(vertical = 10.dp))
        }
        Text(stringResource(R.string.cpu_usages), style = typography.titleLarge, modifier = Modifier.padding(vertical = 4.dp))
        if (properties.cpuUsages.isEmpty()) {
            Text(stringResource(R.string.unsupported))
        } else {
            properties.cpuUsages.forEachIndexed { index, usage ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(index.toString(), style = typography.titleMedium, modifier = Modifier.padding(start = 8.dp, end = 12.dp))
                    Column {
                        Text(stringResource(R.string.active) + ": " + usage.first + "ms")
                        Text(stringResource(R.string.total) + ": " + usage.second + "ms")
                    }
                }
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(stringResource(R.string.fan_speeds), style = typography.titleLarge, modifier = Modifier.padding(vertical = 4.dp))
        if (properties.fanSpeeds.isEmpty()) {
            Text(stringResource(R.string.unsupported))
        } else {
            properties.fanSpeeds.forEachIndexed { index, speed ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(index.toString(), style = typography.titleMedium, modifier = Modifier.padding(start = 8.dp, end = 12.dp))
                    Text("$speed RPM")
                }
            }
        }
    }
}

@Serializable object ChangeTime

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun ChangeTimeScreen(setTime: (Long, Boolean) -> Boolean, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val pagerState = rememberPagerState { 2 }
    tab = pagerState.currentPage
    val coroutine = rememberCoroutineScope()
    var picker by rememberSaveable { mutableIntStateOf(0) } //0:None, 1:DatePicker, 2:TimePicker
    var useCurrentTz by rememberSaveable { mutableStateOf(true) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.change_time)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                colors = TopAppBarDefaults.topAppBarColors(colorScheme.surfaceContainer)
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(tab) {
                Tab(
                    tab == 0, { coroutine.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.selector)) }
                )
                Tab(
                    tab == 1, { coroutine.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.manually_input)) }
                )
            }
            HorizontalPager(
                pagerState, Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { page ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(top = 8.dp)
                        .padding(horizontal = HorizontalPadding)
                ) {
                    if(page == 0) {
                        OutlinedTextField(
                            value = datePickerState.selectedDateMillis?.let { formatDate(it) } ?: "",
                            onValueChange = {}, readOnly = true,
                            label = { Text(stringResource(R.string.date)) },
                            modifier = Modifier.fillMaxWidth().clickableTextField { picker = 1 }
                        )
                        OutlinedTextField(
                            value = timePickerState.hour.toString().padStart(2, '0') + ":" +
                                    timePickerState.minute.toString().padStart(2, '0'),
                            onValueChange = {}, readOnly = true,
                            label = { Text(stringResource(R.string.time)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableTextField { picker = 2 }
                                .padding(vertical = 4.dp)
                        )
                        CheckBoxItem(R.string.use_current_timezone, useCurrentTz) {
                            useCurrentTz = it
                        }
                        Button(
                            onClick = {
                                val timeMillis = datePickerState.selectedDateMillis!! +
                                        timePickerState.hour * 3600000 + timePickerState.minute * 60000
                                context.showOperationResultToast(setTime(timeMillis, useCurrentTz))
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = datePickerState.selectedDateMillis != null
                        ) {
                            Text(stringResource(R.string.apply))
                        }
                    } else {
                        var inputTime by rememberSaveable { mutableStateOf("") }
                        OutlinedTextField(
                            value = inputTime,
                            label = { Text(stringResource(R.string.time_unit_ms)) },
                            onValueChange = { inputTime = it },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                context.showOperationResultToast(setTime(inputTime.toLong(), false))
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            enabled = inputTime.toLongOrNull() != null
                        ) {
                            Text(stringResource(R.string.apply))
                        }
                    }
                    Spacer(Modifier.height(BottomPadding))
                }
            }
        }
    }
    if(picker == 1) DatePickerDialog(
        confirmButton = {
            TextButton(onClick = { picker = 0; focusMgr.clearFocus() } ) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { picker = 0; focusMgr.clearFocus() }
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DatePicker(datePickerState)
        }
    }
    if (picker == 2) TimePickerDialog(
        title = {},
        confirmButton = {
            TextButton({ picker = 0 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { picker = 0 }
    ) {
        TimePicker(timePickerState)
    }
}

@Serializable object ChangeTimeZone

@RequiresApi(28)
@Composable
fun ChangeTimeZoneScreen(setTimeZone: (String) -> Boolean, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var inputTimezone by rememberSaveable { mutableStateOf("") }
    var dialog by rememberSaveable { mutableStateOf(false) }
    val availableIds = TimeZone.getAvailableIDs()
    val validInput = inputTimezone in availableIds
    MyScaffold(R.string.change_timezone, onNavigateUp) {
        OutlinedTextField(
            value = inputTimezone,
            label = { Text(stringResource(R.string.timezone_id)) },
            onValueChange = { inputTimezone = it },
            isError = inputTimezone.isNotEmpty() && !validInput,
            trailingIcon = {
                IconButton(onClick = { dialog = true }) {
                    Icon(imageVector = Icons.AutoMirrored.Default.List, contentDescription = null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                context.showOperationResultToast(setTimeZone(inputTimezone))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputTimezone.isNotEmpty() && validInput
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.disable_auto_time_zone_before_set)
    }
    if(dialog) AlertDialog(
        text = {
            LazyColumn {
                items(availableIds) {
                    Text(
                        text = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .clip(RoundedCornerShape(15))
                            .clickable {
                                inputTimezone = it
                                dialog = false
                            }
                            .padding(start = 6.dp, top = 10.dp, bottom = 10.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { dialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = false }
    )
}

@Serializable object AutoTimePolicy

@RequiresApi(36)
@Composable
fun AutoTimePolicyScreen(
    getPolicy: () -> Int, setPolicy: (Int) -> Unit, onNavigateUp: () -> Unit
) = MyScaffold(R.string.auto_time_policy, onNavigateUp, 0.dp) {
    val context = LocalContext.current
    var policy by rememberSaveable { mutableIntStateOf(getPolicy()) }
    listOf(
        DevicePolicyManager.AUTO_TIME_ENABLED to R.string.enable,
        DevicePolicyManager.AUTO_TIME_DISABLED to R.string.disabled,
        DevicePolicyManager.AUTO_TIME_NOT_CONTROLLED_BY_POLICY to R.string.not_controlled_by_policy
    ).forEach {
        FullWidthRadioButtonItem(it.second, it.first == policy) {
            policy = it.first
        }
    }
    Button(
        {
            setPolicy(policy)
            context.showOperationResultToast(true)
        },
        Modifier
            .fillMaxWidth()
            .padding(horizontal = HorizontalPadding)
    ) {
        Text(stringResource(R.string.apply))
    }
}

@Serializable object AutoTimeZonePolicy

@RequiresApi(36)
@Composable
fun AutoTimeZonePolicyScreen(
    getPolicy: () -> Int, setPolicy: (Int) -> Unit, onNavigateUp: () -> Unit
) = MyScaffold(R.string.auto_timezone_policy, onNavigateUp, 0.dp) {
    val context = LocalContext.current
    var policy by rememberSaveable { mutableIntStateOf(getPolicy()) }
    listOf(
        DevicePolicyManager.AUTO_TIME_ZONE_ENABLED to R.string.enable,
        DevicePolicyManager.AUTO_TIME_ZONE_DISABLED to R.string.disabled,
        DevicePolicyManager.AUTO_TIME_ZONE_NOT_CONTROLLED_BY_POLICY to R.string.not_controlled_by_policy
    ).forEach {
        FullWidthRadioButtonItem(it.second, it.first == policy) {
            policy = it.first
        }
    }
    Button({
        setPolicy(policy)
        context.showOperationResultToast(true)
    }, Modifier
        .fillMaxWidth()
        .padding(horizontal = HorizontalPadding)) {
        Text(stringResource(R.string.apply))
    }
}

/*@RequiresApi(28)
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeyPairs(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var alias by remember { mutableStateOf("") }
    var purpose by remember { mutableIntStateOf(0) }
    //var keySpecType by remember { mutableIntStateOf() }
    var ecStdName by remember { mutableStateOf("") }
    var rsaKeySize by remember { mutableStateOf("") }
    var rsaExponent by remember { mutableStateOf("") }
    var algorithm by remember { mutableStateOf("") }
    var idAttestationFlags by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.key_pairs, 8.dp, navCtrl) {
        OutlinedTextField(
            value = alias, onValueChange = { alias = it }, label = { Text(stringResource(R.string.alias)) },
            modifier = Modifier.fillMaxWidth()
        )
        Text(stringResource(R.string.algorithm), style = typography.titleLarge)
        SingleChoiceSegmentedButtonRow {
            *//*SegmentedButton(
                algorithm == "DH", { algorithm = "DH" },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4)
            ) {
                Text("DH")
            }
            SegmentedButton(
                algorithm == "DSA", { algorithm = "DSA" },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4)
            ) {
                Text("DSA")
            }*//*
            SegmentedButton(
                algorithm == "EC", { algorithm = "EC" },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("EC")
            }
            SegmentedButton(
                algorithm == "RSA", { algorithm = "RSA" },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("RSA")
            }
        }
        AnimatedVisibility(algorithm != "") {
            Text(stringResource(R.string.key_specification), style = typography.titleLarge)
        }
        AnimatedVisibility(algorithm == "EC") {
            OutlinedTextField(
                value = ecStdName, onValueChange = { ecStdName = it }, label = { Text(stringResource(R.string.standard_name)) },
                modifier = Modifier.fillMaxWidth()
            )
        }
        AnimatedVisibility(algorithm == "RSA") {
            Column {
                OutlinedTextField(
                    value = rsaKeySize, onValueChange = { rsaKeySize = it }, label = { Text(stringResource(R.string.key_size)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = rsaExponent, onValueChange = { rsaExponent = it }, label = { Text(stringResource(R.string.exponent)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Text(stringResource(R.string.key_purpose), style = typography.titleLarge)
        FlowRow {
            if(VERSION.SDK_INT >= 23) {
                InputChip(
                    purpose and KeyProperties.PURPOSE_ENCRYPT != 0,
                    { purpose = purpose xor KeyProperties.PURPOSE_ENCRYPT },
                    { Text(stringResource(R.string.kp_encrypt)) },
                    Modifier.padding(horizontal = 4.dp)
                )
                InputChip(
                    purpose and KeyProperties.PURPOSE_DECRYPT != 0,
                    { purpose = purpose xor KeyProperties.PURPOSE_DECRYPT },
                    { Text(stringResource(R.string.kp_decrypt)) },
                    Modifier.padding(horizontal = 4.dp)
                )
                InputChip(
                    purpose and KeyProperties.PURPOSE_SIGN != 0,
                    { purpose = purpose xor KeyProperties.PURPOSE_SIGN },
                    { Text(stringResource(R.string.kp_sign)) },
                    Modifier.padding(horizontal = 4.dp)
                )
                InputChip(
                    purpose and KeyProperties.PURPOSE_VERIFY != 0,
                    { purpose = purpose xor KeyProperties.PURPOSE_VERIFY },
                    { Text(stringResource(R.string.kp_verify)) },
                    Modifier.padding(horizontal = 4.dp)
                )
            }
            if(VERSION.SDK_INT >= 28) InputChip(
                purpose and KeyProperties.PURPOSE_WRAP_KEY != 0,
                { purpose = purpose xor KeyProperties.PURPOSE_WRAP_KEY },
                { Text(stringResource(R.string.kp_wrap)) },
                Modifier.padding(horizontal = 4.dp)
            )
            if(VERSION.SDK_INT >= 31) {
                InputChip(
                    purpose and KeyProperties.PURPOSE_AGREE_KEY != 0,
                    { purpose = purpose xor KeyProperties.PURPOSE_AGREE_KEY },
                    { Text(stringResource(R.string.kp_agree)) },
                    Modifier.padding(horizontal = 4.dp)
                )
                InputChip(
                    purpose and KeyProperties.PURPOSE_ATTEST_KEY != 0,
                    { purpose = purpose xor KeyProperties.PURPOSE_ATTEST_KEY },
                    { Text(stringResource(R.string.kp_attest)) },
                    Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        Text(stringResource(R.string.attestation_record_identifiers), style = typography.titleLarge)
        FlowRow {
            InputChip(
                idAttestationFlags and DevicePolicyManager.ID_TYPE_BASE_INFO != 0,
                { idAttestationFlags = idAttestationFlags xor DevicePolicyManager.ID_TYPE_BASE_INFO },
                { Text(stringResource(R.string.base_info)) },
                Modifier.padding(horizontal = 4.dp)
            )
            InputChip(
                idAttestationFlags and DevicePolicyManager.ID_TYPE_SERIAL != 0,
                { idAttestationFlags = idAttestationFlags xor DevicePolicyManager.ID_TYPE_SERIAL },
                { Text(stringResource(R.string.serial_number)) },
                Modifier.padding(horizontal = 4.dp)
            )
            InputChip(
                idAttestationFlags and DevicePolicyManager.ID_TYPE_IMEI != 0,
                { idAttestationFlags = idAttestationFlags xor DevicePolicyManager.ID_TYPE_IMEI },
                { Text("IMEI") },
                Modifier.padding(horizontal = 4.dp)
            )
            InputChip(
                idAttestationFlags and DevicePolicyManager.ID_TYPE_MEID != 0,
                { idAttestationFlags = idAttestationFlags xor DevicePolicyManager.ID_TYPE_MEID },
                { Text("MEID") },
                Modifier.padding(horizontal = 4.dp)
            )
            if(VERSION.SDK_INT >= 30) InputChip(
                idAttestationFlags and DevicePolicyManager.ID_TYPE_INDIVIDUAL_ATTESTATION != 0,
                { idAttestationFlags = idAttestationFlags xor DevicePolicyManager.ID_TYPE_INDIVIDUAL_ATTESTATION },
                { Text(stringResource(R.string.individual_certificate)) },
                Modifier.padding(horizontal = 4.dp)
            )
        }
        Button(
            onClick = {
                try {
                    val aps = if(algorithm == "EC") ECGenParameterSpec(ecStdName)
                    else RSAKeyGenParameterSpec(rsaKeySize.toInt(), rsaExponent.toBigInteger())
                    val keySpec = KeyGenParameterSpec.Builder(alias, purpose).run {
                        setAlgorithmParameterSpec(aps)
                        this.setAttestationChallenge()
                        build()
                    }
                    dpm.generateKeyPair(receiver, algorithm, keySpec, idAttestationFlags)
                } catch(e: Exception) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(e.message ?: "")
                        .setPositiveButton(R.string.confirm) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = alias != "" && purpose != 0 &&
                    ((algorithm == "EC") || (algorithm == "RSA" && rsaKeySize.all { it.isDigit() } && rsaExponent.all { it.isDigit() }))
        ) {
            Text(stringResource(R.string.generate))
        }
    }
}*/

@Serializable object ContentProtectionPolicy

@RequiresApi(35)
@Composable
fun ContentProtectionPolicyScreen(
    getPolicy: () -> Int, setPolicy: (Int) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var policy by rememberSaveable { mutableIntStateOf(getPolicy()) }
    MyScaffold(R.string.content_protection_policy, onNavigateUp, 0.dp) {
        mapOf(
            DevicePolicyManager.CONTENT_PROTECTION_NOT_CONTROLLED_BY_POLICY to R.string.not_controlled_by_policy,
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED to R.string.enabled,
            DevicePolicyManager.CONTENT_PROTECTION_DISABLED to R.string.disabled
        ).forEach { (policyId, string) ->
            FullWidthRadioButtonItem(string, policy == policyId) { policy = policyId }
        }
        Button(
            onClick = {
                setPolicy(policy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_content_protection_policy, HorizontalPadding)
    }
}

@Serializable object PermissionPolicy

@RequiresApi(23)
@Composable
fun PermissionPolicyScreen(
    getPolicy: () -> Int, setPolicy: (Int) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var selectedPolicy by rememberSaveable { mutableIntStateOf(getPolicy()) }
    MyScaffold(R.string.permission_policy, onNavigateUp, 0.dp) {
        FullWidthRadioButtonItem(R.string.default_stringres, selectedPolicy == PERMISSION_POLICY_PROMPT) {
            selectedPolicy = PERMISSION_POLICY_PROMPT
        }
        FullWidthRadioButtonItem(R.string.auto_grant, selectedPolicy == PERMISSION_POLICY_AUTO_GRANT) {
            selectedPolicy = PERMISSION_POLICY_AUTO_GRANT
        }
        FullWidthRadioButtonItem(R.string.auto_deny, selectedPolicy == PERMISSION_POLICY_AUTO_DENY) {
            selectedPolicy = PERMISSION_POLICY_AUTO_DENY
        }
        Button(
            onClick = {
                setPolicy(selectedPolicy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 5.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_permission_policy, HorizontalPadding)
    }
}

@Serializable object MtePolicy

@RequiresApi(34)
@Composable
fun MtePolicyScreen(
    getPolicy: () -> Int, setPolicy: (Int) -> Boolean, onNavigateUp: () -> Unit
) {
    var policy by rememberSaveable { mutableIntStateOf(getPolicy()) }
    MyScaffold(R.string.mte_policy, onNavigateUp, 0.dp) {
        FullWidthRadioButtonItem(R.string.decide_by_user, policy == MTE_NOT_CONTROLLED_BY_POLICY) {
            policy = MTE_NOT_CONTROLLED_BY_POLICY
        }
        FullWidthRadioButtonItem(R.string.enabled, policy == MTE_ENABLED) { policy = MTE_ENABLED }
        FullWidthRadioButtonItem(R.string.disabled, policy == MTE_DISABLED) { policy = MTE_DISABLED }
        Button(
            onClick = {
                if (!setPolicy(policy)) policy = getPolicy()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_mte_policy, HorizontalPadding)
    }
}

@Serializable object NearbyStreamingPolicy

@RequiresApi(31)
@Composable
fun NearbyStreamingPolicyScreen(
    getAppPolicy: () -> Int, setAppPolicy: (Int) -> Unit, getNotificationPolicy: () -> Int,
    setNotificationPolicy: (Int) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var appPolicy by rememberSaveable { mutableIntStateOf(getAppPolicy()) }
    MySmallTitleScaffold(R.string.nearby_streaming_policy, onNavigateUp, 0.dp) {
        Text(
            stringResource(R.string.nearby_app_streaming),
            Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp), style = typography.titleLarge
        )
        FullWidthRadioButtonItem(
            R.string.decide_by_user,
            appPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY
        ) { appPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        FullWidthRadioButtonItem(R.string.enabled, appPolicy == NEARBY_STREAMING_ENABLED) { appPolicy = NEARBY_STREAMING_ENABLED }
        FullWidthRadioButtonItem(R.string.disabled, appPolicy == NEARBY_STREAMING_DISABLED) { appPolicy = NEARBY_STREAMING_DISABLED }
        FullWidthRadioButtonItem(
            R.string.enable_if_secure_enough,
            appPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY
        ) { appPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY }
        Button(
            onClick = {
                setAppPolicy(appPolicy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_nearby_app_streaming_policy, HorizontalPadding)
        Spacer(Modifier.height(20.dp))
        var notificationPolicy by rememberSaveable { mutableIntStateOf(getNotificationPolicy()) }
        Text(
            stringResource(R.string.nearby_notification_streaming),
            Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp), style = typography.titleLarge
        )
        FullWidthRadioButtonItem(
            R.string.decide_by_user,
            notificationPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY
        ) { notificationPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        FullWidthRadioButtonItem(
            R.string.enabled,
            notificationPolicy == NEARBY_STREAMING_ENABLED
        ) { notificationPolicy = NEARBY_STREAMING_ENABLED }
        FullWidthRadioButtonItem(
            R.string.disabled,
            notificationPolicy == NEARBY_STREAMING_DISABLED
        ) { notificationPolicy = NEARBY_STREAMING_DISABLED }
        FullWidthRadioButtonItem(
            R.string.enable_if_secure_enough,
            notificationPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY
        ) { notificationPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY }
        Button(
            onClick = {
                setNotificationPolicy(notificationPolicy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_nearby_notification_streaming_policy, HorizontalPadding)
    }
}

@Serializable object LockTaskMode

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun LockTaskModeScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    lockTaskPackages: StateFlow<List<AppInfo>>, getLockTaskPackages: () -> Unit,
    setLockTaskPackage: (String, Boolean) -> Unit, startLockTaskMode: (String, String) -> Boolean,
    getLockTaskFeatures: () -> Int, setLockTaskFeature: (Int) -> String?, onNavigateUp: () -> Unit
) {
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    tabIndex = pagerState.targetPage
    LaunchedEffect(Unit) {
        getLockTaskPackages()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lock_task_mode)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                colors = TopAppBarDefaults.topAppBarColors(colorScheme.surfaceContainer)
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(tabIndex) {
                Tab(
                    tabIndex == 0, onClick = { coroutine.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.start)) }
                )
                Tab(
                    tabIndex == 1, onClick = { coroutine.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.applications)) }
                )
                Tab(
                    tabIndex == 2, onClick = { coroutine.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text(stringResource(R.string.features)) }
                )
            }
            HorizontalPager(pagerState, verticalAlignment = Alignment.Top) { page ->
                if(page == 0) {
                    StartLockTaskMode(startLockTaskMode, chosenPackage, onChoosePackage)
                } else if (page == 1) {
                    LockTaskPackages(chosenPackage, onChoosePackage, lockTaskPackages, setLockTaskPackage)
                } else {
                    LockTaskFeatures(getLockTaskFeatures, setLockTaskFeature)
                }
            }
        }
    }
}

@RequiresApi(28)
@Composable
private fun StartLockTaskMode(
    startLockTaskMode: (String, String) -> Boolean,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf("") }
    var activity by rememberSaveable { mutableStateOf("") }
    var specifyActivity by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = HorizontalPadding)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(5.dp))
        if (privilege.dhizuku) Column(
            Modifier
                .fillMaxWidth().padding(vertical = 8.dp)
                .background(colorScheme.errorContainer, RoundedCornerShape(10.dp))
                .padding(8.dp)
        ) {
            Text(
                stringResource(R.string.start_lock_task_mode_not_supported),
                color = colorScheme.onErrorContainer
            )
        }
        PackageNameTextField(packageName, onChoosePackage) { packageName = it }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(specifyActivity, {
                specifyActivity = it
                activity = ""
            })
            OutlinedTextField(
                value = activity,
                onValueChange = { activity = it },
                label = { Text("Activity") },
                enabled = specifyActivity,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp),
            onClick = {
                val result = startLockTaskMode(packageName, activity)
                if (!result) context.showOperationResultToast(false)
            },
            enabled = packageName.isNotBlank() && (!specifyActivity || activity.isNotBlank())
                    && !privilege.dhizuku
        ) {
            Text(stringResource(R.string.start))
        }
        if (!privilege.dhizuku) Notes(R.string.info_start_lock_task_mode)
    }
}

@RequiresApi(26)
@Composable
private fun LockTaskPackages(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    lockTaskPackages: StateFlow<List<AppInfo>>, setLockTaskPackage: (String, Boolean) -> Unit
) {
    val packages by lockTaskPackages.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    LazyColumn {
        items(packages, { it.name }) {
            ApplicationItem(it) { setLockTaskPackage(it.name, false) }
        }
        item {
            Column(Modifier
                .padding(horizontal = HorizontalPadding)
                .padding(bottom = 40.dp)) {
                PackageNameTextField(packageName, onChoosePackage,
                    Modifier.padding(vertical = 3.dp)) { packageName = it }
                Button(
                    onClick = {
                        setLockTaskPackage(packageName, true)
                        packageName = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = packageName.isValidPackageName
                ) {
                    Text(stringResource(R.string.add))
                }
                Notes(R.string.info_lock_task_packages)
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}

@RequiresApi(28)
@Composable
private fun LockTaskFeatures(
    getLockTaskFeatures: () -> Int, setLockTaskFeature: (Int) -> String?
) {
    val context = LocalContext.current
    var flags by rememberSaveable { mutableIntStateOf(getLockTaskFeatures()) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.padding(vertical = 5.dp))
        listOf(
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO to R.string.ltf_sys_info,
            DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS to R.string.ltf_notifications,
            DevicePolicyManager.LOCK_TASK_FEATURE_HOME to R.string.ltf_home,
            DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW to R.string.ltf_overview,
            DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS to R.string.ltf_global_actions,
            DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD to R.string.ltf_keyguard
        ).let {
            if(VERSION.SDK_INT >= 30) it.plus(
                DevicePolicyManager.LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK to
                        R.string.ltf_block_activity_start_in_task)
            else it
        }.forEach { (id, title) ->
            FullWidthCheckBoxItem(title, flags and id != 0) { flags = flags xor id }
        }
        Button(
            onClick = {
                val result = setLockTaskFeature(flags)
                if (result == null) {
                    context.showOperationResultToast(true)
                } else {
                    errorMessage = result
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.height(BottomPadding))
        ErrorDialog(errorMessage) { errorMessage = null }
    }
}

data class CaCertInfo(
    val hash: String,
    val serialNumber: String,
    val issuer: String,
    val subject: String,
    val issuedTime: Long,
    val expiresTime: Long,
    val bytes: ByteArray
)

@Serializable object CaCert

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun CaCertScreen(
    caCertificates: StateFlow<List<CaCertInfo>>, getCerts: () -> Unit,
    selectedCaCert: MutableStateFlow<CaCertInfo?>, selectCaCert: (CaCertInfo) -> Unit,
    installCert: () -> Boolean, parseCert: (Uri) -> Unit,
    exportCert: (Uri) -> Unit, uninstallCert: () -> Unit,
    uninstallAllCerts: () -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    /** 0:none, 1:install, 2:info, 3:uninstall all */
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    val caCerts by caCertificates.collectAsStateWithLifecycle()
    val selectedCert by selectedCaCert.collectAsStateWithLifecycle()
    val getCertLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            parseCert(uri)
            dialog = 1
        }
    }
    val exportCertLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()) { uri ->
        if (uri != null) exportCert(uri)
    }
    LaunchedEffect(Unit) { getCerts() }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ca_cert)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    IconButton({ dialog = 3 }) {
                        Icon(Icons.Outlined.Delete, stringResource(R.string.delete))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton({
                context.popToast(R.string.select_ca_cert)
                getCertLauncher.launch(arrayOf("*/*"))
            }) {
                Icon(Icons.Default.Add, stringResource(R.string.install))
            }
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(caCerts, { it.hash }) { cert ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectCaCert(cert)
                            dialog = 2
                        }
                        .animateItem()
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                ) {
                    Text(cert.hash.substring(0..7))
                }
                HorizontalDivider()
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
        if (selectedCert != null && (dialog == 1 || dialog == 2)) {
            val cert = selectedCert!!
            AlertDialog(
                text = {
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        Text("Serial number", style = typography.labelLarge)
                        SelectionContainer { Text(cert.serialNumber) }
                        Text("Subject", style = typography.labelLarge)
                        SelectionContainer { Text(cert.subject) }
                        Text("Issuer", style = typography.labelLarge)
                        SelectionContainer { Text(cert.issuer) }
                        Text("Issued on", style = typography.labelLarge)
                        SelectionContainer { Text(formatDate(cert.issuedTime)) }
                        Text("Expires on", style = typography.labelLarge)
                        SelectionContainer { Text(formatDate(cert.expiresTime)) }
                        Text("SHA-256 fingerprint", style = typography.labelLarge)
                        SelectionContainer { Text(cert.hash) }
                        if (dialog == 2) Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp), Arrangement.SpaceBetween
                        ) {
                            TextButton(
                                onClick = {
                                    uninstallCert()
                                    dialog = 0
                                },
                                modifier = Modifier.fillMaxWidth(0.49F),
                                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                            ) {
                                Text(stringResource(R.string.uninstall))
                            }
                            FilledTonalButton(
                                onClick = {
                                    exportCertLauncher.launch(cert.hash.substring(0..7) + ".0")
                                },
                                modifier = Modifier.fillMaxWidth(0.96F)
                            ) {
                                Text(stringResource(R.string.export))
                            }
                        }
                    }
                },
                confirmButton = {
                    if (dialog == 1) {
                        TextButton({
                            context.showOperationResultToast(installCert())
                            dialog = 0
                        }) {
                            Text(stringResource(R.string.install))
                        }
                    } else {
                        TextButton({
                            dialog = 0
                        }) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                },
                dismissButton = {
                    if (dialog == 1) {
                        TextButton({
                            dialog = 0
                        }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                },
                onDismissRequest = { dialog = 0 }
            )
        }
        if (dialog == 3) {
            AlertDialog(
                text = {
                    Text(stringResource(R.string.uninstall_all_user_ca_cert))
                },
                confirmButton = {
                    TextButton({
                        uninstallAllCerts()
                        dialog = 0
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton({
                        dialog = 0
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                onDismissRequest = { dialog = 0 }
            )
        }
    }
}

@Serializable object SecurityLogging

@RequiresApi(24)
@Composable
fun SecurityLoggingScreen(
    getEnabled: () -> Boolean, setEnabled: (Boolean) -> Unit, exportLogs: (Uri, () -> Unit) -> Unit,
    getCount: () -> Int, deleteLogs: () -> Unit, getPRLogs: () -> Boolean,
    exportPRLogs: (Uri, () -> Unit) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var enabled by rememberSaveable { mutableStateOf(getEnabled()) }
    var logsCount by rememberSaveable { mutableIntStateOf(getCount()) }
    var exporting by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) {
            exporting = true
            exportLogs(it) {
                exporting = false
                context.showOperationResultToast(true)
            }
        }
    }
    val exportPRLogsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) {
            exporting = true
            exportPRLogs(it) {
                exporting = false
                context.showOperationResultToast(true)
            }
        }
    }
    MyScaffold(R.string.security_logging, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.enable, enabled, {
                setEnabled(it)
                enabled = it
            }
        )
        Text(
            stringResource(R.string.n_logs_in_total, logsCount),
            Modifier.padding(HorizontalPadding)
        )
        Button(
            {
                val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                exportLauncher.launch("security_logs_$date")
            },
            Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
            logsCount > 0
        ) {
            Text(stringResource(R.string.export_logs))
        }
        if (logsCount > 0) FilledTonalButton(
            { dialog = true },
            Modifier.fillMaxWidth().padding(HorizontalPadding, 4.dp)
        ) {
            Text(stringResource(R.string.delete_logs))
        }
        Notes(R.string.info_security_log, HorizontalPadding)
        Button(
            onClick = {
                if (getPRLogs()) {
                    val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    exportPRLogsLauncher.launch("pre_reboot_security_logs_$date")
                } else {
                    context.showOperationResultToast(false)
                }
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 15.dp)
        ) {
            Text(stringResource(R.string.pre_reboot_security_logs))
        }
        Notes(R.string.info_pre_reboot_security_log, HorizontalPadding)
    }
    if (exporting) CircularProgressDialog { exporting = false }
    if (dialog) AlertDialog(
        text = { Text(stringResource(R.string.delete_logs)) },
        confirmButton = {
            TextButton({
                deleteLogs()
                logsCount = 0
                dialog = false
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({ dialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = false }
    )
}

@Serializable object DisableAccountManagement

@Composable
fun DisableAccountManagementScreen(
    mdAccounts: StateFlow<List<String>>, getMdAccounts: () -> Unit,
    setMdAccount: (String, Boolean) -> Unit, onNavigateUp: () -> Unit
) {
    val focusMgr = LocalFocusManager.current
    val list by mdAccounts.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { getMdAccounts() }
    MyScaffold(R.string.disable_account_management, onNavigateUp) {

        Column(modifier = Modifier.animateContentSize()) {
            for(i in list) {
                ListItem(i) {
                    setMdAccount(i, false)
                }
            }
        }
        var inputText by remember{ mutableStateOf("") }
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(stringResource(R.string.account_type)) },
            trailingIcon = {
                IconButton(
                    onClick = {
                        setMdAccount(inputText, true)
                        inputText = ""
                    },
                    enabled = inputText != ""
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.info_disable_account_management)
    }
}

data class FrpPolicyInfo(
    val supported: Boolean,
    val usePolicy: Boolean,
    val enabled: Boolean,
    val accounts: List<String>
)

@Serializable object FrpPolicy

@RequiresApi(30)
@Composable
fun FrpPolicyScreen(
    frpPolicy: FrpPolicyInfo, setFrpPolicy: (FrpPolicyInfo) -> Unit,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var usePolicy by rememberSaveable { mutableStateOf(frpPolicy.usePolicy) }
    var enabled by rememberSaveable { mutableStateOf(frpPolicy.enabled) }
    var supported by rememberSaveable { mutableStateOf(frpPolicy.supported) }
    val accountList = rememberSaveable { mutableStateListOf(*frpPolicy.accounts.toTypedArray()) }
    var inputAccount by rememberSaveable { mutableStateOf("") }
    MyScaffold(R.string.frp_policy, onNavigateUp, 0.dp) {
        if (!supported) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primaryContainer)
            ) {
                Text(stringResource(R.string.frp_not_supported), Modifier.padding(8.dp), color = colorScheme.onPrimaryContainer)
            }
        } else {
            SwitchItem(R.string.use_policy, usePolicy, { usePolicy = it })
        }
        if (usePolicy) {
            FullWidthCheckBoxItem(R.string.enable_frp, enabled) { enabled = it }
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                Text(stringResource(R.string.account_list_is))
                Column(modifier = Modifier.animateContentSize()) {
                    if(accountList.isEmpty()) Text(stringResource(R.string.none))
                    for(i in accountList) {
                        ListItem(i) { accountList -= i }
                    }
                }
                OutlinedTextField(
                    value = inputAccount,
                    onValueChange = { inputAccount = it },
                    label = { Text(stringResource(R.string.account)) },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                accountList += inputAccount
                                inputAccount = ""
                            },
                            enabled = inputAccount.isNotBlank()
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        setFrpPolicy(FrpPolicyInfo(true, usePolicy, enabled, accountList))
                        context.showOperationResultToast(true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
        Notes(R.string.info_frp_policy, HorizontalPadding)
    }
}

@Serializable object WipeData

@Composable
fun WipeDataScreen(
    wipeData: (Boolean, Int, String) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    val focusMgr = LocalFocusManager.current
    var flag by rememberSaveable { mutableIntStateOf(0) }
    var dialog by rememberSaveable { mutableIntStateOf(0) } // 0: none, 1: wipe data, 2: wipe device
    var reason by rememberSaveable { mutableStateOf("") }
    MyScaffold(R.string.wipe_data, onNavigateUp, 0.dp) {
        FullWidthCheckBoxItem(R.string.wipe_external_storage, flag and WIPE_EXTERNAL_STORAGE != 0) {
            flag = flag xor WIPE_EXTERNAL_STORAGE
        }
        if(VERSION.SDK_INT >= 22 && privilege.device) FullWidthCheckBoxItem(
            R.string.wipe_reset_protection_data, flag and WIPE_RESET_PROTECTION_DATA != 0) {
            flag = flag xor WIPE_RESET_PROTECTION_DATA
        }
        if(VERSION.SDK_INT >= 28) FullWidthCheckBoxItem(R.string.wipe_euicc,
            flag and WIPE_EUICC != 0) {
            flag = flag xor WIPE_EUICC
        }
        if (VERSION.SDK_INT < 34 || !userManager.isSystemUser) {
            if(VERSION.SDK_INT >= 29) CheckBoxItem(R.string.wipe_silently, flag and WIPE_SILENTLY != 0) {
                flag = flag xor WIPE_SILENTLY
                reason = ""
            }
            AnimatedVisibility(flag and WIPE_SILENTLY != 0 && VERSION.SDK_INT >= 28) {
                OutlinedTextField(
                    value = reason, onValueChange = { reason = it },
                    label = { Text(stringResource(R.string.reason)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )
            }
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    dialog = 1
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 5.dp)
            ) {
                Text("WipeData")
            }
        }
        if (VERSION.SDK_INT >= 34 && privilege.device) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    dialog = 2
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 5.dp)
            ) {
                Text("WipeDevice")
            }
        }
    }
    if (dialog != 0) {
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.warning), color = colorScheme.error)
            },
            text = {
                Text(
                    text = stringResource(
                        if(VERSION.SDK_INT >= 23 && userManager.isSystemUser) R.string.wipe_data_warning
                        else R.string.info_wipe_data_in_managed_user
                    ),
                    color = colorScheme.error
                )
            },
            onDismissRequest = { dialog = 0 },
            confirmButton = {
                var timer by remember { mutableIntStateOf(6) }
                LaunchedEffect(Unit) {
                    while(timer > 0) {
                        timer -= 1
                        delay(1000)
                    }
                }
                val timerText = if(timer > 0) "(${timer}s)" else ""
                TextButton(
                    onClick = {
                        wipeData(dialog == 2, flag, reason)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
                    modifier = Modifier.animateContentSize(),
                    enabled = timer == 0
                ) {
                    Text(stringResource(R.string.confirm) + timerText)
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = 0 }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

data class SystemUpdatePolicyInfo(val type: Int, val start: Int, val end: Int)
data class PendingSystemUpdateInfo(val exists: Boolean, val time: Long, val securityPatch: Boolean)

@Serializable object SetSystemUpdatePolicy

@RequiresApi(23)
@Composable
fun SystemUpdatePolicyScreen(
    getPolicy: () -> SystemUpdatePolicyInfo, setPolicy: (SystemUpdatePolicyInfo) -> Unit,
    getPendingUpdate: () -> PendingSystemUpdateInfo, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var policyType by remember { mutableIntStateOf(-1) }
    var windowedPolicyStart by remember { mutableStateOf("") }
    var windowedPolicyEnd by remember { mutableStateOf("") }
    var pendingUpdate by remember { mutableStateOf(PendingSystemUpdateInfo(false, 0, false)) }
    LaunchedEffect(Unit) {
        val policy = getPolicy()
        policyType = policy.type
        if (policy.type == TYPE_INSTALL_WINDOWED) {
            windowedPolicyStart = policy.start.toString()
            windowedPolicyEnd = policy.end.toString()
        }
        if (VERSION.SDK_INT >= 26) pendingUpdate = getPendingUpdate()
    }
    MyScaffold(R.string.system_update_policy, onNavigateUp, 0.dp) {
        FullWidthRadioButtonItem(R.string.none, policyType == -1) { policyType = -1 }
        FullWidthRadioButtonItem(
            R.string.system_update_policy_automatic,
            policyType == TYPE_INSTALL_AUTOMATIC
        ) { policyType = TYPE_INSTALL_AUTOMATIC }
        FullWidthRadioButtonItem(
            R.string.system_update_policy_install_windowed,
            policyType == TYPE_INSTALL_WINDOWED
        ) { policyType = TYPE_INSTALL_WINDOWED }
        FullWidthRadioButtonItem(
            R.string.system_update_policy_postpone,
            policyType == TYPE_POSTPONE
        ) { policyType = TYPE_POSTPONE }
        AnimatedVisibility(policyType == TYPE_INSTALL_WINDOWED) {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp), Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        value = windowedPolicyStart,
                        label = { Text(stringResource(R.string.start_time)) },
                        onValueChange = { windowedPolicyStart = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(0.49F)
                    )
                    OutlinedTextField(
                        value = windowedPolicyEnd,
                        onValueChange = { windowedPolicyEnd = it },
                        label = { Text(stringResource(R.string.end_time)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(0.96F)
                    )
                }
                Text(stringResource(R.string.minutes_in_one_day),
                    color = colorScheme.onSurfaceVariant, style = typography.bodyMedium)
            }
        }
        Button(
            onClick = {
                setPolicy(SystemUpdatePolicyInfo(
                    policyType, windowedPolicyStart.toIntOrNull() ?: 0,
                    windowedPolicyEnd.toIntOrNull() ?: 0
                ))
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding),
            enabled = policyType != TYPE_INSTALL_WINDOWED ||
                    listOf(windowedPolicyStart, windowedPolicyEnd).map { it.toIntOrNull() }
                        .all { it != null && it <= 1440 }
        ) {
            Text(stringResource(R.string.apply))
        }
        if (VERSION.SDK_INT >= 26) {
            Column(Modifier.padding(HorizontalPadding)) {
                if (pendingUpdate.exists) {
                    Text(stringResource(R.string.update_received_time, formatDate(pendingUpdate.time)))
                    Text(stringResource(R.string.is_security_patch,
                        stringResource(pendingUpdate.securityPatch.yesOrNo)))
                } else {
                    Text(text = stringResource(R.string.no_system_update))
                }
            }
        }
    }
}

@Serializable object InstallSystemUpdate

@SuppressLint("NewApi")
@Composable
fun InstallSystemUpdateScreen(
    installSystemUpdate: (Uri, (String) -> Unit) -> Unit, onNavigateUp: () -> Unit
) {
    var uri by remember { mutableStateOf<Uri?>(null) }
    var installing by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val getFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri = it }
    MyScaffold(R.string.install_system_update, onNavigateUp) {
        Button(
            onClick = {
                getFileLauncher.launch("application/zip")
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.select_ota_package))
        }
        Button(
            onClick = {
                installing = true
                installSystemUpdate(uri!!) { message ->
                    errorMessage = message
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uri != null && !installing
        ) {
            Text(stringResource(R.string.install_system_update))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.auto_reboot_after_install_succeed)
    }
    ErrorDialog(errorMessage) { errorMessage = null }
}
