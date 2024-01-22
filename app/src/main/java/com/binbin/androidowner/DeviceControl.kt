package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION
import android.os.UserManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DeviceControl(myDpm: DevicePolicyManager, myComponent: ComponentName,myContext: Context){
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(bottom = 20.dp)
            .navigationBarsPadding()
    ) {
        if(isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.disable_cam,R.string.place_holder, R.drawable.photo_camera_fill0,myDpm,{myDpm.getCameraDisabled(null)},{b -> myDpm.setCameraDisabled(myComponent,b)})
        }
        if(isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.disable_scrcap,R.string.aosp_scrrec_also_work,R.drawable.screenshot_fill0,myDpm,{myDpm.getScreenCaptureDisabled(null)},{b -> myDpm.setScreenCaptureDisabled(myComponent,b) })
        }
        if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser))){
            DeviceCtrlItem(R.string.hide_status_bar,R.string.may_hide_notifi_icon_only,R.drawable.notifications_fill0,myDpm,{myDpm.isStatusBarDisabled},{b -> myDpm.setStatusBarDisabled(myComponent,b) })
        }
        if(VERSION.SDK_INT>=30&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.auto_time,R.string.place_holder,R.drawable.schedule_fill0,myDpm,{myDpm.getAutoTimeEnabled(myComponent)},{b -> myDpm.setAutoTimeEnabled(myComponent,b) })
            DeviceCtrlItem(R.string.auto_timezone,R.string.place_holder,R.drawable.globe_fill0,myDpm,{myDpm.getAutoTimeZoneEnabled(myComponent)},{b -> myDpm.setAutoTimeZoneEnabled(myComponent,b) })
        }
        if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
            DeviceCtrlItem(R.string.master_mute,R.string.place_holder,R.drawable.volume_up_fill0,myDpm,{myDpm.isMasterVolumeMuted(myComponent)},{b -> myDpm.setMasterVolumeMuted(myComponent,b) })
        }
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)|| isProfileOwner(myDpm))){
            DeviceCtrlItem(R.string.backup_service,R.string.place_holder,R.drawable.backup_fill0,myDpm,{myDpm.isBackupServiceEnabled(myComponent)},{b -> myDpm.setBackupServiceEnabled(myComponent,b) })
        }
        if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
            DeviceCtrlItem(R.string.disable_bt_contact_share,R.string.place_holder,R.drawable.account_circle_fill0,myDpm,{myDpm.getBluetoothContactSharingDisabled(myComponent)},{b -> myDpm.setBluetoothContactSharingDisabled(myComponent,b)})
        }
        if(VERSION.SDK_INT>=31&&isDeviceOwner(myDpm)){
            if(myDpm.canUsbDataSignalingBeDisabled()){
                DeviceCtrlItem(R.string.usb_signal,R.string.place_holder,R.drawable.usb_fill0,myDpm,{myDpm.isUsbDataSignalingEnabled},{b -> myDpm.isUsbDataSignalingEnabled = b })
            }else{
                Text("你的设备不支持关闭USB信号")
            }
        }
        if(VERSION.SDK_INT>=28){
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(15))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 5.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    onClick = {
                        if(myDpm.setKeyguardDisabled(myComponent,true)){
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser)
                ) {
                    Text("禁用锁屏（需无密码）")
                }
                Spacer(Modifier.padding(horizontal = 5.dp))
                Button(
                    onClick = {
                        if(myDpm.setKeyguardDisabled(myComponent,false)){
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser)
                ) {
                    Text("启用锁屏")
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 6.dp)
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 4.dp),
        ) {
            if(VERSION.SDK_INT>=24){
                Button(onClick = {myDpm.reboot(myComponent)}, enabled = isDeviceOwner(myDpm)) {
                    Text("重启")
                }
                Button(onClick = {myDpm.lockNow()}, enabled = myDpm.isAdminActive(myComponent)) {
                    Text("锁屏")
                }
            }
        }
        if(VERSION.SDK_INT>=24){
            val wifimac = try {
                myDpm.getWifiMacAddress(myComponent).toString()
            }catch(e:SecurityException){
                "没有权限"
            }
            Text(text = "WiFi MAC: $wifimac",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
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
        Button(
            onClick = {myDpm.uninstallAllUserCaCerts(myComponent)},
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm)
        ) {
            Text(text = "清除用户Ca证书")
        }
        if(isDeviceOwner(myDpm)){
            SysUpdatePolicy(myDpm,myComponent,myContext)
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = MaterialTheme.colorScheme.errorContainer)
                .padding(8.dp)
        ) {
            var flag by remember{ mutableIntStateOf(0) }
            var confirmed by remember{ mutableStateOf(false) }
            Text(text = "清除数据",style = MaterialTheme.typography.titleLarge,modifier = Modifier.padding(6.dp))
            RadioButtonItem("默认",{flag==0},{flag=0})
            RadioButtonItem("WIPE_EXTERNAL_STORAGE",{flag==0x0001},{flag=0x0001})
            RadioButtonItem("WIPE_RESET_PROTECTION_DATA",{flag==0x0002},{flag=0x0002})
            RadioButtonItem("WIPE_EUICC",{flag==0x0004},{flag=0x0004})
            RadioButtonItem("WIPE_SILENTLY",{flag==0x0008},{flag=0x0008})
            Text("清空数据的不能是系统用户")
            Button(
                onClick = {confirmed=!confirmed},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(confirmed){MaterialTheme.colorScheme.primary}else{MaterialTheme.colorScheme.error},
                    contentColor = if(confirmed){MaterialTheme.colorScheme.onPrimary}else{MaterialTheme.colorScheme.onError}
                ),
                enabled = myDpm.isAdminActive(myComponent)
            ) {
                Text(text = if(confirmed){"取消"}else{"确定"})
            }
            Row {
                Button(
                    onClick = {myDpm.wipeData(flag)},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    ),
                    enabled = confirmed
                ) {
                    Text("WipeData")
                }
                Spacer(Modifier.padding(horizontal = 5.dp))
                if (VERSION.SDK_INT >= 34) {
                    Button(
                        onClick = {myDpm.wipeDevice(flag)},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        enabled = confirmed
                    ) {
                        Text("WipeDevice(API34)")
                    }
                }
            }
        }
        Spacer(Modifier.padding(vertical = 20.dp))
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
        isEnabled = getMethod()
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                setMethod(!isEnabled)
                isEnabled=getMethod()
            }
        )
    }
}
