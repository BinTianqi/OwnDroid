package com.binbin.androidowner

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusManager
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding()
    ) {
        val myByteArray by remember{ mutableStateOf(byteArrayOf(1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0)) }
        Text(
            text = "以下操作可能会造成不可挽回的损失，请先备份好数据。执行操作时一定要谨慎！！！",
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = sections(MaterialTheme.colorScheme.errorContainer),
            style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium}
        )
        if(isWear){
            Text(
                text = "警告！手表不支持带字母的密码，也不支持超过4位的PIN码！如果你设置了这样的密码（或密码复杂度要求），你将无法解锁你的手表！",
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = sections(MaterialTheme.colorScheme.errorContainer),
                style = typography.bodyMedium
            )
        }
        if(myDpm.isDeviceOwnerApp("com.binbin.androidowner")){
            Column(
                modifier = sections()
            ) {
                if(VERSION.SDK_INT>=29){
                    val passwordComplexity = mapOf(
                        DevicePolicyManager.PASSWORD_COMPLEXITY_NONE to "无（允许不设密码）",
                        DevicePolicyManager.PASSWORD_COMPLEXITY_LOW to "低（允许图案和连续性）",
                        DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM to "中（无连续性，至少4位）",
                        DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH to "高（无连续性，至少6位）"
                    )
                    val pwdComplex = passwordComplexity[myDpm.passwordComplexity]
                    Text(text = "当前密码复杂度：$pwdComplex",style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
                }
                if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
                    Text("密码达到要求：${myDpm.isActivePasswordSufficient}",style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
                }
                val pwdFailedAttempts = myDpm.currentFailedPasswordAttempts
                Text(text = "密码已错误次数：$pwdFailedAttempts",style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
                if(VERSION.SDK_INT>=28&&(myDpm.isManagedProfile(myComponent)||myDpm.isProfileOwnerApp("com.binbin.androidowner"))){
                    val unifiedPwd = myDpm.isUsingUnifiedPassword(myComponent)
                    Text("个人与工作应用密码一致：$unifiedPwd",style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
                }
            }
        }
        if(VERSION.SDK_INT>=26){
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = sections()
            ) {
                Text(text = "密码重置令牌", style = typography.titleLarge,color = MaterialTheme.colorScheme.onPrimaryContainer)
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
                    Button(
                        onClick = {
                            if(myDpm.setResetPasswordToken(myComponent, myByteArray)){ Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show() }
                        },
                        enabled = isDeviceOwner(myDpm),
                        modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.47F)}
                    ) {
                        Text("设置")
                    }
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
                if(isWear){
                    Text(text = "（可以水平滚动）",style=typography.bodyMedium)
                }
                Text("没有密码时会自动激活令牌",style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
                Text("有可能无法设置密码重置令牌，因机而异",style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
            }
        }
        Column(
            modifier = sections()
        ) {
            var confirmed by remember{ mutableStateOf(false) }
            Text(text = "修改密码",style = MaterialTheme.typography.titleLarge,color = MaterialTheme.colorScheme.onPrimaryContainer)
            TextField(
                value = newPwd,
                onValueChange = {newPwd=it},
                enabled = !confirmed&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm)||myDpm.isAdminActive(myComponent)),
                label = { Text("密码")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                modifier = Modifier.padding(vertical = if(isWear){0.dp}else{5.dp}).fillMaxWidth()
            )
            Text(text = stringResource(R.string.reset_pwd_desc), modifier = Modifier.padding(vertical = 3.dp),style=if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
            var resetPwdFlag by remember{ mutableIntStateOf(0) }
            RadioButtonItem("开机时不要求密码（如果有指纹等其他解锁方式）",
                {resetPwdFlag==DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT},
                {resetPwdFlag=DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT})
            RadioButtonItem("要求立即输入新密码",{resetPwdFlag==DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY},
                {resetPwdFlag=DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY})
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
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
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
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError),
                    modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.9F)}
                ) {
                    Text("应用(旧)")
                }
            }
        }

        PasswordItem(R.string.max_pwd_fail,R.string.max_pwd_fail_desc,R.string.max_pwd_fail_textfield, myDpm,focusMgr,false,
            {myDpm.getMaximumFailedPasswordsForWipe(null).toString()},{ic -> myDpm.setMaximumFailedPasswordsForWipe(myComponent, ic.toInt()) })
        PasswordItem(R.string.pwd_timeout,R.string.pwd_timeout_desc,R.string.pwd_timeout_textfield, myDpm,focusMgr,true,
            {myDpm.getPasswordExpiration(null).toString()},{ic -> myDpm.setPasswordExpirationTimeout(myComponent, ic.toLong()) })
        PasswordItem(R.string.pwd_history,R.string.pwd_history_desc,R.string.pwd_history_textfield,myDpm, focusMgr,true,
            {myDpm.getPasswordHistoryLength(null).toString()},{ic -> myDpm.setPasswordHistoryLength(myComponent, ic.toInt()) })

        if(VERSION.SDK_INT>=31){
            Column(modifier = sections()) {
                val passwordComplexity = mapOf(
                    DevicePolicyManager.PASSWORD_COMPLEXITY_NONE to "无（允许不设密码）",
                    DevicePolicyManager.PASSWORD_COMPLEXITY_LOW to "低（允许图案和连续性）",
                    DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM to "中（无连续性，至少4位）",
                    DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH to "高（无连续性，至少6位）"
                ).toList()
                var selectedItem by remember{ mutableIntStateOf(passwordComplexity[0].first) }
                if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
                    selectedItem=myDpm.requiredPasswordComplexity
                }
                Text(text = "密码复杂度要求", style = typography.titleLarge,color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "不是实际密码复杂度",
                    style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
                Text(text = "设置密码复杂度将会取代密码质量",
                    style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
                RadioButtonItem(passwordComplexity[0].second,{selectedItem==passwordComplexity[0].first},{selectedItem=passwordComplexity[0].first})
                RadioButtonItem(passwordComplexity[1].second,{selectedItem==passwordComplexity[1].first},{selectedItem=passwordComplexity[1].first})
                RadioButtonItem(passwordComplexity[2].second,{selectedItem==passwordComplexity[2].first},{selectedItem=passwordComplexity[2].first})
                RadioButtonItem(passwordComplexity[3].second,{selectedItem==passwordComplexity[3].first},{selectedItem=passwordComplexity[3].first},
                if(isWear){MaterialTheme.colorScheme.error}else{MaterialTheme.colorScheme.onBackground})
                Text(text = "连续性：密码重复（6666）或密码递增递减（4321、2468）", modifier = Modifier.padding(vertical = 3.dp),
                    style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
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
                            onClick = {myContext.startActivity(Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD))},
                            modifier = Modifier.fillMaxWidth(0.95F)
                        ){
                            Text("要求设置新密码")
                        }
                    }
                }
                if(isWear){
                    Button(
                        onClick = {myContext.startActivity(Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD))},
                        modifier = Modifier.fillMaxWidth()
                    ){
                        Text("要求设置新密码")
                    }
                }
            }
        }

        Column(
            modifier = sections()
        ) {
            var expanded by remember{ mutableStateOf(VERSION.SDK_INT < 31) }
            val passwordQuality = mapOf(
                DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED to "未指定",
                DevicePolicyManager.PASSWORD_QUALITY_SOMETHING to "需要密码或图案，不管复杂度",
                DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC to "至少1个字母",
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC to "至少1个数字",
                DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC to "数字字母各至少一个",
                DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK to "生物识别（弱）",
                DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX to "复杂数字（无连续性）",
                DevicePolicyManager.PASSWORD_QUALITY_COMPLEX to "自定义（暂不支持）",
            ).toList()
            var selectedItem by remember{ mutableIntStateOf(passwordQuality[0].first) }
            if(isDeviceOwner(myDpm) || isProfileOwner(myDpm)){
                selectedItem=myDpm.getPasswordQuality(myComponent)
            }
            Text(text = "密码质量要求", style = typography.titleLarge,color = MaterialTheme.colorScheme.onPrimaryContainer)
            if(expanded){
            Text(text = "不是实际密码质量",
                style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})}
            if(VERSION.SDK_INT>=31){
                Text(text = "已弃用，请使用上面的”密码复杂度要求“", color = MaterialTheme.colorScheme.error,
                    style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
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
            Text(text = "连续性：密码重复（6666）或密码递增递减（4321、2468）", modifier = Modifier.padding(vertical = 3.dp),
                style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium})
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
            Button(onClick = {myContext.startActivity(Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD))}){
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
fun PasswordItem(
    itemName:Int,
    itemDesc:Int,
    textFieldLabel:Int,
    myDpm:DevicePolicyManager,
    focusMgr:FocusManager,
    allowZero:Boolean,
    getMethod:()->String,
    setMethod:(ic:String)->Unit
){
    Column(modifier = sections()) {
        var inputContent by remember{ mutableStateOf(if(isDeviceOwner(myDpm)){getMethod()}else{""}) }
        var inputContentEdited by remember{ mutableStateOf(false) }
        var ableToApply by remember{ mutableStateOf(true) }
        val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
        Text(text = stringResource(itemName), style = typography.titleLarge,color = MaterialTheme.colorScheme.onPrimaryContainer)
        Text(text= stringResource(itemDesc),modifier=Modifier.padding(vertical = 2.dp),
            style = if(!sharedPref.getBoolean("isWear",false)){typography.bodyLarge}else{typography.bodyMedium})
        if(!sharedPref.getBoolean("isWear",false)){Spacer(Modifier.padding(vertical = 2.dp))}
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
                    if(inputContent!=""&&((inputContent=="0"&&allowZero)||inputContent!="0")){
                        inputContentEdited = inputContent!=getMethod()
                        ableToApply = true
                    }else{
                        ableToApply = false
                    }
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                enabled = isDeviceOwner(myDpm),
                modifier = if(sharedPref.getBoolean("isWear",false)){Modifier.fillMaxWidth()}else{Modifier.fillMaxWidth(0.8F)}
            )
            if(!sharedPref.getBoolean("isWear",false)){
            IconButton(
                onClick = { focusMgr.clearFocus() ; setMethod(inputContent) ; inputContentEdited=inputContent!=getMethod() },
                enabled = isDeviceOwner(myDpm)&&ableToApply,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = if(inputContentEdited){MaterialTheme.colorScheme.onError}else{MaterialTheme.colorScheme.onPrimary},
                    containerColor = if(inputContentEdited){MaterialTheme.colorScheme.error}else{MaterialTheme.colorScheme.primary},
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                )
            ) {
                Icon(imageVector = Icons.Outlined.Check, contentDescription = null)
            }}
        }
        if(sharedPref.getBoolean("isWear",false)){
            Button(
                onClick = {focusMgr.clearFocus() ; setMethod(inputContent) ; inputContentEdited=inputContent!=getMethod()},
                enabled = isDeviceOwner(myDpm)&&ableToApply,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("应用")
            }
        }
    }
}

fun activateToken(myContext: Context){
    val ACTIVATE_TOKEN_PROMPT = "在这里激活密码重置令牌"
    val keyguardManager = myContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val confirmIntent = keyguardManager.createConfirmDeviceCredentialIntent(null, ACTIVATE_TOKEN_PROMPT)
    if (confirmIntent != null) {
        startActivity(myContext,confirmIntent, null)
    } else {
        Toast.makeText(myContext, "激活失败", Toast.LENGTH_SHORT).show()
    }
}
