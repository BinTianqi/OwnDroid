package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
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
import androidx.compose.material3.RadioButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.AppInfo
import com.bintianqi.owndroid.DhizukuClientInfo
import com.bintianqi.owndroid.DhizukuPermissions
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Settings
import com.bintianqi.owndroid.adaptiveInsets
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.InfoItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.yesOrNo
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable data class WorkModes(val canNavigateUp: Boolean)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkModesScreen(
    vm: MyViewModel, params: WorkModes, onNavigateUp: () -> Unit, onActivate: () -> Unit,
    onDeactivate: () -> Unit, onNavigate: (Any) -> Unit
) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    /** 0: none, 1: device owner, 2: circular progress indicator, 3: result, 4: deactivate, 5: command */
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    var operationSucceed by rememberSaveable { mutableStateOf(false) }
    var resultText by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(privilege) {
        if (!params.canNavigateUp && privilege.device) {
            delay(1000)
            if (dialog != 3) { // Activated by ADB command
                operationSucceed = true
                resultText = ""
                dialog = 3
            }
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
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        fun handleResult(succeeded: Boolean, output: String?) {
            operationSucceed = succeeded
            resultText = output ?: ""
            dialog = 3
        }
        Column(Modifier.fillMaxSize().padding(paddingValues)) {
            if (!privilege.profile) {
                WorkingModeItem(R.string.device_owner, privilege.device) {
                    if (!privilege.device || (VERSION.SDK_INT >= 28 && privilege.dhizuku)) {
                        dialog = 1
                    }
                }
            }
            if (privilege.profile) WorkingModeItem(R.string.profile_owner, true) { }
            if (privilege.dhizuku || !privilege.activated) {
                WorkingModeItem(R.string.dhizuku, privilege.dhizuku) {
                    if (!privilege.dhizuku) {
                        dialog = 2
                        vm.activateDhizukuMode(::handleResult)
                    }
                }
            }
            if(
                privilege.work || (VERSION.SDK_INT < 24 || vm.isCreatingWorkProfileAllowed())
            ) {
                WorkingModeItem(R.string.work_profile, privilege.work) {
                    if (!privilege.work) onNavigate(CreateWorkProfile)
                }
            }
            if (privilege.activated && !privilege.dhizuku) Row(
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
                    if (!privilege.dhizuku) {
                        Button({ dialog = 5 }, Modifier.padding(end = 8.dp)) {
                            Text(stringResource(R.string.adb_command))
                        }
                        Button({
                            dialog = 2
                            vm.activateDoByShizuku(::handleResult)
                        }, Modifier.padding(end = 8.dp)) {
                            Text(stringResource(R.string.shizuku))
                        }
                        Button({
                            dialog = 2
                            vm.activateDoByRoot(::handleResult)
                        }, Modifier.padding(end = 8.dp)) {
                            Text("Root")
                        }
                    }
                    if (VERSION.SDK_INT >= 28 && privilege.dhizuku) Button({
                        dialog = 2
                        vm.activateDoByDhizuku(::handleResult)
                    }, Modifier.padding(end = 8.dp)) {
                        Text(stringResource(R.string.dhizuku))
                    }
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
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                    Text(resultText)
                }
            },
            confirmButton = {
                TextButton({
                    dialog = 0
                    if (operationSucceed && !params.canNavigateUp) onActivate()
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
                            vm.deactivateDhizukuMode()
                        } else {
                            if(privilege.device) {
                                vm.clearDeviceOwner()
                            } else if(VERSION.SDK_INT >= 24) {
                                vm.clearProfileOwner()
                            }
                            // Status updated in Receiver.onDisabled()
                        }
                        dialog = 0
                        onDeactivate()
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

@Composable
fun WorkingModeItem(text: Int, active: Boolean, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .background(if (active) colorScheme.primaryContainer else Color.Transparent)
            .padding(HorizontalPadding, 10.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(stringResource(text), style = typography.titleLarge)
        Icon(
            if(active) Icons.Default.Check else Icons.AutoMirrored.Default.KeyboardArrowRight, null,
            tint = if(active) colorScheme.primary else colorScheme.onBackground
        )
    }
}

const val ACTIVATE_DEVICE_OWNER_COMMAND = "dpm set-device-owner com.bintianqi.owndroid/.Receiver"

@Serializable object DhizukuServerSettings

@Composable
fun DhizukuServerSettingsScreen(
    dhizukuClients: StateFlow<List<Pair<DhizukuClientInfo, AppInfo>>>,
    getDhizukuClients: () -> Unit, updateDhizukuClient: (DhizukuClientInfo) -> Unit,
    getServerEnabled: () -> Boolean, setServerEnabled: (Boolean) -> Unit, onNavigateUp: () -> Unit
) {
    var enabled by rememberSaveable { mutableStateOf(getServerEnabled()) }
    val clients by dhizukuClients.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { getDhizukuClients() }
    MyLazyScaffold(R.string.dhizuku_server, onNavigateUp) {
        item {
            SwitchItem(R.string.enable, enabled, {
                setServerEnabled(it)
                enabled = it
            })
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        if (enabled) items(clients) { (client, app) ->
            var expand by remember { mutableStateOf(false) }
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 8.dp)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp, 8.dp, 0.dp, 8.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            rememberDrawablePainter(app.icon), null,
                            Modifier.padding(end = 16.dp).size(45.dp)
                        )
                        Column {
                            Text(app.label, style = typography.titleMedium)
                            Text(app.name, Modifier.alpha(0.7F), style = typography.bodyMedium)
                        }
                    }
                    val ts = when (DhizukuPermissions.filter { it !in client.permissions }.size) {
                        0 -> ToggleableState.On
                        DhizukuPermissions.size -> ToggleableState.Off
                        else -> ToggleableState.Indeterminate
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TriStateCheckbox(ts, {
                            if (ts == ToggleableState.Off) {
                                updateDhizukuClient(client.copy(permissions = DhizukuPermissions))
                            } else {
                                updateDhizukuClient(client.copy(permissions = emptyList()))
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
                            "other" to "Other"
                        ).forEach { (k, v) ->
                            Row(
                                Modifier.fillMaxWidth(), Arrangement.SpaceBetween,
                                Alignment.CenterVertically
                            ) {
                                Text(v)
                                Checkbox(k in client.permissions, {
                                    updateDhizukuClient(client.copy(
                                        permissions = client.permissions.run { if (it) plus(k) else minus(k) }
                                    ))
                                })
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
fun LockScreenInfoScreen(
    getText: () -> String, setText: (String) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var infoText by rememberSaveable { mutableStateOf(getText()) }
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
                setText(infoText)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                setText("")
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

data class DelegatedScope(val id: String, val string: Int, val requiresApi: Int = 26)
@Suppress("InlinedApi")
val delegatedScopesList = listOf(
    DelegatedScope(DevicePolicyManager.DELEGATION_APP_RESTRICTIONS, R.string.manage_application_restrictions),
    DelegatedScope(DevicePolicyManager.DELEGATION_BLOCK_UNINSTALL, R.string.block_uninstall),
    DelegatedScope(DevicePolicyManager.DELEGATION_CERT_INSTALL, R.string.manage_certificates),
    DelegatedScope(DevicePolicyManager.DELEGATION_CERT_SELECTION, R.string.select_keychain_certificates, 29),
    DelegatedScope(DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP, R.string.enable_system_app),
    DelegatedScope(DevicePolicyManager.DELEGATION_INSTALL_EXISTING_PACKAGE, R.string.install_existing_packages, 28),
    DelegatedScope(DevicePolicyManager.DELEGATION_KEEP_UNINSTALLED_PACKAGES, R.string.manage_uninstalled_packages, 28),
    DelegatedScope(DevicePolicyManager.DELEGATION_NETWORK_LOGGING, R.string.network_logging, 29),
    DelegatedScope(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS, R.string.change_package_state),
    DelegatedScope(DevicePolicyManager.DELEGATION_PERMISSION_GRANT, R.string.grant_permissions),
    DelegatedScope(DevicePolicyManager.DELEGATION_SECURITY_LOGGING, R.string.security_logging, 31)
).filter { VERSION.SDK_INT >= it.requiresApi }

data class DelegatedAdmin(val app: AppInfo, val scopes: List<String>)

@Serializable object DelegatedAdmins

@RequiresApi(26)
@Composable
fun DelegatedAdminsScreen(
    delegatedAdmins: StateFlow<List<DelegatedAdmin>>, getDelegatedAdmins: () -> Unit,
    onNavigateUp: () -> Unit, onNavigate: (AddDelegatedAdmin) -> Unit
) {
    val admins by delegatedAdmins.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { getDelegatedAdmins() }
    MyLazyScaffold(R.string.delegated_admins, onNavigateUp) {
        items(admins, { it.app.name }) { (app, scopes) ->
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp).animateItem(),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = rememberDrawablePainter(app.icon), contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp, end = 18.dp).size(40.dp)
                    )
                    Column {
                        Text(app.label)
                        Text(app.name, Modifier.alpha(0.8F), style = typography.bodyMedium)
                    }
                }
                IconButton({ onNavigate(AddDelegatedAdmin(app.name, scopes)) }) {
                    Icon(Icons.Outlined.Edit, null)
                }
            }
        }
        item {
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
}

@Serializable data class AddDelegatedAdmin(val pkg: String = "", val scopes: List<String> = emptyList())

@RequiresApi(26)
@Composable
fun AddDelegatedAdminScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit, data: AddDelegatedAdmin,
    setDelegatedAdmin: (String, List<String>) -> Unit,  onNavigateUp: () -> Unit
) {
    val updateMode = data.pkg.isNotEmpty()
    var input by rememberSaveable { mutableStateOf(data.pkg) }
    val scopes = rememberSaveable { mutableStateListOf(*data.scopes.toTypedArray()) }
    LaunchedEffect(Unit) {
        input = chosenPackage.receive()
    }
    MySmallTitleScaffold(if(updateMode) R.string.place_holder else R.string.add_delegated_admin, onNavigateUp, 0.dp) {
        if (updateMode) {
            OutlinedTextField(input, {}, Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp),
                enabled = false, label = { Text(stringResource(R.string.package_name)) })
        } else {
            PackageNameTextField(input, onChoosePackage,
                Modifier.padding(HorizontalPadding, 8.dp)) { input = it }
        }
        delegatedScopesList.forEach { scope ->
            val checked = scope.id in scopes
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { if (!checked) scopes += scope.id else scopes -= scope.id }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(checked, { if(it) scopes += scope.id else scopes -= scope.id },
                    modifier = Modifier.padding(horizontal = 4.dp))
                Column {
                    Text(stringResource(scope.string))
                    Text(scope.id, style = typography.bodyMedium, color = colorScheme.onSurfaceVariant)
                }
            }
        }
        Button(
            onClick = {
                setDelegatedAdmin(input, scopes)
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, vertical = 4.dp),
            enabled = input.isNotBlank() && (!updateMode || scopes.toList() != data.scopes)
        ) {
            Text(stringResource(if(updateMode) R.string.update else R.string.add))
        }
        if(updateMode) Button(
            onClick = {
                setDelegatedAdmin(input, emptyList())
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
            colors = ButtonDefaults.buttonColors(colorScheme.error, colorScheme.onError)
        ) {
            Text(stringResource(R.string.delete))
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Serializable object DeviceInfo

@Composable
fun DeviceInfoScreen(vm: MyViewModel, onNavigateUp: () -> Unit) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    MyScaffold(R.string.device_info, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 34 && (privilege.device || privilege.org)) {
            InfoItem(R.string.financed_device, vm.getDeviceFinanced().yesOrNo)
        }
        if (VERSION.SDK_INT >= 33) {
            InfoItem(R.string.dpmrh, vm.getDpmRh() ?: stringResource(R.string.none))
        }
        val encryptionStatus = when (vm.getStorageEncryptionStatus()) {
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE -> R.string.es_inactive
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE -> R.string.es_active
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED -> R.string.es_unsupported
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY -> R.string.es_active_default_key
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER -> R.string.es_active_per_user
            else -> R.string.unknown
        }
        InfoItem(R.string.encryption_status, encryptionStatus)
        if (VERSION.SDK_INT >= 28) {
            InfoItem(R.string.support_device_id_attestation, vm.getDeviceIdAttestationSupported().yesOrNo, true) { dialog = 1 }
        }
        if (VERSION.SDK_INT >= 30) {
            InfoItem(R.string.support_unique_device_attestation, vm.getUniqueDeviceAttestationSupported().yesOrNo, true) { dialog = 2 }
        }
        InfoItem(R.string.activated_device_admin, vm.getActiveAdmins())
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
fun SupportMessageScreen(
    getShortMessage: () -> String, getLongMessage: () -> String, setShortMessage: (String?) -> Unit,
    setLongMessage: (String?) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var shortMsg by rememberSaveable { mutableStateOf("") }
    var longMsg by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        shortMsg = getShortMessage()
        longMsg = getLongMessage()
    }
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
                    setShortMessage(shortMsg)
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(text = stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    setShortMessage(null)
                    shortMsg = ""
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
                    setLongMessage(longMsg)
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(text = stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    setLongMessage(null)
                    longMsg = ""
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

data class DeviceAdmin(val app: AppInfo, val admin: ComponentName)

@Serializable object TransferOwnership

@RequiresApi(28)
@Composable
fun TransferOwnershipScreen(
    deviceAdmins: StateFlow<List<DeviceAdmin>>, getDeviceAdmins: () -> Unit,
    transferOwnership: (ComponentName) -> Unit, onNavigateUp: () -> Unit, onTransferred: () -> Unit
) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    val receivers by deviceAdmins.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { getDeviceAdmins() }
    MyLazyScaffold(R.string.transfer_ownership, onNavigateUp) {
        itemsIndexed(receivers) { index, admin ->
            Row(
                Modifier.fillMaxWidth().clickable { selectedIndex = index }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selectedIndex == index, { selectedIndex = index })
                Image(rememberDrawablePainter(admin.app.icon), null, Modifier.size(40.dp))
                Column(Modifier.padding(start = 8.dp)) {
                    Text(admin.app.label)
                    Text(admin.app.name, Modifier.alpha(0.7F), style = typography.bodyMedium)
                }
            }
        }
        item {
            Button(
                onClick = { dialog = true },
                modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 10.dp),
                enabled = receivers.getOrNull(selectedIndex) != null
            ) {
                Text(stringResource(R.string.transfer))
            }
            Notes(R.string.info_transfer_ownership, HorizontalPadding)
        }
    }
    if (dialog) AlertDialog(
        text = {
            Text(stringResource(
                R.string.transfer_ownership_warning,
                stringResource(if(privilege.device) R.string.device_owner else R.string.profile_owner),
                receivers[selectedIndex].app.name
            ))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    transferOwnership(receivers[selectedIndex].admin)
                    dialog = false
                    onTransferred()
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
