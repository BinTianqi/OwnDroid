package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.TopBar
import java.security.SecureRandom

@Composable
fun AppSetting(navCtrl:NavHostController, vm: MyViewModel) {
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    Scaffold(
        topBar = {
            TopBar(backStackEntry, navCtrl, localNavCtrl)
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
            composable(route = "Home") { Home(localNavCtrl) }
            composable(route = "Options") { Options() }
            composable(route = "Theme") { ThemeSettings(vm) }
            composable(route = "Auth") { AuthSettings() }
            composable(route = "Automation") { Automation() }
            composable(route = "About") { About() }
        }
    }
}

@Composable
private fun Home(navCtrl: NavHostController) {
    Column(modifier = Modifier.fillMaxSize()) {
        SubPageItem(R.string.options, "", R.drawable.tune_fill0) { navCtrl.navigate("Options") }
        SubPageItem(R.string.theme, "", R.drawable.format_paint_fill0) { navCtrl.navigate("Theme") }
        SubPageItem(R.string.security, "", R.drawable.lock_fill0) { navCtrl.navigate("Auth") }
        SubPageItem(R.string.automation_api, "", R.drawable.apps_fill0) { navCtrl.navigate("Automation") }
        SubPageItem(R.string.about, "", R.drawable.info_fill0) { navCtrl.navigate("About") }
    }
}

@Composable
private fun Options() {
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 16.dp)) {
        SwitchItem(
            R.string.show_dangerous_features, "", R.drawable.warning_fill0,
            { sharedPref.getBoolean("dangerous_features", false) },
            { sharedPref.edit().putBoolean("dangerous_features", it).apply() }, padding = false
        )
    }
}

@Composable
private fun ThemeSettings(vm: MyViewModel) {
    val theme by vm.theme.collectAsStateWithLifecycle()
    var darkThemeMenu by remember { mutableStateOf(false) }
    val darkThemeTextID = when(theme.darkTheme) {
        true -> R.string.on
        false -> R.string.off
        null -> R.string.follow_system
    }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if(VERSION.SDK_INT >= 31) {
            SwitchItem(
                R.string.material_you_color, "", null,
                theme.materialYou,
                { vm.theme.value = theme.copy(materialYou = it) }
            )
        }
        Box {
            SubPageItem(R.string.dark_theme, stringResource(darkThemeTextID)) { darkThemeMenu = true }
            DropdownMenu(
                expanded = darkThemeMenu, onDismissRequest = { darkThemeMenu = false },
                offset = DpOffset(x = 30.dp, y = 0.dp)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.follow_system)) },
                    onClick = {
                        vm.theme.value = theme.copy(darkTheme = null)
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.on)) },
                    onClick = {
                        vm.theme.value = theme.copy(darkTheme = true)
                        darkThemeMenu = false
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.off)) },
                    onClick = {
                        vm.theme.value = theme.copy(darkTheme = false)
                        darkThemeMenu = false
                    }
                )
            }
        }
        AnimatedVisibility(theme.darkTheme == true || (theme.darkTheme == null && isSystemInDarkTheme())) {
            SwitchItem(
                R.string.black_theme, "", null,
                theme.blackTheme,
                { vm.theme.value = theme.copy(blackTheme = it) }
            )
        }
    }
}

@Composable
private fun AuthSettings() {
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    var auth by remember{ mutableStateOf(sharedPref.getBoolean("auth",false)) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 16.dp)) {
        SwitchItem(
            R.string.lock_owndroid, "", null, auth,
            {
                sharedPref.edit().putBoolean("auth", it).apply()
                auth = sharedPref.getBoolean("auth", false)
            }, padding = false
        )
        if(auth) {
            SwitchItem(
                R.string.enable_bio_auth, "", null,
                { sharedPref.getBoolean("bio_auth", false) },
                { sharedPref.edit().putBoolean("bio_auth", it).apply() }, padding = false
            )
            SwitchItem(
                R.string.lock_in_background, stringResource(R.string.developing), null,
                { sharedPref.getBoolean("lock_in_background", false) },
                { sharedPref.edit().putBoolean("lock_in_background", it).apply() }, padding = false
            )
        }
        SwitchItem(
            R.string.protect_storage, "", null,
            { sharedPref.getBoolean("protect_storage", false) },
            { sharedPref.edit().putBoolean("protect_storage", it).apply() }, padding = false
        )
    }
}

@Composable
private fun Automation() {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.automation_api), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        var key by remember { mutableStateOf("") }
        OutlinedTextField(
            value = key, onValueChange = { key = it }, label = { Text("Key")},
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                sharedPref.edit().putString("automation_key", key).apply()
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            },
            enabled = key.length >= 20
        ) {
            Text(stringResource(R.string.apply))
        }
        SwitchItem(
            R.string.automation_debug, "", null,
            { sharedPref.getBoolean("automation_debug", false) },
            { sharedPref.edit().putBoolean("automation_debug", it).apply() },
            padding = false
        )
    }
}

@Composable
private fun About() {
    val context = LocalContext.current
    val pkgInfo = context.packageManager.getPackageInfo(context.packageName,0)
    val verCode = pkgInfo.versionCode
    val verName = pkgInfo.versionName
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.about), style = typography.headlineLarge, modifier = Modifier.padding(start = 26.dp))
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_name)+" v$verName ($verCode)", modifier = Modifier.padding(start = 26.dp))
        Spacer(Modifier.padding(vertical = 5.dp))
        SubPageItem(R.string.user_guide, "", R.drawable.open_in_new) { shareLink(context, "https://owndroid.pages.dev") }
        SubPageItem(R.string.source_code, "", R.drawable.open_in_new) { shareLink(context, "https://github.com/BinTianqi/OwnDroid") }
    }
}

fun shareLink(inputContext:Context,link:String) {
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Open in browser"),null)
}
