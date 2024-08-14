package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.RemoteException
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
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
import com.bintianqi.owndroid.backToHomeStateFlow
import com.bintianqi.owndroid.ui.*
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import kotlinx.coroutines.launch

@Composable
fun DpmPermissions(navCtrl:NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopBar(backStackEntry,navCtrl,localNavCtrl) {
                if(backStackEntry?.destination?.route=="Home"&&scrollState.maxValue > 100) {
                    Text(
                        text = stringResource(R.string.permission),
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
            composable(route = "Home") { Home(localNavCtrl,scrollState) }
            composable(route = "Shizuku") { ShizukuActivate() }
            composable(route = "DeviceAdmin") { DeviceAdmin() }
            composable(route = "ProfileOwner") { ProfileOwner() }
            composable(route = "DeviceOwner") { DeviceOwner() }
            composable(route = "DeviceInfo") { DeviceInfo() }
            composable(route = "OrgID") { OrgID() }
            composable(route = "SpecificID") { SpecificID() }
            composable(route = "OrgName") { OrgName() }
            composable(route = "DisableAccountManagement") { DisableAccountManagement() }
            composable(route = "LockScreenInfo") { LockScreenInfo() }
            composable(route = "SupportMsg") { SupportMsg() }
            composable(route = "TransformOwnership") { TransformOwnership() }
        }
    }
}

@Composable
private fun Home(localNavCtrl:NavHostController,listScrollState:ScrollState) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val deviceAdmin = context.isDeviceAdmin
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    Column(modifier = Modifier.fillMaxSize().verticalScroll(listScrollState)) {
        Text(
            text = stringResource(R.string.permission),
            style = typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 16.dp)
        )
        if(!dpm.isDeviceOwnerApp(context.packageName)) {
            SwitchItem(
                R.string.dhizuku, "", null,
                { sharedPref.getBoolean("dhizuku", false) },
                {
                    toggleDhizukuMode(it, context)
                }
            )
        }
        SubPageItem(
            R.string.device_admin, stringResource(if(deviceAdmin) R.string.activated else R.string.deactivated),
            operation = { localNavCtrl.navigate("DeviceAdmin") }
        )
        if(!deviceOwner) {
            SubPageItem(
                R.string.profile_owner, stringResource(if(profileOwner) R.string.activated else R.string.deactivated),
                operation = { localNavCtrl.navigate("ProfileOwner") }
            )
        }
        if(!profileOwner) {
            SubPageItem(
                R.string.device_owner, stringResource(if(deviceOwner) R.string.activated else R.string.deactivated),
                operation = { localNavCtrl.navigate("DeviceOwner") }
            )
        }
        SubPageItem(R.string.shizuku,"") { localNavCtrl.navigate("Shizuku") }
        SubPageItem(R.string.device_info, "", R.drawable.perm_device_information_fill0) { localNavCtrl.navigate("DeviceInfo") }
        if((VERSION.SDK_INT >= 26 && deviceOwner) || (VERSION.SDK_INT>=24 && profileOwner)) {
            SubPageItem(R.string.org_name, "", R.drawable.corporate_fare_fill0) { localNavCtrl.navigate("OrgName") }
        }
        if(VERSION.SDK_INT >= 31 && (profileOwner || deviceOwner)) {
            SubPageItem(R.string.org_id, "", R.drawable.corporate_fare_fill0) { localNavCtrl.navigate("OrgID") }
            SubPageItem(R.string.enrollment_specific_id, "", R.drawable.id_card_fill0) { localNavCtrl.navigate("SpecificID") }
        }
        if(deviceOwner || profileOwner) {
            SubPageItem(R.string.disable_account_management, "", R.drawable.account_circle_fill0) { localNavCtrl.navigate("DisableAccountManagement") }
        }
        if(VERSION.SDK_INT >= 24 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.device_owner_lock_screen_info, "", R.drawable.screen_lock_portrait_fill0) { localNavCtrl.navigate("LockScreenInfo") }
        }
        if(VERSION.SDK_INT >= 24 && deviceAdmin) {
            SubPageItem(R.string.support_msg, "", R.drawable.chat_fill0) { localNavCtrl.navigate("SupportMsg") }
        }
        if(VERSION.SDK_INT >= 28 && (deviceOwner || profileOwner)) {
            SubPageItem(R.string.transfer_ownership, "", R.drawable.admin_panel_settings_fill0) { localNavCtrl.navigate("TransformOwnership") }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

private fun toggleDhizukuMode(status: Boolean, context: Context) {
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    if(!status) {
        sharedPref.edit().putBoolean("dhizuku", false).apply()
        backToHomeStateFlow.value = true
        return
    }
    if(!Dhizuku.init(context)) {
        dhizukuErrorStatus.value = 1
        return
    }
    if(Dhizuku.isPermissionGranted()) {
        sharedPref.edit().putBoolean("dhizuku", true).apply()
        backToHomeStateFlow.value = true
    } else {
        Dhizuku.requestPermission(object: DhizukuRequestPermissionListener() {
            @Throws(RemoteException::class)
            override fun onRequestPermission(grantResult: Int) {
                if(grantResult == PackageManager.PERMISSION_GRANTED) {
                    sharedPref.edit().putBoolean("dhizuku", true).apply()
                    context.toggleInstallAppActivity()
                    backToHomeStateFlow.value = true
                } else {
                    dhizukuErrorStatus.value = 2
                }
            }
        })
    }
}

@SuppressLint("NewApi")
@Composable
private fun LockScreenInfo() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var infoText by remember { mutableStateOf(dpm.deviceOwnerLockScreenInfo?.toString() ?: "") }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Text(text = stringResource(R.string.device_owner_lock_screen_info), style = typography.headlineLarge)
        OutlinedTextField(
            value = infoText,
            label = { Text(stringResource(R.string.device_owner_lock_screen_info)) },
            onValueChange = { infoText = it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.setDeviceOwnerLockScreenInfo(receiver,infoText)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.setDeviceOwnerLockScreenInfo(receiver,null)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.reset))
        }
    }
}

