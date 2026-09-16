package com.bintianqi.owndroid.feature.time_blocker

import android.content.ContentValues
import com.bintianqi.owndroid.MyDbHelper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TimeBlockerRepository(private val dbHelper: MyDbHelper) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getRules(): List<BlockRule> {
        val rules = mutableListOf<BlockRule>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT id, package_name, daily_limit_minutes, blocked_windows, allowed_windows, enabled FROM time_block_rules", null
        ).use {
            while (it.moveToNext()) {
                rules += BlockRule(
                    id = it.getInt(0),
                    packageName = it.getString(1),
                    dailyLimitMinutes = it.getInt(2),
                    blockedWindows = try { json.decodeFromString(it.getString(3)) } catch (_: Exception) { emptyList() },
                    allowedWindows = try { json.decodeFromString(it.getString(4)) } catch (_: Exception) { emptyList() },
                    enabled = it.getInt(5) == 1
                )
            }
        }
        return rules
    }

    fun addRule(rule: BlockRule): Int {
        val cv = ContentValues()
        cv.put("package_name", rule.packageName)
        cv.put("daily_limit_minutes", rule.dailyLimitMinutes)
        cv.put("blocked_windows", json.encodeToString(rule.blockedWindows))
        cv.put("allowed_windows", json.encodeToString(rule.allowedWindows))
        cv.put("enabled", if (rule.enabled) 1 else 0)
        return dbHelper.writableDatabase.insert("time_block_rules", null, cv).toInt()
    }

    fun updateRule(rule: BlockRule) {
        val cv = ContentValues()
        cv.put("package_name", rule.packageName)
        cv.put("daily_limit_minutes", rule.dailyLimitMinutes)
        cv.put("blocked_windows", json.encodeToString(rule.blockedWindows))
        cv.put("allowed_windows", json.encodeToString(rule.allowedWindows))
        cv.put("enabled", if (rule.enabled) 1 else 0)
        dbHelper.writableDatabase.update("time_block_rules", cv, "id = ?", arrayOf(rule.id.toString()))
    }

    fun deleteRule(id: Int) {
        dbHelper.writableDatabase.delete("time_block_rules", "id = ?", arrayOf(id.toString()))
    }

    /**
     * Atomically replace all rules (used by settings sync import).
     */
    fun replaceAllRules(rules: List<BlockRule>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("time_block_rules", null, null)
            for (rule in rules) {
                val cv = ContentValues()
                cv.put("package_name", rule.packageName)
                cv.put("daily_limit_minutes", rule.dailyLimitMinutes)
                cv.put("blocked_windows", json.encodeToString(rule.blockedWindows))
                cv.put("allowed_windows", json.encodeToString(rule.allowedWindows))
                cv.put("enabled", if (rule.enabled) 1 else 0)
                db.insert("time_block_rules", null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    fun getEnabledRules(): List<BlockRule> = getRules().filter { it.enabled }

    // Persisted daily usage (survives service restarts)
    fun getUsageToday(dayEpoch: Long): Map<String, Long> {
        val map = mutableMapOf<String, Long>()
        dbHelper.readableDatabase.rawQuery(
            "SELECT package_name, used_ms FROM time_block_usage WHERE day_epoch = ?",
            arrayOf(dayEpoch.toString())
        ).use {
            while (it.moveToNext()) {
                map[it.getString(0)] = it.getLong(1)
            }
        }
        return map
    }

    fun saveUsageToday(dayEpoch: Long, usage: Map<String, Long>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            // Remove stale entries from previous days.
            db.delete("time_block_usage", "day_epoch != ?", arrayOf(dayEpoch.toString()))
            for ((pkg, ms) in usage) {
                val cv = ContentValues()
                cv.put("package_name", pkg)
                cv.put("used_ms", ms)
                cv.put("day_epoch", dayEpoch)
                db.insertWithOnConflict("time_block_usage", null, cv, android.database.sqlite.SQLiteDatabase.CONFLICT_REPLACE)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Track which packages we suspended (for crash recovery)
    fun getSuspendedByUs(): Set<String> {
        val packages = mutableSetOf<String>()
        dbHelper.readableDatabase.rawQuery("SELECT package_name FROM time_block_suspended", null).use {
            while (it.moveToNext()) {
                packages += it.getString(0)
            }
        }
        return packages
    }

    fun setSuspendedByUs(packages: Set<String>) {
        val db = dbHelper.writableDatabase
        db.beginTransaction()
        try {
            db.delete("time_block_suspended", null, null)
            for (pkg in packages) {
                val cv = ContentValues()
                cv.put("package_name", pkg)
                db.insert("time_block_suspended", null, cv)
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }
}
