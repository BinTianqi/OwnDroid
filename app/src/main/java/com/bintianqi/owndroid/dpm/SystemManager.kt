package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.app.admin.SystemUpdateInfo
import android.app.admin.SystemUpdatePolicy
import android.app.admin.SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC
import android.app.admin.SystemUpdatePolicy.TYPE_INSTALL_WINDOWED
import android.app.admin.SystemUpdatePolicy.TYPE_POSTPONE
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
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Receiver
import com.bintianqi.owndroid.toText
import com.bintianqi.owndroid.ui.*
import com.bintianqi.owndroid.ui.theme.bgColor
import kotlinx.coroutines.delay
import java.util.Date

@Composable
fun SystemManage(navCtrl:NavHostController){
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    /*val titleMap = mapOf(
        "Switches" to R.string.options,
        "Keyguard" to R.string.keyguard,
        "BugReport" to R.string.request_bug_report,
        "Reboot" to R.string.reboot,
        "EditTime" to R.string.edit_time,
        "PermissionPolicy" to R.string.permission_policy,
        "MTEPolicy" to R.string.mte_policy,
        "NearbyStreamingPolicy" to R.string.nearby_streaming_policy,
        "LockTaskFeatures" to R.string.lock_task_feature,
        "CaCert" to R.string.ca_cert,
        "SecurityLogs" to R.string.security_logs,
        "SystemUpdatePolicy" to R.string.system_update_policy,
        "WipeData" to R.string.wipe_data
    )*/
    Scaffold(
        topBar = {
            /*TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.device_ctrl))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )*/
            TopBar(backStackEntry,navCtrl,localNavCtrl){
                if(backStackEntry?.destination?.route=="Home"&&scrollState.maxValue>80){
                    Text(
                        text = stringResource(R.string.system_manage),
                        modifier = Modifier.alpha((maxOf(scrollState.value-30,0)).toFloat()/80)
                    )
                }
            }
        }
    ){
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition,
            modifier = Modifier.background(bgColor).padding(top = it.calculateTopPadding())
        ){
            composable(route = "Home"){Home(localNavCtrl,scrollState)}
            composable(route = "Switches"){Switches()}
            composable(route = "Keyguard"){Keyguard()}
            composable(route = "BugReport"){BugReport()}
            composable(route = "Reboot"){Reboot()}
            composable(route = "EditTime"){EditTime()}
            composable(route = "PermissionPolicy"){PermissionPolicy()}
            composable(route = "MTEPolicy"){MTEPolicy()}
            composable(route = "NearbyStreamingPolicy"){NearbyStreamingPolicy()}
            composable(route = "LockTaskFeatures"){LockTaskFeatures()}
            composable(route = "CaCert"){CaCert()}
            composable(route = "SecurityLogs"){SecurityLogs()}
            composable(route = "SystemUpdatePolicy"){SysUpdatePolicy()}
            composable(route = "WipeData"){WipeData()}
        }
    }
}

