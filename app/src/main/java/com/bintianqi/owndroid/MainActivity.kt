package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.dpm.*
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.bintianqi.owndroid.ui.theme.SetDarkTheme
import com.bintianqi.owndroid.ui.theme.bgColor
import kotlinx.coroutines.delay

var backToHome = false
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        registerActivityResult(this)
        setContent {
            OwnDroidTheme {
                MyScaffold()
            }
        }
    }
}

@ExperimentalMaterial3Api
@Composable
fun MyScaffold(){
    val navCtrl = rememberNavController()
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val focusMgr = LocalFocusManager.current
    SetDarkTheme()
    LaunchedEffect(Unit){
        while(true){
            if(backToHome){ navCtrl.navigateUp(); backToHome=false }
            delay(200)
        }
    }
    NavHost(
        navController = navCtrl,
        startDestination = "HomePage",
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .imePadding()
            .pointerInput(Unit) {detectTapGestures(onTap = {focusMgr.clearFocus()})},
        enterTransition = Animations.navHostEnterTransition,
        exitTransition = Animations.navHostExitTransition,
        popEnterTransition = Animations.navHostPopEnterTransition,
        popExitTransition = Animations.navHostPopExitTransition
    ){
        composable(route = "HomePage", content = { HomePage(navCtrl)})
        composable(route = "SystemManage", content = { SystemManage(navCtrl) })
        composable(route = "ManagedProfile", content = {ManagedProfile(navCtrl)})
        composable(route = "Permissions", content = { DpmPermissions(navCtrl)})
        composable(route = "ApplicationManage", content = { ApplicationManage(navCtrl)})
        composable(route = "UserRestriction", content = { UserRestriction(navCtrl)})
        composable(route = "UserManage", content = { UserManage(navCtrl)})
        composable(route = "Password", content = { Password(navCtrl)})
        composable(route = "AppSetting", content = { AppSetting(navCtrl)})
        composable(route = "Network", content = {Network(navCtrl)})
        composable(route = "PackageSelector"){PackageSelector(navCtrl)}
        composable(route = "PermissionPicker"){PermissionPicker(navCtrl)}
    }
    LaunchedEffect(Unit){
        val profileInited = sharedPref.getBoolean("ManagedProfileActivated",false)
        val profileNotActivated = !profileInited&&isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)))
        if(profileNotActivated){
            myDpm.setProfileEnabled(myComponent)
            sharedPref.edit().putBoolean("ManagedProfileActivated",true).apply()
            Toast.makeText(myContext, R.string.work_profile_activated, Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun HomePage(navCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val activateType = stringResource(
        if(isDeviceOwner(myDpm)){R.string.device_owner}
        else if(isProfileOwner(myDpm)){
            if(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)){R.string.work_profile_owner}else{R.string.profile_owner}
        }
        else if(myDpm.isAdminActive(myComponent)){R.string.device_admin}else{R.string.click_to_activate}
    )
    Column(modifier = Modifier.statusBarsPadding().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 25.dp))
        Text(text = stringResource(R.string.app_name), style = typography.headlineLarge, modifier = Modifier.padding(start = 10.dp), color = colorScheme.onBackground)
        Spacer(Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(15))
                .background(color = colorScheme.primary)
                .clickable(onClick = {navCtrl.navigate("Permissions")})
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.padding(start = 22.dp))
            Icon(
                painter = painterResource(if(myDpm.isAdminActive(myComponent)){ R.drawable.check_circle_fill1 }else{ R.drawable.block_fill0 }),
                contentDescription = null,
                tint = colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.padding(start = 10.dp))
            Column {
                Text(
                    text = stringResource(if(myDpm.isAdminActive(myComponent)){R.string.activated}else{R.string.deactivated}),
                    style = typography.headlineSmall,
                    color = colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                if(activateType!=""){ Text(text = activateType, color = colorScheme.onPrimary) }
            }
        }
        HomePageItem(R.string.system_manage, R.drawable.mobile_phone_fill0, "SystemManage", navCtrl)
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm))||isProfileOwner(myDpm)){ HomePageItem(R.string.network, R.drawable.wifi_fill0, "Network",navCtrl) }
        if(
            (VERSION.SDK_INT<24&&!isDeviceOwner(myDpm))||(
                    VERSION.SDK_INT>=24&&(myDpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)||
                            (isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)))
            )
        ){
            HomePageItem(R.string.work_profile, R.drawable.work_fill0, "ManagedProfile",navCtrl)
        }
        HomePageItem(R.string.app_manager, R.drawable.apps_fill0, "ApplicationManage", navCtrl)
        if(VERSION.SDK_INT>=24){
            HomePageItem(R.string.user_restrict, R.drawable.person_off, "UserRestriction", navCtrl)
        }
        HomePageItem(R.string.user_manager,R.drawable.manage_accounts_fill0,"UserManage",navCtrl)
        HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0, "Password",navCtrl)
        HomePageItem(R.string.setting, R.drawable.settings_fill0, "AppSetting",navCtrl)
        Spacer(Modifier.padding(vertical = 20.dp))
    }
}

@Composable
fun HomePageItem(name:Int, imgVector:Int, navTo:String, myNav:NavHostController){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25))
            .clickable(onClick = {myNav.navigate(navTo)})
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.padding(start = 30.dp))
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null,
            tint = colorScheme.onBackground
        )
        Spacer(Modifier.padding(start = 15.dp))
        Column {
            Text(
                text = stringResource(name),
                style = typography.headlineSmall,
                color = colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }
    }
}
