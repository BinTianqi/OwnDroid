package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.WIPE_EUICC
import android.app.admin.DevicePolicyManager.WIPE_EXTERNAL_STORAGE
import android.content.Intent
import android.os.Binder
import android.os.Build.VERSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.yesOrNo
import kotlinx.serialization.Serializable

@Serializable object WorkProfile

@Composable
fun WorkProfileScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    MyScaffold(R.string.work_profile, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 30 && !privilege.org) {
            FunctionItem(R.string.org_owned_work_profile, icon = R.drawable.corporate_fare_fill0) { onNavigate(OrganizationOwnedProfile) }
        }
        if(privilege.org) {
            FunctionItem(R.string.suspend_personal_app, icon = R.drawable.block_fill0) { onNavigate(SuspendPersonalApp) }
        }
        FunctionItem(R.string.intent_filter, icon = R.drawable.filter_alt_fill0) { onNavigate(CrossProfileIntentFilter) }
        FunctionItem(R.string.delete_work_profile, icon = R.drawable.delete_forever_fill0) { onNavigate(DeleteWorkProfile) }
    }
}

data class CreateWorkProfileOptions(
    val skipEncrypt: Boolean, val offline: Boolean, val migrateAccount: Boolean,
    val accountName: String, val accountType: String, val keepAccount: Boolean
)

@Serializable object CreateWorkProfile

@Composable
fun CreateWorkProfileScreen(
    createIntent: (CreateWorkProfileOptions) -> Intent, onNavigateUp: () -> Unit
) {
    val focusMgr = LocalFocusManager.current
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    MyScaffold(R.string.create_work_profile, onNavigateUp, 0.dp) {
        var skipEncrypt by remember { mutableStateOf(false) }
        var offlineProvisioning by remember { mutableStateOf(true) }
        var migrateAccount by remember { mutableStateOf(false) }
        var migrateAccountName by remember { mutableStateOf("") }
        var migrateAccountType by remember { mutableStateOf("") }
        var keepAccount by remember { mutableStateOf(true) }
        FullWidthCheckBoxItem(R.string.migrate_account, migrateAccount) { migrateAccount = it }
        AnimatedVisibility(migrateAccount) {
            val fr = FocusRequester()
            Column(modifier = Modifier.padding(start = 10.dp)) {
                OutlinedTextField(
                    value = migrateAccountName, onValueChange = { migrateAccountName = it },
                    label = { Text(stringResource(R.string.account_name)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions { fr.requestFocus() },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)
                )
                OutlinedTextField(
                    value = migrateAccountType, onValueChange = { migrateAccountType = it },
                    label = { Text(stringResource(R.string.account_type)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPadding)
                        .focusRequester(fr)
                )
                if(VERSION.SDK_INT >= 26) {
                    FullWidthCheckBoxItem(R.string.keep_account, keepAccount) { keepAccount = it }
                }
            }
        }
        if (VERSION.SDK_INT >= 24) FullWidthCheckBoxItem(
            R.string.skip_encryption, skipEncrypt
        ) { skipEncrypt = it }
        if (VERSION.SDK_INT >= 33) FullWidthCheckBoxItem(
            R.string.offline_provisioning, offlineProvisioning
        ) { offlineProvisioning = it }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                val intent = createIntent(CreateWorkProfileOptions(
                    skipEncrypt, offlineProvisioning, migrateAccount, migrateAccountName,
                    migrateAccountType, keepAccount
                ))
                launcher.launch(intent)
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.create))
        }
    }
}

@Serializable object OrganizationOwnedProfile

@RequiresApi(30)
@Composable
fun OrganizationOwnedProfileScreen(
    onActivate: ((Boolean) -> Unit) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var activating by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }
    MyScaffold(R.string.org_owned_work_profile, onNavigateUp) {
        Button({
            activating = true
            onActivate {
                activating = false
                context.showOperationResultToast(it)
                if (it) onNavigateUp()
            }
        }) {
            Text(stringResource(R.string.shizuku))
        }
        Button({ dialog = true }) { Text(stringResource(R.string.adb_command)) }
        if(dialog) AlertDialog(
            text = {
                SelectionContainer {
                    Text(activateOrgProfileCommand)
                }
            },
            confirmButton = {
                TextButton({ dialog = false }) { Text(stringResource(R.string.confirm)) }
            },
            onDismissRequest = { dialog = false }
        )
        if (activating) CircularProgressDialog {  }
    }
}

val activateOrgProfileCommand = "dpm mark-profile-owner-on-organization-owned-device --user " +
        "${Binder.getCallingUid()/100000} com.bintianqi.owndroid/com.bintianqi.owndroid.Receiver"

@Serializable object SuspendPersonalApp

