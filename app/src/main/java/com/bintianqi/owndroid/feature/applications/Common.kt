package com.bintianqi.owndroid.feature.applications

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.content.Context
import android.content.RestrictionsManager
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.getAppInfo
import com.bintianqi.owndroid.utils.searchInString
import com.bintianqi.owndroid.utils.transformAppRestrictionEntryList
import kotlinx.serialization.Serializable

class AppChooserEntry(
    val info: AppInfo,
    val hasMc: Boolean, // Managed configuration
    val mcModified: Boolean,
    val suspended: Boolean,
    val hidden: Boolean,
    val ub: Boolean, // Uninstall blocked
    val ucd: Boolean, // User control disabled
    val mdd: Boolean, // Metered data disabled
    val internet: Boolean,
)

@Serializable
data class AppChooserFilter(
    val userApps: Boolean = true,
    val systemApps: Boolean = false,
    val hasMc: Boolean = false,
    val mcModified: Boolean = false,
    val suspended: Boolean = true,
    val notSuspended: Boolean = true,
    val hidden: Boolean = true,
    val notHidden: Boolean = true,
    val ub: Boolean = true,
    val notUb: Boolean = true,
    val ucDisabled: Boolean = true,
    val ucNotDisabled: Boolean = true,
    val usesInternet: Boolean = false,
    val mdDisabled: Boolean = true,
    val mdNotDisabled: Boolean = true,
    val installed: Boolean = true,
    val notInstalled: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppChooserFilterBottomSheet(
    filter: AppChooserFilter,
    defaultFilter: AppChooserFilter,
    onDismiss: () -> Unit,
    update: (AppChooserFilter) -> Unit
) {
    @Composable
    fun Chip(label: Int, icon: Int, state: Boolean, onClick: (Boolean) -> Unit) {
        FilterChip(
            state, { onClick(!state) }, { Text(stringResource(label)) },
            Modifier.padding(start = 8.dp),
            leadingIcon = { Icon(painterResource(icon), null) }
        )
    }
    ModalBottomSheet(onDismiss) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().padding(10.dp, 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.filters), style = MaterialTheme.typography.titleLarge)
                FilledTonalIconButton({
                    update(defaultFilter)
                }) {
                    Icon(painterResource(R.drawable.restart_alt_fill0), null)
                }
            }
            FlowRow {
                Chip(R.string.user_apps, R.drawable.apps_fill0, filter.userApps) {
                    update(filter.copy(userApps = it))
                }
                Chip(R.string.system_apps, R.drawable.android_fill0, filter.systemApps) {
                    update(filter.copy(systemApps = it))
                }
            }
            FlowRow {
                Chip(R.string.support_mc, R.drawable.description_fill0, filter.hasMc) {
                    update(filter.copy(hasMc = it))
                }
                Chip(R.string.mc_modified, R.drawable.description_fill0, filter.mcModified) {
                    update(filter.copy(mcModified = it))
                }
            }
            FlowRow {
                Chip(R.string.suspended, R.drawable.block_fill0, filter.suspended) {
                    update(filter.copy(suspended = it))
                }
                Chip(R.string.not_suspended, R.drawable.block_fill0, filter.notSuspended) {
                    update(filter.copy(notSuspended = it))
                }
            }
            FlowRow {
                Chip(R.string.hidden, R.drawable.visibility_off_fill0, filter.hidden) {
                    update(filter.copy(hidden = it))
                }
                Chip(R.string.not_hidden, R.drawable.visibility_fill0, filter.notHidden) {
                    update(filter.copy(notHidden = it))
                }
            }
            FlowRow {
                Chip(R.string.uninstall_blocked, R.drawable.delete_forever_fill0, filter.ub) {
                    update(filter.copy(ub = it))
                }
                Chip(R.string.uninstall_not_blocked, R.drawable.delete_fill0, filter.notUb) {
                    update(filter.copy(notUb = it))
                }
            }
            if (Build.VERSION.SDK_INT >= 30) FlowRow {
                Chip(R.string.uc_disabled, R.drawable.do_not_touch_fill0, filter.ucDisabled) {
                    update(filter.copy(ucDisabled = true))
                }
                Chip(
                    R.string.uc_not_disabled, R.drawable.do_not_touch_fill0, filter.ucNotDisabled
                ) {
                    update(filter.copy(ucNotDisabled = it))
                }
            }
            FlowRow {
                Chip(R.string.uses_internet, R.drawable.language_fill0, filter.usesInternet) {
                    update(filter.copy(usesInternet = it))
                }
                if (Build.VERSION.SDK_INT >= 28) {
                    Chip(R.string.md_disabled, R.drawable.money_off_fill0, filter.mdDisabled) {
                        update(filter.copy(mdDisabled = it))
                    }
                    Chip(
                        R.string.md_not_disabled, R.drawable.money_off_fill0, filter.mdNotDisabled
                    ) {
                        update(filter.copy(mdNotDisabled = it))
                    }
                }
            }
            FlowRow {
                Chip(R.string.installed, R.drawable.apk_install_fill0, filter.installed) {
                    update(filter.copy(installed = it))
                }
                Chip(R.string.not_installed, R.drawable.delete_fill0, filter.notInstalled) {
                    update(filter.copy(notInstalled = it))
                }
            }
        }
    }
}


