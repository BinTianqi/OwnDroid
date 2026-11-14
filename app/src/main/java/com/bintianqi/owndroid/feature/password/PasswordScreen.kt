package com.bintianqi.owndroid.feature.password

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
import android.os.Build.VERSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CancelTextButton
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.ConfirmTextButton
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.InfoItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.generateBase64Key
import com.bintianqi.owndroid.utils.yesOrNo

@SuppressLint("NewApi")
@Composable
fun PasswordScreen(
    vm: PasswordViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    MyScaffold(R.string.password_and_keyguard, onNavigateUp, 0.dp) {
        FunctionItem(R.string.password_info, icon = R.drawable.info_fill0) {
            onNavigate(Destination.PasswordInfo)
        }
        if (vm.getDisplayDangerousFeatures()) {
            if (VERSION.SDK_INT >= 26) {
                FunctionItem(R.string.reset_password_token, icon = R.drawable.key_vertical_fill0) {
                    onNavigate(Destination.ResetPasswordToken)
                }
            }
            FunctionItem(R.string.reset_password, icon = R.drawable.lock_reset_fill0) {
                onNavigate(Destination.ResetPassword)
            }
        }
        if (VERSION.SDK_INT >= 31) {
            FunctionItem(R.string.required_password_complexity, icon = R.drawable.password_fill0) {
                onNavigate(Destination.RequiredPasswordComplexity)
            }
        }
        FunctionItem(
            R.string.disable_keyguard_features, icon = R.drawable.screen_lock_portrait_fill0
        ) {
            onNavigate(Destination.KeyguardDisabledFeatures)
        }
        if (privilege.device) {
            FunctionItem(R.string.max_time_to_lock, icon = R.drawable.schedule_fill0) {
                onNavigate(Destination.PasswordConfig(PasswordDialogType.MaxTimeToLock))
            }
            FunctionItem(
                R.string.pwd_expiration_timeout, icon = R.drawable.lock_clock_fill0
            ) {
                onNavigate(Destination.PasswordConfig(PasswordDialogType.ExpirationTimeout))
            }
            if (vm.getDisplayDangerousFeatures()) {
                FunctionItem(
                    R.string.max_pwd_fail, icon = R.drawable.no_encryption_fill0
                ) {
                    onNavigate(Destination.PasswordConfig(PasswordDialogType.MaxFailedForWipe))
                }
            }
        }
        if (VERSION.SDK_INT >= 26) {
            FunctionItem(
                R.string.required_strong_auth_timeout, icon = R.drawable.fingerprint_off_fill0
            ) {
                onNavigate(Destination.PasswordConfig(PasswordDialogType.StrongAuthTimeout))
            }
        }
        FunctionItem(R.string.pwd_history, icon = R.drawable.history_fill0) {
            onNavigate(Destination.PasswordConfig(PasswordDialogType.HistoryLength))
        }
        if (VERSION.SDK_INT < 31) {
            FunctionItem(R.string.required_password_quality, icon = R.drawable.password_fill0) {
                onNavigate(Destination.RequiredPasswordQuality)
            }
        }
    }
}

enum class PasswordDialogType {
    MaxTimeToLock, StrongAuthTimeout, ExpirationTimeout, MaxFailedForWipe, HistoryLength
}

