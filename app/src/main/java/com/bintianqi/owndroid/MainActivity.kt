package com.bintianqi.owndroid

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.dpm.AccountsViewer
import com.bintianqi.owndroid.dpm.AffiliationID
import com.bintianqi.owndroid.dpm.AlwaysOnVPNPackage
import com.bintianqi.owndroid.dpm.ApplicationManage
import com.bintianqi.owndroid.dpm.CACert
import com.bintianqi.owndroid.dpm.ChangeTime
import com.bintianqi.owndroid.dpm.ChangeTimeZone
import com.bintianqi.owndroid.dpm.ChangeUserIcon
import com.bintianqi.owndroid.dpm.ChangeUsername
import com.bintianqi.owndroid.dpm.ContentProtectionPolicy
import com.bintianqi.owndroid.dpm.CreateUser
import com.bintianqi.owndroid.dpm.CreateWorkProfile
import com.bintianqi.owndroid.dpm.CurrentUserInfo
import com.bintianqi.owndroid.dpm.DelegatedAdmins
import com.bintianqi.owndroid.dpm.DeleteWorkProfile
import com.bintianqi.owndroid.dpm.DeviceAdmin
import com.bintianqi.owndroid.dpm.DeviceInfo
import com.bintianqi.owndroid.dpm.DeviceOwner
import com.bintianqi.owndroid.dpm.DisableAccountManagement
import com.bintianqi.owndroid.dpm.DisableKeyguardFeatures
import com.bintianqi.owndroid.dpm.FRPPolicy
import com.bintianqi.owndroid.dpm.HardwareMonitor
import com.bintianqi.owndroid.dpm.InstallSystemUpdate
import com.bintianqi.owndroid.dpm.IntentFilter
import com.bintianqi.owndroid.dpm.Keyguard
import com.bintianqi.owndroid.dpm.LockScreenInfo
import com.bintianqi.owndroid.dpm.LockTaskMode
import com.bintianqi.owndroid.dpm.MTEPolicy
import com.bintianqi.owndroid.dpm.NearbyStreamingPolicy
import com.bintianqi.owndroid.dpm.Network
import com.bintianqi.owndroid.dpm.NetworkLogging
import com.bintianqi.owndroid.dpm.NetworkOptions
import com.bintianqi.owndroid.dpm.NetworkStats
import com.bintianqi.owndroid.dpm.NetworkStatsViewer
import com.bintianqi.owndroid.dpm.OrgOwnedProfile
import com.bintianqi.owndroid.dpm.OverrideAPN
import com.bintianqi.owndroid.dpm.Password
import com.bintianqi.owndroid.dpm.PasswordComplexity
import com.bintianqi.owndroid.dpm.PasswordInfo
import com.bintianqi.owndroid.dpm.PasswordQuality
import com.bintianqi.owndroid.dpm.PermissionPolicy
import com.bintianqi.owndroid.dpm.Permissions
import com.bintianqi.owndroid.dpm.PreferentialNetworkService
import com.bintianqi.owndroid.dpm.PrivateDNS
import com.bintianqi.owndroid.dpm.ProfileOwner
import com.bintianqi.owndroid.dpm.RecommendedGlobalProxy
import com.bintianqi.owndroid.dpm.ResetPassword
import com.bintianqi.owndroid.dpm.ResetPasswordToken
import com.bintianqi.owndroid.dpm.RestrictionData
import com.bintianqi.owndroid.dpm.SecurityLogging
import com.bintianqi.owndroid.dpm.Shizuku
import com.bintianqi.owndroid.dpm.SupportMessages
import com.bintianqi.owndroid.dpm.SuspendPersonalApp
import com.bintianqi.owndroid.dpm.SystemManage
import com.bintianqi.owndroid.dpm.SystemOptions
import com.bintianqi.owndroid.dpm.SystemUpdatePolicy
import com.bintianqi.owndroid.dpm.TransferOwnership
import com.bintianqi.owndroid.dpm.UpdateNetwork
import com.bintianqi.owndroid.dpm.UserOperation
import com.bintianqi.owndroid.dpm.UserOptions
import com.bintianqi.owndroid.dpm.UserRestriction
import com.bintianqi.owndroid.dpm.UserRestrictionItem
import com.bintianqi.owndroid.dpm.UserSessionMessage
import com.bintianqi.owndroid.dpm.Users
import com.bintianqi.owndroid.dpm.Wifi
import com.bintianqi.owndroid.dpm.WifiAuthKeypair
import com.bintianqi.owndroid.dpm.WifiSecurityLevel
import com.bintianqi.owndroid.dpm.WifiSsidPolicy
import com.bintianqi.owndroid.dpm.WipeData
import com.bintianqi.owndroid.dpm.WorkProfile
import com.bintianqi.owndroid.dpm.dhizukuErrorStatus
import com.bintianqi.owndroid.dpm.dhizukuPermissionGranted
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver
import com.bintianqi.owndroid.dpm.isDeviceAdmin
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner
import com.bintianqi.owndroid.dpm.setDefaultAffiliationID
import com.bintianqi.owndroid.dpm.toggleInstallAppActivity
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.rosan.dhizuku.api.Dhizuku
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.util.Locale

