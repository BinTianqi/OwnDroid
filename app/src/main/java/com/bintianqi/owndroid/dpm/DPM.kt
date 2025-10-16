package com.bintianqi.owndroid.dpm

import android.Manifest
import android.annotation.SuppressLint
import android.app.admin.ConnectEvent
import android.app.admin.DevicePolicyManager
import android.app.admin.DnsEvent
import android.app.admin.IDevicePolicyManager
import android.app.admin.SecurityLog
import android.content.Context
import android.content.Intent
import android.content.pm.IPackageInstaller
import android.content.pm.PackageInstaller
import android.os.Build.VERSION
import android.util.Log
import androidx.annotation.RequiresApi
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.NotificationType
import com.bintianqi.owndroid.NotificationUtils
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SP
import com.bintianqi.owndroid.ShortcutUtils
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

@SuppressLint("PrivateApi")
fun binderWrapperDevicePolicyManager(appContext: Context): DevicePolicyManager? {
    try {
        val context = appContext.createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val field = manager.javaClass.getDeclaredField("mService")
        field.isAccessible = true
        val oldInterface = field[manager] as IDevicePolicyManager
        if (oldInterface is DhizukuBinderWrapper) return manager
        val oldBinder = oldInterface.asBinder()
        val newBinder = Dhizuku.binderWrapper(oldBinder)
        val newInterface = IDevicePolicyManager.Stub.asInterface(newBinder)
        field[manager] = newInterface
        return manager
    } catch (e: Exception) {
        e.printStackTrace()
        dhizukuErrorStatus.value = 1
    }
    return null
}

@SuppressLint("PrivateApi")
private fun binderWrapperPackageInstaller(appContext: Context): PackageInstaller? {
    try {
        val context = appContext.createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val installer = context.packageManager.packageInstaller
        val field = installer.javaClass.getDeclaredField("mInstaller")
        field.isAccessible = true
        val oldInterface = field[installer] as IPackageInstaller
        if (oldInterface is DhizukuBinderWrapper) return installer
        val oldBinder = oldInterface.asBinder()
        val newBinder = Dhizuku.binderWrapper(oldBinder)
        val newInterface = IPackageInstaller.Stub.asInterface(newBinder)
        field[installer] = newInterface
        return installer
    } catch (_: Exception) {
        dhizukuErrorStatus.value = 1
    }
    return null
}

fun Context.getPackageInstaller(): PackageInstaller {
    if(SP.dhizuku) {
        if (!dhizukuPermissionGranted()) {
            dhizukuErrorStatus.value = 2
            return this.packageManager.packageInstaller
        }
        return binderWrapperPackageInstaller(this) ?: this.packageManager.packageInstaller
    } else {
        return this.packageManager.packageInstaller
    }
}

val dhizukuErrorStatus = MutableStateFlow(0)

data class PermissionItem(
    val id: String,
    val label: Int,
    val icon: Int,
    val profileOwnerRestricted: Boolean = false,
    val requiresApi: Int = 23
)

