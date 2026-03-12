package com.bintianqi.owndroid.feature.privilege

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.getAppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DhizukuServerViewModel(
    val application: MyApplication, val repo: DhizukuServerRepository,
    val settingsRepo: SettingsRepository
) : ViewModel() {
    val serverEnabledState = MutableStateFlow(false)

    fun getEnabled() {
        serverEnabledState.value = settingsRepo.data.privilege.dhizukuServer
    }

    fun setEnabled(status: Boolean) {
        settingsRepo.update { it.privilege.dhizukuServer = status }
        serverEnabledState.value = status
    }

    val clientsState = MutableStateFlow(emptyList<Pair<DhizukuClientInfo, AppInfo>>())
    fun getClients() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = application.packageManager
            clientsState.value = repo.getDhizukuClients().mapNotNull {
                val packageName = pm.getNameForUid(it.uid)
                if (packageName == null) {
                    repo.deleteDhizukuClient(it)
                    null
                } else {
                    it to getAppInfo(pm, packageName)
                }
            }
        }
    }

    fun updateClient(info: DhizukuClientInfo) {
        repo.setDhizukuClient(info)
        clientsState.update { list ->
            val ml = list.toMutableList()
            val index = ml.indexOfFirst { it.first.uid == info.uid }
            ml[index] = info to ml[index].second
            ml
        }
    }
}
