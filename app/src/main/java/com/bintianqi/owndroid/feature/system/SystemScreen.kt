package com.bintianqi.owndroid.feature.system

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.WIPE_EUICC
import android.app.admin.DevicePolicyManager.WIPE_EXTERNAL_STORAGE
import android.app.admin.DevicePolicyManager.WIPE_RESET_PROTECTION_DATA
import android.app.admin.DevicePolicyManager.WIPE_SILENTLY
import android.content.Context
import android.os.Build.VERSION
import android.os.UserManager
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoItem
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.yesOrNo
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.delay

@Composable
fun SystemScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    // 1: reboot, 2: bug report, 3: org name, 4: org id, 5: enrollment specific id
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    MyScaffold(R.string.system, onNavigateUp, 0.dp) {
        FunctionItem(R.string.options, icon = R.drawable.tune_fill0) {
            onNavigate(Destination.SystemOptions)
        }
        FunctionItem(R.string.keyguard, icon = R.drawable.screen_lock_portrait_fill0) {
            onNavigate(Destination.Keyguard)
        }
        if (VERSION.SDK_INT >= 24 && privilege.device && !privilege.dhizuku)
            FunctionItem(R.string.hardware_monitor, icon = R.drawable.memory_fill0) {
                onNavigate(Destination.HardwareMonitor)
            }
        FunctionItem(R.string.default_input_method, icon = R.drawable.keyboard_fill0) {
            onNavigate(Destination.DefaultInputMethod)
        }
        if (VERSION.SDK_INT >= 24 && privilege.device) {
            FunctionItem(R.string.reboot, icon = R.drawable.restart_alt_fill0) { dialog = 1 }
        }
        if (VERSION.SDK_INT >= 24 && privilege.device && (VERSION.SDK_INT < 28 || privilege.affiliated)) {
            FunctionItem(R.string.bug_report, icon = R.drawable.bug_report_fill0) { dialog = 2 }
        }
        if (VERSION.SDK_INT >= 28 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.time, icon = R.drawable.schedule_fill0) {
                onNavigate(Destination.Time)
            }
        }
        if (VERSION.SDK_INT >= 35 && (privilege.device || (privilege.profile && privilege.affiliated)))
            FunctionItem(R.string.content_protection_policy, icon = R.drawable.search_fill0) {
                onNavigate(Destination.ContentProtectionPolicy)
            }
        FunctionItem(R.string.permission_policy, icon = R.drawable.key_fill0) {
            onNavigate(Destination.PermissionPolicy)
        }
        if (VERSION.SDK_INT >= 34 && privilege.device) {
            FunctionItem(R.string.mte_policy, icon = R.drawable.memory_fill0) {
                onNavigate(Destination.MtePolicy)
            }
        }
        if (VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.nearby_streaming_policy, icon = R.drawable.share_fill0) {
                onNavigate(Destination.NearbyStreamingPolicy)
            }
        }
        if (VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.lock_task_mode, icon = R.drawable.lock_fill0) {
                onNavigate(Destination.LockTaskMode)
            }
        }
        FunctionItem(R.string.ca_cert, icon = R.drawable.license_fill0) {
            onNavigate(Destination.CaCert)
        }
        if (VERSION.SDK_INT >= 26 && !privilege.dhizuku && (privilege.device || privilege.org)) {
            FunctionItem(R.string.security_logging, icon = R.drawable.description_fill0) {
                onNavigate(Destination.SecurityLogging)
            }
        }
        FunctionItem(R.string.device_info, icon = R.drawable.perm_device_information_fill0) {
            onNavigate(Destination.DeviceInfo)
        }
        if (VERSION.SDK_INT >= 24 && (privilege.profile || (VERSION.SDK_INT >= 26 && privilege.device))) {
            FunctionItem(R.string.org_name, icon = R.drawable.corporate_fare_fill0) { dialog = 3 }
        }
        if (VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.org_id, icon = R.drawable.corporate_fare_fill0) { dialog = 4 }
        }
        if (VERSION.SDK_INT >= 31) {
            FunctionItem(
                R.string.enrollment_specific_id, icon = R.drawable.id_card_fill0
            ) { dialog = 5 }
        }
        if (VERSION.SDK_INT >= 24 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.lock_screen_info, icon = R.drawable.screen_lock_portrait_fill0) {
                onNavigate(Destination.LockScreenInfo)
            }
        }
        if (VERSION.SDK_INT >= 24) {
            FunctionItem(R.string.support_messages, icon = R.drawable.chat_fill0) {
                onNavigate(Destination.SupportMessage)
            }
        }
        FunctionItem(R.string.disable_account_management, icon = R.drawable.account_circle_fill0) {
            onNavigate(Destination.DisableAccountManagement)
        }
        if (privilege.device || privilege.org) {
            FunctionItem(R.string.system_update, icon = R.drawable.system_update_fill0) {
                onNavigate(Destination.SystemUpdate)
            }
        }
        if (VERSION.SDK_INT >= 30 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.frp_policy, icon = R.drawable.device_reset_fill0) {
                onNavigate(Destination.FrpPolicy)
            }
        }
        if (vm.getDisplayDangerousFeatures() && !privilege.work) {
            FunctionItem(R.string.wipe_data, icon = R.drawable.device_reset_fill0) {
                onNavigate(Destination.WipeData)
            }
        }
    }
    if ((dialog == 1 || dialog == 2) && VERSION.SDK_INT >= 24) AlertDialog(
        onDismissRequest = { dialog = 0 },
        title = {
            Text(stringResource(if (dialog == 1) R.string.reboot else R.string.bug_report))
        },
        text = {
            Text(
                stringResource(
                    if (dialog == 1) R.string.info_reboot else R.string.confirm_bug_report
                )
            )
        },
        dismissButton = {
            TextButton(onClick = { dialog = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (dialog == 1) {
                        vm.reboot()
                    } else {
                        vm.requestBugReport()
                    }
                    dialog = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        }
    )
    if (dialog in 3..5) {
        val input by when (dialog) {
            3 -> vm.orgNameState.collectAsState()
            4 -> vm.orgIdState.collectAsState()
            else -> vm.enrollmentSpecificIdState.collectAsState()
        }
        AlertDialog(
            text = {
                LaunchedEffect(Unit) {
                    if (dialog == 5 && VERSION.SDK_INT >= 31) vm.getEnrollmentSpecificId()
                    if (dialog == 3 && VERSION.SDK_INT >= 24) vm.getOrgName()
                }
                Column {
                    OutlinedTextField(
                        input, {
                            when (dialog) {
                                3 -> vm.setOrgName(it)
                                4 -> vm.setOrgId(it)
                            }
                        },
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = if (dialog != 3) 8.dp else 0.dp),
                        readOnly = dialog == 5,
                        label = {
                            Text(
                                stringResource(
                                    when (dialog) {
                                        3 -> R.string.org_name
                                        4 -> R.string.org_id
                                        else -> R.string.enrollment_specific_id
                                    }
                                )
                            )
                        },
                        supportingText = {
                            if (dialog == 4) Text(stringResource(R.string.length_6_to_64))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        textStyle = typography.bodyLarge
                    )
                    if (dialog == 5) Text(stringResource(R.string.info_enrollment_specific_id))
                    if (dialog == 4) Text(stringResource(R.string.info_org_id))
                }
            },
            onDismissRequest = { dialog = 0 },
            dismissButton = {
                if (dialog != 5) TextButton({ dialog = 0 }) {
                    Text(
                        stringResource(R.string.cancel)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    {
                        if (dialog == 3 && VERSION.SDK_INT >= 24) vm.applyOrgName()
                        if (dialog == 4 && VERSION.SDK_INT >= 31) vm.applyOrgId()
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

@Composable
fun KeyguardScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    MyScaffold(R.string.keyguard, onNavigateUp) {
        if (privilege.device ||
            (VERSION.SDK_INT >= 28 && privilege.profile && privilege.affiliated)
        ) {
            Row(
                Modifier.fillMaxWidth(), Arrangement.SpaceBetween,
            ) {
                Button(
                    onClick = { vm.setKeyguardDisabled(true) },
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.disable))
                }
                Button(
                    { vm.setKeyguardDisabled(false) },
                    Modifier.fillMaxWidth(0.96F)
                ) {
                    Text(stringResource(R.string.enable))
                }
            }
            Notes(R.string.info_disable_keyguard)
            Spacer(Modifier.padding(vertical = 12.dp))
        }
        Text(text = stringResource(R.string.lock_now), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 2.dp))
        var evictKey by rememberSaveable { mutableStateOf(false) }
        if (VERSION.SDK_INT >= 26 && privilege.work) {
            CheckBoxItem(R.string.evict_credential_encryption_key, evictKey) { evictKey = true }
            Spacer(Modifier.height(5.dp))
            Notes(R.string.info_evict_credential_encryption_key)
        }
        Button(
            { vm.lockScreen(evictKey) },
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lock_now))
        }
    }
}

@Composable
fun DefaultInputMethodScreen(
    vm: SystemViewModel, navigateUp: () -> Unit
) {
    val imList by vm.inputMethodList.collectAsStateWithLifecycle()
    val selectedIm by vm.defaultInputMethodState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getInputMethods()
        vm.getDefaultInputMethod()
    }
    MyLazyScaffold(R.string.default_input_method, navigateUp) {
        items(imList) { (id, info) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { vm.setDefaultInputMethod(id) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selectedIm == id, { vm.setDefaultInputMethod(id) })
                Image(rememberDrawablePainter(info.icon), null, Modifier.size(40.dp))
                Column(Modifier.padding(start = 8.dp)) {
                    Text(info.label)
                    Text(id, Modifier.alpha(0.7F), style = typography.bodyMedium)
                }
            }
        }
        item {
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

@RequiresApi(35)
@Composable
fun ContentProtectionPolicyScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val policy by vm.contentProtectionPolicyState.collectAsState()
    MyScaffold(R.string.content_protection_policy, onNavigateUp, 0.dp) {
        mapOf(
            0 to R.string.not_controlled_by_policy,
            1 to R.string.disabled,
            2 to R.string.enabled
        ).forEach { (id, string) ->
            FullWidthRadioButtonItem(string, policy == id) { vm.setContentProtectionPolicy(id) }
        }
        Notes(R.string.info_content_protection_policy, HorizontalPadding)
    }
}

@Composable
fun PermissionPolicyScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val policy by vm.permissionPolicyState.collectAsState()
    MyScaffold(R.string.permission_policy, onNavigateUp, 0.dp) {
        listOf(
            0 to R.string.default_stringres,
            1 to R.string.auto_grant,
            2 to R.string.auto_deny
        ).forEach {
            FullWidthRadioButtonItem(it.second, policy == it.first) {
                vm.setPermissionPolicy(it.first)
            }
        }
        Notes(R.string.info_permission_policy, HorizontalPadding)
    }
}

@RequiresApi(34)
@Composable
fun MtePolicyScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val policy by vm.mtePolicyState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getMtePolicy()
    }
    MyScaffold(R.string.mte_policy, onNavigateUp, 0.dp) {
        listOf(
            0 to R.string.default_stringres,
            1 to R.string.enabled,
            2 to R.string.disabled
        ).forEach {
            FullWidthRadioButtonItem(it.second, policy == it.first) { vm.setMtePolicy(it.first) }
        }
        Notes(R.string.info_mte_policy, HorizontalPadding)
    }
}

