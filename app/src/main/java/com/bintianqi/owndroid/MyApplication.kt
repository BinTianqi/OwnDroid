package com.bintianqi.owndroid

import android.app.Application
import android.os.Build.VERSION
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        SP = SharedPrefs(applicationContext)
        Privilege.initialize(applicationContext)
        Privilege.updateStatus()
    }
}

lateinit var SP: SharedPrefs
    private set
