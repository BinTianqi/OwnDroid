package com.bintianqi.owndroid.feature.work_profile

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.WIPE_EUICC
import android.app.admin.DevicePolicyManager.WIPE_EXTERNAL_STORAGE
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.yesOrNo
import kotlinx.coroutines.delay

@Composable
fun WorkProfileScreen(
    vm: WorkProfileViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    MyScaffold(R.string.work_profile, onNavigateUp, 0.dp) {
        if (privilege.org) {
            FunctionItem(R.string.suspend_personal_app, icon = R.drawable.block_fill0) {
                onNavigate(Destination.SuspendPersonalApp)
            }
        }
        FunctionItem(R.string.intent_filter, icon = R.drawable.filter_alt_fill0) {
            onNavigate(Destination.CrossProfileIntentFilter)
        }
        FunctionItem(R.string.delete_work_profile, icon = R.drawable.delete_forever_fill0) {
            onNavigate(Destination.DeleteWorkProfile)
        }
    }
}

@RequiresApi(30)
@Composable
fun SuspendPersonalAppScreen(
    vm: WorkProfileViewModel, onNavigateUp: () -> Unit
) {
    val reason by vm.personalAppSuspendedState.collectAsState()
    val time by vm.profileMaxTimeOffState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getPersonalAppsSuspendedReason()
        vm.getProfileMaxTimeOff()
    }
    MyScaffold(R.string.suspend_personal_app, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.suspend_personal_app, reason != 0, vm::setPersonalAppsSuspended
        )
        HorizontalDivider()
        Spacer(Modifier.padding(HorizontalPadding, 10.dp))
        Text(
            stringResource(R.string.profile_max_time_off),
            Modifier.padding(horizontal = HorizontalPadding), style = typography.titleLarge
        )
        Text(
            stringResource(R.string.profile_max_time_out_desc),
            Modifier.padding(horizontal = HorizontalPadding)
        )
        Text(
            stringResource(
                R.string.personal_app_suspended_because_timeout,
                stringResource(
                    (reason == DevicePolicyManager.PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT).yesOrNo
                )
            ),
            Modifier.padding(horizontal = HorizontalPadding)
        )
        OutlinedTextField(
            time, vm::setProfileMaxTimeOff,
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 2.dp),
            label = { Text(stringResource(R.string.time_unit_ms)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Button(
            vm::applyProfileMaxTimeOff,
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding),
            time.toLongOrNull() != null
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_profile_maximum_time_off, HorizontalPadding)
    }
}

@Composable
fun DeleteWorkProfileScreen(
    vm: WorkProfileViewModel, onNavigateUp: () -> Unit
) {
    var flags by remember { mutableIntStateOf(0) }
    var warning by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    MyScaffold(R.string.delete_work_profile, onNavigateUp) {
        CheckBoxItem(R.string.wipe_external_storage, flags and WIPE_EXTERNAL_STORAGE != 0) {
            flags = flags xor WIPE_EXTERNAL_STORAGE
        }
        if (VERSION.SDK_INT >= 28) CheckBoxItem(R.string.wipe_euicc, flags and WIPE_EUICC != 0) {
            flags = flags xor WIPE_EUICC
        }
        CheckBoxItem(R.string.wipe_silently, flags and DevicePolicyManager.WIPE_SILENTLY != 0) {
            flags = flags xor DevicePolicyManager.WIPE_SILENTLY
            reason = ""
        }
        if (VERSION.SDK_INT >= 28) OutlinedTextField(
            reason, { reason = it },
            Modifier
                .fillMaxWidth()
                .padding(vertical = 3.dp),
            label = { Text(stringResource(R.string.reason)) },
            enabled = flags and DevicePolicyManager.WIPE_SILENTLY == 0
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            {
                warning = true
            },
            Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.error, contentColor = colorScheme.onError
            )
        ) {
            Text(stringResource(R.string.delete))
        }
    }
    if (warning) {
        AlertDialog(
            title = {
                Text(stringResource(R.string.warning))
            },
            text = {
                Text(stringResource(R.string.wipe_work_profile_warning))
            },
            onDismissRequest = { warning = false },
            confirmButton = {
                var timer by remember { mutableIntStateOf(3) }
                LaunchedEffect(Unit) {
                    repeat(3) {
                        delay(1000)
                        timer -= 1
                    }
                }
                val timerText = if (timer > 0) " (${timer}s)" else ""
                TextButton(
                    {
                        vm.deleteProfile(flags, reason)
                    },
                    enabled = timer == 0,
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm) + timerText)
                }
            },
            dismissButton = {
                TextButton({ warning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
