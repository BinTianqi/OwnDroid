package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.admin.DevicePolicyManager.ACTION_SET_NEW_PASSWORD
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_BIOMETRICS
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FACE
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_ALL
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FEATURES_NONE
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_FINGERPRINT
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_IRIS
import android.app.admin.DevicePolicyManager.KEYGUARD_DISABLE_REMOTE_INPUT
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
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_COMPLEX
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_NUMERIC
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_NUMERIC_COMPLEX
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_SOMETHING
import android.app.admin.DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED
import android.app.admin.DevicePolicyManager.RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT
import android.app.admin.DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.Information
import com.bintianqi.owndroid.ui.RadioButtonItem
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.TopBar

@Composable
fun Password(navCtrl: NavHostController) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    Scaffold(
        topBar = {
            TopBar(backStackEntry,navCtrl,localNavCtrl) {
                if(backStackEntry?.destination?.route == "Home" && scrollState.maxValue > 80) {
                    Text(
                        text = stringResource(R.string.password_and_keyguard),
                        modifier = Modifier.alpha((maxOf(scrollState.value-30,0)).toFloat()/80)
                    )
                }
            }
        }
    ) {
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition,
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ) {
            composable(route = "Home") { Home(localNavCtrl,scrollState) }
            composable(route = "PasswordInfo") { PasswordInfo() }
            composable(route = "ResetPasswordToken") { ResetPasswordToken() }
            composable(route = "ResetPassword") { ResetPassword() }
            composable(route = "RequirePasswordComplexity") { PasswordComplexity() }
            composable(route = "DisableKeyguardFeatures") { DisableKeyguardFeatures() }
            composable(route = "MaxTimeToLock") { ScreenTimeout() }
            composable(route = "PasswordTimeout") { PasswordExpiration() }
            composable(route = "MaxPasswordFail") { MaxFailedPasswordForWipe() }
            composable(route = "RequiredStrongAuthTimeout") { RequiredStrongAuthTimeout() }
            composable(route = "PasswordHistoryLength") { PasswordHistoryLength() }
            composable(route = "RequirePasswordQuality") { PasswordQuality() }
        }
    }
}

