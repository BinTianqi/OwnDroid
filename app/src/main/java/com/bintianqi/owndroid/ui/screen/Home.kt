package com.bintianqi.owndroid.ui.screen

import android.os.Build.VERSION
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.navigation.Destination
import com.bintianqi.owndroid.utils.BottomPadding
import com.bintianqi.owndroid.utils.PrivilegeStatus
import com.bintianqi.owndroid.utils.adaptiveInsets
import kotlinx.coroutines.flow.StateFlow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    privilegeState: StateFlow<PrivilegeStatus>, getAppListViewMode: () -> Boolean,
    onNavigate: (Destination) -> Unit
) {
    val privilege by privilegeState.collectAsState()
    val sb = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        Modifier.nestedScroll(sb.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                { Text(stringResource(R.string.app_name)) },
                actions = {
                    IconButton({ onNavigate(Destination.WorkingModes(true)) }) {
                        Icon(
                            painterResource(R.drawable.security_fill0), null
                        )
                    }
                    IconButton({ onNavigate(Destination.Settings) }) {
                        Icon(Icons.Default.Settings, null)
                    }
                },
                scrollBehavior = sb
            )
        },
        contentWindowInsets = adaptiveInsets()
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            if (privilege.device || privilege.profile) {
                HomePageItem(R.string.system, R.drawable.android_fill0) {
                    onNavigate(Destination.System)
                }
                HomePageItem(R.string.network, R.drawable.wifi_fill0) { onNavigate(Destination.Network) }
            }
            if (privilege.work) {
                HomePageItem(R.string.work_profile, R.drawable.work_fill0) {
                    onNavigate(Destination.WorkProfile)
                }
            }
            if (privilege.device || privilege.profile) {
                HomePageItem(R.string.applications, R.drawable.apps_fill0) {
                    onNavigate(
                        if (getAppListViewMode()) Destination.ApplicationsList(true, true)
                        else Destination.ApplicationFeatures
                    )
                }
                if (VERSION.SDK_INT >= 24) {
                    HomePageItem(R.string.user_restriction, R.drawable.person_off) {
                        onNavigate(Destination.UserRestriction)
                    }
                }
                HomePageItem(R.string.users, R.drawable.manage_accounts_fill0) {
                    onNavigate(Destination.Users)
                }
                HomePageItem(
                    R.string.password_and_keyguard, R.drawable.password_fill0
                ) { onNavigate(Destination.Password) }
            }
            Spacer(Modifier.height(BottomPadding))
        }
    }
}

@Composable
fun HomePageItem(name: Int, icon: Int, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.padding(start = 30.dp))
        Icon(painterResource(icon), null)
        Spacer(Modifier.padding(start = 15.dp))
        Text(stringResource(name), style = typography.headlineSmall)
    }
}
