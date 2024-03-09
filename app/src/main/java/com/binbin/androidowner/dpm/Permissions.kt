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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import com.binbin.androidowner.R


@Composable
fun DpmPermissions(navCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusManager = LocalFocusManager.current
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val titleColor = colorScheme.onPrimaryContainer
    val bodyTextStyle = if(isWear){typography.bodyMedium}else{typography.bodyLarge}
    val expandCommandBlock by remember{mutableStateOf("")}
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.clickable {navCtrl.navigate("ShizukuActivate")},
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = "Shizuku", style = typography.titleLarge, color = titleColor, modifier = Modifier.padding(vertical = 2.dp))
            Icon(imageVector = Icons.Default.KeyboardArrowRight,contentDescription = null, tint = colorScheme.onPrimaryContainer)
        }
        DeviceAdmin()
        ProfileOwner()
        DeviceOwner()
        if(VERSION.SDK_INT>=30){
            Column {
                Text(text = stringResource(R.string.device_info), style = typography.titleLarge,color = titleColor)
                if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
                    val financed = myDpm.isDeviceFinanced
                    Text(stringResource(R.string.is_device_financed, financed),style=bodyTextStyle)
                }
                if(VERSION.SDK_INT>=33){
                    val dpmRole = myDpm.devicePolicyManagementRoleHolderPackage
                    Text(stringResource(R.string.dpmrh, if(dpmRole==null) { stringResource(R.string.none) } else { "" }),style=bodyTextStyle)
                    if(dpmRole!=null){
                        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())){
                            Text(text = dpmRole, style = bodyTextStyle, color = colorScheme.onPrimaryContainer)
                        }
                    }
                }
                val encryptionStatus = mapOf(
                    DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE to stringResource(R.string.es_inactive),
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE to stringResource(R.string.es_active),
                    DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED to stringResource(R.string.es_unsupported),
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY to stringResource(R.string.es_active_default_key),
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER to stringResource(R.string.es_active_per_user),
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING to stringResource(R.string.unknown)
                )
                Text("加密状态：${encryptionStatus[myDpm.storageEncryptionStatus]}",style=bodyTextStyle)
                val adminList = myDpm.activeAdmins
                if(adminList!=null){
                    var adminListText = ""
                    Text(text = stringResource(R.string.activated_device_admin, adminList.size), style = bodyTextStyle)
                    var count = adminList.size
                    for(each in adminList){
                        count -= 1
                        adminListText += "$each"
                        if(count>0){adminListText += "\n"}
                    }
                    SelectionContainer(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).horizontalScroll(rememberScrollState())){
                        Text(text = adminListText, style = bodyTextStyle, color = titleColor)
                    }
                }
            }
        }
        
        if(VERSION.SDK_INT>=31&&(isProfileOwner(myDpm)|| isDeviceOwner(myDpm))){
            SpecificID()
        }
        
        if((VERSION.SDK_INT>=26&&isDeviceOwner(myDpm))||(VERSION.SDK_INT>=24&&isProfileOwner(myDpm))){
            OrgName()
        }
        
        if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
            NoManageAccount()
        }
        
        if(VERSION.SDK_INT>=24&&isDeviceOwner(myDpm)){
            DeviceOwnerInfo(R.string.owner_lockscr_info,R.string.place_holder,R.string.owner_lockscr_info,focusManager,myContext,
                {myDpm.deviceOwnerLockScreenInfo},{content ->  myDpm.setDeviceOwnerLockScreenInfo(myComponent,content)})
        }
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SupportMsg()
        }
        
        if(VERSION.SDK_INT>=28&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            TransformOwnership()
        }
        
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
fun DeviceOwnerInfo(
    name:Int,
    desc:Int,
    textfield:Int,
    fm:FocusManager,
    myContext:Context,
    input:()->CharSequence?,
    output:(content:String?)->Unit
){
    Column{
        val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
        val isWear = sharedPref.getBoolean("isWear",false)
        Text(text = stringResource(name), style = typography.titleLarge, softWrap = false, color = colorScheme.onPrimaryContainer)
        if(desc!=R.string.place_holder){
            Text(
                text = stringResource(desc),modifier = Modifier.padding(top = 6.dp),
                style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
        }
        var inputContent by remember{ mutableStateOf(input()) }
        OutlinedTextField(
            value = if(inputContent!=null){ inputContent.toString() }else{""},
            label = {Text(stringResource(textfield))},
            onValueChange = { inputContent=it },
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    output(inputContent.toString())
                    inputContent= input()
                    fm.clearFocus()
                    Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                },
                modifier = if(isWear){Modifier.fillMaxWidth(0.49F)}else{Modifier.fillMaxWidth(0.6F)}
            ) {
                Text(text = stringResource(R.string.apply))
            }
            Button(
                onClick = {
                    output(null)
                    inputContent = input()
                    fm.clearFocus()
                    Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(text = stringResource(R.string.reset))
            }
        }
    }
}

@Composable
fun DeviceAdmin(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Device Admin")
                Text(
                    text = stringResource(
                        if(myDpm.isAdminActive(myComponent)) {
                            R.string.activated
                        } else {
                            R.string.deactivated
                        }
                    )
                )
            }
            if(myDpm.isAdminActive(myComponent)) {
                if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)) {
                    Button(onClick = {
                        myDpm.removeActiveAdmin(myComponent)
                    }) {
                        Text(stringResource(R.string.deactivate))
                    }
                }
            } else {
                Button(onClick = {activateDeviceAdmin(myContext, myComponent)}) {
                    Text(stringResource(R.string.activate))
                }
            }
        }
        if(!myDpm.isAdminActive(myComponent)) {
            SelectionContainer {
                Text(text = stringResource(R.string.activate_device_admin_command), color = colorScheme.onTertiaryContainer)
            }
        }
    }
}

