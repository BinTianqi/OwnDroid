package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity


@Composable
fun DpmPermissions(myDpm: DevicePolicyManager, myComponent: ComponentName, myContext:Context){
    //da:DeviceAdmin do:DeviceOwner
    val isda = myDpm.isAdminActive(myComponent)
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    val ispo = myDpm.isProfileOwnerApp("com.binbin.androidowner")

    Column(
        modifier = Modifier
            .padding(8.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("权限：DeviceAdmin < ProfileOwner < DeviceOwner")
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
        ) {
            Text(text = "Device Admin", style = MaterialTheme.typography.titleLarge)
            Text("Device Admin: $isda")
            SelectionContainer {
                Text("dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver")
            }
            Button(onClick = {Runtime.getRuntime().exec("su -c \"dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver\"")}) {
                Text("获取DeviceAdmin（需root，未测试）")
            }
            Button(onClick = { ActivateDeviceAdmin(myDpm, myComponent, myContext) }) {
                Text("在设置中激活DeviceAdmin")
            }
            Button(onClick = {myDpm.removeActiveAdmin(myComponent)}) {
                Text("不当Device Admin了")
            }
        }
        Spacer(modifier = Modifier.padding(6.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
        ) {
            Text(text = "Device Owner", style = MaterialTheme.typography.titleLarge)
            Text("Device Owner: $isdo")
            SelectionContainer {
                Text("dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver")
            }
            Button(onClick = {Runtime.getRuntime().exec("su -c \"dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver\"")}) {
                Text("获取DeviceOwner（需root，未测试）")
            }
            Button(onClick = {myDpm.clearDeviceOwnerApp("com.binbin.androidowner")}) {
                Text("不当Device Owner了")
            }
            Text("注意！在这里清除权限不会清除配置。比如：被停用的应用会保持停用状态")
            var lockScrInfo by remember { mutableStateOf("") }
            TextField(value = lockScrInfo, onValueChange = { lockScrInfo= it}, label = { Text("锁屏信息") })
            Button(onClick = {myDpm.setDeviceOwnerLockScreenInfo(myComponent,lockScrInfo)}) {
                Text("设置锁屏DeviceOwner信息")
            }
        }
        Spacer(modifier = Modifier.padding(6.dp))
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
        ) {
            Text("Profile Owner是一个过时的功能，目前在这个应用里没啥用。")
            Text(text = "Profile Owner", style = MaterialTheme.typography.titleLarge)
            Text("Profile Owner: $ispo")
            SelectionContainer {
                Text("dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver")
            }
            Button(onClick = {Runtime.getRuntime().exec("su -c \"dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver\"")}) {
                Text("获取ProfileOwner（需root，未测试）")
            }
            Button(onClick = {myDpm.clearProfileOwner(myComponent)}) {
                Text("不当Profile Owner了")
            }
        }
    }
}

fun ActivateDeviceAdmin(myDpm: DevicePolicyManager,myComponent: ComponentName,myContext: Context){
    if (!myDpm.isAdminActive(myComponent)) {
        val intent = Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, myComponent)
        intent.putExtra(
            DevicePolicyManager.EXTRA_ADD_EXPLANATION,
            "在这里激活Android Owner"
        )
        startActivity(myContext,intent,null)
    } else {
        Toast.makeText(myContext, "已经激活", Toast.LENGTH_SHORT).show()
    }

}
