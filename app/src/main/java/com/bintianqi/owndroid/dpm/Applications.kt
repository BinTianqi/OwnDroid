package com.bintianqi.owndroid.dpm

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
import android.app.admin.PackagePolicy
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager
import android.os.Build.VERSION
import android.os.Looper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toDrawable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.AppInfo
import com.bintianqi.owndroid.AppInstallerActivity
import com.bintianqi.owndroid.AppInstallerViewModel
import com.bintianqi.owndroid.ChoosePackageContract
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.getInstalledAppsFlags
import com.bintianqi.owndroid.installedApps
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.ErrorDialog
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.serialization.Serializable
import java.util.concurrent.Executors

fun PackageManager.retrieveAppInfo(packageName: String): AppInfo {
    return try {
        getApplicationInfo(packageName, getInstalledAppsFlags).retrieveAppInfo(this)
    } catch (_: PackageManager.NameNotFoundException) {
        AppInfo(packageName, "???", Color.Transparent.toArgb().toDrawable(), 0)
    }
}

fun ApplicationInfo.retrieveAppInfo(pm: PackageManager) =
    installedApps.value.find { it.name == packageName } ?: AppInfo(packageName, loadLabel(pm).toString(), loadIcon(pm), flags)

val String.isValidPackageName
    get() = Regex("""^(?:[a-zA-Z]\w*\.)+[a-zA-Z]\w*$""").matches(this)

@Composable
fun LazyItemScope.ApplicationItem(info: AppInfo, onClear: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp).animateItem(),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(Modifier.fillMaxWidth(0.87F), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberDrawablePainter(info.icon), contentDescription = null,
                modifier = Modifier.padding(start = 12.dp, end = 18.dp).size(30.dp)
            )
            Column {
                Text(info.label)
                Text(info.name, Modifier.alpha(0.8F), style = typography.bodyMedium)
            }
        }
        IconButton(onClear) {
            Icon(Icons.Default.Clear, null)
        }
    }
}

@Composable
fun PackageNameTextField(value: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    val launcher = rememberLauncherForActivityResult(ChoosePackageContract()) {
        if(it != null) onValueChange(it)
    }
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value, onValueChange, Modifier.fillMaxWidth().then(modifier),
        label = { Text(stringResource(R.string.package_name)) },
        trailingIcon = {
            IconButton({
                launcher.launch(null)
            }) {
                Icon(Icons.AutoMirrored.Default.List, null)
            }
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
        keyboardActions = KeyboardActions { fm.clearFocus() }
    )
}

