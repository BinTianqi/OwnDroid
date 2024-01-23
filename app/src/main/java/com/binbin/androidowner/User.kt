package com.binbin.androidowner

import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.UserHandle
import android.os.UserManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.UserManagerCompat


@Composable
fun UserManage(myDpm:DevicePolicyManager,myComponent:ComponentName,myContext: Context){
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        //val myUM = myContext.getSystemService(Context.USER_SERVICE)
        val currentUser = android.os.Process.myUserHandle()
        val userList = Test.returnUsers(myContext)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
        ) {
            Text(text = "用户信息", style = MaterialTheme.typography.titleLarge)
            Text("用户个数：${userList.size}")
            Spacer(Modifier.padding(vertical = 5.dp))
            Text("用户已解锁：${UserManagerCompat.isUserUnlocked(myContext)}")
            if(VERSION.SDK_INT>=24){
                Text("支持多用户：${UserManager.supportsMultipleUsers()}")
            }
            if(VERSION.SDK_INT>=31){
                Text("系统用户: ${UserManager.isHeadlessSystemUserMode()}")
            }
            Spacer(Modifier.padding(vertical = 5.dp))
            if (VERSION.SDK_INT >= 28) {
                val logoutable = myDpm.isLogoutEnabled
                Text(text = "用户可以退出 : $logoutable")
                if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
                    val ephemeralUser = myDpm.isEphemeralUser(myComponent)
                    Text(text = "临时用户： $ephemeralUser")
                }
                val affiliatedUser = myDpm.isAffiliatedUser
                Text(text = "次级用户: $affiliatedUser")
            }
            Spacer(Modifier.padding(5.dp))
            Text("切换用户后或设备重启后会删除临时用户")
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
        ) {
            Text(text = "用户操作", style = MaterialTheme.typography.titleLarge)
            if(VERSION.SDK_INT>28){
                var resultForLogout by remember{ mutableIntStateOf(-1) }
                var resultForStop by remember{ mutableIntStateOf(-1) }
                Text("登出用户需要成为次级用户的Profile Owner")
                Button(onClick = {resultForLogout = myDpm.logoutUser(myComponent)}, enabled = isProfileOwner(myDpm)) {
                    Text("登出用户")
                }
                if(resultForLogout!=-1){
                    Text(userOperationResultCode(resultForLogout))
                }
                Button(onClick = {myDpm.switchUser(myComponent,currentUser)}, enabled = isDeviceOwner(myDpm)) {
                    Text("切换用户")
                }
                Button(onClick = {resultForStop = myDpm.stopUser(myComponent,currentUser)}, enabled = isDeviceOwner(myDpm)) {
                    Text("停止用户")
                }
                if(resultForStop!=-1){
                    Text(userOperationResultCode(resultForStop))
                }
                Button(onClick = {myDpm.setProfileEnabled(myComponent)}, enabled = isProfileOwner(myDpm)||isDeviceOwner(myDpm)) {
                    Text(text = "启用资料")
                }
            }
            Button(
                onClick = {
                    val success = myDpm.removeUser(myComponent,currentUser)
                    if(success){
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isDeviceOwner(myDpm)
            ) {
                Text("移除用户")
            }
            Button(onClick = { createWorkProfile(myContext,myComponent) }) {
                Text("创建工作资料")
            }
            Text("可能无法创建工作资料")
        }

        if(VERSION.SDK_INT>=24){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(10.dp)
            ) {
                var userName by remember{ mutableStateOf("") }
                Text(text = "创建用户", style = MaterialTheme.typography.titleLarge)
                TextField(
                    value = userName,
                    onValueChange = {userName=it},
                    label = {Text("用户名")},
                    modifier = Modifier.padding(vertical = 4.dp),
                    enabled = isDeviceOwner(myDpm)
                )
                var selectedFlag by remember{ mutableIntStateOf(0) }
                RadioButtonItem("无",{selectedFlag==0},{selectedFlag=0})
                RadioButtonItem("跳过创建用户向导",{selectedFlag==DevicePolicyManager.SKIP_SETUP_WIZARD},{selectedFlag=DevicePolicyManager.SKIP_SETUP_WIZARD})
                if(VERSION.SDK_INT>=28){
                    RadioButtonItem("临时用户",{selectedFlag==DevicePolicyManager.MAKE_USER_EPHEMERAL},{selectedFlag=DevicePolicyManager.MAKE_USER_EPHEMERAL})
                    RadioButtonItem("启用所有系统应用",{selectedFlag==DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED},{selectedFlag=DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED})
                }
                var newUserHandle: UserHandle? by remember{ mutableStateOf(null) }
                Row {
                    Button(
                        onClick = {newUserHandle=myDpm.createAndManageUser(myComponent,userName,myComponent,null,selectedFlag)},
                        enabled = isDeviceOwner(myDpm)
                    ) {
                        Text("创建")
                    }
                    if(newUserHandle!=null){
                        Spacer(Modifier.padding(horizontal = 4.dp))
                        Button(
                            onClick = {
                                if(myDpm.switchUser(myComponent,newUserHandle)){
                                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                                } else{
                                    Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("切换至新用户")
                        }
                    }
                }

            }
        }else{
            Text("创建用户需安卓7")
        }
        UserSessionMessage("用户名","用户名",true,myDpm,myContext,{null},{msg ->  myDpm.setProfileName(myComponent, msg.toString())})
        if(VERSION.SDK_INT>=28){
            UserSessionMessage("用户会话开始消息","消息",false,myDpm,myContext,{myDpm.getStartUserSessionMessage(myComponent)},{msg ->  myDpm.setStartUserSessionMessage(myComponent,msg)})
            UserSessionMessage("用户会话结束消息","消息",false,myDpm,myContext,{myDpm.getEndUserSessionMessage(myComponent)},{msg ->  myDpm.setEndUserSessionMessage(myComponent,msg)})
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
fun UserSessionMessage(
    text:String,
    textField:String,
    profileOwner:Boolean,
    myDpm:DevicePolicyManager,
    myContext: Context,
    get:()->CharSequence?,
    setMsg:(msg:CharSequence?)->Unit
){
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(10.dp)
    ) {
        val focusMgr = LocalFocusManager.current
        var msg by remember{ mutableStateOf(if(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner)){ if(get()==null){""}else{get().toString()} }else{""}) }
        Text(text = text, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
        TextField(
            value = msg,
            onValueChange = {msg=it},
            label = {Text(textField)},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.padding(vertical = 6.dp),
            enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner)
        )
        Row {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    setMsg(msg)
                    msg = if(get()==null){""}else{get().toString()}
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner)
            ) {
                Text("应用")
            }
            Spacer(Modifier.padding(horizontal = 5.dp))
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    setMsg(null)
                    msg = if(get()==null){""}else{get().toString()}
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner)
            ) {
                Text("使用默认")
            }
        }
    }
}

fun userOperationResultCode(result:Int): String {
    return when(result){
        UserManager.USER_OPERATION_SUCCESS->"USER_OPERATION_SUCCESS"
        UserManager.USER_OPERATION_ERROR_UNKNOWN->"USER_OPERATION_ERROR_UNKNOWN"
        UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE->"USER_OPERATION_ERROR_MANAGED_PROFILE"
        UserManager.USER_OPERATION_ERROR_CURRENT_USER->"USER_OPERATION_ERROR_CURRENT_USER"
        else->"Unknown"
    }
}

private fun createWorkProfile(myContext: Context,myComponent: ComponentName) {
    val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
    intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME, "com.binbin.androidowner")
    intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME, myComponent)
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE, true)
    }
    intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,"hello")
    /*
    intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_USER_CONSENT, false)
    val adminExtras = PersistableBundle()
    if (adminExtras.size() > 0) {
        intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ADMIN_EXTRAS_BUNDLE, adminExtras)
    }*/
    intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
    startActivity(myContext,intent,null)
}
