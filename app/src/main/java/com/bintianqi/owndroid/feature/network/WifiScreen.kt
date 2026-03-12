package com.bintianqi.owndroid.feature.network

import android.Manifest
import android.app.admin.DevicePolicyManager
import android.os.Build.VERSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.showOperationResultToast
import com.bintianqi.owndroid.utils.writeClipBoard
import com.bintianqi.owndroid.utils.yesOrNo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScreen(
    vm: WifiViewModel, navigate: (Destination) -> Unit, navigateUp: () -> Unit
) {
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    tabIndex = pagerState.currentPage
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wifi)) },
                navigationIcon = { NavIcon(navigateUp) },
                colors = TopAppBarDefaults.topAppBarColors(
                    MaterialTheme.colorScheme.surfaceContainer
                )
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(tabIndex) {
                Tab(
                    tabIndex == 0, { coroutine.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.overview)) }
                )
                Tab(
                    tabIndex == 1, { coroutine.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.saved_networks)) }
                )
                Tab(
                    tabIndex == 2, { coroutine.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text(stringResource(R.string.add_network)) }
                )
            }
            HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
                @Suppress("NewApi")
                when (page) {
                    0 -> WifiOverviewScreen(vm, navigate)
                    1 -> SavedNetworks(vm) {
                        navigate(Destination.UpdateNetwork(it))
                    }
                    2 -> AddNetworkScreenContent(vm) {
                        coroutine.launch { pagerState.animateScrollToPage(1) }
                    }
                }
            }
        }
    }
}

