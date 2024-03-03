package com.binbin.androidowner

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.os.Bundle
import android.os.UserManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
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
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.binbin.androidowner.ui.theme.AndroidOwnerTheme
import com.binbin.androidowner.ui.theme.Animations
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream


lateinit var getCaCert: ActivityResultLauncher<Intent>
lateinit var createUser:ActivityResultLauncher<Intent>
lateinit var createManagedProfile:ActivityResultLauncher<Intent>
lateinit var getApk:ActivityResultLauncher<Intent>
lateinit var getUserIcon:ActivityResultLauncher<Intent>
var userIconUri:Uri? = null
var apkUri: Uri? = null
var caCert = byteArrayOf()

@ExperimentalMaterial3Api
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        getUserIcon = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            userIconUri = it.data?.data
            if(userIconUri==null){ Toast.makeText(applicationContext, "空URI", Toast.LENGTH_SHORT).show() }
        }
        getApk = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            apkUri = it.data?.data
            if(apkUri==null){ Toast.makeText(applicationContext, "空URI", Toast.LENGTH_SHORT).show() }
        }
        getCaCert = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            uriToStream(applicationContext,it.data?.data){stream->
                caCert = stream.readBytes()
                if(caCert.size>50000){ Toast.makeText(applicationContext, "太大了", Toast.LENGTH_SHORT).show(); caCert = byteArrayOf() }
            }
        }
        createUser = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when(it.resultCode){
                Activity.RESULT_OK->Toast.makeText(applicationContext, "成功", Toast.LENGTH_SHORT).show()
                Activity.RESULT_CANCELED->Toast.makeText(applicationContext, "用户太多了", Toast.LENGTH_SHORT).show()
                UserManager.USER_CREATION_FAILED_NOT_PERMITTED->Toast.makeText(applicationContext, "不是管理员用户", Toast.LENGTH_SHORT).show()
                UserManager.USER_CREATION_FAILED_NO_MORE_USERS->Toast.makeText(applicationContext, "用户太多了", Toast.LENGTH_SHORT).show()
                else->Toast.makeText(applicationContext, "创建用户结果未知", Toast.LENGTH_SHORT).show()
            }
        }
        createManagedProfile = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if(it.resultCode==Activity.RESULT_CANCELED){Toast.makeText(applicationContext, "用户已取消", Toast.LENGTH_SHORT).show()}
        }
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
    val focusMgr = LocalFocusManager.current
    val navCtrl = rememberNavController()
    val backStackEntry by navCtrl.currentBackStackEntryAsState()
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val topBarNameMap = mapOf(
        "HomePage" to R.string.app_name,
        "DeviceControl" to R.string.device_ctrl,
        "Network" to R.string.network,
        "ManagedProfile" to R.string.work_profile,
        "Permissions" to R.string.permission,
        "UserManage" to R.string.user_manage,
        "ApplicationManage" to R.string.app_manage,
        "UserRestriction" to R.string.user_restrict,
        "Password" to R.string.password_and_keyguard,
        "AppSetting" to R.string.setting,
        "ShizukuActivate" to R.string.shizuku
    )
    val topBarName = topBarNameMap[backStackEntry?.destination?.route]?: R.string.app_name
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Scaffold(
        topBar = {
            if(!sharedPref.getBoolean("isWear",false)){
            TopAppBar(
                title = {Text(text = stringResource(topBarName) , color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(bottom = 2.dp))},
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                navigationIcon = {
                    AnimatedVisibility(
                        visible = topBarName!=R.string.app_name,
                        enter = Animations(myContext).navIconEnterTransition,
                        exit = Animations(myContext).navIconExitTransition
                    ){
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable{ navCtrl.navigateUp(); focusMgr.clearFocus() }
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
                        focusMgr.clearFocus()
                        navCtrl.navigateUp()
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
        val profileInited = sharedPref.getBoolean("ManagedProfileActivated",false)
        var inited by remember{mutableStateOf(false)}
        val jumpToActivateProfile = !profileInited&&isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)))
        NavHost(
            navController = navCtrl,
            startDestination = "HomePage",
            modifier = Modifier.fillMaxSize().padding(top = it.calculateTopPadding()).imePadding(),
            enterTransition = Animations(myContext).navHostEnterTransition,
            exitTransition = Animations(myContext).navHostExitTransition,
            popEnterTransition = Animations(myContext).navHostPopEnterTransition,
            popExitTransition = Animations(myContext).navHostPopExitTransition
        ){
            composable(route = "HomePage", content = { HomePage(navCtrl)})
            composable(route = "DeviceControl", content = { SystemManage()})
            composable(route = "ManagedProfile", content = {ManagedProfile()})
            composable(route = "Permissions", content = { DpmPermissions(navCtrl)})
            composable(route = "ApplicationManage", content = { ApplicationManage()})
            composable(route = "UserRestriction", content = { UserRestriction()})
            composable(route = "UserManage", content = { UserManage()})
            composable(route = "Password", content = { Password()})
            composable(route = "AppSetting", content = { AppSetting(navCtrl)})
            composable(route = "Network", content = {Network()})
            composable(route = "ActivateManagedProfile", content = {ActivateManagedProfile(navCtrl)})
            composable(route = "ShizukuActivate", content = {ShizukuActivate()})
        }
        if(!inited&&jumpToActivateProfile){navCtrl.navigate("ActivateManagedProfile");inited=true}
    }
}

@Composable
fun HomePage(navCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val activateType =
        if(isDeviceOwner(myDpm)){"Device Owner"}
        else if(isProfileOwner(myDpm)){if(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)){"工作资料"}else{"Profile Owner"}}
        else if(myDpm.isAdminActive(myComponent)){"Device Admin"}
        else{""}
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    caCert = byteArrayOf()
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
        if(isWear){ Spacer(Modifier.padding(vertical = 3.dp)) }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = if (!isWear) { 5.dp } else { 2.dp }, horizontal = if (!isWear) { 8.dp } else { 4.dp })
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7F))
                .clickable(onClick = { navCtrl.navigate("Permissions") })
                .padding(
                    horizontal = 5.dp,
                    vertical = if (!isWear) { 14.dp } else { 2.dp }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(if(myDpm.isAdminActive(myComponent)){ R.drawable.check_fill0 }else{ R.drawable.block_fill0 }),
                contentDescription = null,
                modifier = Modifier.padding(horizontal = if(!isWear){10.dp}else{6.dp}),
                tint = MaterialTheme.colorScheme.tertiary
            )
            Column {
                Text(
                    text = if(isDeviceOwner(myDpm)||myDpm.isAdminActive(myComponent)||isProfileOwner(myDpm)){"已激活"}else{"未激活"},
                    style = typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                if(activateType!=""){ Text(activateType) }
            }
        }
        HomePageItem(R.string.device_ctrl, R.drawable.mobile_phone_fill0, "DeviceControl", navCtrl)
        if(VERSION.SDK_INT>=24){HomePageItem(R.string.network, R.drawable.wifi_fill0, "Network",navCtrl)}
        HomePageItem(R.string.work_profile, R.drawable.work_fill0, "ManagedProfile",navCtrl)
        HomePageItem(R.string.app_manage, R.drawable.apps_fill0, "ApplicationManage", navCtrl)
        HomePageItem(R.string.user_restrict, R.drawable.manage_accounts_fill0, "UserRestriction", navCtrl)
        HomePageItem(R.string.user_manage,R.drawable.account_circle_fill0,"UserManage",navCtrl)
        HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0, "Password",navCtrl)
        HomePageItem(R.string.setting, R.drawable.info_fill0, "AppSetting",navCtrl)
        Spacer(Modifier.padding(vertical = 20.dp))
    }
}