fun getAppStatus(
    context: Context, ph: PrivilegeHelper, packageName: String
): AppChooserEntry {
    val appInfo = getAppInfo(context.packageManager, packageName)
    val rm = context.getSystemService(Context.RESTRICTIONS_SERVICE) as RestrictionsManager
    var hasMc = false
    var mcModified = false
    var suspended = false
    var hidden = false
    var ub = false
    var ucd = false
    var mdd = false
    try {
        ph.safeDpmCall {
            val bundle = dpm.getApplicationRestrictions(dar, packageName)
            val entries = rm.getManifestRestrictions(packageName)
            if (entries != null) {
                hasMc = true
                val restrictions = transformAppRestrictionEntryList(entries, bundle)
                if (restrictions.any { !it.isNull() }) mcModified = true
            }
        }
    } catch (_: Exception) {}
    try {
        ph.safeDpmCall {
            if (Build.VERSION.SDK_INT >= 24) {
                suspended = dpm.isPackageSuspended(dar, packageName)
            }
            hidden = dpm.isApplicationHidden(dar, packageName)
            ub = dpm.isUninstallBlocked(dar, packageName)
            if (Build.VERSION.SDK_INT >= 30) {
                ucd = packageName in dpm.getUserControlDisabledPackages(dar)
            }
            if (Build.VERSION.SDK_INT >= 28) {
                mdd = packageName in dpm.getMeteredDataDisabledPackages(dar)
            }
        }
    } catch (_: Exception) {}
    val pkgInfo = context.packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
    return AppChooserEntry(
        appInfo, hasMc, mcModified, suspended, hidden, ub, ucd, mdd,
        Manifest.permission.INTERNET in (pkgInfo.requestedPermissions ?: emptyArray())
    )
}

fun filterApp(app: AppChooserEntry, filter: AppChooserFilter, query: String): Boolean {
    return (filter.userApps == filter.systemApps ||
            (filter.userApps && app.info.flags and ApplicationInfo.FLAG_SYSTEM == 0) ||
            (filter.systemApps && app.info.flags and ApplicationInfo.FLAG_SYSTEM != 0)) &&
            (!filter.hasMc || app.hasMc) && (!filter.mcModified || app.mcModified) &&
            (filter.suspended == filter.notSuspended || (filter.suspended && app.suspended)
                    || (filter.notSuspended && !app.suspended)) &&
            (filter.hidden == filter.notHidden || (filter.hidden && app.hidden) ||
                    (filter.notHidden && !app.hidden)) &&
            (filter.ub == filter.notUb || (filter.ub && app.ub) || (filter.notUb && !app.ub)) &&
            (filter.ucDisabled == filter.ucNotDisabled || (filter.ucDisabled && app.ucd) ||
                    (filter.ucNotDisabled && !app.ucd)) &&
            (filter.mdDisabled == filter.mdNotDisabled || (filter.mdDisabled && app.mdd) ||
                    (filter.mdNotDisabled && !app.mdd)) &&
            (query.isEmpty() || searchInString(query, app.info.name) ||
                    searchInString(query, app.info.label)) &&
            (filter.installed == filter.notInstalled ||
                    (filter.installed &&
                            app.info.flags and ApplicationInfo.FLAG_INSTALLED != 0) ||
                    (filter.notInstalled &&
                            app.info.flags and ApplicationInfo.FLAG_INSTALLED == 0)) &&
            (!filter.usesInternet || app.internet)
}

@Composable
fun PermissionRadioButtonHint() {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.End) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                true, null, Modifier.padding(end = 4.dp),
                colors = RadioButtonDefaults.colors(MaterialTheme.colorScheme.outline)
            )
            Text(stringResource(R.string.default_str))
        }
        Row(
            Modifier.padding(start = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                true, null, Modifier.padding(end = 4.dp),
                colors = RadioButtonDefaults.colors(MaterialTheme.colorScheme.error)
            )
            Text(stringResource(R.string.denied))
        }
        Row(Modifier.padding(horizontal = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            RadioButton(true, null, Modifier.padding(end = 4.dp),)
            Text(stringResource(R.string.granted))
        }
    }
}

@Composable
fun PermissionRadioButtonRow(state: Int?, grantRestricted: Boolean, onSet: (Int) -> Unit) {
    Row {
        RadioButton(
            state == DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT,
            { onSet(DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT) },
            colors = RadioButtonDefaults.colors(
                MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.outline
            )
        )
        RadioButton(
            state == DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED,
            { onSet(DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED) },
            colors = RadioButtonDefaults.colors(
                MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.error
            )
        )
        RadioButton(
            state == DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED,
            { onSet(DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED) },
            enabled = !grantRestricted,
            colors = RadioButtonDefaults.colors(
                MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary
            )
        )
    }
}
