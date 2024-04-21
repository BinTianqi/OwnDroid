package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.ui.*
import com.bintianqi.owndroid.ui.theme.SetDarkTheme
import com.bintianqi.owndroid.ui.theme.bgColor

@Composable
fun AppSetting(navCtrl:NavHostController){
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
            enterTransition = Animations().navHostEnterTransition,
            exitTransition = Animations().navHostExitTransition,
            popEnterTransition = Animations().navHostPopEnterTransition,
            popExitTransition = Animations().navHostPopExitTransition,
            modifier = Modifier.background(bgColor).padding(top = it.calculateTopPadding())
        ){
            composable(route = "Home"){Home(localNavCtrl)}
            composable(route = "Settings"){Settings()}
            composable(route = "About"){About()}
        }
    }
}

@Composable
private fun Home(navCtrl: NavHostController){
    Column(modifier = Modifier.fillMaxSize()){
        SubPageItem(R.string.setting,"",R.drawable.settings_fill0){navCtrl.navigate("Settings")}
        SubPageItem(R.string.about,"",R.drawable.info_fill0){navCtrl.navigate("About")}
    }
}

@Composable
private fun Settings(){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    SetDarkTheme()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SwitchItem(
            R.string.dynamic_color, stringResource(R.string.dynamic_color_desc),null,
            {sharedPref.getBoolean("dynamicColor",false)},{sharedPref.edit().putBoolean("dynamicColor",it).apply()}
        )
        if(colorScheme.background.toArgb()!=Color(0xFF000000).toArgb()){
            SwitchItem(
                R.string.blackTheme, stringResource(R.string.blackTheme_desc),null,
                {sharedPref.getBoolean("blackTheme",false)},{sharedPref.edit().putBoolean("blackTheme",it).apply()}
            )
        }
        Box(modifier = Modifier.padding(10.dp)){
            Information {
                Text(text = stringResource(R.string.need_relaunch))
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
