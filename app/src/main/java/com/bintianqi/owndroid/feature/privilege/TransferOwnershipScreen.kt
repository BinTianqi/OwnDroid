package com.bintianqi.owndroid.feature.privilege

import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@RequiresApi(28)
@Composable
fun TransferOwnershipScreen(
    vm: TransferOwnershipViewModel, onNavigateUp: () -> Unit, onTransferred: () -> Unit
) {
    val privilege by vm.ps.collectAsState()
    var selectedIndex by rememberSaveable { mutableIntStateOf(-1) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    val receivers by vm.deviceAdminReceivers.collectAsState()
    LaunchedEffect(Unit) { vm.getDeviceAdminReceivers() }
    MyLazyScaffold(R.string.transfer_ownership, onNavigateUp) {
        itemsIndexed(receivers) { index, admin ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { selectedIndex = index }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selectedIndex == index, { selectedIndex = index })
                Image(rememberDrawablePainter(admin.app.icon), null, Modifier.size(40.dp))
                Column(Modifier.padding(start = 8.dp)) {
                    Text(admin.app.label)
                    Text(admin.app.name, Modifier.alpha(0.7F), style = typography.bodyMedium)
                }
            }
        }
        item {
            Button(
                { dialog = true },
                Modifier
                    .fillMaxWidth()
                    .padding(HorizontalPadding, 10.dp),
                receivers.getOrNull(selectedIndex) != null
            ) {
                Text(stringResource(R.string.transfer))
            }
            Notes(R.string.info_transfer_ownership, HorizontalPadding)
        }
    }
    if (dialog) AlertDialog(
        text = {
            Text(
                stringResource(
                    R.string.transfer_ownership_warning,
                    stringResource(
                        if (privilege.device) R.string.device_owner else R.string.profile_owner
                    ),
                    receivers[selectedIndex].app.name
                )
            )
        },
        confirmButton = {
            TextButton(
                {
                    vm.transferOwnership(receivers[selectedIndex].dar)
                    dialog = false
                    onTransferred()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({ dialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = false }
    )
}
