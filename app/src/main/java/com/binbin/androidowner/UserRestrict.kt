package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build.VERSION
import android.os.UserManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun UserRestriction(myDpm: DevicePolicyManager, myComponent: ComponentName){
    val verticalScrolling = rememberScrollState()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(verticalScrolling)
            .padding(bottom = 20.dp)
    ) {
        Text("打开开关后会禁用对应的功能")
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,R.string.config_mobile_network,"",R.drawable.signal_cellular_alt_fill0,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_WIFI,R.string.config_wifi,"",R.drawable.wifi_fill0,myComponent, myDpm)
        if(VERSION.SDK_INT>=26){
            UserRestrictionItem(UserManager.DISALLOW_BLUETOOTH,R.string.bluetooth,"",R.drawable.bluetooth_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_BLUETOOTH_SHARING,R.string.bt_share,"",R.drawable.bluetooth_searching_fill0,myComponent, myDpm)
        }
        if(VERSION.SDK_INT>=28){
            UserRestrictionItem(UserManager.DISALLOW_AIRPLANE_MODE,R.string.airplane_mode,"",R.drawable.airplanemode_active_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_LOCATION,R.string.config_location,"",R.drawable.location_on_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_BRIGHTNESS,R.string.config_brightness,"",R.drawable.brightness_5_fill0,myComponent, myDpm)
        }
        UserRestrictionItem(UserManager.DISALLOW_DEBUGGING_FEATURES,R.string.debug_features,"",R.drawable.adb_fill0,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CREATE_WINDOWS,R.string.create_windows, stringResource(R.string.create_windows_description),R.drawable.web_asset,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_ADJUST_VOLUME,R.string.adjust_volume,"",R.drawable.volume_up_fill0,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_INSTALL_APPS,R.string.install_apps,"",R.drawable.android_fill0,myComponent, myDpm)
        if(VERSION.SDK_INT>=31){
            UserRestrictionItem(UserManager.DISALLOW_CAMERA_TOGGLE,R.string.camera_toggle,"",R.drawable.cameraswitch_fill0,myComponent, myDpm)
        }
        UserRestrictionItem(UserManager.DISALLOW_SMS,R.string.sms,"",R.drawable.sms_fill0,myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_APPS_CONTROL,R.string.apps_ctrl, stringResource(R.string.apps_ctrl_description),R.drawable.apps_fill0,myComponent, myDpm)
        if(VERSION.SDK_INT>=26){
            UserRestrictionItem(UserManager.DISALLOW_AUTOFILL,R.string.autofill, "",R.drawable.password_fill0,myComponent, myDpm)
        }
        UserRestrictionItem(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,R.string.inst_unknown_src,"",R.drawable.android_fill0,myComponent, myDpm)
        if(VERSION.SDK_INT<26){
            Text("以下功能需要安卓8或以上：蓝牙、自动填充服务")
        }
        if(VERSION.SDK_INT<28){
            Text("以下功能需要安卓9或以上：飞行模式、位置信息、调整亮度")
        }
        if(VERSION.SDK_INT<31){
            Text("以下功能需要安卓12或以上：切换相机")
        }
    }
}

@Composable
private fun UserRestrictionItem(restriction:String, itemName:Int, restrictionDescription:String, leadIcon:Int,myComponent: ComponentName, myDpm: DevicePolicyManager){
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    var strictState by remember{ mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(10))
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(leadIcon),
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 8.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = stringResource(itemName),
                    style = MaterialTheme.typography.titleLarge
                )
                if(restrictionDescription!=""){Text(restrictionDescription)}
            }
        }
        if(isdo&&VERSION.SDK_INT>=24){
            strictState = myDpm.getUserRestrictions(myComponent).getBoolean(restriction)
        }
        if(VERSION.SDK_INT>=24){
            Switch(
                checked = strictState,
                onCheckedChange = {
                    strictState=it
                    if(strictState){
                        myDpm.addUserRestriction(myComponent,restriction)
                    }else{
                        myDpm.clearUserRestriction(myComponent,restriction)
                    }
                    strictState = myDpm.getUserRestrictions(myComponent).getBoolean(restriction)

                },
                enabled = isdo
            )
        }else{

        }
    }
}
