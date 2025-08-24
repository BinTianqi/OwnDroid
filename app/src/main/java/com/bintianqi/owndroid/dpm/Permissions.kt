package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.PersistableBundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.ChoosePackageContract
import com.bintianqi.owndroid.DHIZUKU_CLIENTS_FILE
import com.bintianqi.owndroid.DhizukuClientInfo
import com.bintianqi.owndroid.DhizukuPermissions
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.IUserService
import com.bintianqi.owndroid.MyAdminComponent
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Settings
import com.bintianqi.owndroid.SharedPrefs
import com.bintianqi.owndroid.myPrivilege
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.InfoItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.updatePrivilege
import com.bintianqi.owndroid.useShizuku
import com.bintianqi.owndroid.yesOrNo
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable data class WorkModes(val canNavigateUp: Boolean)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkModesScreen(
    params: WorkModes, onNavigateUp: () -> Unit, onActivate: () -> Unit, onNavigate: (Any) -> Unit
) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val privilege by myPrivilege.collectAsStateWithLifecycle()
    /** 0: none, 1: device owner, 2: circular progress indicator, 3: result, 4: deactivate, 5: command */
    var dialog by remember { mutableIntStateOf(0) }
    LaunchedEffect(privilege) {
        if (!params.canNavigateUp && privilege.device) {
            delay(1000)
            if (dialog != 3) dialog = 3 // Activated by ADB command
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                {
                    if(!params.canNavigateUp) {
                        Column {
                            Text(stringResource(R.string.app_name))
                            Text(stringResource(R.string.choose_work_mode), Modifier.alpha(0.8F), style = typography.bodyLarge)
                        }
                    }
                },
                navigationIcon = {
                    if(params.canNavigateUp) NavIcon(onNavigateUp)
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    if(privilege.device || privilege.profile) Box {
                        IconButton({ expanded = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.deactivate)) },
                                {
                                    expanded = false
                                    dialog = 4
                                },
                                leadingIcon = { Icon(Icons.Default.Close, null) }
                            )
                            if (VERSION.SDK_INT >= 26) DropdownMenuItem(
                                { Text(stringResource(R.string.delegated_admins)) },
                                {
                                    expanded = false
                                    onNavigate(DelegatedAdmins)
                                },
                                leadingIcon = { Icon(painterResource(R.drawable.admin_panel_settings_fill0), null) }
                            )
                            if (!privilege.dhizuku && VERSION.SDK_INT >= 28) DropdownMenuItem(
                                { Text(stringResource(R.string.transfer_ownership)) },
                                {
                                    expanded = false
                                    onNavigate(TransferOwnership)
                                },
                                leadingIcon = { Icon(painterResource(R.drawable.swap_horiz_fill0), null) }
                            )
                        }
                    }
                    if(!params.canNavigateUp) IconButton({ onNavigate(Settings) }) {
                        Icon(Icons.Default.Settings, null)
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        var navigateUpOnSucceed by remember { mutableStateOf(true) }
        var operationSucceed by remember { mutableStateOf(false) }
        var resultText by remember { mutableStateOf("") }
        fun handleResult(succeeded: Boolean, activateSucceeded: Boolean, output: String?) {
            if(succeeded) {
                operationSucceed = activateSucceeded
                resultText = output ?: ""
                dialog = 3
                updatePrivilege(context)
                handlePrivilegeChange(context)
            } else {
                dialog = 0
                context.showOperationResultToast(false)
            }
        }
        Column(Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            if(!privilege.profile && (VERSION.SDK_INT >= 28 || !privilege.dhizuku)) Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(!privilege.device || privilege.dhizuku) { dialog = 1 }
                    .background(if (privilege.device) colorScheme.primaryContainer else Color.Transparent)
                    .padding(HorizontalPadding, 10.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.device_owner), style = typography.titleLarge)
                    if(!privilege.device || privilege.dhizuku) Text(
                        stringResource(R.string.recommended), color = colorScheme.primary, style = typography.labelLarge
                    )
                }
                Icon(
                    if(privilege.device) Icons.Default.Check else Icons.AutoMirrored.Default.KeyboardArrowRight, null,
                    tint = if(privilege.device) colorScheme.primary else colorScheme.onBackground
                )
            }
            if(privilege.profile) Row(
                Modifier
                    .fillMaxWidth()
                    .background(if (privilege.device) colorScheme.primaryContainer else Color.Transparent)
                    .padding(HorizontalPadding, 10.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.profile_owner), style = typography.titleLarge)
                }
                Icon(
                    if(privilege.device) Icons.Default.Check else Icons.AutoMirrored.Default.KeyboardArrowRight, null,
                    tint = if(privilege.device) colorScheme.primary else colorScheme.onBackground
                )
            }
            if(privilege.dhizuku || !(privilege.device || privilege.profile)) Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(!privilege.dhizuku) {
                        dialog = 2
                        activateDhizukuMode(context, ::handleResult)
                    }
                    .background(if (privilege.dhizuku) colorScheme.primaryContainer else Color.Transparent)
                    .padding(HorizontalPadding, 10.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.dhizuku), style = typography.titleLarge)
                Icon(
                    if(privilege.dhizuku) Icons.Default.Check else Icons.AutoMirrored.Default.KeyboardArrowRight, null,
                    tint = if(privilege.dhizuku) colorScheme.primary else colorScheme.onBackground
                )
            }
            if(
                privilege.work || (VERSION.SDK_INT < 24 ||
                        context.getDPM().isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE))
            ) Row(
                Modifier
                    .fillMaxWidth()
                    .clickable(!privilege.work) { onNavigate(CreateWorkProfile) }
                    .background(if (privilege.device) colorScheme.primaryContainer else Color.Transparent)
                    .padding(HorizontalPadding, 10.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text(stringResource(R.string.work_profile), style = typography.titleLarge)
                }
                Icon(
                    if(privilege.work) Icons.Default.Check else Icons.AutoMirrored.Default.KeyboardArrowRight, null,
                    tint = if(privilege.device) colorScheme.primary else colorScheme.onBackground
                )
            }
            if ((privilege.device || privilege.profile) && !privilege.dhizuku) Row(
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .clickable { onNavigate(DhizukuServerSettings) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(R.drawable.dhizuku_icon), null, Modifier
                    .padding(8.dp)
                    .size(28.dp))
                Text(stringResource(R.string.dhizuku_server), style = typography.titleLarge)
            }

            Column(Modifier.padding(HorizontalPadding, 20.dp)) {
                Row(Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Warning, null, Modifier.padding(end = 4.dp), colorScheme.error)
                    Text(stringResource(R.string.warning), color = colorScheme.error, style = typography.labelLarge)
                }
                Text(stringResource(R.string.owndroid_warning))
            }
        }
        if(dialog == 1) AlertDialog(
            title = { Text(stringResource(R.string.activate_method)) },
            text = {
                FlowRow(Modifier.fillMaxWidth()) {
                    if(!privilege.dhizuku) Button({
                        dialog = 2
                        coroutine.launch {
                            activateUsingShizuku(context, ::handleResult)
                        }
                    }, Modifier.padding(end = 8.dp)) {
                        Text(stringResource(R.string.shizuku))
                    }
                    if(!privilege.dhizuku) Button({
                        dialog = 2
                        activateUsingRoot(context, ::handleResult)
                    }, Modifier.padding(end = 8.dp)) {
                        Text("Root")
                    }
                    if(VERSION.SDK_INT >= 28) Button({
                        dialog = 2
                        activateUsingDhizuku(context, ::handleResult)
                    }, Modifier.padding(end = 8.dp)) {
                        Text(stringResource(R.string.dhizuku))
                    }
                    if (!privilege.dhizuku) Button({ dialog = 5 }) { Text(stringResource(R.string.adb_command)) }
                }
            },
            confirmButton = {
                TextButton({ dialog = 0 }) { Text(stringResource(R.string.cancel)) }
            },
            onDismissRequest = { dialog = 0 }
        )
        if(dialog == 2) CircularProgressDialog {  }
        if(dialog == 3) AlertDialog(
            title = { Text(stringResource(if(operationSucceed) R.string.succeeded else R.string.failed)) },
            text = {
                Column(Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())) {
                    Text(resultText)
                }
            },
            confirmButton = {
                TextButton({
                    dialog = 0
                    if(navigateUpOnSucceed && operationSucceed && !params.canNavigateUp) onActivate()
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = {}
        )
        if(dialog == 4) AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            text = { Text(stringResource(R.string.info_deactivate)) },
            confirmButton = {
                var time by remember { mutableIntStateOf(3) }
                LaunchedEffect(Unit) {
                    for (i in (0..2).reversed()) {
                        delay(1000)
                        time = i
                    }
                }
                val timeText = if (time != 0) " (${time}s)" else ""
                TextButton(
                    {
                        if(privilege.dhizuku) {
                            SharedPrefs(context).dhizuku = false
                        } else {
                            val dpm = context.getDPM()
                            if(privilege.device) {
                                dpm.clearDeviceOwnerApp(context.packageName)
                            } else if(VERSION.SDK_INT >= 24) {
                                dpm.clearProfileOwner(MyAdminComponent)
                            }
                        }
                        dialog = 0
                        updatePrivilege(context)
                        handlePrivilegeChange(context)
                    },
                    enabled = time == 0,
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm) + timeText)
                }
            },
            dismissButton = {
                TextButton({ dialog = 0 }) { Text(stringResource(R.string.cancel)) }
            },
            onDismissRequest = { dialog = 0 }
        )
        if(dialog == 5) AlertDialog(
            text = {
                SelectionContainer {
                    Text(ACTIVATE_DEVICE_OWNER_COMMAND)
                }
            },
            confirmButton = {
                TextButton({ dialog = 0 }) { Text(stringResource(R.string.confirm)) }
            },
            onDismissRequest = { dialog = 0 }
        )
    }
}

