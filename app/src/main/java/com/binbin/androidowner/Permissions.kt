package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
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
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp),
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
                Button(onClick = { ActivateDeviceAdmin(myComponent, myContext) }) {
                    Text("激活")
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp),
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
        Column(
            horizontalAlignment = Alignment.Start
        ) {
            if(isdo||isda){
                Text(
                    text = "注意！在这里撤销权限不会清除配置。比如：被停用的应用会保持停用状态",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(15))
                        .background(color = MaterialTheme.colorScheme.errorContainer)
                        .padding(6.dp)
                )
            }
            Spacer(Modifier.padding(5.dp))
            if(!isda){
                Text("你可以在adb shell中使用以下命令激活Device Admin")
                SelectionContainer {
                    Text("dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver")
                }
                Text("或者进入设置 -> 安全 -> 更多安全设置 -> 设备管理应用 -> Android Owner")
            }
            if(!isdo){
                Text("你可以在adb shell中使用以下命令激活Device Owner")
                SelectionContainer {
                    Text("dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver")
                }
                if(!isda){
                    Text("使用此命令也会激活Device Admin")
                }
            }
        }
        if(isdo&&VERSION.SDK_INT>=24){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(10.dp)
            ) {
                var lockScrInfo by remember { mutableStateOf(myDpm.deviceOwnerLockScreenInfo) }
                Text(text = "锁屏DeviceOwner信息", style = MaterialTheme.typography.titleLarge)
                TextField(
                    value = if(lockScrInfo!=null){lockScrInfo.toString()}else{""},
                    onValueChange = { lockScrInfo= it},
                    label = { Text("锁屏信息") },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
                Row {
                    Button(onClick = {
                        myDpm.setDeviceOwnerLockScreenInfo(myComponent,lockScrInfo)
                        lockScrInfo=myDpm.deviceOwnerLockScreenInfo
                        focusManager.clearFocus()
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("应用")
                    }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Button(onClick = {
                        myDpm.setDeviceOwnerLockScreenInfo(myComponent,null)
                        lockScrInfo=myDpm.deviceOwnerLockScreenInfo
                        focusManager.clearFocus()
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    }) {
                        Text("默认")
                    }
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(10.dp)
            ) {
                Text(text = "提示消息", style = MaterialTheme.typography.titleLarge)
                Text(text = "如果你禁用了某个功能，用户尝试使用这个功能时会看见这个消息（可多行）",modifier = Modifier.padding(vertical = 6.dp))
                var supportMsg by remember{ mutableStateOf(myDpm.getShortSupportMessage(myComponent)) }
                TextField(
                    value = if(supportMsg!=null){ supportMsg.toString() }else{""},
                    label = {Text("提示消息")},
                    onValueChange = { supportMsg=it },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.padding(vertical = 6.dp)
                ) {
                    Button(
                        onClick = {
                            myDpm.setShortSupportMessage(myComponent,supportMsg)
                            supportMsg=if(myDpm.getShortSupportMessage(myComponent)!=null){ myDpm.getShortSupportMessage(myComponent).toString() }else{""}
                            focusManager.clearFocus()
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "应用")
                    }
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Button(
                        onClick = {
                            myDpm.setShortSupportMessage(myComponent,null)
                            supportMsg = myDpm.getShortSupportMessage(myComponent)
                            focusManager.clearFocus()
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text(text = "默认")
                    }
                }

            }
        }
    }
}

fun ActivateDeviceAdmin(myComponent: ComponentName,myContext: Context){
    val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
    intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, myComponent)
    intent.putExtra(
        DevicePolicyManager.EXTRA_ADD_EXPLANATION,
        "在这里激活Android Owner"
    )
    intent.setFlags(FLAG_ACTIVITY_NEW_TASK)
    startActivity(myContext,intent,null)
}
