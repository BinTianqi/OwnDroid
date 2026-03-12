package com.bintianqi.owndroid.feature.applications

import android.content.ContentValues
import com.bintianqi.owndroid.MyDbHelper

class AppGroupRepository(val dbHelper: MyDbHelper) {
    fun getAppGroups(): List<AppGroup> {
        val list = mutableListOf<AppGroup>()
        dbHelper.readableDatabase.rawQuery("SELECT * FROM app_groups", null).use {
            while (it.moveToNext()) {
                list += AppGroup(it.getInt(0), it.getString(1), it.getString(2).split(','))
            }
        }
        return list
    }

    fun setAppGroup(id: Int?, name: String, apps: List<String>) {
        val cv = ContentValues()
        cv.put("name", name)
        cv.put("apps", apps.joinToString(","))
        if (id == null) {
            dbHelper.writableDatabase.insert("app_groups", null, cv)
        } else {
            dbHelper.writableDatabase.update("app_groups", cv, "id = ?", arrayOf(id.toString()))
        }
    }

    fun deleteAppGroup(id: Int) {
        dbHelper.writableDatabase.delete("app_groups", "id = ?", arrayOf(id.toString()))
    }
}