@RequiresApi(31)
@Composable
fun NearbyStreamingPolicyScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val appPolicy by vm.nsAppPolicyState.collectAsState()
    val notificationPolicy by vm.nsNotificationPolicyState.collectAsState()
    MySmallTitleScaffold(R.string.nearby_streaming_policy, onNavigateUp, 0.dp) {
        Text(
            stringResource(R.string.nearby_app_streaming),
            Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp),
            style = typography.titleLarge
        )
        NearbyStreamingPolicyScreenContent(appPolicy, vm::setNsAppPolicy)
        Notes(R.string.info_nearby_app_streaming_policy, HorizontalPadding)
        Spacer(Modifier.height(20.dp))
        Text(
            stringResource(R.string.nearby_notification_streaming),
            Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp),
            style = typography.titleLarge
        )
        NearbyStreamingPolicyScreenContent(notificationPolicy, vm::setNsNotificationPolicy)
        Notes(R.string.info_nearby_notification_streaming_policy, HorizontalPadding)
        Spacer(Modifier.height(BottomPadding))
    }
}

@Composable
private fun NearbyStreamingPolicyScreenContent(policy: Int, setPolicy: (Int) -> Unit) {
    listOf(
        0 to R.string.default_str,
        1 to R.string.disabled,
        2 to R.string.enabled,
        3 to R.string.enable_if_same_account
    ).forEach {
        FullWidthRadioButtonItem(it.second, policy == it.first) { setPolicy(it.first) }
    }
}

