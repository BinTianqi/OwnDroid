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
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.bintianqi.owndroid.utils.getInstalledAppsFlags
import com.bintianqi.owndroid.utils.searchInString
import com.bintianqi.owndroid.utils.transformAppRestrictionEntryList
import kotlinx.serialization.Serializable

enum class AppChooserMode {
    ListView, Choose, SingleChoose
}

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

enum class AppFilterState {
    Yes, No, Both
}

@Serializable
data class AppChooserFilter(
    val userApps: AppFilterState = AppFilterState.Yes,
    val hasMc: Boolean = false,
    val mcModified: Boolean = false,
    val suspended: AppFilterState = AppFilterState.Both,
    val hidden: AppFilterState = AppFilterState.Both,
    val ub: AppFilterState = AppFilterState.Both,
    val ucDisabled: AppFilterState = AppFilterState.Both,
    val mdDisabled: AppFilterState = AppFilterState.Both,
    val installed: AppFilterState = AppFilterState.Yes,
    val usesInternet: Boolean = false,
)

class PermissionItem(
    val id: String,
    val label: String,
    val icon: Int?
)

val profileOwnerRestrictedPermissions = listOf(
    Manifest.permission.CAMERA,
    Manifest.permission.RECORD_AUDIO,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
    Manifest.permission.BODY_SENSORS,
    Manifest.permission.ACTIVITY_RECOGNITION,
)

fun getIconForPermission(id: String): Int? {
    val exactMatch = when (id) {
        Manifest.permission.POST_NOTIFICATIONS -> R.drawable.notifications_fill0
        Manifest.permission.READ_EXTERNAL_STORAGE -> R.drawable.folder_fill0
        Manifest.permission.WRITE_EXTERNAL_STORAGE -> R.drawable.folder_fill0
        Manifest.permission.READ_MEDIA_AUDIO -> R.drawable.music_note_fill0
        Manifest.permission.READ_MEDIA_VIDEO -> R.drawable.movie_fill0
        Manifest.permission.READ_MEDIA_IMAGES -> R.drawable.image_fill0
        Manifest.permission.CAMERA -> R.drawable.photo_camera_fill0
        Manifest.permission.RECORD_AUDIO -> R.drawable.mic_fill0
        Manifest.permission.ACCESS_COARSE_LOCATION -> R.drawable.location_on_fill0
        Manifest.permission.ACCESS_FINE_LOCATION -> R.drawable.location_on_fill0
        Manifest.permission.ACCESS_BACKGROUND_LOCATION -> R.drawable.location_on_fill0
        Manifest.permission.READ_CONTACTS -> R.drawable.contacts_fill0
        Manifest.permission.WRITE_CONTACTS -> R.drawable.contacts_fill0
        Manifest.permission.READ_CALENDAR -> R.drawable.calendar_month_fill0
        Manifest.permission.WRITE_CALENDAR -> R.drawable.calendar_month_fill0
        Manifest.permission.BLUETOOTH_CONNECT -> R.drawable.bluetooth_fill0
        Manifest.permission.BLUETOOTH_SCAN -> R.drawable.bluetooth_searching_fill0
        Manifest.permission.BLUETOOTH_ADVERTISE -> R.drawable.bluetooth_fill0
        Manifest.permission.NEARBY_WIFI_DEVICES -> R.drawable.wifi_fill0
        Manifest.permission.CALL_PHONE -> R.drawable.call_fill0
        Manifest.permission.ANSWER_PHONE_CALLS -> R.drawable.call_fill0
        Manifest.permission.READ_PHONE_NUMBERS -> R.drawable.mobile_phone_fill0
        Manifest.permission.READ_PHONE_STATE -> R.drawable.mobile_phone_fill0
        Manifest.permission.USE_SIP -> R.drawable.call_fill0
        Manifest.permission.RANGING, Manifest.permission.UWB_RANGING -> R.drawable.cell_tower_fill0
        Manifest.permission.READ_SMS -> R.drawable.sms_fill0
        Manifest.permission.RECEIVE_SMS, Manifest.permission.RECEIVE_MMS -> R.drawable.sms_fill0
        Manifest.permission.SEND_SMS -> R.drawable.sms_fill0
        Manifest.permission.READ_CALL_LOG -> R.drawable.call_log_fill0
        Manifest.permission.WRITE_CALL_LOG -> R.drawable.call_log_fill0
        Manifest.permission.RECEIVE_WAP_PUSH -> R.drawable.wifi_fill0
        Manifest.permission.BODY_SENSORS -> R.drawable.sensors_fill0
        Manifest.permission.BODY_SENSORS_BACKGROUND -> R.drawable.sensors_fill0
        Manifest.permission.ACTIVITY_RECOGNITION -> R.drawable.history_fill0
        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED -> R.drawable.perm_media_fill0
        Manifest.permission.GET_ACCOUNTS -> R.drawable.account_circle_fill0
        Manifest.permission.ACCESS_MEDIA_LOCATION -> R.drawable.location_on_fill0
        Manifest.permission.PROCESS_OUTGOING_CALLS -> R.drawable.call_fill0
        Manifest.permission.ACCEPT_HANDOVER -> R.drawable.call_fill0
        else -> null
    }
    return if (exactMatch == null) {
        if (id.startsWith("android.permission.health")) {
            R.drawable.cardiology_fill0
        } else {
            null
        }
    } else {
        exactMatch
    }
}

