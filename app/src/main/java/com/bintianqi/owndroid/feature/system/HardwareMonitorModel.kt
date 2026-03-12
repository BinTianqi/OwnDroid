package com.bintianqi.owndroid.feature.system

import android.os.HardwarePropertiesManager
import androidx.annotation.RequiresApi
import com.bintianqi.owndroid.R

class HardwareProperties(
    val temperatures: Map<Int, List<Float>> = emptyMap(),
    val cpuUsages: List<Pair<Long, Long>> = emptyList(),
    val fanSpeeds: List<Float> = emptyList()
)

@RequiresApi(24)
val temperatureTypes = mapOf(
    HardwarePropertiesManager.DEVICE_TEMPERATURE_CPU to R.string.cpu_temp,
    HardwarePropertiesManager.DEVICE_TEMPERATURE_GPU to R.string.gpu_temp,
    HardwarePropertiesManager.DEVICE_TEMPERATURE_BATTERY to R.string.battery_temp,
    HardwarePropertiesManager.DEVICE_TEMPERATURE_SKIN to R.string.skin_temp
)