@Suppress("InlinedApi")
val runtimePermissions = listOf(
    PermissionItem(Manifest.permission.POST_NOTIFICATIONS, R.string.permission_POST_NOTIFICATIONS, R.drawable.notifications_fill0, requiresApi = 33),
    PermissionItem(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.permission_READ_EXTERNAL_STORAGE, R.drawable.folder_fill0),
    PermissionItem(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.permission_WRITE_EXTERNAL_STORAGE, R.drawable.folder_fill0),
    PermissionItem(Manifest.permission.READ_MEDIA_AUDIO, R.string.permission_READ_MEDIA_AUDIO, R.drawable.music_note_fill0, requiresApi = 33),
    PermissionItem(Manifest.permission.READ_MEDIA_VIDEO, R.string.permission_READ_MEDIA_VIDEO, R.drawable.movie_fill0, requiresApi = 33),
    PermissionItem(Manifest.permission.READ_MEDIA_IMAGES, R.string.permission_READ_MEDIA_IMAGES, R.drawable.image_fill0, requiresApi = 33),
    PermissionItem(Manifest.permission.CAMERA, R.string.permission_CAMERA, R.drawable.photo_camera_fill0, true),
    PermissionItem(Manifest.permission.RECORD_AUDIO, R.string.permission_RECORD_AUDIO, R.drawable.mic_fill0, true),
    PermissionItem(Manifest.permission.ACCESS_COARSE_LOCATION, R.string.permission_ACCESS_COARSE_LOCATION, R.drawable.location_on_fill0, true),
    PermissionItem(Manifest.permission.ACCESS_FINE_LOCATION, R.string.permission_ACCESS_FINE_LOCATION, R.drawable.location_on_fill0, true),
    PermissionItem(Manifest.permission.ACCESS_BACKGROUND_LOCATION, R.string.permission_ACCESS_BACKGROUND_LOCATION, R.drawable.location_on_fill0, true, 29),
    PermissionItem(Manifest.permission.READ_CONTACTS, R.string.permission_READ_CONTACTS, R.drawable.contacts_fill0),
    PermissionItem(Manifest.permission.WRITE_CONTACTS, R.string.permission_WRITE_CONTACTS, R.drawable.contacts_fill0),
    PermissionItem(Manifest.permission.READ_CALENDAR, R.string.permission_READ_CALENDAR, R.drawable.calendar_month_fill0),
    PermissionItem(Manifest.permission.WRITE_CALENDAR, R.string.permission_WRITE_CALENDAR, R.drawable.calendar_month_fill0),
    PermissionItem(Manifest.permission.BLUETOOTH_CONNECT, R.string.permission_BLUETOOTH_CONNECT, R.drawable.bluetooth_fill0, requiresApi = 31),
    PermissionItem(Manifest.permission.BLUETOOTH_SCAN, R.string.permission_BLUETOOTH_SCAN, R.drawable.bluetooth_searching_fill0, requiresApi = 31),
    PermissionItem(Manifest.permission.BLUETOOTH_ADVERTISE, R.string.permission_BLUETOOTH_ADVERTISE, R.drawable.bluetooth_fill0, requiresApi = 31),
    PermissionItem(Manifest.permission.NEARBY_WIFI_DEVICES, R.string.permission_NEARBY_WIFI_DEVICES, R.drawable.wifi_fill0, requiresApi = 33),
    PermissionItem(Manifest.permission.CALL_PHONE, R.string.permission_CALL_PHONE, R.drawable.call_fill0),
    PermissionItem(Manifest.permission.ANSWER_PHONE_CALLS, R.string.permission_ANSWER_PHONE_CALLS, R.drawable.call_fill0, requiresApi = 26),
    PermissionItem(Manifest.permission.READ_PHONE_NUMBERS, R.string.permission_READ_PHONE_STATE, R.drawable.mobile_phone_fill0, requiresApi = 26),
    PermissionItem(Manifest.permission.READ_PHONE_STATE, R.string.permission_READ_PHONE_STATE, R.drawable.mobile_phone_fill0),
    PermissionItem(Manifest.permission.USE_SIP, R.string.permission_USE_SIP, R.drawable.call_fill0),
    PermissionItem(Manifest.permission.UWB_RANGING, R.string.permission_UWB_RANGING, R.drawable.cell_tower_fill0, requiresApi = 31),
    PermissionItem(Manifest.permission.READ_SMS, R.string.permission_READ_SMS, R.drawable.sms_fill0),
    PermissionItem(Manifest.permission.RECEIVE_SMS, R.string.permission_RECEIVE_SMS, R.drawable.sms_fill0),
    PermissionItem(Manifest.permission.SEND_SMS, R.string.permission_SEND_SMS, R.drawable.sms_fill0),
    PermissionItem(Manifest.permission.READ_CALL_LOG, R.string.permission_READ_CALL_LOG, R.drawable.call_log_fill0),
    PermissionItem(Manifest.permission.WRITE_CALL_LOG, R.string.permission_WRITE_CALL_LOG, R.drawable.call_log_fill0),
    PermissionItem(Manifest.permission.RECEIVE_WAP_PUSH, R.string.permission_RECEIVE_WAP_PUSH, R.drawable.wifi_fill0),
    PermissionItem(Manifest.permission.BODY_SENSORS, R.string.permission_BODY_SENSORS, R.drawable.sensors_fill0, true),
    PermissionItem(Manifest.permission.BODY_SENSORS_BACKGROUND, R.string.permission_BODY_SENSORS_BACKGROUND, R.drawable.sensors_fill0, requiresApi = 33),
    PermissionItem(Manifest.permission.ACTIVITY_RECOGNITION, R.string.permission_ACTIVITY_RECOGNITION, R.drawable.history_fill0, true, 29)
).filter { VERSION.SDK_INT >= it.requiresApi }

