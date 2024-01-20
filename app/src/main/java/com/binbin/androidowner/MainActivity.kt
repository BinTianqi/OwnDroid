package com.binbin.androidowner

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.binbin.androidowner.ui.theme.AndroidOwnerTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val context = applicationContext
        val dpm = context.getSystemService(DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val adminComponent = ComponentName(context,MyDeviceAdminReceiver::class.java)
        setContent {
            val sysUiCtrl = rememberSystemUiController()
            val useDarkIcon = !isSystemInDarkTheme()
            AndroidOwnerTheme {
                SideEffect {
                    sysUiCtrl.setNavigationBarColor(Color.Transparent,useDarkIcon)
                    sysUiCtrl.setStatusBarColor(Color.Transparent,useDarkIcon)
                }
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
    val backStackEntry by navCtrl.currentBackStackEntryAsState()
    val topBarNameMap = mapOf(
        "HomePage" to R.string.app_name,
        "DeviceControl" to R.string.device_ctrl,
        "Permissions" to R.string.permission,
        "UIControl" to R.string.ui_ctrl,
        "ApplicationManage" to R.string.app_manage,
        "UserRestriction" to R.string.user_restrict,
        "Password" to R.string.password
    )
    val topBarName = topBarNameMap[backStackEntry?.destination?.route]?: R.string.app_name
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(topBarName) ,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    if(topBarName!=R.string.app_name){
                        Icon(
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = {
                                    navCtrl.navigate("HomePage") {
                                        popUpTo(
                                            navCtrl.graph.findStartDestination().id
                                        ) { saveState = true }
                                    }
                                })
                                .padding(5.dp)
                        )
                    }
                }
            )
        }
    ) {
        NavHost(
            navController = navCtrl,
            startDestination = "HomePage",
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .imePadding()
        ){
            composable(route = "HomePage", content = { HomePage(navCtrl,mainDpm,mainComponent)})
            composable(route = "DeviceControl", content = { DeviceControl(mainDpm,mainComponent,mainContext)})
            composable(route = "Permissions", content = { DpmPermissions(mainDpm,mainComponent,mainContext,navCtrl)})
            composable(route = "ApplicationManage", content = { ApplicationManage(mainDpm,mainComponent,mainContext)})
            composable(route = "UserRestriction", content = { UserRestriction(mainDpm,mainComponent)})
            composable(route = "Password", content = { Password(mainDpm,mainComponent,mainContext)})
        }
    }
}

@Composable
fun HomePage(navCtrl:NavHostController,myDpm:DevicePolicyManager,myComponent:ComponentName){
    val isda = myDpm.isAdminActive(myComponent)
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    val activateType = if(isdo){"Device Owner"}else if(isda){"Device Admin"}else{""}
    val isActivated = if(isdo||isda){"已激活"}else{"未激活"}
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 5.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                .clickable(onClick = { navCtrl.navigate("Permissions") })
                .padding(horizontal = 5.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = if(isda){
                    painterResource(R.drawable.check_fill0)
                }else{
                    painterResource(R.drawable.block_fill0)
                },
                contentDescription = null,
                modifier = Modifier.padding(horizontal = 13.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Column {
                Text(
                    text = isActivated,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if(activateType!=""){
                    Text(
                        text = activateType,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
        HomePageItem(R.string.device_ctrl, R.drawable.mobile_phone_fill0, R.string.device_ctrl_desc, "DeviceControl", navCtrl)
        HomePageItem(R.string.app_manage, R.drawable.apps_fill0, R.string.apps_ctrl_description, "ApplicationManage", navCtrl)
        HomePageItem(R.string.user_restrict, R.drawable.manage_accounts_fill0, R.string.user_restrict_desc, "UserRestriction", navCtrl)
        HomePageItem(R.string.password, R.drawable.password_fill0,R.string.security_desc, "Password",navCtrl)
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
            .padding(6.dp),
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

@Composable
fun RadioButtonItem(
    text:String,
    selected:()->Boolean,
    operation:()->Unit
){
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        RadioButton(selected = selected(), onClick = operation)
        Text(text)
    }
}
