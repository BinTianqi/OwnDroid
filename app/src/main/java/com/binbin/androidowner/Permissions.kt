package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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


@Composable
fun DpmPermissions(navCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val isda = myDpm.isAdminActive(myComponent)
    val focusManager = LocalFocusManager.current
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val titleColor = colorScheme.onPrimaryContainer
    val bodyTextStyle = if(isWear){typography.bodyMedium}else{typography.bodyLarge}
    var expandCommandBlock by remember{mutableStateOf("")}
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = sections(onClick = {navCtrl.navigate("ShizukuActivate")}, clickable = true),
            horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
        ){
            Text(text = "Shizuku", style = typography.titleLarge, color = titleColor, modifier = Modifier.padding(vertical = 2.dp))
            Icon(imageVector = Icons.Default.KeyboardArrowRight,contentDescription = null, tint = colorScheme.onPrimaryContainer)
        }
        if(!myDpm.isAdminActive(myComponent)&&isWear){
            Button(onClick = { activateDeviceAdmin(myContext,myComponent) },modifier = Modifier.padding(horizontal = 3.dp).fillMaxWidth()) {
                Text(stringResource(R.string.activate_device_admin))
            }
        }
        Row(
            modifier = sections(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Device Admin", fontSize = if(!isWear){22.sp}else{20.sp},color = titleColor)
                Text(text = stringResource(if(isda){R.string.activated}else{R.string.deactivated}))
            }
            if(!isWear)
            if(isda){
                if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
                    Button(
                        onClick = {
                            myDpm.removeActiveAdmin(myComponent)
                            navCtrl.navigateUp()
                        }
                    ) {
                        Text(stringResource(R.string.deactivate))
                    }
                }
            }else{
                Button(onClick = { activateDeviceAdmin(myContext,myComponent) }) {
                    Text(stringResource(R.string.activate))
                }
            }
        }
        if(!isda&&!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer(
                modifier = sections(colorScheme.tertiaryContainer,{expandCommandBlock="admin"},expandCommandBlock!="admin").animateContentSize(animationSpec = scrollAnim())
            ){
                if(expandCommandBlock=="admin"){
                    Text(
                        text = stringResource(R.string.activate_device_admin_command),
                        color = colorScheme.onTertiaryContainer, style = bodyTextStyle
                    )
                }else{
                    Text(text = stringResource(R.string.touch_to_view_command), style = bodyTextStyle)
                }
            }
        }
        
        if(!isDeviceOwner(myDpm)){
            Row(
                modifier = sections(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Profile Owner", fontSize = if(!isWear){22.sp}else{20.sp},color = titleColor)
                    Text(stringResource(if(isProfileOwner(myDpm)){R.string.activated}else{R.string.deactivated}))
                }
                if(isProfileOwner(myDpm)&&VERSION.SDK_INT>=24&&!isWear&&!myDpm.isManagedProfile(myComponent)){
                    Button(
                        onClick = {
                            myDpm.clearProfileOwner(myComponent)
                            navCtrl.navigateUp()
                        }
                    ) {
                        Text(stringResource(R.string.deactivate))
                    }
                }
            }
        }
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer(
                modifier = sections(colorScheme.tertiaryContainer,{expandCommandBlock="profile"},expandCommandBlock!="profile").animateContentSize(animationSpec = scrollAnim())
            ){
                if(expandCommandBlock=="profile"){
                    Text(
                        text = stringResource(R.string.activate_profile_owner_command),
                        color = colorScheme.onTertiaryContainer, style = bodyTextStyle
                    )
                }else{
                    Text(text = stringResource(R.string.touch_to_view_command), style = bodyTextStyle)
                }
            }
        }
        
        if(!isProfileOwner(myDpm)){
            Row(
                modifier = sections(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Device Owner", fontSize = if(!isWear){22.sp}else{20.sp},color = titleColor)
                    Text(stringResource(if(isDeviceOwner(myDpm)){R.string.activated}else{R.string.deactivated}))
                }
                if(isDeviceOwner(myDpm)&&!isWear){
                    Button(
                        onClick = {
                            myDpm.clearDeviceOwnerApp(myContext.packageName)
                            navCtrl.navigateUp()
                        }
                    ) {
                        Text(stringResource(R.string.deactivate))
                    }
                }
            }
        }
        
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer(
                modifier = sections(colorScheme.tertiaryContainer,{expandCommandBlock="device"},expandCommandBlock!="device").animateContentSize(animationSpec = scrollAnim())
            ){
                if(expandCommandBlock=="device"){
                    Text(
                        text = stringResource(R.string.activate_device_owner_command),
                        color = colorScheme.onTertiaryContainer, style = bodyTextStyle
                    )
                }else{
                    Text(text = stringResource(R.string.touch_to_view_command), style = bodyTextStyle)
                }
            }
        }
        if(VERSION.SDK_INT>=30){
            Column(
                modifier = sections()
            ) {
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
            Column(modifier = sections()) {
                val specificId = myDpm.enrollmentSpecificId
                Text(text = stringResource(R.string.enrollment_specific_id), style = typography.titleLarge,color = titleColor)
                if(specificId!=""){
                    SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())){ Text(specificId, style = bodyTextStyle, softWrap = false) }
                }else{
                    Text(stringResource(R.string.require_set_org_id),style=bodyTextStyle)
                }
            }
        }
        
        if((VERSION.SDK_INT>=26&&isDeviceOwner(myDpm))||(VERSION.SDK_INT>=24&&isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var orgName by remember{mutableStateOf(try{myDpm.getOrganizationName(myComponent).toString()}catch(e:SecurityException){""})}
                Text(text = stringResource(R.string.org_name), style = typography.titleLarge, color = titleColor)
                OutlinedTextField(
                    value = orgName, onValueChange = {orgName=it}, modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 3.dp),
                    label = {Text(stringResource(R.string.org_name))},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        myDpm.setOrganizationName(myComponent,orgName)
                        Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text(stringResource(R.string.apply))
                }
            }
        }
        
        if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
            Column(modifier = sections()) {
                Text(text = stringResource(R.string.account_types_management_disabled), style = typography.titleLarge,color = titleColor)
                Text(stringResource(R.string.developing),style=bodyTextStyle)
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
                Text(text = if(accountlist==""){stringResource(R.string.none)}else{accountlist}, style = bodyTextStyle)
                var inputText by remember{ mutableStateOf("") }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {inputText=it},
                    label = {Text(stringResource(R.string.account_types))},
                    modifier = Modifier.focusable().fillMaxWidth().padding(bottom = 4.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
                )
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick={
                            focusManager.clearFocus()
                            myDpm.setAccountManagementDisabled(myComponent,inputText,true)
                            noManageAccount=myDpm.accountTypesWithManagementDisabled
                            refreshList()
                        },
                        modifier = Modifier.fillMaxWidth(0.49f)
                    ){
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick={focusManager.clearFocus()
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
        
        if(VERSION.SDK_INT>=24&&isDeviceOwner(myDpm)){
            DeviceOwnerInfo(R.string.owner_lockscr_info,R.string.place_holder,R.string.owner_lockscr_info,focusManager,myContext,
                {myDpm.deviceOwnerLockScreenInfo},{content ->  myDpm.setDeviceOwnerLockScreenInfo(myComponent,content)})
        }
        if((isDeviceOwner(myDpm)||isProfileOwner(myDpm))&&VERSION.SDK_INT>=24){
            DeviceOwnerInfo(R.string.support_msg,R.string.support_msg_desc,R.string.message,focusManager,myContext,
                {myDpm.getShortSupportMessage(myComponent)},{content ->  myDpm.setShortSupportMessage(myComponent,content)})
            DeviceOwnerInfo(R.string.long_support_msg,R.string.long_support_msg_desc,R.string.message,focusManager,myContext,
                {myDpm.getLongSupportMessage(myComponent)},{content ->  myDpm.setLongSupportMessage(myComponent,content)})
        }
        
        if(VERSION.SDK_INT>=28&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var pkg by remember{mutableStateOf("")}
                var cls by remember{mutableStateOf("")}
                Text(text = stringResource(R.string.transform_ownership), style = typography.titleLarge, color = titleColor)
                Text(text = stringResource(R.string.transform_ownership_desc), style = bodyTextStyle)
                OutlinedTextField(
                    value = pkg, onValueChange = {pkg = it}, label = {Text(stringResource(R.string.target_package_name))},
                    modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(FocusDirection.Down)})
                )
                OutlinedTextField(
                    value = cls, onValueChange = {cls = it}, label = {Text(stringResource(R.string.target_class_name))},
                    modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
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
        
        if(isWear&&(myDpm.isAdminActive(myComponent)||isProfileOwner(myDpm)||isDeviceOwner(myDpm))){
            Column(modifier = sections(), horizontalAlignment = Alignment.CenterHorizontally) {
                Button(
                    onClick = {
                        myDpm.removeActiveAdmin(myComponent)
                        navCtrl.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(contentColor = colorScheme.onError, containerColor = colorScheme.error),
                    enabled = myDpm.isAdminActive(myComponent)
                ) {
                    Text(stringResource(R.string.deactivate_da))
                }
                if(VERSION.SDK_INT>=24){
                    Button(
                        onClick = {
                            myDpm.clearProfileOwner(myComponent)
                            navCtrl.navigateUp()
                        },
                        colors = ButtonDefaults.buttonColors(contentColor = colorScheme.onError, containerColor = colorScheme.error),
                        enabled = isProfileOwner(myDpm)
                    ) {
                        Text(stringResource(R.string.deactivate_po))
                    }
                }
                Button(
                    onClick = {
                        myDpm.clearDeviceOwnerApp(myContext.packageName)
                        navCtrl.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(contentColor = colorScheme.onError, containerColor = colorScheme.error),
                    enabled = isDeviceOwner(myDpm)
                    ) {
                    Text(stringResource(R.string.deactivate_do))
                }
            }
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
    Column(modifier = sections()) {
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
