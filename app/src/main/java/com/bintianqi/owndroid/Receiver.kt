package com.bintianqi.owndroid

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.PersistableBundle
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.bintianqi.owndroid.dpm.handleNetworkLogs
import com.bintianqi.owndroid.dpm.isDeviceOwner
import com.bintianqi.owndroid.dpm.isProfileOwner
import com.bintianqi.owndroid.dpm.processSecurityLogs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
        if(!context.isDeviceOwner && !context.isProfileOwner) SharedPrefs(context).isApiEnabled = false
    }

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        if(context.isProfileOwner || context.isDeviceOwner){
            Toast.makeText(context, context.getString(R.string.onEnabled), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Toast.makeText(context, R.string.onDisabled, Toast.LENGTH_SHORT).show()
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
                val events = getManager(context).retrieveSecurityLogs(ComponentName(context, this@Receiver::class.java)) ?: return@launch
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
        NotificationUtils.registerChannels(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intent = Intent(context, this::class.java).apply { action = "com.bintianqi.owndroid.action.STOP_LOCK_TASK_MODE" }
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val builder = NotificationCompat.Builder(context, "LockTaskMode")
            .setContentTitle(context.getText(R.string.lock_task_mode))
            .setSmallIcon(R.drawable.lock_fill0)
            .addAction(NotificationCompat.Action.Builder(null, context.getString(R.string.stop), pendingIntent).build())
            .setPriority(NotificationCompat.PRIORITY_HIGH)
        nm.notify(1, builder.build())
    }

    override fun onLockTaskModeExiting(context: Context, intent: Intent) {
        super.onLockTaskModeExiting(context, intent)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        nm.cancel(1)
    }
}
