package com.bintianqi.owndroid

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.ui.NavIcon
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PackageInfo(
    val pkgName: String,
    val label: String,
    val icon: Drawable,
    val system: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageSelector(navCtrl: NavHostController, vm: MyViewModel) {
    val context = LocalContext.current
    val pm = context.packageManager
    val flags = if(Build.VERSION.SDK_INT >= 24) PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES else 0
    val apps = pm.getInstalledApplications(flags)
    var progress by remember { mutableIntStateOf(0) }
    var show by remember { mutableStateOf(true) }
    var hideProgress by remember { mutableStateOf(true) }
    var system by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(false) }
    val scrollState = rememberLazyListState()
    val focusMgr = LocalFocusManager.current
    val co = rememberCoroutineScope()
    suspend fun getPkgList() {
        show = false
        progress = 0
        hideProgress = false
        vm.installedPackages.clear()
        for(pkg in apps) {
            vm.installedPackages += PackageInfo(
                pkg.packageName, pkg.loadLabel(pm).toString(), pkg.loadIcon(pm),
                (pkg.flags and ApplicationInfo.FLAG_SYSTEM) != 0
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
                    if(!searchMode) {
                        Icon(
                            painter = painterResource(R.drawable.search_fill0),
                            contentDescription = "search",
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { searchMode = true }
                                .padding(5.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.filter_alt_fill0),
                            contentDescription = "filter",
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable {
                                    system = !system
                                    Toast.makeText(context, if(system) R.string.show_system_app else R.string.show_user_app, Toast.LENGTH_SHORT).show()
                                }
                                .padding(5.dp)
                        )
                        Icon(
                            painter = painterResource(R.drawable.refresh_fill0),
                            contentDescription = "refresh",
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(50))
                                .clickable { co.launch { getPkgList() } }
                                .padding(5.dp)
                        )
                    }
                },
                title = {
                    if(searchMode) {
                        val fr = FocusRequester()
                        LaunchedEffect(Unit) { fr.requestFocus() }
                        OutlinedTextField(
                            value = search,
                            onValueChange = { search = it },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                            placeholder = { Text(stringResource(R.string.search)) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.close_fill0),
                                    contentDescription = "clear search",
                                    modifier = Modifier.clickable {
                                        focusMgr.clearFocus()
                                        search = ""
                                        searchMode = false
                                    }
                                )
                            },
                            textStyle = typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().focusRequester(fr)
                        )
                    } else {
                        Text(stringResource(R.string.pkg_selector))
                    }
                },
                navigationIcon = { NavIcon{ navCtrl.navigateUp() } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.background)
            )
        }
    ) { paddingValues->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding()),
            state = scrollState
        ) {
            items(1) {
                AnimatedVisibility(!hideProgress) {
                    LinearProgressIndicator(progress = { progress.toFloat()/apps.size }, modifier = Modifier.fillMaxWidth())
                }
            }
            if(show) {
                items(vm.installedPackages) {
                    if(system == it.system) {
                        if(search != "") {
                            if(it.pkgName.contains(search, ignoreCase = true) || it.label.contains(search, ignoreCase = true)) {
                                PackageItem(it, navCtrl, vm)
                            }
                        } else {
                            PackageItem(it, navCtrl, vm)
                        }
                    }
                }
                items(1) { Spacer(Modifier.padding(vertical = 30.dp)) }
            } else {
                items(1) {
                    Spacer(Modifier.padding(top = 5.dp))
                    Text(text = stringResource(R.string.loading), modifier = Modifier.alpha(0.8F))
                }
            }
        }
        LaunchedEffect(Unit) {
            if(vm.installedPackages.isEmpty()) { getPkgList() }
        }
    }
}

@Composable
private fun PackageItem(pkg: PackageInfo, navCtrl: NavHostController, vm: MyViewModel) {
    val focusMgr = LocalFocusManager.current
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable{
                focusMgr.clearFocus()
                vm.selectedPackage.value = pkg.pkgName
                navCtrl.navigateUp()
            }
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        Image(
            painter = rememberDrawablePainter(pkg.icon), contentDescription = "App icon",
            modifier = Modifier.padding(start = 12.dp, end = 18.dp).size(40.dp)
        )
        Column {
            Text(text = pkg.label, style = typography.titleLarge)
            Text(text = pkg.pkgName, modifier = Modifier.alpha(0.8F))
        }
    }
}
