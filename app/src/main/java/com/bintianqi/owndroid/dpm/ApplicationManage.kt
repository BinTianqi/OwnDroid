package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
import android.app.admin.PackagePolicy
import android.app.admin.PackagePolicy.PACKAGE_POLICY_ALLOWLIST
import android.app.admin.PackagePolicy.PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM
import android.app.admin.PackagePolicy.PACKAGE_POLICY_BLOCKLIST
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build.VERSION
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.collectAsState
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
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.InstallAppActivity
import com.bintianqi.owndroid.PackageInstallerReceiver
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.fileUriFlow
import com.bintianqi.owndroid.getFile
import com.bintianqi.owndroid.selectedPackage
import com.bintianqi.owndroid.toText
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.Information
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.SwitchItem
import java.util.concurrent.Executors

private var dialogConfirmButtonAction = {}
private var dialogDismissButtonAction = {}
private var dialogGetStatus = { false }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationManage(navCtrl:NavHostController, dialogStatus: MutableIntState) {
    val focusMgr = LocalFocusManager.current
    val localNavCtrl = rememberNavController()
    var pkgName by rememberSaveable { mutableStateOf("") }
    val updatePackage by selectedPackage.collectAsState()
    LaunchedEffect(updatePackage) {
        if(updatePackage != "") {
            pkgName = updatePackage
            selectedPackage.value = ""
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = pkgName,
                        onValueChange = { pkgName = it },
                        label = { Text(stringResource(R.string.package_name)) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        trailingIcon = {
                            Icon(painter = painterResource(R.drawable.checklist_fill0), contentDescription = null,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable(onClick = {
                                        focusMgr.clearFocus()
                                        navCtrl.navigate("PackageSelector")
                                    })
                                    .padding(3.dp))
                        },
                        textStyle = typography.bodyLarge,
                        singleLine = true
                    )
                },
                navigationIcon = { NavIcon { navCtrl.navigateUp() } }
            )
        }
    ) {  paddingValues->
        NavHost(
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition
        ) {
            composable(route = "Home") {
                Home(localNavCtrl, pkgName, dialogStatus)
            }
            composable(route = "AlwaysOnVpn") { AlwaysOnVPNPackage(pkgName) }
            composable(route = "UserControlDisabled") { UserCtrlDisabledPkg(pkgName) }
            composable(route = "PermissionManage") { PermissionManage(pkgName) }
            composable(route = "CrossProfilePackage") { CrossProfilePkg(pkgName) }
            composable(route = "CrossProfileWidget") { CrossProfileWidget(pkgName) }
            composable(route = "CredentialManagePolicy") { CredentialManagePolicy(pkgName) }
            composable(route = "Accessibility") { PermittedAccessibility(pkgName) }
            composable(route = "IME") { PermittedIME(pkgName) }
            composable(route = "KeepUninstalled") { KeepUninstalledApp(pkgName) }
            composable(route = "InstallApp") { InstallApp() }
            composable(route = "UninstallApp") { UninstallApp(pkgName) }
        }
    }
    when(dialogStatus.intValue) {
        0 -> {}
        4 -> EnableSystemAppDialog(dialogStatus, pkgName)
        5 -> ClearAppDataDialog(dialogStatus, pkgName)
        6 -> DefaultDialerAppDialog(dialogStatus, pkgName)
        else -> AppControlDialog(dialogStatus)
    }
    LaunchedEffect(dialogStatus.intValue) {
        focusMgr.clearFocus()
    }
}

