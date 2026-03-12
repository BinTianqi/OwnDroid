package com.bintianqi.owndroid.feature.system

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.utils.HorizontalPadding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(24)
@Composable
fun SecurityLoggingScreen(
    vm: SecurityLoggingViewModel, onNavigateUp: () -> Unit
) {
    val enabled by vm.enabledState.collectAsState()
    val logsCount by vm.countState.collectAsState()
    val exporting by vm.exportingState.collectAsState()
    var dialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        vm.getEnabled()
        vm.getCount()
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) vm.exportLogs(it)
    }
    val exportPRLogsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) vm.exportPreRebootSecurityLogs(it)
    }
    MyScaffold(R.string.security_logging, onNavigateUp, 0.dp) {
        SwitchItem(R.string.enable, enabled, vm::setEnabled)
        Text(
            stringResource(R.string.n_logs_in_total, logsCount),
            Modifier.padding(HorizontalPadding)
        )
        Button(
            {
                val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                exportLauncher.launch("security_logs_$date")
            },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding),
            logsCount > 0
        ) {
            Text(stringResource(R.string.export_logs))
        }
        if (logsCount > 0) FilledTonalButton(
            { dialog = true },
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 4.dp)
        ) {
            Text(stringResource(R.string.delete_logs))
        }
        Notes(R.string.info_security_log, HorizontalPadding)
        Button(
            {
                vm.getPreRebootSecurityLogs {
                    val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                    exportPRLogsLauncher.launch("pre_reboot_security_logs_$date")
                }
            },
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 15.dp)
        ) {
            Text(stringResource(R.string.pre_reboot_security_logs))
        }
        Notes(R.string.info_pre_reboot_security_log, HorizontalPadding)
    }
    if (exporting) CircularProgressDialog {}
    if (dialog) AlertDialog(
        text = { Text(stringResource(R.string.delete_logs)) },
        confirmButton = {
            TextButton({
                vm.deleteLogs()
                dialog = false
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({ dialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = false }
    )
}
