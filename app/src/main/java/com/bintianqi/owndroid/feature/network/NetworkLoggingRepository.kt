package com.bintianqi.owndroid.feature.network

import android.database.DatabaseUtils
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.bintianqi.owndroid.MyDbHelper
import kotlinx.serialization.json.Json
import java.io.OutputStream

class NetworkLoggingRepository(val dbHelper: MyDbHelper) {

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
            if (event.host == null) {
                statement.bindNull(5)
            } else {
                statement.bindString(5, event.host)
            }
            if (event.count == null) {
                statement.bindNull(6)
            } else {
                statement.bindLong(6, event.count.toLong())
            }
            if (event.addresses == null) {
                statement.bindNull(7)
            } else {
                statement.bindString(7, event.addresses.joinToString(","))
            }
            if (event.address == null) {
                statement.bindNull(8)
            } else {
                statement.bindString(8, event.address)
            }
            if (event.port == null) {
                statement.bindNull(9)
            } else {
                statement.bindLong(9, event.port.toLong())
            }
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
