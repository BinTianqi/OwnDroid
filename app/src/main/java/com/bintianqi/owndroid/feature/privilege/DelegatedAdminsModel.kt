package com.bintianqi.owndroid.feature.privilege

import android.app.admin.DevicePolicyManager
import android.os.Build.VERSION
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.AppInfo

class DelegatedScope(val id: String, val string: Int, val requiresApi: Int = 26)

@Suppress("InlinedApi")
val delegatedScopesList = listOf(
    DelegatedScope(DevicePolicyManager.DELEGATION_APP_RESTRICTIONS, R.string.manage_application_restrictions),
    DelegatedScope(DevicePolicyManager.DELEGATION_BLOCK_UNINSTALL, R.string.block_uninstall),
    DelegatedScope(DevicePolicyManager.DELEGATION_CERT_INSTALL, R.string.manage_certificates),
    DelegatedScope(DevicePolicyManager.DELEGATION_CERT_SELECTION, R.string.select_keychain_certificates, 29),
    DelegatedScope(DevicePolicyManager.DELEGATION_ENABLE_SYSTEM_APP, R.string.enable_system_app),
    DelegatedScope(DevicePolicyManager.DELEGATION_INSTALL_EXISTING_PACKAGE, R.string.install_existing_packages, 28),
    DelegatedScope(DevicePolicyManager.DELEGATION_KEEP_UNINSTALLED_PACKAGES, R.string.manage_uninstalled_packages, 28),
    DelegatedScope(DevicePolicyManager.DELEGATION_NETWORK_LOGGING, R.string.network_logging, 29),
    DelegatedScope(DevicePolicyManager.DELEGATION_PACKAGE_ACCESS, R.string.change_package_state),
    DelegatedScope(DevicePolicyManager.DELEGATION_PERMISSION_GRANT, R.string.grant_permissions),
    DelegatedScope(DevicePolicyManager.DELEGATION_SECURITY_LOGGING, R.string.security_logging, 31)
).filter { VERSION.SDK_INT >= it.requiresApi }

class DelegatedAdmin(val app: AppInfo, val scopes: List<String>)
