package com.binbin.androidowner

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
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
import com.binbin.androidowner.ui.Animations
import com.binbin.androidowner.ui.NavIcon
import com.binbin.androidowner.ui.SubPageItem
import com.binbin.androidowner.ui.SwitchItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppSetting(navCtrl:NavHostController){
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val titleMap = mapOf(
        "About" to R.string.about
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.setting))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )
        }
    ){
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations().navHostEnterTransition,
            exitTransition = Animations().navHostExitTransition,
            popEnterTransition = Animations().navHostPopEnterTransition,
            popExitTransition = Animations().navHostPopExitTransition,
            modifier = Modifier
                .background(color = if(isSystemInDarkTheme()) { colorScheme.background }else{ colorScheme.primary.copy(alpha = 0.05F) })
                .padding(top = it.calculateTopPadding())
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
        SubPageItem(R.string.setting,""){navCtrl.navigate("Settings")}
        SubPageItem(R.string.about,""){navCtrl.navigate("About")}
    }
}

@Composable
private fun Settings(){
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        SwitchItem(
            R.string.dynamic_color, stringResource(R.string.dynamic_color_desc),null,
            {sharedPref.getBoolean("dynamicColor",false)},{sharedPref.edit().putBoolean("dynamicColor",it).apply()}
        )
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
