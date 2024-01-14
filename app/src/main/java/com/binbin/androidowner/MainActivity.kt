package com.binbin.androidowner

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
                MyScaffold(dpm,adminComponent)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun MyScaffold(mainDpm:DevicePolicyManager, mainComponent:ComponentName){
    val navCtrl = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Android Owner")}
            )
        }
    ) {
        NavHost(
            navController = navCtrl,
            startDestination = "HomePage",
            modifier = Modifier.padding(top = 80.dp)
        ){
            composable(route = "HomePage", content = { HomePage(navCtrl)})
            composable(route = "DeviceControl", content = { DeviceControl(mainDpm,mainComponent)})
            composable(route = "Permissions", content = { DpmPermissions(mainDpm,mainComponent)})
            composable(route = "UIControl", content = { UIControl(mainDpm,mainComponent)})
            composable(route = "ApplicationManage", content = { ApplicationManage(mainDpm,mainComponent)})
            composable(route = "UserRestriction", content = { UserRestriction(mainDpm,mainComponent)})
        }
    }
}

@Composable
fun HomePage(navCtrl:NavHostController){
    Column {
        Button(onClick = {navCtrl.navigate("Permissions")}) {
            Text("权限")
        }
        Button(onClick = {navCtrl.navigate("DeviceControl")}) {
            Text("设备控制")
        }
        Button(onClick = {navCtrl.navigate("UIControl")}) {
            Text("UI控制")
        }
        Button(onClick = {navCtrl.navigate("ApplicationManage")}) {
            Text("应用管理")
        }
        Button(onClick = {navCtrl.navigate("UserRestriction")}) {
            Text("用户限制")
        }
    }
}
