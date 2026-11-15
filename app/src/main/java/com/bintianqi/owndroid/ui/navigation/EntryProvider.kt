package com.bintianqi.owndroid.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.metadata
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import androidx.navigation3.ui.NavDisplay
import com.bintianqi.owndroid.AppContainer
import com.bintianqi.owndroid.MyViewModelFactory
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.applications.AppChooserFilter
import com.bintianqi.owndroid.feature.applications.AppChooserMode
import com.bintianqi.owndroid.feature.applications.AppChooserScreen
import com.bintianqi.owndroid.feature.applications.AppChooserViewModel
import com.bintianqi.owndroid.feature.applications.AppDetailsViewModel
import com.bintianqi.owndroid.feature.applications.AppFeaturesViewModel
import com.bintianqi.owndroid.feature.applications.AppFilterState
import com.bintianqi.owndroid.feature.applications.AppGroupsScreen
import com.bintianqi.owndroid.feature.applications.AppPermissionsManagerScreen
import com.bintianqi.owndroid.feature.applications.ApplicationDetailsScreen
import com.bintianqi.owndroid.feature.applications.ApplicationsFeaturesScreen
import com.bintianqi.owndroid.feature.applications.ClearAppStorageScreen
import com.bintianqi.owndroid.feature.applications.CredentialManagerPolicyScreen
import com.bintianqi.owndroid.feature.applications.EditAppGroupScreen
import com.bintianqi.owndroid.feature.applications.EnableSystemAppScreen
import com.bintianqi.owndroid.feature.applications.InstallExistingAppScreen
import com.bintianqi.owndroid.feature.applications.ManagedConfigurationBooleanEditorScreen
import com.bintianqi.owndroid.feature.applications.ManagedConfigurationListEditorScreen
import com.bintianqi.owndroid.feature.applications.ManagedConfigurationScreen
import com.bintianqi.owndroid.feature.applications.ManagedConfigurationValueEditorScreen
import com.bintianqi.owndroid.feature.applications.ManagedConfigurationViewModel
import com.bintianqi.owndroid.feature.applications.PackageFunctionScreen
import com.bintianqi.owndroid.feature.applications.PermissionDetailScreen
import com.bintianqi.owndroid.feature.applications.PermissionManagerScreen
import com.bintianqi.owndroid.feature.applications.PermittedAsAndImPackagesScreen
import com.bintianqi.owndroid.feature.applications.SetDefaultDialerScreen
import com.bintianqi.owndroid.feature.applications.UninstallAppScreen
import com.bintianqi.owndroid.feature.network.AddApnSettingScreen
import com.bintianqi.owndroid.feature.network.AddPreferentialNetworkServiceConfigScreen
import com.bintianqi.owndroid.feature.network.AlwaysOnVpnPackageScreen
import com.bintianqi.owndroid.feature.network.NetworkLoggingScreen
import com.bintianqi.owndroid.feature.network.NetworkOptionsScreen
import com.bintianqi.owndroid.feature.network.NetworkScreen
import com.bintianqi.owndroid.feature.network.NetworkStatsScreen
import com.bintianqi.owndroid.feature.network.NetworkStatsViewerScreen
import com.bintianqi.owndroid.feature.network.OverrideApnScreen
import com.bintianqi.owndroid.feature.network.PreferentialNetworkServiceScreen
import com.bintianqi.owndroid.feature.network.PrivateDnsScreen
import com.bintianqi.owndroid.feature.network.RecommendedGlobalProxyScreen
import com.bintianqi.owndroid.feature.network.UpdateNetworkScreen
import com.bintianqi.owndroid.feature.network.WifiScreen
import com.bintianqi.owndroid.feature.network.WifiSecurityLevelScreen
import com.bintianqi.owndroid.feature.network.WifiSsidPolicyScreen
import com.bintianqi.owndroid.feature.password.KeyguardDisabledFeaturesScreen
import com.bintianqi.owndroid.feature.password.PasswordConfigurationDialog
import com.bintianqi.owndroid.feature.password.PasswordInfoScreen
import com.bintianqi.owndroid.feature.password.PasswordScreen
import com.bintianqi.owndroid.feature.password.RequiredPasswordComplexityScreen
import com.bintianqi.owndroid.feature.password.RequiredPasswordQualityScreen
import com.bintianqi.owndroid.feature.password.ResetPasswordScreen
import com.bintianqi.owndroid.feature.password.ResetPasswordTokenScreen
import com.bintianqi.owndroid.feature.privilege.AddDelegatedAdminScreen
import com.bintianqi.owndroid.feature.privilege.DelegatedAdminsScreen
import com.bintianqi.owndroid.feature.privilege.DhizukuServerSettingsScreen
import com.bintianqi.owndroid.feature.privilege.TransferOwnershipScreen
import com.bintianqi.owndroid.feature.privilege.WorkModesScreen
import com.bintianqi.owndroid.feature.search.SearchScreen
import com.bintianqi.owndroid.feature.settings.AboutScreen
import com.bintianqi.owndroid.feature.settings.ApiSettings
import com.bintianqi.owndroid.feature.settings.AppLockSettingsScreen
import com.bintianqi.owndroid.feature.settings.AppearanceScreen
import com.bintianqi.owndroid.feature.settings.ExportSettingsScreen
import com.bintianqi.owndroid.feature.settings.ImportSettingsScreen
import com.bintianqi.owndroid.feature.settings.NotificationsScreen
import com.bintianqi.owndroid.feature.settings.SettingsOptionsScreen
import com.bintianqi.owndroid.feature.settings.SettingsScreen
import com.bintianqi.owndroid.feature.settings.SettingsSyncScreen
import com.bintianqi.owndroid.feature.settings.SettingsViewModel
import com.bintianqi.owndroid.feature.settings.ShortcutSettingsScreen
import com.bintianqi.owndroid.feature.system.BugReportDialog
import com.bintianqi.owndroid.feature.system.CaCertScreen
import com.bintianqi.owndroid.feature.system.ContentProtectionPolicyScreen
import com.bintianqi.owndroid.feature.system.DefaultInputMethodScreen
import com.bintianqi.owndroid.feature.system.DeviceInfoScreen
import com.bintianqi.owndroid.feature.system.DisableAccountManagementScreen
import com.bintianqi.owndroid.feature.system.EnrollmentIdDialog
import com.bintianqi.owndroid.feature.system.FrpPolicyScreen
import com.bintianqi.owndroid.feature.system.HardwareMonitorScreen
import com.bintianqi.owndroid.feature.system.KeyguardScreen
import com.bintianqi.owndroid.feature.system.LockScreenInfoScreen
import com.bintianqi.owndroid.feature.system.LockTaskModeScreen
import com.bintianqi.owndroid.feature.system.MtePolicyScreen
import com.bintianqi.owndroid.feature.system.NearbyStreamingPolicyScreen
import com.bintianqi.owndroid.feature.system.OrgIdDialog
import com.bintianqi.owndroid.feature.system.OrgNameDialog
import com.bintianqi.owndroid.feature.system.PermissionPolicyScreen
import com.bintianqi.owndroid.feature.system.RebootDialog
import com.bintianqi.owndroid.feature.system.SecurityLoggingScreen
import com.bintianqi.owndroid.feature.system.SupportMessageScreen
import com.bintianqi.owndroid.feature.system.SystemOptionsScreen
import com.bintianqi.owndroid.feature.system.SystemScreen
import com.bintianqi.owndroid.feature.system.SystemUpdateScreen
import com.bintianqi.owndroid.feature.system.TimeScreen
import com.bintianqi.owndroid.feature.system.WipeDataScreen
import com.bintianqi.owndroid.feature.time_blocker.TimeBlockerEditScreen
import com.bintianqi.owndroid.feature.time_blocker.TimeBlockerScreen
import com.bintianqi.owndroid.feature.user_restriction.UserRestrictionEditorScreen
import com.bintianqi.owndroid.feature.user_restriction.UserRestrictionOptionsScreen
import com.bintianqi.owndroid.feature.user_restriction.UserRestrictionScreen
import com.bintianqi.owndroid.feature.users.AffiliationIdScreen
import com.bintianqi.owndroid.feature.users.ChangeUserIconDialog
import com.bintianqi.owndroid.feature.users.ChangeUsernameScreen
import com.bintianqi.owndroid.feature.users.CreateUserScreen
import com.bintianqi.owndroid.feature.users.LogoutDialog
import com.bintianqi.owndroid.feature.users.UserInfoScreen
import com.bintianqi.owndroid.feature.users.UserOperationScreen
import com.bintianqi.owndroid.feature.users.UserSessionMessageScreen
import com.bintianqi.owndroid.feature.users.UsersOptionsScreen
import com.bintianqi.owndroid.feature.users.UsersScreen
import com.bintianqi.owndroid.feature.work_profile.AddCrossProfileIntentFilterScreen
import com.bintianqi.owndroid.feature.work_profile.CreateWorkProfileScreen
import com.bintianqi.owndroid.feature.work_profile.CrossProfileIntentFilterPresetsScreen
import com.bintianqi.owndroid.feature.work_profile.CrossProfileIntentFilterScreen
import com.bintianqi.owndroid.feature.work_profile.DeleteWorkProfileScreen
import com.bintianqi.owndroid.feature.work_profile.SuspendPersonalAppScreen
import com.bintianqi.owndroid.feature.work_profile.WorkProfileScreen
import com.bintianqi.owndroid.ui.screen.HomeScreen
import com.bintianqi.owndroid.utils.viewModelFactory

