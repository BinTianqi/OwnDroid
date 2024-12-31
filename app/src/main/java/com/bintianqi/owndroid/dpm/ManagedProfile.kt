package com.bintianqi.owndroid.dpm

import android.accounts.Account
import android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION
import android.app.admin.DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
import android.app.admin.DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED
import android.app.admin.DevicePolicyManager.PERSONAL_APPS_NOT_SUSPENDED
import android.app.admin.DevicePolicyManager.PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT
import android.app.admin.DevicePolicyManager.WIPE_EUICC
import android.app.admin.DevicePolicyManager.WIPE_EXTERNAL_STORAGE
import android.content.*
import android.os.Binder
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CardItem
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.CopyTextButton
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoCard
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.yesOrNo

@Composable
fun WorkProfile(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val profileOwner = context.isProfileOwner
    MyScaffold(R.string.work_profile, 0.dp, navCtrl) {
        if(VERSION.SDK_INT >= 30 && profileOwner && dpm.isManagedProfile(receiver)) {
            FunctionItem(R.string.org_owned_work_profile, icon = R.drawable.corporate_fare_fill0) { navCtrl.navigate("OrgOwnedWorkProfile") }
        }
        if(VERSION.SDK_INT<24 || (VERSION.SDK_INT>=24 && dpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE))) {
            FunctionItem(R.string.create_work_profile, icon = R.drawable.work_fill0) { navCtrl.navigate("CreateWorkProfile") }
        }
        if(dpm.isOrgProfile(receiver)) {
            FunctionItem(R.string.suspend_personal_app, icon = R.drawable.block_fill0) { navCtrl.navigate("SuspendPersonalApp") }
        }
        if(profileOwner && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)))) {
            FunctionItem(R.string.intent_filter, icon = R.drawable.filter_alt_fill0) { navCtrl.navigate("IntentFilter") }
        }
        if(profileOwner && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)))) {
            FunctionItem(R.string.delete_work_profile, icon = R.drawable.delete_forever_fill0) { navCtrl.navigate("DeleteWorkProfile") }
        }
    }
}

@Composable
fun CreateWorkProfile(navCtrl: NavHostController) {
    val context = LocalContext.current
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    MyScaffold(R.string.create_work_profile, 8.dp, navCtrl) {
        var skipEncrypt by remember { mutableStateOf(false) }
        var offlineProvisioning by remember { mutableStateOf(true) }
        var migrateAccount by remember { mutableStateOf(false) }
        var migrateAccountName by remember { mutableStateOf("") }
        var migrateAccountType by remember { mutableStateOf("") }
        var keepAccount by remember { mutableStateOf(true) }
        if(VERSION.SDK_INT >= 22) {
            CheckBoxItem(R.string.migrate_account, migrateAccount) { migrateAccount = it }
            AnimatedVisibility(migrateAccount) {
                val fr = FocusRequester()
                Column(modifier = Modifier.padding(start = 10.dp)) {
                    OutlinedTextField(
                        value = migrateAccountName, onValueChange = { migrateAccountName = it },
                        label = { Text(stringResource(R.string.account_name)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions { fr.requestFocus() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = migrateAccountType, onValueChange = { migrateAccountType = it },
                        label = { Text(stringResource(R.string.account_type)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                        modifier = Modifier.fillMaxWidth().focusRequester(fr)
                    )
                    if(VERSION.SDK_INT >= 26) {
                        CheckBoxItem(R.string.keep_account, keepAccount) { keepAccount = it }
                    }
                }
            }
        }
        if(VERSION.SDK_INT >= 24) CheckBoxItem(R.string.skip_encryption, skipEncrypt) { skipEncrypt = it }
        if(VERSION.SDK_INT >= 33) CheckBoxItem(R.string.offline_provisioning, offlineProvisioning) { offlineProvisioning = it }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                try {
                    val intent = Intent(ACTION_PROVISION_MANAGED_PROFILE)
                    if(VERSION.SDK_INT >= 23) {
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,receiver)
                    } else {
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, context.packageName)
                    }
                    if(migrateAccount && VERSION.SDK_INT >= 22) {
                        intent.putExtra(EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE, Account(migrateAccountName, migrateAccountType))
                        if(VERSION.SDK_INT >= 26) {
                            intent.putExtra(EXTRA_PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION, keepAccount)
                        }
                    }
                    if(VERSION.SDK_INT >= 24) { intent.putExtra(EXTRA_PROVISIONING_SKIP_ENCRYPTION, skipEncrypt) }
                    if(VERSION.SDK_INT >= 33) { intent.putExtra(EXTRA_PROVISIONING_ALLOW_OFFLINE, offlineProvisioning) }
                    launcher.launch(intent)
                } catch(_: ActivityNotFoundException) {
                    Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create))
        }
    }
}

@RequiresApi(30)
@Composable
fun OrgOwnedProfile(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    MyScaffold(R.string.org_owned_work_profile, 8.dp, navCtrl, false) {
        CardItem(R.string.org_owned_work_profile, dpm.isOrganizationOwnedDeviceWithManagedProfile.yesOrNo)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(!dpm.isOrganizationOwnedDeviceWithManagedProfile) {
            SelectionContainer {
                Text(
                    text = stringResource(R.string.activate_org_profile_command, Binder.getCallingUid()/100000),
                    color = colorScheme.onTertiaryContainer
                )
            }
            CopyTextButton(R.string.copy_command, stringResource(R.string.activate_org_profile_command, Binder.getCallingUid()/100000))
        }
    }
}

@RequiresApi(30)
@Composable
fun SuspendPersonalApp(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var suspend by remember { mutableStateOf(dpm.getPersonalAppsSuspendedReasons(receiver) != PERSONAL_APPS_NOT_SUSPENDED) }
    MyScaffold(R.string.suspend_personal_app, 8.dp, navCtrl) {
        SwitchItem(R.string.suspend_personal_app, state = suspend,
            onCheckedChange = {
                dpm.setPersonalAppsSuspended(receiver,it)
                suspend = dpm.getPersonalAppsSuspendedReasons(receiver) != PERSONAL_APPS_NOT_SUSPENDED
            }, padding = false
        )
        var time by remember { mutableStateOf("") }
        time = dpm.getManagedProfileMaximumTimeOff(receiver).toString()
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.profile_max_time_off), style = typography.titleLarge)
        Text(text = stringResource(R.string.profile_max_time_out_desc))
        Text(
            text = stringResource(
                R.string.personal_app_suspended_because_timeout,
                dpm.getPersonalAppsSuspendedReasons(receiver) == PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT
            )
        )
        OutlinedTextField(
            value = time, onValueChange = { time=it }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            label = { Text(stringResource(R.string.time_unit_ms)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() })
        )
        Text(text = stringResource(R.string.cannot_less_than_72_hours))
        Button(
            onClick = {
                dpm.setManagedProfileMaximumTimeOff(receiver,time.toLong())
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_profile_maximum_time_off)
    }
}

