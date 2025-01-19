package com.bintianqi.owndroid.dpm

import android.Manifest
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
import android.content.Intent
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
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.UNKNOWN_CARRIER_ID
import android.telephony.data.ApnSetting.AUTH_TYPE_CHAP
import android.telephony.data.ApnSetting.AUTH_TYPE_NONE
import android.telephony.data.ApnSetting.AUTH_TYPE_PAP
import android.telephony.data.ApnSetting.AUTH_TYPE_PAP_OR_CHAP
import android.telephony.data.ApnSetting.Builder
import android.telephony.data.ApnSetting.MVNO_TYPE_GID
import android.telephony.data.ApnSetting.MVNO_TYPE_ICCID
import android.telephony.data.ApnSetting.MVNO_TYPE_IMSI
import android.telephony.data.ApnSetting.MVNO_TYPE_SPN
import android.telephony.data.ApnSetting.PROTOCOL_IP
import android.telephony.data.ApnSetting.PROTOCOL_IPV4V6
import android.telephony.data.ApnSetting.PROTOCOL_IPV6
import android.telephony.data.ApnSetting.PROTOCOL_NON_IP
import android.telephony.data.ApnSetting.PROTOCOL_PPP
import android.telephony.data.ApnSetting.PROTOCOL_UNSTRUCTURED
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.os.bundleOf
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.formatFileSize
import com.bintianqi.owndroid.humanReadableDate
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.ExpandExposedTextFieldIcon
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoCard
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
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
import java.net.InetAddress
import kotlin.math.max
import kotlin.reflect.jvm.jvmErasure

@Composable
fun Network(navCtrl:NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    val dhizuku = sharedPref.getBoolean("dhizuku", false)
    MyScaffold(R.string.network, 0.dp, navCtrl) {
        if(!dhizuku) FunctionItem(R.string.wifi, icon = R.drawable.wifi_fill0) { navCtrl.navigate("Wifi") }
        if(VERSION.SDK_INT >= 30) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) { navCtrl.navigate("NetworkOptions") }
        }
        if(VERSION.SDK_INT >= 23 && (deviceOwner || profileOwner))
            FunctionItem(R.string.network_stats, icon = R.drawable.query_stats_fill0) { navCtrl.navigate("NetworkStats") }
        if(VERSION.SDK_INT >= 29 && deviceOwner) {
            FunctionItem(R.string.private_dns, icon = R.drawable.dns_fill0) { navCtrl.navigate("PrivateDNS") }
        }
        if(VERSION.SDK_INT >= 24) {
            FunctionItem(R.string.always_on_vpn, icon = R.drawable.vpn_key_fill0) { navCtrl.navigate("AlwaysOnVpn") }
        }
        if(deviceOwner) {
            FunctionItem(R.string.recommended_global_proxy, icon = R.drawable.vpn_key_fill0) { navCtrl.navigate("RecommendedGlobalProxy") }
        }
        if(VERSION.SDK_INT >= 26 && !dhizuku && (deviceOwner || (profileOwner && dpm.isManagedProfile(receiver)))) {
            FunctionItem(R.string.network_logging, icon = R.drawable.description_fill0) { navCtrl.navigate("NetworkLog") }
        }
        if(VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.wifi_auth_keypair, icon = R.drawable.key_fill0) { navCtrl.navigate("WifiAuthKeypair") }
        }
        if(VERSION.SDK_INT >= 33) {
            FunctionItem(R.string.preferential_network_service, icon = R.drawable.globe_fill0) { navCtrl.navigate("PreferentialNetworkService") }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            FunctionItem(R.string.override_apn_settings, icon = R.drawable.cell_tower_fill0) { navCtrl.navigate("OverrideAPN") }
        }
    }
}