/**
 * Nav3 provides a frequently-changing content key factory, which has broken our app several times,
 * so I prefer to use a custom and stable content key factory.
 */
inline fun <reified K: Destination> EntryProviderScope<Destination>.myEntry(
    metadata: Map<String, Any> = emptyMap(), noinline content: @Composable ((K) -> Unit)
) {
    entry({ it::class.qualifiedName!! }, metadata, content)
}

/** @param P parent destination to share ViewModel with */
inline fun <reified K: Destination, reified P: Destination> EntryProviderScope<Destination>.myEntry(
    noinline content: @Composable ((K) -> Unit)
) {
    entry({ it::class.qualifiedName!! }, navParentKey<P>(), content)
}

/** Constructing metadata using [mapOf] won't work */
private val dialogAnimationMetadata = metadata {
    // Disable the default animation
    put(NavDisplay.TransitionKey) {
        EnterTransition.None togetherWith ExitTransition.None
    }
    put(NavDisplay.PopTransitionKey) {
        EnterTransition.None togetherWith ExitTransition.None
    }
    put(NavDisplay.PredictivePopTransitionKey) {
        EnterTransition.None togetherWith ExitTransition.None
    }
}

data class MyOverlayScene<T: Any>(
    override val key: Any,
    override val previousEntries: List<NavEntry<T>>,
    override val overlaidEntries: List<NavEntry<T>>,
    private val entry: NavEntry<T>,
) : OverlayScene<T> {

    override val entries: List<NavEntry<T>> = listOf(entry)

    override val content: @Composable (() -> Unit) = {
        entry.Content()
    }
}

