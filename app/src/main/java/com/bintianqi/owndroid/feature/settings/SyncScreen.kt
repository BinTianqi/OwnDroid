package com.bintianqi.owndroid.feature.settings

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyScaffold

import java.util.concurrent.Executors

private enum class SyncMode { None, Export, Import }

@Composable
fun SettingsSyncScreen(vm: SettingsViewModel, onNavigateUp: () -> Unit) {
    var mode by remember { mutableStateOf(SyncMode.None) }
    val qrBitmap by vm.syncQrBitmap.collectAsState()
    val exportSummary by vm.syncExportSummary.collectAsState()
    val importResult by vm.syncImportResult.collectAsState()

    // The VM is parent-scoped and survives this screen; drop stale one-shot results
    LaunchedEffect(Unit) { vm.clearSyncImportResult() }
    DisposableEffect(Unit) { onDispose { vm.clearSyncImportResult() } }

    MyScaffold(R.string.sync_transfer, onNavigateUp, 0.dp) {
        if (mode == SyncMode.None) {
            FunctionItem(
                R.string.sync_export_title,
                stringResource(R.string.sync_export_desc),
                R.drawable.qr_code_fill0
            ) {
                vm.clearSyncExport()
                vm.buildSyncQr()
                mode = SyncMode.Export
            }
            FunctionItem(
                R.string.sync_import_title,
                stringResource(R.string.sync_import_desc),
                R.drawable.qr_code_scanner_fill0
            ) {
                mode = SyncMode.Import
            }
        } else if (mode == SyncMode.Export) {
            ExportView(qrBitmap, exportSummary) { mode = SyncMode.None }
        } else {
            ImportView(vm) { mode = SyncMode.None }
        }
    }

    // Import result confirmation
    importResult?.let { result ->
        AlertDialog(
            onDismissRequest = { vm.clearSyncImportResult() },
            title = { Text(stringResource(R.string.sync_import_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.sync_import_result,
                        result.rulesImported,
                        stringResource(
                            if (result.totpImported) R.string.sync_totp_included
                            else R.string.sync_totp_not_included
                        ),
                        result.rulesSkipped
                    )
                )
            },
            confirmButton = {
                TextButton({ vm.clearSyncImportResult(); mode = SyncMode.None }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Composable
private fun ExportView(
    qrBitmap: android.graphics.Bitmap?,
    summary: Pair<Int, Boolean>?,
    onBack: () -> Unit
) {
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (qrBitmap != null && summary != null) {
            Image(
                bitmap = qrBitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .padding(16.dp),
                filterQuality = FilterQuality.None // QR codes need crisp module edges
            )
            Text(
                stringResource(
                    R.string.sync_export_summary,
                    summary.first,
                    stringResource(
                        if (summary.second) R.string.sync_totp_included
                        else R.string.sync_totp_not_included
                    )
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.sync_export_warning),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        } else {
            Spacer(Modifier.height(48.dp))
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.sync_export_generating))
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onBack) {
            Text(stringResource(R.string.cancel))
        }
    }
}

@Composable
private fun ImportView(vm: SettingsViewModel, onBack: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                    android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCameraPermission = granted }

    // Pending scanned payload waiting for user confirmation
    var pendingPayload by remember { mutableStateOf<SyncPayload?>(null) }
    var invalidQrShown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (hasCameraPermission) {
            CameraScanner(
                onPayload = { payload -> pendingPayload = payload },
                onInvalid = { invalidQrShown = true }
            )
            Spacer(Modifier.height(12.dp))
            Text(
                stringResource(R.string.sync_import_hint),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            Spacer(Modifier.height(24.dp))
            Text(
                stringResource(R.string.sync_camera_permission_needed),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button({ permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                Text(stringResource(R.string.sync_grant_camera))
            }
        }
        Spacer(Modifier.height(24.dp))
        OutlinedButton(onBack) {
            Text(stringResource(R.string.cancel))
        }
    }

    // Confirmation dialog: show summary, require explicit replace
    pendingPayload?.let { payload ->
        val (validRules, skipped) = SyncCodec.validate(payload)
        val totpIncluded = payload.totpSecret.isNotEmpty() &&
                SyncCodec.isValidTotpSecret(payload.totpSecret)
        AlertDialog(
            onDismissRequest = { pendingPayload = null },
            title = { Text(stringResource(R.string.sync_import_confirm_title)) },
            text = {
                Column {
                    Text(
                        stringResource(
                            R.string.sync_import_confirm_msg,
                            validRules.size,
                            stringResource(
                                if (totpIncluded) R.string.sync_totp_included
                                else R.string.sync_totp_not_included
                            )
                        )
                    )
                    if (skipped > 0) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            stringResource(R.string.sync_import_skipped, skipped),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        stringResource(R.string.sync_import_replace_warning),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton({
                    vm.importSyncPayload(payload)
                    pendingPayload = null
                }) {
                    Text(stringResource(R.string.import_str))
                }
            },
            dismissButton = {
                TextButton({ pendingPayload = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (invalidQrShown) {
        AlertDialog(
            onDismissRequest = { invalidQrShown = false },
            title = { Text(stringResource(R.string.sync_import_title)) },
            text = { Text(stringResource(R.string.sync_import_invalid)) },
            confirmButton = {
                TextButton({ invalidQrShown = false }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        )
    }
}

@Composable
private fun CameraScanner(
    onPayload: (SyncPayload) -> Unit,
    onInvalid: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val executor = remember { Executors.newSingleThreadExecutor() }
    val cameraProviderState = remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var scanning by remember { mutableStateOf(true) }
    // Notify about a foreign/invalid QR at most once per scanning session
    var invalidNotified by remember { mutableStateOf(false) }
    // Set when the composable is disposed before the camera finished binding
    val disposed = remember { java.util.concurrent.atomic.AtomicBoolean(false) }

    DisposableEffect(Unit) {
        onDispose {
            disposed.set(true)
            cameraProviderState.value?.unbindAll()
            executor.shutdown()
        }
    }

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                if (disposed.get()) {
                    // Composable left while camera was starting; don't leak the binding
                    return@addListener
                }
                cameraProviderState.value = cameraProvider
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val analysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analysis.setAnalyzer(executor) { imageProxy ->
                    try {
                        if (scanning) {
                            val bytes = QrUtils.decodeImageProxy(imageProxy)
                            if (bytes != null) {
                                try {
                                    val payload = SyncCodec.decode(bytes)
                                    scanning = false
                                    onPayload(payload)
                                } catch (_: SyncCodec.InvalidPayloadException) {
                                    if (!invalidNotified) {
                                        invalidNotified = true
                                        onInvalid()
                                    }
                                }
                            }
                        }
                    } finally {
                        imageProxy.close()
                    }
                }
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, analysis
                    )
                } catch (_: Exception) {
                    // Camera unavailable (emulator, in use) — nothing sensible to show
                }
            }, ContextCompat.getMainExecutor(ctx))
            previewView
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(8.dp)
    )
}
