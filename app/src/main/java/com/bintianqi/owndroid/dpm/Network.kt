package com.bintianqi.owndroid.dpm

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_OPEN
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_PERSONAL
import android.app.admin.WifiSsidPolicy
import android.app.usage.NetworkStats
import android.net.ConnectivityManager
import android.net.wifi.WifiConfiguration
import android.os.Build.VERSION
import android.provider.Telephony
import android.telephony.TelephonyManager
import android.telephony.data.ApnSetting
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Keep
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.formatFileSize
import com.bintianqi.owndroid.formatTime
import com.bintianqi.owndroid.popToast
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.ErrorDialog
import com.bintianqi.owndroid.ui.ExpandExposedTextFieldIcon
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.writeClipBoard
import com.bintianqi.owndroid.yesOrNo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable object Network

@Composable
fun NetworkScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    MyScaffold(R.string.network, onNavigateUp, 0.dp) {
        if(!privilege.dhizuku) FunctionItem(R.string.wifi, icon = R.drawable.wifi_fill0) { onNavigate(WiFi) }
        if(VERSION.SDK_INT >= 30) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(NetworkOptions) }
        }
        if (VERSION.SDK_INT >= 23 && !privilege.dhizuku)
            FunctionItem(R.string.network_stats, icon = R.drawable.query_stats_fill0) { onNavigate(QueryNetworkStats) }
        if(VERSION.SDK_INT >= 29 && privilege.device) {
            FunctionItem(R.string.private_dns, icon = R.drawable.dns_fill0) { onNavigate(PrivateDns) }
        }
        if(VERSION.SDK_INT >= 24) {
            FunctionItem(R.string.always_on_vpn, icon = R.drawable.vpn_key_fill0) { onNavigate(AlwaysOnVpnPackage) }
        }
        if(privilege.device) {
            FunctionItem(R.string.recommended_global_proxy, icon = R.drawable.vpn_key_fill0) { onNavigate(RecommendedGlobalProxy) }
        }
        if(VERSION.SDK_INT >= 26 && !privilege.dhizuku && (privilege.device || privilege.work)) {
            FunctionItem(R.string.network_logging, icon = R.drawable.description_fill0) { onNavigate(NetworkLogging) }
        }
        /*if(VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.wifi_auth_keypair, icon = R.drawable.key_fill0) { onNavigate(WifiAuthKeypair) }
        }*/
        if (VERSION.SDK_INT >= 33 && (privilege.work || privilege.device)) {
            FunctionItem(R.string.preferential_network_service, icon = R.drawable.globe_fill0) { onNavigate(PreferentialNetworkService) }
        }
        if(VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.override_apn, icon = R.drawable.cell_tower_fill0) { onNavigate(OverrideApn) }
        }
    }
}

@Serializable object NetworkOptions

@Composable
fun NetworkOptionsScreen(
    getLanEnabled: () -> Boolean, setLanEnabled: (Boolean) -> Unit, onNavigateUp: () -> Unit
) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var dialog by remember { mutableIntStateOf(0) }
    var lanEnabled by remember { mutableStateOf(getLanEnabled()) }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 30 && (privilege.device || privilege.org)) {
            SwitchItem(R.string.lockdown_admin_configured_network, icon = R.drawable.wifi_password_fill0,
                state = lanEnabled,
                onCheckedChange = {
                    setLanEnabled(it)
                    lanEnabled = it
                },
                onClickBlank = { dialog = 1 }
            )
        }
    }
    if(dialog != 0) AlertDialog(
        text = { Text(stringResource(R.string.info_lockdown_admin_configured_network)) },
        confirmButton = {
            TextButton(onClick = { dialog = 0 }) { Text(stringResource(R.string.confirm)) }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@Serializable object WiFi

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WifiScreen(
    vm: MyViewModel, onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit,
    editNetwork: (Int) -> Unit
) {
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    tabIndex = pagerState.currentPage
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wifi)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.surfaceContainer)
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            TabRow(tabIndex) {
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
                    0 -> WifiOverviewScreen(vm::setWifiEnabled, vm::disconnectWifi,
                        vm::reconnectWifi, vm::getWifiMac, onNavigate)
                    1 -> SavedNetworks(vm.configuredNetworks, vm::getConfiguredNetworks,
                        vm::enableNetwork, vm::disableNetwork, vm::removeNetwork, editNetwork)
                    2 -> AddNetworkScreen(null, vm::setWifi) {
                        coroutine.launch { pagerState.animateScrollToPage(1) }
                    }
                }
            }
        }
    }
}

@Composable
fun WifiOverviewScreen(
    setWifiEnabled: (Boolean) -> Boolean, disconnect: () -> Boolean, reconnect: () -> Boolean,
    getMac: () -> String?, navigate: (Any) -> Unit
) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var macDialog by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Spacer(Modifier.height(10.dp))
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = { context.showOperationResultToast(setWifiEnabled(true)) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(stringResource(R.string.enable))
            }
            Button(onClick = { context.showOperationResultToast(setWifiEnabled(false)) }) {
                Text(stringResource(R.string.disable))
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Button(
                onClick = { context.showOperationResultToast(disconnect()) },
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text(stringResource(R.string.disconnect))
            }
            Button(onClick = { context.showOperationResultToast(reconnect()) }) {
                Text(stringResource(R.string.reconnect))
            }
        }
        if(VERSION.SDK_INT >= 24 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.wifi_mac_address) { macDialog = true }
        }
        if(VERSION.SDK_INT >= 33 && (privilege.device || privilege.org)) {
            FunctionItem(R.string.min_wifi_security_level) { navigate(WifiSecurityLevel) }
            FunctionItem(R.string.wifi_ssid_policy) { navigate(WifiSsidPolicyScreen) }
        }
    }
    if (macDialog && VERSION.SDK_INT >= 24) {
        AlertDialog(
            title = { Text(stringResource(R.string.wifi_mac_address)) },
            text = {
                val mac = getMac()
                OutlinedTextField(
                    value = mac ?: stringResource(R.string.none), onValueChange = {},
                    readOnly = true, modifier = Modifier.fillMaxWidth(), textStyle = MaterialTheme.typography.bodyLarge,
                    trailingIcon = {
                        if (mac != null) IconButton({ writeClipBoard(context, mac) }) {
                            Icon(painterResource(R.drawable.content_copy_fill0), null)
                        }
                    }
                )
            },
            onDismissRequest = { macDialog = false },
            confirmButton = {
                TextButton({ macDialog = false }) { Text(stringResource(R.string.confirm)) }
            }
        )
    }
}

@Serializable
data class WifiInfo(
    val id: Int, val ssid: String, val hiddenSsid: Boolean?, val bssid: String,
    val macRandomization: WifiMacRandomization?, val status: WifiStatus,
    val security: WifiSecurity?, val password: String, val ipMode: IpMode?, val ipConf: IpConf?,
    val proxyMode: ProxyMode?, val proxyConf: ProxyConf?
)

@Keep
@Suppress("InlinedApi", "DEPRECATION")
enum class WifiMacRandomization(val id: Int, val text: Int) {
    None(WifiConfiguration.RANDOMIZATION_NONE, R.string.none),
    Persistent(WifiConfiguration.RANDOMIZATION_PERSISTENT, R.string.persistent),
    NonPersistent(WifiConfiguration.RANDOMIZATION_NON_PERSISTENT, R.string.non_persistent),
    Auto(WifiConfiguration.RANDOMIZATION_AUTO, R.string.auto)
}

@Keep
@Suppress("InlinedApi", "DEPRECATION")
enum class WifiSecurity(val id: Int, val text: Int) {
    Open(WifiConfiguration.SECURITY_TYPE_OPEN, R.string.wifi_security_open),
    Psk(WifiConfiguration.SECURITY_TYPE_PSK, R.string.wifi_security_psk)
}

@Keep
@Suppress("DEPRECATION")
enum class WifiStatus(val id: Int, val text: Int) {
    Current(WifiConfiguration.Status.CURRENT, R.string.current),
    Enabled(WifiConfiguration.Status.ENABLED, R.string.enabled),
    Disabled(WifiConfiguration.Status.DISABLED, R.string.disabled)
}

