package com.bintianqi.owndroid.feature.network

import android.app.admin.DevicePolicyManager
import android.app.admin.IDevicePolicyManager
import android.net.ProxyInfo
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow

class NetworkViewModel(
    val ph: PrivilegeHelper, val toastChannel: ToastChannel,
    val ps: MutableStateFlow<PrivilegeStatus>
) : ViewModel() {
    // Lockdown admin configured networks
    val lanEnabledState = MutableStateFlow(false)

    @RequiresApi(30)
    fun getLanEnabled() = ph.safeDpmCall {
        lanEnabledState.value = dpm.hasLockdownAdminConfiguredNetworks(dar)
    }

    @RequiresApi(30)
    fun setLanEnabled(state: Boolean) = ph.safeDpmCall {
        dpm.setConfiguredNetworksLockdownState(dar, state)
        getLanEnabled()
    }

    val privateDnsModeState = MutableStateFlow<PrivateDnsMode?>(null)
    val privateDnsHostState = MutableStateFlow("")

    @RequiresApi(29)
    fun getPrivateDnsConf() = ph.safeDpmCall {
        val mode = dpm.getGlobalPrivateDnsMode(dar)
        privateDnsModeState.value = PrivateDnsMode.entries.find { it.id == mode }
        privateDnsHostState.value = dpm.getGlobalPrivateDnsHost(dar) ?: ""
    }

    fun setPrivateDnsMode(mode: PrivateDnsMode) {
        privateDnsModeState.value = mode
    }

    fun setPrivateDnsHost(host: String) {
        privateDnsHostState.value = host
    }

    @Suppress("PrivateApi")
    @RequiresApi(29)
    fun applyPrivateDnsConf() = ph.safeDpmCall {
        val result = try {
            val field = DevicePolicyManager::class.java.getDeclaredField("mService")
            field.isAccessible = true
            val dpm = field.get(dpm) as IDevicePolicyManager
            val host =
                if (privateDnsModeState.value == PrivateDnsMode.Host) privateDnsHostState.value
                else null
            val ret = dpm.setGlobalPrivateDns(dar, privateDnsModeState.value!!.id, host)
            ret == DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
        toastChannel.sendStatus(result)
    }

    val alwaysOnVpnPackageState = MutableStateFlow("")
    val alwaysOnVpnLockdownState = MutableStateFlow(false)

    @RequiresApi(24)
    fun getAlwaysOnVpnPackage() = ph.safeDpmCall {
        alwaysOnVpnPackageState.value = dpm.getAlwaysOnVpnPackage(dar) ?: ""
    }

    @RequiresApi(29)
    fun getAlwaysOnVpnLockdown() = ph.safeDpmCall {
        alwaysOnVpnLockdownState.value = dpm.isAlwaysOnVpnLockdownEnabled(dar)
    }

    fun setAlwaysOnVpnPackage(name: String) {
        alwaysOnVpnPackageState.value = name
    }

    fun setAlwaysOnVpnLockdown(state: Boolean) {
        alwaysOnVpnLockdownState.value = state
    }

    @RequiresApi(24)
    fun applyAlwaysOnVpn() = ph.safeDpmCall {
        val result = try {
            dpm.setAlwaysOnVpnPackage(
                dar, alwaysOnVpnPackageState.value, alwaysOnVpnLockdownState.value
            )
            true
        } catch (_: Exception) {
            false
        }
        toastChannel.sendStatus(result)
    }

    @RequiresApi(24)
    fun clearAlwaysOnVpnConfig() = ph.safeDpmCall {
        dpm.setAlwaysOnVpnPackage(dar, null, false)
        alwaysOnVpnPackageState.value = ""
        alwaysOnVpnLockdownState.value = false
    }

    fun setRecommendedGlobalProxy(conf: RecommendedProxyConf) = ph.safeDpmCall {
        val info = when (conf.type) {
            ProxyType.Off -> null
            ProxyType.Pac -> {
                if (VERSION.SDK_INT >= 30 && conf.specifyPort) {
                    ProxyInfo.buildPacProxy(conf.url.toUri(), conf.port)
                } else {
                    ProxyInfo.buildPacProxy(conf.url.toUri())
                }
            }
            ProxyType.Direct -> {
                ProxyInfo.buildDirectProxy(conf.host, conf.port, conf.exclude)
            }
        }
        dpm.setRecommendedGlobalProxy(dar, info)
        toastChannel.sendStatus(true)
    }
}