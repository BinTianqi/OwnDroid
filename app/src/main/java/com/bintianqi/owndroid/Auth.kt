package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import kotlinx.coroutines.*

class AuthFragment: Fragment() {
    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("UnrememberedMutableState")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val sharedPref = context?.getSharedPreferences("data", Context.MODE_PRIVATE)!!
        val onAuthSucceed = {
            val fragmentManager = this.parentFragmentManager
            val fragment = fragmentManager.findFragmentByTag("auth")
            if(fragment != null) {
                val fragmentTransaction = fragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.base, homeFragment)
                fragmentTransaction.commit()
            }
        }
        val promptInfo = PromptInfo.Builder()
            .setTitle("Auth")
            .setSubtitle("Auth OwnDroid with password or biometric")
            .setConfirmationRequired(true)
        var fallback = false
        val callback = object: AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSucceed()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if(errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL) onAuthSucceed()
                if(errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) fallback = true
                if(errorCode == BiometricPrompt.ERROR_CANCELED) return
                Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
            }
        }
        GlobalScope.launch(Dispatchers.Main) {
            while(true){
                if(fallback){
                    val fallbackPromptInfo = PromptInfo.Builder()
                        .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .setTitle("Auth")
                        .setSubtitle("Auth OwnDroid with password")
                        .setConfirmationRequired(true)
                        .build()
                    authWithBiometricPrompt(requireActivity(), fallbackPromptInfo, callback)
                    break
                }
                delay(50)
            }
        }
        return ComposeView(requireContext()).apply {
            setContent {
                val materialYou = mutableStateOf(sharedPref.getBoolean("material_you",true))
                val blackTheme = mutableStateOf(sharedPref.getBoolean("black_theme", false))
                OwnDroidTheme(materialYou.value, blackTheme.value) {
                    Auth(this@AuthFragment.requireActivity(), callback, promptInfo)
                }
            }
        }
    }
}

@Composable
fun Auth(activity: FragmentActivity, callback: AuthenticationCallback, promptInfo: PromptInfo.Builder) {
    val context = LocalContext.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ){
        Text(text = "Authenticate", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
        Button(
            onClick = {
                val bioManager = BiometricManager.from(context)
                val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
                if(sharedPref.getBoolean("bio_auth", false)){
                    when(BiometricManager.BIOMETRIC_SUCCESS){
                        bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ->
                            promptInfo
                                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                                .setNegativeButtonText("Use password")
                        bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ->
                            promptInfo
                                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                                .setNegativeButtonText("Use password")
                        else -> promptInfo.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    }
                }else{
                    promptInfo.setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                }
                authWithBiometricPrompt(activity, promptInfo.build(), callback)
            }
        ){
            Text(text = "Start")
        }
    }
}

private fun authWithBiometricPrompt(activity: FragmentActivity, promptInfo: PromptInfo, callback: AuthenticationCallback) {
    val executor = ContextCompat.getMainExecutor(activity.applicationContext)
    val biometricPrompt = BiometricPrompt(activity, executor, callback)
    biometricPrompt.authenticate(promptInfo)
}
