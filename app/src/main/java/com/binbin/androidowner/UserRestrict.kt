package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION
import android.os.UserManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private data class Restriction(
    val restriction:String,
    @StringRes val name:Int,
    val desc:String,
    @DrawableRes val ico:Int
)


@Composable
fun UserRestriction(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    var internetVisible by remember{ mutableStateOf(false) }
    var connectivityVisible by remember{ mutableStateOf(false) }
    var applicationVisible by remember{ mutableStateOf(false) }
    var mediaVisible by remember{ mutableStateOf(false) }
    var userVisible by remember{ mutableStateOf(false) }
    var otherVisible by remember{ mutableStateOf(false) }
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){typography.bodyMedium}else{typography.bodyLarge}
    LazyColumn(horizontalAlignment = Alignment.CenterHorizontally){
        items(1){
            Text(text = "打开开关后会禁用对应的功能",modifier = Modifier.padding(3.dp), style = bodyTextStyle)
            if(VERSION.SDK_INT<24){
                Text(text = "所有的用户限制都需要API24，你的设备低于API24，无法使用。", style = bodyTextStyle, color = colorScheme.error)
            }
            if(isProfileOwner(myDpm)){
                Text(text = "Profile owner无法使用部分功能", style = bodyTextStyle)
            }
            if(isWear){
                Text(text = "部分功能在手表上无效", style = typography.bodyMedium)
            }
        }

        items(1){ SectionTab("网络和互联网",{internetVisible}, { internetVisible=!internetVisible}) }
        items(RestrictionData().internet()){data->
            if(internetVisible){
                UserRestrictionItem(data.restriction,data.name,data.desc,data.ico)
            }
        }

        items(1){ SectionTab("更多连接",{connectivityVisible}) { connectivityVisible=!connectivityVisible } }
        items(RestrictionData().connectivity()){data->
            if(connectivityVisible){
                UserRestrictionItem(data.restriction,data.name,data.desc,data.ico)
            }
        }

        items(1){ SectionTab("应用",{applicationVisible}) { applicationVisible=!applicationVisible } }
        items(RestrictionData().application()){data->
            if(applicationVisible){
                UserRestrictionItem(data.restriction,data.name,data.desc,data.ico)
            }
        }

        items(1){ SectionTab("用户",{userVisible}) { userVisible=!userVisible } }
        items(RestrictionData().user()){data->
            if(userVisible){
                UserRestrictionItem(data.restriction,data.name,data.desc,data.ico)
            }
        }

        items(1){ SectionTab("媒体",{mediaVisible}) { mediaVisible=!mediaVisible } }
        items(RestrictionData().media()){data->
            if(mediaVisible){
                UserRestrictionItem(data.restriction,data.name,data.desc,data.ico)
            }
        }

        items(1){ SectionTab("其他",{otherVisible}) { otherVisible=!otherVisible } }
        items(RestrictionData().other()){data->
            if(otherVisible){
                UserRestrictionItem(data.restriction,data.name,data.desc,data.ico)
            }
        }

        items(1){
            Spacer(Modifier.padding(vertical = 5.dp))
            Column(modifier = Modifier.padding(horizontal = if(!isWear){10.dp}else{3.dp})) {
                if(VERSION.SDK_INT<24){ Text(text = "以下功能需要安卓7或以上：数据漫游、修改用户头像、更换壁纸", style = bodyTextStyle) }
                if(VERSION.SDK_INT<26){ Text(text = "以下功能需要安卓8或以上：蓝牙、自动填充服务、添加/移除工作资料", style = bodyTextStyle) }
                if(VERSION.SDK_INT<28){ Text(text = "以下功能需要安卓9或以上：飞行模式、位置信息、调整亮度、修改语言、修改日期时间、修改屏幕超时、打印、分享至工作应用、切换用户", style = bodyTextStyle) }
                if(VERSION.SDK_INT<29){ Text(text = "以下功能需要安卓10或以上：配置私人DNS、内容捕获、内容建议", style = bodyTextStyle) }
                if(VERSION.SDK_INT<31){ Text(text = "以下功能需要安卓12或以上：切换摄像头使用权限、切换麦克风使用权限", style = bodyTextStyle) }
                if(VERSION.SDK_INT<33){ Text(text = "以下功能需要安卓13或以上：添加WiFi配置、分享设备管理器配置的WiFi、WiFi共享", style = bodyTextStyle) }
                if(VERSION.SDK_INT<34){ Text(text = "以下功能需要安卓14或以上：2G信号、启用设备管理器、超宽频段无线电", style = bodyTextStyle) }
            }
            Spacer(Modifier.padding(vertical = 30.dp))
        }
    }
}

