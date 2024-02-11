package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.util.Log
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            DeviceCtrlItem(R.string.disable_cam,R.string.place_holder, R.drawable.photo_camera_fill0,
                {myDpm.getCameraDisabled(null)},{b -> myDpm.setCameraDisabled(myComponent,b)}
            )
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            DeviceCtrlItem(R.string.disable_scrcap,R.string.aosp_scrrec_also_work,R.drawable.screenshot_fill0,
                {myDpm.getScreenCaptureDisabled(null)},{b -> myDpm.setScreenCaptureDisabled(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser))){
            DeviceCtrlItem(R.string.disable_status_bar,R.string.place_holder,R.drawable.notifications_fill0,
                {myDpm.isStatusBarDisabled},{b -> myDpm.setStatusBarDisabled(myComponent,b) }
            )
        }
        if(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile)){
            if(VERSION.SDK_INT>=30){
                DeviceCtrlItem(R.string.auto_time,R.string.place_holder,R.drawable.schedule_fill0,
                    {myDpm.getAutoTimeEnabled(myComponent)},{b -> myDpm.setAutoTimeEnabled(myComponent,b) }
                )
                DeviceCtrlItem(R.string.auto_timezone,R.string.place_holder,R.drawable.globe_fill0,
                    {myDpm.getAutoTimeZoneEnabled(myComponent)},{b -> myDpm.setAutoTimeZoneEnabled(myComponent,b) }
                )
            }else{
                DeviceCtrlItem(R.string.auto_time,R.string.place_holder,R.drawable.schedule_fill0,{myDpm.autoTimeRequired},{b -> myDpm.setAutoTimeRequired(myComponent,b)})
            }
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
        if(VERSION.SDK_INT>=30&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.common_criteria_mode,R.string.common_criteria_mode_desc,R.drawable.security_fill0,
                {myDpm.isCommonCriteriaModeEnabled(myComponent)},{b ->  myDpm.setCommonCriteriaModeEnabled(myComponent,b)}
            )
        }
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            if(myDpm.canUsbDataSignalingBeDisabled()){
                DeviceCtrlItem(R.string.usb_signal,R.string.place_holder,R.drawable.usb_fill0,
                    {myDpm.isUsbDataSignalingEnabled},{b -> myDpm.isUsbDataSignalingEnabled = b }
                )
            }else{
                Text(text = "你的设备不支持关闭USB信号",modifier = Modifier.fillMaxWidth(), style = bodyTextStyle, textAlign = TextAlign.Center)
            }
        }
        if(VERSION.SDK_INT>=28){
            Column(modifier = sections()) {
                Text(text = "锁屏方式", style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
                Text(text = "禁用需要无密码",style=bodyTextStyle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { Toast.makeText(myContext, if(myDpm.setKeyguardDisabled(myComponent,true)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show() },
                        enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("禁用")
                    }
                    Button(
                        onClick = { Toast.makeText(myContext, if(myDpm.setKeyguardDisabled(myComponent,false)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show() },
                        enabled = isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("启用")
                    }
                }
            }
        }
        
        Column(modifier = sections()){
            Text(text = "锁屏", style = typography.titleLarge)
            var flag by remember{mutableIntStateOf(0)}
            if(VERSION.SDK_INT>=26){ CheckBoxItem("需要重新输入密码",{flag==FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY},{flag = if(flag==0){1}else{0} }) }
            Button(
                onClick = {myDpm.lockNow()},
                enabled = myDpm.isAdminActive(myComponent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("立即锁屏")
            }
        }
        
        if(VERSION.SDK_INT>=24){
            Column(modifier = sections()){
                Button(
                    onClick = {
                        val result = myDpm.requestBugreport(myComponent)
                        Toast.makeText(myContext, if(result){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isDeviceOwner(myDpm)
                ) {
                    Text("请求错误报告")
                }
                Button(
                    onClick = {myDpm.reboot(myComponent)},
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("重启")
                }
            }
        }
        
        if(VERSION.SDK_INT>=28){
            Column(modifier = sections()){
                Text(text = "修改时间", style = typography.titleLarge)
                var inputTime by remember{mutableStateOf("")}
                Text(text = "从Epoch(1970/1/1 00:00:00 UTC)到你想设置的时间(毫秒)", style = bodyTextStyle)
                TextField(
                    value = inputTime,
                    label = { Text("时间(ms)")},
                    onValueChange = {inputTime = it},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    enabled = isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile),
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
                        enabled = inputTime!=""&&(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))
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
        
        if(VERSION.SDK_INT>=23&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var selectedPolicy by remember{mutableIntStateOf(myDpm.getPermissionPolicy(myComponent))}
                Text(text = "权限策略", style = typography.titleLarge)
                RadioButtonItem("默认", {selectedPolicy==PERMISSION_POLICY_PROMPT}, {selectedPolicy= PERMISSION_POLICY_PROMPT})
                RadioButtonItem("自动允许", {selectedPolicy==PERMISSION_POLICY_AUTO_GRANT}, {selectedPolicy= PERMISSION_POLICY_AUTO_GRANT})
                RadioButtonItem("自动拒绝", {selectedPolicy==PERMISSION_POLICY_AUTO_DENY}, {selectedPolicy= PERMISSION_POLICY_AUTO_DENY})
                Button(
                    onClick = {
                        myDpm.setPermissionPolicy(myComponent,selectedPolicy)
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
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
        
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var appPolicy by remember{mutableIntStateOf(myDpm.nearbyAppStreamingPolicy)}
                Text(text = "附近App共享", style = typography.titleLarge)
                RadioButtonItem("由用户决定",{appPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY},{appPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY})
                RadioButtonItem("启用",{appPolicy == NEARBY_STREAMING_ENABLED},{appPolicy = NEARBY_STREAMING_ENABLED})
                RadioButtonItem("禁用",{appPolicy == NEARBY_STREAMING_DISABLED},{appPolicy = NEARBY_STREAMING_DISABLED})
                RadioButtonItem("在足够安全时启用",{appPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY},{appPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY})
                Button(
                    onClick = {
                        myDpm.nearbyAppStreamingPolicy = appPolicy
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
                }
                Spacer(Modifier.padding(vertical = 3.dp))
                var notificationPolicy by remember{mutableIntStateOf(myDpm.nearbyNotificationStreamingPolicy)}
                Text(text = "附近通知共享", style = typography.titleLarge)
                RadioButtonItem("由用户决定",{notificationPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY},{notificationPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY})
                RadioButtonItem("启用",{notificationPolicy == NEARBY_STREAMING_ENABLED},{notificationPolicy = NEARBY_STREAMING_ENABLED})
                RadioButtonItem("禁用",{notificationPolicy == NEARBY_STREAMING_DISABLED},{notificationPolicy = NEARBY_STREAMING_DISABLED})
                RadioButtonItem("在足够安全时启用",{notificationPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY},{notificationPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY})
                Button(
                    onClick = {
                        myDpm.nearbyNotificationStreamingPolicy = notificationPolicy
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
                }
            }
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
                    inputPkg=""
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
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            whitelist.add(inputPkg)
                            myDpm.setLockTaskPackages(myComponent,whitelist.toTypedArray())
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                            refreshWhitelist()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("添加")
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
                            refreshWhitelist()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("移除")
                    }
                }
            }
        }
        
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            var exist by remember{mutableStateOf(false)}
            var isEmpty by remember{mutableStateOf(false)}
            val refresh = {
                isEmpty = caCert.isEmpty()
                exist = if(!isEmpty){ myDpm.hasCaCertInstalled(myComponent, caCert) }else{ false }
            }
            LaunchedEffect(exist){ launch{isCaCertSelected(600){refresh()}} }
            Column(modifier = sections()){
                Text(text = "Ca证书", style = typography.titleLarge)
                if(isEmpty){ Text(text = "请选择Ca证书(.0)") }else{ Text(text = "证书已安装：$exist") }
                Button(
                    onClick = {
                        val caCertIntent = Intent(Intent.ACTION_GET_CONTENT)
                        caCertIntent.setType("*/*")
                        caCertIntent.addCategory(Intent.CATEGORY_OPENABLE)
                        getCaCert.launch(caCertIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("选择证书...")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            val result = myDpm.installCaCert(myComponent, caCert)
                            Toast.makeText(myContext, if(result){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("安装")
                    }
                    Button(
                        onClick = {
                            if(exist){
                                myDpm.uninstallCaCert(myComponent, caCert)
                                exist = myDpm.hasCaCertInstalled(myComponent, caCert)
                                Toast.makeText(myContext, if(exist){"失败"}else{"成功"}, Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "不存在", Toast.LENGTH_SHORT).show() }
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("卸载")
                    }
                }
                Button(
                    onClick = {
                        myDpm.uninstallAllUserCaCerts(myComponent)
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("清除用户Ca证书")
                }
            }
        }
        
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                Text(text = "收集安全日志", style = typography.titleLarge)
                Text(text = "功能开发中", style = bodyTextStyle)
                Row(modifier=Modifier.fillMaxWidth().padding(horizontal=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                    var checked by remember{mutableStateOf(myDpm.isSecurityLoggingEnabled(myComponent))}
                    Text(text = "启用", style = typography.titleLarge)
                    Switch(
                        checked = checked,
                        onCheckedChange = {myDpm.setSecurityLoggingEnabled(myComponent,!checked);checked=myDpm.isSecurityLoggingEnabled(myComponent)}
                    )
                }
                Button(
                    onClick = {
                        val log = myDpm.retrieveSecurityLogs(myComponent)
                        if(log!=null){
                            for(i in log){ Log.d("NetLog",i.toString()) }
                            Toast.makeText(myContext,"已输出至Log",Toast.LENGTH_SHORT).show()
                        }else{
                            Log.d("NetLog","无")
                            Toast.makeText(myContext,"无",Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("收集")
                }
            }
        }
        
        if(isDeviceOwner(myDpm)){
            SysUpdatePolicy()
        }
        
        Column(modifier = sections(if(isSystemInDarkTheme()){ colorScheme.errorContainer }else{ colorScheme.errorContainer.copy(alpha = 0.6F) })) {
            var flag by remember{ mutableIntStateOf(0) }
            var confirmed by remember{ mutableStateOf(false) }
            Text(text = "清除数据",style = typography.titleLarge,modifier = Modifier.padding(6.dp),color = colorScheme.onErrorContainer)
            RadioButtonItem("默认",{flag==0},{flag=0}, colorScheme.onErrorContainer)
            RadioButtonItem("清除外部存储",{flag==WIPE_EXTERNAL_STORAGE},{flag=WIPE_EXTERNAL_STORAGE}, colorScheme.onErrorContainer)
            if(VERSION.SDK_INT>=22&&isDeviceOwner(myDpm)){
                RadioButtonItem("清除受保护的数据",{flag==WIPE_RESET_PROTECTION_DATA},{flag=WIPE_RESET_PROTECTION_DATA}, colorScheme.onErrorContainer)
            }
            if(VERSION.SDK_INT>=28){ RadioButtonItem("清除eUICC",{flag==WIPE_EUICC},{flag=WIPE_EUICC}, colorScheme.onErrorContainer) }
            if(VERSION.SDK_INT>=29){ RadioButtonItem("WIPE_SILENTLY",{flag==WIPE_SILENTLY},{flag=WIPE_SILENTLY}, colorScheme.onErrorContainer) }
            Text(text = "清空数据的不能是系统用户",color = colorScheme.onErrorContainer,
                style = if(!sharedPref.getBoolean("isWear",false)){typography.bodyLarge}else{typography.bodyMedium})
            Button(
                onClick = {confirmed=!confirmed},
                colors = ButtonDefaults.buttonColors(
                    containerColor = if(confirmed){ colorScheme.primary }else{ colorScheme.error },
                    contentColor = if(confirmed){ colorScheme.onPrimary }else{ colorScheme.onError }
                ),
                enabled = myDpm.isAdminActive(myComponent),
                modifier = Modifier.fillMaxWidth()
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
                    enabled = confirmed,
                    modifier = Modifier.fillMaxWidth(if(VERSION.SDK_INT>=34){0.49F}else{1F})
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
                        enabled = confirmed,
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("WipeDevice")
                    }
                }
            }
            if(VERSION.SDK_INT>=24&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
                Text("将会删除工作资料")
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
fun DeviceCtrlItem(
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

suspend fun isCaCertSelected(delay:Long,operation:()->Unit){
    while(true){
        delay(delay)
        operation()
    }
}
