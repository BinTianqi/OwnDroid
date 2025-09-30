package com.bintianqi.owndroid.dpm

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.formatTime
import com.bintianqi.owndroid.popToast
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoItem
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.uriToStream
import com.bintianqi.owndroid.yesOrNo
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable

@Serializable object Users

@Composable
fun UsersScreen(vm: MyViewModel, onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    /** 1: secondary users, 2: logout*/
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.users, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 28 && privilege.profile && privilege.affiliated) {
            FunctionItem(R.string.logout, icon = R.drawable.logout_fill0) { dialog = 2 }
        }
        FunctionItem(R.string.user_info, icon = R.drawable.person_fill0) { onNavigate(UserInfo) }
        if(VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.secondary_users, icon = R.drawable.list_fill0) { dialog = 1 }
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(UsersOptions) }
        }
        if(privilege.device) {
            FunctionItem(R.string.user_operation, icon = R.drawable.sync_alt_fill0) { onNavigate(UserOperation) }
        }
        if(VERSION.SDK_INT >= 24 && privilege.device) {
            FunctionItem(R.string.create_user, icon = R.drawable.person_add_fill0) { onNavigate(CreateUser) }
        }
        FunctionItem(R.string.change_username, icon = R.drawable.edit_fill0) { onNavigate(ChangeUsername) }
        if(VERSION.SDK_INT >= 23) {
            var changeUserIconDialog by remember { mutableStateOf(false) }
            var bitmap: Bitmap? by remember { mutableStateOf(null) }
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if(it != null) uriToStream(context, it) { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                    if(bitmap != null) changeUserIconDialog = true
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
        }
        if(VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.user_session_msg, icon = R.drawable.notifications_fill0) { onNavigate(UserSessionMessage) }
        }
        if(VERSION.SDK_INT >= 26) {
            FunctionItem(R.string.affiliation_id, icon = R.drawable.id_card_fill0) { onNavigate(AffiliationId) }
        }
    }
    if (VERSION.SDK_INT >= 28 && dialog == 1) AlertDialog(
        title = { Text(stringResource(R.string.secondary_users)) },
        text = {
            val list = vm.getSecondaryUsers()
            val text = if (list.isEmpty()) {
                stringResource(R.string.no_secondary_users)
            } else {
                "(" + stringResource(R.string.serial_number) + ")\n" + list.joinToString("\n")
            }
            Text(text)
        },
        confirmButton = {
            TextButton({ dialog = 0 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { dialog = 0 }
    )
    if (VERSION.SDK_INT >= 28 && dialog == 2) AlertDialog(
        title = { Text(stringResource(R.string.logout)) },
        text = {
            Text(stringResource(R.string.info_logout))
        },
        confirmButton = {
            TextButton({
                context.popToast(vm.logoutUser())
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

@Serializable object UsersOptions

@Composable
fun UsersOptionsScreen(
    getLogoutEnabled: () -> Boolean, setLogoutEnabled: (Boolean) -> Unit, onNavigateUp: () -> Unit
) {
    var logoutEnabled by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { logoutEnabled = getLogoutEnabled() }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 28) {
            SwitchItem(R.string.enable_logout, logoutEnabled, {
                setLogoutEnabled(it)
                logoutEnabled = it
            })
        }
    }
}

data class UserInformation(
    val multiUser: Boolean = false, val headless: Boolean = false, val system: Boolean = false,
    val admin: Boolean = false, val demo: Boolean = false, val time: Long = 0,
    val logout: Boolean = false, val ephemeral: Boolean = false, val affiliated: Boolean = false,
    val serial: Long = 0
)

@Serializable object UserInfo

@Composable
fun UserInfoScreen(getInfo: () -> UserInformation, onNavigateUp: () -> Unit) {
    var info by remember { mutableStateOf(UserInformation()) }
    var infoDialog by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        info = getInfo()
    }
    MyScaffold(R.string.user_info, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 24) InfoItem(R.string.support_multiuser, info.multiUser.yesOrNo)
        if (VERSION.SDK_INT >= 31) InfoItem(R.string.headless_system_user_mode, info.headless.yesOrNo, true) { infoDialog = 1 }
        Spacer(Modifier.height(8.dp))
        if (VERSION.SDK_INT >= 23) InfoItem(R.string.system_user, info.system.yesOrNo)
        if (VERSION.SDK_INT >= 34) InfoItem(R.string.admin_user, info.admin.yesOrNo)
        if (VERSION.SDK_INT >= 25) InfoItem(R.string.demo_user, info.demo.yesOrNo)
        if (info.time != 0L) InfoItem(R.string.creation_time, formatTime(info.time))

        if (VERSION.SDK_INT >= 28) {
            InfoItem(R.string.logout_enabled, info.logout.yesOrNo)
            InfoItem(R.string.ephemeral_user, info.ephemeral.yesOrNo)
            InfoItem(R.string.affiliated_user, info.affiliated.yesOrNo)
        }
        InfoItem(R.string.user_id, (Binder.getCallingUid() / 100000).toString())
        InfoItem(R.string.user_serial_number, info.serial.toString())
    }
    if(infoDialog != 0) AlertDialog(
        text = { Text(stringResource(R.string.info_headless_system_user_mode)) },
        confirmButton = {
            TextButton(onClick = { infoDialog = 0 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { infoDialog = 0 }
    )
}

@Serializable object UserOperation

@Composable
fun UserOperationScreen(
    startUser: (Int, Boolean) -> Int, switchUser: (Int, Boolean) -> Boolean,
    stopUser: (Int, Boolean) -> Int, deleteUser: (Int, Boolean) -> Boolean, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var input by remember { mutableStateOf("") }
    val focusMgr = LocalFocusManager.current
    var useUserId by remember { mutableStateOf(false) }
    var dialog by remember { mutableStateOf(false) }
    val legalInput = input.toIntOrNull() != null
    MyScaffold(R.string.user_operation, onNavigateUp) {
        if(VERSION.SDK_INT >= 24) SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(!useUserId, { useUserId = false }, SegmentedButtonDefaults.itemShape(0, 2)) {
                Text(stringResource(R.string.serial_number))
            }
            SegmentedButton(useUserId, { useUserId = true }, SegmentedButtonDefaults.itemShape(1, 2)) {
                Text(stringResource(R.string.user_id))
            }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text(stringResource(if(useUserId) R.string.user_id else R.string.serial_number)) },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp, bottom = 8.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        if(VERSION.SDK_INT >= 28) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    context.popToast(startUser(input.toInt(), useUserId))
                },
                enabled = legalInput,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.PlayArrow, null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.start_in_background))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                if (switchUser(input.toInt(), useUserId)) context.popToast(R.string.user_not_exist)
            },
            enabled = legalInput,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(painterResource(R.drawable.sync_alt_fill0), null, Modifier.padding(end = 4.dp))
            Text(stringResource(R.string.user_operation_switch))
        }
        if(VERSION.SDK_INT >= 28) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    context.popToast(stopUser(input.toInt(), useUserId))
                },
                enabled = legalInput,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Close, null, Modifier.padding(end = 4.dp))
                Text(stringResource(R.string.stop))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                dialog = true
            },
            enabled = legalInput,
            modifier = Modifier.fillMaxWidth()
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
                context.showOperationResultToast(deleteUser(input.toInt(), useUserId))
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

