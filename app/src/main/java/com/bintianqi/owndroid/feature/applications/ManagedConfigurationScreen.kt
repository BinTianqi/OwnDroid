package com.bintianqi.owndroid.feature.applications

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.SwitchItem
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.searchInString
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedConfigurationScreen(
    vm: ManagedConfigurationViewModel, navigateUp: () -> Unit, navigate: (Destination) -> Unit
) {
    val manifests by vm.manifestsState.collectAsState()
    val values by vm.valuesState.collectAsState()
    var searchMode by rememberSaveable { mutableStateOf(false) }
    var searchKeyword by rememberSaveable { mutableStateOf("") }
    var showModified by rememberSaveable { mutableStateOf(true) }
    var showUnmodified by rememberSaveable { mutableStateOf(true) }
    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) {
        if (it != null) vm.exportConfiguration(it)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        if (it != null) vm.importConfiguration(it)
    }
    val allIds = (manifests.map { it.key } + values.map { it.id }).distinct()
    val displayRestrictions = allIds.filter { id ->
        val manifest = manifests.find { it.key == id }
        val value = values.find { it.id == id }
        ((showModified && value != null) || (showUnmodified && value == null)) &&
                (!searchMode || searchKeyword.isBlank() ||
                        searchInString(searchKeyword, id) ||
                        searchInString(searchKeyword, manifest?.title ?: "") ||
                        searchInString(searchKeyword, manifest?.description ?: "") ||
                        searchInString(searchKeyword, value?.vString ?: "") ||
                        value?.vList?.any { searchInString(searchKeyword, it) } ?: false)
    }
    var clearRestrictionDialog by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                {
                    if (searchMode) {
                        val fr = remember { FocusRequester() }
                        LaunchedEffect(Unit) {
                            fr.requestFocus()
                        }
                        OutlinedTextField(
                            searchKeyword, { searchKeyword = it },
                            Modifier
                                .fillMaxWidth()
                                .focusRequester(fr),
                            textStyle = MaterialTheme.typography.bodyLarge,
                            placeholder = { Text(stringResource(R.string.search)) },
                            trailingIcon = {
                                IconButton({
                                    searchKeyword = ""
                                    searchMode = false
                                }) {
                                    Icon(Icons.Outlined.Clear, null)
                                }
                            },
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done)
                        )
                    } else {
                        Text(
                            stringResource(R.string.managed_configuration),
                            overflow = TextOverflow.Ellipsis, maxLines = 1
                        )
                    }
                },
                navigationIcon = { NavIcon(navigateUp) },
                actions = {
                    if (!searchMode) {
                        IconButton({
                            searchMode = true
                        }) {
                            Icon(Icons.Outlined.Search, null)
                        }
                        CreateRestrictionMenu(navigate)
                    }
                    Box {
                        var dropdownMenu by remember { mutableStateOf(false) }
                        IconButton({
                            dropdownMenu = true
                        }) {
                            Icon(Icons.Default.MoreVert, null)
                        }
                        DropdownMenu(dropdownMenu, { dropdownMenu = false }) {
                            DropdownMenuItem(
                                { Text(stringResource(R.string.modified)) },
                                { showModified = !showModified },
                                leadingIcon = { Checkbox(showModified, null) }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.unmodified)) },
                                { showUnmodified = !showUnmodified },
                                leadingIcon = { Checkbox(showUnmodified, null) }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                { Text(stringResource(R.string.export)) },
                                {
                                    exportLauncher.launch("mc_${vm.packageName}")
                                    dropdownMenu = false
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.file_export_fill0), null)
                                }
                            )
                            DropdownMenuItem(
                                { Text(stringResource(R.string.import_str)) },
                                {
                                    importLauncher.launch("application/json")
                                    dropdownMenu = false
                                },
                                leadingIcon = {
                                    Icon(painterResource(R.drawable.file_open_fill0), null)
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                { Text(stringResource(R.string.clear)) },
                                {
                                    clearRestrictionDialog = true
                                    dropdownMenu = false
                                },
                                leadingIcon = { Icon(Icons.Outlined.Delete, null) }
                            )
                        }
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            item {
                if (displayRestrictions.isEmpty()) {
                    Text(
                        stringResource(R.string.none), Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
            items(displayRestrictions, { it }) { id ->
                val manifest = manifests.find { it.key == id }
                val value = values.find { it.id == id }
                val iconId = getRestrictionIcon(manifest, value)
                val valueText = value?.run {
                    vString ?: vInt?.toString() ?: vBool?.toString() ?: vList?.joinToString()
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable { navigateToEditor(manifest, value, id, navigate) }
                        .background(
                            if (value == null) MaterialTheme.colorScheme.background
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .padding(HorizontalPadding, 8.dp)
                        .animateItem(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(painterResource(iconId), null, Modifier.padding(end = 12.dp))
                    Column {
                        if (manifest?.title != null) Text(
                            manifest.title, style = MaterialTheme.typography.labelLarge
                        )
                        Text(id, style = MaterialTheme.typography.labelMedium)
                        Text(
                            valueText ?: "null", Modifier.alpha(0.7F),
                            fontStyle = if (valueText == null) FontStyle.Italic else null,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
    if (clearRestrictionDialog) AlertDialog(
        text = {
            Text(stringResource(R.string.clear_configurations))
        },
        confirmButton = {
            TextButton({
                vm.clearRestrictions()
                clearRestrictionDialog = false
            }) {
                Text(stringResource(R.string.confirm))
            }
        },
        dismissButton = {
            TextButton({
                clearRestrictionDialog = false
            }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = {
            clearRestrictionDialog = false
        }
    )
}

private fun getRestrictionIcon(
    manifest: AppRestrictionManifest?, value: AppRestrictionValue?
): Int {
    val fallbackIcon = if (value?.vString != null) {
        R.drawable.abc_fill0
    } else if (value?.vInt != null) {
        R.drawable.number_123_fill0
    } else if (value?.vBool != null) {
        R.drawable.toggle_off_fill0
    } else if (value?.vList != null) {
        R.drawable.list_fill0
    } else {
        null
    }
    return when (manifest?.type) {
        AppRestrictionType.Int -> R.drawable.number_123_fill0
        AppRestrictionType.String -> R.drawable.abc_fill0
        AppRestrictionType.Boolean -> R.drawable.toggle_off_fill0
        AppRestrictionType.Choice -> R.drawable.radio_button_checked_fill0
        AppRestrictionType.MultiSelect -> R.drawable.check_box_fill0
        null -> null
    } ?: fallbackIcon!!
}

private fun navigateToEditor(
    manifest: AppRestrictionManifest?, value: AppRestrictionValue?,
    id: String, navigate: (Destination) -> Unit
) {
    if (
        manifest?.type == AppRestrictionType.String ||
        manifest?.type == AppRestrictionType.Choice ||
        value?.vString != null
    ) {
        navigate(Destination.ManagedConfigurationValueEditor(id, false))
    } else if (
        manifest?.type == AppRestrictionType.Int || value?.vInt != null
    ) {
        navigate(Destination.ManagedConfigurationValueEditor(id, true))
    } else if (
        manifest?.type == AppRestrictionType.Boolean ||
        value?.vBool != null
    ) {
        navigate(Destination.ManagedConfigurationBooleanEditor(id))
    } else if (
        manifest?.type == AppRestrictionType.MultiSelect ||
        value?.vList != null
    ) {
        navigate(Destination.ManagedConfigurationListEditor(id))
    }
}

@Composable
private fun CreateRestrictionMenu(navigate: (Destination) -> Unit) {
    var expand by remember { mutableStateOf(false) }
    Box {
        IconButton({ expand = true }) {
            Icon(Icons.Default.Add, null)
        }
        DropdownMenu(expand, { expand = false }) {
            DropdownMenuItem(
                { Text(stringResource(R.string.value_string)) },
                {
                    expand = false
                    navigate(Destination.ManagedConfigurationValueEditor("", false))
                },
                leadingIcon = { Icon(painterResource(R.drawable.abc_fill0), null) }
            )
            DropdownMenuItem(
                { Text(stringResource(R.string.value_int)) },
                {
                    expand = false
                    navigate(Destination.ManagedConfigurationValueEditor("", true))
                },
                leadingIcon = { Icon(painterResource(R.drawable.number_123_fill0), null) }
            )
            DropdownMenuItem(
                { Text(stringResource(R.string.value_boolean)) },
                {
                    expand = false
                    navigate(Destination.ManagedConfigurationBooleanEditor(""))
                },
                leadingIcon = { Icon(painterResource(R.drawable.toggle_off_fill0), null) }
            )
            DropdownMenuItem(
                { Text(stringResource(R.string.value_list)) },
                {
                    expand = false
                    navigate(Destination.ManagedConfigurationListEditor(""))
                },
                leadingIcon = { Icon(painterResource(R.drawable.list_fill0), null) }
            )
        }
    }
}

@Composable
fun ManagedConfigurationBooleanEditorScreen(
    vm: ManagedConfigurationViewModel, defaultId: String, navigateUp: () -> Unit
) {
    var idInput by rememberSaveable { mutableStateOf(defaultId) }
    val manifestList by vm.manifestsState.collectAsState()
    val valuesList by vm.valuesState.collectAsState()
    val manifest = manifestList.find { it.key == idInput }
    var status by rememberSaveable { mutableStateOf(true) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    // We don't check for conflict values in `vm.valuesState`,
    // because `vm.setRestriction()` will silently override the old value
    val conflictValue = manifest != null && manifest.type != AppRestrictionType.Boolean
    LaunchedEffect(Unit) {
        if (defaultId.isNotEmpty()) {
            val value = vm.valuesState.value.find { it.id == defaultId }
            if (manifest != null) enabled = value?.vBool != null
            status = value?.vBool ?: true
        }
    }
    MySmallTitleScaffold(R.string.place_holder, navigateUp, actions = {
        if (manifest == null && valuesList.find { it.id == idInput } != null) {
            IconButton({
                vm.setRestriction(AppRestrictionValue(idInput))
                navigateUp()
            }) {
                Icon(Icons.Outlined.Delete, null)
            }
        }
        FilledIconButton({
            val value = if (enabled) status else null
            vm.setRestriction(AppRestrictionValue(idInput, vBool = value))
            navigateUp()
        }, enabled = !conflictValue) {
            Icon(Icons.Default.Check, null)
        }
    }) {
        if (manifest != null) RestrictionMetadataBlock(manifest)
        OutlinedTextField(
            idInput, { idInput = it },
            Modifier.fillMaxWidth(),
            readOnly = defaultId.isNotEmpty(),
            label = { Text("id") },
            supportingText = {
                if (conflictValue) Text(
                    stringResource(R.string.restriction_value_exists),
                    color = MaterialTheme.colorScheme.error
                )
            }
        )
        if (manifest != null) SwitchItem(
            R.string.enable, icon = null, enabled, { enabled = it }, padding = false
        )
        Spacer(Modifier.height(8.dp))
        if (enabled) SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            SegmentedButton(
                status, { status = true },
                SegmentedButtonDefaults.itemShape(0, 2)
            ) {
                Text("true")
            }
            SegmentedButton(
                !status, { status = false },
                SegmentedButtonDefaults.itemShape(1, 2)
            ) {
                Text("false")
            }
        }
    }
}

@Composable
fun ManagedConfigurationValueEditorScreen(
    vm: ManagedConfigurationViewModel, defaultId: String, isInt: Boolean, navigateUp: () -> Unit
) {
    var idInput by rememberSaveable { mutableStateOf(defaultId) }
    val manifestList by vm.manifestsState.collectAsState()
    val valuesList by vm.valuesState.collectAsState()
    val manifest = manifestList.find { it.key == idInput }
    var valueInput by rememberSaveable { mutableStateOf("") }
    var enabled by rememberSaveable { mutableStateOf(true) }
    val conflictValue = manifest != null && ((isInt && manifest.type != AppRestrictionType.Int) ||
            (!isInt && (manifest.type != AppRestrictionType.String &&
                    manifest.type != AppRestrictionType.Choice)))
    LaunchedEffect(Unit) {
        if (defaultId.isNotEmpty()) {
            val value = vm.valuesState.value.find { it.id == defaultId }
            if (manifest != null) {
                enabled = if (isInt) value?.vInt != null
                else value?.vString != null
            }
            valueInput = (if (isInt) value?.vInt?.toString() else value?.vString) ?: ""
        }
    }
    MySmallTitleScaffold(R.string.place_holder, navigateUp, 0.dp, {
        if (manifest == null && valuesList.find { it.id == idInput } != null) {
            IconButton({
                vm.setRestriction(AppRestrictionValue(idInput))
                navigateUp()
            }) {
                Icon(Icons.Outlined.Delete, null)
            }
        }
        FilledIconButton({
            val value = if (enabled) valueInput else null
            val item = if (isInt) {
                AppRestrictionValue(idInput, vInt = value?.toInt())
            } else {
                AppRestrictionValue(idInput, vString = value)
            }
            vm.setRestriction(item)
            navigateUp()
        }, enabled = !conflictValue && (!enabled || !isInt || valueInput.toIntOrNull() != null)) {
            Icon(Icons.Default.Check, null)
        }
    }) {
        Column(Modifier.padding(horizontal = HorizontalPadding)) {
            if (manifest != null) RestrictionMetadataBlock(manifest)
            OutlinedTextField(
                idInput, { idInput = it },
                Modifier.fillMaxWidth(),
                readOnly = defaultId.isNotEmpty(),
                label = { Text("id") },
                supportingText = {
                    if (conflictValue) Text(
                        stringResource(R.string.restriction_value_exists),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            )
            if (manifest != null) SwitchItem(
                R.string.enable, icon = null, enabled, { enabled = it }, padding = false
            )
        }
        Spacer(Modifier.height(8.dp))
        if (enabled && manifest != null) manifest.entryValues?.forEachIndexed { index, value ->
            val title = manifest.entries?.getOrNull(index)
            Row(
                Modifier
                    .fillMaxWidth()
                    .clickable { valueInput = value }
                    .padding(HorizontalPadding, 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(value == valueInput, null)
                Column(Modifier.padding(start = 8.dp)) {
                    if (title != null) Text(title, style = MaterialTheme.typography.labelLarge)
                    Text(value, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        if (enabled) OutlinedTextField(
            valueInput, { valueInput = it },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding),
            label = { Text(stringResource(R.string.value)) },
            isError = valueInput.isNotEmpty() && isInt && valueInput.toIntOrNull() == null,
            minLines = if (isInt) 1 else 2
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedConfigurationListEditorScreen(
    vm: ManagedConfigurationViewModel, defaultId: String, navigateUp: () -> Unit
) {
    var idInput by rememberSaveable { mutableStateOf(defaultId) }
    val manifestList by vm.manifestsState.collectAsState()
    val valuesList by vm.valuesState.collectAsState()
    val manifest = manifestList.find { it.key == idInput }
    // Reorderable requires a stable `key` for each item,
    // so not using list index since it will change after reordering.
    // And `list` may contain duplicate items, so not using their content as key.
    var assignedId by rememberSaveable { mutableIntStateOf(0) }
    val list = rememberSaveable { mutableStateListOf<Pair<Int, String>>() }
    var valueInput by rememberSaveable { mutableStateOf("") }
    var enabled by rememberSaveable { mutableStateOf(true) }
    val listState = rememberLazyListState()
    val itemsBeforeList = 1 + (manifest?.entryValues?.size ?: 0)
    val reorderableListState = rememberReorderableLazyListState(listState) { from, to ->
        list.add(from.index - itemsBeforeList, list.removeAt(to.index - itemsBeforeList))
    }
    val conflictValue = manifest != null && manifest.type != AppRestrictionType.MultiSelect
    var editingItem by rememberSaveable { mutableIntStateOf(-1) }
    LaunchedEffect(Unit) {
        if (defaultId.isNotEmpty()) { // Edit an item
            val value = vm.valuesState.value.find { it.id == defaultId }
            if (value != null) { // Has a previously set value
                if (value.vList != null) {
                    list.clear()
                    value.vList.forEach {
                        list += assignedId++ to it
                    }
                }
                enabled = value.vList != null
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar({}, navigationIcon = {
                IconButton(navigateUp) {
                    Icon(Icons.AutoMirrored.Default.ArrowBack, null)
                }
            }, actions = {
                if (manifest == null && valuesList.find { it.id == idInput } != null) {
                    IconButton({
                        vm.setRestriction(AppRestrictionValue(idInput))
                        navigateUp()
                    }) {
                        Icon(Icons.Outlined.Delete, null)
                    }
                }
                FilledIconButton({
                    val value = if (enabled) list.map { it.second } else null
                    vm.setRestriction(AppRestrictionValue(idInput, vList = value))
                    navigateUp()
                }, enabled = !conflictValue) {
                    Icon(Icons.Default.Check, null)
                }
            })
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(
            Modifier.padding(paddingValues), listState
        ) {
            item {
                Column(Modifier.padding(horizontal = HorizontalPadding)) {
                    if (manifest != null) RestrictionMetadataBlock(manifest)
                    OutlinedTextField(
                        idInput, { idInput = it },
                        Modifier.fillMaxWidth(),
                        readOnly = defaultId.isNotEmpty(),
                        label = { Text("id") },
                        supportingText = {
                            if (conflictValue) Text(
                                stringResource(R.string.restriction_value_exists),
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
                if (manifest != null) SwitchItem(R.string.enable, enabled, { enabled = it })
            }
            if (enabled && manifest != null) itemsIndexed(
                manifest.entryValues ?: emptyArray()
            ) { index, value ->
                val title = manifest.entries?.getOrNull(index)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            val indexInList = list.indexOfFirst { it.second == value }
                            if (indexInList == -1) {
                                list += assignedId++ to value
                            } else {
                                list.removeAt(indexInList)
                            }
                        }
                        .padding(HorizontalPadding, 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(list.find { it.second == value } != null, null)
                    Column(Modifier.padding(start = 8.dp)) {
                        if (title != null) Text(title, style = MaterialTheme.typography.labelLarge)
                        Text(value, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            if (enabled) itemsIndexed(list, { _, v -> v.first }) { index, value ->
                ReorderableItem(reorderableListState, value.first) {
                    Row(
                        Modifier
                            .clickable { editingItem = index }
                            .padding(HorizontalPadding, 4.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Text(value.second, Modifier.weight(1F))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton({ list.removeAt(index) }) {
                                Icon(Icons.Outlined.Clear, null)
                            }
                            Icon(
                                painterResource(R.drawable.drag_indicator_fill0), null,
                                Modifier.draggableHandle()
                            )
                        }
                    }
                }
            }
            if (enabled) item {
                OutlinedTextField(
                    valueInput, { valueInput = it },
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = HorizontalPadding),
                    label = { Text(stringResource(R.string.value)) },
                    trailingIcon = {
                        IconButton({
                            list += assignedId++ to valueInput
                            valueInput = ""
                        }) {
                            Icon(Icons.Default.Add, null)
                        }
                    },
                    minLines = 2
                )
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
    if (editingItem != -1) {
        var input by rememberSaveable { mutableStateOf(list[editingItem].second) }
        AlertDialog(
            text = {
                OutlinedTextField(
                    input, { input = it },
                    Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyLarge
                )
            },
            dismissButton = {
                TextButton({ editingItem = -1 }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            confirmButton = {
                TextButton({
                    list[editingItem] = list[editingItem].copy(second = input)
                    editingItem = -1
                }) {
                    Text(stringResource(R.string.confirm))
                }
            },
            onDismissRequest = { editingItem = -1 }
        )
    }
}

@Composable
private fun RestrictionMetadataBlock(manifest: AppRestrictionManifest) {
    var needExpand by rememberSaveable { mutableStateOf(false) }
    var expand by rememberSaveable { mutableStateOf(false) }
    SelectionContainer {
        Column(Modifier.padding(top = 8.dp)) {
            if (manifest.title != null) Text(
                manifest.title, style = MaterialTheme.typography.titleLarge
            )
            if (manifest.description != null) {
                Text(
                    manifest.description,
                    Modifier
                        .alpha(0.8F)
                        .animateContentSize(),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = if (expand) Int.MAX_VALUE else 6,
                    onTextLayout = { if (!needExpand) needExpand = it.didOverflowHeight }
                )
            }
        }
    }
    if (needExpand) TextButton({
        expand = !expand
    }) {
        if (expand) {
            Icon(Icons.Default.KeyboardArrowUp, null)
            Text(stringResource(R.string.collapse))
        } else {
            Icon(Icons.Default.KeyboardArrowDown, null)
            Text(stringResource(R.string.expand))
        }
    }
    Spacer(Modifier.height(8.dp))
}
