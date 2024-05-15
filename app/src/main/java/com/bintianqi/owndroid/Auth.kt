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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme

class AuthFragment: Fragment() {
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
        val callback = object: AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSucceed()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if(errorCode == BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL) onAuthSucceed()
                if(errorCode == BiometricPrompt.ERROR_CANCELED) return
                Toast.makeText(context, errString, Toast.LENGTH_SHORT).show()
            }
        }
        return ComposeView(requireContext()).apply {
            setContent {
                val materialYou = mutableStateOf(sharedPref.getBoolean("material_you",true))
                val blackTheme = mutableStateOf(sharedPref.getBoolean("black_theme", false))
                OwnDroidTheme(materialYou.value, blackTheme.value) {
                    Auth(this@AuthFragment.requireActivity(), callback)
                }
            }
        }
    }
}

@Composable
fun Auth(activity: FragmentActivity, callback: AuthenticationCallback) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ){
        Text(text = "Authenticate", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onBackground)
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
