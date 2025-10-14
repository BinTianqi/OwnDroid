package com.bintianqi.owndroid

import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {
    fun createChannels(context: Context) {
        val channels = MyNotificationChannel.entries.map {
            NotificationChannelCompat.Builder(it.id, it.importance)
                .setName(context.getString(it.text))
                .build()
        }
        NotificationManagerCompat.from(context).createNotificationChannelsCompat(channels)
    }
    fun sendBasicNotification(
        context: Context, type: NotificationType, channel: MyNotificationChannel, text: String
    ) {
        val notification = NotificationCompat.Builder(context, channel.id)
            .setSmallIcon(type.icon)
            .setContentTitle(context.getString(type.text))
            .setContentText(text)
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(type.id, notification)
    }
    fun notifyEvent(context: Context, type: NotificationType, text: String) {
        val enabledNotifications = SP.notifications?.split(',')?.mapNotNull { it.toIntOrNull() }
        if (enabledNotifications == null || type.id in enabledNotifications) {
            sendBasicNotification(context, type, MyNotificationChannel.Events, text)
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
    SystemUpdatePending(11, R.string.system_update_pending, R.drawable.system_update_fill0),
    SecurityLogsCollected(12, R.string.security_logs_collected, R.drawable.description_fill0),
}

enum class MyNotificationChannel(val id: String, val text: Int, val importance: Int) {
    LockTaskMode("LockTaskMode", R.string.lock_task_mode, NotificationManagerCompat.IMPORTANCE_HIGH),
    Events("Events", R.string.events, NotificationManagerCompat.IMPORTANCE_LOW),
    SecurityLogging("SecurityLogging", R.string.security_logging, NotificationManagerCompat.IMPORTANCE_MIN)
}
