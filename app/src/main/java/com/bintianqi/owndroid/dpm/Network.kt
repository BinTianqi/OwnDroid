package com.bintianqi.owndroid.dpm

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_OFF
import android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC
import android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME
import android.app.admin.DevicePolicyManager.PRIVATE_DNS_MODE_UNKNOWN
import android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING
import android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING
import android.app.admin.DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_OPEN
import android.app.admin.DevicePolicyManager.WIFI_SECURITY_PERSONAL
import android.app.admin.PreferentialNetworkServiceConfig
import android.app.admin.WifiSsidPolicy
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.net.ConnectivityManager
import android.net.IpConfiguration
import android.net.LinkAddress
import android.net.ProxyInfo
import android.net.StaticIpConfiguration
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiSsid
import android.os.Build.VERSION
import android.os.Bundle
import android.telephony.data.ApnSetting
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.bintianqi.owndroid.ChoosePackageContract
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SharedPrefs
import com.bintianqi.owndroid.formatFileSize
import com.bintianqi.owndroid.humanReadableDate
import com.bintianqi.owndroid.humanReadableDateTime
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.ExpandExposedTextFieldIcon
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.writeClipBoard
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import java.net.InetAddress
import kotlin.reflect.jvm.jvmErasure

@Serializable object Network

@Composable
fun NetworkScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val dhizuku = SharedPrefs(context).dhizuku
    MyScaffold(R.string.network, 0.dp, onNavigateUp) {
        if(!dhizuku) FunctionItem(R.string.wifi, icon = R.drawable.wifi_fill0) { onNavigate(WiFi) }
        if(VERSION.SDK_INT >= 30) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { onNavigate(NetworkOptions) }
        }
        if(VERSION.SDK_INT >= 23 && !dhizuku && (deviceOwner || profileOwner))
            FunctionItem(R.string.network_stats, icon = R.drawable.query_stats_fill0) { onNavigate(QueryNetworkStats) }
        if(VERSION.SDK_INT >= 29 && deviceOwner) {
            FunctionItem(R.string.private_dns, icon = R.drawable.dns_fill0) { onNavigate(PrivateDns) }
        }
        if(VERSION.SDK_INT >= 24) {
            FunctionItem(R.string.always_on_vpn, icon = R.drawable.vpn_key_fill0) { onNavigate(AlwaysOnVpnPackage) }
        }
        if(deviceOwner) {
            FunctionItem(R.string.recommended_global_proxy, icon = R.drawable.vpn_key_fill0) { onNavigate(RecommendedGlobalProxy) }
        }
        if(VERSION.SDK_INT >= 26 && !dhizuku && (deviceOwner || (profileOwner && dpm.isManagedProfile(receiver)))) {
            FunctionItem(R.string.network_logging, icon = R.drawable.description_fill0) { onNavigate(NetworkLogging) }
        }
        if(VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.wifi_auth_keypair, icon = R.drawable.key_fill0) { onNavigate(WifiAuthKeypair) }
        }
        if(VERSION.SDK_INT >= 33) {
            FunctionItem(R.string.preferential_network_service, icon = R.drawable.globe_fill0) { onNavigate(PreferentialNetworkService) }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            FunctionItem(R.string.override_apn, icon = R.drawable.cell_tower_fill0) { onNavigate(OverrideApn) }
        }
    }
}

@Serializable object NetworkOptions

