package com.bintianqi.owndroid

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build.VERSION
import android.os.UserHandle
import android.os.UserManager
import androidx.core.app.NotificationCompat
import com.bintianqi.owndroid.dpm.handleNetworkLogs
import com.bintianqi.owndroid.dpm.handlePrivilegeChange
import com.bintianqi.owndroid.dpm.retrieveSecurityLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class Receiver : DeviceAdminReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if(VERSION.SDK_INT >= 26 && intent.action == "com.bintianqi.owndroid.action.STOP_LOCK_TASK_MODE") {
            val receiver = ComponentName(context, this::class.java)
            val packages = Privilege.DPM.getLockTaskPackages(receiver)
            Privilege.DPM.setLockTaskPackages(receiver, arrayOf())
            Privilege.DPM.setLockTaskPackages(receiver, packages)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Privilege.updateStatus()
        if (Binder.getCallingUid() / 100000 != 0) handlePrivilegeChange(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Privilege.updateStatus()
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        context.popToast(R.string.create_work_profile_success)
    }

    override fun onNetworkLogsAvailable(context: Context, intent: Intent, batchToken: Long, networkLogsCount: Int) {
        super.onNetworkLogsAvailable(context, intent, batchToken, networkLogsCount)
        if(VERSION.SDK_INT >= 26) {
            CoroutineScope(Dispatchers.IO).launch {
                handleNetworkLogs(context, batchToken)
            }
        }
    }

    override fun onSecurityLogsAvailable(context: Context, intent: Intent) {
        super.onSecurityLogsAvailable(context, intent)
        if (VERSION.SDK_INT >= 24) {
            retrieveSecurityLogs(context.applicationContext as MyApplication)
        }
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        val stopIntent = Intent(context, this::class.java)
            .setAction("com.bintianqi.owndroid.action.STOP_LOCK_TASK_MODE")
        val pendingIntent = PendingIntent.getBroadcast(context, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, MyNotificationChannel.LockTaskMode.id)
            .setContentTitle(context.getText(R.string.lock_task_mode))
            .setSmallIcon(R.drawable.lock_fill0)
            .addAction(NotificationCompat.Action.Builder(null, context.getString(R.string.stop), pendingIntent).build())
            .build()
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.notify(NotificationType.LockTaskMode.id, notification)
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        NotificationUtils.cancel(context, NotificationType.LockTaskMode)
    }

    override fun onPasswordChanged(context: Context, intent: Intent, userHandle: UserHandle) {
        super.onPasswordChanged(context, intent, userHandle)
        sendUserRelatedNotification(context, userHandle, NotificationType.PasswordChanged)
    }

    override fun onUserAdded(context: Context, intent: Intent, addedUser: UserHandle) {
        super.onUserAdded(context, intent, addedUser)
        sendUserRelatedNotification(context, addedUser, NotificationType.UserAdded)
    }

    override fun onUserStarted(context: Context, intent: Intent, startedUser: UserHandle) {
        super.onUserStarted(context, intent, startedUser)
        sendUserRelatedNotification(context, startedUser, NotificationType.UserStarted)
    }

    override fun onUserSwitched(context: Context, intent: Intent, switchedUser: UserHandle) {
        super.onUserSwitched(context, intent, switchedUser)
        sendUserRelatedNotification(context, switchedUser, NotificationType.UserSwitched)
    }

    override fun onUserStopped(context: Context, intent: Intent, stoppedUser: UserHandle) {
        super.onUserStopped(context, intent, stoppedUser)
        sendUserRelatedNotification(context, stoppedUser, NotificationType.UserStopped)
    }

    override fun onUserRemoved(context: Context, intent: Intent, removedUser: UserHandle) {
        super.onUserRemoved(context, intent, removedUser)
        sendUserRelatedNotification(context, removedUser, NotificationType.UserRemoved)
    }

    override fun onBugreportShared(context: Context, intent: Intent, hash: String) {
        super.onBugreportShared(context, intent, hash)
        NotificationUtils.notifyEvent(context, NotificationType.BugReportShared, "SHA-256 hash: $hash")
    }

    override fun onBugreportSharingDeclined(context: Context, intent: Intent) {
        super.onBugreportSharingDeclined(context, intent)
        NotificationUtils.notifyEvent(context, NotificationType.BugReportSharingDeclined, "")
    }

    override fun onBugreportFailed(context: Context, intent: Intent, failureCode: Int) {
        super.onBugreportFailed(context, intent, failureCode)
        val message = when(failureCode) {
            BUGREPORT_FAILURE_FAILED_COMPLETING -> R.string.bug_report_failure_failed_completing
            BUGREPORT_FAILURE_FILE_NO_LONGER_AVAILABLE -> R.string.bug_report_failure_no_longer_available
            else -> R.string.place_holder
        }
        NotificationUtils.notifyEvent(context, NotificationType.BugReportFailed, context.getString(message))
    }

    override fun onSystemUpdatePending(context: Context, intent: Intent, receivedTime: Long) {
        super.onSystemUpdatePending(context, intent, receivedTime)
        val text = context.getString(R.string.received_time) + ": " + formatDate(receivedTime)
        NotificationUtils.notifyEvent(context, NotificationType.SystemUpdatePending, text)
    }

    private fun sendUserRelatedNotification(
        context: Context, userHandle: UserHandle, type: NotificationType
    ) {
        val um = context.getSystemService(Context.USER_SERVICE) as UserManager
        val serial = um.getSerialNumberForUser(userHandle)
        val text = context.getString(R.string.serial_number) + ": $serial"
        NotificationUtils.notifyEvent(context, type, text)
    }
}
