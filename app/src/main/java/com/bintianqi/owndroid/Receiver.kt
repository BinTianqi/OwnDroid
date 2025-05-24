package com.bintianqi.owndroid

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.PersistableBundle
import android.os.UserHandle
import android.os.UserManager
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bintianqi.owndroid.dpm.handleNetworkLogs
import com.bintianqi.owndroid.dpm.handlePrivilegeChange
import com.bintianqi.owndroid.dpm.processSecurityLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Receiver : DeviceAdminReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if(VERSION.SDK_INT >= 26 && intent.action == "com.bintianqi.owndroid.action.STOP_LOCK_TASK_MODE") {
            val dpm = getManager(context)
            val receiver = ComponentName(context, this::class.java)
            val packages = dpm.getLockTaskPackages(receiver)
            dpm.setLockTaskPackages(receiver, arrayOf())
            dpm.setLockTaskPackages(receiver, packages)
        }
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        updatePrivilege(context)
        handlePrivilegeChange(context)
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        updatePrivilege(context)
        handlePrivilegeChange(context)
    }

    override fun onProfileProvisioningComplete(context: Context, intent: Intent) {
        super.onProfileProvisioningComplete(context, intent)
        Toast.makeText(context, R.string.create_work_profile_success, Toast.LENGTH_SHORT).show()
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
        if(VERSION.SDK_INT >= 24) {
            CoroutineScope(Dispatchers.IO).launch {
                val events = getManager(context).retrieveSecurityLogs(MyAdminComponent) ?: return@launch
                val file = context.filesDir.resolve("SecurityLogs.json")
                val fileExists = file.exists()
                file.outputStream().use {
                    if(fileExists) it.write(",".encodeToByteArray())
                    processSecurityLogs(events, it)
                }
            }
        }
    }

    override fun onTransferOwnershipComplete(context: Context, bundle: PersistableBundle?) {
        super.onTransferOwnershipComplete(context, bundle)
        SharedPrefs(context).dhizuku = false
    }

    override fun onLockTaskModeEntering(context: Context, intent: Intent, pkg: String) {
        super.onLockTaskModeEntering(context, intent, pkg)
        if(!NotificationUtils.checkPermission(context)) return
        NotificationUtils.registerChannels(context)
        val intent = Intent(context, this::class.java).setAction("com.bintianqi.owndroid.action.STOP_LOCK_TASK_MODE")
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, NotificationUtils.Channel.LOCK_TASK_MODE)
            .setContentTitle(context.getText(R.string.lock_task_mode))
            .setSmallIcon(R.drawable.lock_fill0)
            .addAction(NotificationCompat.Action.Builder(null, context.getString(R.string.stop), pendingIntent).build())
        NotificationUtils.notify(context, NotificationUtils.ID.LOCK_TASK_MODE, builder.build())
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(NotificationUtils.ID.LOCK_TASK_MODE)
    }

    override fun onPasswordChanged(context: Context, intent: Intent, userHandle: UserHandle) {
        super.onPasswordChanged(context, intent, userHandle)
        sendUserRelatedNotification(context, userHandle, NotificationUtils.ID.PASSWORD_CHANGED, R.string.password_changed, R.drawable.password_fill0)
    }

    override fun onUserAdded(context: Context, intent: Intent, addedUser: UserHandle) {
        super.onUserAdded(context, intent, addedUser)
        sendUserRelatedNotification(context, addedUser, NotificationUtils.ID.USER_ADDED, R.string.user_added, R.drawable.person_add_fill0)
    }

    override fun onUserStarted(context: Context, intent: Intent, startedUser: UserHandle) {
        super.onUserStarted(context, intent, startedUser)
        sendUserRelatedNotification(context, startedUser, NotificationUtils.ID.USER_STARTED, R.string.user_started, R.drawable.person_fill0)
    }

    override fun onUserSwitched(context: Context, intent: Intent, switchedUser: UserHandle) {
        super.onUserSwitched(context, intent, switchedUser)
        sendUserRelatedNotification(context, switchedUser, NotificationUtils.ID.USER_SWITCHED, R.string.user_switched, R.drawable.person_fill0)
    }

    override fun onUserStopped(context: Context, intent: Intent, stoppedUser: UserHandle) {
        super.onUserStopped(context, intent, stoppedUser)
        sendUserRelatedNotification(context, stoppedUser, NotificationUtils.ID.USER_STOPPED, R.string.user_stopped, R.drawable.person_fill0)
    }

    override fun onUserRemoved(context: Context, intent: Intent, removedUser: UserHandle) {
        super.onUserRemoved(context, intent, removedUser)
        sendUserRelatedNotification(context, removedUser, NotificationUtils.ID.USER_REMOVED, R.string.user_removed, R.drawable.person_remove_fill0)
    }

    override fun onBugreportShared(context: Context, intent: Intent, hash: String) {
        super.onBugreportShared(context, intent, hash)
        val builder = NotificationCompat.Builder(context, NotificationUtils.Channel.EVENTS)
            .setContentTitle(context.getString(R.string.bug_report_shared))
            .setContentText("SHA-256 hash: $hash")
            .setSmallIcon(R.drawable.bug_report_fill0)
        NotificationUtils.notify(context, NotificationUtils.ID.BUG_REPORT_SHARED, builder.build())
    }

    override fun onBugreportSharingDeclined(context: Context, intent: Intent) {
        super.onBugreportSharingDeclined(context, intent)
        val builder = NotificationCompat.Builder(context, NotificationUtils.Channel.EVENTS)
            .setContentTitle(context.getString(R.string.bug_report_sharing_declined))
            .setSmallIcon(R.drawable.bug_report_fill0)
        NotificationUtils.notify(context, NotificationUtils.ID.BUG_REPORT_SHARING_DECLINED, builder.build())
    }

    override fun onBugreportFailed(context: Context, intent: Intent, failureCode: Int) {
        super.onBugreportFailed(context, intent, failureCode)
        val message = when(failureCode) {
            BUGREPORT_FAILURE_FAILED_COMPLETING -> R.string.bug_report_failure_failed_completing
            BUGREPORT_FAILURE_FILE_NO_LONGER_AVAILABLE -> R.string.bug_report_failure_no_longer_available
            else -> R.string.place_holder
        }
        val builder = NotificationCompat.Builder(context, NotificationUtils.Channel.EVENTS)
            .setContentTitle(context.getString(R.string.bug_report_failed))
            .setContentText(context.getString(message))
            .setSmallIcon(R.drawable.bug_report_fill0)
        NotificationUtils.notify(context, NotificationUtils.ID.BUG_REPORT_FAILED, builder.build())
    }

    override fun onSystemUpdatePending(context: Context, intent: Intent, receivedTime: Long) {
        super.onSystemUpdatePending(context, intent, receivedTime)
        val time = SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.getDefault()).format(Date(receivedTime))
        val builder = NotificationCompat.Builder(context, NotificationUtils.Channel.EVENTS)
            .setContentTitle(context.getString(R.string.system_update_pending))
            .setContentText(context.getString(R.string.received_time) + ": $time")
            .setSmallIcon(R.drawable.system_update_fill0)
        NotificationUtils.notify(context, NotificationUtils.ID.SYSTEM_UPDATE_PENDING, builder.build())
    }

    private fun sendUserRelatedNotification(context: Context, userHandle: UserHandle, id: Int, title: Int, icon: Int) {
        val um = context.getSystemService(Context.USER_SERVICE) as UserManager
        val serial = um.getSerialNumberForUser(userHandle)
        val builder = NotificationCompat.Builder(context, NotificationUtils.Channel.EVENTS)
            .setContentTitle(context.getString(title))
            .setContentText(context.getString(R.string.serial_number) + ": $serial")
            .setSmallIcon(icon)
        NotificationUtils.notify(context, id, builder.build())
    }
}
