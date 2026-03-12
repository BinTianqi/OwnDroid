package com.bintianqi.owndroid.feature.system

import android.os.Build.VERSION
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.utils.HorizontalPadding

@Composable
fun SystemOptionsScreen(vm: SystemOptionsViewModel, onNavigateUp: () -> Unit) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    val status by vm.optionsState.collectAsStateWithLifecycle()
    val globalSettingsStatus = remember { mutableStateMapOf<String, Boolean>() }
    val secureSettingsStatus = remember { mutableStateMapOf<String, Boolean>() }
    LaunchedEffect(Unit) {
        vm.getSystemOptionsStatus()
        if (privilege.device) {
            globalSettingsStatus.putAll(vm.getGlobalSettings())
            secureSettingsStatus.putAll(vm.getSecureSettings())
        }
    }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.disable_cam, status.cameraDisabled, vm::setCameraDisabled,
            R.drawable.no_photography_fill0
        )
        SwitchItem(
            R.string.disable_screen_capture, status.screenCaptureDisabled,
            vm::setScreenCaptureDisabled, R.drawable.screenshot_fill0
        )
        if (VERSION.SDK_INT >= 34 && privilege.run { device || (profile && affiliated) }) {
            SwitchItem(
                R.string.disable_status_bar, status.statusBarDisabled,
                vm::setStatusBarDisabled, R.drawable.notifications_fill0
            )
        }
        if (privilege.device || privilege.org) {
            if (VERSION.SDK_INT >= 30) {
                SwitchItem(
                    R.string.auto_time, status.autoTimeEnabled, vm::setAutoTimeEnabled,
                    R.drawable.schedule_fill0
                )
                SwitchItem(
                    R.string.auto_timezone, status.autoTimeZoneEnabled,
                    vm::setAutoTimeZoneEnabled, R.drawable.globe_fill0
                )
            } else {
                SwitchItem(
                    R.string.require_auto_time, status.autoTimeRequired,
                    vm::setAutoTimeRequired, R.drawable.schedule_fill0
                )
            }
        }
        if (!privilege.work) SwitchItem(
            R.string.master_mute,
            status.masterVolumeMuted, vm::setMasterVolumeMuted, R.drawable.volume_off_fill0
        )
        if (VERSION.SDK_INT >= 26) {
            SwitchItem(
                R.string.backup_service, icon = R.drawable.backup_fill0,
                state = status.backupServiceEnabled, onCheckedChange = vm::setBackupServiceEnabled,
                onClickBlank = { dialog = 1 })
        }
        if (VERSION.SDK_INT >= 24 && privilege.work) {
            SwitchItem(
                R.string.disable_bt_contact_share, status.btContactSharingDisabled,
                vm::setBtContactSharingDisabled, R.drawable.account_circle_fill0
            )
        }
        if (VERSION.SDK_INT >= 30 && (privilege.device || privilege.org)) {
            SwitchItem(
                R.string.common_criteria_mode, icon = R.drawable.security_fill0,
                state = status.commonCriteriaMode,
                onCheckedChange = vm::setCommonCriteriaModeEnabled,
                onClickBlank = { dialog = 2 })
        }
        if (VERSION.SDK_INT >= 31 && (privilege.device || privilege.org) && status.canDisableUsbSignal) {
            SwitchItem(
                R.string.enable_usb_signal, status.usbSignalEnabled,
                vm::setUsbSignalEnabled, R.drawable.usb_fill0
            )
        }
        SwitchItem(
            R.string.stay_on_while_plugged_in, status.stayOnWhilePluggedIn,
            vm::setStayOnWhilePluggedIn, R.drawable.mobile_phone_fill0
        )
        if (privilege.device && !privilege.dhizuku) {
            globalSettings.forEach {
                SwitchItem(it.name, globalSettingsStatus[it.setting] ?: false, { state ->
                    vm.setGlobalSetting(it.setting, state)
                    globalSettingsStatus[it.setting] = state
                }, it.icon)
            }
            secureSettings.forEach {
                SwitchItem(it.name, secureSettingsStatus[it.setting] ?: false, { state ->
                    vm.setSecureSetting(it.setting, state)
                    secureSettingsStatus[it.setting] = state
                }, it.icon)
            }
        }
        if (VERSION.SDK_INT < 34) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = HorizontalPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.status_bar), style = typography.titleMedium)
                Button({
                    vm.setStatusBarDisabled(true)
                }, Modifier.padding(horizontal = 4.dp)) {
                    Text(stringResource(R.string.disable))
                }
                Button({
                    vm.setStatusBarDisabled(false)
                }) {
                    Text(stringResource(R.string.enable))
                }
            }
        }
    }
    if (dialog != 0) AlertDialog(
        text = {
            Text(
                stringResource(
                    when (dialog) {
                        1 -> R.string.info_backup_service
                        2 -> R.string.info_common_criteria_mode
                        else -> R.string.options
                    }
                )
            )
        },
        confirmButton = {
            TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.confirm)) }
        },
        onDismissRequest = { dialog = 0 }
    )
}