@Serializable object ApplicationsFeatures

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsFeaturesScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit, onSwitchView: () -> Unit) {
    val context = LocalContext.current
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.applications)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    IconButton(onSwitchView) {
                        Icon(painterResource(R.drawable.android_fill0), null)
                    }
                },
                scrollBehavior = sb
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            val privilege by Privilege.status.collectAsStateWithLifecycle()
            if(VERSION.SDK_INT >= 24) FunctionItem(R.string.suspend, icon = R.drawable.block_fill0) { onNavigate(Suspend) }
            FunctionItem(R.string.hide, icon = R.drawable.visibility_off_fill0) { onNavigate(Hide) }
            FunctionItem(R.string.block_uninstall, icon = R.drawable.delete_forever_fill0) { onNavigate(BlockUninstall) }
            if(VERSION.SDK_INT >= 30 && (privilege.device || (VERSION.SDK_INT >= 33 && privilege.profile))) {
                FunctionItem(R.string.disable_user_control, icon = R.drawable.do_not_touch_fill0) { onNavigate(DisableUserControl) }
            }
            if(VERSION.SDK_INT >= 23) {
                FunctionItem(R.string.permissions, icon = R.drawable.shield_fill0) { onNavigate(PermissionsManager()) }
            }
            if(VERSION.SDK_INT >= 28) {
                FunctionItem(R.string.disable_metered_data, icon = R.drawable.money_off_fill0) { onNavigate(DisableMeteredData) }
            }
            if(VERSION.SDK_INT >= 28) {
                FunctionItem(R.string.clear_app_storage, icon = R.drawable.mop_fill0) { onNavigate(ClearAppStorage) }
            }
            FunctionItem(R.string.install_app, icon = R.drawable.install_mobile_fill0) {
                context.startActivity(Intent(context, AppInstallerActivity::class.java))
            }
            FunctionItem(R.string.uninstall_app, icon = R.drawable.delete_fill0) { onNavigate(UninstallApp) }
            if(VERSION.SDK_INT >= 28 && privilege.device) {
                FunctionItem(R.string.keep_uninstalled_packages, icon = R.drawable.delete_fill0) { onNavigate(KeepUninstalledPackages) }
            }
            if (VERSION.SDK_INT >= 28 && (privilege.device || (privilege.profile && privilege.affiliated))) {
                FunctionItem(R.string.install_existing_app, icon = R.drawable.install_mobile_fill0) {
                    onNavigate(InstallExistingApp)
                }
            }
            if(VERSION.SDK_INT >= 30 && privilege.work) {
                FunctionItem(R.string.cross_profile_apps, icon = R.drawable.work_fill0) { onNavigate(CrossProfilePackages) }
            }
            if(privilege.work) {
                FunctionItem(R.string.cross_profile_widget, icon = R.drawable.widgets_fill0) { onNavigate(CrossProfileWidgetProviders) }
            }
            if(VERSION.SDK_INT >= 34 && privilege.device) {
                FunctionItem(R.string.credential_manager_policy, icon = R.drawable.license_fill0) { onNavigate(CredentialManagerPolicy) }
            }
            FunctionItem(R.string.permitted_accessibility_services, icon = R.drawable.settings_accessibility_fill0) {
                onNavigate(PermittedAccessibilityServices)
            }
            FunctionItem(R.string.permitted_ime, icon = R.drawable.keyboard_fill0) { onNavigate(PermittedInputMethods) }
            FunctionItem(R.string.enable_system_app, icon = R.drawable.enable_fill0) { onNavigate(EnableSystemApp) }
            if(VERSION.SDK_INT >= 34 && (privilege.device || privilege.work)) {
                FunctionItem(R.string.set_default_dialer, icon = R.drawable.call_fill0) { onNavigate(SetDefaultDialer) }
            }
        }
    }
}

@Serializable data class ApplicationDetails(val packageName: String)

