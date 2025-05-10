package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.os.Build.VERSION
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import com.bintianqi.owndroid.dpm.AddApnSetting
import com.bintianqi.owndroid.dpm.AddApnSettingScreen
import com.bintianqi.owndroid.dpm.AddDelegatedAdmin
import com.bintianqi.owndroid.dpm.AddDelegatedAdminScreen
import com.bintianqi.owndroid.dpm.AddNetwork
import com.bintianqi.owndroid.dpm.AddNetworkScreen
import com.bintianqi.owndroid.dpm.AddPreferentialNetworkServiceConfig
import com.bintianqi.owndroid.dpm.AddPreferentialNetworkServiceConfigScreen
import com.bintianqi.owndroid.dpm.AffiliationId
import com.bintianqi.owndroid.dpm.AffiliationIdScreen
import com.bintianqi.owndroid.dpm.AlwaysOnVpnPackage
import com.bintianqi.owndroid.dpm.AlwaysOnVpnPackageScreen
import com.bintianqi.owndroid.dpm.ApplicationDetails
import com.bintianqi.owndroid.dpm.ApplicationDetailsScreen
import com.bintianqi.owndroid.dpm.ApplicationsFeatures
import com.bintianqi.owndroid.dpm.ApplicationsFeaturesScreen
import com.bintianqi.owndroid.dpm.BlockUninstall
import com.bintianqi.owndroid.dpm.BlockUninstallScreen
import com.bintianqi.owndroid.dpm.CaCert
import com.bintianqi.owndroid.dpm.CaCertScreen
import com.bintianqi.owndroid.dpm.ChangeTime
import com.bintianqi.owndroid.dpm.ChangeTimeScreen
import com.bintianqi.owndroid.dpm.ChangeTimeZone
import com.bintianqi.owndroid.dpm.ChangeTimeZoneScreen
import com.bintianqi.owndroid.dpm.ChangeUsername
import com.bintianqi.owndroid.dpm.ChangeUsernameScreen
import com.bintianqi.owndroid.dpm.ClearAppStorage
import com.bintianqi.owndroid.dpm.ClearAppStorageScreen
import com.bintianqi.owndroid.dpm.ContentProtectionPolicy
import com.bintianqi.owndroid.dpm.ContentProtectionPolicyScreen
import com.bintianqi.owndroid.dpm.CreateUser
import com.bintianqi.owndroid.dpm.CreateUserScreen
import com.bintianqi.owndroid.dpm.CreateWorkProfile
import com.bintianqi.owndroid.dpm.CreateWorkProfileScreen
import com.bintianqi.owndroid.dpm.CredentialManagerPolicy
import com.bintianqi.owndroid.dpm.CredentialManagerPolicyScreen
import com.bintianqi.owndroid.dpm.CrossProfileIntentFilter
import com.bintianqi.owndroid.dpm.CrossProfileIntentFilterScreen
import com.bintianqi.owndroid.dpm.CrossProfilePackages
import com.bintianqi.owndroid.dpm.CrossProfilePackagesScreen
import com.bintianqi.owndroid.dpm.CrossProfileWidgetProviders
import com.bintianqi.owndroid.dpm.CrossProfileWidgetProvidersScreen
import com.bintianqi.owndroid.dpm.DelegatedAdmins
import com.bintianqi.owndroid.dpm.DelegatedAdminsScreen
import com.bintianqi.owndroid.dpm.DeleteWorkProfile
import com.bintianqi.owndroid.dpm.DeleteWorkProfileScreen
import com.bintianqi.owndroid.dpm.DeviceInfo
import com.bintianqi.owndroid.dpm.DeviceInfoScreen
import com.bintianqi.owndroid.dpm.DisableAccountManagement
import com.bintianqi.owndroid.dpm.DisableAccountManagementScreen
import com.bintianqi.owndroid.dpm.DisableMeteredData
import com.bintianqi.owndroid.dpm.DisableMeteredDataScreen
import com.bintianqi.owndroid.dpm.DisableUserControl
import com.bintianqi.owndroid.dpm.DisableUserControlScreen
import com.bintianqi.owndroid.dpm.EnableSystemApp
import com.bintianqi.owndroid.dpm.EnableSystemAppScreen
import com.bintianqi.owndroid.dpm.FrpPolicy
import com.bintianqi.owndroid.dpm.FrpPolicyScreen
import com.bintianqi.owndroid.dpm.HardwareMonitor
import com.bintianqi.owndroid.dpm.HardwareMonitorScreen
import com.bintianqi.owndroid.dpm.Hide
import com.bintianqi.owndroid.dpm.HideScreen
import com.bintianqi.owndroid.dpm.InstallExistingApp
import com.bintianqi.owndroid.dpm.InstallExistingAppScreen
import com.bintianqi.owndroid.dpm.InstallSystemUpdate
import com.bintianqi.owndroid.dpm.InstallSystemUpdateScreen
import com.bintianqi.owndroid.dpm.KeepUninstalledPackages
import com.bintianqi.owndroid.dpm.KeepUninstalledPackagesScreen
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
import com.bintianqi.owndroid.dpm.PermissionsManager
import com.bintianqi.owndroid.dpm.PermissionsManagerScreen
import com.bintianqi.owndroid.dpm.PermittedAccessibilityServices
import com.bintianqi.owndroid.dpm.PermittedAccessibilityServicesScreen
import com.bintianqi.owndroid.dpm.PermittedInputMethods
import com.bintianqi.owndroid.dpm.PermittedInputMethodsScreen
import com.bintianqi.owndroid.dpm.PreferentialNetworkService
import com.bintianqi.owndroid.dpm.PreferentialNetworkServiceScreen
import com.bintianqi.owndroid.dpm.PrivateDns
import com.bintianqi.owndroid.dpm.PrivateDnsScreen
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
import com.bintianqi.owndroid.dpm.SetDefaultDialer
import com.bintianqi.owndroid.dpm.SetDefaultDialerScreen
import com.bintianqi.owndroid.dpm.SetSystemUpdatePolicy
import com.bintianqi.owndroid.dpm.SupportMessage
import com.bintianqi.owndroid.dpm.SupportMessageScreen
import com.bintianqi.owndroid.dpm.Suspend
import com.bintianqi.owndroid.dpm.SuspendPersonalApp
import com.bintianqi.owndroid.dpm.SuspendPersonalAppScreen
import com.bintianqi.owndroid.dpm.SuspendScreen
import com.bintianqi.owndroid.dpm.SystemManager
import com.bintianqi.owndroid.dpm.SystemManagerScreen
import com.bintianqi.owndroid.dpm.SystemOptions
import com.bintianqi.owndroid.dpm.SystemOptionsScreen
import com.bintianqi.owndroid.dpm.SystemUpdatePolicyScreen
import com.bintianqi.owndroid.dpm.TransferOwnership
import com.bintianqi.owndroid.dpm.TransferOwnershipScreen
import com.bintianqi.owndroid.dpm.UninstallApp
import com.bintianqi.owndroid.dpm.UninstallAppScreen
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
import com.bintianqi.owndroid.dpm.WorkModes
import com.bintianqi.owndroid.dpm.WorkModesScreen
import com.bintianqi.owndroid.dpm.WorkProfile
import com.bintianqi.owndroid.dpm.WorkProfileScreen
import com.bintianqi.owndroid.dpm.dhizukuErrorStatus
import com.bintianqi.owndroid.dpm.dhizukuPermissionGranted
import com.bintianqi.owndroid.dpm.getDPM
import com.bintianqi.owndroid.dpm.getReceiver
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
            var appLockDialog by rememberSaveable { mutableStateOf(false) }
            val theme by vm.theme.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                Home(vm) { appLockDialog = true }
                if (appLockDialog) {
                    AppLockDialog({ appLockDialog = false }) { moveTaskToBack(true) }
                }
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
        updatePrivilege(this)
    }

}

