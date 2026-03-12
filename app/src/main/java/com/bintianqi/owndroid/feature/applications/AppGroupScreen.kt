package com.bintianqi.owndroid.feature.applications

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.PackageNameTextField
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.parsePackageNames
import kotlinx.coroutines.channels.Channel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppGroupsScreen(
    vm: AppGroupViewModel, navigateToEditScreen: () -> Unit, navigateUp: () -> Unit
) {
    val groups by vm.appGroupsState.collectAsStateWithLifecycle()
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) vm.exportGroups(it)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) vm.importGroups(it)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.app_group)) },
                navigationIcon = { NavIcon(navigateUp) },
                actions = {
                    var dropdown by remember { mutableStateOf(false) }
                    Box {
                        IconButton({
                            dropdown = true
                        }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(dropdown, { dropdown = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.export)) },
                                {
                                    exportLauncher.launch("owndroid_app_groups")
                                    dropdown = false
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.file_export_fill0), null)
                                }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.import_str)) },
                                {
                                    importLauncher.launch(arrayOf("application/json"))
                                    dropdown = false
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.file_open_fill0), null)
                                }
                            )
                        }
                    }

                }
            )
        },
        floatingActionButton = {
            FloatingActionButton({
                vm.selectAppGroup(-1)
                navigateToEditScreen()
            }) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            itemsIndexed(groups, { _, it -> it.id }) { index, it ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.selectAppGroup(index)
                            navigateToEditScreen()
                        }
                        .padding(HorizontalPadding, 8.dp)
                ) {
                    Text(it.name)
                    Text(
                        it.apps.size.toString() + " apps", Modifier.alpha(0.7F),
                        style = typography.bodyMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditAppGroupScreen(
    vm: AppGroupViewModel, navigateUp: () -> Unit,
    onChoosePackage: () -> Unit, chosenPackage: Channel<String>
) {
    val uiState by vm.editorUiState.collectAsState()
    var input by rememberSaveable { mutableStateOf("") }
    val inputPackages = parsePackageNames(input)
    LaunchedEffect(Unit) {
        parsePackageNames(chosenPackage.receive()).forEach {
            vm.setGroupApp(it, true)
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                {},
                navigationIcon = { NavIcon(navigateUp) },
                actions = {
                    if (uiState.id != null) IconButton({
                        vm.deleteGroup()
                        navigateUp()
                    }) {
                        Icon(Icons.Outlined.Delete, null)
                    }
                    FilledIconButton(
                        {
                            vm.setGroup()
                            navigateUp()
                        },
                        enabled = uiState.name.isNotBlank() && uiState.apps.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, null)
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            item {
                OutlinedTextField(
                    uiState.name, vm::setGroupName,
                    Modifier
                        .fillMaxWidth()
                        .padding(HorizontalPadding, 8.dp),
                    label = { Text(stringResource(R.string.name)) }
                )
            }
            items(uiState.apps, { it.name }) {
                ApplicationItem(it) {
                    vm.setGroupApp(it.name, false)
                }
            }
            item {
                PackageNameTextField(input, onChoosePackage,
                    Modifier.padding(HorizontalPadding, 8.dp)) { input = it }
                Button(
                    {
                        inputPackages.forEach {
                            vm.setGroupApp(it, true)
                        }
                        input = ""
                    },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPadding)
                        .padding(bottom = 10.dp),
                    inputPackages.all { pkg -> pkg !in uiState.apps.map { it.name } }
                ) {
                    Text(stringResource(R.string.add))
                }
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}
