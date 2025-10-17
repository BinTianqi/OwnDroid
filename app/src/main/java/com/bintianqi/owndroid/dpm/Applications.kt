package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
import android.app.admin.PackagePolicy
import android.content.Intent
import android.os.Build.VERSION
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.AppInfo
import com.bintianqi.owndroid.AppInstallerActivity
import com.bintianqi.owndroid.BottomPadding
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.adaptiveInsets
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

val String.isValidPackageName
    get() = Regex("""^(?:[a-zA-Z]\w*\.)+[a-zA-Z]\w*$""").matches(this)

@Composable
fun LazyItemScope.ApplicationItem(info: AppInfo, onClear: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 6.dp).animateItem(),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
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
fun PackageNameTextField(
    value: String, onChoosePackage: () -> Unit,
    modifier: Modifier = Modifier, onValueChange: (String) -> Unit
) {
    val fm = LocalFocusManager.current
    OutlinedTextField(
        value, onValueChange, Modifier.fillMaxWidth().then(modifier),
        label = { Text(stringResource(R.string.package_name)) },
        trailingIcon = {
            IconButton(onChoosePackage) {
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
        contentWindowInsets = adaptiveInsets()
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

data class AppStatus(
    val suspend: Boolean,
    val hide: Boolean,
    val uninstallBlocked: Boolean,
    val userControlDisabled: Boolean,
    val meteredDataDisabled: Boolean,
    val keepUninstalled: Boolean
)

@Composable
fun ApplicationDetailsScreen(
    param: ApplicationDetails, vm: MyViewModel, onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit
) {
    val packageName = param.packageName
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) } // 1: clear storage, 2: uninstall
    val info = vm.getAppInfo(packageName)
    val status by vm.appStatus.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.getAppStatus(packageName) }
    MySmallTitleScaffold(R.string.place_holder, onNavigateUp, 0.dp) {
        Column(Modifier.align(Alignment.CenterHorizontally).padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Image(rememberDrawablePainter(info.icon), null, Modifier.size(50.dp))
            Text(info.label, Modifier.padding(top = 4.dp))
            Text(info.name, Modifier.alpha(0.7F).padding(bottom = 8.dp), style = typography.bodyMedium)
        }
        FunctionItem(R.string.permissions, icon = R.drawable.shield_fill0) { onNavigate(PermissionsManager(packageName)) }
        if(VERSION.SDK_INT >= 24) SwitchItem(
            R.string.suspend, icon = R.drawable.block_fill0, state = status.suspend,
            onCheckedChange = { vm.adSetPackageSuspended(packageName, it) }
        )
        SwitchItem(
            R.string.hide, icon = R.drawable.visibility_off_fill0,
            state = status.hide,
            onCheckedChange = { vm.adSetPackageHidden(packageName, it) }
        )
        SwitchItem(
            R.string.block_uninstall, icon = R.drawable.delete_forever_fill0,
            state = status.uninstallBlocked,
            onCheckedChange = { vm.adSetPackageUb(packageName, it) }
        )
        if(VERSION.SDK_INT >= 30) SwitchItem(
            R.string.disable_user_control, icon = R.drawable.do_not_touch_fill0,
            state = status.userControlDisabled,
            onCheckedChange = { vm.adSetPackageUcd(packageName, it) }
        )
        if(VERSION.SDK_INT >= 28) SwitchItem(
            R.string.disable_metered_data, icon = R.drawable.money_off_fill0,
            state = status.meteredDataDisabled,
            onCheckedChange = { vm.adSetPackageMdd(packageName, it) }
        )
        if(privilege.device && VERSION.SDK_INT >= 28) SwitchItem(
            R.string.keep_after_uninstall, icon = R.drawable.delete_fill0,
            state = status.keepUninstalled,
            onCheckedChange = { vm.adSetPackageKu(packageName, it) }
        )
        if(VERSION.SDK_INT >= 28) FunctionItem(R.string.clear_app_storage, icon = R.drawable.mop_fill0) { dialog = 1 }
        FunctionItem(R.string.uninstall, icon = R.drawable.delete_fill0) { dialog = 2 }
        Spacer(Modifier.height(BottomPadding))
    }
    if(dialog == 1 && VERSION.SDK_INT >= 28)
        ClearAppStorageDialog(packageName, vm::clearAppData) { dialog = 0 }
    if(dialog == 2) UninstallAppDialog(packageName, vm::uninstallPackage) { dialog = 0 }
}

@Serializable object Suspend

@Serializable object Hide

@Serializable object BlockUninstall

@Serializable object DisableUserControl

@Serializable data class PermissionsManager(val packageName: String? = null)

@RequiresApi(23)
@Composable
fun PermissionsManagerScreen(
    packagePermissions: MutableStateFlow<Map<String, Int>>, getPackagePermissions: (String) -> Unit,
    setPackagePermission: (String, String, Int) -> Boolean, onNavigateUp: () -> Unit,
    param: PermissionsManager, chosenPackage: Channel<String>, onChoosePackage: () -> Unit
) {
    val packageNameParam = param.packageName
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf(packageNameParam ?: "") }
    var selectedPermission by rememberSaveable { mutableIntStateOf(-1) }
    val permissions by packagePermissions.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    LaunchedEffect(packageName) {
        getPackagePermissions(packageName)
    }
    MyLazyScaffold(R.string.permissions, onNavigateUp) {
        item {
            if(packageNameParam == null) {
                PackageNameTextField(packageName, onChoosePackage,
                    Modifier.padding(HorizontalPadding, 8.dp)) { packageName = it }
                Spacer(Modifier.padding(vertical = 4.dp))
            }
        }
        itemsIndexed(runtimePermissions, { _, it -> it.id }) { index, it ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(packageName.isValidPackageName) {
                        selectedPermission = index
                    }
                    .padding(8.dp)
            ) {
                Icon(painterResource(it.icon), null, Modifier.padding(horizontal = 12.dp))
                Column {
                    val state = when(permissions[it.id]) {
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
            Spacer(Modifier.height(BottomPadding))
        }
    }
    if(selectedPermission != -1) {
        val permission = runtimePermissions[selectedPermission]
        fun changeState(state: Int) {
            val result = setPackagePermission(packageName, permission.id, state)
            if (result) selectedPermission = -1
        }
        @Composable
        fun GrantPermissionItem(label: Int, status: Int) {
            val selected = permissions[permission.id] == status
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
            onDismissRequest = { selectedPermission = -1 },
            confirmButton = { TextButton({ selectedPermission = -1 }) { Text(stringResource(R.string.cancel)) } },
            title = { Text(stringResource(permission.label)) },
            text = {
                Column {
                    Text(permission.id)
                    Spacer(Modifier.padding(vertical = 4.dp))
                    if(!(VERSION.SDK_INT >= 31 && permission.profileOwnerRestricted && privilege.profile)) {
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

@Serializable object ClearAppStorage

@RequiresApi(28)
@Composable
fun ClearAppStorageScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onClear: (String, (Boolean) -> Unit) -> Unit, onNavigateUp: () -> Unit
) {
    var dialog by rememberSaveable { mutableStateOf(false) }
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.clear_app_storage, onNavigateUp) {
        PackageNameTextField(packageName, onChoosePackage,
            Modifier.padding(vertical = 8.dp)) { packageName = it }
        Button(
            { dialog = true },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.clear))
        }
    }
    if(dialog) ClearAppStorageDialog(packageName, onClear) { dialog = false }
}

@RequiresApi(28)
@Composable
private fun ClearAppStorageDialog(
    packageName: String, onClear: (String, (Boolean) -> Unit) -> Unit, onClose: () -> Unit
) {
    val context = LocalContext.current
    var clearing by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        title = { Text(stringResource(R.string.clear_app_storage)) },
        text = {
            if(clearing) LinearProgressIndicator(Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(
                {
                    clearing = true
                    onClear(packageName) {
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
        onDismissRequest = {
            if (!clearing) onClose()
        },
        properties = DialogProperties(false, false)
    )
}

@Serializable object UninstallApp

@Composable
fun UninstallAppScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onUninstall: (String, (String?) -> Unit) -> Unit, onNavigateUp: () -> Unit
) {
    var dialog by rememberSaveable { mutableStateOf(false) }
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.uninstall_app, onNavigateUp) {
        PackageNameTextField(packageName, onChoosePackage,
            Modifier.padding(vertical = 8.dp)) { packageName = it }
        Button(
            { dialog = true },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.uninstall))
        }
    }
    if(dialog) UninstallAppDialog(packageName, onUninstall) {
        packageName = ""
        dialog = false
    }
}

@Composable
private fun UninstallAppDialog(
    packageName: String, onUninstall: (String, (String?) -> Unit) -> Unit, onClose: () -> Unit
) {
    var uninstalling by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
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
                    onUninstall(packageName) {
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
        onDismissRequest = onClose,
        properties = DialogProperties(false, false)
    )
}

@Serializable object KeepUninstalledPackages

@Serializable object InstallExistingApp

@RequiresApi(28)
@Composable
fun InstallExistingAppScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onInstall: (String) -> Boolean, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.install_existing_app, onNavigateUp) {
        PackageNameTextField(packageName, onChoosePackage,
            Modifier.padding(vertical = 8.dp)) { packageName = it }
        Button(
            {
                context.showOperationResultToast(onInstall(packageName))
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

@Serializable object CrossProfileWidgetProviders

@Serializable object CredentialManagerPolicy

@RequiresApi(34)
@Composable
fun CredentialManagerPolicyScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    cmPackages: MutableStateFlow<List<AppInfo>>, getCmPolicy: () -> Int,
    setCmPackage: (String, Boolean) -> Unit, setCmPolicy: (Int) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var policy by rememberSaveable { mutableIntStateOf(getCmPolicy()) }
    val packages by cmPackages.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyLazyScaffold(R.string.credential_manager_policy, onNavigateUp) {
        item {
            mapOf(
                -1 to R.string.none,
                PackagePolicy.PACKAGE_POLICY_BLOCKLIST to R.string.blacklist,
                PackagePolicy.PACKAGE_POLICY_ALLOWLIST to R.string.whitelist,
                PackagePolicy.PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM to R.string.whitelist_and_system_app
            ).forEach { (key, value) ->
                FullWidthRadioButtonItem(value, policy == key) { policy = key }
            }
            Spacer(Modifier.padding(vertical = 4.dp))
        }
        if (policy != -1) items(packages, { it.name }) {
            ApplicationItem(it) { setCmPackage(it.name, false) }
        }
        item {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                if (policy != -1) {
                    PackageNameTextField(packageName, onChoosePackage,
                        Modifier.padding(vertical = 8.dp)) { packageName = it }
                    Button(
                        {
                            setCmPackage(packageName, true)
                            packageName = ""
                        },
                        Modifier.fillMaxWidth(),
                        enabled = packageName.isValidPackageName
                    ) {
                        Text(stringResource(R.string.add))
                    }
                }
                Button(
                    {
                        setCmPolicy(policy)
                        context.showOperationResultToast(true)
                    },
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.apply))
                }
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}

@Serializable object PermittedAccessibilityServices

@Serializable object PermittedInputMethods

@Composable
fun PermittedAsAndImPackages(
    title: Int, note: Int, chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    packagesState: MutableStateFlow<List<AppInfo>>, getPackages: () -> Boolean,
    setPackage: (String, Boolean) -> Unit, setPolicy: (Boolean) -> Boolean, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val packages by packagesState.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf("") }
    var allowAll by rememberSaveable { mutableStateOf(getPackages()) }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyLazyScaffold(title, onNavigateUp) {
        item {
            SwitchItem(R.string.allow_all, state = allowAll, onCheckedChange = { allowAll = it })
        }
        if (!allowAll) items(packages, { it.name }) {
            ApplicationItem(it) { setPackage(it.name, false) }
        }
        item {
            if (!allowAll) {
                PackageNameTextField(packageName, onChoosePackage,
                    Modifier.padding(HorizontalPadding, 8.dp)) { packageName = it }
                Button(
                    {
                        setPackage(packageName, true)
                        packageName = ""
                    },
                    Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding),
                    packageName.isValidPackageName
                ) {
                    Text(stringResource(R.string.add))
                }
            }
            Button(
                {
                    context.showOperationResultToast(setPolicy(allowAll))
                },
                Modifier.fillMaxWidth().padding(top = 8.dp).padding(horizontal = HorizontalPadding)
            ) {
                Text(stringResource(R.string.apply))
            }
            Spacer(Modifier.height(10.dp))
            Notes(note, HorizontalPadding)
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

@Serializable object EnableSystemApp

@Composable
fun EnableSystemAppScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onEnable: (String) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.enable_system_app, onNavigateUp) {
        Spacer(Modifier.padding(vertical = 4.dp))
        PackageNameTextField(packageName, onChoosePackage,
            Modifier.padding(bottom = 8.dp)) { packageName = it }
        Button(
            {
                onEnable(packageName)
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
fun SetDefaultDialerScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onSet: (String) -> Unit, onNavigateUp: () -> Unit
) {
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.set_default_dialer, onNavigateUp) {
        Spacer(Modifier.padding(vertical = 4.dp))
        PackageNameTextField(packageName, onChoosePackage,
            Modifier.padding(bottom = 8.dp)) { packageName = it }
        Button(
            {
                onSet(packageName)
            },
            Modifier.fillMaxWidth(),
            packageName.isValidPackageName
        ) {
            Text(stringResource(R.string.set))
        }
    }
}

@Composable
fun PackageFunctionScreenWithoutResult(
    title: Int, packagesState: MutableStateFlow<List<AppInfo>>, onGet: () -> Unit,
    onSet: (String, Boolean) -> Unit, onNavigateUp: () -> Unit,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit, notes: Int? = null
) {
    PackageFunctionScreen(
        title, packagesState, onGet, { name, status -> onSet(name, status); null },
        onNavigateUp, chosenPackage, onChoosePackage, notes
    )
}

@Composable
fun PackageFunctionScreen(
    title: Int, packagesState: MutableStateFlow<List<AppInfo>>, onGet: () -> Unit,
    onSet: (String, Boolean) -> Boolean?, onNavigateUp: () -> Unit,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit, notes: Int? = null
) {
    val packages by packagesState.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        onGet()
        packageName = chosenPackage.receive()
    }
    MyLazyScaffold(title, onNavigateUp) {
        items(packages, { it.name }) {
            ApplicationItem(it) {
                onSet(it.name, false)
            }
        }
        item {
            PackageNameTextField(packageName, onChoosePackage,
                Modifier.padding(HorizontalPadding, 8.dp)) { packageName = it }
            Button(
                {
                    if (onSet(packageName, true) != false) {
                        println("reset")
                        packageName = ""
                    }
                },
                Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding).padding(bottom = 10.dp),
                packageName.isValidPackageName
            ) {
                Text(stringResource(R.string.add))
            }
            if (notes != null) Notes(notes, HorizontalPadding)
            Spacer(Modifier.height(BottomPadding))
        }
    }
}