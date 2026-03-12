package com.bintianqi.owndroid.feature.system

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
class SecurityEvent(
    val id: Long?, val tag: Int, val level: Int?, val time: Long, val data: JsonObject?
)

@Serializable
class SecurityEventWithData(
    val id: Long?, val tag: Int, val level: Int?, val time: Long, val data: SecurityEventData?
)

@Serializable
sealed class SecurityEventData {
    @Serializable
    class AdbShellCmd(val command: String): SecurityEventData()
    @Serializable
    class AppProcessStart(
        val name: String,
        val time: Long,
        val uid: Int,
        val pid: Int,
        val seinfo: String,
        val hash: String
    ): SecurityEventData()
    @Serializable
    class BackupServiceToggled(
        val admin: String,
        val user: Int,
        val state: Int
    ): SecurityEventData()
    @Serializable
    class BluetoothConnection(
        val mac: String,
        val successful: Int,
        @SerialName("failure_reason") val failureReason: String
    ): SecurityEventData()
    @Serializable
    class BluetoothDisconnection(
        val mac: String,
        val reason: String
    ): SecurityEventData()
    @Serializable
    class CameraPolicySet(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val disabled: Int
    ): SecurityEventData()
    @Serializable
    class CaInstalledRemoved(
        val result: Int,
        val subject: String,
        val user: Int
    ): SecurityEventData()
    @Serializable
    class CertValidationFailure(val reason: String): SecurityEventData()
    @Serializable
    class CryptoSelfTestCompleted(val result: Int): SecurityEventData()
    @Serializable
    class KeyguardDisabledFeaturesSet(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val mask: Int
    ): SecurityEventData()
    @Serializable
    class KeyguardDismissAuthAttempt(
        val result: Int,
        val strength: Int
    ): SecurityEventData()
    @Serializable
    class KeyGeneratedImportDestruction(
        val result: Int,
        val alias: String,
        val uid: Int
    ): SecurityEventData()
    @Serializable
    class KeyIntegrityViolation(
        val alias: String,
        val uid: Int
    ): SecurityEventData()
    @Serializable
    class MaxPasswordAttemptsSet(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val value: Int
    ): SecurityEventData()
    @Serializable
    class MaxScreenLockTimeoutSet(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val timeout: Long
    ): SecurityEventData()
    @Serializable
    class MediaMountUnmount(
        @SerialName("mount_point") val mountPoint: String,
        val label: String
    ): SecurityEventData()
    @Serializable
    class OsStartup(
        @SerialName("verified_boot_state") val verifiedBootState: String,
        @SerialName("dm_verity_mode") val dmVerityMode: String
    ): SecurityEventData()
    @Serializable
    class PackageInstalledUninstalledUpdated(
        val name: String,
        val version: Long,
        val user: Int
    ): SecurityEventData()
    @Serializable
    class PasswordChanged(
        val complexity: Int,
        val user: Int
    ): SecurityEventData()
    @Serializable
    class PasswordComplexityRequired(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val complexity: Int
    ): SecurityEventData()
    @Serializable
    class PasswordComplexitySet(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val length: Int,
        val quality: Int,
        val letters: Int,
        @SerialName("non_letters") val nonLetters: Int,
        val digits: Int,
        val uppercase: Int,
        val lowercase: Int,
        val symbols: Int
    ): SecurityEventData()
    @Serializable
    class PasswordExpirationSet(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val expiration: Long
    ): SecurityEventData()
    @Serializable
    class PasswordHistoryLengthSet(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
        val length: Int
    ): SecurityEventData()
    @Serializable
    class RemoteLock(
        val admin: String,
        @SerialName("admin_user") val adminUser: Int,
        @SerialName("target_user") val targetUser: Int,
    ): SecurityEventData()
    @Serializable
    class SyncRecvSendFile(val path: String): SecurityEventData()
    @Serializable
    class UserRestrictionAddedRemoved(
        val admin: String,
        val user: Int,
        val restriction: String
    ): SecurityEventData()
    @Serializable
    class WifiConnection(
        val bssid: String,
        val type: String,
        @SerialName("failure_reason") val failureReason: String
    ): SecurityEventData()
    @Serializable
    class WifiDisconnection(
        val bssid: String,
        val reason: String
    ): SecurityEventData()
}
