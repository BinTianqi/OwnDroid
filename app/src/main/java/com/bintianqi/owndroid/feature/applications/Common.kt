package com.bintianqi.owndroid.feature.applications

import android.content.Context
import android.content.RestrictionsManager
import android.content.pm.ApplicationInfo
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
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
    val mdDisabled: Boolean = true,
    val mdNotDisabled: Boolean = true,
    val installed: Boolean = true,
    val notInstalled: Boolean = true
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppChooserFilterBottomSheet(
    filter: AppChooserFilter, onDismiss: () -> Unit, update: (AppChooserFilter) -> Unit
) {
    ModalBottomSheet(onDismiss) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            Row(
                Modifier.fillMaxWidth().padding(10.dp, 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.filters), style = MaterialTheme.typography.titleLarge)
                FilledTonalIconButton({
                    update(AppChooserFilter())
                }) {
                    Icon(painterResource(R.drawable.restart_alt_fill0), null)
                }
            }
            FullWidthCheckBoxItem(R.string.user_apps, filter.userApps) {
                update(filter.copy(userApps = it))
            }
            FullWidthCheckBoxItem(R.string.system_apps, filter.systemApps) {
                update(filter.copy(systemApps = it))
            }
            HorizontalDivider()
            FullWidthCheckBoxItem(R.string.support_mc, filter.hasMc) {
                update(filter.copy(hasMc = it))
            }
            FullWidthCheckBoxItem(R.string.mc_modified, filter.mcModified) {
                update(filter.copy(mcModified = it))
            }
            HorizontalDivider()
            FullWidthCheckBoxItem(R.string.suspended, filter.suspended) {
                update(filter.copy(suspended = it))
            }
            FullWidthCheckBoxItem(R.string.not_suspended, filter.notSuspended) {
                update(filter.copy(notSuspended = it))
            }
            HorizontalDivider()
            FullWidthCheckBoxItem(R.string.hidden, filter.hidden) {
                update(filter.copy(hidden = it))
            }
            FullWidthCheckBoxItem(R.string.not_hidden, filter.notHidden) {
                update(filter.copy(notHidden = it))
            }
            HorizontalDivider()
            FullWidthCheckBoxItem(R.string.uninstall_blocked, filter.ub) {
                update(filter.copy(ub = it))
            }
            FullWidthCheckBoxItem(R.string.uninstall_not_blocked, filter.notUb) {
                update(filter.copy(notUb = it))
            }
            if (Build.VERSION.SDK_INT >= 30) {
                HorizontalDivider()
                FullWidthCheckBoxItem(R.string.uc_disabled, filter.ucDisabled) {
                    update(filter.copy(ucDisabled = true))
                }
                FullWidthCheckBoxItem(R.string.uc_not_disabled, filter.ucNotDisabled) {
                    update(filter.copy(ucNotDisabled = it))
                }
            }
            if (Build.VERSION.SDK_INT >= 28) {
                HorizontalDivider()
                FullWidthCheckBoxItem(R.string.md_disabled, filter.mdDisabled) {
                    update(filter.copy(mdDisabled = it))
                }
                FullWidthCheckBoxItem(R.string.md_not_disabled, filter.mdNotDisabled) {
                    update(filter.copy(mdNotDisabled = it))
                }
            }
            HorizontalDivider()
            FullWidthCheckBoxItem(R.string.installed, filter.installed) {
                update(filter.copy(installed = it))
            }
            FullWidthCheckBoxItem(R.string.not_installed, filter.installed) {
                update(filter.copy(notInstalled = it))
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
    return AppChooserEntry(appInfo, hasMc, mcModified, suspended, hidden, ub, ucd, mdd)
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
                            app.info.flags and ApplicationInfo.FLAG_INSTALLED == 0))
}