class OverlaySceneStrategy<T: Any> : SceneStrategy<T> {
    override fun SceneStrategyScope<T>.calculateScene(entries: List<NavEntry<T>>): Scene<T>? {
        val entry = entries.lastOrNull()
        return if (entry?.metadata?.get(METADATA_KEY) == true) {
            MyOverlayScene(
                entry.contentKey as T, entries.dropLast(1), entries.dropLast(1), entry
            )
        } else {
            null
        }
    }

    companion object {
        const val METADATA_KEY = "transparent_background"

        fun transparentBackground(): Map<String, Any> {
            return mapOf(METADATA_KEY to true)
        }
    }
}

/** An alias of [viewModel], but with short name and only one argument to simplify code */
@Composable
private inline fun <reified VM: ViewModel> getVm(factory: MyViewModelFactory? = null): VM {
    return viewModel<VM>(factory = factory)
}

private inline fun <reified K: Destination> EntryProviderScope<Destination>.myDialogEntry(
    metadata: Map<String, Any> = emptyMap(), noinline content: @Composable ((K) -> Unit)
) {
    // By default, it will display a white background behind the dialog.
    // By using a custom scene strategy, we can make the background transparent.
    entry(
        { it::class.qualifiedName!! },
        metadata + dialogAnimationMetadata + OverlaySceneStrategy.transparentBackground(),
        content
    )
}

private inline fun <reified K: Destination, reified P: Destination> EntryProviderScope<Destination>.myDialogEntry(
    noinline content: @Composable ((K) -> Unit)
) {
    entry(
        { it::class.qualifiedName!! },
        navParentKey<P>() + dialogAnimationMetadata + OverlaySceneStrategy.transparentBackground(),
        content
    )
}

