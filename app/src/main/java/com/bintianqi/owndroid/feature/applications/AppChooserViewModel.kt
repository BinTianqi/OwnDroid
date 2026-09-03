package com.bintianqi.owndroid.feature.applications

import android.content.pm.ApplicationInfo
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.getInstalledAppsFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.concurrent.atomics.AtomicInt
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlin.concurrent.atomics.update

class AppChooserViewModel(val application: MyApplication, val ph: PrivilegeHelper) : ViewModel() {
    private val packagesState = MutableStateFlow(emptyList<AppChooserEntry>())
    val displayPackagesState = packagesState
        .sample(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), emptyList())

    private val progressState = MutableStateFlow(0F)
    val displayedProgressState = progressState
        .sample(10)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(1000), 0F)

    @OptIn(ExperimentalAtomicApi::class)
    fun refreshPackageList() {
        viewModelScope.launch(Dispatchers.IO) {
            packagesState.value = emptyList()
            val loadedCount = AtomicInt(0)
            val apps = application.packageManager.getInstalledApplications(getInstalledAppsFlags)
            apps.sortBy { it.flags and ApplicationInfo.FLAG_SYSTEM }
            apps.forEach { app ->
                launch(Dispatchers.IO) {
                    val entry = getAppStatus(application, ph, app.packageName)
                    packagesState.update { it + entry }
                    loadedCount.update { it + 1 }
                    progressState.value = loadedCount.load().toFloat() / apps.size
                }
            }
        }
    }

    fun onPackageRemoved(name: String) {
        packagesState.update { list ->
            list.filter { it.info.name != name }
        }
    }

    fun updateAppState(name: String) {
        if (name.isEmpty()) return
        packagesState.update { list ->
            list.map {
                if (it.info.name == name) {
                    getAppStatus(application, ph, name)
                } else it
            }
        }
    }
}
