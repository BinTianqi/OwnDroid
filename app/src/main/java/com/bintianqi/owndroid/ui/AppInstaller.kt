package com.bintianqi.owndroid.ui

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.APK_MIME
import com.bintianqi.owndroid.AppInstallerViewModel
import com.bintianqi.owndroid.AppLockDialog
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SP
import com.bintianqi.owndroid.SerializableSaver
import com.bintianqi.owndroid.SessionParamsOptions
import com.bintianqi.owndroid.dpm.parsePackageInstallerMessage
import kotlinx.coroutines.launch
import java.net.URLDecoder


@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun AppInstaller(
    uiState: AppInstallerViewModel.UiState = AppInstallerViewModel.UiState(),
    onPackagesAdd: (List<Uri>) -> Unit = {},
    onPackageRemove: (Uri) -> Unit = {},
    onStartInstall: (SessionParamsOptions) -> Unit = {},
    onResultDialogClose: () -> Unit = {}
) {
    var appLockDialog by rememberSaveable { mutableStateOf(false) }
    var options by rememberSaveable(stateSaver = SerializableSaver(SessionParamsOptions.serializer())) {
        mutableStateOf(SessionParamsOptions())
    }
    val coroutine = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_installer)) }
            )
        },
        floatingActionButton = {
            if(uiState.packages.isNotEmpty()) ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.start)) },
                icon = {
                    if(uiState.installing) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Icon(Icons.Default.PlayArrow, null)
                },
                onClick = {
                    if(SP.lockPasswordHash.isNullOrEmpty()) onStartInstall(options) else appLockDialog = true
                },
                expanded = !uiState.installing
            )
        }
    ) { paddingValues ->
        var tab by rememberSaveable { mutableIntStateOf(0) }
        val pagerState = rememberPagerState { 2 }
        val scrollState = rememberScrollState()
        tab = pagerState.targetPage
        Column(modifier = Modifier.padding(paddingValues)) {
            TabRow(tab) {
                Tab(
                    tab == 0,
                    onClick = {
                        coroutine.launch { scrollState.animateScrollTo(0) }
                        coroutine.launch { pagerState.animateScrollToPage(0) }
                    },
                    text = { Text(stringResource(R.string.packages)) }
                )
                Tab(
                    tab == 1,
                    onClick = {
                        coroutine.launch { scrollState.animateScrollTo(0) }
                        coroutine.launch { pagerState.animateScrollToPage(1) }
                    },
                    text = { Text(stringResource(R.string.options)) }
                )
            }
            HorizontalPager(pagerState, Modifier.fillMaxHeight(), verticalAlignment = Alignment.Top) { page ->
                if (page == 0) Packages(uiState, onPackageRemove, onPackagesAdd)
                else Options(options) { options = it }
            }
        }
        ResultDialog(uiState.result, onResultDialogClose)
    }
    if(appLockDialog) {
        AppLockDialog({
            appLockDialog = false
            onStartInstall(options)
        }) { appLockDialog = false }
    }
}


@Composable
private fun Packages(
    uiState: AppInstallerViewModel.UiState, onRemove: (Uri) -> Unit, onAdd: (List<Uri>) -> Unit
) {
    val chooseSplitPackage = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents(), onAdd)
    LazyColumn(Modifier.padding(top = 8.dp)) {
        itemsIndexed(uiState.packages, { _, it -> it }) { i, it ->
            val status = when {
                uiState.packageWriting < 0 -> 0
                i < uiState.packageWriting -> 3
                i == uiState.packageWriting  -> 2
                else -> 1
            }
            PackageItem(it, status) { onRemove(it) }
        }
        if (!uiState.installing) {
            item {
                Row(
                    Modifier.fillMaxWidth().animateItem().padding(vertical = 4.dp).clickable {
                        chooseSplitPackage.launch(APK_MIME)
                    }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.padding(horizontal = 10.dp))
                    Text(stringResource(R.string.add_packages), style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}

/**
 * @param status 0: not installing, 1: installing, 2: writing, 3: written
 */
@Composable
private fun LazyItemScope.PackageItem(uri: Uri, status: Int, onRemove: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().animateItem().padding(start = 8.dp, end = 6.dp, bottom = 6.dp).heightIn(min = 40.dp)
    ) {
        Text(
            URLDecoder.decode(URLDecoder.decode(uri.path ?: uri.toString())),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.85F)
        )
        when (status) {
            0 -> IconButton(onRemove) {
                Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.remove))
            }
            2 -> CircularProgressIndicator(Modifier.padding(end = 8.dp).size(24.dp))
            3 -> Icon(Icons.Default.Check, null, Modifier.padding(end = 8.dp), MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
private fun Options(options: SessionParamsOptions, onChange: (SessionParamsOptions) -> Unit) = Column {
    Text(
        stringResource(R.string.mode), modifier = Modifier.padding(top = 10.dp, start = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary
    )
    FullWidthRadioButtonItem(R.string.full_install, options.mode == PackageInstaller.SessionParams.MODE_FULL_INSTALL) {
        onChange(options.copy(mode = PackageInstaller.SessionParams.MODE_FULL_INSTALL, noKill = false))
    }
    FullWidthRadioButtonItem(R.string.inherit_existing, options.mode == PackageInstaller.SessionParams.MODE_INHERIT_EXISTING) {
        onChange(options.copy(mode = PackageInstaller.SessionParams.MODE_INHERIT_EXISTING))
    }
    if(Build.VERSION.SDK_INT >= 34) {
        AnimatedVisibility(options.mode == PackageInstaller.SessionParams.MODE_INHERIT_EXISTING) {
            FullWidthCheckBoxItem(R.string.dont_kill_app, options.noKill) {
                onChange(options.copy(noKill = it))
            }
        }
        FullWidthCheckBoxItem(R.string.keep_original_enabled_setting, options.keepOriginalEnabledSetting) {
            onChange(options.copy(keepOriginalEnabledSetting = it))
        }
    }
    Text(
        stringResource(R.string.install_location), modifier = Modifier.padding(top = 10.dp, start = 8.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary
    )
    FullWidthRadioButtonItem(R.string.auto, options.location == PackageInfo.INSTALL_LOCATION_AUTO) {
        onChange(options.copy(location = PackageInfo.INSTALL_LOCATION_AUTO))
    }
    FullWidthRadioButtonItem(R.string.internal_only, options.location == PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY) {
        onChange(options.copy(location = PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY))
    }
    FullWidthRadioButtonItem(R.string.prefer_external, options.location == PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL) {
        onChange(options.copy(location = PackageInfo.INSTALL_LOCATION_PREFER_EXTERNAL))
    }
}

@Composable
private fun ResultDialog(result: Intent?, onDialogClose: () -> Unit) {
    if(result != null) {
        val status = result.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
        AlertDialog(
            title = {
                val text = if(status == PackageInstaller.STATUS_SUCCESS) R.string.success else R.string.failure
                Text(stringResource(text))
            },
            text = {
                val context = LocalContext.current
                Text(parsePackageInstallerMessage(context, result))
            },
            confirmButton = {
                TextButton(onDialogClose) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = onDialogClose
        )
    }
}
