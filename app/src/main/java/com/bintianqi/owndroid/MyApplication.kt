package com.bintianqi.owndroid

import android.app.Application
import android.os.Build.VERSION
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MyApplication : Application() {
    lateinit var myRepo: MyRepository
    override fun onCreate() {
        super.onCreate()
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        SP = SharedPrefs(applicationContext)
        val dbHelper = MyDbHelper(this)
        myRepo = MyRepository(dbHelper)
        Privilege.initialize(applicationContext)
        NotificationUtils.createChannels(this)
    }
}

lateinit var SP: SharedPrefs
    private set
