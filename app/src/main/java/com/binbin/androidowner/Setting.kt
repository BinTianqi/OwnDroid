package com.binbin.androidowner

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AppSetting(){
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        val myContext = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(vertical = 6.dp)
        ) {
            Column(
                modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)
            ) {
                Text(text = "Android owner", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onPrimaryContainer)
                Text(text = "使用安卓的Device admin、Device owner 、Profile owner，全方位掌控你的设备")
                Spacer(Modifier.padding(vertical = 4.dp))
                Text("这个应用只在AOSP和LineageOS上测试过，不确保每个功能都在其它系统可用，尤其是国内的魔改系统。")
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
                    modifier = Modifier.padding(start = 6.dp, end = 8.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Column {
                    Text(text = "源代码", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(text = "https://github.com/BinTianqi/AndroidOwner", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(text = "欢迎提交issue、给小星星")
                }
            }
        }
    }
}

fun shareLink(inputContext:Context,link:String){
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Hello"),null)
}
