package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
                Button(
                    onClick = {
                        myDpm.removeActiveAdmin(myComponent)
                        navCtrl.navigateUp()
                    }
                ) {
                    Text("撤销")
                }
            }else{
                Button(onClick = { activateDeviceAdmin(myContext,myComponent) }) {
                    Text("激活")
                }
            }
        }
        if(!isda){
            Column(
                modifier = sections(colorScheme.tertiaryContainer.copy(alpha = 0.8F)),
                horizontalAlignment = Alignment.Start
            ) {
                SelectionContainer {
                    Text("adb shell dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                        color = colorScheme.onTertiaryContainer, style = bodyTextStyle)
                }
                Text(text = "或者进入设置（原生安卓） -> 安全 -> 更多安全设置 -> 设备管理应用 -> Android Owner", style = bodyTextStyle)
            }
        }
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
        if(!isProfileOwner(myDpm)){
            Column(
                modifier = sections(colorScheme.tertiaryContainer.copy(alpha = 0.8F)),
                horizontalAlignment = Alignment.Start
            ) {
                if(!isDeviceOwner(myDpm)){
                    SelectionContainer {
                        Text("adb shell dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                            color = colorScheme.onTertiaryContainer, style = bodyTextStyle)
                    }
                    Text(text = "Device owner和Profile owner不能同时存在，强烈建议激活Device owner", style = bodyTextStyle)
                }
                if(isDeviceOwner(myDpm)){
                    Text(text = "Device owner创建其他用户后，这个应用会成为新用户的Profile owner", style = bodyTextStyle)
                }
            }
        }
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
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            Column(
                modifier = sections(colorScheme.tertiaryContainer.copy(alpha = 0.8F)),
                horizontalAlignment = Alignment.Start
            ) {
                SelectionContainer {
                    Text(text = "adb shell dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                    color = colorScheme.onTertiaryContainer, style = bodyTextStyle)
                }
                if(!isda){
                    Text(text = "使用此命令也会激活Device Admin", style = bodyTextStyle)
                }
            }
        }
        if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)||myDpm.isAdminActive(myComponent)){
            Text(
                text = "注意！在这里撤销权限不会清除配置。比如：被停用的应用会保持停用状态",
                color = colorScheme.onErrorContainer,
                modifier = sections(colorScheme.errorContainer.copy(alpha = 0.8F)),
                style = bodyTextStyle
            )
        }
        if(VERSION.SDK_INT>=30){
            Column(
                modifier = sections()
            ) {
                Text(text = "设备信息", style = typography.titleLarge,color = titleColor)
                val orgDevice = myDpm.isOrganizationOwnedDeviceWithManagedProfile
                Text("由组织拥有的受管理资料设备：$orgDevice",style=bodyTextStyle)
                if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
                    Text("Managed profile: ${myDpm.isManagedProfile(myComponent)}",style=bodyTextStyle)
                }
                if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
                    val financed = myDpm.isDeviceFinanced
                    Text("企业资产 : $financed",style=bodyTextStyle)
                }
                if(VERSION.SDK_INT>=33){
                    val dpmRole = myDpm.devicePolicyManagementRoleHolderPackage
                    Text("设备策略管理器角色：${if(dpmRole==null){"null"}else{""}}",style=bodyTextStyle)
                    if(dpmRole!=null){
                        Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())){
                            SelectionContainer {
                                Text(dpmRole)
                            }
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
                val specificId:String = myDpm.enrollmentSpecificId
                Text(text = "设备唯一标识码", style = typography.titleLarge,color = titleColor)
                Text("（恢复出厂设置不变）",style=bodyTextStyle)
                if(specificId!=""){
                    Text(specificId)
                    Button(onClick = {myDpm.setOrganizationId(specificId)}) {
                        Text("设置为组织ID")
                    }
                }else{
                    Text("你的设备不支持",style=bodyTextStyle)
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
                        for(eachAccount in noManageAccount!!){
                            count -= 1
                            accountlist += eachAccount
                            if(count>0){accountlist += "\n"}
                        }
                    }
                }
                refreshList()
                if(accountlist!=""){
                    Text(text = accountlist, color = titleColor)
                }else{
                    Text("无",style=bodyTextStyle)
                }
                var inputText by remember{ mutableStateOf("") }
                TextField(
                    value = inputText,
                    onValueChange = {inputText=it},
                    label = {Text("账号类型")},
                    modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
                )
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
                    Button(onClick={focusManager.clearFocus()
                        myDpm.setAccountManagementDisabled(myComponent,inputText,true)
                        noManageAccount=myDpm.accountTypesWithManagementDisabled
                        refreshList()
                    },modifier = Modifier.fillMaxWidth(0.49f)){
                        Text("列表")
                    }
                    Button(
                        onClick={focusManager.clearFocus()
                            myDpm.setAccountManagementDisabled(myComponent,inputText,false)
                            noManageAccount=myDpm.accountTypesWithManagementDisabled
                            refreshList()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ){
                        Text("从列表中移除")
                    }
                }
            }
        }

        if(isDeviceOwner(myDpm)&&VERSION.SDK_INT>=24){
            DeviceOwnerInfo(R.string.owner_lockscr_info,R.string.place_holder,R.string.owner_lockscr_info,focusManager,myContext,
                {myDpm.deviceOwnerLockScreenInfo},{content ->  myDpm.setDeviceOwnerLockScreenInfo(myComponent,content)})
            DeviceOwnerInfo(R.string.support_msg,R.string.support_msg_desc,R.string.message,focusManager,myContext,
                {myDpm.getShortSupportMessage(myComponent)},{content ->  myDpm.setShortSupportMessage(myComponent,content)})
            DeviceOwnerInfo(R.string.long_support_msg,R.string.long_support_msg_desc,R.string.message,focusManager,myContext,
                {myDpm.getLongSupportMessage(myComponent)},{content ->  myDpm.setLongSupportMessage(myComponent,content)})
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
        TextField(
            value = if(inputContent!=null){ inputContent.toString() }else{""},
            label = {Text(stringResource(textfield))},
            onValueChange = { inputContent=it },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    output(inputContent.toString())
                    inputContent= input()
                    fm.clearFocus()
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                modifier = if(isWear){Modifier.fillMaxWidth(0.48F)}else{Modifier.fillMaxWidth(0.6F)}
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
                modifier = Modifier.fillMaxWidth(0.95F)
            ) {
                Text(text = "重置")
            }
        }
    }
}

fun activateDeviceAdmin(inputContext:Context,inputComponent:ComponentName){
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, inputComponent)
    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "在这里激活Android Owner")
    startActivity(inputContext,intent,null)
}
