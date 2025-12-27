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
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

class AppInstallerViewModel(application: Application): AndroidViewModel(application) {
    val uiState = MutableStateFlow(UiState())
    data class UiState(
        val packages: List<Uri> = emptyList(),
        val installing: Boolean = false,
        val packageWriting: Int = -1,
        val result: Intent? = null
    )

    fun initialize(intent: Intent) {
        val list = mutableListOf<Uri>()
        intent.data?.let { list += it }
        intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { list += it }
        intent.getParcelableArrayExtra(Intent.EXTRA_STREAM)?.forEach { list += it as Uri }
        intent.clipData?.let { clipData ->
            for(i in 0..<clipData.itemCount) {
                list += clipData.getItemAt(i).uri
            }
        }
        uiState.update { it.copy(it.packages + list.distinct()) }
    }

    fun registerInstallerReceiver(context: Context) {
        ContextCompat.registerReceiver(
            context, Receiver(), IntentFilter(ACTION), ContextCompat.RECEIVER_NOT_EXPORTED
        )
    }

    fun onPackagesAdd(packages: List<Uri>) {
        uiState.update {
            it.copy(packages = it.packages.plus(packages).distinct())
        }
    }

    fun onPackageRemove(uri: Uri) {
        uiState.update {
            it.copy(packages = it.packages.minus(uri))
        }
    }

    private fun getSessionParams(options: SessionParamsOptions): PackageInstaller.SessionParams {
        return PackageInstaller.SessionParams(options.mode).apply {
            if(Build.VERSION.SDK_INT >= 34) {
                if(options.keepOriginalEnabledSetting) setApplicationEnabledSettingPersistent()
                setDontKillApp(options.noKill)
            }
            setInstallLocation(options.location)
        }
    }

    fun startInstall(options: SessionParamsOptions) {
        if (uiState.value.installing) return
        viewModelScope.launch(Dispatchers.IO) {
            installPackages(options)
        }
    }

    private fun installPackages(options: SessionParamsOptions) {
        val packageInstaller = application.packageManager.packageInstaller
        val sessionId = packageInstaller.createSession(getSessionParams(options))
        val session = packageInstaller.openSession(sessionId)
        try {
            uiState.update { it.copy(packageWriting = 0) }
            uiState.value.packages.forEach { uri ->
                session.openWrite(uri.hashCode().toString(), 0, -1).use { splitPackageOut ->
                    application.contentResolver.openInputStream(uri)!!.use { splitPackageIn ->
                        splitPackageIn.copyTo(splitPackageOut)
                    }
                    session.fsync(splitPackageOut)
                }
                uiState.update { it.copy(packageWriting = it.packageWriting + 1) }
            }
        } catch(e: Exception) {
            e.printStackTrace()
            session.abandon()
            uiState.update { it.copy(installing = false, packageWriting = -1) }
            return
        }
        val intent = Intent(ACTION).setPackage(application.packageName)
        val pi = if(Build.VERSION.SDK_INT >= 34) {
            PendingIntent.getBroadcast(
                application, sessionId, intent,
                PendingIntent.FLAG_ALLOW_UNSAFE_IMPLICIT_INTENT or PendingIntent.FLAG_MUTABLE
            ).intentSender
        } else {
            PendingIntent.getBroadcast(application, sessionId, intent, PendingIntent.FLAG_MUTABLE).intentSender
        }
        session.commit(pi)
    }

    inner class Receiver() : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val statusExtra = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
            if (statusExtra == PackageInstaller.STATUS_PENDING_USER_ACTION) {
                @SuppressWarnings("UnsafeIntentLaunch")
                context.startActivity(
                    (intent.getParcelableExtra(Intent.EXTRA_INTENT) as Intent?)
                        ?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                uiState.update { it.copy(result = intent) }
            }
        }
    }

    fun closeResultDialog() {
        if (uiState.value.result?.getIntExtra(PackageInstaller.EXTRA_STATUS, 999) == PackageInstaller.STATUS_SUCCESS) {
            uiState.update { it.copy(emptyList(), packageWriting = -1, result = null) }
        } else {
            uiState.update { it.copy(packageWriting = -1, result = null) }
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

@Serializable
data class SessionParamsOptions(
    val mode: Int = PackageInstaller.SessionParams.MODE_FULL_INSTALL,
    val keepOriginalEnabledSetting: Boolean = false,
    val noKill: Boolean = false,
    val location: Int = PackageInfo.INSTALL_LOCATION_INTERNAL_ONLY,
)
