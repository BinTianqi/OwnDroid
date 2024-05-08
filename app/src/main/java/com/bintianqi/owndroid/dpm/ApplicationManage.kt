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
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build.VERSION
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.*
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.*
import com.bintianqi.owndroid.ui.theme.bgColor
import kotlinx.coroutines.delay
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors

private var credentialList = mutableSetOf<String>()
private var crossProfilePkg = mutableSetOf<String>()
private var keepUninstallPkg = mutableListOf<String>()
private var permittedIme = mutableListOf<String>()
private var permittedAccessibility = mutableListOf<String>()

@Composable
fun ApplicationManage(navCtrl:NavHostController){
    val focusMgr = LocalFocusManager.current
    var pkgName by rememberSaveable{ mutableStateOf("") }
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val titleMap = mapOf(
        "BlockUninstall" to R.string.block_uninstall,
        "UserControlDisabled" to R.string.ucd,
        "PermissionManage" to R.string.permission_manage,
        "CrossProfilePackage" to R.string.cross_profile_package,
        "CrossProfileWidget" to R.string.cross_profile_widget,
        "CredentialManagePolicy" to R.string.credential_manage_policy,
        "Accessibility" to R.string.permitted_accessibility_app,
        "IME" to R.string.permitted_ime,
        "KeepUninstalled" to R.string.keep_uninstalled_pkgs,
        "InstallApp" to R.string.install_app,
        "UninstallApp" to R.string.uninstall_app,
        "ClearAppData" to R.string.clear_app_data,
        "DefaultDialer" to R.string.set_default_dialer,
    )
    Scaffold(
        topBar = {
            /*TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.app_manage))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )*/
            TopBar(backStackEntry, navCtrl, localNavCtrl){Text(text = stringResource(titleMap[backStackEntry?.destination?.route] ?: R.string.app_manager))}
        }
    ){ paddingValues->
        Column(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())){
            LaunchedEffect(Unit) {
                while(true){
                    if(applySelectedPackage){ pkgName = selectedPackage; applySelectedPackage = false; applySelectedPermission = true}
                    delay(100)
                }
            }
            if(backStackEntry?.destination?.route!="InstallApp"){
                TextField(
                    value = pkgName,
                    onValueChange = { pkgName = it },
                    label = { Text(stringResource(R.string.package_name)) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    trailingIcon = {
                        Icon(painter = painterResource(R.drawable.checklist_fill0), contentDescription = null,
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = {navCtrl.navigate("PackageSelector")})
                                .padding(3.dp))
                    },
                    singleLine = true
                )
            }
            NavHost(
                navController = localNavCtrl, startDestination = "Home",
                enterTransition = Animations.navHostEnterTransition,
                exitTransition = Animations.navHostExitTransition,
                popEnterTransition = Animations.navHostPopEnterTransition,
                popExitTransition = Animations.navHostPopExitTransition,
                modifier = Modifier.background(bgColor)
            ){
                composable(route = "Home"){Home(localNavCtrl,pkgName)}
                composable(route = "BlockUninstall"){BlockUninstall(pkgName)}
                composable(route = "UserControlDisabled"){UserCtrlDisabledPkg(pkgName)}
                composable(route = "PermissionManage"){PermissionManage(pkgName,navCtrl)}
                composable(route = "CrossProfilePackage"){CrossProfilePkg(pkgName)}
                composable(route = "CrossProfileWidget"){CrossProfileWidget(pkgName)}
                composable(route = "CredentialManagePolicy"){CredentialManagePolicy(pkgName)}
                composable(route = "Accessibility"){PermittedAccessibility(pkgName)}
                composable(route = "IME"){PermittedIME(pkgName)}
                composable(route = "KeepUninstalled"){KeepUninstalledApp(pkgName)}
                composable(route = "InstallApp"){InstallApp()}
                composable(route = "UninstallApp"){UninstallApp(pkgName)}
                composable(route = "ClearAppData"){ClearAppData(pkgName)}
                composable(route = "DefaultDialer"){DefaultDialerApp(pkgName)}
            }
        }
    }
}

