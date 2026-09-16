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
                var nextDelayMs = MAX_POLL_MS
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

                    // Adaptive polling: sleep until the next moment a blocking state
                    // can change — a daily limit being reached (usage only grows in
                    // real time, so remaining-limit time is the earliest possible
                    // flip) or a time-window edge — bounded to [MIN_POLL_MS, MAX_POLL_MS].
                    for (rule in rules) {
                        if (rule.dailyLimitMinutes > 0) {
                            val usedMs = usageMap[rule.packageName] ?: 0L
                            val remainingMs = rule.dailyLimitMinutes * 60_000L - usedMs
                            if (remainingMs > 0) nextDelayMs = minOf(nextDelayMs, remainingMs)
                        }
                        val untilFlip = minutesUntilWindowChange(rule, currentMinutes, dayOfWeek)
                        if (untilFlip != null) nextDelayMs = minOf(nextDelayMs, untilFlip * 60_000L)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error in time blocker check", e)
                    nextDelayMs = MIN_POLL_MS // retry soon after transient errors
                }

                nextDelayMs = nextDelayMs.coerceIn(MIN_POLL_MS, MAX_POLL_MS)
                Log.d(TAG, "Next check in ${nextDelayMs / 1000}s")
                delay(nextDelayMs)
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

    /**
     * Minutes until the window-based blocking state of [rule] flips, or null when
     * no flip happens within the next 24h (or no windows configured). The scan
     * horizon covers a full day; distant flips are picked up by later poll cycles
     * once they enter the horizon.
     */
    private fun minutesUntilWindowChange(rule: BlockRule, currentMinutes: Int, dayOfWeek: Int): Int? {
        if (rule.blockedWindows.isEmpty() && rule.allowedWindows.isEmpty()) return null

        fun windowBlocked(minutes: Int, day: Int): Boolean {
            if (isInAnyWindow(rule.blockedWindows, minutes, day)) return true
            if (rule.allowedWindows.isNotEmpty() && !isInAnyWindow(rule.allowedWindows, minutes, day)) return true
            return false
        }

        val nowBlocked = windowBlocked(currentMinutes, dayOfWeek)
        for (t in 1..1440) {
            val total = currentMinutes + t
            val futureDay = ((dayOfWeek - 1 + total / 1440) % 7) + 1
            if (windowBlocked(total % 1440, futureDay) != nowBlocked) return t
        }
        return null
    }

    private fun getUsageTodayBatch(packageNames: Set<String>, dayStart: Long, now: Long): Map<String, Long> {
        if (packageNames.isEmpty()) return emptyMap()
        val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            ?: return emptyMap()

        // Two independent methods — take the higher per package.
        // Method 1 (events) is precise and handles the current foreground session,
        // but fails on devices whose usage-stats timeline is shifted into the
        // future (all event timestamps land outside [dayStart, now]).
        // Method 2 (stats with a ±30-day window) catches those shifted buckets by
        // finding the newest daily bucket regardless of absolute timestamp, but may
        // lag for the currently-open session.
        val eventTotals = getUsageFromEvents(usm, dayStart, now, packageNames)
        val statsTotals = getUsageFromStats(usm, now, packageNames)

        val merged = mutableMapOf<String, Long>()
        for (pkg in packageNames) {
            merged[pkg] = maxOf(eventTotals[pkg] ?: 0L, statsTotals[pkg] ?: 0L)
        }
        return merged
    }

    /**
     * Reconstruct usage from raw RESUMED/PAUSED events (precise, real-time).
     *
     * Uses a wide ±30-day query window because some devices (observed on Samsung
     * tablets after clock changes) have their UsageStats timeline shifted days
     * into the future — a narrow [dayStart, now] query would return nothing.
     * Instead, we derive the "effective day start" from the newest event
     * timestamp we see: that timestamp belongs to the current bucket, so its
     * calendar-midnight IS the device's internal "today" boundary. All events
     * are then clipped against that dynamically-determined boundary.
     */
    private fun getUsageFromEvents(
        usm: UsageStatsManager, dayStart: Long, now: Long, packageNames: Set<String>
    ): Map<String, Long> {
        return try {
            val thirtyDays = 30L * 24 * 3600_000
            val events = usm.queryEvents(now - thirtyDays, now + thirtyDays)
            val event = android.app.usage.UsageEvents.Event()
            val totals = mutableMapOf<String, Long>()
            val resumeSince = mutableMapOf<String, Long>()
            var newestTs = 0L

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName
                if (pkg !in packageNames) continue
                if (event.timeStamp > newestTs) newestTs = event.timeStamp
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                    @Suppress("DEPRECATION") android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        val existing = resumeSince[pkg]
                        if (existing == null || event.timeStamp < existing) {
                            resumeSince[pkg] = event.timeStamp
                        }
                    }
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                    @Suppress("DEPRECATION") android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        val start = resumeSince.remove(pkg)
                        if (start != null) {
                            // Clip against the effective day start (see below) and
                            // count only positive durations.
                            val delta = event.timeStamp - start
                            if (delta > 0) {
                                totals[pkg] = (totals[pkg] ?: 0L) + delta
                            }
                        }
                    }
                }
            }
            // Still-open sessions count up to the newest event we saw (the
            // internal "now" of the usage-stats timeline), not the wall clock.
            if (newestTs > 0) {
                for ((pkg, start) in resumeSince) {
                    val delta = newestTs - start
                    if (delta > 0) totals[pkg] = (totals[pkg] ?: 0L) + delta
                }
            }
            totals
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get event-based usage", e)
            emptyMap()
        }
    }

    /**
     * Query aggregated stats over a ±30-day window and take the newest daily
     * bucket per package. This catches devices whose usage-stats rollover
     * timestamps have drifted days into the future after clock changes —
     * [queryUsageStats] with a narrow [dayStart, now] window would miss them.
     */
    private fun getUsageFromStats(
        usm: UsageStatsManager, now: Long, packageNames: Set<String>
    ): Map<String, Long> {
        return try {
            val thirtyDays = 30L * 24 * 3600_000
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - thirtyDays,
                now + thirtyDays
            )
            val newestBucket = mutableMapOf<String, Long>()
            val totals = mutableMapOf<String, Long>()
            if (stats != null) {
                for (stat in stats) {
                    val pkg = stat.packageName
                    if (pkg !in packageNames) continue
                    if (stat.totalTimeInForeground <= 0L) continue
                    val prev = newestBucket[pkg]
                    if (prev == null || stat.firstTimeStamp > prev) {
                        newestBucket[pkg] = stat.firstTimeStamp
                        totals[pkg] = stat.totalTimeInForeground
                    }
                }
            }
            totals
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get stats-based usage", e)
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
        // Adaptive polling bounds: never poll faster than MIN, never sleep longer
        // than MAX (so rule changes and near-limit foreground sessions stay timely).
        private const val MIN_POLL_MS = 30_000L
        private const val MAX_POLL_MS = 5 * 60_000L
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
