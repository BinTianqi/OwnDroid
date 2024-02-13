package com.binbin.androidowner

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun AppSetting(navCtrl:NavHostController){
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        val myContext = LocalContext.current
        val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
        val isWear = sharedPref.getBoolean("isWear",false)
        val bodyTextStyle = if(isWear){typography.bodyMedium}else{typography.bodyLarge}
        Column(modifier = sections()) {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Wear", style = typography.titleLarge)
                Switch(
                    checked = isWear,
                    onCheckedChange = {
                        sharedPref.edit().putBoolean("isWear",!isWear).apply()
                        navCtrl.navigateUp()
                    }
                )
            }
            if(VERSION.SDK_INT>=32){
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 3.dp),horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "动态取色", style = typography.titleLarge)
                    Switch(
                        checked = sharedPref.getBoolean("dynamicColor",false),
                        onCheckedChange = {
                            sharedPref.edit().putBoolean("dynamicColor",!sharedPref.getBoolean("dynamicColor",false)).apply()
                            navCtrl.navigateUp()
                        }
                    )
                }
                Text(text = "打开或关闭动态取色需要重启应用", style = if(isWear){typography.bodyMedium}else{typography.bodyLarge})
            }
        }
        Column(modifier = sections()) {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
            ) {
                Text(text = "关于", style = typography.headlineSmall, color = colorScheme.onPrimaryContainer)
                Text(text = "使用安卓的Device admin、Device owner 、Profile owner，全方位掌控你的设备", style = bodyTextStyle)
                Spacer(Modifier.padding(vertical = 4.dp))
                Text(text = "这个应用只在AOSP和LineageOS上测试过，不确保每个功能都在其它系统可用，尤其是国内的魔改系统。", style = bodyTextStyle)
                Spacer(Modifier.padding(vertical = 4.dp))
                Text(text = "大部分功能都要Device owner权限", style = bodyTextStyle)
                Spacer(Modifier.padding(vertical = 2.dp))
                Text(text = "安卓版本越高，支持的功能越多", style = bodyTextStyle)
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { shareLink(myContext, "https://github.com/BinTianqi/AndroidOwner/blob/master/Guide.md") }
                    .padding(start = 8.dp, bottom = 4.dp)
            ){
                Icon(
                    painter = painterResource(id = R.drawable.open_in_new),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 6.dp, end = 10.dp),
                    tint = colorScheme.primary
                )
                Text(text = "使用教程", style = typography.titleLarge, color = colorScheme.onPrimaryContainer, modifier = Modifier.padding(bottom = 2.dp))
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { shareLink(myContext, "https://github.com/BinTianqi/AndroidOwner") }
                    .padding(start = 8.dp, bottom = 4.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.open_in_new),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 6.dp, end = 10.dp),
                    tint = colorScheme.primary
                )
                Text(text = "源代码", style = typography.titleLarge, color = colorScheme.onPrimaryContainer, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

fun shareLink(inputContext:Context,link:String){
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Hello"),null)
}