fun activateUsingShizuku(context: Context, callback: (Boolean, Boolean, String?) -> Unit) {
    useShizuku(context) { service ->
        try {
            val result = IUserService.Stub.asInterface(service).execute(ACTIVATE_DEVICE_OWNER_COMMAND)
            if (result == null) {
                callback(false, false, null)
            } else {
                callback(
                    true, result.getInt("code", -1) == 0,
                    result.getString("output") + "\n" + result.getString("error")
                )
            }
        } catch (e: Exception) {
            callback(false, false, null)
            e.printStackTrace()
        }
    }
}

fun activateUsingRoot(context: Context, callback: (Boolean, Boolean, String?) -> Unit) {
    Shell.getShell { shell ->
        if(shell.isRoot) {
            val result = Shell.cmd(ACTIVATE_DEVICE_OWNER_COMMAND).exec()
            val output = result.out.joinToString("\n") + "\n" + result.err.joinToString("\n")
            callback(true, result.isSuccess, output)
        } else {
            callback(true, false, context.getString(R.string.permission_denied))
        }
    }
}

@RequiresApi(28)
fun activateUsingDhizuku(context: Context, callback: (Boolean, Boolean, String?) -> Unit) {
    fun doTransfer() {
        try {
            val dpm = binderWrapperDevicePolicyManager(context)
            if(dpm == null) {
                context.showOperationResultToast(false)
            } else {
                dpm.transferOwnership(Dhizuku.getOwnerComponent(), MyAdminComponent, PersistableBundle())
                callback(true, true, null)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            callback(true, false, null)
        }
    }
    if(Dhizuku.init(context)) {
        if(Dhizuku.isPermissionGranted()) {
            doTransfer()
        } else {
            Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                override fun onRequestPermission(grantResult: Int) {
                    if(grantResult == PackageManager.PERMISSION_GRANTED) doTransfer()
                    else callback(false, false, null)
                }
            })
        }
    } else {
        callback(true, false, context.getString(R.string.failed_to_init_dhizuku))
    }
}

