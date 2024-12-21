package com.bintianqi.owndroid

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

/**
 * ### Notification channels
 * - LockTaskMode
 *
 * ### Notification IDs
 * - 1: Stop lock task mode
 */
object NotificationUtils {
    fun checkPermission(context: Context): Boolean {
        return if(Build.VERSION.SDK_INT >= 33)
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else false
    }
    fun registerChannels(context: Context) {
        if(Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel("LockTaskMode", context.getString(R.string.lock_task_mode), NotificationManager.IMPORTANCE_HIGH)
        nm.createNotificationChannel(channel)
    }
}