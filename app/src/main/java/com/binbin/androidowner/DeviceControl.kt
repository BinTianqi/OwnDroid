package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build
import android.os.Build.VERSION
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DeviceControl(myDpm: DevicePolicyManager, myComponent: ComponentName){
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp)
            .navigationBarsPadding()
    ) {
        DeviceCtrlItem(R.string.disable_cam,R.string.place_holder, R.drawable.photo_camera_fill0,myDpm,{myDpm.getCameraDisabled(null)},{b -> myDpm.setCameraDisabled(myComponent,b)})
        DeviceCtrlItem(R.string.disable_scrcap,R.string.aosp_scrrec_also_work,R.drawable.screenshot_fill0,myDpm,{myDpm.getScreenCaptureDisabled(null)},{b -> myDpm.setScreenCaptureDisabled(myComponent,b) })
        if(VERSION.SDK_INT>=34){
            DeviceCtrlItem(R.string.hide_status_bar,R.string.may_hide_notifi_icon_only,R.drawable.notifications_fill0,myDpm,{myDpm.isStatusBarDisabled},{b -> myDpm.setStatusBarDisabled(myComponent,b) })
        }
        if(VERSION.SDK_INT>=30){
            DeviceCtrlItem(R.string.auto_time,R.string.place_holder,R.drawable.schedule_fill0,myDpm,{myDpm.getAutoTimeEnabled(myComponent)},{b -> myDpm.setAutoTimeEnabled(myComponent,b) })
            DeviceCtrlItem(R.string.auto_timezone,R.string.place_holder,R.drawable.globe_fill0,myDpm,{myDpm.getAutoTimeZoneEnabled(myComponent)},{b -> myDpm.setAutoTimeZoneEnabled(myComponent,b) })
        }
        DeviceCtrlItem(R.string.master_mute,R.string.place_holder,R.drawable.volume_up_fill0,myDpm,{myDpm.isMasterVolumeMuted(myComponent)},{b -> myDpm.setMasterVolumeMuted(myComponent,b) })
        if(VERSION.SDK_INT>=26){
            DeviceCtrlItem(R.string.backup_service,R.string.place_holder,R.drawable.backup_fill0,myDpm,{myDpm.isBackupServiceEnabled(myComponent)},{b -> myDpm.setBackupServiceEnabled(myComponent,b) })
        }
        if(VERSION.SDK_INT>=31){
            if(myDpm.canUsbDataSignalingBeDisabled()){
                DeviceCtrlItem(R.string.usb_signal,R.string.place_holder,R.drawable.usb_fill0,myDpm,{myDpm.isUsbDataSignalingEnabled},{b -> myDpm.isUsbDataSignalingEnabled = b })
            }else{
                Text("你的设备不支持关闭USB信号")
            }
        }
        if(VERSION.SDK_INT>=24){
            Button(onClick = {myDpm.reboot(myComponent)}) {
                Text("重启")
            }
            val wifimac = try {
                myDpm.getWifiMacAddress(myComponent).toString()
            }catch(e:SecurityException){
                "没有权限"
            }
            Text("WiFi MAC: $wifimac")
        }
        if(VERSION.SDK_INT<24){
            Text("重启和WiFi Mac需要API24")
        }
        if(VERSION.SDK_INT<26){
            Text("备份服务需要API26")
        }
        if(VERSION.SDK_INT<30){
            Text("自动设置时间和自动设置时区需要API30")
        }
        if(VERSION.SDK_INT<31){Text("关闭USB信号需API31")}
        if(VERSION.SDK_INT<34){
            Text("隐藏状态栏需要API34")
        }
        Button(onClick = {myDpm.lockNow()}) {
            Text("锁屏")
        }
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
        if (VERSION.SDK_INT >= 34) {
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

@Composable
private fun DeviceCtrlItem(
    itemName:Int,
    itemDesc:Int,
    leadIcon:Int,
    myDpm: DevicePolicyManager,
    getMethod:()->Boolean,
    setMethod:(b:Boolean)->Unit
){
    var isEnabled by remember{ mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 6.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(15))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ){
            Icon(
                painter = painterResource(leadIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(start = 5.dp, end = 9.dp)
            )
            Column {
                Text(
                    text = stringResource(itemName),
                    style = MaterialTheme.typography.titleLarge
                )
                if(itemDesc!=R.string.place_holder){
                    Text(stringResource(itemDesc))
                }
            }
        }
        if(myDpm.isDeviceOwnerApp("com.binbin.androidowner")){
            isEnabled = getMethod()
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                setMethod(!isEnabled)
                isEnabled=getMethod()
            },
            enabled = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
        )
    }
}
