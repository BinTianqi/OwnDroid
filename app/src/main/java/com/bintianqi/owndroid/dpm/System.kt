package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY
import android.app.admin.DevicePolicyManager.InstallSystemUpdateCallback
import android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK
import android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS
import android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_HOME
import android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD
import android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS
import android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW
import android.app.admin.DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO
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
import android.app.admin.FactoryResetProtectionPolicy
import android.app.admin.SystemUpdateInfo
import android.app.admin.SystemUpdatePolicy
import android.app.admin.SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC
import android.app.admin.SystemUpdatePolicy.TYPE_INSTALL_WINDOWED
import android.app.admin.SystemUpdatePolicy.TYPE_POSTPONE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.HardwarePropertiesManager
import android.os.UserManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.NotificationUtils
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SharedPrefs
import com.bintianqi.owndroid.formatFileSize
import com.bintianqi.owndroid.humanReadableDate
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoCard
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.uriToStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.collections.addAll
import kotlin.math.roundToLong

@Composable
fun SystemManage(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sp = SharedPrefs(context)
    val dhizuku = sp.dhizuku
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.system, 0.dp, navCtrl) {
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { navCtrl.navigate("SystemOptions") }
        }
        FunctionItem(R.string.keyguard, icon = R.drawable.screen_lock_portrait_fill0) { navCtrl.navigate("Keyguard") }
        if(VERSION.SDK_INT >= 24 && deviceOwner && !dhizuku)
            FunctionItem(R.string.hardware_monitor, icon = R.drawable.memory_fill0) { navCtrl.navigate("HardwareMonitor") }
        if(VERSION.SDK_INT >= 24 && deviceOwner) {
            FunctionItem(R.string.reboot, icon = R.drawable.restart_alt_fill0) { dialog = 1 }
        }
        if(deviceOwner && VERSION.SDK_INT >= 24 && (VERSION.SDK_INT < 28 || dpm.isAffiliatedUser)) {
            FunctionItem(R.string.bug_report, icon = R.drawable.bug_report_fill0) { dialog = 2 }
        }
        if(VERSION.SDK_INT >= 28 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.change_time, icon = R.drawable.schedule_fill0) { navCtrl.navigate("ChangeTime") }
            FunctionItem(R.string.change_timezone, icon = R.drawable.schedule_fill0) { navCtrl.navigate("ChangeTimeZone") }
        }
        /*if(VERSION.SDK_INT >= 28 && (deviceOwner || profileOwner))
            FunctionItem(R.string.key_pairs, icon = R.drawable.key_vertical_fill0) { navCtrl.navigate("KeyPairs") }*/
        if(VERSION.SDK_INT >= 35 && (deviceOwner || (profileOwner && dpm.isAffiliatedUser)))
            FunctionItem(R.string.content_protection_policy, icon = R.drawable.search_fill0) { navCtrl.navigate("ContentProtectionPolicy") }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.permission_policy, icon = R.drawable.key_fill0) { navCtrl.navigate("PermissionPolicy") }
        }
        if(VERSION.SDK_INT >= 34 && deviceOwner) {
            FunctionItem(R.string.mte_policy, icon = R.drawable.memory_fill0) { navCtrl.navigate("MTEPolicy") }
        }
        if(VERSION.SDK_INT >= 31 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.nearby_streaming_policy, icon = R.drawable.share_fill0) { navCtrl.navigate("NearbyStreamingPolicy") }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            FunctionItem(R.string.lock_task_mode, icon = R.drawable.lock_fill0) { navCtrl.navigate("LockTaskMode") }
        }
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.ca_cert, icon = R.drawable.license_fill0) { navCtrl.navigate("CACert") }
        }
        if(VERSION.SDK_INT >= 26 && !dhizuku && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.security_logging, icon = R.drawable.description_fill0) { navCtrl.navigate("SecurityLogging") }
        }
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.disable_account_management, icon = R.drawable.account_circle_fill0) { navCtrl.navigate("DisableAccountManagement") }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.system_update_policy, icon = R.drawable.system_update_fill0) { navCtrl.navigate("SystemUpdatePolicy") }
        }
        if(VERSION.SDK_INT >= 29 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.install_system_update, icon = R.drawable.system_update_fill0) { navCtrl.navigate("InstallSystemUpdate") }
        }
        if(VERSION.SDK_INT >= 30 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.frp_policy, icon = R.drawable.device_reset_fill0) { navCtrl.navigate("FRPPolicy") }
        }
        if(sp.displayDangerousFeatures && context.isDeviceAdmin && !(VERSION.SDK_INT >= 24 && profileOwner && dpm.isManagedProfile(receiver))) {
            FunctionItem(R.string.wipe_data, icon = R.drawable.device_reset_fill0) { navCtrl.navigate("WipeData") }
        }
    }
    if(dialog != 0 &&VERSION.SDK_INT >= 24) AlertDialog(
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
                        dpm.reboot(receiver)
                    } else {
                        context.showOperationResultToast(dpm.requestBugreport(receiver))
                    }
                    dialog = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun SystemOptions(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val um = context.getSystemService(Context.USER_SERVICE) as UserManager
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, 0.dp, navCtrl) {
        if(deviceOwner || profileOwner) {
            SwitchItem(R.string.disable_cam, icon = R.drawable.photo_camera_fill0,
                getState = { dpm.getCameraDisabled(null) }, onCheckedChange = { dpm.setCameraDisabled(receiver,it) }
            )
        }
        if(deviceOwner || profileOwner) {
            SwitchItem(R.string.disable_screen_capture, icon = R.drawable.screenshot_fill0,
                getState = { dpm.getScreenCaptureDisabled(null) }, onCheckedChange = { dpm.setScreenCaptureDisabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 34 && (deviceOwner || (profileOwner && dpm.isAffiliatedUser))) {
            SwitchItem(R.string.disable_status_bar, icon = R.drawable.notifications_fill0,
                getState = { dpm.isStatusBarDisabled}, onCheckedChange = { dpm.setStatusBarDisabled(receiver,it) }
            )
        }
        if(deviceOwner || (VERSION.SDK_INT >= 23 && profileOwner && um.isSystemUser) || dpm.isOrgProfile(receiver)) {
            if(VERSION.SDK_INT >= 30) {
                SwitchItem(R.string.auto_time, icon = R.drawable.schedule_fill0,
                    getState = { dpm.getAutoTimeEnabled(receiver) }, onCheckedChange = { dpm.setAutoTimeEnabled(receiver,it) }
                )
                SwitchItem(R.string.auto_timezone, icon = R.drawable.globe_fill0,
                    getState = { dpm.getAutoTimeZoneEnabled(receiver) }, onCheckedChange = { dpm.setAutoTimeZoneEnabled(receiver,it) }
                )
            } else {
                SwitchItem(R.string.require_auto_time, icon = R.drawable.schedule_fill0,
                    getState = { dpm.autoTimeRequired }, onCheckedChange = { dpm.setAutoTimeRequired(receiver,it) }, padding = false)
            }
        }
        if(deviceOwner || profileOwner) {
            SwitchItem(R.string.master_mute, icon = R.drawable.volume_up_fill0,
                getState = { dpm.isMasterVolumeMuted(receiver) }, onCheckedChange = { dpm.setMasterVolumeMuted(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner)) {
            SwitchItem(R.string.backup_service, icon = R.drawable.backup_fill0,
                getState = { dpm.isBackupServiceEnabled(receiver) }, onCheckedChange = { dpm.setBackupServiceEnabled(receiver,it) },
                onClickBlank = { dialog = 1 }
            )
        }
        if(VERSION.SDK_INT >= 24 && profileOwner && dpm.isManagedProfile(receiver)) {
            SwitchItem(R.string.disable_bt_contact_share, icon = R.drawable.account_circle_fill0,
                getState = { dpm.getBluetoothContactSharingDisabled(receiver) },
                onCheckedChange = { dpm.setBluetoothContactSharingDisabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 30 && deviceOwner) {
            SwitchItem(R.string.common_criteria_mode , icon =R.drawable.security_fill0,
                getState = { dpm.isCommonCriteriaModeEnabled(receiver) }, onCheckedChange = { dpm.setCommonCriteriaModeEnabled(receiver,it) },
                onClickBlank = { dialog = 2 }
            )
        }
        if(VERSION.SDK_INT >= 31 && (deviceOwner || dpm.isOrgProfile(receiver)) && dpm.canUsbDataSignalingBeDisabled()) {
            SwitchItem(
                R.string.disable_usb_signal, icon = R.drawable.usb_fill0, getState = { !dpm.isUsbDataSignalingEnabled },
                onCheckedChange = { dpm.isUsbDataSignalingEnabled = !it },
            )
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

@Composable
fun Keyguard(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    MyScaffold(R.string.keyguard, 8.dp, navCtrl) {
        if(VERSION.SDK_INT >= 23) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { context.showOperationResultToast(dpm.setKeyguardDisabled(receiver, true)) },
                    enabled = deviceOwner || (VERSION.SDK_INT >= 28 && profileOwner && dpm.isAffiliatedUser),
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.disable))
                }
                Button(
                    onClick = { context.showOperationResultToast(dpm.setKeyguardDisabled(receiver, false)) },
                    enabled = deviceOwner || (VERSION.SDK_INT >= 28 && profileOwner && dpm.isAffiliatedUser),
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text(stringResource(R.string.enable))
                }
            }
            InfoCard(R.string.info_disable_keyguard)
            Spacer(Modifier.padding(vertical = 12.dp))
        }
        if(VERSION.SDK_INT >= 23) Text(text = stringResource(R.string.lock_now), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 2.dp))
        var flag by remember { mutableIntStateOf(0) }
        if(VERSION.SDK_INT >= 26 && profileOwner && dpm.isManagedProfile(receiver)) {
            CheckBoxItem(
                R.string.evict_credential_encryption_key,
                flag and FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY != 0
            ) { flag = flag xor FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY }
            Spacer(Modifier.padding(vertical = 2.dp))
        }
        Button(
            onClick = {
                if(VERSION.SDK_INT >= 26) dpm.lockNow(flag) else dpm.lockNow()
            },
            enabled = context.isDeviceAdmin,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lock_now))
        }
        if(VERSION.SDK_INT >= 26 && profileOwner && dpm.isManagedProfile(receiver)) {
            InfoCard(R.string.info_evict_credential_encryption_key)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(24)
@Composable
fun HardwareMonitor(navCtrl: NavHostController) {
    val context = LocalContext.current
    val hpm = context.getSystemService(HardwarePropertiesManager::class.java)
    var refreshInterval by remember { mutableFloatStateOf(1F) }
    val refreshIntervalMs = (refreshInterval * 1000).roundToLong()
    val temperatures = remember { mutableStateMapOf<Int, List<Float>>() }
    val tempTypeMap = mapOf(
        HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU to R.string.cpu_temp,
        HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU to R.string.gpu_temp,
        HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY to R.string.battery_temp,
        HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN to R.string.skin_temp
    )
    val cpuUsages = remember { mutableStateListOf<Pair<Long, Long>>() }
    val fanSpeeds = remember { mutableStateListOf<Float>() }
    fun refresh() {
        cpuUsages.clear()
        cpuUsages.addAll(hpm.cpuUsages.map { it.active to it.total })
        temperatures.clear()
        tempTypeMap.forEach {
            temperatures += it.key to hpm.getDeviceTemperatures(it.key, HardwarePropertiesManager.TEMPERATURE_CURRENT).toList()
        }
        fanSpeeds.clear()
        fanSpeeds.addAll(hpm.fanSpeeds.toList())
    }
    LaunchedEffect(Unit) {
        while(true) {
            refresh()
            delay(refreshIntervalMs)
        }
    }
    MyScaffold(R.string.hardware_monitor, 8.dp, navCtrl, false) {
        Text(stringResource(R.string.refresh_interval), style = typography.titleLarge, modifier = Modifier.padding(vertical = 4.dp))
        Slider(refreshInterval, { refreshInterval = it }, valueRange = 0.5F..2F, steps = 14)
        Text("${refreshIntervalMs}ms")
        Spacer(Modifier.padding(vertical = 10.dp))
        temperatures.forEach { tempMapItem ->
            Text(stringResource(tempTypeMap[tempMapItem.key]!!), style = typography.titleLarge, modifier = Modifier.padding(vertical = 4.dp))
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
        if(cpuUsages.isEmpty()) {
            Text(stringResource(R.string.unsupported))
        } else {
            cpuUsages.forEachIndexed { index, usage ->
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
        if(fanSpeeds.isEmpty()) {
            Text(stringResource(R.string.unsupported))
        } else {
            fanSpeeds.forEachIndexed { index, speed ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(index.toString(), style = typography.titleMedium, modifier = Modifier.padding(start = 8.dp, end = 12.dp))
                    Text("$speed RPM")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun ChangeTime(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 2 }
    var manualInput by remember { mutableStateOf(false) }
    var inputTime by remember { mutableStateOf("")}
    var picker by remember { mutableIntStateOf(0) } //0:None, 1:DatePicker, 2:TimePicker
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    val dateInteractionSource = remember { MutableInteractionSource() }
    val timeInteractionSource = remember { MutableInteractionSource() }
    if(dateInteractionSource.collectIsPressedAsState().value) picker = 1
    if(timeInteractionSource.collectIsPressedAsState().value) picker = 2
    val isInputLegal = (manualInput && (try { inputTime.toLong() } catch(_: Exception) { -1 }) >= 0) ||
            (!manualInput && datePickerState.selectedDateMillis != null)
    MyScaffold(R.string.change_time, 8.dp, navCtrl) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        ) {
            SegmentedButton(
                selected = !manualInput, shape = SegmentedButtonDefaults.itemShape(0, 2),
                onClick = {
                    manualInput = false
                    coroutine.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            ) {
                Text(stringResource(R.string.selector))
            }
            SegmentedButton(
                selected = manualInput, shape = SegmentedButtonDefaults.itemShape(1, 2),
                onClick = {
                    manualInput = true
                    coroutine.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            ) {
                Text(stringResource(R.string.manually_input))
            }
        }
        HorizontalPager(
            state = pagerState, modifier = Modifier.height(140.dp).padding(top = 4.dp),
            verticalAlignment = Alignment.Top
        ) { page ->
            if(page == 0) Column {
                OutlinedTextField(
                    value = datePickerState.selectedDateMillis?.humanReadableDate ?: "",
                    onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.date)) },
                    interactionSource = dateInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = timePickerState.hour.toString() + ":" + timePickerState.minute.toString(),
                    onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.time)) },
                    interactionSource = timeInteractionSource,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if(page == 1) OutlinedTextField(
                value = inputTime,
                label = { Text(stringResource(R.string.time_unit_ms)) },
                onValueChange = { inputTime = it },
                supportingText = { Text(stringResource(R.string.info_change_time)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Button(
            onClick = {
                val timeMillis = if(manualInput) inputTime.toLong()
                    else datePickerState.selectedDateMillis!! + timePickerState.hour * 3600000 + timePickerState.minute * 60000
                context.showOperationResultToast(dpm.setTime(receiver, timeMillis))
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isInputLegal
        ) {
            Text(stringResource(R.string.apply))
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
        DatePicker(datePickerState)
    }
    if(picker == 2) AlertDialog(
        text = { TimePicker(timePickerState) },
        confirmButton = {
            TextButton(onClick = { picker = 0; focusMgr.clearFocus() } ) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { picker = 0; focusMgr.clearFocus() }
    )
}

@RequiresApi(28)
@Composable
fun ChangeTimeZone(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    val receiver = context.getReceiver()
    var inputTimezone by remember { mutableStateOf("") }
    var dialog by remember { mutableStateOf(false) }
    MyScaffold(R.string.change_timezone, 8.dp, navCtrl) {
        OutlinedTextField(
            value = inputTimezone,
            label = { Text(stringResource(R.string.timezone_id)) },
            onValueChange = { inputTimezone = it },
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
                context.showOperationResultToast(dpm.setTimeZone(receiver, inputTimezone))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        InfoCard(R.string.disable_auto_time_zone_before_set)
    }
    if(dialog) AlertDialog(
        text = {
            LazyColumn {
                items(TimeZone.getAvailableIDs()) {
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

@RequiresApi(35)
@Composable
fun ContentProtectionPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var policy by remember { mutableIntStateOf(DevicePolicyManager.CONTENT_PROTECTION_NOT_CONTROLLED_BY_POLICY) }
    fun refresh() { policy = dpm.getContentProtectionPolicy(receiver) }
    LaunchedEffect(Unit) { refresh() }
    MyScaffold(R.string.content_protection_policy, 8.dp, navCtrl) {
        mapOf(
            DevicePolicyManager.CONTENT_PROTECTION_NOT_CONTROLLED_BY_POLICY to R.string.not_controlled_by_policy,
            DevicePolicyManager.CONTENT_PROTECTION_ENABLED to R.string.enabled,
            DevicePolicyManager.CONTENT_PROTECTION_DISABLED to R.string.disabled
        ).forEach { (policyId, string) ->
            RadioButtonItem(string, policy == policyId) { policy = policyId }
        }
        Button(
            onClick = {
                dpm.setContentProtectionPolicy(receiver, policy)
                refresh()
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_content_protection_policy)
    }
}

@RequiresApi(23)
@Composable
fun PermissionPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var selectedPolicy by remember { mutableIntStateOf(dpm.getPermissionPolicy(receiver)) }
    MyScaffold(R.string.permission_policy, 8.dp, navCtrl) {
        RadioButtonItem(R.string.default_stringres, selectedPolicy == PERMISSION_POLICY_PROMPT) { selectedPolicy = PERMISSION_POLICY_PROMPT }
        RadioButtonItem(R.string.auto_grant, selectedPolicy == PERMISSION_POLICY_AUTO_GRANT) { selectedPolicy = PERMISSION_POLICY_AUTO_GRANT }
        RadioButtonItem(R.string.auto_deny, selectedPolicy == PERMISSION_POLICY_AUTO_DENY) { selectedPolicy = PERMISSION_POLICY_AUTO_DENY }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.setPermissionPolicy(receiver,selectedPolicy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_permission_policy)
    }
}

@RequiresApi(34)
@Composable
fun MTEPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var selectedMtePolicy by remember { mutableIntStateOf(dpm.mtePolicy) }
    MyScaffold(R.string.mte_policy, 8.dp, navCtrl) {
        RadioButtonItem(R.string.decide_by_user, selectedMtePolicy == MTE_NOT_CONTROLLED_BY_POLICY) { selectedMtePolicy = MTE_NOT_CONTROLLED_BY_POLICY }
        RadioButtonItem(R.string.enabled, selectedMtePolicy == MTE_ENABLED) { selectedMtePolicy = MTE_ENABLED }
        RadioButtonItem(R.string.disabled, selectedMtePolicy == MTE_DISABLED) { selectedMtePolicy = MTE_DISABLED }
        Button(
            onClick = {
                try {
                    dpm.mtePolicy = selectedMtePolicy
                    context.showOperationResultToast(true)
                } catch(_: java.lang.UnsupportedOperationException) {
                    Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
                }
                selectedMtePolicy = dpm.mtePolicy
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_mte_policy)
    }
}

@RequiresApi(31)
@Composable
fun NearbyStreamingPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var appPolicy by remember { mutableIntStateOf(dpm.nearbyAppStreamingPolicy) }
    MyScaffold(R.string.nearby_streaming_policy, 8.dp, navCtrl) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.nearby_app_streaming), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 3.dp))
        RadioButtonItem(
            R.string.decide_by_user,
            appPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY
        ) { appPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        RadioButtonItem(R.string.enabled, appPolicy == NEARBY_STREAMING_ENABLED) { appPolicy = NEARBY_STREAMING_ENABLED }
        RadioButtonItem(R.string.disabled, appPolicy == NEARBY_STREAMING_DISABLED) { appPolicy = NEARBY_STREAMING_DISABLED }
        RadioButtonItem(
            R.string.enable_if_secure_enough,
            appPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY
        ) { appPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY }
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                dpm.nearbyAppStreamingPolicy = appPolicy
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_nearby_app_streaming_policy)
        var notificationPolicy by remember { mutableIntStateOf(dpm.nearbyNotificationStreamingPolicy) }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.nearby_notification_streaming), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 3.dp))
        RadioButtonItem(
            R.string.decide_by_user,
            notificationPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY
        ) { notificationPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        RadioButtonItem(
            R.string.enabled,
            notificationPolicy == NEARBY_STREAMING_ENABLED
        ) { notificationPolicy = NEARBY_STREAMING_ENABLED }
        RadioButtonItem(
            R.string.disabled,
            notificationPolicy == NEARBY_STREAMING_DISABLED
        ) { notificationPolicy = NEARBY_STREAMING_DISABLED }
        RadioButtonItem(
            R.string.enable_if_secure_enough,
            notificationPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY
        ) { notificationPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY }
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                dpm.nearbyNotificationStreamingPolicy = notificationPolicy
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_nearby_notification_streaming_policy)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun LockTaskMode(navCtrl: NavHostController, vm: MyViewModel) {
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    var tabIndex by remember { mutableIntStateOf(0) }
    tabIndex = pagerState.targetPage
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lock_task_mode)) },
                navigationIcon = { NavIcon { navCtrl.navigateUp() } }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            TabRow(tabIndex) {
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
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 8.dp, end = 8.dp, bottom = 80.dp)
                ) {
                    if(page == 0) StartLockTaskMode(navCtrl, vm)
                    else if(page == 1) LockTaskPackages(navCtrl, vm)
                    else LockTaskFeatures()
                }
            }
        }
    }
}

@RequiresApi(28)
@Composable
private fun ColumnScope.StartLockTaskMode(navCtrl: NavHostController, vm: MyViewModel) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    var startLockTaskApp by rememberSaveable { mutableStateOf("") }
    var startLockTaskActivity by rememberSaveable { mutableStateOf("") }
    var specifyActivity by rememberSaveable { mutableStateOf(false) }
    val updatePackage by vm.selectedPackage.collectAsStateWithLifecycle()
    LaunchedEffect(updatePackage) {
        if(updatePackage != "") {
            startLockTaskApp = updatePackage
            vm.selectedPackage.value = ""
        }
    }
    Spacer(Modifier.padding(vertical = 5.dp))
    OutlinedTextField(
        value = startLockTaskApp,
        onValueChange = { startLockTaskApp = it },
        label = { Text(stringResource(R.string.package_name)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
        trailingIcon = {
            Icon(painter = painterResource(R.drawable.list_fill0), contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = {
                        focusMgr.clearFocus()
                        navCtrl.navigate("PackageSelector")
                    })
                    .padding(3.dp))
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    )
    CheckBoxItem(R.string.specify_activity, specifyActivity) { specifyActivity = it }
    AnimatedVisibility(specifyActivity) {
        OutlinedTextField(
            value = startLockTaskActivity,
            onValueChange = { startLockTaskActivity = it },
            label = { Text("Activity") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(bottom = 5.dp)
        )
    }
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if(!NotificationUtils.checkPermission(context)) return@Button
            if(!dpm.isLockTaskPermitted(startLockTaskApp)) {
                Toast.makeText(context, R.string.app_not_allowed, Toast.LENGTH_SHORT).show()
                return@Button
            }
            val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
            val packageManager = context.packageManager
            val launchIntent = if(specifyActivity) Intent().setComponent(ComponentName(startLockTaskApp, startLockTaskActivity))
            else packageManager.getLaunchIntentForPackage(startLockTaskApp)
            if (launchIntent != null) {
                context.startActivity(launchIntent, options.toBundle())
            } else {
                Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
            }
        }
    ) {
        Text(stringResource(R.string.start))
    }
    InfoCard(R.string.info_start_lock_task_mode)
}

@RequiresApi(26)
@Composable
private fun ColumnScope.LockTaskPackages(navCtrl: NavHostController, vm: MyViewModel) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val lockTaskPackages = remember { mutableStateListOf<String>() }
    var input by rememberSaveable { mutableStateOf("") }
    val updatePackage by vm.selectedPackage.collectAsStateWithLifecycle()
    LaunchedEffect(updatePackage) {
        if(updatePackage != "") {
            input = updatePackage
            vm.selectedPackage.value = ""
        }
    }
    LaunchedEffect(Unit) { lockTaskPackages.addAll(dpm.getLockTaskPackages(receiver)) }
    Spacer(Modifier.padding(vertical = 5.dp))
    if(lockTaskPackages.isEmpty()) Text(text = stringResource(R.string.none))
    for(i in lockTaskPackages) {
        ListItem(i) { lockTaskPackages -= i }
    }
    OutlinedTextField(
        value = input,
        onValueChange = { input = it },
        label = { Text(stringResource(R.string.package_name)) },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
        trailingIcon = {
            Icon(painter = painterResource(R.drawable.list_fill0), contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(onClick = {
                        focusMgr.clearFocus()
                        navCtrl.navigate("PackageSelector")
                    })
                    .padding(3.dp))
        },
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
    )
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Button(
            onClick = {
                lockTaskPackages.add(input)
                input = ""
            },
            modifier = Modifier.fillMaxWidth(0.49F)
        ) {
            Text(stringResource(R.string.add))
        }
        Button(
            onClick = {
                lockTaskPackages.remove(input)
                input = ""
            },
            modifier = Modifier.fillMaxWidth(0.96F)
        ) {
            Text(stringResource(R.string.remove))
        }
    }
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            dpm.setLockTaskPackages(receiver, lockTaskPackages.toTypedArray())
            context.showOperationResultToast(true)
        }
    ) {
        Text(stringResource(R.string.apply))
    }
    InfoCard(R.string.info_lock_task_packages)
}

