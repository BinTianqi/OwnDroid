package com.bintianqi.owndroid.feature.system

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.formatDate
import com.bintianqi.owndroid.utils.popToast

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun CaCertScreen(
    vm: CaCertViewModel, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    // 0:none, 1:install, 2:info, 3:uninstall all
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    val caCerts by vm.installedCertsState.collectAsState()
    val selectedCert by vm.selectedCert.collectAsState()
    val getCertLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            vm.parseCert(uri)
            dialog = 1
        }
    }
    val exportCertLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument()
    ) { uri ->
        if (uri != null) vm.exportCert(uri)
    }
    LaunchedEffect(Unit) {
        vm.getCaCerts()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.ca_cert)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    IconButton({ dialog = 3 }) {
                        Icon(Icons.Outlined.Delete, stringResource(R.string.delete))
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton({
                context.popToast(R.string.select_ca_cert)
                getCertLauncher.launch(arrayOf("*/*"))
            }) {
                Icon(Icons.Default.Add, stringResource(R.string.install))
            }
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(caCerts, { it.hash }) { cert ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.selectCert(cert)
                            dialog = 2
                        }
                        .animateItem()
                        .padding(vertical = 10.dp, horizontal = 8.dp)
                ) {
                    Text(cert.hash.substring(0..7))
                }
                HorizontalDivider()
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
    if (selectedCert != null && (dialog == 1 || dialog == 2)) {
        val cert = selectedCert!!
        AlertDialog(
            text = {
                Column(Modifier.verticalScroll(rememberScrollState())) {
                    Text("Serial number", style = typography.labelLarge)
                    SelectionContainer { Text(cert.serialNumber) }
                    Text("Subject", style = typography.labelLarge)
                    SelectionContainer { Text(cert.subject) }
                    Text("Issuer", style = typography.labelLarge)
                    SelectionContainer { Text(cert.issuer) }
                    Text("Issued on", style = typography.labelLarge)
                    SelectionContainer { Text(formatDate(cert.issuedTime)) }
                    Text("Expires on", style = typography.labelLarge)
                    SelectionContainer { Text(formatDate(cert.expiresTime)) }
                    Text("SHA-256 fingerprint", style = typography.labelLarge)
                    SelectionContainer { Text(cert.hash) }
                    if (dialog == 2) Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp), Arrangement.SpaceBetween
                    ) {
                        TextButton(
                            {
                                vm.uninstallCert()
                                dialog = 0
                            },
                            Modifier.fillMaxWidth(0.49F)
                        ) {
                            Text(stringResource(R.string.uninstall))
                        }
                        FilledTonalButton(
                            {
                                exportCertLauncher.launch(cert.hash.substring(0..7) + ".0")
                            },
                            Modifier.fillMaxWidth(0.96F)
                        ) {
                            Text(stringResource(R.string.export))
                        }
                    }
                }
            },
            confirmButton = {
                if (dialog == 1) {
                    TextButton({
                        vm.installCert()
                        dialog = 0
                    }) {
                        Text(stringResource(R.string.install))
                    }
                } else {
                    TextButton({
                        dialog = 0
                    }) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            },
            dismissButton = {
                if (dialog == 1) {
                    TextButton({
                        dialog = 0
                    }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            },
            onDismissRequest = { dialog = 0 }
        )
    }
    if (dialog == 3) {
        AlertDialog(
            text = {
                Text(stringResource(R.string.uninstall_all_user_ca_cert))
            },
            confirmButton = {
                TextButton({
                    vm.uninstallAll()
                    dialog = 0
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton({
                    dialog = 0
                }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { dialog = 0 }
        )
    }
}
