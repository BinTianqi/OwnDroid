package com.binbin.androidowner

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.binbin.androidowner.dpm.*
import com.binbin.androidowner.ui.theme.AndroidOwnerTheme
import com.binbin.androidowner.ui.Animations

lateinit var displayMetrics: DisplayMetrics
@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    private fun registerActivityResult(){
        getUserIcon = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { userIconUri = it.data?.data }
        getApk = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { apkUri = it.data?.data }
        getCaCert = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            uriToStream(applicationContext,it.data?.data){stream->
                caCert = stream.readBytes()
                if(caCert.size>50000){ Toast.makeText(applicationContext, "太大了", Toast.LENGTH_SHORT).show(); caCert = byteArrayOf() }
            }
        }
        createManagedProfile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode==Activity.RESULT_CANCELED){Toast.makeText(applicationContext, "用户已取消", Toast.LENGTH_SHORT).show()}
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        registerActivityResult()
        displayMetrics = applicationContext.resources.displayMetrics
        setContent {
            AndroidOwnerTheme {
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
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    NavHost(
        navController = navCtrl,
        startDestination = "HomePage",
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .background(color = if(isSystemInDarkTheme()) { colorScheme.background }else{ colorScheme.primary.copy(alpha = 0.05F) })
            .imePadding(),
        enterTransition = Animations().navHostEnterTransition,
        exitTransition = Animations().navHostExitTransition,
        popEnterTransition = Animations().navHostPopEnterTransition,
        popExitTransition = Animations().navHostPopExitTransition
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
    }
    LaunchedEffect(Unit){
        val profileInited = sharedPref.getBoolean("ManagedProfileActivated",false)
        val profileNotActivated = !profileInited&&isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)))
        if(profileNotActivated){
            myDpm.setProfileEnabled(myComponent)
            sharedPref.edit().putBoolean("ManagedProfileActivated",true).apply()
            Toast.makeText(myContext, myContext.getString(R.string.work_profile_activated), Toast.LENGTH_SHORT).show()
        }
    }
}

@Composable
private fun HomePage(navCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val activateType =
        if(isDeviceOwner(myDpm)){"Device Owner"}
        else if(isProfileOwner(myDpm)){
            stringResource(if(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)){R.string.work_profile_owner}else{R.string.profile_owner})
        }
        else if(myDpm.isAdminActive(myComponent)){"Device Admin"}else{""}
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 18.dp))
        Text(text = stringResource(R.string.app_name), style = typography.headlineLarge, modifier = Modifier.padding(start = 10.dp))
        Spacer(Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(15))
                .background(color = colorScheme.primaryContainer)
                .clickable(onClick = { navCtrl.navigate("Permissions") })
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.fillMaxWidth(0.06F))
            Icon(
                painter = painterResource(if(myDpm.isAdminActive(myComponent)){ R.drawable.check_circle_fill1 }else{ R.drawable.block_fill0 }),
                contentDescription = null,
                tint = colorScheme.primary
            )
            Spacer(modifier = Modifier.fillMaxWidth(0.05F))
            Column {
                Text(
                    text = if(myDpm.isAdminActive(myComponent)){"已激活"}else{"未激活"},
                    style = typography.headlineSmall,
                    color = colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                if(activateType!=""){ Text(text = activateType, color = colorScheme.onPrimaryContainer, modifier = Modifier.padding(start = 2.dp)) }
            }
        }
        HomePageItem(R.string.device_ctrl, R.drawable.mobile_phone_fill0, "SystemManage", navCtrl)
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm))||isProfileOwner(myDpm)){ HomePageItem(R.string.network, R.drawable.wifi_fill0, "Network",navCtrl) }
        if(
            (VERSION.SDK_INT<24&&!isDeviceOwner(myDpm))||(
                    VERSION.SDK_INT>=24&&(myDpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)||
                            (isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)))
            )
        ){
            HomePageItem(R.string.work_profile, R.drawable.work_fill0, "ManagedProfile",navCtrl)
        }
        HomePageItem(R.string.app_manage, R.drawable.apps_fill0, "ApplicationManage", navCtrl)
        if(VERSION.SDK_INT>=24){
            HomePageItem(R.string.user_restrict, R.drawable.manage_accounts_fill0, "UserRestriction", navCtrl)
        }
        HomePageItem(R.string.user_manage,R.drawable.account_circle_fill0,"UserManage",navCtrl)
        HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0, "Password",navCtrl)
        HomePageItem(R.string.setting, R.drawable.info_fill0, "AppSetting",navCtrl)
        Spacer(Modifier.padding(vertical = 20.dp))
    }
}

@Composable
fun HomePageItem(name:Int, imgVector:Int, navTo:String, myNav:NavHostController){
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25))
            .clickable(onClick = { myNav.navigate(navTo) })
            .padding(vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(modifier = Modifier.fillMaxWidth(0.08F))
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null,
            tint = colorScheme.onBackground
        )
        Spacer(modifier = Modifier.fillMaxWidth(0.05F))
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


@SuppressLint("ModifierFactoryExtensionFunction", "ComposableModifierFactory")
@Composable
@Stable
fun sections(bgColor:Color=colorScheme.primaryContainer,onClick:()->Unit={},clickable:Boolean=false):Modifier{
    val backgroundColor = if(isSystemInDarkTheme()){bgColor.copy(0.3F)}else{bgColor.copy(0.8F)}
    return Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick=onClick, enabled = clickable)
            .background(color = backgroundColor)
            .padding(vertical = 10.dp, horizontal = 10.dp)
    
}