@RequiresApi(28)
@Composable
private fun ColumnScope.LockTaskFeatures() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var flags by remember { mutableIntStateOf(0) }
    var custom by rememberSaveable { mutableStateOf(false) }
    fun refresh() {
        flags = dpm.getLockTaskFeatures(receiver)
        custom = flags != 0
    }
    LaunchedEffect(Unit) { refresh() }
    Spacer(Modifier.padding(vertical = 5.dp))
    RadioButtonItem(R.string.disable_all, !custom) { custom = false }
    RadioButtonItem(R.string.custom, custom) { custom = true }
    AnimatedVisibility(custom) {
        Column {
            CheckBoxItem(
                R.string.ltf_sys_info,
                flags and LOCK_TASK_FEATURE_SYSTEM_INFO != 0
            ) { flags = flags xor LOCK_TASK_FEATURE_SYSTEM_INFO }
            CheckBoxItem(
                R.string.ltf_notifications,
                flags and LOCK_TASK_FEATURE_NOTIFICATIONS != 0
            ) { flags = flags xor LOCK_TASK_FEATURE_NOTIFICATIONS }
            CheckBoxItem(
                R.string.ltf_home,
                flags and LOCK_TASK_FEATURE_HOME != 0
            ) { flags = flags xor LOCK_TASK_FEATURE_HOME }
            CheckBoxItem(
                R.string.ltf_overview,
                flags and LOCK_TASK_FEATURE_OVERVIEW != 0
            ) { flags = flags xor LOCK_TASK_FEATURE_OVERVIEW }
            CheckBoxItem(
                R.string.ltf_global_actions,
                flags and LOCK_TASK_FEATURE_GLOBAL_ACTIONS != 0
            ) { flags = flags xor LOCK_TASK_FEATURE_GLOBAL_ACTIONS }
            CheckBoxItem(
                R.string.ltf_keyguard,
                flags and LOCK_TASK_FEATURE_KEYGUARD != 0
            ) { flags = flags xor LOCK_TASK_FEATURE_KEYGUARD }
            if(VERSION.SDK_INT >= 30) {
                CheckBoxItem(
                    R.string.ltf_block_activity_start_in_task,
                    flags and LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK != 0
                ) { flags = flags xor LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK }
            }
        }
    }
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            try {
                dpm.setLockTaskFeatures(receiver, flags)
                context.showOperationResultToast(true)
            } catch (e: IllegalArgumentException) {
                AlertDialog.Builder(context)
                    .setTitle(R.string.error)
                    .setMessage(e.message)
                    .setPositiveButton(R.string.confirm) { dialog, _ -> dialog.dismiss() }
                    .show()
            }
            refresh()
        }
    ) {
        Text(stringResource(R.string.apply))
    }
}

