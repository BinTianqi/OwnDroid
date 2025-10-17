package com.bintianqi.owndroid

import android.app.admin.SecurityLog
import android.content.ContentValues
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.bintianqi.owndroid.dpm.NetworkLog
import com.bintianqi.owndroid.dpm.SecurityEvent
import com.bintianqi.owndroid.dpm.SecurityEventWithData
import com.bintianqi.owndroid.dpm.transformSecurityEventData
import kotlinx.serialization.json.ClassDiscriminatorMode
import kotlinx.serialization.json.Json
import java.io.OutputStream

class MyRepository(val dbHelper: MyDbHelper) {
    fun getDhizukuClients(): List<DhizukuClientInfo> {
        val list = mutableListOf<DhizukuClientInfo>()
        dbHelper.readableDatabase.rawQuery("SELECT * FROM dhizuku_clients", null).use { cursor ->
            while (cursor.moveToNext()) {
                list += DhizukuClientInfo(
                    cursor.getInt(0), cursor.getString(1),
                    cursor.getString(2).split(",").filter { it.isNotEmpty() }
                )
            }
        }
        return list
    }
    fun checkDhizukuClientPermission(uid: Int, signature: String?, permission: String): Boolean {
        val cursor = if (signature == null) {
            dbHelper.readableDatabase.rawQuery(
                "SELECT permissions FROM dhizuku_clients WHERE uid = $uid AND signature IS NULL",
                null
            )
        } else {
            dbHelper.readableDatabase.rawQuery(
                "SELECT permissions FROM dhizuku_clients WHERE uid = $uid AND signature = ?",
                arrayOf(signature)
            )
        }
        return cursor.use {
            it.moveToNext() && permission in it.getString(0).split(",")
        }
    }
    fun setDhizukuClient(info: DhizukuClientInfo) {
        val cv = ContentValues()
        cv.put("uid", info.uid)
        cv.put("signature", info.signature)
        cv.put("permissions", info.permissions.joinToString(","))
        dbHelper.writableDatabase.insertWithOnConflict("dhizuku_clients", null, cv,
            SQLiteDatabase.CONFLICT_REPLACE)
    }
    fun deleteDhizukuClient(info: DhizukuClientInfo) {
        dbHelper.writableDatabase.delete("dhizuku_clients", "uid = ${info.uid}", null)
    }

    fun getSecurityLogsCount(): Long {
        return DatabaseUtils.queryNumEntries(dbHelper.readableDatabase, "security_logs")
    }
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
                if (VERSION.SDK_INT >= 28) {
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
                    if (VERSION.SDK_INT >= 28) log.id else null, log.tag,
                    if (VERSION.SDK_INT >= 28) log.logLevel else null, log.timeNanos / 1000000,
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

    fun getNetworkLogsCount(): Long {
        return DatabaseUtils.queryNumEntries(dbHelper.readableDatabase, "network_logs")
    }
    fun writeNetworkLogs(logs: List<NetworkLog>) {
        val db = dbHelper.writableDatabase
        val statement = db.compileStatement(
            "INSERT INTO network_logs VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)"
        )
        db.beginTransaction()
        logs.forEach { event ->
            if (event.id == null) statement.bindNull(1)
            else statement.bindLong(1, event.id)
            statement.bindString(2, event.packageName)
            statement.bindLong(3, event.time)
            statement.bindString(4, event.type)
            if (event.host == null) statement.bindNull(5)
            else statement.bindString(5, event.host)
            if (event.count == null) statement.bindNull(6)
            else statement.bindLong(6, event.count.toLong())
            if (event.addresses == null) statement.bindNull(7)
            else statement.bindString(7, event.addresses.joinToString(","))
            if (event.address == null) statement.bindNull(8)
            else statement.bindString(8, event.address)
            if (event.port == null) statement.bindNull(9)
            else statement.bindLong(9, event.port.toLong())
            statement.executeInsert()
            statement.clearBindings()
        }
        db.setTransactionSuccessful()
        db.endTransaction()
        statement.close()
    }
    fun exportNetworkLogs(stream: OutputStream) {
        val bw = stream.bufferedWriter()
        val json = Json {
            explicitNulls = false
        }
        var offset = 0
        var addComma = false
        bw.write("[")
        while (true) {
            val cursor = dbHelper.readableDatabase.rawQuery(
                "SELECT * FROM network_logs LIMIT ? OFFSET ?",
                arrayOf(100.toString(), offset.toString())
            )
            if (cursor.count == 0) break
            while (cursor.moveToNext()) {
                if (addComma) bw.write(",")
                addComma = true
                val log = NetworkLog(
                    cursor.getLongOrNull(0), cursor.getString(1), cursor.getLong(2),
                    cursor.getString(3), cursor.getStringOrNull(4), cursor.getIntOrNull(5),
                    cursor.getStringOrNull(6)?.split(',')?.filter { it.isNotEmpty() },
                    cursor.getStringOrNull(7), cursor.getIntOrNull(8)
                )
                bw.write(json.encodeToString(log))
                offset += 100
            }
            cursor.close()
        }
        bw.write("]")
        bw.close()
    }
    fun deleteNetworkLogs() {
        dbHelper.writableDatabase.execSQL("DELETE FROM network_logs")
    }
}