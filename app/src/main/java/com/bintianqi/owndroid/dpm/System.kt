package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.AlertDialog
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
import android.os.UserManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
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
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.NotificationUtils
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.exportFile
import com.bintianqi.owndroid.exportFilePath
import com.bintianqi.owndroid.fileUriFlow
import com.bintianqi.owndroid.formatFileSize
import com.bintianqi.owndroid.getFile
import com.bintianqi.owndroid.humanReadableDate
import com.bintianqi.owndroid.isExportingSecurityOrNetworkLogs
import com.bintianqi.owndroid.toggle
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoCard
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.uriToStream
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.encodeToStream
import kotlinx.serialization.json.put
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.math.pow

@SuppressLint("NewApi")
@Composable
fun SystemManage(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    val dhizuku = sharedPref.getBoolean("dhizuku", false)
    val dangerousFeatures = sharedPref.getBoolean("dangerous_features", false)
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.system, 0.dp, navCtrl) {
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.options, "", R.drawable.tune_fill0) { navCtrl.navigate("SystemOptions") }
        }
        FunctionItem(R.string.keyguard, "", R.drawable.screen_lock_portrait_fill0) { navCtrl.navigate("Keyguard") }
        if(VERSION.SDK_INT >= 24 && deviceOwner) {
            FunctionItem(R.string.reboot, "", R.drawable.restart_alt_fill0) { dialog = 1 }
        }
        if(deviceOwner && ((VERSION.SDK_INT >= 28 && dpm.isAffiliatedUser) || VERSION.SDK_INT >= 24)) {
            FunctionItem(R.string.bug_report, "", R.drawable.bug_report_fill0) { dialog = 2 }
        }
        if(VERSION.SDK_INT >= 28 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.change_time, "", R.drawable.schedule_fill0) { navCtrl.navigate("ChangeTime") }
            FunctionItem(R.string.change_timezone, "", R.drawable.schedule_fill0) { navCtrl.navigate("ChangeTimeZone") }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.permission_policy, "", R.drawable.key_fill0) { navCtrl.navigate("PermissionPolicy") }
        }
        if(VERSION.SDK_INT >= 34 && deviceOwner) {
            FunctionItem(R.string.mte_policy, "", R.drawable.memory_fill0) { navCtrl.navigate("MTEPolicy") }
        }
        if(VERSION.SDK_INT >= 31 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.nearby_streaming_policy, "", R.drawable.share_fill0) { navCtrl.navigate("NearbyStreamingPolicy") }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            FunctionItem(R.string.lock_task_mode, "", R.drawable.lock_fill0) { navCtrl.navigate("LockTaskMode") }
        }
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.ca_cert, "", R.drawable.license_fill0) { navCtrl.navigate("CACert") }
        }
        if(VERSION.SDK_INT >= 26 && !dhizuku && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.security_logging, "", R.drawable.description_fill0) { navCtrl.navigate("SecurityLogging") }
        }
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.disable_account_management, "", R.drawable.account_circle_fill0) { navCtrl.navigate("DisableAccountManagement") }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.system_update_policy, "", R.drawable.system_update_fill0) { navCtrl.navigate("SystemUpdatePolicy") }
        }
        if(VERSION.SDK_INT >= 29 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.install_system_update, "", R.drawable.system_update_fill0) { navCtrl.navigate("InstallSystemUpdate") }
        }
        if(VERSION.SDK_INT >= 30 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.frp_policy, "", R.drawable.device_reset_fill0) { navCtrl.navigate("FRPPolicy") }
        }
        if(dangerousFeatures && context.isDeviceAdmin && !(VERSION.SDK_INT >= 24 && profileOwner && dpm.isManagedProfile(receiver))) {
            FunctionItem(R.string.wipe_data, "", R.drawable.device_reset_fill0) { navCtrl.navigate("WipeData") }
        }
        LaunchedEffect(Unit) { fileUriFlow.value = Uri.parse("") }
    }
    if(dialog != 0) AlertDialog(
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
                        val result = dpm.requestBugreport(receiver)
                        Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
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
            SwitchItem(R.string.disable_cam,"", R.drawable.photo_camera_fill0,
                { dpm.getCameraDisabled(null) }, { dpm.setCameraDisabled(receiver,it) }
            )
        }
        if(deviceOwner || profileOwner) {
            SwitchItem(R.string.disable_screen_capture, "", R.drawable.screenshot_fill0,
                { dpm.getScreenCaptureDisabled(null) }, { dpm.setScreenCaptureDisabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 34 && (deviceOwner || (profileOwner && dpm.isAffiliatedUser))) {
            SwitchItem(R.string.disable_status_bar, "", R.drawable.notifications_fill0,
                { dpm.isStatusBarDisabled}, { dpm.setStatusBarDisabled(receiver,it) }
            )
        }
        if(deviceOwner || (VERSION.SDK_INT >= 23 && profileOwner && um.isSystemUser) || dpm.isOrgProfile(receiver)) {
            if(VERSION.SDK_INT >= 30) {
                SwitchItem(R.string.auto_time, "", R.drawable.schedule_fill0,
                    { dpm.getAutoTimeEnabled(receiver) }, { dpm.setAutoTimeEnabled(receiver,it) }
                )
                SwitchItem(R.string.auto_timezone, "", R.drawable.globe_fill0,
                    { dpm.getAutoTimeZoneEnabled(receiver) }, { dpm.setAutoTimeZoneEnabled(receiver,it) }
                )
            }else{
                SwitchItem(R.string.require_auto_time, "", R.drawable.schedule_fill0, { dpm.autoTimeRequired}, { dpm.setAutoTimeRequired(receiver,it) }, padding = false)
            }
        }
        if(deviceOwner || (profileOwner && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && !dpm.isManagedProfile(receiver))))) {
            SwitchItem(R.string.master_mute, "", R.drawable.volume_up_fill0,
                { dpm.isMasterVolumeMuted(receiver) }, { dpm.setMasterVolumeMuted(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner)) {
            SwitchItem(R.string.backup_service, "", R.drawable.backup_fill0,
                { dpm.isBackupServiceEnabled(receiver) }, { dpm.setBackupServiceEnabled(receiver,it) },
                onClickBlank = { dialog = 1 }
            )
        }
        if(VERSION.SDK_INT >= 24 && profileOwner && dpm.isManagedProfile(receiver)) {
            SwitchItem(R.string.disable_bt_contact_share, "", R.drawable.account_circle_fill0,
                { dpm.getBluetoothContactSharingDisabled(receiver) }, { dpm.setBluetoothContactSharingDisabled(receiver,it) },
            )
        }
        if(VERSION.SDK_INT >= 30 && deviceOwner) {
            SwitchItem(R.string.common_criteria_mode , "",R.drawable.security_fill0,
                { dpm.isCommonCriteriaModeEnabled(receiver) }, { dpm.setCommonCriteriaModeEnabled(receiver,it) },
                onClickBlank = { dialog = 2 }
            )
        }
        if(VERSION.SDK_INT >= 31 && (deviceOwner || dpm.isOrgProfile(receiver)) && dpm.canUsbDataSignalingBeDisabled()) {
            SwitchItem(
                R.string.disable_usb_signal, "", R.drawable.usb_fill0, { !dpm.isUsbDataSignalingEnabled },
                { dpm.isUsbDataSignalingEnabled = !it },
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
                    onClick = {
                        Toast.makeText(context, if(dpm.setKeyguardDisabled(receiver,true)) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                    },
                    enabled = deviceOwner || (VERSION.SDK_INT >= 28 && profileOwner && dpm.isAffiliatedUser),
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.disable))
                }
                Button(
                    onClick = {
                        Toast.makeText(context, if(dpm.setKeyguardDisabled(receiver,false)) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                    },
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
                R.string.evict_credential_encryptoon_key,
                flag == FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY,
                { flag = if(flag==0) {1}else{0} }
            )
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
@SuppressLint("NewApi")
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
                val result = dpm.setTime(receiver, timeMillis)
                Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
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

@SuppressLint("NewApi")
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
                val result = dpm.setTimeZone(receiver, inputTimezone)
                Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
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

@SuppressLint("NewApi")
@Composable
fun PermissionPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var selectedPolicy by remember { mutableIntStateOf(dpm.getPermissionPolicy(receiver)) }
    MyScaffold(R.string.permission_policy, 8.dp, navCtrl) {
        RadioButtonItem(R.string.default_stringres, selectedPolicy == PERMISSION_POLICY_PROMPT, { selectedPolicy = PERMISSION_POLICY_PROMPT })
        RadioButtonItem(R.string.auto_grant, selectedPolicy == PERMISSION_POLICY_AUTO_GRANT, { selectedPolicy = PERMISSION_POLICY_AUTO_GRANT })
        RadioButtonItem(R.string.auto_deny, selectedPolicy == PERMISSION_POLICY_AUTO_DENY, { selectedPolicy = PERMISSION_POLICY_AUTO_DENY })
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.setPermissionPolicy(receiver,selectedPolicy)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_permission_policy)
    }
}

@SuppressLint("NewApi")
@Composable
fun MTEPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var selectedMtePolicy by remember { mutableIntStateOf(dpm.mtePolicy) }
    MyScaffold(R.string.mte_policy, 8.dp, navCtrl) {
        RadioButtonItem(
            R.string.decide_by_user,
            selectedMtePolicy == MTE_NOT_CONTROLLED_BY_POLICY,
            { selectedMtePolicy = MTE_NOT_CONTROLLED_BY_POLICY }
        )
        RadioButtonItem(
            R.string.enabled,
            selectedMtePolicy == MTE_ENABLED,
            { selectedMtePolicy = MTE_ENABLED }
        )
        RadioButtonItem(
            R.string.disabled,
            selectedMtePolicy == MTE_DISABLED,
            { selectedMtePolicy = MTE_DISABLED }
        )
        Button(
            onClick = {
                try {
                    dpm.mtePolicy = selectedMtePolicy
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
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

@SuppressLint("NewApi")
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
            appPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY,
            { appPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        )
        RadioButtonItem(
            R.string.enabled,
            appPolicy == NEARBY_STREAMING_ENABLED,
            { appPolicy = NEARBY_STREAMING_ENABLED }
        )
        RadioButtonItem(
            R.string.disabled,
            appPolicy == NEARBY_STREAMING_DISABLED,
            { appPolicy = NEARBY_STREAMING_DISABLED }
        )
        RadioButtonItem(
            R.string.enable_if_secure_enough,
            appPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY,
            { appPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY }
        )
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                dpm.nearbyAppStreamingPolicy = appPolicy
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
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
            notificationPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY,
            { notificationPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        )
        RadioButtonItem(
            R.string.enabled,
            notificationPolicy == NEARBY_STREAMING_ENABLED,
            { notificationPolicy = NEARBY_STREAMING_ENABLED }
        )
        RadioButtonItem(
            R.string.disabled,
            notificationPolicy == NEARBY_STREAMING_DISABLED,
            { notificationPolicy = NEARBY_STREAMING_DISABLED }
        )
        RadioButtonItem(
            R.string.enable_if_secure_enough,
            notificationPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY,
            { notificationPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY }
        )
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                dpm.nearbyNotificationStreamingPolicy = notificationPolicy
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_nearby_notification_streaming_policy)
    }
}

@SuppressLint("NewApi")
@Composable
fun LockTaskMode(navCtrl: NavHostController, vm: MyViewModel) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var appSelectorRequest by rememberSaveable { mutableIntStateOf(0) }
    MyScaffold(R.string.lock_task_mode, 8.dp, navCtrl, false) {
        val lockTaskFeatures = remember { mutableStateListOf<Int>() }
        var custom by rememberSaveable { mutableStateOf(false) }
        val refreshFeature = {
            var calculate = dpm.getLockTaskFeatures(receiver)
            lockTaskFeatures.clear()
            if(calculate != 0) {
                var sq = 10
                while(sq >= 1) {
                    val current = (2).toDouble().pow(sq.toDouble()).toInt()
                    if(calculate - current >= 0) {
                        lockTaskFeatures += current
                        calculate -= current
                    }
                    sq--
                }
                if(calculate - 1 >= 0) { lockTaskFeatures += 1 }
                custom = true
            } else {
                custom = false
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.lock_task_feature), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        LaunchedEffect(Unit) { refreshFeature() }
        RadioButtonItem(R.string.disable_all, !custom, { custom = false })
        RadioButtonItem(R.string.custom, custom, { custom = true })
        AnimatedVisibility(custom) {
            Column {
                CheckBoxItem(
                    R.string.ltf_sys_info,
                    LOCK_TASK_FEATURE_SYSTEM_INFO in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_SYSTEM_INFO) }
                )
                CheckBoxItem(
                    R.string.ltf_notifications,
                    LOCK_TASK_FEATURE_NOTIFICATIONS in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_NOTIFICATIONS) }
                )
                CheckBoxItem(
                    R.string.ltf_home,
                    LOCK_TASK_FEATURE_HOME in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_HOME) }
                )
                CheckBoxItem(
                    R.string.ltf_overview,
                    LOCK_TASK_FEATURE_OVERVIEW in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_OVERVIEW) }
                )
                CheckBoxItem(
                    R.string.ltf_global_actions,
                    LOCK_TASK_FEATURE_GLOBAL_ACTIONS in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_GLOBAL_ACTIONS) }
                )
                CheckBoxItem(
                    R.string.ltf_keyguard,
                    LOCK_TASK_FEATURE_KEYGUARD in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_KEYGUARD) }
                )
                if(VERSION.SDK_INT >= 30) {
                    CheckBoxItem(
                        R.string.ltf_block_activity_start_in_task,
                        LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK in lockTaskFeatures,
                        { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK) }
                    )
                }
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                var result = 0
                if(custom) {
                    lockTaskFeatures.forEach { result += it }
                }
                try {
                    dpm.setLockTaskFeatures(receiver, result)
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setMessage(e.message)
                        .setPositiveButton(R.string.confirm) { dialog, _ -> dialog.dismiss() }
                        .show()
                }
                refreshFeature()
            }
        ) {
            Text(stringResource(R.string.apply))
        }

        val lockTaskPackages = remember { mutableStateListOf<String>() }
        var inputLockTaskPkg by rememberSaveable { mutableStateOf("") }
        LaunchedEffect(Unit) { lockTaskPackages.addAll(dpm.getLockTaskPackages(receiver)) }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.lock_task_packages), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Column(modifier = Modifier.animateContentSize()) {
            if(lockTaskPackages.isEmpty()) Text(text = stringResource(R.string.none))
            for(i in lockTaskPackages) {
                ListItem(i) { lockTaskPackages -= i }
            }
        }
        OutlinedTextField(
            value = inputLockTaskPkg,
            onValueChange = { inputLockTaskPkg = it },
            label = { Text(stringResource(R.string.package_name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.list_fill0), contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = {
                            focusMgr.clearFocus()
                            appSelectorRequest = 1
                            navCtrl.navigate("PackageSelector")
                        })
                        .padding(3.dp))
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    lockTaskPackages.add(inputLockTaskPkg)
                    inputLockTaskPkg = ""
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    lockTaskPackages.remove(inputLockTaskPkg)
                    inputLockTaskPkg = ""
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
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            }
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_lock_task_packages)
        var startLockTaskApp by rememberSaveable { mutableStateOf("") }
        var startLockTaskActivity by rememberSaveable { mutableStateOf("") }
        var specifyActivity by rememberSaveable { mutableStateOf(false) }
        val updatePackage by vm.selectedPackage.collectAsStateWithLifecycle()
        LaunchedEffect(updatePackage) {
            if(updatePackage != "") {
                if(appSelectorRequest == 1) inputLockTaskPkg = updatePackage else startLockTaskApp = updatePackage
                vm.selectedPackage.value = ""
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.start_lock_task_mode), style = typography.headlineLarge)
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
                            appSelectorRequest = 2
                            navCtrl.navigate("PackageSelector")
                        })
                        .padding(3.dp))
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
        )
        CheckBoxItem(R.string.specify_activity, specifyActivity, { specifyActivity = it })
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
}

