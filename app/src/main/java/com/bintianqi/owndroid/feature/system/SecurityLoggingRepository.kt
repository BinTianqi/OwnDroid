package com.bintianqi.owndroid.feature.system

import android.app.admin.SecurityLog
import android.database.DatabaseUtils
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.database.getStringOrNull
import com.bintianqi.owndroid.MyDbHelper
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import java.io.OutputStream

class SecurityLoggingRepository(val dbHelper: MyDbHelper) {
    fun getSecurityLogsCount(): Long {
        return DatabaseUtils.queryNumEntries(dbHelper.readableDatabase, "security_logs")
    }

    @OptIn(ExperimentalSerializationApi::class)
    @RequiresApi(24)
    fun writeSecurityLogs(events: List<SecurityLog.SecurityEvent>) {
        val db = dbHelper.writableDatabase
        val json = Json {
            classDiscriminatorMode = ClassDiscriminatorMode.NONE
        }
        val statement = db.compileStatement("INSERT INTO security_logs VALUES (?, ?, ?, ?, ?)")
        db.beginTransaction()
        events.forEach { event ->
            try {
                if (Build.VERSION.SDK_INT >= 28) {
                    statement.bindLong(1, event.id)
                    statement.bindLong(3, event.logLevel.toLong())
                } else {
                    statement.bindNull(1)
                    statement.bindNull(3)
                }
                statement.bindLong(2, event.tag.toLong())
                statement.bindLong(4, event.timeNanos / 1000000)
                val dataObject = transformSecurityEventData(event.tag, event.data)
                if (dataObject == null) {
                    statement.bindNull(5)
                } else {
                    statement.bindString(5, json.encodeToString(dataObject))
                }
                statement.executeInsert()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                statement.clearBindings()
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
        statement.close()
    }

    fun exportSecurityLogs(stream: OutputStream) {
        var offset = 0
        val json = Json {
            explicitNulls = false
        }
        var addComma = false
        val bw = stream.bufferedWriter()
        bw.write("[")
        while (true) {
            dbHelper.readableDatabase.rawQuery(
                "SELECT * FROM security_logs LIMIT ? OFFSET ?",
                arrayOf(100.toString(), offset.toString())
            ).use { cursor ->
                if (cursor.count == 0) {
                    break
                }
                while (cursor.moveToNext()) {
                    if (addComma) bw.write(",")
                    addComma = true
                    val event = SecurityEvent(
                        cursor.getLong(0), cursor.getInt(1), cursor.getInt(2), cursor.getLong(3),
                        cursor.getStringOrNull(4)?.let { json.decodeFromString(it) }
                    )
                    bw.write(json.encodeToString(event))
                }
                offset += 100
            }
        }
        bw.write("]")
        bw.close()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @RequiresApi(24)
    fun exportPRSecurityLogs(logs: List<SecurityLog.SecurityEvent>, stream: OutputStream) {
        val bw = stream.bufferedWriter()
        bw.write("[")
        val json = Json {
            explicitNulls = false
            classDiscriminatorMode = ClassDiscriminatorMode.NONE
        }
        var addComma = false
        logs.forEach { log ->
            try {
                if (addComma) bw.write(",")
                addComma = true
                val event = SecurityEventWithData(
                    if (Build.VERSION.SDK_INT >= 28) log.id else null, log.tag,
                    if (Build.VERSION.SDK_INT >= 28) log.logLevel else null,
                    log.timeNanos / 1000000,
                    transformSecurityEventData(log.tag, log.data)
                )
                bw.write(json.encodeToString(event))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        bw.write("]")
        bw.close()
    }

    fun deleteSecurityLogs() {
        dbHelper.writableDatabase.execSQL("DELETE FROM security_logs")
    }

    companion object {
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
                    SecurityEventData.BackupServiceToggled(
                        data[0] as String, data[1] as Int, data[2] as Int
                    )
                }
                SecurityLog.TAG_BLUETOOTH_CONNECTION -> {
                    val data = payload as Array<*>
                    SecurityEventData.BluetoothConnection(
                        data[0] as String, data[1] as Int, data[2] as String
                    )
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
                    SecurityEventData.CaInstalledRemoved(
                        data[0] as Int, data[1] as String, data[2] as Int
                    )
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
                SecurityLog.TAG_KEY_GENERATED, SecurityLog.TAG_KEY_IMPORT,
                SecurityLog.TAG_KEY_DESTRUCTION -> {
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
                        data[0] as String, data[1] as Int, data[2] as Int, data[3] as Int,
                        data[4] as Int, data[5] as Int, data[6] as Int, data[7] as Int,
                        data[8] as Int, data[9] as Int, data[10] as Int
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
                SecurityLog.TAG_USER_RESTRICTION_ADDED,
                SecurityLog.TAG_USER_RESTRICTION_REMOVED -> {
                    val data = payload as Array<*>
                    SecurityEventData.UserRestrictionAddedRemoved(
                        data[0] as String, data[1] as Int, data[2] as String
                    )
                }
                SecurityLog.TAG_WIFI_CONNECTION -> {
                    val data = payload as Array<*>
                    SecurityEventData.WifiConnection(
                        data[0] as String, data[1] as String, data[2] as String
                    )
                }
                SecurityLog.TAG_WIFI_DISCONNECTION -> {
                    val data = payload as Array<*>
                    SecurityEventData.WifiDisconnection(data[0] as String, data[1] as String)
                }
                SecurityLog.TAG_WIPE_FAILURE -> null
                else -> null
            }
        }
    }
}
