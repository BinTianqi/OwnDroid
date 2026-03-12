package com.bintianqi.owndroid.feature.network

import android.app.usage.NetworkStats
import android.os.Build.VERSION
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.CircularProgressDialog
import com.bintianqi.owndroid.ui.ErrorDialog
import com.bintianqi.owndroid.ui.MyScaffold
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.clickableTextField
import com.bintianqi.owndroid.utils.formatDate
import com.bintianqi.owndroid.utils.formatFileSize
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkStatsScreen(
    vm: NetworkStatsViewModel, choosePackage: () -> Unit, chosenPackage: Channel<String>,
    navigateUp: () -> Unit, navigateToViewer: () -> Unit
) {
    val res = LocalResources.current
    val privilege by vm.privilegeState.collectAsState()
    fun getDefaultSummaryTarget(): NetworkStatsTarget {
        return if (privilege.device) NetworkStatsTarget.Device else NetworkStatsTarget.User
    }
    var menu by remember { mutableStateOf(NetworkStatsMenu.None) }
    var type by rememberSaveable { mutableStateOf(NetworkStatsType.Summary) }
    var target by rememberSaveable { mutableStateOf(getDefaultSummaryTarget()) }
    var networkType by rememberSaveable { mutableStateOf(NetworkType.Mobile) }
    var startTime by rememberSaveable {
        mutableLongStateOf(System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000)
    }
    var endTime by rememberSaveable { mutableLongStateOf(System.currentTimeMillis()) }
    var uid by rememberSaveable { mutableIntStateOf(NetworkStats.Bucket.UID_ALL) }
    var tag by rememberSaveable { mutableIntStateOf(NetworkStats.Bucket.TAG_NONE) }
    var state by rememberSaveable { mutableStateOf(NetworkStatsState.All) }
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    var querying by rememberSaveable { mutableStateOf(false) }
    MyScaffold(R.string.network_stats, navigateUp) {
        ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.Type,
            { menu = if (it) NetworkStatsMenu.Type else NetworkStatsMenu.None },
            Modifier.padding(top = 8.dp, bottom = 4.dp)
        ) {
            OutlinedTextField(
                stringResource(type.text), {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true, label = { Text(stringResource(R.string.type)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == NetworkStatsMenu.Type)
                }
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.Type, { menu = NetworkStatsMenu.None }
            ) {
                NetworkStatsType.entries.forEach {
                    DropdownMenuItem(
                        { Text(stringResource(it.text)) },
                        {
                            type = it
                            target = if (it == NetworkStatsType.Summary) getDefaultSummaryTarget()
                            else NetworkStatsTarget.Uid
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.Target,
            { menu = if (it) NetworkStatsMenu.Target else NetworkStatsMenu.None },
            Modifier.padding(bottom = 4.dp)
        ) {
            OutlinedTextField(
                stringResource(target.text), {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text(stringResource(R.string.target)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == NetworkStatsMenu.Target)
                }
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.Target, { menu = NetworkStatsMenu.None }
            ) {
                NetworkStatsTarget.entries.filter {
                    VERSION.SDK_INT >= it.minApi && type == it.type
                }.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.text)) },
                        onClick = {
                            target = it
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        ExposedDropdownMenuBox(
            menu == NetworkStatsMenu.NetworkType,
            { menu = if (it) NetworkStatsMenu.NetworkType else NetworkStatsMenu.None },
            Modifier.padding(bottom = 4.dp)
        ) {
            OutlinedTextField(
                stringResource(networkType.text), {},
                Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text(stringResource(R.string.network_type)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(menu == NetworkStatsMenu.NetworkType)
                }
            )
            ExposedDropdownMenu(
                menu == NetworkStatsMenu.NetworkType, { menu = NetworkStatsMenu.None }
            ) {
                NetworkType.entries.forEach {
                    DropdownMenuItem(
                        text = { Text(stringResource(it.text)) },
                        onClick = {
                            networkType = it
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            formatDate(startTime), {},
            Modifier
                .fillMaxWidth()
                .clickableTextField { menu = NetworkStatsMenu.StartTime }
                .padding(bottom = 4.dp),
            readOnly = true, label = { Text(stringResource(R.string.start_time)) },
            isError = startTime >= endTime
        )
        OutlinedTextField(
            formatDate(endTime), {},
            Modifier
                .fillMaxWidth()
                .clickableTextField { menu = NetworkStatsMenu.EndTime }
                .padding(bottom = 4.dp),
            readOnly = true,
            label = { Text(stringResource(R.string.end_time)) },
            isError = startTime >= endTime
        )
        if (
            target == NetworkStatsTarget.Uid || target == NetworkStatsTarget.UidTag ||
            target == NetworkStatsTarget.UidTagState
        ) {
            ExposedDropdownMenuBox(
                menu == NetworkStatsMenu.Uid,
                { menu = if (it) NetworkStatsMenu.Uid else NetworkStatsMenu.None }
            ) {
                var uidText by rememberSaveable {
                    mutableStateOf(res.getString(NetworkStatsUID.All.text))
                }
                var readOnly by rememberSaveable { mutableStateOf(true) }
                if (VERSION.SDK_INT >= 24) LaunchedEffect(Unit) {
                    val pkg = chosenPackage.receive()
                    uid = vm.getPackageUid(pkg)
                    uidText = "$uid ($pkg)"
                }
                OutlinedTextField(
                    uidText,
                    {
                        uidText = it
                        it.toIntOrNull()?.let { num -> uid = num }
                    },
                    readOnly = readOnly,
                    label = { Text(stringResource(R.string.uid)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(menu == NetworkStatsMenu.Uid)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = !readOnly && uidText.toIntOrNull() == null,
                    modifier = Modifier
                        .menuAnchor(
                            if (readOnly) ExposedDropdownMenuAnchorType.PrimaryNotEditable
                            else ExposedDropdownMenuAnchorType.PrimaryEditable
                        )
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
                ExposedDropdownMenu(
                    menu == NetworkStatsMenu.Uid, { menu = NetworkStatsMenu.None }
                ) {
                    NetworkStatsUID.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                uid = it.uid
                                readOnly = true
                                uidText = res.getString(it.text)
                                menu = NetworkStatsMenu.None
                            }
                        )
                    }
                    if (VERSION.SDK_INT >= 24) DropdownMenuItem(
                        { Text(stringResource(R.string.choose_an_app)) },
                        {
                            readOnly = true
                            menu = NetworkStatsMenu.None
                            choosePackage()
                        }
                    )
                    DropdownMenuItem(
                        { Text(stringResource(R.string.input)) },
                        {
                            readOnly = false
                            uidText = ""
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        if (
            VERSION.SDK_INT >= 24 &&
            (target == NetworkStatsTarget.UidTag || target == NetworkStatsTarget.UidTagState)
        ) {
            ExposedDropdownMenuBox(
                menu == NetworkStatsMenu.Tag,
                { menu = if (it) NetworkStatsMenu.Tag else NetworkStatsMenu.None },
                Modifier.padding(bottom = 4.dp)
            ) {
                var tagText by rememberSaveable { mutableStateOf(res.getString(R.string.all)) }
                var readOnly by rememberSaveable { mutableStateOf(true) }
                OutlinedTextField(
                    tagText,
                    {
                        tagText = it
                        it.toIntOrNull()?.let { num -> tag = num }
                    },
                    Modifier
                        .menuAnchor(
                            if (readOnly) ExposedDropdownMenuAnchorType.PrimaryNotEditable
                            else ExposedDropdownMenuAnchorType.PrimaryEditable
                        )
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    readOnly = readOnly,
                    label = { Text(stringResource(R.string.uid)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(menu == NetworkStatsMenu.Tag)
                    },
                    isError = !readOnly && tagText.toIntOrNull() == null
                )
                ExposedDropdownMenu(
                    menu == NetworkStatsMenu.Tag, { menu = NetworkStatsMenu.None }
                ) {
                    DropdownMenuItem(
                        { Text(stringResource(R.string.all)) },
                        {
                            tag = NetworkStats.Bucket.TAG_NONE
                            tagText = res.getString(R.string.all)
                            readOnly = true
                            menu = NetworkStatsMenu.None
                        }
                    )
                    DropdownMenuItem(
                        { Text(stringResource(R.string.input)) },
                        {
                            tagText = ""
                            readOnly = false
                            menu = NetworkStatsMenu.None
                        }
                    )
                }
            }
        }
        if (VERSION.SDK_INT >= 28 && target == NetworkStatsTarget.UidTagState) {
            ExposedDropdownMenuBox(
                menu == NetworkStatsMenu.State,
                { menu = if (it) NetworkStatsMenu.State else NetworkStatsMenu.None },
                Modifier.padding(bottom = 4.dp)
            ) {
                OutlinedTextField(
                    stringResource(state.text), {},
                    Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    readOnly = true, label = { Text(stringResource(R.string.uid)) },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(menu == NetworkStatsMenu.State)
                    }
                )
                ExposedDropdownMenu(
                    menu == NetworkStatsMenu.State, { menu = NetworkStatsMenu.None }
                ) {
                    NetworkStatsState.entries.forEach {
                        DropdownMenuItem(
                            { Text(stringResource(it.text)) },
                            {
                                state = it
                                menu = NetworkStatsMenu.None
                            }
                        )
                    }
                }
            }
        }
        Button(
            {
                querying = true
                vm.queryStats(
                    QueryNetworkStatsParams(
                        type, target, networkType, startTime, endTime, uid, tag, state
                    )
                ) {
                    querying = false
                    errorMessage = it
                    if (it == null) navigateToViewer()
                }
            },
            Modifier.fillMaxWidth().padding(top = 8.dp),
            !querying
        ) {
            Text(stringResource(R.string.query))
        }
        if (menu == NetworkStatsMenu.StartTime || menu == NetworkStatsMenu.EndTime) {
            val datePickerState = rememberDatePickerState(
                if (menu == NetworkStatsMenu.StartTime) startTime else endTime
            )
            DatePickerDialog(
                onDismissRequest = { menu = NetworkStatsMenu.None },
                dismissButton = {
                    TextButton(onClick = { menu = NetworkStatsMenu.None }) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (menu == NetworkStatsMenu.StartTime) {
                                startTime = datePickerState.selectedDateMillis!!
                            } else {
                                endTime = datePickerState.selectedDateMillis!!
                            }
                            menu = NetworkStatsMenu.None
                        },
                        enabled = datePickerState.selectedDateMillis != null
                    ) {
                        Text(stringResource(R.string.confirm))
                    }
                }
            ) {
                DatePicker(datePickerState)
            }
        }
    }
    if (querying) CircularProgressDialog { }
    ErrorDialog(errorMessage) { errorMessage = null }
}

@Composable
fun NetworkStatsViewerScreen(
    vm: NetworkStatsViewModel, onNavigateUp: () -> Unit
) {
    var index by rememberSaveable { mutableIntStateOf(0) }
    val size = vm.statsData.size
    val ps = rememberPagerState { size }
    index = ps.currentPage
    val coroutine = rememberCoroutineScope()
    MySmallTitleScaffold(R.string.network_stats, onNavigateUp, 0.dp) {
        if (size > 1) Row(
            Modifier.align(Alignment.CenterHorizontally).padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                {
                    coroutine.launch {
                        ps.animateScrollToPage(index - 1)
                    }
                },
                enabled = index > 0
            ) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowLeft, null)
            }
            Text("${index + 1} / $size", modifier = Modifier.padding(horizontal = 8.dp))
            IconButton(
                {
                    coroutine.launch {
                        ps.animateScrollToPage(index + 1)
                    }
                },
                enabled = index < size - 1
            ) {
                Icon(Icons.AutoMirrored.Default.KeyboardArrowRight, null)
            }
        }
        HorizontalPager(ps, Modifier.padding(top = 8.dp)) { page ->
            val item = vm.statsData[page]
            Column(Modifier.fillMaxWidth().padding(horizontal = HorizontalPadding)) {
                Text(
                    formatDate(item.startTime) + "\n~\n" + formatDate(item.endTime),
                    Modifier.align(Alignment.CenterHorizontally), textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(5.dp))
                val txBytes = item.txBytes
                Text(
                    stringResource(R.string.transmitted),
                    style = MaterialTheme.typography.titleMedium
                )
                Column(Modifier.padding(start = 8.dp, bottom = 4.dp)) {
                    Text("$txBytes bytes (${formatFileSize(txBytes)})")
                    Text(item.txPackets.toString() + " packets")
                }
                val rxBytes = item.rxBytes
                Text(
                    stringResource(R.string.received), style = MaterialTheme.typography.titleMedium
                )
                Column(Modifier.padding(start = 8.dp, bottom = 8.dp)) {
                    Text("$rxBytes bytes (${formatFileSize(rxBytes)})")
                    Text(item.rxPackets.toString() + " packets")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = NetworkStatsState.entries.find { it.id == item.state }!!.text
                    Text(
                        stringResource(R.string.state), Modifier.padding(end = 8.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(stringResource(text))
                }
                if (VERSION.SDK_INT >= 24) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val tag = item.tag
                        Text(
                            stringResource(R.string.tag),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(
                            if (tag == NetworkStats.Bucket.TAG_NONE) stringResource(
                                R.string.all
                            ) else tag.toString()
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val text = when (item.roaming) {
                            NetworkStats.Bucket.ROAMING_ALL -> R.string.all
                            NetworkStats.Bucket.ROAMING_YES -> R.string.yes
                            NetworkStats.Bucket.ROAMING_NO -> R.string.no
                            else -> R.string.unknown
                        }
                        Text(
                            stringResource(R.string.roaming),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Text(stringResource(text))
                    }
                }
                if (VERSION.SDK_INT >= 26) Row(verticalAlignment = Alignment.CenterVertically) {
                    val text = when (item.metered) {
                        NetworkStats.Bucket.METERED_ALL -> R.string.all
                        NetworkStats.Bucket.METERED_YES -> R.string.yes
                        NetworkStats.Bucket.METERED_NO -> R.string.no
                        else -> R.string.unknown
                    }
                    Text(
                        stringResource(R.string.metered),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(stringResource(text))
                }
            }
        }
    }
}