@Composable
private fun Home(navCtrl:NavHostController, pkgName: String){
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
        val myContext = LocalContext.current
        val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val myComponent = ComponentName(myContext, Receiver::class.java)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT>=24&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            Text(text = stringResource(R.string.scope_is_work_profile), textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth())
        }
        SubPageItem(R.string.app_info,"",R.drawable.open_in_new){
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            intent.setData(Uri.parse("package:$pkgName"))
            startActivity(myContext,intent,null)
        }
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SwitchItem(
                R.string.suspend,"",R.drawable.block_fill0,
                {
                    try{ myDpm.isPackageSuspended(myComponent,pkgName) }
                    catch(e:NameNotFoundException){ false }
                    catch(e:IllegalArgumentException){ false }
                },
                {myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName), it)}
            )
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            SwitchItem(
                R.string.hide, stringResource(R.string.isapphidden_desc),R.drawable.visibility_off_fill0,
                {myDpm.isApplicationHidden(myComponent,pkgName)},{myDpm.setApplicationHidden(myComponent, pkgName, it)}
            )
        }
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SwitchItem(
                R.string.always_on_vpn,"",R.drawable.vpn_key_fill0,{pkgName == myDpm.getAlwaysOnVpnPackage(myComponent)},
                {
                    try {
                        myDpm.setAlwaysOnVpnPackage(myComponent, pkgName, it)
                    } catch(e: UnsupportedOperationException) {
                        Toast.makeText(myContext, R.string.unsupported, Toast.LENGTH_SHORT).show()
                    } catch(e: NameNotFoundException) {
                        Toast.makeText(myContext, R.string.not_installed, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            SubPageItem(R.string.block_uninstall,"",R.drawable.delete_forever_fill0){navCtrl.navigate("BlockUninstall")}
        }
        if((VERSION.SDK_INT>=33&&isProfileOwner(myDpm))||(VERSION.SDK_INT>=30&&isDeviceOwner(myDpm))){
            SubPageItem(R.string.ucd,"",R.drawable.do_not_touch_fill0){navCtrl.navigate("UserControlDisabled")}
        }
        if(VERSION.SDK_INT>=23&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SubPageItem(R.string.permission_manage,"",R.drawable.key_fill0){navCtrl.navigate("PermissionManage")}
        }
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            SubPageItem(R.string.cross_profile_package,"",R.drawable.work_fill0){navCtrl.navigate("CrossProfilePackage")}
        }
        if(isProfileOwner(myDpm)){
            SubPageItem(R.string.cross_profile_widget,"",R.drawable.widgets_fill0){navCtrl.navigate("CrossProfileWidget")}
        }
        if(VERSION.SDK_INT>=34&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.credential_manage_policy,"",R.drawable.license_fill0){navCtrl.navigate("CredentialManagePolicy")}
        }
        if(isProfileOwner(myDpm)||isDeviceOwner(myDpm)){
            SubPageItem(R.string.permitted_accessibility_app,"",R.drawable.settings_accessibility_fill0){navCtrl.navigate("Accessibility")}
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            SubPageItem(R.string.permitted_ime,"",R.drawable.keyboard_fill0){navCtrl.navigate("IME")}
        }
        if(VERSION.SDK_INT>=28&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.keep_uninstalled_pkgs,"",R.drawable.delete_fill0){navCtrl.navigate("KeepUninstalled")}
        }
        if(VERSION.SDK_INT>=28){
            SubPageItem(R.string.clear_app_data,"",R.drawable.mop_fill0){navCtrl.navigate("ClearAppData")}
        }
        SubPageItem(R.string.install_app,"",R.drawable.install_mobile_fill0){navCtrl.navigate("InstallApp")}
        SubPageItem(R.string.uninstall_app,"",R.drawable.delete_fill0){navCtrl.navigate("UninstallApp")}
        if(VERSION.SDK_INT>=34){
            SubPageItem(R.string.set_default_dialer,"",R.drawable.call_fill0){navCtrl.navigate("DefaultDialer")}
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun UserCtrlDisabledPkg(pkgName:String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var pkgList = myDpm.getUserControlDisabledPackages(myComponent)
        var listText by remember{mutableStateOf("")}
        val refresh = {
            pkgList = myDpm.getUserControlDisabledPackages(myComponent)
            listText = pkgList.toText()
        }
        var inited by remember{mutableStateOf(false)}
        if(!inited){refresh();inited=true}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.ucd), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.ucd_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()){
            Text(text = if(listText==""){stringResource(R.string.none)}else{listText})
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    if(pkgName!=""){
                        pkgList.add(pkgName)
                        myDpm.setUserControlDisabledPackages(myComponent,pkgList)
                        refresh()
                    }else{
                        Toast.makeText(myContext, R.string.fail, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ){
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    val result = if(pkgName!=""){pkgList.remove(pkgName)}else{false}
                    if(result){
                        myDpm.setUserControlDisabledPackages(myComponent,pkgList)
                        refresh()
                    }else{
                        Toast.makeText(myContext, R.string.not_exist, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ){
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = { myDpm.setUserControlDisabledPackages(myComponent, listOf()); refresh() },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.clear_list))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun BlockUninstall(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var state by remember{mutableStateOf(myDpm.isUninstallBlocked(myComponent,pkgName))}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.block_uninstall), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(stringResource(R.string.current_state, stringResource(if(state){R.string.enabled}else{R.string.disabled})))
        Spacer(Modifier.padding(vertical = 3.dp))
        Text(text = stringResource(R.string.sometimes_get_wrong_block_uninstall_state))
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    myDpm.setUninstallBlocked(myComponent,pkgName,true)
                    Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
                    state = myDpm.isUninstallBlocked(myComponent,pkgName)
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.enable))
            }
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    myDpm.setUninstallBlocked(myComponent,pkgName,false)
                    Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
                    state = myDpm.isUninstallBlocked(myComponent,pkgName)
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ){
                Text(stringResource(R.string.disable))
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun PermissionManage(pkgName: String, navCtrl: NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    var inputPermission by remember{mutableStateOf(selectedPermission)}
    var currentState by remember{mutableStateOf(myContext.getString(R.string.unknown))}
    val grantState = mapOf(
        PERMISSION_GRANT_STATE_DEFAULT to stringResource(R.string.decide_by_user),
        PERMISSION_GRANT_STATE_GRANTED to stringResource(R.string.granted),
        PERMISSION_GRANT_STATE_DENIED to stringResource(R.string.denied)
    )
    LaunchedEffect(Unit) {
        while(true){
            if(applySelectedPermission){inputPermission = selectedPermission; applySelectedPermission = false}
            delay(100)
        }
    }
    LaunchedEffect(pkgName) {
        if(pkgName!=""){currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]!!}
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permission_manage), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputPermission,
            label = { Text(stringResource(R.string.permission))},
            onValueChange = {
                inputPermission = it; selectedPermission = inputPermission
                currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]!!
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth(),
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.checklist_fill0), contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(onClick = {navCtrl.navigate("PermissionPicker")})
                        .padding(3.dp))
            }
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(stringResource(R.string.current_state, currentState))
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    myDpm.setPermissionGrantState(myComponent,pkgName,inputPermission, PERMISSION_GRANT_STATE_GRANTED)
                    currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]!!
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.grant))
            }
            Button(
                onClick = {
                    myDpm.setPermissionGrantState(myComponent,pkgName,inputPermission, PERMISSION_GRANT_STATE_DENIED)
                    currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]!!
                },
                Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.deny))
            }
        }
        Button(
            onClick = {
                myDpm.setPermissionGrantState(myComponent,pkgName,inputPermission, PERMISSION_GRANT_STATE_DEFAULT)
                currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]!!
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.decide_by_user))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun CrossProfilePkg(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.cross_profile_package), style = typography.headlineLarge)
        var list by remember{mutableStateOf("")}
        val refresh = {
            crossProfilePkg = myDpm.getCrossProfilePackages(myComponent)
            list = crossProfilePkg.toText()
        }
        LaunchedEffect(Unit){refresh()}
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()){
            Text(text = if(list==""){stringResource(R.string.none)}else{list})
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    if(pkgName!=""){
                        crossProfilePkg.add(pkgName)}
                    myDpm.setCrossProfilePackages(myComponent, crossProfilePkg)
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    if(pkgName!=""){
                        crossProfilePkg.remove(pkgName)}
                    myDpm.setCrossProfilePackages(myComponent, crossProfilePkg)
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun CrossProfileWidget(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var pkgList: MutableList<String>
        var list by remember{mutableStateOf("")}
        val refresh = {
            pkgList = myDpm.getCrossProfileWidgetProviders(myComponent)
            list = pkgList.toText()
        }
        LaunchedEffect(Unit){refresh()}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.cross_profile_widget), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()){
            Text(text = if(list==""){stringResource(R.string.none)}else{list})
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    if(pkgName!=""){myDpm.addCrossProfileWidgetProvider(myComponent,pkgName)}
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ){
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    if(pkgName!=""){myDpm.removeCrossProfileWidgetProvider(myComponent,pkgName)}
                    refresh()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ){
                Text(stringResource(R.string.remove))
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun CredentialManagePolicy(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    var policy:PackagePolicy?
    var policyType by remember{mutableIntStateOf(-1)}
    var credentialListText by remember{mutableStateOf("")}
    val refreshPolicy = {
        policy = myDpm.credentialManagerPolicy
        policyType = policy?.policyType ?: -1
        credentialList = policy?.packageNames ?: mutableSetOf()
        credentialList = credentialList.toMutableSet()
    }
    LaunchedEffect(Unit){refreshPolicy(); credentialListText = credentialList.toText()}
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.credential_manage_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(stringResource(R.string.none),{policyType==-1},{policyType=-1})
        RadioButtonItem(stringResource(R.string.blacklist),{policyType==PACKAGE_POLICY_BLOCKLIST},{policyType=PACKAGE_POLICY_BLOCKLIST})
        RadioButtonItem(stringResource(R.string.whitelist),{policyType==PACKAGE_POLICY_ALLOWLIST},{policyType=PACKAGE_POLICY_ALLOWLIST})
        RadioButtonItem(stringResource(R.string.whitelist_and_system_app),{policyType==PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM},{policyType=PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM})
        Spacer(Modifier.padding(vertical = 5.dp))
        AnimatedVisibility(policyType!=-1) {
            Column {
                Text(stringResource(R.string.app_list_is))
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                    Text(text = if(credentialListText!=""){ credentialListText }else{ stringResource(R.string.none) })
                }
                Spacer(Modifier.padding(vertical = 10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            if(pkgName!=""){
                                credentialList.add(pkgName)}
                            credentialListText = credentialList.toText()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick = {
                            if(pkgName!=""){
                                credentialList.remove(pkgName)}
                            credentialListText = credentialList.toText()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                }
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                try{
                    if(policyType!=-1&&credentialList.isNotEmpty()){
                        myDpm.credentialManagerPolicy = PackagePolicy(policyType, credentialList)
                    }else{
                        myDpm.credentialManagerPolicy = null
                    }
                    Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
                }catch(e:java.lang.IllegalArgumentException){
                    Toast.makeText(myContext, R.string.fail, Toast.LENGTH_SHORT).show()
                }finally {
                    refreshPolicy()
                    credentialListText = credentialList.toText()
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
private fun PermittedAccessibility(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permitted_accessibility_app), style = typography.headlineLarge)
        var listText by remember{ mutableStateOf("") }
        LaunchedEffect(Unit){
            val getList = myDpm.getPermittedAccessibilityServices(myComponent)
            if(getList!=null){ permittedAccessibility = getList }
            listText = permittedAccessibility.toText()
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize()){
            Text(text = if(listText==""){stringResource(R.string.none)}else{listText})
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = { permittedAccessibility.add(pkgName); listText = permittedAccessibility.toText()},
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = { permittedAccessibility.remove(pkgName); listText = permittedAccessibility.toText() },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                Toast.makeText(
                    myContext,
                    if(myDpm.setPermittedAccessibilityServices(myComponent, permittedAccessibility)){R.string.success}else{R.string.fail},
                    Toast.LENGTH_SHORT
                ).show()
                val getList = myDpm.getPermittedAccessibilityServices(myComponent)
                if(getList!=null){ permittedAccessibility = getList }
                listText = permittedAccessibility.toText()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun PermittedIME(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permitted_ime), style = typography.headlineLarge)
        var imeListText by remember{ mutableStateOf("") }
        LaunchedEffect(Unit){
            val getList = myDpm.getPermittedInputMethods(myComponent)
            if(getList!=null){ permittedIme = getList }
            imeListText = permittedIme.toText()
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
            Text(text = if(imeListText==""){stringResource(R.string.none)}else{imeListText})
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = { permittedIme.add(pkgName); imeListText = permittedIme.toText() },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = { permittedIme.remove(pkgName); imeListText = permittedIme.toText() },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                Toast.makeText(
                    myContext,
                    if(myDpm.setPermittedInputMethods(myComponent, permittedIme)){R.string.success}else{R.string.fail},
                    Toast.LENGTH_SHORT
                ).show()
                val getList = myDpm.getPermittedInputMethods(myComponent)
                if(getList!=null){ permittedIme = getList }
                imeListText = permittedIme.toText()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun KeepUninstalledApp(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.keep_uninstalled_pkgs), style = typography.headlineLarge)
        var listText by remember{mutableStateOf("")}
        LaunchedEffect(Unit){
            val getList = myDpm.getKeepUninstalledPackages(myComponent)
            if(getList!=null){ keepUninstallPkg = getList }
            listText = keepUninstallPkg.toText()
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_list_is))
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
            Text(text = if(listText==""){stringResource(R.string.none)}else{listText})
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    keepUninstallPkg.add(pkgName)
                    listText = keepUninstallPkg.toText()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ){
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    keepUninstallPkg.remove(pkgName)
                    listText = keepUninstallPkg.toText()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ){
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                myDpm.setKeepUninstalledPackages(myComponent, keepUninstallPkg)
                val getList = myDpm.getKeepUninstalledPackages(myComponent)
                if(getList!=null){ keepUninstallPkg = getList }
                listText = keepUninstallPkg.toText()
                Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun UninstallApp(pkgName: String){
    val myContext = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.uninstall_app), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Column(modifier = Modifier.fillMaxWidth()){
            Button(
                onClick = {
                    val intent = Intent(myContext, PackageInstallerReceiver::class.java)
                    val intentSender = PendingIntent.getBroadcast(myContext, 8, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
                    val pkgInstaller = myContext.packageManager.packageInstaller
                    pkgInstaller.uninstall(pkgName, intentSender)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.silent_uninstall))
            }
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                    intent.setData(Uri.parse("package:$pkgName"))
                    myContext.startActivity(intent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.request_uninstall))
            }
        }
    }
}

