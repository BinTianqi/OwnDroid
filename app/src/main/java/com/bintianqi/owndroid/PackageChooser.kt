package com.bintianqi.owndroid

import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.bintianqi.owndroid.ui.theme.OwnDroidTheme
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PackageChooserActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm by viewModels<MyViewModel>()
        if(getPackagesProgress.value < 1F) getPackages()
        setContent {
            val theme by vm.theme.collectAsStateWithLifecycle()
            OwnDroidTheme(theme) {
                val packages by installedPackages.collectAsStateWithLifecycle()
                val progress by getPackagesProgress.collectAsStateWithLifecycle()
                PackageChooserScreen(packages, progress, ::getPackages) {
                    setResult(0, Intent().putExtra("package", it))
                    finish()
                }
            }
        }
    }
    val flags = if(Build.VERSION.SDK_INT >= 24) PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES else 0
    fun getPackages() {
        installedPackages.value = emptyList()
        lifecycleScope.launch(Dispatchers.IO) {
            val pm = packageManager
            val apps = pm.getInstalledApplications(flags)
            for(pkg in apps) {
                installedPackages.update {
                    it + PackageInfo(
                        pkg.packageName, pkg.loadLabel(pm).toString(), pkg.loadIcon(pm),
                        (pkg.flags and ApplicationInfo.FLAG_SYSTEM) != 0
                    )
                }
                withContext(Dispatchers.Main) { getPackagesProgress.value = installedPackages.value.size.toFloat() / apps.size }
            }
        }
    }
    companion object {
        val installedPackages = MutableStateFlow(emptyList<PackageInfo>())
        val getPackagesProgress = MutableStateFlow(0F)
    }
}

data class PackageInfo(
    val name: String,
    val label: String,
    val icon: Drawable,
    val system: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PackageChooserScreen(
    packages: List<PackageInfo>, progress: Float, onRefresh: () -> Unit, onChoosePackage: (String?) -> Unit
) {
    val context = LocalContext.current
    var system by remember { mutableStateOf(false) }
    var search by remember { mutableStateOf("") }
    var searchMode by remember { mutableStateOf(false) }
    val filteredPackages = packages.filter {
        system == it.system &&
        (if(search.isEmpty()) true
        else it.name.contains(search, ignoreCase = true) || it.label.contains(search, ignoreCase = true))
    }
    val focusMgr = LocalFocusManager.current
    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    if(!searchMode) {
                        IconButton({ searchMode = true }) {
                            Icon(painter = painterResource(R.drawable.search_fill0), contentDescription = stringResource(R.string.search))
                        }
                        IconButton({
                            system = !system
                            Toast.makeText(context, if(system) R.string.show_system_app else R.string.show_user_app, Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(painter = painterResource(R.drawable.filter_alt_fill0), contentDescription = null)
                        }
                        IconButton(onRefresh) {
                            Icon(painter = painterResource(R.drawable.refresh_fill0), contentDescription = null)
                        }
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
                                    contentDescription = null,
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
                        Text(stringResource(R.string.package_chooser))
                    }
                },
                navigationIcon = {
                    IconButton({ onChoosePackage(null) }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.surfaceContainer)
            )
        }
    ) { paddingValues->
        LazyColumn(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())
        ) {
            stickyHeader {
                AnimatedVisibility(progress < 1F) {
                    LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                }
            }
            items(filteredPackages, { it.name }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChoosePackage(it.name) }
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .animateItem()
                ) {
                    Image(
                        painter = rememberDrawablePainter(it.icon), contentDescription = null,
                        modifier = Modifier.padding(start = 12.dp, end = 18.dp).size(40.dp)
                    )
                    Column {
                        Text(text = it.label, style = typography.titleLarge)
                        Text(text = it.name, modifier = Modifier.alpha(0.8F))
                    }
                }
            }
            item { Spacer(Modifier.padding(vertical = 30.dp)) }
        }
    }
}
