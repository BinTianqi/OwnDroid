package com.bintianqi.owndroid.feature.privilege

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bintianqi.owndroid.ActivationService
import com.bintianqi.owndroid.IActivationService
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.PrivilegeHelper
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.settings.SettingsRepository
import com.bintianqi.owndroid.utils.DhizukuError
import com.bintianqi.owndroid.utils.MyAdminComponent
import com.bintianqi.owndroid.utils.MyRootService
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.ToastChannel
import com.bintianqi.owndroid.utils.activateOrgProfileCommand
import com.bintianqi.owndroid.utils.getPrivilegeStatus
import com.bintianqi.owndroid.utils.handlePrivilegeChange
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

class WorkingModesViewModel(
    val application: MyApplication, val ph: PrivilegeHelper, val sr: SettingsRepository,
    val ps: MutableStateFlow<PrivilegeStatus>, val toastChannel: ToastChannel,
    val de: MutableStateFlow<DhizukuError?>
) : ViewModel() {

    fun getPrivilegeState() = ph.safeDpmCall {
        ps.value = getPrivilegeStatus(dpm, dar, ph.dhizuku)
    }

    @RequiresApi(24)
    fun isCreatingWorkProfileAllowed(): Boolean {
        return ph.myDpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
    }

    fun activateDoByShizuku(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            useShizuku({ it.activateDeviceOwner() }, callback)
        }
    }

    fun activateDoByRoot(callback: (Boolean, String?) -> Unit) {
        val intent = Intent(application, MyRootService::class.java)
        val conn = ActivationServiceConnection({ it.activateDeviceOwner() }, callback) {
            RootService.stop(intent)
        }
        RootService.bind(intent, conn)
    }

    inner class ActivationServiceConnection(
        val callback: (IActivationService) -> Bundle?,
        val resultCallback: (Boolean, String?) -> Unit,
        val stopCallback: (ActivationServiceConnection) -> Unit
    ) : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val service = IActivationService.Stub.asInterface(service)
            val result = callback(service)
            if (result == null) {
                resultCallback(false, application.getString(R.string.permission_denied))
            } else {
                val stdout = result.getString("output", "")
                val text = (if (stdout.isEmpty()) "" else stdout + "\n") +
                        result.getString("error", "")
                val succeed = result.getInt("code", -1) == 0
                if (succeed) updateStatus()
                resultCallback(succeed, text)
            }
            stopCallback(this)
        }

        override fun onServiceDisconnected(name: ComponentName?) {}
    }

    @RequiresApi(28)
    fun activateDoByDhizuku(callback: (Boolean, String?) -> Unit) = ph.safeDpmCall {
        dpm.transferOwnership(dar, MyAdminComponent, null)
        sr.update { it.privilege.dhizuku = false }
        ph.dhizuku = false
        updateStatus()
        callback(true, null)
    }

    fun activateDhizukuMode(callback: (Boolean, String?) -> Unit) {
        fun onSucceed() {
            sr.update { it.privilege.dhizuku = true }
            ph.dhizuku = true
            updateStatus()
            callback(true, null)
        }
        if (Dhizuku.init(application)) {
            if (Dhizuku.isPermissionGranted()) {
                onSucceed()
            } else {
                Dhizuku.requestPermission(object : DhizukuRequestPermissionListener() {
                    override fun onRequestPermission(grantResult: Int) {
                        if (grantResult == PackageManager.PERMISSION_GRANTED) onSucceed()
                        else callback(
                            false, application.getString(R.string.dhizuku_permission_not_granted)
                        )
                    }
                })
            }
        } else {
            callback(false, application.getString(R.string.failed_to_init_dhizuku))
        }
    }

    fun activateOrgProfileByShizuku(callback: (Boolean, String?) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            useShizuku({ it.execute(activateOrgProfileCommand) }, callback)
        }
    }

    val frpNoticeState = MutableStateFlow(false)

    fun getFrpState() {
        if (Build.VERSION.SDK_INT >= 30 && ps.value.device && !ps.value.dhizuku) {
            ph.safeDpmCall {
                try {
                    frpNoticeState.value = dpm.getFactoryResetProtectionPolicy(dar) != null
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    fun deactivate() {
        if (ps.value.dhizuku) {
            sr.update { it.privilege.dhizuku = false }
            ph.dhizuku = false
        } else {
            if (ps.value.device) {
                ph.myDpm.clearDeviceOwnerApp(application.packageName)
            } else if (Build.VERSION.SDK_INT >= 24) {
                ph.myDpm.clearProfileOwner(MyAdminComponent)
            }
        }
        updateStatus()
    }

    private fun updateStatus() = ph.safeDpmCall {
        ps.value = getPrivilegeStatus(dpm, dar, ph.dhizuku)
        handlePrivilegeChange(application, ps.value, ph, sr)
    }

    private fun useShizuku(
        callback: (IActivationService) -> Bundle?, resultCallback: (Boolean, String?) -> Unit
    ) {
        val cn = ComponentName(application, ActivationService::class.java)
        val shizukuArgs = Shizuku.UserServiceArgs(cn)
            .daemon(false)
            .processNameSuffix("shizuku-service")
            .debuggable(false)
            .version(1)
        val connection = ActivationServiceConnection(callback, resultCallback) {
            Shizuku.unbindUserService(shizukuArgs, it, true)
        }
        try {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                Shizuku.bindUserService(shizukuArgs, connection)
            } else if (Shizuku.shouldShowRequestPermissionRationale()) {
                resultCallback(false, application.getString(R.string.permission_denied))
            } else {
                fun requestPermissionResultListener(requestCode: Int, grantResult: Int) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        Shizuku.bindUserService(shizukuArgs, connection)
                    } else {
                        resultCallback(false, application.getString(R.string.permission_denied))
                    }
                    Shizuku.removeRequestPermissionResultListener(::requestPermissionResultListener)
                }
                Shizuku.addRequestPermissionResultListener(::requestPermissionResultListener)
                Shizuku.requestPermission(0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            resultCallback(false, application.getString(R.string.failed))
        }
    }
}
