package com.bintianqi.owndroid

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

data class AppInfo(
    val name: String,
    val label: String,
    val icon: Drawable,
    val flags: Int
)

private fun searchInString(query: String, content: String)
    = query.split(' ').all { content.contains(it, true) }

@Serializable data class ApplicationsList(val canSwitchView: Boolean)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppChooserScreen(
    canSwitchView: Boolean, packageList: MutableStateFlow<List<AppInfo>>,
    refreshProgress: MutableStateFlow<Float>, onChoosePackage: (String?) -> Unit,
    onSwitchView: () -> Unit, onRefresh: () -> Unit
) {
    val packages by packageList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val progress by refreshProgress.collectAsStateWithLifecycle()
    var system by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var searchMode by rememberSaveable { mutableStateOf(false) }
    val filteredPackages = packages.filter {
        system == (it.flags and ApplicationInfo.FLAG_SYSTEM != 0) &&
                (query.isEmpty() || (searchInString(query, it.label) || searchInString(query, it.name)))
    }
    val focusMgr = LocalFocusManager.current
    LaunchedEffect(Unit) {
        if(packages.size <= 1) onRefresh()
    }
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
                            context.popToast(if(system) R.string.show_system_app else R.string.show_user_app)
                        }) {
                            Icon(painter = painterResource(R.drawable.filter_alt_fill0), contentDescription = null)
                        }
                        IconButton(onRefresh, enabled = progress == 1F) {
                            Icon(painter = painterResource(R.drawable.refresh_fill0), contentDescription = null)
                        }
                        if (canSwitchView) IconButton(onSwitchView) {
                            Icon(Icons.AutoMirrored.Default.List, null)
                        }
                    }
                },
                title = {
                    if(searchMode) {
                        val fr = remember { FocusRequester() }
                        LaunchedEffect(Unit) { fr.requestFocus() }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                            placeholder = { Text(stringResource(R.string.search)) },
                            trailingIcon = {
                                Icon(
                                    painter = painterResource(R.drawable.close_fill0),
                                    contentDescription = null,
                                    modifier = Modifier.clickable {
                                        focusMgr.clearFocus()
                                        query = ""
                                        searchMode = false
                                    }
                                )
                            },
                            textStyle = typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().focusRequester(fr)
                        )
                    }
                },
                navigationIcon = {
                    IconButton({ onChoosePackage(null) }) {
                        Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(MaterialTheme.colorScheme.surfaceContainer)
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.fillMaxSize().padding(paddingValues)) {
            if (progress < 1F) stickyHeader {
                LinearProgressIndicator({ progress }, Modifier.fillMaxWidth())
            }
            items(filteredPackages, { it.name }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            focusMgr.clearFocus()
                            onChoosePackage(it.name)
                        }
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
            item { Spacer(Modifier.height(BottomPadding)) }
        }
    }
}

val getInstalledAppsFlags =
    if(Build.VERSION.SDK_INT >= 24) PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.MATCH_UNINSTALLED_PACKAGES else 0
