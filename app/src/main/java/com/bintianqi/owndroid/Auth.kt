package com.bintianqi.owndroid

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.biometric.BiometricPrompt.PromptInfo.Builder
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import kotlinx.coroutines.*

class AuthFragment: Fragment() {
    @SuppressLint("UnrememberedMutableState")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val context = requireContext()
        val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)!!
        val canStartAuth = mutableStateOf(true)
        val onAuthSucceed = {
            val fragmentManager = this.parentFragmentManager
            val transaction = fragmentManager.beginTransaction()
            transaction.setCustomAnimations(R.anim.enter, R.anim.exit)
            transaction.add(R.id.base, homeFragment)
            requireActivity().findViewById<FrameLayout>(R.id.base).bringChildToFront(homeFragment.view)
            transaction.commit()
            lifecycleScope.launch {
                delay(500)
                fragmentManager.beginTransaction().remove(this@AuthFragment).commit()
            }
        }
        val promptInfo = Builder()
            .setTitle(context.getText(R.string.authenticate))
            .setSubtitle(context.getText(R.string.auth_with_bio))
            .setConfirmationRequired(true)
        var fallback = false
        val callback = object: AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSucceed()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when(errorCode){
                    BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> onAuthSucceed()
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> fallback = true
                    else -> canStartAuth.value = true
                }
                Log.e("OwnDroid", errString.toString())
            }
        }
        lifecycleScope.launch(Dispatchers.Main) {
            while(true){
                if(fallback){
                    val fallbackPromptInfo = Builder()
                        .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                        .setTitle(context.getText(R.string.authenticate))
                        .setSubtitle(context.getText(R.string.auth_with_password))
                        .setConfirmationRequired(true)
                        .build()
                    val executor = ContextCompat.getMainExecutor(requireContext())
                    val biometricPrompt = BiometricPrompt(requireActivity(), executor, callback)
                    biometricPrompt.authenticate(fallbackPromptInfo)
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
                    Auth(this@AuthFragment, promptInfo, callback, canStartAuth)
                }
            }
        }
    }
}

@Composable
fun Auth(activity: Fragment, promptInfo: Builder, callback: AuthenticationCallback, canStartAuth: MutableState<Boolean>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
    ){
        Text(
            text = stringResource(R.string.authenticate),
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        LaunchedEffect(Unit){
            startAuth(activity, promptInfo, callback)
            canStartAuth.value = false
        }
        Button(
            onClick = {
                startAuth(activity, promptInfo, callback)
                canStartAuth.value = false
            },
            enabled = canStartAuth.value
        ){
            Text(text = stringResource(R.string.start))
        }
    }
}

private fun startAuth(activity: Fragment, promptInfo: Builder, callback: AuthenticationCallback){
    val context = activity.requireContext()
    val bioManager = BiometricManager.from(context)
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    if(sharedPref.getBoolean("bio_auth", false)){
        when(BiometricManager.BIOMETRIC_SUCCESS){
            bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) ->
                promptInfo
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                    .setNegativeButtonText(context.getText(R.string.use_password))
            bioManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) ->
                promptInfo
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_WEAK)
                    .setNegativeButtonText(context.getText(R.string.use_password))
            else -> promptInfo
                .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .setSubtitle(context.getText(R.string.auth_with_password))
        }
    }else{
        promptInfo
            .setAllowedAuthenticators(BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .setSubtitle(context.getText(R.string.auth_with_password))
    }
    val executor = ContextCompat.getMainExecutor(context)
    val biometricPrompt = BiometricPrompt(activity, executor, callback)
    biometricPrompt.authenticate(promptInfo.build())
}
