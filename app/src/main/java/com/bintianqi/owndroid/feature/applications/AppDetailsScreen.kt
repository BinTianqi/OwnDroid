package com.bintianqi.owndroid.feature.applications

import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
import android.os.Build.VERSION
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.PermissionItem
import com.bintianqi.owndroid.utils.runtimePermissions
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun ApplicationDetailsScreen(
    vm: AppDetailsViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) } // 1: clear storage, 2: uninstall
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    MySmallTitleScaffold(R.string.place_holder, onNavigateUp, 0.dp) {
        Column(
            Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 16.dp), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(rememberDrawablePainter(vm.appInfo.icon), null, Modifier.size(50.dp))
            Text(vm.appInfo.label, Modifier.padding(top = 4.dp))
            Text(
                vm.appInfo.name,
                Modifier
                    .alpha(0.7F)
                    .padding(bottom = 8.dp),
                style = typography.bodyMedium
            )
        }
        FunctionItem(R.string.permissions, icon = R.drawable.shield_fill0) {
            onNavigate(Destination.AppPermissionsManager(vm.packageName))
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
        if (VERSION.SDK_INT >= 28) FunctionItem(
            R.string.clear_app_storage, icon = R.drawable.mop_fill0
        ) { dialog = 1 }
        FunctionItem(R.string.uninstall, icon = R.drawable.delete_fill0) { dialog = 2 }
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
    var selectedPermission by remember { mutableStateOf<PermissionItem?>(null) }
    val permissions by vm.permissionsState.collectAsState()
    MyLazyScaffold(R.string.permissions, onNavigateUp) {
        items(runtimePermissions) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable {
                        selectedPermission = it
                    }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(painterResource(it.icon), null, Modifier.padding(horizontal = 12.dp))
                Column {
                    val stateStr = when (permissions[it.id]) {
                        PERMISSION_GRANT_STATE_DEFAULT -> R.string.default_stringres
                        PERMISSION_GRANT_STATE_GRANTED -> R.string.granted
                        PERMISSION_GRANT_STATE_DENIED -> R.string.denied
                        else -> R.string.unknown
                    }
                    Text(stringResource(it.label))
                    Text(
                        stringResource(stateStr), Modifier.alpha(0.7F),
                        style = typography.bodyMedium
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(BottomPadding))
        }
    }
    if (selectedPermission != null) PackagePermissionDialog(
        selectedPermission!!, permissions[selectedPermission!!.id]!!, privilege.profile,
        {
            vm.setPermission(selectedPermission!!.id, it)
            selectedPermission = null
        }
    ) { selectedPermission = null }
}
