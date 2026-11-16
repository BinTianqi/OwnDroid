package com.bintianqi.owndroid.feature

import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.password.PasswordDialogType
import com.bintianqi.owndroid.feature.password.keyguardDisabledFeatures
import com.bintianqi.owndroid.feature.system.globalSettings
import com.bintianqi.owndroid.feature.system.secureSettings
import com.bintianqi.owndroid.feature.user_restriction.UserRestrictionCategory
import com.bintianqi.owndroid.feature.user_restriction.UserRestrictionsRepository
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.NotificationType
import com.bintianqi.owndroid.utils.PrivilegeStatus

class FeatureEntry(
    val destination: Destination,
    val name: Int,
    val icon: Int,
    val checkAvailable: PrivilegeStatus.() -> Boolean = { true },
    /** Don't add [name] to [keywords] */
    val keywords: Set<Int> = emptySet()
)

val WorkingModeFeatures = listOf(
    FeatureEntry(
        Destination.TransferOwnership,
        R.string.transfer_ownership, R.drawable.swap_horiz_fill0,
        { version >= 28 && !dhizuku },
        setOf(R.string.info_transfer_ownership)
    ),
    FeatureEntry(
        Destination.DhizukuServerSettings,
        R.string.dhizuku_server, R.drawable.m3_dhizuku_icon,
        { !dhizuku }
    ),
    FeatureEntry(
        Destination.DelegatedAdmins,
        R.string.delegated_admins, R.drawable.admin_panel_settings_fill0,
    ),
)

