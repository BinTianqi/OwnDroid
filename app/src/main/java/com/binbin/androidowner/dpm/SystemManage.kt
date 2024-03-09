package com.binbin.androidowner.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.app.admin.SystemUpdateInfo
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build.VERSION
import android.os.UserManager
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
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
import com.binbin.androidowner.R
import com.binbin.androidowner.sections
import com.binbin.androidowner.ui.CheckBoxItem
import com.binbin.androidowner.ui.RadioButtonItem
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun SystemManage(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
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
                Text(text = stringResource(R.string.turn_off_usb_not_support),modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
        
        Keyguard()
        
        if(VERSION.SDK_INT>=24){
            Column{
                Button(
                    onClick = {
                        val result = myDpm.requestBugreport(myComponent)
                        Toast.makeText(myContext, if(result){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isDeviceOwner(myDpm)
                ) {
                    Text(stringResource(R.string.request_bug_report))
                }
                Button(
                    onClick = {myDpm.reboot(myComponent)},
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.reboot))
                }
            }
        }
        
        if(VERSION.SDK_INT>=28){
            EditTime()
        }
        
        if(VERSION.SDK_INT>=23&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            PermissionPolicy()
        }
        
        if(VERSION.SDK_INT>=34&&isDeviceOwner(myDpm)){
            MTEPolicy()
        }
        
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            NearbyStreamingPolicy()
        }
        
        if(VERSION.SDK_INT>=28&&isDeviceOwner(myDpm)){
            LockTaskFeatures()
        }
        
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            CaCert()
        }
        
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            SecurityLogs()
        }
        
        if(isDeviceOwner(myDpm)){
            SysUpdatePolicy()
        }
        
        WipeData()
        
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

@Composable
fun Keyguard(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column{
        Text(text = stringResource(R.string.keyguard), style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
        if(VERSION.SDK_INT>=23){
            Text(text = stringResource(R.string.require_no_password_to_disable))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        Toast.makeText(myContext,
                            myContext.getString(if(myDpm.setKeyguardDisabled(myComponent,true)){R.string.success}else{R.string.fail}), Toast.LENGTH_SHORT).show()
                    },
                    enabled = isDeviceOwner(myDpm)|| (VERSION.SDK_INT>=28&&isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.disable))
                }
                Button(
                    onClick = {
                        Toast.makeText(myContext,
                            myContext.getString(if(myDpm.setKeyguardDisabled(myComponent,false)){R.string.success}else{R.string.fail}), Toast.LENGTH_SHORT).show()
                    },
                    enabled = isDeviceOwner(myDpm)|| (VERSION.SDK_INT>=28&&isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text(stringResource(R.string.enable))
                }
            }
        }
        var flag by remember{mutableIntStateOf(0)}
        Button(
            onClick = {myDpm.lockNow()},
            enabled = myDpm.isAdminActive(myComponent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.lock_now))
        }
        if(VERSION.SDK_INT>=26){ CheckBoxItem(stringResource(R.string.require_enter_password_again),{flag==FLAG_EVICT_CREDENTIAL_ENCRYPTION_KEY},{flag = if(flag==0){1}else{0} }) }
    }
}

