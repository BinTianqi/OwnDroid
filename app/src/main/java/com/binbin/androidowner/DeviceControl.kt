package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceControl(myDpm: DevicePolicyManager, myComponent: ComponentName){
    val wifimac = try {
        myDpm.getWifiMacAddress(myComponent).toString()
    }catch(e:SecurityException){
        "没有权限"
    }
    Column {
        Text("WiFi MAC: $wifimac")
        Button(onClick = {myDpm.setCameraDisabled(myComponent, true)}) {
            Text("禁用相机")
        }
        Button(onClick = {myDpm.setCameraDisabled(myComponent, false)}) {
            Text("启用相机")
        }
        Button(onClick = {myDpm.setScreenCaptureDisabled(myComponent,true)}) {
            Text("禁止截屏")
        }
        Button(onClick = {myDpm.setScreenCaptureDisabled(myComponent,false)}) {
            Text("允许截屏")
        }
        Button(onClick = {myDpm.reboot(myComponent)}) {
            Text("重启")
        }
        Button(onClick = {myDpm.lockNow()}) {
            Text("锁屏")
        }
        Button(
            onClick = {},
            modifier = Modifier
                .combinedClickable(onClick = {}, onLongClick = {myDpm.wipeData(0)}),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
        Text(
            text = "FACTORY_RESET!!!!! (长按)（未测试）",
            color = MaterialTheme.colorScheme.error)
        }
    }
}
