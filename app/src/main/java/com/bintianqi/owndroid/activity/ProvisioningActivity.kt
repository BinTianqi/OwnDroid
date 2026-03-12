package com.bintianqi.owndroid.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.feature.provisioning.ProvisioningScreen
import com.bintianqi.owndroid.feature.provisioning.ProvisioningViewModel
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme

@RequiresApi(29)
class ProvisioningActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val myApp = application as MyApplication
        val vm by viewModels<ProvisioningViewModel>()
        vm.params = vm.getParamsFromIntent(intent)
        setContent {
            val theme by myApp.container.themeState.collectAsState()
            OwnDroidTheme(theme) {
                ProvisioningScreen(vm.params) {
                    setResult(RESULT_OK, vm.buildResultIntent(it))
                    finish()
                }
            }
        }
    }
}
