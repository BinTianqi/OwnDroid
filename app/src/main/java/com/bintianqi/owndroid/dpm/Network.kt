package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
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
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST
import android.content.ComponentName
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
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Receiver
import com.bintianqi.owndroid.toText
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.TopBar

@Composable
fun Network(navCtrl: NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    val wifiMacDialog = remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopBar(backStackEntry,navCtrl,localNavCtrl) {
                if(backStackEntry?.destination?.route == "Home" && scrollState.maxValue > 80) {
                    Text(
                        text = stringResource(R.string.network),
                        modifier = Modifier.alpha((maxOf(scrollState.value-30,0)).toFloat()/80)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition,
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            composable(route = "Home") { Home(localNavCtrl, scrollState, wifiMacDialog) }
            composable(route = "Switches") { Switches() }
            composable(route = "MinWifiSecurityLevel") { WifiSecLevel() }
            composable(route = "WifiSsidPolicy") { WifiSsidPolicy() }
            composable(route = "PrivateDNS") { PrivateDNS() }
            composable(route = "NetworkLog") { NetworkLog() }
            composable(route = "WifiAuthKeypair") { WifiAuthKeypair() }
            composable(route = "APN") { APN() }
        }
    }
    if(wifiMacDialog.value && VERSION.SDK_INT >= 24) {
        val context = LocalContext.current
        val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val receiver = ComponentName(context, Receiver::class.java)
        AlertDialog(
            onDismissRequest = { wifiMacDialog.value = false },
            confirmButton = { TextButton(onClick = { wifiMacDialog.value = false }) { Text(stringResource(R.string.confirm)) } },
            title = { Text(stringResource(R.string.wifi_mac_addr)) },
            text = { SelectionContainer { Text(dpm.getWifiMacAddress(receiver)?: stringResource(R.string.none)) } },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun Home(navCtrl:NavHostController, scrollState: ScrollState, wifiMacDialog: MutableState<Boolean>) {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context, Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        Text(
            text = stringResource(R.string.network),
            style = typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 15.dp)
        )
        if(VERSION.SDK_INT >= 24 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.wifi_mac_addr, "", R.drawable.wifi_fill0) { wifiMacDialog.value = true }
        }
        if(VERSION.SDK_INT >= 30) {
            SubPageItem(R.string.options, "", R.drawable.tune_fill0) { navCtrl.navigate("Switches") }
        }
        if(VERSION.SDK_INT >= 33 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.min_wifi_security_level, "", R.drawable.wifi_password_fill0) { navCtrl.navigate("MinWifiSecurityLevel") }
        }
        if(VERSION.SDK_INT >= 33 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SubPageItem(R.string.wifi_ssid_policy, "", R.drawable.wifi_fill0) { navCtrl.navigate("WifiSsidPolicy") }
        }
        if(VERSION.SDK_INT >= 29 && isDeviceOwner(dpm)) {
            SubPageItem(R.string.private_dns, "", R.drawable.dns_fill0) { navCtrl.navigate("PrivateDNS") }
        }
        if(VERSION.SDK_INT >= 26&&(isDeviceOwner(dpm) || (isProfileOwner(dpm) && dpm.isManagedProfile(receiver)))) {
            SubPageItem(R.string.retrieve_net_logs, "", R.drawable.description_fill0) { navCtrl.navigate("NetworkLog") }
        }
        if(VERSION.SDK_INT >= 31 && (isDeviceOwner(dpm) || isProfileOwner(dpm))) {
            SubPageItem(R.string.wifi_auth_keypair, "", R.drawable.key_fill0) { navCtrl.navigate("WifiAuthKeypair") }
        }
        if(VERSION.SDK_INT >= 28 && isDeviceOwner(dpm)) {
            SubPageItem(R.string.apn_settings, "", R.drawable.cell_tower_fill0) { navCtrl.navigate("APN") }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Switches() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 33 && isDeviceOwner(dpm)) {
            SwitchItem(
                R.string.preferential_network_service, stringResource(R.string.developing), R.drawable.globe_fill0,
                { dpm.isPreferentialNetworkServiceEnabled }, { dpm.isPreferentialNetworkServiceEnabled = it }
            )
        }
        if(VERSION.SDK_INT>=30 && (isDeviceOwner(dpm) || dpm.isOrgProfile(receiver))) {
            SwitchItem(R.string.lockdown_admin_configured_network, "", R.drawable.wifi_password_fill0,
                { dpm.hasLockdownAdminConfiguredNetworks(receiver) }, { dpm.setConfiguredNetworksLockdownState(receiver,it) }
            )
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun WifiSecLevel() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    var selectedWifiSecLevel by remember { mutableIntStateOf(0) }
    LaunchedEffect(Unit) { selectedWifiSecLevel = dpm.minimumRequiredWifiSecurityLevel }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.min_wifi_security_level), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(
            stringResource(R.string.wifi_security_level_open),
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
    }
}

@SuppressLint("NewApi")
@Composable
private fun WifiSsidPolicy() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        var selectedPolicyType by remember { mutableIntStateOf(-1) }
        val ssidList = remember { mutableStateListOf<WifiSsid>() }
        val refreshPolicy = {
            val policy = dpm.wifiSsidPolicy
            ssidList.clear()
            selectedPolicyType = policy?.policyType ?: -1
            (policy?.ssids ?: mutableSetOf()).forEach { ssidList.add(it) }
        }
        LaunchedEffect(Unit) { refreshPolicy() }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.wifi_ssid_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(
            stringResource(R.string.none),
            selectedPolicyType == -1,
            { selectedPolicyType = -1 }
        )
        RadioButtonItem(
            stringResource(R.string.whitelist),
            selectedPolicyType == WIFI_SSID_POLICY_TYPE_ALLOWLIST,
            { selectedPolicyType = WIFI_SSID_POLICY_TYPE_ALLOWLIST }
        )
        RadioButtonItem(
            stringResource(R.string.blacklist),
            selectedPolicyType == WIFI_SSID_POLICY_TYPE_DENYLIST,
            { selectedPolicyType = WIFI_SSID_POLICY_TYPE_DENYLIST }
        )
        AnimatedVisibility(selectedPolicyType != -1) {
            var inputSsid by remember { mutableStateOf("") }
            Column {
                Column {
                    Spacer(Modifier.padding(vertical = 5.dp))
                    Text(stringResource(R.string.ssid_list_is))
                    SelectionContainer(modifier = Modifier.animateContentSize().horizontalScroll(rememberScrollState())) {
                        Text(if(ssidList.isEmpty()) stringResource(R.string.none) else ssidList.toText())
                    }
                }
                Spacer(Modifier.padding(vertical = 5.dp))
                OutlinedTextField(
                    value = inputSsid,
                    label = { Text("SSID") },
                    onValueChange = {inputSsid = it },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.padding(vertical = 5.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = {
                            if(inputSsid == "") {
                                Toast.makeText(context, R.string.cannot_be_empty, Toast.LENGTH_SHORT).show()
                            } else if (WifiSsid.fromBytes(inputSsid.toByteArray()) in ssidList) {
                                Toast.makeText(context, R.string.already_exist, Toast.LENGTH_SHORT).show()
                            } else {
                                ssidList.add(WifiSsid.fromBytes(inputSsid.toByteArray()))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text(stringResource(R.string.add))
                    }
                    Button(
                        onClick = {
                            if(inputSsid == "") {
                                Toast.makeText(context, R.string.cannot_be_empty, Toast.LENGTH_SHORT).show()
                            } else if (WifiSsid.fromBytes(inputSsid.toByteArray()) in ssidList) {
                                ssidList.remove(WifiSsid.fromBytes(inputSsid.toByteArray()))
                            } else {
                                Toast.makeText(context, R.string.not_exist, Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text(stringResource(R.string.remove))
                    }
                }
                Spacer(Modifier.padding(vertical = 10.dp))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                if(selectedPolicyType == -1) {
                    dpm.wifiSsidPolicy = null
                    refreshPolicy()
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }else{
                    dpm.wifiSsidPolicy = if(ssidList.isEmpty()) { null }else{ WifiSsidPolicy(selectedPolicyType, ssidList.toSet()) }
                    refreshPolicy()
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun PrivateDNS() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.private_dns), style = typography.headlineLarge)
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
                Text(stringResource(R.string.set_to_auto))
            }
        }
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
                } catch(e:IllegalArgumentException) {
                    Toast.makeText(context, R.string.invalid_hostname, Toast.LENGTH_SHORT).show()
                } catch(e:SecurityException) {
                    Toast.makeText(context, R.string.security_exception, Toast.LENGTH_SHORT).show()
                } finally {
                    status = dnsStatus[dpm.getGlobalPrivateDnsMode(receiver)]
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.set_dns_host))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun NetworkLog() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.retrieve_net_logs), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.developing))
        Spacer(Modifier.padding(vertical = 5.dp))
        SwitchItem(R.string.enable,"",null, {dpm.isNetworkLoggingEnabled(receiver) }, {dpm.setNetworkLoggingEnabled(receiver,it) })
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                val log = dpm.retrieveNetworkLogs(receiver,1234567890)
                if(log != null) {
                    for(i in log) { Log.d("NetworkLog",i.toString()) }
                    Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("NetworkLog",context.getString(R.string.none))
                    Toast.makeText(context, R.string.none, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.retrieve))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun WifiAuthKeypair() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        var keyPair by remember { mutableStateOf("") }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.wifi_auth_keypair), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = keyPair,
            label = { Text(stringResource(R.string.keypair)) },
            onValueChange = { keyPair = it },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        val isExist = try{ dpm.isKeyPairGrantedToWifiAuth(keyPair) }catch(e:java.lang.IllegalArgumentException) { false }
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
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    val result = dpm.revokeKeyPairFromWifiAuth(keyPair)
                    Toast.makeText(context, if(result) R.string.success else R.string.failed, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun APN() {
    val context = LocalContext.current
    val dpm = context.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val receiver = ComponentName(context,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        val setting = dpm.getOverrideApns(receiver)
        var inputNum by remember { mutableStateOf("0") }
        var nextStep by remember { mutableStateOf(false) }
        val builder = Builder()
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.apn_settings), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(id = R.string.developing))
        Spacer(Modifier.padding(vertical = 5.dp))
        SwitchItem(R.string.enable, "", null, { dpm.isOverrideApnEnabled(receiver) }, { dpm.setOverrideApnsEnabled(receiver,it) })
        Text(text = stringResource(R.string.total_apn_amount, setting.size))
        if(setting.size>0) {
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
                RadioButtonItem(stringResource(R.string.none), selectedAuthType==AUTH_TYPE_NONE , { selectedAuthType=AUTH_TYPE_NONE })
                RadioButtonItem("CHAP", selectedAuthType == AUTH_TYPE_CHAP , { selectedAuthType = AUTH_TYPE_CHAP })
                RadioButtonItem("PAP", selectedAuthType == AUTH_TYPE_PAP, { selectedAuthType = AUTH_TYPE_PAP })
                RadioButtonItem("PAP/CHAP", selectedAuthType == AUTH_TYPE_PAP_OR_CHAP, { selectedAuthType = AUTH_TYPE_PAP_OR_CHAP })
                
                if(VERSION.SDK_INT>=29) {
                    val ts = context.getSystemService(ComponentActivity.TELEPHONY_SERVICE) as TelephonyManager
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
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}