@Composable
private fun Home(navCtrl: NavHostController,scrollState: ScrollState){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)){
        Text(text = stringResource(R.string.system_manage), style = typography.headlineLarge, modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 15.dp))
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            SubPageItem(R.string.options,"",R.drawable.tune_fill0){navCtrl.navigate("Switches")}
        }
        SubPageItem(R.string.keyguard,"",R.drawable.screen_lock_portrait_fill0){navCtrl.navigate("Keyguard")}
        if(VERSION.SDK_INT>=24){
            SubPageItem(R.string.request_bug_report,"",R.drawable.bug_report_fill0){navCtrl.navigate("BugReport")}
            SubPageItem(R.string.reboot,"",R.drawable.restart_alt_fill0){navCtrl.navigate("Reboot")}
        }
        if(VERSION.SDK_INT>=28){
            SubPageItem(R.string.edit_time,"",R.drawable.schedule_fill0){navCtrl.navigate("EditTime")}
        }
        if(VERSION.SDK_INT>=23&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SubPageItem(R.string.permission_policy,"",R.drawable.key_fill0){navCtrl.navigate("PermissionPolicy")}
        }
        if(VERSION.SDK_INT>=34&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.mte_policy,"",R.drawable.memory_fill0){navCtrl.navigate("MTEPolicy")}
        }
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SubPageItem(R.string.nearby_streaming_policy,"",R.drawable.share_fill0){navCtrl.navigate("NearbyStreamingPolicy")}
        }
        if(VERSION.SDK_INT>=28&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.lock_task_feature,"",R.drawable.lock_fill0){navCtrl.navigate("LockTaskFeatures")}
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            SubPageItem(R.string.ca_cert,"",R.drawable.license_fill0){navCtrl.navigate("CaCert")}
        }
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            SubPageItem(R.string.security_logs,"",R.drawable.description_fill0){navCtrl.navigate("SecurityLogs")}
        }
        if(VERSION.SDK_INT>=23&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.system_update_policy,"",R.drawable.system_update_fill0){navCtrl.navigate("SystemUpdatePolicy")}
        }
        SubPageItem(R.string.wipe_data,"",R.drawable.warning_fill0){navCtrl.navigate("WipeData")}
        Spacer(Modifier.padding(vertical = 30.dp))
        LaunchedEffect(Unit){caCert =byteArrayOf()}
    }
}

