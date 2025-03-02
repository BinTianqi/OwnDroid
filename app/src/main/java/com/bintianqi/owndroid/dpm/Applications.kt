package com.bintianqi.owndroid.dpm

import android.app.AlertDialog
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
import android.app.admin.PackagePolicy
import android.app.admin.PackagePolicy.PACKAGE_POLICY_ALLOWLIST
import android.app.admin.PackagePolicy.PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM
import android.app.admin.PackagePolicy.PACKAGE_POLICY_BLOCKLIST
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build.VERSION
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.APK_MIME
import com.bintianqi.owndroid.AppInstallerActivity
import com.bintianqi.owndroid.AppInstallerViewModel
import com.bintianqi.owndroid.ChoosePackageContract
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SwitchItem
import kotlinx.serialization.Serializable
import java.util.concurrent.Executors

@Serializable
object Applications

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsScreen(onNavigateUp: () -> Unit) {
    val focusMgr = LocalFocusManager.current
    val navController = rememberNavController()
    var pkgName by rememberSaveable { mutableStateOf("") }
    val choosePackage = rememberLauncherForActivityResult(ChoosePackageContract()) {result ->
        result?.let { pkgName = it }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = pkgName,
                        onValueChange = { pkgName = it },
                        label = { Text(stringResource(R.string.package_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                        trailingIcon = {
                            IconButton({
                                focusMgr.clearFocus()
                                choosePackage.launch(null)
                            }) {
                                Icon(Icons.AutoMirrored.Default.List, stringResource(R.string.package_chooser))
                            }
                        },
                        textStyle = typography.bodyLarge,
                        singleLine = true
                    )
                },
                navigationIcon = { NavIcon(onNavigateUp) },
                colors = TopAppBarDefaults.topAppBarColors(colorScheme.surfaceContainer)
            )
        }
    ) {  paddingValues->
        @Suppress("NewApi") NavHost(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            navController = navController, startDestination = Home,
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition
        ) {
            composable<Home> { HomeScreen(pkgName) { navController.navigate(it) } }
            composable<UserControlDisabledPackages> { UserControlDisabledPackagesScreen(pkgName) }
            composable<PermissionManager> { PermissionManagerScreen(pkgName) }
            composable<CrossProfilePackages> { CrossProfilePackagesScreen(pkgName) }
            composable<CrossProfileWidgetProviders> { CrossProfileWidgetProvidersScreen(pkgName) }
            composable<CredentialManagerPolicy> { CredentialManagerPolicyScreen(pkgName) }
            composable<PermittedAccessibilityServices> { PermittedAccessibilityServicesScreen(pkgName) }
            composable<PermittedInputMethods> { PermittedInputMethodsScreen(pkgName) }
            composable<KeepUninstalledPackages> { KeepUninstalledPackagesScreen(pkgName) }
            composable<UninstallPackage> { UninstallPackageScreen(pkgName) }
        }
    }
}

@Serializable private object Home

