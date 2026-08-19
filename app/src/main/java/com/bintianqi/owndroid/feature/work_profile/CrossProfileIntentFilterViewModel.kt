package com.bintianqi.owndroid.feature.work_profile

import android.content.IntentFilter
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

class CrossProfileIntentFilterViewModel(
    val application: MyApplication, val ph: PrivilegeHelper,
    val repo: CrossProfileIntentFilterRepository, val toastChannel: ToastChannel,
    val sr: SettingsRepository
) : ViewModel() {
    val filterListState = MutableStateFlow(emptyList<IntentFilterEntry>())
    val filtersChangedState = MutableStateFlow(false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            filterListState.value = repo.getAllFilters()
            filtersChangedState.value = sr.data.cpifChanged
        }
    }

    fun addFilter(options: IntentFilterOptions) {
        viewModelScope.launch(Dispatchers.IO) {
            val id = repo.addFilter(options)
            filterListState.update {
                listOf(IntentFilterEntry(id.toInt(), options, System.currentTimeMillis())) + it
            }
            setFilterChanged(true)
        }
    }

    fun updateFilter(id: Int, options: IntentFilterOptions) {
        repo.updateFilter(id, options)
        filterListState.update { list ->
            val index = list.indexOfFirst { it.id == id }
            val newList = list.toMutableList()
            newList[index] = IntentFilterEntry(id, options, System.currentTimeMillis())
            newList.sortedByDescending { it.time }
        }
    }

    fun addPreset(preset: IntentFilterPreset, direction: Int) {
        addFilter(IntentFilterOptions(preset.action, preset.category, preset.mimeType, direction))
        toastChannel.sendStatus(true)
    }

    fun deleteEntry(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.deleteById(id)
            filterListState.update { list -> list.filter { it.id != id } }
            setFilterChanged(true)
        }
    }

    fun deleteAllFilters() {
        repo.deleteAllCrossProfileIntentFilters()
        filterListState.value = emptyList()
        setFilterChanged(true)
    }

    fun applyFilters() {
        viewModelScope.launch(Dispatchers.IO) {
            ph.safeDpmCall {
                dpm.clearCrossProfileIntentFilters(dar)
                filterListState.value.forEach {
                    val filter = IntentFilter(it.options.action)
                    if (it.options.category.isNotEmpty()) filter.addCategory(it.options.category)
                    if (it.options.mimeType.isNotEmpty()) filter.addDataType(it.options.mimeType)
                    dpm.addCrossProfileIntentFilter(dar, filter, it.options.direction)
                }
            }
            setFilterChanged(false)
            toastChannel.sendStatus(true)
        }
    }

    fun importFilters(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val bytes = application.contentResolver.openInputStream(uri)!!.use {
                    it.readBytes().decodeToString()
                }
                val data = Json.decodeFromString<List<IntentFilterOptions>>(bytes)
                data.forEach {
                    repo.addFilter(it)
                }
                filterListState.value = repo.getAllFilters()
                setFilterChanged(true)
            } catch (e: Exception) {
                e.printStackTrace()
                toastChannel.sendStatus(false)
            }
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

    private fun setFilterChanged(changed: Boolean) {
        filtersChangedState.value = changed
        sr.update { it.cpifChanged = changed }
    }
}
