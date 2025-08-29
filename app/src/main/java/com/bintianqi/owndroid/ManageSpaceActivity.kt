package com.bintianqi.owndroid

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.core.content.edit
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import kotlin.system.exitProcess

class ManageSpaceActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val vm by viewModels<MyViewModel>()
        setContent {
            val theme by vm.theme.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                var appLockDialog by remember { mutableStateOf(!SP.lockPasswordHash.isNullOrEmpty()) }
                if(appLockDialog) {
                    AppLockDialog({ appLockDialog = false }, ::finish)
                } else {
                    AlertDialog(
                        text = {
                            Text(stringResource(R.string.clear_storage))
                        },
                        onDismissRequest = ::finish,
                        dismissButton = {
                            TextButton(::finish) {
                                Text(stringResource(R.string.cancel))
                            }
                        },
                        confirmButton = {
                            TextButton(::clearStorage) {
                                Text(stringResource(R.string.confirm))
                            }
                        }
                    )
                }
            }
        }
    }

    fun clearStorage() {
        filesDir.deleteRecursively()
        cacheDir.deleteRecursively()
        codeCacheDir.deleteRecursively()
        if(Build.VERSION.SDK_INT >= 24) {
            dataDir.resolve("shared_prefs").deleteRecursively()
        } else {
            val sharedPref = applicationContext.getSharedPreferences("data", MODE_PRIVATE)
            sharedPref.edit(true) { clear() }
        }
        this.showOperationResultToast(true)
        finish()
        exitProcess(0)
    }
}