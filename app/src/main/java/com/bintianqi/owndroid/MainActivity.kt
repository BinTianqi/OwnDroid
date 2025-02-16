package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bintianqi.owndroid.dpm.Accounts
import com.bintianqi.owndroid.dpm.AccountsScreen
import com.bintianqi.owndroid.dpm.AddDelegatedAdmin
import com.bintianqi.owndroid.dpm.AddDelegatedAdminScreen
import com.bintianqi.owndroid.dpm.AddNetwork
import com.bintianqi.owndroid.dpm.AddNetworkScreen
import com.bintianqi.owndroid.dpm.AffiliationId
import com.bintianqi.owndroid.dpm.AffiliationIdScreen
import com.bintianqi.owndroid.dpm.AlwaysOnVpnPackage
import com.bintianqi.owndroid.dpm.AlwaysOnVpnPackageScreen
import com.bintianqi.owndroid.dpm.Applications
import com.bintianqi.owndroid.dpm.ApplicationsScreen
import com.bintianqi.owndroid.dpm.CaCert
import com.bintianqi.owndroid.dpm.CaCertScreen
import com.bintianqi.owndroid.dpm.ChangeTime
import com.bintianqi.owndroid.dpm.ChangeTimeScreen
import com.bintianqi.owndroid.dpm.ChangeTimeZone
import com.bintianqi.owndroid.dpm.ChangeTimeZoneScreen
import com.bintianqi.owndroid.dpm.ChangeUsername
import com.bintianqi.owndroid.dpm.ChangeUsernameScreen
import com.bintianqi.owndroid.dpm.ContentProtectionPolicy
import com.bintianqi.owndroid.dpm.ContentProtectionPolicyScreen
import com.bintianqi.owndroid.dpm.CreateUser
import com.bintianqi.owndroid.dpm.CreateUserScreen
import com.bintianqi.owndroid.dpm.CreateWorkProfile
import com.bintianqi.owndroid.dpm.CreateWorkProfileScreen
import com.bintianqi.owndroid.dpm.CrossProfileIntentFilter
import com.bintianqi.owndroid.dpm.CrossProfileIntentFilterScreen
import com.bintianqi.owndroid.dpm.DelegatedAdmins
import com.bintianqi.owndroid.dpm.DelegatedAdminsScreen
import com.bintianqi.owndroid.dpm.DeleteWorkProfile
import com.bintianqi.owndroid.dpm.DeleteWorkProfileScreen
import com.bintianqi.owndroid.dpm.DeviceAdmin
import com.bintianqi.owndroid.dpm.DeviceAdminScreen
import com.bintianqi.owndroid.dpm.DeviceInfo
import com.bintianqi.owndroid.dpm.DeviceInfoScreen
import com.bintianqi.owndroid.dpm.DeviceOwner
import com.bintianqi.owndroid.dpm.DeviceOwnerScreen
import com.bintianqi.owndroid.dpm.DisableAccountManagement
import com.bintianqi.owndroid.dpm.DisableAccountManagementScreen
import com.bintianqi.owndroid.dpm.FrpPolicy
import com.bintianqi.owndroid.dpm.FrpPolicyScreen
import com.bintianqi.owndroid.dpm.HardwareMonitor
import com.bintianqi.owndroid.dpm.HardwareMonitorScreen
import com.bintianqi.owndroid.dpm.InstallSystemUpdate
import com.bintianqi.owndroid.dpm.InstallSystemUpdateScreen
import com.bintianqi.owndroid.dpm.Keyguard
import com.bintianqi.owndroid.dpm.KeyguardDisabledFeatures
import com.bintianqi.owndroid.dpm.KeyguardDisabledFeaturesScreen
import com.bintianqi.owndroid.dpm.KeyguardScreen
import com.bintianqi.owndroid.dpm.LockScreenInfo
import com.bintianqi.owndroid.dpm.LockScreenInfoScreen
import com.bintianqi.owndroid.dpm.LockTaskMode
import com.bintianqi.owndroid.dpm.LockTaskModeScreen
import com.bintianqi.owndroid.dpm.MtePolicy
import com.bintianqi.owndroid.dpm.MtePolicyScreen
import com.bintianqi.owndroid.dpm.NearbyStreamingPolicy
import com.bintianqi.owndroid.dpm.NearbyStreamingPolicyScreen
import com.bintianqi.owndroid.dpm.Network
import com.bintianqi.owndroid.dpm.NetworkLogging
import com.bintianqi.owndroid.dpm.NetworkLoggingScreen
import com.bintianqi.owndroid.dpm.NetworkOptions
import com.bintianqi.owndroid.dpm.NetworkOptionsScreen
import com.bintianqi.owndroid.dpm.NetworkScreen
import com.bintianqi.owndroid.dpm.NetworkStatsScreen
import com.bintianqi.owndroid.dpm.NetworkStatsViewer
import com.bintianqi.owndroid.dpm.NetworkStatsViewerScreen
import com.bintianqi.owndroid.dpm.OrganizationOwnedProfile
import com.bintianqi.owndroid.dpm.OrganizationOwnedProfileScreen
import com.bintianqi.owndroid.dpm.OverrideApn
import com.bintianqi.owndroid.dpm.OverrideApnScreen
import com.bintianqi.owndroid.dpm.Password
import com.bintianqi.owndroid.dpm.PasswordInfo
import com.bintianqi.owndroid.dpm.PasswordInfoScreen
import com.bintianqi.owndroid.dpm.PasswordScreen
import com.bintianqi.owndroid.dpm.PermissionPolicy
import com.bintianqi.owndroid.dpm.PermissionPolicyScreen
import com.bintianqi.owndroid.dpm.Permissions
import com.bintianqi.owndroid.dpm.PermissionsScreen
import com.bintianqi.owndroid.dpm.PreferentialNetworkService
import com.bintianqi.owndroid.dpm.PreferentialNetworkServiceScreen
import com.bintianqi.owndroid.dpm.PrivateDns
import com.bintianqi.owndroid.dpm.PrivateDnsScreen
import com.bintianqi.owndroid.dpm.ProfileOwner
import com.bintianqi.owndroid.dpm.ProfileOwnerScreen
import com.bintianqi.owndroid.dpm.QueryNetworkStats
import com.bintianqi.owndroid.dpm.RecommendedGlobalProxy
import com.bintianqi.owndroid.dpm.RecommendedGlobalProxyScreen
import com.bintianqi.owndroid.dpm.RequiredPasswordComplexity
import com.bintianqi.owndroid.dpm.RequiredPasswordComplexityScreen
import com.bintianqi.owndroid.dpm.RequiredPasswordQuality
import com.bintianqi.owndroid.dpm.RequiredPasswordQualityScreen
import com.bintianqi.owndroid.dpm.ResetPassword
import com.bintianqi.owndroid.dpm.ResetPasswordScreen
import com.bintianqi.owndroid.dpm.ResetPasswordToken
import com.bintianqi.owndroid.dpm.ResetPasswordTokenScreen
import com.bintianqi.owndroid.dpm.Restriction
import com.bintianqi.owndroid.dpm.SecurityLogging
import com.bintianqi.owndroid.dpm.SecurityLoggingScreen
import com.bintianqi.owndroid.dpm.SetSystemUpdatePolicy
import com.bintianqi.owndroid.dpm.ShizukuScreen
import com.bintianqi.owndroid.dpm.SupportMessage
import com.bintianqi.owndroid.dpm.SupportMessageScreen
import com.bintianqi.owndroid.dpm.SuspendPersonalApp
import com.bintianqi.owndroid.dpm.SuspendPersonalAppScreen
import com.bintianqi.owndroid.dpm.SystemManager
import com.bintianqi.owndroid.dpm.SystemManagerScreen
import com.bintianqi.owndroid.dpm.SystemOptions
import com.bintianqi.owndroid.dpm.SystemOptionsScreen
import com.bintianqi.owndroid.dpm.SystemUpdatePolicyScreen
import com.bintianqi.owndroid.dpm.TransferOwnership
import com.bintianqi.owndroid.dpm.TransferOwnershipScreen
import com.bintianqi.owndroid.dpm.UserInfo
import com.bintianqi.owndroid.dpm.UserInfoScreen
import com.bintianqi.owndroid.dpm.UserOperation
import com.bintianqi.owndroid.dpm.UserOperationScreen
import com.bintianqi.owndroid.dpm.UserRestriction
import com.bintianqi.owndroid.dpm.UserRestrictionOptions
import com.bintianqi.owndroid.dpm.UserRestrictionOptionsScreen
import com.bintianqi.owndroid.dpm.UserRestrictionScreen
import com.bintianqi.owndroid.dpm.UserSessionMessage
import com.bintianqi.owndroid.dpm.UserSessionMessageScreen
import com.bintianqi.owndroid.dpm.Users
import com.bintianqi.owndroid.dpm.UsersOptions
import com.bintianqi.owndroid.dpm.UsersOptionsScreen
import com.bintianqi.owndroid.dpm.UsersScreen
import com.bintianqi.owndroid.dpm.WiFi
import com.bintianqi.owndroid.dpm.WifiAuthKeypair
import com.bintianqi.owndroid.dpm.WifiAuthKeypairScreen
import com.bintianqi.owndroid.dpm.WifiScreen
import com.bintianqi.owndroid.dpm.WifiSecurityLevel
import com.bintianqi.owndroid.dpm.WifiSecurityLevelScreen
import com.bintianqi.owndroid.dpm.WifiSsidPolicyScreen
import com.bintianqi.owndroid.dpm.WipeData
import com.bintianqi.owndroid.dpm.WipeDataScreen
import com.bintianqi.owndroid.dpm.WorkProfile
import com.bintianqi.owndroid.dpm.WorkProfileScreen
import com.bintianqi.owndroid.dpm.dhizukuErrorStatus
import com.bintianqi.owndroid.dpm.dhizukuPermissionGranted
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver
import com.bintianqi.owndroid.dpm.isDeviceAdmin
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner
import com.bintianqi.owndroid.dpm.setDefaultAffiliationID
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.rosan.dhizuku.api.Dhizuku
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
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
        val vm by viewModels<MyViewModel>()
        lifecycleScope.launch { delay(5000); setDefaultAffiliationID(context) }
        setContent {
            val theme by vm.theme.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                Home(this, vm)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sp = SharedPrefs(applicationContext)
        if (sp.dhizuku) {
            if (Dhizuku.init(applicationContext)) {
                if (!dhizukuPermissionGranted()) { dhizukuErrorStatus.value = 2 }
            } else {
                sp.dhizuku = false
                dhizukuErrorStatus.value = 1
            }
        }
    }

}

