package com.bintianqi.owndroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 3) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE dhizuku_clients (uid INTEGER PRIMARY KEY," +
                "signature TEXT, permissions TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE security_logs (id INTEGER, tag INTEGER, level INTEGER," +
                    "time INTEGER, data TEXT)")
        }
        if (oldVersion < 3) {
            db.execSQL(
                "CREATE TABLE network_logs (id INTEGER, package INTEGER, time INTEGER," +
                        "type TEXT, host TEXT, count INTEGER, addresses TEXT, address TEXT," +
                        "port INTEGER)"
            )
        }
    }
}