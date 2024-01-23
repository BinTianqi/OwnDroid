package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController


@Composable
fun DpmPermissions(myDpm: DevicePolicyManager, myComponent: ComponentName, myContext:Context,navCtrl:NavHostController){
    //da:DeviceAdmin do:DeviceOwner
    val isda = myDpm.isAdminActive(myComponent)
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    val focusManager = LocalFocusManager.current
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = sections(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Device Admin", style = MaterialTheme.typography.titleLarge)
                Text(if(isda){"已激活"}else{"未激活"})
            }
            if(isda){
                Button(
                    onClick = {
                        myDpm.removeActiveAdmin(myComponent)
                        navCtrl.navigate("HomePage") {
                            popUpTo(
                                navCtrl.graph.findStartDestination().id
                            ) { saveState = true }
                        }
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
                modifier = sections(MaterialTheme.colorScheme.tertiaryContainer),
                horizontalAlignment = Alignment.Start
            ) {
                Text("你可以在adb shell中使用以下命令激活Device Admin")
                SelectionContainer {
                    Text("dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                        color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Text("或者进入设置 -> 安全 -> 更多安全设置 -> 设备管理应用 -> Android Owner")
            }
        }
        Row(
            modifier = sections(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Profile Owner", style = MaterialTheme.typography.titleLarge)
                Text(if(isProfileOwner(myDpm)){"已激活"}else{"未激活"})
            }
            if(isProfileOwner(myDpm)&&VERSION.SDK_INT>=24){
                Button(
                    onClick = {
                        myDpm.clearProfileOwner(myComponent)
                        navCtrl.navigate("HomePage") {
                            popUpTo(
                                navCtrl.graph.findStartDestination().id
                            ) { saveState = true }
                        }
                    }
                ) {
                    Text("撤销")
                }
            }
        }
        if(!isProfileOwner(myDpm)){
            Column(
                modifier = sections(MaterialTheme.colorScheme.tertiaryContainer),
                horizontalAlignment = Alignment.Start
            ) {
                if(!isDeviceOwner(myDpm)){
                    Text("你可以在adb shell中使用以下命令激活Profile Owner")
                    SelectionContainer {
                        Text("dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                    Text("一个设备只能有一个Device owner，强烈建议激活Device owner")
                }
                if(isDeviceOwner(myDpm)){
                    Text("Device owner创建其他用户后，这个应用会成为新用户的Profile owner")
                }
            }
        }
        Row(
            modifier = sections(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Device Owner", style = MaterialTheme.typography.titleLarge)
                Text(if(isdo){"已激活"}else{"未激活"})
            }
            if(isdo){
                Button(
                    onClick = {
                        myDpm.clearDeviceOwnerApp("com.binbin.androidowner")
                        navCtrl.navigate("HomePage") {
                            popUpTo(
                                navCtrl.graph.findStartDestination().id
                            ) { saveState = true }
                        }
                    }
                ) {
                    Text("撤销")
                }
            }
        }
        if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)||myDpm.isAdminActive(myComponent)){
            Text(
                text = "注意！在这里撤销权限不会清除配置。比如：被停用的应用会保持停用状态",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = sections(MaterialTheme.colorScheme.errorContainer)
            )
        }
        if(!isdo&&!isProfileOwner(myDpm)){
            Column(
                modifier = sections(MaterialTheme.colorScheme.tertiaryContainer),
                horizontalAlignment = Alignment.Start
            ) {
                Text("你可以在adb shell中使用以下命令激活Device Owner")
                SelectionContainer {
                    Text(text = "dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                }
                if(!isda){
                    Text("使用此命令也会激活Device Admin")
                }
            }
        }
        if(VERSION.SDK_INT>=30){
            Column(
                modifier = sections()
            ) {
                Text(text = "设备信息", style = MaterialTheme.typography.titleLarge)
                val orgDevice = myDpm.isOrganizationOwnedDeviceWithManagedProfile
                Text("由组织拥有的受管理资料设备：$orgDevice")
                if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
                    Text("Managed profile: ${myDpm.isManagedProfile(myComponent)}")
                }
                if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
                    val financed = myDpm.isDeviceFinanced
                    Text("企业资产 : $financed")
                }
                if(VERSION.SDK_INT>=33){
                    Text("最小WiFi安全等级：${myDpm.minimumRequiredWifiSecurityLevel}")
                    Text("设备策略管理器角色：${myDpm.devicePolicyManagementRoleHolderPackage}")
                }
            }
        }
        if(VERSION.SDK_INT>=31&&(isProfileOwner(myDpm)|| isDeviceOwner(myDpm))){
            Column(modifier = sections()) {
                val specificId:String = myDpm.enrollmentSpecificId
                Text(text = "设备唯一标识码", style = MaterialTheme.typography.titleLarge)
                Text("（恢复出厂设置不变）")
                if(specificId!=""){
                    Text(specificId)
                    Button(onClick = {myDpm.setOrganizationId(specificId)}, enabled = specificId!="") {
                        Text("设置为组织ID")
                    }
                }else{
                    Text("你的设备不支持")
                }
            }
        }
        if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
            Column(modifier = sections()) {
                Text(text = "不受控制的账号类型", style = MaterialTheme.typography.titleLarge)
                Text("作用未知")
                var noManageAccount = myDpm.accountTypesWithManagementDisabled?.toMutableList()
                var accountlist by remember{ mutableStateOf("") }
                val refreshList = {
                    accountlist = ""
                    if (noManageAccount != null) {
                        for(eachAccount in noManageAccount!!){
                            accountlist+="$eachAccount \n"
                        }
                    }
                }
                refreshList()
                if(accountlist!=""){
                    Text(accountlist)
                }else{
                    Text("列表为空 \n")
                }
                var inputText by remember{ mutableStateOf("") }
                TextField(
                    value = inputText,
                    onValueChange = {inputText=it},
                    label = {Text("账号类型")},
                    modifier = Modifier.padding(bottom = 4.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusManager.clearFocus()})
                )
                Button(onClick={focusManager.clearFocus()
                    myDpm.setAccountManagementDisabled(myComponent,inputText,true)
                    noManageAccount=myDpm.accountTypesWithManagementDisabled?.toMutableList()
                    refreshList()
                }){
                    Text("添加至列表")
                }
                Button(onClick={focusManager.clearFocus()
                    myDpm.setAccountManagementDisabled(myComponent,inputText,false)
                    noManageAccount=myDpm.accountTypesWithManagementDisabled?.toMutableList()
                    refreshList()
                }){
                    Text("从列表中移除")
                }
            }
        }

        if(isdo&&VERSION.SDK_INT>=24){
            DeviceOwnerInfo(R.string.owner_lockscr_info,R.string.place_holder,R.string.owner_lockscr_info,focusManager,myContext,
                {myDpm.deviceOwnerLockScreenInfo},{content ->  myDpm.setDeviceOwnerLockScreenInfo(myComponent,content)})
            DeviceOwnerInfo(R.string.support_msg,R.string.support_msg_desc,R.string.message,focusManager,myContext,
                {myDpm.getShortSupportMessage(myComponent)},{content ->  myDpm.setShortSupportMessage(myComponent,content)})
            DeviceOwnerInfo(R.string.long_support_msg,R.string.long_support_msg_desc,R.string.message,focusManager,myContext,
                {myDpm.getLongSupportMessage(myComponent)},{content ->  myDpm.setLongSupportMessage(myComponent,content)})
        }
        Spacer(Modifier.padding(vertical = 20.dp))
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
        Text(text = stringResource(name), style = MaterialTheme.typography.titleLarge)
        if(desc!=R.string.place_holder){Text(text = stringResource(desc),modifier = Modifier.padding(top = 6.dp))}
        var inputContent by remember{ mutableStateOf(input()) }
        TextField(
            value = if(inputContent!=null){ inputContent.toString() }else{""},
            label = {Text(stringResource(textfield))},
            onValueChange = { inputContent=it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        )
        Row(
            modifier = Modifier.padding(vertical = 6.dp)
        ) {
            Button(
                onClick = {
                    output(inputContent.toString())
                    inputContent= input()
                    fm.clearFocus()
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                }
            ) {
                Text(text = "应用")
            }
            Spacer(Modifier.padding(horizontal = 4.dp))
            Button(
                onClick = {
                    output(null)
                    inputContent = input()
                    fm.clearFocus()
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                }
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
    intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
    startActivity(inputContext,intent,null)
}
