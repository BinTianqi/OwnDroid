package com.bintianqi.owndroid.feature.applications

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.AppInstallerActivity
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.PackageNameTextField
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.isValidPackageName
import com.bintianqi.owndroid.utils.parsePackageNames
import com.bintianqi.owndroid.utils.runtimePermissions
import com.bintianqi.owndroid.utils.searchInString
import com.bintianqi.owndroid.utils.showOperationResultToast
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApplicationsFeaturesScreen(
    vm: AppFeaturesViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit,
    onSwitchView: () -> Unit
) {
    val context = LocalContext.current
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.applications)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    Box {
                        var dropdown by remember { mutableStateOf(false) }
                        IconButton({ dropdown = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(dropdown, { dropdown = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.apps_view)) },
                                {
                                    dropdown = false
                                    onSwitchView()
                                },
                                leadingIcon = { RadioButton(false, null) }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.features_view)) },
                                {},
                                leadingIcon = { RadioButton(true, null) }
                            )
                        }
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
            val privilege by vm.privilegeState.collectAsStateWithLifecycle()
            if (VERSION.SDK_INT >= 24) FunctionItem(
                R.string.suspend, icon = R.drawable.block_fill0
            ) {
                onNavigate(Destination.Suspend)
            }
            FunctionItem(R.string.hide, icon = R.drawable.visibility_off_fill0) {
                onNavigate(Destination.Hide)
            }
            FunctionItem(R.string.block_uninstall, icon = R.drawable.delete_forever_fill0) {
                onNavigate(Destination.BlockUninstall)
            }
            if (VERSION.SDK_INT >= 30 && (privilege.device || (VERSION.SDK_INT >= 33 && privilege.profile))) {
                FunctionItem(R.string.disable_user_control, icon = R.drawable.do_not_touch_fill0) {
                    onNavigate(Destination.DisableUserControl)
                }
            }
            FunctionItem(R.string.permissions, icon = R.drawable.shield_fill0) {
                onNavigate(Destination.PermissionManager)
            }
            if (VERSION.SDK_INT >= 28) {
                FunctionItem(R.string.disable_metered_data, icon = R.drawable.money_off_fill0) {
                    onNavigate(Destination.DisableMeteredData)
                }
            }
            if (VERSION.SDK_INT >= 28) {
                FunctionItem(R.string.clear_app_storage, icon = R.drawable.mop_fill0) {
                    onNavigate(Destination.ClearAppStorage)
                }
            }
            FunctionItem(R.string.install_app, icon = R.drawable.install_mobile_fill0) {
                context.startActivity(Intent(context, AppInstallerActivity::class.java))
            }
            FunctionItem(R.string.uninstall_app, icon = R.drawable.delete_fill0) {
                onNavigate(Destination.UninstallApp)
            }
            if (VERSION.SDK_INT >= 28 && privilege.device) {
                FunctionItem(R.string.keep_uninstalled_packages, icon = R.drawable.delete_fill0) {
                    onNavigate(Destination.KeepUninstalledPackages)
                }
            }
            if (VERSION.SDK_INT >= 28 && (privilege.device || (privilege.profile && privilege.affiliated))) {
                FunctionItem(
                    R.string.install_existing_app, icon = R.drawable.install_mobile_fill0
                ) {
                    onNavigate(Destination.InstallExistingApp)
                }
            }
            if (VERSION.SDK_INT >= 30 && privilege.work) {
                FunctionItem(R.string.cross_profile_apps, icon = R.drawable.work_fill0) {
                    onNavigate(Destination.CrossProfilePackages)
                }
            }
            if (privilege.work) {
                FunctionItem(R.string.cross_profile_widget, icon = R.drawable.widgets_fill0) {
                    onNavigate(Destination.CrossProfileWidgetProviders)
                }
            }
            if (VERSION.SDK_INT >= 34 && privilege.device) {
                FunctionItem(R.string.credential_manager_policy, icon = R.drawable.license_fill0) {
                    onNavigate(Destination.CredentialManagerPolicy)
                }
            }
            FunctionItem(
                R.string.permitted_accessibility_services,
                icon = R.drawable.settings_accessibility_fill0
            ) {
                onNavigate(Destination.PermittedAccessibilityServices)
            }
            FunctionItem(R.string.permitted_ime, icon = R.drawable.keyboard_fill0) {
                onNavigate(Destination.PermittedInputMethods)
            }
            FunctionItem(R.string.enable_system_app, icon = R.drawable.enable_fill0) {
                onNavigate(Destination.EnableSystemApp)
            }
            if (VERSION.SDK_INT >= 34 && (privilege.device || privilege.work)) {
                FunctionItem(R.string.set_default_dialer, icon = R.drawable.call_fill0) {
                    onNavigate(Destination.SetDefaultDialer)
                }
            }
        }
    }
}


