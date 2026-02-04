package com.bintianqi.owndroid

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
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

@Serializable data class ApplicationsList(val canSwitchView: Boolean, val multiSelect: Boolean)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppChooserScreen(
    params: ApplicationsList, packageList: MutableStateFlow<List<AppInfo>>,
    refreshProgress: MutableStateFlow<Float>, onChoosePackage: (String?) -> Unit,
    onSwitchView: () -> Unit, onRefresh: () -> Unit,
    setPackagesSuspend: (List<String>, Boolean) -> Unit,
    setPackagesHidden: (List<String>, Boolean) -> Unit,
) {
    val packages by packageList.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val hf = LocalHapticFeedback.current
    val progress by refreshProgress.collectAsStateWithLifecycle()
    var system by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable { mutableStateOf("") }
    var searchMode by rememberSaveable { mutableStateOf(false) }
    val filteredPackages = packages.filter {
        system == (it.flags and ApplicationInfo.FLAG_SYSTEM != 0) &&
                (query.isEmpty() || (searchInString(query, it.label) || searchInString(query, it.name)))
    }
    val selectedPackages = remember { mutableStateListOf<AppInfo>() }
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
                            Icon(painterResource(R.drawable.filter_alt_fill0), null)
                        }
                        if (selectedPackages.isEmpty()) {
                            IconButton(onRefresh, enabled = progress == 1F) {
                                Icon(Icons.Default.Refresh, null)
                            }
                            if (params.canSwitchView) IconButton(onSwitchView) {
                                Icon(Icons.AutoMirrored.Default.List, null)
                            }
                        }
                    }
                    if (selectedPackages.isNotEmpty()) {
                        if (params.canSwitchView) {
                            var dropdown by remember { mutableStateOf(false) }
                            Box {
                                IconButton({
                                    dropdown = !dropdown
                                }) {
                                    Icon(Icons.Default.MoreVert, null)
                                }
                                DropdownMenu(dropdown, { dropdown = false }) {
                                    if (Build.VERSION.SDK_INT >= 24) {
                                        DropdownMenuItem(
                                            { Text(stringResource(R.string.suspend)) },
                                            {
                                                setPackagesSuspend(selectedPackages.map { it.name }, true)
                                                dropdown = false
                                                selectedPackages.clear()
                                            },
                                            leadingIcon = {
                                                Icon(painterResource(R.drawable.block_fill0), null)
                                            }
                                        )
                                        DropdownMenuItem(
                                            { Text(stringResource(R.string.unsuspend)) },
                                            {
                                                setPackagesSuspend(selectedPackages.map { it.name }, false)
                                                dropdown = false
                                                selectedPackages.clear()
                                            },
                                            leadingIcon = {
                                                Icon(painterResource(R.drawable.enable_fill0), null)
                                            }
                                        )
                                    }
                                    DropdownMenuItem(
                                        { Text(stringResource(R.string.hide)) },
                                        {
                                            setPackagesHidden(selectedPackages.map { it.name }, true)
                                            dropdown = false
                                            selectedPackages.clear()
                                        },
                                        leadingIcon = {
                                            Icon(painterResource(R.drawable.visibility_off_fill0), null)
                                        }
                                    )
                                    DropdownMenuItem(
                                        { Text(stringResource(R.string.unhide)) },
                                        {
                                            setPackagesHidden(selectedPackages.map { it.name }, false)
                                            dropdown = false
                                            selectedPackages.clear()
                                        },
                                        leadingIcon = {
                                            Icon(painterResource(R.drawable.visibility_fill0), null)
                                        }
                                    )
                                }
                            }
                        } else {
                            FilledIconButton({
                                onChoosePackage(selectedPackages.joinToString("\n") { it.name })
                            }) {
                                Icon(Icons.Default.Check, null)
                            }
                        }
                    }
                },
                title = {
                    if (searchMode) {
                        val fr = remember { FocusRequester() }
                        LaunchedEffect(Unit) { fr.requestFocus() }
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions { focusMgr.clearFocus() },
                            placeholder = { Text(stringResource(R.string.search)) },
                            trailingIcon = {
                                IconButton({
                                    query = ""
                                    searchMode = false
                                }) {
                                    Icon(Icons.Outlined.Clear, null)
                                }
                            },
                            textStyle = typography.bodyLarge,
                            modifier = Modifier.fillMaxWidth().focusRequester(fr)
                        )
                    } else {
                        if (selectedPackages.isNotEmpty()) {
                            Text(selectedPackages.size.toString())
                        }
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
                        .combinedClickable(onLongClick = {
                            if (params.multiSelect && it !in selectedPackages) {
                                selectedPackages += it
                                hf.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }, onClick = {
                            if (selectedPackages.isEmpty()) {
                                focusMgr.clearFocus()
                                onChoosePackage(it.name)
                            } else {
                                if (it in selectedPackages) selectedPackages -= it
                                else selectedPackages += it
                            }
                        })
                        .background(
                            if (it in selectedPackages) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.background
                        )
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
