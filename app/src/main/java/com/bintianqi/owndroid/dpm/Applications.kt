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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
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
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

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
    val appRestrictions by vm.appRestrictions.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        vm.getAppStatus(packageName)
        if (VERSION.SDK_INT >= 23) vm.getAppRestrictions(packageName)
    }
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
        if (VERSION.SDK_INT >= 23 && appRestrictions.isNotEmpty()) {
            FunctionItem(R.string.managed_configuration, icon = R.drawable.description_fill0) {
                onNavigate(ManagedConfiguration(packageName))
            }
        }
        if(VERSION.SDK_INT >= 28) FunctionItem(R.string.clear_app_storage, icon = R.drawable.mop_fill0) { dialog = 1 }
        FunctionItem(R.string.uninstall, icon = R.drawable.delete_fill0) { dialog = 2 }
        Spacer(Modifier.height(BottomPadding))
    }
    if(dialog == 1 && VERSION.SDK_INT >= 28)
        ClearAppStorageDialog(packageName, vm::clearAppData) { dialog = 0 }
    if(dialog == 2) UninstallAppDialog(packageName, vm::uninstallPackage) {
        dialog = 0
        if (it) onNavigateUp()
    }
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
    packageName: String, onUninstall: (String, (String?) -> Unit) -> Unit,
    onClose: (Boolean) -> Unit
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
                    if (errorMessage == null) {
                        uninstalling = true
                        onUninstall(packageName) {
                            uninstalling = false
                            if (it == null) onClose(true) else errorMessage = it
                        }
                    } else {
                        onClose(false)
                    }
                },
                enabled = !uninstalling
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            if (errorMessage == null) TextButton({
                onClose(false)
            }, enabled = !uninstalling) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { onClose(false) },
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
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    navigateToGroups: () -> Unit, appGroups: StateFlow<List<AppGroup>>, notes: Int? = null
) {
    PackageFunctionScreen(
        title, packagesState, onGet, { name, status -> onSet(name, status); null },
        onNavigateUp, chosenPackage, onChoosePackage, navigateToGroups, appGroups, notes
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageFunctionScreen(
    title: Int, packagesState: MutableStateFlow<List<AppInfo>>, onGet: () -> Unit,
    onSet: (String, Boolean) -> Boolean?, onNavigateUp: () -> Unit,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    navigateToGroups: () -> Unit, appGroups: StateFlow<List<AppGroup>>, notes: Int? = null
) {
    val groups by appGroups.collectAsStateWithLifecycle()
    val packages by packagesState.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf("") }
    var dialog by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<AppGroup?>(null) }
    LaunchedEffect(Unit) {
        onGet()
        packageName = chosenPackage.receive()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(title)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    var expand by remember { mutableStateOf(false) }
                    Box {
                        IconButton({
                            expand = true
                        }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(expand, { expand = false }) {
                            groups.forEach {
                                DropdownMenuItem(
                                    { Text("(${it.apps.size}) ${it.name}") },
                                    {
                                        selectedGroup = it
                                        dialog = true
                                        expand = false
                                    }
                                )
                            }
                            if (groups.isNotEmpty()) HorizontalDivider()
                            DropdownMenuItem(
                                { Text(stringResource(R.string.manage_app_groups)) },
                                {
                                    navigateToGroups()
                                    expand = false
                                }
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
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
                            packageName = ""
                        }
                    },
                    Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding).padding(bottom = 10.dp),
                    packageName.isValidPackageName &&
                            packages.find { it.name == packageName } == null
                ) {
                    Text(stringResource(R.string.add))
                }
                if (notes != null) Notes(notes, HorizontalPadding)
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
    if (dialog) AlertDialog(
        text = {
            Column {
                Button({
                    selectedGroup!!.apps.forEach {
                        onSet(it, true)
                    }
                    dialog = false
                }) {
                    Text(stringResource(R.string.add_to_list))
                }
                Button({
                    selectedGroup!!.apps.forEach {
                        onSet(it, false)
                    }
                    dialog = false
                }) {
                    Text(stringResource(R.string.remove_from_list))
                }
            }
        },
        confirmButton = {
            TextButton({ dialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = false }
    )
}

class AppGroup(val id: Int, val name: String, val apps: List<String>)

@Serializable object ManageAppGroups

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageAppGroupsScreen(
    appGroups: StateFlow<List<AppGroup>>,
    navigateToEditScreen: (Int?, String, List<String>) -> Unit, navigateUp: () -> Unit
) {
    val groups by appGroups.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.app_group)) },
                navigationIcon = { NavIcon(navigateUp) }
            )
        },
        floatingActionButton = {
            FloatingActionButton({
                navigateToEditScreen(null, "", emptyList())
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(groups, { it.id }) {
                Column(
                    Modifier.fillMaxWidth().clickable {
                        navigateToEditScreen(it.id, it.name, it.apps)
                    }.padding(HorizontalPadding, 8.dp)
                ) {
                    Text(it.name)
                    Text(
                        it.apps.size.toString() + " apps", Modifier.alpha(0.7F),
                        style = typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Serializable class EditAppGroup(val id: Int?, val name: String, val apps: List<String>)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppGroupScreen(
    params: EditAppGroup, getAppInfo: (String) -> AppInfo, navigateUp: () -> Unit,
    setGroup: (Int?, String, List<String>) -> Unit, deleteGroup: (Int) -> Unit,
    onChoosePackage: () -> Unit, chosenPackage: Channel<String>
) {
    var name by rememberSaveable { mutableStateOf(params.name) }
    val list = rememberSaveable { mutableStateListOf(*params.apps.toTypedArray()) }
    val appInfoList = list.map { getAppInfo(it) }
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.edit_app_group)) },
                navigationIcon = {
                    NavIcon(navigateUp)
                },
                actions = {
                    if (params.id != null) IconButton({
                        deleteGroup(params.id)
                        navigateUp()
                    }) {
                        Icon(Icons.Outlined.Delete, null)
                    }
                    IconButton(
                        {
                            setGroup(params.id, name, list)
                            navigateUp()
                        },
                        enabled = name.isNotBlank() && list.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, null)
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            item {
                OutlinedTextField(
                    name, { name = it }, Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp),
                    label = { Text(stringResource(R.string.name)) }
                )
            }
            items(appInfoList, { it.name }) {
                ApplicationItem(it) {
                    list -= it.name
                }
            }
            item {
                PackageNameTextField(packageName, onChoosePackage,
                    Modifier.padding(HorizontalPadding, 8.dp)) { packageName = it }
                Button(
                    {
                        list += packageName
                        packageName = ""
                    },
                    Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding).padding(bottom = 10.dp),
                    packageName.isValidPackageName && packageName !in list
                ) {
                    Text(stringResource(R.string.add))
                }
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}

@Serializable class ManagedConfiguration(val packageName: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedConfigurationScreen(
    params: ManagedConfiguration, appRestrictions: StateFlow<List<AppRestriction>>,
    setRestriction: (String, AppRestriction) -> Unit, clearRestriction: (String) -> Unit,
    navigateUp: () -> Unit
) {
    val restrictions by appRestrictions.collectAsStateWithLifecycle()
    var searchMode by remember { mutableStateOf(false) }
    var searchKeyword by remember { mutableStateOf("") }
    val displayRestrictions = if (searchKeyword.isEmpty()) {
        restrictions
    } else {
        restrictions.filter {
            it.key.contains(searchKeyword, true) ||
                    it.title?.contains(searchKeyword, true) ?: true
        }
    }
    var dialog by remember { mutableStateOf<AppRestriction?>(null) }
    var clearRestrictionDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                {
                    if (searchMode) {
                        val fr = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            fr.requestFocus()
                        }
                        OutlinedTextField(
                            searchKeyword, { searchKeyword = it },
                            Modifier.fillMaxWidth().focusRequester(fr),
                            textStyle = typography.bodyLarge,
                            placeholder = { Text(stringResource(R.string.search)) },
                            trailingIcon = {
                                IconButton({
                                    searchKeyword = ""
                                    searchMode = false
                                }) {
                                    Icon(Icons.Outlined.Clear, null)
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    } else {
                        Text(stringResource(R.string.managed_configuration))
                    }
                },
                navigationIcon = { NavIcon(navigateUp) },
                actions = {
                    if (!searchMode) {
                        IconButton({
                            searchMode = true
                        }) {
                            Icon(Icons.Outlined.Search, null)
                        }
                        IconButton({
                            clearRestrictionDialog = true
                        }) {
                            Icon(Icons.Outlined.Delete, null)
                        }
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(displayRestrictions, { it.key }) { entry ->
                Row(
                    Modifier.fillMaxWidth().clickable {
                        dialog = entry
                    }.padding(HorizontalPadding, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconId = when (entry) {
                        is AppRestriction.IntItem -> R.drawable.number_123_fill0
                        is AppRestriction.StringItem -> R.drawable.abc_fill0
                        is AppRestriction.BooleanItem -> R.drawable.toggle_off_fill0
                        is AppRestriction.ChoiceItem -> R.drawable.radio_button_checked_fill0
                        is AppRestriction.MultiSelectItem -> R.drawable.check_box_fill0
                    }
                    Icon(painterResource(iconId), null, Modifier.padding(end = 12.dp))
                    Column {
                        if (entry.title != null) {
                            Text(entry.title!!, style = typography.labelLarge)
                            Text(entry.key, style = typography.bodyMedium)
                        } else {
                            Text(entry.key, style = typography.labelLarge)
                        }
                        val text = when (entry) {
                            is AppRestriction.IntItem -> entry.value?.toString()
                            is AppRestriction.StringItem -> entry.value?.take(30)
                            is AppRestriction.BooleanItem -> entry.value?.toString()
                            is AppRestriction.ChoiceItem -> entry.value
                            is AppRestriction.MultiSelectItem -> entry.value?.joinToString(limit = 30)
                        }
                        Text(
                            text ?: "null", Modifier.alpha(0.7F),
                            fontStyle = if(text == null) FontStyle.Italic else null,
                            style = typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
    if (dialog != null) Dialog({
        dialog = null
    }) {
        Surface(
            color = AlertDialogDefaults.containerColor,
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            ManagedConfigurationDialog(dialog!!) {
                if (it != null) {
                    setRestriction(params.packageName, it)
                }
                dialog = null
            }
        }
    }
    if (clearRestrictionDialog) AlertDialog(
        text = {
            Text(stringResource(R.string.clear_configurations))
        },
        confirmButton = {
            TextButton({
                clearRestriction(params.packageName)
                clearRestrictionDialog = false
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({
                clearRestrictionDialog = false
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = {
            clearRestrictionDialog = false
        }
    )
}

@Composable
fun ManagedConfigurationDialog(
    restriction: AppRestriction, setRestriction: (AppRestriction?) -> Unit
) {
    var specifyValue by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var inputState by remember { mutableStateOf(false) }
    val multiSelectList = remember {
        mutableStateListOf(
            *(if (restriction is AppRestriction.MultiSelectItem) {
                restriction.entryValues.mapIndexed { index, value ->
                    MultiSelectEntry(
                        value, restriction.entries.getOrNull(index),
                        restriction.value?.contains(value) ?: false
                    )
                }.sortedBy { entry ->
                    val index = restriction.value?.indexOf(entry.value)
                    if (index == null || index == -1) Int.MAX_VALUE else index
                }
            } else emptyList()).toTypedArray()
        )
    }
    LaunchedEffect(Unit) {
        when (restriction) {
            is AppRestriction.IntItem -> restriction.value?.let {
                input = it.toString()
                specifyValue = true
            }
            is AppRestriction.StringItem -> restriction.value?.let {
                input = it
                specifyValue = true
            }
            is AppRestriction.BooleanItem -> restriction.value?.let {
                inputState = it
                specifyValue = true
            }
            is AppRestriction.ChoiceItem -> restriction.value?.let {
                input = it
                specifyValue = true
            }
            is AppRestriction.MultiSelectItem -> restriction.value?.let {
                specifyValue = true
            }
        }
    }
    val listState = rememberLazyListState()
    val reorderableListState = rememberReorderableLazyListState(listState) { from, to ->
        // `-1` because there's an `item` before items
        multiSelectList.add(from.index - 1, multiSelectList.removeAt(to.index - 1))
    }
    LazyColumn(Modifier.padding(12.dp), listState) {
        item {
            SelectionContainer {
                Column {
                    restriction.title?.let {
                        Text(it, style = typography.titleLarge)
                    }
                    Text(restriction.key, Modifier.padding(vertical = 4.dp), style = typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    restriction.description?.let {
                        Text(it, Modifier.alpha(0.8F), style = typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Row(
                Modifier.fillMaxWidth().padding(bottom = 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.specify_value))
                Switch(specifyValue, { specifyValue = it })
            }
        }
        if (specifyValue) when (restriction) {
            is AppRestriction.IntItem -> item {
                OutlinedTextField(
                    input, { input = it }, Modifier.fillMaxWidth(),
                    isError = input.toIntOrNull() == null
                )
            }
            is AppRestriction.StringItem -> item {
                OutlinedTextField(
                    input, { input = it }, Modifier.fillMaxWidth()
                )
            }
            is AppRestriction.BooleanItem -> item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        inputState, { inputState = true },
                        SegmentedButtonDefaults.itemShape(0, 2)
                    ) {
                        Text("true")
                    }
                    SegmentedButton(
                        !inputState, { inputState = false },
                        SegmentedButtonDefaults.itemShape(1, 2)
                    ) {
                        Text("false")
                    }
                }
            }
            is AppRestriction.ChoiceItem -> itemsIndexed(restriction.entryValues) { index, value ->
                val label = restriction.entries.getOrNull(index)
                Row(
                    Modifier.fillMaxWidth().clickable {
                        input = value
                    }.padding(8.dp, 4.dp)
                ) {
                    RadioButton(input == value, { input = value })
                    Spacer(Modifier.width(8.dp))
                    if (label == null) {
                        Text(value)
                    } else {
                        Column {
                            Text(label)
                            Text(value, Modifier.alpha(0.7F), style = typography.bodyMedium)
                        }
                    }
                }
            }
            is AppRestriction.MultiSelectItem -> itemsIndexed(
                multiSelectList, { _, v -> v.value }
            ) { index, entry ->
                ReorderableItem(reorderableListState, entry.value) {
                    Row(
                        Modifier.fillMaxWidth().clickable {
                            val old = multiSelectList[index]
                            multiSelectList[index] = old.copy(selected = !old.selected)
                        }.padding(8.dp, 4.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(entry.selected, null)
                            Spacer(Modifier.width(8.dp))
                            if (entry.title == null) {
                                Text(entry.value)
                            } else {
                                Column {
                                    Text(entry.title)
                                    Text(entry.value, Modifier.alpha(0.7F), style = typography.bodyMedium)
                                }
                            }
                        }
                        Icon(
                            painterResource(R.drawable.drag_indicator_fill0), null,
                            Modifier.draggableHandle()
                        )
                    }
                }
            }
        }
        item {
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), Arrangement.End) {
                TextButton({
                    setRestriction(null)
                }, Modifier.padding(end = 4.dp)) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton({
                    val newRestriction = when (restriction) {
                        is AppRestriction.IntItem -> restriction.copy(
                            value = if (specifyValue) input.toIntOrNull() else null
                        )
                        is AppRestriction.StringItem -> restriction.copy(
                            value = if (specifyValue) input else null
                        )
                        is AppRestriction.BooleanItem -> restriction.copy(
                            value = if (specifyValue) inputState else null
                        )
                        is AppRestriction.ChoiceItem -> restriction.copy(
                            value = if (specifyValue) input else null
                        )
                        is AppRestriction.MultiSelectItem -> restriction.copy(
                            value = if (specifyValue)
                                multiSelectList.filter { it.selected }
                                    .map { it.value }.toTypedArray()
                            else null
                        )
                    }
                    setRestriction(newRestriction)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}

sealed class AppRestriction(
    open val key: String, open val title: String?, open val description: String?
) {
    data class IntItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: Int?,
    ) : AppRestriction(key, title, description)
    data class StringItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: String?
    ) : AppRestriction(key, title, description)
    data class BooleanItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        var value: Boolean?
    ) : AppRestriction(key, title, description)
    data class ChoiceItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        val entries: Array<String>,
        val entryValues: Array<String>,
        var value: String?
    ) : AppRestriction(key, title, description)
    data class MultiSelectItem(
        override val key: String,
        override val title: String?,
        override val description: String?,
        val entries: Array<String>,
        val entryValues: Array<String>,
        var value: Array<String>?
    ) : AppRestriction(key, title, description)
}

data class MultiSelectEntry(val value: String, val title: String?, val selected: Boolean)
