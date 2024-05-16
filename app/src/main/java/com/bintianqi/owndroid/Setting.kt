package com.bintianqi.owndroid

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build.VERSION
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.ui.Animations
import com.bintianqi.owndroid.ui.SubPageItem
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.TopBar

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
            composable(route = "Home"){Home(localNavCtrl)}
            composable(route = "Settings"){Settings(materialYou, blackTheme)}
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
private fun Settings(materialYou:MutableState<Boolean>, blackTheme:MutableState<Boolean>){
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
private fun About(){
    val context = LocalContext.current
    val pkgInfo = context.packageManager.getPackageInfo(context.packageName,0)
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
        SubPageItem(R.string.user_guide,"",R.drawable.open_in_new){shareLink(context, "https://github.com/BinTianqi/AndroidOwner/blob/master/Guide.md")}
        SubPageItem(R.string.source_code,"",R.drawable.open_in_new){shareLink(context, "https://github.com/BinTianqi/AndroidOwner")}
    }
}

fun shareLink(inputContext:Context,link:String){
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Open in browser"),null)
}
