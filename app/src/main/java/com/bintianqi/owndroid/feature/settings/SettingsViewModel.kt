package com.bintianqi.owndroid.feature.settings

import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.NotificationType
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ShortcutUtils
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.hash
import com.bintianqi.owndroid.utils.plusOrMinus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.util.concurrent.TimeUnit

class SettingsViewModel(
    val application: MyApplication, val settingsRepo: SettingsRepository,
    val ph: PrivilegeHelper, val privilegeState: StateFlow<PrivilegeStatus>,
    val toastChannel: ToastChannel, val themeState: MutableStateFlow<MySettings.Theme>
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

    fun setAppLockConfig(password: String, biometrics: Boolean, lockWhenLeaving: Boolean) {
        settingsRepo.update {
            if (password.isNotEmpty()) it.appLock.passwordHash = password.hash()
            it.appLock.biometrics = biometrics
            it.appLock.lockWhenLeaving = lockWhenLeaving
        }
    }

    fun disableAppLock() {
        settingsRepo.update {
            it.appLock.passwordHash = ""
        }
    }

    fun getApiEnabled() = settingsRepo.data.apiKeyHash.isNotEmpty()
    fun setApiKey(key: String) {
        settingsRepo.update {
            it.apiKeyHash = if (key.isEmpty()) "" else key.hash()
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
}