@Suppress("NewApi")
@Composable
fun PasswordConfigurationDialog(
    vm: PasswordViewModel, dialogType: PasswordDialogType, navigateUp: () -> Unit
) {
    val input by when (dialogType) {
        PasswordDialogType.MaxTimeToLock -> vm.maxTimeToLockState
        PasswordDialogType.StrongAuthTimeout -> vm.strongAutoTimeoutState
        PasswordDialogType.ExpirationTimeout -> vm.expirationTimeoutState
        PasswordDialogType.MaxFailedForWipe -> vm.maxFailedForWipeState
        PasswordDialogType.HistoryLength -> vm.historyLengthState
    }.collectAsState()
    LaunchedEffect(Unit) {
        when (dialogType) {
            PasswordDialogType.MaxTimeToLock -> vm.getMaxTimeToLock()
            PasswordDialogType.StrongAuthTimeout -> vm.getStrongAuthTimeout()
            PasswordDialogType.ExpirationTimeout -> vm.getExpirationTimeout()
            PasswordDialogType.MaxFailedForWipe -> vm.getMaxFailedForWipe()
            PasswordDialogType.HistoryLength -> vm.getHistoryLength()
        }
    }
    val titleText = when (dialogType) {
        PasswordDialogType.MaxTimeToLock -> R.string.max_time_to_lock
        PasswordDialogType.StrongAuthTimeout -> R.string.required_strong_auth_timeout
        PasswordDialogType.ExpirationTimeout -> R.string.pwd_expiration_timeout
        PasswordDialogType.MaxFailedForWipe -> R.string.max_pwd_fail
        PasswordDialogType.HistoryLength -> R.string.pwd_history
    }
    val fieldLabel = when (dialogType) {
        PasswordDialogType.HistoryLength -> R.string.length
        PasswordDialogType.MaxFailedForWipe -> R.string.max_pwd_fail_textfield
        else -> R.string.time_unit_ms
    }
    val helpingText = when (dialogType) {
        PasswordDialogType.MaxTimeToLock -> R.string.info_screen_timeout
        PasswordDialogType.StrongAuthTimeout -> R.string.info_required_strong_auth_timeout
        PasswordDialogType.ExpirationTimeout -> R.string.info_password_expiration_timeout
        PasswordDialogType.MaxFailedForWipe -> {
            if (vm.isSystemUser()) R.string.info_max_failed_password_system_user
            else R.string.info_max_failed_password_other_user
        }
        PasswordDialogType.HistoryLength -> R.string.info_password_history_length
    }
    fun setInputValue(input: String) {
        when (dialogType) {
            PasswordDialogType.MaxTimeToLock -> vm.setMaxTimeToLock(input)
            PasswordDialogType.StrongAuthTimeout -> vm.setStrongAuthTimeout(input)
            PasswordDialogType.ExpirationTimeout -> vm.setExpirationTimeout(input)
            PasswordDialogType.MaxFailedForWipe -> vm.setMaxFailedForWipe(input)
            PasswordDialogType.HistoryLength -> vm.setHistoryLength(input)
        }
    }
    fun applyConfig() {
        when (dialogType) {
            PasswordDialogType.MaxTimeToLock -> vm.applyMaxTimeToLock()
            PasswordDialogType.StrongAuthTimeout -> vm.applyStrongAuthTimeout()
            PasswordDialogType.ExpirationTimeout -> vm.applyExpirationTimeout()
            PasswordDialogType.MaxFailedForWipe -> vm.applyMaxFailedForWipe()
            PasswordDialogType.HistoryLength -> vm.applyHistoryLength()
        }
    }
    AlertDialog(
        title = { Text(stringResource(titleText)) },
        text = {
            Column {
                OutlinedTextField(
                    input, ::setInputValue,
                    Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    label = { Text(stringResource(fieldLabel)) },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number, imeAction = ImeAction.Done
                    ),
                    textStyle = typography.bodyLarge
                )
                Text(stringResource(helpingText))
            }
        },
        confirmButton = {
            TextButton(
                {
                    applyConfig()
                    navigateUp()
                },
                enabled = input.toLongOrNull() != null
            ) {
                Text(stringResource(R.string.apply))
            }
        },
        dismissButton = {
            CancelTextButton(navigateUp)
        },
        onDismissRequest = navigateUp
    )
}

fun getComplexityText(complexity: Int): Int {
    return when (complexity) {
        DevicePolicyManager.PASSWORD_COMPLEXITY_NONE -> R.string.none
        DevicePolicyManager.PASSWORD_COMPLEXITY_LOW -> R.string.low
        DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM -> R.string.medium
        DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH -> R.string.high
        else -> R.string.unknown
    }
}

@Composable
fun PasswordInfoScreen(
    vm: PasswordViewModel, onNavigateUp: () -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    val info by vm.passwordInfoState.collectAsState()
    var dialog by rememberSaveable { mutableIntStateOf(0) } // 0:none, 1:password complexity
    LaunchedEffect(Unit) {
        vm.getPasswordInfo()
    }
    MyScaffold(R.string.password_info, onNavigateUp, 0.dp) {
        if (VERSION.SDK_INT >= 31) {
            InfoItem(
                R.string.current_password_complexity, getComplexityText(info.complexity), true
            ) { dialog = 1 }
        }
        InfoItem(R.string.password_sufficient, info.complexitySufficient.yesOrNo)
        if (VERSION.SDK_INT >= 28 && privilege.work) {
            InfoItem(R.string.unified_password, info.unified.yesOrNo)
        }
    }
    if (dialog != 0) AlertDialog(
        text = { Text(stringResource(R.string.info_password_complexity)) },
        confirmButton = {
            ConfirmTextButton { dialog = 0 }
        },
        onDismissRequest = { dialog = 0 }
    )
}

@RequiresApi(26)
@Composable
fun ResetPasswordTokenScreen(
    vm: PasswordViewModel, onNavigateUp: () -> Unit
) {
    var token by rememberSaveable { mutableStateOf("") }
    val tokenSize = token.encodeToByteArray().size
    val state by vm.rpTokenState.collectAsState()
    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) vm.getRpTokenState()
        }
    MyScaffold(R.string.reset_password_token, onNavigateUp) {
        OutlinedTextField(
            token, { token = it }, Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.token)) },
            supportingText = { Text("${tokenSize}/32 bytes") },
            trailingIcon = {
                IconButton({ token = generateBase64Key(24) }) {
                    Icon(painterResource(R.drawable.casino_fill0), null)
                }
            }
        )
        Button(
            {
                vm.setRpToken(token)
            },
            Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp),
            tokenSize >= 32
        ) {
            Text(stringResource(R.string.set))
        }
        if (state.set && !state.active) Button(
            {
                val intent = vm.createActivateRpTokenIntent()
                if (intent != null) {
                    launcher.launch(intent)
                }
            },
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.activate))
        }
        if (state.set) Button(
            vm::clearRpToken,
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Notes(R.string.activate_token_not_required_when_no_password)
    }
}

