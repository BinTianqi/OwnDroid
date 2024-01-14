package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun UIControl(myDpm: DevicePolicyManager, myComponent: ComponentName){
    Button(onClick = {myDpm.setStatusBarDisabled(myComponent,true)}) {
        Text("隐藏状态栏")
    }
    Button(onClick = {myDpm.setStatusBarDisabled(myComponent,false)}) {
        Text("显示状态栏")
    }
}