@Composable
private fun Switches(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext, Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            SwitchItem(R.string.disable_cam,"", R.drawable.photo_camera_fill0,
                {myDpm.getCameraDisabled(null)},{myDpm.setCameraDisabled(myComponent,it)}
            )
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            SwitchItem(R.string.disable_screenshot, stringResource(R.string.also_disable_aosp_screen_record),R.drawable.screenshot_fill0,
                {myDpm.getScreenCaptureDisabled(null)},{myDpm.setScreenCaptureDisabled(myComponent,it) }
            )
        }
        if(VERSION.SDK_INT>=34&&(isDeviceOwner(myDpm)|| (isProfileOwner(myDpm)&&myDpm.isAffiliatedUser))){
            SwitchItem(R.string.disable_status_bar,"",R.drawable.notifications_fill0,
                {myDpm.isStatusBarDisabled},{myDpm.setStatusBarDisabled(myComponent,it) }
            )
        }
        if(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile)){
            if(VERSION.SDK_INT>=30){
                SwitchItem(R.string.auto_time,"",R.drawable.schedule_fill0,
                    {myDpm.getAutoTimeEnabled(myComponent)},{myDpm.setAutoTimeEnabled(myComponent,it) }
                )
                SwitchItem(R.string.auto_timezone,"",R.drawable.globe_fill0,
                    {myDpm.getAutoTimeZoneEnabled(myComponent)},{myDpm.setAutoTimeZoneEnabled(myComponent,it) }
                )
            }else{
                SwitchItem(R.string.auto_time,"",R.drawable.schedule_fill0,{myDpm.autoTimeRequired},{myDpm.setAutoTimeRequired(myComponent,it)})
            }
        }
        if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
            SwitchItem(R.string.master_mute,"",R.drawable.volume_up_fill0,
                {myDpm.isMasterVolumeMuted(myComponent)},{myDpm.setMasterVolumeMuted(myComponent,it) }
            )
        }
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)|| isProfileOwner(myDpm))){
            SwitchItem(R.string.backup_service,"",R.drawable.backup_fill0,
                {myDpm.isBackupServiceEnabled(myComponent)},{myDpm.setBackupServiceEnabled(myComponent,it) }
            )
        }
        if(VERSION.SDK_INT>=23&&(isDeviceOwner(myDpm)|| isProfileOwner(myDpm))){
            SwitchItem(R.string.disable_bt_contact_share,"",R.drawable.account_circle_fill0,
                {myDpm.getBluetoothContactSharingDisabled(myComponent)},{myDpm.setBluetoothContactSharingDisabled(myComponent,it)}
            )
        }
        if(VERSION.SDK_INT>=30&&isDeviceOwner(myDpm)){
            SwitchItem(R.string.common_criteria_mode, stringResource(R.string.common_criteria_mode_desc),R.drawable.security_fill0,
                {myDpm.isCommonCriteriaModeEnabled(myComponent)},{myDpm.setCommonCriteriaModeEnabled(myComponent,it)}
            )
        }
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            SwitchItem(
                R.string.usb_signal,"",R.drawable.usb_fill0, {myDpm.isUsbDataSignalingEnabled},
                {
                    if(myDpm.canUsbDataSignalingBeDisabled()){
                        myDpm.isUsbDataSignalingEnabled = it
                    }else{
                        Toast.makeText(myContext,myContext.getString(R.string.unsupported),Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Keyguard(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.keyguard), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT>=23){
            Button(
                onClick = {
                    Toast.makeText(myContext,
                        myContext.getString(if(myDpm.setKeyguardDisabled(myComponent,true)){R.string.success}else{R.string.fail}), Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(myDpm)|| (VERSION.SDK_INT>=28&&isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.disable))
            }
            Button(
                onClick = {
                    Toast.makeText(myContext,
                        myContext.getString(if(myDpm.setKeyguardDisabled(myComponent,false)){R.string.success}else{R.string.fail}), Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(myDpm)|| (VERSION.SDK_INT>=28&&isProfileOwner(myDpm)&&myDpm.isAffiliatedUser),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.enable))
            }
            Spacer(Modifier.padding(vertical = 3.dp))
            Information{Text(text = stringResource(R.string.require_no_password_to_disable))}
            Spacer(Modifier.padding(vertical = 8.dp))
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
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun BugReport(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
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
    }
}

@SuppressLint("NewApi")
@Composable
private fun Reboot(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp)){
        Spacer(Modifier.padding(vertical = 10.dp))
        Button(
            onClick = {myDpm.reboot(myComponent)},
            enabled = isDeviceOwner(myDpm),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reboot))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun EditTime(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.edit_time), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        var inputTime by remember{mutableStateOf("")}
        Text(text = stringResource(R.string.from_epoch_to_target_time))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputTime,
            label = { Text(stringResource(R.string.time_unit_ms))},
            onValueChange = {inputTime = it},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            enabled = isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile),
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {myDpm.setTime(myComponent,inputTime.toLong())},
            modifier = Modifier.fillMaxWidth(),
            enabled = inputTime!=""&&(isDeviceOwner(myDpm)||(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))
        ) {
            Text("应用")
        }
        Button(
            onClick = {inputTime = System.currentTimeMillis().toString()},
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.get_current_time))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun PermissionPolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var selectedPolicy by remember{mutableIntStateOf(myDpm.getPermissionPolicy(myComponent))}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.permission_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(stringResource(R.string.default_stringres), {selectedPolicy==PERMISSION_POLICY_PROMPT}, {selectedPolicy= PERMISSION_POLICY_PROMPT})
        RadioButtonItem(stringResource(R.string.auto_grant), {selectedPolicy==PERMISSION_POLICY_AUTO_GRANT}, {selectedPolicy= PERMISSION_POLICY_AUTO_GRANT})
        RadioButtonItem(stringResource(R.string.auto_deny), {selectedPolicy==PERMISSION_POLICY_AUTO_DENY}, {selectedPolicy= PERMISSION_POLICY_AUTO_DENY})
        Spacer(Modifier.padding(vertical = 5.dp))
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
private fun MTEPolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.mte_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
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
        Spacer(Modifier.padding(vertical = 5.dp))
        Information{Text(stringResource(R.string.mte_policy_desc))}
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun NearbyStreamingPolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var appPolicy by remember{mutableIntStateOf(myDpm.nearbyAppStreamingPolicy)}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.nearby_app_streaming), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 3.dp))
        RadioButtonItem(stringResource(R.string.decide_by_user),{appPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY},{appPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY})
        RadioButtonItem(stringResource(R.string.enabled),{appPolicy == NEARBY_STREAMING_ENABLED},{appPolicy = NEARBY_STREAMING_ENABLED})
        RadioButtonItem(stringResource(R.string.disabled),{appPolicy == NEARBY_STREAMING_DISABLED},{appPolicy = NEARBY_STREAMING_DISABLED})
        RadioButtonItem(stringResource(R.string.enable_if_secure_enough),{appPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY},{appPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY})
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                myDpm.nearbyAppStreamingPolicy = appPolicy
                Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("应用")
        }
        var notificationPolicy by remember{mutableIntStateOf(myDpm.nearbyNotificationStreamingPolicy)}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.nearby_notification_streaming), style = typography.titleLarge)
        Spacer(Modifier.padding(vertical = 3.dp))
        RadioButtonItem(stringResource(R.string.decide_by_user),{notificationPolicy == NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY},{notificationPolicy = NEARBY_STREAMING_NOT_CONTROLLED_BY_POLICY})
        RadioButtonItem(stringResource(R.string.enabled),{notificationPolicy == NEARBY_STREAMING_ENABLED},{notificationPolicy = NEARBY_STREAMING_ENABLED})
        RadioButtonItem(stringResource(R.string.disabled),{notificationPolicy == NEARBY_STREAMING_DISABLED},{notificationPolicy = NEARBY_STREAMING_DISABLED})
        RadioButtonItem(stringResource(R.string.enable_if_secure_enough),{notificationPolicy == NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY},{notificationPolicy = NEARBY_STREAMING_SAME_MANAGED_ACCOUNT_ONLY})
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                myDpm.nearbyNotificationStreamingPolicy = notificationPolicy
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun LockTaskFeatures(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
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
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.lock_task_feature), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
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
        Spacer(Modifier.padding(vertical = 5.dp))
        val whitelist = myDpm.getLockTaskPackages(myComponent).toMutableList()
        var listText by remember{mutableStateOf("")}
        var inputPkg by remember{mutableStateOf("")}
        val refreshWhitelist = {
            inputPkg=""
            listText=""
            listText = whitelist.toText()
        }
        LaunchedEffect(Unit){refreshWhitelist()}
        Text(text = stringResource(R.string.whitelist_app), style = typography.titleLarge)
        SelectionContainer(modifier = Modifier.animateContentSize()){
            Text(text = if(listText==""){ stringResource(R.string.none) }else{listText})
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
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun CaCert(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    var exist by remember{mutableStateOf(false)}
    var isEmpty by remember{mutableStateOf(true)}
    val refresh = {
        isEmpty = caCert.isEmpty()
        exist = if(!isEmpty){ myDpm.hasCaCertInstalled(myComponent, caCert) }else{ false }
    }
    LaunchedEffect(exist){ while(true){ refresh();delay(600) } }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.ca_cert), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = if(isEmpty){stringResource(R.string.please_select_ca_cert)}else{stringResource(R.string.cacert_installed, exist)}, modifier = Modifier.animateContentSize())
        Spacer(Modifier.padding(vertical = 5.dp))
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
private fun SecurityLogs(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.security_logs), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.developing))
        SwitchItem(R.string.enable,"",null,{myDpm.isSecurityLoggingEnabled(myComponent)},{myDpm.setSecurityLoggingEnabled(myComponent,it)})
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
private fun WipeData(){
    val myContext = LocalContext.current
    val userManager = myContext.getSystemService(Context.USER_SERVICE) as UserManager
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var flag by remember{ mutableIntStateOf(0) }
        var confirmed by remember{ mutableStateOf(false) }
        var externalStorage by remember{mutableStateOf(false)}
        var protectionData by remember{mutableStateOf(false)}
        var euicc by remember{mutableStateOf(false)}
        var silent by remember{mutableStateOf(false)}
        var reason by remember{mutableStateOf("")}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.wipe_data),style = typography.headlineLarge,modifier = Modifier.padding(6.dp),color = colorScheme.error)
        Spacer(Modifier.padding(vertical = 5.dp))
        CheckBoxItem(stringResource(R.string.wipe_external_storage),{externalStorage},{externalStorage=!externalStorage;confirmed=false})
        if(VERSION.SDK_INT>=22&&isDeviceOwner(myDpm)){
            CheckBoxItem(stringResource(R.string.wipe_reset_protection_data),{protectionData},{protectionData=!protectionData;confirmed=false})
        }
        if(VERSION.SDK_INT>=28){ CheckBoxItem(stringResource(R.string.wipe_euicc),{euicc},{euicc=!euicc;confirmed=false}) }
        if(VERSION.SDK_INT>=29){ CheckBoxItem(stringResource(R.string.wipe_silently),{silent},{silent=!silent;confirmed=false}) }
        AnimatedVisibility(!silent&&VERSION.SDK_INT>=28) {
            OutlinedTextField(
                value = reason, onValueChange = {reason=it},
                label = {Text(stringResource(R.string.reason))},
                enabled = !confirmed,
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
            )
        }
        Spacer(Modifier.padding(vertical = 5.dp))
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
        Button(
            onClick = {
                if(VERSION.SDK_INT>=28&&reason!=""){
                    myDpm.wipeData(flag,reason)
                }else{
                    myDpm.wipeData(flag)
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
            enabled = confirmed&&(VERSION.SDK_INT<34||(VERSION.SDK_INT>=34&&!userManager.isSystemUser)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("WipeData")
        }
        if (VERSION.SDK_INT >= 34&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))) {
            Button(
                onClick = {myDpm.wipeDevice(flag)},
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                enabled = confirmed,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("WipeDevice")
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT>=24&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            Information{Text(text = stringResource(R.string.will_delete_work_profile))}
        }
        if(VERSION.SDK_INT>=34&&Binder.getCallingUid()/100000==0){
            Information{Text(text = stringResource(R.string.api34_or_above_wipedata_cannot_in_system_user))}
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun SysUpdatePolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    val sharedPref = myContext.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium}else{typography.bodyLarge}
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        if(VERSION.SDK_INT>=23){
            Column {
                var selectedPolicy by remember{ mutableStateOf(myDpm.systemUpdatePolicy?.policyType) }
                Text(text = stringResource(R.string.system_update_policy), style = typography.headlineLarge)
                Spacer(Modifier.padding(vertical = 5.dp))
                RadioButtonItem(stringResource(R.string.system_update_policy_automatic),{selectedPolicy==TYPE_INSTALL_AUTOMATIC},{selectedPolicy= TYPE_INSTALL_AUTOMATIC})
                RadioButtonItem(stringResource(R.string.system_update_policy_install_windowed),{selectedPolicy==TYPE_INSTALL_WINDOWED},{selectedPolicy= TYPE_INSTALL_WINDOWED})
                RadioButtonItem(stringResource(R.string.system_update_policy_postpone),{selectedPolicy==TYPE_POSTPONE},{selectedPolicy= TYPE_POSTPONE})
                RadioButtonItem(stringResource(R.string.none),{selectedPolicy == null},{selectedPolicy=null})
                var windowedPolicyStart by remember{ mutableStateOf("") }
                var windowedPolicyEnd by remember{ mutableStateOf("") }
                if(selectedPolicy==2){
                    Spacer(Modifier.padding(vertical = 3.dp))
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
                    Spacer(Modifier.padding(vertical = 3.dp))
                    Text(text = stringResource(R.string.minutes_in_one_day), style = bodyTextStyle)
                }
                Button(
                    onClick = {
                        val policy =
                            when(selectedPolicy){
                                TYPE_INSTALL_AUTOMATIC-> SystemUpdatePolicy.createAutomaticInstallPolicy()
                                TYPE_INSTALL_WINDOWED-> SystemUpdatePolicy.createWindowedInstallPolicy(windowedPolicyStart.toInt(),windowedPolicyEnd.toInt())
                                TYPE_POSTPONE-> SystemUpdatePolicy.createPostponeInstallPolicy()
                                else->null
                            }
                        myDpm.setSystemUpdatePolicy(myComponent,policy)
                        Toast.makeText(myContext, "成功！", Toast.LENGTH_SHORT).show()
                    },
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
        if(VERSION.SDK_INT>=26){
            Spacer(Modifier.padding(vertical = 10.dp))
            val sysUpdateInfo = myDpm.getPendingSystemUpdate(myComponent)
            Column {
                if(sysUpdateInfo!=null){
                    Text(text = stringResource(R.string.update_received_time, Date(sysUpdateInfo.receivedTime)), style = bodyTextStyle)
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
        Spacer(Modifier.padding(vertical = 30.dp))
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