@Composable
fun CACert(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val uri by fileUriFlow.collectAsState()
    var exist by remember { mutableStateOf(false) }
    val uriPath = uri.path ?: ""
    var caCertByteArray by remember { mutableStateOf(byteArrayOf()) }
    LaunchedEffect(uri) {
        if(uri != Uri.parse("")) {
            uriToStream(context, uri) {
                val array = it.readBytes()
                caCertByteArray = if(array.size < 10000) {
                    array
                }else{
                    byteArrayOf()
                }
            }
            exist = dpm.hasCaCertInstalled(receiver, caCertByteArray)
        }
    }
    MyScaffold(R.string.ca_cert, 8.dp, navCtrl) {
        AnimatedVisibility(uriPath != "") {
            Text(text = uriPath)
        }
        Text(
            text = if(uriPath == "") { stringResource(R.string.please_select_ca_cert) } else { stringResource(R.string.cert_installed, exist) },
            modifier = Modifier.animateContentSize()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                val caCertIntent = Intent(Intent.ACTION_GET_CONTENT)
                caCertIntent.setType("*/*")
                caCertIntent.addCategory(Intent.CATEGORY_OPENABLE)
                getFile.launch(caCertIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_ca_cert))
        }
        AnimatedVisibility(uriPath != "") {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        val result = dpm.installCaCert(receiver, caCertByteArray)
                        Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                        exist = dpm.hasCaCertInstalled(receiver, caCertByteArray)
                    },
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.install))
                }
                Button(
                    onClick = {
                        if(exist) {
                            dpm.uninstallCaCert(receiver, caCertByteArray)
                            exist = dpm.hasCaCertInstalled(receiver, caCertByteArray)
                            Toast.makeText(context, if(exist) R.string.failed else R.string.success, Toast.LENGTH_SHORT).show()
                        } else { Toast.makeText(context, R.string.not_exist, Toast.LENGTH_SHORT).show() }
                    },
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text(stringResource(R.string.uninstall))
                }
            }
        }
        Button(
            onClick = {
                dpm.uninstallAllUserCaCerts(receiver)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.uninstall_all_user_ca_cert))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun SecurityLogging(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val logFile = context.filesDir.resolve("SecurityLogs.json")
    var fileSize by remember { mutableLongStateOf(0) }
    LaunchedEffect(Unit) {
        fileSize = logFile.length()
    }
    MyScaffold(R.string.security_logging, 8.dp, navCtrl) {
        SwitchItem(R.string.enable, "", null, { dpm.isSecurityLoggingEnabled(receiver) }, { dpm.setSecurityLoggingEnabled(receiver, it) }, padding = false)
        Text(stringResource(R.string.log_file_size_is, formatFileSize(fileSize)))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/json")
                    intent.putExtra(Intent.EXTRA_TITLE, "SecurityLogs.json")
                    exportFilePath = logFile.path
                    isExportingSecurityOrNetworkLogs = true
                    exportFile.launch(intent)
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
                    val securityEvents = buildJsonArray {
                        logs.forEach { event ->
                            addJsonObject {
                                put("time_nanos", event.timeNanos)
                                put("tag", event.tag)
                                if(VERSION.SDK_INT >= 28) put("level", event.logLevel)
                                if(VERSION.SDK_INT >= 28) put("id", event.id)
                                parseSecurityEventData(event).let { if(it != null) put("data", it) }
                            }
                        }
                    }
                    val preRebootSecurityLogs = context.filesDir.resolve("PreRebootSecurityLogs")
                    preRebootSecurityLogs.outputStream().use {
                        val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
                        json.encodeToStream(securityEvents, it)
                    }
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/json")
                    intent.putExtra(Intent.EXTRA_TITLE, "PreRebootSecurityLogs.json")
                    exportFilePath = preRebootSecurityLogs.path
                    isExportingSecurityOrNetworkLogs = true
                    exportFile.launch(intent)
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

@SuppressLint("NewApi")
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
                CheckBoxItem(R.string.enable_frp, enabled, { enabled = it })
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

@SuppressLint("NewApi")
@Composable
fun WipeData(navCtrl: NavHostController) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    var warning by remember { mutableStateOf(false) }
    var wipeDevice by remember { mutableStateOf(false) }
    var externalStorage by remember { mutableStateOf(false) }
    var protectionData by remember { mutableStateOf(false) }
    var euicc by remember { mutableStateOf(false) }
    var silent by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    MyScaffold(R.string.wipe_data, 8.dp, navCtrl) {
        CheckBoxItem(
            R.string.wipe_external_storage,
            externalStorage, { externalStorage = it }
        )
        if(VERSION.SDK_INT >= 22 && context.isDeviceOwner) {
            CheckBoxItem(R.string.wipe_reset_protection_data,
                protectionData, { protectionData = it }
            )
        }
        if(VERSION.SDK_INT >= 28) { CheckBoxItem(R.string.wipe_euicc, euicc, { euicc = it }) }
        if(VERSION.SDK_INT >= 29) { CheckBoxItem(R.string.wipe_silently, silent, { silent = it }) }
        AnimatedVisibility(!silent && VERSION.SDK_INT >= 28) {
            OutlinedTextField(
                value = reason, onValueChange = { reason = it },
                label = { Text(stringResource(R.string.reason)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            )
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT < 34 || (VERSION.SDK_INT >= 34 && !userManager.isSystemUser)) {
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
                    text = stringResource(if(userManager.isSystemUser) R.string.wipe_data_warning else R.string.info_wipe_data_in_managed_user),
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
                        var flag = 0
                        if(externalStorage) { flag += WIPE_EXTERNAL_STORAGE }
                        if(protectionData && VERSION.SDK_INT >= 22) { flag += WIPE_RESET_PROTECTION_DATA }
                        if(euicc && VERSION.SDK_INT >= 28) { flag += WIPE_EUICC }
                        if(silent && VERSION.SDK_INT >= 29) { flag += WIPE_SILENTLY }
                        if(wipeDevice) {
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
                    selectedPolicy == TYPE_INSTALL_AUTOMATIC, { selectedPolicy = TYPE_INSTALL_AUTOMATIC }
                )
                RadioButtonItem(
                    R.string.system_update_policy_install_windowed,
                    selectedPolicy == TYPE_INSTALL_WINDOWED, { selectedPolicy = TYPE_INSTALL_WINDOWED }
                )
                RadioButtonItem(
                    R.string.system_update_policy_postpone,
                    selectedPolicy == TYPE_POSTPONE, { selectedPolicy = TYPE_POSTPONE }
                )
                RadioButtonItem(R.string.none, selectedPolicy == null, { selectedPolicy = null })
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
                        Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
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
    val uri by fileUriFlow.collectAsState()
    MyScaffold(R.string.install_system_update, 8.dp, navCtrl) {
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                intent.setType("application/zip")
                intent.addCategory(Intent.CATEGORY_OPENABLE)
                getFile.launch(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_ota_package))
        }
        AnimatedVisibility(uri != Uri.parse("")) {
            Button(
                onClick = {
                    val executor = Executors.newCachedThreadPool()
                    try {
                        dpm.installSystemUpdate(receiver, uri, executor, callback)
                        Toast.makeText(context, R.string.start_install_system_update, Toast.LENGTH_SHORT).show()
                    }catch(e: Exception) {
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
