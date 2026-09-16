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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.bintianqi.owndroid.utils.hash
import com.bintianqi.owndroid.utils.showOperationResultToast

@Composable
fun AppLockDialog(
    config: MySettings.AppLock, onSucceed: () -> Unit, onDismiss: () -> Unit,
    verifyTotp: ((String) -> Boolean)? = null
) = Dialog(onDismiss, DialogProperties(true, false)) {
    val context = LocalContext.current
    val fm = LocalFocusManager.current
    val fr = remember { FocusRequester() }
    var input by rememberSaveable { mutableStateOf("") }
    var isError by rememberSaveable { mutableStateOf(false) }
    var showPassword by remember { mutableStateOf(false) }
    var totpInput by rememberSaveable { mutableStateOf("") }
    var totpError by rememberSaveable { mutableStateOf(false) }
    val hasPassword = config.passwordHash.isNotEmpty()
    val showTotp = config.totp && verifyTotp != null
    fun unlockTotp() {
        if (verifyTotp?.invoke(totpInput) == true) {
            fm.clearFocus()
            onSucceed()
        } else {
            totpError = true
        }
    }
    fun unlock() {
        if (hasPassword && input.isNotEmpty()) {
            if (input.hash() == config.passwordHash) {
                fm.clearFocus()
                onSucceed()
            } else {
                isError = true
            }
        } else if (showTotp) {
            unlockTotp()
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
    Card(Modifier.pointerInput(Unit) { detectTapGestures(onTap = { fm.clearFocus() }) }, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            if (hasPassword) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        input, { input = it; isError = false }, Modifier.width(200.dp).focusRequester(fr),
                        label = { Text(stringResource(R.string.password)) }, isError = isError,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password, imeAction = if(input.length >= 4) ImeAction.Go else ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions({ fm.clearFocus() }, { unlock() }),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    painter = painterResource(
                                        id = if (showPassword) R.drawable.visibility_fill0 else R.drawable.visibility_off_fill0
                                    ),
                                    contentDescription = if (showPassword) "Hide password" else "Show password"
                                )
                            }
                        }
                    )
                    if (Build.VERSION.SDK_INT >= 28 && config.biometrics && !showTotp) {
                        FilledTonalIconButton({ startBiometricsUnlock(context, onSucceed) }, Modifier.padding(start = 4.dp)) {
                            Icon(painterResource(R.drawable.fingerprint_fill0), null)
                        }
                    }
                }
            }
            if (showTotp) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = if (hasPassword) 8.dp else 0.dp)
                ) {
                    OutlinedTextField(
                        totpInput,
                        { totpInput = it.filter { c -> c.isDigit() }.take(6); totpError = false },
                        Modifier.width(200.dp).focusRequester(if (hasPassword) remember { FocusRequester() } else fr),
                        label = { Text("TOTP") },
                        isError = totpError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Go),
                        keyboardActions = KeyboardActions(onGo = { unlockTotp() }),
                        singleLine = true
                    )
                    if (Build.VERSION.SDK_INT >= 28 && config.biometrics) {
                        FilledTonalIconButton({ startBiometricsUnlock(context, onSucceed) }, Modifier.padding(start = 4.dp)) {
                            Icon(painterResource(R.drawable.fingerprint_fill0), null)
                        }
                    }
                }
            }
            Button(::unlock, Modifier.align(Alignment.End).padding(top = 8.dp)) {
                Text(stringResource(R.string.unlock))
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
