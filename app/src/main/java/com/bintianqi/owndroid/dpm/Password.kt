package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FACE
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_IRIS
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_SHORTCUTS_ALL
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL
import android.app.admin.DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH
import android.app.admin.DevicePolicyManager.PASSWORD_COMPLEXITY_LOW
import android.app.admin.DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM
import android.app.admin.DevicePolicyManager.PASSWORD_COMPLEXITY_NONE
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_ALPHABETIC
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_ALPHANUMERIC
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_BIOMETRIC_WEAK
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_SOMETHING
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED
import android.app.admin.DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT
import android.app.admin.DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY
import android.content.Context
import android.os.Build.VERSION
import android.os.UserManager
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SharedPrefs
import com.bintianqi.owndroid.showOperationResultToast
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.yesOrNo
import kotlinx.serialization.Serializable

@Serializable object Password

@SuppressLint("NewApi")
@Composable
fun PasswordScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val deviceAdmin = context.isDeviceAdmin
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var dialog by remember { mutableIntStateOf(0) }
    MyScaffold(R.string.password_and_keyguard, onNavigateUp, 0.dp) {
        FunctionItem(R.string.password_info, icon = R.drawable.info_fill0) { onNavigate(PasswordInfo) }
        if(SharedPrefs(context).displayDangerousFeatures) {
            if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner)) {
                FunctionItem(R.string.reset_password_token, icon = R.drawable.key_vertical_fill0) { onNavigate(ResetPasswordToken) }
            }
            if(deviceAdmin || deviceOwner || profileOwner) {
                FunctionItem(R.string.reset_password, icon = R.drawable.lock_reset_fill0) { onNavigate(ResetPassword) }
            }
        }
        if(VERSION.SDK_INT >= 31 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.required_password_complexity, icon = R.drawable.password_fill0) { onNavigate(RequiredPasswordComplexity) }
        }
        if(deviceAdmin) {
            FunctionItem(R.string.disable_keyguard_features, icon = R.drawable.screen_lock_portrait_fill0) { onNavigate(KeyguardDisabledFeatures) }
        }
        if(deviceOwner) {
            FunctionItem(R.string.max_time_to_lock, icon = R.drawable.schedule_fill0) { dialog = 1 }
            FunctionItem(R.string.pwd_expiration_timeout, icon = R.drawable.lock_clock_fill0) { dialog = 3 }
            FunctionItem(R.string.max_pwd_fail, icon = R.drawable.no_encryption_fill0) { dialog = 4 }
        }
        if(VERSION.SDK_INT >= 26 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.required_strong_auth_timeout, icon = R.drawable.fingerprint_off_fill0) { dialog = 2 }
        }
        if(deviceAdmin){
            FunctionItem(R.string.pwd_history, icon = R.drawable.history_fill0) { dialog = 5 }
        }
        if(VERSION.SDK_INT < 31 && (deviceOwner || profileOwner)) {
            FunctionItem(R.string.required_password_quality, icon = R.drawable.password_fill0) { onNavigate(RequiredPasswordQuality) }
        }
    }
    if(dialog != 0) {
        val dpm = context.getDPM()
        val receiver = context.getReceiver()
        var input by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            input = when(dialog) {
                1 -> dpm.getMaximumTimeToLock(receiver).toString()
                2 -> dpm.getRequiredStrongAuthTimeout(receiver).toString()
                3 -> dpm.getPasswordExpirationTimeout(receiver).toString()
                4 -> dpm.getMaximumFailedPasswordsForWipe(receiver).toString()
                5 -> dpm.getPasswordHistoryLength(receiver).toString()
                else -> ""
            }
        }
        AlertDialog(
            title = {
                Text(stringResource(
                    when(dialog) {
                        1 -> R.string.max_time_to_lock
                        2 -> R.string.required_strong_auth_timeout
                        3 -> R.string.pwd_expiration_timeout
                        4 -> R.string.max_pwd_fail
                        5 -> R.string.pwd_history
                        else -> R.string.password
                    }
                ))
            },
            text = {
                val focusMgr = LocalFocusManager.current
                val um = context.getSystemService(Context.USER_SERVICE) as UserManager
                Column {
                    OutlinedTextField(
                        value = input,
                        label = {
                            Text(stringResource(
                                when(dialog) {
                                    1,2,3 -> R.string.time_unit_ms
                                    4 -> R.string.max_pwd_fail_textfield
                                    5 -> R.string.length
                                    else -> R.string.password
                                }
                            ))
                        },
                        onValueChange = { input = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                        textStyle = typography.bodyLarge,
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    Text(stringResource(
                        when(dialog) {
                            1 -> R.string.info_screen_timeout
                            2 -> R.string.info_required_strong_auth_timeout
                            3 -> R.string.info_password_expiration_timeout
                            4 -> if(um.isSystemUser) R.string.info_max_failed_password_system_user else R.string.info_max_failed_password_other_user
                            5 -> R.string.info_password_history_length
                            else -> R.string.password
                        }
                    ))
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        when(dialog) {
                            1 -> dpm.setMaximumTimeToLock(receiver, input.toLong())
                            2 -> dpm.setRequiredStrongAuthTimeout(receiver, input.toLong())
                            3 -> dpm.setPasswordExpirationTimeout(receiver, input.toLong())
                            4 -> dpm.setMaximumFailedPasswordsForWipe(receiver, input.toInt())
                            5 -> dpm.setPasswordHistoryLength(receiver, input.toInt())
                        }
                        dialog = 0
                    }
                ) {
                    Text(stringResource(R.string.apply))
                }
            },
            dismissButton = {
                TextButton(onClick = { dialog = 0 }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = {
                dialog = 0
            }
        )
    }
}

@Serializable object PasswordInfo

@Composable
fun PasswordInfoScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val deviceOwner = context.isDeviceOwner
    val profileOwner = context.isProfileOwner
    var dialog by remember { mutableIntStateOf(0) } // 0:none, 1:password complexity
    MyScaffold(R.string.password_info, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 29) {
            val text = when(dpm.passwordComplexity) {
                PASSWORD_COMPLEXITY_NONE -> R.string.none
                PASSWORD_COMPLEXITY_LOW -> R.string.low
                PASSWORD_COMPLEXITY_MEDIUM -> R.string.medium
                PASSWORD_COMPLEXITY_HIGH -> R.string.high
                else -> R.string.unknown
            }
            InfoItem(R.string.current_password_complexity, text, true) { dialog = 1 }
        }
        if(deviceOwner || profileOwner) {
            InfoItem(R.string.password_sufficient, dpm.isActivePasswordSufficient.yesOrNo)
        }
        if(VERSION.SDK_INT >= 28 && profileOwner && dpm.isManagedProfile(receiver)) {
            InfoItem(R.string.unified_password, dpm.isUsingUnifiedPassword(receiver).yesOrNo)
        }
    }
    if(dialog != 0) AlertDialog(
        text = { Text(stringResource(R.string.info_password_complexity)) },
        confirmButton = {
            TextButton({ dialog = 0 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@Serializable object ResetPasswordToken

@RequiresApi(26)
@Composable
fun ResetPasswordTokenScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var token by remember { mutableStateOf("") }
    val tokenByteArray = token.toByteArray()
    val focusMgr = LocalFocusManager.current
    MyScaffold(R.string.reset_password_token, onNavigateUp) {
        OutlinedTextField(
            value = token, onValueChange = { token = it },
            label = { Text(stringResource(R.string.token)) },
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            supportingText = {
                AnimatedVisibility(tokenByteArray.size < 32) {
                    Text(stringResource(R.string.token_must_longer_than_32_byte))
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                try {
                    context.showOperationResultToast(dpm.setResetPasswordToken(receiver, tokenByteArray))
                } catch(_:SecurityException) {
                    Toast.makeText(context, R.string.security_exception, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            enabled = tokenByteArray.size >= 32
        ) {
            Text(stringResource(R.string.set))
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    if(!dpm.isResetPasswordTokenActive(receiver)) {
                        try { activateToken(context) }
                        catch(_:NullPointerException) { Toast.makeText(context, R.string.please_set_a_token, Toast.LENGTH_SHORT).show() }
                    } else { Toast.makeText(context, R.string.token_already_activated, Toast.LENGTH_SHORT).show() }
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.activate))
            }
            Button(
                onClick = { context.showOperationResultToast(dpm.clearResetPasswordToken(receiver)) },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.clear))
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Notes(R.string.activate_token_not_required_when_no_password)
    }
}

@Serializable object ResetPassword

@Composable
fun ResetPasswordScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    var useToken by remember { mutableStateOf(false) }
    var token by remember { mutableStateOf("") }
    val tokenByteArray = token.toByteArray()
    var flag by remember { mutableIntStateOf(0) }
    var confirmDialog by remember { mutableStateOf(false) }
    MyScaffold(R.string.reset_password, onNavigateUp) {
        if(VERSION.SDK_INT >= 26) {
            OutlinedTextField(
                value = token, onValueChange = { token = it },
                label = { Text(stringResource(R.string.token)) },
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                modifier = Modifier.fillMaxWidth()
            )
        }
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            supportingText = { Text(stringResource(R.string.reset_pwd_desc)) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 23) {
            CheckBoxItem(
                R.string.do_not_ask_credentials_on_boot,
                flag and RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT != 0
            ) { flag = flag xor RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT }
        }
        CheckBoxItem(
            R.string.reset_password_require_entry,
            flag and RESET_PASSWORD_REQUIRE_ENTRY != 0
        ) { flag = flag xor RESET_PASSWORD_REQUIRE_ENTRY }
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 26) {
            Button(
                onClick = {
                    useToken = true
                    confirmDialog = true
                    focusMgr.clearFocus()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                enabled = tokenByteArray.size >=32 && password.length !in 1..3 && (context.isDeviceOwner || context.isProfileOwner),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reset_password_with_token))
            }
        }
        if(VERSION.SDK_INT <= 30) {
            Button(
                onClick = {
                    useToken = false
                    confirmDialog = true
                    focusMgr.clearFocus()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                enabled = password.length !in 1..3,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reset_password))
            }
        }
        Notes(R.string.info_reset_password)
    }
    if(confirmDialog) {
        var confirmPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { confirmDialog = false },
            title = { Text(stringResource(R.string.reset_password)) },
            text = {
                val dialogFocusMgr = LocalFocusManager.current
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text(stringResource(R.string.confirm_password)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { dialogFocusMgr.clearFocus() }),
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val success = if(VERSION.SDK_INT >= 26 && useToken) {
                            dpm.resetPasswordWithToken(receiver, password, tokenByteArray, flag)
                        } else {
                            dpm.resetPassword(password, flag)
                        }
                        context.showOperationResultToast(success)
                        password = ""
                        confirmDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = colorScheme.error),
                    enabled = confirmPassword == password
                ) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Serializable object RequiredPasswordComplexity

@RequiresApi(31)
@Composable
fun RequiredPasswordComplexityScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val passwordComplexity = mapOf(
        PASSWORD_COMPLEXITY_NONE to R.string.none,
        PASSWORD_COMPLEXITY_LOW to R.string.low,
        PASSWORD_COMPLEXITY_MEDIUM to R.string.medium,
        PASSWORD_COMPLEXITY_HIGH to R.string.high
    )
    var selectedItem by remember { mutableIntStateOf(PASSWORD_COMPLEXITY_NONE) }
    LaunchedEffect(Unit) { selectedItem = dpm.requiredPasswordComplexity }
    MyScaffold(R.string.required_password_complexity, onNavigateUp, 0.dp) {
        passwordComplexity.forEach {
            FullWidthRadioButtonItem(it.value, selectedItem == it.key) { selectedItem = it.key }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.requiredPasswordComplexity = selectedItem
                selectedItem = dpm.requiredPasswordComplexity
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Notes(R.string.info_password_complexity, HorizontalPadding)
    }
}

@Serializable object KeyguardDisabledFeatures

@Composable
fun KeyguardDisabledFeaturesScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var flag by remember { mutableIntStateOf(0) }
    var mode by remember { mutableIntStateOf(0) } // 0:Enable all, 1:Disable all, 2:Custom
    val flagsLiat = mutableListOf(
        R.string.disable_keyguard_features_widgets to KEYGUARD_DISABLE_WIDGETS_ALL,
        R.string.disable_keyguard_features_camera to KEYGUARD_DISABLE_SECURE_CAMERA,
        R.string.disable_keyguard_features_notification to KEYGUARD_DISABLE_SECURE_NOTIFICATIONS,
        R.string.disable_keyguard_features_unredacted_notification to KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS,
        R.string.disable_keyguard_features_trust_agents to KEYGUARD_DISABLE_TRUST_AGENTS,
        R.string.disable_keyguard_features_fingerprint to KEYGUARD_DISABLE_FINGERPRINT
    )
    if(VERSION.SDK_INT >= 28) {
        flagsLiat +=R.string.disable_keyguard_features_face to KEYGUARD_DISABLE_FACE
        flagsLiat += R.string.disable_keyguard_features_iris to KEYGUARD_DISABLE_IRIS
        flagsLiat += R.string.disable_keyguard_features_biometrics to KEYGUARD_DISABLE_BIOMETRICS
    }
    if(VERSION.SDK_INT >= 34) flagsLiat += R.string.disable_keyguard_features_shortcuts to KEYGUARD_DISABLE_SHORTCUTS_ALL
    fun refresh() {
        flag = dpm.getKeyguardDisabledFeatures(receiver)
        mode = when(flag) {
            KEYGUARD_DISABLE_FEATURES_NONE -> 0
            KEYGUARD_DISABLE_FEATURES_ALL -> 1
            else -> 2
        }
    }
    LaunchedEffect(mode) { if(mode != 2) flag = dpm.getKeyguardDisabledFeatures(receiver) }
    LaunchedEffect(Unit) { refresh() }
    MyScaffold(R.string.disable_keyguard_features, onNavigateUp) {
        FullWidthRadioButtonItem(R.string.enable_all, mode == 0) { mode = 0 }
        FullWidthRadioButtonItem(R.string.disable_all, mode == 1) { mode = 1 }
        FullWidthRadioButtonItem(R.string.custom, mode == 2) { mode = 2 }
        AnimatedVisibility(mode == 2) {
            Column {
                flagsLiat.forEach {
                    FullWidthCheckBoxItem(it.first, flag and it.second == it.second) { checked ->
                        flag = if(checked) flag or it.second else flag and (flag xor it.second)
                    }
                }
            }
        }
        Button(
            onClick = {
                val disabledFeatures = if(mode == 0) KEYGUARD_DISABLE_FEATURES_NONE else if(mode == 1) KEYGUARD_DISABLE_FEATURES_ALL else flag
                dpm.setKeyguardDisabledFeatures(receiver, disabledFeatures)
                refresh()
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp, horizontal = HorizontalPadding)
        ) {
            Text(text = stringResource(R.string.apply))
        }
    }
}

