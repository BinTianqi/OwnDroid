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
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
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


@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContent {
            val sysUiCtrl = rememberSystemUiController()
            val useDarkIcon = !isSystemInDarkTheme()
            AndroidOwnerTheme {
                SideEffect {
                    sysUiCtrl.setNavigationBarColor(Color.Transparent,useDarkIcon)
                    sysUiCtrl.setStatusBarColor(Color.Transparent,useDarkIcon)
                }
                MyScaffold()
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MyScaffold(){
    val focusMgr = LocalFocusManager.current
    val navCtrl = rememberNavController()
    val backStackEntry by navCtrl.currentBackStackEntryAsState()
    val topBarNameMap = mapOf(
        "HomePage" to R.string.app_name,
        "DeviceControl" to R.string.device_ctrl,
        "Permissions" to R.string.permission,
        "UserManage" to R.string.user_manage,
        "ApplicationManage" to R.string.app_manage,
        "UserRestriction" to R.string.user_restrict,
        "Password" to R.string.password,
        "AppSetting" to R.string.setting
    )
    val topBarName = topBarNameMap[backStackEntry?.destination?.route]?: R.string.app_name
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Scaffold(
        topBar = {
            if(!sharedPref.getBoolean("isWear",false)){
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
                                    focusMgr.clearFocus()
                                })
                                .padding(5.dp)
                        )
                    }
                }
            )
            }
        },
        floatingActionButton = {
            if(sharedPref.getBoolean("isWear",false)&&topBarName!=R.string.app_name){
                FloatingActionButton(
                    onClick = {
                        navCtrl.navigate("HomePage") {
                            popUpTo(
                                navCtrl.graph.findStartDestination().id
                            ) { saveState = true }
                        }
                        focusMgr.clearFocus()
                    },
                    modifier = Modifier.size(35.dp),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.tertiary
                ) {
                    Icon(imageVector = Icons.Outlined.Home, contentDescription = null)
                }
            }
        }
    ) {
        NavHost(
            navController = navCtrl,
            startDestination = "HomePage",
            modifier = Modifier
                .padding(top = it.calculateTopPadding())
                .imePadding()
        ){
            composable(route = "HomePage", content = { HomePage(navCtrl)})
            composable(route = "DeviceControl", content = { DeviceControl()})
            composable(route = "Permissions", content = { DpmPermissions(navCtrl)})
            composable(route = "ApplicationManage", content = { ApplicationManage()})
            composable(route = "UserRestriction", content = { UserRestriction()})
            composable(route = "UserManage", content = { UserManage()})
            composable(route = "Password", content = { Password()})
            composable(route = "AppSetting", content = { AppSetting(navCtrl)})
        }
    }
}

@Composable
fun HomePage(navCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val isda = myDpm.isAdminActive(myComponent)
    val isdo = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
    val activateType = if(isDeviceOwner(myDpm)){"Device Owner"}else if(isProfileOwner(myDpm)){"Profile Owner"}else if(isda){"Device Admin"}else{""}
    val isActivated = if(isdo||isda){"已激活"}else{"未激活"}
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        if(sharedPref.getBoolean("isWear",false)){
            Spacer(Modifier.padding(vertical = 3.dp))
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (!sharedPref.getBoolean("isWear", false)) { 5.dp } else { 2.dp }, horizontal = if (!sharedPref.getBoolean("isWear", false)) { 8.dp } else { 4.dp })
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.tertiaryContainer)
                .clickable(onClick = { navCtrl.navigate("Permissions") })
                .padding(
                    horizontal = 5.dp,
                    vertical = if (!sharedPref.getBoolean("isWear", false)) { 14.dp } else { 2.dp }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = if(isda){
                    painterResource(R.drawable.check_fill0)
                }else{
                    painterResource(R.drawable.block_fill0)
                },
                contentDescription = null,
                modifier = Modifier.padding(horizontal = if(!sharedPref.getBoolean("isWear",false)){10.dp}else{6.dp}),
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
        HomePageItem(R.string.user_manage,R.drawable.account_circle_fill0,R.string.user_manage_desc,"UserManage",navCtrl)
        HomePageItem(R.string.password, R.drawable.password_fill0,R.string.security_desc, "Password",navCtrl)
        HomePageItem(R.string.setting, R.drawable.info_fill0, R.string.setting_desc, "AppSetting",navCtrl)
        Spacer(Modifier.padding(vertical = 20.dp))
    }
}

@Composable
fun HomePageItem(name:Int, imgVector:Int, description:Int, navTo:String, myNav:NavHostController){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (!sharedPref.getBoolean("isWear", false)) { 5.dp } else { 2.dp }, horizontal = if (!sharedPref.getBoolean("isWear", false)) { 8.dp } else { 4.dp })
            .clip(RoundedCornerShape(15))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .clickable(onClick = { myNav.navigate(navTo) })
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = if(!sharedPref.getBoolean("isWear",false)){10.dp}else{6.dp}),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = stringResource(name),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if(!sharedPref.getBoolean("isWear",false)){
                Text(
                    text = stringResource(description),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
fun RadioButtonItem(
    text:String,
    selected:()->Boolean,
    operation:()->Unit
){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = if(sharedPref.getBoolean("isWear",false)){3.dp}else{0.dp})
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        RadioButton(selected = selected(), onClick = operation,modifier=if(sharedPref.getBoolean("isWear",false)){Modifier.size(28.dp)}else{Modifier})
        Text(text = text, style = if(!sharedPref.getBoolean("isWear",false)){MaterialTheme.typography.bodyLarge}else{MaterialTheme.typography.bodyMedium})
    }
}

fun isDeviceOwner(dpm:DevicePolicyManager): Boolean {
    return dpm.isDeviceOwnerApp("com.binbin.androidowner")
}

fun isProfileOwner(dpm:DevicePolicyManager): Boolean {
    return dpm.isProfileOwnerApp("com.binbin.androidowner")
}

@SuppressLint("ModifierFactoryExtensionFunction")
@Composable
fun sections(bgColor:Color=MaterialTheme.colorScheme.primaryContainer):Modifier{
    return if(!LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("isWear",false)){
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(color = bgColor)
            .padding(vertical = 6.dp, horizontal = 10.dp)
    }else{
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(color = bgColor)
            .padding(vertical = 2.dp, horizontal = 3.dp)
    }
}