@ExperimentalMaterial3Api
@Composable
fun Home(activity: FragmentActivity, vm: MyViewModel) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val backToHome by backToHomeStateFlow.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(backToHome) {
        if(backToHome) { navController.navigateUp(); backToHomeStateFlow.value = false }
    }
    val userRestrictions by vm.userRestrictions.collectAsStateWithLifecycle()
    fun navigateUp() { navController.navigateUp() }
    fun navigate(destination: Any) { navController.navigate(destination) }
    @Suppress("NewApi") NavHost(
        navController = navController,
        startDestination = Home,
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
        composable<Home> { HomeScreen { navController.navigate(it) } }

        composable<Permissions> {
            PermissionsScreen(::navigateUp, { navController.navigate(it) }) {
                val dest = navController.graph.findNode(ShizukuScreen)!!.id
                navController.navigate(dest, it)
            }
        }
        composable<ShizukuScreen> { ShizukuScreen(it.arguments!!, ::navigateUp) { navController.navigate(it) } }
        composable<Accounts>(mapOf(serializableNavTypePair<List<Accounts.Account>>())) { AccountsScreen(it.toRoute(), ::navigateUp) }
        composable<DeviceAdmin> { DeviceAdminScreen(::navigateUp) }
        composable<ProfileOwner> { ProfileOwnerScreen(::navigateUp) }
        composable<DeviceOwner> { DeviceOwnerScreen(::navigateUp) }
        composable<DelegatedAdmins> { DelegatedAdminsScreen(::navigateUp, ::navigate) }
        composable<AddDelegatedAdmin>{ AddDelegatedAdminScreen(it.toRoute(), ::navigateUp) }
        composable<DeviceInfo> { DeviceInfoScreen(::navigateUp) }
        composable<LockScreenInfo> { LockScreenInfoScreen(::navigateUp) }
        composable<SupportMessage> { SupportMessageScreen(::navigateUp) }
        composable<TransferOwnership> { TransferOwnershipScreen(::navigateUp) }

        composable<SystemManager> { SystemManagerScreen(::navigateUp, ::navigate) }
        composable<SystemOptions> { SystemOptionsScreen(::navigateUp) }
        composable<Keyguard> { KeyguardScreen(::navigateUp) }
        composable<HardwareMonitor> { HardwareMonitorScreen(::navigateUp) }
        composable<ChangeTime> { ChangeTimeScreen(::navigateUp) }
        composable<ChangeTimeZone> { ChangeTimeZoneScreen(::navigateUp) }
        //composable<> { KeyPairs(::navigateUp) }
        composable<ContentProtectionPolicy> { ContentProtectionPolicyScreen(::navigateUp) }
        composable<PermissionPolicy> { PermissionPolicyScreen(::navigateUp) }
        composable<MtePolicy> { MtePolicyScreen(::navigateUp) }
        composable<NearbyStreamingPolicy> { NearbyStreamingPolicyScreen(::navigateUp) }
        composable<LockTaskMode> { LockTaskModeScreen(::navigateUp) }
        composable<CaCert> { CaCertScreen(::navigateUp) }
        composable<SecurityLogging> { SecurityLoggingScreen(::navigateUp) }
        composable<DisableAccountManagement> { DisableAccountManagementScreen(::navigateUp) }
        composable<SetSystemUpdatePolicy> { SystemUpdatePolicyScreen(::navigateUp) }
        composable<InstallSystemUpdate> { InstallSystemUpdateScreen(::navigateUp) }
        composable<FrpPolicy> { FrpPolicyScreen(::navigateUp) }
        composable<WipeData> { WipeDataScreen(::navigateUp) }

        composable<Network> { NetworkScreen(::navigateUp, ::navigate) }
        composable<WiFi> {
            WifiScreen(::navigateUp, { navController.navigate(it) }) {
                val dest = navController.graph.findNode(AddNetwork)!!.id
                navController.navigate(dest, it)
            }
        }
        composable<NetworkOptions> { NetworkOptionsScreen(::navigateUp) }
        composable<AddNetwork> { AddNetworkScreen(it.arguments!!, ::navigateUp) }
        composable<WifiSecurityLevel> { WifiSecurityLevelScreen(::navigateUp) }
        composable<WifiSsidPolicyScreen> { WifiSsidPolicyScreen(::navigateUp) }
        composable<QueryNetworkStats> { NetworkStatsScreen(::navigateUp, ::navigate) }
        composable<NetworkStatsViewer>(mapOf(serializableNavTypePair<List<NetworkStatsViewer.Data>>())) {
            NetworkStatsViewerScreen(it.toRoute()) { navController.navigateUp() }
        }
        composable<PrivateDns> { PrivateDnsScreen(::navigateUp) }
        composable<AlwaysOnVpnPackage> { AlwaysOnVpnPackageScreen(::navigateUp) }
        composable<RecommendedGlobalProxy> { RecommendedGlobalProxyScreen(::navigateUp) }
        composable<NetworkLogging> { NetworkLoggingScreen(::navigateUp) }
        composable<WifiAuthKeypair> { WifiAuthKeypairScreen(::navigateUp) }
        composable<PreferentialNetworkService> { PreferentialNetworkServiceScreen(::navigateUp) }
        composable<OverrideApn> { OverrideApnScreen(::navigateUp) }

        composable<WorkProfile> { WorkProfileScreen(::navigateUp, ::navigate) }
        composable<OrganizationOwnedProfile> { OrganizationOwnedProfileScreen(::navigateUp) }
        composable<CreateWorkProfile> { CreateWorkProfileScreen(::navigateUp) }
        composable<SuspendPersonalApp> { SuspendPersonalAppScreen(::navigateUp) }
        composable<CrossProfileIntentFilter> { CrossProfileIntentFilterScreen(::navigateUp) }
        composable<DeleteWorkProfile> { DeleteWorkProfileScreen(::navigateUp) }

        composable<Applications> { ApplicationsScreen(::navigateUp) }

        composable<UserRestriction> {
            LaunchedEffect(Unit) {
                vm.userRestrictions.value = context.getDPM().getUserRestrictions(receiver)
            }
            UserRestrictionScreen(::navigateUp) { title, items ->
                navController.navigate(UserRestrictionOptions(title, items))
            }
        }
        composable<UserRestrictionOptions>(mapOf(serializableNavTypePair<List<Restriction>>())) {
            UserRestrictionOptionsScreen(it.toRoute(), userRestrictions, ::navigateUp) { id, status ->
                try {
                    val dpm = context.getDPM()
                    if(status) dpm.addUserRestriction(receiver, id)
                    else dpm.clearUserRestriction(receiver, id)
                    @SuppressLint("NewApi")
                    vm.userRestrictions.value = dpm.getUserRestrictions(receiver)
                } catch(_: Exception) {
                    context.showOperationResultToast(false)
                }
            }
        }

        composable<Users> { UsersScreen(::navigateUp, ::navigate) }
        composable<UserInfo> { UserInfoScreen(::navigateUp) }
        composable<UsersOptions> { UsersOptionsScreen(::navigateUp) }
        composable<UserOperation> { UserOperationScreen(::navigateUp) }
        composable<CreateUser> { CreateUserScreen(::navigateUp) }
        composable<ChangeUsername> { ChangeUsernameScreen(::navigateUp) }
        composable<UserSessionMessage> { UserSessionMessageScreen(::navigateUp) }
        composable<AffiliationId> { AffiliationIdScreen(::navigateUp) }

        composable<Password> { PasswordScreen(::navigateUp, ::navigate) }
        composable<PasswordInfo> { PasswordInfoScreen(::navigateUp) }
        composable<ResetPasswordToken> { ResetPasswordTokenScreen(::navigateUp) }
        composable<ResetPassword> { ResetPasswordScreen(::navigateUp) }
        composable<RequiredPasswordComplexity> { RequiredPasswordComplexityScreen(::navigateUp) }
        composable<KeyguardDisabledFeatures> { KeyguardDisabledFeaturesScreen(::navigateUp) }
        composable<RequiredPasswordQuality> { RequiredPasswordQualityScreen(::navigateUp) }

        composable<Settings> { SettingsScreen(::navigateUp, ::navigate) }
        composable<SettingsOptions> { SettingsOptionsScreen(::navigateUp) }
        composable<Appearance> {
            val theme by vm.theme.collectAsStateWithLifecycle()
            AppearanceScreen(::navigateUp, theme) { vm.theme.value = it }
        }
        composable<AuthSettings> { AuthSettingsScreen(::navigateUp) }
        composable<ApiSettings> { ApiSettings(::navigateUp) }
        composable<About> { AboutScreen(::navigateUp) }

        composable<Authenticate>(
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = { fadeOut(animationSpec = tween(400)) }
        ) { AuthenticateScreen(activity, ::navigateUp) }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val sp = SharedPrefs(context)
            if(
                (event == Lifecycle.Event.ON_RESUME && sp.auth && sp.lockInBackground) ||
                (event == Lifecycle.Event.ON_CREATE && sp.auth)
            ) {
                navController.navigate(Authenticate) { launchSingleTop = true }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    LaunchedEffect(Unit) {
        val dpm = context.getDPM()
        val sp = SharedPrefs(context)
        val profileNotActivated = !sp.managedProfileActivated && context.isProfileOwner && (VERSION.SDK_INT < 24 || dpm.isManagedProfile(receiver))
        if(profileNotActivated) {
            dpm.setProfileEnabled(receiver)
            sp.managedProfileActivated = true
            Toast.makeText(context, R.string.work_profile_activated, Toast.LENGTH_SHORT).show()
        }
    }
    DhizukuErrorDialog()
}

@Serializable private object Home

@Composable
private fun HomeScreen(onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var activated by remember { mutableStateOf(false) }
    var activateType by remember { mutableStateOf("") }
    val deviceAdmin = context.isDeviceAdmin
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val refreshStatus by dhizukuErrorStatus.collectAsState()
    LaunchedEffect(refreshStatus) {
        activated = context.isProfileOwner || context.isDeviceOwner
        activateType = if(SharedPrefs(context).dhizuku) context.getString(R.string.dhizuku) + " - " else ""
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
                    .clickable(onClick = { onNavigate(Permissions) })
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
            HomePageItem(R.string.system, R.drawable.android_fill0) { onNavigate(SystemManager) }
            if(deviceOwner || profileOwner) { HomePageItem(R.string.network, R.drawable.wifi_fill0) { onNavigate(Network) } }
            if(
                (VERSION.SDK_INT < 24 && !deviceOwner) || (dpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE) ||
                                (profileOwner && dpm.isManagedProfile(receiver))
                        )
            ) {
                HomePageItem(R.string.work_profile, R.drawable.work_fill0) { onNavigate(WorkProfile) }
            }
            if(deviceOwner || profileOwner) HomePageItem(R.string.applications, R.drawable.apps_fill0) { onNavigate(Applications) }
            if(VERSION.SDK_INT >= 24 && (profileOwner || deviceOwner)) {
                HomePageItem(R.string.user_restriction, R.drawable.person_off) { onNavigate(UserRestriction) }
            }
            HomePageItem(R.string.users,R.drawable.manage_accounts_fill0) { onNavigate(Users) }
            HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0) { onNavigate(Password) }
            HomePageItem(R.string.settings, R.drawable.settings_fill0) { onNavigate(Settings) }
            Spacer(Modifier.padding(vertical = 20.dp))
        }
    }
}

@Composable
fun HomePageItem(name: Int, imgVector: Int, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
        val sp = SharedPrefs(LocalContext.current)
        LaunchedEffect(Unit) {
            sp.dhizuku = false
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
                if(sp.dhizuku) text += "\n" + stringResource(R.string.dhizuku_mode_disabled)
                Text(text)
            }
        )
    }
}