data class CreateUserResult(val message: Int, val serial: Long = -1)

@Serializable object CreateUser

@RequiresApi(24)
@Composable
fun CreateUserScreen(
    createUser: (String, Int, (CreateUserResult) -> Unit) -> Unit, onNavigateUp: () -> Unit
) {
    var result by remember { mutableStateOf<CreateUserResult?>(null) }
    val focusMgr = LocalFocusManager.current
    var userName by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }
    var flags by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.create_user, onNavigateUp, 0.dp) {
        OutlinedTextField(
            userName, { userName= it }, Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
            label = { Text(stringResource(R.string.username)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        FullWidthCheckBoxItem(
            R.string.create_user_skip_wizard,
            flags and DevicePolicyManager.SKIP_SETUP_WIZARD != 0
        ) { flags = flags xor DevicePolicyManager.SKIP_SETUP_WIZARD }
        if(VERSION.SDK_INT >= 28) {
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
            onClick = {
                focusMgr.clearFocus()
                creating = true
                createUser(userName, flags) {
                    creating = false
                    result = it
                }
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)
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
        if (creating) CircularProgressDialog {  }
    }
}

@Serializable object AffiliationId

@RequiresApi(26)
@Composable
fun AffiliationIdScreen(
    affiliationIds: StateFlow<List<String>>, getIds: () -> Unit, setId: (String, Boolean) -> Unit,
    onNavigateUp: () -> Unit
) {
    val focusMgr = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    val list by affiliationIds.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { getIds() }
    MyScaffold(R.string.affiliation_id, onNavigateUp) {
        Column(modifier = Modifier.animateContentSize()) {
            if (list.isEmpty()) Text(stringResource(R.string.none))
            for (i in list) {
                ListItem(i) { setId(i, false) }
            }
        }
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("ID") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        setId(input, true)
                        input = ""
                    },
                    enabled = input.isNotEmpty()
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Notes(R.string.info_affiliation_id)
    }
}

@Serializable object ChangeUsername

@Composable
fun ChangeUsernameScreen(setName: (String) -> Unit, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var inputUsername by remember { mutableStateOf("") }
    MyScaffold(R.string.change_username, onNavigateUp) {
        OutlinedTextField(
            value = inputUsername,
            onValueChange = { inputUsername= it },
            label = { Text(stringResource(R.string.username)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                setName(inputUsername)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Serializable object UserSessionMessage

@RequiresApi(28)
@Composable
fun UserSessionMessageScreen(
    getMessages: () -> Pair<String, String>, setStartMessage: (String?) -> Unit,
    setEndMessage: (String?) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val messages = getMessages()
        start = messages.first
        end = messages.second
    }
    MyScaffold(R.string.user_session_msg, onNavigateUp) {
        OutlinedTextField(
            value = start,
            onValueChange = { start= it },
            label = { Text(stringResource(R.string.start_user_session_msg)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    setStartMessage(start)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    setStartMessage(null)
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.reset))
            }
        }
        Spacer(Modifier.padding(vertical = 8.dp))
        OutlinedTextField(
            value = end,
            onValueChange = { end= it },
            label = { Text(stringResource(R.string.end_user_session_msg)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    setStartMessage(end)
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    setEndMessage(null)
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.reset))
            }
        }
    }
}

@RequiresApi(23)
@Composable
private fun ChangeUserIconDialog(bitmap: Bitmap, onSet: () -> Unit, onClose: () -> Unit) {
    AlertDialog(
        title = { Text(stringResource(R.string.change_user_icon)) },
        text = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Image(
                    bitmap = bitmap.asImageBitmap(), contentDescription = null,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(50))
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