@Composable
fun CACert(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var dialog by remember { mutableStateOf(false) }
    var caCertByteArray = remember { byteArrayOf() }
    val getFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri ?: return@rememberLauncherForActivityResult
        uriToStream(context, uri) {
            caCertByteArray = it.readBytes()
        }
        dialog = true
    }
    MyScaffold(R.string.ca_cert, 8.dp, navCtrl) {
        Button(
            onClick = {
                getFileLauncher.launch("*/*")
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Text(stringResource(R.string.select_ca_cert))
        }
        Button(
            onClick = {
                dpm.uninstallAllUserCaCerts(receiver)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.uninstall_all_user_ca_cert))
        }
        if(dialog) {
            val exist = dpm.hasCaCertInstalled(receiver, caCertByteArray)
            AlertDialog(
                confirmButton = {
                    TextButton({
                        if(exist) {
                            dpm.uninstallCaCert(receiver, caCertByteArray)
                        } else {
                            val result = dpm.installCaCert(receiver, caCertByteArray)
                            context.showOperationResultToast(result)
                        }
                        dialog = false
                    }) {
                        Text(stringResource(if(exist) R.string.uninstall else R.string.install))
                    }
                },
                dismissButton = {
                    TextButton({ dialog = false }) { Text(stringResource(R.string.cancel)) }
                },
                onDismissRequest = { dialog = false }
            )
        }
    }
}