fun activateDhizukuMode(context: Context, callback: (Boolean, Boolean, String?) -> Unit) {
    fun onSucceed() {
        SharedPrefs(context).dhizuku = true
        callback(true, true, null)
    }
    if(Dhizuku.init(context)) {
        if(Dhizuku.isPermissionGranted()) {
            onSucceed()
        } else {
            Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                override fun onRequestPermission(grantResult: Int) {
                    if(grantResult == PackageManager.PERMISSION_GRANTED) onSucceed()
                }
            })
        }
    } else {
        callback(true, false, context.getString(R.string.failed_to_init_dhizuku))
    }
}

const val ACTIVATE_DEVICE_OWNER_COMMAND = "dpm set-device-owner com.bintianqi.owndroid/.Receiver"

@Serializable object DhizukuServerSettings

@Composable
fun DhizukuServerSettingsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val sp = SharedPrefs(context)
    val file = context.filesDir.resolve(DHIZUKU_CLIENTS_FILE)
    var enabled by remember { mutableStateOf(sp.dhizukuServer) }
    val clients = remember { mutableStateListOf<DhizukuClientInfo>() }
    fun changeEnableState(status: Boolean) {
        enabled = status
        sp.dhizukuServer = status
    }
    fun writeList() {
        file.writeText(Json.encodeToString(clients.toList()))
    }
    LaunchedEffect(Unit) {
        if (!file.exists()) file.writeText("[]")
    }
    LaunchedEffect(enabled) {
        if (enabled) {
            clients.clear()
            val json = Json { ignoreUnknownKeys = true }
            clients.addAll(json.decodeFromString<List<DhizukuClientInfo>>(file.readText()))
        }
    }
    MyLazyScaffold(R.string.dhizuku_server, onNavigateUp) {
        item {
            SwitchItem(R.string.enable, getState = { sp.dhizukuServer }, onCheckedChange = ::changeEnableState)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        if (enabled) itemsIndexed(clients) { index, client ->
            val name = pm.getNameForUid(client.uid)
            if (name == null) {
                clients.dropWhile { it.uid == client.uid }
                writeList()
            } else {
                val info = pm.getApplicationInfo(name, 0)
                var expand by remember { mutableStateOf(false) }
                Card(
                    Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(8.dp, 8.dp, 0.dp, 8.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                rememberDrawablePainter(info.loadIcon(pm)), null,
                                Modifier
                                    .padding(end = 16.dp)
                                    .size(50.dp)
                            )
                            Column {
                                Text(info.loadLabel(pm).toString(), style = typography.titleLarge)
                                Text(name, Modifier.alpha(0.7F), style = typography.bodyMedium)
                            }
                        }
                        val ts = when (DhizukuPermissions.filter { it !in client.permissions }.size) {
                            0 -> ToggleableState.On
                            DhizukuPermissions.size -> ToggleableState.Off
                            else -> ToggleableState.Indeterminate
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TriStateCheckbox(ts, {
                                clients[index] = when (ts) {
                                    ToggleableState.On, ToggleableState.Indeterminate -> client.copy(permissions = emptyList())
                                    ToggleableState.Off -> client.copy(permissions = DhizukuPermissions)
                                }
                            })
                            val degrees by animateFloatAsState(if(expand) 180F else 0F)
                            IconButton({ expand = !expand }) {
                                Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(degrees))
                            }
                        }
                    }
                    AnimatedVisibility(expand, Modifier.padding(8.dp, 0.dp, 8.dp, 8.dp)) {
                        Column {
                            mapOf(
                                "remote_transact" to "Remote transact", "remote_process" to "Remote process",
                                "user_service" to "User service", "delegated_scopes" to "Delegated scopes",
                                "other" to context.getString(R.string.other)
                            ).forEach { (k, v) ->
                                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                                    Text(v)
                                    Checkbox(k in client.permissions, {
                                        val newPermissions = if (it) client.permissions.plus(k) else client.permissions.minus(k)
                                        clients[index] = client.copy(permissions = newPermissions)
                                        writeList()
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
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
                infoText = ""
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
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .padding(start = 14.dp, end = 8.dp),
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
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 4.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = HorizontalPadding)
        )
        DelegatedScope.entries.filter { VERSION.SDK_INT >= it.requiresApi }.forEach { scope ->
            val checked = scope in scopes
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { if (!checked) scopes += scope else scopes -= scope }
                    .padding(vertical = 4.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, vertical = 4.dp),
            enabled = input.isNotBlank() && (!updateMode || scopes.toList() != data.scopes)
        ) {
            Text(stringResource(if(updateMode) R.string.update else R.string.add))
        }
        if(updateMode) Button(
            onClick = {
                context.getDPM().setDelegatedScopes(context.getReceiver(), input, emptyList())
                onNavigateUp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding),
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
    val privilege by myPrivilege.collectAsStateWithLifecycle()
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.device_info, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT>=34 && (privilege.device || privilege.org)) {
            InfoItem(R.string.financed_device, dpm.isDeviceFinanced.yesOrNo)
        }
        if(VERSION.SDK_INT >= 33) {
            val dpmRole = dpm.devicePolicyManagementRoleHolderPackage
            InfoItem(R.string.dpmrh, dpmRole ?: stringResource(R.string.none))
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
            InfoItem(R.string.activated_device_admin, adminList.joinToString("\n") { it.flattenToShortString() })
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 2.dp)
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
fun TransferOwnershipScreen(onNavigateUp: () -> Unit, onTransferred: () -> Unit) {
    val context = LocalContext.current
    val privilege by myPrivilege.collectAsStateWithLifecycle()
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
                stringResource(if(privilege.device) R.string.device_owner else R.string.profile_owner),
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
                        updatePrivilege(context)
                        dialog = false
                        onTransferred()
                    } catch(e: Exception) {
                        e.printStackTrace()
                        context.showOperationResultToast(false)
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
