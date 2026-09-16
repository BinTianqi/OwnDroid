package com.bintianqi.owndroid.feature.settings

import com.bintianqi.owndroid.feature.time_blocker.TotpUtils
import kotlinx.serialization.json.Json
import java.util.zip.Deflater
import java.util.zip.Inflater

/**
 * Encodes/decodes sync payloads for QR transfer.
 * Pipeline: JSON (compact keys) -> deflate -> raw bytes in QR byte mode.
 */
object SyncCodec {
    const val FORMAT_VERSION = 1

    // QR byte-mode capacity at error correction level M (~version 40)
    const val MAX_QR_BYTES = 2331

    // Sanity bound: no legitimate config has this many rules
    const val MAX_RULES = 500

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = false
    }

    class PayloadTooLargeException(val size: Int) : Exception("Payload too large: $size bytes")
    class InvalidPayloadException : Exception("Invalid sync payload")

    fun encode(payload: SyncPayload): ByteArray {
        val jsonBytes = json.encodeToString(payload).toByteArray(Charsets.UTF_8)
        val deflater = Deflater(Deflater.BEST_COMPRESSION)
        deflater.setInput(jsonBytes)
        deflater.finish()
        val buffer = ByteArray(jsonBytes.size + 64)
        val length = deflater.deflate(buffer)
        deflater.end()
        val compressed = buffer.copyOf(length)
        if (compressed.size > MAX_QR_BYTES) throw PayloadTooLargeException(compressed.size)
        return compressed
    }

    fun decode(compressed: ByteArray): SyncPayload {
        val inflater = Inflater()
        inflater.setInput(compressed)
        // Bound: compressed <= ~2.9 KB (QR max); refuse zip-bomb style expansion
        val buffer = ByteArray(256 * 1024)
        val length: Int
        val finished: Boolean
        try {
            length = inflater.inflate(buffer)
            finished = inflater.finished()
        } catch (_: Exception) {
            throw InvalidPayloadException()
        } finally {
            inflater.end()
        }
        if (!finished || length <= 0) throw InvalidPayloadException()
        val payload = try {
            json.decodeFromString<SyncPayload>(buffer.copyOf(length).toString(Charsets.UTF_8))
        } catch (_: Exception) {
            throw InvalidPayloadException()
        }
        if (payload.version != FORMAT_VERSION) throw InvalidPayloadException()
        if (payload.rules.size > MAX_RULES) throw InvalidPayloadException()
        return payload
    }

    /**
     * Filter out invalid rules; return count of skipped rules.
     */
    fun validate(payload: SyncPayload): Pair<List<SyncRule>, Int> {
        val valid = payload.rules.filter { rule ->
            rule.packageName.isNotBlank() && rule.dailyLimitMinutes >= 0 &&
                    rule.blockedWindows.all { it.isValid() } &&
                    rule.allowedWindows.all { it.isValid() }
        }
        return valid to (payload.rules.size - valid.size)
    }

    fun isValidTotpSecret(secret: String): Boolean {
        if (secret.isEmpty()) return true // empty = unchanged
        return try {
            // RFC 4226 minimum is 80 bits (10 bytes); standard TOTP secrets are 20 bytes
            TotpUtils.decodeBase32(secret).size >= 10
        } catch (_: Exception) {
            false
        }
    }
}
