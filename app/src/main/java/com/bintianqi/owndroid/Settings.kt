package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import kotlinx.serialization.Serializable
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.system.exitProcess

@Serializable object Settings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onNavigateUp: () -> Unit, onNavigate: (Any) -> Unit) {
    val context = LocalContext.current
    val privilege by Privilege.status.collectAsStateWithLifecycle()
    val exportLogsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/plain")) {
        if(it != null) exportLogs(context, it)
    }
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    var dropdown by remember { mutableStateOf(false) }
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.settings)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                scrollBehavior = sb,
                actions = {
                    Box {
                        IconButton({ dropdown = true }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(dropdown, { dropdown = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.export_logs)) },
                                {
                                    dropdown = false
                                    val time = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
                                        .format(Date(System.currentTimeMillis()))
                                    exportLogsLauncher.launch("owndroid_log_$time")
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.description_fill0), null)
                                }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.exit)) },
                                { exitProcess(0) },
                                leadingIcon = { Icon(Icons.Default.Close, null) }
                            )
                        }
                    }
                }
            )
        },
        contentWindowInsets = WindowInsets.ime
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            FunctionItem(title = R.string.options, icon = R.drawable.tune_fill0) { onNavigate(SettingsOptions) }
            FunctionItem(title = R.string.appearance, icon = R.drawable.format_paint_fill0) { onNavigate(Appearance) }
            FunctionItem(R.string.app_lock, icon = R.drawable.lock_fill0) { onNavigate(AppLockSettings) }
            if (privilege.device || privilege.profile)
                FunctionItem(title = R.string.api, icon = R.drawable.code_fill0) { onNavigate(ApiSettings) }
            if (privilege.device && !privilege.dhizuku)
                FunctionItem(R.string.notifications, icon = R.drawable.notifications_fill0) { onNavigate(Notifications) }
            FunctionItem(title = R.string.about, icon = R.drawable.info_fill0) { onNavigate(About) }
        }
    }
}

@Serializable object SettingsOptions

@Composable
fun SettingsOptionsScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.show_dangerous_features, icon = R.drawable.warning_fill0,
            getState = { SP.displayDangerousFeatures },
            onCheckedChange = { SP.displayDangerousFeatures = it }
        )
        SwitchItem(
            R.string.shortcuts, icon = R.drawable.open_in_new,
            getState = { SP.shortcuts }, onCheckedChange = {
                SP.shortcuts = it
                ShortcutUtils.setAllShortcuts(context)
            }
        )
    }
}

@Serializable object Appearance

@Composable
fun AppearanceScreen(onNavigateUp: () -> Unit, currentTheme: ThemeSettings, onThemeChange: (ThemeSettings) -> Unit) {
    var darkThemeMenu by remember { mutableStateOf(false) }
    var theme by remember { mutableStateOf(currentTheme) }
    fun update(it: ThemeSettings) {
        theme = it
        onThemeChange(it)
    }
    val darkThemeTextID = when(theme.darkTheme) {
        1 -> R.string.on
        0 -> R.string.off
        else -> R.string.follow_system
    }
    MyScaffold(R.string.appearance, onNavigateUp, 0.dp) {
        if(VERSION.SDK_INT >= 31) {
            SwitchItem(
                R.string.material_you_color,
                state = theme.materialYou,
                onCheckedChange = { update(theme.copy(materialYou = it)) }
            )
        }
        Box {
            FunctionItem(R.string.dark_theme, stringResource(darkThemeTextID)) { darkThemeMenu = true }
            DropdownMenu(
                expanded = darkThemeMenu, onDismissRequest = { darkThemeMenu = false },
                offset = DpOffset(x = 25.dp, y = 0.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.follow_system)) },
                    onClick = {
                        update(theme.copy(darkTheme = -1))
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.on)) },
                    onClick = {
                        update(theme.copy(darkTheme = 1))
                        theme = theme.copy(darkTheme = 1)
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.off)) },
                    onClick = {
                        update(theme.copy(darkTheme = 0))
                        darkThemeMenu = false
                    }
                )
            }
        }
        AnimatedVisibility(theme.darkTheme == 1 || (theme.darkTheme == -1 && isSystemInDarkTheme())) {
            SwitchItem(
                R.string.black_theme, state = theme.blackTheme,
                onCheckedChange = { update(theme.copy(blackTheme = it)) }
            )
        }
    }
}

@Serializable object AppLockSettings