@Serializable
data class IpConf(val address: String, val gateway: String, val dns: List<String>)

@Serializable
data class ProxyConf(val host: String, val port: Int, val exclude: List<String>)

@Keep
enum class IpMode(val text: Int) {
    Dhcp(R.string.wifi_mode_dhcp), Static(R.string.static_str)
}
@Keep
enum class ProxyMode(val text: Int) {
    None(R.string.none), Http(R.string.http)
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SavedNetworks(
    configuredNetworks: StateFlow<List<WifiInfo>>, getConfiguredNetworks: () -> Unit,
    enableNetwork: (Int) -> Boolean, disableNetwork: (Int) -> Boolean,
    removeNetwork: (Int) -> Boolean, editNetwork: (Int) -> Unit
) {
    val context = LocalContext.current
    var dialog by remember { mutableIntStateOf(-1) }
    val list by configuredNetworks.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        getConfiguredNetworks()
    }
    val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) getConfiguredNetworks()
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
                    Icons.Outlined.LocationOn, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp))
                Text(
                    text = stringResource(R.string.request_location_permission_description),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        itemsIndexed(list) { index, network ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(12.dp, 4.dp)
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
                    Modifier.fillMaxWidth().padding(top = 8.dp), Arrangement.SpaceBetween
                ) {
                    FilledTonalButton({
                        val result = if (network.status == WifiStatus.Disabled) {
                            enableNetwork(network.id)
                        } else {
                            disableNetwork(network.id)
                        }
                        context.showOperationResultToast(result)
                        dialog = -1
                        getConfiguredNetworks()
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
                            val result = removeNetwork(network.id)
                            context.showOperationResultToast(result)
                            if (result) {
                                dialog = -1
                                getConfiguredNetworks()
                            }
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

@Serializable
data class UpdateNetwork(val index: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UpdateNetworkScreen(info: WifiInfo, setNetwork: (WifiInfo) -> Boolean, onNavigateUp: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.update_network)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                colors = TopAppBarDefaults.topAppBarColors(colorScheme.surfaceContainer)
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            AddNetworkScreen(info, setNetwork, onNavigateUp)
        }
    }
}

@Composable
fun UnchangedMenuItem(onClick: () -> Unit) {
    DropdownMenuItem({ Text(stringResource(R.string.unchanged)) }, onClick)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNetworkScreen(
    wifiInfo: WifiInfo?, setNetwork: (WifiInfo) -> Boolean, onNavigateUp: () -> Unit
) {
    val updating = wifiInfo != null
    val context = LocalContext.current
    val fm = LocalFocusManager.current
    /** 0: None, 1:Status, 2:Security, 3:MAC randomization, 4:Static IP, 5:Proxy, 6:Hidden SSID */
    var menu by remember { mutableIntStateOf(0) }
    var status by remember { mutableStateOf(WifiStatus.Enabled) }
    var ssid by remember { mutableStateOf("") }
    var hiddenSsid by remember { mutableStateOf<Boolean?>(false) }
    var security by remember { mutableStateOf<WifiSecurity?>(WifiSecurity.Open) }
    var password by remember { mutableStateOf("") }
    var macRandomization by remember { mutableStateOf<WifiMacRandomization?>(WifiMacRandomization.None) }
    var ipMode by remember { mutableStateOf<IpMode?>(IpMode.Dhcp) }
    var ipAddress by remember { mutableStateOf("") }
    var gatewayAddress by remember { mutableStateOf("") }
    var dnsServers by remember { mutableStateOf("") }
    var proxyMode by remember { mutableStateOf<ProxyMode?>(ProxyMode.None) }
    var httpProxyHost by remember { mutableStateOf("") }
    var httpProxyPort by remember { mutableStateOf("") }
    var httpProxyExclList by remember { mutableStateOf("") }
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
            menu == 1, { menu = if(it) 1 else 0 }, Modifier.padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                stringResource(status.text), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true,
                label = { Text(stringResource(R.string.status)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == 1) },
            )
            ExposedDropdownMenu(menu == 1, { menu = 0 }) {
                WifiStatus.entries.forEach {
                    DropdownMenuItem(
                        { Text(stringResource(it.text)) },
                        {
                            status = it
                            menu = 0
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            ssid, { ssid = it }, Modifier.fillMaxWidth().padding(bottom = 8.dp),
            label = { Text("SSID") }
        )
        ExposedDropdownMenuBox(
            menu == 6, { menu = if (it) 6 else 0 }, Modifier.padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                stringResource(hiddenSsid?.yesOrNo ?: R.string.unchanged), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.hidden_ssid)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == 1) }
            )
            DropdownMenu(menu == 6, { menu = 0 }) {
                if (updating) DropdownMenuItem(
                    { Text(stringResource(R.string.unchanged)) },
                    {
                        hiddenSsid = null
                        menu = 0
                    }
                )
                DropdownMenuItem(
                    { Text(stringResource(R.string.yes)) },
                    {
                        hiddenSsid = true
                        menu = 0
                    }
                )
                DropdownMenuItem(
                    { Text(stringResource(R.string.no)) },
                    {
                        hiddenSsid = false
                        menu = 0
                    }
                )
            }
        }
        ExposedDropdownMenuBox(
            menu == 2, { menu = if(it) 2 else 0 }, Modifier.padding(bottom = 8.dp)
        ) {
            OutlinedTextField(
                stringResource(security?.text ?: R.string.unchanged), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.security)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == 1) }
            )
            ExposedDropdownMenu(menu == 2, { menu = 0 }) {
                if (updating) UnchangedMenuItem { security = null }
                WifiSecurity.entries.forEach {
                    DropdownMenuItem(
                        { Text(stringResource(it.text)) },
                        {
                            security = it
                            menu = 0
                        }
                    )
                }
            }
        }
        AnimatedVisibility(security == WifiSecurity.Psk) {
            OutlinedTextField(
                password, { password = it }, Modifier.fillMaxWidth().padding(bottom = 8.dp),
                label = { Text(stringResource(R.string.password)) }
            )
        }
        if (VERSION.SDK_INT >= 33) {
            ExposedDropdownMenuBox(
                menu == 3, { menu = if(it) 3 else 0 }, Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    stringResource(macRandomization?.text ?: R.string.unchanged), {},
                    Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    readOnly = true, label = { Text(stringResource(R.string.mac_randomization)) },
                    trailingIcon = { ExpandExposedTextFieldIcon(menu == 3) },
                )
                ExposedDropdownMenu(menu == 3, { menu = 0 }) {
                    if (updating) UnchangedMenuItem { macRandomization = null }
                    WifiMacRandomization.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                macRandomization = it
                                menu = 0
                            }
                        )
                    }
                }
            }
        }
        if (VERSION.SDK_INT >= 33) {
            ExposedDropdownMenuBox(
                menu == 4, { menu = if(it) 4 else 0 }, Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    stringResource(ipMode?.text ?: R.string.unchanged), {},
                    Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    readOnly = true, label = { Text(stringResource(R.string.ip_settings)) },
                    trailingIcon = { ExpandExposedTextFieldIcon(menu == 4) },
                )
                ExposedDropdownMenu(menu == 4, { menu = 0 }) {
                    if (updating) UnchangedMenuItem { ipMode = null }
                    IpMode.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                ipMode = it
                                menu = 0
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(ipMode == IpMode.Static) {
                val gatewayFr = FocusRequester()
                val dnsFr = FocusRequester()
                Column {
                    OutlinedTextField(
                        value = ipAddress, onValueChange = { ipAddress = it },
                        label = { Text(stringResource(R.string.ip_address)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        //keyboardActions = KeyboardActions { gatewayFr.requestFocus() },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = gatewayAddress, onValueChange = { gatewayAddress = it },
                        label = { Text(stringResource(R.string.gateway_address)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions { dnsFr.requestFocus() },
                        modifier = Modifier.focusRequester(gatewayFr).fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = dnsServers, onValueChange = { dnsServers = it },
                        label = { Text(stringResource(R.string.dns_servers)) }, minLines = 2,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { fm.clearFocus() },
                        modifier = Modifier.focusRequester(dnsFr).fillMaxWidth().padding(bottom = 4.dp)
                    )
                }
            }
        }
        if(VERSION.SDK_INT >= 26) {
            ExposedDropdownMenuBox(
                menu == 5, { menu = if(it) 5 else 0 }, Modifier.padding(bottom = 8.dp)
            ) {
                OutlinedTextField(
                    stringResource(proxyMode?.text ?: R.string.unchanged), {},
                    Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                    readOnly = true, label = { Text(stringResource(R.string.proxy)) },
                    trailingIcon = { ExpandExposedTextFieldIcon(menu == 5) },
                )
                ExposedDropdownMenu(menu == 5, { menu = 0 }) {
                    if (updating) UnchangedMenuItem { proxyMode = null }
                    ProxyMode.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                proxyMode = it
                                menu = 0
                            }
                        )
                    }
                }
            }
            AnimatedVisibility(proxyMode == ProxyMode.Http) {
                val portFr = FocusRequester()
                val exclListFr = FocusRequester()
                Column {
                    OutlinedTextField(
                        httpProxyHost, { httpProxyHost = it },
                        Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.host)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions { portFr.requestFocus() }
                    )
                    OutlinedTextField(
                        httpProxyPort, { httpProxyPort = it },
                        Modifier.focusRequester(portFr).fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.port)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Number),
                        keyboardActions = KeyboardActions { exclListFr.requestFocus() }
                    )
                    OutlinedTextField(
                        httpProxyExclList, { httpProxyExclList = it },
                        Modifier.focusRequester(exclListFr).fillMaxWidth().padding(bottom = 4.dp),
                        label = { Text(stringResource(R.string.excluded_hosts)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { fm.clearFocus() },
                        minLines = 2
                    )
                }
            }
        }
        Button(
            onClick = {
                val result = setNetwork(WifiInfo(
                    -1, ssid, hiddenSsid, "", macRandomization, status, security, password, ipMode,
                    IpConf(ipAddress, gatewayAddress, dnsServers.lines().filter { it.isNotBlank() }),
                    proxyMode, ProxyConf(httpProxyHost, httpProxyPort.toInt(), httpProxyExclList.lines().filter { it.isNotBlank() })
                ))
                context.showOperationResultToast(result)
                if (result) onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(if (updating) R.string.update else R.string.add))
        }
        Spacer(Modifier.height(40.dp))
    }
}

