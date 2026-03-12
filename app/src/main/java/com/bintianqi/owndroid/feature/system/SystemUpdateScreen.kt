package com.bintianqi.owndroid.feature.system

import android.net.Uri
import android.os.Build.VERSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.ErrorDialog
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.formatDate
import com.bintianqi.owndroid.utils.yesOrNo

@Composable
fun SystemUpdateScreen(
    vm: SystemUpdateViewModel, onNavigateUp: () -> Unit
) {
    val policy by vm.policyState.collectAsState()
    val pendingUpdate by vm.pendingUpdateState.collectAsState()
    var uri by remember { mutableStateOf<Uri?>(null) }
    var installing by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    val getFileLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        uri = it
    }
    LaunchedEffect(Unit) {
        vm.getPolicy()
        if (VERSION.SDK_INT >= 26) vm.getPendingUpdate()
    }
    MySmallTitleScaffold(R.string.system_update, onNavigateUp, 0.dp) {
        Text(
            stringResource(R.string.system_update_policy),
            Modifier.padding(start = HorizontalPadding, top = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        SystemUpdatePolicyType.entries.forEach {
            FullWidthRadioButtonItem(it.text, policy.type == it) {
                vm.setPolicy(policy.copy(type = it))
            }
        }
        AnimatedVisibility(policy.type == SystemUpdatePolicyType.Windowed) {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    Arrangement.SpaceBetween
                ) {
                    OutlinedTextField(
                        policy.start, { vm.setPolicy(policy.copy(start = it)) },
                        Modifier.fillMaxWidth(0.49F),
                        label = { Text(stringResource(R.string.start_time)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        policy.end, { vm.setPolicy(policy.copy(end = it)) },
                        Modifier.fillMaxWidth(0.96F),
                        label = { Text(stringResource(R.string.end_time)) },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                        )
                    )
                }
                Text(
                    stringResource(R.string.minutes_in_one_day),
                    color = colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        Button(
            vm::applyPolicy,
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = HorizontalPadding),
            policy.type != SystemUpdatePolicyType.Windowed ||
                    listOf(policy.start, policy.end).map { it.toIntOrNull() }
                        .all { it != null && it <= 1440 }
        ) {
            Text(stringResource(R.string.apply))
        }
        if (VERSION.SDK_INT >= 26) {
            Column(Modifier.padding(HorizontalPadding)) {
                if (pendingUpdate.exists) {
                    Text(
                        stringResource(
                            R.string.update_received_time, formatDate(pendingUpdate.time)
                        )
                    )
                    Text(
                        stringResource(
                            R.string.is_security_patch,
                            stringResource(pendingUpdate.securityPatch.yesOrNo)
                        )
                    )
                } else {
                    Text(text = stringResource(R.string.no_system_update))
                }
            }
        }
        if (VERSION.SDK_INT >= 29) {
            Text(
                stringResource(R.string.install_system_update),
                Modifier.padding(start = HorizontalPadding),
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(10.dp))
            Button(
                {
                    getFileLauncher.launch("application/zip")
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)
            ) {
                Text(stringResource(R.string.select_ota_package))
            }
            Button(
                {
                    installing = true
                    vm.installUpdate(uri!!) { message ->
                        errorMessage = message
                    }
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
                uri != null && !installing
            ) {
                Text(stringResource(R.string.install_system_update))
            }
            Spacer(Modifier.padding(vertical = 10.dp))
            Notes(R.string.auto_reboot_after_install_succeed, HorizontalPadding)
        }
        Spacer(Modifier.height(BottomPadding))
    }
    ErrorDialog(errorMessage) { errorMessage = null }
}