@Composable
fun ApplicationDetailsScreen(param: ApplicationDetails, onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val packageName = param.packageName
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    val pm = context.packageManager
    var dialog by remember { mutableIntStateOf(0) } // 1: clear storage, 2: uninstall
    val info = pm.getApplicationInfo(packageName, getInstalledAppsFlags)
    MySmallTitleScaffold(R.string.place_holder, onNavigateUp, 0.dp) {
        Column(Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(rememberDrawablePainter(info.loadIcon(pm)), null, Modifier.size(50.dp))
            Text(info.loadLabel(pm).toString(), Modifier.padding(top = 4.dp))
            Text(info.packageName, Modifier.alpha(0.7F).padding(bottom = 8.dp), style = typography.bodyMedium)
        }
        FunctionItem(R.string.permissions, icon = R.drawable.shield_fill0) { onNavigate(PermissionsManager(packageName)) }
        if(VERSION.SDK_INT >= 24) SwitchItem(
            R.string.suspend, icon = R.drawable.block_fill0,
            getState = { Privilege.DPM.isPackageSuspended(Privilege.DAR, packageName) },
            onCheckedChange = { Privilege.DPM.setPackagesSuspended(Privilege.DAR, arrayOf(packageName), it) }
        )
        SwitchItem(
            R.string.hide, icon = R.drawable.visibility_off_fill0,
            getState = { Privilege.DPM.isApplicationHidden(Privilege.DAR, packageName) },
            onCheckedChange = { Privilege.DPM.setApplicationHidden(Privilege.DAR, packageName, it) }
        )
        SwitchItem(
            R.string.block_uninstall, icon = R.drawable.delete_forever_fill0,
            getState = { Privilege.DPM.isUninstallBlocked(Privilege.DAR, packageName) },
            onCheckedChange = { Privilege.DPM.setUninstallBlocked(Privilege.DAR, packageName, it) }
        )
        if(VERSION.SDK_INT >= 30) SwitchItem(
            R.string.disable_user_control, icon = R.drawable.do_not_touch_fill0,
            getState = { packageName in Privilege.DPM.getUserControlDisabledPackages(Privilege.DAR) },
            onCheckedChange = { state ->
                Privilege.DPM.setUserControlDisabledPackages(Privilege.DAR,
                    Privilege.DPM.getUserControlDisabledPackages(Privilege.DAR).let { if(state) it.plus(packageName) else it.minus(packageName) }
                )
            }
        )
        if(VERSION.SDK_INT >= 28) SwitchItem(
            R.string.disable_metered_data, icon = R.drawable.money_off_fill0,
            getState = { packageName in Privilege.DPM.getMeteredDataDisabledPackages(Privilege.DAR) },
            onCheckedChange = { state ->
                Privilege.DPM.setMeteredDataDisabledPackages(Privilege.DAR,
                    Privilege.DPM.getMeteredDataDisabledPackages(Privilege.DAR).let { if(state) it.plus(packageName) else it.minus(packageName) }
                )
            }
        )
        if(privilege.device && VERSION.SDK_INT >= 28) SwitchItem(
            R.string.keep_after_uninstall, icon = R.drawable.delete_fill0,
            getState = { Privilege.DPM.getKeepUninstalledPackages(Privilege.DAR)?.contains(packageName) == true },
            onCheckedChange = { state ->
                Privilege.DPM.setKeepUninstalledPackages(Privilege.DAR,
                    Privilege.DPM.getKeepUninstalledPackages(Privilege.DAR)?.let { if(state) it.plus(packageName) else it.minus(packageName) } ?: listOf(packageName)
                )
            }
        )
        if(VERSION.SDK_INT >= 28) FunctionItem(R.string.clear_app_storage, icon = R.drawable.mop_fill0) { dialog = 1 }
        FunctionItem(R.string.uninstall, icon = R.drawable.delete_fill0) { dialog = 2 }
    }
    if(dialog == 1 && VERSION.SDK_INT >= 28) ClearAppStorageDialog(packageName) { dialog = 0 }
    if(dialog == 2) UninstallAppDialog(packageName) { dialog = 0 }
}

@Serializable object Suspend

@RequiresApi(24)
@Composable
fun SuspendScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var packageName by remember { mutableStateOf("") }
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        pm.getInstalledApplications(getInstalledAppsFlags).filter {
            Privilege.DPM.isPackageSuspended(Privilege.DAR, it.packageName)
        }.forEach {
            packages += it.retrieveAppInfo(pm)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.suspend, onNavigateUp) {
        items(packages, { it.name }) {
            ApplicationItem(it) {
                Privilege.DPM.setPackagesSuspended(Privilege.DAR, arrayOf(it.name), false)
                refresh()
            }
        }
        item {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
                Button(
                    {
                        if(Privilege.DPM.setPackagesSuspended(Privilege.DAR, arrayOf(packageName), true).isEmpty()) packageName = ""
                        else context.showOperationResultToast(false)
                        refresh()
                    },
                    Modifier.fillMaxWidth(),
                    packageName.isValidPackageName
                ) {
                    Text(stringResource(R.string.suspend))
                }
                Notes(R.string.info_suspend_app)
            }
        }
    }
}

@Serializable object Hide

@Composable
fun HideScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var packageName by remember { mutableStateOf("") }
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        pm.getInstalledApplications(getInstalledAppsFlags).filter { Privilege.DPM.isApplicationHidden(Privilege.DAR, it.packageName) }.forEach {
            packages += it.retrieveAppInfo(pm)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.hide, onNavigateUp) {
        items(packages, { it.name }) {
            ApplicationItem(it) {
                Privilege.DPM.setApplicationHidden(Privilege.DAR, it.name, false)
                refresh()
            }
        }
        item {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
                Button(
                    {
                        if(Privilege.DPM.setApplicationHidden(Privilege.DAR, packageName, true)) packageName = ""
                        else context.showOperationResultToast(false)
                        refresh()
                    },
                    Modifier.fillMaxWidth(),
                    packageName.isValidPackageName
                ) {
                    Text(stringResource(R.string.hide))
                }
            }
        }
    }
}