val SystemFeatures = listOf(
    FeatureEntry(
        Destination.SystemOptions,
        R.string.options, R.drawable.tune_fill0,
        keywords = setOf(
            R.string.disable_cam, R.string.disable_screen_capture, R.string.disable_status_bar,
            R.string.auto_time, R.string.auto_timezone, R.string.require_auto_time,
            R.string.backup_service, R.string.disable_bt_contact_share,
            R.string.common_criteria_mode, R.string.enable_usb_signal,
            R.string.stay_on_while_plugged_in, R.string.status_bar
        ) + globalSettings.map { it.name } + secureSettings.map { it.name }
    ),
    FeatureEntry(
        Destination.Keyguard,
        R.string.keyguard, R.drawable.screen_lock_portrait_fill0,
        keywords = setOf(R.string.lock_now)
    ),
    FeatureEntry(
        Destination.HardwareMonitor,
        R.string.hardware_monitor, R.drawable.memory_fill0,
        { version >= 24 && device && !dhizuku },
        setOf(
            R.string.cpu_temp, R.string.gpu_temp, R.string.skin_temp, R.string.fan_speeds,
            R.string.cpu_usages
        )
    ),
    FeatureEntry(
        Destination.DefaultInputMethod,
        R.string.default_input_method, R.drawable.keyboard_fill0
    ),
    FeatureEntry(
        Destination.Reboot,
        R.string.reboot, R.drawable.restart_alt_fill0,
        keywords = setOf(R.string.info_reboot)
    ),
    FeatureEntry(
        Destination.BugReport,
        R.string.bug_report, R.drawable.bug_report_fill0,
        { version >= 24 && device && (version < 28 || affiliated) }
    ),
    FeatureEntry(
        Destination.Time, R.string.time, R.drawable.schedule_fill0,
        { device || (work && org) },
        keywords = setOf(
            R.string.timezone, R.string.auto_time_policy, R.string.auto_timezone_policy
        )
    ),
    FeatureEntry(
        Destination.ContentProtectionPolicy,
        R.string.content_protection_policy, R.drawable.search_fill0,
        { version >= 35 && (device || (profile && affiliated) || (profile && mainUser)) },
        setOf(R.string.info_content_protection_policy)
    ),
    FeatureEntry(
        Destination.PermissionPolicy,
        R.string.permission_policy, R.drawable.key_fill0,
        keywords = setOf(R.string.info_permission_policy)
    ),
    FeatureEntry(
        Destination.MtePolicy,
        R.string.mte_policy, R.drawable.memory_fill0,
        { version >= 34 && (device || (profile && org)) },
        setOf(R.string.info_mte_policy)
    ),
    FeatureEntry(
        Destination.NearbyStreamingPolicy,
        R.string.nearby_streaming_policy, R.drawable.share_fill0,
        { version >= 31 },
        setOf(
            R.string.info_nearby_app_streaming_policy,
            R.string.info_nearby_notification_streaming_policy
        )
    ),
    FeatureEntry(
        Destination.LockTaskMode,
        R.string.lock_task_mode, R.drawable.lock_fill0,
        { version >= 28 && (device || (profile && affiliated) || (profile && mainUser)) },
        setOf(
            R.string.info_start_lock_task_mode, R.string.info_lock_task_packages,
            R.string.features
        )
    ),
    FeatureEntry(
        Destination.CaCert,
        R.string.ca_cert, R.drawable.license_fill0
    ),
    FeatureEntry(
        Destination.SecurityLogging,
        R.string.security_logging, R.drawable.description_fill0,
        { version >= 26 && !dhizuku && (device || (work && org)) },
        setOf(R.string.info_security_log)
    ),
    FeatureEntry(
        Destination.DeviceInfo,
        R.string.device_info, R.drawable.perm_device_information_fill0,
        keywords = setOf(
            R.string.financed_device, R.string.dpmrh, R.string.encryption_status,
            R.string.support_device_id_attestation, R.string.support_unique_device_attestation,
            R.string.activated_device_admin
        )
    ),
    FeatureEntry(
        Destination.OrgName,
        R.string.org_name, R.drawable.corporate_fare_fill0,
        { (version >= 24 && profile) || (version >= 26 && device) }
    ),
    FeatureEntry(
        Destination.OrgId,
        R.string.org_id, R.drawable.corporate_fare_fill0,
        { version >= 31 },
        setOf(R.string.info_org_id)
    ),
    FeatureEntry(
        Destination.EnrollmentId,
        R.string.enrollment_specific_id, R.drawable.id_card_fill0,
        { version >= 31 },
        setOf(R.string.info_enrollment_specific_id)
    ),
    FeatureEntry(
        Destination.LockScreenInfo,
        R.string.lock_screen_info, R.drawable.screen_lock_portrait_fill0,
        { version >= 24 && (device || org) },
        setOf(R.string.info_lock_screen_info)
    ),
    FeatureEntry(
        Destination.SupportMessage,
        R.string.support_messages, R.drawable.chat_fill0,
        { version >= 24 },
        setOf(
            R.string.short_support_msg, R.string.info_short_support_message,
            R.string.long_support_msg, R.string.info_long_support_message
        )
    ),
    FeatureEntry(
        Destination.DisableAccountManagement,
        R.string.disable_account_management, R.drawable.account_circle_fill0,
        keywords = setOf(R.string.info_disable_account_management)
    ),
    FeatureEntry(
        Destination.SystemUpdate,
        R.string.system_update, R.drawable.system_update_fill0,
        { device || (work && org) },
        setOf(
            R.string.update_received_time, R.string.system_update_policy_windowed,
            R.string.system_update_policy_postpone, R.string.install_system_update,
            R.string.system_update_policy
        )
    ),
    FeatureEntry(
        Destination.FrpPolicy,
        R.string.frp_policy, R.drawable.device_reset_fill0,
        { version >= 30 && (device || org) },
        setOf(R.string.info_frp_policy, R.string.account_list_is)
    ),
    FeatureEntry(
        Destination.WipeData,
        R.string.wipe_data, R.drawable.device_reset_fill0,
        { !work },
        setOf(R.string.wipe_external_storage, R.string.wipe_euicc)
    ),
)

