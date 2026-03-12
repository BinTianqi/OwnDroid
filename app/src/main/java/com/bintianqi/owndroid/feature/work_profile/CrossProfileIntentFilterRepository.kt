package com.bintianqi.owndroid.feature.work_profile

import android.content.ContentValues
import com.bintianqi.owndroid.MyDbHelper

class CrossProfileIntentFilterRepository(val dbHelper: MyDbHelper) {
    fun setCrossProfileIntentFilter(data: IntentFilterOptions) {
        val cv = ContentValues()
        cv.put("action_str", data.action)
        cv.put("category", data.category)
        cv.put("mime_type", data.mimeType)
        cv.put("direction", data.direction)
        dbHelper.writableDatabase.insert("cross_profile_intent_filters", null, cv)
    }

    fun getAllCrossProfileIntentFilters(): List<IntentFilterOptions> {
        val list = mutableListOf<IntentFilterOptions>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT * FROM cross_profile_intent_filters", null
        ).use {
            while (it.moveToNext()) {
                list += IntentFilterOptions(
                    it.getString(0), it.getString(1), it.getString(2), it.getInt(3)
                )
            }
        }
        return list
    }

    fun deleteAllCrossProfileIntentFilters() {
        dbHelper.writableDatabase.delete("cross_profile_intent_filters", null, null)
    }
}
