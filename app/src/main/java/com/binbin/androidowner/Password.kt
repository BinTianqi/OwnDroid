package com.binbin.androidowner

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity

@Composable
fun Password(myDpm:DevicePolicyManager,myComponent:ComponentName,myContext:Context){
    var newPwd by remember{ mutableStateOf("") }
    var confirmed by remember{ mutableStateOf(false) }
    val focusMgr = LocalFocusManager.current
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.errorContainer)
                .padding(8.dp)
        )
        if(myDpm.isDeviceOwnerApp("com.binbin.androidowner")){
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(15))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                if(VERSION.SDK_INT>=29){
                    val pwdComplex = myDpm.passwordComplexity
                    Text(text = "密码复杂度：$pwdComplex")
                }
                val pwdFailedAttempts = myDpm.currentFailedPasswordAttempts
                Text(text = "密码已错误次数：$pwdFailedAttempts")
                if(VERSION.SDK_INT>=28&&(myDpm.isManagedProfile(myComponent)||myDpm.isProfileOwnerApp("com.binbin.androidowner"))){
                    val unifiedPwd = myDpm.isUsingUnifiedPassword(myComponent)
                    Text("个人与工作应用密码一致：$unifiedPwd")
                }
            }
        }
        if(VERSION.SDK_INT>=26){
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(10))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text(text = "密码重置令牌", style = MaterialTheme.typography.titleLarge)
                Row {
                    Button(
                        onClick = {
                            if(myDpm.clearResetPasswordToken(myComponent)){ Toast.makeText(myContext, "清除成功", Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "清除失败", Toast.LENGTH_SHORT).show() }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = isDeviceOwner(myDpm)
                    ) {
                        Text("清除")
                    }
                    Button(
                        onClick = {
                            if(myDpm.setResetPasswordToken(myComponent, myByteArray)){ Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show() }
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        enabled = isDeviceOwner(myDpm)
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
                        enabled = isDeviceOwner(myDpm)
                    ) {
                        Text("激活")
                    }
                }
                Text("没有密码时会自动激活令牌")
                Text("有可能无法设置密码重置令牌，因机而异，AVD上能用")
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(10))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(10.dp)
        ) {
            TextField(
                value = newPwd,
                onValueChange = {newPwd=it},
                enabled = !confirmed&& isDeviceOwner(myDpm),
                label = { Text("密码")},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            )
            Text(text = stringResource(R.string.reset_pwd_desc), modifier = Modifier.padding(vertical = 5.dp))
            Row {
                Button(
                    onClick = {
                        if(newPwd.length>=4||newPwd.isEmpty()){ confirmed=!confirmed
                        }else{ Toast.makeText(myContext, "需要4位数字或字母", Toast.LENGTH_SHORT).show() }
                    },
                    modifier = Modifier.padding(end = 10.dp),
                    enabled = isDeviceOwner(myDpm)
                ) {
                    Text("确认密码")
                }
                if(VERSION.SDK_INT>=26){
                    Button(
                        onClick = {
                            val resetSuccess = myDpm.resetPasswordWithToken(myComponent,newPwd,myByteArray,0)
                            if(resetSuccess){ Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show() }
                            confirmed=false
                        },
                        enabled = confirmed,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    ) {
                        Text("设置密码")
                    }
                }else{
                    Button(
                        onClick = {
                            val resetSuccess = myDpm.resetPassword(newPwd,0)
                            if(resetSuccess){ Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                            }else{ Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show() }
                            confirmed=false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError)
                    ) {
                        Text("设置密码")
                    }
                }
            }
        }
        PasswordItem(R.string.max_pwd_fail,R.string.max_pwd_fail_desc,R.string.max_pwd_fail_textfield, myDpm,focusMgr,false,
            {myDpm.getMaximumFailedPasswordsForWipe(null).toString()},{ic -> myDpm.setMaximumFailedPasswordsForWipe(myComponent, ic.toInt()) })
        PasswordItem(R.string.pwd_timeout,R.string.pwd_timeout_desc,R.string.pwd_timeout_textfield, myDpm,focusMgr,true,
            {myDpm.getPasswordExpiration(null).toString()},{ic -> myDpm.setPasswordExpirationTimeout(myComponent, ic.toLong()) })
        PasswordItem(R.string.pwd_history,R.string.pwd_history_desc,R.string.pwd_history_textfield,myDpm, focusMgr,true,
            {myDpm.getPasswordHistoryLength(null).toString()},{ic -> myDpm.setPasswordHistoryLength(myComponent, ic.toInt()) })
        Spacer(Modifier.padding(vertical = 20.dp))
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(10.dp)
    ) {
        var inputContent by remember{ mutableStateOf(if(isDeviceOwner(myDpm)){getMethod()}else{""}) }
        var inputContentEdited by remember{ mutableStateOf(false) }
        var ableToApply by remember{ mutableStateOf(true) }
        Text(text = stringResource(itemName), style = MaterialTheme.typography.titleLarge)
        Text(text= stringResource(itemDesc),modifier=Modifier.padding(vertical = 2.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp)
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
                enabled = isDeviceOwner(myDpm)
            )
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
            }
        }
    }
}

fun activateToken(myContext: Context){
    val ACTIVATE_TOKEN_PROMPT = "在这里激活密码重置令牌"
    val keyguardManager = myContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val confirmIntent = keyguardManager.createConfirmDeviceCredentialIntent(null, ACTIVATE_TOKEN_PROMPT)
    confirmIntent.setFlags(FLAG_ACTIVITY_NEW_TASK)
    if (confirmIntent != null) {
        startActivity(myContext,confirmIntent, null)
    } else {
        Toast.makeText(myContext, "激活失败", Toast.LENGTH_SHORT).show()
    }
}
