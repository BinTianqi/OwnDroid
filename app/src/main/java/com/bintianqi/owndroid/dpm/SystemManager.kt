package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
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
import android.os.UserManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Receiver
import com.bintianqi.owndroid.StopLockTaskModeReceiver
import com.bintianqi.owndroid.fileUriFlow
import com.bintianqi.owndroid.getFile
import com.bintianqi.owndroid.prepareForNotification
import com.bintianqi.owndroid.toText
import com.bintianqi.owndroid.toggle
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.Information
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.TopBar
import com.bintianqi.owndroid.uriToStream
import kotlinx.coroutines.launch
import java.util.Date
import java.util.TimeZone
import java.util.concurrent.Executors
import kotlin.math.pow

@Composable
fun SystemManage(navCtrl:NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    val rebootDialog = remember { mutableStateOf(false) }
    val bugReportDialog = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopBar(backStackEntry,navCtrl,localNavCtrl) {
                if(backStackEntry?.destination?.route=="Home"&&scrollState.maxValue>80) {
                    Text(
                        text = stringResource(R.string.system_manage),
                        modifier = Modifier.alpha((maxOf(scrollState.value-30,0)).toFloat()/80)
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
            composable(route = "Home") { Home(localNavCtrl, scrollState, rebootDialog, bugReportDialog) }
            composable(route = "Switches") { Switches() }
            composable(route = "Keyguard") { Keyguard() }
            composable(route = "EditTime") { EditTime() }
            composable(route = "EditTimeZone") { EditTimeZone() }
            composable(route = "PermissionPolicy") { PermissionPolicy() }
            composable(route = "MTEPolicy") { MTEPolicy() }
            composable(route = "NearbyStreamingPolicy") { NearbyStreamingPolicy() }
            composable(route = "LockTaskMode") { LockTaskMode() }
            composable(route = "CaCert") { CaCert() }
            composable(route = "SecurityLogs") { SecurityLogs() }
            composable(route = "SystemUpdatePolicy") { SysUpdatePolicy() }
            composable(route = "InstallSystemUpdate") { InstallSystemUpdate() }
            composable(route = "WipeData") { WipeData() }
            composable(route = "FRP") { FactoryResetProtection() }
        }
    }
    if(rebootDialog.value) {
        RebootDialog(rebootDialog)
    }
    if(bugReportDialog.value) {
        BugReportDialog(bugReportDialog)
    }
}

@Composable
private fun Home(navCtrl: NavHostController, scrollState: ScrollState, rebootDialog: MutableState<Boolean>, bugReportDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context, Receiver::class.java)
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    val dangerousFeatures = sharedPref.getBoolean("dangerous_features", false)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Text(
            text = stringResource(R.string.system_manage),
            style = typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 15.dp)
        )
        if(isDeviceOwner(dpm) || isProfileOwner(dpm)) {
            SubPageItem(R.string.options, "", R.drawable.tune_fill0) { navCtrl.navigate("Switches") }
        }
        SubPageItem(R.string.keyguard, "", R.drawable.screen_lock_portrait_fill0) { navCtrl.navigate("Keyguard") }
        if(VERSION.SDK_INT >= 24 && isDeviceOwner(dpm)) {
            SubPageItem(R.string.reboot, "", R.drawable.restart_alt_fill0) { rebootDialog.value = true }
        }
        if(isDeviceOwner(dpm) && ((VERSION.SDK_INT >= 28 && dpm.isAffiliatedUser) || VERSION.SDK_INT >= 24)) {
            SubPageItem(R.string.bug_report, "", R.drawable.bug_report_fill0) { bugReportDialog.value = true }
        }
        if(VERSION.SDK_INT >= 28 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.edit_time, "", R.drawable.schedule_fill0) { navCtrl.navigate("EditTime") }
            SubPageItem(R.string.edit_timezone, "", R.drawable.schedule_fill0) { navCtrl.navigate("EditTimeZone") }
        }
        if(VERSION.SDK_INT >= 23 && (isDeviceOwner(dpm) || isProfileOwner(dpm))) {
            SubPageItem(R.string.permission_policy, "", R.drawable.key_fill0) { navCtrl.navigate("PermissionPolicy") }
        }
        if(VERSION.SDK_INT >= 34 && isDeviceOwner(dpm)) {
            SubPageItem(R.string.mte_policy, "", R.drawable.memory_fill0) { navCtrl.navigate("MTEPolicy") }
        }
        if(VERSION.SDK_INT >= 31 && (isDeviceOwner(dpm) || isProfileOwner(dpm))) {
            SubPageItem(R.string.nearby_streaming_policy, "", R.drawable.share_fill0) { navCtrl.navigate("NearbyStreamingPolicy") }
        }
        if(VERSION.SDK_INT >= 28 && isDeviceOwner(dpm)) {
            SubPageItem(R.string.lock_task_mode, "", R.drawable.lock_fill0) { navCtrl.navigate("LockTaskMode") }
        }
        if(isDeviceOwner(dpm) || isProfileOwner(dpm)) {
            SubPageItem(R.string.ca_cert, "", R.drawable.license_fill0) { navCtrl.navigate("CaCert") }
        }
        if(VERSION.SDK_INT >= 26 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.security_logs, "", R.drawable.description_fill0) { navCtrl.navigate("SecurityLogs") }
        }
        if(VERSION.SDK_INT >= 23 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.system_update_policy, "", R.drawable.system_update_fill0) { navCtrl.navigate("SystemUpdatePolicy") }
        }
        if(VERSION.SDK_INT >= 29 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.install_system_update, "", R.drawable.system_update_fill0) { navCtrl.navigate("InstallSystemUpdate") }
        }
        if(VERSION.SDK_INT >= 30 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.frp_policy, "", R.drawable.device_reset_fill0) { navCtrl.navigate("FRP") }
        }
        if(dangerousFeatures && dpm.isAdminActive(receiver) && !(VERSION.SDK_INT >= 24 && isProfileOwner(dpm) && dpm.isManagedProfile(receiver))) {
            SubPageItem(R.string.wipe_data, "", R.drawable.device_reset_fill0) { navCtrl.navigate("WipeData") }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
        LaunchedEffect(Unit) { fileUriFlow.value = Uri.parse("") }
    }
}

