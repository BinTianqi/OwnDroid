package com.bintianqi.owndroid.feature.network

import android.app.admin.WifiSsidPolicy
import android.content.Context
import android.net.IpConfiguration
import android.net.LinkAddress
import android.net.ProxyInfo
import android.net.StaticIpConfiguration
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiSsid
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.net.InetAddress
import kotlin.reflect.jvm.jvmErasure

class WifiViewModel(
    val application: MyApplication, val privilegeHelper: PrivilegeHelper,
    val toastChannel: ToastChannel, val privilegeState: StateFlow<PrivilegeStatus>
) : ViewModel() {
    val wm = application.getSystemService(Context.WIFI_SERVICE) as WifiManager
    
    fun setWifiEnabled(enabled: Boolean) {
        toastChannel.sendStatus(wm.setWifiEnabled(enabled))
    }

    fun disconnect() {
        toastChannel.sendStatus(wm.disconnect())
    }

    fun reconnect() {
        toastChannel.sendStatus(wm.reconnect())
    }

    val macState = MutableStateFlow<String?>(null)

    @RequiresApi(24)
    fun getMac() {
        macState.value = privilegeHelper.dpm.getWifiMacAddress(privilegeHelper.dar)
    }

    val configuredNetworksState = MutableStateFlow(emptyList<WifiInfo>())

    @Suppress("MissingPermission")
    fun getConfiguredNetworks() {
        configuredNetworksState.value = wm.configuredNetworks.distinctBy { it.networkId }.map { conf ->
            WifiInfo(
                conf.networkId, conf.SSID.removeSurrounding("\""), null, conf.BSSID ?: "", null,
                WifiStatus.entries.find { it.id == conf.status }!!, null, "", null, null, null, null
            )
        }
    }

    fun enableNetwork(id: Int) {
        toastChannel.sendStatus(wm.enableNetwork(id, false))
        getConfiguredNetworks()
    }

    fun disableNetwork(id: Int) {
        toastChannel.sendStatus(wm.disableNetwork(id))
        getConfiguredNetworks()
    }

    fun removeNetwork(id: Int) {
        toastChannel.sendStatus(wm.removeNetwork(id))
        getConfiguredNetworks()
    }

    var selectedWifiInfo: WifiInfo? = null

    fun setWifi(info: WifiInfo): Boolean {
        val conf = WifiConfiguration()
        conf.SSID = "\"" + info.ssid + "\""
        info.hiddenSsid?.let { conf.hiddenSSID = it }
        if (VERSION.SDK_INT >= 30) info.security?.let { conf.setSecurityParams(it.id) }
        if (info.security == WifiSecurity.Psk) conf.preSharedKey = info.password
        if (VERSION.SDK_INT >= 33) info.macRandomization?.let {
            conf.macRandomizationSetting = it.id
        }
        if (VERSION.SDK_INT >= 33 && info.ipMode != null) {
            val ipConf = if (info.ipMode == IpMode.Static && info.ipConf != null) {
                val constructor = LinkAddress::class.constructors.find {
                    it.parameters.size == 1 && it.parameters[0].type.jvmErasure == String::class
                }
                val address = constructor!!.call(info.ipConf.address)
                val staticIpConf = StaticIpConfiguration.Builder()
                    .setIpAddress(address)
                    .setGateway(InetAddress.getByName(info.ipConf.gateway))
                    .setDnsServers(info.ipConf.dns.map { InetAddress.getByName(it) })
                    .build()
                IpConfiguration.Builder().setStaticIpConfiguration(staticIpConf).build()
            } else null
            conf.setIpConfiguration(ipConf)
        }
        if (VERSION.SDK_INT >= 26 && info.proxyMode != null) {
            val proxy = if (info.proxyMode == ProxyMode.Http) {
                info.proxyConf?.let {
                    ProxyInfo.buildDirectProxy(it.host, it.port, it.exclude)
                }
            } else null
            conf.httpProxy = proxy
        }
        val result = if (info.id != -1) {
            conf.networkId = info.id
            wm.updateNetwork(conf)
        } else {
            wm.addNetwork(conf)
        }
        if (result != -1) {
            when (info.status) {
                WifiStatus.Current -> wm.enableNetwork(result, true)
                WifiStatus.Enabled -> wm.enableNetwork(result, false)
                WifiStatus.Disabled -> wm.disableNetwork(result)
            }
            getConfiguredNetworks()
        }
        return result != -1
    }

    val minWifiSecurityLevelState = MutableStateFlow(0)

    @RequiresApi(33)
    fun getMinimumWifiSecurityLevel() {
        minWifiSecurityLevelState.value = privilegeHelper.dpm.minimumRequiredWifiSecurityLevel
    }

    @RequiresApi(33)
    fun setMinimumWifiSecurityLevel(level: Int) {
        privilegeHelper.dpm.minimumRequiredWifiSecurityLevel = level
        getMinimumWifiSecurityLevel()
    }

    @RequiresApi(33)
    fun getSsidPolicy(): SsidPolicy {
        val policy = privilegeHelper.dpm.wifiSsidPolicy
        return SsidPolicy(
            SsidPolicyType.entries.find { it.id == policy?.policyType } ?: SsidPolicyType.None,
            policy?.ssids?.map { it.bytes.decodeToString() } ?: emptyList()
        )
    }

    @RequiresApi(33)
    fun setSsidPolicy(policy: SsidPolicy) {
        val newPolicy = if (policy.type != SsidPolicyType.None) {
            WifiSsidPolicy(
                policy.type.id,
                policy.list.map { WifiSsid.fromBytes(it.encodeToByteArray()) }.toSet()
            )
        } else null
        privilegeHelper.dpm.wifiSsidPolicy = newPolicy
        toastChannel.sendStatus(true)
    }
}
