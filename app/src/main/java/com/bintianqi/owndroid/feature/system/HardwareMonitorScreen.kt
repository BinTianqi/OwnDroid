package com.bintianqi.owndroid.feature.system

import android.os.HardwarePropertiesManager
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyScaffold
import kotlin.math.roundToLong

@RequiresApi(24)
@Composable
fun HardwareMonitorScreen(
    vm: HardwareMonitorViewModel, onNavigateUp: () -> Unit
) {
    val properties by vm.propertiesState.collectAsState()
    var refreshInterval by rememberSaveable { mutableFloatStateOf(1F) }
    val refreshIntervalMs = (refreshInterval * 1000).roundToLong()
    LaunchedEffect(Unit) {
        vm.startHardwareMonitor()
    }
    MyScaffold(R.string.hardware_monitor, onNavigateUp) {
        Text(
            stringResource(R.string.refresh_interval), Modifier.padding(top = 8.dp, bottom = 4.dp),
            style = typography.titleLarge
        )
        Slider(refreshInterval, {
            refreshInterval = it
            vm.setRefreshInterval(it)
        }, valueRange = 0.5F..2F, steps = 14)
        Text("${refreshIntervalMs}ms")
        Spacer(Modifier.padding(vertical = 10.dp))
        properties.temperatures.forEach { tempMapItem ->
            Text(
                stringResource(temperatureTypes[tempMapItem.key]!!), style = typography.titleLarge,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            if (tempMapItem.value.isEmpty()) {
                Text(stringResource(R.string.unsupported))
            } else {
                tempMapItem.value.forEachIndexed { index, temp ->
                    Row(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            index.toString(), style = typography.titleMedium,
                            modifier = Modifier.padding(start = 8.dp, end = 12.dp)
                        )
                        Text(
                            if (temp == HardwarePropertiesManager.UNDEFINED_TEMPERATURE) stringResource(
                                R.string.undefined
                            ) else temp.toString()
                        )
                    }
                }
            }
            Spacer(Modifier.padding(vertical = 10.dp))
        }
        Text(
            stringResource(R.string.cpu_usages), style = typography.titleLarge,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        if (properties.cpuUsages.isEmpty()) {
            Text(stringResource(R.string.unsupported))
        } else {
            properties.cpuUsages.forEachIndexed { index, usage ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        index.toString(), style = typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp, end = 12.dp)
                    )
                    Column {
                        Text(stringResource(R.string.active) + ": " + usage.first + "ms")
                        Text(stringResource(R.string.total) + ": " + usage.second + "ms")
                    }
                }
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(
            stringResource(R.string.fan_speeds), style = typography.titleLarge,
            modifier = Modifier.padding(vertical = 4.dp)
        )
        if (properties.fanSpeeds.isEmpty()) {
            Text(stringResource(R.string.unsupported))
        } else {
            properties.fanSpeeds.forEachIndexed { index, speed ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        index.toString(), style = typography.titleMedium,
                        modifier = Modifier.padding(start = 8.dp, end = 12.dp)
                    )
                    Text("$speed RPM")
                }
            }
        }
    }
}
