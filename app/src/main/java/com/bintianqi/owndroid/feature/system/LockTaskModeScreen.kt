package com.bintianqi.owndroid.feature.system

import android.app.admin.DevicePolicyManager
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.feature.applications.ApplicationItem
import com.bintianqi.owndroid.ui.ErrorDialog
import com.bintianqi.owndroid.ui.FullWidthCheckBoxItem
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.Notes
import com.bintianqi.owndroid.ui.PackageNameTextField
import com.bintianqi.owndroid.utils.AppInfo
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.bintianqi.owndroid.utils.isValidPackageName
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(28)
@Composable
fun LockTaskModeScreen(
    vm: LockTaskModeViewModel, chosenPackage: Channel<String>, chooseSinglePackage: () -> Unit,
    choosePackage: () -> Unit, onNavigateUp: () -> Unit
) {
    val coroutine = rememberCoroutineScope()
    val pagerState = rememberPagerState { 3 }
    val tabIndex = pagerState.targetPage
    LaunchedEffect(Unit) {
        vm.getPackages()
        vm.getFeatures()
    }
    Scaffold(
        topBar = {
            TopAppBar(
                { Text(stringResource(R.string.lock_task_mode)) },
                navigationIcon = { NavIcon(onNavigateUp) }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            PrimaryTabRow(tabIndex) {
                Tab(
                    tabIndex == 0, { coroutine.launch { pagerState.animateScrollToPage(0) } },
                    text = { Text(stringResource(R.string.start)) }
                )
                Tab(
                    tabIndex == 1, { coroutine.launch { pagerState.animateScrollToPage(1) } },
                    text = { Text(stringResource(R.string.applications)) }
                )
                Tab(
                    tabIndex == 2, { coroutine.launch { pagerState.animateScrollToPage(2) } },
                    text = { Text(stringResource(R.string.features)) }
                )
            }
            HorizontalPager(pagerState, verticalAlignment = Alignment.Top) { page ->
                if(page == 0) {
                    StartLockTaskMode(vm::startLockTaskMode, chosenPackage, chooseSinglePackage)
                } else if (page == 1) {
                    LockTaskPackages(chosenPackage, choosePackage, vm.packagesState, vm::setPackage)
                } else {
                    LockTaskFeatures(vm.featuresState, vm::setFeatures, vm::applyFeatures)
                }
            }
        }
    }
}

@RequiresApi(28)
@Composable
private fun StartLockTaskMode(
    startLockTaskMode: (String, String, Boolean, Boolean) -> Unit,
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit
) {
    val focusMgr = LocalFocusManager.current
    var packageName by rememberSaveable { mutableStateOf("") }
    var activity by rememberSaveable { mutableStateOf("") }
    var specifyActivity by rememberSaveable { mutableStateOf(false) }
    var clearTask by rememberSaveable { mutableStateOf(true) }
    var showNotification by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        PackageNameTextField(
            packageName, onChoosePackage, Modifier.padding(HorizontalPadding, 8.dp)
        ) { packageName = it }
        FullWidthCheckBoxItem(
            R.string.lock_task_mode_start_clear_task, clearTask
        ) { clearTask = it }
        FullWidthCheckBoxItem(
            R.string.lock_task_mode_show_notification, showNotification
        ) { showNotification = it }
        Row(
            Modifier
                .fillMaxWidth()
                .padding(4.dp, 4.dp, HorizontalPadding, 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(specifyActivity, {
                specifyActivity = it
                activity = ""
            })
            OutlinedTextField(
                activity, { activity = it },
                Modifier.fillMaxWidth(),
                label = { Text("Activity") },
                enabled = specifyActivity,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusMgr.clearFocus() })
            )
        }
        Button(
            {
                startLockTaskMode(packageName, activity, clearTask, showNotification)
            },
            Modifier
                .fillMaxWidth()
                .padding(horizontal = HorizontalPadding),
            packageName.isNotBlank() && (!specifyActivity || activity.isNotBlank())
        ) {
            Text(stringResource(R.string.start))
        }
        Spacer(Modifier.height(5.dp))
        Notes(R.string.info_start_lock_task_mode, HorizontalPadding)
    }
}

@RequiresApi(26)
@Composable
private fun LockTaskPackages(
    chosenPackage: Channel<String>, onChoosePackage: () -> Unit,
    packagesState: StateFlow<List<AppInfo>>, setPackage: (String, Boolean) -> Unit
) {
    val packages by packagesState.collectAsStateWithLifecycle()
    var packageName by rememberSaveable { mutableStateOf("") }
    LaunchedEffect(Unit) {
        packageName = chosenPackage.receive()
    }
    LazyColumn {
        items(packages, { it.name }) {
            ApplicationItem(it) { setPackage(it.name, false) }
        }
        item {
            Column(
                Modifier.padding(horizontal = HorizontalPadding)
            ) {
                PackageNameTextField(
                    packageName, onChoosePackage, Modifier.padding(vertical = 3.dp)
                ) { packageName = it }
                Button(
                    {
                        setPackage(packageName, true)
                        packageName = ""
                    },
                    Modifier.fillMaxWidth(),
                    packageName.isValidPackageName
                ) {
                    Text(stringResource(R.string.add))
                }
                Notes(R.string.info_lock_task_packages)
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}

@RequiresApi(28)
@Composable
private fun LockTaskFeatures(
    featuresState: StateFlow<Int>, setFlag: (Int) -> Unit, apply: ((String?) -> Unit) -> Unit
) {
    val flags by featuresState.collectAsState()
    var errorMessage by rememberSaveable { mutableStateOf<String?>(null) }
    Column(
        Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.padding(vertical = 5.dp))
        listOf(
            DevicePolicyManager.LOCK_TASK_FEATURE_SYSTEM_INFO to R.string.ltf_sys_info,
            DevicePolicyManager.LOCK_TASK_FEATURE_NOTIFICATIONS to R.string.ltf_notifications,
            DevicePolicyManager.LOCK_TASK_FEATURE_HOME to R.string.ltf_home,
            DevicePolicyManager.LOCK_TASK_FEATURE_OVERVIEW to R.string.ltf_overview,
            DevicePolicyManager.LOCK_TASK_FEATURE_GLOBAL_ACTIONS to R.string.ltf_global_actions,
            DevicePolicyManager.LOCK_TASK_FEATURE_KEYGUARD to R.string.ltf_keyguard
        ).let {
            if(VERSION.SDK_INT >= 30) it.plus(
                DevicePolicyManager.LOCK_TASK_FEATURE_BLOCK_ACTIVITY_START_IN_TASK to
                        R.string.ltf_block_activity_start_in_task)
            else it
        }.forEach { (id, title) ->
            FullWidthCheckBoxItem(title, flags and id != 0) { setFlag(flags xor id) }
        }
        Button(
            {
                apply { errorMessage = it }
            },
            Modifier
                .fillMaxWidth()
                .padding(HorizontalPadding, 4.dp)
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.height(BottomPadding))
    }
    ErrorDialog(errorMessage) { errorMessage = null }
}