val backToHomeStateFlow = MutableStateFlow(false)
@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        registerActivityResult(this)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val context = applicationContext
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        val locale = context.resources?.configuration?.locale
        zhCN = locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.CHINESE || locale == Locale.CHINA
        toggleInstallAppActivity()
        val vm by viewModels<MyViewModel>()
        if(!vm.initialized) vm.initialize(context)
        lifecycleScope.launch { delay(5000); setDefaultAffiliationID(context) }
        setContent {
            OwnDroidTheme(vm) {
                Home(this, vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = applicationContext.getSharedPreferences("data", MODE_PRIVATE)
        if (sharedPref.getBoolean("dhizuku", false)) {
            if (Dhizuku.init(applicationContext)) {
                if (!dhizukuPermissionGranted()) { dhizukuErrorStatus.value = 2 }
            } else {
                sharedPref.edit().putBoolean("dhizuku", false).apply()
                dhizukuErrorStatus.value = 1
            }
        }
    }

}

@ExperimentalMaterial3Api
@Composable
fun Home(activity: FragmentActivity, vm: MyViewModel) {
    val navCtrl = rememberNavController()
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    val focusMgr = LocalFocusManager.current
    val backToHome by backToHomeStateFlow.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(backToHome) {
        if(backToHome) { navCtrl.navigateUp(); backToHomeStateFlow.value = false }
    }
    @Suppress("NewApi") NavHost(
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
        composable(route = "HomePage") { HomePage(navCtrl) }

        composable(route = "Permissions") { Permissions(navCtrl) }
        composable(route = "Shizuku") { Shizuku(navCtrl, it.arguments!!) }
        composable(route = "AccountsViewer") { AccountsViewer(navCtrl, it.arguments!!) }
        composable(route = "DeviceAdmin") { DeviceAdmin(navCtrl) }
        composable(route = "ProfileOwner") { ProfileOwner(navCtrl) }
        composable(route = "DeviceOwner") { DeviceOwner(navCtrl) }
        composable(route = "DelegatedAdmins") { DelegatedAdmins(navCtrl, vm) }
        composable(route = "DeviceInfo") { DeviceInfo(navCtrl) }
        composable(route = "LockScreenInfo") { LockScreenInfo(navCtrl) }
        composable(route = "SupportMessages") { SupportMessages(navCtrl) }
        composable(route = "TransferOwnership") { TransferOwnership(navCtrl) }

        composable(route = "System") { SystemManage(navCtrl) }
        composable(route = "SystemOptions") { SystemOptions(navCtrl) }
        composable(route = "Keyguard") { Keyguard(navCtrl) }
        composable(route = "HardwareMonitor") { HardwareMonitor(navCtrl) }
        composable(route = "ChangeTime") { ChangeTime(navCtrl) }
        composable(route = "ChangeTimeZone") { ChangeTimeZone(navCtrl) }
        //composable(route = "KeyPairs") { KeyPairs(navCtrl) }
        composable(route = "ContentProtectionPolicy") { ContentProtectionPolicy(navCtrl) }
        composable(route = "PermissionPolicy") { PermissionPolicy(navCtrl) }
        composable(route = "MTEPolicy") { MTEPolicy(navCtrl) }
        composable(route = "NearbyStreamingPolicy") { NearbyStreamingPolicy(navCtrl) }
        composable(route = "LockTaskMode") { LockTaskMode(navCtrl, vm) }
        composable(route = "CACert") { CACert(navCtrl) }
        composable(route = "SecurityLogging") { SecurityLogging(navCtrl) }
        composable(route = "DisableAccountManagement") { DisableAccountManagement(navCtrl) }
        composable(route = "SystemUpdatePolicy") { SystemUpdatePolicy(navCtrl) }
        composable(route = "InstallSystemUpdate") { InstallSystemUpdate(navCtrl) }
        composable(route = "FRPPolicy") { FRPPolicy(navCtrl) }
        composable(route = "WipeData") { WipeData(navCtrl) }

        composable(route = "Network") { Network(navCtrl) }
        composable(route = "Wifi") { Wifi(navCtrl) }
        composable(route = "NetworkOptions") { NetworkOptions(navCtrl) }
        composable(route = "UpdateNetwork") { UpdateNetwork(it.arguments!!, navCtrl) }
        composable(route = "MinWifiSecurityLevel") { WifiSecurityLevel(navCtrl) }
        composable(route = "WifiSsidPolicy") { WifiSsidPolicy(navCtrl) }
        composable(route = "NetworkStats") { NetworkStats(navCtrl, vm) }
        composable(route = "NetworkStatsViewer") { NetworkStatsViewer(navCtrl, it.arguments!!) }
        composable(route = "PrivateDNS") { PrivateDNS(navCtrl) }
        composable(route = "AlwaysOnVpn") { AlwaysOnVPNPackage(navCtrl, vm) }
        composable(route = "RecommendedGlobalProxy") { RecommendedGlobalProxy(navCtrl) }
        composable(route = "NetworkLog") { NetworkLogging(navCtrl) }
        composable(route = "WifiAuthKeypair") { WifiAuthKeypair(navCtrl) }
        composable(route = "PreferentialNetworkService") { PreferentialNetworkService(navCtrl) }
        composable(route = "OverrideAPN") { OverrideAPN(navCtrl) }

        composable(route = "WorkProfile") { WorkProfile(navCtrl) }
        composable(route = "OrgOwnedWorkProfile") { OrgOwnedProfile(navCtrl) }
        composable(route = "CreateWorkProfile") { CreateWorkProfile(navCtrl) }
        composable(route = "SuspendPersonalApp") { SuspendPersonalApp(navCtrl) }
        composable(route = "IntentFilter") { IntentFilter(navCtrl) }
        composable(route = "DeleteWorkProfile") { DeleteWorkProfile(navCtrl) }

        composable(route = "Applications") { ApplicationManage(navCtrl, vm) }

        composable(route = "UserRestriction") { UserRestriction(navCtrl) }
        composable(route = "UR-Internet") {
            MyScaffold(R.string.network_and_internet, 0.dp, navCtrl) { RestrictionData.internet.forEach { UserRestrictionItem(it, vm) } }
        }
        composable(route = "UR-Connectivity") {
            MyScaffold(R.string.connectivity, 0.dp, navCtrl) { RestrictionData.connectivity.forEach { UserRestrictionItem(it, vm) } }
        }
        composable(route = "UR-Applications") {
            MyScaffold(R.string.applications, 0.dp, navCtrl) { RestrictionData.applications.forEach { UserRestrictionItem(it, vm) } }
        }
        composable(route = "UR-Users") {
            MyScaffold(R.string.users, 0.dp, navCtrl) { RestrictionData.users.forEach { UserRestrictionItem(it, vm) } }
        }
        composable(route = "UR-Media") {
            MyScaffold(R.string.media, 0.dp, navCtrl) { RestrictionData.media.forEach { UserRestrictionItem(it, vm) } }
        }
        composable(route = "UR-Other") {
            MyScaffold(R.string.other, 0.dp, navCtrl) { RestrictionData.other.forEach { UserRestrictionItem(it, vm) } }
        }

        composable(route = "Users") { Users(navCtrl) }
        composable(route = "UserInfo") { CurrentUserInfo(navCtrl) }
        composable(route = "UserOptions") { UserOptions(navCtrl) }
        composable(route = "UserOperation") { UserOperation(navCtrl) }
        composable(route = "CreateUser") { CreateUser(navCtrl) }
        composable(route = "ChangeUsername") { ChangeUsername(navCtrl) }
        composable(route = "ChangeUserIcon") { ChangeUserIcon(navCtrl) }
        composable(route = "UserSessionMessage") { UserSessionMessage(navCtrl) }
        composable(route = "AffiliationID") { AffiliationID(navCtrl) }

        composable(route = "Password") { Password(navCtrl) }
        composable(route = "PasswordInfo") { PasswordInfo(navCtrl) }
        composable(route = "ResetPasswordToken") { ResetPasswordToken(navCtrl) }
        composable(route = "ResetPassword") { ResetPassword(navCtrl) }
        composable(route = "RequirePasswordComplexity") { PasswordComplexity(navCtrl) }
        composable(route = "DisableKeyguardFeatures") { DisableKeyguardFeatures(navCtrl) }
        composable(route = "RequirePasswordQuality") { PasswordQuality(navCtrl) }

        composable(route = "Settings") { Settings(navCtrl) }
        composable(route = "Options") { SettingsOptions(navCtrl) }
        composable(route = "Appearance") { Appearance(navCtrl, vm) }
        composable(route = "AuthSettings") { AuthSettings(navCtrl) }
        composable(route = "ApiSettings") { ApiSettings(navCtrl) }
        composable(route = "About") { About(navCtrl) }

        composable(route = "PackageSelector") { PackageSelector(navCtrl, vm) }

        composable(
            route = "Authenticate",
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) { Authenticate(activity, navCtrl) }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if(
                (event == Lifecycle.Event.ON_RESUME &&
                        sharedPref.getBoolean("auth", false) &&
                        sharedPref.getBoolean("lock_in_background", false)) ||
                (event == Lifecycle.Event.ON_CREATE && sharedPref.getBoolean("auth", false))
            ) {
                navCtrl.navigate("Authenticate") { launchSingleTop = true }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        val profileInitialized = sharedPref.getBoolean("ManagedProfileActivated", false)
        val profileNotActivated = !profileInitialized && context.isProfileOwner && (VERSION.SDK_INT < 24 || dpm.isManagedProfile(receiver))
        if(profileNotActivated) {
            dpm.setProfileEnabled(receiver)
            sharedPref.edit().putBoolean("ManagedProfileActivated", true).apply()
            Toast.makeText(context, R.string.work_profile_activated, Toast.LENGTH_SHORT).show()
        }
    }
    DhizukuErrorDialog()
}

@Composable
private fun HomePage(navCtrl:NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    var activated by remember { mutableStateOf(false) }
    var activateType by remember { mutableStateOf("") }
    val deviceAdmin = context.isDeviceAdmin
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val refreshStatus by dhizukuErrorStatus.collectAsState()
    LaunchedEffect(refreshStatus) {
        activated = context.isProfileOwner || context.isDeviceOwner
        activateType = if(sharedPref.getBoolean("dhizuku", false)) context.getString(R.string.dhizuku) + " - " else ""
        activateType += context.getString(
            if(deviceOwner) { R.string.device_owner }
            else if(profileOwner) {
                if(VERSION.SDK_INT >= 24 && dpm.isManagedProfile(receiver)) R.string.work_profile_owner else R.string.profile_owner
            }
            else if(deviceAdmin) R.string.device_admin else R.string.click_to_activate
        )
    }
    Scaffold {
        Column(modifier = Modifier.padding(it).verticalScroll(rememberScrollState())) {
            Spacer(Modifier.padding(vertical = 25.dp))
            Text(
                text = stringResource(R.string.app_name), style = typography.headlineLarge,
                modifier = Modifier.padding(start = 10.dp)
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
                    painter = painterResource(if(activated) R.drawable.check_circle_fill1 else R.drawable.block_fill0),
                    contentDescription = null,
                    tint = colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.padding(start = 10.dp))
                Column {
                    Text(
                        text = stringResource(if(activated) R.string.activated else R.string.deactivated),
                        style = typography.headlineSmall,
                        color = colorScheme.onPrimary,
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                    if(activateType != "") { Text(text = activateType, color = colorScheme.onPrimary) }
                }
            }
            HomePageItem(R.string.system, R.drawable.android_fill0, "System", navCtrl)
            if(deviceOwner || profileOwner) { HomePageItem(R.string.network, R.drawable.wifi_fill0, "Network", navCtrl) }
            if(
                (VERSION.SDK_INT < 24 && !deviceOwner) || (dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) ||
                                (profileOwner && dpm.isManagedProfile(receiver))
                        )
            ) {
                HomePageItem(R.string.work_profile, R.drawable.work_fill0, "ManagedProfile", navCtrl)
            }
            if(deviceOwner || profileOwner) HomePageItem(R.string.applications, R.drawable.apps_fill0, "Applications", navCtrl)
            if(VERSION.SDK_INT >= 24 && (profileOwner || deviceOwner)) {
                HomePageItem(R.string.user_restriction, R.drawable.person_off, "UserRestriction", navCtrl)
            }
            HomePageItem(R.string.users,R.drawable.manage_accounts_fill0,"Users", navCtrl)
            HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0, "Password", navCtrl)
            HomePageItem(R.string.settings, R.drawable.settings_fill0, "Settings", navCtrl)
            Spacer(Modifier.padding(vertical = 20.dp))
        }
    }
}

@Composable
fun HomePageItem(name: Int, imgVector: Int, navTo: String, navCtrl: NavHostController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = { navCtrl.navigate(navTo) })
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.padding(start = 30.dp))
        Icon(
            painter = painterResource(imgVector),
            contentDescription = null
        )
        Spacer(Modifier.padding(start = 15.dp))
        Text(
            text = stringResource(name),
            style = typography.headlineSmall,
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
            context.toggleInstallAppActivity()
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