@Serializable object WifiSecurityLevel

@RequiresApi(33)
@Composable
fun WifiSecurityLevelScreen(
    getLevel: () -> Int, setLevel: (Int) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var level by remember { mutableIntStateOf(getLevel()) }
    MyScaffold(R.string.min_wifi_security_level, onNavigateUp, 0.dp) {
        FullWidthRadioButtonItem(R.string.wifi_security_open, level == WIFI_SECURITY_OPEN) { level = WIFI_SECURITY_OPEN }
        FullWidthRadioButtonItem("WEP, WPA(2)-PSK", level == WIFI_SECURITY_PERSONAL) { level = WIFI_SECURITY_PERSONAL }
        FullWidthRadioButtonItem("WPA-EAP", level == WIFI_SECURITY_ENTERPRISE_EAP) { level = WIFI_SECURITY_ENTERPRISE_EAP }
        FullWidthRadioButtonItem("WPA3-192bit", level == WIFI_SECURITY_ENTERPRISE_192) { level = WIFI_SECURITY_ENTERPRISE_192 }
        Button(
            onClick = {
                setLevel(level)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_minimum_wifi_security_level, HorizontalPadding)
    }
}

data class SsidPolicy(val type: SsidPolicyType, val list: List<String>)

@Suppress("InlinedApi")
enum class SsidPolicyType(val id: Int, val text: Int) {
    None(-1, R.string.none),
    Whitelist(WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST, R.string.whitelist),
    Blacklist(WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST, R.string.blacklist)
}

@Serializable object WifiSsidPolicyScreen

@RequiresApi(33)
@Composable
fun WifiSsidPolicyScreen(
    getPolicy: () -> SsidPolicy, setPolicy: (SsidPolicy) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.wifi_ssid_policy, onNavigateUp, 0.dp) {
        var type by remember { mutableStateOf(SsidPolicyType.None) }
        val list = remember { mutableStateListOf<String>() }
        LaunchedEffect(Unit) {
            getPolicy().let {
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
                Column(modifier = Modifier.animateContentSize()) {
                    for(i in list) {
                        ListItem(i) { list -= i }
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                OutlinedTextField(
                    inputSsid, { inputSsid = it }, Modifier.fillMaxWidth(),
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
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
                )
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                setPolicy(SsidPolicy(type, list))
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp),
            enabled = type == SsidPolicyType.None || list.isNotEmpty()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

private enum class NetworkStatsMenu {
    None, Type, Target, NetworkType, StartTime, EndTime, Uid, Tag, State
}
enum class NetworkStatsType(val text: Int) { Summary(R.string.summary), Details(R.string.details) }
@Suppress("DEPRECATION")
enum class NetworkType(val type: Int, val text: Int) {
    Mobile(ConnectivityManager.TYPE_MOBILE, R.string.mobile),
    Wifi(ConnectivityManager.TYPE_WIFI, R.string.wifi),
    Bluetooth(ConnectivityManager.TYPE_BLUETOOTH, R.string.bluetooth),
    Ethernet(ConnectivityManager.TYPE_ETHERNET, R.string.ethernet),
    Vpn(ConnectivityManager.TYPE_VPN, R.string.vpn),
}
enum class NetworkStatsTarget(val text: Int, val type: NetworkStatsType, val minApi: Int = 23) {
    Device(R.string.device, NetworkStatsType.Summary),
    User(R.string.user, NetworkStatsType.Summary),
    Uid(R.string.uid, NetworkStatsType.Details),
    UidTag(R.string.uid_tag, NetworkStatsType.Details, 24),
    UidTagState(R.string.uid_tag_state, NetworkStatsType.Details, 28)
}
@Suppress("InlinedApi")
enum class NetworkStatsState(val id: Int, val text: Int) {
    All(NetworkStats.Bucket.STATE_ALL, R.string.all),
    Default(NetworkStats.Bucket.STATE_DEFAULT, R.string.default_str),
    Foreground(NetworkStats.Bucket.STATE_FOREGROUND, R.string.foreground)
}
@RequiresApi(23)
enum class NetworkStatsUID(val uid: Int, val text: Int) {
    All(NetworkStats.Bucket.UID_ALL, R.string.all),
    Removed(NetworkStats.Bucket.UID_REMOVED, R.string.uninstalled),
    Tethering(NetworkStats.Bucket.UID_TETHERING, R.string.tethering)
}

data class QueryNetworkStatsParams(
    val type: NetworkStatsType, val target: NetworkStatsTarget, val networkType: NetworkType,
    val startTime: Long, val endTime: Long, val uid: Int, val tag: Int, val state: NetworkStatsState
)

@Serializable object QueryNetworkStats

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(23)
@Composable
fun NetworkStatsScreen(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit, getUid: (String) -> Int,
    queryStats: (QueryNetworkStatsParams, (String?) -> Unit) -> Unit, onNavigateUp: () -> Unit,
    onNavigateToViewer: () -> Unit
) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    fun getDefaultSummaryTarget(): NetworkStatsTarget {
        return if (privilege.device) NetworkStatsTarget.Device else NetworkStatsTarget.User
    }
    var menu by remember { mutableStateOf(NetworkStatsMenu.None) }
    var type by rememberSaveable { mutableStateOf(NetworkStatsType.Summary) }
    var target by rememberSaveable { mutableStateOf(getDefaultSummaryTarget()) }
    var networkType by rememberSaveable { mutableStateOf(NetworkType.Mobile) }
    var startTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis() - 7*24*60*60*1000) }
    var endTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var uid by rememberSaveable { mutableIntStateOf(NetworkStats.Bucket.UID_ALL) }
    var tag by rememberSaveable { mutableIntStateOf(NetworkStats.Bucket.TAG_NONE) }
    var state by rememberSaveable { mutableStateOf(NetworkStatsState.All) }
    val startTimeIs = remember { MutableInteractionSource() }
    val endTimeIs = remember { MutableInteractionSource() }
    if (startTimeIs.collectIsPressedAsState().value) menu = NetworkStatsMenu.StartTime
    if (endTimeIs.collectIsPressedAsState().value) menu = NetworkStatsMenu.EndTime
    var errorMessage by remember { mutableStateOf<String?>(null) }
    MyScaffold(R.string.network_stats, onNavigateUp) {
        ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.Type,
            { menu = if (it) NetworkStatsMenu.Type else NetworkStatsMenu.None },
            Modifier.padding(top = 8.dp, bottom = 4.dp)
        ) {
            OutlinedTextField(
                stringResource(type.text), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.type)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == NetworkStatsMenu.Type) }
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.Type, { menu = NetworkStatsMenu.None }
            ) {
                NetworkStatsType.entries.forEach {
                    DropdownMenuItem(
                        { Text(stringResource(it.text)) },
                        {
                            type = it
                            target = if (it == NetworkStatsType.Summary) getDefaultSummaryTarget()
                                else NetworkStatsTarget.Uid
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.Target,
            { menu = if(it) NetworkStatsMenu.Target else NetworkStatsMenu.None },
            Modifier.padding(bottom = 4.dp)
        ) {
            OutlinedTextField(
                stringResource(target.text), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.target)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == NetworkStatsMenu.Target) }
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.Target, { menu = NetworkStatsMenu.None }
            ) {
                NetworkStatsTarget.entries.filter {
                    VERSION.SDK_INT >= it.minApi && type == it.type
                }.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.text)) },
                        onClick = {
                            target = it
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.NetworkType,
            { menu = if(it) NetworkStatsMenu.NetworkType else NetworkStatsMenu.None },
            Modifier.padding(bottom = 4.dp)
        ) {
            OutlinedTextField(
                stringResource(networkType.text), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.network_type)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == NetworkStatsMenu.NetworkType) }
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.NetworkType, { menu = NetworkStatsMenu.None }
            ) {
                NetworkType.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.text)) },
                        onClick = {
                            networkType = it
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = startTime.let { if(it == -1L) "" else formatTime(it) }, onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.start_time)) },
            interactionSource = startTimeIs,
            isError = startTime >= endTime,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = formatTime(endTime), onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.end_time)) },
            interactionSource = endTimeIs,
            isError = startTime >= endTime,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        if(target == NetworkStatsTarget.Uid || target == NetworkStatsTarget.UidTag || target == NetworkStatsTarget.UidTagState)
            ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.Uid,
            { menu = if(it) NetworkStatsMenu.Uid else NetworkStatsMenu.None }
        ) {
            var uidText by rememberSaveable { mutableStateOf(context.getString(NetworkStatsUID.All.text)) }
            var readOnly by rememberSaveable { mutableStateOf(true) }
            if (VERSION.SDK_INT >= 24) LaunchedEffect(Unit) {
                val pkg = chosenPackage.receive()
                uid = getUid(pkg)
                uidText = "$uid ($pkg)"
            }
            OutlinedTextField(
                uidText,
                {
                    uidText = it
                    it.toIntOrNull()?.let { num -> uid = num }
                },
                readOnly = readOnly, label = { Text(stringResource(R.string.uid)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == NetworkStatsMenu.Uid) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = !readOnly && uidText.toIntOrNull() == null,
                modifier = Modifier
                    .menuAnchor(if(readOnly) MenuAnchorType.PrimaryNotEditable else MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.Uid, { menu = NetworkStatsMenu.None }
            ) {
                NetworkStatsUID.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.text)) },
                        onClick = {
                            uid = it.uid
                            readOnly = true
                            uidText = context.getString(it.text)
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
                if(VERSION.SDK_INT >= 24) DropdownMenuItem(
                    text = { Text(stringResource(R.string.choose_an_app)) },
                    onClick = {
                        readOnly = true
                        menu = NetworkStatsMenu.None
                        onChoosePackage()
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.input)) },
                    onClick = {
                        readOnly = false
                        uidText = ""
                        menu = NetworkStatsMenu.None
                    }
                )
            }
        }
        if (VERSION.SDK_INT >= 24 && (target == NetworkStatsTarget.UidTag || target == NetworkStatsTarget.UidTagState))
            ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.Tag,
            { menu = if(it) NetworkStatsMenu.Tag else NetworkStatsMenu.None },
            Modifier.padding(bottom = 4.dp)
        ) {
            var tagText by rememberSaveable { mutableStateOf(context.getString(R.string.all)) }
            var readOnly by rememberSaveable { mutableStateOf(true) }
            OutlinedTextField(
                tagText,
                {
                    tagText = it
                    it.toIntOrNull()?.let { num -> tag = num }
                },
                readOnly = readOnly, label = { Text(stringResource(R.string.uid)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == NetworkStatsMenu.Tag) },
                isError = !readOnly && tagText.toIntOrNull() == null,
                modifier = Modifier
                    .menuAnchor(if(readOnly) MenuAnchorType.PrimaryNotEditable else MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.Tag, { menu = NetworkStatsMenu.None }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.all)) },
                    onClick = {
                        tag = NetworkStats.Bucket.TAG_NONE
                        tagText = context.getString(R.string.all)
                        readOnly = true
                        menu = NetworkStatsMenu.None
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.input)) },
                    onClick = {
                        tagText = ""
                        readOnly = false
                        menu = NetworkStatsMenu.None
                    }
                )
            }
        }
        if (VERSION.SDK_INT >= 28 && target == NetworkStatsTarget.UidTagState) ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.State,
            { menu = if(it) NetworkStatsMenu.State else NetworkStatsMenu.None },
            Modifier.padding(bottom = 4.dp)
        ) {
            OutlinedTextField(
                stringResource(state.text), {},
                Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.uid)) },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == NetworkStatsMenu.State) }
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.State, { menu = NetworkStatsMenu.None }
            ) {
                NetworkStatsState.entries.forEach {
                    DropdownMenuItem(
                        { Text(stringResource(it.text)) },
                        {
                            state = it
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        var querying by rememberSaveable { mutableStateOf(false) }
        Button(
            onClick = {
                querying = true
                queryStats(QueryNetworkStatsParams(
                    type, target, networkType, startTime, endTime, uid, tag, state
                )) {
                    querying = false
                    errorMessage = it
                    if (it == null) onNavigateToViewer()
                }
            },
            enabled = !querying,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.query))
        }
        if (menu == NetworkStatsMenu.StartTime || menu == NetworkStatsMenu.EndTime) {
            val datePickerState = rememberDatePickerState(if (menu == NetworkStatsMenu.StartTime) startTime else endTime)
            DatePickerDialog(
                onDismissRequest = { menu = NetworkStatsMenu.None },
                dismissButton = {
                    TextButton(onClick = { menu = NetworkStatsMenu.None }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (menu == NetworkStatsMenu.StartTime) startTime = datePickerState.selectedDateMillis!!
                            else endTime = datePickerState.selectedDateMillis!!
                            menu = NetworkStatsMenu.None
                        },
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            ) {
                DatePicker(datePickerState)
            }
        }
    }
    ErrorDialog(errorMessage) { errorMessage = null }
}

data class NetworkStatsData(
    val rxBytes: Long,
    val rxPackets: Long,
    val txBytes: Long,
    val txPackets: Long,
    val uid: Int,
    val state: Int,
    val startTime: Long,
    val endTime: Long,
    val tag: Int?,
    val roaming: Int?,
    val metered: Int?
)

@Serializable object NetworkStatsViewer

@RequiresApi(23)
@Composable
fun NetworkStatsViewerScreen(
    data: List<NetworkStatsData>, clearData: () -> Unit, onNavigateUp: () -> Unit
) {
    var index by remember { mutableIntStateOf(0) }
    val size = data.size
    val ps = rememberPagerState { size }
    index = ps.currentPage
    val coroutine = rememberCoroutineScope()
    DisposableEffect(Unit) {
        onDispose {
            clearData()
        }
    }
    MySmallTitleScaffold(R.string.network_stats, onNavigateUp, 0.dp) {
        if(size > 1) Row(
            Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    coroutine.launch {
                        ps.animateScrollToPage(index - 1)
                    }
                },
                enabled = index > 0
            ) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, null)
            }
            Text("${index + 1} / $size", modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(
                onClick = {
                    coroutine.launch {
                        ps.animateScrollToPage(index + 1)
                    }
                },
                enabled = index < size - 1
            ) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null)
            }
        }
        HorizontalPager(ps, Modifier.padding(top = 8.dp)) { page ->
            val item = data[index]
            Column(Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)) {
                Text(formatTime(item.startTime) + "\n~\n" + formatTime(item.endTime),
                    Modifier.align(Alignment.CenterHorizontally), textAlign = TextAlign.Center)
                Spacer(Modifier.height(5.dp))
                val txBytes = item.txBytes
                Text(stringResource(R.string.transmitted), style = MaterialTheme.typography.titleMedium)
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                    Text("$txBytes bytes (${formatFileSize(txBytes)})")
                    Text(item.txPackets.toString() + " packets")
                }
                val rxBytes = item.rxBytes
                Text(stringResource(R.string.received), style = MaterialTheme.typography.titleMedium)
                Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                    Text("$rxBytes bytes (${formatFileSize(rxBytes)})")
                    Text(item.rxPackets.toString() + " packets")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = NetworkStatsState.entries.find { it.id == item.state }!!.text
                    Text(stringResource(R.string.state), Modifier.padding(end = 8.dp), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(text))
                }
                if(VERSION.SDK_INT >= 24) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val tag = item.tag
                        Text(stringResource(R.string.tag), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                        Text(if(tag == NetworkStats.Bucket.TAG_NONE) stringResource(R.string.all) else tag.toString())
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val text = when(item.roaming) {
                            NetworkStats.Bucket.ROAMING_ALL -> R.string.all
                            NetworkStats.Bucket.ROAMING_YES -> R.string.yes
                            NetworkStats.Bucket.ROAMING_NO -> R.string.no
                            else -> R.string.unknown
                        }
                        Text(stringResource(R.string.roaming), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                        Text(stringResource(text))
                    }
                }
                if(VERSION.SDK_INT >= 26) Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = when(item.metered) {
                        NetworkStats.Bucket.METERED_ALL -> R.string.all
                        NetworkStats.Bucket.METERED_YES -> R.string.yes
                        NetworkStats.Bucket.METERED_NO -> R.string.no
                        else -> R.string.unknown
                    }
                    Text(stringResource(R.string.metered), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                    Text(stringResource(text))
                }
            }
        }
    }
}

