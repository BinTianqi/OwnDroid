package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.dpm.ApplicationManage
import com.bintianqi.owndroid.dpm.DpmPermissions
import com.bintianqi.owndroid.dpm.ManagedProfile
import com.bintianqi.owndroid.dpm.Network
import com.bintianqi.owndroid.dpm.Password
import com.bintianqi.owndroid.dpm.SystemManage
import com.bintianqi.owndroid.dpm.UserManage
import com.bintianqi.owndroid.dpm.UserRestriction
import com.bintianqi.owndroid.dpm.dhizukuErrorStatus
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver
import com.bintianqi.owndroid.dpm.isDeviceAdmin
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.rosan.dhizuku.api.Dhizuku
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.util.Locale

var backToHomeStateFlow = MutableStateFlow(false)
@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {
    private val showAuth = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        registerActivityResult(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val sharedPref = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        if(sharedPref.getBoolean("auth", false)) {
            showAuth.value = true
        }
        val locale = applicationContext.resources?.configuration?.locale
        zhCN = locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.CHINESE || locale == Locale.CHINA
        setContent {
            val materialYou = remember { mutableStateOf(sharedPref.getBoolean("material_you", true)) }
            val blackTheme = remember { mutableStateOf(sharedPref.getBoolean("black_theme", false)) }
            OwnDroidTheme(materialYou.value, blackTheme.value) {
                Home(materialYou, blackTheme)
                if(showAuth.value) {
                    AuthScreen(this, showAuth)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        if(
            sharedPref.getBoolean("auth", false) &&
            sharedPref.getBoolean("lock_in_background", false)
        ) {
            showAuth.value = true
        }
        if (sharedPref.getBoolean("dhizuku", false)) {
            if (!Dhizuku.init(applicationContext)) { dhizukuErrorStatus.value = 1 }
            if (!Dhizuku.isPermissionGranted()) { dhizukuErrorStatus.value = 2 }
        }
    }

}

@ExperimentalMaterial3Api
@Composable
fun Home(materialYou:MutableState<Boolean>, blackTheme:MutableState<Boolean>) {
    val navCtrl = rememberNavController()
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val focusMgr = LocalFocusManager.current
    val pkgName = remember { mutableStateOf("") }
    val dialogStatus = remember { mutableIntStateOf(0) }
    val backToHome by backToHomeStateFlow.collectAsState()
    LaunchedEffect(backToHome) {
        if(backToHome) { navCtrl.navigateUp(); backToHomeStateFlow.value = false }
    }
    NavHost(
        navController = navCtrl,
        startDestination = "HomePage",
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .imePadding()
            .pointerInput(Unit) { detectTapGestures(onTap = { focusMgr.clearFocus() }) },
        enterTransition = Animations.navHostEnterTransition,
        exitTransition = Animations.navHostExitTransition,
        popEnterTransition = Animations.navHostPopEnterTransition,
        popExitTransition = Animations.navHostPopExitTransition
    ) {
        composable(route = "HomePage") { HomePage(navCtrl, pkgName) }
        composable(route = "SystemManage") { SystemManage(navCtrl) }
        composable(route = "ManagedProfile") { ManagedProfile(navCtrl) }
        composable(route = "Permissions") { DpmPermissions(navCtrl) }
        composable(route = "ApplicationManage") { ApplicationManage(navCtrl, pkgName, dialogStatus) }
        composable(route = "UserRestriction") { UserRestriction(navCtrl) }
        composable(route = "UserManage") { UserManage(navCtrl) }
        composable(route = "Password") { Password(navCtrl) }
        composable(route = "AppSetting") { AppSetting(navCtrl, materialYou, blackTheme) }
        composable(route = "Network") { Network(navCtrl) }
        composable(route = "PackageSelector") { PackageSelector(navCtrl, pkgName) }
        composable(route = "PermissionPicker") { PermissionPicker(navCtrl) }
    }
    LaunchedEffect(Unit) {
        val profileInited = sharedPref.getBoolean("ManagedProfileActivated", false)
        val profileNotActivated = !profileInited && context.isProfileOwner && (VERSION.SDK_INT < 24 || (VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)))
        if(profileNotActivated) {
            dpm.setProfileEnabled(receiver)
            sharedPref.edit().putBoolean("ManagedProfileActivated", true).apply()
            Toast.makeText(context, R.string.work_profile_activated, Toast.LENGTH_SHORT).show()
        }
    }
    DhizukuErrorDialog()
}

@Composable
private fun HomePage(navCtrl:NavHostController, pkgName: MutableState<String>) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    var activateType = if(sharedPref.getBoolean("dhizuku", false)) stringResource(R.string.dhizuku) + " - " else ""
    activateType += stringResource(
            if(context.isDeviceOwner) { R.string.device_owner }
            else if(context.isProfileOwner) {
                if(VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)) R.string.work_profile_owner else R.string.profile_owner
            }
            else if(context.isDeviceAdmin) R.string.device_admin else R.string.click_to_activate
        )
    LaunchedEffect(Unit) { pkgName.value = "" }
    Column(modifier = Modifier.background(colorScheme.background).statusBarsPadding().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 25.dp))
        Text(
            text = stringResource(R.string.app_name), style = typography.headlineLarge,
            modifier = Modifier.padding(start = 10.dp), color = colorScheme.onBackground
        )
        Spacer(Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 8.dp)
                .clip(RoundedCornerShape(15))
                .background(color = colorScheme.primary)
                .clickable(onClick = { navCtrl.navigate("Permissions") })
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.padding(start = 22.dp))
            Icon(
                painter = painterResource(if(context.isDeviceAdmin) R.drawable.check_circle_fill1 else R.drawable.block_fill0),
                contentDescription = null,
                tint = colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.padding(start = 10.dp))
            Column {
                Text(
                    text = stringResource(if(context.isDeviceAdmin) R.string.activated else R.string.deactivated),
                    style = typography.headlineSmall,
                    color = colorScheme.onPrimary,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                if(activateType != "") { Text(text = activateType, color = colorScheme.onPrimary) }
            }
        }
        HomePageItem(R.string.system_manage, R.drawable.mobile_phone_fill0, "SystemManage", navCtrl)
        if(VERSION.SDK_INT >= 24 && (context.isDeviceOwner) || context.isProfileOwner) { HomePageItem(R.string.network, R.drawable.wifi_fill0, "Network", navCtrl) }
        if(
            (VERSION.SDK_INT < 24 && !context.isDeviceOwner) || (
                    VERSION.SDK_INT >= 24 && (dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) ||
                            (context.isProfileOwner && dpm.isManagedProfile(receiver)))
            )
        ) {
            HomePageItem(R.string.work_profile, R.drawable.work_fill0, "ManagedProfile", navCtrl)
        }
        HomePageItem(R.string.app_manager, R.drawable.apps_fill0, "ApplicationManage", navCtrl)
        if(VERSION.SDK_INT>=24) {
            HomePageItem(R.string.user_restrict, R.drawable.person_off, "UserRestriction", navCtrl)
        }
        HomePageItem(R.string.user_manager,R.drawable.manage_accounts_fill0,"UserManage", navCtrl)
        HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0, "Password", navCtrl)
        HomePageItem(R.string.setting, R.drawable.settings_fill0, "AppSetting", navCtrl)
        Spacer(Modifier.padding(vertical = 20.dp))
    }
}

