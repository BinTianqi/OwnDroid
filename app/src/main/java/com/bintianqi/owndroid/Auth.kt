package com.bintianqi.owndroid

import android.content.Context
import androidx.activity.compose.BackHandler
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo.Builder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun Authenticate(activity: FragmentActivity, navCtrl: NavHostController) {
    BackHandler { activity.moveTaskToBack(true) }
    var status by rememberSaveable { mutableIntStateOf(0) } // 0:Prompt automatically, 1:Authenticating, 2:Prompt manually
    val onAuthSucceed = { navCtrl.navigateUp() }
    val callback = object: AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            onAuthSucceed()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            when(errorCode) {
                BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> onAuthSucceed()
                else -> status = 2
            }
        }
    }
    LaunchedEffect(Unit) {
        if(status == 0) {
            delay(300)
            startAuth(activity, callback)
            status = 1
        }
    }
    Scaffold { paddingValues ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Text(
                text = stringResource(R.string.authenticate),
                style = MaterialTheme.typography.headlineLarge,
            )
            Button(
                onClick = {
                    startAuth(activity, callback)
                    status = 1
                },
                enabled = status != 1
            ) {
                Text(text = stringResource(R.string.start))
            }
        }
    }
}

fun startAuth(activity: FragmentActivity, callback: AuthenticationCallback) {
    val context = activity.applicationContext
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    val promptInfo = Builder().setTitle(context.getText(R.string.authenticate))
    if(sharedPref.getInt("biometrics_auth", 0) != 0) {
        promptInfo.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL or BiometricManager.Authenticators.BIOMETRIC_WEAK)
    } else {
        promptInfo.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
    }
    val executor = ContextCompat.getMainExecutor(context)
    BiometricPrompt(activity, executor, callback).authenticate(promptInfo.build())
}
