package com.bintianqi.owndroid.feature.privilege

import android.os.Build.VERSION
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CancelTextButton
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.ConfirmTextButton
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.ACTIVATE_DEVICE_OWNER_COMMAND
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.activateOrgProfileCommand
import com.bintianqi.owndroid.utils.adaptiveInsets
import kotlinx.coroutines.delay

private enum class WorkingModeDialog {
    None,
    DeviceOwner, ProfileOwner, OrgProfile,
    DeviceOwnerCommand, ProfileOwnerCommand, OrgProfileCommand,
    ProgressIndicator, Result,
    Deactivate
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkModesScreen(
    vm: WorkingModesViewModel, params: Destination.WorkingModes, onNavigateUp: () -> Unit,
    onActivate: () -> Unit, onDeactivate: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.ps.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableStateOf(WorkingModeDialog.None) }
    var operationSucceed by rememberSaveable { mutableStateOf(false) }
    var resultText by rememberSaveable { mutableStateOf("") }
    fun handleResult(succeeded: Boolean, output: String?) {
        operationSucceed = succeeded
        resultText = output ?: ""
        dialog = WorkingModeDialog.Result
    }
    LaunchedEffect(Unit) {
        vm.getPrivilegeState()
    }
    LaunchedEffect(privilege) {
        if (!params.canNavigateUp && privilege.device) {
            delay(1000)
            if (dialog != WorkingModeDialog.Result) { // Activated by ADB command
                operationSucceed = true
                resultText = ""
                dialog = WorkingModeDialog.Result
                vm.de.value = null
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                {
                    if (!params.canNavigateUp) {
                        Column {
                            Text(stringResource(R.string.app_name))
                            Text(
                                stringResource(R.string.choose_working_mode), Modifier.alpha(0.8F),
                                style = typography.bodyLarge
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (params.canNavigateUp) NavIcon(onNavigateUp)
                },
                actions = {
                    var expanded by remember { mutableStateOf(false) }
                    if (privilege.device || privilege.profile) Box {
                        IconButton({ expanded = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(expanded, { expanded = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.deactivate)) },
                                {
                                    expanded = false
                                    dialog = WorkingModeDialog.Deactivate
                                },
                                leadingIcon = { Icon(Icons.Default.Close, null) }
                            )
                            if (!privilege.dhizuku && VERSION.SDK_INT >= 28) DropdownMenuItem(
                                { Text(stringResource(R.string.transfer_ownership)) },
                                {
                                    expanded = false
                                    onNavigate(Destination.TransferOwnership)
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.swap_horiz_fill0), null
                                    )
                                }
                            )
                        }
                    }
                    if (!params.canNavigateUp) IconButton({ onNavigate(Destination.Settings) }) {
                        Icon(Icons.Default.Settings, null)
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (!privilege.profile) {
                WorkingModeItem(R.string.device_owner, privilege.device) {
                    if (!privilege.device || (VERSION.SDK_INT >= 28 && privilege.dhizuku)) {
                        dialog = WorkingModeDialog.DeviceOwner
                    }
                }
            }
            if (!privilege.device) {
                WorkingModeItem(R.string.profile_owner, privilege.profile) {
                    if (!privilege.profile) {
                        dialog = WorkingModeDialog.ProfileOwner
                    }
                }
            }
            if (privilege.dhizuku || !privilege.activated) {
                WorkingModeItem(R.string.dhizuku, privilege.dhizuku) {
                    if (!privilege.dhizuku) {
                        dialog = WorkingModeDialog.ProgressIndicator
                        vm.activateDhizukuMode(::handleResult)
                    }
                }
            }
            if (
                privilege.work || (VERSION.SDK_INT < 24 || vm.isCreatingWorkProfileAllowed())
            ) {
                WorkingModeItem(R.string.work_profile, privilege.work) {
                    if (!privilege.work) onNavigate(Destination.CreateWorkProfile)
                }
            }
            if (privilege.work) {
                WorkingModeItem(R.string.org_owned_work_profile, privilege.org) {
                    if (!privilege.org) dialog = WorkingModeDialog.OrgProfile
                }
            }
            if (privilege.activated && !privilege.dhizuku) Row(
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .clickable { onNavigate(Destination.DhizukuServerSettings) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.m3_dhizuku_icon), null,
                    Modifier.padding(start = 12.dp, end = 8.dp)
                )
                Text(stringResource(R.string.dhizuku_server), style = typography.titleLarge)
            }
            if (privilege.device || privilege.profile) Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { onNavigate(Destination.DelegatedAdmins) }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.admin_panel_settings_fill0), null,
                    Modifier.padding(start = 12.dp, end = 8.dp)
                )
                Text(stringResource(R.string.delegated_admins), style = typography.titleLarge)
            }
            Column(Modifier.padding(HorizontalPadding, 20.dp)) {
                Row(
                    Modifier.padding(bottom = 4.dp), verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Outlined.Warning, null, Modifier.padding(end = 4.dp),
                        colorScheme.error
                    )
                    Text(
                        stringResource(R.string.warning), color = colorScheme.error,
                        style = typography.labelLarge
                    )
                }
                Text(stringResource(R.string.owndroid_warning))
            }
        }
    }
    if (dialog == WorkingModeDialog.DeviceOwner) AlertDialog(
        title = { Text(stringResource(R.string.activate_method)) },
        text = {
            FlowRow(Modifier.fillMaxWidth()) {
                if (!privilege.dhizuku) {
                    Button(
                        { dialog = WorkingModeDialog.DeviceOwnerCommand },
                        Modifier.padding(end = 8.dp)
                    ) {
                        Text(stringResource(R.string.adb_command))
                    }
                    Button({
                        dialog = WorkingModeDialog.ProgressIndicator
                        vm.activateDoByShizuku(::handleResult)
                    }, Modifier.padding(end = 8.dp)) {
                        Text(stringResource(R.string.shizuku))
                    }
                    Button({
                        dialog = WorkingModeDialog.ProgressIndicator
                        vm.activateDoByRoot(::handleResult)
                    }, Modifier.padding(end = 8.dp)) {
                        Text("Root")
                    }
                }
                if (VERSION.SDK_INT >= 28 && privilege.dhizuku) Button({
                    dialog = WorkingModeDialog.ProgressIndicator
                    vm.activateDoByDhizuku(::handleResult)
                }, Modifier.padding(end = 8.dp)) {
                    Text(stringResource(R.string.dhizuku))
                }
            }
        },
        confirmButton = {
            CancelTextButton { dialog = WorkingModeDialog.None }
        },
        onDismissRequest = { dialog = WorkingModeDialog.None }
    )
    if (dialog == WorkingModeDialog.ProfileOwner) AlertDialog(
        text = {
            Column {
                Button({
                    dialog = WorkingModeDialog.ProgressIndicator
                    vm.activatePoByShizuku(::handleResult)
                }) {
                    Text(stringResource(R.string.shizuku))
                }
                Button({ dialog = WorkingModeDialog.ProfileOwnerCommand }) {
                    Text(stringResource(R.string.adb_command))
                }
            }
        },
        confirmButton = {
            CancelTextButton { dialog = WorkingModeDialog.None }
        },
        onDismissRequest = { dialog = WorkingModeDialog.None }
    )
    if (dialog == WorkingModeDialog.OrgProfile) AlertDialog(
        text = {
            Column {
                Button({
                    dialog = WorkingModeDialog.ProgressIndicator
                    vm.activateOrgProfileByShizuku(::handleResult)
                }) {
                    Text(stringResource(R.string.shizuku))
                }
                Button({ dialog = WorkingModeDialog.OrgProfileCommand }) {
                    Text(stringResource(R.string.adb_command))
                }
            }
        },
        confirmButton = {
            CancelTextButton { dialog = WorkingModeDialog.None }
        },
        onDismissRequest = { dialog = WorkingModeDialog.None }
    )
    if (dialog == WorkingModeDialog.ProgressIndicator) CircularProgressDialog { }
    if (dialog == WorkingModeDialog.Result) AlertDialog(
        title = {
            Text(stringResource(if (operationSucceed) R.string.succeeded else R.string.failed))
        },
        text = {
            SelectionContainer(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Text(resultText)
            }
        },
        confirmButton = {
            ConfirmTextButton {
                dialog = WorkingModeDialog.None
                if (operationSucceed && !params.canNavigateUp) onActivate()
            }
        },
        onDismissRequest = {}
    )
    if (dialog == WorkingModeDialog.Deactivate) {
        var confirmation by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            text = {
                val frpNotice by vm.frpNoticeState.collectAsState()
                LaunchedEffect(Unit) {
                    // Dhizuku mode is checked inside that function
                    vm.getFrpState()
                }
                val noticeText =
                    if (frpNotice) ("\n" + stringResource(R.string.info_frp_policy_exists))
                    else ""
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.info_deactivate) + noticeText)
                    Text(stringResource(R.string.enter_confirm_to_deactivate))
                    OutlinedTextField(
                        confirmation, { confirmation = it },
                        Modifier.fillMaxWidth(),
                        singleLine = true,
                        textStyle = typography.bodyLarge
                    )
                }
            },
            confirmButton = {
                TextButton(
                    {
                        vm.deactivate()
                        dialog = WorkingModeDialog.None
                        onDeactivate()
                    },
                    enabled = confirmation == "CONFIRM",
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                CancelTextButton { dialog = WorkingModeDialog.None }
            },
            onDismissRequest = { dialog = WorkingModeDialog.None }
        )
    }
    if (
        dialog == WorkingModeDialog.DeviceOwnerCommand ||
        dialog == WorkingModeDialog.ProfileOwnerCommand ||
        dialog == WorkingModeDialog.OrgProfileCommand
    ) {
        AlertDialog(
            text = {
                SelectionContainer {
                    val text = when (dialog) {
                        WorkingModeDialog.DeviceOwnerCommand -> ACTIVATE_DEVICE_OWNER_COMMAND
                        WorkingModeDialog.ProfileOwnerCommand -> vm.getProfileOwnerCommand()
                        else -> activateOrgProfileCommand
                    }
                    Text(text)
                }
            },
            confirmButton = {
                ConfirmTextButton { dialog = WorkingModeDialog.None }
            },
            onDismissRequest = { dialog = WorkingModeDialog.None }
        )
    }
}

@Composable
private fun WorkingModeItem(text: Int, active: Boolean, onClick: () -> Unit) {
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
            if (active) Icons.Default.Check else Icons.AutoMirrored.Default.KeyboardArrowRight,
            null,
            tint = if (active) colorScheme.primary else colorScheme.onBackground
        )
    }
}