@Composable
private fun Home(navCtrl:NavHostController,scrollState: ScrollState) {
    val context = LocalContext.current
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState).padding(start = 30.dp, end = 12.dp)) {
        Text(
            text = stringResource(R.string.password_and_keyguard),
            style = typography.headlineLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 5.dp)
        )
        SubPageItem(R.string.password_info, "", R.drawable.info_fill0) { navCtrl.navigate("PasswordInfo") }
        if(VERSION.SDK_INT >= 26 && (context.isDeviceOwner || context.isProfileOwner)) {
            SubPageItem(R.string.reset_password_token, "", R.drawable.key_vertical_fill0) { navCtrl.navigate("ResetPasswordToken") }
        }
        if(context.isDeviceAdmin || context.isDeviceOwner || context.isProfileOwner) {
            SubPageItem(R.string.reset_password, "", R.drawable.lock_reset_fill0) { navCtrl.navigate("ResetPassword") }
        }
        if(VERSION.SDK_INT >= 31 && (context.isDeviceOwner || context.isProfileOwner)) {
            SubPageItem(R.string.required_password_complexity, "", R.drawable.password_fill0) { navCtrl.navigate("RequirePasswordComplexity") }
        }
        if(context.isDeviceAdmin) {
            SubPageItem(R.string.disable_keyguard_features, "", R.drawable.screen_lock_portrait_fill0) { navCtrl.navigate("DisableKeyguardFeatures") }
        }
        if(context.isDeviceOwner) {
            SubPageItem(R.string.max_time_to_lock, "", R.drawable.schedule_fill0) { navCtrl.navigate("MaxTimeToLock") }
            SubPageItem(R.string.pwd_expiration_timeout, "", R.drawable.lock_clock_fill0) { navCtrl.navigate("PasswordTimeout") }
            SubPageItem(R.string.max_pwd_fail, "", R.drawable.no_encryption_fill0) { navCtrl.navigate("MaxPasswordFail") }
        }
        if(context.isDeviceAdmin){
            SubPageItem(R.string.pwd_history, "", R.drawable.history_fill0) { navCtrl.navigate("PasswordHistoryLength") }
        }
        if(VERSION.SDK_INT >= 26 && (context.isDeviceOwner || context.isProfileOwner)) {
            SubPageItem(R.string.required_strong_auth_timeout, "", R.drawable.fingerprint_off_fill0) { navCtrl.navigate("RequiredStrongAuthTimeout") }
        }
        if(context.isDeviceOwner || context.isProfileOwner) {
            SubPageItem(R.string.required_password_quality, "", R.drawable.password_fill0) { navCtrl.navigate("RequirePasswordQuality") }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun PasswordInfo() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.password_info), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 29) {
            val passwordComplexity = mapOf(
                PASSWORD_COMPLEXITY_NONE to stringResource(R.string.password_complexity_none),
                PASSWORD_COMPLEXITY_LOW to stringResource(R.string.password_complexity_low),
                PASSWORD_COMPLEXITY_MEDIUM to stringResource(R.string.password_complexity_medium),
                PASSWORD_COMPLEXITY_HIGH to stringResource(R.string.password_complexity_high)
            )
            val pwdComplex = passwordComplexity[dpm.passwordComplexity]
            Text(text = stringResource(R.string.current_password_complexity_is, pwdComplex?:stringResource(R.string.unknown)))
        }
        if(context.isDeviceOwner || context.isProfileOwner) {
            Text(stringResource(R.string.is_password_sufficient, dpm.isActivePasswordSufficient))
        }
        if(context.isDeviceAdmin) {
            val pwdFailedAttempts = dpm.currentFailedPasswordAttempts
            Text(text = stringResource(R.string.password_failed_attempts_is, pwdFailedAttempts))
        }
        if(VERSION.SDK_INT >= 28 && context.isProfileOwner && dpm.isManagedProfile(receiver)) {
            val unifiedPwd = dpm.isUsingUnifiedPassword(receiver)
            Text(stringResource(R.string.is_using_unified_password, unifiedPwd))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun ResetPasswordToken() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val tokenByteArray by remember { mutableStateOf(byteArrayOf(1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0)) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.reset_password_token), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                Toast.makeText(
                    context,
                    if(dpm.clearResetPasswordToken(receiver)) R.string.success else R.string.failed,
                    Toast.LENGTH_SHORT
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.clear))
        }
        Button(
            onClick = {
                try {
                    Toast.makeText(
                        context,
                        if(dpm.setResetPasswordToken(receiver, tokenByteArray)) R.string.success else R.string.failed,
                        Toast.LENGTH_SHORT
                    ).show()
                }catch(e:SecurityException) {
                    Toast.makeText(context, R.string.security_exception, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.set))
        }
        Button(
            onClick = {
                if(!dpm.isResetPasswordTokenActive(receiver)) {
                    try { activateToken(context) }
                    catch(e:NullPointerException) { Toast.makeText(context, R.string.please_set_a_token, Toast.LENGTH_SHORT).show() }
                } else { Toast.makeText(context, R.string.token_already_activated, Toast.LENGTH_SHORT).show() }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.activate))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Information{ Text(stringResource(R.string.activate_token_not_required_when_no_password)) }
    }
}