@Composable
fun SectionTab(txt:String,getSection:()->Boolean,setSection:()->Unit){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Text(
        text = txt,
        color = if(getSection()){
            colorScheme.onTertiaryContainer}else{
            colorScheme.onPrimaryContainer},
        textAlign = TextAlign.Center,
        style = if(!sharedPref.getBoolean("isWear",false)){typography.headlineMedium}else{typography.titleLarge},
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if(!sharedPref.getBoolean("isWear",false)){8.dp}else{4.dp},
                vertical = if(!sharedPref.getBoolean("isWear",false)){5.dp}else{2.dp})
            .clip(RoundedCornerShape(15.dp))
            .background(
                color = if (getSection()) {
                    colorScheme.tertiaryContainer.copy(alpha = 0.8F)
                } else {
                    colorScheme.primaryContainer.copy(alpha = 0.8F)
                }
            )
            .clickable(onClick = setSection)
            .padding(vertical = if(!sharedPref.getBoolean("isWear",false)){8.dp}else{3.dp})
    )
}

@Composable
private fun UserRestrictionItem(
    restriction:String, itemName:Int,
    restrictionDescription:String,
    leadIcon:Int
){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    var strictState by remember{ mutableStateOf(false) }
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Row(
        modifier = sections(colorScheme.secondaryContainer),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ){
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = if(isWear){Modifier.fillMaxWidth(0.65F)}else{Modifier.fillMaxWidth(0.8F)}
        ) {
            if(!isWear){
            Icon(
                painter = painterResource(leadIcon),
                contentDescription = null,
                modifier = Modifier.padding(start = 4.dp, end = 8.dp),
                tint = colorScheme.secondary
            )}
            Column{
                Text(
                    text = stringResource(itemName),
                    style = if(!isWear){typography.titleLarge}else{typography.titleMedium},
                    color = colorScheme.onSecondaryContainer,
                    fontWeight = if(isWear){ FontWeight.SemiBold }else{ FontWeight.Medium }
                )
                if(restrictionDescription!=""){
                    Text(text = restrictionDescription, color = colorScheme.onSecondaryContainer, style = if(isWear){typography.bodyMedium}else{typography.bodyLarge})
                }
            }
        }
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm)|| isProfileOwner(myDpm))){
            strictState = myDpm.getUserRestrictions(myComponent).getBoolean(restriction)
        }
        if(VERSION.SDK_INT>=24){
            Switch(
                checked = strictState,
                onCheckedChange = {
                    strictState=it
                    try{
                        if(strictState){
                            myDpm.addUserRestriction(myComponent,restriction)
                        }else{
                            myDpm.clearUserRestriction(myComponent,restriction)
                        }
                    }catch(e:SecurityException){
                        if(isProfileOwner(myDpm)){
                            Toast.makeText(myContext, "需要DeviceOwner", Toast.LENGTH_SHORT).show()
                        }
                    }
                    strictState = myDpm.getUserRestrictions(myComponent).getBoolean(restriction)
                },
                enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
                modifier = Modifier.padding(end = if(!isWear){5.dp}else{0.dp})
            )
        }
    }
}

