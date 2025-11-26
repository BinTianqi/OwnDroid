package com.bintianqi.owndroid

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.bintianqi.owndroid.dpm.AddApnSetting
import com.bintianqi.owndroid.dpm.AddApnSettingScreen
import com.bintianqi.owndroid.dpm.AddDelegatedAdmin
import com.bintianqi.owndroid.dpm.AddDelegatedAdminScreen
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
import com.bintianqi.owndroid.dpm.AutoTimePolicy
import com.bintianqi.owndroid.dpm.AutoTimePolicyScreen
import com.bintianqi.owndroid.dpm.AutoTimeZonePolicy
import com.bintianqi.owndroid.dpm.AutoTimeZonePolicyScreen
import com.bintianqi.owndroid.dpm.BlockUninstall
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
import com.bintianqi.owndroid.dpm.CrossProfileWidgetProviders
import com.bintianqi.owndroid.dpm.DelegatedAdmins
import com.bintianqi.owndroid.dpm.DelegatedAdminsScreen
import com.bintianqi.owndroid.dpm.DeleteWorkProfile
import com.bintianqi.owndroid.dpm.DeleteWorkProfileScreen
import com.bintianqi.owndroid.dpm.DeviceInfo
import com.bintianqi.owndroid.dpm.DeviceInfoScreen
import com.bintianqi.owndroid.dpm.DhizukuServerSettings
import com.bintianqi.owndroid.dpm.DhizukuServerSettingsScreen
import com.bintianqi.owndroid.dpm.DisableAccountManagement
import com.bintianqi.owndroid.dpm.DisableAccountManagementScreen
import com.bintianqi.owndroid.dpm.DisableMeteredData
import com.bintianqi.owndroid.dpm.DisableUserControl
import com.bintianqi.owndroid.dpm.EditAppGroup
import com.bintianqi.owndroid.dpm.EditAppGroupScreen
import com.bintianqi.owndroid.dpm.EnableSystemApp
import com.bintianqi.owndroid.dpm.EnableSystemAppScreen
import com.bintianqi.owndroid.dpm.FrpPolicy
import com.bintianqi.owndroid.dpm.FrpPolicyScreen
import com.bintianqi.owndroid.dpm.HardwareMonitor
import com.bintianqi.owndroid.dpm.HardwareMonitorScreen
import com.bintianqi.owndroid.dpm.Hide
import com.bintianqi.owndroid.dpm.InstallExistingApp
import com.bintianqi.owndroid.dpm.InstallExistingAppScreen
import com.bintianqi.owndroid.dpm.InstallSystemUpdate
import com.bintianqi.owndroid.dpm.InstallSystemUpdateScreen
import com.bintianqi.owndroid.dpm.KeepUninstalledPackages
import com.bintianqi.owndroid.dpm.Keyguard
import com.bintianqi.owndroid.dpm.KeyguardDisabledFeatures
import com.bintianqi.owndroid.dpm.KeyguardDisabledFeaturesScreen
import com.bintianqi.owndroid.dpm.KeyguardScreen
import com.bintianqi.owndroid.dpm.LockScreenInfo
import com.bintianqi.owndroid.dpm.LockScreenInfoScreen
import com.bintianqi.owndroid.dpm.LockTaskMode
import com.bintianqi.owndroid.dpm.LockTaskModeScreen
import com.bintianqi.owndroid.dpm.ManageAppGroups
import com.bintianqi.owndroid.dpm.ManageAppGroupsScreen
import com.bintianqi.owndroid.dpm.ManagedConfiguration
import com.bintianqi.owndroid.dpm.ManagedConfigurationScreen
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
import com.bintianqi.owndroid.dpm.PackageFunctionScreen
import com.bintianqi.owndroid.dpm.PackageFunctionScreenWithoutResult
import com.bintianqi.owndroid.dpm.Password
import com.bintianqi.owndroid.dpm.PasswordInfo
import com.bintianqi.owndroid.dpm.PasswordInfoScreen
import com.bintianqi.owndroid.dpm.PasswordScreen
import com.bintianqi.owndroid.dpm.PermissionPolicy
import com.bintianqi.owndroid.dpm.PermissionPolicyScreen
import com.bintianqi.owndroid.dpm.PermissionsManager
import com.bintianqi.owndroid.dpm.PermissionsManagerScreen
import com.bintianqi.owndroid.dpm.PermittedAccessibilityServices
import com.bintianqi.owndroid.dpm.PermittedAsAndImPackages
import com.bintianqi.owndroid.dpm.PermittedInputMethods
import com.bintianqi.owndroid.dpm.PreferentialNetworkService
import com.bintianqi.owndroid.dpm.PreferentialNetworkServiceInfo
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
import com.bintianqi.owndroid.dpm.SystemManager
import com.bintianqi.owndroid.dpm.SystemManagerScreen
import com.bintianqi.owndroid.dpm.SystemOptions
import com.bintianqi.owndroid.dpm.SystemOptionsScreen
import com.bintianqi.owndroid.dpm.SystemUpdatePolicyScreen
import com.bintianqi.owndroid.dpm.TransferOwnership
import com.bintianqi.owndroid.dpm.TransferOwnershipScreen
import com.bintianqi.owndroid.dpm.UninstallApp
import com.bintianqi.owndroid.dpm.UninstallAppScreen
import com.bintianqi.owndroid.dpm.UpdateNetwork
import com.bintianqi.owndroid.dpm.UpdateNetworkScreen
import com.bintianqi.owndroid.dpm.UserInfo
import com.bintianqi.owndroid.dpm.UserInfoScreen
import com.bintianqi.owndroid.dpm.UserOperation
import com.bintianqi.owndroid.dpm.UserOperationScreen
import com.bintianqi.owndroid.dpm.UserRestriction
import com.bintianqi.owndroid.dpm.UserRestrictionEditor
import com.bintianqi.owndroid.dpm.UserRestrictionEditorScreen
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
import com.bintianqi.owndroid.ui.NavTransition
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import kotlinx.serialization.Serializable
import java.util.Locale