@RequiresApi(30)
@Composable
fun SuspendPersonalAppScreen(
    getSuspendedReasons: () -> Int, setSuspended: (Boolean) -> Unit, getMaxTime: () -> Long,
    setMaxTime: (Long) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var reason by remember { mutableIntStateOf(DevicePolicyManager.PERSONAL_APPS_NOT_SUSPENDED) }
    var time by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        reason = getSuspendedReasons()
        time = getMaxTime().toString()
    }
    MyScaffold(R.string.suspend_personal_app, onNavigateUp) {
        SwitchItem(R.string.suspend_personal_app, state = reason != 0,
            onCheckedChange = {
                setSuspended(it)
                reason = if (it) DevicePolicyManager.PERSONAL_APPS_SUSPENDED_EXPLICITLY
                else DevicePolicyManager.PERSONAL_APPS_NOT_SUSPENDED
            }, padding = false
        )
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.profile_max_time_off), style = typography.titleLarge)
        Text(text = stringResource(R.string.profile_max_time_out_desc))
        Text(stringResource(
            R.string.personal_app_suspended_because_timeout,
            stringResource((reason == DevicePolicyManager.PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT).yesOrNo)
        ))
        OutlinedTextField(
            value = time, onValueChange = { time=it }, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            label = { Text(stringResource(R.string.time_unit_ms)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() })
        )
        Button(
            onClick = {
                setMaxTime(time.toLong())
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = time.toLongOrNull() != null
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_profile_maximum_time_off)
    }
}

data class IntentFilterOptions(
    val action: String, val category: String, val mimeType: String,
    val direction: IntentFilterDirection
)
enum class IntentFilterDirection(val text: Int) {
    ToParent(R.string.work_to_personal), ToManaged(R.string.personal_to_work),
    Both(R.string.both_direction)
}

@Serializable object CrossProfileIntentFilter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossProfileIntentFilterScreen(
    addFilter: (IntentFilterOptions) -> Unit,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var action by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }
    var customMimeType by remember { mutableStateOf(false) }
    var mimeType by remember { mutableStateOf("") }
    var dropdown by remember { mutableStateOf(false) }
    var direction by remember { mutableStateOf(IntentFilterDirection.Both) }
    MyScaffold(R.string.intent_filter, onNavigateUp) {
        OutlinedTextField(
            value = action, onValueChange = { action = it },
            label = { Text("Action") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(customCategory, {
                customCategory = it
                category = ""
            })
            OutlinedTextField(
                category, { category = it }, Modifier.fillMaxWidth(),
                label = { Text("Category") }, enabled = customCategory
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(customMimeType, {
                customMimeType = it
                mimeType = ""
            })
            OutlinedTextField(
                mimeType, { mimeType = it }, Modifier.fillMaxWidth(),
                label = { Text("MIME type") }, enabled = customMimeType
            )
        }
        ExposedDropdownMenuBox(dropdown, { dropdown = it }, Modifier.padding(vertical = 5.dp)) {
            OutlinedTextField(
                stringResource(direction.text), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                label = { Text(stringResource(R.string.direction)) }, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdown) }
            )
            ExposedDropdownMenu(dropdown, { dropdown = false }) {
                IntentFilterDirection.entries.forEach {
                    DropdownMenuItem({ Text(stringResource(it.text)) }, {
                        direction = it
                        dropdown = false
                    })
                }
            }
        }
        Button(
            {
                addFilter(IntentFilterOptions(
                    action, category, mimeType, direction
                ))
                context.showOperationResultToast(true)
            },
            Modifier.fillMaxWidth(),
            enabled = action.isNotBlank() && (!customCategory || category.isNotBlank()) &&
                    (!customMimeType || mimeType.isNotBlank())
        ) {
            Text(stringResource(R.string.add))
        }
        Button(
            onClick = {
                Privilege.DPM.clearCrossProfileIntentFilters(Privilege.DAR)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
        ) {
            Text(stringResource(R.string.clear_cross_profile_filters))
        }
        Notes(R.string.info_cross_profile_intent_filter)
    }
}

@Serializable object DeleteWorkProfile

@Composable
fun DeleteWorkProfileScreen(
    deleteProfile: (Boolean, Int, String) -> Unit, onNavigateUp: () -> Unit
) {
    val focusMgr = LocalFocusManager.current
    var flags by remember { mutableIntStateOf(0) }
    var warning by remember { mutableStateOf(false) }
    var reason by remember { mutableStateOf("") }
    MyScaffold(R.string.delete_work_profile, onNavigateUp) {
        CheckBoxItem(R.string.wipe_external_storage, flags and WIPE_EXTERNAL_STORAGE != 0) {
            flags = flags xor WIPE_EXTERNAL_STORAGE
        }
        if(VERSION.SDK_INT >= 28) CheckBoxItem(R.string.wipe_euicc, flags and WIPE_EUICC != 0) {
            flags = flags xor WIPE_EUICC
        }
        CheckBoxItem(R.string.wipe_silently, flags and DevicePolicyManager.WIPE_SILENTLY != 0) {
            flags = flags xor DevicePolicyManager.WIPE_SILENTLY
            reason = ""
        }
        if (VERSION.SDK_INT >= 28) OutlinedTextField(
            value = reason, onValueChange = { reason = it },
            label = { Text(stringResource(R.string.reason)) },
            enabled = flags and DevicePolicyManager.WIPE_SILENTLY == 0,
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                warning = true
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }
    }
    if (warning) {
        AlertDialog(
            title = {
                Text(text = stringResource(R.string.warning), color = colorScheme.error)
            },
            text = {
                Text(text = stringResource(R.string.wipe_work_profile_warning), color = colorScheme.error)
            },
            onDismissRequest = { warning = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        deleteProfile(false, flags, reason)
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { warning = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