@Composable
private fun Home(
    navCtrl:NavHostController,
    pkgName: String,
    dialogStatus: MutableIntState
) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sharedPrefs = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 24 && profileOwner && dpm.isManagedProfile(receiver)) {
            Text(text = stringResource(R.string.scope_is_work_profile), textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth())
        }
        SubPageItem(R.string.app_info,"", R.drawable.open_in_new) { 
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.parse("package:$pkgName"))
            startActivity(context, intent, null)
        }
        if(VERSION.SDK_INT >= 24 && (deviceOwner || profileOwner)) {
            val getSuspendStatus = {
                try{ dpm.isPackageSuspended(receiver, pkgName) }
                catch(e:NameNotFoundException) { false }
                catch(e:IllegalArgumentException) { false }
            }
            SwitchItem(
                title = R.string.suspend, desc = "", icon = R.drawable.block_fill0,
                getState = getSuspendStatus,
                onCheckedChange = { dpm.setPackagesSuspended(receiver, arrayOf(pkgName), it) },
                onClickBlank = {
                    dialogGetStatus = getSuspendStatus
                    dialogConfirmButtonAction = { dpm.setPackagesSuspended(receiver, arrayOf(pkgName), true) }
                    dialogDismissButtonAction = { dpm.setPackagesSuspended(receiver, arrayOf(pkgName), false) }
                    dialogStatus.intValue = 1
                }
            )
        }
        if(deviceOwner || profileOwner) {
            SwitchItem(
                title = R.string.hide, desc = stringResource(R.string.isapphidden_desc), icon = R.drawable.visibility_off_fill0,
                getState = { dpm.isApplicationHidden(receiver,pkgName) },
                onCheckedChange = { dpm.setApplicationHidden(receiver, pkgName, it) },
                onClickBlank = {
                    dialogGetStatus = { dpm.isApplicationHidden(receiver,pkgName) }
                    dialogConfirmButtonAction = { dpm.setApplicationHidden(receiver, pkgName, true) }
                    dialogDismissButtonAction = { dpm.setApplicationHidden(receiver, pkgName, false) }
                    dialogStatus.intValue = 2
                }
            )
        }
        if(deviceOwner || profileOwner) {
            SwitchItem(
                title = R.string.block_uninstall, desc = "", icon = R.drawable.delete_forever_fill0,
                getState = { dpm.isUninstallBlocked(receiver,pkgName) },
                onCheckedChange = { dpm.setUninstallBlocked(receiver,pkgName,it) },
                onClickBlank = {
                    dialogGetStatus = { dpm.isUninstallBlocked(receiver,pkgName) }
                    dialogConfirmButtonAction = { dpm.setUninstallBlocked(receiver,pkgName,true) }
                    dialogDismissButtonAction = { dpm.setUninstallBlocked(receiver,pkgName,false) }
                    dialogStatus.intValue = 3
                }
            )
        }
        if(VERSION.SDK_INT>=24 && (deviceOwner || profileOwner)) {
            SubPageItem(R.string.always_on_vpn, "", R.drawable.vpn_key_fill0) { navCtrl.navigate("AlwaysOnVpn") }
        }
        if((VERSION.SDK_INT>=33&&profileOwner)||(VERSION.SDK_INT>=30&&deviceOwner)) { 
            SubPageItem(R.string.ucd, "", R.drawable.do_not_touch_fill0) { navCtrl.navigate("UserControlDisabled") }
        }
        if(VERSION.SDK_INT>=23&&(deviceOwner||profileOwner)) { 
            SubPageItem(R.string.permission_manage, "", R.drawable.key_fill0) { navCtrl.navigate("PermissionManage") }
        }
        if(VERSION.SDK_INT >= 30 && profileOwner && dpm.isManagedProfile(receiver)) {
            SubPageItem(R.string.cross_profile_package, "", R.drawable.work_fill0) { navCtrl.navigate("CrossProfilePackage") }
        }
        if(profileOwner) { 
            SubPageItem(R.string.cross_profile_widget, "", R.drawable.widgets_fill0) { navCtrl.navigate("CrossProfileWidget") }
        }
        if(VERSION.SDK_INT >= 34 && deviceOwner) {
            SubPageItem(R.string.credential_manage_policy, "", R.drawable.license_fill0) { navCtrl.navigate("CredentialManagePolicy") }
        }
        if(profileOwner || deviceOwner) {
            SubPageItem(R.string.permitted_accessibility_services, "", R.drawable.settings_accessibility_fill0) { navCtrl.navigate("Accessibility") }
        }
        if(deviceOwner || profileOwner) {
            SubPageItem(R.string.permitted_ime, "", R.drawable.keyboard_fill0) { navCtrl.navigate("IME") }
        }
        if(deviceOwner || profileOwner) {
            SubPageItem(R.string.enable_system_app, "", R.drawable.enable_fill0) {
                if(pkgName != "") dialogStatus.intValue = 4
            }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            SubPageItem(R.string.keep_uninstalled_packages, "", R.drawable.delete_fill0) { navCtrl.navigate("KeepUninstalled") }
        }
        if(VERSION.SDK_INT >= 28 && (deviceOwner || profileOwner)) {
            SubPageItem(R.string.clear_app_storage, "", R.drawable.mop_fill0) {
                if(pkgName != "") dialogStatus.intValue = 5
            }
        }
        if(!sharedPrefs.getBoolean("dhizuku", false)) {
            SubPageItem(R.string.install_app, "", R.drawable.install_mobile_fill0) { navCtrl.navigate("InstallApp") }
        }
        SubPageItem(R.string.uninstall_app, "", R.drawable.delete_fill0) { navCtrl.navigate("UninstallApp") }
        if(VERSION.SDK_INT >= 34 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.set_default_dialer, "", R.drawable.call_fill0) {
                if(pkgName != "") dialogStatus.intValue = 6
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
        LaunchedEffect(Unit) { fileUriFlow.value = Uri.parse("") }
    }
}

