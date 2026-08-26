package com.bintianqi.owndroid.feature.applications

import android.os.Build.VERSION
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun ApplicationDetailsScreen(
    vm: AppDetailsViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) } // 1: clear storage, 2: uninstall
    val appInfo by vm.appInfo.collectAsState()
    val detailedAppInfo by vm.detailedAppInfo.collectAsState()
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        vm.getInfo()
    }
    MySmallTitleScaffold(R.string.place_holder, onNavigateUp, 0.dp, {
        IconButton({ vm.viewAppDetails(context) }) {
            Icon(Icons.Outlined.Info, null)
        }
    }) {
        if (appInfo != null) Column(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp, bottom = 8.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(rememberDrawablePainter(appInfo!!.icon), null, Modifier.size(50.dp))
            SelectionContainer {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(appInfo!!.label, Modifier.padding(top = 4.dp))
                    Text(
                        appInfo!!.name, Modifier.alpha(0.7F),
                        style = typography.bodyMedium
                    )
                    Text(
                        detailedAppInfo.version + " (${detailedAppInfo.versionNumber})",
                        Modifier.alpha(0.7F),
                        style = typography.bodyMedium
                    )
                }
            }
            Text(
                stringResource(if (appInfo!!.isSystem) R.string.system_app else R.string.user_app),
                Modifier.alpha(0.7F),
                style = typography.bodyMedium
            )
        }
        FunctionItem(R.string.permissions, icon = R.drawable.shield_fill0) {
            onNavigate(Destination.AppPermissionsManager)
        }
        if (VERSION.SDK_INT >= 24) SwitchItem(
            R.string.suspend, uiState.suspend, vm::setSuspended, R.drawable.block_fill0
        )
        SwitchItem(
            R.string.hide, uiState.hide, vm::setHidden, R.drawable.visibility_off_fill0
        )
        SwitchItem(
            R.string.block_uninstall, uiState.uninstallBlocked,
            vm::setUninstallBlocked, R.drawable.delete_forever_fill0
        )
        if (VERSION.SDK_INT >= 30) SwitchItem(
            R.string.disable_user_control, uiState.userControlDisabled,
            vm::setUserControlDisabled, R.drawable.do_not_touch_fill0
        )
        if (VERSION.SDK_INT >= 28) SwitchItem(
            R.string.disable_metered_data, uiState.meteredDataDisabled,
            vm::setMeteredDataDisabled, R.drawable.money_off_fill0
        )
        if (privilege.device && VERSION.SDK_INT >= 28) SwitchItem(
            R.string.keep_after_uninstall, uiState.keepUninstalled,
            vm::setKeepUninstalled, R.drawable.delete_fill0
        )
        FunctionItem(R.string.managed_configuration, icon = R.drawable.description_fill0) {
            onNavigate(Destination.ManagedConfiguration(vm.packageName))
        }
        Row(Modifier.fillMaxWidth().padding(8.dp)) {
            FilledTonalButton(
                { dialog = 1 },
                Modifier.padding(horizontal = 4.dp).weight(1F)
            ) {
                Text(stringResource(R.string.clear_storage))
            }
            FilledTonalButton(
                { dialog = 2 },
                Modifier.padding(horizontal = 4.dp).weight(1F)
            ) {
                Text(stringResource(R.string.uninstall))
            }
        }
        Spacer(Modifier.height(BottomPadding))
    }
    if (dialog == 1 && VERSION.SDK_INT >= 28)
        ClearAppStorageDialog({
            vm.clearData { dialog = 0 }
        }) { dialog = 0 }
    if (dialog == 2) UninstallAppDialog(vm::uninstall) {
        dialog = 0
        if (it) onNavigateUp()
    }
}

@Composable
fun AppPermissionsManagerScreen(
    vm: AppDetailsViewModel, onNavigateUp: () -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    val permissions by vm.permissionsState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getPermissions()
    }
    MyLazyScaffold(R.string.permissions, onNavigateUp) {
        item {
            if (permissions.isEmpty()) {
                Text(
                    stringResource(R.string.none),
                    Modifier
                        .fillMaxWidth()
                        .alpha(0.7F),
                    textAlign = TextAlign.Center
                )
            } else {
                PermissionRadioButtonHint()
            }
        }
        items(permissions.toList()) { (permission, state) ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(
                    Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically
                ) {
                    if (permission.icon != null) Icon(
                        painterResource(permission.icon), null,
                        Modifier.padding(horizontal = 12.dp)
                    )
                    Column {
                        Text(permission.label)
                        Text(permission.id, Modifier.alpha(0.7F), style = typography.bodySmall)
                    }
                }
                PermissionRadioButtonRow(
                    state,
                    (VERSION.SDK_INT >= 31 &&
                            permission.id in profileOwnerRestrictedPermissions && privilege.profile)
                ) { state ->
                    vm.setPermission(permission.id, state)
                }
            }
        }
        item {
            Spacer(Modifier.height(BottomPadding))
        }
    }
}