@Composable
fun NetworkOptionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, 0.dp, onNavigateUp) {
        if(VERSION.SDK_INT>=30 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            SwitchItem(R.string.lockdown_admin_configured_network, icon = R.drawable.wifi_password_fill0,
                getState = { dpm.hasLockdownAdminConfiguredNetworks(receiver) }, onCheckedChange = { dpm.setConfiguredNetworksLockdownState(receiver,it) },
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
fun WifiScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit, onNavigateToUpdateNetwork: (Bundle) -> Unit) {
    val context = LocalContext.current
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
        }
    ) { paddingValues ->
        var wifiMacDialog by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            TabRow(tabIndex) {
                Tab(
                    selected = tabIndex == 0, onClick = { tabIndex = 0; coroutine.launch { pagerState.animateScrollToPage(tabIndex) } },
                    text = { Text(stringResource(R.string.overview)) }
                )
                Tab(
                    selected = tabIndex == 1, onClick = { tabIndex = 1; coroutine.launch { pagerState.animateScrollToPage(tabIndex) } },
                    text = { Text(stringResource(R.string.saved_networks)) }
                )
                Tab(
                    selected = tabIndex == 2, onClick = { tabIndex = 2; coroutine.launch { pagerState.animateScrollToPage(tabIndex) } },
                    text = { Text(stringResource(R.string.add_network)) }
                )
            }
            HorizontalPager(state = pagerState, verticalAlignment = Alignment.Top) { page ->
                if(page == 0) {
                    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                    val deviceOwner = context.isDeviceOwner
                    val orgProfileOwner = context.getDPM().isOrgProfile(context.getReceiver())
                    @Suppress("DEPRECATION") Column(
                        modifier = Modifier.fillMaxSize().padding(top = 12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { context.showOperationResultToast(wm.setWifiEnabled(true)) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(stringResource(R.string.enable))
                            }
                            Button(onClick = { context.showOperationResultToast(wm.setWifiEnabled(false)) }) {
                                Text(stringResource(R.string.disable))
                            }
                        }
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                        ) {
                            Button(
                                onClick = { context.showOperationResultToast(wm.disconnect()) },
                                modifier = Modifier.padding(end = 8.dp)
                            ) {
                                Text(stringResource(R.string.disconnect))
                            }
                            Button(onClick = { context.showOperationResultToast(wm.reconnect()) }) {
                                Text(stringResource(R.string.reconnect))
                            }
                        }
                        if(VERSION.SDK_INT >= 24 && (deviceOwner || orgProfileOwner)) {
                            FunctionItem(R.string.wifi_mac_address) { wifiMacDialog = true }
                        }
                        if(VERSION.SDK_INT >= 33 && (deviceOwner || orgProfileOwner)) {
                            FunctionItem(R.string.min_wifi_security_level) { onNavigate(WifiSecurityLevel) }
                            FunctionItem(R.string.wifi_ssid_policy) { onNavigate(WifiSsidPolicyScreen) }
                        }
                    }
                } else if(page == 1) {
                    SavedNetworks(onNavigateToUpdateNetwork)
                } else {
                    AddNetworkScreen(null) {}
                }
            }
        }
        if(wifiMacDialog && VERSION.SDK_INT >= 24) {
            val context = LocalContext.current
            val dpm = context.getDPM()
            val receiver = context.getReceiver()
            AlertDialog(
                onDismissRequest = { wifiMacDialog = false },
                confirmButton = { TextButton(onClick = { wifiMacDialog = false }) { Text(stringResource(R.string.confirm)) } },
                text = {
                    val mac = dpm.getWifiMacAddress(receiver)
                    OutlinedTextField(
                        value = mac ?: stringResource(R.string.none), label = { Text(stringResource(R.string.wifi_mac_address)) },
                        onValueChange = {}, readOnly = true, modifier = Modifier.fillMaxWidth(), textStyle = typography.bodyLarge,
                        trailingIcon = {
                            if(mac != null) IconButton(onClick = { writeClipBoard(context, mac) }) {
                                Icon(painter = painterResource(R.drawable.content_copy_fill0), contentDescription = stringResource(R.string.copy))
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun SavedNetworks(onNavigateToUpdateNetwork: (Bundle) -> Unit) {
    val context = LocalContext.current
    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val configuredNetworks = remember { mutableStateListOf<WifiConfiguration>() }
    var networkDetailsDialog by remember { mutableIntStateOf(-1) } // -1:Hidden, 0+:Index of configuredNetworks
    val coroutine = rememberCoroutineScope()
    fun refresh() {
        configuredNetworks.clear()
        coroutine.launch(Dispatchers.IO) {
            val list = wm.configuredNetworks.distinctBy { it.networkId }
            withContext(Dispatchers.Main) { configuredNetworks.addAll(list) }
        }
    }
    LaunchedEffect(Unit) { refresh() }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 8.dp, end = 8.dp, bottom = 60.dp)
    ) {
        val locationPermission = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        val requestPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
            if(it) refresh()
        }
        if(!locationPermission.status.isGranted) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .clip(RoundedCornerShape(15))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable { requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp))
                Text(
                    text = stringResource(R.string.request_location_permission_description),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
        configuredNetworks.forEachIndexed { index, network ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(start = 8.dp, top = 8.dp)
            ) {
                Text(text = network.SSID.removeSurrounding("\""), style = typography.titleLarge)
                IconButton(onClick = { networkDetailsDialog = index }) {
                    Icon(painter = painterResource(R.drawable.more_horiz_fill0), contentDescription = null)
                }
            }
        }
    }
    if(networkDetailsDialog != -1) AlertDialog(
        text = {
            val network = configuredNetworks[networkDetailsDialog]
            val statusText = when(network.status) {
                WifiConfiguration.Status.CURRENT -> R.string.current
                WifiConfiguration.Status.DISABLED -> R.string.disabled
                WifiConfiguration.Status.ENABLED -> R.string.enabled
                else -> R.string.place_holder
            }
            Column {
                Text(stringResource(R.string.network_id) + ": " + network.networkId.toString())
                SelectionContainer {
                    Text("SSID: " + network.SSID)
                    if(network.BSSID != null) Text("BSSID: " + network.BSSID)
                }
                Text(stringResource(R.string.status) + ": " + stringResource(statusText))
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
                ) {
                    Button(
                        onClick = {
                            context.showOperationResultToast(wm.enableNetwork(network.networkId, false))
                            networkDetailsDialog = -1
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text(stringResource(R.string.enable))
                    }
                    Button(
                        onClick = {
                            context.showOperationResultToast(wm.disableNetwork(network.networkId))
                            networkDetailsDialog = -1
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text(stringResource(R.string.disable))
                    }
                }
                Button(
                    onClick = {
                        networkDetailsDialog = -1
                        onNavigateToUpdateNetwork(bundleOf("wifi_configuration" to network))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Edit, null)
                    Text(stringResource(R.string.edit))
                }
                TextButton(
                    onClick = {
                        context.showOperationResultToast(wm.removeNetwork(network.networkId))
                        networkDetailsDialog = -1
                        refresh()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.Delete, null)
                    Text(stringResource(R.string.remove))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { networkDetailsDialog = -1 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { networkDetailsDialog = -1 }
    )
}

@Serializable
object AddNetwork

@Composable
fun AddNetworkScreen(data: Bundle, onNavigateUp: () -> Unit) {
    MySmallTitleScaffold(R.string.update_network, 0.dp, onNavigateUp) {
        AddNetworkScreen(data.getParcelable("wifi_configuration"), onNavigateUp)
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNetworkScreen(wifiConfig: WifiConfiguration? = null, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val fm = LocalFocusManager.current
    var resultDialog by remember { mutableStateOf(false) }
    var createdNetworkId by remember { mutableIntStateOf(-1) }
    var createNetworkResult by remember { mutableIntStateOf(0) }
    var dropdownMenu by remember { mutableIntStateOf(0) } // 0: None, 1:Status, 2:Security, 3:MAC randomization, 4:Static IP, 5:Proxy
    var status by remember { mutableIntStateOf(WifiConfiguration.Status.ENABLED) }
    var ssid by remember { mutableStateOf("") }
    var hiddenSsid by remember { mutableStateOf(false) }
    var securityType by remember { mutableIntStateOf(WifiConfiguration.SECURITY_TYPE_OPEN) }
    var password by remember { mutableStateOf("") }
    var macRandomizationSetting by remember { mutableIntStateOf(WifiConfiguration.RANDOMIZATION_AUTO) }
    var useStaticIp by remember { mutableStateOf(false) }
    var ipAddress by remember { mutableStateOf("") }
    var gatewayAddress by remember { mutableStateOf("") }
    var dnsServers by remember { mutableStateOf("") }
    var useHttpProxy by remember { mutableStateOf(false) }
    var httpProxyHost by remember { mutableStateOf("") }
    var httpProxyPort by remember { mutableStateOf("") }
    var httpProxyExclList by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if(wifiConfig != null) {
            status = wifiConfig.status
            if(wifiConfig.status == WifiConfiguration.Status.CURRENT) status = WifiConfiguration.Status.ENABLED
            ssid = wifiConfig.SSID.removeSurrounding("\"")
        }
    }
    Column(
        modifier = (if(wifiConfig == null) Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(bottom = 60.dp) else Modifier)
            .padding(start = 8.dp, end = 8.dp, top = 12.dp)
    ) {
        ExposedDropdownMenuBox(dropdownMenu == 1, { dropdownMenu = if(it) 1 else 0 }) {
            val statusText = when(status) {
                WifiConfiguration.Status.DISABLED -> R.string.disabled
                WifiConfiguration.Status.ENABLED -> R.string.enabled
                else -> R.string.place_holder
            }
            OutlinedTextField(
                value = stringResource(statusText), onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.status)) },
                trailingIcon = { ExpandExposedTextFieldIcon(dropdownMenu == 1) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 16.dp)
            )
            ExposedDropdownMenu(dropdownMenu == 1, { dropdownMenu = 0 }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.disabled)) },
                    onClick = {
                        status = WifiConfiguration.Status.DISABLED
                        dropdownMenu = 0
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.enabled)) },
                    onClick = {
                        status = WifiConfiguration.Status.ENABLED
                        dropdownMenu = 0
                    }
                )
            }
        }
        OutlinedTextField(
            value = ssid, onValueChange = { ssid = it }, label = { Text("SSID") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        CheckBoxItem(R.string.hidden_ssid, hiddenSsid) { hiddenSsid = it }
        if(VERSION.SDK_INT >= 30) {
            // TODO: more protocols
            val securityTypeTextMap = mutableMapOf(
                WifiConfiguration.SECURITY_TYPE_OPEN to stringResource(R.string.wifi_security_open),
                WifiConfiguration.SECURITY_TYPE_PSK to "PSK"
            )
            ExposedDropdownMenuBox(dropdownMenu == 2, { dropdownMenu = if(it) 2 else 0 }) {
                OutlinedTextField(
                    value = securityTypeTextMap[securityType] ?: "", onValueChange = {}, label = { Text(stringResource(R.string.security)) },
                    trailingIcon = { ExpandExposedTextFieldIcon(dropdownMenu == 1) }, readOnly = true,
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(vertical = 4.dp)
                )
                ExposedDropdownMenu(dropdownMenu == 2, { dropdownMenu = 0 }) {
                    securityTypeTextMap.forEach {
                        DropdownMenuItem(text = { Text(it.value) }, onClick = { securityType = it.key; dropdownMenu = 0 })
                    }
                }
            }
            AnimatedVisibility(securityType == WifiConfiguration.SECURITY_TYPE_PSK) {
                OutlinedTextField(
                    value = password, onValueChange = { password = it }, label = { Text(stringResource(R.string.password)) },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }
        }
        if(VERSION.SDK_INT >= 33) {
            val macRandomizationSettingTextMap = mapOf(
                WifiConfiguration.RANDOMIZATION_NONE to R.string.none,
                WifiConfiguration.RANDOMIZATION_PERSISTENT to R.string.persistent,
                WifiConfiguration.RANDOMIZATION_NON_PERSISTENT to R.string.non_persistent,
                WifiConfiguration.RANDOMIZATION_AUTO to R.string.auto
            )
            ExposedDropdownMenuBox(dropdownMenu == 3, { dropdownMenu = if(it) 3 else 0 }) {
                OutlinedTextField(
                    value = stringResource(macRandomizationSettingTextMap[macRandomizationSetting] ?: R.string.place_holder),
                    onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.mac_randomization)) },
                    trailingIcon = { ExpandExposedTextFieldIcon(dropdownMenu == 3) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 8.dp)
                )
                ExposedDropdownMenu(dropdownMenu == 3, { dropdownMenu = 0 }) {
                    macRandomizationSettingTextMap.forEach {
                        DropdownMenuItem(
                            text = { Text(stringResource(it.value)) },
                            onClick = {
                                macRandomizationSetting = it.key
                                dropdownMenu = 0
                            }
                        )
                    }
                }
            }
        }
        if(VERSION.SDK_INT >= 33) {
            ExposedDropdownMenuBox(dropdownMenu == 4, { dropdownMenu = if(it) 4 else 0 }) {
                OutlinedTextField(
                    value = if(useStaticIp) stringResource(R.string.static_str) else "DHCP",
                    onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.ip_settings)) },
                    trailingIcon = { ExpandExposedTextFieldIcon(dropdownMenu == 4) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 4.dp)
                )
                ExposedDropdownMenu(dropdownMenu == 4, { dropdownMenu = 0 }) {
                    DropdownMenuItem(text = { Text("DHCP") }, onClick = { useStaticIp = false; dropdownMenu = 0 })
                    DropdownMenuItem(text = { Text(stringResource(R.string.static_str)) }, onClick = { useStaticIp = true; dropdownMenu = 0 })
                }
            }
            AnimatedVisibility(visible = useStaticIp, modifier = Modifier.padding(bottom = 8.dp)) {
                val gatewayFr = FocusRequester()
                val dnsFr = FocusRequester()
                Column {
                    OutlinedTextField(
                        value = ipAddress, onValueChange = { ipAddress = it },
                        placeholder = { Text("192.168.1.2/24") }, label = { Text(stringResource(R.string.ip_address)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions { gatewayFr.requestFocus() },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = gatewayAddress, onValueChange = { gatewayAddress = it },
                        placeholder = { Text("192.168.1.1") }, label = { Text(stringResource(R.string.gateway_address)) },
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
            ExposedDropdownMenuBox(dropdownMenu == 5, { dropdownMenu = if(it) 5 else 0 }) {
                OutlinedTextField(
                    value = if(useHttpProxy) "HTTP" else stringResource(R.string.none),
                    onValueChange = {}, readOnly = true,
                    label = { Text(stringResource(R.string.proxy)) },
                    trailingIcon = { ExpandExposedTextFieldIcon(dropdownMenu == 5) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 4.dp)
                )
                ExposedDropdownMenu(dropdownMenu == 5, { dropdownMenu = 0 }) {
                    DropdownMenuItem(text = { Text(stringResource(R.string.none)) }, onClick = { useHttpProxy = false; dropdownMenu = 0 })
                    DropdownMenuItem(text = { Text("HTTP") }, onClick = { useHttpProxy = true; dropdownMenu = 0 })
                }
            }
            AnimatedVisibility(visible = useHttpProxy, modifier = Modifier.padding(bottom = 8.dp)) {
                val portFr = FocusRequester()
                val exclListFr = FocusRequester()
                Column {
                    OutlinedTextField(
                        value = httpProxyHost, onValueChange = { httpProxyHost = it }, label = { Text(stringResource(R.string.host)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions { portFr.requestFocus() },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = httpProxyPort, onValueChange = { httpProxyPort = it }, label = { Text(stringResource(R.string.port)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next, keyboardType = KeyboardType.Number),
                        keyboardActions = KeyboardActions { exclListFr.requestFocus() },
                        modifier = Modifier.focusRequester(portFr).fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = httpProxyExclList, onValueChange = { httpProxyExclList = it }, label = { Text(stringResource(R.string.excluded_hosts)) },
                        minLines = 2, placeholder = { Text("example.com\n*.example.com") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions { fm.clearFocus() },
                        modifier = Modifier.focusRequester(exclListFr).fillMaxWidth().padding(bottom = 4.dp)
                    )
                }
            }
        }
        Button(
            onClick = {
                val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
                try {
                    val config = WifiConfiguration()
                    config.status = status
                    config.SSID = '"' + ssid + '"'
                    config.hiddenSSID = hiddenSsid
                    if(VERSION.SDK_INT >= 30) config.setSecurityParams(securityType)
                    if(securityType == WifiConfiguration.SECURITY_TYPE_PSK) config.preSharedKey = '"' + password + '"'
                    if(VERSION.SDK_INT >= 33) config.macRandomizationSetting = macRandomizationSetting
                    if(VERSION.SDK_INT >= 33 && useStaticIp) {
                        val ipConf = IpConfiguration.Builder()
                        val staticIpConf = StaticIpConfiguration.Builder()
                        val la: LinkAddress
                        val con = LinkAddress::class.constructors.find { it.parameters.size == 1 && it.parameters[0].type.jvmErasure == String::class }
                        la = con!!.call(ipAddress)
                        staticIpConf.setIpAddress(la)
                        staticIpConf.setGateway(InetAddress.getByName(gatewayAddress))
                        staticIpConf.setDnsServers(dnsServers.lines().map { InetAddress.getByName(it) })
                        ipConf.setStaticIpConfiguration(staticIpConf.build())
                        config.setIpConfiguration(ipConf.build())
                    }
                    if(VERSION.SDK_INT >= 26 && useHttpProxy) {
                        config.httpProxy = ProxyInfo.buildDirectProxy(httpProxyHost, httpProxyPort.toInt(), httpProxyExclList.lines())
                    }
                    if(wifiConfig != null) {
                        config.networkId = wifiConfig.networkId
                        createdNetworkId = wm.updateNetwork(config)
                    } else {
                        if(VERSION.SDK_INT >= 31) {
                            val result = wm.addNetworkPrivileged(config)
                            createdNetworkId = result.networkId
                            createNetworkResult = result.statusCode
                        } else {
                            createdNetworkId = wm.addNetwork(config)
                        }
                    }
                    resultDialog = true
                } catch(e: Exception) {
                    e.printStackTrace()
                    AlertDialog.Builder(context)
                        .setTitle(R.string.error)
                        .setPositiveButton(R.string.confirm) { dialog, _ -> dialog.cancel() }
                        .setMessage(e.message ?: "")
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(if(wifiConfig != null) R.string.update else R.string.add))
        }
        if(resultDialog) AlertDialog(
            text = {
                val statusText = when(createNetworkResult) {
                    WifiManager.AddNetworkResult.STATUS_SUCCESS -> R.string.success
                    //WifiManager.AddNetworkResult.STATUS_ADD_WIFI_CONFIG_FAILURE -> R.string.failed
                    WifiManager.AddNetworkResult.STATUS_INVALID_CONFIGURATION -> R.string.add_network_result_invalid_configuration
                    else -> R.string.failed
                }
                Text(stringResource(statusText) + "\n" + stringResource(R.string.network_id) + ": " + createdNetworkId)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        resultDialog = false
                        if(createdNetworkId != -1) onNavigateUp()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { resultDialog = false }
        )
    }
}

@Serializable object WifiSecurityLevel

@RequiresApi(33)
@Composable
fun WifiSecurityLevelScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var selectedWifiSecLevel by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { selectedWifiSecLevel = dpm.minimumRequiredWifiSecurityLevel }
    MySmallTitleScaffold(R.string.min_wifi_security_level, 0.dp, onNavigateUp) {
        FullWidthRadioButtonItem(R.string.wifi_security_open, selectedWifiSecLevel == WIFI_SECURITY_OPEN) { selectedWifiSecLevel = WIFI_SECURITY_OPEN }
        FullWidthRadioButtonItem("WEP, WPA(2)-PSK", selectedWifiSecLevel == WIFI_SECURITY_PERSONAL) { selectedWifiSecLevel = WIFI_SECURITY_PERSONAL }
        FullWidthRadioButtonItem("WPA-EAP", selectedWifiSecLevel == WIFI_SECURITY_ENTERPRISE_EAP) { selectedWifiSecLevel = WIFI_SECURITY_ENTERPRISE_EAP }
        FullWidthRadioButtonItem("WPA3-192bit", selectedWifiSecLevel == WIFI_SECURITY_ENTERPRISE_192) { selectedWifiSecLevel = WIFI_SECURITY_ENTERPRISE_192 }
        Button(
            onClick = {
                dpm.minimumRequiredWifiSecurityLevel = selectedWifiSecLevel
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = 8.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_minimum_wifi_security_level, 8.dp)
    }
}

@Serializable object WifiSsidPolicyScreen

@RequiresApi(33)
@Composable
fun WifiSsidPolicyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.wifi_ssid_policy, 0.dp, onNavigateUp) {
        var selectedPolicyType by remember { mutableIntStateOf(-1) }
        val ssidList = remember { mutableStateListOf<WifiSsid>() }
        fun refreshPolicy() {
            val policy = dpm.wifiSsidPolicy
            ssidList.clear()
            selectedPolicyType = policy?.policyType ?: -1
            ssidList.addAll(policy?.ssids ?: mutableSetOf())
        }
        LaunchedEffect(Unit) { refreshPolicy() }
        FullWidthRadioButtonItem(R.string.none, selectedPolicyType == -1) { selectedPolicyType = -1 }
        FullWidthRadioButtonItem(R.string.whitelist, selectedPolicyType == WIFI_SSID_POLICY_TYPE_ALLOWLIST) {
            selectedPolicyType = WIFI_SSID_POLICY_TYPE_ALLOWLIST
        }
        FullWidthRadioButtonItem(R.string.blacklist, selectedPolicyType == WIFI_SSID_POLICY_TYPE_DENYLIST) {
            selectedPolicyType = WIFI_SSID_POLICY_TYPE_DENYLIST
        }
        AnimatedVisibility(selectedPolicyType != -1) {
            var inputSsid by remember { mutableStateOf("") }
            Column(Modifier.padding(horizontal = 8.dp)) {
                Text(stringResource(R.string.ssid_list_is))
                if(ssidList.isEmpty()) Text(stringResource(R.string.none))
                Column(modifier = Modifier.animateContentSize()) {
                    for(i in ssidList) {
                        ListItem(i.bytes.decodeToString()) { ssidList -= i }
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                OutlinedTextField(
                    value = inputSsid,
                    label = { Text("SSID") },
                    onValueChange = { inputSsid = it },
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                ssidList += WifiSsid.fromBytes(inputSsid.encodeToByteArray())
                                inputSsid = ""
                            },
                            enabled = inputSsid != ""
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = stringResource(R.string.add))
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                dpm.wifiSsidPolicy = if(selectedPolicyType == -1 || ssidList.isEmpty()) {
                    null
                } else {
                    WifiSsidPolicy(selectedPolicyType, ssidList.toSet())
                }
                refreshPolicy()
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

private enum class NetworkStatsActiveTextField { None, Type, Target, NetworkType, SubscriberId,  StartTime, EndTime, Uid, Tag, State }
@Suppress("DEPRECATION")
private enum class NetworkType(val type: Int, @StringRes val strRes: Int) {
    Mobile(ConnectivityManager.TYPE_MOBILE, R.string.mobile),
    Wifi(ConnectivityManager.TYPE_WIFI, R.string.wifi),
    Bluetooth(ConnectivityManager.TYPE_BLUETOOTH, R.string.bluetooth),
    Ethernet(ConnectivityManager.TYPE_ETHERNET, R.string.ethernet),
    Vpn(ConnectivityManager.TYPE_VPN, R.string.vpn),
}
private enum class NetworkStatsTarget(@StringRes val strRes: Int, val minApi: Int) {
    Device(R.string.device, 23), User(R.string.user, 23),
    Uid(R.string.uid, 23), UidTag(R.string.uid_tag, 24), UidTagState(R.string.uid_tag_state, 28)
}
@RequiresApi(23)
private enum class NetworkStatsUID(val uid: Int, @StringRes val strRes: Int) {
    All(NetworkStats.Bucket.UID_ALL, R.string.all),
    Removed(NetworkStats.Bucket.UID_REMOVED, R.string.uninstalled),
    Tethering(NetworkStats.Bucket.UID_TETHERING, R.string.tethering)
}
@RequiresApi(23)
fun NetworkStats.toBucketList(): List<NetworkStats.Bucket> {
    val list = mutableListOf<NetworkStats.Bucket>()
    while(hasNextBucket()) {
        val bucket = NetworkStats.Bucket()
        if(getNextBucket(bucket)) list += bucket
    }
    close()
    return list
}

@Serializable object QueryNetworkStats

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(23)
@Composable
fun NetworkStatsScreen(onNavigateUp: () -> Unit, onNavigateToViewer: (NetworkStatsViewer) -> Unit) {
    val context = LocalContext.current
    val deviceOwner = context.isDeviceOwner
    val fm = LocalFocusManager.current
    val nsm = context.getSystemService(NetworkStatsManager::class.java)
    val coroutine = rememberCoroutineScope()
    var activeTextField by remember { mutableStateOf(NetworkStatsActiveTextField.None) } //0:None, 1:Network type, 2:Start time, 3:End time
    var queryType by rememberSaveable { mutableIntStateOf(1) } //1:Summary, 2:Details
    var target by rememberSaveable { mutableStateOf(NetworkStatsTarget.Device) }
    var networkType by rememberSaveable { mutableStateOf(NetworkType.Mobile) }
    var subscriberId by rememberSaveable { mutableStateOf<String?>(null) }
    var startTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis() - 7*24*60*60*1000) }
    var endTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var uid by rememberSaveable { mutableIntStateOf(NetworkStats.Bucket.UID_ALL) }
    var tag by rememberSaveable { mutableIntStateOf(NetworkStats.Bucket.TAG_NONE) }
    var state by rememberSaveable { mutableIntStateOf(NetworkStats.Bucket.STATE_ALL) }
    val startTimeTextFieldInteractionSource = remember { MutableInteractionSource() }
    val endTimeTextFieldInteractionSource = remember { MutableInteractionSource() }
    if(startTimeTextFieldInteractionSource.collectIsPressedAsState().value) activeTextField = NetworkStatsActiveTextField.StartTime
    if(endTimeTextFieldInteractionSource.collectIsPressedAsState().value) activeTextField = NetworkStatsActiveTextField.EndTime
    MyScaffold(R.string.network_stats, 8.dp, onNavigateUp) {
        ExposedDropdownMenuBox(
            activeTextField == NetworkStatsActiveTextField.Type,
            { activeTextField = if(it) NetworkStatsActiveTextField.Type else NetworkStatsActiveTextField.Type }
        ) {
            val typeTextMap = mapOf(
                1 to R.string.summary,
                2 to R.string.details
            )
            OutlinedTextField(
                value = stringResource(typeTextMap[queryType]!!), onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.type)) },
                trailingIcon = { ExpandExposedTextFieldIcon(activeTextField == NetworkStatsActiveTextField.Type) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                activeTextField == NetworkStatsActiveTextField.Type, { activeTextField = NetworkStatsActiveTextField.None }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.summary)) },
                    onClick = {
                        queryType = 1
                        target = NetworkStatsTarget.Device
                        activeTextField = NetworkStatsActiveTextField.None
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.details)) },
                    onClick = {
                        queryType = 2
                        target = NetworkStatsTarget.Uid
                        activeTextField = NetworkStatsActiveTextField.None
                    }
                )
            }
        }
        ExposedDropdownMenuBox(
            activeTextField == NetworkStatsActiveTextField.Target,
            { activeTextField = if(it) NetworkStatsActiveTextField.Target else NetworkStatsActiveTextField.None }
        ) {
            OutlinedTextField(
                value = stringResource(target.strRes), onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.target)) },
                trailingIcon = { ExpandExposedTextFieldIcon(activeTextField == NetworkStatsActiveTextField.Target) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                activeTextField == NetworkStatsActiveTextField.Target, { activeTextField = NetworkStatsActiveTextField.None }
            ) {
                NetworkStatsTarget.entries.forEach {
                    if(
                        VERSION.SDK_INT >= it.minApi &&
                        (deviceOwner || it != NetworkStatsTarget.Device) &&
                        ((queryType == 1 && (it == NetworkStatsTarget.Device || it == NetworkStatsTarget.User)) ||
                        (queryType == 2 && (it == NetworkStatsTarget.Uid || it == NetworkStatsTarget.UidTag || it == NetworkStatsTarget.UidTagState)))
                    ) DropdownMenuItem(
                        text = { Text(stringResource(it.strRes)) },
                        onClick = {
                            target = it
                            activeTextField = NetworkStatsActiveTextField.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            activeTextField == NetworkStatsActiveTextField.NetworkType,
            { activeTextField = if(it) NetworkStatsActiveTextField.NetworkType else NetworkStatsActiveTextField.None }
        ) {
            OutlinedTextField(
                value = stringResource(networkType.strRes), onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.network_type)) },
                trailingIcon = { ExpandExposedTextFieldIcon(activeTextField == NetworkStatsActiveTextField.NetworkType) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                activeTextField == NetworkStatsActiveTextField.NetworkType, { activeTextField = NetworkStatsActiveTextField.None }
            ) {
                NetworkType.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.strRes)) },
                        onClick = {
                            networkType = it
                            activeTextField = NetworkStatsActiveTextField.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            activeTextField == NetworkStatsActiveTextField.SubscriberId,
            { activeTextField = if(it) NetworkStatsActiveTextField.SubscriberId else NetworkStatsActiveTextField.None }
        ) {
            var readOnly by rememberSaveable { mutableStateOf(true) }
            OutlinedTextField(
                value = subscriberId ?: "null", onValueChange = { if(!readOnly) subscriberId = it }, readOnly = readOnly,
                label = { Text(stringResource(R.string.subscriber_id)) },
                isError = !readOnly && subscriberId.isNullOrBlank(),
                trailingIcon = { ExpandExposedTextFieldIcon(activeTextField == NetworkStatsActiveTextField.SubscriberId) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { fm.clearFocus() },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                activeTextField == NetworkStatsActiveTextField.SubscriberId, { activeTextField = NetworkStatsActiveTextField.None }
            ) {
                DropdownMenuItem(
                    text = { Text("null") },
                    onClick = {
                        readOnly = true
                        subscriberId = null
                        activeTextField = NetworkStatsActiveTextField.None
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.input)) },
                    onClick = {
                        readOnly = false
                        subscriberId = ""
                        activeTextField = NetworkStatsActiveTextField.None
                    }
                )
            }
        }
        OutlinedTextField(
            value = startTime.let { if(it == -1L) "" else it.humanReadableDate }, onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.start_time)) },
            interactionSource = startTimeTextFieldInteractionSource,
            isError = startTime >= endTime,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = endTime.humanReadableDate, onValueChange = {}, readOnly = true,
            label = { Text(stringResource(R.string.end_time)) },
            interactionSource = endTimeTextFieldInteractionSource,
            isError = startTime >= endTime,
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
        )
        if(target == NetworkStatsTarget.Uid || target == NetworkStatsTarget.UidTag || target == NetworkStatsTarget.UidTagState)
            ExposedDropdownMenuBox(
            activeTextField == NetworkStatsActiveTextField.Uid,
            { activeTextField = if(it) NetworkStatsActiveTextField.Uid else NetworkStatsActiveTextField.None }
        ) {
            var uidText by rememberSaveable { mutableStateOf(context.getString(NetworkStatsUID.All.strRes)) }
            var readOnly by rememberSaveable { mutableStateOf(true) }
            if(!readOnly && uidText.toIntOrNull() != null) uid = uidText.toInt()
            val choosePackage = rememberLauncherForActivityResult(ChoosePackageContract()) {
                it ?: return@rememberLauncherForActivityResult
                if(VERSION.SDK_INT >= 24 && readOnly) {
                    try {
                        uid = context.packageManager.getPackageUid(it, 0)
                        uidText = "$it ($uid)"
                    } catch(_: NameNotFoundException) {
                        context.showOperationResultToast(false)
                    }
                }
            }
            OutlinedTextField(
                value = uidText, onValueChange = { if(!readOnly) uidText = it }, readOnly = readOnly,
                label = { Text(stringResource(R.string.uid)) },
                trailingIcon = { ExpandExposedTextFieldIcon(activeTextField == NetworkStatsActiveTextField.Uid) },
                isError = !readOnly && uidText.toIntOrNull() == null,
                modifier = Modifier
                    .menuAnchor(if(readOnly) MenuAnchorType.PrimaryNotEditable else MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                activeTextField == NetworkStatsActiveTextField.Uid, { activeTextField = NetworkStatsActiveTextField.None }
            ) {
                NetworkStatsUID.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.strRes)) },
                        onClick = {
                            uid = it.uid
                            readOnly = true
                            uidText = context.getString(it.strRes)
                            activeTextField = NetworkStatsActiveTextField.None
                        }
                    )
                }
                if(VERSION.SDK_INT >= 24) DropdownMenuItem(
                    text = { Text(stringResource(R.string.choose_an_app)) },
                    onClick = {
                        readOnly = true
                        activeTextField = NetworkStatsActiveTextField.None
                        choosePackage.launch(null)
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.input)) },
                    onClick = {
                        readOnly = false
                        uidText = ""
                        activeTextField = NetworkStatsActiveTextField.None
                    }
                )
            }
        }
        if(VERSION.SDK_INT >= 24 && (target == NetworkStatsTarget.UidTag || target == NetworkStatsTarget.UidTagState))
            ExposedDropdownMenuBox(
            activeTextField == NetworkStatsActiveTextField.Tag,
            { activeTextField == if(it) NetworkStatsActiveTextField.Tag else NetworkStatsActiveTextField.None }
        ) {
            var tagText by rememberSaveable { mutableStateOf(context.getString(R.string.all)) }
            var readOnly by rememberSaveable { mutableStateOf(true) }
            if(!readOnly && tagText.toIntOrNull() != null) tag = tagText.toInt()
            OutlinedTextField(
                value = tagText, onValueChange = { if(!readOnly) tagText = it }, readOnly = readOnly,
                label = { Text(stringResource(R.string.uid)) },
                trailingIcon = { ExpandExposedTextFieldIcon(activeTextField == NetworkStatsActiveTextField.Tag) },
                isError = !readOnly && tagText.toIntOrNull() == null,
                modifier = Modifier
                    .menuAnchor(if(readOnly) MenuAnchorType.PrimaryNotEditable else MenuAnchorType.PrimaryEditable)
                    .fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                activeTextField == NetworkStatsActiveTextField.Tag, { activeTextField = NetworkStatsActiveTextField.None }
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.all)) },
                    onClick = {
                        tag = NetworkStats.Bucket.TAG_NONE
                        tagText = context.getString(R.string.all)
                        readOnly = true
                        activeTextField = NetworkStatsActiveTextField.None
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.input)) },
                    onClick = {
                        tagText = ""
                        readOnly = false
                        activeTextField = NetworkStatsActiveTextField.None
                    }
                )
            }
        }
        if(VERSION.SDK_INT >= 28 && target == NetworkStatsTarget.UidTagState)
            ExposedDropdownMenuBox(
            activeTextField == NetworkStatsActiveTextField.State,
            { activeTextField = if(it) NetworkStatsActiveTextField.State else NetworkStatsActiveTextField.None }
        ) {
            val textMap = mapOf(
                NetworkStats.Bucket.STATE_ALL to R.string.all,
                NetworkStats.Bucket.STATE_DEFAULT to R.string.default_str,
                NetworkStats.Bucket.STATE_FOREGROUND to R.string.foreground
            )
            OutlinedTextField(
                value = stringResource(textMap[state]!!), onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.uid)) },
                trailingIcon = { ExpandExposedTextFieldIcon(activeTextField == NetworkStatsActiveTextField.State) },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 4.dp)
            )
            ExposedDropdownMenu(
                activeTextField == NetworkStatsActiveTextField.State, { activeTextField = NetworkStatsActiveTextField.None }
            ) {
                textMap.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.value)) },
                        onClick = {
                            state = it.key
                            activeTextField = NetworkStatsActiveTextField.None
                        }
                    )
                }
            }
        }
        var querying by rememberSaveable { mutableStateOf(false) }
        Button(
            onClick = {
                querying = true
                coroutine.launch {
                    val buckets = try {
                        @Suppress("NewApi") if(queryType == 1) {
                            if(target == NetworkStatsTarget.Device)
                                listOf(nsm.querySummaryForDevice(networkType.type, subscriberId, startTime, endTime))
                            else listOf(nsm.querySummaryForUser(networkType.type, subscriberId, startTime, endTime))
                        } else {
                            if(target == NetworkStatsTarget.Uid)
                                nsm.queryDetailsForUid(networkType.type, subscriberId, startTime, endTime, uid).toBucketList()
                            else if(target == NetworkStatsTarget.UidTag)
                                nsm.queryDetailsForUidTag(networkType.type, subscriberId, startTime, endTime, uid, tag).toBucketList()
                            else nsm.queryDetailsForUidTagState(networkType.type, subscriberId, startTime, endTime, uid, tag, state).toBucketList()
                        }
                    } catch(e: Exception) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            querying = false
                            AlertDialog.Builder(context)
                                .setTitle(R.string.error)
                                .setMessage(e.message ?: "")
                                .setPositiveButton(R.string.confirm) { dialog, _ -> dialog.dismiss() }
                                .show()
                        }
                        return@launch
                    }
                    if(buckets.isEmpty()) {
                        withContext(Dispatchers.Main) {
                            querying = false
                            context.showOperationResultToast(false)
                        }
                    } else {
                        val stats = buckets.map {
                            NetworkStatsViewer.Data(
                                it.rxBytes, it.rxPackets, it.txBytes, it.txPackets,
                                it.uid, it.state, it.startTimeStamp, it.endTimeStamp,
                                if(VERSION.SDK_INT >= 24) it.tag else null,
                                if(VERSION.SDK_INT >= 24) it.roaming else null,
                                if(VERSION.SDK_INT >= 26) it.metered else null
                            )
                        }
                        withContext(Dispatchers.Main) {
                            querying = false
                            onNavigateToViewer(NetworkStatsViewer(stats))
                        }
                    }
                }
            },
            enabled = !querying,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(stringResource(R.string.query))
        }
        if(activeTextField == NetworkStatsActiveTextField.StartTime || activeTextField == NetworkStatsActiveTextField.EndTime) {
            val datePickerState = rememberDatePickerState(if(activeTextField == NetworkStatsActiveTextField.StartTime) startTime else endTime)
            DatePickerDialog(
                onDismissRequest = { activeTextField = NetworkStatsActiveTextField.None },
                dismissButton = {
                    TextButton(onClick = { activeTextField = NetworkStatsActiveTextField.None }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if(activeTextField == NetworkStatsActiveTextField.StartTime) startTime = datePickerState.selectedDateMillis!!
                            else endTime = datePickerState.selectedDateMillis!!
                            activeTextField = NetworkStatsActiveTextField.None
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
}

@Serializable
data class NetworkStatsViewer(
    val stats: List<Data>
) {
    @Serializable
    data class Data(
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
}

@RequiresApi(23)
@Composable
fun NetworkStatsViewerScreen(nsv: NetworkStatsViewer, onNavigateUp: () -> Unit) {
    var index by remember { mutableIntStateOf(0) }
    val size = nsv.stats.size
    MySmallTitleScaffold(R.string.network_stats, 8.dp, onNavigateUp) {
        if(size > 1) Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        ) {
            IconButton(
                onClick = { index -= 1 },
                enabled = index > 0
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft, contentDescription = null)
            }
            Text("${index + 1} / $size", modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(
                onClick = { index += 1 },
                enabled = index < size - 1
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
        val data = nsv.stats[index]
        Text(
            data.startTime.humanReadableDateTime + "  ~  " + data.endTime.humanReadableDateTime,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        )
        val txBytes = data.txBytes
        Text(stringResource(R.string.transmitted), style = typography.titleLarge)
        Column(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
            Text("$txBytes bytes (${formatFileSize(txBytes)})")
            Text(data.txPackets.toString() + " packets")
        }
        val rxBytes = data.rxBytes
        Text(stringResource(R.string.received), style = typography.titleLarge)
        Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
            Text("$rxBytes bytes (${formatFileSize(rxBytes)})")
            Text(data.rxPackets.toString() + " packets")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val text = when(data.state) {
                NetworkStats.Bucket.STATE_ALL -> R.string.all
                NetworkStats.Bucket.STATE_DEFAULT -> R.string.default_str
                NetworkStats.Bucket.STATE_FOREGROUND -> R.string.foreground
                else -> R.string.unknown
            }
            Text(stringResource(R.string.state), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(text))
        }
        if(VERSION.SDK_INT >= 24) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val tag = data.tag
                Text(stringResource(R.string.tag), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                Text(if(tag == NetworkStats.Bucket.TAG_NONE) stringResource(R.string.all) else tag.toString())
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val text = when(data.roaming) {
                    NetworkStats.Bucket.ROAMING_ALL -> R.string.all
                    NetworkStats.Bucket.ROAMING_YES -> R.string.yes
                    NetworkStats.Bucket.ROAMING_NO -> R.string.no
                    else -> R.string.unknown
                }
                Text(stringResource(R.string.roaming), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(text))
            }
        }
        if(VERSION.SDK_INT >= 26) Row(verticalAlignment = Alignment.CenterVertically) {
            val text = when(data.metered) {
                NetworkStats.Bucket.METERED_ALL -> R.string.all
                NetworkStats.Bucket.METERED_YES -> R.string.yes
                NetworkStats.Bucket.METERED_NO -> R.string.no
                else -> R.string.unknown
            }
            Text(stringResource(R.string.metered), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(text))
        }
    }
}

@Serializable object PrivateDns

@RequiresApi(29)
@Composable
fun PrivateDnsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.private_dns, 8.dp, onNavigateUp) {
        val dnsStatus = mapOf(
            PRIVATE_DNS_MODE_UNKNOWN to stringResource(R.string.unknown),
            PRIVATE_DNS_MODE_OFF to stringResource(R.string.disabled),
            PRIVATE_DNS_MODE_OPPORTUNISTIC to stringResource(R.string.auto),
            PRIVATE_DNS_MODE_PROVIDER_HOSTNAME to stringResource(R.string.dns_provide_hostname)
        )
        val operationResult = mapOf(
            PRIVATE_DNS_SET_NO_ERROR to stringResource(R.string.success),
            PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING to stringResource(R.string.host_not_serving_dns_tls),
            PRIVATE_DNS_SET_ERROR_FAILURE_SETTING to stringResource(R.string.failed)
        )
        var status by remember { mutableStateOf(dnsStatus[dpm.getGlobalPrivateDnsMode(receiver)]) }
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.current_state, status?:stringResource(R.string.unknown)))
        AnimatedVisibility(visible = dpm.getGlobalPrivateDnsMode(receiver)!=PRIVATE_DNS_MODE_OPPORTUNISTIC) {
            Spacer(Modifier.padding(vertical = 5.dp))
            Button(
                onClick = {
                    val result = dpm.setGlobalPrivateDnsModeOpportunistic(receiver)
                    Toast.makeText(context, operationResult[result], Toast.LENGTH_SHORT).show()
                    status = dnsStatus[dpm.getGlobalPrivateDnsMode(receiver)]
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.set_to_opportunistic))
            }
        }
        Notes(R.string.info_private_dns_mode_oppertunistic)
        Spacer(Modifier.padding(vertical = 10.dp))
        var inputHost by remember { mutableStateOf(dpm.getGlobalPrivateDnsHost(receiver) ?: "") }
        OutlinedTextField(
            value = inputHost,
            onValueChange = { inputHost=it },
            label = { Text(stringResource(R.string.dns_hostname)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                val result: Int
                try {
                    result = dpm.setGlobalPrivateDnsModeSpecifiedHost(receiver,inputHost)
                    Toast.makeText(context, operationResult[result], Toast.LENGTH_SHORT).show()
                } catch(e: IllegalArgumentException) {
                    e.printStackTrace()
                    Toast.makeText(context, R.string.invalid_hostname, Toast.LENGTH_SHORT).show()
                } catch(e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(context, R.string.security_exception, Toast.LENGTH_SHORT).show()
                } finally {
                    status = dnsStatus[dpm.getGlobalPrivateDnsMode(receiver)]
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.set_dns_host))
        }
        Notes(R.string.info_set_private_dns_host)
    }
}