@Composable
fun HomePageItem(name:Int, imgVector:Int, navTo:String, myNav:NavHostController){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear", false)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (!isWear) { 4.dp } else { 2.dp }, horizontal = if (!isWear) { 7.dp } else { 4.dp })
            .clip(RoundedCornerShape(15))
            .background(color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7F))
            .clickable(onClick = { myNav.navigate(navTo) })
            .padding(vertical = if(isWear){6.dp}else{10.dp}, horizontal = 6.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null,
            modifier = Modifier.padding(horizontal = if(!sharedPref.getBoolean("isWear",false)){12.dp}else{6.dp}),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = stringResource(name),
                style = typography.headlineSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.padding(top = 2.dp))
        }
    }
}

@Composable
fun RadioButtonItem(
    text:String,
    selected:()->Boolean,
    operation:()->Unit,
    textColor:Color = MaterialTheme.colorScheme.onBackground
){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = if(isWear){3.dp}else{0.dp})
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        RadioButton(selected = selected(), onClick = operation,modifier=if(isWear){Modifier.size(28.dp)}else{Modifier})
        Text(text = text, style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium}, color = textColor,
            modifier = Modifier.padding(bottom = 2.dp))
    }
}
@Composable
fun CheckBoxItem(
    text:String,
    checked:()->Boolean,
    operation:()->Unit,
    textColor:Color = MaterialTheme.colorScheme.onBackground
){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Row(verticalAlignment = Alignment.CenterVertically,modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = if(isWear){3.dp}else{0.dp})
        .clip(RoundedCornerShape(25))
        .clickable(onClick = operation)
    ) {
        Checkbox(
            checked = checked(),
            onCheckedChange = {operation()},
            modifier=if(isWear){Modifier.size(28.dp)}else{Modifier}
        )
        Text(text = text, style = if(!isWear){typography.bodyLarge}else{typography.bodyMedium}, color = textColor, modifier = Modifier.padding(bottom = 2.dp))
    }
}

fun isDeviceOwner(dpm:DevicePolicyManager): Boolean {
    return dpm.isDeviceOwnerApp("com.binbin.androidowner")
}

fun isProfileOwner(dpm:DevicePolicyManager): Boolean {
    return dpm.isProfileOwnerApp("com.binbin.androidowner")
}

@SuppressLint("ModifierFactoryExtensionFunction", "ComposableModifierFactory")
@Composable
@Stable
fun sections(bgColor:Color=MaterialTheme.colorScheme.primaryContainer,onClick:()->Unit={},clickable:Boolean=false):Modifier{
    val backgroundColor = if(isSystemInDarkTheme()){bgColor.copy(0.3F)}else{bgColor.copy(0.8F)}
    return if(!LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE).getBoolean("isWear",false)){
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick=onClick, enabled = clickable)
            .background(color = backgroundColor)
            .padding(vertical = 10.dp, horizontal = 10.dp)
    }else{
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick=onClick, enabled = clickable)
            .background(color = backgroundColor)
            .padding(vertical = 2.dp, horizontal = 3.dp)
    }
}

fun uriToStream(
    context: Context,
    uri: Uri?,
    operation:(stream:InputStream)->Unit
){
    if(uri!=null){
        apkUri = uri
        try{
            val stream = context.contentResolver.openInputStream(uri)
            if(stream!=null) { operation(stream) }
            else{ Toast.makeText(context, "空的流", Toast.LENGTH_SHORT).show() }
            stream?.close()
        }
        catch(e:FileNotFoundException){ Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show() }
        catch(e:IOException){ Toast.makeText(context, "IO异常", Toast.LENGTH_SHORT).show() }
    }else{ Toast.makeText(context, "空URI", Toast.LENGTH_SHORT).show() }
}