@Composable
fun ResetPasswordScreen(vm: PasswordViewModel, onNavigateUp: () -> Unit) {
    var password by rememberSaveable { mutableStateOf("") }
    var token by rememberSaveable { mutableStateOf("") }
    var flags by rememberSaveable { mutableIntStateOf(0) }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    MyScaffold(R.string.reset_password, onNavigateUp) {
        if (VERSION.SDK_INT >= 26) {
            OutlinedTextField(
                token, { token = it }, Modifier
                    .fillMaxWidth()
                    .padding(bottom = 5.dp),
                label = { Text(stringResource(R.string.token)) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )
        }
        OutlinedTextField(
            password, { password = it },
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.password)) },
            isError = password.length in 1..3,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Next
            ),
            visualTransformation = PasswordVisualTransformation()
        )
        OutlinedTextField(
            confirmPassword, { confirmPassword = it },
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.confirm_password)) },
            isError = confirmPassword != password,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password, imeAction = ImeAction.Done
            ),
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
            {
                vm.resetPassword(password, token, flags)
            },
            Modifier.fillMaxWidth(),
            password == confirmPassword,
            colors = ButtonDefaults.buttonColors(colorScheme.error, colorScheme.onError)
        ) {
            Text(stringResource(R.string.reset_password))
        }
        Notes(R.string.info_reset_password)
    }
}

@RequiresApi(31)
@Composable
fun RequiredPasswordComplexityScreen(
    vm: PasswordViewModel, onNavigateUp: () -> Unit
) {
    val complexity by vm.requiredComplexityState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getRequiredComplexity()
    }
    MyScaffold(R.string.required_password_complexity, onNavigateUp, 0.dp) {
        listOf(
            DevicePolicyManager.PASSWORD_COMPLEXITY_NONE to R.string.none,
            DevicePolicyManager.PASSWORD_COMPLEXITY_LOW to R.string.low,
            DevicePolicyManager.PASSWORD_COMPLEXITY_MEDIUM to R.string.medium,
            DevicePolicyManager.PASSWORD_COMPLEXITY_HIGH to R.string.high
        ).forEach {
            FullWidthRadioButtonItem(it.second, complexity == it.first) {
                vm.setRequiredComplexity(it.first)
            }
        }
        Button(
            vm::applyRequiredComplexity,
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 8.dp)
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Notes(R.string.info_password_complexity, HorizontalPadding)
    }
}


@Composable
fun KeyguardDisabledFeaturesScreen(
    vm: PasswordViewModel, onNavigateUp: () -> Unit
) {
    val config by vm.keyguardDisableState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getKeyguardDisableConfig()
    }
    MyScaffold(R.string.disable_keyguard_features, onNavigateUp, 0.dp) {
        KeyguardDisableMode.entries.forEach {
            FullWidthRadioButtonItem(it.text, config.mode == it) {
                vm.setKeyguardDisableConfig(config.copy(mode = it))
            }
        }
        Spacer(Modifier.height(8.dp))
        AnimatedVisibility(config.mode == KeyguardDisableMode.Custom) {
            Column {
                keyguardDisabledFeatures.forEach {
                    FullWidthCheckBoxItem(it.text, config.flags and it.id == it.id) { _ ->
                        vm.setKeyguardDisableConfig(config.copy(flags = config.flags xor it.id))
                    }
                }
            }
        }
        Button(
            vm::applyKeyguardDisableConfig,
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 8.dp)
        ) {
            Text(text = stringResource(R.string.apply))
        }
    }
}

@Composable
fun RequiredPasswordQualityScreen(
    vm: PasswordViewModel,
    onNavigateUp: () -> Unit
) {
    val quality by vm.qualityState.collectAsState()
    LaunchedEffect(Unit) {
        vm.getQuality()
    }
    MyScaffold(R.string.required_password_quality, onNavigateUp) {
        mapOf(
            PASSWORD_QUALITY_UNSPECIFIED to R.string.password_quality_unspecified,
            PASSWORD_QUALITY_SOMETHING to R.string.password_quality_something,
            PASSWORD_QUALITY_ALPHABETIC to R.string.password_quality_alphabetic,
            PASSWORD_QUALITY_NUMERIC to R.string.password_quality_numeric,
            PASSWORD_QUALITY_ALPHANUMERIC to R.string.password_quality_alphanumeric,
            PASSWORD_QUALITY_BIOMETRIC_WEAK to R.string.password_quality_biometrics_weak,
            PASSWORD_QUALITY_NUMERIC_COMPLEX to R.string.password_quality_numeric_complex
        ).forEach {
            RadioButtonItem(it.value, quality == it.key) { vm.setQuality(it.key) }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            vm::applyQuality,
            Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}