@Serializable object AlwaysOnVpnPackage

@RequiresApi(24)
@Composable
fun AlwaysOnVpnPackageScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var lockdown by rememberSaveable { mutableStateOf(false) }
    var pkgName by rememberSaveable { mutableStateOf("") }
    val focusMgr = LocalFocusManager.current
    val refresh = { pkgName = dpm.getAlwaysOnVpnPackage(receiver) ?: "" }
    LaunchedEffect(Unit) { refresh() }
    val choosePackage = rememberLauncherForActivityResult(ChoosePackageContract()) { result ->
        result?.let { pkgName = it }
    }
    val setAlwaysOnVpn: (String?, Boolean)->Boolean = { vpnPkg: String?, lockdownEnabled: Boolean ->
        try {
            dpm.setAlwaysOnVpnPackage(receiver, vpnPkg, lockdownEnabled)
            context.showOperationResultToast(true)
            true
        } catch(e: UnsupportedOperationException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.unsupported, Toast.LENGTH_SHORT).show()
            false
        } catch(e: NameNotFoundException) {
            e.printStackTrace()
            Toast.makeText(context, R.string.not_installed, Toast.LENGTH_SHORT).show()
            false
        }
    }
    MyScaffold(R.string.always_on_vpn, 8.dp, onNavigateUp) {
        OutlinedTextField(
            value = pkgName,
            onValueChange = { pkgName = it },
            label = { Text(stringResource(R.string.package_name)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            trailingIcon = {
                Icon(painter = painterResource(R.drawable.list_fill0), contentDescription = null,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { choosePackage.launch(null) }
                        .padding(3.dp))
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
        )
        SwitchItem(R.string.enable_lockdown, state = lockdown, onCheckedChange = { lockdown = it }, padding = false)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { if(setAlwaysOnVpn(pkgName, lockdown)) refresh() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { if(setAlwaysOnVpn(null, false)) refresh() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear_current_config))
        }
        Notes(R.string.info_always_on_vpn)
    }
}

@Serializable object RecommendedGlobalProxy

@Composable
fun RecommendedGlobalProxyScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var proxyType by remember { mutableIntStateOf(0) }
    var proxyUri by remember { mutableStateOf("") }
    var specifyPort by remember { mutableStateOf(false) }
    var proxyPort by remember { mutableStateOf("") }
    var exclList by remember { mutableStateOf("") }
    MyScaffold(R.string.recommended_global_proxy, 8.dp, onNavigateUp) {
        RadioButtonItem(R.string.proxy_type_off, proxyType == 0) { proxyType = 0 }
        RadioButtonItem(R.string.proxy_type_pac, proxyType == 1) { proxyType = 1 }
        RadioButtonItem(R.string.proxy_type_direct, proxyType == 2) { proxyType = 2 }
        AnimatedVisibility(proxyType != 0) {
            OutlinedTextField(
                value = proxyUri,
                onValueChange = { proxyUri = it },
                label = { Text(if(proxyType == 1) "URL" else "Host") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(proxyType == 1 && VERSION.SDK_INT >= 30) {
            Box(modifier = Modifier.padding(top = 2.dp)) {
                CheckBoxItem(R.string.specify_port, specifyPort) { specifyPort = it }
            }
        }
        AnimatedVisibility((proxyType == 1 && specifyPort && VERSION.SDK_INT >= 30) || proxyType == 2) {
            OutlinedTextField(
                value = proxyPort,
                onValueChange = { proxyPort = it },
                label = { Text(stringResource(R.string.port)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )
        }
        AnimatedVisibility(proxyType == 2) {
            OutlinedTextField(
                value = exclList,
                onValueChange = { exclList = it },
                label = { Text(stringResource(R.string.excluded_hosts)) },
                maxLines = 5,
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )
        }
        Button(
            onClick = {
                if(proxyType == 0) {
                    dpm.setRecommendedGlobalProxy(receiver, null)
                    context.showOperationResultToast(true)
                    return@Button
                }
                if(proxyUri == "") {
                    Toast.makeText(context, R.string.invalid_config, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val uri = Uri.parse(proxyUri)
                val port: Int
                try {
                    port = proxyPort.toInt()
                } catch(e: NumberFormatException) {
                    e.printStackTrace()
                    Toast.makeText(context, R.string.invalid_config, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                val proxyInfo =
                    if(proxyType == 1) {
                        if(specifyPort && VERSION.SDK_INT >= 30) {
                            ProxyInfo.buildPacProxy(uri, port)
                        } else {
                            ProxyInfo.buildPacProxy(uri)
                        }
                    } else {
                        ProxyInfo.buildDirectProxy(proxyUri, port, exclList.lines())
                    }
                if(VERSION.SDK_INT >= 30 && !proxyInfo.isValid) {
                    Toast.makeText(context, R.string.invalid_config, Toast.LENGTH_SHORT).show()
                    return@Button
                }
                dpm.setRecommendedGlobalProxy(receiver, proxyInfo)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_recommended_global_proxy)
    }
}

@Serializable object NetworkLogging

@RequiresApi(26)
@Composable
fun NetworkLoggingScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
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
    MyScaffold(R.string.network_logging, 8.dp, onNavigateUp) {
        SwitchItem(
            R.string.enable,
            getState = { dpm.isNetworkLoggingEnabled(receiver) },
            onCheckedChange = { dpm.setNetworkLoggingEnabled(receiver,it) },
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
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    var keyPair by remember { mutableStateOf("") }
    MyScaffold(R.string.wifi_auth_keypair, 8.dp, onNavigateUp) {
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
            dpm.isKeyPairGrantedToWifiAuth(keyPair)
        } catch(e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
            false
        }
        Text(stringResource(R.string.already_exist)+"：$isExist")
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = { context.showOperationResultToast(dpm.grantKeyPairToWifiAuth(keyPair)) },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.grant))
            }
            Button(
                onClick = { context.showOperationResultToast(dpm.revokeKeyPairFromWifiAuth(keyPair)) },
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
fun PreferentialNetworkServiceScreen(onNavigateUp: () -> Unit, onNavigate: (AddPreferentialNetworkServiceConfig) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var masterEnabled by remember { mutableStateOf(false) }
    val configs = remember { mutableStateListOf<PreferentialNetworkServiceConfig>() }
    fun refresh() {
        masterEnabled = dpm.isPreferentialNetworkServiceEnabled
        configs.clear()
        configs.addAll(dpm.preferentialNetworkServiceConfigs)
    }
    LaunchedEffect(Unit) { refresh() }
    MySmallTitleScaffold(R.string.preferential_network_service, 0.dp, onNavigateUp) {
        SwitchItem(R.string.enabled, state = masterEnabled, onCheckedChange = {
            dpm.isPreferentialNetworkServiceEnabled = it
            refresh()
        })
        Spacer(Modifier.padding(vertical = 4.dp))
        configs.forEachIndexed { index, config ->
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text(index.toString())
                }
                IconButton({
                    onNavigate(AddPreferentialNetworkServiceConfig(
                        enabled = config.isEnabled,
                        id = config.networkId,
                        allowFallback = config.isFallbackToDefaultConnectionAllowed,
                        blockNonMatching = if(VERSION.SDK_INT >= 34) config.shouldBlockNonMatchingNetworks() else false,
                        excludedUids = config.excludedUids.toList(),
                        includedUids = config.includedUids.toList(),
                        index = index
                    ))
                }) {
                    Icon(Icons.Default.Edit, stringResource(R.string.edit))
                }
            }
        }
        Row(
            Modifier.fillMaxWidth()
                .padding(top = 4.dp)
                .clickable { onNavigate(AddPreferentialNetworkServiceConfig()) }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, Modifier.padding(horizontal = 8.dp))
            Text(stringResource(R.string.add_config))
        }
    }
}

@Serializable data class AddPreferentialNetworkServiceConfig(
    val enabled: Boolean = true,
    val id: Int = -1,
    val allowFallback: Boolean = false,
    val blockNonMatching: Boolean = false,
    val excludedUids: List<Int> = emptyList(),
    val includedUids: List<Int> = emptyList(),
    val index: Int = -1
)

@RequiresApi(33)
@Composable
fun AddPreferentialNetworkServiceConfigScreen(route: AddPreferentialNetworkServiceConfig,onNavigateUp: () -> Unit) {
    val updateMode = route.index != -1
    val context = LocalContext.current
    val dpm = context.getDPM()
    var enabled by remember { mutableStateOf(route.enabled) }
    var id by remember { mutableIntStateOf(route.id) }
    var allowFallback by remember { mutableStateOf(route.allowFallback) }
    var blockNonMatching by remember { mutableStateOf(route.blockNonMatching) }
    var excludedUids by remember { mutableStateOf(route.excludedUids.joinToString("\n")) }
    var includedUids by remember { mutableStateOf(route.includedUids.joinToString("\n")) }
    MySmallTitleScaffold(R.string.preferential_network_service, 8.dp, onNavigateUp) {
        SwitchItem(title = R.string.enabled, state = enabled, onCheckedChange = { enabled = it }, padding = false)
        AnimatedVisibility(enabled) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("ID", Modifier.padding(end = 8.dp), style = typography.titleLarge)
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    for(i in 1..5) {
                        SegmentedButton(id == i, { id = i }, SegmentedButtonDefaults.itemShape(i - 1, 5)) {
                            Text(i.toString())
                        }
                    }
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
        val includedUidsLegal = includedUids.lines().filter { it.isNotBlank() }.let {
            it.isEmpty() || (it.all { it.toIntOrNull() != null } && excludedUids.isBlank())
        }
        OutlinedTextField(
            value = includedUids, onValueChange = { includedUids = it }, minLines = 2,
            label = { Text(stringResource(R.string.included_uids)) },
            supportingText = { Text(stringResource(R.string.one_uid_per_line)) },
            isError = !includedUidsLegal,
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        val excludedUidsLegal = excludedUids.lines().filter { it.isNotBlank() }.let {
            it.isEmpty() || (it.all { it.toIntOrNull() != null } && includedUids.isBlank())
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
                try {
                    val config = PreferentialNetworkServiceConfig.Builder().apply {
                        setEnabled(enabled)
                        if(enabled) setNetworkId(id.toInt())
                        setFallbackToDefaultConnectionAllowed(allowFallback)
                        setExcludedUids(excludedUids.lines().filter { it.isNotBlank() }.map { it.toInt() }.toIntArray())
                        setIncludedUids(includedUids.lines().filter { it.isNotBlank() }.map { it.toInt() }.toIntArray())
                        if(VERSION.SDK_INT >= 34) setShouldBlockNonMatchingNetworks(blockNonMatching)
                    }.build()
                    val configs = dpm.preferentialNetworkServiceConfigs
                    if(updateMode) configs[route.index] = config
                    else configs += config
                    dpm.preferentialNetworkServiceConfigs = configs
                    onNavigateUp()
                } catch(e: Exception) {
                    context.showOperationResultToast(false)
                    e.printStackTrace()
                }
            },
            enabled = includedUidsLegal && excludedUidsLegal,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(if(updateMode) R.string.update else R.string.add))
        }
        if(updateMode) Button(
            onClick = {
                try {
                    dpm.preferentialNetworkServiceConfigs = dpm.preferentialNetworkServiceConfigs.drop(route.index)
                    onNavigateUp()
                } catch(e: Exception) {
                    context.showOperationResultToast(false)
                    e.printStackTrace()
                }
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
fun OverrideApnScreen(onNavigateUp: () -> Unit, onNavigateToAddSetting: (Bundle) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var enabled by remember { mutableStateOf(false) }
    val settings = remember { mutableStateListOf<ApnSetting>() }
    fun refresh() {
        enabled = dpm.isOverrideApnEnabled(receiver)
        settings.clear()
        settings.addAll(dpm.getOverrideApns(receiver))
    }
    LaunchedEffect(Unit) { refresh() }
    MyScaffold(R.string.override_apn, 0.dp, onNavigateUp) {
        SwitchItem(
            R.string.enable, state = enabled,
            onCheckedChange = {
                dpm.setOverrideApnsEnabled(receiver, it)
                refresh()
            }
        )
        settings.forEach {
            Row(
                Modifier.fillMaxWidth().padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text(it.id.toString())
                    Text(it.apnName.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = typography.bodyMedium)
                    Text(it.entryName.toString(), color = MaterialTheme.colorScheme.onSurfaceVariant, style = typography.bodyMedium)
                }
                IconButton({
                    onNavigateToAddSetting(bundleOf("setting" to it))
                }) {
                    Icon(Icons.Default.Edit, null)
                }
            }
        }
        Row(
            Modifier.fillMaxWidth().clickable {
                onNavigateToAddSetting(Bundle())
            }.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, Modifier.padding(horizontal = 8.dp))
            Text(stringResource(R.string.add_config), style = typography.labelLarge)
        }
    }
}

private data class ApnType(val id: Int, val name: String, val requiresApi: Int = 0)
@SuppressLint("InlinedApi")
private val apnTypes = listOf(
    ApnType(ApnSetting.TYPE_DEFAULT, "Default"), ApnType(ApnSetting.TYPE_MMS, "MMS"), ApnType(ApnSetting.TYPE_SUPL, "SUPL"),
    ApnType(ApnSetting.TYPE_DUN, "DUN"), ApnType(ApnSetting.TYPE_HIPRI, "HiPri"), ApnType(ApnSetting.TYPE_FOTA, "FOTA"),
    ApnType(ApnSetting.TYPE_IMS, "IMS"), ApnType(ApnSetting.TYPE_CBS, "CBS"), ApnType(ApnSetting.TYPE_IA, "IA"),
    ApnType(ApnSetting.TYPE_EMERGENCY, "Emergency"), ApnType(ApnSetting.TYPE_MCX, "MCX", 29), ApnType(ApnSetting.TYPE_XCAP, "XCAP", 30),
    ApnType(ApnSetting.TYPE_BIP, "BIP", 31), ApnType(ApnSetting.TYPE_VSIM, "VSIM", 31), ApnType(ApnSetting.TYPE_ENTERPRISE, "Enterprise", 33),
    ApnType(ApnSetting.TYPE_RCS, "RCS", 35) // TODO: Adapt A16 later
)

@Serializable object AddApnSetting

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun AddApnSettingScreen(origin: ApnSetting?, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val fm = LocalFocusManager.current
    var dropdown by remember { mutableIntStateOf(0) } // 1:Auth type, 2:MVNO type, 3:Protocol, 4:Roaming protocol
    var dialog by remember { mutableIntStateOf(0) } // 1:Proxy, 2:MMS proxy
    var enabled by remember { mutableStateOf(true) }
    var apnName by remember { mutableStateOf(origin?.apnName ?: "") }
    var entryName by remember { mutableStateOf(origin?.entryName ?: "") }
    var apnType by remember { mutableIntStateOf(origin?.apnTypeBitmask ?: 0) }
    var profileId by remember { mutableStateOf(if(VERSION.SDK_INT >= 33) origin?.profileId?.toString() ?: "" else "") }
    var carrierId by remember { mutableStateOf(if(VERSION.SDK_INT >= 29) origin?.carrierId?.toString() ?: "" else "") }
    var authType by remember { mutableIntStateOf(origin?.authType ?: ApnSetting.AUTH_TYPE_NONE) }
    var user by remember { mutableStateOf(origin?.user ?: "") }
    var password by remember { mutableStateOf(origin?.password ?: "") }
    var proxyAddress by remember { mutableStateOf(if(VERSION.SDK_INT >= 29) origin?.proxyAddressAsString ?: "" else "") }
    var proxyPort by remember { mutableStateOf(if(VERSION.SDK_INT >= 29) origin?.proxyPort?.toString() ?: "" else "") }
    var mmsProxyAddress by remember { mutableStateOf(if(VERSION.SDK_INT >= 29) origin?.mmsProxyAddressAsString ?: "" else "") }
    var mmsProxyPort by remember { mutableStateOf(if(VERSION.SDK_INT >= 29) origin?.mmsProxyPort?.toString() ?: "" else "") }
    var mmsc by remember { mutableStateOf(origin?.mmsc?.toString() ?: "") }
    var mtuV4 by remember { mutableStateOf(if(VERSION.SDK_INT >= 33) origin?.mtuV4?.toString() ?: "" else "") }
    var mtuV6 by remember { mutableStateOf(if(VERSION.SDK_INT >= 33) origin?.mtuV6?.toString() ?: "" else "") }
    var mvnoType by remember { mutableIntStateOf(origin?.mvnoType ?: ApnSetting.MVNO_TYPE_SPN) }
    var networkTypeBitmask by remember { mutableStateOf(origin?.networkTypeBitmask?.toString() ?: "") }
    var operatorNumeric by remember { mutableStateOf(origin?.operatorNumeric ?: "") }
    var protocol by remember { mutableIntStateOf(origin?.protocol ?: ApnSetting.PROTOCOL_IP) }
    var roamingProtocol by remember { mutableIntStateOf(origin?.roamingProtocol ?: ApnSetting.PROTOCOL_IP) }
    var persistent by remember { mutableStateOf(if(VERSION.SDK_INT >= 33) origin?.isPersistent == true else false) }
    var alwaysOn by remember { mutableStateOf(VERSION.SDK_INT >= 35 && origin?.isAlwaysOn == true) }
    var errorMessage: String? by remember { mutableStateOf(null) }
    MySmallTitleScaffold(R.string.apn_setting, 8.dp, onNavigateUp) {
        val protocolMap = mapOf(
            ApnSetting.PROTOCOL_IP to "IPv4", ApnSetting.PROTOCOL_IPV6 to "IPv6",
            ApnSetting.PROTOCOL_IPV4V6 to "IPv4/v6", ApnSetting.PROTOCOL_PPP to "PPP"
        ).let {
            if(VERSION.SDK_INT >= 29) {
                it.plus(listOf(ApnSetting.PROTOCOL_NON_IP to "Non-IP", ApnSetting.PROTOCOL_UNSTRUCTURED to "Unstructured"))
            } else it
        }
        SwitchItem(R.string.enabled, state = enabled, onCheckedChange = { enabled = it }, padding = false)
        OutlinedTextField(
            apnName, { apnName = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.apn_name) + " (*)") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        OutlinedTextField(
            entryName, { entryName = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
            label = { Text(stringResource(R.string.entry_name) + " (*)") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        Text(stringResource(R.string.type) + " (*)", Modifier.padding(vertical = 4.dp), style = typography.titleLarge)
        FlowRow(Modifier.padding(bottom = 4.dp)) {
            apnTypes.filter { VERSION.SDK_INT >= it.requiresApi }.forEach {
                FilterChip(
                    apnType and it.id == it.id, {
                        apnType = if(apnType and it.id == it.id) apnType and (apnType xor it.id) else apnType or it.id
                    },
                    { Text(it.name) }, Modifier.padding(horizontal = 4.dp)
                )
            }
        }
        if(VERSION.SDK_INT >= 33) OutlinedTextField(
            profileId, { profileId = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.profile_id)) }, isError = profileId.isNotEmpty() && profileId.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        if(VERSION.SDK_INT >= 29) OutlinedTextField(
            carrierId, { carrierId = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
            label = { Text(stringResource(R.string.carrier_id)) },
            isError = carrierId.isNotEmpty() && carrierId.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            val rotate by animateFloatAsState(if(dropdown == 1) 180F else 0F)
            val authTypeMap = mapOf(
                ApnSetting.AUTH_TYPE_NONE to stringResource(R.string.none), ApnSetting.AUTH_TYPE_PAP to "PAP",
                ApnSetting.AUTH_TYPE_CHAP to "CHAP", ApnSetting.AUTH_TYPE_PAP_OR_CHAP to "PAP/CHAP"
            )
            Text(stringResource(R.string.auth_type))
            Row(Modifier.clickable { dropdown = 1 }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(authTypeMap[authType]!!, Modifier.padding(2.dp))
                Icon(Icons.Default.ArrowDropDown, null, Modifier.padding(start = 4.dp).rotate(rotate))
                DropdownMenu(dropdown == 1, { dropdown = 0 }) {
                    authTypeMap.forEach {
                        DropdownMenuItem({ Text(it.value) }, { authType = it.key; dropdown = 0 })
                    }
                }
            }
        }
        OutlinedTextField(
            user, { user = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.user)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        OutlinedTextField(
            password, { password = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        if(VERSION.SDK_INT >= 29) {
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.proxy), Modifier.padding(end = 8.dp))
                    Text(
                        if(proxyAddress.isEmpty()) stringResource(R.string.none) else "$proxyAddress:$proxyPort",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = typography.bodyMedium
                    )
                }
                TextButton({ dialog = 1 }) { Text(stringResource(R.string.edit)) }
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text(stringResource(R.string.mms_proxy), Modifier.padding(end = 8.dp))
                    Text(
                        if(mmsProxyAddress.isEmpty()) stringResource(R.string.none) else "$mmsProxyAddress:$mmsProxyPort",
                        color = MaterialTheme.colorScheme.onSurfaceVariant, style = typography.bodyMedium
                    )
                }
                TextButton({ dialog = 2 }) { Text(stringResource(R.string.edit)) }
            }
        }
        OutlinedTextField(
            mmsc, { mmsc = it }, Modifier.fillMaxWidth(),
            label = { Text("MMSC") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        if(VERSION.SDK_INT >= 33) Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), Arrangement.SpaceBetween) {
            val fr = FocusRequester()
            OutlinedTextField(
                mtuV4, { mtuV4 = it }, Modifier.fillMaxWidth(0.49F),
                label = { Text("MTU (IPv4)") },
                isError = !mtuV4.isEmpty() && mtuV4.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions { fr.requestFocus() }
            )
            OutlinedTextField(
                mtuV6, { mtuV6 = it }, Modifier.focusRequester(fr).fillMaxWidth(0.96F),
                label = { Text("MTU (IPv6)") },
                isError = !mtuV6.isEmpty() && mtuV6.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions { fm.clearFocus() }
            )
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            val rotate by animateFloatAsState(if(dropdown == 2) 180F else 0F)
            val mvnoTypeMap = mapOf(
                ApnSetting.MVNO_TYPE_SPN to "SPM", ApnSetting.MVNO_TYPE_IMSI to "IMSI",
                ApnSetting.MVNO_TYPE_GID to "GID", ApnSetting.MVNO_TYPE_ICCID to "ICCID"
            )
            Text(stringResource(R.string.mvno_type))
            Row(Modifier.clickable { dropdown = 2 }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(mvnoTypeMap[mvnoType]!!, Modifier.padding(4.dp))
                Icon(Icons.Default.ArrowDropDown, null, Modifier.padding(start = 4.dp).rotate(rotate))
                DropdownMenu(dropdown == 2, { dropdown = 0 }) {
                    mvnoTypeMap.forEach {
                        DropdownMenuItem({ Text(it.value) }, { mvnoType = it.key; dropdown = 0 })
                    }
                }
            }
        }
        OutlinedTextField(
            networkTypeBitmask, { networkTypeBitmask = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.network_type_bitmask)) },
            isError = networkTypeBitmask.isNotEmpty() && networkTypeBitmask.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        OutlinedTextField(
            operatorNumeric, { operatorNumeric = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
            label = { Text("Numeric operator ID") },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            val rotate by animateFloatAsState(if(dropdown == 3) 180F else 0F)
            Text(stringResource(R.string.protocol))
            Row(Modifier.clickable { dropdown = 3 }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(protocolMap[protocol]!!, Modifier.padding(2.dp))
                Icon(Icons.Default.ArrowDropDown, null, Modifier.padding(start = 4.dp).rotate(rotate))
                DropdownMenu(dropdown == 3, { dropdown = 0 }) {
                    protocolMap.forEach {
                        DropdownMenuItem({ Text(it.value) }, { protocol = it.key; dropdown = 0 })
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            val rotate by animateFloatAsState(if(dropdown == 4) 180F else 0F)
            Text(stringResource(R.string.roaming_protocol))
            Row(Modifier.clickable { dropdown = 4 }.padding(4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(protocolMap[roamingProtocol]!!, Modifier.padding(2.dp))
                Icon(Icons.Default.ArrowDropDown, null, Modifier.padding(start = 4.dp).rotate(rotate))
                DropdownMenu(dropdown == 4, { dropdown = 0 }) {
                    protocolMap.forEach {
                        DropdownMenuItem({ Text(it.value) }, { roamingProtocol = it.key; dropdown = 0 })
                    }
                }
            }
        }
        if(VERSION.SDK_INT >= 33) Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(stringResource(R.string.persistent))
            Switch(persistent, { persistent = it })
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(stringResource(R.string.always_on))
            Switch(alwaysOn, { alwaysOn = it })
        }
        Button(
            {
                try {
                    val setting = ApnSetting.Builder().apply {
                        setCarrierEnabled(enabled)
                        setApnName(apnName)
                        setEntryName(entryName)
                        setApnTypeBitmask(apnType)
                        setAuthType(authType)
                        setUser(user)
                        setPassword(password)
                        if(VERSION.SDK_INT >= 33) profileId.toIntOrNull()?.let { setProfileId(it) }
                        if(VERSION.SDK_INT >= 29) {
                            carrierId.toIntOrNull()?.let { setCarrierId(it) }
                            setProxyAddress(proxyAddress)
                            proxyPort.toIntOrNull()?.let { setProxyPort(it) }
                            setMmsProxyAddress(mmsProxyAddress)
                            mmsProxyPort.toIntOrNull()?.let { setMmsProxyPort(it) }
                        }
                        setMmsc(Uri.parse(mmsc))
                        if(VERSION.SDK_INT >= 33) {
                            mtuV4.toIntOrNull()?.let { setMtuV4(it) }
                            mtuV6.toIntOrNull()?.let { setMtuV6(it) }
                        }
                        setMvnoType(mvnoType)
                        networkTypeBitmask.toIntOrNull()?.let { setNetworkTypeBitmask(it) }
                        setOperatorNumeric(operatorNumeric)
                        setProtocol(protocol)
                        setRoamingProtocol(roamingProtocol)
                        if(VERSION.SDK_INT >= 33) setPersistent(persistent)
                        if(VERSION.SDK_INT >= 35) setAlwaysOn(alwaysOn)
                    }.build()
                    if(origin == null) {
                        dpm.addOverrideApn(receiver, setting)
                    } else {
                        dpm.updateOverrideApn(receiver, origin.id, setting)
                    }
                    onNavigateUp()
                } catch(e: Exception) {
                    errorMessage = (e::class.qualifiedName ?: "") + "\n" + (e.message ?: "")
                }
            },
            Modifier.fillMaxWidth().padding(vertical = 4.dp)
        ) {
            Text(stringResource(if(origin != null) R.string.update else R.string.add))
        }
        if(origin != null) Button(
            {
                dpm.removeOverrideApn(receiver, origin.id)
                onNavigateUp()
            },
            Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError)
        ) {
            Text(stringResource(R.string.delete))
        }
        if(dialog != 0) {
            var address by remember { mutableStateOf((if(dialog == 1) proxyAddress else mmsProxyAddress)) }
            var port by remember { mutableStateOf((if(dialog == 1) proxyPort else mmsProxyPort)) }
            val fr = FocusRequester()
            AlertDialog(
                title = { Text(if(dialog == 1) "Proxy" else "MMS proxy") },
                text = {
                    val fm = LocalFocusManager.current
                    Column {
                        OutlinedTextField(
                            address, { address = it }, Modifier.fillMaxWidth().padding(bottom = 4.dp),
                            textStyle = typography.bodyLarge,
                            label = { Text(stringResource(R.string.address)) },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions { fr.requestFocus() }
                        )
                        OutlinedTextField(
                            port, { port = it }, Modifier.fillMaxWidth().focusRequester(fr),
                            textStyle = typography.bodyLarge,
                            isError = port.isNotEmpty() && port.toIntOrNull() == null,
                            label = { Text(stringResource(R.string.port)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions { fm.clearFocus() }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        {
                            if(dialog == 1) {
                                proxyAddress = address
                                proxyPort = port
                            } else {
                                mmsProxyAddress = address
                                mmsProxyPort = port
                            }
                            dialog = 0
                        }
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                },
                dismissButton = {
                    TextButton({ dialog = 0 }) { Text(stringResource(R.string.cancel)) }
                },
                onDismissRequest = { dialog = 0 }
            )
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
}