@Composable
fun DeviceInfoScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    val info by vm.deviceInfoState.collectAsState()
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        vm.getDeviceInfo()
    }
    MyScaffold(R.string.device_info, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 34 && (privilege.device || privilege.org)) {
            InfoItem(R.string.financed_device, info.financed.yesOrNo)
        }
        if (VERSION.SDK_INT >= 33) {
            InfoItem(R.string.dpmrh, info.dpmrh ?: stringResource(R.string.none))
        }
        val encryptionStatus = when (info.storageEncryptionStatus) {
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> R.string.es_inactive
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> R.string.es_active
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> R.string.es_unsupported
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY -> R.string.es_active_default_key
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER -> R.string.es_active_per_user
            else -> R.string.unknown
        }
        InfoItem(R.string.encryption_status, encryptionStatus)
        if (VERSION.SDK_INT >= 28) {
            InfoItem(
                R.string.support_device_id_attestation, info.deviceIdAttestationSupported.yesOrNo,
                true
            ) { dialog = 1 }
        }
        if (VERSION.SDK_INT >= 30) {
            InfoItem(
                R.string.support_unique_device_attestation,
                info.uniqueDeviceAttestationSupported.yesOrNo, true
            ) { dialog = 2 }
        }
        InfoItem(R.string.activated_device_admin, info.activeAdmins.joinToString("\n"))
    }
    if (dialog != 0) AlertDialog(
        text = {
            Text(
                stringResource(
                    if (dialog == 1) R.string.info_device_id_attestation
                    else R.string.info_unique_device_attestation
                )
            )
        },
        confirmButton = {
            TextButton({ dialog = 0 }) { Text(stringResource(R.string.confirm)) }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@RequiresApi(24)
@Composable
fun SupportMessageScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val shortMsg by vm.shortSupportMessageState.collectAsState()
    val longMsg by vm.longSupportMessageState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getSupportMessages()
    }
    MyScaffold(R.string.support_messages, onNavigateUp) {
        OutlinedTextField(
            shortMsg, vm::setShortSupportMessage,
            Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            label = { Text(stringResource(R.string.short_support_msg)) },
            minLines = 2
        )
        Notes(R.string.info_short_support_message)
        Spacer(Modifier.padding(vertical = 8.dp))
        OutlinedTextField(
            longMsg, vm::setLongSupportMessage,
            Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp),
            label = { Text(stringResource(R.string.long_support_msg)) },
            minLines = 3
        )
        Notes(R.string.info_long_support_message)
        Spacer(Modifier.padding(vertical = 8.dp))
        Button(
            vm::applySupportMessages,
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
fun DisableAccountManagementScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val list by vm.mdAccountTypes.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.getMdAccountTypes() }
    MyScaffold(R.string.disable_account_management, onNavigateUp) {
        Column(Modifier.animateContentSize()) {
            for (i in list) {
                ListItem(i) {
                    vm.setMdAccountType(i, false)
                }
            }
        }
        var inputText by remember { mutableStateOf("") }
        OutlinedTextField(
            inputText, { inputText = it },
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            label = { Text(stringResource(R.string.account_type)) },
            trailingIcon = {
                IconButton(
                    {
                        vm.setMdAccountType(inputText, true)
                        inputText = ""
                    },
                    enabled = inputText != ""
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add)
                    )
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.info_disable_account_management)
    }
}