@Composable
fun HomePageItem(name: Int, imgVector: Int, navTo: String, navCtrl: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(25))
            .clickable(onClick = { navCtrl.navigate(navTo) })
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
        Text(
            text = stringResource(name),
            style = typography.headlineSmall,
            color = colorScheme.onBackground,
            modifier = Modifier.padding(bottom = if(zhCN) { 2 } else { 0 }.dp)
        )
    }
}

@Composable
private fun DhizukuErrorDialog() {
    val status by dhizukuErrorStatus.collectAsState()
    if (status != 0) {
        val context = LocalContext.current
        val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        LaunchedEffect(Unit) {
            delay(200)
            sharedPref.edit().putBoolean("dhizuku", false).apply()
        }
        AlertDialog(
            onDismissRequest = { dhizukuErrorStatus.value = 0 },
            confirmButton = {
                TextButton(onClick = { dhizukuErrorStatus.value = 0 }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            title = { Text(stringResource(R.string.dhizuku)) },
            text = {
                var text = stringResource(
                    when(status){
                        1 -> R.string.failed_to_init_dhizuku
                        2 -> R.string.dhizuku_permission_not_granted
                        else -> R.string.failed_to_init_dhizuku
                    }
                )
                if(sharedPref.getBoolean("dhizuku", false)) text += "\n" + stringResource(R.string.dhizuku_mode_disabled)
                Text(text)
            }
        )
    }
}
