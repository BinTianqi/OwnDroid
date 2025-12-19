package com.bintianqi.owndroid

import android.app.ActivityManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@RequiresApi(28)
class LockTaskService: Service() {
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    val stopReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            coroutineScope.cancel()
            stopLockTask()
            stop()
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val filter = IntentFilter(STOP_ACTION)
        ContextCompat.registerReceiver(
            this, stopReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED
        )
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, Intent(STOP_ACTION).setPackage(this.packageName), PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, MyNotificationChannel.LockTaskMode.id)
            .setContentTitle(getText(R.string.lock_task_mode))
            .setSmallIcon(R.drawable.lock_fill0)
            .addAction(NotificationCompat.Action.Builder(null, getString(R.string.stop), pendingIntent).build())
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        ServiceCompat.startForeground(
            this, NotificationType.LockTaskMode.id, notification,
            if (Build.VERSION.SDK_INT < 34) 0 else ServiceInfo.FOREGROUND_SERVICE_TYPE_MANIFEST
        )
        coroutineScope.launch {
            val am = getSystemService(ActivityManager::class.java)
            delay(3000)
            while (am.lockTaskModeState == ActivityManager.LOCK_TASK_MODE_LOCKED) {
                delay(1000)
            }
            stop()
        }
        return START_NOT_STICKY
    }

    fun stop() {
        unregisterReceiver(stopReceiver)
        stopSelf()
    }

    fun stopLockTask() {
        val features = Privilege.DPM.getLockTaskFeatures(Privilege.DAR)
        val packages = Privilege.DPM.getLockTaskPackages(Privilege.DAR)
        Privilege.DPM.setLockTaskPackages(Privilege.DAR, arrayOf())
        Privilege.DPM.setLockTaskPackages(Privilege.DAR, packages)
        Privilege.DPM.setLockTaskFeatures(Privilege.DAR, features)
    }

    companion object {
        const val STOP_ACTION = "com.bintianqi.owndroid.action.STOP_LOCK_TASK_MODE"
    }
}