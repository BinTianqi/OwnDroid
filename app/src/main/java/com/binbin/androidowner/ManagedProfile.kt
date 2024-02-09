package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ManagedProfile() {
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium}else{ typography.bodyLarge}
    Column(modifier = Modifier.verticalScroll(rememberScrollState())){
        
        Column(modifier = sections()){
            Text(text = "信息", style = typography.titleLarge)
            if(VERSION.SDK_INT>=24){
                if(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
                    Text(text = "已是工作资料")
                }else{
                    Text(text = "可以创建工作资料：${myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE)}", style = bodyTextStyle)
                    if(isDeviceOwner(myDpm)){
                        Text(text = "Device owner不能创建工作资料", style = bodyTextStyle)
                    }
                }
            }
            if(VERSION.SDK_INT>=30){
                Text(text = "由组织拥有的工作资料：${myDpm.isOrganizationOwnedDeviceWithManagedProfile}", style = bodyTextStyle)
            }
            if(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent))){
                Button(
                    onClick = { myContext.startActivity(Intent("com.binbin.androidowner.MAIN_ACTION")) }, modifier = Modifier.fillMaxWidth()
                ){
                    Text("跳转至个人应用")
                }
            }else{
                if(!myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE)){
                    Button(
                        onClick = { myContext.startActivity(Intent("com.binbin.androidowner.MAIN_ACTION")) }, modifier = Modifier.fillMaxWidth()
                    ){
                        Text("跳转至工作资料")
                    }
                }
            }
        }
        
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)&&!myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            Column(modifier = sections(colorScheme.tertiaryContainer)){
                Text("成为组织拥有的工作资料")
                Text(text = "首先在“用户管理”中查看UserID，然后使用ADB执行下面这条命令", style = bodyTextStyle)
                SelectionContainer {
                    Text(
                        text = "adb shell “dpm mark-profile-owner-on-organization-owned-device --user USER_ID com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver”",
                        color = colorScheme.onTertiaryContainer, style = bodyTextStyle
                    )
                }
                Text(text = "把上面命令中的USER_ID替换成你的UserID", style = bodyTextStyle)
            }
        }
        if(!isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE)))){
            Column(modifier = sections()) {
                Text(text = "工作资料", style = typography.titleLarge)
                var skipEncrypt by remember{mutableStateOf(false)}
                if(VERSION.SDK_INT>=24){CheckBoxItem("跳过加密",{skipEncrypt},{skipEncrypt=!skipEncrypt})}
                Button(
                    onClick = {
                        val intent = Intent(ACTION_PROVISION_MANAGED_PROFILE)
                        if(VERSION.SDK_INT>=23){
                            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,myComponent)
                        }else{
                            intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,"com.binbin.androidowner")
                        }
                        if(VERSION.SDK_INT>=24){intent.putExtra(EXTRA_PROVISIONING_SKIP_ENCRYPTION,skipEncrypt)}
                        if(VERSION.SDK_INT>=33){intent.putExtra(EXTRA_PROVISIONING_ALLOW_OFFLINE,true)}
                        createManagedProfile.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("创建")
                }
            }
        }
        
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            Row(modifier = sections(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                var suspended by remember{mutableStateOf(false)}
                suspended = myDpm.getPersonalAppsSuspendedReasons(myComponent)!=PERSONAL_APPS_NOT_SUSPENDED
                Text(text = "挂起个人应用", style = typography.titleLarge)
                Switch(
                    checked = suspended,
                    onCheckedChange ={
                        myDpm.setPersonalAppsSuspended(myComponent,!suspended)
                        suspended = myDpm.getPersonalAppsSuspendedReasons(myComponent)!=PERSONAL_APPS_NOT_SUSPENDED
                    }
                )
            }
        }
        
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            Column(modifier = sections()){
                var time by remember{mutableStateOf("")}
                time = myDpm.getManagedProfileMaximumTimeOff(myComponent).toString()
                Text(text = "资料关闭时间", style = typography.titleLarge)
                Text(text = "工作资料处于关闭状态的时间达到该限制后会挂起个人应用，0为无限制", style = bodyTextStyle)
                Text(text = "个人应用已经因此挂起：${myDpm.getPersonalAppsSuspendedReasons(myComponent)==PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT}")
                TextField(
                    value = time, onValueChange = {time=it}, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    label = {Text("时间(ms)")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
                )
                Text(text = "不能少于72小时", style = bodyTextStyle)
                Button(
                    onClick = {
                        myDpm.setManagedProfileMaximumTimeOff(myComponent,time.toLong())
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
                }
            }
        }
        
        if(isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)))){
            Column(modifier = sections()){
                var action by remember{mutableStateOf("")}
                Text(text = "Intent过滤器", style = typography.titleLarge)
                TextField(
                    value = action, onValueChange = {action = it},
                    label = {Text("Action")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                Button(
                    onClick = {
                        myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter(action), FLAG_PARENT_CAN_ACCESS_MANAGED)
                        Toast.makeText(myContext,"成功",Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("添加（工作到个人）")
                }
                Button(
                    onClick = {
                        myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter(action), FLAG_MANAGED_CAN_ACCESS_PARENT)
                        Toast.makeText(myContext,"成功",Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("添加（个人到工作）")
                }
                Button(
                    onClick = {
                        myDpm.clearCrossProfileIntentFilters(myComponent)
                        myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_MANAGED_CAN_ACCESS_PARENT)
                        myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_PARENT_CAN_ACCESS_MANAGED)
                        Toast.makeText(myContext,"成功",Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("清除所有过滤器")
                }
            }
        }
        
        if(VERSION.SDK_INT>=31&&(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent))){
            Column(modifier = sections()){
                var orgId by remember{mutableStateOf("")}
                Text(text = "组织ID", style = typography.titleLarge)
                TextField(
                    value = orgId, onValueChange = {orgId=it},
                    label = {Text("组织ID")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                AnimatedVisibility(orgId.length !in 6..64) {
                    Text(text = "长度应在6~64个字符之间", style = bodyTextStyle)
                }
                Button(
                    onClick = {
                        myDpm.setOrganizationId(orgId)
                        Toast.makeText(myContext,"成功",Toast.LENGTH_SHORT).show()
                    },
                    enabled = orgId.length in 6..64,
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("应用")
                }
                Text(text = "设置组织ID后才能获取设备唯一标识码", style = bodyTextStyle)
            }
        }
        
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
fun ActivateManagedProfile(navCtrl: NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val sharedPref = myContext.getSharedPreferences("data", Context.MODE_PRIVATE)
    myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_MANAGED_CAN_ACCESS_PARENT)
    myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_PARENT_CAN_ACCESS_MANAGED)
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally){
        Text(text = "激活工作资料", style = typography.titleLarge)
        Text(text = "你还没有激活工作资料，请立即激活")
        Button(
            onClick = {
                myDpm.setProfileEnabled(myComponent)
                navCtrl.popBackStack("HomePage",false)
                sharedPref.edit().putBoolean("ManagedProfileActivated",true).apply()
                Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text("激活")
        }
    }
}
