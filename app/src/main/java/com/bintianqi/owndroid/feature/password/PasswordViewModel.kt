package com.bintianqi.owndroid.feature.password

import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class PasswordViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val settingsRepo: SettingsRepository,
    val privilegeState: StateFlow<PrivilegeStatus>, val toastChannel: ToastChannel
) : ViewModel() {
    fun getDisplayDangerousFeatures() = settingsRepo.data.displayDangerousFeatures

    val passwordInfoState = MutableStateFlow(PasswordInfo())

    fun getPasswordInfo() = ph.safeDpmCall {
        val privilege = privilegeState.value
        passwordInfoState.value = PasswordInfo(
            complexity = if (VERSION.SDK_INT >= 31) dpm.passwordComplexity else 0,
            complexitySufficient = dpm.isActivePasswordSufficient,
            unified =
                if (VERSION.SDK_INT >= 28 && privilege.work) dpm.isUsingUnifiedPassword(dar)
                else false
        )
    }

    // Reset password token
    val rpTokenState = MutableStateFlow(RpTokenState())

    @RequiresApi(26)
    fun getRpTokenState() {
        rpTokenState.value = try {
            var active = false
            ph.safeDpmCall {
                active = dpm.isResetPasswordTokenActive(dar)
            }
            RpTokenState(true, active)
        } catch (_: IllegalStateException) {
            RpTokenState(false, false)
        }
    }

    @RequiresApi(26)
    fun setRpToken(token: String) {
        try {
            ph.safeDpmCall {
                val result = dpm.setResetPasswordToken(dar, token.encodeToByteArray())
                toastChannel.sendStatus(result)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            toastChannel.sendStatus(false)
        }
        getRpTokenState()
    }

    @RequiresApi(26)
    fun clearRpToken() = ph.safeDpmCall {
        val result = dpm.clearResetPasswordToken(dar)
        toastChannel.sendStatus(result)
        getRpTokenState()
    }

    @RequiresApi(26)
    fun createActivateRpTokenIntent(): Intent? {
        val km = application.getSystemService(KeyguardManager::class.java)
        val title = application.getString(R.string.activate_reset_password_token)
        return km.createConfirmDeviceCredentialIntent(title, "")
    }

    fun resetPassword(password: String, token: String, flags: Int) = ph.safeDpmCall {
        val result = if (VERSION.SDK_INT >= 26) {
            dpm.resetPasswordWithToken(dar, password, token.encodeToByteArray(), flags)
        } else {
            dpm.resetPassword(password, flags)
        }
        toastChannel.sendStatus(result)
    }

    val requiredComplexityState = MutableStateFlow(0)

    @RequiresApi(31)
    fun getRequiredComplexity() = ph.safeDpmCall {
        requiredComplexityState.value = dpm.requiredPasswordComplexity
    }

    fun setRequiredComplexity(complexity: Int) = ph.safeDpmCall {
        requiredComplexityState.value = complexity
    }

    @RequiresApi(31)
    fun applyRequiredComplexity() = ph.safeDpmCall {
        dpm.requiredPasswordComplexity = requiredComplexityState.value
        toastChannel.sendStatus(true)
    }

    val keyguardDisableState = MutableStateFlow(KeyguardDisableConfig())

    fun getKeyguardDisableConfig() = ph.safeDpmCall {
        val flags = dpm.getKeyguardDisabledFeatures(dar)
        val mode = when (flags) {
            DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE -> KeyguardDisableMode.None
            DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL -> KeyguardDisableMode.All
            else -> KeyguardDisableMode.Custom
        }
        keyguardDisableState.value = KeyguardDisableConfig(mode, flags)
    }

    fun setKeyguardDisableConfig(config: KeyguardDisableConfig) {
        keyguardDisableState.value = config
    }

    fun applyKeyguardDisableConfig() = ph.safeDpmCall {
        val flags = when (keyguardDisableState.value.mode) {
            KeyguardDisableMode.None -> DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE
            KeyguardDisableMode.All -> DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL
            else -> keyguardDisableState.value.flags
        }
        dpm.setKeyguardDisabledFeatures(dar, flags)
        toastChannel.sendStatus(true)
    }

    val maxTimeToLockState = MutableStateFlow("")

    fun getMaxTimeToLock() = ph.safeDpmCall {
        maxTimeToLockState.value = dpm.getMaximumTimeToLock(dar).toString()
    }

    fun setMaxTimeToLock(time: String) {
        maxTimeToLockState.value = time
    }

    fun applyMaxTimeToLock() = ph.safeDpmCall {
        dpm.setMaximumTimeToLock(dar, maxTimeToLockState.value.toLong())
    }

    val strongAutoTimeoutState = MutableStateFlow("")

    @RequiresApi(26)
    fun getStrongAuthTimeout() = ph.safeDpmCall {
        strongAutoTimeoutState.value = dpm.getRequiredStrongAuthTimeout(dar).toString()
    }

    fun setStrongAuthTimeout(time: String) {
        strongAutoTimeoutState.value = time
    }

    @RequiresApi(26)
    fun applyStrongAuthTimeout() = ph.safeDpmCall {
        dpm.setRequiredStrongAuthTimeout(dar, strongAutoTimeoutState.value.toLong())
    }

    val expirationTimeoutState = MutableStateFlow("")

    fun getExpirationTimeout() = ph.safeDpmCall {
        expirationTimeoutState.value = dpm.getPasswordExpirationTimeout(dar).toString()
    }

    fun setExpirationTimeout(time: String) {
        expirationTimeoutState.value = time
    }

    fun applyExpirationTimeout() = ph.safeDpmCall {
        dpm.setPasswordExpirationTimeout(dar, expirationTimeoutState.value.toLong())
    }

    val maxFailedForWipeState = MutableStateFlow("")

    fun getMaxFailedForWipe() = ph.safeDpmCall {
        maxFailedForWipeState.value = dpm.getMaximumFailedPasswordsForWipe(dar).toString()
    }

    fun setMaxFailedForWipe(times: String) {
        maxFailedForWipeState.value = times
    }

    fun applyMaxFiledForWipe() = ph.safeDpmCall {
        dpm.setMaximumFailedPasswordsForWipe(dar, maxFailedForWipeState.value.toInt())
    }

    val historyLengthState = MutableStateFlow("")

    fun getHistoryLength() = ph.safeDpmCall {
        historyLengthState.value = dpm.getPasswordHistoryLength(dar).toString()
    }

    fun setHistoryLength(length: String) {
        historyLengthState.value = length
    }

    fun applyHistoryLength() = ph.safeDpmCall {
        dpm.setPasswordHistoryLength(dar, historyLengthState.value.toInt())
    }

    val qualityState = MutableStateFlow(0)

    fun getQuality() = ph.safeDpmCall {
        qualityState.value = dpm.getPasswordQuality(dar)
    }

    fun setQuality(level: Int) {
        qualityState.value = level
    }

    fun applyQuality() = ph.safeDpmCall {
        dpm.setPasswordQuality(dar, qualityState.value)
        toastChannel.sendStatus(true)
    }
}