@Serializable object BlockUninstall

@Composable
fun BlockUninstallScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var packageName by remember { mutableStateOf("") }
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        pm.getInstalledApplications(getInstalledAppsFlags).filter { Privilege.DPM.isUninstallBlocked(Privilege.DAR, it.packageName) }.forEach {
            packages += it.retrieveAppInfo(pm)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.block_uninstall, onNavigateUp) {
        items(packages, { it.name }) {
            ApplicationItem(it) {
                Privilege.DPM.setUninstallBlocked(Privilege.DAR, it.name, false)
                refresh()
            }
        }
        item {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
                Button(
                    {
                        Privilege.DPM.setUninstallBlocked(Privilege.DAR, packageName, true)
                        packageName = ""
                        refresh()
                    },
                    Modifier.fillMaxWidth(),
                    packageName.isValidPackageName
                ) {
                    Text(stringResource(R.string.block_uninstall))
                }
            }
        }
    }
}

@Serializable object DisableUserControl

@RequiresApi(30)
@Composable
fun DisableUserControlScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        Privilege.DPM.getUserControlDisabledPackages(Privilege.DAR).forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.disable_user_control, onNavigateUp) {
        items(packages, { it.name }) { info ->
            ApplicationItem(info) {
                Privilege.DPM.setUserControlDisabledPackages(Privilege.DAR, packages.minus(info).map { it.name })
                refresh()
            }
        }
        item {
            var packageName by remember { mutableStateOf("") }
            PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp, horizontal = HorizontalPadding)) { packageName = it }
            Button(
                {
                    Privilege.DPM.setUserControlDisabledPackages(Privilege.DAR, packages.map { it.name } + packageName)
                    refresh()
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding).padding(bottom = 8.dp),
            ) {
                Text(stringResource(R.string.add))
            }
            Notes(R.string.info_disable_user_control, HorizontalPadding)
        }
    }
}

@Serializable data class PermissionsManager(val packageName: String? = null)

