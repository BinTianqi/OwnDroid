package com.bintianqi.owndroid.feature.time_blocker

import android.content.pm.PackageManager
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.adaptiveInsets
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeBlockerScreen(
    vm: TimeBlockerViewModel,
    onNavigate: (Destination) -> Unit,
    onNavigateUp: () -> Unit
) {
    val rules by vm.rulesState.collectAsState()
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.time_blocker)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                scrollBehavior = sb
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { onNavigate(Destination.TimeBlockerEdit()) },
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text(stringResource(R.string.time_blocker_add_rule)) }
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) { padding ->
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Usage permission card
            item {
                val hasPermission = vm.hasUsageStatsPermission()
                StatusCard(
                    title = if (hasPermission) stringResource(R.string.time_blocker_usage_permission_granted)
                            else stringResource(R.string.time_blocker_usage_permission),
                    subtitle = if (!hasPermission) stringResource(R.string.time_blocker_usage_permission_needed) else null,
                    isPositive = hasPermission,
                    onClick = { if (!hasPermission) vm.openUsageAccessSettings() }
                )
            }

            // Service status card with real toggle
            item {
                val running by vm.serviceRunning.collectAsState()
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (running) colorScheme.primaryContainer else colorScheme.errorContainer
                    )
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (running) stringResource(R.string.time_blocker_service_running)
                                else stringResource(R.string.time_blocker_service_stopped),
                                style = MaterialTheme.typography.titleSmall
                            )
                            Text(
                                if (running) stringResource(R.string.time_blocker_service_running_sub)
                                else stringResource(R.string.time_blocker_service_stopped_sub),
                                style = MaterialTheme.typography.bodySmall,
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = running,
                            onCheckedChange = { if (running) vm.stopService() else vm.startService() }
                        )
                    }
                }
            }

            // Rules
            if (rules.isEmpty()) {
                item {
                    Text(
                        stringResource(R.string.time_blocker_no_rules),
                        Modifier.padding(24.dp),
                        style = MaterialTheme.typography.bodyLarge,
                        color = colorScheme.onSurfaceVariant
                    )
                }
            }
            items(rules, key = { it.id }) { rule ->
                RuleItem(
                    rule = rule,
                    onToggle = { vm.toggleRuleEnabled(rule) },
                    onClick = { onNavigate(Destination.TimeBlockerEdit(rule.id)) }
                )
            }

            item { Spacer(Modifier.height(BottomPadding + 80.dp)) }
        }
    }
}

@Composable
fun StatusCard(title: String, subtitle: String?, isPositive: Boolean, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) colorScheme.primaryContainer else colorScheme.errorContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) {
                Text(subtitle, style = MaterialTheme.typography.bodySmall,
                    color = colorScheme.onSurfaceVariant)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RuleItem(rule: BlockRule, onToggle: () -> Unit, onClick: () -> Unit) {
    val context = LocalContext.current
    val pm = context.packageManager
    val (appLabel, appIcon) = remember(rule.packageName) {
        val info = try { pm.getApplicationInfo(rule.packageName, 0) } catch (_: PackageManager.NameNotFoundException) { null }
        (info?.loadLabel(pm)?.toString() ?: rule.packageName) to info?.loadIcon(pm)
    }

    Card(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (appIcon != null) {
                Image(
                    rememberDrawablePainter(appIcon), null,
                    Modifier.size(40.dp)
                )
                Spacer(Modifier.width(12.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(appLabel, style = MaterialTheme.typography.titleMedium)
                if (rule.dailyLimitMinutes > 0) {
                    Text(
                        stringResource(R.string.time_blocker_daily_limit) + ": " +
                                stringResource(R.string.time_blocker_daily_limit_minutes, rule.dailyLimitMinutes),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                }
                if (rule.blockedWindows.isNotEmpty()) {
                    Text(
                        stringResource(R.string.time_blocker_blocked_windows),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        rule.blockedWindows.forEach { window ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        stringResource(R.string.time_blocker_window_format, formatMinutes(window.startMinutes), formatMinutes(window.endMinutes)),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }
                if (rule.allowedWindows.isNotEmpty()) {
                    Text(
                        stringResource(R.string.time_blocker_allowed_windows),
                        style = MaterialTheme.typography.bodySmall,
                        color = colorScheme.onSurfaceVariant
                    )
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        rule.allowedWindows.forEach { window ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(
                                        stringResource(R.string.time_blocker_window_format, formatMinutes(window.startMinutes), formatMinutes(window.endMinutes)),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            )
                        }
                    }
                }
            }
            Switch(
                checked = rule.enabled,
                onCheckedChange = { onToggle() }
            )
        }
    }
}

fun formatMinutes(totalMinutes: Int): String {
    val h = totalMinutes / 60
    val m = totalMinutes % 60
    return "%02d:%02d".format(h, m)
}
