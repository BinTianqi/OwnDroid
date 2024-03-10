package com.binbin.androidowner.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.binbin.androidowner.R
import com.binbin.androidowner.ui.Animations
import com.binbin.androidowner.ui.Information
import com.binbin.androidowner.ui.NavIcon
import com.binbin.androidowner.ui.SubPageItem
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DpmPermissions(navCtrl:NavHostController){
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val titleMap = mapOf(
        "Home" to R.string.permission,
        "Shizuku" to R.string.shizuku,
        "DeviceAdmin" to R.string.device_admin,
        "ProfileOwner" to R.string.profile_owner,
        "DeviceOwner" to R.string.device_owner,
        "DeviceInfo" to R.string.device_info,
        "SpecificID" to R.string.enrollment_specific_id,
        "OrgName" to R.string.org_name,
        "NoManagementAccount" to R.string.account_types_management_disabled,
        "LockScreenInfo" to R.string.owner_lockscr_info,
        "SupportMsg" to R.string.support_msg,
        "TransformOwnership" to R.string.transform_ownership
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.permission))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )
        }
    ){
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations().navHostEnterTransition,
            exitTransition = Animations().navHostExitTransition,
            popEnterTransition = Animations().navHostPopEnterTransition,
            popExitTransition = Animations().navHostPopExitTransition,
            modifier = Modifier
                .background(color = if(isSystemInDarkTheme()) { colorScheme.background }else{ colorScheme.primary.copy(alpha = 0.05F) })
                .padding(top = it.calculateTopPadding())
        ){
            composable(route = "Home"){Home(localNavCtrl)}
            composable(route = "Shizuku"){ShizukuActivate()}
            composable(route = "DeviceAdmin"){DeviceAdmin(navCtrl)}
            composable(route = "ProfileOwner"){ProfileOwner()}
            composable(route = "DeviceOwner"){DeviceOwner(navCtrl)}
            composable(route = "DeviceInfo"){DeviceInfo()}
            composable(route = "SpecificID"){SpecificID()}
            composable(route = "OrgName"){OrgName()}
            composable(route = "NoManagementAccount"){NoManageAccount()}
            composable(route = "LockScreenInfo"){LockScreenInfo()}
            composable(route = "SupportMsg"){SupportMsg()}
            composable(route = "TransformOwnership"){TransformOwnership()}
        }
    }
}

@Composable
private fun Home(localNavCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        SubPageItem(
            R.string.device_admin, stringResource(if(myDpm.isAdminActive(myComponent)){R.string.activated}else{R.string.deactivated}),
            operation = {localNavCtrl.navigate("DeviceAdmin")}
        )
        SubPageItem(
            R.string.profile_owner, stringResource(if(isProfileOwner(myDpm)){R.string.activated}else{R.string.deactivated}),
            operation = {localNavCtrl.navigate("ProfileOwner")}
        )
        SubPageItem(
            R.string.device_owner, stringResource(if(isDeviceOwner(myDpm)){R.string.activated}else{R.string.deactivated}),
            operation = {localNavCtrl.navigate("DeviceOwner")}
        )
        SubPageItem(R.string.shizuku,""){localNavCtrl.navigate("Shizuku")}
        SubPageItem(R.string.device_info,""){localNavCtrl.navigate("DeviceInfo")}
        if(VERSION.SDK_INT>=31&&(isProfileOwner(myDpm)|| isDeviceOwner(myDpm))){
            SubPageItem(R.string.enrollment_specific_id,""){localNavCtrl.navigate("SpecificID")}
        }
        if((VERSION.SDK_INT>=26&&isDeviceOwner(myDpm))||(VERSION.SDK_INT>=24&&isProfileOwner(myDpm))){
            SubPageItem(R.string.org_name,""){localNavCtrl.navigate("OrgName")}
        }
        if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
            SubPageItem(R.string.account_types_management_disabled,""){localNavCtrl.navigate("NoManagementAccount")}
        }
        if(VERSION.SDK_INT>=24&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.owner_lockscr_info,""){localNavCtrl.navigate("LockScreenInfo")}
        }
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SubPageItem(R.string.support_msg,""){localNavCtrl.navigate("SupportMsg")}
        }
        if(VERSION.SDK_INT>=28&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SubPageItem(R.string.transform_ownership,""){localNavCtrl.navigate("TransformOwnership")}
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun LockScreenInfo(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    var infoText by remember{mutableStateOf(myDpm.deviceOwnerLockScreenInfo?.toString() ?: "")}
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        Text(text = stringResource(R.string.owner_lockscr_info), style = typography.headlineLarge)
        OutlinedTextField(
            value = infoText,
            label = {Text(stringResource(R.string.owner_lockscr_info))},
            onValueChange = { infoText=it },
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 4.dp)
        )
        Button(
            onClick = {
                focusMgr.clearFocus()
                myDpm.setDeviceOwnerLockScreenInfo(myComponent,infoText)
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                myDpm.setDeviceOwnerLockScreenInfo(myComponent,null)
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.reset))
        }
    }
}

