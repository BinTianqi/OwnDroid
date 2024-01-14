package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.UserManager
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceControl(myDpm: DevicePolicyManager, myComponent: ComponentName){
    var wifimac = "Unknown"
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    if(isdo){
        wifimac = myDpm.getWifiMacAddress(myComponent).toString()
    }
    Column {
        Text("WiFi MAC: $wifimac")
        Button(onClick = {myDpm.addUserRestriction(myComponent, UserManager.DISALLOW_BLUETOOTH)}) {
            Text("禁用蓝牙（未测试）")
        }
        Button(onClick = {myDpm.reboot(myComponent)}) {
            Text("重启")
        }
        Button(onClick = {myDpm.lockNow()}) {
            Text("锁屏")
        }
        Button(
                onClick = {},
        modifier = Modifier.combinedClickable(
            onClick = {},
            onLongClick = {myDpm.wipeData(0)})
        ) {
        Text("FACTORY_RESET!!!!! (长按)（未测试）", color = Color.Red)
        }
    }
}
