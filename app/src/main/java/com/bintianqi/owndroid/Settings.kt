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
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.SwitchItem
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.Serializable
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
fun SettingsOptionsScreen(
    getDisplayDangerousFeatures: () -> Boolean, getShortcutsEnabled: () -> Boolean,
    setDisplayDangerousFeatures: (Boolean) -> Unit, setShortcutsEnabled: (Boolean) -> Unit,
    onNavigateUp: () -> Unit
) {
    var dangerousFeatures by remember { mutableStateOf(getDisplayDangerousFeatures()) }
    var shortcuts by remember { mutableStateOf(getShortcutsEnabled()) }
    MyScaffold(R.string.options, onNavigateUp, 0.dp) {
        SwitchItem(
            R.string.show_dangerous_features, dangerousFeatures, {
                setDisplayDangerousFeatures(it)
                dangerousFeatures = it
            }, R.drawable.warning_fill0
        )
        SwitchItem(
            R.string.shortcuts, shortcuts, {
                setShortcutsEnabled(it)
                shortcuts = it
            }, R.drawable.open_in_new
        )
    }
}

@Serializable object Appearance

@Composable
fun AppearanceScreen(
    onNavigateUp: () -> Unit, currentTheme: StateFlow<ThemeSettings>,
    setTheme: (ThemeSettings) -> Unit
) {
    var darkThemeMenu by remember { mutableStateOf(false) }
    val theme by currentTheme.collectAsStateWithLifecycle()
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
                onCheckedChange = { setTheme(theme.copy(materialYou = it)) }
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
                        setTheme(theme.copy(darkTheme = -1))
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.on)) },
                    onClick = {
                        setTheme(theme.copy(darkTheme = 1))
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.off)) },
                    onClick = {
                        setTheme(theme.copy(darkTheme = 0))
                        darkThemeMenu = false
                    }
                )
            }
        }
        AnimatedVisibility(theme.darkTheme == 1 || (theme.darkTheme == -1 && isSystemInDarkTheme())) {
            SwitchItem(
                R.string.black_theme, state = theme.blackTheme,
                onCheckedChange = { setTheme(theme.copy(blackTheme = it)) }
            )
        }
    }
}

data class AppLockConfig(
    /** null means no password, empty means password already set */
    val password: String?, val biometrics: Boolean, val whenLeaving: Boolean
)

@Serializable object AppLockSettings

@Composable
fun AppLockSettingsScreen(
    getConfig: () -> AppLockConfig, setConfig: (AppLockConfig) -> Unit,
    onNavigateUp: () -> Unit
) = MyScaffold(R.string.app_lock, onNavigateUp) {
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var allowBiometrics by remember { mutableStateOf(false) }
    var lockWhenLeaving by remember { mutableStateOf(false) }
    var alreadySet by remember { mutableStateOf(false) }
    val isInputLegal = password.length !in 1..3 && (alreadySet || password.isNotBlank())
    LaunchedEffect(Unit) {
        val config = getConfig()
        password = config.password ?: ""
        allowBiometrics = config.biometrics
        lockWhenLeaving = config.whenLeaving
        alreadySet = config.password != null
    }
    OutlinedTextField(
        password, { password = it }, Modifier.fillMaxWidth().padding(vertical = 4.dp),
        label = { Text(stringResource(R.string.password)) },
        supportingText = { Text(stringResource(if(alreadySet) R.string.leave_empty_to_remain_unchanged else R.string.minimum_length_4)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next)
    )
    OutlinedTextField(
        confirmPassword, { confirmPassword = it }, Modifier.fillMaxWidth(),
        label = { Text(stringResource(R.string.confirm_password)) },
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done)
    )
    if (VERSION.SDK_INT >= 28) Row(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.allow_biometrics))
        Switch(allowBiometrics, { allowBiometrics = it })
    }
    Row(
        Modifier.fillMaxWidth().padding(bottom = 6.dp),
        Arrangement.SpaceBetween, Alignment.CenterVertically
    ) {
        Text(stringResource(R.string.lock_when_leaving))
        Switch(lockWhenLeaving, { lockWhenLeaving = it })
    }
    Button(
        onClick = {
            setConfig(AppLockConfig(password, allowBiometrics, lockWhenLeaving))
            onNavigateUp()
        },
        modifier = Modifier.fillMaxWidth(),
        enabled = isInputLegal && confirmPassword == password
    ) {
        Text(stringResource(if(alreadySet) R.string.update else R.string.set))
    }
    if (alreadySet) FilledTonalButton(
        onClick = {
            setConfig(AppLockConfig(null, false, false))
            onNavigateUp()
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(stringResource(R.string.disable))
    }
}

@Serializable object ApiSettings

@Composable
fun ApiSettings(
    getEnabled: () -> Boolean, setKey: (String) -> Unit, onNavigateUp: () -> Unit
) {
    val context = LocalContext.current
    var alreadyEnabled by remember { mutableStateOf(getEnabled()) }
    MyScaffold(R.string.api, onNavigateUp) {
        var enabled by remember { mutableStateOf(alreadyEnabled) }
        var key by remember { mutableStateOf("") }
        SwitchItem(R.string.enable, state = enabled, onCheckedChange = {
            enabled = it
        }, padding = false)
        if (enabled) {
            OutlinedTextField(
                key, { key = it }, Modifier.fillMaxWidth().padding(bottom = 4.dp),
                label = { Text(stringResource(R.string.api_key)) },
                trailingIcon = {
                    IconButton({ key = generateBase64Key(10) }) {
                        Icon(painterResource(R.drawable.casino_fill0), null)
                    }
                }
            )
        }
        Button(
            onClick = {
                setKey(if (enabled) key else "")
                alreadyEnabled = enabled
                context.showOperationResultToast(true)
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
            enabled = !enabled || key.length !in 0..7
        ) {
            Text(stringResource(R.string.apply))
        }
        if (enabled && alreadyEnabled) Notes(R.string.api_key_exist)
    }
}

@Serializable object Notifications

@Composable
fun NotificationsScreen(
    getState: () -> List<NotificationType>, setNotification: (NotificationType, Boolean) -> Unit,
    onNavigateUp: () -> Unit
) = MyScaffold(R.string.notifications, onNavigateUp, 0.dp) {
    val enabledNotifications = remember { mutableStateListOf(*getState().toTypedArray()) }
    NotificationType.entries.forEach { type ->
        SwitchItem(type.text, type in enabledNotifications, {
            setNotification(type, it)
            enabledNotifications.run { if (it) plusAssign(type) else minusAssign(type) }
        })
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