@SuppressLint("NewApi")
@Composable
fun EditTime(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
        Text(text = stringResource(R.string.edit_time), style = typography.titleLarge)
        var inputTime by remember{mutableStateOf("")}
        Text(text = stringResource(R.string.from_epoch_to_target_time))
        OutlinedTextField(
            value = inputTime,
            label = { Text(stringResource(R.string.time_unit_ms))},
            onValueChange = {inputTime = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            enabled = isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile),
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {myDpm.setTime(myComponent,inputTime.toLong())},
                modifier = Modifier.fillMaxWidth(0.35F),
                enabled = inputTime!=""&&(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))
            ) {
                Text("应用")
            }
            Button(
                onClick = {inputTime = System.currentTimeMillis().toString()},
                modifier = Modifier.fillMaxWidth(0.98F)
            ) {
                Text(stringResource(R.string.get_current_time))
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun PermissionPolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column{
        var selectedPolicy by remember{mutableIntStateOf(myDpm.getPermissionPolicy(myComponent))}
        Text(text = stringResource(R.string.permission_policy), style = typography.titleLarge)
        RadioButtonItem(stringResource(R.string.default_stringres), {selectedPolicy==PERMISSION_POLICY_PROMPT}, {selectedPolicy= PERMISSION_POLICY_PROMPT})
        RadioButtonItem(stringResource(R.string.auto_grant), {selectedPolicy==PERMISSION_POLICY_AUTO_GRANT}, {selectedPolicy= PERMISSION_POLICY_AUTO_GRANT})
        RadioButtonItem(stringResource(R.string.auto_deny), {selectedPolicy==PERMISSION_POLICY_AUTO_DENY}, {selectedPolicy= PERMISSION_POLICY_AUTO_DENY})
        Button(
            onClick = {
                myDpm.setPermissionPolicy(myComponent,selectedPolicy)
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun MTEPolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column{
        Text(text = stringResource(R.string.mte_policy), style = typography.titleLarge)
        Text(stringResource(R.string.mte_policy_desc))
        var selectedMtePolicy by remember{mutableIntStateOf(myDpm.mtePolicy)}
        RadioButtonItem(stringResource(R.string.decide_by_user), {selectedMtePolicy==MTE_NOT_CONTROLLED_BY_POLICY}, {selectedMtePolicy= MTE_NOT_CONTROLLED_BY_POLICY})
        RadioButtonItem(stringResource(R.string.enabled), {selectedMtePolicy==MTE_ENABLED}, {selectedMtePolicy=MTE_ENABLED})
        RadioButtonItem(stringResource(R.string.disabled), {selectedMtePolicy==MTE_DISABLED}, {selectedMtePolicy=MTE_DISABLED})
        Button(
            onClick = {
                try {
                    myDpm.mtePolicy = selectedMtePolicy
                    Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                }catch(e:java.lang.UnsupportedOperationException){
                    Toast.makeText(myContext, myContext.getString(R.string.unsupported), Toast.LENGTH_SHORT).show()
                }
                selectedMtePolicy = myDpm.mtePolicy
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun NearbyStreamingPolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column{
        var appPolicy by remember{mutableIntStateOf(myDpm.nearbyAppStreamingPolicy)}
        Text(text = stringResource(R.string.nearby_app_streaming), style = typography.titleLarge)
        RadioButtonItem(stringResource(R.string.decide_by_user),{appPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY},{appPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY})
        RadioButtonItem(stringResource(R.string.enabled),{appPolicy == NEARBY_STREAMING_ENABLED},{appPolicy = NEARBY_STREAMING_ENABLED})
        RadioButtonItem(stringResource(R.string.disabled),{appPolicy == NEARBY_STREAMING_DISABLED},{appPolicy = NEARBY_STREAMING_DISABLED})
        RadioButtonItem(stringResource(R.string.enable_if_secure_enough),{appPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY},{appPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY})
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
        Text(text = stringResource(R.string.nearby_notifi_streaming), style = typography.titleLarge)
        RadioButtonItem(stringResource(R.string.decide_by_user),{notificationPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY},{notificationPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY})
        RadioButtonItem(stringResource(R.string.enabled),{notificationPolicy == NEARBY_STREAMING_ENABLED},{notificationPolicy = NEARBY_STREAMING_ENABLED})
        RadioButtonItem(stringResource(R.string.disabled),{notificationPolicy == NEARBY_STREAMING_DISABLED},{notificationPolicy = NEARBY_STREAMING_DISABLED})
        RadioButtonItem(stringResource(R.string.enable_if_secure_enough),{notificationPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY},{notificationPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY})
        Button(
            onClick = {
                myDpm.nearbyNotificationStreamingPolicy = notificationPolicy
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun LockTaskFeatures(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
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
        Text(text = stringResource(R.string.lock_task_feature), style = typography.titleLarge)
        if(!inited){ refreshFeature();custom=myDpm.getLockTaskFeatures(myComponent)!=0;inited=true }
        RadioButtonItem(stringResource(R.string.disable_all),{!custom},{custom=false})
        RadioButtonItem(stringResource(R.string.custom),{custom},{custom=true})
        AnimatedVisibility(custom) {
            Column {
                CheckBoxItem(stringResource(R.string.ltf_sys_info),{sysInfo},{sysInfo=!sysInfo})
                CheckBoxItem(stringResource(R.string.ltf_notifications),{notifications},{notifications=!notifications})
                CheckBoxItem(stringResource(R.string.ltf_home),{home},{home=!home})
                CheckBoxItem(stringResource(R.string.ltf_overview),{overview},{overview=!overview})
                CheckBoxItem(stringResource(R.string.ltf_global_actions),{globalAction},{globalAction=!globalAction})
                CheckBoxItem(stringResource(R.string.ltf_keyguard),{keyGuard},{keyGuard=!keyGuard})
                if(VERSION.SDK_INT>=30){ CheckBoxItem(stringResource(R.string.ltf_block_activity_start_in_task),{blockAct},{blockAct=!blockAct}) }
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
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            }
        ) {
            Text(stringResource(R.string.apply))
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
        Text(text = stringResource(R.string.whitelist_app), style = typography.titleLarge)
        if(listText!=""){
            SelectionContainer {
                Text(text = listText)
            }
        }else{
            Text(text = stringResource(R.string.none))
        }
        OutlinedTextField(
            value = inputPkg,
            onValueChange = {inputPkg=it},
            label = {Text(stringResource(R.string.package_name))},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 3.dp)
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    whitelist.add(inputPkg)
                    myDpm.setLockTaskPackages(myComponent,whitelist.toTypedArray())
                    Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                    refreshWhitelist()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    if(inputPkg in whitelist){
                        whitelist.remove(inputPkg)
                        myDpm.setLockTaskPackages(myComponent,whitelist.toTypedArray())
                        Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
                    }else{
                        Toast.makeText(myContext, myContext.getString(R.string.not_exist), Toast.LENGTH_SHORT).show()
                    }
                    refreshWhitelist()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
    }
}

@Composable
fun CaCert(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    var exist by remember{mutableStateOf(false)}
    var isEmpty by remember{mutableStateOf(false)}
    val refresh = {
        isEmpty = caCert.isEmpty()
        exist = if(!isEmpty){ myDpm.hasCaCertInstalled(myComponent, caCert) }else{ false }
    }
    LaunchedEffect(exist){ while(true){ delay(600); refresh() } }
    Column{
        Text(text = stringResource(R.string.ca_cert), style = typography.titleLarge)
        if(isEmpty){ Text(text = stringResource(R.string.please_select_ca_cert)) }else{ Text(text = stringResource(R.string.cacert_installed, exist)) }
        Button(
            onClick = {
                val caCertIntent = Intent(Intent.ACTION_GET_CONTENT)
                caCertIntent.setType("*/*")
                caCertIntent.addCategory(Intent.CATEGORY_OPENABLE)
                getCaCert.launch(caCertIntent)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.select_ca_cert))
        }
        AnimatedVisibility(!isEmpty) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                Button(
                    onClick = {
                        val result = myDpm.installCaCert(myComponent, caCert)
                        Toast.makeText(myContext, myContext.getString(if(result){R.string.success}else{R.string.fail}), Toast.LENGTH_SHORT).show()
                        refresh()
                    },
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text(stringResource(R.string.install))
                }
                Button(
                    onClick = {
                        if(exist){
                            myDpm.uninstallCaCert(myComponent, caCert)
                            exist = myDpm.hasCaCertInstalled(myComponent, caCert)
                            Toast.makeText(myContext, myContext.getString(if(exist){R.string.fail}else{R.string.success}), Toast.LENGTH_SHORT).show()
                        }else{ Toast.makeText(myContext, myContext.getString(R.string.not_exist), Toast.LENGTH_SHORT).show() }
                    },
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text(stringResource(R.string.uninstall))
                }
            }
        }
        Button(
            onClick = {
                myDpm.uninstallAllUserCaCerts(myComponent)
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.uninstall_all_user_ca_cert))
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun SecurityLogs(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column{
        Text(text = stringResource(R.string.retrieve_security_logs), style = typography.titleLarge)
        Text(text = stringResource(R.string.developing))
        Row(modifier=Modifier.fillMaxWidth().padding(horizontal=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            var checked by remember{mutableStateOf(myDpm.isSecurityLoggingEnabled(myComponent))}
            Text(text = stringResource(R.string.enabled), style = typography.titleLarge)
            Switch(
                checked = checked,
                onCheckedChange = {myDpm.setSecurityLoggingEnabled(myComponent,!checked);checked=myDpm.isSecurityLoggingEnabled(myComponent)}
            )
        }
        Button(
            onClick = {
                val log = myDpm.retrieveSecurityLogs(myComponent)
                if(log!=null){
                    for(i in log){ Log.d("SecureLog",i.toString()) }
                    Toast.makeText(myContext,myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("SecureLog",myContext.getString(R.string.none))
                    Toast.makeText(myContext, myContext.getString(R.string.no_logs),Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.security_logs))
        }
        Button(
            onClick = {
                val log = myDpm.retrievePreRebootSecurityLogs(myComponent)
                if(log!=null){
                    for(i in log){ Log.d("SecureLog",i.toString()) }
                    Toast.makeText(myContext,myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("SecureLog",myContext.getString(R.string.none))
                    Toast.makeText(myContext,myContext.getString(R.string.no_logs),Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.pre_reboot_security_logs))
        }
    }
}

@Composable
fun WipeData(){
    val myContext = LocalContext.current
    val userManager = myContext.getSystemService(Context.USER_SERVICE) as UserManager
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
        var flag by remember{ mutableIntStateOf(0) }
        var confirmed by remember{ mutableStateOf(false) }
        var externalStorage by remember{mutableStateOf(false)}
        var protectionData by remember{mutableStateOf(false)}
        var euicc by remember{mutableStateOf(false)}
        var silent by remember{mutableStateOf(false)}
        var reason by remember{mutableStateOf("")}
        Text(text = stringResource(R.string.wipe_data),style = typography.titleLarge,modifier = Modifier.padding(6.dp),color = colorScheme.onErrorContainer)
        CheckBoxItem(stringResource(R.string.wipe_external_storage),{externalStorage},{externalStorage=!externalStorage;confirmed=false}, colorScheme.onErrorContainer)
        if(VERSION.SDK_INT>=22&&isDeviceOwner(myDpm)){
            CheckBoxItem(stringResource(R.string.wipe_reset_protection_data),{protectionData},{protectionData=!protectionData;confirmed=false}, colorScheme.onErrorContainer)
        }
        if(VERSION.SDK_INT>=28){ CheckBoxItem(stringResource(R.string.wipe_euicc),{euicc},{euicc=!euicc;confirmed=false}, colorScheme.onErrorContainer) }
        if(VERSION.SDK_INT>=29){ CheckBoxItem(stringResource(R.string.wipe_silently),{silent},{silent=!silent;confirmed=false}, colorScheme.onErrorContainer) }
        AnimatedVisibility(!silent&&VERSION.SDK_INT>=28) {
            OutlinedTextField(
                value = reason, onValueChange = {reason=it},
                label = {Text(stringResource(R.string.reason))},
                enabled = !confirmed,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 3.dp)
            )
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                flag = 0
                if(externalStorage){flag += WIPE_EXTERNAL_STORAGE}
                if(protectionData&&VERSION.SDK_INT>=22){flag += WIPE_RESET_PROTECTION_DATA}
                if(euicc&&VERSION.SDK_INT>=28){flag += WIPE_EUICC}
                if(reason==""){silent = true}
                if(silent&&VERSION.SDK_INT>=29){flag += WIPE_SILENTLY}
                confirmed=!confirmed
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if(confirmed){ colorScheme.primary }else{ colorScheme.error },
                contentColor = if(confirmed){ colorScheme.onPrimary }else{ colorScheme.onError }
            ),
            enabled = myDpm.isAdminActive(myComponent),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(if(confirmed){ R.string.cancel }else{ R.string.confirm }))
        }
        Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    if(VERSION.SDK_INT>=28){myDpm.wipeData(flag,reason)}
                    else{myDpm.wipeData(flag)}
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                enabled = confirmed&&(VERSION.SDK_INT<34||(VERSION.SDK_INT>=34&&!userManager.isSystemUser)),
                modifier = Modifier.fillMaxWidth(if(VERSION.SDK_INT >= 34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){0.49F}else{1F})
            ) {
                Text("WipeData")
            }
            if (VERSION.SDK_INT >= 34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))) {
                Button(
                    onClick = {myDpm.wipeDevice(flag)},
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                    enabled = confirmed,
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text("WipeDevice")
                }
            }
        }
        if(VERSION.SDK_INT>=24&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            Text(text = stringResource(R.string.will_delete_work_profile))
        }
        if(VERSION.SDK_INT>=34&&Binder.getCallingUid()/100000==0){
            Text(text = stringResource(R.string.api34_or_above_wipedata_cannot_in_system_user))
        }
    }
}

@Composable
fun SysUpdatePolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    val sharedPref = myContext.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium}else{typography.bodyLarge}
    Column {
        if(VERSION.SDK_INT>=26&&isDeviceOwner(myDpm)){
            val sysUpdateInfo = myDpm.getPendingSystemUpdate(myComponent)
            Column(modifier = sections()) {
                if(sysUpdateInfo!=null){
                    Text(text = "Update first available: ${Date(sysUpdateInfo.receivedTime)}", style = bodyTextStyle)
                    Text(text = "Hash code: ${sysUpdateInfo.hashCode()}", style = bodyTextStyle)
                    val securityStateDesc = when(sysUpdateInfo.securityPatchState){
                        SystemUpdateInfo.SECURITY_PATCH_STATE_UNKNOWN-> stringResource(R.string.unknown)
                        SystemUpdateInfo.SECURITY_PATCH_STATE_TRUE->"true"
                        else->"false"
                    }
                    Text(text = stringResource(R.string.is_security_patch, securityStateDesc), style = bodyTextStyle)
                }else{
                    Text(text = stringResource(R.string.no_system_update), style = bodyTextStyle)
                }
            }
        }
        if(VERSION.SDK_INT>=23){
            Column(modifier = sections()) {
                var selectedPolicy by remember{ mutableStateOf(myDpm.systemUpdatePolicy?.policyType) }
                Text(text = stringResource(R.string.system_update_policy), style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                RadioButtonItem(stringResource(R.string.system_update_policy_automatic),{selectedPolicy==SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC},{selectedPolicy= SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC})
                RadioButtonItem(stringResource(R.string.system_update_policy_install_windowed),{selectedPolicy==SystemUpdatePolicy.TYPE_INSTALL_WINDOWED},{selectedPolicy= SystemUpdatePolicy.TYPE_INSTALL_WINDOWED})
                RadioButtonItem(stringResource(R.string.system_update_policy_postpone),{selectedPolicy==SystemUpdatePolicy.TYPE_POSTPONE},{selectedPolicy= SystemUpdatePolicy.TYPE_POSTPONE})
                RadioButtonItem(stringResource(R.string.none),{selectedPolicy == null},{selectedPolicy=null})
                var windowedPolicyStart by remember{ mutableStateOf("") }
                var windowedPolicyEnd by remember{ mutableStateOf("") }
                if(selectedPolicy==2){
                    Spacer(Modifier.padding(vertical = 3.dp))
                    Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center) {
                        OutlinedTextField(
                            value = windowedPolicyStart,
                            label = { Text(stringResource(R.string.start_time))},
                            onValueChange = {windowedPolicyStart=it},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.focusable().fillMaxWidth(0.5F)
                        )
                        Spacer(Modifier.padding(horizontal = 3.dp))
                        OutlinedTextField(
                            value = windowedPolicyEnd,
                            onValueChange = {windowedPolicyEnd=it},
                            label = {Text(stringResource(R.string.end_time))},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.focusable().fillMaxWidth()
                        )
                    }
                    Spacer(Modifier.padding(vertical = 3.dp))
                    Text(text = stringResource(R.string.minutes_in_one_day), style = bodyTextStyle)
                }
                val policy =
                    when(selectedPolicy){
                        SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC-> SystemUpdatePolicy.createAutomaticInstallPolicy()
                        SystemUpdatePolicy.TYPE_INSTALL_WINDOWED-> SystemUpdatePolicy.createWindowedInstallPolicy(windowedPolicyStart.toInt(),windowedPolicyEnd.toInt())
                        SystemUpdatePolicy.TYPE_POSTPONE-> SystemUpdatePolicy.createPostponeInstallPolicy()
                        else->null
                    }
                Button(
                    onClick = {myDpm.setSystemUpdatePolicy(myComponent,policy);Toast.makeText(myContext, "成功！", Toast.LENGTH_SHORT).show()},
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.apply))
                }
            }}
        /*if(VERSION.SDK_INT>=29){
            Column(modifier = sections()){
                var resultUri by remember{mutableStateOf(otaUri)}
                Text(text = "安装系统更新", style = typography.titleLarge)
                Button(
                    onClick = {
                        val getUri = Intent(Intent.ACTION_GET_CONTENT)
                        getUri.setType("application/zip")
                        getUri.addCategory(Intent.CATEGORY_OPENABLE)
                        getOtaPackage.launch(getUri)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
                ) {
                    Text("选择OTA包")
                }
                Button(
                    onClick = {resultUri = otaUri},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
                ) {
                    Text("查看OTA包详情")
                }
                Text("URI: $resultUri")
                if(installOta){
                    Button(
                        onClick = {
                            val sysUpdateExecutor = Executors.newCachedThreadPool()
                            val sysUpdateCallback:InstallSystemUpdateCallback = InstallSystemUpdateCallback
                            myDpm.installSystemUpdate(myComponent,resultUri,sysUpdateExecutor,sysUpdateCallback)
                        },
                        enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
                    ){
                        Text("安装")
                    }
                }
            }
        }*/
    }
}
