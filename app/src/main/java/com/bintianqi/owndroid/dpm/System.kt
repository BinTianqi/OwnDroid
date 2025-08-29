package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY
import android.app.admin.DevicePolicyManager.InstallSystemUpdateCallback
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
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
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
import androidx.compose.material3.TopAppBarDefaults
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
import com.bintianqi.owndroid.ChoosePackageContract
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.NotificationUtils
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SP
import com.bintianqi.owndroid.createShortcuts
import com.bintianqi.owndroid.formatFileSize
import com.bintianqi.owndroid.humanReadableDate
import com.bintianqi.owndroid.parseDate
import com.bintianqi.owndroid.popToast
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CheckBoxItem
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
import com.bintianqi.owndroid.uriToStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.math.roundToLong

@Serializable object SystemManager

@Composable
fun SystemManagerScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    /** 1: reboot, 2: bug report, 3: org name, 4: org id, 5: enrollment specific id*/
    var dialog by remember { mutableIntStateOf(0) }
    var enrollmentSpecificId by remember {
        mutableStateOf(if (VERSION.SDK_INT >= 31 && (privilege.device || privilege.profile)) Privilege.DPM.enrollmentSpecificId else "")
    }
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
        if(VERSION.SDK_INT >= 28 && privilege.device) {
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
        if(enrollmentSpecificId != "") {
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
                        Privilege.DPM.reboot(Privilege.DAR)
                    } else {
                        context.showOperationResultToast(Privilege.DPM.requestBugreport(Privilege.DAR))
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
        var input by remember { mutableStateOf("") }
        AlertDialog(
            text = {
                val focusMgr = LocalFocusManager.current
                LaunchedEffect(Unit) {
                    if(dialog == 5 && VERSION.SDK_INT >= 31) input = Privilege.DPM.enrollmentSpecificId
                }
                Column {
                    OutlinedTextField(
                        input, { input = it },
                        Modifier.fillMaxWidth().padding(bottom = if (dialog != 3) 8.dp else 0.dp),
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
                        try {
                            if (dialog == 3 && VERSION.SDK_INT >= 24) Privilege.DPM.setOrganizationName(Privilege.DAR, input)
                            if (dialog == 4 && VERSION.SDK_INT >= 31) {
                                Privilege.DPM.setOrganizationId(input)
                                enrollmentSpecificId = Privilege.DPM.enrollmentSpecificId
                            }
                            dialog = 0
                        } catch(_: IllegalStateException) {
                            context.showOperationResultToast(false)
                        }
                    },
                    enabled = dialog != 4 || input.length in 6..64
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Serializable object SystemOptions

@Composable
fun SystemOptionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        SwitchItem(R.string.disable_cam, icon = R.drawable.no_photography_fill0,
            getState = { Privilege.DPM.getCameraDisabled(null) }, onCheckedChange = {
                Privilege.DPM.setCameraDisabled(Privilege.DAR, it)
                createShortcuts(context)
            }
        )
        SwitchItem(R.string.disable_screen_capture, icon = R.drawable.screenshot_fill0,
            getState = { Privilege.DPM.getScreenCaptureDisabled(null) },
            onCheckedChange = { Privilege.DPM.setScreenCaptureDisabled(Privilege.DAR, it) }
        )
        if(VERSION.SDK_INT >= 34 && (privilege.device || (privilege.profile && privilege.affiliated))) {
            SwitchItem(R.string.disable_status_bar, icon = R.drawable.notifications_fill0,
                getState = { Privilege.DPM.isStatusBarDisabled},
                onCheckedChange = { Privilege.DPM.setStatusBarDisabled(Privilege.DAR, it) }
            )
        }
        if(privilege.device || privilege.org) {
            if(VERSION.SDK_INT >= 30) {
                SwitchItem(R.string.auto_time, icon = R.drawable.schedule_fill0,
                    getState = { Privilege.DPM.getAutoTimeEnabled(Privilege.DAR) },
                    onCheckedChange = { Privilege.DPM.setAutoTimeEnabled(Privilege.DAR, it) }
                )
                SwitchItem(R.string.auto_timezone, icon = R.drawable.globe_fill0,
                    getState = { Privilege.DPM.getAutoTimeZoneEnabled(Privilege.DAR) },
                    onCheckedChange = { Privilege.DPM.setAutoTimeZoneEnabled(Privilege.DAR, it) }
                )
            } else {
                SwitchItem(R.string.require_auto_time, icon = R.drawable.schedule_fill0,
                    getState = { Privilege.DPM.autoTimeRequired },
                    onCheckedChange = { Privilege.DPM.setAutoTimeRequired(Privilege.DAR, it) }, padding = false)
            }
        }
        if (!privilege.work) SwitchItem(R.string.master_mute, icon = R.drawable.volume_off_fill0,
            getState = { Privilege.DPM.isMasterVolumeMuted(Privilege.DAR) }, onCheckedChange = {
                Privilege.DPM.setMasterVolumeMuted(Privilege.DAR, it)
                createShortcuts(context)
            }
        )
        if(VERSION.SDK_INT >= 26) {
            SwitchItem(R.string.backup_service, icon = R.drawable.backup_fill0,
                getState = { Privilege.DPM.isBackupServiceEnabled(Privilege.DAR) },
                onCheckedChange = { Privilege.DPM.setBackupServiceEnabled(Privilege.DAR, it) },
                onClickBlank = { dialog = 1 }
            )
        }
        if(VERSION.SDK_INT >= 24 && privilege.work) {
            SwitchItem(R.string.disable_bt_contact_share, icon = R.drawable.account_circle_fill0,
                getState = { Privilege.DPM.getBluetoothContactSharingDisabled(Privilege.DAR) },
                onCheckedChange = { Privilege.DPM.setBluetoothContactSharingDisabled(Privilege.DAR, it) }
            )
        }
        if(VERSION.SDK_INT >= 30 && privilege.device) {
            SwitchItem(R.string.common_criteria_mode , icon =R.drawable.security_fill0,
                getState = { Privilege.DPM.isCommonCriteriaModeEnabled(Privilege.DAR) },
                onCheckedChange = { Privilege.DPM.setCommonCriteriaModeEnabled(Privilege.DAR, it) },
                onClickBlank = { dialog = 2 }
            )
        }
        if(VERSION.SDK_INT >= 31 && (privilege.device || privilege.org) && Privilege.DPM.canUsbDataSignalingBeDisabled()) {
            SwitchItem(
                R.string.disable_usb_signal, icon = R.drawable.usb_fill0, getState = { !Privilege.DPM.isUsbDataSignalingEnabled },
                onCheckedChange = { Privilege.DPM.isUsbDataSignalingEnabled = !it },
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

@Serializable object Keyguard

@Composable
fun KeyguardScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    MyScaffold(R.string.keyguard, onNavigateUp) {
        if(VERSION.SDK_INT >= 23 && (privilege.device || (VERSION.SDK_INT >= 28 && privilege.profile && privilege.affiliated))) {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = { context.showOperationResultToast(Privilege.DPM.setKeyguardDisabled(Privilege.DAR, true)) },
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.disable))
                }
                Button(
                    onClick = { context.showOperationResultToast(Privilege.DPM.setKeyguardDisabled(Privilege.DAR, false)) },
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
        var flag by remember { mutableIntStateOf(0) }
        if(VERSION.SDK_INT >= 26 && privilege.work) {
            CheckBoxItem(
                R.string.evict_credential_encryption_key,
                flag and FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY != 0
            ) { flag = flag xor FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY }
            Spacer(Modifier.padding(vertical = 2.dp))
        }
        Button(
            onClick = {
                if(VERSION.SDK_INT >= 26) Privilege.DPM.lockNow(flag) else Privilege.DPM.lockNow()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lock_now))
        }
        if(VERSION.SDK_INT >= 26 && privilege.work) {
            Notes(R.string.info_evict_credential_encryption_key)
        }
    }
}

@Serializable object HardwareMonitor

@RequiresApi(24)
@Composable
fun HardwareMonitorScreen(onNavigateUp: () -> Unit) {
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
    MyScaffold(R.string.hardware_monitor, onNavigateUp) {
        Text(stringResource(R.string.refresh_interval), style = typography.titleLarge, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
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

@Serializable object ChangeTime

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun ChangeTimeScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val pagerState = rememberPagerState { 2 }
    var picker by remember { mutableIntStateOf(0) } //0:None, 1:DatePicker, 2:TimePicker
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()
    val dateInteractionSource = remember { MutableInteractionSource() }
    val timeInteractionSource = remember { MutableInteractionSource() }
    if(dateInteractionSource.collectIsPressedAsState().value) picker = 1
    if(timeInteractionSource.collectIsPressedAsState().value) picker = 2
    MyScaffold(R.string.change_time, onNavigateUp) {
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        ) {
            val coroutine = rememberCoroutineScope()
            SegmentedButton(
                selected = pagerState.targetPage == 0, shape = SegmentedButtonDefaults.itemShape(0, 2),
                onClick = {
                    coroutine.launch {
                        pagerState.animateScrollToPage(0)
                    }
                }
            ) {
                Text(stringResource(R.string.selector))
            }
            SegmentedButton(
                selected = pagerState.targetPage == 1, shape = SegmentedButtonDefaults.itemShape(1, 2),
                onClick = {
                    coroutine.launch {
                        pagerState.animateScrollToPage(1)
                    }
                }
            ) {
                Text(stringResource(R.string.manually_input))
            }
        }
        HorizontalPager(
            state = pagerState, modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) { page ->
            Column(Modifier.padding(top = 4.dp)) {
                if(page == 0) {
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    )
                    Button(
                        onClick = {
                            val timeMillis = datePickerState.selectedDateMillis!! + timePickerState.hour * 3600000 + timePickerState.minute * 60000
                            context.showOperationResultToast(Privilege.DPM.setTime(Privilege.DAR, timeMillis))
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text(stringResource(R.string.apply))
                    }
                } else {
                    var inputTime by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = inputTime,
                        label = { Text(stringResource(R.string.time_unit_ms)) },
                        onValueChange = { inputTime = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            val timeMillis = inputTime.toLong()
                            context.showOperationResultToast(Privilege.DPM.setTime(Privilege.DAR, timeMillis))
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        enabled = inputTime.toLongOrNull() != null
                    ) {
                        Text(stringResource(R.string.apply))
                    }
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

@Serializable object ChangeTimeZone

@RequiresApi(28)
@Composable
fun ChangeTimeZoneScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var inputTimezone by remember { mutableStateOf("") }
    var dialog by remember { mutableStateOf(false) }
    MyScaffold(R.string.change_timezone, onNavigateUp) {
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
                context.showOperationResultToast(Privilege.DPM.setTimeZone(Privilege.DAR, inputTimezone))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.disable_auto_time_zone_before_set)
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

@Serializable object AutoTimePolicy

@RequiresApi(36)
@Composable
fun AutoTimePolicyScreen(onNavigateUp: () -> Unit) = MyScaffold(R.string.auto_time_policy, onNavigateUp, 0.dp) {
    var policy by remember { mutableIntStateOf(Privilege.DPM.autoTimePolicy) }
    listOf(
        DevicePolicyManager.AUTO_TIME_ENABLED to R.string.enable,
        DevicePolicyManager.AUTO_TIME_DISABLED to R.string.disabled,
        DevicePolicyManager.AUTO_TIME_NOT_CONTROLLED_BY_POLICY to R.string.not_controlled_by_policy
    ).forEach {
        FullWidthRadioButtonItem(it.second, it.first == policy) {
            policy = it.first
        }
    }
    Button({
        Privilege.DPM.autoTimePolicy = policy
        policy = Privilege.DPM.autoTimePolicy
    }, Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)) {
        Text(stringResource(R.string.apply))
    }
}

@Serializable object AutoTimeZonePolicy

@RequiresApi(36)
@Composable
fun AutoTimeZonePolicyScreen(onNavigateUp: () -> Unit) = MyScaffold(R.string.auto_timezone_policy, onNavigateUp, 0.dp) {
    var policy by remember { mutableIntStateOf(Privilege.DPM.autoTimeZonePolicy) }
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
        Privilege.DPM.autoTimeZonePolicy = policy
        policy = Privilege.DPM.autoTimeZonePolicy
    }, Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)) {
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
fun ContentProtectionPolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var policy by remember { mutableIntStateOf(DevicePolicyManager.CONTENT_PROTECTION_NOT_CONTROLLED_BY_POLICY) }
    fun refresh() { policy = Privilege.DPM.getContentProtectionPolicy(Privilege.DAR) }
    LaunchedEffect(Unit) { refresh() }
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
                Privilege.DPM.setContentProtectionPolicy(Privilege.DAR, policy)
                refresh()
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
fun PermissionPolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var selectedPolicy by remember { mutableIntStateOf(Privilege.DPM.getPermissionPolicy(Privilege.DAR)) }
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
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                Privilege.DPM.setPermissionPolicy(Privilege.DAR,selectedPolicy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_permission_policy, HorizontalPadding)
    }
}

@Serializable object MtePolicy

@RequiresApi(34)
@Composable
fun MtePolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var selectedMtePolicy by remember { mutableIntStateOf(Privilege.DPM.mtePolicy) }
    MyScaffold(R.string.mte_policy, onNavigateUp, 0.dp) {
        FullWidthRadioButtonItem(R.string.decide_by_user, selectedMtePolicy == MTE_NOT_CONTROLLED_BY_POLICY) {
            selectedMtePolicy = MTE_NOT_CONTROLLED_BY_POLICY
        }
        FullWidthRadioButtonItem(R.string.enabled, selectedMtePolicy == MTE_ENABLED) { selectedMtePolicy = MTE_ENABLED }
        FullWidthRadioButtonItem(R.string.disabled, selectedMtePolicy == MTE_DISABLED) { selectedMtePolicy = MTE_DISABLED }
        Button(
            onClick = {
                try {
                    Privilege.DPM.mtePolicy = selectedMtePolicy
                    context.showOperationResultToast(true)
                } catch(_: java.lang.UnsupportedOperationException) {
                    context.popToast(R.string.unsupported)
                }
                selectedMtePolicy = Privilege.DPM.mtePolicy
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
fun NearbyStreamingPolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var appPolicy by remember { mutableIntStateOf(Privilege.DPM.nearbyAppStreamingPolicy) }
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
                Privilege.DPM.nearbyAppStreamingPolicy = appPolicy
                appPolicy = Privilege.DPM.nearbyAppStreamingPolicy
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_nearby_app_streaming_policy, HorizontalPadding)
        var notificationPolicy by remember { mutableIntStateOf(Privilege.DPM.nearbyNotificationStreamingPolicy) }
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
                Privilege.DPM.nearbyNotificationStreamingPolicy = notificationPolicy
                notificationPolicy = Privilege.DPM.nearbyNotificationStreamingPolicy
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
fun LockTaskModeScreen(onNavigateUp: () -> Unit) {
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    var tabIndex by remember { mutableIntStateOf(0) }
    tabIndex = pagerState.targetPage
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lock_task_mode)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                colors = TopAppBarDefaults.topAppBarColors(colorScheme.surfaceContainer)
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
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
                if(page == 0 || page == 1) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = HorizontalPadding)
                            .padding(bottom = 80.dp)
                    ) {
                        if(page == 0) StartLockTaskMode()
                        else LockTaskPackages()
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 80.dp)
                    ) {
                        LockTaskFeatures()
                    }
                }
            }
        }
    }
}

