package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.binbin.androidowner.ui.theme.AndroidOwnerTheme

@Composable
fun ApplicationManage(myDpm:DevicePolicyManager, myComponent:ComponentName){
    var pkgName by remember { mutableStateOf("com.mihoyo.yuanshen") }
    Column {
        Text("以下功能都需要DeviceOwner权限")
        TextField(value = pkgName, onValueChange = {pkgName = it}, label = { Text("包名") })
        Button(onClick = { myDpm.setApplicationHidden(myComponent,pkgName,true) }) {
            Text("隐藏")
        }
        Button(onClick = { myDpm.setApplicationHidden(myComponent,pkgName,false) }) {
            Text("显示")
        }
        val isAppHidden = myDpm.isApplicationHidden(myComponent,pkgName)
        Text("应用隐藏：$isAppHidden ${if(isAppHidden==true){"（这个应用也许没有被安装）"}else{""}}")
        Button(onClick = {myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName),true)}) {
            Text("停用")
        }
        Button(onClick = {myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName),true)}) {
            Text("启用")
        }
        Button(onClick = { myDpm.setUninstallBlocked(myComponent, pkgName, true) }) {
            Text("禁止卸载")
        }
        Button(onClick = { myDpm.setUninstallBlocked(myComponent, pkgName, false)}) {
            Text("允许卸载")
        }
    }
}
