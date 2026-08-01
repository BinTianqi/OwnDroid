package com.bintianqi.owndroid.feature.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.getInstalledAppsFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppChooserViewModel(val application: MyApplication, val ph: PrivilegeHelper) : ViewModel() {
    val packagesState = MutableStateFlow(emptyList<AppChooserEntry>())
    val progressState = MutableStateFlow(0F)

    fun refreshPackageList() {
        viewModelScope.launch(Dispatchers.IO) {
            packagesState.value = emptyList()
            val apps = application.packageManager.getInstalledApplications(getInstalledAppsFlags)
            apps.forEachIndexed { index, info ->
                packagesState.update {
                    it + getAppStatus(application, ph, info.packageName)
                }
                progressState.value = (index + 1).toFloat() / apps.size
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
