package com.binbin.androidowner

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.binbin.androidowner.ui.*
import com.binbin.androidowner.ui.theme.SetDarkTheme
import com.binbin.androidowner.ui.theme.bgColor

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
        SwitchItem(
            R.string.blackTheme, stringResource(R.string.blackTheme_desc),null,
            {sharedPref.getBoolean("blackTheme",false)},{sharedPref.edit().putBoolean("blackTheme",it).apply()}
        )
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
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.about), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.app_name)+" v$verName ($verCode)")
        Text(text = stringResource(R.string.about_desc))
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { shareLink(myContext, "https://github.com/BinTianqi/AndroidOwner/blob/master/Guide.md") }
                .padding(start = 8.dp, bottom = 8.dp)
        ){
            Icon(
                painter = painterResource(id = R.drawable.open_in_new),
                contentDescription = null,
                modifier = Modifier.padding(start = 6.dp, end = 10.dp)
            )
            Text(text = stringResource(R.string.user_guide), style = typography.titleLarge, modifier = Modifier.padding(bottom = 2.dp))
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable { shareLink(myContext, "https://github.com/BinTianqi/AndroidOwner") }
                .padding(start = 8.dp, bottom = 4.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.open_in_new),
                contentDescription = null,
                modifier = Modifier.padding(start = 6.dp, end = 10.dp)
            )
            Text(text = stringResource(R.string.source_code), style = typography.titleLarge, modifier = Modifier.padding(bottom = 2.dp))
        }
    }
}

fun shareLink(inputContext:Context,link:String){
    val uri = Uri.parse(link)
    val intent = Intent(Intent.ACTION_VIEW, uri)
    inputContext.startActivity(Intent.createChooser(intent, "Open in browser"),null)
}
