package com.bintianqi.owndroid.feature.applications

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.TopBarSearchTextField
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.SerializableSaver
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AppChooserScreen(
    params: Destination.ApplicationsList, vm: AppChooserViewModel,
    onChoosePackage: (String?) -> Unit,
) {
    val packages by vm.displayPackagesState.collectAsStateWithLifecycle()
    val hf = LocalHapticFeedback.current
    val progress by vm.displayedProgressState.collectAsStateWithLifecycle()
    var query by rememberSaveable { mutableStateOf("") }
    var searchMode by rememberSaveable { mutableStateOf(false) }
    var filter by rememberSaveable(stateSaver = SerializableSaver(AppChooserFilter.serializer())) {
        mutableStateOf(params.defaultFilter)
    }
    var filterDrawer by remember { mutableStateOf(false) }
    val filteredPackages = packages.filter {
        filterApp(it, filter, query)
    }
    var sortingOptions by rememberSaveable(
        stateSaver = SerializableSaver(AppSortingOptions.serializer())
    ) {
        mutableStateOf(AppSortingOptions())
    }
    val sortedPackages = sortAppList(filteredPackages, sortingOptions)
    val selectedPackages = remember { mutableStateListOf<AppInfo>() }
    val focusMgr = LocalFocusManager.current
    var enteredApp by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        if (packages.size <= 1) vm.refreshPackageList()
        vm.updateAppState(enteredApp)
    }
    @Composable
    fun TopBarActions() {
        if (!searchMode) IconButton({ searchMode = true }) {
            Icon(
                painterResource(R.drawable.search_fill0),
                stringResource(R.string.search)
            )
        }
        if (!searchMode) {
            IconButton({ filterDrawer = true }) {
                Icon(painterResource(R.drawable.filter_alt_fill0), null)
            }
            var sortingMenu by remember { mutableStateOf(false) }
            Box {
                IconButton({ sortingMenu = true }) {
                    Icon(painterResource(R.drawable.sort_fill0), null)
                }
                DropdownMenu(sortingMenu, { sortingMenu = false }) {
                    AppSortingMenuContent(sortingOptions) { sortingOptions = it }
                }
            }
        }
        var menuExpanded by remember { mutableStateOf(false) }
        Box {
            IconButton({
                menuExpanded = !menuExpanded
            }) {
                Icon(Icons.Default.MoreVert, null)
            }
            DropdownMenu(menuExpanded, { menuExpanded = false }) {
                if (searchMode) {
                    DropdownMenuItem(
                        { Text(stringResource(R.string.filters)) },
                        {
                            filterDrawer = true
                            menuExpanded = false
                        },
                        leadingIcon = {
                            Icon(painterResource(R.drawable.filter_alt_fill0), null)
                        }
                    )
                }
                DropdownMenuItem(
                    { Text(stringResource(R.string.refresh)) },
                    {
                        vm.refreshPackageList()
                        menuExpanded = false
                    },
                    leadingIcon = { Icon(Icons.Default.Refresh, null) }
                )
                // Though cascading menu appears in Material 3 design specs,
                // Compose doesn't support it officially.
                // I can't find out a way to lay out the submenu on the left side of the main menu.
                // So I decided to append the submenu's content to the main menu.
                if (searchMode) {
                    AppSortingMenuContent(sortingOptions) { sortingOptions = it }
                }
            }
        }
        if (selectedPackages.isNotEmpty() && params.mode == AppChooserMode.Choose) {
            FilledIconButton({
                onChoosePackage(selectedPackages.joinToString("\n") { it.name })
            }) {
                Icon(Icons.Default.Check, null)
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                actions = {
                    TopBarActions()
                },
                title = {
                    if (searchMode) {
                        TopBarSearchTextField(query, { query = it }) {
                            query = ""
                            searchMode = false
                        }
                    } else {
                        if (selectedPackages.isNotEmpty()) {
                            Text(selectedPackages.size.toString())
                        }
                    }
                },
                navigationIcon = {
                    NavIcon { onChoosePackage(null) }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (progress < 1F) stickyHeader {
                LinearProgressIndicator({ progress }, Modifier.fillMaxWidth())
            }
            item {
                if (packages.isNotEmpty() && filteredPackages.isEmpty()) {
                    Text(
                        stringResource(R.string.no_matching_apps),
                        Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                            .alpha(0.7F),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(sortedPackages, { it.info.name }) { app ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(onLongClick = {
                            if (params.mode == AppChooserMode.Choose &&
                                app.info !in selectedPackages
                            ) {
                                selectedPackages += app.info
                                hf.performHapticFeedback(HapticFeedbackType.LongPress)
                            }
                        }, onClick = {
                            if (selectedPackages.isEmpty()) {
                                focusMgr.clearFocus()
                                enteredApp = app.info.name
                                onChoosePackage(app.info.name)
                            } else {
                                if (app.info in selectedPackages) selectedPackages -= app.info
                                else selectedPackages += app.info
                            }
                        })
                        .background(
                            if (app.info in selectedPackages) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.background
                        )
                        .padding(horizontal = 8.dp, vertical = 10.dp)
                        .animateItem()
                ) {
                    Image(
                        rememberDrawablePainter(app.info.icon), null,
                        Modifier
                            .padding(start = 12.dp, end = 18.dp)
                            .size(40.dp)
                    )
                    Column {
                        Text(app.info.label, style = MaterialTheme.typography.titleLarge)
                        Text(app.info.name, Modifier.alpha(0.8F))
                    }
                }
            }
            item { Spacer(Modifier.height(BottomPadding)) }
        }
        if (filterDrawer) {
            AppChooserFilterBottomSheet(
                filter, params.defaultFilter, { filterDrawer = false }
            ) { filter = it }
        }
    }
}
