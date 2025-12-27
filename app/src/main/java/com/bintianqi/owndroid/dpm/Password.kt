package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.Activity
import android.app.admin.DevicePolicyManager
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
import android.content.Intent
import android.os.Build.VERSION
import android.os.UserManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.HorizontalPadding
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.Privilege
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.SP
import com.bintianqi.owndroid.generateBase64Key
import com.bintianqi.owndroid.popToast
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
fun PasswordScreen(vm: MyViewModel,onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) }
    MyScaffold(R.string.password_and_keyguard, onNavigateUp, 0.dp) {
        FunctionItem(R.string.password_info, icon = R.drawable.info_fill0) { onNavigate(PasswordInfo) }
        if (SP.displayDangerousFeatures) {
            if(VERSION.SDK_INT >= 26) {
                FunctionItem(R.string.reset_password_token, icon = R.drawable.key_vertical_fill0) { onNavigate(ResetPasswordToken) }
            }
            FunctionItem(R.string.reset_password, icon = R.drawable.lock_reset_fill0) { onNavigate(ResetPassword) }
        }
        if(VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.required_password_complexity, icon = R.drawable.password_fill0) { onNavigate(RequiredPasswordComplexity) }
        }
        FunctionItem(R.string.disable_keyguard_features, icon = R.drawable.screen_lock_portrait_fill0) { onNavigate(KeyguardDisabledFeatures) }
        if(privilege.device) {
            FunctionItem(R.string.max_time_to_lock, icon = R.drawable.schedule_fill0) { dialog = 1 }
            FunctionItem(R.string.pwd_expiration_timeout, icon = R.drawable.lock_clock_fill0) { dialog = 3 }
            FunctionItem(R.string.max_pwd_fail, icon = R.drawable.no_encryption_fill0) { dialog = 4 }
        }
        if(VERSION.SDK_INT >= 26) {
            FunctionItem(R.string.required_strong_auth_timeout, icon = R.drawable.fingerprint_off_fill0) { dialog = 2 }
        }
        FunctionItem(R.string.pwd_history, icon = R.drawable.history_fill0) { dialog = 5 }
        if(VERSION.SDK_INT < 31) {
            FunctionItem(R.string.required_password_quality, icon = R.drawable.password_fill0) { onNavigate(RequiredPasswordQuality) }
        }
    }
    if(dialog != 0) {
        var input by remember { mutableStateOf("") }
        LaunchedEffect(Unit) {
            input = when (dialog) {
                1 -> vm.getMaxTimeToLock()
                2 -> vm.getRequiredStrongAuthTimeout()
                3 -> vm.getPasswordExpirationTimeout()
                4 -> vm.getMaxFailedPasswordsForWipe()
                5 -> vm.getPasswordHistoryLength()
                else -> 0
            }.toString()
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
                            1 -> vm.setMaxTimeToLock(input.toLong())
                            2 -> vm.setRequiredStrongAuthTimeout(input.toLong())
                            3 -> vm.setPasswordExpirationTimeout(input.toLong())
                            4 -> vm.setMaxFailedPasswordsForWipe(input.toInt())
                            5 -> vm.setPasswordHistoryLength(input.toInt())
                        }
                        dialog = 0
                    },
                    enabled = input.toLongOrNull() != null
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

@RequiresApi(29)
enum class PasswordComplexity(val id: Int, val text: Int) {
    None(DevicePolicyManager.PASSWORD_COMPLEXITY_NONE, R.string.none),
    Low(DevicePolicyManager.PASSWORD_COMPLEXITY_LOW, R.string.low),
    Medium(DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM, R.string.medium),
    High(DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH, R.string.high)
}

@Serializable object PasswordInfo

