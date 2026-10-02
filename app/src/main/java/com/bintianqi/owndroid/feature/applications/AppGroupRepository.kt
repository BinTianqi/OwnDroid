package com.bintianqi.owndroid.feature.applications

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.bintianqi.owndroid.MyDbHelper

class AppGroupRepository(val dbHelper: MyDbHelper) {
    fun getAppGroups(): List<AppGroup> {
        val groupApps = getGroupApps()
        val list = mutableListOf<AppGroup>()
        dbHelper.readableDatabase.rawQuery("SELECT * FROM app_groups", null).use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getInt(0)
                list += AppGroup(
                    id, cursor.getString(1),
                    groupApps.filter { it.first == id }.map { it.second }
                )
            }
        }
        return list
    }

    private fun getGroupApps(): List<Pair<Int, String>> {
        // Using JSON functions for aggregation may be better,
        // but they're compiled into SQLite by default since 3.38.0 (2022),
        // and we don't know whether it works on older Android versions.
        val items = mutableListOf<Pair<Int, String>>()
        dbHelper.writableDatabase.rawQuery("SELECT * FROM app_group_apps", null).use {
            while (it.moveToNext()) {
                items += it.getInt(0) to it.getString(1)
            }
        }
        return items
    }

    fun setAppGroup(id: Int?, name: String, apps: List<String>) {
        val cv = ContentValues()
        cv.put("name", name)
        val groupId: Long
        if (id == null) {
            groupId = dbHelper.writableDatabase.insert("app_groups", null, cv)
        } else {
            dbHelper.writableDatabase.update("app_groups", cv, "id = ?", arrayOf(id.toString()))
            groupId = id.toLong()
        }
        setGroupApps(dbHelper.writableDatabase, groupId, apps)
    }


    fun deleteAppGroup(id: Int) {
        dbHelper.writableDatabase.delete("app_groups", "id = ?", arrayOf(id.toString()))
    }

    companion object {
        /**
         * Because it's used in [MyDbHelper.onUpgrade],
         * we are only allowed to use the `SQLiteDatabase` object provided by that function`.
         * Using [android.database.sqlite.SQLiteOpenHelper.readableDatabase] will cause an error.
         */
        @Deprecated("Only use it for migrating purpose")
        fun getAppGroupsV1(db: SQLiteDatabase): List<AppGroup> {
            val list = mutableListOf<AppGroup>()
            db.rawQuery("SELECT * FROM app_groups", null).use {
                while (it.moveToNext()) {
                    list += AppGroup(it.getInt(0), it.getString(1), it.getString(2).split(','))
                }
            }
            return list
        }

        fun setGroupApps(db: SQLiteDatabase, groupId: Long, apps: List<String>) {
            val deleteStmt = db.compileStatement("DELETE FROM app_group_apps WHERE group_id = ?")
            deleteStmt.bindLong(1, groupId)
            deleteStmt.executeUpdateDelete()
            deleteStmt.close()

            val stmt = db.compileStatement("INSERT INTO app_group_apps VALUES (?, ?)")
            stmt.bindLong(1, groupId)
            apps.forEach {
                stmt.bindString(2, it)
                stmt.executeInsert()
            }
            stmt.close()
        }
    }
}
