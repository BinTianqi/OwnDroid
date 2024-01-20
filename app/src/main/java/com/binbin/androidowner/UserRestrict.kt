package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Build.VERSION
import android.os.UserManager
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun UserRestriction(myDpm: DevicePolicyManager, myComponent: ComponentName){
    val verticalScrolling = rememberScrollState()
    var currentSection by remember{ mutableStateOf(0) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(verticalScrolling)
            .padding(bottom = 20.dp)
            .navigationBarsPadding()
    ) {
        Text("打开开关后会禁用对应的功能")

        SectionTab("网络和互联网",{currentSection==1}) { currentSection = if(currentSection==1){ 0 }else{ 1 } }
        if(currentSection==1){
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,R.string.config_mobile_network,"",R.drawable.signal_cellular_alt_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=24){UserRestrictionItem(UserManager.DISALLOW_DATA_ROAMING,R.string.data_roaming,"",R.drawable.network_cell_fill0,myComponent, myDpm)}
            if(VERSION.SDK_INT>=34){
                UserRestrictionItem(UserManager.DISALLOW_CELLULAR_2G,R.string.cellular_2g,"",R.drawable.network_cell_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_ULTRA_WIDEBAND_RADIO,R.string.ultra_wideband_radio,"",R.drawable.android_fill0,myComponent, myDpm)
            }
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_WIFI,R.string.config_wifi,"",R.drawable.wifi_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=33){
                UserRestrictionItem(UserManager.DISALLOW_ADD_WIFI_CONFIG,R.string.add_wifi_conf,"",R.drawable.wifi_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_CHANGE_WIFI_STATE,R.string.change_wifi_state,"",R.drawable.wifi_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_WIFI_DIRECT,R.string.wifi_direct,"",R.drawable.wifi_tethering_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_WIFI_TETHERING,R.string.wifi_tethering,"",R.drawable.wifi_tethering_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI,R.string.share_admin_wifi,"",R.drawable.share_fill0,myComponent, myDpm)
            }
            UserRestrictionItem(UserManager.DISALLOW_NETWORK_RESET,R.string.network_reset,"",R.drawable.reset_wrench_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_TETHERING,R.string.config_tethering,"",R.drawable.wifi_tethering_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_VPN,R.string.config_vpn,"",R.drawable.vpn_key_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=29){UserRestrictionItem(UserManager.DISALLOW_CONFIG_PRIVATE_DNS,R.string.config_private_dns,"",R.drawable.dns_fill0,myComponent, myDpm)}

            if(VERSION.SDK_INT>=28){ UserRestrictionItem(UserManager.DISALLOW_AIRPLANE_MODE,R.string.airplane_mode,"",R.drawable.airplanemode_active_fill0,myComponent, myDpm) }
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS,R.string.config_cell_broadcasts,"",R.drawable.cell_tower_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_SMS,R.string.sms,"",R.drawable.sms_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_OUTGOING_CALLS,R.string.outgoing_calls,"",R.drawable.phone_forwarded_fill0,myComponent, myDpm)
        }

        SectionTab("其他连接",{currentSection==6}) { currentSection = if(currentSection==6){ 0 }else{ 6 } }
        if(currentSection==6){
            if(VERSION.SDK_INT>=26){
                UserRestrictionItem(UserManager.DISALLOW_BLUETOOTH,R.string.bluetooth,"",R.drawable.bluetooth_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_BLUETOOTH_SHARING,R.string.bt_share,"",R.drawable.bluetooth_searching_fill0,myComponent, myDpm)
            }
            UserRestrictionItem(UserManager.DISALLOW_SHARE_LOCATION,R.string.share_location,"",R.drawable.location_on_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=28){UserRestrictionItem(UserManager.DISALLOW_CONFIG_LOCATION,R.string.config_location,"",R.drawable.location_on_fill0,myComponent, myDpm)}
            UserRestrictionItem(UserManager.DISALLOW_OUTGOING_BEAM,R.string.outgoing_beam,"",R.drawable.nfc_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_USB_FILE_TRANSFER,R.string.usb_file_transfer,"",R.drawable.usb_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,R.string.mount_physical_media, stringResource(R.string.mount_phisical_media_desc),R.drawable.sd_card_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=28){UserRestrictionItem(UserManager.DISALLOW_PRINTING,R.string.printing,"",R.drawable.print_fill0,myComponent, myDpm)}
        }
        SectionTab("应用",{currentSection==2}) { currentSection = if(currentSection==2){ 0 }else{ 2 } }
        if(currentSection==2){
            UserRestrictionItem(UserManager.DISALLOW_INSTALL_APPS,R.string.install_apps,"",R.drawable.android_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=29){UserRestrictionItem(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,R.string.install_unknown_src_globally,"",R.drawable.android_fill0,myComponent, myDpm)}
            UserRestrictionItem(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,R.string.inst_unknown_src,"",R.drawable.android_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_UNINSTALL_APPS,R.string.uninstall_apps,"",R.drawable.delete_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_APPS_CONTROL,R.string.apps_ctrl, stringResource(R.string.apps_ctrl_description),R.drawable.apps_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=34){ UserRestrictionItem(UserManager.DISALLOW_CONFIG_DEFAULT_APPS,R.string.config_default_apps,"",R.drawable.apps_fill0,myComponent, myDpm) }
        }

        SectionTab("显示与音量",{currentSection==3}) { currentSection = if(currentSection==3){ 0 }else{ 3 } }
        if(currentSection==3){
            if(VERSION.SDK_INT>=28){
                UserRestrictionItem(UserManager.DISALLOW_CONFIG_BRIGHTNESS,R.string.config_brightness,"",R.drawable.brightness_5_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT,R.string.config_scr_timeout,"",R.drawable.screen_lock_portrait_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_AMBIENT_DISPLAY,R.string.ambient_display,"",R.drawable.brightness_5_fill0,myComponent, myDpm)
            }
            UserRestrictionItem(UserManager.DISALLOW_ADJUST_VOLUME,R.string.adjust_volume,"",R.drawable.volume_up_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_UNMUTE_MICROPHONE,R.string.unmute_microphone,"",R.drawable.mic_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=31){
                UserRestrictionItem(UserManager.DISALLOW_CAMERA_TOGGLE,R.string.camera_toggle,"",R.drawable.cameraswitch_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_MICROPHONE_TOGGLE,R.string.microphone_toggle,"",R.drawable.mic_fill0,myComponent, myDpm)
            }
        }

        SectionTab("用户与工作资料",{currentSection==4}) { currentSection = if(currentSection==4){ 0 }else{ 4 } }
        if(currentSection==4){
            UserRestrictionItem(UserManager.DISALLOW_ADD_USER,R.string.add_user,"",R.drawable.account_circle_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_REMOVE_USER,R.string.remove_user,"",R.drawable.account_circle_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=28){UserRestrictionItem(UserManager.DISALLOW_USER_SWITCH,R.string.switch_user,"",R.drawable.account_circle_fill0,myComponent, myDpm)}
            if(VERSION.SDK_INT>=24){UserRestrictionItem(UserManager.DISALLOW_SET_USER_ICON,R.string.set_user_icon,"",R.drawable.account_circle_fill0,myComponent, myDpm)}
            UserRestrictionItem(UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE,R.string.cross_profile_copy, stringResource(R.string.cross_profile_copy_desc),R.drawable.content_paste_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=26){
                UserRestrictionItem(UserManager.DISALLOW_ADD_MANAGED_PROFILE,R.string.add_managed_profile,"",R.drawable.work_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_REMOVE_MANAGED_PROFILE,R.string.remove_managed_profile,"",R.drawable.work_fill0,myComponent, myDpm)
            }
            if(VERSION.SDK_INT>=28){
                UserRestrictionItem(UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE,R.string.share_into_managed_profile,"",R.drawable.share_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_UNIFIED_PASSWORD,R.string.unifiied_pwd,"",R.drawable.work_fill0,myComponent, myDpm)
            }
        }

        SectionTab("杂项",{currentSection==5}) { currentSection = if(currentSection==5){ 0 }else{ 5 } }
        if(currentSection==5){
            if(VERSION.SDK_INT>=26){ UserRestrictionItem(UserManager.DISALLOW_AUTOFILL,R.string.autofill, "",R.drawable.password_fill0,myComponent, myDpm) }
            UserRestrictionItem(UserManager.DISALLOW_CONFIG_CREDENTIALS,R.string.config_credentials,"",R.drawable.android_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=29){
                UserRestrictionItem(UserManager.DISALLOW_CONTENT_CAPTURE,R.string.content_capture,"",R.drawable.android_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_CONTENT_SUGGESTIONS,R.string.content_suggestions,"",R.drawable.android_fill0,myComponent, myDpm)
            }
            UserRestrictionItem(UserManager.DISALLOW_CREATE_WINDOWS,R.string.create_windows, stringResource(R.string.create_windows_description),R.drawable.web_asset,myComponent, myDpm)
            if(VERSION.SDK_INT>=24){UserRestrictionItem(UserManager.DISALLOW_SET_WALLPAPER,R.string.set_wallpaper,"",R.drawable.wallpaper_fill0,myComponent, myDpm)}
            if(VERSION.SDK_INT>=34){ UserRestrictionItem(UserManager.DISALLOW_GRANT_ADMIN,R.string.grant_admin,"",R.drawable.android_fill0,myComponent, myDpm) }
            UserRestrictionItem(UserManager.DISALLOW_FUN,R.string.`fun`,"可能会影响谷歌商店的游戏",R.drawable.stadia_controller_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_MODIFY_ACCOUNTS,R.string.modify_accounts,"",R.drawable.manage_accounts_fill0,myComponent, myDpm)
            if(VERSION.SDK_INT>=28){
                UserRestrictionItem(UserManager.DISALLOW_CONFIG_LOCALE,R.string.config_locale,"",R.drawable.language_fill0,myComponent, myDpm)
                UserRestrictionItem(UserManager.DISALLOW_CONFIG_DATE_TIME,R.string.config_date_time,"",R.drawable.schedule_fill0,myComponent, myDpm)
            }
            if(VERSION.SDK_INT>=28){UserRestrictionItem(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS,R.string.sys_err_dialog,"",R.drawable.android_fill0,myComponent, myDpm)}
            UserRestrictionItem(UserManager.DISALLOW_FACTORY_RESET,R.string.factory_reset,"",R.drawable.android_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_SAFE_BOOT,R.string.safe_boot,"",R.drawable.android_fill0,myComponent, myDpm)
            UserRestrictionItem(UserManager.DISALLOW_DEBUGGING_FEATURES,R.string.debug_features,"",R.drawable.adb_fill0,myComponent, myDpm)
        }

        if(VERSION.SDK_INT<24){
            Text("以下功能需要安卓7或以上：数据漫游、修改用户头像、更换壁纸")
        }
        if(VERSION.SDK_INT<26){
            Text("以下功能需要安卓8或以上：蓝牙、自动填充服务、添加/移除工作资料")
        }
        if(VERSION.SDK_INT<28){
            Text("以下功能需要安卓9或以上：飞行模式、位置信息、调整亮度、修改语言、修改日期时间、修改屏幕超时、打印、分享至工作应用、切换用户")
        }
        if(VERSION.SDK_INT<29){
            Text("以下功能需要安卓10或以上：配置私人DNS、内容捕获、内容建议")
        }
        if(VERSION.SDK_INT<31){
            Text("以下功能需要安卓12或以上：切换摄像头使用权限")
        }
        if(VERSION.SDK_INT<33){
            Text("以下功能需要安卓13或以上：添加WiFi配置、分享设备管理器配置的WiFi、WiFi共享")
        }
        if(VERSION.SDK_INT<34){
            Text("以下功能需要安卓14或以上：2G信号、启用设备管理器、超宽频段无线电")
        }
    }
}

@Composable
fun SectionTab(txt:String,getSection:()->Boolean,setSection:()->Unit){
    Text(
        text = txt,
        color = if(getSection()){MaterialTheme.colorScheme.onTertiaryContainer}else{MaterialTheme.colorScheme.onPrimaryContainer},
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.headlineMedium,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(15))
            .background(color = if(getSection()){MaterialTheme.colorScheme.tertiaryContainer}else{MaterialTheme.colorScheme.primaryContainer})
            .clickable(onClick = setSection)
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun UserRestrictionItem(restriction:String, itemName:Int, restrictionDescription:String, leadIcon:Int,myComponent: ComponentName, myDpm: DevicePolicyManager){
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    var strictState by remember{ mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(20))
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
                tint = MaterialTheme.colorScheme.secondary
            )
            Column(
                modifier = Modifier.align(Alignment.CenterVertically)
            ) {
                Text(
                    text = stringResource(itemName),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if(restrictionDescription!=""){Text(text = restrictionDescription, color = MaterialTheme.colorScheme.onSecondaryContainer)}
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
        }
    }
}