@RequiresApi(29)
enum class PrivateDnsMode(val id: Int, val text: Int) {
    Opportunistic(DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC, R.string.automatic),
    Host(DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME, R.string.enabled)
}

data class PrivateDnsConfiguration(val mode: PrivateDnsMode, val host: String)

@Serializable object PrivateDns

@RequiresApi(29)
@Composable
fun PrivateDnsScreen(
    getPrivateDns: () -> PrivateDnsConfiguration,
    setPrivateDns: (PrivateDnsConfiguration) -> Boolean, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var mode by remember { mutableStateOf(PrivateDnsMode.Opportunistic) }
    var inputHost by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        val conf = getPrivateDns()
        mode = conf.mode
        inputHost = conf.host
    }
    MyScaffold(R.string.private_dns, onNavigateUp, 0.dp) {
        PrivateDnsMode.entries.forEach {
            FullWidthRadioButtonItem(it.text, mode == it) { mode = it }
        }
        if (mode == PrivateDnsMode.Host) OutlinedTextField(
            inputHost, { inputHost=it }, Modifier.fillMaxWidth().padding(HorizontalPadding, 4.dp),
            label = { Text(stringResource(R.string.dns_hostname)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
        )
        Button(
            onClick = {
                focusMgr.clearFocus()
                val result = setPrivateDns(PrivateDnsConfiguration(mode, inputHost))
                context.showOperationResultToast(result)
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Serializable object AlwaysOnVpnPackage

@RequiresApi(24)
@Composable
fun AlwaysOnVpnPackageScreen(
    getPackage: () -> String, getLockdown: () -> Boolean, setConf: (String?, Boolean) -> Int,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var lockdown by rememberSaveable { mutableStateOf(getLockdown()) }
    var pkgName by rememberSaveable { mutableStateOf(getPackage()) }
    LaunchedEffect(Unit) {
        pkgName = chosenPackage.receive()
    }
    MyScaffold(R.string.always_on_vpn, onNavigateUp) {
        PackageNameTextField(pkgName, onChoosePackage,
            Modifier.padding(vertical = 4.dp)) { pkgName = it }
        SwitchItem(R.string.enable_lockdown, state = lockdown, onCheckedChange = { lockdown = it }, padding = false)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                context.popToast(setConf(pkgName, lockdown))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                context.popToast(setConf(null, false))
                pkgName = ""
                lockdown = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_current_config))
        }
        Notes(R.string.info_always_on_vpn)
    }
}

enum class ProxyType(val text: Int) {
    Off(R.string.proxy_type_off), Pac(R.string.proxy_type_pac), Direct(R.string.proxy_type_direct)
}

data class RecommendedProxyConf(
    val type: ProxyType, val url: String, val host: String, val specifyPort: Boolean,
    val port: Int, val exclude: List<String>
)

@Serializable object RecommendedGlobalProxy

@Composable
fun RecommendedGlobalProxyScreen(
    setProxy: (RecommendedProxyConf) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var type by remember { mutableStateOf(ProxyType.Off) }
    var pacUrl by remember { mutableStateOf("") }
    var specifyPort by remember { mutableStateOf(false) }
    var host by remember { mutableStateOf("") }
    var port by remember { mutableStateOf("") }
    var exclList by remember { mutableStateOf("") }
    MyScaffold(R.string.recommended_global_proxy, onNavigateUp, 0.dp) {
        ProxyType.entries.forEach {
            FullWidthRadioButtonItem(it.text, type == it) { type = it }
        }
        AnimatedVisibility(type == ProxyType.Pac) {
            OutlinedTextField(
                pacUrl, { pacUrl = it }, Modifier.fillMaxWidth().padding(HorizontalPadding, 4.dp),
                label = { Text("URL") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
            )
        }
        AnimatedVisibility(type == ProxyType.Direct) {
            OutlinedTextField(
                host, { host = it }, Modifier.fillMaxWidth().padding(HorizontalPadding, 4.dp),
                label = { Text("Host") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
            )
        }
        AnimatedVisibility(type == ProxyType.Pac && VERSION.SDK_INT >= 30) {
            FullWidthCheckBoxItem(R.string.specify_port, specifyPort) { specifyPort = it }
        }
        AnimatedVisibility((specifyPort && VERSION.SDK_INT >= 30) || type == ProxyType.Direct) {
            OutlinedTextField(
                port, { port = it }, Modifier.fillMaxWidth().padding(HorizontalPadding, 4.dp),
                label = { Text(stringResource(R.string.port)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
            )
        }
        AnimatedVisibility(type == ProxyType.Direct) {
            OutlinedTextField(
                exclList, { exclList = it }, Modifier.fillMaxWidth().padding(HorizontalPadding, 4.dp),
                label = { Text(stringResource(R.string.excluded_hosts)) },
                maxLines = 5,
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { focusMgr.clearFocus() }
            )
        }
        Button(
            onClick = {
                setProxy(RecommendedProxyConf(
                    type, pacUrl, host, specifyPort, port.toIntOrNull() ?: 0,
                    exclList.lines().filter { it.isNotBlank() }
                ))
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 4.dp),
            enabled = type == ProxyType.Off ||
                    (type == ProxyType.Pac && pacUrl.isNotBlank() && (!specifyPort || port.toIntOrNull() != null)) ||
                    (type == ProxyType.Direct && port.toIntOrNull() != null)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_recommended_global_proxy, HorizontalPadding)
    }
}

@Serializable object NetworkLogging

@RequiresApi(26)
@Composable
fun NetworkLoggingScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val logFile = context.filesDir.resolve("NetworkLogs.json")
    var fileSize by remember { mutableLongStateOf(0) }
    LaunchedEffect(Unit) { fileSize = logFile.length() }
    val exportNetworkLogsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if(uri != null) context.contentResolver.openOutputStream(uri)?.use { outStream ->
            outStream.write("[".encodeToByteArray())
            logFile.inputStream().use { it.copyTo(outStream) }
            outStream.write("]".encodeToByteArray())
            context.showOperationResultToast(true)
        }
    }
    MyScaffold(R.string.network_logging, onNavigateUp) {
        SwitchItem(
            R.string.enable,
            getState = { Privilege.DPM.isNetworkLoggingEnabled(Privilege.DAR) },
            onCheckedChange = { Privilege.DPM.setNetworkLoggingEnabled(Privilege.DAR, it) },
            padding = false
        )
        Text(stringResource(R.string.log_file_size_is, formatFileSize(fileSize)))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    exportNetworkLogsLauncher.launch("NetworkLogs.json")
                },
                enabled = fileSize > 0,
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.export_logs))
            }
            Button(
                onClick = {
                    logFile.delete()
                    fileSize = logFile.length()
                },
                enabled = fileSize > 0,
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.delete_logs))
            }
        }
        Notes(R.string.info_network_log)
    }
}