@RequiresApi(23)
@Composable
fun PermissionsManagerScreen(onNavigateUp: () -> Unit, param: PermissionsManager) {
    val packageNameParam = param.packageName
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var packageName by remember { mutableStateOf(packageNameParam ?: "") }
    var selectedPermission by remember { mutableStateOf<PermissionItem?>(null) }
    val statusMap = remember { mutableStateMapOf<String, Int>() }
    LaunchedEffect(packageName) {
        if(packageName.isValidPackageName) {
            permissionList().forEach { statusMap[it.permission] = Privilege.DPM.getPermissionGrantState(Privilege.DAR, packageName, it.permission) }
        } else {
            statusMap.clear()
        }
    }
    MyLazyScaffold(R.string.permissions, onNavigateUp) {
        item {
            if(packageNameParam == null) {
                PackageNameTextField(packageName, Modifier.padding(HorizontalPadding, 8.dp)) { packageName = it }
                Spacer(Modifier.padding(vertical = 4.dp))
            }
        }
        items(permissionList(), { it.permission }) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(packageName.isValidPackageName) {
                        selectedPermission = it
                    }
                    .padding(8.dp)
            ) {
                Icon(painterResource(it.icon), null, Modifier.padding(horizontal = 12.dp))
                Column {
                    val state = when(statusMap[it.permission]) {
                        PERMISSION_GRANT_STATE_DEFAULT -> R.string.default_stringres
                        PERMISSION_GRANT_STATE_GRANTED -> R.string.granted
                        PERMISSION_GRANT_STATE_DENIED -> R.string.denied
                        else -> R.string.unknown
                    }
                    Text(stringResource(it.label))
                    Text(stringResource(state), Modifier.alpha(0.7F), style = typography.bodyMedium)
                }
            }
        }
        item {
            Spacer(Modifier.padding(vertical = 30.dp))
        }
    }
    if(selectedPermission != null) {
        fun changeState(state: Int) {
            val result = Privilege.DPM.setPermissionGrantState(Privilege.DAR, packageName, selectedPermission!!.permission, state)
            if (!result) context.showOperationResultToast(false)
            statusMap[selectedPermission!!.permission] = Privilege.DPM.getPermissionGrantState(Privilege.DAR, packageName, selectedPermission!!.permission)
            selectedPermission = null
        }
        @Composable
        fun GrantPermissionItem(label: Int, status: Int) {
            val selected = statusMap[selectedPermission!!.permission] == status
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if(selected) colorScheme.primaryContainer else Color.Transparent)
                    .clickable { changeState(status) }
                    .padding(vertical = 16.dp, horizontal = 12.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically,
            ) {
                Text(stringResource(label), color = if(selected) colorScheme.primary else Color.Unspecified)
                if(selected) Icon(Icons.Outlined.CheckCircle, null, tint = colorScheme.primary)
            }
        }
        AlertDialog(
            onDismissRequest = { selectedPermission = null },
            confirmButton = { TextButton({ selectedPermission = null }) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(selectedPermission!!.label)) },
            text = {
                Column {
                    Text(selectedPermission!!.permission)
                    Spacer(Modifier.padding(vertical = 4.dp))
                    if(!(VERSION.SDK_INT >= 31 && selectedPermission!!.profileOwnerRestricted && privilege.profile)) {
                        GrantPermissionItem(R.string.granted, PERMISSION_GRANT_STATE_GRANTED)
                    }
                    GrantPermissionItem(R.string.denied, PERMISSION_GRANT_STATE_DENIED)
                    GrantPermissionItem(R.string.default_stringres, PERMISSION_GRANT_STATE_DEFAULT)
                }
            }
        )
    }
}

@Serializable object DisableMeteredData

@RequiresApi(28)
@Composable
fun DisableMeteredDataScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var packageName by remember { mutableStateOf("") }
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        Privilege.DPM.getMeteredDataDisabledPackages(Privilege.DAR).forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.disable_metered_data, onNavigateUp) {
        items(packages, { it.name }) { info ->
            ApplicationItem(info) {
                Privilege.DPM.setMeteredDataDisabledPackages(Privilege.DAR, packages.minus(info).map { it.name })
                refresh()
            }
        }
        item {
            PackageNameTextField(packageName, Modifier.padding(HorizontalPadding, 8.dp)) { packageName = it }
            Button(
                {
                    if(Privilege.DPM.setMeteredDataDisabledPackages(Privilege.DAR, packages.map { it.name } + packageName).isEmpty()) {
                        packageName = ""
                    } else {
                        context.showOperationResultToast(false)
                    }
                    refresh()
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
                packageName.isValidPackageName
            ) {
                Text(stringResource(R.string.add))
            }
        }
    }
}

@Serializable object ClearAppStorage

@RequiresApi(28)
@Composable
fun ClearAppStorageScreen(onNavigateUp: () -> Unit) {
    var dialog by remember { mutableStateOf(false) }
    var packageName by remember { mutableStateOf("") }
    MyScaffold(R.string.clear_app_storage, onNavigateUp) {
        PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
        Button(
            { dialog = true },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.clear))
        }
    }
    if(dialog) ClearAppStorageDialog(packageName) { dialog = false }
}

@RequiresApi(28)
@Composable
private fun ClearAppStorageDialog(packageName: String, onClose: () -> Unit) {
    val context = LocalContext.current
    var clearing by remember { mutableStateOf(false) }
    AlertDialog(
        title = { Text(stringResource(R.string.clear_app_storage)) },
        text = {
            if(clearing) LinearProgressIndicator(Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(
                {
                    clearing = true
                    Privilege.DPM.clearApplicationUserData(
                        Privilege.DAR, packageName, Executors.newSingleThreadExecutor()
                    ) { _, it ->
                        Looper.prepare()
                        context.showOperationResultToast(it)
                        onClose()
                    }
                },
                enabled = !clearing,
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClose, enabled = !clearing) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = onClose
    )
}

@Serializable object UninstallApp

@Composable
fun UninstallAppScreen(onNavigateUp: () -> Unit) {
    var dialog by remember { mutableStateOf(false) }
    var packageName by remember { mutableStateOf("") }
    MyScaffold(R.string.uninstall_app, onNavigateUp) {
        PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
        Button(
            { dialog = true },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.uninstall))
        }
    }
    if(dialog) UninstallAppDialog(packageName) { dialog = false }
}

