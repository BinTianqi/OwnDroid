package com.bintianqi.owndroid

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

object NotificationUtils {
    fun checkPermission(context: Context): Boolean {
        return if(Build.VERSION.SDK_INT >= 33)
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else false
    }
    fun registerChannels(context: Context) {
        if(Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val lockTaskMode = NotificationChannel(Channel.LOCK_TASK_MODE, context.getString(R.string.lock_task_mode), NotificationManager.IMPORTANCE_HIGH)
        val events = NotificationChannel(Channel.EVENTS, context.getString(R.string.events), NotificationManager.IMPORTANCE_HIGH)
        nm.createNotificationChannels(listOf(lockTaskMode, events))
    }
    fun notify(context: Context, id: Int, notification: Notification) {
        val sp = context.getSharedPreferences("data", Context.MODE_PRIVATE)
        if(sp.getBoolean("n_$id", true) && checkPermission(context)) {
            registerChannels(context)
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(id, notification)
        }
    }
    object Channel {
        const val LOCK_TASK_MODE = "LockTaskMode"
        const val EVENTS = "Events"
    }
    object ID {
        const val LOCK_TASK_MODE = 1
        const val PASSWORD_CHANGED = 2
        const val USER_ADDED = 3
        const val USER_STARTED = 4
        const val USER_SWITCHED = 5
        const val USER_STOPPED = 6
        const val USER_REMOVED = 7
        const val BUG_REPORT_SHARED = 8
        const val BUG_REPORT_SHARING_DECLINED = 9
        const val BUG_REPORT_FAILED = 10
        const val SYSTEM_UPDATE_PENDING = 11
    }
}