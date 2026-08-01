package com.bintianqi.owndroid.feature.work_profile

import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CrossProfileIntentFilterViewModel(
    val application: MyApplication, val ph: PrivilegeHelper,
    val repo: CrossProfileIntentFilterRepository, val toastChannel: ToastChannel
) : ViewModel() {
    val filterListState = MutableStateFlow(emptyList<IntentFilterEntry>())

    init {
        viewModelScope.launch(Dispatchers.IO) {
            filterListState.value = repo.getAllFilters()
        }
    }

    private fun addFilterInternal(options: IntentFilterOptions) {
        val filter = IntentFilter(options.action)
        if (options.category.isNotEmpty()) filter.addCategory(options.category)
        if (options.mimeType.isNotEmpty()) filter.addDataType(options.mimeType)
        ph.safeDpmCall {
            dpm.addCrossProfileIntentFilter(dar, filter, options.direction)
        }
        repo.setCrossProfileIntentFilter(options)
    }

    fun addFilter(options: IntentFilterOptions) {
        viewModelScope.launch(Dispatchers.IO) {
            addFilterInternal(options)
            filterListState.value = repo.getAllFilters()
            toastChannel.sendStatus(true)
        }
    }

    fun addPreset(preset: IntentFilterPreset, direction: Int) {
        addFilter(IntentFilterOptions(preset.action, preset.category, preset.mimeType, direction))
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val newList = filterListState.value.filter { it.id != id }
            ph.safeDpmCall {
                dpm.clearCrossProfileIntentFilters(dar)
                newList.forEach {
                    addFilterInternal(it.options)
                }
            }
            repo.deleteById(id)
            filterListState.value = newList
        }
    }

    fun deleteAllFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            ph.safeDpmCall {
                dpm.clearCrossProfileIntentFilters(dar)
            }
            repo.deleteAllCrossProfileIntentFilters()
            filterListState.value = emptyList()
        }
    }

    fun importFilters(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val bytes = application.contentResolver.openInputStream(uri)!!.use {
                it.readBytes().decodeToString()
            }
            val data = Json.decodeFromString<List<IntentFilterOptions>>(bytes)
            data.forEach {
                addFilterInternal(it)
            }
            filterListState.value = repo.getAllFilters()
            toastChannel.sendStatus(true)
        }
    }

    fun exportFilters(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            val data = filterListState.value.map { it.options }
            val bytes = Json.encodeToString(data).encodeToByteArray()
            application.contentResolver.openOutputStream(uri)!!.use {
                it.write(bytes)
            }
            toastChannel.sendStatus(true)
        }
    }
}
