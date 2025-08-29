package com.bintianqi.owndroid

import android.content.Context
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun AppLockDialog(onSucceed: () -> Unit, onDismiss: () -> Unit) = Dialog(onDismiss, DialogProperties(true, false)) {
    val context = LocalContext.current
    val fm = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    fun unlock() {
        if(input.hash() == SP.lockPasswordHash) {
            fm.clearFocus()
            onSucceed()
        } else {
            isError = true
        }
    }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= 28 && SP.biometricsUnlock) startBiometricsUnlock(context, onSucceed)
    }
    BackHandler(onBack = onDismiss)
    Card(Modifier.pointerInput(Unit) { detectTapGestures(onTap = { fm.clearFocus() }) }, shape = RoundedCornerShape(16.dp)) {
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    input, { input = it; isError = false }, Modifier.width(200.dp),
                    label = { Text(stringResource(R.string.password)) }, isError = isError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password, imeAction = if(input.length >= 4) ImeAction.Go else ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions({ fm.clearFocus() }, { unlock() })
                )
                if(Build.VERSION.SDK_INT >= 28 && SP.biometricsUnlock) {
                    FilledTonalIconButton({ startBiometricsUnlock(context, onSucceed) }, Modifier.padding(start = 4.dp)) {
                        Icon(painterResource(R.drawable.fingerprint_fill0), null)
                    }
                }
            }
            Button(::unlock, Modifier.align(Alignment.End).padding(top = 8.dp), input.length >= 4) {
                Text(stringResource(R.string.unlock))
            }
        }
    }
}

@RequiresApi(28)
fun startBiometricsUnlock(context: Context, onSucceed: () -> Unit) {
    val callback = object : AuthenticationCallback() {
        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult?) {
            super.onAuthenticationSucceeded(result)
            onSucceed()
        }
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence?) {
            super.onAuthenticationError(errorCode, errString)
            if(errorCode != BiometricPrompt.BIOMETRIC_ERROR_CANCELED) context.showOperationResultToast(false)
        }
    }
    val cancel = CancellationSignal()
    BiometricPrompt.Builder(context)
        .setTitle(context.getText(R.string.unlock))
        .setNegativeButton(context.getString(R.string.cancel), context.mainExecutor) { _, _ -> cancel.cancel() }
        .build()
        .authenticate(cancel, context.mainExecutor, callback)
}
