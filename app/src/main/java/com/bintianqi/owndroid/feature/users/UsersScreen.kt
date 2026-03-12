package com.bintianqi.owndroid.feature.users

import android.app.admin.DevicePolicyManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build.VERSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoItem
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.formatDate
import com.bintianqi.owndroid.utils.popToast
import com.bintianqi.owndroid.utils.uriToStream
import com.bintianqi.owndroid.utils.yesOrNo

@Composable
fun UsersScreen(vm: UsersViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit) {
    val context = LocalContext.current
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    // 1: logout
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    MyScaffold(R.string.users, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 28 && privilege.profile && privilege.affiliated) {
            FunctionItem(R.string.logout, icon = R.drawable.logout_fill0) { dialog = 1 }
        }
        FunctionItem(R.string.user_info, icon = R.drawable.person_fill0) {
            onNavigate(Destination.UserInfo)
        }
        if (VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) {
                onNavigate(Destination.UsersOptions)
            }
        }
        if (privilege.device) {
            FunctionItem(R.string.user_operation, icon = R.drawable.sync_alt_fill0) {
                onNavigate(Destination.UserOperation)
            }
        }
        if (VERSION.SDK_INT >= 24 && privilege.device) {
            FunctionItem(R.string.create_user, icon = R.drawable.person_add_fill0) {
                onNavigate(Destination.CreateUser)
            }
        }
        FunctionItem(R.string.change_username, icon = R.drawable.edit_fill0) {
            onNavigate(Destination.ChangeUsername)
        }
        var changeUserIconDialog by remember { mutableStateOf(false) }
        var bitmap: Bitmap? by remember { mutableStateOf(null) }
        val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
            if (it != null) uriToStream(context, it) { stream ->
                bitmap = BitmapFactory.decodeStream(stream)
                if (bitmap != null) changeUserIconDialog = true
            }
        }
        FunctionItem(R.string.change_user_icon, icon = R.drawable.account_circle_fill0) {
            context.popToast(R.string.select_an_image)
            launcher.launch("image/*")
        }
        if (changeUserIconDialog) ChangeUserIconDialog(
            bitmap!!, {
                vm.setUserIcon(bitmap!!)
                changeUserIconDialog = false
            }) { changeUserIconDialog = false }
        if (VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.user_session_msg, icon = R.drawable.notifications_fill0) {
                onNavigate(Destination.UserSessionMessage)
            }
        }
        if (VERSION.SDK_INT >= 26) {
            FunctionItem(R.string.affiliation_id, icon = R.drawable.id_card_fill0) {
                onNavigate(Destination.AffiliationId)
            }
        }
    }
    if (VERSION.SDK_INT >= 28 && dialog == 1) AlertDialog(
        title = { Text(stringResource(R.string.logout)) },
        text = {
            Text(stringResource(R.string.info_logout))
        },
        confirmButton = {
            TextButton({
                vm.logout()
                dialog = 0
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({ dialog = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@Composable
fun UsersOptionsScreen(
    vm: UsersViewModel, onNavigateUp: () -> Unit
) {
    val logoutEnabled by vm.logoutEnabledState.collectAsState()
    LaunchedEffect(Unit) {
        if (VERSION.SDK_INT >= 28) vm.getLogoutEnabled()
    }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 28) {
            SwitchItem(R.string.enable_logout, logoutEnabled, vm::setLogoutEnabled)
        }
    }
}

@Composable
fun UserInfoScreen(vm: UsersViewModel, onNavigateUp: () -> Unit) {
    val info by vm.userInformationState.collectAsState()
    var infoDialog by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        vm.getUserInformation()
    }
    MyScaffold(R.string.user_info, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 24) InfoItem(R.string.support_multiuser, info.multiUser.yesOrNo)
        if (VERSION.SDK_INT >= 31) InfoItem(
            R.string.headless_system_user_mode, info.headless.yesOrNo, true
        ) { infoDialog = 1 }
        Spacer(Modifier.height(8.dp))
        InfoItem(R.string.system_user, info.system.yesOrNo)
        if (VERSION.SDK_INT >= 34) InfoItem(R.string.admin_user, info.admin.yesOrNo)
        if (VERSION.SDK_INT >= 25) InfoItem(R.string.demo_user, info.demo.yesOrNo)
        if (info.time != 0L) InfoItem(R.string.creation_time, formatDate(info.time))

        if (VERSION.SDK_INT >= 28) {
            InfoItem(R.string.logout_enabled, info.logout.yesOrNo)
            InfoItem(R.string.ephemeral_user, info.ephemeral.yesOrNo)
            InfoItem(R.string.affiliated_user, info.affiliated.yesOrNo)
        }
        InfoItem(R.string.user_id, (Binder.getCallingUid() / 100000).toString())
        InfoItem(R.string.user_serial_number, info.serial.toString())
    }
    if (infoDialog != 0) AlertDialog(
        text = { Text(stringResource(R.string.info_headless_system_user_mode)) },
        confirmButton = {
            TextButton(onClick = { infoDialog = 0 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { infoDialog = 0 }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserOperationScreen(
    vm: UsersViewModel, onNavigateUp: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    val focusMgr = LocalFocusManager.current
    var useUserId by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    var menu by remember { mutableStateOf(false) }
    val legalInput = input.toIntOrNull() != null
    val identifiers by vm.secondaryUsersState.collectAsState()
    @Composable
    fun CreateShortcutIcon(type: UserOperationType) {
        FilledTonalIconButton({
            vm.createUserOperationShortcut(type, input.toInt(), useUserId)
        }, enabled = legalInput) {
            Icon(painterResource(R.drawable.open_in_new), null)
        }
    }
    LaunchedEffect(Unit) {
        if (VERSION.SDK_INT >= 28) vm.getUserIdentifiers()
    }
    MyScaffold(R.string.user_operation, onNavigateUp) {
        if (VERSION.SDK_INT >= 24) SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                !useUserId,
                {
                    useUserId = false
                    input = ""
                },
                SegmentedButtonDefaults.itemShape(0, 2)
            ) {
                Text(stringResource(R.string.serial_number))
            }
            SegmentedButton(
                useUserId,
                {
                    useUserId = true
                    input = ""
                },
                SegmentedButtonDefaults.itemShape(1, 2)
            ) {
                Text(stringResource(R.string.user_id))
            }
        }
        ExposedDropdownMenuBox(menu, { menu = it }) {
            OutlinedTextField(
                input, { input = it },
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
                    .padding(top = 4.dp, bottom = 8.dp),
                label = {
                    Text(
                        stringResource(if (useUserId) R.string.user_id else R.string.serial_number)
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu)
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                )
            )
            ExposedDropdownMenu(menu, { menu = false }) {
                if (identifiers.isEmpty()) {
                    DropdownMenuItem(
                        { Text(stringResource(R.string.no_secondary_users)) }, {}
                    )
                } else {
                    identifiers.forEach {
                        val text = (if (useUserId) it.id else it.serial).toString()
                        DropdownMenuItem(
                            { Text(text) },
                            {
                                input = text
                                menu = false
                            }
                        )
                    }
                }
            }
        }
        if (VERSION.SDK_INT >= 28) Row {
            Button(
                {
                    focusMgr.clearFocus()
                    vm.doUserOperation(UserOperationType.Start, input.toInt(), useUserId)
                },
                Modifier.weight(1F),
                legalInput
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.start_in_background))
            }
            CreateShortcutIcon(UserOperationType.Start)
        }
        Row {
            Button(
                {
                    focusMgr.clearFocus()
                    vm.doUserOperation(UserOperationType.Switch, input.toInt(), useUserId)
                },
                Modifier.weight(1F),
                legalInput
            ) {
                Icon(painterResource(R.drawable.sync_alt_fill0), null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.user_operation_switch))
            }
            CreateShortcutIcon(UserOperationType.Switch)
        }
        if (VERSION.SDK_INT >= 28) Row {
            Button(
                {
                    focusMgr.clearFocus()
                    vm.doUserOperation(UserOperationType.Stop, input.toInt(), useUserId)
                },
                Modifier.weight(1F),
                legalInput
            ) {
                Icon(Icons.Default.Close, null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.stop))
            }
            CreateShortcutIcon(UserOperationType.Stop)
        }
        Button(
            {
                focusMgr.clearFocus()
                dialog = true
            },
            Modifier.fillMaxWidth(),
            legalInput
        ) {
            Icon(Icons.Default.Delete, null, Modifier.padding(end = 4.dp))
            Text(stringResource(R.string.delete))
        }
    }
    if (dialog) AlertDialog(
        text = {
            Text(stringResource(R.string.delete_user_confirmation, input))
        },
        confirmButton = {
            TextButton({
                vm.doUserOperation(UserOperationType.Delete, input.toInt(), useUserId)
                dialog = false
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({ dialog = false }) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { dialog = false }
    )
}

@RequiresApi(24)
@Composable
fun CreateUserScreen(
    vm: UsersViewModel, onNavigateUp: () -> Unit
) {
    var result by remember { mutableStateOf<CreateUserResult?>(null) }
    val focusMgr = LocalFocusManager.current
    var userName by rememberSaveable { mutableStateOf("") }
    var creating by rememberSaveable { mutableStateOf(false) }
    var flags by rememberSaveable { mutableIntStateOf(0) }
    MyScaffold(R.string.create_user, onNavigateUp, 0.dp) {
        OutlinedTextField(
            userName, { userName = it },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding),
            label = { Text(stringResource(R.string.username)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        FullWidthCheckBoxItem(
            R.string.create_user_skip_wizard,
            flags and DevicePolicyManager.SKIP_SETUP_WIZARD != 0
        ) { flags = flags xor DevicePolicyManager.SKIP_SETUP_WIZARD }
        if (VERSION.SDK_INT >= 28) {
            FullWidthCheckBoxItem(
                R.string.create_user_ephemeral_user,
                flags and DevicePolicyManager.MAKE_USER_EPHEMERAL != 0
            ) { flags = flags xor DevicePolicyManager.MAKE_USER_EPHEMERAL }
            FullWidthCheckBoxItem(
                R.string.create_user_enable_all_system_app,
                flags and DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED != 0
            ) { flags = flags xor DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            {
                focusMgr.clearFocus()
                creating = true
                vm.createUser(userName, flags) {
                    creating = false
                    result = it
                }
            },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.create))
        }
        if (result != null) AlertDialog(
            text = {
                Column {
                    Text(stringResource(result!!.message))
                    if (result?.serial != -1L) {
                        Text(stringResource(R.string.serial_number) + ": " + result!!.serial)
                    }
                }
            },
            confirmButton = {
                TextButton({ result = null }) { Text(stringResource(R.string.confirm)) }
            },
            onDismissRequest = { result = null }
        )
        if (creating) CircularProgressDialog { }
    }
}

@RequiresApi(26)
@Composable
fun AffiliationIdScreen(
    vm: UsersViewModel, onNavigateUp: () -> Unit
) {
    var input by rememberSaveable { mutableStateOf("") }
    val list by vm.affiliationIdsState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getAffiliationIds()
    }
    MyScaffold(R.string.affiliation_id, onNavigateUp) {
        Column(Modifier.animateContentSize()) {
            if (list.isEmpty()) Text(stringResource(R.string.none))
            for (i in list) {
                ListItem(i) { vm.setAffiliationId(i, false) }
            }
        }
        OutlinedTextField(
            input, { input = it },
            Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp),
            label = { Text("ID") },
            trailingIcon = {
                IconButton(
                    {
                        vm.setAffiliationId(input, true)
                        input = ""
                    },
                    enabled = input.isNotEmpty()
                ) {
                    Icon(Icons.Default.Add, stringResource(R.string.add))
                }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Button(vm::applyAffiliationIds) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_affiliation_id)
    }
}

@Composable
fun ChangeUsernameScreen(vm: UsersViewModel, onNavigateUp: () -> Unit) {
    var inputUsername by rememberSaveable { mutableStateOf("") }
    MyScaffold(R.string.change_username, onNavigateUp) {
        OutlinedTextField(
            inputUsername, { inputUsername = it },
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.username)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            {
                vm.setProfileName(inputUsername)
            },
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@RequiresApi(28)
@Composable
fun UserSessionMessageScreen(
    vm: UsersViewModel, onNavigateUp: () -> Unit
) {
    val startMessage by vm.startSessionMessageState.collectAsState()
    val endMessage by vm.endSessionMessageState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getSessionMessages()
    }
    MyScaffold(R.string.user_session_msg, onNavigateUp) {
        OutlinedTextField(
            startMessage, vm::setStartSessionMessage,
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.start_user_session_msg)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            endMessage, vm::setEndSessionMessage,
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            label = { Text(stringResource(R.string.end_user_session_msg)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Button(
            vm::applySessionMessages,
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
private fun ChangeUserIconDialog(bitmap: Bitmap, onSet: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        title = { Text(stringResource(R.string.change_user_icon)) },
        text = {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    bitmap.asImageBitmap(), null,
                    Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        },
        confirmButton = {
            TextButton(onSet) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClose) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = onClose
    )
}
