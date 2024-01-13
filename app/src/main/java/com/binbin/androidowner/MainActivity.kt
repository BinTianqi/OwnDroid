package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.os.UserManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.binbin.androidowner.ui.theme.AndroidOwnerTheme


class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = applicationContext
        val dpm = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        setContent {
            AndroidOwnerTheme {
                val adminComponent = ComponentName(context,MyDeviceAdminReceiver::class.java)
                val isdo = dpm.isDeviceOwnerApp("com.binbin.androidowner")
                val isda = dpm.isAdminActive(adminComponent)
                var wifimac = "Unknown"
                var pkgName by remember { mutableStateOf("com.mihoyo.yuanshen") }
                if(isdo){
                    wifimac = dpm.getWifiMacAddress(adminComponent).toString()
                }
                Column {
                    Column {
                        Text("Device Admin 功能")
                        Text("DeviceAdmin: $isda")
                        Button(onClick = {dpm.lockNow()}) {
                            Text("锁屏")
                        }
                        Button(
                            onClick = {},
                            modifier = Modifier.combinedClickable(
                                onClick = {},
                                onLongClick = {dpm.wipeData(0)}
                            )
                        ) {
                            Text("FACTORY_RESET!!!!! (长按)（未测试）", color = Color.Red)
                        }

                    }
                    Column {
                        Text("Device Owner 功能")
                        Text("DeviceOwner: $isdo")
                        Button(onClick = {dpm.reboot(adminComponent)}) {
                            Text("重启")
                        }
                        Button(onClick = {dpm.clearDeviceOwnerApp("com.binbin.androidowner")}) {
                            Text("不当Device Owner了")
                        }
                        Button(onClick = {dpm.setStatusBarDisabled(adminComponent,true)}) {
                            Text("隐藏状态栏")
                        }
                        Button(onClick = {dpm.setStatusBarDisabled(adminComponent,false)}) {
                            Text("显示状态栏")
                        }
                        Button(onClick = {dpm.addUserRestriction(adminComponent,UserManager.DISALLOW_BLUETOOTH)}) {
                            Text("禁用蓝牙（未测试）")
                        }
                        Text("WiFi MAC: $wifimac")
                        TextField(value = pkgName, onValueChange = {pkgName = it}, label = { Text("包名")})
                        Button(onClick = { dpm.setApplicationHidden(adminComponent,pkgName,true) }) {
                            Text("隐藏")
                        }
                        Button(onClick = { dpm.setApplicationHidden(adminComponent,pkgName,false) }) {
                            Text("显示")
                        }
                        Button(onClick = { dpm.setUninstallBlocked(adminComponent, pkgName, true) }) {
                            Text("禁止卸载")
                        }
                        Button(onClick = {dpm.setUninstallBlocked(adminComponent, pkgName, false)}) {
                            Text("允许卸载")
                        }
                    }
                }
            }
        }
    }
}