@Composable
private fun UninstallAppDialog(packageName: String, onClose: () -> Unit) {
    val context = LocalContext.current
    var uninstalling by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    AlertDialog(
        title = { Text(stringResource(R.string.uninstall)) },
        text = {
            if(errorMessage != null) Text(errorMessage!!)
            if(uninstalling) LinearProgressIndicator(Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(
                {
                    uninstalling = true
                    uninstallPackage(context, packageName) {
                        uninstalling = false
                        if(it == null) onClose() else errorMessage = it
                    }
                },
                enabled = !uninstalling,
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClose, enabled = !uninstalling) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = onClose
    )
}

@Serializable object KeepUninstalledPackages

@RequiresApi(28)
@Composable
fun KeepUninstalledPackagesScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        Privilege.DPM.getKeepUninstalledPackages(Privilege.DAR)?.forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.keep_uninstalled_packages, onNavigateUp) {
        items(packages, { it.name }) { info ->
            ApplicationItem(info) {
                Privilege.DPM.setKeepUninstalledPackages(Privilege.DAR, packages.minus(info).map { it.name })
                refresh()
            }
        }
        item {
            var packageName by remember { mutableStateOf("") }
            PackageNameTextField(packageName, Modifier.padding(HorizontalPadding, 8.dp)) { packageName = it }
            Button(
                {
                    Privilege.DPM.setKeepUninstalledPackages(Privilege.DAR, packages.map { it.name } + packageName)
                    packageName = ""
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding).padding(bottom = 8.dp),
                packageName.isValidPackageName
            ) {
                Text(stringResource(R.string.add))
            }
            Notes(R.string.info_keep_uninstalled_apps, HorizontalPadding)
        }
    }
}

@Serializable object InstallExistingApp

@RequiresApi(28)
@Composable
fun InstallExistingAppScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    MyScaffold(R.string.install_existing_app, onNavigateUp) {
        var packageName by remember { mutableStateOf("") }
        PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
        Button(
            {
                context.showOperationResultToast(
                    Privilege.DPM.installExistingPackage(Privilege.DAR, packageName)
                )
            },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.install))
        }
        Notes(R.string.info_install_existing_app)
    }
}

@Serializable object CrossProfilePackages

@RequiresApi(30)
@Composable
fun CrossProfilePackagesScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        Privilege.DPM.getCrossProfilePackages(Privilege.DAR).forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.cross_profile_apps, onNavigateUp) {
        items(packages, { it.name }) { info ->
            ApplicationItem(info) {
                Privilege.DPM.setCrossProfilePackages(Privilege.DAR, packages.minus(info).map { it.name }.toSet())
                refresh()
            }
        }
        item {
            var packageName by remember { mutableStateOf("") }
            PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
            Button(
                {
                    Privilege.DPM.setCrossProfilePackages(Privilege.DAR, packages.map { it.name }.toSet() + packageName)
                    packageName = ""
                    refresh()
                },
                Modifier.fillMaxWidth(),
                packageName.isValidPackageName
            ) {
                Text(stringResource(R.string.add))
            }
        }
    }
}

@Serializable object CrossProfileWidgetProviders