@Composable
fun IntentFilter(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.intent_filter, 8.dp, navCtrl) {
        var action by remember { mutableStateOf("") }
        OutlinedTextField(
            value = action, onValueChange = { action = it },
            label = { Text("Action") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.addCrossProfileIntentFilter(receiver, IntentFilter(action), FLAG_PARENT_CAN_ACCESS_MANAGED)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_work_to_personal))
        }
        Button(
            onClick = {
                dpm.addCrossProfileIntentFilter(receiver, IntentFilter(action), FLAG_MANAGED_CAN_ACCESS_PARENT)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_personal_to_work))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        Button(
            onClick = {
                dpm.clearCrossProfileIntentFilters(receiver)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_cross_profile_filters))
        }
        InfoCard(R.string.info_cross_profile_intent_filter)
    }
}

@Composable
fun DeleteWorkProfile(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    var flag by remember { mutableIntStateOf(0) }
    var warning by remember { mutableStateOf(false) }
    var silent by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    MyScaffold(R.string.delete_work_profile, 8.dp, navCtrl) {
        CheckBoxItem(R.string.wipe_external_storage, flag and WIPE_EXTERNAL_STORAGE != 0) { flag = flag xor WIPE_EXTERNAL_STORAGE }
        if(VERSION.SDK_INT >= 28) CheckBoxItem(R.string.wipe_euicc, flag and WIPE_EUICC != 0) { flag = flag xor WIPE_EUICC }
        CheckBoxItem(R.string.wipe_silently, silent) { silent = it }
        AnimatedVisibility(!silent && VERSION.SDK_INT >= 28) {
            OutlinedTextField(
                value = reason, onValueChange = { reason = it },
                label = { Text(stringResource(R.string.reason)) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            )
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                silent = reason == ""
                warning = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }
    }
    if(warning) {
        LaunchedEffect(Unit) { silent = reason == "" }
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.warning), color = colorScheme.error)
            },
            text = {
                Text(text = stringResource(R.string.wipe_work_profile_warning), color = colorScheme.error)
            },
            onDismissRequest = { warning = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        if(VERSION.SDK_INT >= 28 && !silent) {
                            dpm.wipeData(flag, reason)
                        } else {
                            dpm.wipeData(flag)
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