@Serializable object WifiAuthKeypair

@RequiresApi(31)
@Composable
fun WifiAuthKeypairScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val focusMgr = LocalFocusManager.current
    var keyPair by remember { mutableStateOf("") }
    MyScaffold(R.string.wifi_auth_keypair, onNavigateUp) {
        OutlinedTextField(
            value = keyPair,
            label = { Text(stringResource(R.string.alias)) },
            onValueChange = { keyPair = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        val isExist = try {
            Privilege.DPM.isKeyPairGrantedToWifiAuth(keyPair)
        } catch(e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
            false
        }
        Text(stringResource(R.string.already_exist)+"：$isExist")
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { context.showOperationResultToast(Privilege.DPM.grantKeyPairToWifiAuth(keyPair)) },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.grant))
            }
            Button(
                onClick = { context.showOperationResultToast(Privilege.DPM.revokeKeyPairFromWifiAuth(keyPair)) },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.revoke))
            }
        }
    }
}

@Serializable object PreferentialNetworkService

@RequiresApi(33)
@Composable
fun PreferentialNetworkServiceScreen(
    getEnabled: () -> Boolean, setEnabled: (Boolean) -> Unit,
    pnsConfigs: StateFlow<List<PreferentialNetworkServiceInfo>>, getConfigs: () -> Unit,
    onNavigateUp: () -> Unit, onNavigate: (AddPreferentialNetworkServiceConfig) -> Unit
) {
    var masterEnabled by remember { mutableStateOf(getEnabled()) }
    val configs by pnsConfigs.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        getConfigs()
    }
    MySmallTitleScaffold(R.string.preferential_network_service, onNavigateUp, 0.dp) {
        SwitchItem(R.string.enabled, state = masterEnabled, onCheckedChange = {
            setEnabled(it)
            masterEnabled = it
        })
        Spacer(Modifier.padding(vertical = 4.dp))
        configs.forEachIndexed { index, config ->
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(config.id.toString())
                IconButton({
                    onNavigate(AddPreferentialNetworkServiceConfig(index))
                }) {
                    Icon(Icons.Default.Edit, stringResource(R.string.edit))
                }
            }
        }
        Row(
            Modifier.fillMaxWidth()
                .padding(top = 4.dp)
                .clickable { onNavigate(AddPreferentialNetworkServiceConfig(-1)) }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, Modifier.padding(horizontal = 8.dp))
            Text(stringResource(R.string.add_config))
        }
    }
}

