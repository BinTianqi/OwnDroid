package com.bintianqi.owndroid

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.ui.AppInstaller
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme

class AppInstallerActivity:FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val myVm by viewModels<MyViewModel>()
        val vm by viewModels<AppInstallerViewModel>()
        vm.initialize(intent)
        setContent {
            val theme by myVm.theme.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                val uiState by vm.uiState.collectAsState()
                AppInstaller(
                    uiState, vm::onPackagesAdd, vm::onPackageRemove, vm::startInstall, vm::closeResultDialog
                )
            }
        }
    }
}