@Composable
fun NetworkOptions(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, 0.dp, navCtrl) {
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Wifi(navCtrl: NavHostController) {
    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    var tabIndex by rememberSaveable { mutableIntStateOf(0) }
    tabIndex = pagerState.currentPage
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.wifi)) },
                navigationIcon = { NavIcon { navCtrl.navigateUp() } }
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
                            FunctionItem(R.string.min_wifi_security_level) { navCtrl.navigate("MinWifiSecurityLevel") }
                            FunctionItem(R.string.wifi_ssid_policy) { navCtrl.navigate("WifiSsidPolicy") }
                        }
                    }
                } else if(page == 1) {
                    SavedNetworks(navCtrl)
                } else {
                    AddNetwork()
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
private fun SavedNetworks(navCtrl: NavHostController) {
    val context = LocalContext.current
    val wm = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
    val configuredNetworks = remember { mutableStateListOf<WifiConfiguration>() }
    var networkDetailsDialog by remember { mutableIntStateOf(-1) } // -1:Hidden, 0+:Index of configuredNetworks
    fun refresh() {
        configuredNetworks.clear()
        wm.configuredNetworks.forEach { network ->
            if(configuredNetworks.none { it.networkId == network.networkId }) configuredNetworks += network
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
                        val dest = navCtrl.graph.findNode("UpdateNetwork")
                        if(dest != null)
                        navCtrl.navigate(dest.id, bundleOf("wifi_configuration" to network))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
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

@Composable
fun UpdateNetwork(arguments: Bundle, navCtrl: NavHostController) {
    MyScaffold(R.string.update_network, 0.dp, navCtrl, false) {
        AddNetwork(arguments.getParcelable("wifi_configuration"), navCtrl)
    }
}

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNetwork(wifiConfig: WifiConfiguration? = null, navCtrl: NavHostController? = null) {
    val context = LocalContext.current
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
                Column {
                    OutlinedTextField(
                        value = ipAddress, onValueChange = { ipAddress = it },
                        placeholder = { Text("192.168.1.2/24") }, label = { Text(stringResource(R.string.ip_address)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = gatewayAddress, onValueChange = { gatewayAddress = it },
                        placeholder = { Text("192.168.1.1") }, label = { Text(stringResource(R.string.gateway_address)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = dnsServers, onValueChange = { dnsServers = it },
                        label = { Text(stringResource(R.string.dns_servers)) }, minLines = 2,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
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
                Column {
                    OutlinedTextField(
                        value = httpProxyHost, onValueChange = { httpProxyHost = it }, label = { Text(stringResource(R.string.host)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = httpProxyPort, onValueChange = { httpProxyPort = it }, label = { Text(stringResource(R.string.port)) },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
                    )
                    OutlinedTextField(
                        value = httpProxyExclList, onValueChange = { httpProxyExclList = it }, label = { Text(stringResource(R.string.excluded_hosts)) },
                        minLines = 2, placeholder = { Text("example.com\n*.example.com") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp)
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
                        if(createdNetworkId != -1) navCtrl?.navigateUp()
                    }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { resultDialog = false }
        )
    }
}

@RequiresApi(33)
@Composable
fun WifiSecurityLevel(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var selectedWifiSecLevel by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { selectedWifiSecLevel = dpm.minimumRequiredWifiSecurityLevel }
    MyScaffold(R.string.min_wifi_security_level, 8.dp, navCtrl) {
        RadioButtonItem(R.string.wifi_security_open, selectedWifiSecLevel == WIFI_SECURITY_OPEN) { selectedWifiSecLevel = WIFI_SECURITY_OPEN }
        RadioButtonItem("WEP, WPA(2)-PSK", selectedWifiSecLevel == WIFI_SECURITY_PERSONAL) { selectedWifiSecLevel = WIFI_SECURITY_PERSONAL }
        RadioButtonItem("WPA-EAP", selectedWifiSecLevel == WIFI_SECURITY_ENTERPRISE_EAP) { selectedWifiSecLevel = WIFI_SECURITY_ENTERPRISE_EAP }
        RadioButtonItem("WPA3-192bit", selectedWifiSecLevel == WIFI_SECURITY_ENTERPRISE_192) { selectedWifiSecLevel = WIFI_SECURITY_ENTERPRISE_192 }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.minimumRequiredWifiSecurityLevel = selectedWifiSecLevel
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_minimum_wifi_security_level)
    }
}

@RequiresApi(33)
@Composable
fun WifiSsidPolicy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.wifi_ssid_policy, 8.dp, navCtrl) {
        var selectedPolicyType by remember { mutableIntStateOf(-1) }
        val ssidList = remember { mutableStateListOf<WifiSsid>() }
        val refreshPolicy = {
            val policy = dpm.wifiSsidPolicy
            ssidList.clear()
            selectedPolicyType = policy?.policyType ?: -1
            ssidList.addAll(policy?.ssids ?: mutableSetOf())
        }
        LaunchedEffect(Unit) { refreshPolicy() }
        RadioButtonItem(R.string.none, selectedPolicyType == -1) { selectedPolicyType = -1 }
        RadioButtonItem(R.string.whitelist, selectedPolicyType == WIFI_SSID_POLICY_TYPE_ALLOWLIST) {
            selectedPolicyType = WIFI_SSID_POLICY_TYPE_ALLOWLIST
        }
        RadioButtonItem(R.string.blacklist, selectedPolicyType == WIFI_SSID_POLICY_TYPE_DENYLIST) {
            selectedPolicyType = WIFI_SSID_POLICY_TYPE_DENYLIST
        }
        AnimatedVisibility(selectedPolicyType != -1) {
            var inputSsid by remember { mutableStateOf("") }
            Column {
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
                Spacer(Modifier.padding(vertical = 10.dp))
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
            modifier = Modifier.fillMaxWidth()
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

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(23)
@Composable
fun NetworkStats(navCtrl: NavHostController, vm: MyViewModel) {
    val context = LocalContext.current
    val deviceOwner = context.isDeviceOwner
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
    MyScaffold(R.string.network_stats, 8.dp, navCtrl) {
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
            if(VERSION.SDK_INT >= 24) {
                val selectedPackage by vm.selectedPackage.collectAsStateWithLifecycle()
                if(readOnly && selectedPackage != "") {
                    try {
                        uid = context.packageManager.getPackageUid(selectedPackage, 0)
                        uidText = "$selectedPackage ($uid)"
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
                        navCtrl.navigate("PackageSelector")
                        activeTextField = NetworkStatsActiveTextField.None
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
                        if(queryType == 1) {
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
                        val bundle = Bundle()
                        bundle.putInt("size", buckets.size)
                        buckets.forEachIndexed { index, bucket ->
                            val subBundle = bundleOf(
                                "rx_bytes" to bucket.rxBytes,
                                "rx_packets" to bucket.rxPackets,
                                "tx_bytes" to bucket.txBytes,
                                "tx_packets" to bucket.txPackets,
                                "uid" to bucket.uid,
                                "state" to bucket.state,
                                "start_time" to bucket.startTimeStamp,
                                "end_time" to bucket.endTimeStamp
                            )
                            if(VERSION.SDK_INT >= 24) {
                                subBundle.putInt("tag", bucket.tag)
                                subBundle.putInt("roaming", bucket.roaming)
                            }
                            if(VERSION.SDK_INT >= 26) subBundle.putInt("metered", bucket.metered)
                            bundle.putBundle(index.toString(), subBundle)
                        }
                        withContext(Dispatchers.Main) {
                            querying = false
                            val nodeId = navCtrl.graph.findNode("NetworkStatsViewer")?.id
                            if(nodeId != null) navCtrl.navigate(nodeId, bundle)
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

@RequiresApi(23)
@Composable
fun NetworkStatsViewer(navCtrl: NavHostController, navArgs: Bundle) {
    var index by remember { mutableIntStateOf(0) }
    val size = navArgs.getInt("size", 1)
    MyScaffold(R.string.place_holder, 8.dp, navCtrl, false) {
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
        val data = navArgs.getBundle(index.toString())!!
        Text(
            data.getLong("start_time").humanReadableDate + "  ~  " + data.getLong("end_time").humanReadableDate,
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp)
        )
        val txBytes = data.getLong("tx_bytes")
        Text(stringResource(R.string.transmitted), style = typography.titleLarge)
        Column(modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)) {
            Text("$txBytes bytes")
            Text(formatFileSize(txBytes))
            Text(data.getLong("tx_packets").toString() + " packets")
        }
        val rxBytes = data.getLong("rx_bytes")
        Text(stringResource(R.string.received), style = typography.titleLarge)
        Column(modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)) {
            Text("$rxBytes bytes")
            Text(formatFileSize(rxBytes))
            Text(data.getLong("rx_packets").toString() + " packets")
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            val textMap = mapOf(
                NetworkStats.Bucket.STATE_ALL to R.string.all,
                NetworkStats.Bucket.STATE_DEFAULT to R.string.default_str,
                NetworkStats.Bucket.STATE_FOREGROUND to R.string.foreground
            )
            Text(stringResource(R.string.state), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(textMap[data.getInt("state")] ?: R.string.unknown))
        }
        if(VERSION.SDK_INT >= 24) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val tag = data.getInt("tag")
                Text(stringResource(R.string.tag), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                Text(if(tag == NetworkStats.Bucket.TAG_NONE) stringResource(R.string.all) else tag.toString())
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                val textMap = mapOf(
                    NetworkStats.Bucket.ROAMING_ALL to R.string.all,
                    NetworkStats.Bucket.ROAMING_YES to R.string.yes,
                    NetworkStats.Bucket.ROAMING_NO to R.string.no
                )
                Text(stringResource(R.string.roaming), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
                Text(stringResource(textMap[data.getInt("roaming")] ?: R.string.unknown))
            }
        }
        if(VERSION.SDK_INT >= 26) Row(verticalAlignment = Alignment.CenterVertically) {
            val textMap = mapOf(
                NetworkStats.Bucket.METERED_ALL to R.string.all,
                NetworkStats.Bucket.METERED_YES to R.string.yes,
                NetworkStats.Bucket.METERED_NO to R.string.no
            )
            Text(stringResource(R.string.metered), style = typography.titleMedium, modifier = Modifier.padding(end = 8.dp))
            Text(stringResource(textMap[data.getInt("metered")] ?: R.string.unknown))
        }
    }
}

@RequiresApi(29)
@Composable
fun PrivateDNS(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.private_dns, 8.dp, navCtrl) {
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
        InfoCard(R.string.info_private_dns_mode_oppertunistic)
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
        InfoCard(R.string.info_set_private_dns_host)
    }
}

@RequiresApi(24)
@Composable
fun AlwaysOnVPNPackage(navCtrl: NavHostController, vm: MyViewModel) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var lockdown by rememberSaveable { mutableStateOf(false) }
    var pkgName by rememberSaveable { mutableStateOf("") }
    val focusMgr = LocalFocusManager.current
    val refresh = { pkgName = dpm.getAlwaysOnVpnPackage(receiver) ?: "" }
    LaunchedEffect(Unit) { refresh() }
    val updatePackage by vm.selectedPackage.collectAsState()
    LaunchedEffect(updatePackage) {
        if(updatePackage != "") {
            pkgName = updatePackage
            vm.selectedPackage.value = ""
        }
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
    MyScaffold(R.string.always_on_vpn, 8.dp, navCtrl) {
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
                        .clickable(onClick = {
                            focusMgr.clearFocus()
                            navCtrl.navigate("PackageSelector")
                        })
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
        InfoCard(R.string.info_always_on_vpn)
    }
}

@Composable
fun RecommendedGlobalProxy(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var proxyType by remember { mutableIntStateOf(0) }
    var proxyUri by remember { mutableStateOf("") }
    var specifyPort by remember { mutableStateOf(false) }
    var proxyPort by remember { mutableStateOf("") }
    var exclList by remember { mutableStateOf("") }
    MyScaffold(R.string.recommended_global_proxy, 8.dp, navCtrl) {
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
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
            )
        }
        Spacer(Modifier.padding(vertical = 4.dp))
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
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_recommended_global_proxy)
    }
}

@RequiresApi(26)
@Composable
fun NetworkLogging(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val logFile = context.filesDir.resolve("NetworkLogs.json")
    var fileSize by remember { mutableLongStateOf(0) }
    LaunchedEffect(Unit) { fileSize = logFile.length() }
    val exportNetworkLogsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        result.data?.data?.let { uri ->
            context.contentResolver.openOutputStream(uri)?.use { outStream ->
                outStream.write("[".encodeToByteArray())
                logFile.inputStream().use { it.copyTo(outStream) }
                outStream.write("]".encodeToByteArray())
                context.showOperationResultToast(true)
            }
        }
    }
    MyScaffold(R.string.network_logging, 8.dp, navCtrl) {
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
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/json")
                    intent.putExtra(Intent.EXTRA_TITLE, "NetworkLogs.json")
                    exportNetworkLogsLauncher.launch(intent)
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
        InfoCard(R.string.info_network_log)
    }
}

@RequiresApi(31)
@Composable
fun WifiAuthKeypair(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val focusMgr = LocalFocusManager.current
    var keyPair by remember { mutableStateOf("") }
    MyScaffold(R.string.wifi_auth_keypair, 8.dp, navCtrl) {
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

@RequiresApi(33)
@Composable
fun PreferentialNetworkService(navCtrl: NavHostController) {
    val focusMgr = LocalFocusManager.current
    val context = LocalContext.current
    val dpm = context.getDPM()
    var masterEnabled by remember { mutableStateOf(false) }
    val configs = remember { mutableStateListOf<PreferentialNetworkServiceConfig>() }
    var index by remember { mutableIntStateOf(-1) }
    var enabled by remember { mutableStateOf(false) }
    var networkId by remember { mutableStateOf("") }
    var allowFallback by remember { mutableStateOf(false) }
    var blockNonMatching by remember { mutableStateOf(false) }
    var excludedUids by remember { mutableStateOf("") }
    var includedUids by remember { mutableStateOf("") }
    fun refresh() {
        val config = configs.getOrNull(index)
        enabled = config?.isEnabled == true
        networkId = config?.networkId?.toString() ?: ""
        allowFallback = config?.isFallbackToDefaultConnectionAllowed == true
        if(VERSION.SDK_INT >= 34) blockNonMatching = config?.shouldBlockNonMatchingNetworks() == true
        includedUids = config?.includedUids?.joinToString("\n") ?: ""
        excludedUids = config?.excludedUids?.joinToString("\n") ?: ""
    }
    fun saveCurrentConfig() {
        val builder = PreferentialNetworkServiceConfig.Builder()
        builder.setEnabled(enabled)
        builder.setNetworkId(networkId.toInt())
        builder.setFallbackToDefaultConnectionAllowed(allowFallback)
        if(VERSION.SDK_INT >= 34) builder.setShouldBlockNonMatchingNetworks(blockNonMatching)
        builder.setIncludedUids(includedUids.lines().dropWhile { it == "" }.map { it.toInt() }.toIntArray())
        builder.setExcludedUids(excludedUids.lines().dropWhile { it == "" }.map { it.toInt() }.toIntArray())
        if(index < configs.size) configs[index] = builder.build() else configs += builder.build()
    }
    fun initialize() {
        masterEnabled = dpm.isPreferentialNetworkServiceEnabled
        configs.addAll(dpm.preferentialNetworkServiceConfigs)
        index = max(0, configs.size - 1)
        refresh()
    }
    LaunchedEffect(Unit) { initialize() }
    MyScaffold(R.string.preferential_network_service, 8.dp, navCtrl) {
        SwitchItem(R.string.enabled, state = masterEnabled, onCheckedChange = { masterEnabled = it }, padding = false)
        Row(
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            IconButton(
                onClick = {
                    try {
                        saveCurrentConfig()
                        index -= 1
                        refresh()
                    } catch(e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, R.string.failed_to_save_current_config, Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = index > 0
            ) {
                Icon(imageVector = Icons.AutoMirrored.Default.KeyboardArrowLeft, contentDescription = stringResource(R.string.previous))
            }
            Text("${index + 1} / ${configs.size}")
            IconButton(
                onClick = {
                    try {
                        saveCurrentConfig()
                        index += 1
                        refresh()
                    } catch(e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, R.string.failed_to_save_current_config, Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Icon(
                    imageVector = if(index + 1 >= configs.size) Icons.Default.Add else Icons.AutoMirrored.Default.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.previous)
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = {
                    try {
                        saveCurrentConfig()
                        context.showOperationResultToast(true)
                    } catch(e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, R.string.failed_to_save_current_config, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.padding(end = 10.dp)
            ) {
                Icon(painter = painterResource(R.drawable.save_fill0), contentDescription = stringResource(R.string.save_current_config))
            }
            IconButton(
                onClick = {
                    if(index < configs.size) configs.removeAt(index)
                    if(index > 0) index -= 1
                    refresh()
                }
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = stringResource(R.string.delete_current_config))
            }
        }
        SwitchItem(title = R.string.enabled, state = enabled, onCheckedChange = { enabled = it }, padding = false)
        OutlinedTextField(
            value = networkId, onValueChange = { networkId = it },
            label = { Text(stringResource(R.string.network_id)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { focusMgr.clearFocus() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        SwitchItem(
            title = R.string.allow_fallback_to_default_connection,
            state = allowFallback, onCheckedChange = { allowFallback = it }, padding = false
        )
        if(VERSION.SDK_INT >= 34) SwitchItem(
            title = R.string.block_non_matching_networks,
            state = blockNonMatching, onCheckedChange = { blockNonMatching = it }, padding = false
        )
        OutlinedTextField(
            value = includedUids, onValueChange = { includedUids = it }, minLines = 2,
            label = { Text(stringResource(R.string.included_uids)) },
            supportingText = { Text(stringResource(R.string.one_uid_per_line)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = excludedUids, onValueChange = { excludedUids = it }, minLines = 2,
            label = { Text(stringResource(R.string.excluded_uids)) },
            supportingText = { Text(stringResource(R.string.one_uid_per_line)) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        Button(
            onClick = {
                dpm.isPreferentialNetworkServiceEnabled = masterEnabled
                dpm.preferentialNetworkServiceConfigs = configs
                initialize()
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@RequiresApi(28)
@Composable
fun OverrideAPN(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    val setting = dpm.getOverrideApns(receiver)
    var inputNum by remember { mutableStateOf("0") }
    var nextStep by remember { mutableStateOf(false) }
    val builder = Builder()
    MyScaffold(R.string.override_apn_settings, 8.dp, navCtrl) {
        Text(text = stringResource(id = R.string.developing))
        Spacer(Modifier.padding(vertical = 5.dp))
        SwitchItem(
            R.string.enable,
            getState = { dpm.isOverrideApnEnabled(receiver) }, onCheckedChange = { dpm.setOverrideApnsEnabled(receiver,it) },
            padding = false
        )
        Text(text = stringResource(R.string.total_apn_amount, setting.size))
        if(setting.isNotEmpty()) {
            Text(text = stringResource(R.string.select_a_apn_or_create, setting.size))
            TextField(
                value = inputNum,
                label = { Text("APN") },
                onValueChange = { inputNum = it },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                enabled = !nextStep
            )
        }else{
            Text(text = stringResource(R.string.no_apn_you_should_create_one))
        }
        Button(
            onClick = { focusMgr.clearFocus(); nextStep =! nextStep },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputNum != "" && (nextStep || inputNum=="0" || setting[inputNum.toInt()-1] != null)
        ) {
            Text(stringResource(if(nextStep) R.string.previous_step else R.string.next_step))
        }
        var result = Builder().build()
        AnimatedVisibility(nextStep) {
            var carrierEnabled by remember { mutableStateOf(false) }
            var inputApnName by remember { mutableStateOf("") }
            var user by remember { mutableStateOf("") }
            var profileId by remember { mutableStateOf("") }
            var selectedAuthType by remember { mutableIntStateOf(AUTH_TYPE_NONE) }
            var carrierId by remember { mutableStateOf("$UNKNOWN_CARRIER_ID") }
            var apnTypeBitmask by remember { mutableStateOf("") }
            var entryName by remember { mutableStateOf("") }
            var mmsProxyAddress by remember { mutableStateOf("") }
            var mmsProxyPort by remember { mutableStateOf("") }
            var proxyAddress by remember { mutableStateOf("") }
            var proxyPort by remember { mutableStateOf("") }
            var mmsc by remember { mutableStateOf("") }
            var mtuV4 by remember { mutableStateOf("") }
            var mtuV6 by remember { mutableStateOf("") }
            var mvnoType by remember { mutableIntStateOf(-1) }
            var networkTypeBitmask by remember { mutableStateOf("") }
            var operatorNumeric by remember { mutableStateOf("") }
            var password by remember { mutableStateOf("") }
            var persistent by remember { mutableStateOf(false) }
            var protocol by remember { mutableIntStateOf(-1) }
            var roamingProtocol by remember { mutableIntStateOf(-1) }
            var id by remember { mutableIntStateOf(0) }
            
            if(inputNum!="0") {
                val current = setting[inputNum.toInt()-1]
                id = current.id
                carrierEnabled = current.isEnabled
                inputApnName = current.apnName
                user = current.user
                if(VERSION.SDK_INT>=33) {profileId = current.profileId.toString() }
                selectedAuthType = current.authType
                apnTypeBitmask = current.apnTypeBitmask.toString()
                entryName = current.entryName
                if(VERSION.SDK_INT>=29) {mmsProxyAddress = current.mmsProxyAddressAsString}
                mmsProxyPort = current.mmsProxyPort.toString()
                if(VERSION.SDK_INT>=29) {proxyAddress = current.proxyAddressAsString}
                proxyPort = current.proxyPort.toString()
                mmsc = current.mmsc.toString()
                if(VERSION.SDK_INT>=33) { mtuV4 = current.mtuV4.toString(); mtuV6 = current.mtuV6.toString() }
                mvnoType = current.mvnoType
                networkTypeBitmask = current.networkTypeBitmask.toString()
                operatorNumeric = current.operatorNumeric
                password = current.password
                if(VERSION.SDK_INT>=33) {persistent = current.isPersistent}
                protocol = current.protocol
                roamingProtocol = current.roamingProtocol
            }
            
            Column {
                
                Text(text = "APN", style = typography.titleLarge)
                TextField(
                    value = inputApnName,
                    onValueChange = {inputApnName=it },
                    label = { Text(stringResource(R.string.name)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = stringResource(R.string.enable), style = typography.titleLarge)
                    Switch(checked = carrierEnabled, onCheckedChange = {carrierEnabled=it })
                }
                
                Text(text = stringResource(R.string.user_name), style = typography.titleLarge)
                TextField(
                    value = user,
                    onValueChange = { user=it },
                    label = { Text(stringResource(R.string.user_name)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                if(VERSION.SDK_INT>=33) {
                    Text(text = stringResource(R.string.profile_id), style = typography.titleLarge)
                    TextField(
                        value = profileId,
                        onValueChange = { profileId=it },
                        label = { Text("ID") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                
                Text(text = stringResource(R.string.auth_type), style = typography.titleLarge)
                RadioButtonItem(R.string.none, selectedAuthType==AUTH_TYPE_NONE) { selectedAuthType = AUTH_TYPE_NONE }
                RadioButtonItem("CHAP", selectedAuthType == AUTH_TYPE_CHAP) { selectedAuthType = AUTH_TYPE_CHAP }
                RadioButtonItem("PAP", selectedAuthType == AUTH_TYPE_PAP) { selectedAuthType = AUTH_TYPE_PAP }
                RadioButtonItem("PAP/CHAP", selectedAuthType == AUTH_TYPE_PAP_OR_CHAP) { selectedAuthType = AUTH_TYPE_PAP_OR_CHAP }

                if(VERSION.SDK_INT>=29) {
                    val ts = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                    carrierId = ts.simCarrierId.toString()
                    Text(text = "CarrierID", style = typography.titleLarge)
                    TextField(
                        value = carrierId,
                        onValueChange = { carrierId=it },
                        label = { Text("ID") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                
                Text(text = stringResource(R.string.apn_type), style = typography.titleLarge)
                TextField(
                    value = apnTypeBitmask,
                    onValueChange = { apnTypeBitmask=it },
                    label = { Text(stringResource(R.string.bitmask)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.description), style = typography.titleLarge)
                TextField(
                    value = entryName,
                    onValueChange = {entryName=it },
                    label = { Text(stringResource(R.string.description)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.mms_proxy), style = typography.titleLarge)
                if(VERSION.SDK_INT>=29) {
                    TextField(
                        value = mmsProxyAddress,
                        onValueChange = { mmsProxyAddress=it },
                        label = { Text(stringResource(R.string.address)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                TextField(
                    value = mmsProxyPort,
                    onValueChange = { mmsProxyPort=it },
                    label = { Text(stringResource(R.string.port)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.proxy), style = typography.titleLarge)
                if(VERSION.SDK_INT>=29) {
                    TextField(
                        value = proxyAddress,
                        onValueChange = { proxyAddress=it },
                        label = { Text(stringResource(R.string.address)) },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                TextField(
                    value = proxyPort,
                    onValueChange = { proxyPort=it },
                    label = { Text(stringResource(R.string.port)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = "MMSC", style = typography.titleLarge)
                TextField(
                    value = mmsc,
                    onValueChange = { mmsc=it },
                    label = { Text("Uri") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                if(VERSION.SDK_INT>=33) {
                    Text(text = "MTU", style = typography.titleLarge)
                    TextField(
                        value = mtuV4,
                        onValueChange = { mtuV4=it },
                        label = { Text("IPV4") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                    TextField(
                        value = mtuV6,
                        onValueChange = { mtuV6=it },
                        label = { Text("IPV6") },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                
                Text(text = "MVNO", style = typography.titleLarge)
                RadioButtonItem("SPN", mvnoType == MVNO_TYPE_SPN) { mvnoType = MVNO_TYPE_SPN }
                RadioButtonItem("IMSI", mvnoType == MVNO_TYPE_IMSI) { mvnoType = MVNO_TYPE_IMSI }
                RadioButtonItem("GID", mvnoType == MVNO_TYPE_GID) { mvnoType = MVNO_TYPE_GID }
                RadioButtonItem("ICCID", mvnoType == MVNO_TYPE_ICCID) { mvnoType = MVNO_TYPE_ICCID }

                Text(text = stringResource(R.string.apn_network_type), style = typography.titleLarge)
                TextField(
                    value = networkTypeBitmask,
                    onValueChange = { networkTypeBitmask=it },
                    label = { Text(stringResource(R.string.bitmask)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = "OperatorNumeric", style = typography.titleLarge)
                TextField(
                    value = operatorNumeric,
                    onValueChange = { operatorNumeric=it },
                    label = { Text("ID") },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.password), style = typography.titleLarge)
                TextField(
                    value = password,
                    onValueChange = { password=it },
                    label = { Text(stringResource(R.string.password)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                if(VERSION.SDK_INT>=33) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = stringResource(R.string.persistent), style = typography.titleLarge)
                        Switch(checked = persistent, onCheckedChange = { persistent=it })
                    }
                }
                
                Text(text = stringResource(R.string.protocol), style = typography.titleLarge)
                RadioButtonItem("IPV4", protocol == PROTOCOL_IP) { protocol = PROTOCOL_IP }
                RadioButtonItem("IPV6", protocol == PROTOCOL_IPV6) { protocol = PROTOCOL_IPV6 }
                RadioButtonItem("IPV4/IPV6", protocol == PROTOCOL_IPV4V6) { protocol = PROTOCOL_IPV4V6 }
                RadioButtonItem("PPP", protocol == PROTOCOL_PPP) { protocol = PROTOCOL_PPP }
                if(VERSION.SDK_INT>=29) {
                    RadioButtonItem("non-IP", protocol == PROTOCOL_NON_IP) { protocol = PROTOCOL_NON_IP }
                    RadioButtonItem("Unstructured", protocol == PROTOCOL_UNSTRUCTURED) { protocol = PROTOCOL_UNSTRUCTURED }
                }
                
                Text(text = stringResource(R.string.roaming_protocol), style = typography.titleLarge)
                RadioButtonItem("IPV4", roamingProtocol == PROTOCOL_IP) { roamingProtocol = PROTOCOL_IP }
                RadioButtonItem("IPV6", roamingProtocol == PROTOCOL_IPV6) { roamingProtocol = PROTOCOL_IPV6 }
                RadioButtonItem("IPV4/IPV6", roamingProtocol == PROTOCOL_IPV4V6) { roamingProtocol = PROTOCOL_IPV4V6 }
                RadioButtonItem("PPP", roamingProtocol == PROTOCOL_PPP) { roamingProtocol = PROTOCOL_PPP }
                if(VERSION.SDK_INT>=29) {
                    RadioButtonItem("non-IP", roamingProtocol == PROTOCOL_NON_IP) { roamingProtocol = PROTOCOL_NON_IP }
                    RadioButtonItem("Unstructured", roamingProtocol == PROTOCOL_UNSTRUCTURED) { roamingProtocol = PROTOCOL_UNSTRUCTURED }
                }
                
                var finalStep by remember { mutableStateOf(false) }
                Button(
                    onClick = {
                        if(!finalStep) {
                            builder.setCarrierEnabled(carrierEnabled)
                            builder.setApnName(inputApnName)
                            builder.setUser(user)
                            if(VERSION.SDK_INT>=33) { builder.setProfileId(profileId.toInt()) }
                            builder.setAuthType(selectedAuthType)
                            if(VERSION.SDK_INT>=29) { builder.setCarrierId(carrierId.toInt()) }
                            builder.setApnTypeBitmask(apnTypeBitmask.toInt())
                            builder.setEntryName(entryName)
                            if(VERSION.SDK_INT>=29) { builder.setMmsProxyAddress(mmsProxyAddress) }
                            builder.setMmsProxyPort(mmsProxyPort.toInt())
                            if(VERSION.SDK_INT>=29) { builder.setProxyAddress(proxyAddress) }
                            builder.setProxyPort(proxyPort.toInt())
                            builder.setMmsc(mmsc.toUri())
                            if(VERSION.SDK_INT>=33) { builder.setMtuV4(mtuV4.toInt()); builder.setMtuV6(mtuV6.toInt()) }
                            builder.setMvnoType(mvnoType)
                            builder.setNetworkTypeBitmask(networkTypeBitmask.toInt())
                            builder.setOperatorNumeric(operatorNumeric)
                            builder.setPassword(password)
                            if(VERSION.SDK_INT>=33) { builder.setPersistent(persistent) }
                            builder.setProtocol(protocol)
                            builder.setRoamingProtocol(roamingProtocol)
                            result = builder.build()
                        }
                        finalStep=!finalStep
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(if(finalStep) R.string.previous_step else R.string.next_step))
                }
                AnimatedVisibility(finalStep) {
                    if(inputNum=="0") {
                        Button(
                            onClick = { dpm.addOverrideApn(receiver,result) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.create))
                        }
                    }else{
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Button(
                                onClick = { context.showOperationResultToast(dpm.updateOverrideApn(receiver, id, result)) },
                                modifier = Modifier.fillMaxWidth(0.49F)
                            ) {
                                Text(stringResource(R.string.update))
                            }
                            Button(
                                onClick = { context.showOperationResultToast(dpm.removeOverrideApn(receiver,id)) },
                                modifier = Modifier.fillMaxWidth(0.96F)
                            ) {
                                Text(stringResource(R.string.remove))
                            }
                        }
                    }
                }
            }
        }
    }
}
