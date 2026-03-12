package com.bintianqi.owndroid.feature.user_restriction

import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.FunctionItem
import com.bintianqi.owndroid.ui.MyLazyScaffold
import com.bintianqi.owndroid.ui.NavIcon
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.HorizontalPadding
import com.bintianqi.owndroid.utils.adaptiveInsets

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(24)
@Composable
fun UserRestrictionScreen(
    vm: UserRestrictionViewModel, onNavigateUp: () -> Unit, onNavigate: (Destination) -> Unit
) {
    val privilege by vm.privilegeState.collectAsStateWithLifecycle()
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    LaunchedEffect(Unit) {
        vm.getRestrictions()
    }
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.user_restriction)) },
                navigationIcon = { NavIcon(onNavigateUp) },
                actions = {
                    IconButton({ onNavigate(Destination.UserRestrictionEditor) }) {
                        Icon(Icons.Default.Edit, null)
                    }
                },
                scrollBehavior = sb
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 80.dp)
        ) {
            Spacer(Modifier.padding(vertical = 2.dp))
            Text(
                stringResource(R.string.switch_to_disable_feature),
                Modifier.padding(start = 16.dp)
            )
            if (privilege.profile && !privilege.work) {
                Text(
                    stringResource(R.string.profile_owner_is_restricted),
                    Modifier.padding(start = 16.dp)
                )
            }
            if (privilege.work) {
                Text(
                    stringResource(R.string.some_features_invalid_in_work_profile),
                    Modifier.padding(start = 16.dp)
                )
            }
            Spacer(Modifier.padding(vertical = 2.dp))
            UserRestrictionCategory.entries.forEach {
                FunctionItem(it.title, icon = it.icon) {
                    onNavigate(Destination.UserRestrictionOptions(it.name))
                }
            }
        }
    }
}

@RequiresApi(24)
@Composable
fun UserRestrictionOptionsScreen(
    args: Destination.UserRestrictionOptions, vm: UserRestrictionViewModel,
    onNavigateUp: () -> Unit
) {
    val status by vm.restrictionsState.collectAsStateWithLifecycle()
    val (title, items) = UserRestrictionsRepository.getData(args.id)
    MyLazyScaffold(title, onNavigateUp) {
        items(items) { restriction ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .combinedClickable(onClick = {}, onLongClick = {
                        vm.createShortcut(restriction.id)
                    })
                    .padding(15.dp, 6.dp),
                Arrangement.SpaceBetween, Alignment.CenterVertically
            ) {
                Row(Modifier.weight(1F), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painterResource(restriction.icon), null,
                        Modifier.padding(start = 6.dp, end = 16.dp)
                    )
                    Column {
                        Text(stringResource(restriction.name), style = typography.titleMedium)
                        Text(
                            restriction.id, style = typography.bodyMedium,
                            color = colorScheme.onBackground.copy(alpha = 0.8F)
                        )
                    }
                }
                Switch(
                    status[restriction.id] == true,
                    {
                        vm.setRestriction(restriction.id, it)
                    },
                    Modifier.padding(start = 8.dp)
                )
            }
        }
        item {
            Row(
                Modifier
                    .padding(HorizontalPadding, 10.dp)
                    .fillMaxWidth()
                    .background(colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Outlined.Info, null, Modifier.padding(end = 8.dp),
                    colorScheme.onPrimaryContainer
                )
                Text(
                    stringResource(R.string.user_restriction_tip),
                    color = colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@RequiresApi(24)
@Composable
fun UserRestrictionEditorScreen(
    vm: UserRestrictionViewModel, onNavigateUp: () -> Unit
) {
    val map by vm.restrictionsState.collectAsStateWithLifecycle()
    val list = map.filter { it.value }.map { it.key }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit)) },
                navigationIcon = { NavIcon(onNavigateUp) }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { paddingValues ->
        LazyColumn(Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
            items(list, { it }) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(HorizontalPadding, 2.dp)
                        .animateItem(),
                    Arrangement.SpaceBetween, Alignment.CenterVertically
                ) {
                    Text(it)
                    IconButton({
                        vm.setRestriction(it, false)
                    }) {
                        Icon(Icons.Outlined.Delete, null)
                    }
                }
            }
            item {
                var input by rememberSaveable { mutableStateOf("") }
                fun add() {
                    vm.setRestriction(input, true)
                }
                OutlinedTextField(
                    input, { input = it }, Modifier
                        .fillMaxWidth()
                        .padding(HorizontalPadding, 8.dp),
                    label = { Text("id") },
                    trailingIcon = {
                        IconButton(::add, enabled = input.isNotBlank()) {
                            Icon(Icons.Default.Add, null)
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions { add() }
                )
                Spacer(Modifier.height(BottomPadding))
            }
        }
    }
}
