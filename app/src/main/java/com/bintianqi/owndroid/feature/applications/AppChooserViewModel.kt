package com.bintianqi.owndroid.feature.applications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.getAppInfo
import com.bintianqi.owndroid.utils.getInstalledAppsFlags
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AppChooserViewModel(val application: MyApplication) : ViewModel() {
    val packagesState = MutableStateFlow(emptyList<AppInfo>())
    val progressState = MutableStateFlow(0F)

    fun refreshPackageList() {
        viewModelScope.launch(Dispatchers.IO) {
            packagesState.value = emptyList()
            val apps = application.packageManager.getInstalledApplications(getInstalledAppsFlags)
            apps.forEachIndexed { index, info ->
                packagesState.update {
                    it + getAppInfo(application.packageManager, info)
                }
                progressState.value = (index + 1).toFloat() / apps.size
            }
        }
    }

    fun onPackageRemoved(name: String) {
        packagesState.update { list ->
            list.filter { it.name != name }
        }
    }
}
