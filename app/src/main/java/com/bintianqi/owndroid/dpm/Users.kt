package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build.VERSION
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.parseTimestamp
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CardItem
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoCard
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.uriToStream
import com.bintianqi.owndroid.yesOrNo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable

@Serializable object Users

@Composable
fun UsersScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.users, 0.dp, onNavigateUp) {
        if(VERSION.SDK_INT >= 28 && profileOwner && dpm.isAffiliatedUser) {
            FunctionItem(R.string.logout, icon = R.drawable.logout_fill0) { dialog = 2 }
        }
        FunctionItem(R.string.user_info, icon = R.drawable.person_fill0) { onNavigate(UserInfo) }
        if(deviceOwner && VERSION.SDK_INT >= 28) {
            FunctionItem(R.string.secondary_users, icon = R.drawable.list_fill0) { dialog = 1 }
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(UsersOptions) }
        }
        if(deviceOwner) {
            FunctionItem(R.string.user_operation, icon = R.drawable.sync_alt_fill0) { onNavigate(UserOperation) }
        }
        if(VERSION.SDK_INT >= 24 && deviceOwner) {
            FunctionItem(R.string.create_user, icon = R.drawable.person_add_fill0) { onNavigate(CreateUser) }
        }
        if(deviceOwner || profileOwner) {
            FunctionItem(R.string.change_username, icon = R.drawable.edit_fill0) { onNavigate(ChangeUsername) }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || profileOwner)) {
            var changeUserIconDialog by remember { mutableStateOf(false) }
            var bitmap: Bitmap? by remember { mutableStateOf(null) }
            val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
                if(it != null) uriToStream(context, it) { stream ->
                    bitmap = BitmapFactory.decodeStream(stream)
                    if(bitmap != null) changeUserIconDialog = true
                }
            }
            FunctionItem(R.string.change_user_icon, icon = R.drawable.account_circle_fill0) { launcher.launch("image/*") }
            if(changeUserIconDialog == true) ChangeUserIconDialog(bitmap!!) { changeUserIconDialog = false }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            FunctionItem(R.string.user_session_msg, icon = R.drawable.notifications_fill0) { onNavigate(UserSessionMessage) }
        }
        if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.affiliation_id, icon = R.drawable.id_card_fill0) { onNavigate(AffiliationId) }
        }
    }
    if(dialog != 0 && VERSION.SDK_INT >= 28) AlertDialog(
        title = { Text(stringResource(if(dialog == 1) R.string.secondary_users else R.string.logout)) },
        text = {
            if(dialog == 1) {
                val um = context.getSystemService(Context.USER_SERVICE) as UserManager
                val list = dpm.getSecondaryUsers(receiver)
                if(list.isEmpty()) {
                    Text(stringResource(R.string.no_secondary_users))
                } else {
                    Text("(" + stringResource(R.string.serial_number) + ")\n" + list.joinToString("\n") { um.getSerialNumberForUser(it).toString() })
                }
            } else {
                Text(stringResource(R.string.info_logout))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if(dialog == 2) {
                        val result = dpm.logoutUser(receiver)
                        Toast.makeText(context, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                    }
                    dialog = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            if(dialog != 1) TextButton(onClick = { dialog = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@Serializable object UsersOptions

@Composable
fun UsersOptionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    MyScaffold(R.string.options, 0.dp, onNavigateUp) {
        if(VERSION.SDK_INT >= 28) {
            SwitchItem(R.string.enable_logout, getState = { dpm.isLogoutEnabled }, onCheckedChange = { dpm.setLogoutEnabled(receiver, it) })
        }
    }
}

@Serializable object UserInfo

@Composable
fun UserInfoScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val user = Process.myUserHandle()
    var infoDialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.user_info, 8.dp, onNavigateUp) {
        if(VERSION.SDK_INT >= 24) CardItem(R.string.support_multiuser, UserManager.supportsMultipleUsers().yesOrNo)
        if(VERSION.SDK_INT >= 31) CardItem(R.string.headless_system_user_mode, UserManager.isHeadlessSystemUserMode().yesOrNo) { infoDialog = 1 }
        Spacer(Modifier.padding(vertical = 8.dp))
        if(VERSION.SDK_INT >= 23) CardItem(R.string.system_user, userManager.isSystemUser.yesOrNo)
        if(VERSION.SDK_INT >= 34) CardItem(R.string.admin_user, userManager.isAdminUser.yesOrNo)
        if(VERSION.SDK_INT >= 25) CardItem(R.string.demo_user, userManager.isDemoUser.yesOrNo)
        if(VERSION.SDK_INT >= 26) CardItem(R.string.creation_time, parseTimestamp(userManager.getUserCreationTime(user)))
        if (VERSION.SDK_INT >= 28) {
            CardItem(R.string.logout_enabled, dpm.isLogoutEnabled.yesOrNo)
            if(context.isDeviceOwner || context.isProfileOwner) {
                CardItem(R.string.ephemeral_user, dpm.isEphemeralUser(receiver).yesOrNo)
            }
            CardItem(R.string.affiliated_user, dpm.isAffiliatedUser.yesOrNo)
        }
        CardItem(R.string.user_id, (Binder.getCallingUid() / 100000).toString())
        CardItem(R.string.user_serial_number, userManager.getSerialNumberForUser(Process.myUserHandle()).toString())
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
fun UserOperationScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var input by remember { mutableStateOf("") }
    val focusMgr = LocalFocusManager.current
    var useUserId by remember { mutableStateOf(false) }
    fun withUserHandle(operation: (UserHandle) -> Unit) {
        val userHandle = if(useUserId && VERSION.SDK_INT >= 24) {
            UserHandle.getUserHandleForUid(input.toInt() * 100000)
        } else {
            userManager.getUserForSerialNumber(input.toLong())
        }
        if(userHandle == null) {
            Toast.makeText(context, R.string.user_not_exist, Toast.LENGTH_SHORT).show()
        } else {
            operation(userHandle)
        }
    }
    val legalInput = input.toIntOrNull() != null
    MyScaffold(R.string.user_operation, 8.dp, onNavigateUp) {
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
                    withUserHandle {
                        val result = dpm.startUserInBackground(receiver, it)
                        Toast.makeText(context, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                    }
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
                withUserHandle { context.showOperationResultToast(dpm.switchUser(receiver, it)) }
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
                    withUserHandle {
                        val result = dpm.stopUser(receiver, it)
                        Toast.makeText(context, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                    }
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
                withUserHandle {
                    if(dpm.removeUser(receiver, it)) {
                        context.showOperationResultToast(true)
                        input = ""
                    } else {
                        Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                }
            },
            enabled = legalInput,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.Delete, null, Modifier.padding(end = 4.dp))
            Text(stringResource(R.string.delete))
        }
    }
}

@Serializable object CreateUser

@RequiresApi(24)
@Composable
fun CreateUserScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var userName by remember { mutableStateOf("") }
    var creating by remember { mutableStateOf(false) }
    var createdUserSerialNumber by remember { mutableLongStateOf(-1) }
    var flag by remember { mutableIntStateOf(0) }
    val coroutine = rememberCoroutineScope()
    MyScaffold(R.string.create_user, 8.dp, onNavigateUp) {
        OutlinedTextField(
            value = userName,
            onValueChange = { userName= it },
            label = { Text(stringResource(R.string.username)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        CheckBoxItem(
            R.string.create_user_skip_wizard,
            flag and DevicePolicyManager.SKIP_SETUP_WIZARD != 0
        ) { flag = flag xor DevicePolicyManager.SKIP_SETUP_WIZARD }
        if(VERSION.SDK_INT >= 28) {
            CheckBoxItem(
                R.string.create_user_ephemeral_user,
                flag and DevicePolicyManager.MAKE_USER_EPHEMERAL != 0
            ) { flag = flag xor DevicePolicyManager.MAKE_USER_EPHEMERAL }
            CheckBoxItem(
                R.string.create_user_enable_all_system_app,
                flag and DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED != 0
            ) { flag = flag xor DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                creating = true
                coroutine.launch(Dispatchers.IO) {
                    try {
                        val uh = dpm.createAndManageUser(receiver, userName, receiver, null, flag)
                        withContext(Dispatchers.Main) {
                            createdUserSerialNumber = userManager.getSerialNumberForUser(uh)
                        }
                    } catch(_: Exception) {
                        context.showOperationResultToast(false)
                    }
                    withContext(Dispatchers.Main) { creating = false }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create))
        }
        if(createdUserSerialNumber != -1L) AlertDialog(
            title = { Text(stringResource(R.string.success)) },
            text = { Text(stringResource(R.string.serial_number_of_new_user_is, createdUserSerialNumber)) },
            confirmButton = {
                TextButton({ createdUserSerialNumber = -1 }) { Text(stringResource(R.string.confirm)) }
            },
            onDismissRequest = { createdUserSerialNumber = -1 }
        )
        if(creating) Dialog({}, DialogProperties(false, false)) {
            CircularProgressIndicator()
        }
    }
}

@Serializable object AffiliationId

@RequiresApi(26)
@Composable
fun AffiliationIdScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    val list = remember { mutableStateListOf<String>() }
    val refreshIds = {
        list.clear()
        list.addAll(dpm.getAffiliationIds(receiver))
    }
    LaunchedEffect(Unit) { refreshIds() }
    MyScaffold(R.string.affiliation_id, 8.dp, onNavigateUp) {
        Column(modifier = Modifier.animateContentSize()) {
            if(list.isEmpty()) Text(stringResource(R.string.none))
            for(i in list) {
                ListItem(i) { list -= i }
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("ID") },
            trailingIcon = {
                IconButton(
                    onClick = {
                        list += input
                        input = ""
                    }
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                list.removeAll(listOf(""))
                dpm.setAffiliationIds(receiver, list.toSet())
                context.showOperationResultToast(true)
                refreshIds()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_affiliated_id)
    }
}

@Serializable object ChangeUsername

@Composable
fun ChangeUsernameScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var inputUsername by remember { mutableStateOf("") }
    MyScaffold(R.string.change_username, 8.dp, onNavigateUp) {
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
                dpm.setProfileName(receiver, inputUsername)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Button(
            onClick = { dpm.setProfileName(receiver,null) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset))
        }
    }
}

@Serializable object UserSessionMessage

@RequiresApi(28)
@Composable
fun UserSessionMessageScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var start by remember { mutableStateOf("") }
    var end by remember { mutableStateOf("") }
    val refreshMsg = {
        start = dpm.getStartUserSessionMessage(receiver)?.toString() ?: ""
        end = dpm.getEndUserSessionMessage(receiver)?.toString() ?: ""
    }
    LaunchedEffect(Unit) { refreshMsg() }
    MyScaffold(R.string.user_session_msg, 8.dp, onNavigateUp) {
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
                    dpm.setStartUserSessionMessage(receiver,start)
                    refreshMsg()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    dpm.setStartUserSessionMessage(receiver,null)
                    refreshMsg()
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
                    dpm.setEndUserSessionMessage(receiver,end)
                    refreshMsg()
                    context.showOperationResultToast(true)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    dpm.setEndUserSessionMessage(receiver,null)
                    refreshMsg()
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
private fun ChangeUserIconDialog(bitmap: Bitmap, onClose: () -> Unit) {
    val context = LocalContext.current
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
            TextButton({
                context.getDPM().setUserIcon(context.getReceiver(), bitmap)
                context.showOperationResultToast(true)
                onClose()
            }) {
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

@StringRes
private fun userOperationResultCode(result:Int): Int =
    when(result) {
        UserManager.USER_OPERATION_SUCCESS -> R.string.success
        UserManager.USER_OPERATION_ERROR_UNKNOWN -> R.string.unknown_error
        UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE-> R.string.fail_managed_profile
        UserManager.USER_OPERATION_ERROR_CURRENT_USER-> R.string.fail_current_user
        else -> R.string.unknown
    }