val NetworkFeatures = listOf(
    FeatureEntry(
        Destination.WiFi,
        R.string.wifi, R.drawable.wifi_fill0,
        { !dhizuku },
        setOf(
            R.string.disconnect, R.string.reconnect, R.string.wifi_mac_address,
            R.string.min_wifi_security_level, R.string.info_minimum_wifi_security_level,
            R.string.wifi_ssid_policy
        )
    ),
    FeatureEntry(
        Destination.NetworkOptions,
        R.string.options, R.drawable.tune_fill0,
        { version >= 30 && (device || (work && org)) },
        setOf(
            R.string.lockdown_admin_configured_network,
            R.string.info_lockdown_admin_configured_network
        )
    ),
    FeatureEntry(
        Destination.NetworkStats,
        R.string.network_stats, R.drawable.query_stats_fill0,
        { !dhizuku }
    ),
    FeatureEntry(
        Destination.PrivateDns,
        R.string.private_dns, R.drawable.dns_fill0,
        { version >= 29 && device }
    ),
    FeatureEntry(
        Destination.AlwaysOnVpn,
        R.string.always_on_vpn, R.drawable.vpn_key_fill0,
        { version >= 24 },
        setOf(R.string.info_always_on_vpn)
    ),
    FeatureEntry(
        Destination.RecommendedGlobalProxy,
        R.string.recommended_global_proxy, R.drawable.vpn_key_fill0,
        { device },
        setOf(R.string.recommended_global_proxy)
    ),
    FeatureEntry(
        Destination.NetworkLogging,
        R.string.network_logging, R.drawable.description_fill0,
        { version >= 26 && !dhizuku && (device || work) },
        setOf(R.string.info_network_log)
    ),
    FeatureEntry(
        Destination.PreferentialNetworkService,
        R.string.preferential_network_service, R.drawable.globe_fill0,
        { (version >= 31 && work) || (version >= 33 && device) }
    ),
    FeatureEntry(
        Destination.OverrideApn,
        R.string.override_apn, R.drawable.cell_tower_fill0,
        { version >= 28 && device }
    ),
)

val WorkProfileFeatures = listOf(
    FeatureEntry(
        Destination.SuspendPersonalApp,
        R.string.suspend_personal_app, R.drawable.block_fill0,
        { version >= 30 && work && org }
    ),
    FeatureEntry(
        Destination.CrossProfileIntentFilter,
        R.string.intent_filter, R.drawable.filter_alt_fill0,
        { work }
    ),
    FeatureEntry(
        Destination.DeleteWorkProfile,
        R.string.delete_work_profile, R.drawable.delete_forever_fill0,
        { work }
    )
)

val ApplicationStateFeatures = listOf(
    FeatureEntry(
        Destination.PermissionManager,
        R.string.permissions, R.drawable.shield_fill0
    ),
    FeatureEntry(
        Destination.Suspend,
        R.string.suspend, R.drawable.block_fill0,
        { version >= 24 },
        setOf(R.string.info_suspend_app)
    ),
    FeatureEntry(
        Destination.Hide,
        R.string.hide, R.drawable.visibility_off_fill0
    ),
    FeatureEntry(
        Destination.BlockUninstall,
        R.string.block_uninstall, R.drawable.delete_forever_fill0
    ),
    FeatureEntry(
        Destination.DisableUserControl,
        R.string.disable_user_control, R.drawable.do_not_touch_fill0,
        { version >= 30 },
        setOf(R.string.info_disable_user_control)
    ),
    FeatureEntry(
        Destination.DisableMeteredData,
        R.string.disable_metered_data, R.drawable.money_off_fill0,
        { version >= 28 }
    ),
    FeatureEntry(
        Destination.ClearAppStorage,
        R.string.clear_app_storage, R.drawable.mop_fill0,
        { version >= 28 }
    ),
    FeatureEntry(
        Destination.UninstallApp,
        R.string.uninstall_app, R.drawable.delete_fill0
    ),
    FeatureEntry(
        Destination.KeepUninstalledPackages,
        R.string.keep_uninstalled_packages, R.drawable.delete_fill0,
        { version >= 28 && device },
        setOf(R.string.info_keep_uninstalled_apps)
    ),
    FeatureEntry(
        Destination.InstallExistingApp,
        R.string.install_existing_app, R.drawable.install_mobile_fill0,
        { version >= 28 && (device || (profile && affiliated)) },
        setOf(R.string.info_install_existing_app)
    ),
    FeatureEntry(
        Destination.CrossProfilePackages,
        R.string.cross_profile_apps, R.drawable.work_fill0,
        { version >= 30 && work }
    ),
    FeatureEntry(
        Destination.CrossProfileWidgetProviders,
        R.string.cross_profile_widget, R.drawable.widgets_fill0,
        { work }
    ),
    FeatureEntry(
        Destination.CredentialManagerPolicy,
        R.string.credential_manager_policy, R.drawable.license_fill0,
        { version >= 34 && (device || work) }
    ),
    FeatureEntry(
        Destination.PermittedAccessibilityServices,
        R.string.permitted_accessibility_services, R.drawable.settings_accessibility_fill0
    ),
    FeatureEntry(
        Destination.PermittedInputMethods,
        R.string.permitted_ime, R.drawable.keyboard_fill0
    ),
    FeatureEntry(
        Destination.EnableSystemApp,
        R.string.enable_system_app, R.drawable.enable_fill0,
        keywords = setOf(R.string.info_enable_system_app)
    ),
    FeatureEntry(
        Destination.SetDefaultDialer,
        R.string.set_default_dialer, R.drawable.call_fill0,
        { version >= 34 && (device || (work && org)) }
    ),
)

