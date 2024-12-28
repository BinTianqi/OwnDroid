package com.bintianqi.owndroid.dpm

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
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.IpConfiguration
import android.net.LinkAddress
import android.net.ProxyInfo
import android.net.StaticIpConfiguration
import android.net.Uri
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiSsid
import android.os.Build.VERSION
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
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.exportFile
import com.bintianqi.owndroid.exportFilePath
import com.bintianqi.owndroid.formatFileSize
import com.bintianqi.owndroid.isExportingSecurityOrNetworkLogs
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoCard
import com.bintianqi.owndroid.ui.ListItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.UpOrDownTextFieldTrailingIconButton
import com.bintianqi.owndroid.writeClipBoard
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
    var wifiMacDialog by remember { mutableStateOf(false) }
    MyScaffold(R.string.network, 0.dp, navCtrl) {
        if(VERSION.SDK_INT >= 24 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.wifi_mac_address, "", R.drawable.wifi_fill0) { wifiMacDialog = true }
        }
        if(VERSION.SDK_INT >= 30) {
            FunctionItem(R.string.options, "", R.drawable.tune_fill0) { navCtrl.navigate("NetworkOptions") }
        }
        FunctionItem(R.string.add_wifi, "", R.drawable.wifi_add_fill0) { navCtrl.navigate("AddWifi") }
        if(VERSION.SDK_INT >= 33 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.min_wifi_security_level, "", R.drawable.wifi_password_fill0) { navCtrl.navigate("MinWifiSecurityLevel") }
        }
        if(VERSION.SDK_INT >= 33 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            FunctionItem(R.string.wifi_ssid_policy, "", R.drawable.wifi_fill0) { navCtrl.navigate("WifiSsidPolicy") }
        }
        if(VERSION.SDK_INT >= 29 && deviceOwner) {
            FunctionItem(R.string.private_dns, "", R.drawable.dns_fill0) { navCtrl.navigate("PrivateDNS") }
        }
        if(VERSION.SDK_INT >= 24 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.always_on_vpn, "", R.drawable.vpn_key_fill0) { navCtrl.navigate("AlwaysOnVpn") }
        }
        if(deviceOwner) {
            FunctionItem(R.string.recommended_global_proxy, "", R.drawable.vpn_key_fill0) { navCtrl.navigate("RecommendedGlobalProxy") }
        }
        if(VERSION.SDK_INT >= 26 && !dhizuku && (deviceOwner || (profileOwner && dpm.isManagedProfile(receiver)))) {
            FunctionItem(R.string.network_logging, "", R.drawable.description_fill0) { navCtrl.navigate("NetworkLog") }
        }
        if(VERSION.SDK_INT >= 31 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.wifi_auth_keypair, "", R.drawable.key_fill0) { navCtrl.navigate("WifiAuthKeypair") }
        }
        if(VERSION.SDK_INT >= 33 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.preferential_network_service, "", R.drawable.globe_fill0) { navCtrl.navigate("PreferentialNetworkService") }
        }
        if(VERSION.SDK_INT >= 28 && deviceOwner) {
            FunctionItem(R.string.override_apn_settings, "", R.drawable.cell_tower_fill0) { navCtrl.navigate("OverrideAPN") }
        }
    }
    if(wifiMacDialog && VERSION.SDK_INT >= 24) {
        val context = LocalContext.current
        val dpm = context.getDPM()
        val receiver = context.getReceiver()
        AlertDialog(
            onDismissRequest = { wifiMacDialog = false },
            confirmButton = { TextButton(onClick = { wifiMacDialog = false }) { Text(stringResource(R.string.confirm)) } },
            title = { Text(stringResource(R.string.wifi_mac_address)) },
            text = {
                val mac = dpm.getWifiMacAddress(receiver)
                OutlinedTextField(
                    value = mac ?: stringResource(R.string.none),
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

@Composable
fun NetworkOptions(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.options, 0.dp, navCtrl) {
        if(VERSION.SDK_INT>=30 && (deviceOwner || dpm.isOrgProfile(receiver))) {
            SwitchItem(R.string.lockdown_admin_configured_network, "", R.drawable.wifi_password_fill0,
                { dpm.hasLockdownAdminConfiguredNetworks(receiver) }, { dpm.setConfiguredNetworksLockdownState(receiver,it) },
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

@Suppress("DEPRECATION")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNetwork(navCtrl: NavHostController) {
    val context = LocalContext.current
    var resultDialog by remember { mutableStateOf(false) }
    var createdNetworkId by remember { mutableIntStateOf(-1) }
    var createNetworkResult by remember {mutableIntStateOf(0)}
    var dropdownMenu by remember { mutableIntStateOf(0) } // 0: None, 1:Status, 2:Security, 3:MAC randomization, 4:Static IP, 5:Proxy
    var networkId by remember { mutableStateOf("") }
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
    MyScaffold(R.string.add_wifi, 8.dp, navCtrl) {
        OutlinedTextField(
            value = networkId, onValueChange = { networkId = it }, label = { Text(stringResource(R.string.network_id)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            isError = networkId != "" && (try { networkId.toInt(); false } catch(_: NumberFormatException) { true }),
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
        )
        ExposedDropdownMenuBox(dropdownMenu == 1, { dropdownMenu = if(it) 1 else 0 }) {
            val statusText = when(status) {
                WifiConfiguration.Status.CURRENT -> R.string.current
                WifiConfiguration.Status.DISABLED -> R.string.disabled
                WifiConfiguration.Status.ENABLED -> R.string.enabled
                else -> R.string.place_holder
            }
            OutlinedTextField(
                value = stringResource(statusText), onValueChange = {}, readOnly = true,
                label = { Text(stringResource(R.string.status)) },
                trailingIcon = { UpOrDownTextFieldTrailingIconButton(dropdownMenu == 1) {} },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth().padding(bottom = 16.dp)
            )
            ExposedDropdownMenu(dropdownMenu == 1, { dropdownMenu = 0 }) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.current)) },
                    onClick = {
                        status = WifiConfiguration.Status.CURRENT
                        dropdownMenu = 0
                    }
                )
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
        CheckBoxItem(R.string.hidden_ssid, hiddenSsid, { hiddenSsid = it })
        if(VERSION.SDK_INT >= 30) {
            // TODO: more protocols
            val securityTypeTextMap = mutableMapOf(
                WifiConfiguration.SECURITY_TYPE_OPEN to stringResource(R.string.wifi_security_open),
                WifiConfiguration.SECURITY_TYPE_PSK to "PSK"
            )
            ExposedDropdownMenuBox(dropdownMenu == 2, { dropdownMenu = if(it) 2 else 0 }) {
                OutlinedTextField(
                    value = securityTypeTextMap[securityType] ?: "", onValueChange = {}, label = { Text(stringResource(R.string.security)) },
                    trailingIcon = { UpOrDownTextFieldTrailingIconButton(dropdownMenu == 1) {} }, readOnly = true,
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
                    trailingIcon = { UpOrDownTextFieldTrailingIconButton(dropdownMenu == 3) {} },
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
                    trailingIcon = { UpOrDownTextFieldTrailingIconButton(dropdownMenu == 4) {} },
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
                    trailingIcon = { UpOrDownTextFieldTrailingIconButton(dropdownMenu == 5) {} },
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
                    if(networkId != "") config.networkId = networkId.toInt()
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
                    if(VERSION.SDK_INT >= 31) {
                        val result = wm.addNetworkPrivileged(config)
                        createdNetworkId = result.networkId
                        createNetworkResult = result.statusCode
                    } else {
                        createdNetworkId = wm.addNetwork(config)
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
            Text(stringResource(R.string.add))
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
                TextButton(onClick = { resultDialog = false }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { resultDialog = false }
        )
    }
}

@SuppressLint("NewApi")
@Composable
fun WifiSecurityLevel(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    var selectedWifiSecLevel by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { selectedWifiSecLevel = dpm.minimumRequiredWifiSecurityLevel }
    MyScaffold(R.string.min_wifi_security_level, 8.dp, navCtrl) {
        RadioButtonItem(
            R.string.wifi_security_open,
            selectedWifiSecLevel == WIFI_SECURITY_OPEN,
            { selectedWifiSecLevel = WIFI_SECURITY_OPEN }
        )
        RadioButtonItem(
            "WEP, WPA(2)-PSK",
            selectedWifiSecLevel == WIFI_SECURITY_PERSONAL,
            { selectedWifiSecLevel = WIFI_SECURITY_PERSONAL }
        )
        RadioButtonItem(
            "WPA-EAP",
            selectedWifiSecLevel == WIFI_SECURITY_ENTERPRISE_EAP,
            { selectedWifiSecLevel = WIFI_SECURITY_ENTERPRISE_EAP }
        )
        RadioButtonItem(
            "WPA3-192bit",
            selectedWifiSecLevel == WIFI_SECURITY_ENTERPRISE_192,
            { selectedWifiSecLevel = WIFI_SECURITY_ENTERPRISE_192 }
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.minimumRequiredWifiSecurityLevel = selectedWifiSecLevel
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_minimum_wifi_security_level)
    }
}

@SuppressLint("NewApi")
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
        RadioButtonItem(
            R.string.none,
            selectedPolicyType == -1,
            { selectedPolicyType = -1 }
        )
        RadioButtonItem(
            R.string.whitelist,
            selectedPolicyType == WIFI_SSID_POLICY_TYPE_ALLOWLIST,
            { selectedPolicyType = WIFI_SSID_POLICY_TYPE_ALLOWLIST }
        )
        RadioButtonItem(
            R.string.blacklist,
            selectedPolicyType == WIFI_SSID_POLICY_TYPE_DENYLIST,
            { selectedPolicyType = WIFI_SSID_POLICY_TYPE_DENYLIST }
        )
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
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
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

@SuppressLint("NewApi")
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
            Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
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
        SwitchItem(R.string.enable_lockdown, "", null, lockdown, { lockdown = it }, padding = false)
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
        RadioButtonItem(R.string.proxy_type_off, proxyType == 0, { proxyType = 0 })
        RadioButtonItem(R.string.proxy_type_pac, proxyType == 1, { proxyType = 1 })
        RadioButtonItem(R.string.proxy_type_direct, proxyType == 2, { proxyType = 2 })
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
                CheckBoxItem(R.string.specify_port, specifyPort, { specifyPort = it })
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
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
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
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        InfoCard(R.string.info_recommended_global_proxy)
    }
}

@SuppressLint("NewApi")
@Composable
fun NetworkLogging(navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val logFile = context.filesDir.resolve("NetworkLogs.json")
    var fileSize by remember { mutableLongStateOf(0) }
    LaunchedEffect(Unit) {
        fileSize = logFile.length()
    }
    MyScaffold(R.string.network_logging, 8.dp, navCtrl) {
        SwitchItem(R.string.enable, "", null, { dpm.isNetworkLoggingEnabled(receiver) }, { dpm.setNetworkLoggingEnabled(receiver,it) }, padding = false)
        Text(stringResource(R.string.log_file_size_is, formatFileSize(fileSize)))
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    intent.addCategory(Intent.CATEGORY_OPENABLE)
                    intent.setType("application/json")
                    intent.putExtra(Intent.EXTRA_TITLE, "NetworkLogs.json")
                    exportFilePath = logFile.path
                    isExportingSecurityOrNetworkLogs = true
                    exportFile.launch(intent)
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

@SuppressLint("NewApi")
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
                onClick = {
                    val result = dpm.grantKeyPairToWifiAuth(keyPair)
                    Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.grant))
            }
            Button(
                onClick = {
                    val result = dpm.revokeKeyPairFromWifiAuth(keyPair)
                    Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.revoke))
            }
        }
    }
}

@SuppressLint("NewApi")
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
        SwitchItem(
            title = R.string.enabled, desc = "", icon = null,
            state = masterEnabled, onCheckedChange = { masterEnabled = it }, padding = false
        )
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
                        Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
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
        SwitchItem(
            title = R.string.enabled, desc = "", icon = null,
            state = enabled, onCheckedChange = { enabled = it }, padding = false
        )
        OutlinedTextField(
            value = networkId, onValueChange = { networkId = it },
            label = { Text(stringResource(R.string.network_id)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { focusMgr.clearFocus() },
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp)
        )
        SwitchItem(
            title = R.string.allow_fallback_to_default_connection, desc = "", icon = null,
            state = allowFallback, onCheckedChange = { allowFallback = it }, padding = false
        )
        if(VERSION.SDK_INT >= 34) SwitchItem(
            title = R.string.block_non_matching_networks, desc = "", icon = null,
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
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
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
        SwitchItem(R.string.enable, "", null, { dpm.isOverrideApnEnabled(receiver) }, { dpm.setOverrideApnsEnabled(receiver,it) }, padding = false)
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
                RadioButtonItem(R.string.none, selectedAuthType==AUTH_TYPE_NONE , { selectedAuthType=AUTH_TYPE_NONE })
                RadioButtonItem("CHAP", selectedAuthType == AUTH_TYPE_CHAP , { selectedAuthType = AUTH_TYPE_CHAP })
                RadioButtonItem("PAP", selectedAuthType == AUTH_TYPE_PAP, { selectedAuthType = AUTH_TYPE_PAP })
                RadioButtonItem("PAP/CHAP", selectedAuthType == AUTH_TYPE_PAP_OR_CHAP, { selectedAuthType = AUTH_TYPE_PAP_OR_CHAP })
                
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
                RadioButtonItem("SPN", mvnoType == MVNO_TYPE_SPN, { mvnoType = MVNO_TYPE_SPN })
                RadioButtonItem("IMSI", mvnoType == MVNO_TYPE_IMSI, { mvnoType = MVNO_TYPE_IMSI })
                RadioButtonItem("GID", mvnoType == MVNO_TYPE_GID, { mvnoType = MVNO_TYPE_GID })
                RadioButtonItem("ICCID", mvnoType == MVNO_TYPE_ICCID, { mvnoType = MVNO_TYPE_ICCID })
                
                Text(text = stringResource(R.string.network_type), style = typography.titleLarge)
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
                RadioButtonItem("IPV4", protocol == PROTOCOL_IP, { protocol = PROTOCOL_IP })
                RadioButtonItem("IPV6", protocol == PROTOCOL_IPV6, { protocol = PROTOCOL_IPV6 })
                RadioButtonItem("IPV4/IPV6", protocol == PROTOCOL_IPV4V6, { protocol = PROTOCOL_IPV4V6 })
                RadioButtonItem("PPP", protocol == PROTOCOL_PPP, { protocol = PROTOCOL_PPP })
                if(VERSION.SDK_INT>=29) {
                    RadioButtonItem("non-IP", protocol == PROTOCOL_NON_IP, { protocol = PROTOCOL_NON_IP })
                    RadioButtonItem("Unstructured", protocol == PROTOCOL_UNSTRUCTURED, { protocol = PROTOCOL_UNSTRUCTURED })
                }
                
                Text(text = stringResource(R.string.roaming_protocol), style = typography.titleLarge)
                RadioButtonItem("IPV4", roamingProtocol == PROTOCOL_IP, { roamingProtocol = PROTOCOL_IP })
                RadioButtonItem("IPV6", roamingProtocol == PROTOCOL_IPV6, { roamingProtocol = PROTOCOL_IPV6 })
                RadioButtonItem("IPV4/IPV6", roamingProtocol == PROTOCOL_IPV4V6, { roamingProtocol = PROTOCOL_IPV4V6 })
                RadioButtonItem("PPP", roamingProtocol == PROTOCOL_PPP, { roamingProtocol = PROTOCOL_PPP})
                if(VERSION.SDK_INT>=29) {
                    RadioButtonItem("non-IP", roamingProtocol == PROTOCOL_NON_IP, { roamingProtocol = PROTOCOL_NON_IP })
                    RadioButtonItem("Unstructured", roamingProtocol == PROTOCOL_UNSTRUCTURED, { roamingProtocol = PROTOCOL_UNSTRUCTURED })
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
                                onClick = {
                                    val success = dpm.updateOverrideApn(receiver,id,result)
                                    Toast.makeText(context, if(success) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                                },
                                Modifier.fillMaxWidth(0.49F)
                            ) {
                                Text(stringResource(R.string.update))
                            }
                            Button(
                                onClick = {
                                    val success = dpm.removeOverrideApn(receiver,id)
                                    Toast.makeText(context, if(success) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                                },
                                Modifier.fillMaxWidth(0.96F)
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
