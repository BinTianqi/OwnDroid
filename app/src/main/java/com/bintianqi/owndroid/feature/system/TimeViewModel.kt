package com.bintianqi.owndroid.feature.system

import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.ZoneId
import java.time.ZonedDateTime

class TimeViewModel(
    val ph: PrivilegeHelper, val toastChannel: ToastChannel
) : ViewModel() {
    @RequiresApi(28)
    fun setTime(time: Long, useCurrentTz: Boolean) = ph.safeDpmCall {
        val offset = if (useCurrentTz) {
            ZonedDateTime.now(ZoneId.systemDefault()).offset.totalSeconds * 1000L
        } else 0L
        val result = dpm.setTime(dar, time - offset)
        toastChannel.sendStatus(result)
    }

    @RequiresApi(28)
    fun setTimeZone(tz: String) = ph.safeDpmCall {
        val result = dpm.setTimeZone(dar, tz)
        toastChannel.sendStatus(result)
    }

    val autoTimePolicyState = MutableStateFlow(0)

    @RequiresApi(36)
    fun getAutoTimePolicy() = ph.safeDpmCall {
        autoTimePolicyState.value = dpm.autoTimePolicy
    }

    @RequiresApi(36)
    fun setAutoTimePolicy(policy: Int) = ph.safeDpmCall {
        dpm.autoTimePolicy = policy
        getAutoTimePolicy()
    }

    val autoTimeZonePolicyState = MutableStateFlow(0)

    @RequiresApi(36)
    fun getAutoTimeZonePolicy() = ph.safeDpmCall {
        autoTimeZonePolicyState.value = dpm.autoTimeZonePolicy
    }

    @RequiresApi(36)
    fun setAutoTimeZonePolicy(policy: Int) = ph.safeDpmCall {
        dpm.autoTimeZonePolicy = policy
        getAutoTimeZonePolicy()
    }
}