@RequiresApi(30)
@Composable
fun FrpPolicyScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val policy by vm.frpPolicyState.collectAsState()
    var inputAccount by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        vm.getFrpPolicy()
    }
    MyScaffold(R.string.frp_policy, onNavigateUp, 0.dp) {
        if (!policy.supported) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(colorScheme.primaryContainer)
            ) {
                Text(
                    stringResource(R.string.frp_not_supported), Modifier.padding(8.dp),
                    color = colorScheme.onPrimaryContainer
                )
            }
        } else {
            SwitchItem(
                R.string.use_policy, policy.usePolicy,
                { vm.setFrpPolicy(policy.copy(usePolicy = it)) }
            )
        }
        if (policy.usePolicy) {
            FullWidthCheckBoxItem(R.string.enable_frp, policy.enabled) {
                vm.setFrpPolicy(policy.copy(enabled = it))
            }
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                Text(stringResource(R.string.account_list_is))
                Column(Modifier.animateContentSize()) {
                    if (policy.accounts.isEmpty()) Text(stringResource(R.string.none))
                    for (i in policy.accounts) {
                        ListItem(i) {
                        }
                    }
                }
                OutlinedTextField(
                    inputAccount, { inputAccount = it },
                    Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.account)) },
                    trailingIcon = {
                        IconButton(
                            {
                                vm.setFrpPolicy(
                                    policy.copy(accounts = policy.accounts + inputAccount)
                                )
                                inputAccount = ""
                            },
                            enabled = inputAccount.isNotBlank()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = stringResource(R.string.add)
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done
                    )
                )
                Button(
                    vm::applyFrpPolicy,
                    Modifier
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

@Composable
fun WipeDataScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    val focusMgr = LocalFocusManager.current
    var flag by rememberSaveable { mutableIntStateOf(0) }
    var dialog by rememberSaveable { mutableIntStateOf(0) } // 0: none, 1: wipe data, 2: wipe device
    var reason by rememberSaveable { mutableStateOf("") }
    MyScaffold(R.string.wipe_data, onNavigateUp, 0.dp) {
        FullWidthCheckBoxItem(R.string.wipe_external_storage, flag and WIPE_EXTERNAL_STORAGE != 0) {
            flag = flag xor WIPE_EXTERNAL_STORAGE
        }
        if (privilege.device) FullWidthCheckBoxItem(
            R.string.wipe_reset_protection_data, flag and WIPE_RESET_PROTECTION_DATA != 0
        ) {
            flag = flag xor WIPE_RESET_PROTECTION_DATA
        }
        if (VERSION.SDK_INT >= 28) FullWidthCheckBoxItem(
            R.string.wipe_euicc,
            flag and WIPE_EUICC != 0
        ) {
            flag = flag xor WIPE_EUICC
        }
        if (VERSION.SDK_INT < 34 || !userManager.isSystemUser) {
            if (VERSION.SDK_INT >= 29) CheckBoxItem(
                R.string.wipe_silently, flag and WIPE_SILENTLY != 0
            ) {
                flag = flag xor WIPE_SILENTLY
                reason = ""
            }
            AnimatedVisibility(flag and WIPE_SILENTLY != 0 && VERSION.SDK_INT >= 28) {
                OutlinedTextField(
                    reason, { reason = it },
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    label = { Text(stringResource(R.string.reason)) }
                )
            }
            Button(
                {
                    focusMgr.clearFocus()
                    dialog = 1
                },
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.error, contentColor = colorScheme.onError
                )
            ) {
                Text("WipeData")
            }
        }
        if (VERSION.SDK_INT >= 34 && privilege.device) {
            Button(
                {
                    focusMgr.clearFocus()
                    dialog = 2
                },
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 5.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.error, contentColor = colorScheme.onError
                )
            ) {
                Text("WipeDevice")
            }
        }
    }
    if (dialog != 0) {
        AlertDialog(
            title = {
                Text(stringResource(R.string.warning), color = colorScheme.error)
            },
            text = {
                Text(
                    stringResource(
                        if (userManager.isSystemUser) R.string.wipe_data_warning
                        else R.string.info_wipe_data_in_managed_user
                    ),
                    color = colorScheme.error
                )
            },
            onDismissRequest = { dialog = 0 },
            confirmButton = {
                var timer by remember { mutableIntStateOf(5) }
                LaunchedEffect(Unit) {
                    while (timer > 0) {
                        delay(1000)
                        timer -= 1
                    }
                }
                val timerText = if (timer > 0) "(${timer}s)" else ""
                TextButton(
                    {
                        vm.wipeData(dialog == 2, flag, reason)
                    },
                    Modifier.animateContentSize(),
                    timer == 0,
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
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

@RequiresApi(24)
@Composable
fun LockScreenInfoScreen(
    vm: SystemViewModel, onNavigateUp: () -> Unit
) {
    val text by vm.lockScreenInfoState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getLockScreenInfo()
    }
    MyScaffold(R.string.lock_screen_info, onNavigateUp) {
        OutlinedTextField(
            text, vm::setLockScreenInfo,
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            label = { Text(stringResource(R.string.lock_screen_info)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Button(
            vm::applyLockScreenInfo,
            Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.info_lock_screen_info)
    }
}