@Composable
private fun HomeScreen(pkgName: String, onNavigate: (Any) -> Unit) {
    /** 1:Enable system app, 2:Clear app storage, 3:Set default dialer, 4:App control, 5:Install existing app */
    var dialogStatus by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var suspend by remember { mutableStateOf(false) }
    var hide by remember { mutableStateOf(false) }
    var blockUninstall by remember { mutableStateOf(false) }
    var appControlAction by remember { mutableIntStateOf(0) } // 1:Suspend, 2:Hide, 3:Block uninstall
    val focusMgr = LocalFocusManager.current
    fun refresh() {
        if(VERSION.SDK_INT >= 24) {
            try {
                suspend = dpm.isPackageSuspended(receiver, pkgName)
            } catch(_: Exception) {}
        }
        hide = dpm.isApplicationHidden(receiver, pkgName)
        blockUninstall = dpm.isUninstallBlocked(receiver, pkgName)
    }
    fun appControl(status: Boolean) {
        when(appControlAction) {
            1 -> if(VERSION.SDK_INT >= 24) dpm.setPackagesSuspended(receiver, arrayOf(pkgName), status)
            2 -> dpm.setApplicationHidden(receiver, pkgName, status)
            3 -> dpm.setUninstallBlocked(receiver, pkgName, status)
        }
        refresh()
    }
    LaunchedEffect(pkgName) { refresh() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 24 && profileOwner && dpm.isManagedProfile(receiver)) {
            Text(text = stringResource(R.string.scope_is_work_profile), textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth())
        }
        FunctionItem(title = R.string.app_info, icon = R.drawable.open_in_new) {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.parse("package:$pkgName"))
            startActivity(context, intent, null)
        }
        if(VERSION.SDK_INT >= 24) {
            SwitchItem(
                title = R.string.suspend, icon = R.drawable.block_fill0,
                state = suspend,
                onCheckedChange = { appControlAction = 1; appControl(it) },
                onClickBlank = { appControlAction = 1; dialogStatus = 4 }
            )
        }
        SwitchItem(
            title = R.string.hide, icon = R.drawable.visibility_off_fill0,
            state = hide,
            onCheckedChange = { appControlAction = 2; appControl(it) },
            onClickBlank = { appControlAction = 2; dialogStatus = 4 }
        )
        SwitchItem(
            title = R.string.block_uninstall, icon = R.drawable.delete_forever_fill0,
            state = blockUninstall,
            onCheckedChange = { appControlAction = 3; appControl(it) },
            onClickBlank = { appControlAction = 3; dialogStatus = 4 }
        )
        if(VERSION.SDK_INT >= 30 && (deviceOwner || (VERSION.SDK_INT >= 33 && profileOwner))) {
            FunctionItem(title = R.string.ucd, icon = R.drawable.do_not_touch_fill0) { onNavigate(UserControlDisabledPackages) }
        }
        if(VERSION.SDK_INT>=23) {
            FunctionItem(title = R.string.permission_manage, icon = R.drawable.key_fill0) { onNavigate(PermissionManager) }
        }
        if(VERSION.SDK_INT >= 30 && profileOwner && dpm.isManagedProfile(receiver)) {
            FunctionItem(title = R.string.cross_profile_package, icon = R.drawable.work_fill0) { onNavigate(CrossProfilePackages) }
        }
        if(profileOwner) { 
            FunctionItem(title = R.string.cross_profile_widget, icon = R.drawable.widgets_fill0) { onNavigate(CrossProfileWidgetProviders) }
        }
        if(VERSION.SDK_INT >= 34 && deviceOwner) {
            FunctionItem(title = R.string.credential_manage_policy, icon = R.drawable.license_fill0) { onNavigate(CredentialManagerPolicy) }
        }
        FunctionItem(title = R.string.permitted_accessibility_services, icon = R.drawable.settings_accessibility_fill0) {
            onNavigate(PermittedAccessibilityServices)
        }
        FunctionItem(title = R.string.permitted_ime, icon = R.drawable.keyboard_fill0) { onNavigate(PermittedInputMethods) }
        FunctionItem(title = R.string.enable_system_app, icon = R.drawable.enable_fill0) {
            if(pkgName != "") dialogStatus = 1
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            FunctionItem(title = R.string.keep_uninstalled_packages, icon = R.drawable.delete_fill0) { onNavigate(KeepUninstalledPackages) }
        }
        if(VERSION.SDK_INT >= 28) {
            FunctionItem(title = R.string.clear_app_storage, icon = R.drawable.mop_fill0) {
                if(pkgName != "") dialogStatus = 2
            }
        }
        val chooseApks = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) {
            if(it.isEmpty()) return@rememberLauncherForActivityResult
            val intent = Intent(context, AppInstallerActivity::class.java)
            intent.putExtra(Intent.EXTRA_STREAM, it.toTypedArray())
            startActivity(context, intent, null)
        }
        FunctionItem(title = R.string.install_app, icon = R.drawable.install_mobile_fill0) {
            Toast.makeText(context, R.string.choose_apk_file, Toast.LENGTH_SHORT).show()
            chooseApks.launch(APK_MIME)
        }
        if(VERSION.SDK_INT >= 28) FunctionItem(R.string.install_existing_app, icon = R.drawable.install_mobile_fill0) { dialogStatus = 5 }
        FunctionItem(title = R.string.uninstall_app, icon = R.drawable.delete_fill0) { onNavigate(UninstallPackage) }
        if(VERSION.SDK_INT >= 34 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(title = R.string.set_default_dialer, icon = R.drawable.call_fill0) {
                if(pkgName != "") dialogStatus = 3
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
    if(dialogStatus == 1) AlertDialog(
        title = { Text(stringResource(R.string.enable_system_app)) },
        text = {
            Text(stringResource(R.string.enable_system_app_desc) + "\n" + pkgName)
        },
        onDismissRequest = { dialogStatus = 0 },
        dismissButton = {
            TextButton({ dialogStatus = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton({
                try {
                    dpm.enableSystemApp(receiver, pkgName)
                    context.showOperationResultToast(true)
                } catch(_: IllegalArgumentException) {
                    Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                }
                dialogStatus = 0
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    if(dialogStatus == 2 && VERSION.SDK_INT >= 28) AlertDialog(
        title = { Text(text = stringResource(R.string.clear_app_storage)) },
        text = {
            Text(stringResource(R.string.app_storage_will_be_cleared) + "\n" + pkgName)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val executor = Executors.newCachedThreadPool()
                    dpm.clearApplicationUserData(receiver, pkgName, executor) { pkg: String, succeed: Boolean ->
                        Looper.prepare()
                        val toastText =
                            if(pkg != "") { "$pkg\n" } else { "" } +
                                    context.getString(R.string.clear_data) +
                                    context.getString(if(succeed) R.string.success else R.string.failed )
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                        Looper.loop()
                    }
                    dialogStatus = 0
                },
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(text = stringResource(R.string.clear))
            }
        },
        dismissButton = {
            TextButton({ dialogStatus = 0 }) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialogStatus = 0 },
        modifier = Modifier.fillMaxWidth()
    )
    if(dialogStatus == 3 && VERSION.SDK_INT >= 34) AlertDialog(
        title = { Text(stringResource(R.string.set_default_dialer)) },
        text = {
            Text(stringResource(R.string.app_will_be_default_dialer) + "\n" + pkgName)
        },
        onDismissRequest = { dialogStatus = 0 },
        dismissButton = {
            TextButton({ dialogStatus = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton({
                try {
                    dpm.setDefaultDialerApplication(pkgName)
                    context.showOperationResultToast(true)
                } catch(_: IllegalArgumentException) {
                    context.showOperationResultToast(false)
                }
                dialogStatus = 0
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
    if(dialogStatus == 4) {
        LaunchedEffect(Unit) {
            focusMgr.clearFocus()
        }
        AlertDialog(
            onDismissRequest = { dialogStatus = 0 },
            title = {
                Text(stringResource(
                    when(appControlAction) {
                        1 -> R.string.suspend
                        2 -> R.string.hide
                        3 -> R.string.block_uninstall
                        4 -> R.string.always_on_vpn
                        else -> R.string.unknown
                    }
                ))
            },
            text = {
                val enabled = when(appControlAction){
                    1 -> suspend
                    2 -> hide
                    3 -> blockUninstall
                    else -> false
                }
                Column {
                    Text(stringResource(R.string.current_state, stringResource(if(enabled) R.string.enabled else R.string.disabled)))
                    Spacer(Modifier.padding(vertical = 4.dp))
                    if(appControlAction == 1) Text(stringResource(R.string.info_suspend_app))
                }
            },
            confirmButton = {
                TextButton({
                    appControl(true)
                    dialogStatus = 0
                }) {
                    Text(text = stringResource(R.string.enable))
                }
            },
            dismissButton = {
                TextButton({
                    appControl(false)
                    dialogStatus = 0
                }) {
                    Text(text = stringResource(R.string.disable))
                }
            }
        )
    }
    if(dialogStatus == 5 && VERSION.SDK_INT >= 28) AlertDialog(
        text = { Text(stringResource(R.string.info_install_existing_app)) },
        confirmButton = {
            TextButton({
                context.showOperationResultToast(dpm.installExistingPackage(receiver, pkgName))
                dialogStatus = 0
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({ dialogStatus = 0 }) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { dialogStatus = 0 }
    )
    LaunchedEffect(dialogStatus) { focusMgr.clearFocus() }
}

@Serializable private object UserControlDisabledPackages

@RequiresApi(30)
@Composable
private fun UserControlDisabledPackagesScreen(pkgName:String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        val refresh = {
            pkgList.clear()
            pkgList.addAll(dpm.getUserControlDisabledPackages(receiver))
        }
        LaunchedEffect(Unit) { refresh() }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.ucd), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.ucd_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        Column(modifier = Modifier.animateContentSize()) {
            if(pkgList.isEmpty()) Text(stringResource(R.string.none))
            for(i in pkgList) {
                ListItem(i) { pkgList -= i }
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { pkgList += pkgName },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add))
        }
        Button(
            onClick = { dpm.setUserControlDisabledPackages(receiver, pkgList); refresh() },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_disable_user_control)
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Serializable private object PermissionManager

@RequiresApi(23)
@Composable
private fun PermissionManagerScreen(pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var showDialog by remember { mutableStateOf(false) }
    var selectedPermission by remember { mutableStateOf(PermissionItem("", R.string.unknown, R.drawable.block_fill0)) }
    val statusMap = remember { mutableStateMapOf<String, Int>() }
    val grantState = mapOf(
        PERMISSION_GRANT_STATE_DEFAULT to stringResource(R.string.default_stringres),
        PERMISSION_GRANT_STATE_GRANTED to stringResource(R.string.granted),
        PERMISSION_GRANT_STATE_DENIED to stringResource(R.string.denied)
    )
    LaunchedEffect(pkgName) {
        if(pkgName != "") {
            permissionList().forEach { statusMap[it.permission] = dpm.getPermissionGrantState(receiver, pkgName, it.permission) }
        } else {
            statusMap.clear()
        }
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 4.dp))
        for(permission in permissionList()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        if(pkgName != "") {
                            selectedPermission = permission
                            showDialog = true
                        }
                    }
                    .padding(8.dp)
            ) {
                Icon(
                    painter = painterResource(permission.icon),
                    contentDescription = stringResource(permission.label),
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                Column {
                    Text(text = stringResource(permission.label))
                    Text(
                        text = grantState[statusMap[permission.permission]]?: stringResource(R.string.unknown),
                        modifier = Modifier.alpha(0.7F), style = typography.bodyMedium
                    )
                }
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
    if(showDialog) {
        val grantPermission: (Int)->Unit = {
            dpm.setPermissionGrantState(receiver, pkgName, selectedPermission.permission, it)
            statusMap[selectedPermission.permission] = dpm.getPermissionGrantState(receiver, pkgName, selectedPermission.permission)
            showDialog = false
        }
        @Composable
        fun GrantPermissionItem(label: Int, status: Int) {
            val selected = statusMap[selectedPermission.permission] == status
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if(selected) colorScheme.primaryContainer else Color.Transparent)
                    .clickable { grantPermission(status) }
                    .padding(vertical = 16.dp, horizontal = 12.dp)
            ) {
                Text(text = stringResource(label), color = if(selected) colorScheme.primary else Color.Unspecified)
                if(selected) {
                    Icon(
                        painter = painterResource(R.drawable.check_circle_fill0),
                        contentDescription = stringResource(label),
                        tint = colorScheme.primary
                    )
                }
            }
        }
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(selectedPermission.label)) },
            text = {
                Column {
                    Text(selectedPermission.permission)
                    Spacer(Modifier.padding(vertical = 4.dp))
                    if(!(VERSION.SDK_INT >=31 && context.isProfileOwner && selectedPermission.profileOwnerRestricted)) {
                        GrantPermissionItem(R.string.granted, PERMISSION_GRANT_STATE_GRANTED)
                    }
                    GrantPermissionItem(R.string.denied, PERMISSION_GRANT_STATE_DENIED)
                    GrantPermissionItem(R.string.default_stringres, PERMISSION_GRANT_STATE_DEFAULT)
                }
            }
        )
    }
}

@Serializable private object CrossProfilePackages

@RequiresApi(30)
@Composable
private fun CrossProfilePackagesScreen(pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val crossProfilePkg = remember { mutableStateListOf<String>() }
    val refresh = {
        crossProfilePkg.clear()
        crossProfilePkg.addAll(dpm.getCrossProfilePackages(receiver))
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.cross_profile_package), style = typography.headlineLarge)
        Text(text = stringResource(R.string.app_list_is))
        Column(modifier = Modifier.animateContentSize()) {
            if(crossProfilePkg.isEmpty()) Text(stringResource(R.string.none))
            for(i in crossProfilePkg) {
                ListItem(i) { crossProfilePkg -= i }
            }
        }
        Button(
            onClick = { crossProfilePkg += pkgName },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add))
        }
        Button(
            onClick = {
                dpm.setCrossProfilePackages(receiver, crossProfilePkg.toSet())
                refresh()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Serializable private object CrossProfileWidgetProviders

@Composable
private fun CrossProfileWidgetProvidersScreen(pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    val refresh = {
        pkgList.clear()
        pkgList.addAll(dpm.getCrossProfileWidgetProviders(receiver))
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.cross_profile_widget), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        Column(modifier = Modifier.animateContentSize()) {
            if(pkgList.isEmpty()) Text(stringResource(R.string.none))
            for(i in pkgList) {
                ListItem(i) {
                    dpm.removeCrossProfileWidgetProvider(receiver, i)
                    refresh()
                }
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                if(pkgName != "") { dpm.addCrossProfileWidgetProvider(receiver, pkgName) }
                refresh()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
    }
}

@Serializable private object CredentialManagerPolicy

@RequiresApi(34)
@Composable
private fun CredentialManagerPolicyScreen(pkgName: String) { // TODO: rename "manage" to "manager"
    val context = LocalContext.current
    val dpm = context.getDPM()
    var policy: PackagePolicy?
    var policyType by remember{ mutableIntStateOf(-1) }
    val pkgList = remember { mutableStateListOf<String>() }
    val refreshPolicy = {
        policy = dpm.credentialManagerPolicy
        policyType = policy?.policyType ?: -1
        pkgList.clear()
        pkgList.addAll(policy?.packageNames ?: setOf())
    }
    LaunchedEffect(Unit) { refreshPolicy() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.credential_manage_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(R.string.none, policyType == -1) { policyType = -1 }
        RadioButtonItem(R.string.blacklist, policyType == PACKAGE_POLICY_BLOCKLIST) { policyType = PACKAGE_POLICY_BLOCKLIST }
        RadioButtonItem(R.string.whitelist, policyType == PACKAGE_POLICY_ALLOWLIST){ policyType = PACKAGE_POLICY_ALLOWLIST }
        RadioButtonItem(
            R.string.whitelist_and_system_app,
            policyType == PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM
        ) { policyType = PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM }
        Spacer(Modifier.padding(vertical = 5.dp))
        AnimatedVisibility(policyType != -1) {
            Column {
                Text(stringResource(R.string.app_list_is))
                Column(modifier = Modifier.animateContentSize()) {
                    if(pkgList.isEmpty()) Text(stringResource(R.string.none))
                    for(i in pkgList) {
                        ListItem(i) { pkgList -= i }
                    }
                }
                Button(
                    onClick = { pkgList += pkgName },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add))
                }
                Button(
                    onClick = {
                        try {
                            if(policyType != -1 && pkgList.isNotEmpty()) {
                                dpm.credentialManagerPolicy = PackagePolicy(policyType, pkgList.toSet())
                            } else {
                                dpm.credentialManagerPolicy = null
                            }
                            context.showOperationResultToast(true)
                        } catch(_: IllegalArgumentException) {
                            Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                        } finally {
                            refreshPolicy()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Serializable private object PermittedAccessibilityServices

@Composable
private fun PermittedAccessibilityServicesScreen(pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    var allowAll by remember { mutableStateOf(true) }
    val refresh = {
        pkgList.clear()
        val getList = dpm.getPermittedAccessibilityServices(receiver)
        allowAll = getList == null
        pkgList.addAll(getList ?: listOf())
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permitted_accessibility_services), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 8.dp)
        ) {
            Text(stringResource(R.string.allow_all), style = typography.titleLarge)
            Switch(
                checked = allowAll,
                onCheckedChange = {
                    dpm.setPermittedAccessibilityServices(receiver, if(it) null else listOf())
                    refresh()
                }
            )
        }
        AnimatedVisibility(!allowAll) {
            Column {
                Column(modifier = Modifier.animateContentSize()) {
                    Text(stringResource(if(pkgList.isEmpty()) R.string.only_system_accessibility_allowed else R.string.permitted_packages_is))
                    if(pkgList.isEmpty()) Text(stringResource(R.string.none))
                    for(i in pkgList) {
                        ListItem(i) { pkgList -= i }
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                Button(
                    onClick = { pkgList += pkgName },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add))
                }
                Button(
                    onClick = {
                        dpm.setPermittedAccessibilityServices(receiver, pkgList)
                        refresh()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
        Notes(R.string.system_accessibility_always_allowed)
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Serializable private object PermittedInputMethods

@Composable
private fun PermittedInputMethodsScreen(pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    var allowAll by remember { mutableStateOf(true) }
    val refresh = {
        pkgList.clear()
        val getList = dpm.getPermittedInputMethods(receiver)
        allowAll = getList == null
        pkgList.addAll(getList ?: listOf())
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permitted_ime), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        SwitchItem(
            R.string.allow_all, state = allowAll,
            onCheckedChange = {
                dpm.setPermittedInputMethods(receiver, if(it) null else listOf())
                refresh()
            }, padding = false
        )
        AnimatedVisibility(!allowAll) {
            Column {
                Column(modifier = Modifier.animateContentSize()) {
                    Text(stringResource(if(pkgList.isEmpty()) R.string.only_system_ime_allowed else R.string.permitted_packages_is))
                    for(i in pkgList) {
                        ListItem(i) { pkgList -= i }
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                Button(
                    onClick = { pkgList += pkgName },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.add))
                }
                Button(
                    onClick = {
                        dpm.setPermittedInputMethods(receiver, pkgList)
                        refresh()
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
        Notes(R.string.system_ime_always_allowed)
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Serializable private object KeepUninstalledPackages

@RequiresApi(28)
@Composable
private fun KeepUninstalledPackagesScreen(pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    val refresh = {
        pkgList.clear()
        dpm.getKeepUninstalledPackages(receiver)?.forEach { pkgList += it }
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.keep_uninstalled_packages), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        Column(modifier = Modifier.animateContentSize()) {
            if(pkgList.isEmpty()) Text(stringResource(R.string.none))
            for(i in pkgList) {
                ListItem(i) { pkgList -= i }
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { pkgList += pkgName },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add))
        }
        Button(
            onClick = {
                dpm.setKeepUninstalledPackages(receiver, pkgList)
                refresh()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_keep_uninstalled_apps)
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Serializable private object UninstallPackage

@Composable
private fun UninstallPackageScreen(pkgName: String) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.uninstall_app), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Column(modifier = Modifier.fillMaxWidth()) { 
            Button(
                onClick = {
                    val receiver = object : BroadcastReceiver() {
                        override fun onReceive(context: Context, intent: Intent) {
                            val statusExtra = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
                            if(statusExtra == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                                @SuppressWarnings("UnsafeIntentLaunch")
                                context.startActivity(intent.getParcelableExtra(Intent.EXTRA_INTENT) as Intent?)
                            } else {
                                context.unregisterReceiver(this)
                                if(statusExtra == PackageInstaller.STATUS_SUCCESS) {
                                    context.showOperationResultToast(true)
                                } else {
                                    AlertDialog.Builder(context)
                                        .setTitle(R.string.failure)
                                        .setMessage(parsePackageInstallerMessage(context, intent))
                                        .setPositiveButton(R.string.confirm) { dialog, _ -> dialog.dismiss() }
                                        .show()
                                }
                            }
                        }
                    }
                    ContextCompat.registerReceiver(
                        context, receiver, IntentFilter(AppInstallerViewModel.ACTION), null,
                        null, ContextCompat.RECEIVER_EXPORTED
                    )
                    val pi = if(VERSION.SDK_INT >= 34) {
                        PendingIntent.getBroadcast(
                            context, 0, Intent(AppInstallerViewModel.ACTION),
                            PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_MUTABLE
                        ).intentSender
                    } else {
                        PendingIntent.getBroadcast(context, 0, Intent(AppInstallerViewModel.ACTION), PendingIntent.FLAG_MUTABLE).intentSender
                    }
                    context.getPackageInstaller().uninstall(pkgName, pi)
                },
                enabled = pkgName != "",
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.silent_uninstall))
            }
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                    intent.setData(Uri.parse("package:$pkgName"))
                    context.startActivity(intent)
                },
                enabled = pkgName != "",
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.request_uninstall))
            }
        }
    }
}

