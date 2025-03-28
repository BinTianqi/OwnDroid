package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build.VERSION
import android.os.Bundle
import android.os.IBinder
import android.os.RemoteException
import android.os.UserManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.os.bundleOf
import com.bintianqi.owndroid.ChoosePackageContract
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SharedPrefs
import com.bintianqi.owndroid.backToHomeStateFlow
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.*
import com.bintianqi.owndroid.writeClipBoard
import com.bintianqi.owndroid.yesOrNo
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import rikka.shizuku.Shizuku
import rikka.sui.Sui

@Serializable object Permissions

@SuppressLint("NewApi")
@Composable
fun PermissionsScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit, onNavigateToShizuku: (Bundle) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceAdmin = context.isDeviceAdmin
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    var dialog by remember { mutableIntStateOf(0) }
    var bindingShizuku by remember { mutableStateOf(false) }
    val enrollmentSpecificId = if(VERSION.SDK_INT >= 31 && (deviceOwner || profileOwner)) dpm.enrollmentSpecificId else ""
    MyScaffold(R.string.permissions, onNavigateUp, 0.dp) {
        if(!dpm.isDeviceOwnerApp(context.packageName)) {
            SwitchItem(
                R.string.dhizuku,
                getState = { SharedPrefs(context).dhizuku },
                onCheckedChange = { toggleDhizukuMode(it, context) },
                onClickBlank = { dialog = 4 }
            )
        }
        FunctionItem(
            R.string.device_admin, stringResource(if(deviceAdmin) R.string.activated else R.string.deactivated),
            operation = { onNavigate(DeviceAdmin) }
        )
        if(profileOwner || !userManager.isSystemUser) {
            FunctionItem(
                R.string.profile_owner, stringResource(if(profileOwner) R.string.activated else R.string.deactivated),
                operation = { onNavigate(ProfileOwner) }
            )
        }
        if(!profileOwner && userManager.isSystemUser) {
            FunctionItem(
                R.string.device_owner, stringResource(if(deviceOwner) R.string.activated else R.string.deactivated),
                operation = { onNavigate(DeviceOwner) }
            )
        }
        FunctionItem(R.string.shizuku) {
            try {
                if(Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                    bindingShizuku = true
                    fun onBind(binder: IBinder) {
                        bindingShizuku = false
                        onNavigateToShizuku(bundleOf("binder" to binder))
                    }
                    try {
                        controlShizukuService(context, ::onBind, { bindingShizuku = false }, true)
                    } catch(e: Exception) {
                        e.printStackTrace()
                        bindingShizuku = false
                    }
                } else if(Shizuku.shouldShowRequestPermissionRationale()) {
                    Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                } else {
                    Sui.init(context.packageName)
                    fun requestPermissionResultListener(requestCode: Int, grantResult: Int) {
                        if(grantResult != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(context, R.string.permission_denied, Toast.LENGTH_SHORT).show()
                        }
                        Shizuku.removeRequestPermissionResultListener(::requestPermissionResultListener)
                    }
                    Shizuku.addRequestPermissionResultListener(::requestPermissionResultListener)
                    Shizuku.requestPermission(0)
                }
            } catch(_: IllegalStateException) {
                Toast.makeText(context, R.string.shizuku_not_started, Toast.LENGTH_SHORT).show()
            }
        }
        if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner))
            FunctionItem(R.string.delegated_admins) { onNavigate(DelegatedAdmins) }
        FunctionItem(R.string.device_info, icon = R.drawable.perm_device_information_fill0) { onNavigate(DeviceInfo) }
        if(VERSION.SDK_INT >= 24 && (profileOwner || (VERSION.SDK_INT >= 26 && deviceOwner))) {
            FunctionItem(R.string.org_name, icon = R.drawable.corporate_fare_fill0) { dialog = 2 }
        }
        if(VERSION.SDK_INT >= 31 && (profileOwner || deviceOwner)) {
            FunctionItem(R.string.org_id, icon = R.drawable.corporate_fare_fill0) { dialog = 3 }
        }
        if(enrollmentSpecificId != "") {
            FunctionItem(R.string.enrollment_specific_id, icon = R.drawable.id_card_fill0) { dialog = 1 }
        }
        if(VERSION.SDK_INT >= 24 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.lock_screen_info, icon = R.drawable.screen_lock_portrait_fill0) { onNavigate(LockScreenInfo) }
        }
        if(VERSION.SDK_INT >= 24 && deviceAdmin) {
            FunctionItem(R.string.support_messages, icon = R.drawable.chat_fill0) { onNavigate(SupportMessage) }
        }
        if(VERSION.SDK_INT >= 28 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.transfer_ownership, icon = R.drawable.admin_panel_settings_fill0) { onNavigate(TransferOwnership) }
        }
    }
    if(bindingShizuku) {
        Dialog(onDismissRequest = { bindingShizuku = false }) {
            CircularProgressIndicator()
        }
    }
    if(dialog != 0) {
        var input by remember { mutableStateOf("") }
        AlertDialog(
            title = {
                Text(stringResource(
                    when(dialog){
                        1 -> R.string.enrollment_specific_id
                        2 -> R.string.org_name
                        3 -> R.string.org_id
                        4 -> R.string.dhizuku
                        else -> R.string.permissions
                    }
                ))
            },
            text = {
                val focusMgr = LocalFocusManager.current
                LaunchedEffect(Unit) {
                    if(dialog == 1) input = dpm.enrollmentSpecificId
                }
                Column {
                    if(dialog != 4) OutlinedTextField(
                        value = input,
                        onValueChange = { input = it }, readOnly = dialog == 1,
                        label = {
                            Text(stringResource(
                                when(dialog){
                                    1 -> R.string.enrollment_specific_id
                                    2 -> R.string.org_name
                                    3 -> R.string.org_id
                                    else -> R.string.permissions
                                }
                            ))
                        },
                        trailingIcon = {
                            if(dialog == 1) IconButton(onClick = { writeClipBoard(context, input) }) {
                                Icon(painter = painterResource(R.drawable.content_copy_fill0), contentDescription = stringResource(R.string.copy))
                            }
                        },
                        supportingText = {
                            if(dialog == 3) Text(stringResource(R.string.length_6_to_64))
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                        textStyle = typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().padding(bottom = if(dialog == 2) 0.dp else 10.dp)
                    )
                    if(dialog == 1) Text(stringResource(R.string.info_enrollment_specific_id))
                    if(dialog == 3) Text(stringResource(R.string.info_org_id))
                    if(dialog == 4) Text(stringResource(R.string.info_dhizuku))
                }
            },
            onDismissRequest = { dialog = 0 },
            dismissButton = {
                if(dialog != 4) TextButton(
                    onClick = { dialog = 0 }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        try {
                            if(dialog == 2) dpm.setOrganizationName(receiver, input)
                            if(dialog == 3) dpm.setOrganizationId(input)
                            dialog = 0
                        } catch(_: IllegalStateException) {
                            Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = (dialog == 3 && input.length in 6..64) || dialog != 3
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

private fun toggleDhizukuMode(status: Boolean, context: Context) {
    val sp = SharedPrefs(context)
    if(!status) {
        sp.dhizuku = false
        backToHomeStateFlow.value = true
        return
    }
    if(!Dhizuku.init(context)) {
        dhizukuErrorStatus.value = 1
        return
    }
    if(dhizukuPermissionGranted()) {
        sp.dhizuku = true
        Dhizuku.init(context)
        backToHomeStateFlow.value = true
    } else {
        Dhizuku.requestPermission(object: DhizukuRequestPermissionListener() {
            @Throws(RemoteException::class)
            override fun onRequestPermission(grantResult: Int) {
                if(grantResult == PackageManager.PERMISSION_GRANTED) {
                    sp.dhizuku = true
                    Dhizuku.init(context)
                    backToHomeStateFlow.value = true
                } else {
                    dhizukuErrorStatus.value = 2
                }
            }
        })
    }
}

@Serializable object LockScreenInfo

@RequiresApi(24)
@Composable
fun LockScreenInfoScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var infoText by remember { mutableStateOf(dpm.deviceOwnerLockScreenInfo?.toString() ?: "") }
    MyScaffold(R.string.lock_screen_info, onNavigateUp) {
        OutlinedTextField(
            value = infoText,
            label = { Text(stringResource(R.string.lock_screen_info)) },
            onValueChange = { infoText = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { focusMgr.clearFocus() },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.setDeviceOwnerLockScreenInfo(receiver,infoText)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.setDeviceOwnerLockScreenInfo(receiver, null)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.reset))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.info_lock_screen_info)
    }
}

@Serializable object DeviceAdmin

@Composable
fun DeviceAdminScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var deactivateDialog by remember { mutableStateOf(false) }
    val deviceAdmin = context.isDeviceAdmin
    MyScaffold(R.string.device_admin, onNavigateUp) {
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

@Serializable object ProfileOwner

@Composable
fun ProfileOwnerScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var deactivateDialog by remember { mutableStateOf(false) }
    val profileOwner = context.isProfileOwner
    MyScaffold(R.string.profile_owner, onNavigateUp) {
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
        if(!profileOwner) {
            val command = context.getString(R.string.activate_profile_owner_command, (Binder.getCallingUid() / 100000).toString())
            SelectionContainer {
                Text(command)
            }
            CopyTextButton(R.string.copy_command, command)
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
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Serializable object DeviceOwner

@Composable
fun DeviceOwnerScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var deactivateDialog by remember { mutableStateOf(false) }
    val deviceOwner = context.isDeviceOwner
    MyScaffold(R.string.device_owner, onNavigateUp) {
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
        val sp = SharedPrefs(context)
        var resetPolicy by remember { mutableStateOf(false) }
        val coroutine = rememberCoroutineScope()
        AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            text = {
                Column {
                    if(sp.dhizuku) Text(stringResource(R.string.dhizuku_will_be_deactivated))
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
                            if(sp.dhizuku) {
                                if (!Dhizuku.init(context)) {
                                    sp.dhizuku = false
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

@Keep
@Suppress("InlinedApi")
enum class DelegatedScope(val id: String, @StringRes val string: Int, val requiresApi: Int = 0) {
    AppRestrictions(DevicePolicyManager.DELEGATION_APP_RESTRICTIONS, R.string.manage_application_restrictions),
    BlockUninstall(DevicePolicyManager.DELEGATION_BLOCK_UNINSTALL, R.string.block_uninstall),
    CertInstall(DevicePolicyManager.DELEGATION_CERT_INSTALL, R.string.manage_certificates),
    CertSelection(DevicePolicyManager.DELEGATION_CERT_SELECTION, R.string.select_keychain_certificates, 29),
    EnableSystemApp(DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP, R.string.enable_system_app),
    InstallExistingPackage(DevicePolicyManager.DELEGATION_INSTALL_EXISTING_PACKAGE, R.string.install_existing_packages, 28),
    KeepUninstalledPackages(DevicePolicyManager.DELEGATION_KEEP_UNINSTALLED_PACKAGES, R.string.manage_uninstalled_packages, 28),
    NetworkLogging(DevicePolicyManager.DELEGATION_NETWORK_LOGGING, R.string.network_logging, 29),
    PackageAccess(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS, R.string.change_package_state),
    PermissionGrant(DevicePolicyManager.DELEGATION_PERMISSION_GRANT, R.string.grant_permissions),
    SecurityLogging(DevicePolicyManager.DELEGATION_SECURITY_LOGGING, R.string.security_logging, 31)
}

@Serializable object DelegatedAdmins

@RequiresApi(26)
@Composable
fun DelegatedAdminsScreen(onNavigateUp: () -> Unit, onNavigate: (AddDelegatedAdmin) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val packages = remember { mutableStateMapOf<String, MutableList<DelegatedScope>>() }
    fun refresh() {
        val list = mutableMapOf<String, MutableList<DelegatedScope>>()
        DelegatedScope.entries.forEach { ds ->
            if(VERSION.SDK_INT >= ds.requiresApi) {
                dpm.getDelegatePackages(receiver, ds.id)?.forEach { pkg ->
                    if(list[pkg] != null) {
                        list[pkg]!!.add(ds)
                    } else {
                        list[pkg] = mutableListOf(ds)
                    }
                }
            }
        }
        packages.clear()
        packages.putAll(list)
    }
    LaunchedEffect(Unit) { refresh() }
    MyScaffold(R.string.delegated_admins, onNavigateUp, 0.dp) {
        packages.forEach { (pkg, scopes) ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp).padding(start = 14.dp, end = 8.dp),
                Arrangement.SpaceBetween
            ) {
                Column {
                    Text(pkg, style = typography.titleMedium)
                    Text(
                        scopes.size.toString() + " " + stringResource(R.string.delegated_scope),
                        color = colorScheme.onSurfaceVariant, style = typography.bodyMedium
                    )
                }
                IconButton({ onNavigate(AddDelegatedAdmin(pkg, scopes)) }) {
                    Icon(Icons.Outlined.Edit, stringResource(R.string.edit))
                }
            }
        }
        if(packages.isEmpty()) Text(
            stringResource(R.string.none),
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onNavigate(AddDelegatedAdmin()) }
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.padding(end = 12.dp))
            Text(stringResource(R.string.add_delegated_admin), style = typography.titleMedium)
        }
    }
}

@Serializable data class AddDelegatedAdmin(val pkg: String = "", val scopes: List<DelegatedScope> = emptyList())

@RequiresApi(26)
@Composable
fun AddDelegatedAdminScreen(data: AddDelegatedAdmin, onNavigateUp: () -> Unit) {
    val updateMode = data.pkg.isNotEmpty()
    val fm = LocalFocusManager.current
    val context = LocalContext.current
    var input by remember { mutableStateOf(data.pkg) }
    val scopes = remember { mutableStateListOf(*data.scopes.toTypedArray()) }
    val choosePackage = rememberLauncherForActivityResult(ChoosePackageContract()) { result ->
        result?.let { input = it }
    }
    MySmallTitleScaffold(if(updateMode) R.string.place_holder else R.string.add_delegated_admin, onNavigateUp, 0.dp) {
        OutlinedTextField(
            value = input, onValueChange = { input = it },
            label = { Text(stringResource(R.string.package_name)) },
            trailingIcon = {
                if(!updateMode) IconButton({ choosePackage.launch(null) }) {
                    Icon(painterResource(R.drawable.list_fill0), null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() },
            readOnly = updateMode,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp, horizontal = HorizontalPadding)
        )
        DelegatedScope.entries.filter { VERSION.SDK_INT >= it.requiresApi }.forEach { scope ->
            val checked = scope in scopes
            Row(
                Modifier.fillMaxWidth().clickable { if(!checked) scopes += scope else scopes -= scope }.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked, { if(it) scopes += scope else scopes -= scope }, modifier = Modifier.padding(horizontal = 4.dp))
                Column {
                    Text(stringResource(scope.string))
                    Text(scope.id, style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                }
            }
        }
        Button(
            onClick = {
                context.getDPM().setDelegatedScopes(context.getReceiver(), input, scopes.map { it.id })
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, vertical = 4.dp),
            enabled = input.isNotBlank() && (!updateMode || scopes.toList() != data.scopes)
        ) {
            Text(stringResource(if(updateMode) R.string.update else R.string.add))
        }
        if(updateMode) Button(
            onClick = {
                context.getDPM().setDelegatedScopes(context.getReceiver(), input, emptyList())
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding),
            colors = ButtonDefaults.buttonColors(colorScheme.error, colorScheme.onError)
        ) {
            Text(stringResource(R.string.delete))
        }
    }
}

@Serializable object DeviceInfo

@Composable
fun DeviceInfoScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.device_info, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT>=34 && (context.isDeviceOwner || dpm.isOrgProfile(receiver))) {
            InfoItem(R.string.financed_device, dpm.isDeviceFinanced.yesOrNo)
        }
        if(VERSION.SDK_INT >= 33) {
            val dpmRole = dpm.devicePolicyManagementRoleHolderPackage
            InfoItem(R.string.dpmrh, if(dpmRole == null) stringResource(R.string.none) else dpmRole)
        }
        val encryptionStatus = mutableMapOf(
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE to R.string.es_inactive,
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE to R.string.es_active,
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED to R.string.es_unsupported
        )
        if(VERSION.SDK_INT >= 23) { encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY] = R.string.es_active_default_key }
        if(VERSION.SDK_INT >= 24) { encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER] = R.string.es_active_per_user }
        InfoItem(R.string.encryption_status, encryptionStatus[dpm.storageEncryptionStatus] ?: R.string.unknown)
        if(VERSION.SDK_INT >= 28) {
            InfoItem(R.string.support_device_id_attestation, dpm.isDeviceIdAttestationSupported.yesOrNo, true) { dialog = 1 }
        }
        if (VERSION.SDK_INT >= 30) {
            InfoItem(R.string.support_unique_device_attestation, dpm.isUniqueDeviceAttestationSupported.yesOrNo, true) { dialog = 2 }
        }
        val adminList = dpm.activeAdmins
        if(adminList != null) {
            InfoItem(R.string.activated_device_admin, adminList.map { it.flattenToShortString() }.joinToString("\n"))
        }
    }
    if(dialog != 0) AlertDialog(
        text = { Text(stringResource(if(dialog == 1) R.string.info_device_id_attestation else R.string.info_unique_device_attestation)) },
        confirmButton = { TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.confirm)) } },
        onDismissRequest = { dialog = 0 }
    )
}

@Serializable object SupportMessage

@RequiresApi(24)
@Composable
fun SupportMessageScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var shortMsg by remember { mutableStateOf("") }
    var longMsg by remember { mutableStateOf("") }
    val refreshMsg = {
        shortMsg = dpm.getShortSupportMessage(receiver)?.toString() ?: ""
        longMsg = dpm.getLongSupportMessage(receiver)?.toString() ?: ""
    }
    LaunchedEffect(Unit) { refreshMsg() }
    MyScaffold(R.string.support_messages, onNavigateUp) {
        OutlinedTextField(
            value = shortMsg,
            label = { Text(stringResource(R.string.short_support_msg)) },
            onValueChange = { shortMsg = it },
            minLines = 2,
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    dpm.setShortSupportMessage(receiver, shortMsg)
                    refreshMsg()
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(text = stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    dpm.setShortSupportMessage(receiver, null)
                    refreshMsg()
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(text = stringResource(R.string.reset))
            }
        }
        Notes(R.string.info_short_support_message)
        Spacer(Modifier.padding(vertical = 8.dp))
        OutlinedTextField(
            value = longMsg,
            label = { Text(stringResource(R.string.long_support_msg)) },
            onValueChange = { longMsg = it },
            minLines = 3,
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    dpm.setLongSupportMessage(receiver, longMsg)
                    refreshMsg()
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(text = stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    dpm.setLongSupportMessage(receiver, null)
                    refreshMsg()
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(text = stringResource(R.string.reset))
            }
        }
        Notes(R.string.info_long_support_message)
    }
}

