package com.bintianqi.owndroid.feature.system

import android.os.HardwarePropertiesManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class HardwareMonitorViewModel(val application: MyApplication) : ViewModel() {
    val propertiesState = MutableStateFlow(HardwareProperties())

    var refreshInterval = 1000L

    fun setRefreshInterval(interval: Float) {
        refreshInterval = (interval * 1000).toLong()
    }

    lateinit var job: Job

    @RequiresApi(24)
    fun startHardwareMonitor() {
        job = viewModelScope.launch {
            val hpm = application.getSystemService(HardwarePropertiesManager::class.java)
            while (true) {
                val properties = HardwareProperties(
                    temperatureTypes.map { (type, _) ->
                        type to hpm.getDeviceTemperatures(
                            type, HardwarePropertiesManager.TEMPERATURE_CURRENT
                        ).toList()
                    }.toMap(),
                    hpm.cpuUsages.map { it.active to it.total },
                    hpm.fanSpeeds.toList()
                )
                if (properties.cpuUsages.isEmpty() && properties.fanSpeeds.isEmpty() &&
                    properties.temperatures.isEmpty()
                ) {
                    break
                }
                propertiesState.value = properties
                delay(refreshInterval)
            }
        }
    }

    override fun onCleared() {
        job.cancel()
        super.onCleared()
    }
}