@Composable
fun PermissionManagerScreen(
    onNavigate: (Destination.PermissionDetail) -> Unit, onNavigateUp: () -> Unit
) {
    MyLazyScaffold(R.string.permissions, onNavigateUp) {
        items(runtimePermissions) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onNavigate(Destination.PermissionDetail(it.id))
                    }
                    .padding(8.dp, 12.dp)
            ) {
                Icon(painterResource(it.icon), null, Modifier.padding(horizontal = 12.dp))
                Text(stringResource(it.label))
            }
        }
        item {
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionDetailScreen(
    param: Destination.PermissionDetail, vm: AppFeaturesViewModel, onNavigateUp: () -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    val permissionItem = runtimePermissions.find { it.id == param.permission }!!
    val packagesList by vm.permissionPackagesState.collectAsState()
    var selectedPackage by remember { mutableStateOf<Pair<String, Int>?>(null) }
    var showUserApps by rememberSaveable { mutableStateOf(true) }
    var showSystemApps by rememberSaveable { mutableStateOf(false) }
    var searchMode by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    val displayedPackagesList = packagesList.filter {
        ((showUserApps && it.first.flags and ApplicationInfo.FLAG_SYSTEM == 0) ||
                (showSystemApps && it.first.flags and ApplicationInfo.FLAG_SYSTEM != 0)) &&
                (!searchMode || query.isBlank() || searchInString(query, it.first.name) ||
                        searchInString(query, it.first.label))
    }
    val fm = LocalFocusManager.current
    LaunchedEffect(Unit) {
        vm.getPermissionPackages(param.permission)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                {
                    if (searchMode) {
                        val fr = remember { FocusRequester() }
                        LaunchedEffect(Unit) { fr.requestFocus() }
                        OutlinedTextField(
                            query, { query = it },
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(fr),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions { fm.clearFocus() },
                            placeholder = { Text(stringResource(R.string.search)) },
                            trailingIcon = {
                                IconButton({
                                    query = ""
                                    searchMode = false
                                }) {
                                    Icon(Icons.Outlined.Clear, null)
                                }
                            },
                            textStyle = typography.bodyLarge
                        )
                    } else {
                        Text(stringResource(permissionItem.label))
                    }
                },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    if (!searchMode) {
                        IconButton({ searchMode = true }) {
                            Icon(Icons.Default.Search, null)
                        }
                    }
                    var menu by remember { mutableStateOf(false) }
                    Box {
                        IconButton({ menu = true }) {
                            Icon(painterResource(R.drawable.filter_alt_fill0), null)
                        }
                        DropdownMenu(menu, { menu = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.user_apps)) },
                                { showUserApps = !showUserApps },
                                leadingIcon = { Checkbox(showUserApps, null) }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.system_apps)) },
                                { showSystemApps = !showSystemApps },
                                leadingIcon = { Checkbox(showSystemApps, null) }
                            )
                        }
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(displayedPackagesList, { it.first.name }) { (info, grantState) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { selectedPackage = info.name to grantState }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .animateItem(),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            rememberDrawablePainter(info.icon), null,
                            Modifier
                                .padding(start = 12.dp, end = 18.dp)
                                .size(30.dp)
                        )
                        Column {
                            Text(info.label)
                            Text(info.name, Modifier.alpha(0.8F), style = typography.bodyMedium)
                        }
                    }
                    if (grantState != 0) {
                        Icon(
                            painterResource(
                                if (grantState == 1) R.drawable.check_circle_fill0
                                else R.drawable.cancel_fill0
                            ),
                            null
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
    if (selectedPackage != null) PackagePermissionDialog(
        permissionItem, selectedPackage!!.second, privilege.profile,
        {
            vm.setPackagePermission(selectedPackage!!.first, param.permission, it)
            selectedPackage = null
        }
    ) { selectedPackage = null }
}

@RequiresApi(28)
@Composable
fun ClearAppStorageScreen(
    vm: AppFeaturesViewModel,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit, onNavigateUp: () -> Unit
) {
    var dialog by rememberSaveable { mutableStateOf(false) }
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.clear_app_storage, onNavigateUp) {
        PackageNameTextField(
            packageName, onChoosePackage,
            Modifier.padding(vertical = 8.dp)
        ) { packageName = it }
        Button(
            { dialog = true },
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear))
        }
    }
    if (dialog) ClearAppStorageDialog({
        vm.clearStorage(packageName) { dialog = false }
    }) { dialog = false }
}