@Suppress("NewApi")
fun myEntryProvider(
    backstack: NavBackStack<NavKey>, appChooserVm: AppChooserViewModel,
    container: AppContainer
) = entryProvider {
    fun navigate(dest: Destination) {
        backstack += dest
    }

    fun navigateUp() {
        if (backstack.size > 1) backstack.removeLastOrNull()
    }

    fun navigateToAppGroups() {
        navigate(Destination.AppGroups)
    }

    fun navigateAndPopAll(dest: Destination) {
        navigate(dest)
        repeat(backstack.size - 1) {
            backstack.removeFirstOrNull()
        }
    }

    fun choosePackage() {
        navigate(Destination.ApplicationsList(AppChooserMode.Choose))
    }

    fun chooseSinglePackage() {
        navigate(Destination.ApplicationsList(AppChooserMode.SingleChoose))
    }
    
    val vmf = container.viewModelFactory

    myEntry<Destination.Home> {
        HomeScreen(
            container.privilegeState, ::navigate
        )
    }
    myEntry<Destination.Search> {
        SearchScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myEntry<Destination.WorkingModes> {
        WorkModesScreen(getVm(vmf), it, ::navigateUp, {
            navigateAndPopAll(Destination.Home)
        }, {
            navigateAndPopAll(Destination.WorkingModes(false))
        }, ::navigate)
    }
    myEntry<Destination.DhizukuServerSettings> {
        DhizukuServerSettingsScreen(getVm(), ::navigateUp)
    }

    myEntry<Destination.DelegatedAdmins> {
        DelegatedAdminsScreen(getVm(), ::navigateUp, ::navigate)
    }
    myEntry<Destination.DelegatedAdminDetails, Destination.DelegatedAdmins> {
        AddDelegatedAdminScreen(
            getVm(), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.TransferOwnership> {
        TransferOwnershipScreen(getVm(), ::navigateUp) {
            navigate(Destination.WorkingModes(false))
            while (backstack.size > 1) {
                backstack.removeFirstOrNull()
            }
        }
    }

    myEntry<Destination.System> {
        SystemScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myDialogEntry<Destination.Reboot, Destination.System> {
        RebootDialog(getVm(vmf), ::navigateUp)
    }
    myDialogEntry<Destination.BugReport, Destination.System> {
        BugReportDialog(getVm(vmf), ::navigateUp)
    }
    myDialogEntry<Destination.OrgName, Destination.System> {
        OrgNameDialog(getVm(vmf), ::navigateUp)
    }
    myDialogEntry<Destination.OrgId, Destination.System> {
        OrgIdDialog(getVm(vmf), ::navigateUp)
    }
    myDialogEntry<Destination.EnrollmentId, Destination.System> {
        EnrollmentIdDialog(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.DeviceInfo, Destination.System> {
        DeviceInfoScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.LockScreenInfo, Destination.System> {
        LockScreenInfoScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.SupportMessage, Destination.System> {
        SupportMessageScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.SystemOptions> {
        SystemOptionsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.Keyguard, Destination.System> {
        KeyguardScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.HardwareMonitor> {
        HardwareMonitorScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.DefaultInputMethod, Destination.System> {
        DefaultInputMethodScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.Time> {
        TimeScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.ContentProtectionPolicy, Destination.System> {
        ContentProtectionPolicyScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.PermissionPolicy, Destination.System> {
        PermissionPolicyScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.MtePolicy, Destination.System> {
        MtePolicyScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.NearbyStreamingPolicy, Destination.System> {
        NearbyStreamingPolicyScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.LockTaskMode> {
        LockTaskModeScreen(
            getVm(vmf),
            container.chosenPackage, ::chooseSinglePackage, ::choosePackage, ::navigateUp
        )
    }
    myEntry<Destination.CaCert> {
        CaCertScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.SecurityLogging> {
        SecurityLoggingScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.DisableAccountManagement, Destination.System> {
        DisableAccountManagementScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.SystemUpdate> {
        SystemUpdateScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.FrpPolicy, Destination.System> {
        FrpPolicyScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.WipeData, Destination.System> { WipeDataScreen(getVm(vmf), ::navigateUp) }

    myEntry<Destination.Network> {
        NetworkScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myEntry<Destination.WiFi> {
        WifiScreen(getVm(vmf), ::navigate, ::navigateUp)
    }
    myEntry<Destination.UpdateNetwork, Destination.WiFi> {
        UpdateNetworkScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.WifiSecurityLevel, Destination.WiFi> {
        WifiSecurityLevelScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.WifiSsidPolicy, Destination.WiFi> {
        WifiSsidPolicyScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.NetworkOptions, Destination.Network> {
        NetworkOptionsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.NetworkStats> {
        NetworkStatsScreen(
            getVm(vmf),
            ::chooseSinglePackage, container.chosenPackage, ::navigateUp
        ) {
            navigate(Destination.NetworkStatsViewer)
        }
    }
    myEntry<Destination.NetworkStatsViewer, Destination.NetworkStats> {
        NetworkStatsViewerScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.PrivateDns, Destination.Network> {
        PrivateDnsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.AlwaysOnVpn, Destination.Network> {
        AlwaysOnVpnPackageScreen(
            getVm(vmf), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.RecommendedGlobalProxy, Destination.Network> {
        RecommendedGlobalProxyScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.NetworkLogging> {
        NetworkLoggingScreen(getVm(vmf), ::navigateUp)
    }
    //entry<Destination.WifiAuthKeypair> { WifiAuthKeypairScreen(::navigateUp) }
    myEntry<Destination.PreferentialNetworkService> {
        PreferentialNetworkServiceScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myEntry<Destination.AddPreferentialNetworkServiceConfig, Destination.PreferentialNetworkService> {
        AddPreferentialNetworkServiceConfigScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.OverrideApn> {
        OverrideApnScreen(
            getVm(vmf), ::navigateUp
        ) { navigate(Destination.AddApnSetting) }
    }
    myEntry<Destination.AddApnSetting, Destination.OverrideApn> {
        AddApnSettingScreen(getVm(), ::navigateUp)
    }

    myEntry<Destination.WorkProfile> {
        WorkProfileScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myEntry<Destination.CreateWorkProfile> {
        CreateWorkProfileScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.SuspendPersonalApp, Destination.WorkProfile> {
        SuspendPersonalAppScreen(
            getVm(vmf), ::navigateUp
        )
    }
    myEntry<Destination.CrossProfileIntentFilter> {
        CrossProfileIntentFilterScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myEntry<Destination.AddCrossProfileIntentFilter, Destination.CrossProfileIntentFilter> {
        AddCrossProfileIntentFilterScreen(it, getVm(), ::navigateUp)
    }
    myEntry<Destination.CrossProfileIntentFilterPresets, Destination.CrossProfileIntentFilter> {
        CrossProfileIntentFilterPresetsScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.DeleteWorkProfile, Destination.WorkProfile> {
        DeleteWorkProfileScreen(getVm(vmf), ::navigateUp)
    }

    myEntry<Destination.ApplicationsList> { params ->
        AppChooserScreen(
            params, appChooserVm
        ) { name ->
            if (params.mode == AppChooserMode.ListView) {
                if (name != null) {
                    navigate(Destination.ApplicationDetails(name))
                } else {
                    navigateUp()
                }
            } else {
                if (name != null) container.chosenPackage.trySend(name)
                navigateUp()
            }
        }
    }

    myEntry<Destination.ApplicationFeatures> {
        ApplicationsFeaturesScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myEntry<Destination.ApplicationDetails> {
        ApplicationDetailsScreen(
            viewModel(
                factory = viewModelFactory {
                    AppDetailsViewModel(
                        it.packageName, container.app, container.privilegeHelper,
                        container.privilegeState, container.toastChannel
                    )
                }
            ), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.Suspend, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.suspend, vm.suspendedPackages, vm::getSuspendedPackages,
            vm::setPackageSuspended, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, R.string.info_suspend_app,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.Hide, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.hide, vm.hiddenPackages, vm::getHiddenPackages, vm::setPackageHidden,
            ::navigateUp, container.chosenPackage, ::choosePackage, ::navigateToAppGroups,
            container.appGroupsState, null, vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.BlockUninstall, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.block_uninstall, vm.ubPackages, vm::getUbPackages, vm::setPackageUb,
            ::navigateUp, container.chosenPackage, ::choosePackage, ::navigateToAppGroups,
            container.appGroupsState, null, vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.DisableUserControl, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.disable_user_control, vm.ucdPackages, vm::getUcdPackages,
            vm::setPackageUcd, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, R.string.info_disable_user_control,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.AppPermissionsManager, Destination.ApplicationDetails> {
        AppPermissionsManagerScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.PermissionManager, Destination.ApplicationFeatures> {
        PermissionManagerScreen(getVm(vmf), ::navigate, ::navigateUp)
    }
    myEntry<Destination.PermissionDetail, Destination.ApplicationFeatures> {
        PermissionDetailScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.DisableMeteredData, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.disable_metered_data, vm.mddPackages, vm::getMddPackages,
            vm::setPackageMdd, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, null,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting,
            AppChooserFilter(usesInternet = true)
        )
    }
    myEntry<Destination.ClearAppStorage, Destination.ApplicationFeatures> {
        ClearAppStorageScreen(
            getVm(vmf), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.UninstallApp, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        UninstallAppScreen(
            vm, container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.KeepUninstalledPackages, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.keep_uninstalled_packages, vm.kuPackages, vm::getKuPackages,
            vm::setPackageKu, ::navigateUp, container.chosenPackage, ::choosePackage,
            ::navigateToAppGroups, container.appGroupsState, R.string.info_keep_uninstalled_apps,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.InstallExistingApp, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        InstallExistingAppScreen(
            vm, container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }
    myEntry<Destination.CrossProfilePackages, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.cross_profile_apps, vm.cpPackages,
            vm::getCpPackages, vm::setPackageCp, ::navigateUp, container.chosenPackage,
            ::choosePackage, ::navigateToAppGroups, container.appGroupsState, null,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.CrossProfileWidgetProviders, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PackageFunctionScreen(
            R.string.cross_profile_widget, vm.cpwProviders,
            vm::getCpwProviders, vm::setCpwProvider, ::navigateUp, container.chosenPackage,
            ::choosePackage, ::navigateToAppGroups, container.appGroupsState, null,
            vm.allPackagesState, vm::getAllPackages,
            vm.isDefaultSwitchView(), vm::saveSwitchViewSetting
        )
    }
    myEntry<Destination.CredentialManagerPolicy, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        CredentialManagerPolicyScreen(
            vm, container.chosenPackage, ::choosePackage, ::navigateUp
        )
    }
    myEntry<Destination.PermittedAccessibilityServices, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PermittedAsAndImPackagesScreen(
            R.string.permitted_accessibility_services,
            R.string.system_accessibility_always_allowed, container.chosenPackage, ::choosePackage,
            vm.pasAllowAll, vm.pasPackages, vm::getPasPolicy, vm::setPasAllowAll, vm::setPasPackage,
            vm::applyPasPolicy, ::navigateUp
        )
    }
    myEntry<Destination.PermittedInputMethods, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        PermittedAsAndImPackagesScreen(
            R.string.permitted_ime, R.string.system_ime_always_allowed,
            container.chosenPackage, ::choosePackage, vm.pimAllowAll, vm.pimPackages,
            vm::getPimPolicy, vm::setPimAllowAll,
            vm::setPimPackage, vm::applyPimPolicy, ::navigateUp
        )
    }
    myEntry<Destination.EnableSystemApp, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        EnableSystemAppScreen(
            container.chosenPackage, {
                navigate(
                    Destination.ApplicationsList(
                        AppChooserMode.SingleChoose, AppChooserFilter(
                            userApps = AppFilterState.No, installed = AppFilterState.No
                        )
                    )
                )
            }, vm::enableSystemApp, ::navigateUp
        )
    }
    myEntry<Destination.SetDefaultDialer, Destination.ApplicationFeatures> {
        val vm = getVm<AppFeaturesViewModel>(vmf)
        SetDefaultDialerScreen(
            container.chosenPackage, ::chooseSinglePackage, vm::setDefaultDialer, ::navigateUp
        )
    }
    myEntry<Destination.ManagedConfiguration> {
        ManagedConfigurationScreen(
            viewModel(factory = viewModelFactory {
                ManagedConfigurationViewModel(
                    it.packageName, container.app, container.privilegeHelper, container.toastChannel
                )
            }), ::navigateUp, ::navigate
        )
    }
    myEntry<Destination.ManagedConfigurationValueEditor, Destination.ManagedConfiguration> {
        ManagedConfigurationValueEditorScreen(getVm(), it.id, it.isInt, ::navigateUp)
    }
    myEntry<Destination.ManagedConfigurationBooleanEditor, Destination.ManagedConfiguration> {
        ManagedConfigurationBooleanEditorScreen(getVm(), it.id, ::navigateUp)
    }
    myEntry<Destination.ManagedConfigurationListEditor, Destination.ManagedConfiguration> {
        ManagedConfigurationListEditorScreen(getVm(), it.id, ::navigateUp)
    }
    myEntry<Destination.AppGroups> {
        AppGroupsScreen(
            getVm(vmf),
            { navigate(Destination.EditAppGroup) },
            ::navigateUp
        )
    }
    myEntry<Destination.EditAppGroup, Destination.AppGroups> {
        EditAppGroupScreen(
            getVm(), ::navigateUp, ::choosePackage, container.chosenPackage
        )
    }

    myEntry<Destination.UserRestriction> {
        UserRestrictionScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myEntry<Destination.UserRestrictionEditor, Destination.UserRestriction> {
        UserRestrictionEditorScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.UserRestrictionOptions, Destination.UserRestriction> {
        UserRestrictionOptionsScreen(it, getVm(vmf), ::navigateUp)
    }

    myEntry<Destination.Users> {
        UsersScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myDialogEntry<Destination.Logout, Destination.Users> {
        LogoutDialog(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.UserInfo, Destination.Users> {
        UserInfoScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.UsersOptions, Destination.Users> {
        UsersOptionsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.UserOperation, Destination.Users> {
        UserOperationScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.CreateUser, Destination.Users> {
        CreateUserScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.ChangeUsername, Destination.Users> {
        ChangeUsernameScreen(getVm(vmf), ::navigateUp)
    }
    myDialogEntry<Destination.ChangeUserIcon, Destination.Users> {
        ChangeUserIconDialog(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.UserSessionMessage, Destination.Users> {
        UserSessionMessageScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.AffiliationId, Destination.Users> {
        AffiliationIdScreen(getVm(vmf), ::navigateUp)
    }

    myEntry<Destination.Password> {
        PasswordScreen(getVm(vmf), ::navigateUp, ::navigate)
    }
    myDialogEntry<Destination.PasswordConfig, Destination.Password> {
        PasswordConfigurationDialog(getVm(vmf), it.type, ::navigateUp)
    }
    myEntry<Destination.PasswordInfo, Destination.Password> {
        PasswordInfoScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.ResetPasswordToken, Destination.Password> {
        ResetPasswordTokenScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.ResetPassword, Destination.Password> {
        ResetPasswordScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.RequiredPasswordComplexity, Destination.Password> {
        RequiredPasswordComplexityScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.KeyguardDisabledFeatures, Destination.Password> {
        KeyguardDisabledFeaturesScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.RequiredPasswordQuality, Destination.Password> {
        RequiredPasswordQualityScreen(getVm(vmf), ::navigateUp)
    }

    myEntry<Destination.TimeBlocker> {
        TimeBlockerScreen(getVm(vmf), ::navigate, ::navigateUp)
    }
    myEntry<Destination.TimeBlockerEdit, Destination.TimeBlocker> { params ->
        TimeBlockerEditScreen(
            params.ruleId, getVm(), container.chosenPackage, ::chooseSinglePackage, ::navigateUp
        )
    }

    myEntry<Destination.Settings> {
        SettingsScreen(getVm(vmf), ::navigate, ::navigateUp)
    }
    myEntry<Destination.SettingsOptions, Destination.Settings> {
        SettingsOptionsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.AppearanceSettings, Destination.Settings> {
        AppearanceScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.ShortcutSettings, Destination.Settings> {
        ShortcutSettingsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.AppLockSettings, Destination.Settings> {
        AppLockSettingsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.SettingsSync, Destination.Settings> {
        SettingsSyncScreen(getVm(vmf), ::navigate, ::navigateUp)
    }
    myEntry<Destination.ApiSettings, Destination.Settings> {
        ApiSettings(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.ExportSettings, Destination.Settings> {
        val vm = getVm<SettingsViewModel>(vmf)
        val qrBitmap by vm.syncQrBitmap.collectAsState()
        val exportSummary by vm.syncExportSummary.collectAsState()
        ExportSettingsScreen(qrBitmap, exportSummary, ::navigateUp)
    }
    myEntry<Destination.ImportSettings, Destination.Settings> {
        ImportSettingsScreen(getVm(), ::navigateUp)
    }
    myEntry<Destination.NotificationSettings, Destination.Settings> {
        NotificationsScreen(getVm(vmf), ::navigateUp)
    }
    myEntry<Destination.About> { AboutScreen(::navigateUp) }
}
