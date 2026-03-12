package com.bintianqi.owndroid.feature.network

import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.HorizontalPadding

@RequiresApi(33)
@Composable
fun PreferentialNetworkServiceScreen(
    vm: PreferentialNetworkViewModel,
    onNavigateUp: () -> Unit, onNavigate: (Destination.AddPreferentialNetworkServiceConfig) -> Unit
) {
    val masterEnabled by vm.enabledState.collectAsState()
    val configs by vm.configsState.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        vm.getConfigs()
    }
    MySmallTitleScaffold(R.string.preferential_network_service, onNavigateUp, 0.dp) {
        SwitchItem(R.string.enabled, masterEnabled, vm::setEnabled)
        Spacer(Modifier.padding(vertical = 4.dp))
        configs.forEachIndexed { index, config ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 4.dp, 8.dp, 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(config.id.toString())
                IconButton({
                    vm.selectedConfigIndex = index
                    onNavigate(Destination.AddPreferentialNetworkServiceConfig)
                }) {
                    Icon(Icons.Default.Edit, stringResource(R.string.edit))
                }
            }
        }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .clickable {
                    vm.selectedConfigIndex = -1
                    onNavigate(Destination.AddPreferentialNetworkServiceConfig)
                }
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Add, null, Modifier.padding(horizontal = 8.dp))
            Text(stringResource(R.string.add_config))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(33)
@Composable
fun AddPreferentialNetworkServiceConfigScreen(
    vm: PreferentialNetworkViewModel, onNavigateUp: () -> Unit
) {
    val configList by vm.configsState.collectAsState()
    val origin =
        if (vm.selectedConfigIndex != -1) configList[vm.selectedConfigIndex]
        else PreferentialNetworkServiceInfo()
    val updateMode = origin.id != -1
    var enabled by rememberSaveable { mutableStateOf(origin.enabled) }
    var id by rememberSaveable { mutableIntStateOf(origin.id) }
    var allowFallback by rememberSaveable { mutableStateOf(origin.allowFallback) }
    var blockNonMatching by rememberSaveable { mutableStateOf(origin.blockNonMatching) }
    var excludedUids by rememberSaveable { mutableStateOf(origin.excludedUids.joinToString("\n")) }
    var includedUids by rememberSaveable { mutableStateOf(origin.includedUids.joinToString("\n")) }
    var dropdown by remember { mutableStateOf(false) }
    MySmallTitleScaffold(R.string.preferential_network_service, onNavigateUp, 0.dp) {
        SwitchItem(R.string.enabled, enabled, { enabled = it })
        ExposedDropdownMenuBox(
            dropdown, { dropdown = it }, Modifier.padding(horizontal = HorizontalPadding)
        ) {
            OutlinedTextField(
                if (id == -1) "" else id.toString(), {},
                Modifier
                    .fillMaxWidth()
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                readOnly = true, label = { Text("id") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdown) }
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
            R.string.allow_fallback_to_default_connection,
            allowFallback, { allowFallback = it }
        )
        if (VERSION.SDK_INT >= 34) SwitchItem(
            R.string.block_non_matching_networks, blockNonMatching, { blockNonMatching = it }
        )
        val includedUidsLegal = includedUids.lines().filter { it.isNotBlank() }.let { uid ->
            uid.isEmpty() || (uid.all { it.toIntOrNull() != null } && excludedUids.isBlank())
        }
        OutlinedTextField(
            includedUids, { includedUids = it },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding)
                .padding(bottom = 6.dp),
            minLines = 2,
            label = { Text(stringResource(R.string.included_uids)) },
            supportingText = { Text(stringResource(R.string.one_uid_per_line)) },
            isError = !includedUidsLegal
        )
        val excludedUidsLegal = excludedUids.lines().filter { it.isNotBlank() }.let { uid ->
            uid.isEmpty() || (uid.all { it.toIntOrNull() != null } && includedUids.isBlank())
        }
        OutlinedTextField(
            excludedUids, { excludedUids = it },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding)
                .padding(bottom = 6.dp),
            minLines = 2,
            label = { Text(stringResource(R.string.excluded_uids)) },
            supportingText = { Text(stringResource(R.string.one_uid_per_line)) },
            isError = !excludedUidsLegal
        )
        Button(
            {
                vm.setConfig(
                    PreferentialNetworkServiceInfo(
                        enabled, id, allowFallback, blockNonMatching,
                        excludedUids.lines().mapNotNull { it.toIntOrNull() },
                        includedUids.lines().mapNotNull { it.toIntOrNull() }
                    ), true)
                onNavigateUp()
            },
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 4.dp),
            includedUidsLegal && excludedUidsLegal && id in 1..5
        ) {
            Text(stringResource(if (updateMode) R.string.update else R.string.add))
        }
        if (updateMode) FilledTonalButton(
            {
                vm.setConfig(origin, false)
                onNavigateUp()
            },
            Modifier
                .padding(horizontal = HorizontalPadding)
                .fillMaxWidth()
        ) {
            Text(stringResource(R.string.delete))
        }
    }
}