@RequiresApi(28)
@Composable
private fun ColumnScope.StartLockTaskMode() {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var startLockTaskApp by rememberSaveable { mutableStateOf("") }
    var startLockTaskActivity by rememberSaveable { mutableStateOf("") }
    var specifyActivity by rememberSaveable { mutableStateOf(false) }
    val choosePackage = rememberLauncherForActivityResult(ChoosePackageContract()) { result ->
        result?.let { startLockTaskApp = it }
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
                    .clickable { choosePackage.launch(null) }
                    .padding(3.dp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    )
    CheckBoxItem(R.string.specify_activity, specifyActivity) { specifyActivity = it }
    AnimatedVisibility(specifyActivity) {
        OutlinedTextField(
            value = startLockTaskActivity,
            onValueChange = { startLockTaskActivity = it },
            label = { Text("Activity") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 5.dp)
        )
    }
    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
            if(!NotificationUtils.checkPermission(context)) return@Button
            if(!Privilege.DPM.isLockTaskPermitted(startLockTaskApp)) {
                context.popToast(R.string.app_not_allowed)
                return@Button
            }
            val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
            val packageManager = context.packageManager
            val launchIntent = if(specifyActivity) Intent().setComponent(ComponentName(startLockTaskApp, startLockTaskActivity))
            else packageManager.getLaunchIntentForPackage(startLockTaskApp)
            if (launchIntent != null) {
                context.startActivity(launchIntent, options.toBundle())
            } else {
                context.showOperationResultToast(false)
            }
        },
        enabled = startLockTaskApp.isNotBlank() && (!specifyActivity || startLockTaskActivity.isNotBlank())
    ) {
        Text(stringResource(R.string.start))
    }
    Notes(R.string.info_start_lock_task_mode)
}

