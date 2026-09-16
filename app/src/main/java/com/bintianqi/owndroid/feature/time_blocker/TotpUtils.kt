package com.bintianqi.owndroid.feature.time_blocker

import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.math.pow

/**
 * Minimal TOTP implementation (RFC 6238).
 * Compatible with Google Authenticator and other standard TOTP apps.
 */
object TotpUtils {
    private const val DIGITS = 6
    private const val PERIOD = 30L // seconds

    /**
     * Generate the current TOTP code for the given Base32-encoded secret.
     */
    fun generateCode(base32Secret: String, timeMillis: Long = System.currentTimeMillis()): String {
        val key = decodeBase32(base32Secret)
        val counter = timeMillis / 1000 / PERIOD
        val hash = hmacSha1(key, counterToBytes(counter))
        val offset = hash[hash.size - 1].toInt() and 0x0F
        val binary = ((hash[offset].toInt() and 0x7F) shl 24) or
                ((hash[offset + 1].toInt() and 0xFF) shl 16) or
                ((hash[offset + 2].toInt() and 0xFF) shl 8) or
                (hash[offset + 3].toInt() and 0xFF)
        val otp = binary % 10.0.pow(DIGITS).toInt()
        return otp.toString().padStart(DIGITS, '0')
    }

    /**
     * Verify a TOTP code with ±1 window tolerance (covers clock skew).
     */
    fun verifyCode(base32Secret: String, code: String, timeMillis: Long = System.currentTimeMillis()): Boolean {
        if (code.length != DIGITS) return false
        for (offset in -1..1) {
            val t = timeMillis + offset * PERIOD * 1000
            if (generateCode(base32Secret, t) == code) return true
        }
        return false
    }

    /**
     * Generate a random Base32-encoded secret (160 bits = 20 bytes, standard for TOTP).
     */
    fun generateSecret(): String {
        val bytes = ByteArray(20)
        java.security.SecureRandom().nextBytes(bytes)
        return encodeBase32(bytes)
    }

    /**
     * Build a standard otpauth:// URI for QR code generation.
     */
    fun buildOtpAuthUri(secret: String, accountName: String = "OwnDroid TimeBlocker", issuer: String = "OwnDroid"): String {
        return "otpauth://totp/${android.net.Uri.encode(issuer)}:${android.net.Uri.encode(accountName)}" +
                "?secret=$secret&issuer=${android.net.Uri.encode(issuer)}&algorithm=SHA1&digits=$DIGITS&period=$PERIOD"
    }

    private fun hmacSha1(key: ByteArray, data: ByteArray): ByteArray {
        val mac = Mac.getInstance("HmacSHA1")
        mac.init(SecretKeySpec(key, "HmacSHA1"))
        return mac.doFinal(data)
    }

    private fun counterToBytes(counter: Long): ByteArray {
        val bytes = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            bytes[i] = (value and 0xFF).toByte()
            value = value shr 8
        }
        return bytes
    }

    // Manual Base32 encode/decode (RFC 4648) to avoid platform dependency
    private const val BASE32_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567"

    fun encodeBase32(data: ByteArray): String {
        val sb = StringBuilder()
        var buffer = 0
        var bitsLeft = 0
        for (byte in data) {
            buffer = (buffer shl 8) or (byte.toInt() and 0xFF)
            bitsLeft += 8
            while (bitsLeft >= 5) {
                sb.append(BASE32_CHARS[(buffer shr (bitsLeft - 5)) and 0x1F])
                bitsLeft -= 5
            }
        }
        if (bitsLeft > 0) {
            sb.append(BASE32_CHARS[(buffer shl (5 - bitsLeft)) and 0x1F])
        }
        return sb.toString()
    }

    fun decodeBase32(encoded: String): ByteArray {
        val cleaned = encoded.uppercase().replace("=", "").replace(" ", "")
        val output = mutableListOf<Byte>()
        var buffer = 0
        var bitsLeft = 0
        for (c in cleaned) {
            val value = BASE32_CHARS.indexOf(c)
            if (value < 0) continue
            buffer = (buffer shl 5) or value
            bitsLeft += 5
            if (bitsLeft >= 8) {
                output.add((buffer shr (bitsLeft - 8) and 0xFF).toByte())
                bitsLeft -= 8
            }
        }
        return output.toByteArray()
    }
}