@Composable
private fun WifiOverviewScreen(
    vm: WifiViewModel, navigate: (Destination) -> Unit
) {
    val context = LocalContext.current
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    var macDialog by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(10.dp))
        Row(
            Modifier.fillMaxWidth(),
            Arrangement.Center,
        ) {
            Button({ vm.setWifiEnabled(true) }) {
                Text(stringResource(R.string.enable))
            }
            Spacer(Modifier.width(8.dp))
            Button({ vm.setWifiEnabled(false) }) {
                Text(stringResource(R.string.disable))
            }
        }
        Row(
            Modifier.fillMaxWidth().padding(vertical = 8.dp),
            Arrangement.Center
        ) {
            Button({ vm.disconnect() }) {
                Text(stringResource(R.string.disconnect))
            }
            Spacer(Modifier.width(8.dp))
            Button({ vm.reconnect() }) {
                Text(stringResource(R.string.reconnect))
            }
        }
        if (VERSION.SDK_INT >= 24 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.wifi_mac_address) { macDialog = true }
        }
        if (VERSION.SDK_INT >= 33 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.min_wifi_security_level) {
                navigate(Destination.WifiSecurityLevel)
            }
            FunctionItem(R.string.wifi_ssid_policy) {
                navigate(Destination.WifiSsidPolicy)
            }
        }
    }
    if (macDialog && VERSION.SDK_INT >= 24) {
        val mac by vm.macState.collectAsState()
        LaunchedEffect(Unit) {
            vm.getMac()
        }
        AlertDialog(
            title = { Text(stringResource(R.string.wifi_mac_address)) },
            text = {
                OutlinedTextField(
                    mac ?: stringResource(R.string.none), {},
                    Modifier.fillMaxWidth(),
                    readOnly = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    trailingIcon = {
                        if (mac != null) IconButton({ writeClipBoard(context, mac!!) }) {
                            Icon(painterResource(R.drawable.content_copy_fill0), null)
                        }
                    }
                )
            },
            onDismissRequest = { macDialog = false },
            confirmButton = {
                TextButton({ macDialog = false }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SavedNetworks(
    vm: WifiViewModel, editNetwork: (Int) -> Unit
) {
    var dialog by rememberSaveable { mutableIntStateOf(-1) }
    val list by vm.configuredNetworksState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getConfiguredNetworks()
    }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) vm.getConfiguredNetworks()
    }
    LazyColumn {
        item {
            if (!locationPermission.status.isGranted) Row(
                Modifier
                    .padding(10.dp)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(15))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) },
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.LocationOn, null,
                    Modifier.padding(start = 8.dp, end = 4.dp),
                    MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    stringResource(R.string.request_location_permission_description),
                    Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        itemsIndexed(list) { index, network ->
            Row(
                Modifier.fillMaxWidth().padding(12.dp, 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(network.ssid)
                IconButton({ dialog = index }) {
                    Icon(painterResource(R.drawable.more_horiz_fill0), null)
                }
            }
        }
    }
    if (dialog != -1) AlertDialog(
        text = {
            val network = list[dialog]
            Column {
                Text(stringResource(R.string.network_id) + ": " + network.id.toString())
                Spacer(Modifier.height(4.dp))
                Text("SSID", style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(network.ssid)
                }
                Spacer(Modifier.height(4.dp))
                if (network.bssid.isNotEmpty()) {
                    Text("BSSID", style = MaterialTheme.typography.titleMedium)
                    SelectionContainer {
                        Text(network.bssid)
                    }
                    Spacer(Modifier.height(4.dp))
                }
                Text(stringResource(R.string.status), style = MaterialTheme.typography.titleMedium)
                SelectionContainer {
                    Text(stringResource(network.status.text))
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp), Arrangement.SpaceBetween
                ) {
                    FilledTonalButton({
                        if (network.status == WifiStatus.Disabled) {
                            vm.enableNetwork(network.id)
                        } else {
                            vm.disableNetwork(network.id)
                        }
                        dialog = -1
                    }) {
                        if (network.status == WifiStatus.Disabled) {
                            Text(stringResource(R.string.enable))
                        } else {
                            Text(stringResource(R.string.disable))
                        }
                    }
                    Row {
                        FilledTonalIconButton({
                            editNetwork(dialog)
                            dialog = -1
                        }) {
                            Icon(Icons.Outlined.Edit, stringResource(R.string.edit))
                        }
                        FilledTonalIconButton({
                            vm.removeNetwork(network.id)
                            dialog = -1
                        }) {
                            Icon(Icons.Outlined.Delete, stringResource(R.string.delete))
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton({ dialog = -1 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { dialog = -1 }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNetworkScreen(
    vm: WifiViewModel, onNavigateUp: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.update_network)) },
                navigationIcon = { NavIcon(onNavigateUp) }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier.fillMaxSize().padding(paddingValues)
        ) {
            AddNetworkScreenContent(vm, onNavigateUp)
        }
    }
}

@Composable
fun UnchangedMenuItem(onClick: () -> Unit) {
    DropdownMenuItem({ Text(stringResource(R.string.unchanged)) }, onClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNetworkScreenContent(
    vm: WifiViewModel, onNavigateUp: () -> Unit
) {
    val wifiInfo = vm.selectedWifiInfo
    val updating = wifiInfo != null
    val context = LocalContext.current
    var menu by remember { mutableStateOf(AddNetworkMenu.None) }
    var status by rememberSaveable { mutableStateOf(WifiStatus.Enabled) }
    var ssid by rememberSaveable { mutableStateOf(wifiInfo?.ssid ?: "") }
    var hiddenSsid by rememberSaveable { mutableStateOf<Boolean?>(false) }
    var security by rememberSaveable { mutableStateOf<WifiSecurity?>(WifiSecurity.Open) }
    var password by rememberSaveable { mutableStateOf("") }
    var macRandomization by rememberSaveable {
        mutableStateOf<WifiMacRandomization?>(WifiMacRandomization.None)
    }
    var ipMode by rememberSaveable { mutableStateOf<IpMode?>(IpMode.Dhcp) }
    var ipAddress by rememberSaveable { mutableStateOf("") }
    var gatewayAddress by rememberSaveable { mutableStateOf("") }
    var dnsServers by rememberSaveable { mutableStateOf("") }
    var proxyMode by rememberSaveable { mutableStateOf<ProxyMode?>(ProxyMode.None) }
    var httpProxyHost by rememberSaveable { mutableStateOf("") }
    var httpProxyPort by rememberSaveable { mutableStateOf("") }
    var httpProxyExclList by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (updating) {
            hiddenSsid = null
            security = null
            macRandomization = null
            ipMode = null
            proxyMode = null
            status = wifiInfo.status
            ssid = wifiInfo.ssid
        }
    }
    Column(
        Modifier.verticalScroll(rememberScrollState()).padding(horizontal = HorizontalPadding)
    ) {
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(
            menu == AddNetworkMenu.Status,
            { menu = if (it) AddNetworkMenu.Status else AddNetworkMenu.None },
            Modifier.padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                stringResource(status.text), {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text(stringResource(R.string.status)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == AddNetworkMenu.Status)
                },
            )
            ExposedDropdownMenu(menu == AddNetworkMenu.Status, { menu = AddNetworkMenu.None }) {
                WifiStatus.entries.forEach {
                    DropdownMenuItem(
                        { Text(stringResource(it.text)) },
                        {
                            status = it
                            menu = AddNetworkMenu.None
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            ssid, { ssid = it },
            Modifier.fillMaxWidth().padding(bottom = 8.dp),
            label = { Text("SSID") }
        )
        ExposedDropdownMenuBox(
            menu == AddNetworkMenu.HiddenSSID,
            { menu = if (it) AddNetworkMenu.HiddenSSID else AddNetworkMenu.None },
            Modifier.padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                stringResource(hiddenSsid?.yesOrNo ?: R.string.unchanged), {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text(stringResource(R.string.hidden_ssid)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == AddNetworkMenu.HiddenSSID)
                }
            )
            DropdownMenu(menu == AddNetworkMenu.HiddenSSID, { menu = AddNetworkMenu.None }) {
                if (updating) UnchangedMenuItem {
                    hiddenSsid = null
                    menu = AddNetworkMenu.None
                }
                DropdownMenuItem(
                    { Text(stringResource(R.string.yes)) },
                    {
                        hiddenSsid = true
                        menu = AddNetworkMenu.None
                    }
                )
                DropdownMenuItem(
                    { Text(stringResource(R.string.no)) },
                    {
                        hiddenSsid = false
                        menu = AddNetworkMenu.None
                    }
                )
            }
        }
        ExposedDropdownMenuBox(
            menu == AddNetworkMenu.Security,
            { menu = if (it) AddNetworkMenu.Security else AddNetworkMenu.None },
            Modifier.padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                stringResource(security?.text ?: R.string.unchanged), {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.security)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(menu == AddNetworkMenu.Security) }
            )
            ExposedDropdownMenu(menu == AddNetworkMenu.Security, { menu = AddNetworkMenu.None }) {
                if (updating) UnchangedMenuItem { security = null }
                WifiSecurity.entries.forEach {
                    DropdownMenuItem(
                        { Text(stringResource(it.text)) },
                        {
                            security = it
                            menu = AddNetworkMenu.None
                        }
                    )
                }
            }
        }
        AnimatedVisibility(security == WifiSecurity.Psk) {
            OutlinedTextField(
                password, { password = it },
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                label = { Text(stringResource(R.string.password)) }
            )
        }
        if (VERSION.SDK_INT >= 33) {
            ExposedDropdownMenuBox(
                menu == AddNetworkMenu.MacRandomization,
                { menu = if (it) AddNetworkMenu.MacRandomization else AddNetworkMenu.None },
                Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    stringResource(macRandomization?.text ?: R.string.unchanged), {},
                    Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    readOnly = true,
                    label = { Text(stringResource(R.string.mac_randomization)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(
                            menu == AddNetworkMenu.MacRandomization
                        )
                    }
                )
                ExposedDropdownMenu(
                    menu == AddNetworkMenu.MacRandomization, { menu = AddNetworkMenu.None }
                ) {
                    if (updating) UnchangedMenuItem { macRandomization = null }
                    WifiMacRandomization.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                macRandomization = it
                                menu = AddNetworkMenu.MacRandomization
                            }
                        )
                    }
                }
            }
        }
        if (VERSION.SDK_INT >= 33) {
            ExposedDropdownMenuBox(
                menu == AddNetworkMenu.Ip,
                { menu = if (it) AddNetworkMenu.Ip else AddNetworkMenu.None },
                Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    stringResource(ipMode?.text ?: R.string.unchanged), {},
                    Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    readOnly = true, label = { Text(stringResource(R.string.ip_settings)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(menu == AddNetworkMenu.Ip)
                    }
                )
                ExposedDropdownMenu(menu == AddNetworkMenu.Ip, { menu = AddNetworkMenu.None }) {
                    if (updating) UnchangedMenuItem { ipMode = null }
                    IpMode.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                ipMode = it
                                menu = AddNetworkMenu.None
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(ipMode == IpMode.Static) {
                Column {
                    OutlinedTextField(
                        ipAddress, { ipAddress = it },
                        Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.ip_address)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        gatewayAddress, { gatewayAddress = it },
                        Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.gateway_address)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                    OutlinedTextField(
                        dnsServers, { dnsServers = it },
                        Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.dns_servers)) },
                        minLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                    )
                }
            }
        }
        if (VERSION.SDK_INT >= 26) {
            ExposedDropdownMenuBox(
                menu == AddNetworkMenu.Proxy,
                { menu = if (it) AddNetworkMenu.Proxy else AddNetworkMenu.None },
                Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    stringResource(proxyMode?.text ?: R.string.unchanged), {},
                    Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    readOnly = true,
                    label = { Text(stringResource(R.string.proxy)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(menu == AddNetworkMenu.Proxy)
                    }
                )
                ExposedDropdownMenu(menu == AddNetworkMenu.Proxy, { menu = AddNetworkMenu.None }) {
                    if (updating) UnchangedMenuItem { proxyMode = null }
                    ProxyMode.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                proxyMode = it
                                menu = AddNetworkMenu.None
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(proxyMode == ProxyMode.Http) {
                Column {
                    OutlinedTextField(
                        httpProxyHost, { httpProxyHost = it },
                        Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.host)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    )
                    OutlinedTextField(
                        httpProxyPort, { httpProxyPort = it },
                        Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.port)) },
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Next, keyboardType = KeyboardType.Number
                        )
                    )
                    OutlinedTextField(
                        httpProxyExclList, { httpProxyExclList = it },
                        Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.excluded_hosts)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        minLines = 2
                    )
                }
            }
        }
        Button(
            onClick = {
                val proxyConf = if (proxyMode == ProxyMode.Http) {
                    ProxyConf(
                        httpProxyHost, httpProxyPort.toInt(),
                        httpProxyExclList.lines().filter { it.isNotBlank() }
                    )
                } else null
                val ipConf = if (ipMode == IpMode.Static) {
                    IpConf(ipAddress, gatewayAddress, dnsServers.lines().filter { it.isNotBlank() })
                } else null
                val result = vm.setWifi(
                    WifiInfo(
                        -1, ssid, hiddenSsid, "", macRandomization, status, security, password,
                        ipMode, ipConf, proxyMode, proxyConf
                    )
                )
                context.showOperationResultToast(result)
                if (result) onNavigateUp()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            enabled = (proxyMode != ProxyMode.Http ||
                    (httpProxyPort.toIntOrNull() != null && httpProxyHost.isNotBlank()))
        ) {
            Text(stringResource(if (updating) R.string.update else R.string.add))
        }
        Spacer(Modifier.height(BottomPadding))
    }
}

