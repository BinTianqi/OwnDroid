package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
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
                Text("激活Device admin")
            }
        }
        Row(
            modifier = sections(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Device Admin", fontSize = if(!isWear){22.sp}else{20.sp},color = titleColor)
                Text(text = if(isda){"已激活"}else{"未激活"})
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
                        Text("撤销")
                    }
                }
            }else{
                Button(onClick = { activateDeviceAdmin(myContext,myComponent) }) {
                    Text("激活")
                }
            }
        }
        if(!isda&&!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer(modifier = sections(colorScheme.tertiaryContainer)){
                Text("激活命令：\nadb shell dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                    color = colorScheme.onTertiaryContainer, style = bodyTextStyle)
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
                    Text(if(isProfileOwner(myDpm)){"已激活"}else{"未激活"})
                }
                if(isProfileOwner(myDpm)&&VERSION.SDK_INT>=24&&!isWear){
                    Button(
                        onClick = {
                            myDpm.clearProfileOwner(myComponent)
                            navCtrl.navigateUp()
                        }
                    ) {
                        Text("撤销")
                    }
                }
            }
        }
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer(modifier = sections(colorScheme.tertiaryContainer)){
                Text("激活命令：\nadb shell dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                    color = colorScheme.onTertiaryContainer, style = bodyTextStyle)
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
                    Text(if(isDeviceOwner(myDpm)){"已激活"}else{"未激活"})
                }
                if(isDeviceOwner(myDpm)&&!isWear){
                    Button(
                        onClick = {
                            myDpm.clearDeviceOwnerApp("com.binbin.androidowner")
                            navCtrl.navigateUp()
                        }
                    ) {
                        Text("撤销")
                    }
                }
            }
        }
        
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            SelectionContainer(modifier = sections(colorScheme.tertiaryContainer)){
                Text(text = "激活命令：\nadb shell dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                    color = colorScheme.onTertiaryContainer, style = bodyTextStyle)
            }
        }
        if(VERSION.SDK_INT>=30){
            Column(
                modifier = sections()
            ) {
                Text(text = "设备信息", style = typography.titleLarge,color = titleColor)
                if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
                    val financed = myDpm.isDeviceFinanced
                    Text("企业资产 : $financed",style=bodyTextStyle)
                }
                if(VERSION.SDK_INT>=33){
                    val dpmRole = myDpm.devicePolicyManagementRoleHolderPackage
                    Text("设备策略管理器角色：${if(dpmRole==null){"null"}else{""}}",style=bodyTextStyle)
                    if(dpmRole!=null){
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){
                            SelectionContainer { Text(dpmRole) }
                        }
                    }
                }
                val encryptionStatus = mapOf(
                    DevicePolicyManager.ENCRYPTION_STATUS_INACTIVE to "未使用",
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE to "正在使用",
                    DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED to "不支持",
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_DEFAULT_KEY to "使用默认密钥",
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER to "每个用户分别加密",
                    DevicePolicyManager.ENCRYPTION_STATUS_ACTIVATING to "未知"
                )
                Text("加密状态：${encryptionStatus[myDpm.storageEncryptionStatus]}",style=bodyTextStyle)
                val adminList = myDpm.activeAdmins
                if(adminList!=null){
                    var adminListText = ""
                    Text(text = "激活的Device admin: ${adminList.size}", style = bodyTextStyle)
                    var count = adminList.size
                    for(each in adminList){
                        count -= 1
                        adminListText += "$each"
                        if(count>0){adminListText += "\n"}
                    }
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp).horizontalScroll(rememberScrollState())){
                        SelectionContainer {
                            Text(text = adminListText, style = bodyTextStyle, color = titleColor)
                        }
                    }
                }
            }
        }
        if(VERSION.SDK_INT>=31&&(isProfileOwner(myDpm)|| isDeviceOwner(myDpm))){
            Column(modifier = sections()) {
                val specificId = myDpm.enrollmentSpecificId
                Text(text = "设备唯一标识码", style = typography.titleLarge,color = titleColor)
                Text(text = "（恢复出厂设置不变）",style=bodyTextStyle)
                if(specificId!=""){
                    SelectionContainer{ Text(specificId, style = bodyTextStyle) }
                }else{
                    Text("需要设置组织ID",style=bodyTextStyle)
                }
            }
        }
        
        if((VERSION.SDK_INT>=26&&isDeviceOwner(myDpm))||(VERSION.SDK_INT>=24&&isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var orgName by remember{mutableStateOf(try{myDpm.getOrganizationName(myComponent).toString()}catch(e:SecurityException){""})}
                Text(text = "组织名称", style = typography.titleLarge, color = titleColor)
                OutlinedTextField(
                    value = orgName, onValueChange = {orgName=it}, modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 3.dp),
                    label = {Text("组织名称")},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
                )
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        myDpm.setOrganizationName(myComponent,orgName)
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("应用")
                }
            }
        }
        
        if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
            Column(modifier = sections()) {
                Text(text = "不受控制的账号类型", style = typography.titleLarge,color = titleColor)
                Text("作用未知",style=bodyTextStyle)
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
                Text(text = if(accountlist==""){"无"}else{accountlist}, style = bodyTextStyle)
                var inputText by remember{ mutableStateOf("") }
                OutlinedTextField(
                    value = inputText,
                    onValueChange = {inputText=it},
                    label = {Text("账号类型")},
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
                        Text("添加")
                    }
                    Button(
                        onClick={focusManager.clearFocus()
                            myDpm.setAccountManagementDisabled(myComponent,inputText,false)
                            noManageAccount=myDpm.accountTypesWithManagementDisabled
                            refreshList()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ){
                        Text("移除")
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
                Text(text = "转移所有权", style = typography.titleLarge, color = titleColor)
                Text(text = "把Device owner或Profile owner权限转移到另一个应用。目标必须是Device admin", style = bodyTextStyle)
                OutlinedTextField(
                    value = pkg, onValueChange = {pkg = it}, label = {Text("目标包名")},
                    modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = {focusManager.moveFocus(FocusDirection.Down)})
                )
                OutlinedTextField(
                    value = cls, onValueChange = {cls = it}, label = {Text("目标类名")},
                    modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
                )
                Button(
                    onClick = {
                        try {
                            myDpm.transferOwnership(myComponent,ComponentName(pkg, cls),null)
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }catch(e:IllegalArgumentException){
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                ) {
                    Text("转移")
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
                    Text("撤销Device admin")
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
                        Text("撤销Profile owner")
                    }
                }
                Button(
                    onClick = {
                        myDpm.clearDeviceOwnerApp("com.binbin.androidowner")
                        navCtrl.navigateUp()
                    },
                    colors = ButtonDefaults.buttonColors(contentColor = colorScheme.onError, containerColor = colorScheme.error),
                    enabled = isDeviceOwner(myDpm)
                    ) {
                    Text("撤销Device owner")
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
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                modifier = if(isWear){Modifier.fillMaxWidth(0.49F)}else{Modifier.fillMaxWidth(0.6F)}
            ) {
                Text(text = "应用")
            }
            Button(
                onClick = {
                    output(null)
                    inputContent = input()
                    fm.clearFocus()
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(text = "重置")
            }
        }
    }
}

fun activateDeviceAdmin(inputContext:Context,inputComponent:ComponentName){
    try {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, inputComponent)
        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "在这里激活Android Owner")
        startActivity(inputContext,intent,null)
    }catch(e:ActivityNotFoundException){
        Toast.makeText(inputContext,"不支持",Toast.LENGTH_SHORT).show()
    }
}