@RequiresApi(26)
fun handleNetworkLogs(context: Context, batchToken: Long) {
    val networkEvents = Privilege.DPM.retrieveNetworkLogs(Privilege.DAR, batchToken) ?: return
    val file = context.filesDir.resolve("NetworkLogs.json")
    val fileExist = file.exists()
    val json = Json { ignoreUnknownKeys = true; explicitNulls = false }
    val buffer = file.bufferedWriter()
    networkEvents.forEachIndexed { index, event ->
        if(fileExist && index == 0) buffer.write(",")
        val item = buildJsonObject {
            if(VERSION.SDK_INT >= 28) put("id", event.id)
            put("time", event.timestamp)
            put("package", event.packageName)
            if(event is DnsEvent) {
                put("type", "dns")
                put("host", event.hostname)
                put("count", event.totalResolvedAddressCount)
                putJsonArray("addresses") {
                    event.inetAddresses.forEach { inetAddresses ->
                        add(inetAddresses.hostAddress)
                    }
                }
            }
            if(event is ConnectEvent) {
                put("type", "connect")
                put("address", event.inetAddress.hostAddress)
                put("port", event.port)
            }
        }
        buffer.write(json.encodeToString(item))
        if(index < networkEvents.size - 1) buffer.write(",")
    }
    buffer.close()
}

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

