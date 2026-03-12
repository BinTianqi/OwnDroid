package com.bintianqi.owndroid.feature.work_profile

import android.os.Build.VERSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.utils.HorizontalPadding

@Composable
fun CreateWorkProfileScreen(
    vm: CreateWorkProfileViewModel, onNavigateUp: () -> Unit
) {
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    MyScaffold(R.string.create_work_profile, onNavigateUp, 0.dp) {
        var skipEncrypt by remember { mutableStateOf(false) }
        var offlineProvisioning by remember { mutableStateOf(true) }
        var migrateAccount by remember { mutableStateOf(false) }
        var migrateAccountName by remember { mutableStateOf("") }
        var migrateAccountType by remember { mutableStateOf("") }
        var keepAccount by remember { mutableStateOf(true) }
        FullWidthCheckBoxItem(R.string.migrate_account, migrateAccount) { migrateAccount = it }
        AnimatedVisibility(migrateAccount) {
            Column(Modifier.padding(start = 10.dp)) {
                OutlinedTextField(
                    migrateAccountName, { migrateAccountName = it },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPadding),
                    label = { Text(stringResource(R.string.account_name)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                OutlinedTextField(
                    migrateAccountType, { migrateAccountType = it },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPadding),
                    label = { Text(stringResource(R.string.account_type)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                )
                if (VERSION.SDK_INT >= 26) {
                    FullWidthCheckBoxItem(R.string.keep_account, keepAccount) { keepAccount = it }
                }
            }
        }
        if (VERSION.SDK_INT >= 24) FullWidthCheckBoxItem(
            R.string.skip_encryption, skipEncrypt
        ) { skipEncrypt = it }
        if (VERSION.SDK_INT >= 33) FullWidthCheckBoxItem(
            R.string.offline_provisioning, offlineProvisioning
        ) { offlineProvisioning = it }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            {
                val intent = vm.createIntent(
                    CreateWorkProfileOptions(
                        skipEncrypt, offlineProvisioning, migrateAccount, migrateAccountName,
                        migrateAccountType, keepAccount
                    )
                )
                launcher.launch(intent)
            },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding)
        ) {
            Text(stringResource(R.string.create))
        }
    }
}
