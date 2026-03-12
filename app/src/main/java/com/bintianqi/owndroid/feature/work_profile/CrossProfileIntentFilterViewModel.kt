package com.bintianqi.owndroid.feature.work_profile

import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.serialization.json.Json

class CrossProfileIntentFilterViewModel(
    val application: MyApplication, val ph: PrivilegeHelper,
    val repo: CrossProfileIntentFilterRepository, val toastChannel: ToastChannel
) : ViewModel() {
    private fun addFilterInternal(options: IntentFilterOptions) = ph.safeDpmCall {
        val filter = IntentFilter(options.action)
        if (options.category.isNotEmpty()) filter.addCategory(options.category)
        if (options.mimeType.isNotEmpty()) filter.addDataType(options.mimeType)
        dpm.addCrossProfileIntentFilter(dar, filter, options.direction)
        repo.setCrossProfileIntentFilter(options)
    }

    fun addFilter(options: IntentFilterOptions) {
        addFilterInternal(options)
        toastChannel.sendStatus(true)
    }

    fun addPreset(preset: IntentFilterPreset, direction: Int) {
        addFilter(IntentFilterOptions(preset.action, preset.category, preset.mimeType, direction))
    }

    fun clearFilters() = ph.safeDpmCall {
        dpm.clearCrossProfileIntentFilters(dar)
        repo.deleteAllCrossProfileIntentFilters()
        toastChannel.sendStatus(true)
    }

    fun importFilters(uri: Uri) = ph.safeDpmCall {
        val bytes = application.contentResolver.openInputStream(uri)!!.use {
            it.readBytes().decodeToString()
        }
        val data = Json.decodeFromString<List<IntentFilterOptions>>(bytes)
        data.forEach {
            addFilterInternal(it)
        }
        toastChannel.sendStatus(true)
    }

    fun exportFilters(uri: Uri) {
        val data = repo.getAllCrossProfileIntentFilters()
        val bytes = Json.encodeToString(data).encodeToByteArray()
        application.contentResolver.openOutputStream(uri)!!.use {
            it.write(bytes)
        }
        toastChannel.sendStatus(true)
    }
}