@RequiresApi(24)
@Composable
fun SecurityLogging(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val logFile = context.filesDir.resolve("SecurityLogs.json")
    var fileSize by remember { mutableLongStateOf(0) }
    LaunchedEffect(Unit) { fileSize = logFile.length() }
    var preRebootSecurityLogs by remember { mutableStateOf(byteArrayOf()) }
    val exportPreRebootSecurityLogs = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outStream ->
                preRebootSecurityLogs.inputStream().copyTo(outStream)
            }
        }
    }
    val exportSecurityLogs = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outStream ->
                outStream.write("[".toByteArray())
                logFile.inputStream().use { it.copyTo(outStream) }
                outStream.write("]".toByteArray())
                context.showOperationResultToast(true)
            }
        }
    }
    MyScaffold(R.string.security_logging, 8.dp, navCtrl) {
        SwitchItem(
            R.string.enable,
            getState = { dpm.isSecurityLoggingEnabled(receiver) }, onCheckedChange = { dpm.setSecurityLoggingEnabled(receiver, it) },
            padding = false
        )
        Text(stringResource(R.string.log_file_size_is, formatFileSize(fileSize)))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/json")
                    intent.putExtra(Intent.EXTRA_TITLE, "SecurityLogs.json")
                    exportSecurityLogs.launch(intent)
                },
                enabled = fileSize > 0,
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.export_logs))
            }
            Button(
                onClick = {
                    logFile.delete()
                    fileSize = logFile.length()
                },
                enabled = fileSize > 0,
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.delete_logs))
            }
        }
        InfoCard(R.string.info_security_log)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                val logs = dpm.retrievePreRebootSecurityLogs(receiver)
                if(logs == null) {
                    Toast.makeText(context, R.string.no_logs, Toast.LENGTH_SHORT).show()
                    return@Button
                } else {
                    val outputStream = ByteArrayOutputStream()
                    outputStream.write("[".encodeToByteArray())
                    processSecurityLogs(logs, outputStream)
                    outputStream.write("]".encodeToByteArray())
                    preRebootSecurityLogs = outputStream.toByteArray()
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/json")
                    intent.putExtra(Intent.EXTRA_TITLE, "PreRebootSecurityLogs.json")
                    exportPreRebootSecurityLogs.launch(intent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pre_reboot_security_logs))
        }
        InfoCard(R.string.info_pre_reboot_security_log)
    }
}

