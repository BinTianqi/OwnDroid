package com.bintianqi.owndroid.feature.settings

import kotlinx.serialization.json.Json
import java.io.File

class SettingsRepository(val file: File) {
    var data: MySettings

    init {
        if (file.exists()) {
            data = readData()
        } else {
            data = MySettings()
            write()
        }
    }

    fun readData(): MySettings {
        return Json.Default.decodeFromString(file.readText())
    }

    fun update(block: (MySettings) -> Unit) {
        block(data)
        write()
    }

    fun write() {
        file.writeText(Json.encodeToString(data))
    }
}
