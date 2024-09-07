package com.bintianqi.owndroid.encryption

import org.signal.argon2.Argon2
import org.signal.argon2.MemoryCost
import org.signal.argon2.Type
import org.signal.argon2.Version
import java.security.SecureRandom
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class EncryptionManager {
    private val argon2 = Argon2.Builder(Version.V13)
        .type(Type.Argon2id)
        .memoryCost(MemoryCost.MiB(32))
        .parallelism(1)
        .iterations(3)
        .build()

    @OptIn(ExperimentalEncodingApi::class)
    fun makeHash(password: String,): Pair<String,String> {
        val salt = ByteArray(16)
        SecureRandom().nextBytes(salt)
        return argon2.hash(password.toByteArray(),salt).encoded to Base64.encode(salt)
    }

    @OptIn(ExperimentalEncodingApi::class)
    fun checkPassword(password: String, hash: String, salt: String) : Boolean =
        argon2.hash(password.toByteArray(),Base64.decode(salt)).encoded == hash
}