@RequiresApi(26)
@Composable
private fun ColumnScope.LockTaskPackages() {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val lockTaskPackages = remember { mutableStateListOf<String>() }
    var input by rememberSaveable { mutableStateOf("") }
    val choosePackage = rememberLauncherForActivityResult(ChoosePackageContract()) { result ->
        result?.let { input = it }
    }
    LaunchedEffect(Unit) { lockTaskPackages.addAll(Privilege.DPM.getLockTaskPackages(Privilege.DAR)) }
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
                    .clickable { choosePackage.launch(null) }
                    .padding(3.dp))
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
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
            Privilege.DPM.setLockTaskPackages(Privilege.DAR, lockTaskPackages.toTypedArray())
            context.showOperationResultToast(true)
        }
    ) {
        Text(stringResource(R.string.apply))
    }
    Notes(R.string.info_lock_task_packages)
}

@RequiresApi(28)
@Composable
private fun ColumnScope.LockTaskFeatures() {
    val context = LocalContext.current
    var flags by remember { mutableIntStateOf(0) }
    var custom by rememberSaveable { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    fun refresh() {
        flags = Privilege.DPM.getLockTaskFeatures(Privilege.DAR)
        custom = flags != 0
    }
    LaunchedEffect(Unit) { refresh() }
    Spacer(Modifier.padding(vertical = 5.dp))
    FullWidthRadioButtonItem(R.string.disable_all, !custom) { custom = false }
    FullWidthRadioButtonItem(R.string.custom, custom) { custom = true }
    AnimatedVisibility(custom, Modifier.padding(top = 4.dp)) {
        Column {
            listOf(
                DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO to R.string.ltf_sys_info,
                DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS to R.string.ltf_notifications,
                DevicePolicyManager.LOCK_TASK_FEATURE_HOME to R.string.ltf_home,
                DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW to R.string.ltf_overview,
                DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS to R.string.ltf_global_actions,
                DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD to R.string.ltf_keyguard
            ).let {
                if(VERSION.SDK_INT >= 30)
                    it.plus(DevicePolicyManager.LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK to R.string.ltf_block_activity_start_in_task)
                else it
            }.forEach { (id, title) ->
                FullWidthCheckBoxItem(title, flags and id != 0) { flags = flags xor id }
            }
        }
    }
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = HorizontalPadding),
        onClick = {
            try {
                Privilege.DPM.setLockTaskFeatures(Privilege.DAR, flags)
                context.showOperationResultToast(true)
            } catch (e: IllegalArgumentException) {
                errorMessage = e.message
            }
            refresh()
        }
    ) {
        Text(stringResource(R.string.apply))
    }
    ErrorDialog(errorMessage) { errorMessage = null }
}

