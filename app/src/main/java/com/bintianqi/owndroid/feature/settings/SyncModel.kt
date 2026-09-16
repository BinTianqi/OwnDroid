package com.bintianqi.owndroid.feature.settings

import com.bintianqi.owndroid.feature.time_blocker.BlockRule
import com.bintianqi.owndroid.feature.time_blocker.TimeWindow
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Compact sync payload for QR transfer. Short keys keep the JSON small.
 * v = format version, totp = Base32 TOTP secret (empty = unchanged on import),
 * rules = time blocker rules.
 */
@Serializable
data class SyncPayload(
    @SerialName("v") val version: Int = 1,
    @SerialName("totp") val totpSecret: String = "",
    @SerialName("rules") val rules: List<SyncRule> = emptyList()
)

@Serializable
data class SyncRule(
    @SerialName("p") val packageName: String,
    @SerialName("l") val dailyLimitMinutes: Int = 0,
    @SerialName("b") val blockedWindows: List<SyncWindow> = emptyList(),
    @SerialName("a") val allowedWindows: List<SyncWindow> = emptyList(),
    @SerialName("e") val enabled: Boolean = true
) {
    fun toBlockRule(): BlockRule = BlockRule(
        packageName = packageName,
        dailyLimitMinutes = dailyLimitMinutes,
        blockedWindows = blockedWindows.map { it.toTimeWindow() },
        allowedWindows = allowedWindows.map { it.toTimeWindow() },
        enabled = enabled
    )

    companion object {
        fun fromBlockRule(rule: BlockRule): SyncRule = SyncRule(
            packageName = rule.packageName,
            dailyLimitMinutes = rule.dailyLimitMinutes,
            blockedWindows = rule.blockedWindows.map { SyncWindow.fromTimeWindow(it) },
            allowedWindows = rule.allowedWindows.map { SyncWindow.fromTimeWindow(it) },
            enabled = rule.enabled
        )
    }
}

@Serializable
data class SyncWindow(
    @SerialName("s") val startMinutes: Int,
    @SerialName("en") val endMinutes: Int,
    @SerialName("d") val daysOfWeek: Set<Int> = (1..7).toSet()
) {
    fun toTimeWindow(): TimeWindow = TimeWindow(
        startMinutes = startMinutes,
        endMinutes = endMinutes,
        daysOfWeek = daysOfWeek
    )

    fun isValid(): Boolean =
        startMinutes in 0..1439 && endMinutes in 0..1439 &&
                daysOfWeek.isNotEmpty() && daysOfWeek.all { it in 1..7 }

    companion object {
        fun fromTimeWindow(window: TimeWindow): SyncWindow = SyncWindow(
            startMinutes = window.startMinutes,
            endMinutes = window.endMinutes,
            daysOfWeek = window.daysOfWeek
        )
    }
}
