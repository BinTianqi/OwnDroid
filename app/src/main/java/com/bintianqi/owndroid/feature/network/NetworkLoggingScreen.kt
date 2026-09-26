package com.bintianqi.owndroid.feature.network

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CancelTextButton
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.ConfirmTextButton
import com.bintianqi.owndroid.ui.MasterSwitch
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.utils.HorizontalPadding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(26)
@Composable
fun NetworkLoggingScreen(
    vm: NetworkLoggingViewModel, onNavigateUp: () -> Unit
) {
    val enabled by vm.enabledState.collectAsState()
    var count by remember { mutableIntStateOf(0) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    var exporting by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        vm.getEnabled()
        vm.getCount()
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            exporting = true
            vm.exportLogs(uri) {
                exporting = false
            }
        }
    }
    MyScaffold(R.string.network_logging, onNavigateUp, 0.dp) {
        MasterSwitch(R.string.enable, enabled, vm::setEnabled)
        Text(
            stringResource(R.string.n_logs_in_total, count),
            Modifier.padding(HorizontalPadding, 5.dp)
        )
        Button(
            {
                val date = SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())
                exportLauncher.launch("network_logs_$date")
            },
            Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
            count > 0
        ) {
            Text(stringResource(R.string.export_logs))
        }
        if (count > 0) Button(
            {
                dialog = true
            },
            Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
        ) {
            Text(stringResource(R.string.delete_logs))
        }
        Spacer(Modifier.height(10.dp))
        Notes(R.string.info_network_log, HorizontalPadding)
    }
    if (exporting) CircularProgressDialog { }
    if (dialog) AlertDialog(
        text = {
            Text(stringResource(R.string.delete_logs))
        },
        confirmButton = {
            ConfirmTextButton {
                vm.deleteLogs()
                dialog = false
            }
        },
        dismissButton = {
            CancelTextButton { dialog = false }
        },
        onDismissRequest = { dialog = false }
    )
}