@Composable
private fun DeviceAdmin() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var deactivateDialog by remember { mutableStateOf(false) }
    val deviceAdmin = context.isDeviceAdmin
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.device_admin), style = typography.headlineLarge)
        Text(text = stringResource(if(context.isDeviceAdmin) R.string.activated else R.string.deactivated), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        AnimatedVisibility(deviceAdmin) {
            Button(
                onClick = { deactivateDialog = true },
                enabled = !context.isProfileOwner && !context.isDeviceOwner,
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError)
            ) {
                Text(stringResource(R.string.deactivate))
            }
        }
        AnimatedVisibility(!deviceAdmin) {
            Column {
                Button(onClick = { activateDeviceAdmin(context, receiver) }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.activate_jump))
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                SelectionContainer {
                    Text(text = stringResource(R.string.activate_device_admin_command))
                }
                CopyTextButton(R.string.copy_command, stringResource(R.string.activate_device_admin_command))
            }
        }
    }
    if(deactivateDialog) {
        AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            onDismissRequest = { deactivateDialog = false },
            dismissButton = {
                TextButton(
                    onClick = { deactivateDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dpm.removeActiveAdmin(receiver)
                        deactivateDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Composable
private fun ProfileOwner() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var deactivateDialog by remember { mutableStateOf(false) }
    val profileOwner = context.isProfileOwner
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.profile_owner), style = typography.headlineLarge)
        Text(stringResource(if(profileOwner) R.string.activated else R.string.deactivated), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 24 && profileOwner) {
            Button(
                onClick = { deactivateDialog = true },
                enabled = !dpm.isManagedProfile(receiver),
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError)
            ) {
                Text(stringResource(R.string.deactivate))
            }
        }
    }
    if(deactivateDialog && VERSION.SDK_INT >= 24) {
        AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            onDismissRequest = { deactivateDialog = false },
            dismissButton = {
                TextButton(
                    onClick = { deactivateDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        dpm.clearProfileOwner(receiver)
                        deactivateDialog = false
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Composable
private fun DeviceOwner() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var deactivateDialog by remember { mutableStateOf(false) }
    var resetPolicy by remember { mutableStateOf(true) }
    val deviceOwner = context.isDeviceOwner
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.device_owner), style = typography.headlineLarge)
        Text(text = stringResource(if(deviceOwner) R.string.activated else R.string.deactivated), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        AnimatedVisibility(deviceOwner) {
            Button(
                onClick = { deactivateDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError)
            ) {
                Text(text = stringResource(R.string.deactivate))
            }
        }
        AnimatedVisibility(!deviceOwner) {
            Column {
                SelectionContainer{
                    Text(text = stringResource(R.string.activate_device_owner_command))
                }
                CopyTextButton(R.string.copy_command, stringResource(R.string.activate_device_owner_command))
            }
        }
    }
    if(deactivateDialog) {
        val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
        val coroutine = rememberCoroutineScope()
        AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            text = {
                Column {
                    if(sharedPref.getBoolean("dhizuku", false)) Text(stringResource(R.string.dhizuku_will_be_deactivated))
                    Spacer(Modifier.padding(vertical = 4.dp))
                    CheckBoxItem(text = R.string.reset_device_policy, checked = resetPolicy, operation = { resetPolicy = it })
                }
            },
            onDismissRequest = { deactivateDialog = false },
            dismissButton = {
                TextButton(
                    onClick = { deactivateDialog = false }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutine.launch {
                            if(resetPolicy) context.resetDevicePolicy()
                            dpm.clearDeviceOwnerApp(context.dpcPackageName)
                            if(sharedPref.getBoolean("dhizuku", false)) {
                                if (!Dhizuku.init(context)) {
                                    sharedPref.edit().putBoolean("dhizuku", false).apply()
                                    backToHomeStateFlow.value = true
                                }
                            }
                            deactivateDialog = false
                        }
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Composable
fun DeviceInfo() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.device_info), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT>=34 && (context.isDeviceOwner || dpm.isOrgProfile(receiver))) {
            val financed = dpm.isDeviceFinanced
            Text(stringResource(R.string.is_device_financed, financed))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        if(VERSION.SDK_INT >= 33) {
            val dpmRole = dpm.devicePolicyManagementRoleHolderPackage
            Text(stringResource(R.string.dpmrh, if(dpmRole == null) stringResource(R.string.none) else ""))
            if(dpmRole!=null) {
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                    Text(text = dpmRole)
                }
            }
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        val encryptionStatus = mutableMapOf(
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE to stringResource(R.string.es_inactive),
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE to stringResource(R.string.es_active),
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED to stringResource(R.string.es_unsupported)
        )
        if(VERSION.SDK_INT >= 23) { encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY] = stringResource(R.string.es_active_default_key) }
        if(VERSION.SDK_INT >= 24) { encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER] = stringResource(R.string.es_active_per_user) }
        Text(stringResource(R.string.encrypt_status_is)+encryptionStatus[dpm.storageEncryptionStatus])
        Spacer(Modifier.padding(vertical = 2.dp))
        if(VERSION.SDK_INT >= 28) {
            Text(stringResource(R.string.support_device_id_attestation) + dpm.isDeviceIdAttestationSupported)
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        if (VERSION.SDK_INT >= 30) {
            Text(stringResource(R.string.support_unique_device_attestation) + dpm.isUniqueDeviceAttestationSupported)
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        val adminList = dpm.activeAdmins
        if(adminList!=null) {
            var adminListText = ""
            Text(text = stringResource(R.string.activated_device_admin, adminList.size))
            var count = adminList.size
            for(each in adminList) {
                count -= 1
                adminListText += "${each.packageName}/${each.className}"
                if(count>0) {adminListText += "\n"}
            }
            SelectionContainer(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).horizontalScroll(rememberScrollState())) {
                Text(text = adminListText)
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun OrgID() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        var orgId by remember { mutableStateOf("") }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.org_id), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = orgId, onValueChange = {orgId=it},
            label = { Text(stringResource(R.string.org_id)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 2.dp))
        AnimatedVisibility(orgId.length !in 6..64) {
            Text(text = stringResource(R.string.length_6_to_64))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.setOrganizationId(orgId)
                Toast.makeText(context, R.string.success,Toast.LENGTH_SHORT).show()
            },
            enabled = orgId.length in 6..64,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Information{ Text(text = stringResource(R.string.get_specific_id_after_set_org_id)) }
    }
}

@SuppressLint("NewApi")
@Composable
private fun SpecificID() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        val specificId = dpm.enrollmentSpecificId
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.enrollment_specific_id), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(specificId != "") {
            SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())) { Text(specificId) }
            CopyTextButton(R.string.copy, specificId)
        }else{
            Text(stringResource(R.string.require_set_org_id))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun OrgName() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        var orgName by remember { mutableStateOf(try{dpm.getOrganizationName(receiver).toString() }catch(e:SecurityException) {""}) }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.org_name), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = orgName, onValueChange = { orgName = it }, modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
            label = { Text(stringResource(R.string.org_name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.setOrganizationName(receiver,orgName)
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
private fun SupportMsg() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var shortMsg by remember { mutableStateOf(dpm.getShortSupportMessage(receiver)?.toString() ?: "") }
    var longMsg by remember { mutableStateOf(dpm.getLongSupportMessage(receiver)?.toString() ?: "") }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.support_msg), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = shortMsg,
            label = { Text(stringResource(R.string.short_support_msg)) },
            onValueChange = { shortMsg = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 2.dp))
        OutlinedTextField(
            value = longMsg,
            label = { Text(stringResource(R.string.long_support_msg)) },
            onValueChange = { longMsg = it },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.setShortSupportMessage(receiver, shortMsg)
                dpm.setLongSupportMessage(receiver, longMsg)
                shortMsg = dpm.getShortSupportMessage(receiver)?.toString() ?: ""
                longMsg = dpm.getLongSupportMessage(receiver)?.toString() ?: ""
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 1.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.setShortSupportMessage(receiver, null)
                dpm.setLongSupportMessage(receiver, null)
                shortMsg = dpm.getShortSupportMessage(receiver)?.toString() ?: ""
                longMsg = dpm.getLongSupportMessage(receiver)?.toString() ?: ""
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.reset))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Information{ Text(text = stringResource(R.string.support_msg_desc)) }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun DisableAccountManagement() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.disable_account_management), style = typography.headlineLarge)
        Text(stringResource(R.string.unknown_effect))
        var accountList by remember{ mutableStateOf("") }
        val refreshList = {
            val noManageAccount = dpm.accountTypesWithManagementDisabled
            accountList = ""
            if (noManageAccount != null) {
                var count = noManageAccount.size
                for(each in noManageAccount) { count -= 1; accountList += each; if(count>0) { accountList += "\n" } }
            }
        }
        var inited by remember { mutableStateOf(false) }
        if(!inited) { refreshList(); inited=true }
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = if(accountList=="") stringResource(R.string.none) else accountList)
        var inputText by remember{ mutableStateOf("") }
        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text(stringResource(R.string.account_types_are)) },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Button(
            onClick={
                dpm.setAccountManagementDisabled(receiver, inputText, true)
                refreshList()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add))
        }
        Button(
            onClick={
                dpm.setAccountManagementDisabled(receiver, inputText, false)
                refreshList()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.remove))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun TransformOwnership() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val focusRequester = FocusRequester()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        var pkg by remember { mutableStateOf("") }
        var cls by remember { mutableStateOf("") }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.transfer_ownership), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.transfer_ownership_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = pkg, onValueChange = { pkg = it }, label = { Text(stringResource(R.string.target_package_name)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusRequester.requestFocus() })
        )
        Spacer(Modifier.padding(vertical = 2.dp))
        OutlinedTextField(
            value = cls, onValueChange = {cls = it }, label = { Text(stringResource(R.string.target_class_name)) },
            modifier = Modifier.focusRequester(focusRequester).fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                try {
                    dpm.transferOwnership(receiver,ComponentName(pkg, cls),null)
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }catch(e:IllegalArgumentException) {
                    Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.transfer))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

private fun activateDeviceAdmin(inputContext:Context,inputComponent:ComponentName) {
    try {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, inputComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, inputContext.getString(R.string.activate_device_admin_here))
        addDeviceAdmin.launch(intent)
    }catch(e:ActivityNotFoundException) {
        Toast.makeText(inputContext, R.string.unsupported, Toast.LENGTH_SHORT).show()
    }
}
