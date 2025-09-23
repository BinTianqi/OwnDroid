package com.bintianqi.owndroid

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

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
}