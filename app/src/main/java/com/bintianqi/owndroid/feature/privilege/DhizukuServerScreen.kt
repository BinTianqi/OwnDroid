package com.bintianqi.owndroid.feature.privilege

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.DhizukuPermissions
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.google.accompanist.drawablepainter.rememberDrawablePainter


@Composable
fun DhizukuServerSettingsScreen(
    vm: DhizukuServerViewModel, onNavigateUp: () -> Unit
) {
    val enabled by vm.serverEnabledState.collectAsState()
    val clients by vm.clientsState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getEnabled()
        vm.getClients()
    }
    MyLazyScaffold(R.string.dhizuku_server, onNavigateUp) {
        item {
            SwitchItem(R.string.enable, enabled, vm::setEnabled)
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        if (enabled) items(clients) { (client, app) ->
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 8.dp)
            ) {
                DhizukuClientCardContent(client, app, vm::updateClient)
            }
        }
    }
}

@Composable
private fun DhizukuClientCardContent(
    client: DhizukuClientInfo, app: AppInfo, update: (DhizukuClientInfo) -> Unit
) {
    var expand by remember { mutableStateOf(false) }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(8.dp, 8.dp, 0.dp, 8.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
            Image(
                rememberDrawablePainter(app.icon), null,
                Modifier
                    .padding(end = 16.dp)
                    .size(45.dp)
            )
            Column {
                Text(app.label, style = typography.titleMedium)
                Text(app.name, Modifier.alpha(0.7F), style = typography.bodyMedium)
            }
        }
        val ts = when (DhizukuPermissions.filter { it !in client.permissions }.size) {
            0 -> ToggleableState.On
            DhizukuPermissions.size -> ToggleableState.Off
            else -> ToggleableState.Indeterminate
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            TriStateCheckbox(ts, {
                if (ts == ToggleableState.Off) {
                    update(client.copy(permissions = DhizukuPermissions))
                } else {
                    update(client.copy(permissions = emptyList()))
                }
            })
            val degrees by animateFloatAsState(if (expand) 180F else 0F)
            IconButton({ expand = !expand }) {
                Icon(Icons.Default.ArrowDropDown, null, Modifier.rotate(degrees))
            }
        }
    }
    AnimatedVisibility(expand, Modifier.padding(8.dp, 0.dp, 8.dp, 8.dp)) {
        Column {
            mapOf(
                "remote_transact" to "Remote transact",
                "remote_process" to "Remote process",
                "user_service" to "User service",
                "delegated_scopes" to "Delegated scopes",
                "other" to "Other"
            ).forEach { (k, v) ->
                Row(
                    Modifier.fillMaxWidth(), Arrangement.SpaceBetween,
                    Alignment.CenterVertically
                ) {
                    Text(v)
                    Checkbox(k in client.permissions, {
                        update(
                            client.copy(
                                permissions = client.permissions.run {
                                    if (it) plus(k) else minus(k)
                                }
                            ))
                    })
                }
            }
        }
    }
}
