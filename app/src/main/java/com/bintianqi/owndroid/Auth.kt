package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme

var authenticated = false

class AuthActivity: FragmentActivity(){
    @SuppressLint("UnrememberedMutableState")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        val mainActivityIntent = Intent(applicationContext, MainActivity::class.java)
        val sharedPref = applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)
        val callback = object: AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                authenticated = true
                startActivity(mainActivityIntent)
                finish()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(applicationContext, errString, Toast.LENGTH_SHORT).show()
                /*if (errString.toString().isNotEmpty()) {
                }*/
            }
        }
        setContent {
            val materialYou = mutableStateOf(sharedPref.getBoolean("material_you",true))
            val blackTheme = mutableStateOf(sharedPref.getBoolean("black_theme", false))
            OwnDroidTheme(materialYou.value, blackTheme.value) {
                Auth(this, callback)
            }
        }
    }
}

@Composable
fun Auth(activity: FragmentActivity, callback: AuthenticationCallback) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ){
        Text(text = "Authenticate", style = MaterialTheme.typography.headlineLarge)
        Button(
            onClick = {
                authWithBiometricPrompt(activity, callback)
            }
        ){
            Text(text = "Start")
        }
    }
}

private fun authWithBiometricPrompt(activity: FragmentActivity, callback: AuthenticationCallback) {
    val executor = ContextCompat.getMainExecutor(activity.applicationContext)
    val biometricPrompt = BiometricPrompt(activity, executor, callback)
    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
        .setTitle("Auth")
        .setConfirmationRequired(true)
        .setSubtitle("Enter password")
        .build()
    biometricPrompt.authenticate(promptInfo)
}