@Composable
fun CrossProfileWidgetProvidersScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val pm = context.packageManager
        packages.clear()
        Privilege.DPM.getCrossProfileWidgetProviders(Privilege.DAR).forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.cross_profile_widget, onNavigateUp) {
        items(packages, { it.name }) {
            ApplicationItem(it) {
                Privilege.DPM.removeCrossProfileWidgetProvider(Privilege.DAR, it.name)
                refresh()
            }
        }
        item {
            var packageName by remember { mutableStateOf("") }
            PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp, horizontal = HorizontalPadding)) { packageName = it }
            Button(
                {
                    Privilege.DPM.addCrossProfileWidgetProvider(Privilege.DAR, packageName)
                    packageName = ""
                    refresh()
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
                packageName.isValidPackageName
            ) {
                Text(stringResource(R.string.add))
            }
        }
    }
}

@Serializable object CredentialManagerPolicy

@RequiresApi(34)
@Composable
fun CredentialManagerPolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    var policyType by remember{ mutableIntStateOf(-1) }
    val packages = remember { mutableStateListOf<AppInfo>() }
    fun refresh() {
        val policy = Privilege.DPM.credentialManagerPolicy
        policyType = policy?.policyType ?: -1
        packages.clear()
        policy?.packageNames?.forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.credential_manager_policy, onNavigateUp) {
        item {
            mapOf(
                -1 to R.string.none,
                PackagePolicy.PACKAGE_POLICY_BLOCKLIST to R.string.blacklist,
                PackagePolicy.PACKAGE_POLICY_ALLOWLIST to R.string.whitelist,
                PackagePolicy.PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM to R.string.whitelist_and_system_app
            ).forEach { (key, value) ->
                FullWidthRadioButtonItem(value, policyType == key) { policyType = key }
            }
            Spacer(Modifier.padding(vertical = 4.dp))
        }
        items(packages, { it.name }) {
            ApplicationItem(it) { packages -= it }
        }
        item {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                var packageName by remember { mutableStateOf("") }
                PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp)) { packageName = it }
                Button(
                    {
                        packages += pm.retrieveAppInfo(packageName)
                    },
                    Modifier.fillMaxWidth(),
                    enabled = packageName.isValidPackageName
                ) {
                    Text(stringResource(R.string.add))
                }
                Button(
                    {
                        try {
                            if(policyType != -1 && packages.isNotEmpty()) {
                                Privilege.DPM.credentialManagerPolicy = PackagePolicy(policyType, packages.map { it.name }.toSet())
                            } else {
                            Privilege.DPM.credentialManagerPolicy = null
                            }
                            context.showOperationResultToast(true)
                        } catch(_: IllegalArgumentException) {
                            context.showOperationResultToast(false)
                        } finally {
                            refresh()
                        }
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.apply))
                }
            }
        }
    }
}

@Serializable object PermittedAccessibilityServices

@Composable
fun PermittedAccessibilityServicesScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val packages = remember { mutableStateListOf<AppInfo>() }
    var allowAll by remember { mutableStateOf(true) }
    fun refresh() {
        packages.clear()
        val list = Privilege.DPM.getPermittedAccessibilityServices(Privilege.DAR)
        allowAll = list == null
        list?.forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.permitted_accessibility_services, onNavigateUp) {
        item {
            SwitchItem(R.string.allow_all, state = allowAll, onCheckedChange = { allowAll = it })
        }
        items(packages, { it.name }) {
            ApplicationItem(it) { packages -= it }
        }
        item {
            var packageName by remember { mutableStateOf("") }
            PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp, horizontal = HorizontalPadding)) { packageName = it }
            Button(
                {
                    packages += pm.retrieveAppInfo(packageName)
                    packageName = ""
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
                packageName.isValidPackageName
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                {
                    val result = Privilege.DPM.setPermittedAccessibilityServices(Privilege.DAR, if(allowAll) null else packages.map { it.name })
                    context.showOperationResultToast(result)
                    refresh()
                },
                Modifier.fillMaxWidth().padding(top = 8.dp).padding(horizontal = HorizontalPadding)
            ) {
                Text(stringResource(R.string.apply))
            }
            Notes(R.string.system_accessibility_always_allowed, HorizontalPadding)
        }
    }
}

@Serializable object PermittedInputMethods