@Composable
fun DisableAccountManagement(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.disable_account_management, 8.dp, navCtrl) {
        val list = remember { mutableStateListOf<String>() }
        fun refreshList() {
            list.clear()
            dpm.accountTypesWithManagementDisabled?.forEach { list += it }
        }
        LaunchedEffect(Unit) { refreshList() }
        Column(modifier = Modifier.animateContentSize()) {
            if(list.isEmpty()) Text(stringResource(R.string.none))
            for(i in list) {
                ListItem(i) {
                    dpm.setAccountManagementDisabled(receiver, i, false)
                    refreshList()
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
                        dpm.setAccountManagementDisabled(receiver, inputText, true)
                        inputText = ""
                        refreshList()
                    },
                    enabled = inputText != ""
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 10.dp))
        InfoCard(R.string.info_disable_account_management)
    }
}

@RequiresApi(30)
@Composable
fun FRPPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    val receiver = context.getReceiver()
    var usePolicy by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(false) }
    var unsupported by remember { mutableStateOf(false) }
    val accountList = remember { mutableStateListOf<String>() }
    var inputAccount by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        var policy: FactoryResetProtectionPolicy? = FactoryResetProtectionPolicy.Builder().build()
        try {
            policy = dpm.getFactoryResetProtectionPolicy(receiver)
        } catch(_: UnsupportedOperationException) {
            unsupported = true
            policy = null
        } finally {
            if(policy == null) {
                usePolicy = false
            } else {
                usePolicy = true
                enabled = policy.isFactoryResetProtectionEnabled
            }
        }
    }
    MyScaffold(R.string.frp_policy, 8.dp, navCtrl) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.use_policy), style = typography.titleLarge)
            Switch(checked = usePolicy, onCheckedChange = { usePolicy = it })
        }
        AnimatedVisibility(usePolicy) {
            Column {
                CheckBoxItem(R.string.enable_frp, enabled) { enabled = it }
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
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(vertical = 2.dp))
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                if(unsupported) {
                    Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
                } else {
                    val policy = FactoryResetProtectionPolicy.Builder()
                        .setFactoryResetProtectionEnabled(enabled)
                        .setFactoryResetProtectionAccounts(accountList)
                        .build()
                    dpm.setFactoryResetProtectionPolicy(receiver, policy)
                }
            },
            modifier = Modifier.width(100.dp).align(Alignment.CenterHorizontally)
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        if(unsupported) Text(stringResource(R.string.frp_policy_not_supported))
        Spacer(Modifier.padding(vertical = 6.dp))
        InfoCard(R.string.info_frp_policy)
    }
}

