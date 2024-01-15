package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

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
        var isCameraDisabled by remember{ mutableStateOf(myDpm.getCameraDisabled(null)) }
        Button(onClick = {myDpm.setCameraDisabled(myComponent, true);isCameraDisabled = myDpm.getCameraDisabled(null)}) {
            Text("禁用相机")
        }
        Button(onClick = {myDpm.setCameraDisabled(myComponent, false);isCameraDisabled = myDpm.getCameraDisabled(null)}) {
            Text("启用相机")
        }
        Text("AVD上没有相机，未测试")
        Text("相机被禁用：$isCameraDisabled")
        var isScrCapDisabled by remember{ mutableStateOf(myDpm.getScreenCaptureDisabled(null)) }
        Button(onClick = {myDpm.setScreenCaptureDisabled(myComponent,true);isScrCapDisabled = myDpm.getScreenCaptureDisabled(null)}) {
            Text("禁止截屏")
        }
        Button(onClick = {myDpm.setScreenCaptureDisabled(myComponent,false);isScrCapDisabled = myDpm.getScreenCaptureDisabled(null)}) {
            Text("允许截屏")
        }
        Text("对AOSP的录屏也起作用")
        Text("禁止截屏：$isScrCapDisabled")
        Button(onClick = {myDpm.reboot(myComponent)}) {
            Text("重启")
        }
        Button(onClick = {myDpm.lockNow()}) {
            Text("锁屏")
        }
        var isMasterMuted by remember{ mutableStateOf(false) }
        isMasterMuted = try{ myDpm.isMasterVolumeMuted(myComponent) }catch(e:SecurityException){ false }
        Button(onClick = {myDpm.setMasterVolumeMuted(myComponent,true);isMasterMuted=myDpm.isMasterVolumeMuted(myComponent)}) {
            Text("全部静音")
        }
        Button(onClick = {myDpm.setMasterVolumeMuted(myComponent,false);isMasterMuted=myDpm.isMasterVolumeMuted(myComponent)}) {
            Text("取消静音")
        }
        Text("静音：$isMasterMuted")
        Text("以下功能需要长按按钮，作者并未测试")
        Button(
            onClick = {},
            modifier = Modifier
                .combinedClickable(onClick = {}, onLongClick = {myDpm.wipeData(0)}),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text("WipeData")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            Button(
                modifier = Modifier
                    .combinedClickable(onClick = {}, onLongClick = {myDpm.wipeDevice(0)}),
                onClick = {},
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("WipeDevice(API34)")
            }
        }
    }
}