data class CaCertInfo(
    val hash: String,
    val data: ByteArray
)

@Serializable object CaCert

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun CaCertScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    /** 0:none, 1:install, 2:info, 3:uninstall all */
    var dialog by remember { mutableIntStateOf(0) }
    var caCertByteArray by remember { mutableStateOf(byteArrayOf()) }
    val coroutine = rememberCoroutineScope()
    val getCertLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {uri ->
        if(uri != null) {
            uriToStream(context, uri) {
                caCertByteArray = it.readBytes()
            }
            dialog = 1
        }
    }
    val exportCertLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument()) { uri ->
        if(uri != null) {
            context.contentResolver.openOutputStream(uri)?.use {
                it.write(caCertByteArray)
            }
            context.showOperationResultToast(true)
        }
    }
    val caCerts = remember { mutableStateListOf<CaCertInfo>() }
    fun refresh() {
        caCerts.clear()
        coroutine.launch(Dispatchers.IO) {
            val md = MessageDigest.getInstance("SHA-256")
            Privilege.DPM.getInstalledCaCerts(Privilege.DAR).forEach { ba ->
                val hash = md.digest(ba).toHexString()
                withContext(Dispatchers.Main) { caCerts += CaCertInfo(hash, ba) }
            }
        }
    }
    LaunchedEffect(Unit) { refresh() }
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
        contentWindowInsets = WindowInsets.ime
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
                            caCertByteArray = cert.data
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
                if(caCerts.isEmpty()) Text(stringResource(R.string.no_ca_cert), Modifier.padding(top = 8.dp), colorScheme.onSurfaceVariant)
                else Spacer(Modifier.padding(vertical = 30.dp))
            }
        }
        if(dialog != 0) AlertDialog(
            text = {
                if(dialog == 3) Text(stringResource(R.string.uninstall_all_user_ca_cert))
                else {
                    var text: String
                    val sha256 = MessageDigest.getInstance("SHA-256").digest(caCertByteArray).toHexString()
                    try {
                        val cf = CertificateFactory.getInstance("X.509")
                        val cert = cf.generateCertificate(caCertByteArray.inputStream()) as X509Certificate
                        text = "Serial number\n" + cert.serialNumber.toString(16) + "\n\n" +
                                "Subject\n" + cert.subjectX500Principal.name + "\n\n" +
                                "Issuer\n" + cert.issuerX500Principal.name + "\n\n" +
                                "Issued on: " + parseDate(cert.notBefore) + "\n" +
                                "Expires on: " + parseDate(cert.notAfter) + "\n\n" +
                                "SHA-256 fingerprint" + "\n$sha256"
                    } catch(e: Exception) {
                        e.printStackTrace()
                        text = stringResource(R.string.parse_cert_failed)
                    }
                    Column(Modifier.verticalScroll(rememberScrollState())) {
                        SelectionContainer {
                            Text(text)
                        }
                        if(dialog == 2) Row(Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp), Arrangement.SpaceBetween) {
                            TextButton(
                                onClick = {
                                    Privilege.DPM.uninstallCaCert(Privilege.DAR, caCertByteArray)
                                    refresh()
                                    dialog = 0
                                },
                                modifier = Modifier.fillMaxWidth(0.49F),
                                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                            ) {
                                Text(stringResource(R.string.uninstall))
                            }
                            FilledTonalButton(
                                onClick = {
                                    exportCertLauncher.launch(sha256.substring(0..7) + ".0")
                                },
                                modifier = Modifier.fillMaxWidth(0.96F)
                            ) {
                                Text(stringResource(R.string.export))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton({
                    try {
                        if(dialog == 1) {
                            context.showOperationResultToast(Privilege.DPM.installCaCert(Privilege.DAR, caCertByteArray))
                        }
                        if(dialog == 3) {
                            Privilege.DPM.uninstallAllUserCaCerts(Privilege.DAR)
                        }
                        refresh()
                        dialog = 0
                    } catch(e: Exception) {
                        e.printStackTrace()
                        context.showOperationResultToast(false)
                    }
                }) {
                    Text(stringResource(if(dialog == 1) R.string.install else R.string.confirm))
                }
            },
            dismissButton = {
                if(dialog != 2) TextButton({ dialog = 0 }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { dialog = 0 }
        )
    }
}

@Serializable object SecurityLogging

@RequiresApi(24)
@Composable
fun SecurityLoggingScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val logFile = context.filesDir.resolve("SecurityLogs.json")
    var fileSize by remember { mutableLongStateOf(0) }
    LaunchedEffect(Unit) { fileSize = logFile.length() }
    var preRebootSecurityLogs by remember { mutableStateOf(byteArrayOf()) }
    val exportPreRebootSecurityLogs = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if(uri != null) context.contentResolver.openOutputStream(uri)?.use { outStream ->
            preRebootSecurityLogs.inputStream().copyTo(outStream)
        }
    }
    val exportSecurityLogs = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if(uri != null) context.contentResolver.openOutputStream(uri)?.use { outStream ->
            outStream.write("[".toByteArray())
            logFile.inputStream().use { it.copyTo(outStream) }
            outStream.write("]".toByteArray())
            context.showOperationResultToast(true)
        }
    }
    MyScaffold(R.string.security_logging, onNavigateUp) {
        SwitchItem(
            R.string.enable,
            getState = { Privilege.DPM.isSecurityLoggingEnabled(Privilege.DAR) },
            onCheckedChange = { Privilege.DPM.setSecurityLoggingEnabled(Privilege.DAR, it) },
            padding = false
        )
        Text(stringResource(R.string.log_file_size_is, formatFileSize(fileSize)))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    exportSecurityLogs.launch("SecurityLogs.json")
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
        Notes(R.string.info_security_log)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                val logs = Privilege.DPM.retrievePreRebootSecurityLogs(Privilege.DAR)
                if(logs == null) {
                    context.popToast(R.string.no_logs)
                    return@Button
                } else {
                    val outputStream = ByteArrayOutputStream()
                    outputStream.write("[".encodeToByteArray())
                    processSecurityLogs(logs, outputStream)
                    outputStream.write("]".encodeToByteArray())
                    preRebootSecurityLogs = outputStream.toByteArray()
                    exportPreRebootSecurityLogs.launch("PreRebootSecurityLogs.json")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pre_reboot_security_logs))
        }
        Notes(R.string.info_pre_reboot_security_log)
    }
}