data class PreferentialNetworkServiceInfo(
    val enabled: Boolean = true,
    val id: Int = -1,
    val allowFallback: Boolean = false,
    val blockNonMatching: Boolean = false,
    val excludedUids: List<Int> = emptyList(),
    val includedUids: List<Int> = emptyList()
)

@Serializable
data class AddPreferentialNetworkServiceConfig(val index: Int)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(33)
@Composable
fun AddPreferentialNetworkServiceConfigScreen(
    origin: PreferentialNetworkServiceInfo,
    setConfig: (PreferentialNetworkServiceInfo, Boolean) -> Unit, onNavigateUp: () -> Unit
) {
    val updateMode = origin.id != -1
    var enabled by remember { mutableStateOf(origin.enabled) }
    var id by remember { mutableIntStateOf(origin.id) }
    var allowFallback by remember { mutableStateOf(origin.allowFallback) }
    var blockNonMatching by remember { mutableStateOf(origin.blockNonMatching) }
    var excludedUids by remember { mutableStateOf(origin.excludedUids.joinToString("\n")) }
    var includedUids by remember { mutableStateOf(origin.includedUids.joinToString("\n")) }
    var dropdown by remember { mutableStateOf(false) }
    MySmallTitleScaffold(R.string.preferential_network_service, onNavigateUp) {
        SwitchItem(title = R.string.enabled, state = enabled, onCheckedChange = { enabled = it }, padding = false)
        ExposedDropdownMenuBox(dropdown, { dropdown = it }) {
            OutlinedTextField(
                if (id == -1) "" else id.toString(), {},
                Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true, label = { Text("id") },
                trailingIcon = { ExpandExposedTextFieldIcon(dropdown) }
            )
            ExposedDropdownMenu(dropdown, { dropdown = false }) {
                for (i in 1..5) {
                    DropdownMenuItem(
                        { Text(i.toString()) },
                        {
                            id = i
                            dropdown = false
                        }
                    )
                }
            }
        }
        SwitchItem(
            title = R.string.allow_fallback_to_default_connection,
            state = allowFallback, onCheckedChange = { allowFallback = it }, padding = false
        )
        if(VERSION.SDK_INT >= 34) SwitchItem(
            title = R.string.block_non_matching_networks,
            state = blockNonMatching, onCheckedChange = { blockNonMatching = it }, padding = false
        )
        val includedUidsLegal = includedUids.lines().filter { it.isNotBlank() }.let { uid ->
            uid.isEmpty() || (uid.all { it.toIntOrNull() != null } && excludedUids.isBlank())
        }
        OutlinedTextField(
            value = includedUids, onValueChange = { includedUids = it }, minLines = 2,
            label = { Text(stringResource(R.string.included_uids)) },
            supportingText = { Text(stringResource(R.string.one_uid_per_line)) },
            isError = !includedUidsLegal,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        val excludedUidsLegal = excludedUids.lines().filter { it.isNotBlank() }.let { uid ->
            uid.isEmpty() || (uid.all { it.toIntOrNull() != null } && includedUids.isBlank())
        }
        OutlinedTextField(
            value = excludedUids, onValueChange = { excludedUids = it }, minLines = 2,
            label = { Text(stringResource(R.string.excluded_uids)) },
            supportingText = { Text(stringResource(R.string.one_uid_per_line)) },
            isError = !excludedUidsLegal,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        Button(
            onClick = {
                setConfig(PreferentialNetworkServiceInfo(
                    enabled, id, allowFallback, blockNonMatching,
                    excludedUids.lines().mapNotNull { it.toIntOrNull() },
                    includedUids.lines().mapNotNull { it.toIntOrNull() }
                ), true)
                onNavigateUp()
            },
            enabled = includedUidsLegal && excludedUidsLegal && id in 1..5,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(if(updateMode) R.string.update else R.string.add))
        }
        if(updateMode) Button(
            onClick = {
                setConfig(origin, false)
                onNavigateUp()
            },
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }
    }
}

@Serializable object OverrideApn

@RequiresApi(28)
@Composable
fun OverrideApnScreen(
    apnConfigs: StateFlow<List<ApnConfig>>, getConfigs: () -> Unit, getEnabled: () -> Boolean,
    setEnabled: (Boolean) -> Unit, onNavigateUp: () -> Unit, onNavigateToAddSetting: (Int) -> Unit
) {
    var enabled by remember { mutableStateOf(getEnabled()) }
    val configs by apnConfigs.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { getConfigs() }
    MyScaffold(R.string.override_apn, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.enable, enabled,
            {
                setEnabled(it)
                enabled = it
            }
        )
        configs.forEach {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row {
                    Text(it.id.toString(), Modifier.padding(end = 8.dp))
                    Column {
                        Text(it.name)
                        Text(it.apn, Modifier.alpha(0.7F), style = MaterialTheme.typography.bodyMedium)
                    }
                }
                IconButton({
                    onNavigateToAddSetting(it.id)
                }) {
                    Icon(Icons.Outlined.Edit, null)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().clickable {
                onNavigateToAddSetting(-1)
            }.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, Modifier.padding(horizontal = 8.dp))
            Text(stringResource(R.string.add_config), style = MaterialTheme.typography.labelLarge)
        }
    }
}

