package com.bintianqi.owndroid.feature.applications

import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.AppInfo
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun LazyItemScope.ApplicationItem(info: AppInfo, onClear: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .animateItem(),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
            Image(
                rememberDrawablePainter(info.icon), null,
                Modifier
                    .padding(start = 12.dp, end = 18.dp)
                    .size(30.dp)
            )
            Column {
                Text(info.label)
                Text(info.name, Modifier.alpha(0.8F), style = typography.bodyMedium)
            }
        }
        IconButton(onClear) {
            Icon(Icons.Default.Clear, null)
        }
    }
}

@RequiresApi(28)
@Composable
internal fun ClearAppStorageDialog(
    onClear: () -> Unit, onClose: () -> Unit
) {
    var clearing by rememberSaveable { mutableStateOf(false) }
    AlertDialog(
        title = { Text(stringResource(R.string.clear_app_storage)) },
        text = {
            if (clearing) LinearProgressIndicator(Modifier.fillMaxWidth())
            else Text(stringResource(R.string.clear_app_storage_confirmation))
        },
        confirmButton = {
            TextButton(
                {
                    clearing = true
                    onClear()
                },
                enabled = !clearing,
                colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error)
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton(onClose, enabled = !clearing) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = {
            if (!clearing) onClose()
        },
        properties = DialogProperties(false, false)
    )
}


@Composable
internal fun UninstallAppDialog(
    onUninstall: ((String?) -> Unit) -> Unit, onClose: (Boolean) -> Unit
) {
    var uninstalling by rememberSaveable { mutableStateOf(false) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    AlertDialog(
        title = { Text(stringResource(R.string.uninstall)) },
        text = {
            if (errorMessage != null) Text(errorMessage!!)
            if (uninstalling) LinearProgressIndicator(Modifier.fillMaxWidth())
        },
        confirmButton = {
            TextButton(
                {
                    if (errorMessage == null) {
                        uninstalling = true
                        onUninstall {
                            uninstalling = false
                            if (it == null) onClose(true) else errorMessage = it
                        }
                    } else {
                        onClose(false)
                    }
                },
                enabled = !uninstalling
            ) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            if (errorMessage == null) TextButton({
                onClose(false)
            }, enabled = !uninstalling) { Text(stringResource(R.string.cancel)) }
        },
        onDismissRequest = { onClose(false) },
        properties = DialogProperties(false, false)
    )
}
