package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.bintianqi.owndroid.ui.*

@Composable
fun AppSetting(navCtrl:NavHostController, materialYou: MutableState<Boolean>, blackTheme: MutableState<Boolean>){
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    /*val titleMap = mapOf(
        "About" to R.string.about
    )*/
    Scaffold(
        topBar = {
            /*TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.setting))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )*/
            TopBar(backStackEntry, navCtrl, localNavCtrl)
        }
    ){
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition,
            modifier = Modifier.padding(top = it.calculateTopPadding())
        ){
            composable(route = "Home"){ Home(localNavCtrl) }
            composable(route = "Theme"){ ThemeSettings(materialYou, blackTheme) }
            composable(route = "Auth"){ AuthSettings() }
            composable(route = "About"){ About() }
        }
    }
}

@Composable
private fun Home(navCtrl: NavHostController){
    Column(modifier = Modifier.fillMaxSize()){
        SubPageItem(R.string.theme,"",R.drawable.format_paint_fill0){navCtrl.navigate("Theme")}
        SubPageItem(R.string.security,"",R.drawable.lock_fill0){navCtrl.navigate("Auth")}
        SubPageItem(R.string.about,"",R.drawable.info_fill0){navCtrl.navigate("About")}
    }
}

@Composable
private fun ThemeSettings(materialYou:MutableState<Boolean>, blackTheme:MutableState<Boolean>){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        if(VERSION.SDK_INT>=31){
            SwitchItem(
                R.string.material_you_color, stringResource(R.string.dynamic_color_desc), null,
                { sharedPref.getBoolean("material_you",true) },
                {
                    sharedPref.edit().putBoolean("material_you",it).apply()
                    materialYou.value = it
                }
            )
        }
        if(isSystemInDarkTheme()){
            SwitchItem(
                R.string.amoled_black, stringResource(R.string.blackTheme_desc), null,
                { sharedPref.getBoolean("black_theme",false) },
                {
                    sharedPref.edit().putBoolean("black_theme",it).apply()
                    blackTheme.value = it
                }
            )
        }
    }
}

@Composable
private fun AuthSettings(){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    var auth by remember{ mutableStateOf(sharedPref.getBoolean("auth",false)) }
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SwitchItem(
            R.string.lock_owndroid, "", null,
            { auth },
            {
                sharedPref.edit().putBoolean("auth",it).apply()
                auth = sharedPref.getBoolean("auth",false)
            }
        )
        if(auth){
            SwitchItem(
                R.string.enable_bio_auth, "", null,
                { sharedPref.getBoolean("bio_auth",false) },
                { sharedPref.edit().putBoolean("bio_auth",it).apply() }
            )
        }
        Box(modifier = Modifier.padding(horizontal = 8.dp)){
            Information {
                Text(text = stringResource(R.string.auth_on_start))
            }
        }
    }
}

@Composable
private fun About(){
    val myContext = LocalContext.current
    val pkgInfo = myContext.packageManager.getPackageInfo(myContext.packageName,0)
    val verCode = pkgInfo.versionCode
    val verName = pkgInfo.versionName
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Column(modifier = Modifier.padding(horizontal = 8.dp)){
            Text(text = stringResource(R.string.about), style = typography.headlineLarge)
            Spacer(Modifier.padding(vertical = 5.dp))
            Text(text = stringResource(R.string.app_name)+" v$verName ($verCode)")
            Text(text = stringResource(R.string.about_desc))
            Spacer(Modifier.padding(vertical = 5.dp))
        }
        SubPageItem(R.string.user_guide,"",R.drawable.open_in_new){shareLink(myContext, "https://github.com/BinTianqi/AndroidOwner/blob/master/Guide.md")}
        SubPageItem(R.string.source_code,"",R.drawable.open_in_new){shareLink(myContext, "https://github.com/BinTianqi/AndroidOwner")}
    }
}

fun shareLink(inputContext:Context,link:String){
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Open in browser"),null)
}
