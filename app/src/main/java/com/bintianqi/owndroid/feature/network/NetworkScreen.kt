package com.bintianqi.owndroid.feature.network

import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.PackageNameTextField
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.HorizontalPadding
import kotlinx.coroutines.channels.Channel

@Composable
fun NetworkScreen(
    vm: NetworkViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.ps.collectAsStateWithLifecycle()
    MyScaffold(R.string.network, onNavigateUp, 0.dp) {
        if (!privilege.dhizuku) FunctionItem(R.string.wifi, icon = R.drawable.wifi_fill0) {
            onNavigate(Destination.WiFi)
        }
        if (VERSION.SDK_INT >= 30) {
            FunctionItem(R.string.options, icon = R.drawable.tune_fill0) {
                onNavigate(Destination.NetworkOptions)
            }
        }
        if (!privilege.dhizuku)
            FunctionItem(R.string.network_stats, icon = R.drawable.query_stats_fill0) {
                onNavigate(Destination.NetworkStats)
            }
        if (VERSION.SDK_INT >= 29 && privilege.device) {
            FunctionItem(R.string.private_dns, icon = R.drawable.dns_fill0) {
                onNavigate(Destination.PrivateDns)
            }
        }
        if (VERSION.SDK_INT >= 24) {
            FunctionItem(R.string.always_on_vpn, icon = R.drawable.vpn_key_fill0) {
                onNavigate(Destination.AlwaysOnVpnPackage)
            }
        }
        if (privilege.device) {
            FunctionItem(R.string.recommended_global_proxy, icon = R.drawable.vpn_key_fill0) {
                onNavigate(Destination.RecommendedGlobalProxy)
            }
        }
        if (VERSION.SDK_INT >= 26 && !privilege.dhizuku && (privilege.device || privilege.work)) {
            FunctionItem(R.string.network_logging, icon = R.drawable.description_fill0) {
                onNavigate(Destination.NetworkLogging)
            }
        }
        /*if(VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.wifi_auth_keypair, icon = R.drawable.key_fill0) { onNavigate(Destination.WifiAuthKeypair) }
        }*/
        if (VERSION.SDK_INT >= 33 && (privilege.work || privilege.device)) {
            FunctionItem(R.string.preferential_network_service, icon = R.drawable.globe_fill0) {
                onNavigate(Destination.PreferentialNetworkService)
            }
        }
        if (VERSION.SDK_INT >= 28 && privilege.device) {
            FunctionItem(R.string.override_apn, icon = R.drawable.cell_tower_fill0) {
                onNavigate(Destination.OverrideApn)
            }
        }
    }
}

@Composable
fun NetworkOptionsScreen(
    vm: NetworkViewModel, onNavigateUp: () -> Unit
) {
    val privilege by vm.ps.collectAsState()
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 30 && (privilege.device || privilege.org)) {
            val lanEnabled by vm.lanEnabledState.collectAsState()
            LaunchedEffect(Unit) {
                vm.getLanEnabled()
            }
            SwitchItem(
                R.string.lockdown_admin_configured_network, lanEnabled, vm::setLanEnabled,
                R.drawable.wifi_password_fill0
            )
            Notes(R.string.info_lockdown_admin_configured_network, HorizontalPadding)
        }
    }
}

