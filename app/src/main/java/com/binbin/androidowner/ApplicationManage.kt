package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.pm.PackageManager.NameNotFoundException
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ApplicationManage(myDpm:DevicePolicyManager, myComponent:ComponentName){
    var pkgName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var isAppHidden by remember{ mutableStateOf(false) }
        var isAppSuspended by remember{ mutableStateOf(false) }
        var isAppUninstallBlock by remember{ mutableStateOf(false) }
        var suspendedReply = ""
        isAppHidden = try {
            myDpm.isApplicationHidden(myComponent,pkgName)
        }catch (e:SecurityException){
            false
        }
        isAppUninstallBlock = try {
            myDpm.isUninstallBlocked(myComponent,pkgName)
        }catch (e:SecurityException){
            false
        }
        try{
            isAppSuspended = myDpm.isPackageSuspended(myComponent,pkgName)
        }catch(e:NameNotFoundException){
            suspendedReply = "应用不存在！"
            isAppSuspended = false
        }catch (e:SecurityException){
            suspendedReply = "无权限"
            isAppSuspended = false
        }
        Text("以下功能都需要DeviceOwner权限")
        TextField(value = pkgName, onValueChange = {pkgName = it}, label = { Text("包名") })
        Spacer(Modifier.padding(5.dp))
        Row{
            Button(onClick = { myDpm.setApplicationHidden(myComponent,pkgName,true); isAppHidden = myDpm.isApplicationHidden(myComponent,pkgName) },modifier = Modifier.padding(end = 8.dp)) {
                Text("隐藏")
            }
            Button(onClick = { myDpm.setApplicationHidden(myComponent,pkgName,false); isAppHidden = myDpm.isApplicationHidden(myComponent,pkgName) }) {
                Text("显示")
            }
        }
        Text("应用隐藏：$isAppHidden ${if(isAppHidden){"（这个应用也许没有被安装）"}else{""}}")
        Row{
            Button(onClick = {myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName),true); isAppSuspended = myDpm.isPackageSuspended(myComponent,pkgName)},modifier = Modifier.padding(end = 8.dp)) {
                Text("停用")
            }
            Button(onClick = {myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName),false); isAppSuspended = myDpm.isPackageSuspended(myComponent,pkgName)}) {
                Text("启用")
            }
        }
        Text("应用停用：$isAppSuspended $suspendedReply")
        Text("阻止卸载功能有可能出问题")
        Row {
            Button(onClick = { myDpm.setUninstallBlocked(myComponent, pkgName, true); isAppUninstallBlock = myDpm.isUninstallBlocked(myComponent,pkgName) },modifier = Modifier.padding(end = 8.dp)) {
                Text("阻止卸载")
            }
            Button(onClick = { myDpm.setUninstallBlocked(myComponent, pkgName, false); isAppUninstallBlock = myDpm.isUninstallBlocked(myComponent,pkgName)}) {
                Text("允许卸载")
            }
        }
        Text("应用防卸载：$isAppUninstallBlock ${if(!isAppUninstallBlock){"（这个应用也许没有被安装）"}else{""}}")
    }
}
