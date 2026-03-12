package com.bintianqi.owndroid.feature.privilege

import android.app.admin.DeviceAdminInfo
import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.getAppInfo
import com.bintianqi.owndroid.utils.getPrivilegeStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class TransferOwnershipViewModel(
    val application: MyApplication, val ph: PrivilegeHelper,
    val ps: MutableStateFlow<PrivilegeStatus>
) : ViewModel() {
    val deviceAdminReceivers = MutableStateFlow(emptyList<DeviceAdmin>())

    fun getDeviceAdminReceivers() {
        viewModelScope.launch(Dispatchers.IO) {
            val pm = application.packageManager
            deviceAdminReceivers.value = pm.queryBroadcastReceivers(
                Intent(DeviceAdminReceiver.ACTION_DEVICE_ADMIN_ENABLED),
                PackageManager.GET_META_DATA
            ).mapNotNull {
                try {
                    DeviceAdminInfo(application, it)
                } catch (_: Exception) {
                    null
                }
            }.filter {
                it.isVisible && it.packageName != "com.bintianqi.owndroid" &&
                        it.activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0
            }.map {
                DeviceAdmin(getAppInfo(pm, it.packageName), it.component)
            }
        }
    }

    @RequiresApi(28)
    fun transferOwnership(component: ComponentName) = ph.safeDpmCall {
        dpm.transferOwnership(dar, component, null)
        ps.value = getPrivilegeStatus(ph)
    }
}
