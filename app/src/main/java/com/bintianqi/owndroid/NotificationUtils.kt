package com.bintianqi.owndroid

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat

object NotificationUtils {
    fun checkPermission(context: Context): Boolean {
        return if(Build.VERSION.SDK_INT >= 33)
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        else false
    }
    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val lockTaskMode = NotificationChannel(
            MyNotificationChannel.LockTaskMode.id,
            context.getString(MyNotificationChannel.LockTaskMode.text),
            NotificationManager.IMPORTANCE_HIGH
        )
        val events = NotificationChannel(
            MyNotificationChannel.Events.id,
            context.getString(MyNotificationChannel.Events.text),
            NotificationManager.IMPORTANCE_HIGH
        )
        nm.createNotificationChannels(listOf(lockTaskMode, events))
    }
    fun notifyEvent(context: Context, type: NotificationType, text: String) {
        val notification = NotificationCompat.Builder(context, MyNotificationChannel.Events.id)
            .setSmallIcon(type.icon)
            .setContentTitle(context.getString(type.text))
            .setContentText(text)
            .build()
        notify(context, type, notification)
    }
    fun notify(context: Context, type: NotificationType, notification: Notification) {
        val enabledNotifications = SP.notifications?.split(',')?.mapNotNull { it.toIntOrNull() }
        if (enabledNotifications == null || type.id in enabledNotifications) {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.notify(type.id, notification)
        }
    }
    fun cancel(context: Context, type: NotificationType) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(type.id)
    }
}

enum class NotificationType(val id: Int, val text: Int, val icon: Int) {
    LockTaskMode(1, R.string.lock_task_mode, R.drawable.lock_fill0),
    PasswordChanged(2, R.string.password_changed, R.drawable.password_fill0),
    UserAdded(3, R.string.user_added, R.drawable.person_add_fill0),
    UserStarted(4, R.string.user_started, R.drawable.person_fill0),
    UserSwitched(5, R.string.user_switched, R.drawable.person_fill0),
    UserStopped(6, R.string.user_stopped, R.drawable.person_off),
    UserRemoved(7, R.string.user_removed, R.drawable.person_remove_fill0),
    BugReportShared(8, R.string.bug_report_shared, R.drawable.bug_report_fill0),
    BugReportSharingDeclined(9, R.string.bug_report_sharing_declined, R.drawable.bug_report_fill0),
    BugReportFailed(10, R.string.bug_report_failed, R.drawable.bug_report_fill0),
    SystemUpdatePending(11, R.string.system_update_pending, R.drawable.system_update_fill0)
}

enum class MyNotificationChannel(val id: String, val text: Int) {
    LockTaskMode("LockTaskMode", R.string.lock_task_mode),
    Events("Events", R.string.events)
}