@Composable
fun UninstallAppScreen(
    vm: AppFeaturesViewModel, chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onNavigateUp: () -> Unit
) {
    var dialog by rememberSaveable { mutableStateOf(false) }
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.uninstall_app, onNavigateUp) {
        PackageNameTextField(
            packageName, onChoosePackage,
            Modifier.padding(vertical = 8.dp)
        ) { packageName = it }
        Button(
            { dialog = true },
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.uninstall))
        }
    }
    if (dialog) UninstallAppDialog({
        vm.uninstallApp(packageName, it)
    }) {
        packageName = ""
        dialog = false
    }
}

@RequiresApi(28)
@Composable
fun InstallExistingAppScreen(
    vm: AppFeaturesViewModel, chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onNavigateUp: () -> Unit
) {
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    MyScaffold(R.string.install_existing_app, onNavigateUp) {
        PackageNameTextField(
            packageName, onChoosePackage,
            Modifier.padding(vertical = 8.dp)
        ) { packageName = it }
        Button(
            {
                vm.installExistingApp(packageName)
            },
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.install))
        }
        Notes(R.string.info_install_existing_app)
    }
}

@RequiresApi(34)
@Composable
fun CredentialManagerPolicyScreen(
    vm: AppFeaturesViewModel, chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val policy by vm.cmPolicyState.collectAsState()
    val packages by vm.cmPackagesState.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    val inputPackages = parsePackageNames(input)
    LaunchedEffect(Unit) {
        input = chosenPackage.receive()
    }
    MyLazyScaffold(R.string.credential_manager_policy, onNavigateUp) {
        item {
            mapOf(
                -1 to R.string.none,
                1 to R.string.blacklist,
                2 to R.string.whitelist_and_system_app,
                3 to R.string.whitelist
            ).forEach { (key, value) ->
                FullWidthRadioButtonItem(value, policy == key) { vm.setCmPolicy(key) }
            }
            Spacer(Modifier.padding(vertical = 4.dp))
        }
        if (policy != -1) items(packages, { it.name }) {
            ApplicationItem(it) { vm.setCmPackage(listOf(it.name), false) }
        }
        item {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                if (policy != -1) {
                    PackageNameTextField(
                        input, onChoosePackage,
                        Modifier.padding(vertical = 8.dp)
                    ) { input = it }
                    Button(
                        {
                            vm.setCmPackage(inputPackages, true)
                            input = ""
                        },
                        Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.add))
                    }
                }
                Button(
                    vm::applyCmPolicy,
                    Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.apply))
                }
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}