@Serializable object DisableAccountManagement

@Composable
fun DisableAccountManagementScreen(onNavigateUp: () -> Unit) {
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.disable_account_management, onNavigateUp) {
        val list = remember { mutableStateListOf<String>() }
        fun refreshList() {
            list.clear()
            Privilege.DPM.accountTypesWithManagementDisabled?.forEach { list += it }
        }
        LaunchedEffect(Unit) { refreshList() }
        Column(modifier = Modifier.animateContentSize()) {
            if(list.isEmpty()) Text(stringResource(R.string.none))
            for(i in list) {
                ListItem(i) {
                    Privilege.DPM.setAccountManagementDisabled(Privilege.DAR, i, false)
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
                        Privilege.DPM.setAccountManagementDisabled(Privilege.DAR, inputText, true)
                        inputText = ""
                        refreshList()
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

@Serializable object FrpPolicy

@RequiresApi(30)
@Composable
fun FrpPolicyScreen(onNavigateUp: () -> Unit) {
    val focusMgr = LocalFocusManager.current
    var usePolicy by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(false) }
    var unsupported by remember { mutableStateOf(false) }
    val accountList = remember { mutableStateListOf<String>() }
    var inputAccount by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        var policy: FactoryResetProtectionPolicy? = null
        try {
            policy = Privilege.DPM.getFactoryResetProtectionPolicy(Privilege.DAR)
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
    MyScaffold(R.string.frp_policy, onNavigateUp) {
        if(unsupported) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primaryContainer)
            ) {
                Text(stringResource(R.string.frp_not_supported), Modifier.padding(8.dp), color = colorScheme.onPrimaryContainer)
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 8.dp)
            ) {
                Text(stringResource(R.string.use_policy), style = typography.titleLarge)
                Switch(checked = usePolicy, onCheckedChange = { usePolicy = it })
            }
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
            }
        }
        if(!unsupported) Button(
            onClick = {
                focusMgr.clearFocus()
                val policy = FactoryResetProtectionPolicy.Builder()
                    .setFactoryResetProtectionEnabled(enabled)
                    .setFactoryResetProtectionAccounts(accountList)
                    .build()
                Privilege.DPM.setFactoryResetProtectionPolicy(Privilege.DAR, policy)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_frp_policy)
    }
}

