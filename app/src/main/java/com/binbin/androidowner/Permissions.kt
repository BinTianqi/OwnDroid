package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.binbin.androidowner.ui.theme.AndroidOwnerTheme

@Composable
fun DpmPermissions(myDpm: DevicePolicyManager, myComponent: ComponentName){
    //da:DeviceAdmin do:DeviceOwner
    val isda = myDpm.isAdminActive(myComponent)
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    Column {
        Text("Device Admin: $isda")
        Text("Device Owner: $isdo")
        Button(onClick = {myDpm.clearDeviceOwnerApp("com.binbin.androidowner")}) {
            Text("不当Device Owner了")
        }
    }
}