val UserRestrictionFeatures = UserRestrictionCategory.entries.map { category ->
    FeatureEntry(
        Destination.UserRestrictionOptions(category.name),
        category.title, category.icon,
        keywords = UserRestrictionsRepository.getData(category.name).second.map { it.name }.toSet()
    )
}

val UserFeatures = listOf(
    FeatureEntry(
        Destination.Logout,
        R.string.logout, R.drawable.logout_fill0,
        { version >= 28 && profile && affiliated },
        setOf(R.string.info_logout)
    ),
    FeatureEntry(
        Destination.UserInfo,
        R.string.user_info, R.drawable.person_fill0
    ),
    FeatureEntry(
        Destination.UserOperation,
        R.string.user_operation, R.drawable.sync_alt_fill0,
        { device },
        setOf(
            R.string.start_in_background, R.string.user_operation_switch, R.string.stop,
            R.string.delete
        )
    ),
    FeatureEntry(
        Destination.UsersOptions,
        R.string.options, R.drawable.tune_fill0,
        { version >= 28 && device },
        setOf(R.string.enable_logout)
    ),
    FeatureEntry(
        Destination.CreateUser,
        R.string.create_user, R.drawable.person_add_fill0,
        { version >= 24 && device }
    ),
    FeatureEntry(
        Destination.ChangeUsername,
        R.string.change_username, R.drawable.edit_fill0
    ),
    FeatureEntry(
        Destination.ChangeUserIcon,
        R.string.change_user_icon, R.drawable.account_circle_fill0
    ),
    FeatureEntry(
        Destination.UserSessionMessage,
        R.string.user_session_msg, R.drawable.notifications_fill0,
        { version >= 28 && device },
        setOf(R.string.start_user_session_msg, R.string.end_user_session_msg)
    ),
    FeatureEntry(
        Destination.AffiliationId,
        R.string.affiliation_id, R.drawable.id_card_fill0,
        { version >= 26 },
        setOf(R.string.info_affiliation_id)
    ),
)

