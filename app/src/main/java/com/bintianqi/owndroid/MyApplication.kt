package com.bintianqi.owndroid

import android.app.Application
import android.os.Build.VERSION
import com.bintianqi.owndroid.utils.NotificationUtils
import com.bintianqi.owndroid.utils.getPrivilegeStatus
import org.lsposed.hiddenapibypass.HiddenApiBypass

class MyApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        if (VERSION.SDK_INT >= 28) HiddenApiBypass.setHiddenApiExemptions("")
        container = AppContainer(this)
        container.privilegeState.value = getPrivilegeStatus(container.privilegeHelper)
        NotificationUtils.createChannels(this)
    }
}
