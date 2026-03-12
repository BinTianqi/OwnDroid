package com.bintianqi.owndroid.feature.network

import android.os.Build.VERSION
import android.provider.Telephony
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.utils.clickableTextField

@RequiresApi(28)
@Composable
fun OverrideApnScreen(
    vm: OverrideApnViewModel, onNavigateUp: () -> Unit, onNavigateToAddSetting: () -> Unit
) {
    val enabled by vm.enabledState.collectAsState()
    val configs by vm.configsState.collectAsState()
    LaunchedEffect(Unit) { vm.getConfigs() }
    MyScaffold(R.string.override_apn, onNavigateUp, 0.dp) {
        SwitchItem(R.string.enable, enabled, vm::setEnabled)
        configs.forEach {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 8.dp, 8.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row {
                    Text(it.id.toString(), Modifier.padding(end = 8.dp))
                    Column {
                        Text(it.name)
                        Text(
                            it.apn, Modifier.alpha(0.7F),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                IconButton({
                    vm.selectedConfig = it
                    onNavigateToAddSetting()
                }) {
                    Icon(Icons.Outlined.Edit, null)
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .clickable {
                    vm.selectedConfig = null
                    onNavigateToAddSetting()
                }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, Modifier.padding(horizontal = 8.dp))
            Text(stringResource(R.string.add_config), style = MaterialTheme.typography.labelLarge)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun AddApnSettingScreen(
    vm: OverrideApnViewModel, onNavigateUp: () -> Unit
) {
    val origin = vm.selectedConfig
    var menu by remember { mutableStateOf(ApnMenu.None) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var entryName by rememberSaveable { mutableStateOf(origin?.name ?: "") }
    var apnName by rememberSaveable { mutableStateOf(origin?.apn ?: "") }
    var apnType by rememberSaveable { mutableIntStateOf(origin?.apnType ?: 0) }
    var profileId by rememberSaveable { mutableStateOf(origin?.profileId?.toString() ?: "") }
    var carrierId by rememberSaveable { mutableStateOf(origin?.carrierId?.toString() ?: "") }
    var authType by rememberSaveable { mutableIntStateOf(0) }
    var user by rememberSaveable { mutableStateOf(origin?.username ?: "") }
    var password by rememberSaveable { mutableStateOf(origin?.password ?: "") }
    var proxy by rememberSaveable { mutableStateOf(origin?.proxy ?: "") }
    var port by rememberSaveable { mutableStateOf(origin?.port?.toString() ?: "") }
    var mmsProxy by rememberSaveable { mutableStateOf(origin?.mmsProxy ?: "") }
    var mmsPort by rememberSaveable { mutableStateOf(origin?.mmsPort?.toString() ?: "") }
    var mmsc by rememberSaveable { mutableStateOf(origin?.mmsc ?: "") }
    var mtuV4 by rememberSaveable { mutableStateOf(origin?.mtuV4?.toString() ?: "") }
    var mtuV6 by rememberSaveable { mutableStateOf(origin?.mtuV6?.toString() ?: "") }
    var mvnoType by rememberSaveable { mutableStateOf(origin?.mvno ?: ApnMvnoType.SPN) }
    var networkType by rememberSaveable { mutableIntStateOf(origin?.networkType ?: 0) }
    var operatorNumeric by rememberSaveable { mutableStateOf(origin?.operatorNumeric ?: "") }
    var protocol by rememberSaveable { mutableIntStateOf(origin?.protocol ?: 0) }
    var roamingProtocol by rememberSaveable { mutableIntStateOf(origin?.roamingProtocol ?: 0) }
    var persistent by rememberSaveable { mutableStateOf(origin?.persistent == true) }
    var alwaysOn by rememberSaveable { mutableStateOf(origin?.alwaysOn == true) }
    var errorMessage: String? by rememberSaveable { mutableStateOf(null) }
    MySmallTitleScaffold(R.string.apn_setting, onNavigateUp) {
        SwitchItem(
            R.string.enabled, state = enabled, onCheckedChange = { enabled = it }, padding = false
        )
        OutlinedTextField(
            entryName, { entryName = it }, Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            )
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
        OutlinedTextField(
            apnTypes.filter { apnType and it.id == it.id }.joinToString { it.name }, {},
            Modifier
                .fillMaxWidth()
                .clickableTextField { menu = ApnMenu.ApnType },
            readOnly = true, label = { Text("APN type") }
        )
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
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            )
        )
        ExposedDropdownMenuBox(
            menu == ApnMenu.AuthType, { menu = if (it) ApnMenu.AuthType else ApnMenu.None }
        ) {
            OutlinedTextField(
                apnAuthTypes.find { it.id == authType }!!.text, {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("Authentication type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == ApnMenu.AuthType)
                }
            )
            ExposedDropdownMenu(menu == ApnMenu.AuthType, { menu = ApnMenu.None }) {
                apnAuthTypes.forEach {
                    DropdownMenuItem(
                        { Text(it.text) },
                        {
                            authType = it.id
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
                apnProtocols.find { it.id == protocol }!!.text, {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("APN protocol") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == ApnMenu.Protocol)
                }
            )
            ExposedDropdownMenu(menu == ApnMenu.Protocol, { menu = ApnMenu.None }) {
                apnProtocols.filter { VERSION.SDK_INT >= it.requiresApi }.forEach {
                    DropdownMenuItem(
                        { Text(it.text) },
                        {
                            protocol = it.id
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
                apnProtocols.find { it.id == roamingProtocol }!!.text, {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("APN roaming protocol") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == ApnMenu.RoamingProtocol)
                }
            )
            ExposedDropdownMenu(menu == ApnMenu.RoamingProtocol, { menu = ApnMenu.None }) {
                apnProtocols.filter { VERSION.SDK_INT >= it.requiresApi }.forEach {
                    DropdownMenuItem(
                        { Text(it.text) },
                        {
                            roamingProtocol = it.id
                            menu = ApnMenu.None
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            apnNetworkTypes.filter { networkType and it.id == it.id }.joinToString { it.text }, {},
            Modifier
                .fillMaxWidth()
                .clickableTextField { menu = ApnMenu.NetworkType },
            readOnly = true, label = { Text("Network type") }
        )
        if (VERSION.SDK_INT >= 33) OutlinedTextField(
            profileId, { profileId = it },
            Modifier.fillMaxWidth(),
            label = { Text("Profile id") },
            isError = profileId.isNotEmpty() && profileId.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            )
        )
        if (VERSION.SDK_INT >= 29) OutlinedTextField(
            carrierId, { carrierId = it },
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            label = { Text("Carrier id") },
            isError = carrierId.isNotEmpty() && carrierId.toIntOrNull() == null,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
            )
        )
        if (VERSION.SDK_INT >= 33) Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp), Arrangement.SpaceBetween
        ) {
            OutlinedTextField(
                mtuV4, { mtuV4 = it }, Modifier.fillMaxWidth(0.49F),
                label = { Text("MTU (IPv4)") },
                isError = mtuV4.isNotEmpty() && mtuV4.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                )
            )
            OutlinedTextField(
                mtuV6, { mtuV6 = it }, Modifier.fillMaxWidth(0.96F),
                label = { Text("MTU (IPv6)") },
                isError = mtuV6.isNotEmpty() && mtuV6.toIntOrNull() == null,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                )
            )
        }
        ExposedDropdownMenuBox(
            menu == ApnMenu.MvnoType, { menu = if (it) ApnMenu.MvnoType else ApnMenu.None }
        ) {
            OutlinedTextField(
                mvnoType.text, {},
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                readOnly = true, label = { Text("MVNO type") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == ApnMenu.RoamingProtocol)
                }
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
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
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
        if (VERSION.SDK_INT >= 33) Row(
            Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Text("Persistent")
            Switch(persistent, { persistent = it })
        }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Always on")
            Switch(alwaysOn, { alwaysOn = it })
        }
        Button(
            {
                vm.setConfig(
                    ApnConfig(
                        enabled, entryName, apnName, proxy, port.toIntOrNull(), user, password,
                        apnType,
                        mmsc, mmsProxy, mmsPort.toIntOrNull(), authType, protocol, roamingProtocol,
                        networkType, profileId.toIntOrNull(), carrierId.toIntOrNull(),
                        mtuV4.toIntOrNull(), mtuV6.toIntOrNull(), mvnoType,
                        operatorNumeric, persistent, alwaysOn
                    )
                ) {
                    onNavigateUp()
                }
            },
            Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(stringResource(if (origin != null) R.string.update else R.string.add))
        }
        if (origin != null) Button(
            {
                vm.removeConfig(origin.id) {
                    onNavigateUp()
                }
            },
            Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.onError
            )
        ) {
            Text(stringResource(R.string.delete))
        }
        if (errorMessage != null) AlertDialog(
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
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                apnType = if (checked) apnType and type.id.inv()
                                else apnType or type.id
                            }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked, null)
                        Text(
                            type.name, Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
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
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                            .clickable {
                                networkType = if (checked) networkType and type.id.inv()
                                else networkType or type.id
                            }
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked, null)
                        Text(
                            type.text, Modifier.padding(start = 6.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
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
