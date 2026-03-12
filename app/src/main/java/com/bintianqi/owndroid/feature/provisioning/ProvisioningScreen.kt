package com.bintianqi.owndroid.feature.provisioning

import android.app.admin.DevicePolicyManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets

@RequiresApi(29)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvisioningScreen(params: ProvisioningParams, callback: (ProvisioningOptions) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.app_name)) }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            Column(Modifier.padding(horizontal = HorizontalPadding)) {
                if (!params.imei.isNullOrEmpty()) {
                    Text("IMEI", style = MaterialTheme.typography.titleMedium)
                    Text(params.imei, Modifier.padding(bottom = 8.dp))
                }
                if (!params.serial.isNullOrEmpty()) {
                    Text(
                        stringResource(R.string.serial_number),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(params.serial, Modifier.padding(bottom = 8.dp))
                }
            }
            if (DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE in params.modes) {
                Spacer(Modifier.height(10.dp))
                var skipEncryption by remember { mutableStateOf(false) }
                FullWidthCheckBoxItem(R.string.skip_encryption, skipEncryption) {
                    skipEncryption = it
                }
                Button(
                    {
                        callback(ProvisioningOptions(skipEncryption))
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(HorizontalPadding, 4.dp)
                ) {
                    Text(stringResource(R.string.continue_str))
                }
            } else {
                Text(
                    stringResource(R.string.unsupported),
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Spacer(Modifier.height(BottomPadding))
        }
    }
}
