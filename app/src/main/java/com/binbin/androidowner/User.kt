package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun User(myDpm:DevicePolicyManager,myComponent:ComponentName){
    Column {
        Column {
            Text(text = "参数", style = MaterialTheme.typography.titleLarge)
            if(Build.VERSION.SDK_INT>=34&&(myDpm.isProfileOwnerApp("com.binbin.androidowner")||myDpm.isManagedProfile(myComponent))){
                val financed = myDpm.isDeviceFinanced
                Text("Financed Device : $financed")
            }
            if (Build.VERSION.SDK_INT >= 28) {
                val logoutable = myDpm.isLogoutEnabled
                Text(text = "用户可以退出 : $logoutable")
                val ephemeralUser = myDpm.isEphemeralUser(myComponent)
                Text(text = "临时用户： $ephemeralUser")
                Text("切换用户后或设备重启后会删除临时用户")
                val affiliatedUser = myDpm.isAffiliatedUser
                Text(text = "Affiliated User:$affiliatedUser")
            }
        }
    }
}
