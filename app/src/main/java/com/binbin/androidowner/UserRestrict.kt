package com.binbin.androidowner

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.UserManager
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@SuppressLint("UnrememberedMutableState")
@Composable
fun UserRestrict(myDpm: DevicePolicyManager, myComponent: ComponentName){
    Column {
        val strictState = myDpm.getUserRestrictions(myComponent)
        for (key in strictState.keySet()) {
            val value = when (strictState[key]) {
                is Boolean -> if (strictState.getBoolean(key)) "true" else "false"
                else -> ""
            }
            //println("Key: $key, Value: $value")
            Log.e(">>>>>>>>>>>","Key: $key, Value: $value")
        }
        Text("限制蓝牙：${strictState.getBoolean("no_bluetooth")}")
        Button(onClick = {myDpm.addUserRestriction(myComponent, UserManager.DISALLOW_BLUETOOTH)}) {
            Text("禁用蓝牙")
        }
        Button(onClick = {myDpm.clearUserRestriction(myComponent, UserManager.DISALLOW_BLUETOOTH)}) {
            Text("允许蓝牙")
        }
        Text("限制Wi-Fi：${strictState.getBoolean("no_config_wifi")}")
        Button(onClick = {myDpm.addUserRestriction(myComponent, UserManager.DISALLOW_CONFIG_WIFI)}) {
            Text("禁用Wi-Fi")
        }
        Button(onClick = {myDpm.clearUserRestriction(myComponent, UserManager.DISALLOW_CONFIG_WIFI)}) {
            Text("允许Wi-Fi")
        }
        Text("限制调试：${strictState.getBoolean("no_debug_features")}")
        Button(onClick = {myDpm.addUserRestriction(myComponent, UserManager.DISALLOW_DEBUGGING_FEATURES)}) {
            Text("禁用调试")
        }
        Button(onClick = {myDpm.clearUserRestriction(myComponent, UserManager.DISALLOW_DEBUGGING_FEATURES)}) {
            Text("允许调试")
        }
        Text("限制定位：${strictState.getBoolean("no_config_location")}")
        Button(onClick = {myDpm.addUserRestriction(myComponent, UserManager.DISALLOW_CONFIG_LOCATION)}) {
            Text("禁用定位（需安卓9）")
        }
        Button(onClick = {myDpm.clearUserRestriction(myComponent, UserManager.DISALLOW_CONFIG_LOCATION)}) {
            Text("允许定位（需安卓9）")
        }
        Text("限制移动数据：${strictState.getBoolean("no_config_mobile_network")}")
        Button(onClick = {myDpm.addUserRestriction(myComponent, UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)}) {
            Text("禁用移动数据")
        }
        Button(onClick = {myDpm.clearUserRestriction(myComponent, UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS)}) {
            Text("允许移动数据")
        }
        Text("限制弹窗：${strictState.getBoolean("no_create_windows")}")
        Text("弹窗包括toast、通知和应用的“显示在其他应用上层”")
        Button(onClick = {myDpm.addUserRestriction(myComponent, UserManager.DISALLOW_CREATE_WINDOWS)}) {
            Text("禁止弹窗")
        }
        Button(onClick = {myDpm.clearUserRestriction(myComponent, UserManager.DISALLOW_CREATE_WINDOWS)}) {
            Text("允许弹窗")
        }
    }
}