private class RestrictionData{
    fun internet():List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        list += Restriction(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,R.string.config_mobile_network,"",R.drawable.signal_cellular_alt_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_WIFI,R.string.config_wifi,"",R.drawable.wifi_fill0)
        if(VERSION.SDK_INT>=24){list += Restriction(UserManager.DISALLOW_DATA_ROAMING,R.string.data_roaming,"",R.drawable.network_cell_fill0)}
        if(VERSION.SDK_INT>=34){
            list += Restriction(UserManager.DISALLOW_CELLULAR_2G,R.string.cellular_2g,"",R.drawable.network_cell_fill0)
            list += Restriction(UserManager.DISALLOW_ULTRA_WIDEBAND_RADIO,R.string.ultra_wideband_radio,"",R.drawable.android_fill0)
        }
        list += Restriction(UserManager.DISALLOW_CONFIG_WIFI,R.string.config_wifi,"",R.drawable.wifi_fill0)
        if(VERSION.SDK_INT>=33){
            list += Restriction(UserManager.DISALLOW_ADD_WIFI_CONFIG,R.string.add_wifi_conf,"",R.drawable.wifi_fill0)
            list += Restriction(UserManager.DISALLOW_CHANGE_WIFI_STATE,R.string.change_wifi_state,"",R.drawable.wifi_fill0)
            list += Restriction(UserManager.DISALLOW_WIFI_DIRECT,R.string.wifi_direct,"",R.drawable.wifi_tethering_fill0)
            list += Restriction(UserManager.DISALLOW_WIFI_TETHERING,R.string.wifi_tethering,"",R.drawable.wifi_tethering_fill0)
            list += Restriction(UserManager.DISALLOW_SHARING_ADMIN_CONFIGURED_WIFI,R.string.share_admin_wifi,"",R.drawable.share_fill0)
        }
        if(VERSION.SDK_INT>=23){ list += Restriction(UserManager.DISALLOW_NETWORK_RESET,R.string.network_reset,"",R.drawable.reset_wrench_fill0) }
        list += Restriction(UserManager.DISALLOW_CONFIG_TETHERING,R.string.config_tethering,"",R.drawable.wifi_tethering_fill0)
        list += Restriction(UserManager.DISALLOW_CONFIG_VPN,R.string.config_vpn,"",R.drawable.vpn_key_fill0)
        if(VERSION.SDK_INT>=29){list += Restriction(UserManager.DISALLOW_CONFIG_PRIVATE_DNS,R.string.config_private_dns,"",R.drawable.dns_fill0)}
        if(VERSION.SDK_INT>=28){list += Restriction(UserManager.DISALLOW_AIRPLANE_MODE,R.string.airplane_mode,"",R.drawable.airplanemode_active_fill0)}
        list += Restriction(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS,R.string.config_cell_broadcasts,"",R.drawable.cell_tower_fill0)
        list += Restriction(UserManager.DISALLOW_SMS,R.string.sms,"",R.drawable.sms_fill0)
        list += Restriction(UserManager.DISALLOW_OUTGOING_CALLS,R.string.outgoing_calls,"",R.drawable.phone_forwarded_fill0)
        return list
    }
    fun connectivity():List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        if(VERSION.SDK_INT>=26){
            list += Restriction(UserManager.DISALLOW_BLUETOOTH,R.string.bluetooth,"",R.drawable.bluetooth_fill0)
            list += Restriction(UserManager.DISALLOW_BLUETOOTH_SHARING,R.string.bt_share,"",R.drawable.bluetooth_searching_fill0)
        }
        list += Restriction(UserManager.DISALLOW_SHARE_LOCATION,R.string.share_location,"",R.drawable.location_on_fill0)
        if(VERSION.SDK_INT>=28){list += Restriction(UserManager.DISALLOW_CONFIG_LOCATION,R.string.config_location,"",R.drawable.location_on_fill0)}
        if(VERSION.SDK_INT>=22){list += Restriction(UserManager.DISALLOW_OUTGOING_BEAM,R.string.outgoing_beam,"",R.drawable.nfc_fill0)}
        list += Restriction(UserManager.DISALLOW_USB_FILE_TRANSFER,R.string.usb_file_transfer,"",R.drawable.usb_fill0)
        list += Restriction(UserManager.DISALLOW_MOUNT_PHYSICAL_MEDIA,R.string.mount_physical_media, "包括TF卡和USB-OTG",R.drawable.sd_card_fill0)
        if(VERSION.SDK_INT>=28){list += Restriction(UserManager.DISALLOW_PRINTING,R.string.printing,"",R.drawable.print_fill0)}
        return list
    }
    fun application():List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        list += Restriction(UserManager.DISALLOW_INSTALL_APPS,R.string.install_apps,"",R.drawable.android_fill0)
        if(VERSION.SDK_INT>=29){list += Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY,R.string.install_unknown_src_globally,"",R.drawable.android_fill0)}
        list += Restriction(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,R.string.inst_unknown_src,"",R.drawable.android_fill0)
        list += Restriction(UserManager.DISALLOW_UNINSTALL_APPS,R.string.uninstall_apps,"",R.drawable.delete_fill0)
        list += Restriction(UserManager.DISALLOW_APPS_CONTROL,R.string.apps_ctrl, "清空缓存/清空内部存储",R.drawable.apps_fill0)
        if(VERSION.SDK_INT>=34){ list += Restriction(UserManager.DISALLOW_CONFIG_DEFAULT_APPS,R.string.config_default_apps,"",R.drawable.apps_fill0) }
        return list
    }
    fun media():List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        if(VERSION.SDK_INT>=28){
            list += Restriction(UserManager.DISALLOW_CONFIG_BRIGHTNESS,R.string.config_brightness,"",R.drawable.brightness_5_fill0)
            list += Restriction(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT,R.string.config_scr_timeout,"",R.drawable.screen_lock_portrait_fill0)
            list += Restriction(UserManager.DISALLOW_AMBIENT_DISPLAY,R.string.ambient_display,"",R.drawable.brightness_5_fill0)
        }
        list += Restriction(UserManager.DISALLOW_ADJUST_VOLUME,R.string.adjust_volume,"",R.drawable.volume_up_fill0)
        list += Restriction(UserManager.DISALLOW_UNMUTE_MICROPHONE,R.string.unmute_microphone,"",R.drawable.mic_fill0)
        if(VERSION.SDK_INT>=31){
            list += Restriction(UserManager.DISALLOW_CAMERA_TOGGLE,R.string.camera_toggle,"",R.drawable.cameraswitch_fill0)
            list += Restriction(UserManager.DISALLOW_MICROPHONE_TOGGLE,R.string.microphone_toggle,"",R.drawable.mic_fill0)
        }
        return list
    }
    fun user():List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        list += Restriction(UserManager.DISALLOW_ADD_USER,R.string.add_user,"",R.drawable.account_circle_fill0)
        list += Restriction(UserManager.DISALLOW_REMOVE_USER,R.string.remove_user,"",R.drawable.account_circle_fill0)
        if(VERSION.SDK_INT>=28){list += Restriction(UserManager.DISALLOW_USER_SWITCH,R.string.switch_user,"",R.drawable.account_circle_fill0)}
        if(VERSION.SDK_INT>=24){list += Restriction(UserManager.DISALLOW_SET_USER_ICON,R.string.set_user_icon,"",R.drawable.account_circle_fill0)}
        list += Restriction(UserManager.DISALLOW_CROSS_PROFILE_COPY_PASTE,R.string.cross_profile_copy, "在不同用户和工作资料之间复制粘贴",R.drawable.content_paste_fill0)
        if(VERSION.SDK_INT>=26){
            list += Restriction(UserManager.DISALLOW_ADD_MANAGED_PROFILE,R.string.add_managed_profile,"",R.drawable.work_fill0)
            list += Restriction(UserManager.DISALLOW_REMOVE_MANAGED_PROFILE,R.string.remove_managed_profile,"",R.drawable.work_fill0)
        }
        if(VERSION.SDK_INT>=28){
            list += Restriction(UserManager.DISALLOW_SHARE_INTO_MANAGED_PROFILE,R.string.share_into_managed_profile,"",R.drawable.share_fill0)
            list += Restriction(UserManager.DISALLOW_UNIFIED_PASSWORD,R.string.unifiied_pwd,"",R.drawable.work_fill0)
        }
        return list
    }
    fun other():List<Restriction>{
        val list:MutableList<Restriction> = mutableListOf()
        if(VERSION.SDK_INT>=26){ list += Restriction(UserManager.DISALLOW_AUTOFILL,R.string.autofill, "",R.drawable.password_fill0) }
        list += Restriction(UserManager.DISALLOW_CONFIG_CREDENTIALS,R.string.config_credentials,"",R.drawable.android_fill0)
        if(VERSION.SDK_INT>=29){
            list += Restriction(UserManager.DISALLOW_CONTENT_CAPTURE,R.string.content_capture,"",R.drawable.android_fill0)
            list += Restriction(UserManager.DISALLOW_CONTENT_SUGGESTIONS,R.string.content_suggestions,"",R.drawable.android_fill0)
        }
        list += Restriction(UserManager.DISALLOW_CREATE_WINDOWS,R.string.create_windows, "可能包括Toast和浮动通知",R.drawable.web_asset)
        if(VERSION.SDK_INT>=24){list += Restriction(UserManager.DISALLOW_SET_WALLPAPER,R.string.set_wallpaper,"",R.drawable.wallpaper_fill0)}
        if(VERSION.SDK_INT>=34){ list += Restriction(UserManager.DISALLOW_GRANT_ADMIN,R.string.grant_admin,"",R.drawable.android_fill0) }
        if(VERSION.SDK_INT>=23){ list += Restriction(UserManager.DISALLOW_FUN,R.string.`fun`,"可能会影响谷歌商店的游戏",R.drawable.stadia_controller_fill0) }
        list += Restriction(UserManager.DISALLOW_MODIFY_ACCOUNTS,R.string.modify_accounts,"",R.drawable.manage_accounts_fill0)
        if(VERSION.SDK_INT>=28){
            list += Restriction(UserManager.DISALLOW_CONFIG_LOCALE,R.string.config_locale,"",R.drawable.language_fill0)
            list += Restriction(UserManager.DISALLOW_CONFIG_DATE_TIME,R.string.config_date_time,"",R.drawable.schedule_fill0)
        }
        if(VERSION.SDK_INT>=28){list += Restriction(UserManager.DISALLOW_SYSTEM_ERROR_DIALOGS,R.string.sys_err_dialog,"",R.drawable.android_fill0)}
        list += Restriction(UserManager.DISALLOW_FACTORY_RESET,R.string.factory_reset,"",R.drawable.android_fill0)
        if(VERSION.SDK_INT>=23){ list += Restriction(UserManager.DISALLOW_SAFE_BOOT,R.string.safe_boot,"",R.drawable.security_fill0) }
        list += Restriction(UserManager.DISALLOW_DEBUGGING_FEATURES,R.string.debug_features,"",R.drawable.adb_fill0)
        return list
    }
}