@Composable
fun PermittedAsAndImPackagesScreen(
    title: Int, note: Int, chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    policyState: StateFlow<Boolean>, packagesState: MutableStateFlow<List<AppInfo>>,
    getPolicy: () -> Unit, setPolicy: (Boolean) -> Unit,
    setPackage: (List<String>, Boolean) -> Unit, applyPolicy: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val allowAll by policyState.collectAsState()
    val packages by packagesState.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    val inputPackages = parsePackageNames(input)
    var initialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialized) {
            getPolicy()
            initialized = true
        }
    }
    LaunchedEffect(Unit) {
        input = chosenPackage.receive()
    }
    MyLazyScaffold(title, onNavigateUp) {
        item {
            SwitchItem(R.string.allow_all, allowAll, setPolicy)
        }
        if (!allowAll) items(packages, { it.name }) {
            ApplicationItem(it) { setPackage(listOf(it.name), false) }
        }
        item {
            if (!allowAll) {
                PackageNameTextField(
                    input, onChoosePackage,
                    Modifier.padding(HorizontalPadding, 8.dp)
                ) { input = it }
                Button(
                    {
                        setPackage(inputPackages, true)
                        input = ""
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPadding)
                ) {
                    Text(stringResource(R.string.add))
                }
            }
            Button(
                applyPolicy,
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .padding(horizontal = HorizontalPadding)
            ) {
                Text(stringResource(R.string.apply))
            }
            Spacer(Modifier.height(10.dp))
            Notes(note, HorizontalPadding)
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

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
        PackageNameTextField(
            packageName, onChoosePackage,
            Modifier.padding(bottom = 8.dp)
        ) { packageName = it }
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
        PackageNameTextField(
            packageName, onChoosePackage,
            Modifier.padding(bottom = 8.dp)
        ) { packageName = it }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageFunctionScreen(
    title: Int, packagesState: MutableStateFlow<List<AppInfo>>, onGet: () -> Unit,
    onSet: (List<String>, Boolean) -> Unit, onNavigateUp: () -> Unit,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    navigateToGroups: () -> Unit, appGroups: StateFlow<List<AppGroup>>, notes: Int? = null
) {
    val groups by appGroups.collectAsStateWithLifecycle()
    val packages by packagesState.collectAsStateWithLifecycle()
    var input by rememberSaveable { mutableStateOf("") }
    val inputPackages = parsePackageNames(input)
    var dialog by remember { mutableStateOf(false) }
    var selectedGroup by remember { mutableStateOf<AppGroup?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val res = LocalResources.current
    val coroutine = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        onGet()
        input = chosenPackage.receive()
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
        },
        snackbarHost = {
            SnackbarHost(snackbar)
        }
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(packages, { it.name }) {
                ApplicationItem(it) {
                    onSet(listOf(it.name), false)
                    coroutine.launch {
                        val result = snackbar.showSnackbar(
                            res.getString(R.string.package_removed, it.name),
                            res.getString(R.string.undo),
                            true, SnackbarDuration.Short
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            onSet(listOf(it.name), true)
                        }
                    }
                }
            }
            item {
                PackageNameTextField(
                    input, onChoosePackage,
                    Modifier.padding(HorizontalPadding, 8.dp)
                ) { input = it }
                Button(
                    {
                        onSet(inputPackages, true)
                        input = ""
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPadding)
                        .padding(bottom = 10.dp),
                    packages.none { it.name in inputPackages }
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
                Text(selectedGroup!!.name, style = typography.titleLarge)
                Spacer(Modifier.height(6.dp))
                Button({
                    onSet(selectedGroup!!.apps, true)
                    dialog = false
                }) {
                    Text(stringResource(R.string.add_to_list))
                }
                Button({
                    onSet(selectedGroup!!.apps, false)
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
