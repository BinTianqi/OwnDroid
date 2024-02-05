package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.app.admin.WifiSsidPolicy
import android.content.ComponentName
import android.content.Context
import android.net.wifi.WifiSsid
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DeviceControl(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){typography.bodyMedium}else{typography.bodyLarge}
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).navigationBarsPadding()) {
        if(isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.disable_cam,R.string.place_holder, R.drawable.photo_camera_fill0,
                {myDpm.getCameraDisabled(null)},{b -> myDpm.setCameraDisabled(myComponent,b)}
            )
        }
        if(isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.disable_scrcap,R.string.aosp_scrrec_also_work,R.drawable.screenshot_fill0,
                {myDpm.getScreenCaptureDisabled(null)},{b -> myDpm.setScreenCaptureDisabled(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser))){
            DeviceCtrlItem(R.string.hide_status_bar,R.string.may_hide_notifi_icon_only,R.drawable.notifications_fill0,
                {myDpm.isStatusBarDisabled},{b -> myDpm.setStatusBarDisabled(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=30&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.auto_time,R.string.place_holder,R.drawable.schedule_fill0,
                {myDpm.getAutoTimeEnabled(myComponent)},{b -> myDpm.setAutoTimeEnabled(myComponent,b) }
            )
            DeviceCtrlItem(R.string.auto_timezone,R.string.place_holder,R.drawable.globe_fill0,
                {myDpm.getAutoTimeZoneEnabled(myComponent)},{b -> myDpm.setAutoTimeZoneEnabled(myComponent,b) }
            )
        }else{
            DeviceCtrlItem(R.string.auto_time,R.string.place_holder,R.drawable.schedule_fill0,{myDpm.autoTimeRequired},{b -> myDpm.setAutoTimeRequired(myComponent,b)})
        }
        if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
            DeviceCtrlItem(R.string.master_mute,R.string.place_holder,R.drawable.volume_up_fill0,
                {myDpm.isMasterVolumeMuted(myComponent)},{b -> myDpm.setMasterVolumeMuted(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)|| isProfileOwner(myDpm))){
            DeviceCtrlItem(R.string.backup_service,R.string.place_holder,R.drawable.backup_fill0,
                {myDpm.isBackupServiceEnabled(myComponent)},{b -> myDpm.setBackupServiceEnabled(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=23&&(isDeviceOwner(myDpm)|| isProfileOwner(myDpm))){
            DeviceCtrlItem(R.string.disable_bt_contact_share,R.string.place_holder,R.drawable.account_circle_fill0,
                {myDpm.getBluetoothContactSharingDisabled(myComponent)},{b -> myDpm.setBluetoothContactSharingDisabled(myComponent,b)}
            )
        }
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            DeviceCtrlItem(R.string.network_logging,R.string.no_effect,R.drawable.wifi_fill0,
                {myDpm.isNetworkLoggingEnabled(myComponent)},{b -> myDpm.setNetworkLoggingEnabled(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=24&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.secure_logging,R.string.no_effect,R.drawable.description_fill0,
                {myDpm.isSecurityLoggingEnabled(myComponent)},{b -> myDpm.setSecurityLoggingEnabled(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=33&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.preferential_network_service,R.string.no_effect,R.drawable.globe_fill0,
                {myDpm.isPreferentialNetworkServiceEnabled},{b ->  myDpm.isPreferentialNetworkServiceEnabled = b}
            )
        }
        if(VERSION.SDK_INT>=30&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.common_criteria_mode,R.string.common_criteria_mode_desc,R.drawable.security_fill0,
                {myDpm.isCommonCriteriaModeEnabled(myComponent)},{b ->  myDpm.setCommonCriteriaModeEnabled(myComponent,b)}
            )
        }
        if(VERSION.SDK_INT>=31&&isDeviceOwner(myDpm)){
            if(myDpm.canUsbDataSignalingBeDisabled()){
                DeviceCtrlItem(R.string.usb_signal,R.string.place_holder,R.drawable.usb_fill0,
                    {myDpm.isUsbDataSignalingEnabled},{b -> myDpm.isUsbDataSignalingEnabled = b }
                )
            }else{
                Text(text = "你的设备不支持关闭USB信号",modifier = Modifier.fillMaxWidth(), style = bodyTextStyle, textAlign = TextAlign.Center)
            }
        }
        if(isDeviceOwner(myDpm)){
            if(VERSION.SDK_INT<23){ Text(text = "禁止蓝牙分享联系人需API23") }
            if(VERSION.SDK_INT<24){ Text(text = "安全日志API24",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = bodyTextStyle) }
            if(VERSION.SDK_INT<26){ Text(text = "备份服务和网络日志需要API26",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = bodyTextStyle) }
            if(VERSION.SDK_INT<30){ Text(text = "自动设置时区和通用标准模式需要API30",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = bodyTextStyle) }
            if(VERSION.SDK_INT<31){ Text(text = "关闭USB信号需API31",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = bodyTextStyle) }
            if(VERSION.SDK_INT<34){ Text(text = "隐藏状态栏需要API34",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = bodyTextStyle) }
        }
        if(VERSION.SDK_INT>=28){
            Column(modifier = sections()) {
                Text(text = "锁屏方式", style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
                Text(text = "禁用需要无密码",style=bodyTextStyle)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = { Toast.makeText(myContext, if(myDpm.setKeyguardDisabled(myComponent,true)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show() },
                    enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.48F)}
                ) {
                    Text("禁用")
                }
                Button(
                    onClick = { Toast.makeText(myContext, if(myDpm.setKeyguardDisabled(myComponent,false)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show() },
                    enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.92F)}
                ) {
                    Text("启用")
                }
            }}
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = sections()) {
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
        if(VERSION.SDK_INT<24){ Text(text = "重启和WiFi Mac需要API24",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center, style = bodyTextStyle) }
        if(VERSION.SDK_INT>=24){
            val wifimac = try { myDpm.getWifiMacAddress(myComponent).toString() }catch(e:SecurityException){ "没有权限" }
            Text(text = "WiFi MAC: $wifimac",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,style=bodyTextStyle)
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
        Button(
            onClick = {myDpm.uninstallAllUserCaCerts(myComponent);Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()},
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "清除用户Ca证书")
        }}
        
        if(VERSION.SDK_INT>=28){
            Column(modifier = sections()){
                Text(text = "修改时间", style = typography.titleLarge)
                var inputTime by remember{mutableStateOf("")}
                Text(text = "从epoch(1970/1/1 00:00:00 UTC)到现在(毫秒)")
                TextField(
                    value = inputTime,
                    label = { Text("时间(ms)")},
                    onValueChange = {inputTime = it},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                if(isWear){
                    Button(
                        onClick = {inputTime = System.currentTimeMillis().toString()},
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("获取当前时间")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {myDpm.setTime(myComponent,inputTime.toLong())},
                        modifier = Modifier.fillMaxWidth(if(isWear){1F}else{0.35F}),
                        enabled = inputTime!=""&&isDeviceOwner(myDpm)
                    ) {
                        Text("应用")
                    }
                    if(!isWear){
                        Button(
                            onClick = {inputTime = System.currentTimeMillis().toString()},
                            modifier = Modifier.fillMaxWidth(0.98F)
                        ) {
                            Text("获取当前时间")
                        }
                    }
                }
            }
        }
        
        if(VERSION.SDK_INT>=34&&isDeviceOwner(myDpm)){
            Column(modifier = sections()){
                Text(text = "MTE策略", style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                Text("MTE：内存标记拓展，安卓14和ARMv9的高端功能")
                var selectedMtePolicy by remember{mutableIntStateOf(myDpm.mtePolicy)}
                RadioButtonItem("由用户决定", {selectedMtePolicy==MTE_NOT_CONTROLLED_BY_POLICY}, {selectedMtePolicy= MTE_NOT_CONTROLLED_BY_POLICY})
                RadioButtonItem("开启", {selectedMtePolicy==MTE_ENABLED}, {selectedMtePolicy=MTE_ENABLED})
                RadioButtonItem("关闭", {selectedMtePolicy==MTE_DISABLED}, {selectedMtePolicy=MTE_DISABLED})
                Button(
                    onClick = {
                        try {
                            myDpm.mtePolicy = selectedMtePolicy
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }catch(e:java.lang.UnsupportedOperationException){
                            Toast.makeText(myContext, "不支持", Toast.LENGTH_SHORT).show()
                        }
                        selectedMtePolicy = myDpm.mtePolicy
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
                }
            }
        }
        
        if(VERSION.SDK_INT>=33){
            Column(modifier = sections()){
                var selectedWifiSecLevel by remember{mutableIntStateOf(myDpm.minimumRequiredWifiSecurityLevel)}
                Text(text = "要求最小WiFi安全等级", style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                RadioButtonItem("开放", {selectedWifiSecLevel==WIFI_SECURITY_OPEN}, {selectedWifiSecLevel= WIFI_SECURITY_OPEN})
                RadioButtonItem("WEP, WPA(2)-PSK", {selectedWifiSecLevel==WIFI_SECURITY_PERSONAL}, {selectedWifiSecLevel= WIFI_SECURITY_PERSONAL})
                RadioButtonItem("WPA-EAP", {selectedWifiSecLevel==WIFI_SECURITY_ENTERPRISE_EAP}, {selectedWifiSecLevel= WIFI_SECURITY_ENTERPRISE_EAP})
                RadioButtonItem("WPA3-192bit", {selectedWifiSecLevel==WIFI_SECURITY_ENTERPRISE_192}, {selectedWifiSecLevel= WIFI_SECURITY_ENTERPRISE_192})
                Button(
                    enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm),
                    onClick = {
                        myDpm.minimumRequiredWifiSecurityLevel=selectedWifiSecLevel
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("应用")
                }
            }
        }else{
            Text(text = "Wifi安全等级需API33", modifier = Modifier.padding(vertical = 3.dp))
        }
        
        if(VERSION.SDK_INT>=28&&isDeviceOwner(myDpm)){
            Column(modifier = sections()){
                val lockTaskPolicyList = mutableListOf(
                    LOCK_TASK_FEATURE_NONE,
                    LOCK_TASK_FEATURE_SYSTEM_INFO,
                    LOCK_TASK_FEATURE_NOTIFICATIONS,
                    LOCK_TASK_FEATURE_HOME,
                    LOCK_TASK_FEATURE_OVERVIEW,
                    LOCK_TASK_FEATURE_GLOBAL_ACTIONS,
                    LOCK_TASK_FEATURE_KEYGUARD
                )
                var sysInfo by remember{mutableStateOf(false)}
                var notifications by remember{mutableStateOf(false)}
                var home by remember{mutableStateOf(false)}
                var overview by remember{mutableStateOf(false)}
                var globalAction by remember{mutableStateOf(false)}
                var keyGuard by remember{mutableStateOf(false)}
                var blockAct by remember{mutableStateOf(false)}
                if(VERSION.SDK_INT>=30){lockTaskPolicyList.add(LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK)}
                var inited by remember{mutableStateOf(false)}
                var custom by remember{mutableStateOf(false)}
                val refreshFeature = {
                    var calculate = myDpm.getLockTaskFeatures(myComponent)
                    if(calculate!=0){
                        if(VERSION.SDK_INT>=30&&calculate-lockTaskPolicyList[7]>=0){blockAct=true;calculate-=lockTaskPolicyList[7]}
                        if(calculate-lockTaskPolicyList[6]>=0){keyGuard=true;calculate-=lockTaskPolicyList[6]}
                        if(calculate-lockTaskPolicyList[5]>=0){globalAction=true;calculate-=lockTaskPolicyList[5]}
                        if(calculate-lockTaskPolicyList[4]>=0){overview=true;calculate-=lockTaskPolicyList[4]}
                        if(calculate-lockTaskPolicyList[3]>=0){home=true;calculate-=lockTaskPolicyList[3]}
                        if(calculate-lockTaskPolicyList[2]>=0){notifications=true;calculate-=lockTaskPolicyList[2]}
                        if(calculate-lockTaskPolicyList[1]>=0){sysInfo=true;calculate-=lockTaskPolicyList[1]}
                    }else{
                        custom = false
                    }
                }
                Text(text = "锁定任务模式", style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                if(!inited){ refreshFeature();custom=myDpm.getLockTaskFeatures(myComponent)!=0;inited=true }
                Text(text = "在锁定任务模式下：", style = bodyTextStyle)
                RadioButtonItem("禁用全部",{!custom},{custom=false})
                RadioButtonItem("自定义",{custom},{custom=true})
                AnimatedVisibility(custom) {
                    Column {
                        CheckBoxItem("允许状态栏信息",{sysInfo},{sysInfo=!sysInfo})
                        CheckBoxItem("允许通知",{notifications},{notifications=!notifications})
                        CheckBoxItem("允许返回主屏幕",{home},{home=!home})
                        CheckBoxItem("允许打开后台应用概览",{overview},{overview=!overview})
                        CheckBoxItem("允许全局行为(比如长按电源键对话框)",{globalAction},{globalAction=!globalAction})
                        CheckBoxItem("允许锁屏(如果没有选择此项，即使有密码也不会锁屏)",{keyGuard},{keyGuard=!keyGuard})
                        if(VERSION.SDK_INT>=30){ CheckBoxItem("阻止启动未允许的应用",{blockAct},{blockAct=!blockAct}) }
                    }
                }
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        var result = lockTaskPolicyList[0]
                        if(custom){
                            if(blockAct&&VERSION.SDK_INT>=30){result+=lockTaskPolicyList[7]}
                            if(keyGuard){result+=lockTaskPolicyList[6]}
                            if(globalAction){result+=lockTaskPolicyList[5]}
                            if(overview){result+=lockTaskPolicyList[4]}
                            if(home){result+=lockTaskPolicyList[3]}
                            if(notifications){result+=lockTaskPolicyList[2]}
                            if(sysInfo){result+=lockTaskPolicyList[1]}
                        }
                        myDpm.setLockTaskFeatures(myComponent,result)
                        refreshFeature()
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("应用")
                }
                Spacer(Modifier.padding(vertical = 4.dp))
                val whitelist = myDpm.getLockTaskPackages(myComponent).toMutableList()
                var listText by remember{mutableStateOf("")}
                var inputPkg by remember{mutableStateOf("")}
                val refreshWhitelist = {
                    listText=""
                    var currentItem = whitelist.size
                    for(each in whitelist){
                        currentItem-=1
                        listText += each
                        if(currentItem>0){listText += "\n"}
                    }
                }
                refreshWhitelist()
                Text(text = "白名单应用", style = typography.titleLarge)
                if(listText!=""){ Text(listText) }else{ Text(("无")) }
                TextField(
                    value = inputPkg,
                    onValueChange = {inputPkg=it},
                    label = {Text("包名")},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                )
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        whitelist.add(inputPkg)
                        myDpm.setLockTaskPackages(myComponent,whitelist.toTypedArray())
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        inputPkg=""
                        refreshWhitelist()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("加入白名单")
                }
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        if(inputPkg in whitelist){
                            whitelist.remove(inputPkg)
                            myDpm.setLockTaskPackages(myComponent,whitelist.toTypedArray())
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "不存在", Toast.LENGTH_SHORT).show()
                        }
                        inputPkg=""
                        refreshWhitelist()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("从白名单中移除")
                }
            }
        }
        
        if(VERSION.SDK_INT>=29&&isDeviceOwner(myDpm)){
            Column(modifier = sections()){
                Text(text = "私人DNS", style = typography.titleLarge)
                val dnsStatus = mapOf(
                    PRIVATE_DNS_MODE_UNKNOWN to "未知",
                    PRIVATE_DNS_MODE_OFF to "关闭",
                    PRIVATE_DNS_MODE_OPPORTUNISTIC to "自动",
                    PRIVATE_DNS_MODE_PROVIDER_HOSTNAME to "指定主机名"
                )
                val operationResult = mapOf(
                    PRIVATE_DNS_SET_NO_ERROR to "成功",
                    PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING to "主机不支持DNS over TLS",
                    PRIVATE_DNS_SET_ERROR_FAILURE_SETTING to "失败"
                )
                var status by remember{mutableStateOf(dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)])}
                Text(text = "状态：$status")
                Button(
                    onClick = {
                        val result = myDpm.setGlobalPrivateDnsModeOpportunistic(myComponent)
                        Toast.makeText(myContext, operationResult[result], Toast.LENGTH_SHORT).show()
                        status = dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)]
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("设为自动")
                }
                Spacer(Modifier.padding(vertical = 3.dp))
                var inputHost by remember{mutableStateOf(myDpm.getGlobalPrivateDnsHost(myComponent) ?: "")}
                TextField(
                    value = inputHost,
                    onValueChange = {inputHost=it},
                    label = {Text("DNS主机名")},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val result: Int
                        try{
                            result = myDpm.setGlobalPrivateDnsModeSpecifiedHost(myComponent,inputHost)
                            Toast.makeText(myContext, operationResult[result], Toast.LENGTH_SHORT).show()
                        }catch(e:IllegalArgumentException){
                            Toast.makeText(myContext, "无效主机名", Toast.LENGTH_SHORT).show()
                        }catch(e:SecurityException){
                            Toast.makeText(myContext, "安全错误", Toast.LENGTH_SHORT).show()
                        }finally {
                            status = dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)]
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("设置DNS主机")
                }
            }
        }
        
        if(isDeviceOwner(myDpm)){
            SysUpdatePolicy()
        }
        
        Column(modifier = sections(if(isSystemInDarkTheme()){
            colorScheme.errorContainer}else{
            colorScheme.errorContainer.copy(alpha = 0.6F)})) {
            var flag by remember{ mutableIntStateOf(0) }
            var confirmed by remember{ mutableStateOf(false) }
            Text(text = "清除数据",style = typography.titleLarge,modifier = Modifier.padding(6.dp),color = colorScheme.onErrorContainer)
            RadioButtonItem("默认",{flag==0},{flag=0}, colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_EXTERNAL_STORAGE",{flag==0x0001},{flag=0x0001}, colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_RESET_PROTECTION_DATA",{flag==0x0002},{flag=0x0002}, colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_EUICC",{flag==0x0004},{flag=0x0004}, colorScheme.onErrorContainer)
            RadioButtonItem("WIPE_SILENTLY",{flag==0x0008},{flag=0x0008}, colorScheme.onErrorContainer)
            Text(text = "清空数据的不能是系统用户",color = colorScheme.onErrorContainer,
                style = if(!sharedPref.getBoolean("isWear",false)){typography.bodyLarge}else{typography.bodyMedium})
            Button(
                onClick = {confirmed=!confirmed},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(confirmed){
                        colorScheme.primary}else{
                        colorScheme.error},
                    contentColor = if(confirmed){
                        colorScheme.onPrimary}else{
                        colorScheme.onError}
                ),
                enabled = myDpm.isAdminActive(myComponent)
            ) {
                Text(text = if(confirmed){"取消"}else{"确定"})
            }
            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {myDpm.wipeData(flag)},
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorScheme.error,
                        contentColor = colorScheme.onError
                    ),
                    enabled = confirmed
                ) {
                    Text("WipeData")
                }
                if (VERSION.SDK_INT >= 34) {
                    Button(
                        onClick = {myDpm.wipeDevice(flag)},
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorScheme.error,
                            contentColor = colorScheme.onError
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
            modifier = if(isWear){Modifier.fillMaxWidth(0.65F)}else{Modifier.fillMaxWidth(0.8F)}
        ){
            if(!isWear){
            Icon(
                painter = painterResource(leadIcon),
                contentDescription = null,
                tint = colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(start = 5.dp, end = 9.dp)
            )}
            Column {
                Text(
                    text = stringResource(itemName),
                    style = if(!isWear){typography.titleLarge}else{typography.titleMedium},
                    color = colorScheme.onPrimaryContainer,
                    fontWeight = if(isWear){ FontWeight.SemiBold }else{ FontWeight.Medium }
                )
                if(itemDesc!=R.string.place_holder){ Text(stringResource(itemDesc)) }
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
