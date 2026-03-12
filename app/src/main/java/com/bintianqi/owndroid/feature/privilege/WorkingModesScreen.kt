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
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.ACTIVATE_DEVICE_OWNER_COMMAND
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.activateOrgProfileCommand
import com.bintianqi.owndroid.utils.adaptiveInsets
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WorkModesScreen(
    vm: WorkingModesViewModel, params: Destination.WorkingModes, onNavigateUp: () -> Unit,
    onActivate: () -> Unit, onDeactivate: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.ps.collectAsStateWithLifecycle()
    // 0: none, 1: device owner, 2: circular progress indicator, 3: result, 4: deactivate
    // 5: command, 6: org profile, 7: org profile command
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
                                    dialog = 4
                                },
                                leadingIcon = { Icon(Icons.Default.Close, null) }
                            )
                            if (VERSION.SDK_INT >= 26) DropdownMenuItem(
                                { Text(stringResource(R.string.delegated_admins)) },
                                {
                                    expanded = false
                                    onNavigate(Destination.DelegatedAdmins)
                                },
                                leadingIcon = {
                                    Icon(
                                        painterResource(R.drawable.admin_panel_settings_fill0), null
                                    )
                                }
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
        fun handleResult(succeeded: Boolean, output: String?) {
            operationSucceed = succeeded
            resultText = output ?: ""
            dialog = 3
        }
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
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
            if (
                privilege.work || (VERSION.SDK_INT < 24 || vm.isCreatingWorkProfileAllowed())
            ) {
                WorkingModeItem(R.string.work_profile, privilege.work) {
                    if (!privilege.work) onNavigate(Destination.CreateWorkProfile)
                }
            }
            if (privilege.work) {
                WorkingModeItem(R.string.org_owned_work_profile, privilege.org) {
                    if (!privilege.org) dialog = 6
                }
            }
            if (privilege.activated && !privilege.dhizuku) Row(
                Modifier
                    .padding(top = 20.dp)
                    .fillMaxWidth()
                    .clickable { onNavigate(Destination.DhizukuServerSettings) }
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painterResource(R.drawable.dhizuku_icon), null,
                    Modifier
                        .padding(8.dp)
                        .size(28.dp)
                )
                Text(stringResource(R.string.dhizuku_server), style = typography.titleLarge)
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
        if (dialog == 1) AlertDialog(
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
        if (dialog == 2) CircularProgressDialog { }
        if (dialog == 3) AlertDialog(
            title = {
                Text(
                    stringResource(if (operationSucceed) R.string.succeeded else R.string.failed)
                )
            },
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
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
        if (dialog == 4) AlertDialog(
            title = { Text(stringResource(R.string.deactivate)) },
            text = { Text(stringResource(R.string.info_deactivate)) },
            confirmButton = {
                var time by remember { mutableIntStateOf(if (privilege.dhizuku) 0 else 3) }
                if (!privilege.dhizuku) LaunchedEffect(Unit) {
                    for (i in (0..2).reversed()) {
                        delay(1000)
                        time = i
                    }
                }
                val timeText = if (time != 0) " (${time}s)" else ""
                TextButton(
                    {
                        vm.deactivate()
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
        if (dialog == 5) AlertDialog(
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
        if (dialog == 6) AlertDialog(
            text = {
                Column {
                    Button({
                        dialog = 2
                        vm.activateOrgProfileByShizuku { dialog = 0 }
                    }) {
                        Text(stringResource(R.string.shizuku))
                    }
                    Button({ dialog = 7 }) {
                        Text(stringResource(R.string.adb_command))
                    }
                }
            },
            confirmButton = {
                TextButton({ dialog = 0 }) { Text(stringResource(R.string.cancel)) }
            },
            onDismissRequest = { dialog = 0 }
        )
        if (dialog == 7) AlertDialog(
            text = {
                SelectionContainer {
                    Text(activateOrgProfileCommand)
                }
            },
            confirmButton = {
                TextButton({ dialog = 0 }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { dialog = 0 }
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