@SuppressLint("NewApi")
@Composable
fun AlwaysOnVPNPackage(pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var lockdown by remember { mutableStateOf(false) }
    var pkg by remember { mutableStateOf<String?>("") }
    val refresh = { pkg = dpm.getAlwaysOnVpnPackage(receiver) }
    LaunchedEffect(Unit) { refresh() }
    val setAlwaysOnVpn: (String?, Boolean)->Unit = { vpnPkg: String?, lockdownEnabled: Boolean ->
        try {
            dpm.setAlwaysOnVpnPackage(receiver, vpnPkg, lockdownEnabled)
            Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
        } catch(e: UnsupportedOperationException) {
            Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
        } catch(e: NameNotFoundException) {
            Toast.makeText(context, R.string.not_installed, Toast.LENGTH_SHORT).show()
        }
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.always_on_vpn), style = typography.headlineLarge, modifier = Modifier.padding(vertical = 8.dp))
        Text(text = stringResource(R.string.current_app_is) + pkg, modifier = Modifier.padding(vertical = 8.dp))
        SwitchItem(R.string.enable_lockdown, "", null, { lockdown }, { lockdown = it }, padding = false)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { setAlwaysOnVpn(pkgName, lockdown); refresh() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { setAlwaysOnVpn(null, false); refresh() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_current_config))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun UserCtrlDisabledPkg(pkgName:String) { 
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        val refresh = {
            pkgList.clear()
            dpm.getUserControlDisabledPackages(receiver).forEach { pkgList.add(it) }
        }
        LaunchedEffect(Unit) { refresh() }
        var inited by remember{mutableStateOf(false)}
        if(!inited) { refresh();inited=true }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.ucd), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.ucd_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()) { 
            Text(text = if(pkgList.isEmpty()) stringResource(R.string.none) else pkgList.toText())
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { 
            Button(
                onClick = {
                    pkgList.add(pkgName)
                    dpm.setUserControlDisabledPackages(receiver, pkgList)
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) { 
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    pkgList.remove(pkgName)
                    dpm.setUserControlDisabledPackages(receiver,pkgList)
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) { 
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = { dpm.setUserControlDisabledPackages(receiver, listOf()); refresh() },
            modifier = Modifier.fillMaxWidth()
        ) { 
            Text(stringResource(R.string.clear_list))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun PermissionManage(pkgName: String) {
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
                        GrantPermissionItem(R.string.grant, PERMISSION_GRANT_STATE_GRANTED)
                    }
                    GrantPermissionItem(R.string.deny, PERMISSION_GRANT_STATE_DENIED)
                    GrantPermissionItem(R.string.default_stringres, PERMISSION_GRANT_STATE_DEFAULT)
                }
            }
        )
    }
}

