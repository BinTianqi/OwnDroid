package com.bintianqi.owndroid.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

sealed class Destination : NavKey {
    override fun toString(): String {
        return this::class.simpleName!!
    }

    @Serializable object Home : Destination()

    @Serializable class WorkingModes(val canNavigateUp: Boolean) : Destination()
    @Serializable object DhizukuServerSettings : Destination()
    @Serializable object DelegatedAdmins : Destination()
    @Serializable object DelegatedAdminDetails : Destination()

    @Serializable object TransferOwnership : Destination()

    @Serializable object System : Destination()
    @Serializable object SystemOptions : Destination()
    @Serializable object Keyguard : Destination()
    @Serializable object HardwareMonitor : Destination()
    @Serializable object DefaultInputMethod : Destination()
    @Serializable object Time : Destination()
    @Serializable object ContentProtectionPolicy : Destination()
    @Serializable object PermissionPolicy : Destination()
    @Serializable object MtePolicy : Destination()
    @Serializable object NearbyStreamingPolicy : Destination()
    @Serializable object LockTaskMode : Destination()
    @Serializable object CaCert : Destination()
    @Serializable object SecurityLogging : Destination()
    @Serializable object DeviceInfo : Destination()
    @Serializable object LockScreenInfo : Destination()
    @Serializable object SupportMessage : Destination()
    @Serializable object DisableAccountManagement : Destination()
    @Serializable object FrpPolicy : Destination()
    @Serializable object WipeData : Destination()
    @Serializable object SystemUpdate : Destination()

    @Serializable object Network : Destination()
    @Serializable object NetworkOptions : Destination()
    @Serializable object WiFi : Destination()
    @Serializable class UpdateNetwork(val index: Int) : Destination()
    @Serializable object WifiSecurityLevel : Destination()
    @Serializable object WifiSsidPolicy : Destination()
    @Serializable object NetworkStats : Destination()
    @Serializable object NetworkStatsViewer : Destination()
    @Serializable object PrivateDns : Destination()
    @Serializable object AlwaysOnVpnPackage : Destination()
    @Serializable object RecommendedGlobalProxy : Destination()
    @Serializable object NetworkLogging : Destination()
    @Serializable object PreferentialNetworkService : Destination()
    @Serializable object AddPreferentialNetworkServiceConfig : Destination()
    @Serializable object OverrideApn : Destination()
    @Serializable object AddApnSetting : Destination()

    @Serializable object WorkProfile : Destination()
    @Serializable object CreateWorkProfile : Destination()
    @Serializable object SuspendPersonalApp : Destination()
    @Serializable object CrossProfileIntentFilter : Destination()
    @Serializable object CrossProfileIntentFilterPresets: Destination()
    @Serializable object DeleteWorkProfile : Destination()

    @Serializable object ApplicationFeatures : Destination()
    @Serializable object Suspend : Destination()
    @Serializable object Hide : Destination()
    @Serializable object BlockUninstall : Destination()
    @Serializable object DisableUserControl : Destination()
    @Serializable object PermissionManager : Destination()
    @Serializable class PermissionDetail(val permission: String) : Destination()
    @Serializable object DisableMeteredData : Destination()
    @Serializable object ClearAppStorage : Destination()
    @Serializable object UninstallApp : Destination()
    @Serializable object KeepUninstalledPackages : Destination()
    @Serializable object InstallExistingApp : Destination()
    @Serializable object CrossProfilePackages : Destination()
    @Serializable object CrossProfileWidgetProviders : Destination()
    @Serializable object CredentialManagerPolicy : Destination()
    @Serializable object PermittedAccessibilityServices : Destination()
    @Serializable object PermittedInputMethods : Destination()
    @Serializable object EnableSystemApp : Destination()
    @Serializable object SetDefaultDialer : Destination()
    @Serializable object AppGroups : Destination()
    @Serializable object EditAppGroup : Destination()

    @Serializable class ApplicationDetails(val packageName: String) : Destination()
    @Serializable class AppPermissionsManager(val packageName: String) : Destination()
    @Serializable class ManagedConfiguration(val packageName: String) : Destination()

    @Serializable data class ApplicationsList(
        val canSwitchView: Boolean, val multiSelect: Boolean
    ) : Destination()

    @Serializable object UserRestriction : Destination()
    @Serializable data class UserRestrictionOptions(val id: String) : Destination()
    @Serializable object UserRestrictionEditor : Destination()

    @Serializable object Users : Destination()
    @Serializable object UsersOptions : Destination()
    @Serializable object UserInfo : Destination()
    @Serializable object UserOperation : Destination()
    @Serializable object CreateUser : Destination()
    @Serializable object AffiliationId : Destination()
    @Serializable object ChangeUsername : Destination()
    @Serializable object UserSessionMessage : Destination()

    @Serializable object Password : Destination()
    @Serializable object PasswordInfo : Destination()
    @Serializable object ResetPasswordToken : Destination()
    @Serializable object ResetPassword : Destination()
    @Serializable object RequiredPasswordComplexity : Destination()
    @Serializable object KeyguardDisabledFeatures : Destination()
    @Serializable object RequiredPasswordQuality : Destination()

    @Serializable object Settings : Destination()
    @Serializable object SettingsOptions : Destination()
    @Serializable object AppearanceSettings : Destination()
    @Serializable object AppLockSettings : Destination()
    @Serializable object ApiSettings : Destination()
    @Serializable object NotificationSettings : Destination()
    @Serializable object About : Destination()
}