@Serializable object TransferOwnership

@RequiresApi(28)
@Composable
fun TransferOwnershipScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    val componentName = ComponentName.unflattenFromString(input)
    var dialog by remember { mutableStateOf(false) }
    MyScaffold(R.string.transfer_ownership, onNavigateUp) {
        OutlinedTextField(
            value = input, onValueChange = { input = it }, label = { Text(stringResource(R.string.target_component_name)) },
            modifier = Modifier.fillMaxWidth(),
            isError = input != "" && componentName == null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { dialog = true },
            modifier = Modifier.fillMaxWidth(),
            enabled = componentName != null
        ) {
            Text(stringResource(R.string.transfer))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.info_transfer_ownership)
    }
    if(dialog) AlertDialog(
        text = {
            Text(stringResource(
                R.string.transfer_ownership_warning,
                stringResource(if(context.isDeviceOwner) R.string.device_owner else R.string.profile_owner),
                ComponentName.unflattenFromString(input)!!.packageName
            ))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val dpm = context.getDPM()
                    val receiver = context.getReceiver()
                    try {
                        dpm.transferOwnership(receiver, componentName!!, null)
                        context.showOperationResultToast(true)
                        dialog = false
                        backToHomeStateFlow.value = true
                    } catch(e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                },
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = { dialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = false }
    )
}

private fun activateDeviceAdmin(inputContext:Context,inputComponent:ComponentName) {
    try {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, inputComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, inputContext.getString(R.string.activate_device_admin_here))
        addDeviceAdmin.launch(intent)
    } catch(_:ActivityNotFoundException) {
        Toast.makeText(inputContext, R.string.unsupported, Toast.LENGTH_SHORT).show()
    }
}
