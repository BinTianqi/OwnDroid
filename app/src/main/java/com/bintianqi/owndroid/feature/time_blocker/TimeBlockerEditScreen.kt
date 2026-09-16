package com.bintianqi.owndroid.feature.time_blocker

import android.content.pm.PackageManager
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.google.accompanist.drawablepainter.rememberDrawablePainter
import kotlinx.coroutines.channels.Channel

private val DAY_NAME_RES = listOf(
    R.string.time_blocker_mon, R.string.time_blocker_tue,
    R.string.time_blocker_wed, R.string.time_blocker_thu,
    R.string.time_blocker_fri, R.string.time_blocker_sat,
    R.string.time_blocker_sun
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TimeBlockerEditScreen(
    ruleId: Int,
    vm: TimeBlockerViewModel,
    chosenPackage: Channel<String>,
    chooseSinglePackage: () -> Unit,
    onNavigateUp: () -> Unit
) {
    val rules by vm.rulesState.collectAsState()
    val existingRule = if (ruleId >= 0) rules.find { it.id == ruleId } else null

    // Wait for rules to load when editing
    if (ruleId >= 0 && existingRule == null && rules.isEmpty()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.time_blocker_edit_rule)) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                )
            },
            contentWindowInsets = adaptiveInsets()
        ) { padding ->
            Column(
                Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }
        return
    }

    val isNew = existingRule == null

    var packageName by rememberSaveable { mutableStateOf(existingRule?.packageName ?: "") }
    var dailyLimitText by rememberSaveable {
        mutableStateOf(
            if ((existingRule?.dailyLimitMinutes ?: 0) > 0) existingRule!!.dailyLimitMinutes.toString() else ""
        )
    }
    val blockedWindows = remember {
        mutableStateListOf<TimeWindow>().apply { existingRule?.blockedWindows?.let { addAll(it) } }
    }
    val allowedWindows = remember {
        mutableStateListOf<TimeWindow>().apply { existingRule?.allowedWindows?.let { addAll(it) } }
    }
    var windowListTarget by rememberSaveable { mutableIntStateOf(0) }  // 0 = blocked, 1 = allowed
    var windowEditIndex by rememberSaveable { mutableIntStateOf(-1) }  // -1 = adding new
    var showStartPicker by rememberSaveable { mutableStateOf(false) }
    var showEndPicker by rememberSaveable { mutableStateOf(false) }
    var pendingStartMinutes by rememberSaveable { mutableIntStateOf(0) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    var showDaysDialog by rememberSaveable { mutableStateOf(false) }
    var showDiscardConfirm by rememberSaveable { mutableStateOf(false) }

    fun targetList(): androidx.compose.runtime.snapshots.SnapshotStateList<TimeWindow> =
        if (windowListTarget == 0) blockedWindows else allowedWindows

    fun isDirty(): Boolean {
        return if (isNew) {
            packageName.isNotEmpty() || dailyLimitText.isNotEmpty() ||
                    blockedWindows.isNotEmpty() || allowedWindows.isNotEmpty()
        } else {
            val r = existingRule
            packageName != r.packageName ||
                    dailyLimitText != (if (r.dailyLimitMinutes > 0) r.dailyLimitMinutes.toString() else "") ||
                    blockedWindows.toList() != r.blockedWindows ||
                    allowedWindows.toList() != r.allowedWindows
        }
    }

    fun onBackRequested() {
        if (isDirty()) showDiscardConfirm = true else onNavigateUp()
    }

    BackHandler(onBack = ::onBackRequested)

    // Receive chosen package from AppChooser
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (isNew) R.string.time_blocker_add_rule else R.string.time_blocker_edit_rule))
                },
                navigationIcon = {
                    IconButton(onClick = ::onBackRequested) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    if (!isNew) {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.Delete, stringResource(R.string.time_blocker_delete_rule))
                        }
                    }
                    TextButton(onClick = {
                        val limit = dailyLimitText.toIntOrNull() ?: 0
                        val rule = BlockRule(
                            id = existingRule?.id ?: 0,
                            packageName = packageName,
                            dailyLimitMinutes = limit,
                            blockedWindows = blockedWindows.toList(),
                            allowedWindows = allowedWindows.toList(),
                            enabled = existingRule?.enabled ?: true
                        )
                        if (isNew) vm.addRule(rule) else vm.updateRule(rule)
                        onNavigateUp()
                    }, enabled = packageName.isNotEmpty()) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // App selection
            AppSelectionCard(
                packageName = packageName,
                onClick = chooseSinglePackage
            )

            Spacer(Modifier.height(16.dp))

            // Daily limit
            OutlinedTextField(
                value = dailyLimitText,
                onValueChange = { dailyLimitText = it.filter { c -> c.isDigit() } },
                label = { Text(stringResource(R.string.time_blocker_daily_limit)) },
                suffix = { Text("min") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(R.string.time_blocker_windows_hint),
                Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                style = MaterialTheme.typography.bodySmall,
                color = colorScheme.onSurfaceVariant
            )

            WindowSection(
                titleRes = R.string.time_blocker_blocked_windows,
                windows = blockedWindows,
                accent = colorScheme.errorContainer,
                onAccent = colorScheme.onErrorContainer,
                headerIcon = R.drawable.block_fill0,
                addLabelRes = R.string.time_blocker_add_blocked_window,
                onAdd = { windowListTarget = 0; windowEditIndex = -1; showStartPicker = true },
                onEditTime = { windowListTarget = 0; windowEditIndex = it; showStartPicker = true },
                onEditDays = { windowListTarget = 0; windowEditIndex = it; showDaysDialog = true },
                onRemove = { blockedWindows.removeAt(it) }
            )

            WindowSection(
                titleRes = R.string.time_blocker_allowed_windows,
                windows = allowedWindows,
                accent = colorScheme.primaryContainer,
                onAccent = colorScheme.onPrimaryContainer,
                headerIcon = R.drawable.check_circle_fill0,
                addLabelRes = R.string.time_blocker_add_allowed_window,
                onAdd = { windowListTarget = 1; windowEditIndex = -1; showStartPicker = true },
                onEditTime = { windowListTarget = 1; windowEditIndex = it; showStartPicker = true },
                onEditDays = { windowListTarget = 1; windowEditIndex = it; showDaysDialog = true },
                onRemove = { allowedWindows.removeAt(it) }
            )

            Spacer(Modifier.height(BottomPadding))
        }
    }

    // Time picker for start time
    if (showStartPicker) {
        val editing = windowEditIndex in targetList().indices
        val initial = if (editing) targetList()[windowEditIndex].startMinutes else 0
        val tps = rememberTimePickerState(initialHour = initial / 60, initialMinute = initial % 60)
        AlertDialog(
            onDismissRequest = { showStartPicker = false },
            title = { Text("Start") },
            text = { TimePicker(tps) },
            confirmButton = {
                TextButton(onClick = {
                    pendingStartMinutes = tps.hour * 60 + tps.minute
                    showStartPicker = false
                    showEndPicker = true
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showStartPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Time picker for end time
    if (showEndPicker) {
        val editing = windowEditIndex in targetList().indices
        val initial = if (editing) targetList()[windowEditIndex].endMinutes else 0
        val tpe = rememberTimePickerState(initialHour = initial / 60, initialMinute = initial % 60)
        AlertDialog(
            onDismissRequest = { showEndPicker = false },
            title = { Text("End") },
            text = { TimePicker(tpe) },
            confirmButton = {
                TextButton(onClick = {
                    val endMinutes = tpe.hour * 60 + tpe.minute
                    if (windowEditIndex in targetList().indices) {
                        targetList()[windowEditIndex] = targetList()[windowEditIndex].copy(
                            startMinutes = pendingStartMinutes, endMinutes = endMinutes
                        )
                    } else {
                        targetList() += TimeWindow(pendingStartMinutes, endMinutes)
                    }
                    showEndPicker = false
                    windowEditIndex = -1
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showEndPicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Days of week dialog
    if (showDaysDialog && windowEditIndex in targetList().indices) {
        val window = targetList()[windowEditIndex]
        val selectedDays = remember { mutableStateListOf<Int>().apply { addAll(window.daysOfWeek) } }
        AlertDialog(
            onDismissRequest = { showDaysDialog = false },
            title = { Text(stringResource(R.string.time_blocker_days)) },
            text = {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    (1..7).forEach { day ->
                        FilterChip(
                            selected = day in selectedDays,
                            onClick = {
                                if (day in selectedDays) selectedDays.remove(day)
                                else selectedDays.add(day)
                            },
                            label = { Text(stringResource(DAY_NAME_RES[day - 1])) }
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    targetList()[windowEditIndex] = window.copy(daysOfWeek = selectedDays.toSet())
                    showDaysDialog = false
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDaysDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Discard unsaved changes confirmation
    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text(stringResource(R.string.time_blocker_discard_changes_title)) },
            text = { Text(stringResource(R.string.time_blocker_discard_changes_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDiscardConfirm = false
                    onNavigateUp()
                }) { Text(stringResource(R.string.discard)) }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete confirmation
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.time_blocker_delete_rule)) },
            confirmButton = {
                TextButton(onClick = {
                    vm.deleteRule(existingRule!!.id)
                    showDeleteConfirm = false
                    onNavigateUp()
                }) { Text(stringResource(R.string.confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun AppSelectionCard(packageName: String, onClick: () -> Unit) {
    val pm = LocalContext.current.packageManager
    val (appLabel, appIcon) = remember(packageName) {
        if (packageName.isEmpty()) null to null
        else {
            val info = try {
                pm.getApplicationInfo(packageName, 0)
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
            info?.loadLabel(pm)?.toString() to info?.loadIcon(pm)
        }
    }

    OutlinedCard(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appIcon != null) {
                Image(
                    rememberDrawablePainter(appIcon), null,
                    Modifier.size(40.dp)
                )
            } else {
                Icon(
                    painterResource(R.drawable.apps_fill0), null,
                    Modifier.size(40.dp),
                    tint = colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    appLabel ?: stringResource(R.string.time_blocker_select_application),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    packageName.ifEmpty { stringResource(R.string.time_blocker_tap_to_choose) },
                    style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, null,
                tint = colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WindowSection(
    @StringRes titleRes: Int,
    windows: List<TimeWindow>,
    accent: Color,
    onAccent: Color,
    @DrawableRes headerIcon: Int,
    @StringRes addLabelRes: Int,
    onAdd: () -> Unit,
    onEditTime: (Int) -> Unit,
    onEditDays: (Int) -> Unit,
    onRemove: (Int) -> Unit
) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        colors = CardDefaults.cardColors(containerColor = accent)
    ) {
        // Header
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
            Arrangement.SpaceBetween, Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painterResource(headerIcon), null,
                    Modifier.size(20.dp),
                    tint = onAccent
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    stringResource(titleRes),
                    style = MaterialTheme.typography.titleMedium,
                    color = onAccent
                )
            }
            IconButton(onClick = onAdd) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(addLabelRes),
                    tint = onAccent
                )
            }
        }

        // Windows
        windows.forEachIndexed { index, window ->
            if (index > 0) {
                HorizontalDivider(
                    Modifier.padding(horizontal = 12.dp),
                    color = onAccent.copy(alpha = 0.2f)
                )
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth(),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(
                            R.string.time_blocker_window_format,
                            formatMinutes(window.startMinutes),
                            formatMinutes(window.endMinutes)
                        ),
                        Modifier.clickable { onEditTime(index) },
                        style = MaterialTheme.typography.titleMedium,
                        color = onAccent
                    )
                    Row {
                        IconButton(onClick = { onEditDays(index) }) {
                            Icon(
                                painterResource(R.drawable.calendar_month_fill0),
                                contentDescription = stringResource(R.string.time_blocker_days),
                                tint = onAccent
                            )
                        }
                        IconButton(onClick = { onRemove(index) }) {
                            Icon(
                                painterResource(R.drawable.delete_fill0),
                                contentDescription = stringResource(R.string.remove),
                                tint = onAccent
                            )
                        }
                    }
                }
                FlowRow(
                    Modifier.clickable { onEditDays(index) },
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    window.daysOfWeek.sorted().forEach { day ->
                        CompactDayChip(DAY_NAME_RES[day - 1])
                    }
                }
            }
        }

        if (windows.isEmpty()) {
            Text(
                stringResource(R.string.time_blocker_no_windows),
                Modifier
                    .padding(horizontal = 12.dp)
                    .padding(bottom = 12.dp),
                style = MaterialTheme.typography.bodySmall,
                color = onAccent.copy(alpha = 0.7f)
            )
        } else {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CompactDayChip(@StringRes labelRes: Int) {
    Surface(
        shape = CircleShape,
        color = colorScheme.surface,
        contentColor = colorScheme.onSurface
    ) {
        Text(
            stringResource(labelRes),
            Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall
        )
    }
}