@SuppressLint("NewApi")
@Composable
private fun CrossProfilePkg(pkgName: String) { 
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val crossProfilePkg = remember { mutableStateListOf<String>() }
    val refresh = {
        crossProfilePkg.clear()
        dpm.getCrossProfilePackages(receiver).forEach { crossProfilePkg += it }
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.cross_profile_package), style = typography.headlineLarge)
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()) { 
            Text(text = if(crossProfilePkg.isEmpty()) stringResource(R.string.none) else crossProfilePkg.toText())
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { 
            Button(
                onClick = {
                    crossProfilePkg.add(pkgName)
                    dpm.setCrossProfilePackages(receiver, crossProfilePkg.toSet())
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    crossProfilePkg.remove(pkgName)
                    dpm.setCrossProfilePackages(receiver, crossProfilePkg.toSet())
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                crossProfilePkg.clear()
                dpm.setCrossProfilePackages(receiver, crossProfilePkg.toSet())
                refresh()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_list))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun CrossProfileWidget(pkgName: String) { 
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    val refresh = {
        pkgList.clear()
        dpm.getCrossProfileWidgetProviders(receiver).forEach {
            pkgList += it
        }
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.cross_profile_widget), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()) { 
            Text(text = if(pkgList.isEmpty()) stringResource(R.string.none) else pkgList.toText())
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { 
            Button(
                onClick = {
                    if(pkgName != "") { dpm.addCrossProfileWidgetProvider(receiver, pkgName) }
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) { 
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    if(pkgName != "") { dpm.removeCrossProfileWidgetProvider(receiver, pkgName) }
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) { 
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                pkgList.forEach {
                    dpm.removeCrossProfileWidgetProvider(receiver, it)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_list))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun CredentialManagePolicy(pkgName: String) { 
    val context = LocalContext.current
    val dpm = context.getDPM()
    var policy: PackagePolicy?
    var policyType by remember{ mutableIntStateOf(-1) }
    val credentialList = remember { mutableStateListOf<String>() }
    val refreshPolicy = {
        policy = dpm.credentialManagerPolicy
        policyType = policy?.policyType ?: -1
        (policy?.packageNames ?: mutableSetOf()).forEach { credentialList += it }
    }
    val apply = {
        try {
            if(policyType != -1 && credentialList.isNotEmpty()) {
                dpm.credentialManagerPolicy = PackagePolicy(policyType, credentialList.toSet())
            }else{
                dpm.credentialManagerPolicy = null
            }
            Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
        } catch(e:java.lang.IllegalArgumentException) {
            Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
        } finally {
            refreshPolicy()
        }
    }
    LaunchedEffect(Unit) { refreshPolicy() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.credential_manage_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(
            R.string.none,
            policyType == -1, { policyType = -1 }
        )
        RadioButtonItem(
            R.string.blacklist,
            policyType == PACKAGE_POLICY_BLOCKLIST,
            { policyType = PACKAGE_POLICY_BLOCKLIST }
        )
        RadioButtonItem(
            R.string.whitelist,
            policyType == PACKAGE_POLICY_ALLOWLIST,
            { policyType = PACKAGE_POLICY_ALLOWLIST }
        )
        RadioButtonItem(
            R.string.whitelist_and_system_app,
            policyType == PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM,
            { policyType = PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM }
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        AnimatedVisibility(policyType != -1) {
            Column {
                Text(stringResource(R.string.app_list_is))
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()) {
                    Text(text = if(credentialList.isEmpty()) stringResource(R.string.none) else credentialList.toText())
                }
                Spacer(Modifier.padding(vertical = 10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { 
                    Button(
                        onClick = {
                            credentialList.add(pkgName)
                            apply()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick = {
                            credentialList.remove(pkgName)
                            apply()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                }
                Button(
                    onClick = {
                        credentialList.clear()
                        apply()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.clear_list))
                }
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun PermittedAccessibility(pkgName: String) { 
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val pkgList = remember { mutableStateListOf<String>() }
    var allowAll by remember { mutableStateOf(false) }
    val refresh = {
        pkgList.clear()
        val getList = dpm.getPermittedAccessibilityServices(receiver)
        if(getList != null) {
            allowAll = false
            getList.forEach { pkgList += it }
        } else {
            allowAll = true
        }
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
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()) {
                    if (pkgList.isEmpty()) {
                        Text(stringResource(R.string.only_system_accessibility_allowed))
                    } else {
                        Text(stringResource(R.string.permitted_packages_is) + pkgList.toText())
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = {
                            pkgList.add(pkgName)
                            dpm.setPermittedAccessibilityServices(receiver, pkgList)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick = {
                            pkgList.remove(pkgName)
                            dpm.setPermittedAccessibilityServices(receiver, pkgList)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                }
                Button(
                    onClick = {
                        pkgList.clear()
                        dpm.setPermittedAccessibilityServices(receiver, pkgList)
                        refresh()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.clear_list))
                }
            }
        }
        Information {
            Text(stringResource(R.string.system_accessibility_always_allowed))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun PermittedIME(pkgName: String) { 
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val permittedIme = remember { mutableStateListOf<String>() }
    var allowAll by remember { mutableStateOf(false) }
    val refresh = {
        permittedIme.clear()
        val getList = dpm.getPermittedInputMethods(receiver)
        if(getList != null) {
            allowAll = false
            getList.forEach { permittedIme += it }
        } else {
            allowAll = true
        }
    }
    LaunchedEffect(Unit) { refresh() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permitted_ime), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        SwitchItem(
            R.string.allow_all, "", null, { allowAll },
            {
                dpm.setPermittedInputMethods(receiver, if(it) null else listOf())
                refresh()
            }, padding = false
        )
        AnimatedVisibility(!allowAll) {
            Column {
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()) {
                    if(permittedIme.isEmpty()) {
                        Text(stringResource(R.string.only_system_ime_allowed))
                    } else {
                        Text(stringResource(R.string.permitted_packages_is) + permittedIme.toText())
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = {
                            permittedIme.add(pkgName)
                            dpm.setPermittedInputMethods(receiver, permittedIme)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick = {
                            permittedIme.remove(pkgName)
                            dpm.setPermittedInputMethods(receiver, permittedIme)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                }
                Button(
                    onClick = {
                        permittedIme.clear()
                        dpm.setPermittedInputMethods(receiver, permittedIme)
                        refresh()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.clear_list))
                }
            }
        }
        Information {
            Text(stringResource(R.string.system_ime_always_allowed))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun KeepUninstalledApp(pkgName: String) { 
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
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()) {
            Text(text = if(pkgList.isEmpty()) stringResource(R.string.none) else pkgList.toText())
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { 
            Button(
                onClick = {
                    pkgList.add(pkgName)
                    dpm.setKeepUninstalledPackages(receiver, pkgList)
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) { 
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    pkgList.remove(pkgName)
                    dpm.setKeepUninstalledPackages(receiver, pkgList)
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) { 
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                pkgList.clear()
                dpm.setKeepUninstalledPackages(receiver, pkgList)
                refresh()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_list))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun UninstallApp(pkgName: String) { 
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.uninstall_app), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Column(modifier = Modifier.fillMaxWidth()) { 
            Button(
                onClick = {
                    val intent = Intent(context, PackageInstallerReceiver::class.java)
                    val intentSender = PendingIntent.getBroadcast(context, 8, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
                    val pkgInstaller = context.getPI()
                    pkgInstaller.uninstall(pkgName, intentSender)
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

@Composable
private fun InstallApp() { 
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val selected = fileUriFlow.collectAsState().value != Uri.parse("")
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) { 
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.install_app), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                val installApkIntent = Intent(Intent.ACTION_GET_CONTENT)
                installApkIntent.setType("application/vnd.android.package-archive")
                installApkIntent.addCategory(Intent.CATEGORY_OPENABLE)
                getFile.launch(installApkIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_apk))
        }
        AnimatedVisibility(selected) {
            Spacer(Modifier.padding(vertical = 3.dp))
            Column(modifier = Modifier.fillMaxWidth()) { 
                Button(
                    onClick = {
                        val intent = Intent(context, InstallAppActivity::class.java)
                        intent.data = fileUriFlow.value
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.silent_install))
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                        intent.setData(fileUriFlow.value)
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.request_install))
                }
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun ClearAppDataDialog(status: MutableIntState, pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    AlertDialog(
        title = { Text(text = stringResource(R.string.clear_app_storage)) },
        text = {
            Text(stringResource(R.string.app_storage_will_be_cleared) + "\n" + pkgName)
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val executor = Executors.newCachedThreadPool()
                    val onClear = DevicePolicyManager.OnClearApplicationUserDataListener { pkg: String, succeed: Boolean ->
                        Looper.prepare()
                        val toastText =
                            if(pkg!="") { "$pkg\n" }else{ "" } +
                                    context.getString(R.string.clear_data) +
                                    context.getString(if(succeed) R.string.success else R.string.failed )
                        Toast.makeText(context, toastText, Toast.LENGTH_SHORT).show()
                        Looper.loop()
                    }
                    dpm.clearApplicationUserData(receiver, pkgName, executor, onClear)
                    status.intValue = 0
                },
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(text = stringResource(R.string.clear))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { status.intValue = 0 }
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { status.intValue = 0 },
        modifier = Modifier.fillMaxWidth()
    )
}

@SuppressLint("NewApi")
@Composable
private fun DefaultDialerAppDialog(status: MutableIntState, pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    AlertDialog(
        title = { Text(stringResource(R.string.set_default_dialer)) },
        text = {
            Text(stringResource(R.string.app_will_be_default_dialer) + "\n" + pkgName)
        },
        onDismissRequest = { status.intValue = 0 },
        dismissButton = {
            TextButton(onClick = { status.intValue = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try{
                        dpm.setDefaultDialerApplication(pkgName)
                        Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                    }catch(e:IllegalArgumentException) {
                        Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                    status.intValue = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun EnableSystemAppDialog(status: MutableIntState, pkgName: String) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    AlertDialog(
        title = { Text(stringResource(R.string.enable_system_app)) },
        text = {
            Text(stringResource(R.string.enable_system_app_desc) + "\n" + pkgName)
        },
        onDismissRequest = { status.intValue = 0 },
        dismissButton = {
            TextButton(onClick = { status.intValue = 0 }) {
                Text(stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    try {
                        dpm.enableSystemApp(receiver, pkgName)
                        Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                    } catch(e: IllegalArgumentException) {
                        Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
                    }
                    status.intValue = 0
                }
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun AppControlDialog(status: MutableIntState) {
    val enabled = dialogGetStatus()
    AlertDialog(
        onDismissRequest = { status.intValue = 0 },
        title = {
            Text(
                text = stringResource(
                    when(status.intValue) {
                        1 -> R.string.suspend
                        2 -> R.string.hide
                        3 -> R.string.block_uninstall
                        4 -> R.string.always_on_vpn
                        else -> R.string.unknown
                    }
                ),
                style = typography.headlineMedium,
                modifier = Modifier.padding(start = 5.dp)
            )
        },
        text = {
            Text(
                text = stringResource(R.string.current_status_is) + stringResource(if(enabled) R.string.enabled else R.string.disabled),
                modifier = Modifier.padding(start = 5.dp, top = 5.dp, bottom = 5.dp)
            )
        },
        confirmButton = {
            TextButton(
                onClick = { dialogConfirmButtonAction(); status.intValue = 0 }
            ) {
                Text(text = stringResource(R.string.enable))
            }
        },
        dismissButton = {
            TextButton(
                onClick = { dialogDismissButtonAction(); status.intValue = 0 }
            ) {
                Text(text = stringResource(R.string.disable))
            }
        }
    )
}
