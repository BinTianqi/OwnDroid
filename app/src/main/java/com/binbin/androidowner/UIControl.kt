package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun UIControl(myDpm: DevicePolicyManager, myComponent: ComponentName){
    Column(
        modifier = Modifier.padding(8.dp)
    ) {
        Button(onClick = {myDpm.setStatusBarDisabled(myComponent,true)}) {
            Text("隐藏状态栏")
        }
        Button(onClick = {myDpm.setStatusBarDisabled(myComponent,false)}) {
            Text("显示状态栏")
        }
    }
}
