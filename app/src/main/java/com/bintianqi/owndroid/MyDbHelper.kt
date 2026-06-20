package com.bintianqi.owndroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 7) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DHIZUKU_CLIENTS_TABLE)
        db.execSQL(SECURITY_LOGS_TABLE)
        db.execSQL(NETWORK_LOGS_TABLE)
        db.execSQL(APP_GROUPS_TABLE)
        db.execSQL(CP_INTENTS_TABLE)
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
        if (oldVersion < 5) {
            db.execSQL(CP_INTENTS_TABLE)
        }
        if (oldVersion < 6) {
            db.execSQL(UPGRADE_CP_INTENTS_TABLE)
        }
        if (oldVersion < 7) {
            db.execSQL(RENAME_CP_COLUMN)
        }
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
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, apps TEXT)"
        const val CP_INTENTS_TABLE = "CREATE TABLE cross_profile_intent_filters (" +
                "action_str TEXT, category TEXT, mime_type TEXT, direction INTEGER, time INTEGER)"
        const val UPGRADE_CP_INTENTS_TABLE = "ALTER TABLE cross_profile_intent_filters " +
                "ADD COLUMN created_at INTEGER NOT NULL DEFAULT 0"
        const val RENAME_CP_COLUMN = "ALTER TABLE cross_profile_intent_filters " +
                "RENAME COLUMN created_at TO time"
    }
}