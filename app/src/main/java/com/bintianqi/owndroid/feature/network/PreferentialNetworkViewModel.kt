package com.bintianqi.owndroid.feature.network

import android.app.admin.PreferentialNetworkServiceConfig
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.PrivilegeHelper
import kotlinx.coroutines.flow.MutableStateFlow

class PreferentialNetworkViewModel(
    val ph: PrivilegeHelper
) : ViewModel() {
    val enabledState = MutableStateFlow(false)

    @RequiresApi(31)
    fun getEnabled() = ph.safeDpmCall {
        enabledState.value = dpm.isPreferentialNetworkServiceEnabled
    }

    @RequiresApi(31)
    fun setEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.isPreferentialNetworkServiceEnabled = enabled
        getEnabled()
    }
    
    val configsState = MutableStateFlow(emptyList<PreferentialNetworkServiceInfo>())

    @RequiresApi(33)
    fun getConfigs() = ph.safeDpmCall {
        configsState.value = dpm.preferentialNetworkServiceConfigs.map {
            PreferentialNetworkServiceInfo(
                it.isEnabled, it.networkId, it.isFallbackToDefaultConnectionAllowed,
                if (VERSION.SDK_INT >= 34) it.shouldBlockNonMatchingNetworks() else false,
                it.excludedUids.toList(), it.includedUids.toList()
            )
        }
    }

    var selectedConfigIndex = -1

    @RequiresApi(33)
    private fun buildConfig(
        info: PreferentialNetworkServiceInfo
    ): PreferentialNetworkServiceConfig {
        return PreferentialNetworkServiceConfig.Builder().apply {
            setEnabled(info.enabled)
            @Suppress("WrongConstant")
            setNetworkId(info.id)
            setFallbackToDefaultConnectionAllowed(info.allowFallback)
            if (VERSION.SDK_INT >= 34) setShouldBlockNonMatchingNetworks(info.blockNonMatching)
            setIncludedUids(info.includedUids.toIntArray())
            setExcludedUids(info.excludedUids.toIntArray())
        }.build()
    }

    @RequiresApi(33)
    fun setConfig(info: PreferentialNetworkServiceInfo, state: Boolean) = ph.safeDpmCall {
        val originList = configsState.value.toMutableList()
        if (selectedConfigIndex == -1) {
            originList += info
        } else {
            if (state) originList[selectedConfigIndex] = info
            else originList.removeAt(selectedConfigIndex)
        }
        dpm.preferentialNetworkServiceConfigs = originList.map { buildConfig(it) }
    }
}
