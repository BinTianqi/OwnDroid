package com.bintianqi.owndroid.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.popToast

class ProvisioningPolicyComplianceActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(RESULT_OK)
        popToast(R.string.app_name)
        finish()
    }
}
