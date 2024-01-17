package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun Security(myDpm:DevicePolicyManager,myComponent:ComponentName){
    Column {
        Button(onClick = {myDpm.clearResetPasswordToken(myComponent)}) {
            Text("清除重置密码令牌")
        }
        Button(onClick = {myDpm.setResetPasswordToken(myComponent, byteArrayOf(32))}) {
            Text("设置重置密码令牌")
        }
        Text("不知道上面两个东西干啥用的")
        Button(onClick = {myDpm.resetPassword(null,0)}) {
            Text("清除密码")
        }
    }
    
}