@ExperimentalMaterial3Api
class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val context = applicationContext
        val locale = context.resources?.configuration?.locale
        zhCN = locale == Locale.SIMPLIFIED_CHINESE || locale == Locale.CHINESE || locale == Locale.CHINA
        val vm by viewModels<MyViewModel>()
        if (
            VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            val launcher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {}
            launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        registerPackageRemovedReceiver(this) {
            vm.onPackageRemoved(it)
        }
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
    }

}

@ExperimentalMaterial3Api
@Composable
fun Home(vm: MyViewModel, onLock: () -> Unit) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val lifecycleOwner = LocalLifecycleOwner.current
    fun navigateUp() { navController.navigateUp() }
    fun navigate(destination: Any) {
        navController.navigate(destination) {
            launchSingleTop = true
        }
    }
    fun choosePackage() {
        navController.navigate(ApplicationsList(false))
    }
    fun navigateToAppGroups() {
        navController.navigate(ManageAppGroups)
    }
    LaunchedEffect(Unit) {
        if(!Privilege.status.value.activated) {
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
            .pointerInput(Unit) { detectTapGestures(onTap = { focusMgr.clearFocus() }) },
        enterTransition = { NavTransition.enterTransition },
        exitTransition = { NavTransition.exitTransition },
        popEnterTransition = { NavTransition.popEnterTransition },
        popExitTransition = { NavTransition.popExitTransition }
    ) {
        composable<Home> { HomeScreen(::navigate) }
        composable<WorkModes> {
            WorkModesScreen(vm, it.toRoute(), ::navigateUp, {
                navController.navigate(Home) {
                    popUpTo<WorkModes> { inclusive = true }
                }
            }, {
                navController.navigate(WorkModes(false)) {
                    popUpTo(Home) { inclusive = true }
                }
            }, ::navigate)
        }
        composable<DhizukuServerSettings> {
            DhizukuServerSettingsScreen(vm.dhizukuClients, vm::getDhizukuClients,
                vm::updateDhizukuClient, vm::getDhizukuServerEnabled, vm::setDhizukuServerEnabled,
                ::navigateUp)
        }

        composable<DelegatedAdmins> {
            DelegatedAdminsScreen(vm.delegatedAdmins, vm::getDelegatedAdmins, ::navigateUp, ::navigate)
        }
        composable<AddDelegatedAdmin>{
            AddDelegatedAdminScreen(vm.chosenPackage, ::choosePackage, it.toRoute(),
                vm::setDelegatedAdmin,  ::navigateUp)
        }
        composable<DeviceInfo> { DeviceInfoScreen(vm, ::navigateUp) }
        composable<LockScreenInfo> {
            LockScreenInfoScreen(vm::getLockScreenInfo, vm::setLockScreenInfo, ::navigateUp)
        }
        composable<SupportMessage> {
            SupportMessageScreen(vm::getShortSupportMessage, vm::getLongSupportMessage,
                vm::setShortSupportMessage, vm::setLongSupportMessage, ::navigateUp)
        }
        composable<TransferOwnership> {
            TransferOwnershipScreen(vm.deviceAdminReceivers, vm::getDeviceAdminReceivers,
                vm::transferOwnership, ::navigateUp) {
                navController.navigate(WorkModes(false)) {
                    popUpTo(Home) { inclusive = true }
                }
            }
        }

        composable<SystemManager> { SystemManagerScreen(vm, ::navigateUp, ::navigate) }
        composable<SystemOptions> { SystemOptionsScreen(vm, ::navigateUp) }
        composable<Keyguard> {
            KeyguardScreen(vm::setKeyguardDisabled, vm::lockScreen, ::navigateUp)
        }
        composable<HardwareMonitor> {
            HardwareMonitorScreen(vm.hardwareProperties, vm::getHardwareProperties,
                vm::setHpRefreshInterval, ::navigateUp)
        }
        composable<ChangeTime> { ChangeTimeScreen(vm::setTime, ::navigateUp) }
        composable<ChangeTimeZone> { ChangeTimeZoneScreen(vm::setTimeZone, ::navigateUp) }
        composable<AutoTimePolicy> {
            AutoTimePolicyScreen(vm::getAutoTimePolicy, vm::setAutoTimePolicy, ::navigateUp)
        }
        composable<AutoTimeZonePolicy> {
            AutoTimeZonePolicyScreen(vm::getAutoTimeZonePolicy, vm::setAutoTimeZonePolicy,
                ::navigateUp)
        }
        //composable<> { KeyPairs(::navigateUp) }
        composable<ContentProtectionPolicy> {
            ContentProtectionPolicyScreen(vm::getContentProtectionPolicy,
                vm::setContentProtectionPolicy, ::navigateUp)
        }
        composable<PermissionPolicy> {
            PermissionPolicyScreen(vm::getPermissionPolicy, vm::setPermissionPolicy, ::navigateUp)
        }
        composable<MtePolicy> {
            MtePolicyScreen(vm::getMtePolicy, vm::setMtePolicy, ::navigateUp)
        }
        composable<NearbyStreamingPolicy> {
            NearbyStreamingPolicyScreen(vm::getNsAppPolicy, vm::setNsAppPolicy,
                vm::getNsNotificationPolicy, vm::setNsNotificationPolicy, ::navigateUp)
        }
        composable<LockTaskMode> {
            LockTaskModeScreen(vm.chosenPackage, ::choosePackage, vm.lockTaskPackages,
                vm::getLockTaskPackages, vm::setLockTaskPackage, vm::startLockTaskMode,
                vm:: getLockTaskFeatures, vm::setLockTaskFeatures, ::navigateUp)
        }
        composable<CaCert> {
            CaCertScreen(vm.installedCaCerts, vm::getCaCerts, vm.selectedCaCert, vm::selectCaCert, vm::installCaCert, vm::parseCaCert,
                vm::exportCaCert, vm::uninstallCaCert, vm::uninstallAllCaCerts, ::navigateUp)
        }
        composable<SecurityLogging> {
            SecurityLoggingScreen(vm::getSecurityLoggingEnabled, vm::setSecurityLoggingEnabled,
                vm::exportSecurityLogs, vm::getSecurityLogsCount, vm::deleteSecurityLogs,
                vm::getPreRebootSecurityLogs, vm::exportPreRebootSecurityLogs, ::navigateUp)
        }
        composable<DisableAccountManagement> {
            DisableAccountManagementScreen(vm.mdAccountTypes, vm::getMdAccountTypes,
                vm::setMdAccountType, ::navigateUp)
        }
        composable<SetSystemUpdatePolicy> {
            SystemUpdatePolicyScreen(vm::getSystemUpdatePolicy, vm::setSystemUpdatePolicy,
                vm::getPendingSystemUpdate, ::navigateUp)
        }
        composable<InstallSystemUpdate> {
            InstallSystemUpdateScreen(vm::installSystemUpdate, ::navigateUp)
        }
        composable<FrpPolicy> {
            FrpPolicyScreen(vm.getFrpPolicy(), vm::setFrpPolicy, ::navigateUp)
        }
        composable<WipeData> { WipeDataScreen(vm::wipeData, ::navigateUp) }

        composable<Network> { NetworkScreen(::navigateUp, ::navigate) }
        composable<WiFi> {
            WifiScreen(vm, ::navigateUp, ::navigate) { navController.navigate(UpdateNetwork(it)) }
        }
        composable<NetworkOptions> {
            NetworkOptionsScreen(vm::getLanEnabled, vm::setLanEnabled, ::navigateUp)
        }
        composable<UpdateNetwork> {
            val info = vm.configuredNetworks.collectAsStateWithLifecycle().value[
                (it.toRoute() as UpdateNetwork).index
            ]
            UpdateNetworkScreen(info, vm::setWifi, ::navigateUp)
        }
        composable<WifiSecurityLevel> {
            WifiSecurityLevelScreen(vm::getMinimumWifiSecurityLevel,
                vm::setMinimumWifiSecurityLevel, ::navigateUp)
        }
        composable<WifiSsidPolicyScreen> {
            WifiSsidPolicyScreen(vm::getSsidPolicy, vm::setSsidPolicy, ::navigateUp)
        }
        composable<QueryNetworkStats> {
            NetworkStatsScreen(vm.chosenPackage, ::choosePackage, vm::getPackageUid,
                vm::queryNetworkStats, ::navigateUp) { navController.navigate(NetworkStatsViewer) }
        }
        composable<NetworkStatsViewer> {
            NetworkStatsViewerScreen(vm.networkStatsData, vm::clearNetworkStats, ::navigateUp)
        }
        composable<PrivateDns> {
            PrivateDnsScreen(vm::getPrivateDns, vm::setPrivateDns, ::navigateUp)
        }
        composable<AlwaysOnVpnPackage> {
            AlwaysOnVpnPackageScreen(vm::getAlwaysOnVpnPackage, vm::getAlwaysOnVpnLockdown,
                vm::setAlwaysOnVpn, vm.chosenPackage, ::choosePackage, ::navigateUp)
        }
        composable<RecommendedGlobalProxy> {
            RecommendedGlobalProxyScreen(vm::setRecommendedGlobalProxy, ::navigateUp)
        }
        composable<NetworkLogging> {
            NetworkLoggingScreen(vm::getNetworkLoggingEnabled, vm::setNetworkLoggingEnabled,
                vm::getNetworkLogsCount, vm::exportNetworkLogs, vm::deleteNetworkLogs, ::navigateUp)
        }
        //composable<WifiAuthKeypair> { WifiAuthKeypairScreen(::navigateUp) }
        composable<PreferentialNetworkService> {
            PreferentialNetworkServiceScreen(vm::getPnsEnabled, vm::setPnsEnabled, vm.pnsConfigs,
                vm::getPnsConfigs, ::navigateUp, ::navigate)
        }
        composable<AddPreferentialNetworkServiceConfig> {
            val info = vm.pnsConfigs.collectAsStateWithLifecycle().value.getOrNull(
                it.toRoute<AddPreferentialNetworkServiceConfig>().index
            ) ?: PreferentialNetworkServiceInfo()
            AddPreferentialNetworkServiceConfigScreen(info, vm::setPnsConfig, ::navigateUp)
        }
        composable<OverrideApn> {
            OverrideApnScreen(vm.apnConfigs, vm::getApnConfigs, vm::getApnEnabled,
                vm::setApnEnabled, ::navigateUp) { navController.navigate(AddApnSetting(it)) }
        }
        composable<AddApnSetting> {
            val origin = vm.apnConfigs.collectAsStateWithLifecycle().value.getOrNull((it.toRoute() as AddApnSetting).index)
            AddApnSettingScreen(vm::setApnConfig, vm::removeApnConfig, origin, ::navigateUp)
        }

        composable<WorkProfile> { WorkProfileScreen(::navigateUp, ::navigate) }
        composable<OrganizationOwnedProfile> {
            OrganizationOwnedProfileScreen(vm::activateOrgProfileByShizuku, ::navigateUp)
        }
        composable<CreateWorkProfile> {
            CreateWorkProfileScreen(vm::createWorkProfile, ::navigateUp)
        }
        composable<SuspendPersonalApp> {
            SuspendPersonalAppScreen(
                vm::getPersonalAppsSuspendedReason, vm::setPersonalAppsSuspended,
                vm::getProfileMaxTimeOff, vm::setProfileMaxTimeOff, ::navigateUp
            )
        }
        composable<CrossProfileIntentFilter> {
            CrossProfileIntentFilterScreen(vm::addCrossProfileIntentFilter, ::navigateUp)
        }
        composable<DeleteWorkProfile> { DeleteWorkProfileScreen(vm::wipeData, ::navigateUp) }

        composable<ApplicationsList> {
            val canSwitchView = (it.toRoute() as ApplicationsList).canSwitchView
            AppChooserScreen(
                canSwitchView, vm.installedPackages, vm.refreshPackagesProgress, { name ->
                if (canSwitchView) {
                    if (name == null) {
                        navigateUp()
                    } else {
                        navigate(ApplicationDetails(name))
                    }
                } else {
                    if (name != null) vm.chosenPackage.trySend(name)
                    navigateUp()
                }
            }, {
                SP.applicationsListView = false
                navController.navigate(ApplicationsFeatures) {
                    popUpTo(Home)
                }
            }, vm::refreshPackageList)
        }
        composable<ApplicationsFeatures> {
            ApplicationsFeaturesScreen(::navigateUp, ::navigate) {
                SP.applicationsListView = true
                navController.navigate(ApplicationsList(true)) {
                    popUpTo(Home)
                }
            }
        }
        composable<ApplicationDetails> {
            ApplicationDetailsScreen(it.toRoute(), vm, ::navigateUp, ::navigate)
        }
        composable<Suspend> {
            PackageFunctionScreen(R.string.suspend, vm.suspendedPackages, vm::getSuspendedPackaged,
                vm::setPackageSuspended, ::navigateUp, vm.chosenPackage, ::choosePackage,
                ::navigateToAppGroups, vm.appGroups, R.string.info_suspend_app)
        }
        composable<Hide> {
            PackageFunctionScreen(R.string.hide, vm.hiddenPackages, vm::getHiddenPackages,
                vm::setPackageHidden, ::navigateUp, vm.chosenPackage, ::choosePackage, ::navigateToAppGroups, vm.appGroups)
        }
        composable<BlockUninstall> {
            PackageFunctionScreenWithoutResult(R.string.block_uninstall, vm.ubPackages,
                vm::getUbPackages, vm::setPackageUb, ::navigateUp, vm.chosenPackage, ::choosePackage, ::navigateToAppGroups, vm.appGroups)
        }
        composable<DisableUserControl> {
            PackageFunctionScreenWithoutResult(R.string.disable_user_control, vm.ucdPackages,
                vm::getUcdPackages, vm::setPackageUcd, ::navigateUp, vm.chosenPackage,
                ::choosePackage, ::navigateToAppGroups, vm.appGroups, R.string.info_disable_user_control)
        }
        composable<PermissionsManager> {
            PermissionsManagerScreen(vm.packagePermissions, vm::getPackagePermissions,
                vm::setPackagePermission, ::navigateUp, it.toRoute(), vm.chosenPackage, ::choosePackage)
        }
        composable<DisableMeteredData> {
            PackageFunctionScreen(R.string.disable_metered_data, vm.mddPackages,
                vm::getMddPackages, vm::setPackageMdd, ::navigateUp, vm.chosenPackage,
                ::choosePackage, ::navigateToAppGroups, vm.appGroups)
        }
        composable<ClearAppStorage> {
            ClearAppStorageScreen(vm.chosenPackage, ::choosePackage, vm::clearAppData, ::navigateUp)
        }
        composable<UninstallApp> {
            UninstallAppScreen(vm.chosenPackage, ::choosePackage, vm::uninstallPackage, ::navigateUp)
        }
        composable<KeepUninstalledPackages> {
            PackageFunctionScreenWithoutResult(R.string.keep_uninstalled_packages, vm.kuPackages,
                vm::getKuPackages, vm::setPackageKu, ::navigateUp, vm.chosenPackage,
                ::choosePackage, ::navigateToAppGroups, vm.appGroups,
                R.string.info_keep_uninstalled_apps)
        }
        composable<InstallExistingApp> {
            InstallExistingAppScreen(vm.chosenPackage, ::choosePackage,
                vm::installExistingApp, ::navigateUp)
        }
        composable<CrossProfilePackages> {
            PackageFunctionScreenWithoutResult(R.string.cross_profile_apps, vm.cpPackages,
                vm::getCpPackages, vm::setPackageCp, ::navigateUp, vm.chosenPackage,
                ::choosePackage, ::navigateToAppGroups, vm.appGroups)
        }
        composable<CrossProfileWidgetProviders> {
            PackageFunctionScreen(R.string.cross_profile_widget, vm.cpwProviders,
                vm::getCpwProviders, vm::setCpwProvider, ::navigateUp, vm.chosenPackage,
                ::choosePackage, ::navigateToAppGroups, vm.appGroups)
        }
        composable<CredentialManagerPolicy> {
            CredentialManagerPolicyScreen(vm.chosenPackage, ::choosePackage,
                vm.cmPackages, vm::getCmPolicy, vm::setCmPackage, vm::setCmPolicy, ::navigateUp)
        }
        composable<PermittedAccessibilityServices> {
            PermittedAsAndImPackages(R.string.permitted_accessibility_services,
                R.string.system_accessibility_always_allowed, vm.chosenPackage, ::choosePackage,
                vm.pasPackages, vm::getPasPackages, vm::setPasPackage, vm::setPasPolicy, ::navigateUp)
        }
        composable<PermittedInputMethods> {
            PermittedAsAndImPackages(R.string.permitted_ime, R.string.system_ime_always_allowed,
                vm.chosenPackage, ::choosePackage, vm.pimPackages, vm::getPimPackages,
                vm::setPimPackage, vm::setPimPolicy, ::navigateUp)
        }
        composable<EnableSystemApp> {
            EnableSystemAppScreen(vm.chosenPackage, ::choosePackage, vm::enableSystemApp, ::navigateUp)
        }
        composable<SetDefaultDialer> {
            SetDefaultDialerScreen(vm.chosenPackage, ::choosePackage, vm::setDefaultDialer, ::navigateUp)
        }
        composable<ManagedConfiguration> {
            ManagedConfigurationScreen(
                it.toRoute(), vm.appRestrictions, vm::setAppRestrictions,
                vm::clearAppRestrictions, ::navigateUp
            )
        }
        composable<ManageAppGroups> {
            ManageAppGroupsScreen(
                vm.appGroups,
                { id, name, apps -> navController.navigate(EditAppGroup(id, name, apps)) },
                ::navigateUp
            )
        }
        composable<EditAppGroup> {
            EditAppGroupScreen(
                it.toRoute(), vm::getAppInfo, ::navigateUp, vm::setAppGroup,
                vm::deleteAppGroup, ::choosePackage, vm.chosenPackage
            )
        }

        composable<UserRestriction> {
            UserRestrictionScreen(vm::getUserRestrictions, ::navigateUp, ::navigate)
        }
        composable<UserRestrictionEditor> {
            UserRestrictionEditorScreen(vm.userRestrictions, vm::setUserRestriction, ::navigateUp)
        }
        composable<UserRestrictionOptions> {
            UserRestrictionOptionsScreen(it.toRoute(), vm.userRestrictions,
                vm::setUserRestriction, vm::createUserRestrictionShortcut, ::navigateUp)
        }

        composable<Users> { UsersScreen(vm, ::navigateUp, ::navigate) }
        composable<UserInfo> { UserInfoScreen(vm::getUserInformation, ::navigateUp) }
        composable<UsersOptions> {
            UsersOptionsScreen(vm::getLogoutEnabled, vm::setLogoutEnabled, ::navigateUp)
        }
        composable<UserOperation> {
            UserOperationScreen(vm::getUserIdentifiers, vm::doUserOperation,
                vm::createUserOperationShortcut, ::navigateUp)
        }
        composable<CreateUser> { CreateUserScreen(vm::createUser, ::navigateUp) }
        composable<ChangeUsername> { ChangeUsernameScreen(vm::setProfileName, ::navigateUp) }
        composable<UserSessionMessage> {
            UserSessionMessageScreen(vm::getUserSessionMessages, vm::setStartUserSessionMessage,
                vm::setEndUserSessionMessage, ::navigateUp)
        }
        composable<AffiliationId> {
            AffiliationIdScreen(vm.affiliationIds, vm::getAffiliationIds, vm::setAffiliationId,
                ::navigateUp)
        }

        composable<Password> { PasswordScreen(vm, ::navigateUp, ::navigate) }
        composable<PasswordInfo> {
            PasswordInfoScreen(vm::getPasswordComplexity, vm::isPasswordComplexitySufficient,
                vm::isUsingUnifiedPassword, ::navigateUp)
        }
        composable<ResetPasswordToken> {
            ResetPasswordTokenScreen(vm::getRpTokenState, vm::setRpToken,
                vm::createActivateRpTokenIntent, vm::clearRpToken, ::navigateUp)
        }
        composable<ResetPassword> { ResetPasswordScreen(vm::resetPassword, ::navigateUp) }
        composable<RequiredPasswordComplexity> {
            RequiredPasswordComplexityScreen(vm::getRequiredPasswordComplexity,
                vm::setRequiredPasswordComplexity, ::navigateUp)
        }
        composable<KeyguardDisabledFeatures> {
            KeyguardDisabledFeaturesScreen(vm::getKeyguardDisableConfig,
                vm::setKeyguardDisableConfig, ::navigateUp)
        }
        composable<RequiredPasswordQuality> { RequiredPasswordQualityScreen(::navigateUp) }

        composable<Settings> { SettingsScreen(::navigateUp, ::navigate) }
        composable<SettingsOptions> {
            SettingsOptionsScreen(vm::getDisplayDangerousFeatures, vm::getShortcutsEnabled,
                vm::setDisplayDangerousFeatures, vm::setShortcutsEnabled, ::navigateUp)
        }
        composable<Appearance> {
            AppearanceScreen(::navigateUp, vm.theme, vm::changeTheme)
        }
        composable<AppLockSettings> {
            AppLockSettingsScreen(vm.getAppLockConfig(), vm::setAppLockConfig, ::navigateUp)
        }
        composable<ApiSettings> {
            ApiSettings(vm::getApiEnabled, vm::setApiKey, ::navigateUp)
        }
        composable<Notifications> {
            NotificationsScreen(vm.enabledNotifications, vm::getEnabledNotifications,
                vm::setNotificationEnabled, ::navigateUp)
        }
        composable<About> { AboutScreen(::navigateUp) }
    }
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (
                (event == Lifecycle.Event.ON_CREATE && !SP.lockPasswordHash.isNullOrEmpty()) ||
                (event == Lifecycle.Event.ON_RESUME && SP.lockWhenLeaving)
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
        val profileNotActivated = !SP.managedProfileActivated && Privilege.status.value.work
        if(profileNotActivated) {
            Privilege.DPM.setProfileEnabled(Privilege.DAR)
            SP.managedProfileActivated = true
            context.popToast(R.string.work_profile_activated)
        }
    }
    DhizukuErrorDialog {
        dhizukuErrorStatus.value = 0
        Privilege.updateStatus()
        navController.navigate(WorkModes(false)) {
            popUpTo<Home> { inclusive = true }
            launchSingleTop = true
        }
    }
}

