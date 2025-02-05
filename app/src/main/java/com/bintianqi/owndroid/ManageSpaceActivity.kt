package com.bintianqi.owndroid

import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
        val authenticate = sharedPref.getBoolean("auth", false)
        val vm by viewModels<MyViewModel>()
        fun clearStorage() {
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
        setContent {
            var authenticating by remember { mutableStateOf(false) }
            val callback = object: AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    clearStorage()
                }
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    when(errorCode) {
                        BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> clearStorage()
                        else -> authenticating = false
                    }
                }
            }
            OwnDroidTheme(vm) {
                AlertDialog(
                    text = {
                        Text(stringResource(R.string.clear_storage))
                    },
                    onDismissRequest = { finish() },
                    dismissButton = {
                        TextButton(onClick = { finish() }) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if(authenticate) {
                                    authenticating = true
                                    startAuth(this, callback)
                                } else {
                                    clearStorage()
                                }
                            },
                            enabled = !authenticating
                        ) {
                            Text(stringResource(R.string.confirm))
                        }
                    }
                )
            }
        }
    }
}