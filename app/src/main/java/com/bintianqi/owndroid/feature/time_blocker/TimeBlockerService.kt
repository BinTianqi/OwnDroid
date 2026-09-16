package com.bintianqi.owndroid.feature.time_blocker

import android.app.Service
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.MyNotificationChannel
import com.bintianqi.owndroid.utils.NotificationType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Collections

@RequiresApi(24)
class TimeBlockerService : Service() {
    private val coroutineScope = CoroutineScope(Dispatchers.IO)
    private var pollingJob: Job? = null
    private val currentlySuspended = Collections.synchronizedSet(mutableSetOf<String>())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

        val notification = NotificationCompat.Builder(this, MyNotificationChannel.TimeBlocker.id)
            .setContentTitle(getString(R.string.time_blocker))
            .setSmallIcon(R.drawable.timer_fill0)
            .setOngoing(true)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()
        startForeground(NotificationType.TimeBlocker.id, notification)

        isRunning = true
        runningState.value = true
        val myApp = application as MyApplication
        val repo = myApp.container.timeBlockerRepo
        val ph = myApp.container.privilegeHelper

        // Crash recovery: restore state from DB
        pollingJob?.cancel()
        pollingJob = coroutineScope.launch {
            currentlySuspended.addAll(repo.getSuspendedByUs())

            while (true) {
                try {
                    val rules = repo.getEnabledRules()
                    val shouldSuspend = mutableSetOf<String>()
                    val now = System.currentTimeMillis()
                    val cal = Calendar.getInstance()
                    val currentMinutes = cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
                    // Calendar.DAY_OF_WEEK: Sun=1, Mon=2, ..., Sat=7
                    // We use 1=Mon..7=Sun
                    val dayOfWeek = when (cal.get(Calendar.DAY_OF_WEEK)) {
                        Calendar.MONDAY -> 1; Calendar.TUESDAY -> 2; Calendar.WEDNESDAY -> 3
                        Calendar.THURSDAY -> 4; Calendar.FRIDAY -> 5; Calendar.SATURDAY -> 6
                        Calendar.SUNDAY -> 7; else -> 1
                    }

                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    val dayStart = cal.timeInMillis

                    // Batch query usage for all rules with daily limits
                    val limitPackages = rules.filter { it.dailyLimitMinutes > 0 }.map { it.packageName }.toSet()
                    val usageMap = getUsageTodayBatch(limitPackages, dayStart, now)

                    for (rule in rules) {
                        var blocked = false

                        // Check time windows: blockedWindows block inside; allowedWindows block outside
                        if (isInAnyWindow(rule.blockedWindows, currentMinutes, dayOfWeek)) {
                            blocked = true
                        }
                        if (!blocked && rule.allowedWindows.isNotEmpty() &&
                            !isInAnyWindow(rule.allowedWindows, currentMinutes, dayOfWeek)
                        ) {
                            blocked = true
                        }

                        // Check daily limit
                        if (!blocked && rule.dailyLimitMinutes > 0) {
                            val usedMinutes = (usageMap[rule.packageName] ?: 0L) / 60_000
                            Log.d(TAG, "Usage ${rule.packageName}: ${usedMinutes}min used / ${rule.dailyLimitMinutes}min limit")
                            if (usedMinutes >= rule.dailyLimitMinutes) {
                                blocked = true
                            }
                        }

                        if (blocked) shouldSuspend.add(rule.packageName)
                    }

                    // Compute changes
                    val toUnsuspend = synchronized(currentlySuspended) { currentlySuspended - shouldSuspend }
                    val toSuspend = shouldSuspend - synchronized(currentlySuspended) { currentlySuspended.toSet() }

                    // Update tracking BEFORE executing DPM calls
                    if (toSuspend.isNotEmpty() || toUnsuspend.isNotEmpty()) {
                        currentlySuspended.removeAll(toUnsuspend)
                        currentlySuspended.addAll(toSuspend)
                        repo.setSuspendedByUs(synchronized(currentlySuspended) { currentlySuspended.toSet() })
                    }

                    // Now execute DPM calls
                    if (toUnsuspend.isNotEmpty()) {
                        ph.safeDpmCall {
                            dpm.setPackagesSuspended(dar, toUnsuspend.toTypedArray(), false)
                        }
                    }
                    if (toSuspend.isNotEmpty()) {
                        ph.safeDpmCall {
                            dpm.setPackagesSuspended(dar, toSuspend.toTypedArray(), true)
                        }
                    }

                    // Update notification
                    val notif = NotificationCompat.Builder(this@TimeBlockerService, MyNotificationChannel.TimeBlocker.id)
                        .setContentTitle(getString(R.string.time_blocker))
                        .setContentText(getString(R.string.time_blocker_active, rules.size))
                        .setSmallIcon(R.drawable.timer_fill0)
                        .setOngoing(true)
                        .build()
                    val nm = getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
                    nm.notify(NotificationType.TimeBlocker.id, notif)

                } catch (e: Exception) {
                    Log.e(TAG, "Error in time blocker check", e)
                }

                delay(30_000) // Check every 30 seconds
            }
        }

