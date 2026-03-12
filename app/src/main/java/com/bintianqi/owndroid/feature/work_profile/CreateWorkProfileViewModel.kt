package com.bintianqi.owndroid.feature.work_profile

import android.accounts.Account
import android.app.admin.DevicePolicyManager
import android.content.Intent
import android.os.Build.VERSION
import androidx.lifecycle.ViewModel
import com.bintianqi.owndroid.utils.MyAdminComponent

class CreateWorkProfileViewModel : ViewModel() {
    fun createIntent(options: CreateWorkProfileOptions): Intent {
        val intent = Intent(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)
        intent.putExtra(
            DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,
            MyAdminComponent
        )
        if (options.migrateAccount) {
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_ACCOUNT_TO_MIGRATE,
                Account(options.accountName, options.accountType)
            )
            if (VERSION.SDK_INT >= 26) {
                intent.putExtra(
                    DevicePolicyManager.EXTRA_PROVISIONING_KEEP_ACCOUNT_ON_MIGRATION,
                    options.keepAccount
                )
            }
        }
        if (VERSION.SDK_INT >= 24) {
            intent.putExtra(
                DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION,
                options.skipEncrypt
            )
        }
        if (VERSION.SDK_INT >= 33) {
            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE, options.offline)
        }
        return intent
    }
}
