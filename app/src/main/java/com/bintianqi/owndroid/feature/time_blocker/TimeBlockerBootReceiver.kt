package com.bintianqi.owndroid.feature.time_blocker

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import com.bintianqi.owndroid.MyApplication
import kotlin.concurrent.thread

class TimeBlockerBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        if (Build.VERSION.SDK_INT < 24) return
        val pendingResult = goAsync()
        thread {
            try {
                val myApp = context.applicationContext as MyApplication
                val repo = myApp.container.timeBlockerRepo
                val serviceEnabled = myApp.container.settingsRepo.data.timeBlockerServiceEnabled
                if (serviceEnabled && repo.getEnabledRules().isNotEmpty()) {
                    TimeBlockerService.start(context)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