@Composable
private fun ResetPassword() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var newPwd by remember { mutableStateOf("") }
    val tokenByteArray by remember { mutableStateOf(byteArrayOf(1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0,1,1,4,5,1,4,1,9,1,9,8,1,0)) }
    var confirmed by remember { mutableStateOf(false) }
    var resetPwdFlag by remember { mutableIntStateOf(0) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.reset_password),style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = newPwd,
            onValueChange = { newPwd=it },
            enabled = !confirmed,
            label = { Text(stringResource(R.string.password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 3.dp))
        Text(text = stringResource(R.string.reset_pwd_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT >= 23) {
            RadioButtonItem(
                stringResource(R.string.do_not_ask_credentials_on_boot),
                resetPwdFlag == RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT,
                { resetPwdFlag = RESET_PASSWORD_DO_NOT_ASK_CREDENTIALS_ON_BOOT }
            )
        }
        RadioButtonItem(
            stringResource(R.string.reset_password_require_entry),
            resetPwdFlag == RESET_PASSWORD_REQUIRE_ENTRY,
            { resetPwdFlag = RESET_PASSWORD_REQUIRE_ENTRY }
        )
        RadioButtonItem(stringResource(R.string.none), resetPwdFlag == 0, { resetPwdFlag = 0 })
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                if(newPwd.length>=4 || newPwd.isEmpty()) { confirmed=!confirmed }
                else { Toast.makeText(context, R.string.require_4_digit_password, Toast.LENGTH_SHORT).show() }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if(confirmed) colorScheme.primary else colorScheme.error,
                contentColor = if(confirmed) colorScheme.onPrimary else colorScheme.onError 
            )
        ) {
            Text(text = stringResource(if(confirmed) R.string.cancel else R.string.confirm))
        }
        Spacer(Modifier.padding(vertical = 3.dp))
        if(VERSION.SDK_INT >= 26) {
            Button(
                onClick = {
                    val resetSuccess = dpm.resetPasswordWithToken(receiver,newPwd,tokenByteArray,resetPwdFlag)
                    if(resetSuccess) { Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show(); newPwd=""}
                    else{ Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show() }
                    confirmed=false
                },
                colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
                enabled = confirmed && (context.isDeviceOwner || context.isProfileOwner),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.reset_password_with_token))
            }
        }
        Button(
            onClick = {
                val resetSuccess = dpm.resetPassword(newPwd,resetPwdFlag)
                if(resetSuccess) { Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show(); newPwd="" }
                else{ Toast.makeText(context, R.string.failed, Toast.LENGTH_SHORT).show() }
                confirmed=false
            },
            enabled = confirmed,
            colors = ButtonDefaults.buttonColors(containerColor = colorScheme.error, contentColor = colorScheme.onError),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.reset_password_deprecated))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun PasswordComplexity() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val passwordComplexity = mapOf(
        PASSWORD_COMPLEXITY_NONE to stringResource(R.string.password_complexity_none),
        PASSWORD_COMPLEXITY_LOW to stringResource(R.string.password_complexity_low),
        PASSWORD_COMPLEXITY_MEDIUM to stringResource(R.string.password_complexity_medium),
        PASSWORD_COMPLEXITY_HIGH to stringResource(R.string.password_complexity_high)
    ).toList()
    var selectedItem by remember { mutableIntStateOf(passwordComplexity[0].first) }
    LaunchedEffect(Unit) { selectedItem = dpm.requiredPasswordComplexity }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.required_password_complexity), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(
            passwordComplexity[0].second,
            selectedItem == passwordComplexity[0].first,
            { selectedItem = passwordComplexity[0].first }
        )
        RadioButtonItem(
            passwordComplexity[1].second,
            selectedItem == passwordComplexity[1].first,
            { selectedItem = passwordComplexity[1].first }
        )
        RadioButtonItem(
            passwordComplexity[2].second,
            selectedItem == passwordComplexity[2].first,
            { selectedItem = passwordComplexity[2].first }
        )
        RadioButtonItem(
            passwordComplexity[3].second,
            selectedItem == passwordComplexity[3].first,
            { selectedItem = passwordComplexity[3].first }
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        
        Button(
            onClick = {
                dpm.requiredPasswordComplexity = selectedItem
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { context.startActivity(Intent(ACTION_SET_NEW_PASSWORD)) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.require_set_new_password))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun ScreenTimeout() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var inputContent by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { inputContent = dpm.getMaximumTimeToLock(receiver).toString() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.max_time_to_lock), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text= stringResource(R.string.max_time_to_lock_desc),modifier=Modifier.padding(vertical = 2.dp))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputContent,
            label = { Text(stringResource(R.string.time_unit_ms)) },
            onValueChange = { inputContent = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { focusMgr.clearFocus(); dpm.setMaximumTimeToLock(receiver, inputContent.toLong()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputContent != ""
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun RequiredStrongAuthTimeout() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var input by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { input = dpm.getRequiredStrongAuthTimeout(receiver).toString() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.required_strong_auth_timeout), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = input,
            label = { Text(stringResource(R.string.time_unit_ms)) },
            onValueChange = { input = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { focusMgr.clearFocus(); dpm.setRequiredStrongAuthTimeout(receiver, input.toLong()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = input != ""
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Information { Text(stringResource(R.string.zero_means_no_control)) }
        Spacer(Modifier.padding(vertical = 60.dp))
    }
}

@Composable
private fun MaxFailedPasswordForWipe() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var inputContent by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { inputContent = dpm.getMaximumFailedPasswordsForWipe(receiver).toString() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.max_pwd_fail), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text= stringResource(R.string.max_pwd_fail_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputContent,
            label = { Text(stringResource(R.string.max_pwd_fail_textfield)) },
            onValueChange = { inputContent = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { focusMgr.clearFocus(); dpm.setMaximumFailedPasswordsForWipe(receiver,inputContent.toInt()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputContent != ""
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
private fun PasswordExpiration() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var inputContent by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { inputContent = dpm.getPasswordExpirationTimeout(receiver).toString() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.pwd_expiration_timeout), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputContent,
            label = { Text(stringResource(R.string.time_unit_ms)) },
            onValueChange = { inputContent = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { focusMgr.clearFocus() ; dpm.setPasswordExpirationTimeout(receiver,inputContent.toLong()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputContent != ""
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
private fun PasswordHistoryLength() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val focusMgr = LocalFocusManager.current
    var inputContent by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { inputContent = dpm.getPasswordHistoryLength(receiver).toString() }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.pwd_history), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text= stringResource(R.string.pwd_history_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputContent,
            label = { Text(stringResource(R.string.length)) },
            onValueChange = { inputContent = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = { focusMgr.clearFocus() ; dpm.setPasswordHistoryLength(receiver,inputContent.toInt()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = inputContent != ""
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
private fun DisableKeyguardFeatures() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    var state by remember { mutableIntStateOf(-1) }
    var shortcuts by remember { mutableStateOf(false) }
    var biometrics by remember { mutableStateOf(false) }
    var iris by remember { mutableStateOf(false) }
    var face by remember { mutableStateOf(false) }
    var remote by remember { mutableStateOf(false) }
    var fingerprint by remember { mutableStateOf(false) }
    var agents by remember { mutableStateOf(false) }
    var unredacted by remember { mutableStateOf(false) }
    var notification by remember { mutableStateOf(false) }
    var camera by remember { mutableStateOf(false) }
    var widgets by remember { mutableStateOf(false) }
    val calculateCustomFeature = {
        var calculate = dpm.getKeyguardDisabledFeatures(receiver)
        if(calculate==0) {state=0}
        else{
            if(calculate-KEYGUARD_DISABLE_SHORTCUTS_ALL >= 0 && VERSION.SDK_INT >= 34) { shortcuts=true; calculate-= KEYGUARD_DISABLE_SHORTCUTS_ALL }
            if(calculate-KEYGUARD_DISABLE_BIOMETRICS >= 0 && VERSION.SDK_INT >= 28) { biometrics=true; calculate -= KEYGUARD_DISABLE_BIOMETRICS }
            if(calculate-KEYGUARD_DISABLE_IRIS >= 0 && VERSION.SDK_INT >= 28) { iris=true; calculate -= KEYGUARD_DISABLE_IRIS }
            if(calculate-KEYGUARD_DISABLE_FACE >= 0 && VERSION.SDK_INT >= 28) { face=true; calculate -= KEYGUARD_DISABLE_FACE }
            if(calculate-KEYGUARD_DISABLE_REMOTE_INPUT >= 0 && VERSION.SDK_INT >= 24) { remote=true; calculate -= KEYGUARD_DISABLE_REMOTE_INPUT }
            if(calculate-KEYGUARD_DISABLE_FINGERPRINT >= 0) { fingerprint=true; calculate -= KEYGUARD_DISABLE_FINGERPRINT }
            if(calculate-KEYGUARD_DISABLE_TRUST_AGENTS >= 0) { agents=true; calculate -= KEYGUARD_DISABLE_TRUST_AGENTS }
            if(calculate-KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS >= 0) { unredacted=true; calculate -= KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS }
            if(calculate-KEYGUARD_DISABLE_SECURE_NOTIFICATIONS >= 0) { notification=true; calculate -= KEYGUARD_DISABLE_SECURE_NOTIFICATIONS }
            if(calculate-KEYGUARD_DISABLE_SECURE_CAMERA >= 0) { camera=true; calculate -= KEYGUARD_DISABLE_SECURE_CAMERA }
            if(calculate-KEYGUARD_DISABLE_WIDGETS_ALL >= 0) { widgets=true; calculate -= KEYGUARD_DISABLE_WIDGETS_ALL }
        }
    }
    if(state==-1) {
        state = when(dpm.getKeyguardDisabledFeatures(receiver)) {
            KEYGUARD_DISABLE_FEATURES_NONE -> 0
            KEYGUARD_DISABLE_FEATURES_ALL -> 1
            else -> 2
        }
        calculateCustomFeature()
    }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.disable_keyguard_features), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(stringResource(R.string.enable_all), state == 0, { state = 0 })
        RadioButtonItem(stringResource(R.string.disable_all), state == 1, { state = 1 })
        RadioButtonItem(stringResource(R.string.custom), state == 2 , { state = 2 })
        AnimatedVisibility(state==2) {
            Column {
                CheckBoxItem(stringResource(R.string.disable_keyguard_features_widgets), widgets, { widgets = it })
                CheckBoxItem(stringResource(R.string.disable_keyguard_features_camera), camera, { camera = it })
                CheckBoxItem(stringResource(R.string.disable_keyguard_features_notification), notification, { notification = it })
                CheckBoxItem(stringResource(R.string.disable_keyguard_features_unredacted_notification), unredacted, { unredacted = it })
                CheckBoxItem(stringResource(R.string.disable_keyguard_features_trust_agents), agents, { agents = it })
                CheckBoxItem(stringResource(R.string.disable_keyguard_features_fingerprint), fingerprint, { fingerprint = it })
                if(VERSION.SDK_INT >= 24) { CheckBoxItem(stringResource(R.string.disable_keyguard_features_remote_input), remote , { remote = it }) }
                if(VERSION.SDK_INT >= 28) {
                    CheckBoxItem(stringResource(R.string.disable_keyguard_features_face), face, { face = it })
                    CheckBoxItem(stringResource(R.string.disable_keyguard_features_iris), iris, { iris = it })
                    CheckBoxItem(stringResource(R.string.disable_keyguard_features_biometrics), biometrics, { biometrics = it })
                }
                if(VERSION.SDK_INT >= 34) { CheckBoxItem(stringResource(R.string.disable_keyguard_features_shortcuts), shortcuts, { shortcuts = it }) }
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                var result = 0
                if(state==0) { result = 0 }
                else if(state==1) { result = KEYGUARD_DISABLE_FEATURES_ALL }
                else{
                    if(shortcuts && VERSION.SDK_INT >= 34) { result+=KEYGUARD_DISABLE_SHORTCUTS_ALL }
                    if(biometrics && VERSION.SDK_INT >= 28) { result+=KEYGUARD_DISABLE_BIOMETRICS }
                    if(iris && VERSION.SDK_INT >= 28) { result+=KEYGUARD_DISABLE_IRIS }
                    if(face && VERSION.SDK_INT >= 28) { result+=KEYGUARD_DISABLE_FACE }
                    if(remote && VERSION.SDK_INT >= 24) { result+=KEYGUARD_DISABLE_REMOTE_INPUT }
                    if(fingerprint) { result+=KEYGUARD_DISABLE_FINGERPRINT }
                    if(agents) { result+=KEYGUARD_DISABLE_TRUST_AGENTS }
                    if(unredacted) { result+=KEYGUARD_DISABLE_UNREDACTED_NOTIFICATIONS }
                    if(notification) { result+=KEYGUARD_DISABLE_SECURE_NOTIFICATIONS }
                    if(camera) { result+=KEYGUARD_DISABLE_SECURE_CAMERA }
                    if(widgets) { result+=KEYGUARD_DISABLE_WIDGETS_ALL }
                }
                dpm.setKeyguardDisabledFeatures(receiver,result)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
                calculateCustomFeature()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun PasswordQuality() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val passwordQuality = mapOf(
        PASSWORD_QUALITY_UNSPECIFIED to stringResource(R.string.password_quality_unspecified),
        PASSWORD_QUALITY_SOMETHING to stringResource(R.string.password_quality_something),
        PASSWORD_QUALITY_ALPHABETIC to stringResource(R.string.password_quality_alphabetic),
        PASSWORD_QUALITY_NUMERIC to stringResource(R.string.password_quality_numeric),
        PASSWORD_QUALITY_ALPHANUMERIC to stringResource(R.string.password_quality_alphanumeric),
        PASSWORD_QUALITY_BIOMETRIC_WEAK to stringResource(R.string.password_quality_biometrics_weak),
        PASSWORD_QUALITY_NUMERIC_COMPLEX to stringResource(R.string.password_quality_numeric_complex),
        PASSWORD_QUALITY_COMPLEX to stringResource(R.string.custom) + "（${stringResource(R.string.unsupported) }）",
    ).toList()
    var selectedItem by remember { mutableIntStateOf(passwordQuality[0].first) }
    LaunchedEffect(Unit) { selectedItem=dpm.getPasswordQuality(receiver) }
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.required_password_quality), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.password_complexity_instead_password_quality))
        if(VERSION.SDK_INT >= 31) { Text(text = stringResource(R.string.password_quality_deprecated_desc), color = colorScheme.error) }
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(passwordQuality[0].second, selectedItem == passwordQuality[0].first, { selectedItem = passwordQuality[0].first })
        RadioButtonItem(passwordQuality[1].second, selectedItem == passwordQuality[1].first, { selectedItem = passwordQuality[1].first })
        RadioButtonItem(passwordQuality[2].second, selectedItem == passwordQuality[2].first, { selectedItem = passwordQuality[2].first })
        RadioButtonItem(passwordQuality[3].second, selectedItem == passwordQuality[3].first, { selectedItem = passwordQuality[3].first })
        RadioButtonItem(passwordQuality[4].second, selectedItem == passwordQuality[4].first, { selectedItem = passwordQuality[4].first })
        RadioButtonItem(passwordQuality[5].second, selectedItem == passwordQuality[5].first, { selectedItem = passwordQuality[5].first })
        RadioButtonItem(passwordQuality[6].second, selectedItem == passwordQuality[6].first, { selectedItem = passwordQuality[6].first })
        RadioButtonItem(passwordQuality[7].second, selectedItem == passwordQuality[7].first, { selectedItem = passwordQuality[7].first })
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                dpm.setPasswordQuality(receiver,selectedItem)
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
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