@Serializable object RequiredPasswordQuality

@Composable
fun RequiredPasswordQualityScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val passwordQuality = mapOf(
        PASSWORD_QUALITY_UNSPECIFIED to R.string.password_quality_unspecified,
        PASSWORD_QUALITY_SOMETHING to R.string.password_quality_something,
        PASSWORD_QUALITY_ALPHABETIC to R.string.password_quality_alphabetic,
        PASSWORD_QUALITY_NUMERIC to R.string.password_quality_numeric,
        PASSWORD_QUALITY_ALPHANUMERIC to R.string.password_quality_alphanumeric,
        PASSWORD_QUALITY_BIOMETRIC_WEAK to R.string.password_quality_biometrics_weak,
        PASSWORD_QUALITY_NUMERIC_COMPLEX to R.string.password_quality_numeric_complex
    )
    var selectedItem by remember { mutableIntStateOf(PASSWORD_QUALITY_UNSPECIFIED) }
    LaunchedEffect(Unit) { selectedItem=dpm.getPasswordQuality(receiver) }
    MyScaffold(R.string.required_password_quality, onNavigateUp) {
        passwordQuality.forEach {
            RadioButtonItem(it.value, selectedItem == it.key) { selectedItem = it.key }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.setPasswordQuality(receiver,selectedItem)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

private fun activateToken(context: Context) {
    val desc = context.getString(R.string.activate_reset_password_token_here)
    val keyguardManager = context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val confirmIntent = keyguardManager.createConfirmDeviceCredentialIntent(context.getString(R.string.app_name), desc)
    if (confirmIntent != null) {
        startActivity(context,confirmIntent, null)
    } else {
        Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show()
    }
}
