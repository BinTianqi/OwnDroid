package com.bintianqi.owndroid.feature.network

import android.os.Build.VERSION
import android.telephony.data.ApnSetting
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.ToastChannel
import kotlinx.coroutines.flow.MutableStateFlow
import java.net.InetAddress

class OverrideApnViewModel(
    val ph: PrivilegeHelper, val tc: ToastChannel
) : ViewModel() {
    val enabledState = MutableStateFlow(false)
    @RequiresApi(28)
    fun getEnabled() = ph.safeDpmCall {
        dpm.isOverrideApnEnabled(dar)
    }

    @RequiresApi(28)
    fun setEnabled(enabled: Boolean) = ph.safeDpmCall {
        dpm.setOverrideApnsEnabled(dar, enabled)
        getEnabled()
    }

    val configsState = MutableStateFlow(listOf<ApnConfig>())

    @RequiresApi(28)
    fun getConfigs() = ph.safeDpmCall {
        configsState.value = dpm.getOverrideApns(dar).map {
            val proxy =
                if (VERSION.SDK_INT >= 29) it.proxyAddressAsString else it.proxyAddress.hostName
            val mmsProxy =
                if (VERSION.SDK_INT >= 29) it.mmsProxyAddressAsString
                else it.mmsProxyAddress.hostName
            ApnConfig(
                it.isEnabled, it.entryName, it.apnName, proxy, it.proxyPort,
                it.user, it.password, it.apnTypeBitmask, it.mmsc.toString(),
                mmsProxy, it.mmsProxyPort,
                it.authType,
                it.protocol,
                it.roamingProtocol,
                it.networkTypeBitmask,
                if (VERSION.SDK_INT >= 33) it.profileId else 0,
                if (VERSION.SDK_INT >= 29) it.carrierId else 0,
                if (VERSION.SDK_INT >= 33) it.mtuV4 else 0,
                if (VERSION.SDK_INT >= 33) it.mtuV6 else 0,
                ApnMvnoType.entries.find { type -> type.id == it.mvnoType }!!,
                it.operatorNumeric,
                if (VERSION.SDK_INT >= 33) it.isPersistent else true,
                if (VERSION.SDK_INT >= 35) it.isAlwaysOn else true,
                it.id
            )
        }
    }

    var selectedConfig: ApnConfig? = null

    @RequiresApi(28)
    fun buildApnSetting(config: ApnConfig): ApnSetting? {
        val builder = ApnSetting.Builder()
        builder.setCarrierEnabled(config.enabled)
        builder.setEntryName(config.name)
        builder.setApnName(config.apn)
        if (VERSION.SDK_INT >= 29) builder.setProxyAddress(config.proxy)
        else builder.setProxyAddress(InetAddress.getByName(config.proxy))
        config.port?.let { builder.setProxyPort(it) }
        builder.setUser(config.username)
        builder.setPassword(config.password)
        builder.setApnTypeBitmask(config.apnType)
        builder.setMmsc(config.mmsc.toUri())
        if (VERSION.SDK_INT >= 29) builder.setMmsProxyAddress(config.mmsProxy)
        else builder.setMmsProxyAddress(InetAddress.getByName(config.mmsProxy))
        builder.setAuthType(config.authType)
        builder.setProtocol(config.protocol)
        builder.setRoamingProtocol(config.roamingProtocol)
        builder.setNetworkTypeBitmask(config.networkType)
        if (VERSION.SDK_INT >= 33) config.profileId?.let { builder.setProfileId(it) }
        if (VERSION.SDK_INT >= 29) config.carrierId?.let { builder.setCarrierId(it) }
        if (VERSION.SDK_INT >= 33) {
            config.mtuV4?.let { builder.setMtuV4(it) }
            config.mtuV6?.let { builder.setMtuV6(it) }
        }
        builder.setMvnoType(config.mvno.id)
        builder.setOperatorNumeric(config.operatorNumeric)
        if (VERSION.SDK_INT >= 33) builder.setPersistent(config.persistent)
        if (VERSION.SDK_INT >= 35) builder.setAlwaysOn(config.alwaysOn)
        return builder.build()
    }

    @RequiresApi(28)
    fun setConfig(config: ApnConfig, succeedCallback: () -> Unit) = ph.safeDpmCall {
        val settings = buildApnSetting(config)
        val result = if (settings == null) {
            false
        } else {
            if (config.id == -1) {
                dpm.addOverrideApn(dar, settings) != -1
            } else {
                dpm.updateOverrideApn(dar, config.id, settings)
            }
        }
        if (result) succeedCallback() else tc.sendStatus(false)
    }

    @RequiresApi(28)
    fun removeConfig(id: Int, succeedCallback: () -> Unit) = ph.safeDpmCall {
        val result = dpm.removeOverrideApn(dar, id)
        if (result) {
            succeedCallback()
            getConfigs()
        }
    }
}