        return START_STICKY
    }

    private fun isInAnyWindow(windows: List<TimeWindow>, currentMinutes: Int, dayOfWeek: Int): Boolean {
        for (window in windows) {
            // For midnight-wrap windows in post-midnight portion, check previous day
            val effectiveDay = if (window.startMinutes > window.endMinutes && currentMinutes < window.endMinutes) {
                if (dayOfWeek == 1) 7 else dayOfWeek - 1
            } else {
                dayOfWeek
            }
            if (effectiveDay !in window.daysOfWeek) continue
            val inWindow = if (window.startMinutes <= window.endMinutes) {
                currentMinutes in window.startMinutes until window.endMinutes
            } else {
                currentMinutes >= window.startMinutes || currentMinutes < window.endMinutes
            }
            if (inWindow) return true
        }
        return false
    }

    private fun getUsageTodayBatch(packageNames: Set<String>, dayStart: Long, now: Long): Map<String, Long> {
        if (packageNames.isEmpty()) return emptyMap()
        return try {
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
                ?: return emptyMap()
            // We reconstruct usage from raw events instead of queryUsageStats():
            // the aggregated stats buckets are aligned to the service's internal
            // rollover points, which drift away from calendar midnight after clock
            // changes — and buckets for the currently-foreground app are often
            // missing entirely. Both make queryUsageStats unreliable on real devices.
            //
            // Robustness rules for the reconstruction:
            //  - A PAUSED without a preceding RESUMED is ignored (e.g. app was
            //    already running before our query window or process died).
            //  - A RESUMED while one is already open restarts the clock (overlap
            //    from config changes / multi-activity flows) — we keep the EARLIER
            //    start to avoid undercounting.
            //  - Sessions are clipped to [dayStart, now]: a session that started
            //    before today only counts from dayStart; one still open counts
            //    until now.
            val events = usm.queryEvents(dayStart, now)
            val event = android.app.usage.UsageEvents.Event()
            val totals = mutableMapOf<String, Long>()
            val resumeSince = mutableMapOf<String, Long>()

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName
                if (pkg !in packageNames) continue
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                    @Suppress("DEPRECATION") android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        // Keep the earlier start on overlapping resumes.
                        val existing = resumeSince[pkg]
                        if (existing == null || event.timeStamp < existing) {
                            resumeSince[pkg] = event.timeStamp
                        }
                    }
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                    @Suppress("DEPRECATION") android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val start = resumeSince.remove(pkg)
                        if (start != null) {
                            val from = maxOf(start, dayStart)
                            val delta = event.timeStamp - from
                            if (delta > 0) totals[pkg] = (totals[pkg] ?: 0L) + delta
                        }
                    }
                }
            }
            // Sessions still open at query time count up to now.
            for ((pkg, start) in resumeSince) {
                val from = maxOf(start, dayStart)
                val delta = now - from
                if (delta > 0) totals[pkg] = (totals[pkg] ?: 0L) + delta
            }
            totals
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get batch usage", e)
            emptyMap()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        coroutineScope.cancel()
        isRunning = false
        runningState.value = false

        // Unsuspend all packages we suspended (graceful stop)
        val myApp = application as MyApplication
        val snapshot = synchronized(currentlySuspended) { currentlySuspended.toTypedArray() }
        if (snapshot.isNotEmpty()) {
            myApp.container.privilegeHelper.safeDpmCall {
                dpm.setPackagesSuspended(dar, snapshot, false)
            }
        }
        myApp.container.timeBlockerRepo.setSuspendedByUs(emptySet())
    }

    companion object {
        private const val TAG = "TimeBlockerService"
        @Volatile var isRunning = false
            private set
        /** Observable running state for the UI (isRunning stays for sync checks). */
        val runningState = kotlinx.coroutines.flow.MutableStateFlow(false)

        fun start(context: Context) {
            val intent = Intent(context, TimeBlockerService::class.java)
            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimeBlockerService::class.java))
        }
    }
}