@Composable
private fun DeviceAdmin(navCtrl: NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val co = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.device_admin), style = typography.headlineLarge)
        Text(text = stringResource(if(myDpm.isAdminActive(myComponent)) { R.string.activated } else { R.string.deactivated }), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(myDpm.isAdminActive(myComponent)) {
            if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)) {
                Button(
                    onClick = {
                        myDpm.removeActiveAdmin(myComponent)
                        co.launch{ delay(600); if(!myDpm.isAdminActive(myComponent)){navCtrl.navigateUp()} }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.deactivate))
                }
            }
        } else {
            Button(onClick = {activateDeviceAdmin(myContext, myComponent)}, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.activate))
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        if(!myDpm.isAdminActive(myComponent)) {
            SelectionContainer {
                Text(text = stringResource(R.string.activate_device_admin_command))
            }
        }
    }
}

@Composable
private fun ProfileOwner(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.profile_owner), style = typography.headlineLarge)
        Text(stringResource(if(isProfileOwner(myDpm)){R.string.activated}else{R.string.deactivated}), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(isProfileOwner(myDpm)&&VERSION.SDK_INT>=24){
            Button(onClick = {myDpm.clearProfileOwner(myComponent)}, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.deactivate))
            }
        }
        if(!isProfileOwner(myDpm)){
            SelectionContainer{
                Text(text = stringResource(R.string.activate_profile_owner_command))
            }
        }
    }
}

@Composable
private fun DeviceOwner(navCtrl: NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val co = rememberCoroutineScope()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.device_owner), style = typography.headlineLarge)
        Text(text = stringResource(if(isDeviceOwner(myDpm)){R.string.activated}else{R.string.deactivated}), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(isDeviceOwner(myDpm)){
            Button(
                onClick = {
                    myDpm.clearDeviceOwnerApp(myContext.packageName)
                    co.launch{ delay(600); if(!isDeviceOwner(myDpm)){navCtrl.navigateUp()} }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(R.string.deactivate))
            }
        }
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer{
                Text(text = stringResource(R.string.activate_device_owner_command))
            }
        }
    }
}

