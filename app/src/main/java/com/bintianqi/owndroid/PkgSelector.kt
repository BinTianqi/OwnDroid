package com.bintianqi.owndroid

import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.ui.NavIcon
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class PkgInfo(
    val pkgName: String,
    val label: String,
    val icon: Drawable,
    val type:String
)

private val pkgs = mutableListOf<PkgInfo>()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun PackageSelector(navCtrl:NavHostController, pkgName: MutableState<String>){
    val context = LocalContext.current
    val pm = context.packageManager
    val apps = pm.getInstalledApplications(0)
    var progress by remember{mutableIntStateOf(0)}
    var show by remember{mutableStateOf(true)}
    var hideProgress by remember{mutableStateOf(true)}
    var filter by remember{mutableStateOf("data")}
    val scrollState = rememberLazyListState()
    val co = rememberCoroutineScope()
    val getPkgList: suspend ()->Unit = {
        show = false
        progress = 0
        hideProgress = false
        pkgs.clear()
        for(pkg in apps){
            val srcDir = pkg.sourceDir
            pkgs += PkgInfo(
                pkg.packageName, pkg.loadLabel(pm).toString(), pkg.loadIcon(pm),
                if(srcDir.contains("/data/")){ "data" }
                else if(
                    srcDir.contains("system/priv-app")||srcDir.contains("product/priv-app")||
                    srcDir.contains("ext/priv-app")||srcDir.contains("vendor/priv-app")
                ){"priv"}
                else if(srcDir.contains("apex")){"apex"}
                else{"system"}
            )
            withContext(Dispatchers.Main) { progress += 1 }
        }
        show = true
        delay(500)
        hideProgress = true
    }
    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    Icon(
                        painter = painterResource(R.drawable.filter_alt_fill0),
                        contentDescription = "filter",
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(50))
                            .combinedClickable(
                                onClick = {
                                    when(filter){
                                        "data"-> {
                                            filter = "system"; co.launch {scrollState.scrollToItem(0)}
                                            Toast.makeText(context, R.string.show_system_app, Toast.LENGTH_SHORT).show()
                                        }
                                        "system"-> {
                                            filter = "priv"; co.launch {scrollState.scrollToItem(0)}
                                            Toast.makeText(context, R.string.show_priv_app, Toast.LENGTH_SHORT).show()
                                        }
                                        else-> {
                                            filter = "data"; co.launch {scrollState.scrollToItem(0)}
                                            Toast.makeText(context, R.string.show_user_app, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                onLongClick = {
                                    filter = "apex"
                                    Toast.makeText(context, R.string.show_apex_app, Toast.LENGTH_SHORT).show()
                                }
                            )
                            .padding(5.dp)
                    )
                    Icon(
                        painter = painterResource(R.drawable.refresh_fill0),
                        contentDescription = "refresh",
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(50))
                            .clickable{
                                co.launch{
                                    getPkgList()
                                }
                            }
                            .padding(5.dp)
                    )
                },
                title = {
                    Text(text = stringResource(R.string.pkg_selector))
                },
                navigationIcon = { NavIcon{navCtrl.navigateUp()} },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background)
            )
        }
    ){paddingValues->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
            state = scrollState
        ){
            items(1){
                AnimatedVisibility(!hideProgress) {
                    LinearProgressIndicator(progress = {progress.toFloat()/apps.size}, modifier = Modifier.fillMaxWidth())
                }
            }
            if(show) {
                items(pkgs) {
                    if(filter==it.type){
                        PackageItem(it, navCtrl, pkgName)
                    }
                }
                items(1){Spacer(Modifier.padding(vertical = 30.dp))}
            }else{
                items(1){
                    Spacer(Modifier.padding(top = 5.dp))
                    Text(text = stringResource(R.string.loading), modifier = Modifier.alpha(0.8F))
                }
            }
        }
        LaunchedEffect(Unit) {
            if(pkgs.size==0) { getPkgList() }
        }
    }
}

@Composable
private fun PackageItem(pkg: PkgInfo, navCtrl: NavHostController, pkgName: MutableState<String>){
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable{ pkgName.value = pkg.pkgName; navCtrl.navigateUp()}
            .padding(vertical = 3.dp)
    ){
        Spacer(Modifier.padding(start = 15.dp))
        Image(
            painter = rememberDrawablePainter(pkg.icon), contentDescription = "App icon",
            modifier = Modifier.size(50.dp)
        )
        Spacer(Modifier.padding(start = 15.dp))
        Column {
            Text(text = pkg.label, style = typography.titleLarge)
            Text(text = pkg.pkgName, modifier = Modifier.alpha(0.8F))
            Spacer(Modifier.padding(top = 3.dp))
        }
    }
}