enum class ApnMenu {
    None, ApnType, AuthType, Protocol, RoamingProtocol, NetworkType, MvnoType, OperatorNumeric
}

data class ApnType(val id: Int, val name: String, val requiresApi: Int = 0)
@SuppressLint("InlinedApi")
val apnTypes = listOf(
    ApnType(ApnSetting.TYPE_DEFAULT, "Default"),
    ApnType(ApnSetting.TYPE_MMS, "MMS"),
    ApnType(ApnSetting.TYPE_SUPL, "SUPL"),
    ApnType(ApnSetting.TYPE_DUN, "DUN"),
    ApnType(ApnSetting.TYPE_HIPRI, "HiPri"),
    ApnType(ApnSetting.TYPE_FOTA, "FOTA"),
    ApnType(ApnSetting.TYPE_IMS, "IMS"),
    ApnType(ApnSetting.TYPE_CBS, "CBS"),
    ApnType(ApnSetting.TYPE_IA, "IA"),
    ApnType(ApnSetting.TYPE_EMERGENCY, "Emergency"),
    ApnType(ApnSetting.TYPE_MCX, "MCX", 29),
    ApnType(ApnSetting.TYPE_XCAP, "XCAP", 30),
    ApnType(ApnSetting.TYPE_VSIM, "VSIM", 31),
    ApnType(ApnSetting.TYPE_BIP, "BIP", 31),
    ApnType(ApnSetting.TYPE_ENTERPRISE, "Enterprise", 33),
    ApnType(ApnSetting.TYPE_RCS, "RCS", 35),
    ApnType(ApnSetting.TYPE_OEM_PAID, "OEM paid"),
    ApnType(ApnSetting.TYPE_OEM_PRIVATE, "OEM private")
).filter { VERSION.SDK_INT >= it.requiresApi }

@Suppress("InlinedApi")
enum class ApnProtocol(val id: Int, val text: String, val requiresApi: Int = 28) {
    Ip(ApnSetting.PROTOCOL_IP, "IPv4"),
    Ipv6(ApnSetting.PROTOCOL_IPV6, "IPv6"),
    Ipv4v6(ApnSetting.PROTOCOL_IPV4V6, "IPv4/IPv6"),
    Ppp(ApnSetting.PROTOCOL_PPP, "PPP"),
    NonIp(ApnSetting.PROTOCOL_NON_IP, "Non-IP", 29),
    Unstructured(ApnSetting.PROTOCOL_UNSTRUCTURED, "Unstructured", 29)
}

@Suppress("InlinedApi")
enum class ApnAuthType(val id: Int, val text: String) {
    None(ApnSetting.AUTH_TYPE_NONE, "None"),
    Pap(ApnSetting.AUTH_TYPE_PAP, "PAP"),
    Chap(ApnSetting.AUTH_TYPE_CHAP, "CHAP"),
    PapChap(ApnSetting.AUTH_TYPE_PAP_OR_CHAP, "PAP/CHAP")
}

data class ApnNetworkType(val id: Int, val text: String, val requiresApi: Int = 0)
@Suppress("InlinedApi", "DEPRECATION")
val apnNetworkTypes = listOf(
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_LTE, "LTE"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSPAP, "HSPA+"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSPA, "HSPA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSUPA, "HSUPA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_HSDPA, "HSDPA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_UMTS, "UMTS"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EDGE, "EDGE"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_GPRS, "GPRS"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EHRPD, "CDMA - eHRPD"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_B, "CDMA - EvDo rev. B"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_A, "CDMA - EvDo rev. A"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_EVDO_0, "CDMA - EvDo rev. 0"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_1xRTT, "CDMA - 1xRTT"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_CDMA, "CDMA"),
    ApnNetworkType(TelephonyManager.NETWORK_TYPE_NR, "NR", 29)
).filter { VERSION.SDK_INT >= it.requiresApi }

@Suppress("InlinedApi")
enum class ApnMvnoType(val id: Int, val text: String) {
    SPN(ApnSetting.MVNO_TYPE_SPN, "SPN"),
    IMSI(ApnSetting.MVNO_TYPE_IMSI, "IMSI"),
    GID(ApnSetting.MVNO_TYPE_GID, "GID"),
    ICCID(ApnSetting.MVNO_TYPE_ICCID, "ICCID")
}

data class ApnConfig(
    val enabled: Boolean, val name: String, val apn: String, val proxy: String, val port: Int?,
    val username: String, val password: String, val apnType: Int, val mmsc: String,
    val mmsProxy: String, val mmsPort: Int?, val authType: ApnAuthType, val protocol: ApnProtocol,
    val roamingProtocol: ApnProtocol, val networkType: Int, val profileId: Int?, val carrierId: Int?,
    val mtuV4: Int?, val mtuV6: Int?, val mvno: ApnMvnoType, val operatorNumeric: String,
    val persistent: Boolean, val alwaysOn: Boolean, val id: Int = -1
)

