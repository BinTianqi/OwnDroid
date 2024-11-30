package com.bintianqi.owndroid

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import kotlin.system.exitProcess

class ManageSpaceActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val sharedPref = applicationContext.getSharedPreferences("data", MODE_PRIVATE)
        val protected = sharedPref.getBoolean("protect_storage", false)
        val vm by viewModels<MyViewModel>()
        if(!vm.initialized) vm.initialize(applicationContext)
        setContent {
            OwnDroidTheme(vm) {
                AlertDialog(
                    title = {
                        Text(stringResource(R.string.clear_storage))
                    },
                    text = {
                        if(protected) Text(stringResource(R.string.storage_is_protected))
                    },
                    onDismissRequest = { finish() },
                    dismissButton = {
                        TextButton(onClick = { finish() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    confirmButton = {
                        if(!protected) TextButton(
                            onClick = {
                                filesDir.deleteRecursively()
                                cacheDir.deleteRecursively()
                                codeCacheDir.deleteRecursively()
                                if(Build.VERSION.SDK_INT >= 24) {
                                    dataDir.resolve("shared_prefs").deleteRecursively()
                                } else {
                                    sharedPref.edit().clear().apply()
                                }
                                finish()
                                exitProcess(0)
                            }
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                )
            }
        }
    }
}