@Composable
fun PasswordInfoScreen(
    getComplexity: () -> PasswordComplexity, isSufficient: () -> Boolean, isUnified: () -> Boolean,
    onNavigateUp: () -> Unit
) {
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    var dialog by rememberSaveable { mutableIntStateOf(0) } // 0:none, 1:password complexity
    MyScaffold(R.string.password_info, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 31) {
            InfoItem(R.string.current_password_complexity, getComplexity().text, true) { dialog = 1 }
        }
        InfoItem(R.string.password_sufficient, isSufficient().yesOrNo)
        if(VERSION.SDK_INT >= 28 && privilege.work) {
            InfoItem(R.string.unified_password, isUnified().yesOrNo)
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

data class RpTokenState(val set: Boolean, val active: Boolean)

@Serializable object ResetPasswordToken

@RequiresApi(26)
@Composable
fun ResetPasswordTokenScreen(
    getState: () -> RpTokenState, setToken: (String) -> Boolean, getIntent: () -> Intent?,
    clearToken: () -> Boolean, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var token by rememberSaveable { mutableStateOf("") }
    var state by remember { mutableStateOf(getState()) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == Activity.RESULT_OK) {
            context.popToast(R.string.token_activated)
            state = getState()
        }
    }
    MyScaffold(R.string.reset_password_token, onNavigateUp) {
        OutlinedTextField(
            token, { token = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.token)) },
            supportingText = { Text("${token.length}/32") },
            trailingIcon = {
                IconButton({ token = generateBase64Key(24) }) {
                    Icon(painterResource(R.drawable.casino_fill0), null)
                }
            }
        )
        Button(
            onClick = {
                val result = setToken(token)
                context.showOperationResultToast(result)
                if (result) state = getState()
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            enabled = token.length >= 32
        ) {
            Text(stringResource(R.string.set))
        }
        if (state.set && !state.active) Button(
            onClick = {
                val intent = getIntent()
                if (intent == null) {
                    context.showOperationResultToast(false)
                } else {
                    launcher.launch(intent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.activate))
        }
        if (state.set) Button(
            onClick = {
                val result = clearToken()
                context.showOperationResultToast(result)
                state = getState()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Notes(R.string.activate_token_not_required_when_no_password)
    }
}

@Serializable object ResetPassword

@Composable
fun ResetPasswordScreen(resetPassword: (String, String, Int) -> Boolean, onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    var password by rememberSaveable { mutableStateOf("") }
    var token by rememberSaveable { mutableStateOf("") }
    var flags by rememberSaveable { mutableIntStateOf(0) }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    MyScaffold(R.string.reset_password, onNavigateUp) {
        if (VERSION.SDK_INT >= 26) {
            OutlinedTextField(
                token, { token = it }, Modifier.fillMaxWidth().padding(bottom = 5.dp),
                label = { Text(stringResource(R.string.token)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }
        OutlinedTextField(
            password, { password = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password)) },
            isError = password.length in 1..3,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.confirm_password)) },
            isError = confirmPassword != password,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            visualTransformation = PasswordVisualTransformation()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        CheckBoxItem(
            R.string.do_not_ask_credentials_on_boot,
            flags and RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT != 0
        ) { flags = flags xor RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT }
        CheckBoxItem(
            R.string.reset_password_require_entry,
            flags and RESET_PASSWORD_REQUIRE_ENTRY != 0
        ) { flags = flags xor RESET_PASSWORD_REQUIRE_ENTRY }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                context.showOperationResultToast(resetPassword(password, token, flags))
            },
            colors = ButtonDefaults.buttonColors(colorScheme.error, colorScheme.onError),
            modifier = Modifier.fillMaxWidth(),
            enabled = password == confirmPassword
        ) {
            Text(stringResource(R.string.reset_password))
        }
        Notes(R.string.info_reset_password)
    }
}

@Serializable object RequiredPasswordComplexity

