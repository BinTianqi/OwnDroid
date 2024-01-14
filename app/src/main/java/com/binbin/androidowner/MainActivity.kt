package com.binbin.androidowner

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.binbin.androidowner.ui.theme.AndroidOwnerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val context = applicationContext
        val dpm = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context,MyDeviceAdminReceiver::class.java)
        setContent {
            AndroidOwnerTheme {
                MyScaffold(dpm,adminComponent,context)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyScaffold(mainDpm:DevicePolicyManager, mainComponent:ComponentName, mainContext:Context){
    val navCtrl = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Android Owner",color = MaterialTheme.colorScheme.onSurface)},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) {
        NavHost(
            navController = navCtrl,
            startDestination = "HomePage",
            modifier = Modifier.padding(top = 70.dp)
        ){
            composable(route = "HomePage", content = { HomePage(navCtrl)})
            composable(route = "DeviceControl", content = { DeviceControl(mainDpm,mainComponent)})
            composable(route = "Permissions", content = { DpmPermissions(mainDpm,mainComponent,mainContext)})
            composable(route = "UIControl", content = { UIControl(mainDpm,mainComponent)})
            composable(route = "ApplicationManage", content = { ApplicationManage(mainDpm,mainComponent)})
            composable(route = "UserRestriction", content = { UserRestriction(mainDpm,mainComponent)})
        }
    }
}

@Composable
fun HomePage(navCtrl:NavHostController){
    Column {
        HomePageItem(R.string.permission, R.drawable.info_fill0, R.string.permission_desc, "Permissions", navCtrl)
        HomePageItem(R.string.device_ctrl, R.drawable.info_fill0, R.string.device_ctrl_desc, "DeviceControl", navCtrl)
        HomePageItem(R.string.ui_ctrl, R.drawable.info_fill0, R.string.ui_ctrl_desc, "UIControl", navCtrl)
        HomePageItem(R.string.app_manage, R.drawable.info_fill0, R.string.apps_ctrl_description, "ApplicationManage", navCtrl)
        HomePageItem(R.string.user_restrict, R.drawable.info_fill0, R.string.user_restrict_desc, "UserRestriction", navCtrl)
    }
}

@Composable
fun HomePageItem(name:Int, imgVector:Int, description:Int, navTo:String, myNav:NavHostController){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(15))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = { myNav.navigate(navTo) })
            .padding(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = 10.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = stringResource(name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = stringResource(description),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}