@Composable
fun ProfileOwner(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column {
        if(!isDeviceOwner(myDpm)){
            Row(
                
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Profile Owner")
                    Text(stringResource(if(isProfileOwner(myDpm)){R.string.activated}else{R.string.deactivated}))
                }
                if(isProfileOwner(myDpm)&&VERSION.SDK_INT>=24&&!myDpm.isManagedProfile(myComponent)){
                    Button(onClick = {myDpm.clearProfileOwner(myComponent)}) {
                        Text(stringResource(R.string.deactivate))
                    }
                }
            }
        }
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer{
                Text(text = stringResource(R.string.activate_profile_owner_command), color = colorScheme.onTertiaryContainer)
            }
        }
    }
}

@Composable
fun DeviceOwner(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column {
        if(!isProfileOwner(myDpm)){
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Device Owner")
                    Text(stringResource(if(isDeviceOwner(myDpm)){R.string.activated}else{R.string.deactivated}))
                }
                if(isDeviceOwner(myDpm)){
                    Button(
                        onClick = {
                            myDpm.clearDeviceOwnerApp(myContext.packageName)
                        }
                    ) {
                        Text(stringResource(R.string.deactivate))
                    }
                }
            }
        }
        
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer{
                Text(text = stringResource(R.string.activate_device_owner_command), color = colorScheme.onTertiaryContainer)
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun SpecificID(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column{
        val specificId = myDpm.enrollmentSpecificId
        Text(text = stringResource(R.string.enrollment_specific_id), style = typography.titleLarge)
        if(specificId!=""){
            SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())){ Text(specificId,softWrap = false) }
        }else{
            Text(stringResource(R.string.require_set_org_id))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun OrgName(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
        var orgName by remember{mutableStateOf(try{myDpm.getOrganizationName(myComponent).toString()}catch(e:SecurityException){""})}
        Text(text = stringResource(R.string.org_name), style = typography.titleLarge)
        OutlinedTextField(
            value = orgName, onValueChange = {orgName=it}, modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 3.dp),
            label = {Text(stringResource(R.string.org_name))},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
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
fun SupportMsg(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    DeviceOwnerInfo(R.string.support_msg,R.string.support_msg_desc,R.string.message,focusMgr,myContext,
        {myDpm.getShortSupportMessage(myComponent)},{content ->  myDpm.setShortSupportMessage(myComponent,content)})
    DeviceOwnerInfo(R.string.long_support_msg,R.string.long_support_msg_desc,R.string.message,focusMgr,myContext,
        {myDpm.getLongSupportMessage(myComponent)},{content ->  myDpm.setLongSupportMessage(myComponent,content)})
}

@Composable
fun NoManageAccount(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
        Text(text = stringResource(R.string.account_types_management_disabled), style = typography.titleLarge)
        Text(stringResource(R.string.developing))
        var noManageAccount = myDpm.accountTypesWithManagementDisabled
        var accountlist by remember{ mutableStateOf("") }
        val refreshList = {
            accountlist = ""
            if (noManageAccount != null) {
                var count = noManageAccount!!.size
                for(each in noManageAccount!!){ count -= 1; accountlist += each; if(count>0){accountlist += "\n"} }
            }
        }
        var inited by remember{mutableStateOf(false)}
        if(!inited){ refreshList(); inited=true }
        Text(text = if(accountlist==""){stringResource(R.string.none)}else{accountlist})
        var inputText by remember{ mutableStateOf("") }
        OutlinedTextField(
            value = inputText,
            onValueChange = {inputText=it},
            label = {Text(stringResource(R.string.account_types))},
            modifier = Modifier.focusable().fillMaxWidth().padding(bottom = 4.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick={
                    focusMgr.clearFocus()
                    myDpm.setAccountManagementDisabled(myComponent,inputText,true)
                    noManageAccount=myDpm.accountTypesWithManagementDisabled
                    refreshList()
                },
                modifier = Modifier.fillMaxWidth(0.49f)
            ){
                Text(stringResource(R.string.add))
            }
            Button(
                onClick={focusMgr.clearFocus()
                    myDpm.setAccountManagementDisabled(myComponent,inputText,false)
                    noManageAccount=myDpm.accountTypesWithManagementDisabled
                    refreshList()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ){
                Text(stringResource(R.string.remove))
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun TransformOwnership(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
        var pkg by remember{mutableStateOf("")}
        var cls by remember{mutableStateOf("")}
        Text(text = stringResource(R.string.transform_ownership), style = typography.titleLarge)
        Text(text = stringResource(R.string.transform_ownership_desc))
        OutlinedTextField(
            value = pkg, onValueChange = {pkg = it}, label = {Text(stringResource(R.string.target_package_name))},
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = {focusMgr.moveFocus(FocusDirection.Down)})
        )
        OutlinedTextField(
            value = cls, onValueChange = {cls = it}, label = {Text(stringResource(R.string.target_class_name))},
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        Button(
            onClick = {
                try {
                    myDpm.transferOwnership(myComponent,ComponentName(pkg, cls),null)
                    Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                }catch(e:IllegalArgumentException){
                    Toast.makeText(myContext, myContext.getString(R.string.fail), Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
        ) {
            Text(stringResource(R.string.transform))
        }
    }
}

fun activateDeviceAdmin(inputContext:Context,inputComponent:ComponentName){
    try {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, inputComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, inputContext.getString(R.string.activate_android_owner_here))
        startActivity(inputContext,intent,null)
    }catch(e:ActivityNotFoundException){
        Toast.makeText(inputContext,inputContext.getString(R.string.unsupported),Toast.LENGTH_SHORT).show()
    }
}