val PasswordFeatures = listOf(
    FeatureEntry(
        Destination.PasswordInfo,
        R.string.password_info, R.drawable.info_fill0,
        keywords = setOf(
            R.string.current_password_complexity, R.string.info_password_complexity,
            R.string.password_sufficient, R.string.unified_password
        )
    ),
    FeatureEntry(
        Destination.ResetPasswordToken,
        R.string.reset_password_token, R.drawable.key_vertical_fill0,
        { version >= 26 }
    ),
    FeatureEntry(
        Destination.ResetPassword,
        R.string.reset_password, R.drawable.lock_reset_fill0,
        keywords = setOf(R.string.info_reset_password)
    ),
    FeatureEntry(
        Destination.RequiredPasswordComplexity,
        R.string.required_password_complexity, R.drawable.password_fill0,
        { version >= 31 },
        setOf(R.string.info_password_complexity)
    ),
    FeatureEntry(
        Destination.KeyguardDisabledFeatures,
        R.string.disable_keyguard_features, R.drawable.screen_lock_portrait_fill0,
        keywords = keyguardDisabledFeatures.map { it.text }.toSet()
    ),
    FeatureEntry(
        Destination.PasswordConfig(PasswordDialogType.MaxTimeToLock),
        R.string.max_time_to_lock, R.drawable.schedule_fill0,
        keywords = setOf(R.string.info_screen_timeout)
    ),
    FeatureEntry(
        Destination.PasswordConfig(PasswordDialogType.ExpirationTimeout),
        R.string.pwd_expiration_timeout, R.drawable.lock_clock_fill0,
        keywords = setOf(R.string.info_password_expiration_timeout)
    ),
    FeatureEntry(
        Destination.PasswordConfig(PasswordDialogType.MaxFailedForWipe),
        R.string.max_pwd_fail, R.drawable.no_encryption_fill0,
        keywords = setOf(
            R.string.info_max_failed_password_system_user,
            R.string.info_max_failed_password_other_user
        )
    ),
    FeatureEntry(
        Destination.PasswordConfig(PasswordDialogType.StrongAuthTimeout),
        R.string.required_strong_auth_timeout, R.drawable.fingerprint_off_fill0,
        { version >= 26 },
        keywords = setOf(R.string.info_required_strong_auth_timeout)
    ),
    FeatureEntry(
        Destination.PasswordConfig(PasswordDialogType.HistoryLength),
        R.string.pwd_history, R.drawable.history_fill0,
        keywords = setOf(R.string.info_password_history_length)
    ),
    FeatureEntry(
        Destination.RequiredPasswordQuality,
        R.string.required_password_quality, R.drawable.password_fill0,
        { version < 31 },
        keywords = setOf(
            R.string.password_quality_unspecified, R.string.password_quality_something,
            R.string.password_quality_alphabetic, R.string.password_quality_numeric,
            R.string.password_quality_alphanumeric, R.string.password_quality_biometrics_weak,
            R.string.password_quality_numeric_complex
        )
    )
)

val SettingsFeatures = listOf(
    FeatureEntry(
        Destination.SettingsOptions,
        R.string.options, R.drawable.tune_fill0,
    ),
    FeatureEntry(
        Destination.AppearanceSettings,
        R.string.appearance, R.drawable.format_paint_fill0,
        keywords = setOf(R.string.material_you_color, R.string.dark_theme, R.string.black_theme)
    ),
    FeatureEntry(
        Destination.ShortcutSettings,
        R.string.shortcuts, R.drawable.open_in_new
    ),
    FeatureEntry(
        Destination.AppLockSettings,
        R.string.app_lock, R.drawable.lock_fill0
    ),
    FeatureEntry(
        Destination.SettingsSync,
        R.string.sync_transfer, R.drawable.sync_alt_fill0
    ),
    FeatureEntry(
        Destination.ApiSettings,
        R.string.api, R.drawable.code_fill0,
        { activated }
    ),
    FeatureEntry(
        Destination.NotificationSettings,
        R.string.notifications, R.drawable.notifications_fill0,
        { activated },
        NotificationType.entries.map { it.text }.toSet()
    ),
    FeatureEntry(
        Destination.About,
        R.string.about, R.drawable.info_fill0
    )
)

val FeatureEntries = listOf(
    R.string.system to SystemFeatures,
    R.string.network to NetworkFeatures,
    R.string.work_profile to WorkProfileFeatures,
    R.string.applications_state to ApplicationStateFeatures,
    R.string.user_restriction to UserRestrictionFeatures,
    R.string.users to UserFeatures,
    R.string.password to PasswordFeatures,
    R.string.settings to SettingsFeatures,
    R.string.other to WorkingModeFeatures,
)