@Composable
fun WipeData(navCtrl: NavHostController) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    var flag by remember { mutableIntStateOf(0) }
    var warning by remember { mutableStateOf(false) }
    var wipeDevice by remember { mutableStateOf(false) }
    var silent by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    MyScaffold(R.string.wipe_data, 8.dp, navCtrl) {
        CheckBoxItem(R.string.wipe_external_storage, flag and WIPE_EXTERNAL_STORAGE != 0) { flag = flag xor WIPE_EXTERNAL_STORAGE }
        if(VERSION.SDK_INT >= 22 && context.isDeviceOwner) CheckBoxItem(
            R.string.wipe_reset_protection_data, flag and WIPE_RESET_PROTECTION_DATA != 0) { flag = flag xor WIPE_RESET_PROTECTION_DATA }
        if(VERSION.SDK_INT >= 28) CheckBoxItem(R.string.wipe_euicc, flag and WIPE_EUICC != 0) { flag = flag xor WIPE_EUICC }
        if(VERSION.SDK_INT >= 29) CheckBoxItem(R.string.wipe_silently, silent) { silent = it }
        AnimatedVisibility(!silent && VERSION.SDK_INT >= 28) {
            OutlinedTextField(
                value = reason, onValueChange = { reason = it },
                label = { Text(stringResource(R.string.reason)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            )
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT < 34 || !userManager.isSystemUser) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    wipeDevice = false
                    warning = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("WipeData")
            }
        }
        if (VERSION.SDK_INT >= 34 && context.isDeviceOwner) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    wipeDevice = true
                    warning = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("WipeDevice")
            }
        }
    }
    if(warning) {
        LaunchedEffect(Unit) { silent = reason == "" }
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
            onDismissRequest = { warning = false },
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
                        if(silent && VERSION.SDK_INT >= 29) { flag = flag or WIPE_SILENTLY }
                        if(wipeDevice && VERSION.SDK_INT >= 34) {
                            dpm.wipeDevice(flag)
                        } else {
                            if(VERSION.SDK_INT >= 28 && reason != "") {
                                dpm.wipeData(flag, reason)
                            } else {
                                dpm.wipeData(flag)
                            }
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
                    modifier = Modifier.animateContentSize(),
                    enabled = timer == 0
                ) {
                    Text(stringResource(R.string.confirm) + timerText)
                }
            },
            dismissButton = {
                TextButton(onClick = { warning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
fun SystemUpdatePolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.system_update_policy, 8.dp, navCtrl) {
        if(VERSION.SDK_INT >= 23) {
            Column {
                var selectedPolicy by remember { mutableStateOf(dpm.systemUpdatePolicy?.policyType) }
                RadioButtonItem(
                    R.string.system_update_policy_automatic,
                    selectedPolicy == TYPE_INSTALL_AUTOMATIC
                ) { selectedPolicy = TYPE_INSTALL_AUTOMATIC }
                RadioButtonItem(
                    R.string.system_update_policy_install_windowed,
                    selectedPolicy == TYPE_INSTALL_WINDOWED
                ) { selectedPolicy = TYPE_INSTALL_WINDOWED }
                RadioButtonItem(
                    R.string.system_update_policy_postpone,
                    selectedPolicy == TYPE_POSTPONE
                ) { selectedPolicy = TYPE_POSTPONE }
                RadioButtonItem(R.string.none, selectedPolicy == null) { selectedPolicy = null }
                var windowedPolicyStart by remember { mutableStateOf("") }
                var windowedPolicyEnd by remember { mutableStateOf("") }
                AnimatedVisibility(selectedPolicy == 2) {
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween
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
                                onValueChange = {windowedPolicyEnd = it },
                                label = { Text(stringResource(R.string.end_time)) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                                modifier = Modifier.fillMaxWidth(0.96F).padding(bottom = 2.dp)
                            )
                        }
                        Text(text = stringResource(R.string.minutes_in_one_day))
                    }
                }
                Button(
                    onClick = {
                        val policy =
                            when(selectedPolicy) {
                                TYPE_INSTALL_AUTOMATIC-> SystemUpdatePolicy.createAutomaticInstallPolicy()
                                TYPE_INSTALL_WINDOWED-> SystemUpdatePolicy.createWindowedInstallPolicy(windowedPolicyStart.toInt(), windowedPolicyEnd.toInt())
                                TYPE_POSTPONE-> SystemUpdatePolicy.createPostponeInstallPolicy()
                                else -> null
                            }
                        dpm.setSystemUpdatePolicy(receiver,policy)
                        context.showOperationResultToast(true)
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
        if(VERSION.SDK_INT >= 26) {
            Spacer(Modifier.padding(vertical = 10.dp))
            val sysUpdateInfo = dpm.getPendingSystemUpdate(receiver)
            Column {
                if(sysUpdateInfo != null) {
                    Text(text = stringResource(R.string.update_received_time, Date(sysUpdateInfo.receivedTime)))
                    val securityStateDesc = when(sysUpdateInfo.securityPatchState) {
                        SystemUpdateInfo.SECURITY_PATCH_STATE_UNKNOWN -> stringResource(R.string.unknown)
                        SystemUpdateInfo.SECURITY_PATCH_STATE_TRUE -> "true"
                        else->"false"
                    }
                    Text(text = stringResource(R.string.is_security_patch, securityStateDesc))
                }else{
                    Text(text = stringResource(R.string.no_system_update))
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun InstallSystemUpdate(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val callback = object: InstallSystemUpdateCallback() {
        override fun onInstallUpdateError(errorCode: Int, errorMessage: String) {
            super.onInstallUpdateError(errorCode, errorMessage)
            val errDetail = when(errorCode) {
                UPDATE_ERROR_BATTERY_LOW -> R.string.battery_low
                UPDATE_ERROR_UPDATE_FILE_INVALID -> R.string.update_file_invalid
                UPDATE_ERROR_INCORRECT_OS_VERSION -> R.string.incorrect_os_ver
                UPDATE_ERROR_FILE_NOT_FOUND -> R.string.file_not_exist
                else -> R.string.unknown
            }
            val errMsg = context.getString(R.string.install_system_update_failed) + context.getString(errDetail)
            Toast.makeText(context, errMsg, Toast.LENGTH_SHORT).show()
        }
    }
    var uri by remember { mutableStateOf<Uri?>(null) }
    val getFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        uri = it.data?.data
    }
    MyScaffold(R.string.install_system_update, 8.dp, navCtrl) {
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("application/zip")
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                getFileLauncher.launch(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_ota_package))
        }
        AnimatedVisibility(uri != null) {
            Button(
                onClick = {
                    val executor = Executors.newCachedThreadPool()
                    try {
                        dpm.installSystemUpdate(receiver, uri!!, executor, callback)
                        Toast.makeText(context, R.string.start_install_system_update, Toast.LENGTH_SHORT).show()
                    } catch(e: Exception) {
                        Toast.makeText(
                            context,
                            context.getString(R.string.install_system_update_failed) + e.cause.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.install_system_update))
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        InfoCard(R.string.auto_reboot_after_install_succeed)
    }
}
