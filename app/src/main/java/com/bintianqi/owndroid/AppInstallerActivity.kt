package com.bintianqi.owndroid

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.dpm.parsePackageInstallerMessage
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder

class AppInstallerActivity:FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val myVm by viewModels<MyViewModel>()
        val vm by viewModels<AppInstallerViewModel>()
        vm.initialize(intent)
        setContent {
            val theme by myVm.theme.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                val installing by vm.installing.collectAsStateWithLifecycle()
                val options by vm.options.collectAsStateWithLifecycle()
                val packages by vm.packages.collectAsStateWithLifecycle()
                val writtenPackages by vm.writtenPackages.collectAsStateWithLifecycle()
                val writingPackage by vm.writingPackage.collectAsStateWithLifecycle()
                val result by vm.result.collectAsStateWithLifecycle()
                AppInstaller(
                    installing, options, { if(!installing) vm.options.value = it },
                    packages, { uri -> vm.packages.update { it.minus(uri) } },
                    { uris -> vm.packages.update { it.plus(uris) } },
                    vm::startInstall, writtenPackages, writingPackage,
                    result, { vm.result.value = null }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AppInstaller(
    installing: Boolean = false,
    options: SessionParamsOptions = SessionParamsOptions(),
    onOptionsChange: (SessionParamsOptions) -> Unit = {},
    packages: Set<Uri> = setOf("https://example.com".toUri()),
    onPackageRemove: (Uri) -> Unit = {},
    onPackageChoose: (List<Uri>) -> Unit = {},
    onStartInstall: () -> Unit = {},
    writtenPackages: Set<Uri> = setOf("https://example.com".toUri()),
    writingPackage: Uri? = null,
    result: Intent? = null,
    onResultDialogClose: () -> Unit = {}
) {
    var appLockDialog by rememberSaveable { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_installer)) }
            )
        },
        floatingActionButton = {
            if(packages.isNotEmpty()) ExtendedFloatingActionButton(
                text = { Text(stringResource(R.string.start)) },
                icon = {
                    if(installing) CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    else Icon(Icons.Default.PlayArrow, null)
                },
                onClick = {
                    if(SP.lockPasswordHash.isNullOrEmpty()) onStartInstall() else appLockDialog = true
                },
                expanded = !installing
            )
        }
    ) { paddingValues ->
        var tab by remember { mutableIntStateOf(0) }
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
            HorizontalPager(pagerState) { page ->
                Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(top = 8.dp)) {
                    if(page == 0) Packages(installing, packages, onPackageRemove, onPackageChoose, writtenPackages, writingPackage)
                    else Options(options, onOptionsChange)
                }
            }
            ResultDialog(result, onResultDialogClose)
        }
    }
    if(appLockDialog) {
        AppLockDialog({
            appLockDialog = false
            onStartInstall()
        }) { appLockDialog = false }
    }
}


@Composable
private fun ColumnScope.Packages(
    installing: Boolean,
    packages: Set<Uri>, onRemove: (Uri) -> Unit, onChoose: (List<Uri>) -> Unit,
    writtenPackages: Set<Uri>, writingPackage: Uri?
) {
    val chooseSplitPackage = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents(), onChoose)
    packages.forEach {
        PackageItem(
            it, installing,
            { onRemove(it) }, it in writtenPackages, it == writingPackage
        )
    }
    AnimatedVisibility(!installing) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                chooseSplitPackage.launch(APK_MIME)
            }.padding(vertical = 12.dp)
        ) {
            Icon(Icons.Default.Add, null, modifier = Modifier.padding(horizontal = 10.dp))
            Text(stringResource(R.string.add_packages), style = MaterialTheme.typography.titleMedium)
        }
    }
}


@Composable
private fun PackageItem(uri: Uri, installing: Boolean, onRemove: () -> Unit, isWritten: Boolean, isWriting: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 6.dp, bottom = 6.dp).heightIn(min = 40.dp)
    ) {
        Text(
            URLDecoder.decode(URLDecoder.decode(uri.path ?: uri.toString())),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.fillMaxWidth(0.85F)
        )
        if(!installing) IconButton(onRemove) {
            Icon(Icons.Default.Clear, contentDescription = stringResource(R.string.remove))
        }
        if(isWritten) Icon(Icons.Default.Check, null, Modifier.padding(end = 8.dp), MaterialTheme.colorScheme.secondary)
        if(isWriting) CircularProgressIndicator(Modifier.padding(end = 8.dp).size(24.dp))
    }
}

