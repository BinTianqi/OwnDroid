package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Binder
import android.os.Build.VERSION
import android.os.Process
import android.os.UserHandle
import android.os.UserManager
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.os.UserManagerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.fileUriFlow
import com.bintianqi.owndroid.getFile
import com.bintianqi.owndroid.toText
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.TopBar
import com.bintianqi.owndroid.uriToStream

var affiliationID = mutableSetOf<String>()
@Composable
fun UserManage(navCtrl: NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopBar(backStackEntry, navCtrl, localNavCtrl) {
                if(backStackEntry?.destination?.route == "Home" && scrollState.maxValue > 100) {
                    Text(
                        text = stringResource(R.string.user_manager),
                        modifier = Modifier.alpha((maxOf(scrollState.value-30, 0)).toFloat() / 80)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition,
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            composable(route = "Home") { Home(localNavCtrl, scrollState) }
            composable(route = "UserInfo") { CurrentUserInfo() }
            composable(route = "UserOperation") { UserOperation() }
            composable(route = "CreateUser") { CreateUser() }
            composable(route = "EditUsername") { Username() }
            composable(route = "ChangeUserIcon") { UserIcon() }
            composable(route = "UserSessionMessage") { UserSessionMessage() }
            composable(route = "AffiliationID") { AffiliationID() }
        }
    }
}

@Composable
private fun Home(navCtrl: NavHostController,scrollState: ScrollState) {
    val context = LocalContext.current
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Text(
            text = stringResource(R.string.user_manager),
            style = typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 16.dp)
        )
        SubPageItem(R.string.user_info, "", R.drawable.person_fill0) { navCtrl.navigate("UserInfo") }
        if(deviceOwner) {
            SubPageItem(R.string.user_operation, "", R.drawable.sync_alt_fill0) { navCtrl.navigate("UserOperation") }
        }
        if(VERSION.SDK_INT >= 24 && deviceOwner) {
            SubPageItem(R.string.create_user, "", R.drawable.person_add_fill0) { navCtrl.navigate("CreateUser") }
        }
        if(deviceOwner || profileOwner) {
            SubPageItem(R.string.edit_username, "", R.drawable.edit_fill0) { navCtrl.navigate("EditUsername") }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || profileOwner)) {
            SubPageItem(R.string.change_user_icon, "", R.drawable.account_circle_fill0) { navCtrl.navigate("ChangeUserIcon") }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            SubPageItem(R.string.user_session_msg, "", R.drawable.notifications_fill0) { navCtrl.navigate("UserSessionMessage") }
        }
        if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner)) {
            SubPageItem(R.string.affiliation_id, "", R.drawable.id_card_fill0) { navCtrl.navigate("AffiliationID") }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
        LaunchedEffect(Unit) { fileUriFlow.value = Uri.parse("") }
    }
}

@Composable
private fun CurrentUserInfo() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.user_info), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(stringResource(R.string.is_user_unlocked, UserManagerCompat.isUserUnlocked(context)))
        if(VERSION.SDK_INT >= 24) { Text(stringResource(R.string.is_support_multi_user, UserManager.supportsMultipleUsers())) }
        if(VERSION.SDK_INT >= 23) { Text(text = stringResource(R.string.is_system_user, userManager.isSystemUser)) }
        if(VERSION.SDK_INT >= 34) { Text(text = stringResource(R.string.is_admin_user, userManager.isAdminUser)) }
        if(VERSION.SDK_INT >= 31) { Text(text = stringResource(R.string.is_headless_system_user, UserManager.isHeadlessSystemUserMode())) }
        Spacer(Modifier.padding(vertical = 5.dp))
        if (VERSION.SDK_INT >= 28) {
            val logoutable = dpm.isLogoutEnabled
            Text(text = stringResource(R.string.user_can_logout, logoutable))
            if(context.isDeviceOwner || context.isProfileOwner) {
                val ephemeralUser = dpm.isEphemeralUser(receiver)
                Text(text = stringResource(R.string.is_ephemeral_user, ephemeralUser))
            }
            Text(text = stringResource(R.string.is_affiliated_user, dpm.isAffiliatedUser))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.user_id_is, Binder.getCallingUid() / 100000))
        Text(text = stringResource(R.string.user_serial_number_is, userManager.getSerialNumberForUser(Process.myUserHandle())))
    }
}

