package com.bintianqi.owndroid.feature.applications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.searchInString
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManagedConfigurationScreen(
    vm: ManagedConfigurationViewModel, navigateUp: () -> Unit
) {
    val restrictions by vm.restrictionsState.collectAsStateWithLifecycle()
    var searchMode by rememberSaveable { mutableStateOf(false) }
    var searchKeyword by rememberSaveable { mutableStateOf("") }
    val displayRestrictions = if (!searchMode || searchKeyword.isBlank()) {
        restrictions
    } else {
        restrictions.filter {
            searchInString(searchKeyword, it.key) || it.title?.contains(searchKeyword, true) ?: true
        }
    }
    var dialog by remember { mutableStateOf<AppRestriction?>(null) }
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
                            textStyle = typography.bodyLarge,
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
                        Text(stringResource(R.string.managed_configuration))
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
                        IconButton({
                            clearRestrictionDialog = true
                        }) {
                            Icon(Icons.Outlined.Delete, null)
                        }
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            items(displayRestrictions, { it.key }) { entry ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            dialog = entry
                        }
                        .padding(HorizontalPadding, 8.dp)
                        .animateItem(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val iconId = when (entry) {
                        is AppRestriction.IntItem -> R.drawable.number_123_fill0
                        is AppRestriction.StringItem -> R.drawable.abc_fill0
                        is AppRestriction.BooleanItem -> R.drawable.toggle_off_fill0
                        is AppRestriction.ChoiceItem -> R.drawable.radio_button_checked_fill0
                        is AppRestriction.MultiSelectItem -> R.drawable.check_box_fill0
                    }
                    Icon(painterResource(iconId), null, Modifier.padding(end = 12.dp))
                    Column {
                        if (entry.title != null) {
                            Text(entry.title!!, style = typography.labelLarge)
                            Text(entry.key, style = typography.bodyMedium)
                        } else {
                            Text(entry.key, style = typography.labelLarge)
                        }
                        val text = when (entry) {
                            is AppRestriction.IntItem -> entry.value?.toString()
                            is AppRestriction.StringItem -> entry.value?.take(30)
                            is AppRestriction.BooleanItem -> entry.value?.toString()
                            is AppRestriction.ChoiceItem -> entry.value
                            is AppRestriction.MultiSelectItem -> entry.value?.joinToString(
                                limit = 30
                            )
                        }
                        Text(
                            text ?: "null", Modifier.alpha(0.7F),
                            fontStyle = if (text == null) FontStyle.Italic else null,
                            style = typography.bodyMedium
                        )
                    }
                }
            }
            item {
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
    if (dialog != null) Dialog({
        dialog = null
    }) {
        Surface(
            color = AlertDialogDefaults.containerColor,
            shape = AlertDialogDefaults.shape,
            tonalElevation = AlertDialogDefaults.TonalElevation,
        ) {
            ManagedConfigurationDialogContent(dialog!!) {
                if (it != null) {
                    vm.setRestriction(it)
                }
                dialog = null
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

@Composable
fun ManagedConfigurationDialogContent(
    restriction: AppRestriction, setRestriction: (AppRestriction?) -> Unit
) {
    var specifyValue by remember { mutableStateOf(false) }
    var input by remember { mutableStateOf("") }
    var inputState by remember { mutableStateOf(false) }
    val multiSelectList = remember {
        mutableStateListOf(
            *(if (restriction is AppRestriction.MultiSelectItem) {
                restriction.entryValues.mapIndexed { index, value ->
                    MultiSelectEntry(
                        value, restriction.entries.getOrNull(index),
                        restriction.value?.contains(value) ?: false
                    )
                }.sortedBy { entry ->
                    val index = restriction.value?.indexOf(entry.value)
                    if (index == null || index == -1) Int.MAX_VALUE else index
                }
            } else emptyList()).toTypedArray()
        )
    }
    LaunchedEffect(Unit) {
        when (restriction) {
            is AppRestriction.IntItem -> restriction.value?.let {
                input = it.toString()
                specifyValue = true
            }

            is AppRestriction.StringItem -> restriction.value?.let {
                input = it
                specifyValue = true
            }

            is AppRestriction.BooleanItem -> restriction.value?.let {
                inputState = it
                specifyValue = true
            }

            is AppRestriction.ChoiceItem -> restriction.value?.let {
                input = it
                specifyValue = true
            }

            is AppRestriction.MultiSelectItem -> restriction.value?.let {
                specifyValue = true
            }
        }
    }
    val listState = rememberLazyListState()
    val reorderableListState = rememberReorderableLazyListState(listState) { from, to ->
        // `-1` because there's an `item` before items
        multiSelectList.add(from.index - 1, multiSelectList.removeAt(to.index - 1))
    }
    LazyColumn(Modifier.padding(12.dp), listState) {
        item {
            SelectionContainer {
                Column {
                    restriction.title?.let {
                        Text(it, style = typography.titleLarge)
                    }
                    Text(
                        restriction.key, Modifier.padding(vertical = 4.dp),
                        style = typography.labelLarge
                    )
                    Spacer(Modifier.height(4.dp))
                    restriction.description?.let {
                        Text(it, Modifier.alpha(0.8F), style = typography.bodyMedium)
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Text(stringResource(R.string.specify_value))
                Switch(specifyValue, { specifyValue = it })
            }
        }
        if (specifyValue) when (restriction) {
            is AppRestriction.IntItem -> item {
                OutlinedTextField(
                    input, { input = it }, Modifier.fillMaxWidth(),
                    isError = input.toIntOrNull() == null
                )
            }

            is AppRestriction.StringItem -> item {
                OutlinedTextField(
                    input, { input = it }, Modifier.fillMaxWidth()
                )
            }

            is AppRestriction.BooleanItem -> item {
                SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                    SegmentedButton(
                        inputState, { inputState = true },
                        SegmentedButtonDefaults.itemShape(0, 2)
                    ) {
                        Text("true")
                    }
                    SegmentedButton(
                        !inputState, { inputState = false },
                        SegmentedButtonDefaults.itemShape(1, 2)
                    ) {
                        Text("false")
                    }
                }
            }

            is AppRestriction.ChoiceItem -> itemsIndexed(restriction.entryValues) { index, value ->
                val label = restriction.entries.getOrNull(index)
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            input = value
                        }
                        .padding(8.dp, 4.dp)
                ) {
                    RadioButton(input == value, { input = value })
                    Spacer(Modifier.width(8.dp))
                    if (label == null) {
                        Text(value)
                    } else {
                        Column {
                            Text(label)
                            Text(value, Modifier.alpha(0.7F), style = typography.bodyMedium)
                        }
                    }
                }
            }

            is AppRestriction.MultiSelectItem -> itemsIndexed(
                multiSelectList, { _, v -> v.value }
            ) { index, entry ->
                ReorderableItem(reorderableListState, entry.value) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable {
                                val old = multiSelectList[index]
                                multiSelectList[index] = old.copy(selected = !old.selected)
                            }
                            .padding(8.dp, 4.dp),
                        Arrangement.SpaceBetween, Alignment.CenterVertically
                    ) {
                        Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(entry.selected, null)
                            Spacer(Modifier.width(8.dp))
                            if (entry.title == null) {
                                Text(entry.value)
                            } else {
                                Column {
                                    Text(entry.title)
                                    Text(
                                        entry.value, Modifier.alpha(0.7F),
                                        style = typography.bodyMedium
                                    )
                                }
                            }
                        }
                        Icon(
                            painterResource(R.drawable.drag_indicator_fill0), null,
                            Modifier.draggableHandle()
                        )
                    }
                }
            }
        }
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp), Arrangement.End
            ) {
                TextButton({
                    setRestriction(null)
                }, Modifier.padding(end = 4.dp)) {
                    Text(stringResource(R.string.cancel))
                }
                TextButton({
                    val newRestriction = when (restriction) {
                        is AppRestriction.IntItem -> restriction.copy(
                            value = if (specifyValue) input.toIntOrNull() else null
                        )

                        is AppRestriction.StringItem -> restriction.copy(
                            value = if (specifyValue) input else null
                        )

                        is AppRestriction.BooleanItem -> restriction.copy(
                            value = if (specifyValue) inputState else null
                        )

                        is AppRestriction.ChoiceItem -> restriction.copy(
                            value = if (specifyValue) input else null
                        )

                        is AppRestriction.MultiSelectItem -> restriction.copy(
                            value = if (specifyValue)
                                multiSelectList.filter { it.selected }
                                    .map { it.value }.toTypedArray()
                            else null
                        )
                    }
                    setRestriction(newRestriction)
                }) {
                    Text(stringResource(R.string.confirm))
                }
            }
        }
    }
}
