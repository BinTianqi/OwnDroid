package com.bintianqi.owndroid.feature.work_profile

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class CrossProfileIntentFilterRepository(val database: SQLiteDatabase) {
    fun addFilter(data: IntentFilterOptions): Long {
        val cv = ContentValues()
        cv.put("action_str", data.action)
        cv.put("category", data.category)
        cv.put("mime_type", data.mimeType)
        cv.put("direction", data.direction)
        cv.put("created_at", System.currentTimeMillis())
        return database.insert("cpif2", null, cv)
    }

    fun updateFilter(id: Int, data: IntentFilterOptions) {
        val cv = ContentValues()
        cv.put("action_str", data.action)
        cv.put("category", data.category)
        cv.put("mime_type", data.mimeType)
        cv.put("direction", data.direction)
        cv.put("created_at", System.currentTimeMillis())
        database.update("cpif2", cv, "id = ?", arrayOf(id.toString()))
    }

    fun setEnabled(id: Int, enabled: Boolean) {
        val cv = ContentValues()
        cv.put("enabled", if (enabled) 1 else 0)
        database.update("cpif2", cv, "id = ?", arrayOf(id.toString()))
    }

    fun getAllFilters(): List<IntentFilterEntry> {
        val list = mutableListOf<IntentFilterEntry>()
        database.rawQuery(
            "SELECT * FROM cpif2 ORDER BY created_at DESC", null
        ).use {
            while (it.moveToNext()) {
                val options = IntentFilterOptions(
                    it.getString(1), it.getString(2), it.getString(3), it.getInt(4)
                )
                list += IntentFilterEntry(it.getInt(0), options, it.getLong(5), it.getInt(6) == 1)
            }
        }
        return list
    }

    fun deleteById(id: Int) {
        database.delete("cpif2", "id = ?", arrayOf("$id"))
    }

    fun deleteAllCrossProfileIntentFilters() {
        database.delete("cpif2", null, null)
    }
}