@Serializable object WipeData

@Composable
fun WipeDataScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    val focusMgr = LocalFocusManager.current
    var flag by remember { mutableIntStateOf(0) }
    var warning by remember { mutableStateOf(false) }
    var wipeDevice by remember { mutableStateOf(false) }
    var silent by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    MyScaffold(R.string.wipe_data, onNavigateUp) {
        CheckBoxItem(R.string.wipe_external_storage, flag and WIPE_EXTERNAL_STORAGE != 0) { flag = flag xor WIPE_EXTERNAL_STORAGE }
        if(VERSION.SDK_INT >= 22 && privilege.device) CheckBoxItem(
            R.string.wipe_reset_protection_data, flag and WIPE_RESET_PROTECTION_DATA != 0) { flag = flag xor WIPE_RESET_PROTECTION_DATA }
        if(VERSION.SDK_INT >= 28) CheckBoxItem(R.string.wipe_euicc, flag and WIPE_EUICC != 0) { flag = flag xor WIPE_EUICC }
        if(VERSION.SDK_INT >= 29) CheckBoxItem(R.string.wipe_silently, silent) { silent = it }
        AnimatedVisibility(!silent && VERSION.SDK_INT >= 28) {
            OutlinedTextField(
                value = reason, onValueChange = { reason = it },
                label = { Text(stringResource(R.string.reason)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp)
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
        if (VERSION.SDK_INT >= 34 && privilege.device) {
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
                            Privilege.DPM.wipeDevice(flag)
                        } else {
                            if(VERSION.SDK_INT >= 28 && reason != "") {
                                Privilege.DPM.wipeData(flag, reason)
                            } else {
                                Privilege.DPM.wipeData(flag)
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

@Serializable object SetSystemUpdatePolicy

@RequiresApi(23)
@Composable
fun SystemUpdatePolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.system_update_policy, onNavigateUp, 0.dp) {
        var selectedPolicy by remember { mutableStateOf(Privilege.DPM.systemUpdatePolicy?.policyType) }
        FullWidthRadioButtonItem(
            R.string.system_update_policy_automatic,
            selectedPolicy == TYPE_INSTALL_AUTOMATIC
        ) { selectedPolicy = TYPE_INSTALL_AUTOMATIC }
        FullWidthRadioButtonItem(
            R.string.system_update_policy_install_windowed,
            selectedPolicy == TYPE_INSTALL_WINDOWED
        ) { selectedPolicy = TYPE_INSTALL_WINDOWED }
        FullWidthRadioButtonItem(
            R.string.system_update_policy_postpone,
            selectedPolicy == TYPE_POSTPONE
        ) { selectedPolicy = TYPE_POSTPONE }
        FullWidthRadioButtonItem(R.string.none, selectedPolicy == null) { selectedPolicy = null }
        var windowedPolicyStart by remember { mutableStateOf("") }
        var windowedPolicyEnd by remember { mutableStateOf("") }
        AnimatedVisibility(selectedPolicy == 2) {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                Row(Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp), Arrangement.SpaceBetween) {
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
                        modifier = Modifier
                            .fillMaxWidth(0.96F)
                            .padding(bottom = 2.dp)
                    )
                }
                Text(stringResource(R.string.minutes_in_one_day), color = colorScheme.onSurfaceVariant, style = typography.bodyMedium)
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
                Privilege.DPM.setSystemUpdatePolicy(Privilege.DAR, policy)
                context.showOperationResultToast(true)
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        if(VERSION.SDK_INT >= 26) {
            val sysUpdateInfo = Privilege.DPM.getPendingSystemUpdate(Privilege.DAR)
            Column(Modifier.padding(HorizontalPadding)) {
                if(sysUpdateInfo != null) {
                    Text(text = stringResource(R.string.update_received_time, Date(sysUpdateInfo.receivedTime)))
                    val securityPatchStateText = when(sysUpdateInfo.securityPatchState) {
                        SystemUpdateInfo.SECURITY_PATCH_STATE_FALSE -> R.string.no
                        SystemUpdateInfo.SECURITY_PATCH_STATE_TRUE -> R.string.yes
                        else -> R.string.unknown
                    }
                    Text(text = stringResource(R.string.is_security_patch, stringResource(securityPatchStateText)))
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
fun InstallSystemUpdateScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
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
            context.popToast(errMsg)
        }
    }
    var uri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
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
        AnimatedVisibility(uri != null) {
            Button(
                onClick = {
                    val executor = Executors.newCachedThreadPool()
                    try {
                        Privilege.DPM.installSystemUpdate(Privilege.DAR, uri!!, executor, callback)
                        context.popToast(R.string.start_install_system_update)
                    } catch(e: Exception) {
                        errorMessage = e.message
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.install_system_update))
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.auto_reboot_after_install_succeed)
    }
    ErrorDialog(errorMessage) { errorMessage = null }
}
