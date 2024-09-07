package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.encryption.EncryptionManager
import com.bintianqi.owndroid.ui.*

@Composable
fun AppSetting(navCtrl:NavHostController, materialYou: MutableState<Boolean>, blackTheme: MutableState<Boolean>) {
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
            composable(route = "Theme") { ThemeSettings(materialYou, blackTheme) }
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
private fun ThemeSettings(materialYou:MutableState<Boolean>, blackTheme:MutableState<Boolean>) {
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(start = 20.dp, end = 16.dp)) {
        if(VERSION.SDK_INT >= 31) {
            SwitchItem(
                R.string.material_you_color, stringResource(R.string.dynamic_color_desc), null,
                { sharedPref.getBoolean("material_you",true) },
                {
                    sharedPref.edit().putBoolean("material_you", it).apply()
                    materialYou.value = it
                }, padding = false
            )
        }
        if(isSystemInDarkTheme()) {
            SwitchItem(
                R.string.amoled_black, stringResource(R.string.blackTheme_desc), null,
                { sharedPref.getBoolean("black_theme",false) },
                {
                    sharedPref.edit().putBoolean("black_theme", it).apply()
                    blackTheme.value = it
                }, padding = false
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
    val encryptionManager = EncryptionManager()
    val sharedPref = context.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())) {
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.automation_api), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        var key by remember { mutableStateOf("") }
        TextField(
            value = key, onValueChange = { key = it }, label = { Text("Key")},
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                sharedPref.edit().putString("automation_key", key).apply()
                val (hash, salt) = encryptionManager.makeHash(key)
                sharedPref.edit().putString("automation_key", hash).putString("salt",salt).apply()
                Toast.makeText(context, R.string.success, Toast.LENGTH_SHORT).show()
            }
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
