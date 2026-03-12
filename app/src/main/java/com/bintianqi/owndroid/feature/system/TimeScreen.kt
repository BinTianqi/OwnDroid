package com.bintianqi.owndroid.feature.system

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CheckBoxItem
import com.bintianqi.owndroid.ui.FullWidthRadioButtonItem
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.clickableTextField
import com.bintianqi.owndroid.utils.formatDate
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun TimeScreen(vm: TimeViewModel, onNavigateUp: () -> Unit) {
    val pagerState = rememberPagerState { if (Build.VERSION.SDK_INT >= 36) 2 else 1 }
    val tab = pagerState.currentPage
    val coroutine = rememberCoroutineScope()
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.time)) },
                navigationIcon = { NavIcon(onNavigateUp) }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (Build.VERSION.SDK_INT >= 36) PrimaryTabRow(tab) {
                Tab(
                    tab == 0, { coroutine.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.change_time)) }
                )
                Tab(
                    tab == 1, { coroutine.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.auto_time_policy)) }
                )
            }
            HorizontalPager(
                pagerState, Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) { page ->
                if (page == 0) {
                    ChangeTimeScreen(vm)
                } else if (Build.VERSION.SDK_INT >= 36) {
                    AutoTimePolicyScreen(vm)
                }
            }
        }
    }
}

@RequiresApi(28)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeTimeScreen(vm: TimeViewModel) {
    val pagerState = rememberPagerState { 2 }
    val coroutine = rememberCoroutineScope()
    val tab = pagerState.targetPage
    Column(Modifier.fillMaxSize()) {
        SecondaryTabRow(tab) {
            Tab(
                tab == 0,
                {
                    coroutine.launch {
                        pagerState.animateScrollToPage(0)
                    }
                },
                text = { Text(stringResource(R.string.time)) }
            )
            Tab(
                tab == 1,
                {
                    coroutine.launch {
                        pagerState.animateScrollToPage(1)
                    }
                },
                text = { Text(stringResource(R.string.timezone)) }
            )
        }
        HorizontalPager(pagerState) { page ->
            if (page == 0) {
                ChangeTimeScreenContent(vm::setTime)
            } else {
                ChangeTimeZoneScreenContent(vm::setTimeZone)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChangeTimeScreenContent(setTime: (Long, Boolean) -> Unit) {
    var picker by rememberSaveable { mutableIntStateOf(0) } //0:None, 1:DatePicker, 2:TimePicker
    var useCurrentTz by rememberSaveable { mutableStateOf(true) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState(is24Hour = true)
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .padding(horizontal = HorizontalPadding)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            datePickerState.selectedDateMillis?.let { formatDate(it) } ?: "", {},
            Modifier
                .fillMaxWidth()
                .clickableTextField { picker = 1 },
            readOnly = true,
            label = { Text(stringResource(R.string.date)) }
        )
        OutlinedTextField(
            timePickerState.hour.toString().padStart(2, '0') + ":" +
                    timePickerState.minute.toString().padStart(2, '0'),
            {},
            Modifier
                .fillMaxWidth()
                .clickableTextField { picker = 2 }
                .padding(vertical = 4.dp),
            readOnly = true,
            label = { Text(stringResource(R.string.time)) }
        )
        CheckBoxItem(R.string.use_current_timezone, useCurrentTz) {
            useCurrentTz = it
        }
        Button(
            {
                val timeMillis = datePickerState.selectedDateMillis!! +
                        timePickerState.hour * 3600000 + timePickerState.minute * 60000
                setTime(timeMillis, useCurrentTz)
            },
            Modifier.fillMaxWidth(),
            datePickerState.selectedDateMillis != null
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.height(BottomPadding))
    }
    if (picker == 1) DatePickerDialog(
        confirmButton = {
            TextButton({ picker = 0 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { picker = 0 }
    ) {
        Column(Modifier.verticalScroll(rememberScrollState())) {
            DatePicker(datePickerState)
        }
    }
    if (picker == 2) TimePickerDialog(
        title = {},
        confirmButton = {
            TextButton({ picker = 0 }) {
                Text(stringResource(R.string.confirm))
            }
        },
        onDismissRequest = { picker = 0 }
    ) {
        TimePicker(timePickerState)
    }
}

@Composable
private fun ChangeTimeZoneScreenContent(setTimeZone: (String) -> Unit) {
    var inputTimezone by rememberSaveable { mutableStateOf(TimeZone.getDefault().id) }
    var dialog by rememberSaveable { mutableStateOf(false) }
    val availableIds = TimeZone.getAvailableIDs()
    val validInput = inputTimezone in availableIds
    Column(
        Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .padding(horizontal = HorizontalPadding)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            inputTimezone, { inputTimezone = it },
            Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.timezone_id)) },
            isError = inputTimezone.isNotEmpty() && !validInput,
            trailingIcon = {
                IconButton({ dialog = true }) {
                    Icon(Icons.AutoMirrored.Default.List, null)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii)
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            {
                setTimeZone(inputTimezone)
            },
            Modifier.fillMaxWidth(),
            inputTimezone.isNotEmpty() && validInput
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        Notes(R.string.disable_auto_time_zone_before_set)
    }
    if (dialog) AlertDialog(
        text = {
            LazyColumn {
                items(availableIds) {
                    Text(
                        it,
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp)
                            .clickable {
                                inputTimezone = it
                                dialog = false
                            }
                            .padding(start = 6.dp, top = 10.dp, bottom = 10.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { dialog = false }) {
                Text(stringResource(R.string.cancel))
            }
        },
        onDismissRequest = { dialog = false }
    )
}

@RequiresApi(36)
@Composable
private fun AutoTimePolicyScreen(vm: TimeViewModel) {
    LaunchedEffect(Unit) {
        vm.getAutoTimePolicy()
        vm.getAutoTimeZonePolicy()
    }
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            stringResource(R.string.auto_time_policy),
            Modifier.padding(start = 8.dp, top = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        AutoTimePolicyScreenContent(vm.autoTimePolicyState, vm::setAutoTimePolicy)
        Spacer(Modifier.height(10.dp))
        Text(
            stringResource(R.string.auto_timezone_policy),
            Modifier.padding(start = 8.dp),
            style = MaterialTheme.typography.titleLarge
        )
        AutoTimePolicyScreenContent(vm.autoTimeZonePolicyState, vm::setAutoTimeZonePolicy)
    }
}

@Composable
private fun AutoTimePolicyScreenContent(
    policyState: StateFlow<Int>, setPolicy: (Int) -> Unit
) {
    val policy by policyState.collectAsState()
    listOf(
        0 to R.string.not_controlled_by_policy,
        1 to R.string.disabled,
        2 to R.string.enable
    ).forEach {
        FullWidthRadioButtonItem(it.second, it.first == policy) {
            setPolicy(it.first)
        }
    }
}