fun transformSecurityEventData(tag: Int, payload: Any): SecurityEventData? {
    return when(tag) {
        SecurityLog.TAG_ADB_SHELL_CMD -> SecurityEventData.AdbShellCmd(payload as String)
        SecurityLog.TAG_ADB_SHELL_INTERACTIVE -> null
        SecurityLog.TAG_APP_PROCESS_START -> {
            val data = payload as Array<*>
            SecurityEventData.AppProcessStart(
                data[0] as String, data[1] as Long, data[2] as Int, data[3] as Int,
                data[4] as String, data[5] as String
            )
        }
        SecurityLog.TAG_BACKUP_SERVICE_TOGGLED -> {
            val data = payload as Array<*>
            SecurityEventData.BackupServiceToggled(data[0] as String, data[1] as Int, data[2] as Int)
        }
        SecurityLog.TAG_BLUETOOTH_CONNECTION -> {
            val data = payload as Array<*>
            SecurityEventData.BluetoothConnection(data[0] as String, data[1] as Int, data[2] as String)
        }
        SecurityLog.TAG_BLUETOOTH_DISCONNECTION -> {
            val data = payload as Array<*>
            SecurityEventData.BluetoothDisconnection(data[0] as String, data[1] as String)
        }
        SecurityLog.TAG_CAMERA_POLICY_SET -> {
            val data = payload as Array<*>
            SecurityEventData.CameraPolicySet(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Int
            )
        }
        SecurityLog.TAG_CERT_AUTHORITY_INSTALLED, SecurityLog.TAG_CERT_AUTHORITY_REMOVED -> {
            val data = payload as Array<*>
            SecurityEventData.CaInstalledRemoved(data[0] as Int, data[1] as String, data[2] as Int)
        }
        SecurityLog.TAG_CERT_VALIDATION_FAILURE ->
            SecurityEventData.CertValidationFailure(payload as String)
        SecurityLog.TAG_CRYPTO_SELF_TEST_COMPLETED ->
            SecurityEventData.CryptoSelfTestCompleted(payload as Int)
        SecurityLog.TAG_KEYGUARD_DISABLED_FEATURES_SET -> {
            val data = payload as Array<*>
            SecurityEventData.KeyguardDisabledFeaturesSet(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Int
            )
        }
        SecurityLog.TAG_KEYGUARD_DISMISSED -> null
        SecurityLog.TAG_KEYGUARD_DISMISS_AUTH_ATTEMPT -> {
            val data = payload as Array<*>
            SecurityEventData.KeyguardDismissAuthAttempt(data[0] as Int, data[1] as Int)
        }
        SecurityLog.TAG_KEYGUARD_SECURED -> null
        SecurityLog.TAG_KEY_GENERATED, SecurityLog.TAG_KEY_IMPORT, SecurityLog.TAG_KEY_DESTRUCTION -> {
            val data = payload as Array<*>
            SecurityEventData.KeyGeneratedImportDestruction(
                data[0] as Int, data[1] as String, data[2] as Int
            )
        }
        SecurityLog.TAG_LOGGING_STARTED, SecurityLog.TAG_LOGGING_STOPPED -> null
        SecurityLog.TAG_LOG_BUFFER_SIZE_CRITICAL -> null
        SecurityLog.TAG_MAX_PASSWORD_ATTEMPTS_SET -> {
            val data = payload as Array<*>
            SecurityEventData.MaxPasswordAttemptsSet(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Int
            )
        }
        SecurityLog.TAG_MAX_SCREEN_LOCK_TIMEOUT_SET -> {
            val data = payload as Array<*>
            SecurityEventData.MaxScreenLockTimeoutSet(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Long
            )
        }
        SecurityLog.TAG_MEDIA_MOUNT, SecurityLog.TAG_MEDIA_UNMOUNT -> {
            val data = payload as Array<*>
            SecurityEventData.MediaMountUnmount(data[0] as String, data[1] as String)
        }
        SecurityLog.TAG_NFC_ENABLED, SecurityLog.TAG_NFC_DISABLED -> null
        SecurityLog.TAG_OS_SHUTDOWN -> null
        SecurityLog.TAG_OS_STARTUP -> {
            val data = payload as Array<*>
            SecurityEventData.OsStartup(data[0] as String, data[1] as String)
        }
        SecurityLog.TAG_PACKAGE_INSTALLED, SecurityLog.TAG_PACKAGE_UPDATED,
        SecurityLog.TAG_PACKAGE_UNINSTALLED -> {
            val data = payload as Array<*>
            SecurityEventData.PackageInstalledUninstalledUpdated(
                data[0] as String, data[1] as Long, data[2] as Int
            )
        }
        SecurityLog.TAG_PASSWORD_CHANGED -> {
            val data = payload as Array<*>
            SecurityEventData.PasswordChanged(data[0] as Int, data[1] as Int)
        }
        SecurityLog.TAG_PASSWORD_COMPLEXITY_REQUIRED -> {
            val data = payload as Array<*>
            SecurityEventData.PasswordComplexityRequired(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Int
            )
        }
        SecurityLog.TAG_PASSWORD_COMPLEXITY_SET -> {
            val data = payload as Array<*>
            SecurityEventData.PasswordComplexitySet(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Int, data[4] as Int,
                data[5] as Int, data[6] as Int, data[7] as Int, data[8] as Int, data[9] as Int,
                data[10] as Int
            )
        }
        SecurityLog.TAG_PASSWORD_EXPIRATION_SET -> {
            val data = payload as Array<*>
            SecurityEventData.PasswordExpirationSet(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Long
            )
        }
        SecurityLog.TAG_PASSWORD_HISTORY_LENGTH_SET -> {
            val data = payload as Array<*>
            SecurityEventData.PasswordHistoryLengthSet(
                data[0] as String, data[1] as Int, data[2] as Int, data[3] as Int
            )
        }
        SecurityLog.TAG_REMOTE_LOCK -> {
            val data = payload as Array<*>
            SecurityEventData.RemoteLock(data[0] as String, data[1] as Int, data[2] as Int)
        }
        SecurityLog.TAG_SYNC_RECV_FILE, SecurityLog.TAG_SYNC_SEND_FILE ->
            SecurityEventData.SyncRecvSendFile(payload as String)
        SecurityLog.TAG_USER_RESTRICTION_ADDED, SecurityLog.TAG_USER_RESTRICTION_REMOVED -> {
            val data = payload as Array<*>
            SecurityEventData.UserRestrictionAddedRemoved(
                data[0] as String, data[1] as Int, data[2] as String
            )
        }
        SecurityLog.TAG_WIFI_CONNECTION -> {
            val data = payload as Array<*>
            SecurityEventData.WifiConnection(data[0] as String, data[1] as String, data[2] as String)
        }
        SecurityLog.TAG_WIFI_DISCONNECTION -> {
            val data = payload as Array<*>
            SecurityEventData.WifiDisconnection(data[0] as String, data[1] as String)
        }
        SecurityLog.TAG_WIPE_FAILURE -> null
        else -> null
    }
}

