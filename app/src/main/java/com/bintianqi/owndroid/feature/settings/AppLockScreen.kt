package com.bintianqi.owndroid.feature.settings

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CancelTextButton
import com.bintianqi.owndroid.ui.ConfirmTextButton
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppLockSettingsScreen(
    vm: AppLockViewModel, onNavigateUp: () -> Unit
) {
    val res = LocalResources.current
    val coroutine = rememberCoroutineScope()
    val uiState by vm.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    var passwordDialog by rememberSaveable { mutableStateOf(false) }
    var totpDialog by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        vm.getState()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.app_lock)) },
                navigationIcon = { NavIcon(onNavigateUp) }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbar, Modifier.navigationBarsPadding())
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = HorizontalPadding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        stringResource(R.string.password),
                        style = MaterialTheme.typography.labelLarge
                    )
                    Text(stringResource(
                        if (uiState.password) R.string.configured else R.string.not_configured
                    ), style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    Button({ passwordDialog = true }) {
                        Text(stringResource(
                            if (uiState.password) R.string.change else R.string.set_up
                        ))
                    }
                    if (uiState.password) FilledTonalButton({
                        vm.removePassword()
                        coroutine.launch {
                            val result = snackbar.showSnackbar(
                                res.getString(R.string.password_removed),
                                res.getString(R.string.restore),
                                true,
                                SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                vm.restorePassword()
                            }
                        }
                    }, Modifier.padding(start = 6.dp)) {
                        Text(stringResource(R.string.remove))
                    }
                }
            }
            Row(
                Modifier.fillMaxWidth(),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Column {
                    Text("TOTP", style = MaterialTheme.typography.labelLarge)
                    Text(stringResource(
                        if (uiState.totp) R.string.configured else R.string.not_configured
                    ), style = MaterialTheme.typography.bodyMedium)
                }
                Row {
                    Button({
                        vm.setupTotp()
                        totpDialog = true
                    }) {
                        Text(stringResource(
                            if (uiState.totp) R.string.show_secret else R.string.set_up
                        ))
                    }
                    if (uiState.totp) FilledTonalButton({
                        vm.removeTotp()
                        coroutine.launch {
                            val result = snackbar.showSnackbar(
                                res.getString(R.string.totp_removed),
                                res.getString(R.string.restore),
                                true,
                                SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                vm.restoreTotp()
                            }
                        }
                    }, Modifier.padding(start = 6.dp)) {
                        Text(stringResource(R.string.remove))
                    }
                }
            }
            AnimatedVisibility(uiState.password || uiState.totp) {
                Column {
                    if (Build.VERSION.SDK_INT >= 28) Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.allow_biometric_unlock))
                        Switch(uiState.biometrics, vm::setBiometricsAllowed)
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Text(stringResource(R.string.lock_when_leaving))
                        Switch(uiState.whenLeaving, vm::setLockWhenLeaving)
                    }
                }
            }
            if (!uiState.password && !uiState.totp) Notes(R.string.info_app_lock)
        }
    }
    if (passwordDialog) {
        var input by rememberSaveable { mutableStateOf("") }
        var inputConfirm by rememberSaveable { mutableStateOf("") }
        AlertDialog(
            text = {
                Column {
                    OutlinedTextField(
                        input, { input = it },
                        Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        label = { Text(stringResource(R.string.password)) },
                        supportingText = {
                            Text(stringResource(R.string.minimum_length_4))
                        },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
                        )
                    )
                    OutlinedTextField(
                        inputConfirm, { inputConfirm = it }, Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge,
                        label = { Text(stringResource(R.string.confirm_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
                        )
                    )
                }
            },
            onDismissRequest = { passwordDialog = false },
            confirmButton = {
                ConfirmTextButton(input.length >= 4 && input == inputConfirm) {
                    vm.setPassword(input)
                    passwordDialog = false
                }
            },
            dismissButton = {
                CancelTextButton { passwordDialog = false }
            }
        )
    }
    if (totpDialog) {
        val totpUiState by vm.totpUiState.collectAsState()
        LaunchedEffect(Unit) {
            vm.getTotpUiState()
        }
        AlertDialog(
            onDismissRequest = { totpDialog = false },
            title = { Text(stringResource(R.string.time_blocker_totp_secret)) },
            text = {
                if (totpUiState != null) Column {
                    Text(stringResource(R.string.time_blocker_scan_qr))
                    Spacer(Modifier.height(8.dp))
                    Image(
                        bitmap = totpUiState!!.qrCode.asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f),
                        filterQuality = FilterQuality.None
                    )
                    Spacer(Modifier.height(8.dp))
                    SelectionContainer {
                        Text(totpUiState!!.otpUri)
                    }
                }
            },
            confirmButton = {
                ConfirmTextButton { totpDialog = false }
            }
        )
    }
}
