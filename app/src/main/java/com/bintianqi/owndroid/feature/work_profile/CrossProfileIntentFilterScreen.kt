package com.bintianqi.owndroid.feature.work_profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossProfileIntentFilterScreen(
    vm: CrossProfileIntentFilterViewModel, onNavigateUp: () -> Unit, navigateToPresets: () -> Unit
) {
    val focusMgr = LocalFocusManager.current
    var action by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf(false) }
    var category by remember { mutableStateOf("") }
    var customMimeType by remember { mutableStateOf(false) }
    var mimeType by remember { mutableStateOf("") }
    var dropdown by remember { mutableStateOf(false) }
    var direction by remember { mutableIntStateOf(3) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) vm.importFilters(it)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) vm.exportFilters(it)
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.intent_filter)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    var menu by remember { mutableStateOf(false) }
                    Box {
                        IconButton({ menu = !menu }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(menu, { menu = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.presets)) },
                                {
                                    navigateToPresets()
                                    menu = false
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.list_fill0), null)
                                }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.import_str)) },
                                {
                                    importLauncher.launch(arrayOf("application/json"))
                                    menu = false
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.file_open_fill0), null)
                                }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.export)) },
                                {
                                    exportLauncher.launch("owndroid_intent_filters")
                                    menu = false
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.file_export_fill0), null)
                                }
                            )
                        }
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier
                .padding(paddingValues)
                .padding(horizontal = HorizontalPadding)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = action, onValueChange = { action = it },
                label = { Text("Action") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() }),
                modifier = Modifier.fillMaxWidth()
            )
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(customCategory, {
                    customCategory = it
                    category = ""
                })
                OutlinedTextField(
                    category, { category = it }, Modifier.fillMaxWidth(),
                    label = { Text("Category") }, enabled = customCategory
                )
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(customMimeType, {
                    customMimeType = it
                    mimeType = ""
                })
                OutlinedTextField(
                    mimeType, { mimeType = it }, Modifier.fillMaxWidth(),
                    label = { Text("MIME type") }, enabled = customMimeType
                )
            }
            ExposedDropdownMenuBox(dropdown, { dropdown = it }, Modifier.padding(vertical = 5.dp)) {
                OutlinedTextField(
                    stringResource(directionTextMap[direction]!!), {},
                    Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    label = { Text(stringResource(R.string.direction)) }, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdown) }
                )
                ExposedDropdownMenu(dropdown, { dropdown = false }) {
                    directionTextMap.forEach {
                        DropdownMenuItem({ Text(stringResource(it.value)) }, {
                            direction = it.key
                            dropdown = false
                        })
                    }
                }
            }
            Button(
                {
                    vm.addFilter(IntentFilterOptions(action, category, mimeType, direction))
                },
                Modifier.fillMaxWidth(),
                enabled = action.isNotBlank() && (!customCategory || category.isNotBlank()) &&
                        (!customMimeType || mimeType.isNotBlank())
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                {
                    vm.clearFilters()
                },
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Text(stringResource(R.string.clear_cross_profile_filters))
            }
            Notes(R.string.info_cross_profile_intent_filter)
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossProfileIntentFilterPresetsScreen(
    vm: CrossProfileIntentFilterViewModel, navigateUp: () -> Unit
) {
    var dialog by remember { mutableStateOf<IntentFilterPreset?>(null) }
    MyLazyScaffold(R.string.presets, navigateUp) {
        items(crossProfileIntentFilterPresets) {
            Row(
                Modifier.padding(start = HorizontalPadding, end = 8.dp, bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1F)) {
                    Text(stringResource(it.name))
                    Text(
                        it.action,
                        Modifier.alpha(0.7F),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                IconButton({ dialog = it }) {
                    Icon(Icons.Default.Add, null)
                }
            }
        }
    }
    if (dialog != null) {
        var direction by remember { mutableIntStateOf(3) }
        AlertDialog(
            title = {
                Text(stringResource(dialog!!.name))
            },
            text = {
                Column {
                    var dropdown by remember { mutableStateOf(false) }
                    Text(dialog!!.action)
                    ExposedDropdownMenuBox(
                        dropdown, { dropdown = it }, Modifier.padding(top = 5.dp)
                    ) {
                        OutlinedTextField(
                            stringResource(directionTextMap[direction]!!), {},
                            Modifier
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                                .fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            label = { Text(stringResource(R.string.direction)) }, readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(dropdown) }
                        )
                        ExposedDropdownMenu(dropdown, { dropdown = false }) {
                            directionTextMap.forEach {
                                DropdownMenuItem({ Text(stringResource(it.value)) }, {
                                    direction = it.key
                                    dropdown = false
                                })
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton({
                    vm.addPreset(dialog!!, direction)
                    dialog = null
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            dismissButton = {
                TextButton({ dialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            onDismissRequest = { dialog = null }
        )
    }
}