@RequiresApi(33)
@Composable
fun WifiSecurityLevelScreen(
    vm: WifiViewModel, onNavigateUp: () -> Unit
) {
    val level by vm.minWifiSecurityLevelState.collectAsState()
    MyScaffold(R.string.min_wifi_security_level, onNavigateUp, 0.dp) {
        FullWidthRadioButtonItem(
            R.string.wifi_security_open, level == DevicePolicyManager.WIFI_SECURITY_OPEN
        ) {
            vm.setMinimumWifiSecurityLevel(DevicePolicyManager.WIFI_SECURITY_OPEN)
        }
        FullWidthRadioButtonItem(
            "WEP, WPA(2)-PSK", level == DevicePolicyManager.WIFI_SECURITY_PERSONAL
        ) {
            vm.setMinimumWifiSecurityLevel(DevicePolicyManager.WIFI_SECURITY_PERSONAL)
        }
        FullWidthRadioButtonItem(
            "WPA-EAP", level == DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP
        ) {
            vm.setMinimumWifiSecurityLevel(DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP)
        }
        FullWidthRadioButtonItem(
            "WPA3-192bit", level == DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192
        ) {
            vm.setMinimumWifiSecurityLevel(DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192)
        }
        Spacer(Modifier.height(12.dp))
        Notes(R.string.info_minimum_wifi_security_level, HorizontalPadding)
    }
}

