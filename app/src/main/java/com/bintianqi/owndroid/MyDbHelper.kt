package com.bintianqi.owndroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 12) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DHIZUKU_CLIENTS_TABLE)
        db.execSQL(SECURITY_LOGS_TABLE)
        db.execSQL(NETWORK_LOGS_TABLE)
        db.execSQL(APP_GROUPS_TABLE)
        db.execSQL(CPIF2_TABLE)
        db.execSQL(TIME_BLOCK_RULES_TABLE)
        db.execSQL(TIME_BLOCK_SUSPENDED_TABLE)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(SECURITY_LOGS_TABLE)
        }
        if (oldVersion < 3) {
            db.execSQL(NETWORK_LOGS_TABLE)
        }
        if (oldVersion < 4) {
            db.execSQL(APP_GROUPS_TABLE)
        }
        if (oldVersion < 8) {
            db.execSQL(CPIF_TABLE)
        }
        if (oldVersion < 9) {
            db.execSQL(DELETE_CPIF)
            db.execSQL(CPIF2_TABLE_OLD)
        }
        if (oldVersion < 10) {
            db.execSQL(CPIF2_ADD_STATUS)
        }
        if (oldVersion < 11) {
            // Table `cpif2` won't have column `enabled` if the table is created at version 10
            // It will only have that column if it's upgraded from a previous version
            // At this point, we don't know whether that table has that column
            // So, re-create that table to ensure it has that column
            db.execSQL("DROP TABLE cpif2")
            db.execSQL(CPIF2_TABLE)
        }
        if (oldVersion < 12) {
            db.execSQL(TIME_BLOCK_RULES_TABLE)
            db.execSQL(TIME_BLOCK_SUSPENDED_TABLE)
        }
    }

    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Debug builds are often installed over newer builds (branch switching).
        // SQLiteOpenHelper crashes by default on downgrade; reset the DB instead.
        Log.w("MyDbHelper", "DB downgrade $oldVersion -> $newVersion, resetting all tables")
        val tables = mutableListOf<String>()
        db.rawQuery(
            "SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE 'android_%'",
            null
        ).use { c ->
            while (c.moveToNext()) tables += c.getString(0)
        }
        for (table in tables) {
            db.execSQL("DROP TABLE IF EXISTS `$table`")
        }
        onCreate(db)
    }
    companion object {
        const val DHIZUKU_CLIENTS_TABLE = "CREATE TABLE dhizuku_clients (uid INTEGER PRIMARY KEY," +
                "signature TEXT, permissions TEXT)"
        const val SECURITY_LOGS_TABLE = "CREATE TABLE security_logs (id INTEGER, tag INTEGER," +
                "level INTEGER, time INTEGER, data TEXT)"
        const val NETWORK_LOGS_TABLE = "CREATE TABLE network_logs (id INTEGER, package INTEGER," +
                "time INTEGER, type TEXT, host TEXT, count INTEGER, addresses TEXT," +
                "address TEXT, port INTEGER)"
        const val APP_GROUPS_TABLE = "CREATE TABLE app_groups(" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT, apps TEXT)"
        const val CPIF_TABLE = "CREATE TABLE cpif (" +
                "action_str TEXT, category TEXT, mime_type TEXT, direction INTEGER, time INTEGER)"
        const val DELETE_CPIF = "DROP TABLE cpif"
        const val CPIF2_TABLE_OLD = "CREATE TABLE cpif2 (id INTEGER PRIMARY KEY," +
                "action_str TEXT, category TEXT, mime_type TEXT, direction INTEGER," +
                "created_at INTEGER)"
        const val CPIF2_ADD_STATUS = "ALTER TABLE cpif2 ADD COLUMN enabled INTEGER DEFAULT TRUE"
        const val CPIF2_TABLE = "CREATE TABLE cpif2 (id INTEGER PRIMARY KEY," +
                "action_str TEXT, category TEXT, mime_type TEXT, direction INTEGER," +
                "created_at INTEGER, enabled INTEGER DEFAULT TRUE)"
        const val TIME_BLOCK_RULES_TABLE = "CREATE TABLE time_block_rules (" +
                "id INTEGER PRIMARY KEY," +
                "package_name TEXT, daily_limit_minutes INTEGER," +
                "blocked_windows TEXT, allowed_windows TEXT, enabled INTEGER)"
        const val TIME_BLOCK_SUSPENDED_TABLE = "CREATE TABLE time_block_suspended (" +
                "package_name TEXT PRIMARY KEY)"
    }
}
