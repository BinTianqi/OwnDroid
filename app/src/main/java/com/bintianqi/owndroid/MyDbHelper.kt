package com.bintianqi.owndroid

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MyDbHelper(context: Context): SQLiteOpenHelper(context, "data", null, 2) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE dhizuku_clients (uid INTEGER PRIMARY KEY," +
                "signature TEXT, permissions TEXT)")
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE security_logs (id INTEGER, tag INTEGER, level INTEGER," +
                    "time INTEGER, data TEXT)")
        }
    }
}