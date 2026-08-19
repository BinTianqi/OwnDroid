package com.bintianqi.owndroid.feature.work_profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrossProfileIntentFilterScreen(
    vm: CrossProfileIntentFilterViewModel, onNavigateUp: () -> Unit, navigate: (Destination) -> Unit
) {
    val filterList by vm.filterListState.collectAsState()
    val filtersChanged by vm.filtersChangedState.collectAsState()
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        if (it != null) vm.importFilters(it)
    }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) vm.exportFilters(it)
    }
    var confirmDeleteDialog by remember { mutableStateOf(false) }
    MyLazyScaffold(R.string.intent_filter, onNavigateUp, {
        IconButton({
            navigate(Destination.AddCrossProfileIntentFilter(-1))
        }) {
            Icon(Icons.Default.Add, null)
        }
        IconButton({
            navigate(Destination.CrossProfileIntentFilterPresets)
        }) {
            Icon(Icons.AutoMirrored.Default.List, null)
        }
        if (filtersChanged) FilledIconButton(vm::applyFilters) {
            Icon(Icons.Default.Check, null)
        }
        var menu by remember { mutableStateOf(false) }
        Box {
            IconButton({ menu = !menu }) {
                Icon(Icons.Default.MoreVert, null)
            }
            DropdownMenu(menu, { menu = false }) {
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
                HorizontalDivider()
                DropdownMenuItem(
                    { Text(stringResource(R.string.delete)) },
                    {
                        confirmDeleteDialog = true
                        menu = false
                    },
                    leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                )
            }
        }
    }) {
        item {
            if (filterList.isEmpty()) {
                Text(stringResource(R.string.none), Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
            }
        }
        items(filterList, { it.id }) {
            Column(Modifier.animateItem()) {
                Row(
                    Modifier
                        .clickable {
                            navigate(Destination.AddCrossProfileIntentFilter(it.id))
                        }
                        .padding(16.dp, 4.dp, 8.dp, 4.dp),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1F)) {
                        Text(it.options.action)
                        if (it.options.category.isNotEmpty()) {
                            Text(
                                it.options.category, Modifier.alpha(0.7F),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (it.options.mimeType.isNotEmpty()) {
                            Text(
                                it.options.mimeType, Modifier.alpha(0.7F),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Text(
                            stringResource(directionTextMap[it.options.direction]!!),
                            Modifier.alpha(0.7F), style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            formatDate(it.time),
                            Modifier.alpha(0.6F), style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    IconButton({
                        vm.deleteEntry(it.id)
                    }) {
                        Icon(Icons.Outlined.Delete, null)
                    }
                }
                HorizontalDivider()
            }
        }
        item {
            Spacer(Modifier.height(BottomPadding))
        }
    }
    if (confirmDeleteDialog) AlertDialog(
        text = { Text(stringResource(R.string.delete_all_filters_confirmation)) },
        onDismissRequest = { confirmDeleteDialog = false },
        confirmButton = {
            TextButton({
                confirmDeleteDialog = false
                vm.deleteAllFilters()
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({ confirmDeleteDialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCrossProfileIntentFilterScreen(
    params: Destination.AddCrossProfileIntentFilter,
    vm: CrossProfileIntentFilterViewModel, navigateUp: () -> Unit
) {
    var action by rememberSaveable { mutableStateOf("") }
    var enableCategory by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf("") }
    var enableMimeType by rememberSaveable { mutableStateOf(false) }
    var mimeType by rememberSaveable { mutableStateOf("") }
    var dropdown by remember { mutableStateOf(false) }
    var direction by rememberSaveable { mutableIntStateOf(3) }
    LaunchedEffect(Unit) {
        if (params.editingId != -1) {
            val options = vm.filterListState.value.find { it.id == params.editingId }!!.options
            action = options.action
            enableCategory = options.category.isNotEmpty()
            category = options.category
            enableMimeType = options.mimeType.isNotEmpty()
            mimeType = options.mimeType
            direction = options.direction
        }
    }
    MyScaffold(
        if (params.editingId == -1) R.string.add_filter else R.string.edit,
        navigateUp
    ) {
        OutlinedTextField(
            action, { action = it }, Modifier.fillMaxWidth(),
            label = { Text("Action") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done
            )
        )
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(enableCategory, {
                enableCategory = it
                category = ""
            })
            OutlinedTextField(
                category, { category = it }, Modifier.fillMaxWidth(),
                label = { Text("Category") }, enabled = enableCategory
            )
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(enableMimeType, {
                enableMimeType = it
                mimeType = ""
            })
            OutlinedTextField(
                mimeType, { mimeType = it }, Modifier.fillMaxWidth(),
                label = { Text("MIME type") }, enabled = enableMimeType
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
                val options = IntentFilterOptions(action, category, mimeType, direction)
                if (params.editingId == -1) {
                    vm.addFilter(options)
                } else {
                    vm.updateFilter(params.editingId, options)
                }
                navigateUp()
            },
            Modifier.fillMaxWidth(),
            enabled = action.isNotBlank() && (!enableCategory || category.isNotBlank()) &&
                    (!enableMimeType || mimeType.isNotBlank())
        ) {
            Text(stringResource(if (params.editingId == -1) R.string.add else R.string.update))
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
                Modifier.padding(HorizontalPadding, 2.dp, 8.dp, 2.dp),
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
