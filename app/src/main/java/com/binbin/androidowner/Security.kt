package com.binbin.androidowner

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity

@Composable
fun Security(myDpm:DevicePolicyManager,myComponent:ComponentName,myContext:Context){
    var newPwd by remember{ mutableStateOf("") }
    var confirmed by remember{ mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        val myByteArray by remember{ mutableStateOf(byteArrayOf(1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0)) }
        if(VERSION.SDK_INT>=26){
            Column(
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .clip(RoundedCornerShape(10))
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp)
            ) {
                Text(
                    text = "密码重置令牌",
                    style = MaterialTheme.typography.titleLarge
                )
                Row {
                    Button(
                        onClick = {
                            if(myDpm.clearResetPasswordToken(myComponent)){
                                Toast.makeText(myContext, "清除成功", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(myContext, "清除失败", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("清除")
                    }
                    Button(
                        onClick = {
                            if(myDpm.setResetPasswordToken(myComponent, myByteArray)){
                                Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                            }else{
                                Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("设置")
                    }
                    Button(
                        onClick = {
                            if(!myDpm.isResetPasswordTokenActive(myComponent)){
                                try{
                                    activateToken(myContext)
                                }catch(e:NullPointerException){
                                    Toast.makeText(myContext, "请先设置令牌", Toast.LENGTH_SHORT).show()
                                }
                            }else{
                                Toast.makeText(myContext, "已经激活", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Text("激活")
                    }
                }
                Text("没有密码时会自动激活令牌")
            }
        }
        TextField(
            value = newPwd,
            onValueChange = {newPwd=it},
            enabled = !confirmed,
            label = { Text("密码")}
        )
        Text(
            text = "（留空可以清除密码）",
            modifier = Modifier.padding(vertical = 5.dp)
        )
        Row {
            Button(
                onClick = {
                    if(newPwd.length>=4||newPwd.isEmpty()){
                        confirmed=!confirmed
                    }else{
                        Toast.makeText(myContext, "需要4位数字或字母", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Text("确认密码")
            }
            if(VERSION.SDK_INT>=26){
                Button(
                    onClick = {
                        val resetSuccess = myDpm.resetPasswordWithToken(myComponent,newPwd,myByteArray,0)
                        if(resetSuccess){
                            Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show()
                        }
                        confirmed=false
                    },
                    enabled = confirmed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("设置密码")
                }
            }else{
                Button(
                    onClick = {
                        val resetSuccess = myDpm.resetPassword(newPwd,0)
                        if(resetSuccess){
                            Toast.makeText(myContext, "设置成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "设置失败", Toast.LENGTH_SHORT).show()
                        }
                        confirmed=false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("设置密码")
                }
            }
        }
        Text(
            text = "该操作可能会造成不可挽回的损失，请先备份好数据。设置密码的时候一定要谨慎！！！",
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.errorContainer)
                .padding(8.dp)
        )
    }
}

fun activateToken(myContext: Context){
    val ACTIVATE_TOKEN_PROMPT = "Use your credentials to enable remote password reset"
    val keyguardManager = myContext.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val confirmIntent = keyguardManager.createConfirmDeviceCredentialIntent(null, ACTIVATE_TOKEN_PROMPT)
    confirmIntent.setFlags(FLAG_ACTIVITY_NEW_TASK)
    if (confirmIntent != null) {
        startActivity(myContext,confirmIntent, null)
    } else {
        Toast.makeText(myContext, "激活失败", Toast.LENGTH_SHORT).show()
    }
}
