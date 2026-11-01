package com.bintianqi.owndroid.feature.applications

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

/**
 * Because of compatibility issues, the `apps` column may or may not exist,
 * so we have to specify column names explicitly.
 * Also see comments in [MyDbHelper.onUpgrade].
 */
class AppGroupRepository(val database: SQLiteDatabase) {
    fun getAppGroups(): List<AppGroup> {
        val groupApps = getGroupApps()
        val list = mutableListOf<AppGroup>()
        database.rawQuery(
            "SELECT id, name FROM app_groups ORDER BY position", null
        ).use { cursor ->
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
        database.rawQuery("SELECT * FROM app_group_apps", null).use {
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
            var maxPosition = -1
            database.rawQuery(
                "SELECT position FROM app_groups ORDER BY position DESC LIMIT 1", null
            ).use {
                if (it.moveToNext()) maxPosition = it.getInt(0)
            }
            cv.put("position", maxPosition + 1)
            groupId = database.insert("app_groups", null, cv)
        } else {
            database.update("app_groups", cv, "id = ?", arrayOf(id.toString()))
            groupId = id.toLong()
        }
        setGroupApps(groupId, apps)
    }

    fun setGroupPosition(id: Int, place: Int) {
        val cv = ContentValues()
        cv.put("position", place)
        database.update("app_groups", cv, "id = ?", arrayOf(id.toString()))
    }

    fun deleteAppGroup(id: Int) {
        database.delete("app_groups", "id = ?", arrayOf(id.toString()))
    }

    @Deprecated("Only use it for migrating purpose")
    fun getAppGroupsV1(): List<AppGroup> {
        val list = mutableListOf<AppGroup>()
        database.rawQuery("SELECT id, name, apps FROM app_groups", null).use {
            while (it.moveToNext()) {
                list += AppGroup(it.getInt(0), it.getString(1), it.getString(2).split(','))
            }
        }
        return list
    }

    fun setGroupApps(groupId: Long, apps: List<String>) {
        val deleteStmt = database.compileStatement("DELETE FROM app_group_apps WHERE group_id = ?")
        deleteStmt.bindLong(1, groupId)
        deleteStmt.executeUpdateDelete()
        deleteStmt.close()

        val stmt = database.compileStatement("INSERT INTO app_group_apps VALUES (?, ?)")
        stmt.bindLong(1, groupId)
        apps.forEach {
            stmt.bindString(2, it)
            stmt.executeInsert()
        }
        stmt.close()
    }
}
