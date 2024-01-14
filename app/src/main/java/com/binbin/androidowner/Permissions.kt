package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable
fun DpmPermissions(myDpm: DevicePolicyManager, myComponent: ComponentName, myContext:Context){
    //da:DeviceAdmin do:DeviceOwner
    val isda = myDpm.isAdminActive(myComponent)
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Text("Device Admin: $isda")
        Text("Device Owner: $isdo")
        SelectionContainer {
            Column {
                Text("设置DeviceAdmin命令：dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver")
                Text("设置DeviceOwner命令：dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver")
            }
        }
        Button(onClick = {Runtime.getRuntime().exec("su -c \"dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver\"")}) {
            Text("获取DeviceAdmin（需root，未测试）")
        }
        Button(onClick = {Runtime.getRuntime().exec("su -c \"dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver\"")}) {
            Text("获取DeviceOwner（需root，未测试）")
        }
        Text("注意！在这里清除权限不会清除配置。比如：被停用的应用会保持停用状态")
        Button(onClick = {myDpm.clearDeviceOwnerApp("com.binbin.androidowner")}) {
            Text("不当Device Owner了")
        }
        Button(onClick = {myDpm.removeActiveAdmin(myComponent)}) {
            Text("不当Device Admin了（同时会取消DeviceOwner）")
        }
    }
}