@Composable
fun DeviceInfo(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.device_info), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            val financed = myDpm.isDeviceFinanced
            Text(stringResource(R.string.is_device_financed, financed))
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        if(VERSION.SDK_INT>=33){
            val dpmRole = myDpm.devicePolicyManagementRoleHolderPackage
            Text(stringResource(R.string.dpmrh, if(dpmRole==null) { stringResource(R.string.none) } else { "" }))
            if(dpmRole!=null){
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())){
                    Text(text = dpmRole)
                }
            }
        }
        Spacer(Modifier.padding(vertical = 2.dp))
        val encryptionStatus = mutableMapOf(
            DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE to stringResource(R.string.es_inactive),
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE to stringResource(R.string.es_active),
            DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED to stringResource(R.string.es_unsupported),
            DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING to stringResource(R.string.unknown)
        )
        if(VERSION.SDK_INT>=23){ encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY] = stringResource(R.string.es_active_default_key) }
        if(VERSION.SDK_INT>=24){ encryptionStatus[DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER] = stringResource(R.string.es_active_per_user) }
        Text("加密状态：${encryptionStatus[myDpm.storageEncryptionStatus]}")
        Spacer(Modifier.padding(vertical = 2.dp))
        val adminList = myDpm.activeAdmins
        if(adminList!=null){
            var adminListText = ""
            Text(text = stringResource(R.string.activated_device_admin, adminList.size))
            var count = adminList.size
            for(each in adminList){
                count -= 1
                adminListText += "$each"
                if(count>0){adminListText += "\n"}
            }
            SelectionContainer(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).horizontalScroll(rememberScrollState())){
                Text(text = adminListText)
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun SpecificID(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        val specificId = myDpm.enrollmentSpecificId
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.enrollment_specific_id), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(specificId!=""){
            SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())){ Text(specificId) }
        }else{
            Text(stringResource(R.string.require_set_org_id))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun OrgName(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        var orgName by remember{mutableStateOf(try{myDpm.getOrganizationName(myComponent).toString()}catch(e:SecurityException){""})}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.org_name), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = orgName, onValueChange = {orgName=it}, modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 3.dp),
            label = {Text(stringResource(R.string.org_name))},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                myDpm.setOrganizationName(myComponent,orgName)
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun SupportMsg(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    var shortMsg by remember{mutableStateOf(myDpm.getShortSupportMessage(myComponent)?.toString() ?: "")}
    var longMsg by remember{mutableStateOf(myDpm.getLongSupportMessage(myComponent)?.toString() ?: "")}
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.short_support_msg), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = shortMsg,
            label = {Text(stringResource(R.string.short_support_msg))},
            onValueChange = { shortMsg=it },
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 2.dp))
        OutlinedTextField(
            value = longMsg,
            label = {Text(stringResource(R.string.long_support_msg))},
            onValueChange = { longMsg=it },
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                myDpm.setShortSupportMessage(myComponent, shortMsg)
                myDpm.setLongSupportMessage(myComponent, longMsg)
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 1.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                myDpm.setShortSupportMessage(myComponent, null)
                myDpm.setLongSupportMessage(myComponent, null)
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.reset))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Information{Text(text = stringResource(R.string.support_msg_desc))}
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun NoManageAccount(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.account_types_management_disabled), style = typography.headlineLarge)
        Text(stringResource(R.string.unknown_effect))
        var accountList by remember{ mutableStateOf("") }
        val refreshList = {
            val noManageAccount = myDpm.accountTypesWithManagementDisabled
            accountList = ""
            if (noManageAccount != null) {
                var count = noManageAccount.size
                for(each in noManageAccount){ count -= 1; accountList += each; if(count>0){accountList += "\n"} }
            }
        }
        var inited by remember{mutableStateOf(false)}
        if(!inited){ refreshList(); inited=true }
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = if(accountList==""){stringResource(R.string.none)}else{accountList})
        var inputText by remember{ mutableStateOf("") }
        OutlinedTextField(
            value = inputText,
            onValueChange = {inputText=it},
            label = {Text(stringResource(R.string.account_types))},
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 4.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        Button(
            onClick={
                myDpm.setAccountManagementDisabled(myComponent,inputText,true)
                refreshList()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.add))
        }
        Button(
            onClick={
                myDpm.setAccountManagementDisabled(myComponent,inputText,false)
                refreshList()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.remove))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun TransformOwnership(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    val focusRequester = FocusRequester()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 8.dp)){
        var pkg by remember{mutableStateOf("")}
        var cls by remember{mutableStateOf("")}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.transform_ownership), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.transform_ownership_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = pkg, onValueChange = {pkg = it}, label = {Text(stringResource(R.string.target_package_name))},
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {focusRequester.requestFocus()})
        )
        Spacer(Modifier.padding(vertical = 2.dp))
        OutlinedTextField(
            value = cls, onValueChange = {cls = it}, label = {Text(stringResource(R.string.target_class_name))},
            modifier = Modifier.focusRequester(focusRequester).fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                try {
                    myDpm.transferOwnership(myComponent,ComponentName(pkg, cls),null)
                    Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                }catch(e:IllegalArgumentException){
                    Toast.makeText(myContext, myContext.getString(R.string.fail), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.transform))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

private fun activateDeviceAdmin(inputContext:Context,inputComponent:ComponentName){
    try {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, inputComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, inputContext.getString(R.string.activate_android_owner_here))
        startActivity(inputContext,intent,null)
    }catch(e:ActivityNotFoundException){
        Toast.makeText(inputContext,inputContext.getString(R.string.unsupported),Toast.LENGTH_SHORT).show()
    }
}