data class SessionParamsOptions(
    val mode: Int = PackageInstaller.SessionParams.MODE_FULL_INSTALL,
    val keepOriginalEnabledSetting: Boolean = false,
    val noKill: Boolean = false,
    val location: Int = PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY,
)

@Composable
private fun ColumnScope.Options(options: SessionParamsOptions, onChange: (SessionParamsOptions) -> Unit) {
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

class AppInstallerViewModel(application: Application): AndroidViewModel(application) {
    fun initialize(intent: Intent) {
        intent.data?.let { uri -> packages.update { it + uri } }
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { uri -> packages.update { it + uri } }
        intent.getParcelableArrayExtra(Intent.EXTRA_STREAM)?.forEach { uri -> packages.update { it + (uri as Uri) } }
        intent.clipData?.let { clipData ->
            for(i in 0..clipData.itemCount) {
                packages.update { it + clipData.getItemAt(i).uri }
            }
        }
    }
    val installing = MutableStateFlow(false)
    val result = MutableStateFlow<Intent?>(null)
    val packages = MutableStateFlow(setOf<Uri>())

    val options = MutableStateFlow(SessionParamsOptions())

    val writtenPackages = MutableStateFlow(setOf<Uri>())
    val writingPackage = MutableStateFlow<Uri?>(null)
    private fun getSessionParams(): PackageInstaller.SessionParams {
        return PackageInstaller.SessionParams(options.value.mode).apply {
            if(Build.VERSION.SDK_INT >= 34) {
                if(options.value.keepOriginalEnabledSetting) setApplicationEnabledSettingPersistent()
                setDontKillApp(options.value.noKill)
            }
            setInstallLocation(options.value.location)
        }
    }
    fun startInstall() {
        if(installing.value) return
        installing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val packageInstaller = context.packageManager.packageInstaller
            val sessionId = packageInstaller.createSession(getSessionParams())
            val session = packageInstaller.openSession(sessionId)
            try {
                packages.value.forEach { splitPackageUri ->
                    withContext(Dispatchers.Main) { writingPackage.value = splitPackageUri }
                    session.openWrite(splitPackageUri.hashCode().toString(), 0, -1).use { splitPackageOut ->
                        context.contentResolver.openInputStream(splitPackageUri)!!.use { splitPackageIn ->
                            splitPackageIn.copyTo(splitPackageOut)
                        }
                        session.fsync(splitPackageOut)
                    }
                    withContext(Dispatchers.Main) { writtenPackages.update { it.plus(splitPackageUri) } }
                }
                withContext(Dispatchers.Main) { writingPackage.value = null }
            } catch(e: Exception) {
                e.printStackTrace()
                session.abandon()
                return@launch
            }
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val statusExtra = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
                    if(statusExtra == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                        @SuppressWarnings("UnsafeIntentLaunch")
                        context.startActivity(
                            (intent.getParcelableExtra(Intent.EXTRA_INTENT) as Intent?)
                                ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    } else {
                        result.value = intent
                        writtenPackages.value = setOf()
                        if(statusExtra == PackageInstaller.STATUS_SUCCESS) {
                            packages.value = setOf()
                        }
                        installing.value = false
                        context.unregisterReceiver(this)
                    }
                }
            }
            ContextCompat.registerReceiver(
                context, receiver, IntentFilter(ACTION), null,
                null, ContextCompat.RECEIVER_EXPORTED
            )
            val pi = if(Build.VERSION.SDK_INT >= 34) {
                PendingIntent.getBroadcast(
                    context, sessionId, Intent(ACTION),
                    PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_MUTABLE
                ).intentSender
            } else {
                PendingIntent.getBroadcast(context, sessionId, Intent(ACTION), PendingIntent.FLAG_MUTABLE).intentSender
            }
            session.commit(pi)
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.cancel()
    }
    companion object {
        const val ACTION = "com.bintianqi.owndroid.action.PACKAGE_INSTALLER_SESSION_STATUS_CHANGED"
    }
}