@RequiresApi(33)
@Composable
fun WifiSsidPolicyScreen(
    vm: WifiViewModel, onNavigateUp: () -> Unit
) {
    MyScaffold(R.string.wifi_ssid_policy, onNavigateUp, 0.dp) {
        var type by rememberSaveable { mutableStateOf(SsidPolicyType.None) }
        val list = rememberSaveable { mutableStateListOf<String>() }
        LaunchedEffect(Unit) {
            vm.getSsidPolicy().let {
                type = it.type
                list.addAll(it.list)
            }
        }
        SsidPolicyType.entries.forEach {
            FullWidthRadioButtonItem(it.text, type == it) { type = it }
        }
        AnimatedVisibility(type != SsidPolicyType.None) {
            var inputSsid by remember { mutableStateOf("") }
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                Column(Modifier.animateContentSize()) {
                    for(i in list) {
                        ListItem(i) { list -= i }
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                OutlinedTextField(
                    inputSsid, { inputSsid = it },
                    Modifier.fillMaxWidth(),
                    label = { Text("SSID") },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                list += inputSsid
                                inputSsid = ""
                            },
                            enabled = inputSsid.isNotEmpty()
                        ) {
                            Icon(Icons.Default.Add, stringResource(R.string.add))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
            }
        }
        Button(
            {
                vm.setSsidPolicy(SsidPolicy(type, list))
            },
            Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp),
            type == SsidPolicyType.None || list.isNotEmpty()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}