@Serializable data class AddApnSetting(val index: Int)

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun AddApnSettingScreen(
    setApn: (ApnConfig) -> Boolean, deleteApn: (Int) -> Boolean, origin: ApnConfig?,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var menu by remember { mutableStateOf(ApnMenu.None) }
    var enabled by remember { mutableStateOf(true) }
    var entryName by remember { mutableStateOf(origin?.name ?: "") }
    var apnName by remember { mutableStateOf(origin?.apn ?: "") }
    var apnType by remember { mutableIntStateOf(origin?.apnType ?: 0) }
    var profileId by remember { mutableStateOf(origin?.profileId?.toString() ?: "") }
    var carrierId by remember { mutableStateOf(origin?.carrierId?.toString() ?: "") }
    var authType by remember { mutableStateOf(ApnAuthType.None) }
    var user by remember { mutableStateOf(origin?.username ?: "") }
    var password by remember { mutableStateOf(origin?.password ?: "") }
    var proxy by remember { mutableStateOf(origin?.proxy ?: "") }
    var port by remember { mutableStateOf(origin?.port?.toString() ?: "") }
    var mmsProxy by remember { mutableStateOf(origin?.mmsProxy ?: "") }
    var mmsPort by remember { mutableStateOf(origin?.mmsPort?.toString() ?: "") }
    var mmsc by remember { mutableStateOf(origin?.mmsc ?: "") }
    var mtuV4 by remember { mutableStateOf(origin?.mtuV4?.toString() ?: "") }
    var mtuV6 by remember { mutableStateOf(origin?.mtuV6?.toString() ?: "") }
    var mvnoType by remember { mutableStateOf(origin?.mvno ?: ApnMvnoType.SPN) }
    var networkType by remember { mutableIntStateOf(origin?.networkType ?: 0) }
    var operatorNumeric by remember { mutableStateOf(origin?.operatorNumeric ?: "") }
    var protocol by remember { mutableStateOf(origin?.protocol ?: ApnProtocol.Ip) }
    var roamingProtocol by remember { mutableStateOf(origin?.roamingProtocol ?: ApnProtocol.Ip) }
    var persistent by remember { mutableStateOf(origin?.persistent == true) }
    var alwaysOn by remember { mutableStateOf(origin?.alwaysOn == true) }
    var errorMessage: String? by remember { mutableStateOf(null) }
    MySmallTitleScaffold(R.string.apn_setting, onNavigateUp) {
        SwitchItem(R.string.enabled, state = enabled, onCheckedChange = { enabled = it }, padding = false)
        OutlinedTextField(
            entryName, { entryName = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
            label = { Text("Name") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            apnName, { apnName = it }, Modifier.fillMaxWidth(),
            label = { Text("APN") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            proxy, { proxy = it }, Modifier.fillMaxWidth(),
            label = { Text("Proxy") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            port, { port = it }, Modifier.fillMaxWidth(),
            label = { Text("Port") },
            isError = port.isNotEmpty() && port.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            user, { user = it }, Modifier.fillMaxWidth(),
            label = { Text("Username") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            password, { password = it }, Modifier.fillMaxWidth(),
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        Box {
            OutlinedTextField(
                apnTypes.filter { apnType and it.id == it.id }.joinToString { it.name }, {},
                Modifier.fillMaxWidth(),
                readOnly = true, label = { Text("APN type") }
            )
            Box(
                Modifier.matchParentSize().pointerInput(Unit) {
                    detectTapGestures(onTap = { menu = ApnMenu.ApnType })
                }
            )
        }
        OutlinedTextField(
            mmsc, { mmsc = it }, Modifier.fillMaxWidth(),
            label = { Text("MMSC") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            mmsProxy, { mmsProxy = it }, Modifier.fillMaxWidth(),
            label = { Text("MMS proxy") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
        )
        OutlinedTextField(
            mmsPort, { mmsPort = it }, Modifier.fillMaxWidth(),
            label = { Text("MMS port") },
            isError = mmsPort.isNotEmpty() && mmsPort.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )
        ExposedDropdownMenuBox(
            menu == ApnMenu.AuthType, { menu = if (it) ApnMenu.AuthType else ApnMenu.None }
        ) {
            OutlinedTextField(
                authType.text, {}, Modifier.fillMaxWidth(),
                label = { Text("Authentication type") },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == ApnMenu.AuthType) }
            )
            ExposedDropdownMenu(menu == ApnMenu.AuthType, { menu = ApnMenu.None }) {
                ApnAuthType.entries.forEach {
                    DropdownMenuItem(
                        { Text(it.text) },
                        {
                            authType = it
                            menu = ApnMenu.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            menu == ApnMenu.Protocol, { menu = if (it) ApnMenu.Protocol else ApnMenu.None }
        ) {
            OutlinedTextField(
                protocol.text, {}, Modifier.fillMaxWidth(),
                label = { Text("APN protocol") },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == ApnMenu.Protocol) }
            )
            ExposedDropdownMenu(menu == ApnMenu.Protocol, { menu = ApnMenu.None }) {
                ApnProtocol.entries.filter { VERSION.SDK_INT >= it.requiresApi }.forEach {
                    DropdownMenuItem(
                        { Text(it.text) },
                        {
                            protocol = it
                            menu = ApnMenu.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            menu == ApnMenu.RoamingProtocol,
            { menu = if (it) ApnMenu.RoamingProtocol else ApnMenu.None }
        ) {
            OutlinedTextField(
                roamingProtocol.text, {}, Modifier.fillMaxWidth(),
                label = { Text("APN roaming protocol") },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == ApnMenu.RoamingProtocol) }
            )
            ExposedDropdownMenu(menu == ApnMenu.RoamingProtocol, { menu = ApnMenu.None }) {
                ApnProtocol.entries.filter { VERSION.SDK_INT >= it.requiresApi }.forEach {
                    DropdownMenuItem(
                        { Text(it.text) },
                        {
                            roamingProtocol = it
                            menu = ApnMenu.None
                        }
                    )
                }
            }
        }
        Box {
            OutlinedTextField(
                apnNetworkTypes.filter { networkType and it.id == it.id }.joinToString { it.text }, {},
                Modifier.fillMaxWidth(),
                readOnly = true, label = { Text("Network type") }
            )
            Box(
                Modifier.matchParentSize().pointerInput(Unit) {
                    detectTapGestures(onTap = { menu = ApnMenu.NetworkType })
                }
            )
        }
        if (VERSION.SDK_INT >= 33) OutlinedTextField(
            profileId, { profileId = it }, Modifier.fillMaxWidth(),
            label = { Text("Profile id") },
            isError = profileId.isNotEmpty() && profileId.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )
        if (VERSION.SDK_INT >= 29) OutlinedTextField(
            carrierId, { carrierId = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
            label = { Text("Carrier id") },
            isError = carrierId.isNotEmpty() && carrierId.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
        )
        if (VERSION.SDK_INT >= 33) Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
            OutlinedTextField(
                mtuV4, { mtuV4 = it }, Modifier.fillMaxWidth(0.49F),
                label = { Text("MTU (IPv4)") },
                isError = mtuV4.isNotEmpty() && mtuV4.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
            OutlinedTextField(
                mtuV6, { mtuV6 = it }, Modifier.fillMaxWidth(0.96F),
                label = { Text("MTU (IPv6)") },
                isError = mtuV6.isNotEmpty() && mtuV6.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
            )
        }
        ExposedDropdownMenuBox(
            menu == ApnMenu.MvnoType, { menu = if (it) ApnMenu.MvnoType else ApnMenu.None }
        ) {
            OutlinedTextField(
                mvnoType.text, {},
                Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true, label = { Text("MVNO type") },
                trailingIcon = { ExpandExposedTextFieldIcon(menu == ApnMenu.RoamingProtocol) }
            )
            ExposedDropdownMenu(menu == ApnMenu.MvnoType, { menu = ApnMenu.None }) {
                ApnMvnoType.entries.forEach {
                    DropdownMenuItem(
                        { Text(it.text) },
                        {
                            mvnoType = it
                            menu = ApnMenu.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            menu == ApnMenu.OperatorNumeric,
            { menu = if (it) ApnMenu.OperatorNumeric else ApnMenu.None }
        ) {
            OutlinedTextField(
                operatorNumeric, {},
                Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                readOnly = true, label = { Text("Numeric operator ID") }
            )
            ExposedDropdownMenu(menu == ApnMenu.OperatorNumeric, { menu = ApnMenu.None }) {
                listOf(Telephony.Carriers.MCC, Telephony.Carriers.MNC).forEach {
                    DropdownMenuItem({ Text(it) }, {
                        operatorNumeric = it
                        menu = ApnMenu.None
                    })
                }
            }
        }
        if (VERSION.SDK_INT >= 33) Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Persistent")
            Switch(persistent, { persistent = it })
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Always on")
            Switch(alwaysOn, { alwaysOn = it })
        }
        Button(
            {
                val result = setApn(ApnConfig(
                    enabled, entryName, apnName, proxy, port.toIntOrNull(), user, password, apnType,
                    mmsc, mmsProxy, mmsPort.toIntOrNull(), authType, protocol, roamingProtocol,
                    networkType, profileId.toIntOrNull(), carrierId.toIntOrNull(),
                    mtuV4.toIntOrNull(), mtuV6.toIntOrNull(), mvnoType,
                    operatorNumeric, persistent, alwaysOn
                ))
                context.showOperationResultToast(result)
                if (result) onNavigateUp()
            },
            Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(if(origin != null) R.string.update else R.string.add))
        }
        if (origin != null) Button(
            {
                val result = deleteApn(origin.id)
                context.showOperationResultToast(result)
                if (result) onNavigateUp()
            },
            Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
        ) {
            Text(stringResource(R.string.delete))
        }
        if(errorMessage != null) AlertDialog(
            title = { Text(stringResource(R.string.error)) },
            text = { Text(errorMessage ?: "") },
            confirmButton = {
                TextButton({ errorMessage = null }) { Text(stringResource(R.string.confirm)) }
            },
            onDismissRequest = { errorMessage = null }
        )
    }
    if (menu == ApnMenu.ApnType) AlertDialog(
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                apnTypes.forEach { type ->
                    val checked = apnType and type.id == type.id
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                            apnType = if (checked) apnType and type.id.inv()
                            else apnType or type.id
                        }.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked, null)
                        Text(type.name, Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton({ menu = ApnMenu.None }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { menu = ApnMenu.None }
    )
    if (menu == ApnMenu.NetworkType) AlertDialog(
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                apnNetworkTypes.forEach { type ->
                    val checked = type.id and networkType == type.id
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 2.dp).clickable {
                            networkType = if (checked) networkType and type.id.inv()
                                else networkType or type.id
                        }.padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked, null)
                        Text(type.text, Modifier.padding(start = 6.dp), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        },
        confirmButton = {
            TextButton({ menu = ApnMenu.None }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = {
            menu = ApnMenu.None
        }
    )
}