@Composable
private fun TriStateFilterItem(
    text: Int, icon: Int, state: AppFilterState, update: (AppFilterState) -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(painterResource(icon), null, Modifier.padding(end = 8.dp))
            Text(stringResource(text))
        }
        Row {
            RadioButton(
                state == AppFilterState.Yes, { update(AppFilterState.Yes) },
                colors = RadioButtonDefaults.colors(
                    MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary
                )
            )
            RadioButton(
                state == AppFilterState.No, { update(AppFilterState.No) },
                colors = RadioButtonDefaults.colors(
                    MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.secondary
                )
            )
            RadioButton(
                state == AppFilterState.Both, { update(AppFilterState.Both) },
                colors = RadioButtonDefaults.colors(
                    MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

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
            SingleChoiceSegmentedButtonRow(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp)
            ) {
                SegmentedButton(
                    filter.userApps == AppFilterState.Yes,
                    { update(filter.copy(userApps = AppFilterState.Yes)) },
                    SegmentedButtonDefaults.itemShape(0, 3)
                ) {
                    Text(stringResource(R.string.user_apps))
                }
                SegmentedButton(
                    filter.userApps == AppFilterState.No,
                    { update(filter.copy(userApps = AppFilterState.No)) },
                    SegmentedButtonDefaults.itemShape(1, 3)
                ) {
                    Text(stringResource(R.string.system_apps))
                }
                SegmentedButton(
                    filter.userApps == AppFilterState.Both,
                    { update(filter.copy(userApps = AppFilterState.Both)) },
                    SegmentedButtonDefaults.itemShape(2, 3)
                ) {
                    Text(stringResource(R.string.both))
                }
            }
            Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp), Arrangement.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        true, null, Modifier.padding(end = 4.dp),
                        colors = RadioButtonDefaults.colors(MaterialTheme.colorScheme.secondary)
                    )
                    Text(stringResource(R.string.yes))
                }
                Row(
                    Modifier.padding(start = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(
                        true, null, Modifier.padding(end = 4.dp),
                        colors = RadioButtonDefaults.colors(MaterialTheme.colorScheme.secondary)
                    )
                    Text(stringResource(R.string.no))
                }
                Row(
                    Modifier.padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RadioButton(true, null, Modifier.padding(end = 4.dp),)
                    Text(stringResource(R.string.both))
                }
            }
            TriStateFilterItem(R.string.suspended, R.drawable.block_fill0, filter.suspended) {
                update(filter.copy(suspended = it))
            }
            TriStateFilterItem(R.string.hidden, R.drawable.visibility_off_fill0, filter.hidden) {
                update(filter.copy(hidden = it))
            }
            TriStateFilterItem(
                R.string.uninstall_blocked, R.drawable.delete_forever_fill0, filter.ub
            ) {
                update(filter.copy(ub = it))
            }
            if (Build.VERSION.SDK_INT >= 30) TriStateFilterItem(
                R.string.uc_disabled, R.drawable.do_not_touch_fill0, filter.ucDisabled
            ) {
                update(filter.copy(ucDisabled = it))
            }
            if (Build.VERSION.SDK_INT >= 28) TriStateFilterItem(
                R.string.md_disabled, R.drawable.money_off_fill0, filter.mdDisabled
            ) {
                update(filter.copy(mdDisabled = it))
            }
            TriStateFilterItem(
                R.string.installed, R.drawable.apk_install_fill0, filter.installed
            ) {
                update(filter.copy(installed = it))
            }
            FlowRow {
                Chip(R.string.support_mc, R.drawable.description_fill0, filter.hasMc) {
                    update(filter.copy(hasMc = it))
                }
                Chip(R.string.mc_modified, R.drawable.description_fill0, filter.mcModified) {
                    update(filter.copy(mcModified = it))
                }
            }
            Chip(R.string.uses_internet, R.drawable.language_fill0, filter.usesInternet) {
                update(filter.copy(usesInternet = it))
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
    val pkgInfo = context.packageManager.getPackageInfo(
        packageName, PackageManager.GET_PERMISSIONS or getInstalledAppsFlags
    )
    return AppChooserEntry(
        appInfo, hasMc, mcModified, suspended, hidden, ub, ucd, mdd,
        Manifest.permission.INTERNET in (pkgInfo.requestedPermissions ?: emptyArray())
    )
}

fun filterApp(app: AppChooserEntry, filter: AppChooserFilter, query: String): Boolean {
    fun filterItem(state: AppFilterState, item: Boolean): Boolean {
        return state == AppFilterState.Both ||
                (state == AppFilterState.Yes && item) ||
                (state == AppFilterState.No && !item)
    }
    return filterItem(filter.userApps, !app.info.isSystem) &&
            (!filter.hasMc || app.hasMc) && (!filter.mcModified || app.mcModified) &&
            filterItem(filter.suspended, app.suspended) &&
            filterItem(filter.hidden, app.hidden) &&
            filterItem(filter.ub, app.ub) &&
            filterItem(filter.ucDisabled, app.ucd) &&
            filterItem(filter.mdDisabled, app.mdd) &&
            (query.isEmpty() || searchInString(query, app.info.name) ||
                    searchInString(query, app.info.label)) &&
            filterItem(filter.installed, app.info.flags and ApplicationInfo.FLAG_INSTALLED != 0) &&
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