@ExperimentalMaterial3Api
@Composable
fun Home(vm: MyViewModel, onLock: () -> Unit) {
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
    LaunchedEffect(Unit) {
        val privilege = myPrivilege.value
        if(!privilege.device && !privilege.profile) {
            navController.navigate(WorkModes(false)) {
                popUpTo<Home> { inclusive = true }
            }
        }
    }
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
        composable<WorkModes> {
            WorkModesScreen(it.toRoute(), ::navigateUp, {
                navController.navigate(Home) {
                    popUpTo<WorkModes> { inclusive = true }
                }
            }, {
                navController.navigate(WorkModes(false)) {
                    popUpTo<Home> { inclusive = true }
                }
            }, {
                navController.navigate(it)
            })
        }

        composable<DelegatedAdmins> { DelegatedAdminsScreen(::navigateUp, ::navigate) }
        composable<AddDelegatedAdmin>{ AddDelegatedAdminScreen(it.toRoute(), ::navigateUp) }
        composable<DeviceInfo> { DeviceInfoScreen(::navigateUp) }
        composable<LockScreenInfo> { LockScreenInfoScreen(::navigateUp) }
        composable<SupportMessage> { SupportMessageScreen(::navigateUp) }
        composable<TransferOwnership> {
            TransferOwnershipScreen(::navigateUp) {
                navController.navigate(WorkModes(false)) {
                    popUpTo(Home) { inclusive = true }
                }
            }
        }

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
        composable<WiFi> { WifiScreen(::navigateUp, { navController.navigate(it) }) { navController.navigate(AddNetwork, it)} }
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
        composable<PreferentialNetworkService> { PreferentialNetworkServiceScreen(::navigateUp, ::navigate) }
        composable<AddPreferentialNetworkServiceConfig> { AddPreferentialNetworkServiceConfigScreen(it.toRoute(), ::navigateUp) }
        composable<OverrideApn> { OverrideApnScreen(::navigateUp) { navController.navigate(AddApnSetting, it) } }
        composable<AddApnSetting> { AddApnSettingScreen(it.arguments?.getParcelable("setting"), ::navigateUp) }

        composable<WorkProfile> { WorkProfileScreen(::navigateUp, ::navigate) }
        composable<OrganizationOwnedProfile> { OrganizationOwnedProfileScreen(::navigateUp) }
        composable<CreateWorkProfile> { CreateWorkProfileScreen(::navigateUp) }
        composable<SuspendPersonalApp> { SuspendPersonalAppScreen(::navigateUp) }
        composable<CrossProfileIntentFilter> { CrossProfileIntentFilterScreen(::navigateUp) }
        composable<DeleteWorkProfile> { DeleteWorkProfileScreen(::navigateUp) }

        composable<ApplicationsList> {
            AppChooserScreen(it.toRoute(), { dest ->
                if(dest == null) navigateUp() else navigate(ApplicationDetails(dest))
            }, {
                SharedPrefs(context).applicationsListView = false
                navController.navigate(ApplicationsFeatures) {
                    popUpTo(Home)
                }
            })
        }
        composable<ApplicationsFeatures> {
            ApplicationsFeaturesScreen(::navigateUp, ::navigate) {
                SharedPrefs(context).applicationsListView = true
                navController.navigate(ApplicationsList(true)) {
                    popUpTo(Home)
                }
            }
        }
        composable<ApplicationDetails> { ApplicationDetailsScreen(it.toRoute(), ::navigateUp, ::navigate) }
        composable<Suspend> { SuspendScreen(::navigateUp) }
        composable<Hide> { HideScreen(::navigateUp) }
        composable<BlockUninstall> { BlockUninstallScreen(::navigateUp) }
        composable<DisableUserControl> { DisableUserControlScreen(::navigateUp) }
        composable<PermissionsManager> { PermissionsManagerScreen(::navigateUp, it.toRoute()) }
        composable<DisableMeteredData> { DisableMeteredDataScreen(::navigateUp) }
        composable<ClearAppStorage> { ClearAppStorageScreen(::navigateUp) }
        composable<UninstallApp> { UninstallAppScreen(::navigateUp) }
        composable<KeepUninstalledPackages> { KeepUninstalledPackagesScreen(::navigateUp) }
        composable<InstallExistingApp> { InstallExistingAppScreen(::navigateUp) }
        composable<CrossProfilePackages> { CrossProfilePackagesScreen(::navigateUp) }
        composable<CrossProfileWidgetProviders> { CrossProfileWidgetProvidersScreen(::navigateUp) }
        composable<CredentialManagerPolicy> { CredentialManagerPolicyScreen(::navigateUp) }
        composable<PermittedAccessibilityServices> { PermittedAccessibilityServicesScreen(::navigateUp) }
        composable<PermittedInputMethods> { PermittedInputMethodsScreen(::navigateUp) }
        composable<EnableSystemApp> { EnableSystemAppScreen(::navigateUp) }
        composable<SetDefaultDialer> { SetDefaultDialerScreen(::navigateUp) }

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
        composable<AppLockSettings> { AppLockSettingsScreen(::navigateUp) }
        composable<ApiSettings> { ApiSettings(::navigateUp) }
        composable<Notifications> { NotificationsScreen(::navigateUp) }
        composable<About> { AboutScreen(::navigateUp) }
    }
    DisposableEffect(lifecycleOwner) {
        val sp = SharedPrefs(context)
        val observer = LifecycleEventObserver { _, event ->
            if (
                (event == Lifecycle.Event.ON_CREATE && !sp.lockPasswordHash.isNullOrEmpty()) ||
                (event == Lifecycle.Event.ON_RESUME && sp.lockWhenLeaving)
            ) {
                onLock()
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
        val profileNotActivated = !sp.managedProfileActivated && myPrivilege.value.work
        if(profileNotActivated) {
            dpm.setProfileEnabled(receiver)
            sp.managedProfileActivated = true
            Toast.makeText(context, R.string.work_profile_activated, Toast.LENGTH_SHORT).show()
        }
    }
    DhizukuErrorDialog()
}

@Serializable private object Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val privilege by myPrivilege.collectAsStateWithLifecycle()
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton({ onNavigate(WorkModes(true)) }) { Icon(painterResource(R.drawable.security_fill0), null) }
                    IconButton({ onNavigate(Settings) }) { Icon(Icons.Default.Settings, null) }
                },
                scrollBehavior = sb
            )
        }
    ) {
        Column(Modifier.fillMaxSize().padding(it).verticalScroll(rememberScrollState())) {
            if(privilege.device || privilege.profile) {
                HomePageItem(R.string.system, R.drawable.android_fill0) { onNavigate(SystemManager) }
                HomePageItem(R.string.network, R.drawable.wifi_fill0) { onNavigate(Network) }
            }
            if(privilege.work) {
                HomePageItem(R.string.work_profile, R.drawable.work_fill0) {
                    onNavigate(WorkProfile)
                }
            }
            if(privilege.device || privilege.profile) {
                HomePageItem(R.string.applications, R.drawable.apps_fill0) {
                    onNavigate(if(SharedPrefs(context).applicationsListView) ApplicationsList(true) else ApplicationsFeatures)
                }
                if(VERSION.SDK_INT >= 24) {
                    HomePageItem(R.string.user_restriction, R.drawable.person_off) { onNavigate(UserRestriction) }
                }
                HomePageItem(R.string.users,R.drawable.manage_accounts_fill0) { onNavigate(Users) }
                HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0) { onNavigate(Password) }
            }
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
