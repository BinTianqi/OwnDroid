package com.bintianqi.owndroid.feature.time_blocker

import kotlinx.serialization.Serializable

@Serializable
data class BlockRule(
    val id: Int = 0,
    val packageName: String,
    val dailyLimitMinutes: Int = 0,        // 0 = no daily limit
    val blockedWindows: List<TimeWindow> = emptyList(),
    val allowedWindows: List<TimeWindow> = emptyList(),
    val enabled: Boolean = true
)

@Serializable
data class TimeWindow(
    val startMinutes: Int,   // minutes since 00:00
    val endMinutes: Int,     // minutes since 00:00, can be < startMinutes (wraps midnight)
    val daysOfWeek: Set<Int> = (1..7).toSet()  // 1=Mon .. 7=Sun
)