@Composable
private fun Switches() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context, Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        if(isDeviceOwner(dpm) || isProfileOwner(dpm)) {
            SwitchItem(R.string.disable_cam,"", R.drawable.photo_camera_fill0,
                { dpm.getCameraDisabled(null) }, { dpm.setCameraDisabled(receiver,it) }
            )
        }
        if(isDeviceOwner(dpm) || isProfileOwner(dpm)) {
            SwitchItem(R.string.disable_screen_capture, stringResource(R.string.also_disable_aosp_screen_record), R.drawable.screenshot_fill0,
                { dpm.getScreenCaptureDisabled(null) }, { dpm.setScreenCaptureDisabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 34 && (isDeviceOwner(dpm) || (isProfileOwner(dpm) && dpm.isAffiliatedUser))) {
            SwitchItem(R.string.disable_status_bar, "", R.drawable.notifications_fill0,
                { dpm.isStatusBarDisabled}, { dpm.setStatusBarDisabled(receiver,it) }
            )
        }
        if(isDeviceOwner(dpm) || dpm.isOrgProfile(receiver)) {
            if(VERSION.SDK_INT >= 30) {
                SwitchItem(R.string.auto_time, "", R.drawable.schedule_fill0,
                    { dpm.getAutoTimeEnabled(receiver) }, { dpm.setAutoTimeEnabled(receiver,it) }
                )
                SwitchItem(R.string.auto_timezone, "", R.drawable.globe_fill0,
                    { dpm.getAutoTimeZoneEnabled(receiver) }, { dpm.setAutoTimeZoneEnabled(receiver,it) }
                )
            }else{
                SwitchItem(R.string.require_auto_time, "", R.drawable.schedule_fill0, { dpm.autoTimeRequired}, { dpm.setAutoTimeRequired(receiver,it) })
            }
        }
        if(isDeviceOwner(dpm) || isProfileOwner(dpm)) {
            SwitchItem(R.string.master_mute, "", R.drawable.volume_up_fill0,
                { dpm.isMasterVolumeMuted(receiver) }, { dpm.setMasterVolumeMuted(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 26 && (isDeviceOwner(dpm) || isProfileOwner(dpm))) {
            SwitchItem(R.string.backup_service, "", R.drawable.backup_fill0,
                { dpm.isBackupServiceEnabled(receiver) }, { dpm.setBackupServiceEnabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 23 && (isDeviceOwner(dpm) || isProfileOwner(dpm))) {
            SwitchItem(R.string.disable_bt_contact_share, "", R.drawable.account_circle_fill0,
                { dpm.getBluetoothContactSharingDisabled(receiver) }, { dpm.setBluetoothContactSharingDisabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 30 && isDeviceOwner(dpm)) {
            SwitchItem(R.string.common_criteria_mode, stringResource(R.string.common_criteria_mode_desc),R.drawable.security_fill0,
                { dpm.isCommonCriteriaModeEnabled(receiver) }, { dpm.setCommonCriteriaModeEnabled(receiver,it) }
            )
        }
        if(VERSION.SDK_INT >= 31 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SwitchItem(
                R.string.usb_signal, "", R.drawable.usb_fill0, { dpm.isUsbDataSignalingEnabled },
                {
                    if(dpm.canUsbDataSignalingBeDisabled()) {
                        dpm.isUsbDataSignalingEnabled = it
                    } else {
                        Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Keyguard() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.keyguard), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 23) {
            Button(
                onClick = {
                    Toast.makeText(context, if(dpm.setKeyguardDisabled(receiver,true)) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(dpm) || (VERSION.SDK_INT >= 28 && isProfileOwner(dpm) && dpm.isAffiliatedUser),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.disable))
            }
            Button(
                onClick = {
                    Toast.makeText(context, if(dpm.setKeyguardDisabled(receiver,false)) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(dpm) || (VERSION.SDK_INT >= 28 && isProfileOwner(dpm) && dpm.isAffiliatedUser),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.enable))
            }
            Spacer(Modifier.padding(vertical = 3.dp))
            Information{ Text(text = stringResource(R.string.require_no_password_to_disable)) }
            Spacer(Modifier.padding(vertical = 8.dp))
        }
        var flag by remember { mutableIntStateOf(0) }
        Button(
            onClick = { dpm.lockNow() },
            enabled = dpm.isAdminActive(receiver),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lock_now))
        }
        if(VERSION.SDK_INT >= 26) {
            CheckBoxItem(
                stringResource(R.string.evict_credential_encryptoon_key),
                flag == FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY,
                { flag = if(flag==0) {1}else{0} }
            )
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun BugReportDialog(status: MutableState<Boolean>) {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    AlertDialog(
        onDismissRequest = { status.value = false },
        title = { Text(stringResource(R.string.bug_report)) },
        text = { Text(stringResource(R.string.confirm_bug_report)) },
        dismissButton = {
            TextButton(onClick = { status.value = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val result = dpm.requestBugreport(receiver)
                    Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                    status.value = false
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@SuppressLint("NewApi")
@Composable
private fun RebootDialog(status: MutableState<Boolean>) {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    AlertDialog(
        onDismissRequest = { status.value = false },
        title = { Text(stringResource(R.string.reboot)) },
        text = { Text(stringResource(R.string.confirm_reboot)) },
        dismissButton = {
            TextButton(onClick = { status.value = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = { dpm.reboot(receiver) },
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(stringResource(R.string.reboot))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@SuppressLint("NewApi")
@Composable
private fun EditTime() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.edit_time), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        var inputTime by remember { mutableStateOf("") }
        Text(text = stringResource(R.string.from_epoch_to_target_time))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputTime,
            label = { Text(stringResource(R.string.time_unit_ms)) },
            onValueChange = { inputTime = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { dpm.setTime(receiver,inputTime.toLong()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputTime != ""
        ) {
            Text(stringResource(R.string.apply))
        }
        Button(
            onClick = { inputTime = System.currentTimeMillis().toString() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.get_current_time))
        }
    }
}
@SuppressLint("NewApi")
@Composable
private fun EditTimeZone() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    val receiver = ComponentName(context,Receiver::class.java)
    var expanded by remember { mutableStateOf(false) }
    var inputTimezone by remember { mutableStateOf("") }
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.edit_timezone), style = typography.headlineLarge, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputTimezone,
            label = { Text(stringResource(R.string.timezone_id)) },
            onValueChange = { inputTimezone = it },
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
            modifier = Modifier.width(100.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 7.dp))
        Button(onClick = { expanded = !expanded }) {
            Text(stringResource(if(expanded) R.string.hide_all_timezones else R.string.view_all_timezones))
        }
        AnimatedVisibility(expanded) {
            var ids = ""
            TimeZone.getAvailableIDs().forEach { ids += "$it\n" }
            SelectionContainer {
                Text(ids)
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Information {
            Text(stringResource(R.string.disable_auto_time_zone_before_set))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun PermissionPolicy() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        var selectedPolicy by remember { mutableIntStateOf(dpm.getPermissionPolicy(receiver)) }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permission_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(stringResource(R.string.default_stringres), selectedPolicy == PERMISSION_POLICY_PROMPT, { selectedPolicy = PERMISSION_POLICY_PROMPT })
        RadioButtonItem(stringResource(R.string.auto_grant), selectedPolicy == PERMISSION_POLICY_AUTO_GRANT, { selectedPolicy = PERMISSION_POLICY_AUTO_GRANT })
        RadioButtonItem(stringResource(R.string.auto_deny), selectedPolicy == PERMISSION_POLICY_AUTO_DENY, { selectedPolicy = PERMISSION_POLICY_AUTO_DENY })
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
    }
}

@SuppressLint("NewApi")
@Composable
private fun MTEPolicy() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.mte_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        var selectedMtePolicy by remember { mutableIntStateOf(dpm.mtePolicy) }
        RadioButtonItem(
            stringResource(R.string.decide_by_user),
            selectedMtePolicy == MTE_NOT_CONTROLLED_BY_POLICY,
            { selectedMtePolicy = MTE_NOT_CONTROLLED_BY_POLICY }
        )
        RadioButtonItem(
            stringResource(R.string.enabled),
            selectedMtePolicy == MTE_ENABLED,
            { selectedMtePolicy = MTE_ENABLED }
        )
        RadioButtonItem(
            stringResource(R.string.disabled),
            selectedMtePolicy == MTE_DISABLED,
            { selectedMtePolicy = MTE_DISABLED }
        )
        Button(
            onClick = {
                try {
                    dpm.mtePolicy = selectedMtePolicy
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }catch(e:java.lang.UnsupportedOperationException) {
                    Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
                }
                selectedMtePolicy = dpm.mtePolicy
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Information { Text(stringResource(R.string.mte_policy_desc)) }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun NearbyStreamingPolicy() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        var appPolicy by remember { mutableIntStateOf(dpm.nearbyAppStreamingPolicy) }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.nearby_app_streaming), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 3.dp))
        RadioButtonItem(
            stringResource(R.string.decide_by_user),
            appPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY,
            { appPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        )
        RadioButtonItem(
            stringResource(R.string.enabled),
            appPolicy == NEARBY_STREAMING_ENABLED,
            { appPolicy = NEARBY_STREAMING_ENABLED }
        )
        RadioButtonItem(
            stringResource(R.string.disabled),
            appPolicy == NEARBY_STREAMING_DISABLED,
            { appPolicy = NEARBY_STREAMING_DISABLED }
        )
        RadioButtonItem(
            stringResource(R.string.enable_if_secure_enough),
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
        var notificationPolicy by remember { mutableIntStateOf(dpm.nearbyNotificationStreamingPolicy) }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.nearby_notification_streaming), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 3.dp))
        RadioButtonItem(
            stringResource(R.string.decide_by_user),
            notificationPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY,
            { notificationPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY }
        )
        RadioButtonItem(
            stringResource(R.string.enabled),
            notificationPolicy == NEARBY_STREAMING_ENABLED,
            { notificationPolicy = NEARBY_STREAMING_ENABLED }
        )
        RadioButtonItem(
            stringResource(R.string.disabled),
            notificationPolicy == NEARBY_STREAMING_DISABLED,
            { notificationPolicy = NEARBY_STREAMING_DISABLED }
        )
        RadioButtonItem(
            stringResource(R.string.enable_if_secure_enough),
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
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun LockTaskMode() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    val coroutine = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        val lockTaskFeatures = remember { mutableStateListOf<Int>() }
        var custom by remember { mutableStateOf(false) }
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
            }else{
                custom = false
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.lock_task_feature), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        LaunchedEffect(Unit) { refreshFeature() }
        RadioButtonItem(stringResource(R.string.disable_all), !custom, { custom = false })
        RadioButtonItem(stringResource(R.string.custom), custom, { custom = true })
        AnimatedVisibility(custom) {
            Column {
                CheckBoxItem(
                    stringResource(R.string.ltf_sys_info),
                    LOCK_TASK_FEATURE_SYSTEM_INFO in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_SYSTEM_INFO) }
                )
                CheckBoxItem(
                    stringResource(R.string.ltf_notifications),
                    LOCK_TASK_FEATURE_NOTIFICATIONS in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_NOTIFICATIONS) }
                )
                CheckBoxItem(
                    stringResource(R.string.ltf_home),
                    LOCK_TASK_FEATURE_HOME in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_HOME) }
                )
                CheckBoxItem(
                    stringResource(R.string.ltf_overview),
                    LOCK_TASK_FEATURE_OVERVIEW in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_OVERVIEW) }
                )
                CheckBoxItem(
                    stringResource(R.string.ltf_global_actions),
                    LOCK_TASK_FEATURE_GLOBAL_ACTIONS in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_GLOBAL_ACTIONS) }
                )
                CheckBoxItem(
                    stringResource(R.string.ltf_keyguard),
                    LOCK_TASK_FEATURE_KEYGUARD in lockTaskFeatures,
                    { lockTaskFeatures.toggle(it, LOCK_TASK_FEATURE_KEYGUARD) }
                )
                if(VERSION.SDK_INT >= 30) {
                    CheckBoxItem(
                        stringResource(R.string.ltf_block_activity_start_in_task),
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
                    dpm.setLockTaskFeatures(receiver,result)
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                } catch (e: IllegalArgumentException) {
                    AlertDialog.Builder(context)
                        .setTitle("Error")
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
        var inputLockTaskPkg by remember { mutableStateOf("") }
        LaunchedEffect(Unit) { lockTaskPackages.addAll(dpm.getLockTaskPackages(receiver)) }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.lock_task_packages), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        SelectionContainer(modifier = Modifier.animateContentSize()) {
            var listText = ""
            lockTaskPackages.forEach { listText += "\n" + it }
            Text(text = stringResource(R.string.app_list_is) + if(listText == "") stringResource(R.string.none) else listText)
        }
        OutlinedTextField(
            value = inputLockTaskPkg,
            onValueChange = { inputLockTaskPkg = it },
            label = { Text(stringResource(R.string.package_name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { lockTaskPackages.add(inputLockTaskPkg) },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = { lockTaskPackages.remove(inputLockTaskPkg) },
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
        var startLockTaskApp by remember { mutableStateOf("") }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.start_lock_task_mode), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = startLockTaskApp,
            onValueChange = { startLockTaskApp = it },
            label = { Text(stringResource(R.string.package_name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                if(!dpm.getLockTaskPackages(receiver).contains(startLockTaskApp)) {
                    Toast.makeText(context, R.string.app_not_allowed, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val options = ActivityOptions.makeBasic().setLockTaskEnabled(true)
                val packageManager = context.packageManager
                val launchIntent = packageManager.getLaunchIntentForPackage(startLockTaskApp)
                if (launchIntent != null) {
                    coroutine.launch {
                        prepareForNotification(context) {
                            sendStopLockTaskNotification(context)
                            context.startActivity(launchIntent, options.toBundle())
                            Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            Text(stringResource(R.string.start))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun CaCert() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.ca_cert), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        AnimatedVisibility(uriPath != "") {
            Text(text = uriPath)
        }
        Text(
            text = if(uriPath == "") { stringResource(R.string.please_select_ca_cert) } else { stringResource(R.string.cacert_installed, exist) },
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
private fun SecurityLogs() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context, Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.security_logs), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.developing))
        SwitchItem(R.string.enable, "", null, { dpm.isSecurityLoggingEnabled(receiver) }, { dpm.setSecurityLoggingEnabled(receiver,it) })
        Button(
            onClick = {
                val log = dpm.retrieveSecurityLogs(receiver)
                if(log!=null) {
                    for(i in log) { Log.d("SecureLog",i.toString()) }
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("SecureLog", context.getString(R.string.none))
                    Toast.makeText(context, R.string.no_logs, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.security_logs))
        }
        Button(
            onClick = {
                val log = dpm.retrievePreRebootSecurityLogs(receiver)
                if(log!=null) {
                    for(i in log) { Log.d("SecureLog",i.toString()) }
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("SecureLog", context.getString(R.string.none))
                    Toast.makeText(context, R.string.no_logs, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pre_reboot_security_logs))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun FactoryResetProtection() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    val receiver = ComponentName(context,Receiver::class.java)
    var usePolicy by remember { mutableStateOf(false) }
    var enabled by remember { mutableStateOf(false) }
    var unsupported by remember { mutableStateOf(false) }
    val accountList = remember { mutableStateListOf<String>() }
    var inputAccount by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        var policy: FactoryResetProtectionPolicy? = FactoryResetProtectionPolicy.Builder().build()
        try {
            policy = dpm.getFactoryResetProtectionPolicy(receiver)
        } catch(e: UnsupportedOperationException) {
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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.frp_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 3.dp))
        Text(stringResource(R.string.factory_reset_protection_policy))
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.use_policy), style = typography.titleLarge)
            Switch(checked = usePolicy, onCheckedChange = { usePolicy = it })
        }
        AnimatedVisibility(usePolicy) {
            Column {
                CheckBoxItem(stringResource(R.string.enable_frp), enabled, { enabled = it })
                Text(stringResource(R.string.account_list_is))
                Text(
                    text = if(accountList.isEmpty()) stringResource(R.string.none) else accountList.toText(),
                    modifier = Modifier.animateContentSize()
                )
                OutlinedTextField(
                    value = inputAccount,
                    label = { Text(stringResource(R.string.account)) },
                    onValueChange = { inputAccount = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(vertical = 2.dp))
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { accountList.add(inputAccount) },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick = { accountList.remove(inputAccount) },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                }
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
        if(unsupported) {
            Information {
                Text(stringResource(R.string.frp_policy_not_supported))
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun WipeData() {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    var warning by remember { mutableStateOf(false) }
    var wipeDevice by remember { mutableStateOf(false) }
    var externalStorage by remember { mutableStateOf(false) }
    var protectionData by remember { mutableStateOf(false) }
    var euicc by remember { mutableStateOf(false) }
    var silent by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(
            text = stringResource(R.string.wipe_data),
            style = typography.headlineLarge,
            modifier = Modifier.padding(6.dp),color = colorScheme.error
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        CheckBoxItem(
            stringResource(R.string.wipe_external_storage),
            externalStorage, { externalStorage = it }
        )
        if(VERSION.SDK_INT >= 22 && isDeviceOwner(dpm)) {
            CheckBoxItem(stringResource(R.string.wipe_reset_protection_data),
                protectionData, { protectionData = it }
            )
        }
        if(VERSION.SDK_INT >= 28) { CheckBoxItem(stringResource(R.string.wipe_euicc), euicc, { euicc = it }) }
        if(VERSION.SDK_INT >= 29) { CheckBoxItem(stringResource(R.string.wipe_silently), silent, { silent = it }) }
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
        if (VERSION.SDK_INT >= 34 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
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
        Spacer(Modifier.padding(vertical = 30.dp))
    }
    if(warning) {
        LaunchedEffect(Unit) { silent = reason == "" }
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.warning), color = colorScheme.error)
            },
            text = {
                Text(text = stringResource(R.string.wipe_data_warning), color = colorScheme.error)
            },
            onDismissRequest = { warning = false },
            confirmButton = {
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
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
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
private fun SysUpdatePolicy() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        if(VERSION.SDK_INT >= 23) {
            Column {
                var selectedPolicy by remember { mutableStateOf(dpm.systemUpdatePolicy?.policyType) }
                Text(text = stringResource(R.string.system_update_policy), style = typography.headlineLarge)
                Spacer(Modifier.padding(vertical = 5.dp))
                RadioButtonItem(
                    stringResource(R.string.system_update_policy_automatic),
                    selectedPolicy == TYPE_INSTALL_AUTOMATIC, { selectedPolicy = TYPE_INSTALL_AUTOMATIC }
                )
                RadioButtonItem(
                    stringResource(R.string.system_update_policy_install_windowed),
                    selectedPolicy == TYPE_INSTALL_WINDOWED, { selectedPolicy = TYPE_INSTALL_WINDOWED }
                )
                RadioButtonItem(
                    stringResource(R.string.system_update_policy_postpone),
                    selectedPolicy == TYPE_POSTPONE, { selectedPolicy = TYPE_POSTPONE }
                )
                RadioButtonItem(stringResource(R.string.none), selectedPolicy == null, { selectedPolicy = null })
                var windowedPolicyStart by remember { mutableStateOf("") }
                var windowedPolicyEnd by remember { mutableStateOf("") }
                if(selectedPolicy == 2) {
                    Spacer(Modifier.padding(vertical = 3.dp))
                    OutlinedTextField(
                        value = windowedPolicyStart,
                        label = { Text(stringResource(R.string.start_time)) },
                        onValueChange = { windowedPolicyStart = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth(0.5F)
                    )
                    Spacer(Modifier.padding(horizontal = 3.dp))
                    OutlinedTextField(
                        value = windowedPolicyEnd,
                        onValueChange = {windowedPolicyEnd = it },
                        label = { Text(stringResource(R.string.end_time)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.padding(vertical = 3.dp))
                    Text(text = stringResource(R.string.minutes_in_one_day))
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
                    modifier = Modifier.fillMaxWidth()
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
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
fun InstallSystemUpdate() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.install_system_update), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
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
        Information {
            Text(stringResource(R.string.auto_reboot_after_install_succeed))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
private fun sendStopLockTaskNotification(context: Context) {
    val nm = context.getSystemService(ComponentActivity.NOTIFICATION_SERVICE) as NotificationManager
    if (VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel("LockTaskMode", context.getString(R.string.lock_task_mode), NotificationManager.IMPORTANCE_HIGH).apply {
            description = "Notification channel for stop lock task mode"
            setShowBadge(false)
        }
        nm.createNotificationChannel(channel)
    }
    val intent = Intent(context, StopLockTaskModeReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
    val builder = NotificationCompat.Builder(context, "LockTaskMode")
        .setContentTitle(context.getText(R.string.lock_task_mode))
        .setSmallIcon(R.drawable.lock_fill0)
        .addAction(NotificationCompat.Action.Builder(R.drawable.lock_fill0, context.getText(R.string.stop), pendingIntent).build())
        .setPriority(NotificationCompat.PRIORITY_HIGH)
    nm.notify(1, builder.build())
}
