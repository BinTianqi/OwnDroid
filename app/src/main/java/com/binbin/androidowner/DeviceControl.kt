package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DeviceControl(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
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
                Text(text = "你的设备不支持关闭USB信号",modifier = Modifier.fillMaxWidth(),
                    style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium}, textAlign = TextAlign.Center)
            }
        }
        if(VERSION.SDK_INT<24&&isDeviceOwner(myDpm)){
            Text(text = "重启和WiFi Mac需要API24",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
        }
        if(VERSION.SDK_INT<26&&isDeviceOwner(myDpm)){
            Text(text = "备份服务需要API26",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
        }
        if(VERSION.SDK_INT<30&&isDeviceOwner(myDpm)){
            Text(text = "自动设置时间和自动设置时区需要API30",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
        }
        if(VERSION.SDK_INT<31&&isDeviceOwner(myDpm)){Text(text = "关闭USB信号需API31",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
            style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})}
        if(VERSION.SDK_INT<34&&isDeviceOwner(myDpm)){
            Text(text = "隐藏状态栏需要API34",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,
                style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
        }
        if(VERSION.SDK_INT>=28){
            Column(modifier = sections()) {
                Text(text = "锁屏方式", style = typography.titleLarge,color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "禁用需要无密码",style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        if(myDpm.setKeyguardDisabled(myComponent,true)){
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.48F)}
                ) {
                    Text("禁用")
                }
                Button(
                    onClick = {
                        if(myDpm.setKeyguardDisabled(myComponent,false)){
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.92F)}
                ) {
                    Text("启用")
                }
            }}
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = sections(),
        ) {
            if(VERSION.SDK_INT>=24){
                Button(onClick = {myDpm.reboot(myComponent)}, enabled = isDeviceOwner(myDpm),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.48F)}) {
                    Text("重启")
                }
                Button(onClick = {myDpm.lockNow()}, enabled = myDpm.isAdminActive(myComponent),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.92F)}) {
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
            Text(text = "WiFi MAC: $wifimac",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
        Button(
            onClick = {myDpm.uninstallAllUserCaCerts(myComponent);Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()},
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "清除用户Ca证书")
        }}
        if(isDeviceOwner(myDpm)){
            SysUpdatePolicy(myDpm,myComponent,myContext)
        }
        Column(modifier = sections(if(isSystemInDarkTheme())
        {MaterialTheme.colorScheme.errorContainer}else{MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6F)})
        ) {
            var flag by remember{ mutableIntStateOf(0) }
            var confirmed by remember{ mutableStateOf(false) }
            Text(text = "清除数据",style = typography.titleLarge,modifier = Modifier.padding(6.dp),color = MaterialTheme.colorScheme.onErrorContainer)
            RadioButtonItem("默认",{flag==0},{flag=0},MaterialTheme.colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_EXTERNAL_STORAGE",{flag==0x0001},{flag=0x0001},MaterialTheme.colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_RESET_PROTECTION_DATA",{flag==0x0002},{flag=0x0002},MaterialTheme.colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_EUICC",{flag==0x0004},{flag=0x0004},MaterialTheme.colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_SILENTLY",{flag==0x0008},{flag=0x0008},MaterialTheme.colorScheme.onErrorContainer)
            Text(text = "清空数据的不能是系统用户",color = MaterialTheme.colorScheme.onErrorContainer,
                style = if(!sharedPref.getBoolean("isWear",false)){typography.bodyLarge}else{typography.bodyMedium})
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
            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
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
        Spacer(Modifier.padding(vertical = 30.dp))
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
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Row(
        modifier = sections(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if(isWear){Modifier.fillMaxWidth(0.65F)}else{Modifier.fillMaxWidth(0.75F)}
        ){
            if(!sharedPref.getBoolean("isWear",false)){
            Icon(
                painter = painterResource(leadIcon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(start = 5.dp, end = 9.dp)
            )}
            Column {
                Text(
                    text = stringResource(itemName),
                    style = if(!sharedPref.getBoolean("isWear",false)){typography.titleLarge}else{typography.bodyLarge},
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                if(itemDesc!=R.string.place_holder&&!sharedPref.getBoolean("isWear",false)){
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
            },
            modifier = Modifier.padding(end = 5.dp)
        )
    }
}
