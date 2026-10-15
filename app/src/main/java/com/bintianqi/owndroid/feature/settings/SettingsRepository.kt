package com.bintianqi.owndroid.feature.settings

import kotlinx.serialization.json.Json
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

class SettingsRepository(val file: File) {
    var data: MySettings
    val hideDestructiveOptionsState = MutableStateFlow(false)

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    init {
        if (file.exists()) {
            data = readData()
        } else {
            data = MySettings()
            write()
        }
        hideDestructiveOptionsState.value = data.hideDestructiveOptions
    }

    fun readData(): MySettings {
        return json.decodeFromString(file.readText())
    }

    fun update(block: (MySettings) -> Unit) {
        block(data)
        write()
        hideDestructiveOptionsState.value = data.hideDestructiveOptions
    }

    fun write() {
        file.writeText(json.encodeToString(data))
    }
}
