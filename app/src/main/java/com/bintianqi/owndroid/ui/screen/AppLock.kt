package com.bintianqi.owndroid.ui.screen

import android.content.Context
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.hardware.biometrics.BiometricPrompt.AuthenticationCallback
import android.os.Build
import android.os.CancellationSignal
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.settings.MySettings
import com.bintianqi.owndroid.utils.TotpUtils
import com.bintianqi.owndroid.utils.hash
import com.bintianqi.owndroid.utils.showOperationResultToast

@Composable
fun AppLockDialog(
    config: MySettings.AppLock, onSucceed: () -> Unit, onDismiss: () -> Unit
) = Dialog(onDismiss, DialogProperties(true, false)) {
    val context = LocalContext.current
    val fm = LocalFocusManager.current
    val fr = remember { FocusRequester() }
    var passwordInput by rememberSaveable { mutableStateOf("") }
    var passwordError by rememberSaveable { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var totpInput by rememberSaveable { mutableStateOf("") }
    var totpError by rememberSaveable { mutableStateOf(false) }
    fun unlockUsingTotp() {
        if (TotpUtils.verifyCode(config.totpSecret, totpInput)) {
            fm.clearFocus()
            onSucceed()
        } else {
            totpError = true
        }
    }
    fun unlockUsingPassword() {
        if (passwordInput.hash() == config.passwordHash) {
            fm.clearFocus()
            onSucceed()
        } else {
            passwordError = true
        }
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 28 && config.biometrics) {
            startBiometricsUnlock(context, onSucceed)
        } else {
            fr.requestFocus()
        }
    }
    BackHandler(onBack = onDismiss)
    Card(
        Modifier.pointerInput(Unit) { detectTapGestures(onTap = { fm.clearFocus() }) },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(12.dp, 16.dp).width(250.dp)) {
            if (config.passwordHash.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        passwordInput,
                        {
                            passwordInput = it
                            passwordError = false
                        },
                        Modifier
                            .weight(1F)
                            .padding(vertical = 4.dp)
                            .focusRequester(fr),
                        label = { Text(stringResource(R.string.password)) },
                        isError = passwordError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions { unlockUsingPassword() },
                        visualTransformation = if (showPassword) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        trailingIcon = {
                            IconButton({ showPassword = !showPassword }) {
                                Icon(
                                    painterResource(
                                        if (showPassword) R.drawable.visibility_fill0
                                        else R.drawable.visibility_off_fill0
                                    ),
                                    null
                                )
                            }
                        }
                    )
                    FilledIconButton(::unlockUsingPassword) {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    }
                }
            }
            if (config.totpSecret.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        totpInput,
                        {
                            totpInput = it.filter { c -> c.isDigit() }.take(6)
                            totpError = false
                        },
                        Modifier.weight(1F).then(
                            if (config.passwordHash.isEmpty()) Modifier.focusRequester(fr)
                            else Modifier
                        ),
                        label = { Text("TOTP") },
                        isError = totpError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number, imeAction = ImeAction.Go
                        ),
                        keyboardActions = KeyboardActions { unlockUsingTotp() },
                        singleLine = true
                    )
                    FilledIconButton(::unlockUsingTotp) {
                        Icon(Icons.AutoMirrored.Default.ArrowForward, null)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            if (Build.VERSION.SDK_INT >= 28 && config.biometrics) OutlinedButton({
                startBiometricsUnlock(context, onSucceed)
            }, Modifier.fillMaxWidth()) {
                Icon(painterResource(R.drawable.fingerprint_fill0), null)
                Text(stringResource(R.string.use_biometrics))
            }
        }
    }
}

@RequiresApi(28)
fun startBiometricsUnlock(context: Context, onSucceed: () -> Unit) {
    if (Build.VERSION.SDK_INT >= 30) {
        val bm = context.getSystemService(BiometricManager::class.java)
        val status = bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK)
        if (status != BiometricManager.BIOMETRIC_SUCCESS) {
            context.showOperationResultToast(false)
            return
        }
    }
    val callback = object : AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            onSucceed()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            super.onAuthenticationError(errorCode, errString)
            if (errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED) {
                context.showOperationResultToast(false)
            }
        }
    }
    val cancel = CancellationSignal()
    val builder = BiometricPrompt.Builder(context)
        .setTitle(context.getText(R.string.unlock))
        .setNegativeButton(context.getString(R.string.cancel), context.mainExecutor) { _, _ -> cancel.cancel() }
    if (Build.VERSION.SDK_INT >= 30) {
        builder.setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK)
    }
    builder.build().authenticate(cancel, context.mainExecutor, callback)
}
