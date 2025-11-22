package com.bintianqi.owndroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 4) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DHIZUKU_CLIENTS_TABLE)
        db.execSQL(SECURITY_LOGS_TABLE)
        db.execSQL(NETWORK_LOGS_TABLE)
        db.execSQL(APP_GROUPS_TABLE)
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
    }
}