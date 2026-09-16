package com.bintianqi.owndroid.feature.settings

import android.content.ComponentName
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.time_blocker.TimeBlockerRepository
import com.bintianqi.owndroid.feature.time_blocker.TimeBlockerService
import com.bintianqi.owndroid.feature.time_blocker.UnlockManager
import com.bintianqi.owndroid.utils.NotificationType
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ShortcutUtils
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.hash
import com.bintianqi.owndroid.utils.plusOrMinus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SettingsViewModel(
    val application: MyApplication, val settingsRepo: SettingsRepository,
    val ph: PrivilegeHelper, val privilegeState: StateFlow<PrivilegeStatus>,
    val toastChannel: ToastChannel, val themeState: MutableStateFlow<MySettings.Theme>,
    val um: UnlockManager, val tbRepo: TimeBlockerRepository
) : ViewModel() {
    fun exportLogs(uri: Uri) {
        application.contentResolver.openOutputStream(uri)?.use { output ->
            val proc = Runtime.getRuntime().exec("logcat -d")
            proc.inputStream.copyTo(output)
            if (Build.VERSION.SDK_INT >= 26) proc.waitFor(2L, TimeUnit.SECONDS)
            else proc.waitFor()
            toastChannel.sendStatus(proc.exitValue() == 0)
        }
    }

    fun setMaterialYou(enabled: Boolean) {
        themeState.update { it.copy(materialYou = enabled) }
        settingsRepo.update { it.theme = it.theme.copy(materialYou = enabled) }
    }

    fun setDarkMode(mode: MySettings.DarkMode) {
        themeState.update { it.copy(dark = mode) }
        settingsRepo.update { it.theme = it.theme.copy(dark = mode) }
    }

    fun setBlackTheme(enabled: Boolean) {
        themeState.update { it.copy(black = enabled) }
        settingsRepo.update { it.theme = it.theme.copy(black = enabled) }
    }

    val dangerousFeaturesState = MutableStateFlow(settingsRepo.data.displayDangerousFeatures)
    val shortcutsState = MutableStateFlow(settingsRepo.data.shortcut.enabled)

    fun setDisplayDangerousFeatures(state: Boolean) {
        settingsRepo.update { it.displayDangerousFeatures = state }
        dangerousFeaturesState.value = state
    }

    fun setShortcutsEnabled(enabled: Boolean) {
        settingsRepo.update { it.shortcut.enabled = enabled }
        ShortcutUtils.setAllShortcuts(application, settingsRepo, ph, enabled)
        shortcutsState.value = enabled
    }

    fun getAppLockConfig() = settingsRepo.data.appLock

    fun setAppLockConfig(password: String, biometrics: Boolean, lockWhenLeaving: Boolean, totp: Boolean) {
        settingsRepo.update {
            if (password.isNotEmpty()) it.appLock.passwordHash = password.hash()
            it.appLock.biometrics = biometrics
            it.appLock.lockWhenLeaving = lockWhenLeaving
            it.appLock.totp = totp
        }
    }

    fun disableAppLock() {
        settingsRepo.update {
            it.appLock.passwordHash = ""
            it.appLock.totp = false
        }
    }

    fun isTotpConfigured() = um.isConfigured

    fun setupTotp(): String = um.setupTotp()

    fun removeTotp() {
        um.removeTotp()
        // Safety: if TOTP secret is removed, disable TOTP app lock so user isn't locked out
        settingsRepo.update { it.appLock.totp = false }
    }

    fun getTotpSecret(): String = um.config.totpSecret

    fun getOtpAuthUri(): String = um.getOtpAuthUri()

    fun getApiEnabled() = settingsRepo.data.api.enabled
    fun getApiKey() = settingsRepo.data.api.key
    fun setApiEnabled(enabled: Boolean, key: String) {
        settingsRepo.update {
            it.api.enabled = enabled
            it.api.key = key
        }
        toastChannel.sendStatus(true)
    }

    val enabledNotifications = MutableStateFlow(emptyList<Int>())
    fun setNotificationEnabled(type: NotificationType, enabled: Boolean) {
        settingsRepo.update {
            it.notifications.clear()
            it.notifications.addAll(enabledNotifications.value)
        }
        enabledNotifications.update {
            it.plusOrMinus(enabled, type.id)
        }
    }

    val hiddenState = MutableStateFlow(false)

    val aliasActivityComponent = ComponentName(
        application.packageName, "com.bintianqi.owndroid.AliasActivity"
    )

    fun getAppHidden() {
        hiddenState.value = application.packageManager.getComponentEnabledSetting(
            aliasActivityComponent
        ) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED
    }

    fun hideApp() {
        application.packageManager.setComponentEnabledSetting(
            aliasActivityComponent,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.DONT_KILL_APP
        )
        hiddenState.value = true
        toastChannel.sendStatus(true)
    }

    fun unhideApp() {
        application.packageManager.setComponentEnabledSetting(
            aliasActivityComponent,
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
            PackageManager.DONT_KILL_APP
        )
        hiddenState.value = false
        toastChannel.sendStatus(true)
    }

    // --- Settings sync (QR transfer) ---

    val syncQrBitmap = MutableStateFlow<android.graphics.Bitmap?>(null)
    val syncExportSummary = MutableStateFlow<Pair<Int, Boolean>?>(null) // rules count, totp included
    val syncImportResult = MutableStateFlow<ImportResult?>(null)

    data class ImportResult(
        val rulesImported: Int, val rulesSkipped: Int, val totpImported: Boolean
    )

    /** Build QR for export. Sets syncQrBitmap or reports failure via toast. */
    fun buildSyncQr(sizePx: Int = 900) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val payload = SyncPayload(
                    totpSecret = um.config.totpSecret,
                    rules = tbRepo.getRules().map { SyncRule.fromBlockRule(it) }
                )
                val bytes = SyncCodec.encode(payload)
                syncQrBitmap.value = QrUtils.encodeToBitmap(bytes, sizePx)
                syncExportSummary.value = payload.rules.size to payload.totpSecret.isNotEmpty()
            } catch (_: SyncCodec.PayloadTooLargeException) {
                syncQrBitmap.value = null
                syncExportSummary.value = null
                toastChannel.sendText(application.getString(R.string.sync_import_too_large))
            } catch (_: Exception) {
                syncQrBitmap.value = null
                syncExportSummary.value = null
                toastChannel.sendStatus(false)
            }
        }
    }

    /**
     * Import a scanned payload: replaces ALL rules, sets TOTP if present.
     * Password hash is never touched (stays local).
     */
    fun importSyncPayload(payload: SyncPayload) {
        viewModelScope.launch(Dispatchers.IO) {
            val (validRules, skipped) = SyncCodec.validate(payload)
            // Full replace, atomic
            tbRepo.replaceAllRules(validRules.map { it.toBlockRule() })

            val totpImported = payload.totpSecret.isNotEmpty() &&
                    SyncCodec.isValidTotpSecret(payload.totpSecret)
            if (totpImported) {
                um.setupTotp(payload.totpSecret)
                settingsRepo.update { it.appLock.totp = true }
            }

            // Start blocker service if rules are now active and it's not running
            if (validRules.any { it.enabled } && !TimeBlockerService.isRunning) {
                TimeBlockerService.start(application)
            }

            syncImportResult.value = ImportResult(validRules.size, skipped, totpImported)
            toastChannel.sendStatus(true)
        }
    }

    fun clearSyncImportResult() {
        syncImportResult.value = null
    }

    fun clearSyncExport() {
        syncQrBitmap.value = null
        syncExportSummary.value = null
    }
}