@Composable
private fun InstallApp(){
    val myContext = LocalContext.current
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.install_app), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                val installApkIntent = Intent(Intent.ACTION_GET_CONTENT)
                installApkIntent.setType("application/vnd.android.package-archive")
                installApkIntent.addCategory(Intent.CATEGORY_OPENABLE)
                getApk.launch(installApkIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_apk))
        }
        var selected by remember{mutableStateOf(false)}
        LaunchedEffect(selected){while(true){ delay(800); selected = apkUri!=null}}
        AnimatedVisibility(selected) {
            Spacer(Modifier.padding(vertical = 3.dp))
            Column(modifier = Modifier.fillMaxWidth()){
                Button(
                    onClick = { uriToStream(myContext, apkUri){stream -> installPackage(myContext,stream)} },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.silent_install))
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                        intent.setData(apkUri)
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        myContext.startActivity(intent)
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
private fun ClearAppData(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Button(
            onClick = {
                val executor = Executors.newCachedThreadPool()
                val onClear = DevicePolicyManager.OnClearApplicationUserDataListener { pkg: String, succeed: Boolean ->
                    Looper.prepare()
                    focusMgr.clearFocus()
                    val toastText = if(pkg!=""){"$pkg\n"}else{""} + myContext.getString(R.string.clear_data) + myContext.getString(if(succeed){R.string.success}else{R.string.fail})
                    Toast.makeText(myContext, toastText, Toast.LENGTH_SHORT).show()
                    Looper.loop()
                }
                myDpm.clearApplicationUserData(myComponent,pkgName,executor,onClear)
            },
            enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ) {
            Text(stringResource(R.string.clear_app_data))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun DefaultDialerApp(pkgName: String){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Button(
            onClick = {
                try{
                    myDpm.setDefaultDialerApplication(pkgName)
                    Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
                }catch(e:IllegalArgumentException){
                    Toast.makeText(myContext, R.string.fail, Toast.LENGTH_SHORT).show()
                }
            },
            enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
        ) {
            Text(stringResource(R.string.set_default_dialer))
        }
    }
}

@Throws(IOException::class)
private fun installPackage(context: Context, inputStream: InputStream){
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)
    val out = session.openWrite("COSU", 0, -1)
    val buffer = ByteArray(65536)
    var c: Int
    while(inputStream.read(buffer).also{c = it}!=-1) { out.write(buffer, 0, c) }
    session.fsync(out)
    inputStream.close()
    out.close()
    val intent = Intent(context, PackageInstallerReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, sessionId, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
    session.commit(pendingIntent)
}
