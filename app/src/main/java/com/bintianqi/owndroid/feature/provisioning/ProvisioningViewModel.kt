package com.bintianqi.owndroid.feature.provisioning

import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel

@RequiresApi(29)
class ProvisioningViewModel : ViewModel() {
    lateinit var params: ProvisioningParams

    fun getParamsFromIntent(intent: Intent): ProvisioningParams {
        val modes = if (Build.VERSION.SDK_INT >= 31) intent.getIntegerArrayListExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_ALLOWED_PROVISIONING_MODES
        ) else null
        return ProvisioningParams(
            intent.getStringExtra(DevicePolicyManager.EXTRA_PROVISIONING_IMEI),
            intent.getStringExtra(DevicePolicyManager.EXTRA_PROVISIONING_SERIAL_NUMBER),
            modes ?: listOf(DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE)
        )
    }

    fun buildResultIntent(options: ProvisioningOptions): Intent {
        val intent = Intent()
        intent.putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION, options.skipEncryption
        )
        intent.putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_MODE,
            DevicePolicyManager.PROVISIONING_MODE_FULLY_MANAGED_DEVICE
        )
        return intent
    }
}
