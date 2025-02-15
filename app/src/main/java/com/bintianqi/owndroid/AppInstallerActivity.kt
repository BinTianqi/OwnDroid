package com.bintianqi.owndroid

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInstaller
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.dpm.parsePackageInstallerMessage
import com.bintianqi.owndroid.ui.RadioButtonItem
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
                val sessionMode by vm.sessionMode.collectAsStateWithLifecycle()
                val packages by vm.packages.collectAsStateWithLifecycle()
                val writtenPackages by vm.writtenPackages.collectAsStateWithLifecycle()
                val writingPackage by vm.writingPackage.collectAsStateWithLifecycle()
                val result by vm.result.collectAsStateWithLifecycle()
                AppInstaller(
                    installing, sessionMode, { vm.sessionMode.value = it },
                    packages, { uri -> vm.packages.update { it.minus(uri) } },
                    { uris -> vm.packages.update { it.plus(uris) } },
                    { vm.startInstallationProcess(this) }, writtenPackages, writingPackage,
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
    sessionMode: Int = PackageInstaller.SessionParams.MODE_INHERIT_EXISTING,
    onSessionModeChoose: (Int) -> Unit = {},
    packages: Set<Uri> = setOf(Uri.parse("https://example.com")),
    onPackageRemove: (Uri) -> Unit = {},
    onPackageChoose: (List<Uri>) -> Unit = {},
    onFabPressed: () -> Unit = {},
    writtenPackages: Set<Uri> = setOf(Uri.parse("https://example.com")),
    writingPackage: Uri? = null,
    result: Intent? = null,
    onResultDialogClose: () -> Unit = {}
) {
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
                onClick = onFabPressed,
                expanded = !installing
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            SessionMode(sessionMode, onSessionModeChoose)
            Packages(installing, packages, onPackageRemove, onPackageChoose, writtenPackages, writingPackage)
            ResultDialog(result, onResultDialogClose)
        }
    }
}

@Composable
private fun SessionMode(mode: Int, onChoose: (Int) -> Unit) {
    Text(
        stringResource(R.string.mode), modifier = Modifier.padding(top = 10.dp, start = 8.dp),
        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary
    )
    RadioButtonItem(R.string.full_install, mode == PackageInstaller.SessionParams.MODE_FULL_INSTALL) {
        onChoose(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    }
    RadioButtonItem(R.string.inherit_existing, mode == PackageInstaller.SessionParams.MODE_INHERIT_EXISTING) {
        onChoose(PackageInstaller.SessionParams.MODE_INHERIT_EXISTING)
    }
}

@Composable
private fun Packages(
    installing: Boolean,
    packages: Set<Uri>, onRemove: (Uri) -> Unit, onChoose: (List<Uri>) -> Unit,
    writtenPackages: Set<Uri>, writingPackage: Uri?
) {
    val chooseSplitPackage = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents(), onChoose)
    Text(
        stringResource(R.string.packages), modifier = Modifier.padding(start = 8.dp, top = 10.dp, bottom = 4.dp),
        style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary
    )
    packages.forEach {
        PackageItem(
            it, installing,
            { onRemove(it) }, it in writtenPackages, it == writingPackage
        )
    }
    if(!installing) Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
            chooseSplitPackage.launch(APK_MIME)
        }.padding(vertical = 12.dp)
    ) {
        Icon(Icons.Default.Add, null, modifier = Modifier.padding(horizontal = 10.dp))
        Text(stringResource(R.string.add_packages), style = MaterialTheme.typography.titleMedium)
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
            for(i in 0..(clipData.itemCount - 1)) {
                packages.update { it + clipData.getItemAt(i).uri }
            }
        }
    }
    val installing = MutableStateFlow(false)
    val result = MutableStateFlow<Intent?>(null)
    val packages = MutableStateFlow(setOf<Uri>())

    val sessionMode = MutableStateFlow(PackageInstaller.SessionParams.MODE_FULL_INSTALL)

    val writtenPackages = MutableStateFlow(setOf<Uri>())
    val writingPackage = MutableStateFlow<Uri?>(null)
    fun startInstallationProcess(activity: FragmentActivity) {
        val sp = SharedPrefs(getApplication<Application>())
        if(sp.auth) startAuth(activity, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                startInstall()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(activity, R.string.failed_to_authenticate, Toast.LENGTH_SHORT).show()
            }
        })
        else startInstall()
    }
    private fun startInstall() {
        if(installing.value) return
        installing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val context = getApplication<Application>()
            val packageInstaller = context.packageManager.packageInstaller
            val sessionId = packageInstaller.createSession(PackageInstaller.SessionParams(sessionMode.value))
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
                        context.startActivity(intent.getParcelableExtra(Intent.EXTRA_INTENT) as Intent?)
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
