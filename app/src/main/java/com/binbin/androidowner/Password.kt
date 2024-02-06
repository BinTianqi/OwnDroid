package com.binbin.androidowner

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity

@Composable
fun Password(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    var newPwd by remember{ mutableStateOf("") }
    val focusMgr = LocalFocusManager.current
    val isWear = sharedPref.getBoolean("isWear",false)
    val titleColor = colorScheme.onPrimaryContainer
    val bodyTextStyle = if(isWear){typography.bodyMedium}else{typography.bodyLarge}
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).navigationBarsPadding()
    ) {
        val myByteArray by remember{ mutableStateOf(byteArrayOf(1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0)) }
        Text(
            text = "以下操作可能会造成不可挽回的损失，请先备份好数据。执行操作时一定要谨慎！！！",
            color = colorScheme.onErrorContainer,
            modifier = sections(colorScheme.errorContainer),
            style=bodyTextStyle
        )
        if(isWear){
            Text(
                text = "警告！手表不支持带字母的密码，也不支持超过4位的PIN码！如果你设置了这样的密码（或密码复杂度要求），你将无法解锁你的手表！",
                color = colorScheme.onErrorContainer,
                modifier = sections(colorScheme.errorContainer),
                style = typography.bodyMedium
            )
        }
        if(myDpm.isDeviceOwnerApp("com.binbin.androidowner")){
            Column(modifier = sections()) {
                if(VERSION.SDK_INT>=29){
                    val passwordComplexity = mapOf(
                        PASSWORD_COMPLEXITY_NONE to "无（允许不设密码）",
                        PASSWORD_COMPLEXITY_LOW to "低（允许图案和连续性）",
                        PASSWORD_COMPLEXITY_MEDIUM to "中（无连续性，至少4位）",
                        PASSWORD_COMPLEXITY_HIGH to "高（无连续性，至少6位）"
                    )
                    val pwdComplex = passwordComplexity[myDpm.passwordComplexity]
                    Text(text = "当前密码复杂度：$pwdComplex",style=bodyTextStyle)
                }
                if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
                    Text("密码达到要求：${myDpm.isActivePasswordSufficient}",style=bodyTextStyle)
                }
                val pwdFailedAttempts = myDpm.currentFailedPasswordAttempts
                Text(text = "密码已错误次数：$pwdFailedAttempts",style=bodyTextStyle)
                if(VERSION.SDK_INT>=28&&(myDpm.isManagedProfile(myComponent)||myDpm.isProfileOwnerApp("com.binbin.androidowner"))){
                    val unifiedPwd = myDpm.isUsingUnifiedPassword(myComponent)
                    Text("个人与工作应用密码一致：$unifiedPwd",style=bodyTextStyle)
                }
            }
        }
        if(VERSION.SDK_INT>=26){
            Column(horizontalAlignment = Alignment.Start, modifier = sections()) {
                Text(text = "密码重置令牌", style = typography.titleLarge,color = titleColor)
                Row(
                    modifier = if(!isWear){Modifier.fillMaxWidth()}else{Modifier.horizontalScroll(rememberScrollState())},
                    horizontalArrangement = Arrangement.SpaceBetween
                ){
                    Button(
                        onClick = {
                            if(myDpm.clearResetPasswordToken(myComponent)){ Toast.makeText(myContext, "清除成功", Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "清除失败", Toast.LENGTH_SHORT).show() }
                        },
                        modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.32F)},
                        enabled = isDeviceOwner(myDpm)
                    ) {
                        Text("清除")
                    }
                    if(isWear){Spacer(Modifier.padding(horizontal = 2.dp))}
                    Button(
                        onClick = {
                            try {
                                if(myDpm.setResetPasswordToken(myComponent, myByteArray)){
                                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                                }else{
                                    Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                                }
                            }catch(e:SecurityException){
                                Toast.makeText(myContext, "失败（安全异常）", Toast.LENGTH_SHORT).show()
                            }
                        },
                        enabled = isDeviceOwner(myDpm),
                        modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.47F)}
                    ) {
                        Text("设置")
                    }
                    if(isWear){Spacer(Modifier.padding(horizontal = 2.dp))}
                    Button(
                        onClick = {
                            if(!myDpm.isResetPasswordTokenActive(myComponent)){
                                try{ activateToken(myContext)
                                }catch(e:NullPointerException){ Toast.makeText(myContext, "请先设置令牌", Toast.LENGTH_SHORT).show() }
                            }else{ Toast.makeText(myContext, "已经激活", Toast.LENGTH_SHORT).show() }
                        },
                        enabled = isDeviceOwner(myDpm),
                        modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.88F)}
                    ) {
                        Text("激活")
                    }
                }
                if(isWear){ Text(text = "（可以水平滚动）",style=typography.bodyMedium) }
                Text("没有密码时会自动激活令牌",style=bodyTextStyle)
                Text("有可能无法设置密码重置令牌，因机而异",style=bodyTextStyle)
            }
        }
        Column(
            modifier = sections()
        ) {
            var confirmed by remember{ mutableStateOf(false) }
            Text(text = "修改密码",style = typography.titleLarge,color = titleColor)
            TextField(
                value = newPwd,
                onValueChange = {newPwd=it},
                enabled = !confirmed&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm)||myDpm.isAdminActive(myComponent)),
                label = { Text("密码")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                modifier = Modifier.padding(vertical = if(isWear){0.dp}else{5.dp}).fillMaxWidth()
            )
            Text(text = stringResource(R.string.reset_pwd_desc), modifier = Modifier.padding(vertical = 3.dp),style=bodyTextStyle)
            var resetPwdFlag by remember{ mutableIntStateOf(0) }
            if(VERSION.SDK_INT>=23){
                RadioButtonItem("开机时不要求密码（如果有指纹等其他解锁方式）", {resetPwdFlag==RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT}, {resetPwdFlag=RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT})
            }
            RadioButtonItem("要求立即输入新密码",{resetPwdFlag==RESET_PASSWORD_REQUIRE_ENTRY}, {resetPwdFlag=RESET_PASSWORD_REQUIRE_ENTRY})
            RadioButtonItem("无",{resetPwdFlag==0},{resetPwdFlag=0})
            Row(modifier = if(!isWear){Modifier.fillMaxWidth()}else{Modifier.horizontalScroll(rememberScrollState())},horizontalArrangement = Arrangement.SpaceBetween) {
                Button(
                    onClick = {
                        if(newPwd.length>=4||newPwd.isEmpty()){ confirmed=!confirmed
                        }else{ Toast.makeText(myContext, "需要4位密码", Toast.LENGTH_SHORT).show() }
                    },
                    enabled = isDeviceOwner(myDpm) || isProfileOwner(myDpm) || myDpm.isAdminActive(myComponent),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.3F)}
                ) {
                    Text(text = if(confirmed){"取消"}else{"确定"})
                }
                if(VERSION.SDK_INT>=26){
                    Button(
                        onClick = {
                            val resetSuccess = myDpm.resetPasswordWithToken(myComponent,newPwd,myByteArray,resetPwdFlag)
                            if(resetSuccess){ Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show() }
                            confirmed=false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                        enabled = confirmed&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm)),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.42F)}
                    ) {
                        Text("应用")
                    }
                }
                Button(
                    onClick = {
                        val resetSuccess = myDpm.resetPassword(newPwd,resetPwdFlag)
                        if(resetSuccess){ Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                        }else{ Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show() }
                        confirmed=false
                    },
                    enabled = confirmed,
                    colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.9F)}
                ) {
                    Text("应用(旧)")
                }
            }
        }

        PasswordItem(R.string.max_pwd_fail,R.string.max_pwd_fail_desc,R.string.max_pwd_fail_textfield, false,
            {myDpm.getMaximumFailedPasswordsForWipe(null).toString()},{ic -> myDpm.setMaximumFailedPasswordsForWipe(myComponent, ic.toInt()) })
        PasswordItem(R.string.pwd_timeout,R.string.pwd_timeout_desc,R.string.pwd_timeout_textfield,true,
            {myDpm.getPasswordExpiration(null).toString()},{ic -> myDpm.setPasswordExpirationTimeout(myComponent, ic.toLong()) })
        PasswordItem(R.string.pwd_history,R.string.pwd_history_desc,R.string.pwd_history_textfield,true,
            {myDpm.getPasswordHistoryLength(null).toString()},{ic -> myDpm.setPasswordHistoryLength(myComponent, ic.toInt()) })
        PasswordItem(R.string.max_time_to_lock,R.string.max_time_to_lock_desc,R.string.max_time_to_lock_textfield,true,
            {myDpm.getMaximumTimeToLock(myComponent).toString()},{ic -> myDpm.setMaximumTimeToLock(myComponent,ic.toLong())})

        if(VERSION.SDK_INT>=31){
            Column(modifier = sections()) {
                val passwordComplexity = mapOf(
                    PASSWORD_COMPLEXITY_NONE to "无（允许不设密码）",
                    PASSWORD_COMPLEXITY_LOW to "低（允许图案和连续性）",
                    PASSWORD_COMPLEXITY_MEDIUM to "中（无连续性，至少4位）",
                    PASSWORD_COMPLEXITY_HIGH to "高（无连续性，至少6位）"
                ).toList()
                var selectedItem by remember{ mutableIntStateOf(passwordComplexity[0].first) }
                if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
                    selectedItem=myDpm.requiredPasswordComplexity
                }
                Text(text = "密码复杂度要求", style = typography.titleLarge,color = titleColor)
                Text(text = "不是实际密码复杂度", style = bodyTextStyle)
                RadioButtonItem(passwordComplexity[0].second,{selectedItem==passwordComplexity[0].first},{selectedItem=passwordComplexity[0].first})
                RadioButtonItem(passwordComplexity[1].second,{selectedItem==passwordComplexity[1].first},{selectedItem=passwordComplexity[1].first})
                RadioButtonItem(passwordComplexity[2].second,{selectedItem==passwordComplexity[2].first},{selectedItem=passwordComplexity[2].first})
                RadioButtonItem(passwordComplexity[3].second,{selectedItem==passwordComplexity[3].first},{selectedItem=passwordComplexity[3].first},
                if(isWear){
                    colorScheme.error}else{
                    colorScheme.onBackground})
                Text(text = "连续性：密码重复（6666）或密码递增递减（4321、2468）", modifier = Modifier.padding(vertical = 3.dp), style = bodyTextStyle)
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            myDpm.requiredPasswordComplexity = selectedItem
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        },
                        enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
                        modifier = if(isWear){Modifier.fillMaxWidth()}else{Modifier.fillMaxWidth(0.4F)}
                    ) {
                        Text(text = "应用")
                    }
                    if(!isWear){
                        Button(
                            onClick = {myContext.startActivity(Intent(ACTION_SET_NEW_PASSWORD))},
                            modifier = Modifier.fillMaxWidth(0.95F)
                        ){
                            Text("要求设置新密码")
                        }
                    }
                }
                if(isWear){
                    Button(
                        onClick = {myContext.startActivity(Intent(ACTION_SET_NEW_PASSWORD))},
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text("要求设置新密码")
                    }
                }
            }
        }
        
        Column(modifier = sections()){
            var state by remember{mutableIntStateOf(-1)}
            var shortcuts by remember{mutableStateOf(false)}
            var biometrics by remember{mutableStateOf(false)}
            var iris by remember{mutableStateOf(false)}
            var face by remember{mutableStateOf(false)}
            var remote by remember{mutableStateOf(false)}
            var fingerprint by remember{mutableStateOf(false)}
            var agents by remember{mutableStateOf(false)}
            var unredacted by remember{mutableStateOf(false)}
            var notification by remember{mutableStateOf(false)}
            var camera by remember{mutableStateOf(false)}
            var widgets by remember{mutableStateOf(false)}
            val calculateCustomFeature = {
                var calculate = myDpm.getKeyguardDisabledFeatures(myComponent)
                if(calculate==0){state=0}
                else{
                    if(calculate-KEYGUARD_DISABLE_SHORTCUTS_ALL>=0 && VERSION.SDK_INT>=34){shortcuts=true;calculate-=KEYGUARD_DISABLE_SHORTCUTS_ALL}
                    if(calculate-KEYGUARD_DISABLE_BIOMETRICS>=0&&VERSION.SDK_INT>=28){biometrics=true;calculate -= KEYGUARD_DISABLE_BIOMETRICS}
                    if(calculate-KEYGUARD_DISABLE_IRIS>=0&&VERSION.SDK_INT>=28){iris=true;calculate -=KEYGUARD_DISABLE_IRIS }
                    if(calculate-KEYGUARD_DISABLE_FACE>=0&&VERSION.SDK_INT>=28){face=true;calculate -= KEYGUARD_DISABLE_FACE}
                    if(calculate-KEYGUARD_DISABLE_REMOTE_INPUT>=0&&VERSION.SDK_INT>=24){remote=true;calculate -= KEYGUARD_DISABLE_REMOTE_INPUT}
                    if(calculate-KEYGUARD_DISABLE_FINGERPRINT>=0){fingerprint=true;calculate -= KEYGUARD_DISABLE_FINGERPRINT}
                    if(calculate-KEYGUARD_DISABLE_TRUST_AGENTS>=0){agents=true;calculate -= KEYGUARD_DISABLE_TRUST_AGENTS}
                    if(calculate-KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS>=0){unredacted=true;calculate -= KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS}
                    if(calculate-KEYGUARD_DISABLE_SECURE_NOTIFICATIONS>=0){notification=true;calculate -= KEYGUARD_DISABLE_SECURE_NOTIFICATIONS}
                    if(calculate-KEYGUARD_DISABLE_SECURE_CAMERA>=0){camera=true;calculate -= KEYGUARD_DISABLE_SECURE_CAMERA}
                    if(calculate-KEYGUARD_DISABLE_WIDGETS_ALL>=0){widgets=true;calculate -= KEYGUARD_DISABLE_WIDGETS_ALL}
                }
            }
            if(state==-1){
                state = when(myDpm.getKeyguardDisabledFeatures(myComponent)){
                    KEYGUARD_DISABLE_FEATURES_NONE->0
                    KEYGUARD_DISABLE_FEATURES_ALL->1
                    else->2
                }
                calculateCustomFeature()
            }
            Text(text = "锁屏功能", style = typography.titleLarge)
            RadioButtonItem("允许全部",{state==0},{state=0})
            RadioButtonItem("禁用全部",{state==1},{state=1})
            RadioButtonItem("自定义",{state==2},{state=2})
            AnimatedVisibility(state==2) {
                Column {
                    CheckBoxItem("禁用小工具(安卓5以下)",{widgets},{widgets=!widgets})
                    CheckBoxItem("禁用相机",{camera},{camera=!camera})
                    CheckBoxItem("禁用通知",{notification},{notification=!notification})
                    CheckBoxItem("禁用未经编辑的通知",{unredacted},{unredacted=!unredacted})
                    CheckBoxItem("禁用可信代理",{agents},{agents=!agents})
                    CheckBoxItem("禁用指纹解锁",{fingerprint},{fingerprint=!fingerprint})
                    if(VERSION.SDK_INT>=24){ CheckBoxItem("禁止在锁屏通知中输入(弃用)",{remote}, {remote=!remote}) }
                    if(VERSION.SDK_INT>=28){
                        CheckBoxItem("禁用人脸解锁",{face},{face=!face})
                        CheckBoxItem("禁用虹膜解锁(?)",{iris},{iris=!iris})
                        CheckBoxItem("禁用生物识别",{biometrics},{biometrics=!biometrics})
                    }
                    if(VERSION.SDK_INT>=34){ CheckBoxItem("禁用锁屏快捷方式",{shortcuts},{shortcuts=!shortcuts}) }
                }
            }
            Button(
                onClick = {
                    var result = 0
                    if(state==0){ result = 0 }
                    else if(state==1){ result = KEYGUARD_DISABLE_FEATURES_ALL }
                    else{
                        if(shortcuts&&VERSION.SDK_INT>=34){result+=KEYGUARD_DISABLE_SHORTCUTS_ALL}
                        if(biometrics&&VERSION.SDK_INT>=28){result+=KEYGUARD_DISABLE_BIOMETRICS}
                        if(iris&&VERSION.SDK_INT>=28){result+=KEYGUARD_DISABLE_IRIS}
                        if(face&&VERSION.SDK_INT>=28){result+=KEYGUARD_DISABLE_FACE}
                        if(remote&&VERSION.SDK_INT>=24){result+=KEYGUARD_DISABLE_REMOTE_INPUT}
                        if(fingerprint){result+=KEYGUARD_DISABLE_FINGERPRINT}
                        if(agents){result+=KEYGUARD_DISABLE_TRUST_AGENTS}
                        if(unredacted){result+=KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS}
                        if(notification){result+=KEYGUARD_DISABLE_SECURE_NOTIFICATIONS}
                        if(camera){result+=KEYGUARD_DISABLE_SECURE_CAMERA}
                        if(widgets){result+=KEYGUARD_DISABLE_WIDGETS_ALL}
                    }
                    myDpm.setKeyguardDisabledFeatures(myComponent,result)
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    calculateCustomFeature()
                },
                enabled = isProfileOwner(myDpm)||isDeviceOwner(myDpm),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "应用")
            }
        }

        Column(modifier = sections()) {
            var expanded by remember{ mutableStateOf(VERSION.SDK_INT < 31) }
            val passwordQuality = mapOf(
                PASSWORD_QUALITY_UNSPECIFIED to "未指定",
                PASSWORD_QUALITY_SOMETHING to "需要密码或图案，不管复杂度",
                PASSWORD_QUALITY_ALPHABETIC to "至少1个字母",
                PASSWORD_QUALITY_NUMERIC to "至少1个数字",
                PASSWORD_QUALITY_ALPHANUMERIC to "数字字母各至少一个",
                PASSWORD_QUALITY_BIOMETRIC_WEAK to "生物识别（弱）",
                PASSWORD_QUALITY_NUMERIC_COMPLEX to "复杂数字（无连续性）",
                PASSWORD_QUALITY_COMPLEX to "自定义（暂不支持）",
            ).toList()
            var selectedItem by remember{ mutableIntStateOf(passwordQuality[0].first) }
            if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
                selectedItem=myDpm.getPasswordQuality(myComponent)
            }
            Text(text = "密码质量要求", style = typography.titleLarge,color = titleColor)
            if(expanded){
                Text(text = "不是实际密码质量", style = bodyTextStyle)
                Text(text = "设置密码复杂度将会取代密码质量", style = bodyTextStyle)
            }
            if(VERSION.SDK_INT>=31){
                Text(text = "已弃用，请使用上面的”密码复杂度要求“", color = colorScheme.error, style = bodyTextStyle)
            }
            if(expanded){
            RadioButtonItem(passwordQuality[0].second,{selectedItem==passwordQuality[0].first},{selectedItem=passwordQuality[0].first})
            RadioButtonItem(passwordQuality[1].second,{selectedItem==passwordQuality[1].first},{selectedItem=passwordQuality[1].first})
            RadioButtonItem(passwordQuality[2].second,{selectedItem==passwordQuality[2].first},{selectedItem=passwordQuality[2].first})
            RadioButtonItem(passwordQuality[3].second,{selectedItem==passwordQuality[3].first},{selectedItem=passwordQuality[3].first})
            RadioButtonItem(passwordQuality[4].second,{selectedItem==passwordQuality[4].first},{selectedItem=passwordQuality[4].first})
            RadioButtonItem(passwordQuality[5].second,{selectedItem==passwordQuality[5].first},{selectedItem=passwordQuality[5].first})
            RadioButtonItem(passwordQuality[6].second,{selectedItem==passwordQuality[6].first},{selectedItem=passwordQuality[6].first})
            RadioButtonItem(passwordQuality[7].second,{selectedItem==passwordQuality[7].first},{selectedItem=passwordQuality[7].first})
            Text(text = "连续性：密码重复（6666）或密码递增递减（4321、2468）", modifier = Modifier.padding(vertical = 3.dp), style = bodyTextStyle)
            Button(
                onClick = {
                    myDpm.setPasswordQuality(myComponent,selectedItem)
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(myDpm) || isProfileOwner(myDpm),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("应用")
            }
            if(VERSION.SDK_INT<31){
            Button(onClick = {myContext.startActivity(Intent(ACTION_SET_NEW_PASSWORD))}){
                Text("要求设置新密码")
            }}
            }else{
                Button(onClick = {expanded=true}) {
                    Text("展开")
                }
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun PasswordItem(
    itemName:Int,
    itemDesc:Int,
    textFieldLabel:Int,
    allowZero:Boolean,
    getMethod:()->String,
    setMethod:(ic:String)->Unit
){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val focusMgr = LocalFocusManager.current
    Column(modifier = sections()) {
        var inputContent by remember{ mutableStateOf(if(isDeviceOwner(myDpm)){getMethod()}else{""}) }
        var ableToApply by remember{ mutableStateOf(inputContent!=""&&((inputContent=="0"&&allowZero)||inputContent!="0")) }
        Text(text = stringResource(itemName), style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
        Text(text= stringResource(itemDesc),modifier=Modifier.padding(vertical = 2.dp), style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
        if(!isWear){Spacer(Modifier.padding(vertical = 2.dp))}
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ){
            TextField(
                value = inputContent,
                label = { Text(stringResource(textFieldLabel))},
                onValueChange = {
                    inputContent = it
                    ableToApply = inputContent!=""&&((inputContent=="0"&&allowZero)||inputContent!="0")
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                enabled = isDeviceOwner(myDpm),
                modifier = if(isWear){Modifier.fillMaxWidth()}else{Modifier.fillMaxWidth(0.8F)}
            )
            if(!isWear){
            IconButton(
                onClick = { focusMgr.clearFocus() ; setMethod(inputContent) },
                enabled = isDeviceOwner(myDpm)&&ableToApply,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = colorScheme.onPrimary,
                    containerColor = colorScheme.primary,
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
            }}
        }
        if(isWear){
            Button(
                onClick = {focusMgr.clearFocus() ; setMethod(inputContent)},
                enabled = isDeviceOwner(myDpm)&&ableToApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("应用")
            }
        }
    }
}

fun activateToken(myContext: Context){
    val desc = "在这里激活密码重置令牌"
    val keyguardManager = myContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val confirmIntent = keyguardManager.createConfirmDeviceCredentialIntent(null, desc)
    if (confirmIntent != null) {
        startActivity(myContext,confirmIntent, null)
    } else {
        Toast.makeText(myContext, "激活失败", Toast.LENGTH_SHORT).show()
    }
}
