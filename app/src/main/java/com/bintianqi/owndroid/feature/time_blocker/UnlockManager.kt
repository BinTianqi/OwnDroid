package com.bintianqi.owndroid.feature.time_blocker

import com.bintianqi.owndroid.utils.TotpUtils
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * Pure TOTP secret store + verifier.
 * - TOTP secret is stored persistently in a JSON file.
 */
class UnlockManager(private val file: File) {
    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    @Serializable
    data class UnlockConfig(
        var totpSecret: String = ""
    )

    var config: UnlockConfig
        private set

    init {
        config = if (file.exists()) {
            try { json.decodeFromString(file.readText()) } catch (_: Exception) { UnlockConfig() }
        } else {
            UnlockConfig()
        }
    }

    val isConfigured: Boolean
        get() = config.totpSecret.isNotEmpty()

    /**
     * Set up TOTP. Returns the secret (Base32-encoded) for the user to add to their authenticator.
     */
    fun setupTotp(secret: String = TotpUtils.generateSecret()): String {
        config = config.copy(totpSecret = secret)
        save()
        return secret
    }

    /**
     * Remove TOTP configuration. This is itself a protected operation.
     */
    fun removeTotp() {
        config = config.copy(totpSecret = "")
        save()
    }

    /**
     * Verify a TOTP code.
     */
    fun verifyCode(code: String): Boolean {
        if (!isConfigured) return false
        return TotpUtils.verifyCode(config.totpSecret, code)
    }

    /**
     * Get the otpauth:// URI for QR code display.
     */
    fun getOtpAuthUri(): String {
        return TotpUtils.buildOtpAuthUri(config.totpSecret)
    }

    private fun save() {
        file.writeText(json.encodeToString(config))
    }
}