@Composable
fun PermittedInputMethodsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val packages = remember { mutableStateListOf<AppInfo>() }
    var allowAll by remember { mutableStateOf(true) }
    fun refresh() {
        packages.clear()
        val list = Privilege.DPM.getPermittedInputMethods(Privilege.DAR)
        allowAll = list == null
        list?.forEach {
            packages += pm.retrieveAppInfo(it)
        }
    }
    LaunchedEffect(Unit) { refresh() }
    MyLazyScaffold(R.string.permitted_ime, onNavigateUp) {
        item {
            SwitchItem(R.string.allow_all, state = allowAll, onCheckedChange = { allowAll = it })
        }
        items(packages, { it.name }) {
            ApplicationItem(it) { packages -= it }
        }
        item {
            var packageName by remember { mutableStateOf("") }
            PackageNameTextField(packageName, Modifier.padding(vertical = 8.dp, horizontal = HorizontalPadding)) { packageName = it }
            Button(
                {
                    packages += pm.retrieveAppInfo(packageName)
                    packageName = ""
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
                packageName.isValidPackageName
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                {
                    val result = Privilege.DPM.setPermittedInputMethods(Privilege.DAR, if(allowAll) null else packages.map { it.name })
                    context.showOperationResultToast(result)
                    refresh()
                },
                Modifier.fillMaxWidth().padding(top = 8.dp).padding(horizontal = HorizontalPadding)
            ) {
                Text(stringResource(R.string.apply))
            }
            Notes(R.string.system_ime_always_allowed, HorizontalPadding)
        }
    }
}

@Serializable object EnableSystemApp

@Composable
fun EnableSystemAppScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    MyScaffold(R.string.enable_system_app, onNavigateUp) {
        var packageName by remember { mutableStateOf("") }
        Spacer(Modifier.padding(vertical = 4.dp))
        PackageNameTextField(packageName, Modifier.padding(bottom = 8.dp)) { packageName = it }
        Button(
            {
                Privilege.DPM.enableSystemApp(Privilege.DAR, packageName)
                packageName = ""
                context.showOperationResultToast(true)
            },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.enable))
        }
        Notes(R.string.info_enable_system_app)
    }
}

@Serializable object SetDefaultDialer

@RequiresApi(34)
@Composable
fun SetDefaultDialerScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var errorMessage by remember { mutableStateOf<String?>(null) }
    MyScaffold(R.string.set_default_dialer, onNavigateUp) {
        var packageName by remember { mutableStateOf("") }
        Spacer(Modifier.padding(vertical = 4.dp))
        PackageNameTextField(packageName, Modifier.padding(bottom = 8.dp)) { packageName = it }
        Button(
            {
                try {
                    Privilege.DPM.setDefaultDialerApplication(packageName)
                    context.showOperationResultToast(true)
                } catch(e: Exception) {
                    errorMessage = e.message
                }
            },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.set))
        }
    }
    ErrorDialog(errorMessage) { errorMessage = null }
}

private fun uninstallPackage(context: Context, packageName: String, onComplete: (String?) -> Unit) {
    val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val statusExtra = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
            if(statusExtra == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                @SuppressWarnings("UnsafeIntentLaunch")
                context.startActivity(intent.getParcelableExtra(Intent.EXTRA_INTENT) as Intent?)
            } else {
                context.unregisterReceiver(this)
                if(statusExtra == PackageInstaller.STATUS_SUCCESS) {
                    onComplete(null)
                } else {
                    onComplete(parsePackageInstallerMessage(context, intent))
                }
            }
        }
    }
    ContextCompat.registerReceiver(
        context, receiver, IntentFilter(AppInstallerViewModel.ACTION), null,
        null, ContextCompat.RECEIVER_EXPORTED
    )
    val pi = if(VERSION.SDK_INT >= 34) {
        PendingIntent.getBroadcast(
            context, 0, Intent(AppInstallerViewModel.ACTION),
            PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_MUTABLE
        ).intentSender
    } else {
        PendingIntent.getBroadcast(context, 0, Intent(AppInstallerViewModel.ACTION), PendingIntent.FLAG_MUTABLE).intentSender
    }
    context.getPackageInstaller().uninstall(packageName, pi)
}