@RequiresApi(31)
@Composable
fun RequiredPasswordComplexityScreen(
    getComplexity: () -> PasswordComplexity, setComplexity: (PasswordComplexity) -> Unit,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var complexity by rememberSaveable { mutableStateOf(PasswordComplexity.None) }
    LaunchedEffect(Unit) { complexity = getComplexity() }
    MyScaffold(R.string.required_password_complexity, onNavigateUp, 0.dp) {
        PasswordComplexity.entries.forEach {
            FullWidthRadioButtonItem(it.text, complexity == it) { complexity = it }
        }
        Button(
            onClick = {
                setComplexity(complexity)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp)
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Notes(R.string.info_password_complexity, HorizontalPadding)
    }
}

data class KeyguardDisabledFeature(val id: Int, val text: Int, val requiresApi: Int = 0)
@Suppress("InlinedApi")
val keyguardDisabledFeatures = listOf(
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_WIDGETS_ALL, R.string.disable_keyguard_features_widgets),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_SECURE_CAMERA, R.string.disable_keyguard_features_camera),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_SECURE_NOTIFICATIONS, R.string.disable_keyguard_features_notification),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS, R.string.disable_keyguard_features_unredacted_notification),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_TRUST_AGENTS, R.string.disable_keyguard_features_trust_agents),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT, R.string.disable_keyguard_features_fingerprint),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_FACE, R.string.disable_keyguard_features_face, 28),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_IRIS, R.string.disable_keyguard_features_iris, 28),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS, R.string.disable_keyguard_features_biometrics, 28),
    KeyguardDisabledFeature(DevicePolicyManager.KEYGUARD_DISABLE_SHORTCUTS_ALL, R.string.disable_keyguard_features_shortcuts, 34)
).filter { VERSION.SDK_INT >= it.requiresApi }

enum class KeyguardDisableMode(val text: Int) {
    None(R.string.enable_all), Custom(R.string.custom), All(R.string.disable_all)
}

data class KeyguardDisableConfig(val mode: KeyguardDisableMode, val flags: Int)


@Serializable object KeyguardDisabledFeatures

@Composable
fun KeyguardDisabledFeaturesScreen(
    getConfig: () -> KeyguardDisableConfig, setConfig: (KeyguardDisableConfig) -> Unit,
    onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var mode by rememberSaveable { mutableStateOf(KeyguardDisableMode.None) }
    var flags by rememberSaveable { mutableIntStateOf(0) }
    LaunchedEffect(Unit) {
        val config = getConfig()
        mode = config.mode
        flags = config.flags
    }
    MyScaffold(R.string.disable_keyguard_features, onNavigateUp) {
        KeyguardDisableMode.entries.forEach {
            FullWidthRadioButtonItem(it.text, mode == it) { mode = it }
        }
        Spacer(Modifier.height(8.dp))
        AnimatedVisibility(mode == KeyguardDisableMode.Custom) {
            Column {
                keyguardDisabledFeatures.forEach {
                    FullWidthCheckBoxItem(it.text, flags and it.id == it.id) { checked ->
                        flags = flags xor it.id
                    }
                }
            }
        }
        Button(
            onClick = {
                setConfig(KeyguardDisableConfig(mode, flags))
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(HorizontalPadding, 8.dp)
        ) {
            Text(text = stringResource(R.string.apply))
        }
    }
}

@Serializable object RequiredPasswordQuality

@Composable
fun RequiredPasswordQualityScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val passwordQuality = mapOf(
        PASSWORD_QUALITY_UNSPECIFIED to R.string.password_quality_unspecified,
        PASSWORD_QUALITY_SOMETHING to R.string.password_quality_something,
        PASSWORD_QUALITY_ALPHABETIC to R.string.password_quality_alphabetic,
        PASSWORD_QUALITY_NUMERIC to R.string.password_quality_numeric,
        PASSWORD_QUALITY_ALPHANUMERIC to R.string.password_quality_alphanumeric,
        PASSWORD_QUALITY_BIOMETRIC_WEAK to R.string.password_quality_biometrics_weak,
        PASSWORD_QUALITY_NUMERIC_COMPLEX to R.string.password_quality_numeric_complex
    )
    var selectedItem by rememberSaveable { mutableIntStateOf(PASSWORD_QUALITY_UNSPECIFIED) }
    LaunchedEffect(Unit) { selectedItem = Privilege.DPM.getPasswordQuality(Privilege.DAR) }
    MyScaffold(R.string.required_password_quality, onNavigateUp) {
        passwordQuality.forEach {
            RadioButtonItem(it.value, selectedItem == it.key) { selectedItem = it.key }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                Privilege.DPM.setPasswordQuality(Privilege.DAR, selectedItem)
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}
