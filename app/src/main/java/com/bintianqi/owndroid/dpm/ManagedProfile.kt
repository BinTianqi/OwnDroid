package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME
import android.app.admin.DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION
import android.app.admin.DevicePolicyManager.FLAG_MANAGED_CAN_ACCESS_PARENT
import android.app.admin.DevicePolicyManager.FLAG_PARENT_CAN_ACCESS_MANAGED
import android.app.admin.DevicePolicyManager.PERSONAL_APPS_NOT_SUSPENDED
import android.app.admin.DevicePolicyManager.PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT
import android.app.admin.DevicePolicyManager.WIPE_EUICC
import android.app.admin.DevicePolicyManager.WIPE_EXTERNAL_STORAGE
import android.app.admin.DevicePolicyManager.WIPE_SILENTLY
import android.content.*
import android.os.Binder
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Receiver
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.CopyTextButton
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.TopBar

@Composable
fun ManagedProfile(navCtrl: NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    Scaffold(
        topBar = {
            TopBar(backStackEntry, navCtrl, localNavCtrl)
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
            composable(route = "Home") { Home(localNavCtrl) }
            composable(route = "OrgOwnedWorkProfile") { OrgOwnedProfile() }
            composable(route = "CreateWorkProfile") { CreateWorkProfile() }
            composable(route = "SuspendPersonalApp") { SuspendPersonalApp() }
            composable(route = "IntentFilter") { IntentFilter() }
            composable(route = "DeleteWorkProfile") { DeleteWorkProfile() }
        }
    }
}

@Composable
private fun Home(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context, Receiver::class.java)
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Text(
            text = stringResource(R.string.work_profile),
            style = typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 15.dp)
        )
        if(VERSION.SDK_INT >= 30 && isProfileOwner(dpm) && dpm.isManagedProfile(receiver)) {
            SubPageItem(R.string.org_owned_work_profile, "", R.drawable.corporate_fare_fill0) { navCtrl.navigate("OrgOwnedWorkProfile") }
        }
        if(VERSION.SDK_INT<24 || (VERSION.SDK_INT>=24 && dpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE))) {
            SubPageItem(R.string.create_work_profile, "", R.drawable.work_fill0) { navCtrl.navigate("CreateWorkProfile") }
        }
        if(dpm.isOrgProfile(receiver)) {
            SubPageItem(R.string.suspend_personal_app, "", R.drawable.block_fill0) { navCtrl.navigate("SuspendPersonalApp") }
        }
        if(isProfileOwner(dpm) && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)))) {
            SubPageItem(R.string.intent_filter, "", R.drawable.filter_alt_fill0) { navCtrl.navigate("IntentFilter") }
        }
        if(isProfileOwner(dpm) && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)))) {
            SubPageItem(R.string.delete_work_profile, "", R.drawable.delete_forever_fill0) { navCtrl.navigate("DeleteWorkProfile") }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun CreateWorkProfile() {
    val context = LocalContext.current
    val receiver = ComponentName(context,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.create_work_profile), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        var skipEncrypt by remember { mutableStateOf(false) }
        if(VERSION.SDK_INT>=24) {
            CheckBoxItem(stringResource(R.string.skip_encryption), skipEncrypt, { skipEncrypt = it })
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                try {
                    val intent = Intent(ACTION_PROVISION_MANAGED_PROFILE)
                    if(VERSION.SDK_INT>=23) {
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,receiver)
                    }else{
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, context.packageName)
                    }
                    if(VERSION.SDK_INT>=24) { intent.putExtra(EXTRA_PROVISIONING_SKIP_ENCRYPTION,skipEncrypt) }
                    if(VERSION.SDK_INT>=33) { intent.putExtra(EXTRA_PROVISIONING_ALLOW_OFFLINE,true) }
                    createManagedProfile.launch(intent)
                }catch(e:ActivityNotFoundException) {
                    Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun OrgOwnedProfile() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.org_owned_work_profile), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.is_org_owned_profile,dpm.isOrganizationOwnedDeviceWithManagedProfile))
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

@SuppressLint("NewApi")
@Composable
private fun SuspendPersonalApp() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        SwitchItem(
            R.string.suspend_personal_app, "", null,
            { dpm.getPersonalAppsSuspendedReasons(receiver)!=PERSONAL_APPS_NOT_SUSPENDED },
            { dpm.setPersonalAppsSuspended(receiver,it) }
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
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
private fun IntentFilter() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        var action by remember { mutableStateOf("") }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.intent_filter), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
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
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_work_to_personal))
        }
        Button(
            onClick = {
                dpm.addCrossProfileIntentFilter(receiver, IntentFilter(action), FLAG_MANAGED_CAN_ACCESS_PARENT)
                Toast.makeText(context, R.string.success,Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_personal_to_work))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        Button(
            onClick = {
                dpm.clearCrossProfileIntentFilters(receiver)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_cross_profile_filters))
        }
    }
}

@Composable
private fun DeleteWorkProfile() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    var warning by remember { mutableStateOf(false) }
    var externalStorage by remember { mutableStateOf(false) }
    var euicc by remember { mutableStateOf(false) }
    var silent by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(
            text = stringResource(R.string.delete_work_profile),
            style = typography.headlineLarge,
            modifier = Modifier.padding(6.dp),color = colorScheme.error
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        CheckBoxItem(stringResource(R.string.wipe_external_storage), externalStorage, { externalStorage = it })
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
        Button(
            onClick = {
                focusMgr.clearFocus()
                warning = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
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
                Text(text = stringResource(R.string.wipe_work_profile_warning), color = colorScheme.error)
            },
            onDismissRequest = { warning = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        var flag = 0
                        if(externalStorage) { flag += WIPE_EXTERNAL_STORAGE }
                        if(euicc && VERSION.SDK_INT >= 28) { flag += WIPE_EUICC }
                        if(silent && VERSION.SDK_INT >= 29) { flag += WIPE_SILENTLY }
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