@RequiresApi(24)
fun retrieveSecurityLogs(app: MyApplication) {
    CoroutineScope(Dispatchers.IO).launch {
        val logs = Privilege.DPM.retrieveSecurityLogs(Privilege.DAR) ?: return@launch
        app.myRepo.writeSecurityLogs(logs)
        NotificationUtils.sendBasicNotification(
            app, NotificationType.SecurityLogsCollected,
            app.getString(R.string.n_logs_in_total, logs.size)
        )
    }
}

fun setDefaultAffiliationID() {
    if (VERSION.SDK_INT < 26) return
    if(!SP.isDefaultAffiliationIdSet) {
        try {
            Privilege.DPM.setAffiliationIds(Privilege.DAR, setOf("OwnDroid_default_affiliation_id"))
            SP.isDefaultAffiliationIdSet = true
            Log.d("DPM", "Default affiliation id set")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

fun dhizukuPermissionGranted() =
    try {
        Dhizuku.isPermissionGranted()
    } catch(_: Exception) {
        false
    }

fun parsePackageInstallerMessage(context: Context, result: Intent): String {
    val status = result.getIntExtra(PackageInstaller.EXTRA_STATUS, 999)
    val statusMessage = result.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
    val otherPackageName = result.getStringExtra(PackageInstaller.EXTRA_OTHER_PACKAGE_NAME)
    return when(status) {
        PackageInstaller.STATUS_FAILURE_BLOCKED ->
            context.getString(
                R.string.status_failure_blocked,
                otherPackageName ?: context.getString(R.string.unknown)
            )
        PackageInstaller.STATUS_FAILURE_ABORTED ->
            context.getString(R.string.status_failure_aborted)
        PackageInstaller.STATUS_FAILURE_INVALID ->
            context.getString(R.string.status_failure_invalid)
        PackageInstaller.STATUS_FAILURE_CONFLICT ->
            context.getString(R.string.status_failure_conflict, otherPackageName ?: "???")
        PackageInstaller.STATUS_FAILURE_STORAGE ->
            context.getString(R.string.status_failure_storage) +
                    result.getStringExtra(PackageInstaller.EXTRA_STORAGE_PATH).let { if(it == null) "" else "\n$it" }
        PackageInstaller.STATUS_FAILURE_INCOMPATIBLE ->
            context.getString(R.string.status_failure_incompatible)
        PackageInstaller.STATUS_FAILURE_TIMEOUT ->
            context.getString(R.string.timeout)
        else -> ""
    } + statusMessage.let { if(it == null) "" else "\n$it" }
}


fun handlePrivilegeChange(context: Context) {
    val privilege = Privilege.status.value
    SP.dhizukuServer = false
    SP.shortcuts = privilege.activated
    if (privilege.activated) {
        ShortcutUtils.setAllShortcuts(context, true)
        if (!privilege.dhizuku) {
            setDefaultAffiliationID()
        }
    } else {
        SP.isDefaultAffiliationIdSet = false
        ShortcutUtils.setAllShortcuts(context, false)
        SP.apiKeyHash = ""
    }
}