@Serializable private object Home

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onNavigate: (Any) -> Unit) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
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
        },
        contentWindowInsets = adaptiveInsets()
    ) {
        Column(Modifier
            .fillMaxSize()
            .padding(it)
            .verticalScroll(rememberScrollState())) {
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
                    onNavigate(if(SP.applicationsListView) ApplicationsList(true) else ApplicationsFeatures)
                }
                if(VERSION.SDK_INT >= 24) {
                    HomePageItem(R.string.user_restriction, R.drawable.person_off) { onNavigate(UserRestriction) }
                }
                HomePageItem(R.string.users,R.drawable.manage_accounts_fill0) { onNavigate(Users) }
                HomePageItem(R.string.password_and_keyguard, R.drawable.password_fill0) { onNavigate(Password) }
            }
            Spacer(Modifier.height(BottomPadding))
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
private fun DhizukuErrorDialog(onClose: () -> Unit) {
    val status by dhizukuErrorStatus.collectAsState()
    if (status != 0) {
        LaunchedEffect(Unit) {
            SP.dhizuku = false
        }
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                TextButton(onClose) {
                    Text(stringResource(R.string.confirm))
                }
            },
            title = { Text(stringResource(R.string.dhizuku)) },
            text = {
                val text = stringResource(
                    when(status){
                        1 -> R.string.failed_to_init_dhizuku
                        2 -> R.string.dhizuku_permission_not_granted
                        else -> R.string.failed_to_init_dhizuku
                    }
                )
                Text(text)
            },
            properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
        )
    }
}