@Composable
fun AppLockSettingsScreen(onNavigateUp: () -> Unit) = MyScaffold(R.string.app_lock, onNavigateUp, 0.dp) {
    val fm = LocalFocusManager.current
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var allowBiometrics by remember { mutableStateOf(SP.biometricsUnlock) }
    var lockWhenLeaving by remember { mutableStateOf(SP.lockWhenLeaving) }
    val fr = remember { FocusRequester() }
    val alreadySet = !SP.lockPasswordHash.isNullOrEmpty()
    val isInputLegal = password.length !in 1..3 && (alreadySet || (password.isNotEmpty() && password.isNotBlank()))
    Column(Modifier
        .widthIn(max = 300.dp)
        .align(Alignment.CenterHorizontally)) {
        OutlinedTextField(
            password, { password = it }, Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            label = { Text(stringResource(R.string.password)) },
            supportingText = { Text(stringResource(if(alreadySet) R.string.leave_empty_to_remain_unchanged else R.string.minimum_length_4)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions { fr.requestFocus() }
        )
        OutlinedTextField(
            confirmPassword, { confirmPassword = it }, Modifier
                .fillMaxWidth()
                .focusRequester(fr),
            label = { Text(stringResource(R.string.confirm_password)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions { fm.clearFocus() }
        )
        if(VERSION.SDK_INT >= 28) Row(Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(stringResource(R.string.allow_biometrics))
            Switch(allowBiometrics, { allowBiometrics = it })
        }
        Row(Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text(stringResource(R.string.lock_when_leaving))
            Switch(lockWhenLeaving, { lockWhenLeaving = it })
        }
        Button(
            onClick = {
                fm.clearFocus()
                if(password.isNotEmpty()) SP.lockPasswordHash = password.hash()
                SP.biometricsUnlock = allowBiometrics
                SP.lockWhenLeaving = lockWhenLeaving
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isInputLegal && confirmPassword == password
        ) {
            Text(stringResource(if(alreadySet) R.string.update else R.string.set))
        }
        if(alreadySet) FilledTonalButton(
            onClick = {
                fm.clearFocus()
                SP.lockPasswordHash = ""
                SP.biometricsUnlock = false
                SP.lockWhenLeaving = false
                onNavigateUp()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.disable))
        }
    }
}

@Serializable object ApiSettings

@Composable
fun ApiSettings(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    MyScaffold(R.string.api, onNavigateUp) {
        var enabled by remember { mutableStateOf(SP.isApiEnabled) }
        SwitchItem(R.string.enable, state = enabled, onCheckedChange = {
            enabled = it
            SP.isApiEnabled = it
            if(!it) SP.sharedPrefs.edit { remove("api.key") }
        }, padding = false)
        if(enabled) {
            var key by remember { mutableStateOf("") }
            OutlinedTextField(
                value = key, onValueChange = { key = it }, label = { Text(stringResource(R.string.api_key)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp), readOnly = true,
                trailingIcon = {
                    IconButton(
                        onClick = {
                            val charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                            val sr = SecureRandom()
                            key = (1..20).map { charset[sr.nextInt(charset.length)] }.joinToString("")
                        }
                    ) {
                        Icon(painter = painterResource(R.drawable.casino_fill0), contentDescription = "Random")
                    }
                }
            )
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                onClick = {
                    SP.apiKey = key
                    context.showOperationResultToast(true)
                },
                enabled = key.isNotEmpty()
            ) {
                Text(stringResource(R.string.apply))
            }
            if(SP.apiKey != null) Notes(R.string.api_key_exist)
        }
    }
}

@Serializable object Notifications

@Composable
fun NotificationsScreen(onNavigateUp: () -> Unit) = MyScaffold(R.string.notifications, onNavigateUp, 0.dp) {
    val sp = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val map = mapOf(
        NotificationUtils.ID.PASSWORD_CHANGED to R.string.password_changed, NotificationUtils.ID.USER_ADDED to R.string.user_added,
        NotificationUtils.ID.USER_STARTED to R.string.user_started, NotificationUtils.ID.USER_SWITCHED to R.string.user_switched,
        NotificationUtils.ID.USER_STOPPED to R.string.user_stopped, NotificationUtils.ID.USER_REMOVED to R.string.user_removed,
        NotificationUtils.ID.BUG_REPORT_SHARED to R.string.bug_report_shared,
        NotificationUtils.ID.BUG_REPORT_SHARING_DECLINED to R.string.bug_report_sharing_declined,
        NotificationUtils.ID.BUG_REPORT_FAILED to R.string.bug_report_failed,
        NotificationUtils.ID.SYSTEM_UPDATE_PENDING to R.string.system_update_pending
    )
    map.forEach { (k, v) ->
        SwitchItem(v, getState = { sp.getBoolean("n_$k", true) }, onCheckedChange = { sp.edit(true) { putBoolean("n_$k", it) } })
    }
}

@Serializable object About

@Composable
fun AboutScreen(onNavigateUp: () -> Unit) {
    val context = LocalContext.current
    val pkgInfo = context.packageManager.getPackageInfo(context.packageName,0)
    val verCode = pkgInfo.versionCode
    val verName = pkgInfo.versionName
    MyScaffold(R.string.about, onNavigateUp, 0.dp) {
        Text(text = stringResource(R.string.app_name)+" v$verName ($verCode)", modifier = Modifier.padding(start = 16.dp))
        Spacer(Modifier.padding(vertical = 5.dp))
        FunctionItem(R.string.project_homepage, "GitHub", R.drawable.open_in_new) { shareLink(context, "https://github.com/BinTianqi/OwnDroid") }
    }
}

fun shareLink(inputContext: Context, link: String) {
    val uri = link.toUri()
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Open in browser"), null)
}