@Composable
private fun UserOperation() {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.user_operation), style = typography.headlineLarge)
        var idInput by remember { mutableStateOf("") }
        var userHandleById: UserHandle by remember { mutableStateOf(Process.myUserHandle()) }
        var useUid by remember { mutableStateOf(false) }
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = idInput,
            onValueChange = {
                idInput = it
                if(useUid) {
                    if(idInput != "" && VERSION.SDK_INT >= 24) {
                        userHandleById = UserHandle.getUserHandleForUid(idInput.toInt())
                    }
                }else{
                    val userHandleBySerial = userManager.getUserForSerialNumber(idInput.toLong())
                    userHandleById = userHandleBySerial ?: Process.myUserHandle()
                }
            },
            label = { Text(if(useUid) "UID" else stringResource(R.string.serial_number)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 3.dp))
        if(VERSION.SDK_INT >= 24) {
            CheckBoxItem(text = R.string.use_uid, checked = useUid, operation = { idInput=""; useUid = it })
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT > 28) {
            if(context.isProfileOwner && dpm.isAffiliatedUser) {
                Button(
                    onClick = {
                        val result = dpm.logoutUser(receiver)
                        Toast.makeText(context, userOperationResultCode(result, context), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.logout_current_user))
                }
            }
        }
        if(VERSION.SDK_INT >= 28) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    val result = dpm.startUserInBackground(receiver, userHandleById)
                    Toast.makeText(context, userOperationResultCode(result, context), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.start_in_background))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                Toast.makeText(context, if(dpm.switchUser(receiver,userHandleById)) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.user_operation_switch))
        }
        if(VERSION.SDK_INT >= 28) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    try{
                        val result = dpm.stopUser(receiver,userHandleById)
                        Toast.makeText(context, userOperationResultCode(result,context), Toast.LENGTH_SHORT).show()
                    }catch(e:IllegalArgumentException) {
                        Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.user_operation_stop))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                if(dpm.removeUser(receiver,userHandleById)) {
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                    idInput = ""
                }else{
                    Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.user_operation_remove))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun CreateUser() {
    val context = LocalContext.current
    val userManager = context.getSystemService(Context.USER_SERVICE) as UserManager
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var userName by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.create_user), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = userName,
            onValueChange = { userName= it },
            label = { Text(stringResource(R.string.username)) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        var selectedFlag by remember { mutableIntStateOf(0) }
        RadioButtonItem(R.string.none, selectedFlag == 0, { selectedFlag = 0 })
        RadioButtonItem(
            R.string.create_user_skip_wizard,
            selectedFlag == DevicePolicyManager.SKIP_SETUP_WIZARD,
            { selectedFlag = DevicePolicyManager.SKIP_SETUP_WIZARD }
        )
        if(VERSION.SDK_INT >= 28) {
            RadioButtonItem(
                R.string.create_user_ephemeral_user,
                selectedFlag == DevicePolicyManager.MAKE_USER_EPHEMERAL,
                { selectedFlag = DevicePolicyManager.MAKE_USER_EPHEMERAL }
            )
            RadioButtonItem(
                R.string.create_user_enable_all_system_app,
                selectedFlag == DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED,
                { selectedFlag = DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED }
            )
        }
        var newUserHandle: UserHandle? by remember { mutableStateOf(null) }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                newUserHandle = dpm.createAndManageUser(receiver, userName, receiver, null, selectedFlag)
                focusMgr.clearFocus()
                Toast.makeText(context, if(newUserHandle!=null) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        if(newUserHandle != null) { Text(text = stringResource(R.string.serial_number_of_new_user_is, userManager.getSerialNumberForUser(newUserHandle))) }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun AffiliationID() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    var list by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        affiliationID = dpm.getAffiliationIds(receiver)
        list = affiliationID.toText()
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.affiliation_id), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(list != "") {
            SelectionContainer { Text(text = list) }
        }else{
            Text(text = stringResource(R.string.none))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = input,
            onValueChange = {input = it},
            label = { Text("ID") },
            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() })
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { affiliationID.add(input); list = affiliationID.toText() },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = { affiliationID.remove(input); list = affiliationID.toText() },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                if("" in affiliationID) {
                    Toast.makeText(context, R.string.include_empty_string, Toast.LENGTH_SHORT).show()
                }else if(affiliationID.isEmpty()) {
                    Toast.makeText(context, R.string.cannot_be_empty, Toast.LENGTH_SHORT).show()
                }else{
                    dpm.setAffiliationIds(receiver, affiliationID)
                    affiliationID = dpm.getAffiliationIds(receiver)
                    list = affiliationID.toText()
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Username() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var inputUsername by remember { mutableStateOf("") }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.edit_username), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
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
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
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

@SuppressLint("NewApi")
@Composable
private fun UserSessionMessage() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val getStart = dpm.getStartUserSessionMessage(receiver)?:""
    val getEnd = dpm.getEndUserSessionMessage(receiver)?:""
    var start by remember { mutableStateOf(getStart.toString()) }
    var end by remember { mutableStateOf(getEnd.toString()) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.user_session_msg), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = start,
            onValueChange = { start= it },
            label = { Text(stringResource(R.string.start_user_session_msg)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 2.dp))
        OutlinedTextField(
            value = end,
            onValueChange = { end= it },
            label = { Text(stringResource(R.string.end_user_session_msg)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.setStartUserSessionMessage(receiver,start)
                dpm.setEndUserSessionMessage(receiver,end)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Button(
            onClick = {
                dpm.setStartUserSessionMessage(receiver,null)
                dpm.setEndUserSessionMessage(receiver,null)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun UserIcon() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var getContent by remember { mutableStateOf(false) }
    val canApply = fileUriFlow.collectAsState().value != Uri.parse("")
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.change_user_icon), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.pick_a_square_image))
        Spacer(Modifier.padding(vertical = 5.dp))
        CheckBoxItem(R.string.file_picker_instead_gallery, getContent, { getContent = it })
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                val intent = Intent(if(getContent) Intent.ACTION_GET_CONTENT else Intent.ACTION_PICK)
                if(getContent) intent.addCategory(Intent.CATEGORY_OPENABLE)
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*")
                getFile.launch(intent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_picture))
        }
        AnimatedVisibility(canApply) {
            Button(
                onClick = {
                    uriToStream(context, fileUriFlow.value) {stream ->
                        val bitmap = BitmapFactory.decodeStream(stream)
                        dpm.setUserIcon(receiver, bitmap)
                        Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.apply))
            }
        }
    }
}

private fun userOperationResultCode(result:Int, context: Context): String {
    return when(result) {
        UserManager.USER_OPERATION_SUCCESS->context.getString(R.string.success)
        UserManager.USER_OPERATION_ERROR_UNKNOWN-> context.getString(R.string.unknown_result)
        UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE-> context.getString(R.string.fail_managed_profile)
        UserManager.USER_OPERATION_ERROR_CURRENT_USER-> context.getString(R.string.fail_current_user)
        else->context.getString(R.string.unknown)
    }
}