@RequiresApi(29)
@Composable
fun PrivateDnsScreen(
    vm: NetworkViewModel, onNavigateUp: () -> Unit
) {
    val mode by vm.privateDnsModeState.collectAsState()
    val host by vm.privateDnsHostState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getPrivateDnsConf()
    }
    MyScaffold(R.string.private_dns, onNavigateUp, 0.dp) {
        PrivateDnsMode.entries.forEach {
            FullWidthRadioButtonItem(it.text, mode == it) { vm.setPrivateDnsMode(it) }
        }
        if (mode == PrivateDnsMode.Host) {
            OutlinedTextField(
                host, vm::setPrivateDnsHost,
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 4.dp),
                label = { Text(stringResource(R.string.dns_hostname)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
        Button(
            vm::applyPrivateDnsConf,
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding),
            mode != null
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@RequiresApi(24)
@Composable
fun AlwaysOnVpnPackageScreen(
    vm: NetworkViewModel, chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val lockdown by vm.alwaysOnVpnLockdownState.collectAsState()
    val pkgName by vm.alwaysOnVpnPackageState.collectAsState()
    var initialized by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!initialized) {
            if (VERSION.SDK_INT >= 29) vm.getAlwaysOnVpnLockdown()
            vm.getAlwaysOnVpnPackage()
        }
        vm.setAlwaysOnVpnPackage(chosenPackage.receive())
    }
    MyScaffold(R.string.always_on_vpn, onNavigateUp, 0.dp) {
        PackageNameTextField(
            pkgName, onChoosePackage,
            Modifier.padding(HorizontalPadding, 4.dp),
            vm::setAlwaysOnVpnPackage
        )
        SwitchItem(R.string.enable_lockdown, lockdown, vm::setAlwaysOnVpnLockdown)
        Button(
            vm::applyAlwaysOnVpn,
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.apply))
        }
        Button(
            vm::clearAlwaysOnVpnConfig,
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 5.dp)
        ) {
            Text(stringResource(R.string.clear_current_config))
        }
        Notes(R.string.info_always_on_vpn, HorizontalPadding)
    }
}

@Composable
fun RecommendedGlobalProxyScreen(
    vm: NetworkViewModel, onNavigateUp: () -> Unit
) {
    var type by rememberSaveable { mutableStateOf(ProxyType.Off) }
    var pacUrl by rememberSaveable { mutableStateOf("") }
    var specifyPort by rememberSaveable { mutableStateOf(false) }
    var host by rememberSaveable { mutableStateOf("") }
    var port by rememberSaveable { mutableStateOf("") }
    var exclList by rememberSaveable { mutableStateOf("") }
    MyScaffold(R.string.recommended_global_proxy, onNavigateUp, 0.dp) {
        ProxyType.entries.forEach {
            FullWidthRadioButtonItem(it.text, type == it) { type = it }
        }
        AnimatedVisibility(type == ProxyType.Pac) {
            OutlinedTextField(
                pacUrl, { pacUrl = it },
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 4.dp),
                label = { Text("URL") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
        AnimatedVisibility(type == ProxyType.Direct) {
            OutlinedTextField(
                host, { host = it },
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 4.dp),
                label = { Text("Host") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
        AnimatedVisibility(type == ProxyType.Pac && VERSION.SDK_INT >= 30) {
            FullWidthCheckBoxItem(R.string.specify_port, specifyPort) { specifyPort = it }
        }
        AnimatedVisibility((specifyPort && VERSION.SDK_INT >= 30) || type == ProxyType.Direct) {
            OutlinedTextField(
                port, { port = it },
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 4.dp),
                label = { Text(stringResource(R.string.port)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                )
            )
        }
        AnimatedVisibility(type == ProxyType.Direct) {
            OutlinedTextField(
                exclList, { exclList = it },
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 4.dp),
                label = { Text(stringResource(R.string.excluded_hosts)) },
                maxLines = 5,
                minLines = 2,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
            )
        }
        Button(
            {
                vm.setRecommendedGlobalProxy(
                    RecommendedProxyConf(
                        type, pacUrl, host, specifyPort, port.toIntOrNull() ?: 0,
                        exclList.lines().filter { it.isNotBlank() }
                    ))
            },
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 4.dp),
            enabled = type == ProxyType.Off ||
                    (type == ProxyType.Pac && pacUrl.isNotBlank() &&
                            (!specifyPort || port.toIntOrNull() != null)) ||
                    (type == ProxyType.Direct && port.toIntOrNull() != null)
        ) {
            Text(stringResource(R.string.apply))
        }
        Notes(R.string.info_recommended_global_proxy, HorizontalPadding)
    }
}
