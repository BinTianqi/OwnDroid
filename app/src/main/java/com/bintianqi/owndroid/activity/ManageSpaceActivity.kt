package com.bintianqi.owndroid.activity

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import com.bintianqi.owndroid.MyApplication
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.screen.AppLockDialog
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.bintianqi.owndroid.utils.showOperationResultToast
import kotlin.system.exitProcess

class ManageSpaceActivity: FragmentActivity() {
    val myApp = application as MyApplication
    val settingsRepo = myApp.container.settingsRepo
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val theme by myApp.container.themeState.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                var appLockDialog by remember {
                    mutableStateOf(settingsRepo.data.appLock.passwordHash.isNotEmpty())
                }
                if (appLockDialog) {
                    AppLockDialog(settingsRepo.data.appLock, { appLockDialog = false }, ::finish)
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
            dataDir.resolve("databases").deleteRecursively()
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
