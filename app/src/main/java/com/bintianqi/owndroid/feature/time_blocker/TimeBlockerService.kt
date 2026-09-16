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

    // Self-managed foreground tracking. On devices with corrupted UsageStats
    // (clock was time-traveled, buckets shifted into the future), the system
    // stats are unreliable. We track foreground state directly by querying
    // events with a forward-extended window — event timestamps are correct,
    // only the bucket indexing is shifted.
    //
    // At each poll we:
    //   1. Query the last few minutes of events with a forward-extended end
    //      to catch the current foreground transition.
    //   2. Determine which monitored app was last resumed (i.e. is currently
    //      in the foreground).
    //   3. Accumulate wall-clock time for that app since the last poll.
    private val foregroundWallClockStart = Collections.synchronizedMap(mutableMapOf<String, Long>())
    // Self-accumulated usage per package since service start, in milliseconds.
    private val usageTodayMs = Collections.synchronizedMap(mutableMapOf<String, Long>())
    // Frozen baseline per package: usage that happened BEFORE this service
    // started. Total usage = baselineMs + usageTodayMs. The baseline is read
    // once at service start (max of system stats and persisted value) and
    // never updated afterwards — updating it would double-count time that the
    // self-managed tracker already covered once the system flushes its stats.
    private val baselineMs = Collections.synchronizedMap(mutableMapOf<String, Long>())
    // The calendar day (epochDay) the tracking state belongs to.
    @Volatile private var usageDayEpoch: Long = -1L
    @Volatile private var lastPollWallClock: Long = 0L

    // Previous cycle's usage values — used to detect apps that are actively
    // being used (usage growing) so we can poll them more frequently.
    private val prevUsageMs = Collections.synchronizedMap(mutableMapOf<String, Long>())

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
            // Initialize the usage baseline: everything that happened before
            // this service started. We take the max of the system's persisted
            // stats and our own persisted merged value — whichever is fresher.
            // Self-managed counting (usageTodayMs) starts at 0 from here.
            val todayEpoch = java.time.LocalDate.now().toEpochDay()
            usageDayEpoch = todayEpoch
            usageTodayMs.clear()
            baselineMs.clear()
            foregroundWallClockStart.clear()
            val usm = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            val now = System.currentTimeMillis()
            val dayStartCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
            }
            val pkgs = repo.getEnabledRules().map { it.packageName }.toSet()
            if (usm != null && pkgs.isNotEmpty()) {
                val sys = getSystemUsageToday(usm, dayStartCal.timeInMillis, now, pkgs)
                val persisted = repo.getUsageToday(todayEpoch)
                for (pkg in pkgs) {
                    baselineMs[pkg] = maxOf(sys[pkg] ?: 0L, persisted[pkg] ?: 0L)
                }
            }

            // Immediately seed the foreground tracker: if a monitored app is
            // already in the foreground right now (e.g. user opened it while
            // the service was down), we start counting from service start.
            val currentFg = try {
                usm?.let { getForegroundFromEvents(it, pkgs) }
            } catch (_: Exception) { null }
            if (currentFg != null) {
                foregroundWallClockStart[currentFg] = now
            }
            lastPollWallClock = now

            var lastSaveWallClock = 0L

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

                    // Persist the merged total every 5 minutes so it survives
                    // service restarts (it becomes the next start's baseline).
                    val persistNow = System.currentTimeMillis()
                    if (persistNow - lastSaveWallClock > 5 * 60_000L) {
                        repo.saveUsageToday(usageDayEpoch, mergedTotals())
                        lastSaveWallClock = persistNow
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

                            // If this app's usage grew since the last check, the app
                            // is actively in the foreground right now. Poll it at the
                            // minimum interval so we catch the limit as soon as it's hit.
                            val prevUsed = prevUsageMs[rule.packageName] ?: 0L
                            if (usedMs > prevUsed) {
                                nextDelayMs = minOf(nextDelayMs, MIN_POLL_MS)
                            }
                            prevUsageMs[rule.packageName] = usedMs
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

        // Reset tracking state at day rollover: new day, new baseline.
        val todayEpoch = java.time.LocalDate.now().toEpochDay()
        if (usageDayEpoch != todayEpoch) {
            usageDayEpoch = todayEpoch
            usageTodayMs.clear()
            foregroundWallClockStart.clear()
            lastPollWallClock = 0L
            // Fresh baseline for the new day (system stats for the new day).
            baselineMs.clear()
            val fresh = getSystemUsageToday(usm, dayStart, now, packageNames)
            baselineMs.putAll(fresh)
        }

        // Determine which monitored app is currently in the foreground by
        // querying recent events with a forward-extended end. Event timestamps
        // are real-time even on devices with corrupted bucket indexing.
        val wallClockNow = System.currentTimeMillis()
        val currentForeground = getForegroundFromEvents(usm, packageNames)

        // Accumulate wall-clock time for the app that was in the foreground
        // during the last poll interval.
        if (lastPollWallClock > 0) {
            val elapsed = wallClockNow - lastPollWallClock
            if (elapsed > 0) {
                synchronized(foregroundWallClockStart) {
                    for ((pkg, startTime) in foregroundWallClockStart) {
                        usageTodayMs[pkg] = (usageTodayMs[pkg] ?: 0L) + (wallClockNow - startTime)
                    }
                    foregroundWallClockStart.clear()
                }
            }
        }
        lastPollWallClock = wallClockNow

        // Mark the currently-foreground app for the next poll.
        if (currentForeground != null) {
            foregroundWallClockStart[currentForeground] = wallClockNow
        }

        // Total usage = frozen baseline (before service start) + self-managed
        // accumulation (since service start). The baseline is intentionally NOT
        // re-read here: the system flushes its stats lazily, so re-reading
        // would double-count time our own tracker already covered.
        val merged = mutableMapOf<String, Long>()
        for (pkg in packageNames) {
            merged[pkg] = (baselineMs[pkg] ?: 0L) + (usageTodayMs[pkg] ?: 0L)
        }
        return merged
    }

    /** Current merged totals (baseline + self-managed) for all tracked packages. */
    private fun mergedTotals(): Map<String, Long> {
        val merged = mutableMapOf<String, Long>()
        val pkgs = baselineMs.keys + usageTodayMs.keys
        for (pkg in pkgs) {
            merged[pkg] = (baselineMs[pkg] ?: 0L) + (usageTodayMs[pkg] ?: 0L)
        }
        return merged
    }

    /**
     * Determine which monitored package is currently in the foreground by
     * querying recent usage events. The events come back chronologically
     * ordered; we track the last RESUMED/PAUSED transition per package to
     * know which app is currently active.
     *
     * On devices with corrupted UsageStats (clock was time-traveled), the
     * query window extends into the future to catch events whose timestamps
     * were written with a shifted clock. The FUTURE-dated events are only
     * system junk (config changes, broadcasts), not RESUMED/PAUSED for the
     * monitored apps, so the chronological ordering still yields the right
     * answer.
     */
    private fun getForegroundFromEvents(
        usm: UsageStatsManager, packageNames: Set<String>
    ): String? {
        return try {
            val now = System.currentTimeMillis()
            val events = usm.queryEvents(now - 24L * 3600_000, now + 14L * 24 * 3600_000)
            val event = android.app.usage.UsageEvents.Event()
            var lastResumed: String? = null

            while (events.hasNextEvent()) {
                events.getNextEvent(event)
                val pkg = event.packageName
                if (pkg !in packageNames) continue
                when (event.eventType) {
                    android.app.usage.UsageEvents.Event.ACTIVITY_RESUMED,
                    @Suppress("DEPRECATION") android.app.usage.UsageEvents.Event.MOVE_TO_FOREGROUND -> {
                        lastResumed = pkg
                    }
                    android.app.usage.UsageEvents.Event.ACTIVITY_PAUSED,
                    @Suppress("DEPRECATION") android.app.usage.UsageEvents.Event.MOVE_TO_BACKGROUND -> {
                        if (pkg == lastResumed) {
                            lastResumed = null
                        }
                    }
                }
            }
            lastResumed
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get foreground from events", e)
            null
        }
    }

    /**
     * Read the system's persisted usage for today. Uses a wide window (24h back,
     * 14 days forward) to catch corrupted timelines where bucket dates are
     * shifted into the future.
     */
    private fun getSystemUsageToday(
        usm: UsageStatsManager, dayStart: Long, now: Long, packageNames: Set<String>
    ): Map<String, Long> {
        return try {
            val stats = usm.queryUsageStats(
                UsageStatsManager.INTERVAL_DAILY,
                now - 24L * 3600_000,
                now + 14L * 24 * 3600_000
            )
            val totals = mutableMapOf<String, Long>()
            if (stats != null) {
                for (stat in stats) {
                    if (stat.packageName in packageNames && stat.totalTimeInForeground > 0) {
                        // Take the newest bucket (corrupted devices may have
                        // multiple future-dated buckets).
                        val existing = totals[stat.packageName] ?: 0L
                        if (stat.totalTimeInForeground > existing) {
                            totals[stat.packageName] = stat.totalTimeInForeground
                        }
                    }
                }
            }
            totals
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get system usage", e)
            emptyMap()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Persist the merged total before shutdown (becomes next baseline).
        try {
            val repo = (application as MyApplication).container.timeBlockerRepo
            repo.saveUsageToday(usageDayEpoch, mergedTotals())
        } catch (e: Exception) {
            Log.e(TAG, "Failed to persist usage on destroy", e)
        }
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
