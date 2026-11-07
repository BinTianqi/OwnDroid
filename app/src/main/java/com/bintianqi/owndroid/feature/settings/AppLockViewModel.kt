package com.bintianqi.owndroid.feature.settings

import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.utils.TotpUtils
import com.bintianqi.owndroid.utils.hash
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

class AppLockViewModel(val settingsRepo: SettingsRepository) : ViewModel() {
    val uiState = MutableStateFlow(AppLockUiState())

    fun getState() {
        val config = settingsRepo.data.appLock
        uiState.value = AppLockUiState(
            config.passwordHash.isNotEmpty(),
            config.totpSecret.isNotEmpty(),
            config.biometrics,
            config.lockWhenLeaving
        )
    }

    fun setPassword(password: String) {
        settingsRepo.update { it.appLock.passwordHash = password.hash() }
        uiState.update { it.copy(password = true) }
    }

    fun removePassword() {
        removedPassword = settingsRepo.data.appLock.passwordHash
        settingsRepo.update { it.appLock.passwordHash = "" }
        uiState.update { it.copy(password = false) }
    }

    var removedPassword = ""

    fun restorePassword() {
        settingsRepo.update { it.appLock.passwordHash = removedPassword }
        uiState.update { it.copy(password = true) }
    }

    fun setBiometricsAllowed(allowed: Boolean) {
        settingsRepo.update { it.appLock.biometrics = allowed }
        uiState.update { it.copy(biometrics = allowed) }
    }

    fun setLockWhenLeaving(lock: Boolean) {
        settingsRepo.update { it.appLock.lockWhenLeaving = lock }
        uiState.update { it.copy(whenLeaving = lock) }
    }

    fun setupTotp() {
        settingsRepo.update { it.appLock.totpSecret = TotpUtils.generateSecret() }
        uiState.update { it.copy(totp = true) }
        setTotpUiState()
    }

    fun removeTotp() {
        removedTotpSecret = settingsRepo.data.appLock.totpSecret
        settingsRepo.update { it.appLock.totpSecret = "" }
        uiState.update { it.copy(totp = false) }
    }

    var removedTotpSecret = ""

    fun restoreTotp() {
        settingsRepo.update { it.appLock.totpSecret = removedTotpSecret }
        uiState.update { it.copy(totp = true) }
    }

    val totpUiState = MutableStateFlow<AppLockTotpUiState?>(null)

    private fun setTotpUiState() {
        val uri = TotpUtils.buildOtpAuthUri(settingsRepo.data.appLock.totpSecret)
        // otpauth URI is ASCII; byte-mode QR via ISO-8859-1 roundtrips it exactly
        totpUiState.value = AppLockTotpUiState(
            uri,
            QrUtils.encodeToBitmap(uri.toByteArray(Charsets.ISO_8859_1), 600)
        )
    }

    fun getTotpUiState() {
        if (totpUiState.value == null) setTotpUiState()
    }
}
