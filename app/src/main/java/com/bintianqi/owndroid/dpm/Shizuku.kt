package com.bintianqi.owndroid.dpm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build.VERSION
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.IUserService
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MySmallTitleScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import rikka.shizuku.Shizuku

@Serializable object ShizukuScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShizukuScreen(navArgs: Bundle, onNavigateUp: () -> Unit, onNavigateToAccountsViewer: (Accounts) -> Unit) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val coScope = rememberCoroutineScope()
    val outputTextScrollState = rememberScrollState()
    var outputText by rememberSaveable { mutableStateOf("") }
    var showDeviceOwnerButton by remember { mutableStateOf(!context.isDeviceOwner) }
    var showOrgProfileOwnerButton by remember { mutableStateOf(true) }
    val binder = navArgs.getBinder("binder")!!
    var service by remember { mutableStateOf<IUserService?>(null) }
    LaunchedEffect(Unit) {
        service = if(binder.pingBinder()) {
            IUserService.Stub.asInterface(binder)
        } else {
            null
        }
    }
    MySmallTitleScaffold(R.string.shizuku, onNavigateUp, 0.dp) {
        
        Button(
            onClick = {
                coScope.launch{
                    outputText = service!!.execute("dpm list-owners")
                    outputTextScrollState.animateScrollTo(0)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.list_owners))
        }
        Button(
            onClick = {
                coScope.launch{
                    outputText = service!!.execute("pm list users")
                    outputTextScrollState.animateScrollTo(0)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.list_users))
        }
        Button(
            onClick = {
                Log.d("Shizuku", "List accounts")
                try {
                    val accounts = service!!.listAccounts().map {
                        Accounts.Account(it.type, it.name)
                    }
                    onNavigateToAccountsViewer(Accounts(accounts))
                } catch(_: Exception) {
                    outputText = service!!.execute("dumpsys account")
                    coScope.launch{
                        outputTextScrollState.animateScrollTo(0)
                    }
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.list_accounts))
        }
        Spacer(Modifier.padding(vertical = 5.dp))

        AnimatedVisibility(showDeviceOwnerButton, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Button(
                onClick = {
                    coScope.launch{
                        outputText = service!!.execute(context.getString(R.string.dpm_activate_do_command))
                        outputTextScrollState.animateScrollTo(0)
                        delay(500)
                        showDeviceOwnerButton = !context.isDeviceOwner
                    }
                }
            ) {
                Text(text = stringResource(R.string.activate_device_owner))
            }
        }
        
        if(VERSION.SDK_INT >= 30 && context.isProfileOwner && dpm.isManagedProfile(receiver) && !dpm.isOrganizationOwnedDeviceWithManagedProfile) {
            AnimatedVisibility(showOrgProfileOwnerButton) {
                Button(
                    onClick = {
                        coScope.launch{
                            val userID = Binder.getCallingUid() / 100000
                            outputText = service!!.execute(
                                "dpm mark-profile-owner-on-organization-owned-device --user $userID com.bintianqi.owndroid/com.bintianqi.owndroid.Receiver"
                            )
                            outputTextScrollState.animateScrollTo(0)
                            delay(500)
                            showOrgProfileOwnerButton = !dpm.isOrganizationOwnedDeviceWithManagedProfile
                        }
                    },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(text = stringResource(R.string.activate_org_profile))
                }
            }
        }
        
        SelectionContainer(modifier = Modifier.fillMaxWidth().horizontalScroll(outputTextScrollState)) {
            Text(text = outputText, softWrap = false, modifier = Modifier.padding(vertical = 9.dp, horizontal = 12.dp))
        }
    }
}

fun controlShizukuService(
    context: Context,
    onServiceConnected: (IBinder) -> Unit,
    onServiceDisconnected: () -> Unit,
    state: Boolean
) {
    val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            onServiceConnected(binder)
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            onServiceDisconnected()
        }
    }
    val userServiceArgs = Shizuku.UserServiceArgs(ComponentName(context, ShizukuService::class.java))
        .daemon(false)
        .processNameSuffix("shizuku-service")
        .debuggable(false)
        .version(26)
    if(state) Shizuku.bindUserService(userServiceArgs, userServiceConnection)
    else Shizuku.unbindUserService(userServiceArgs, userServiceConnection, true)
}

@Serializable
data class Accounts(
    val list: List<Account>
) {
    @Serializable data class Account(val type: String, val name: String)
}

@Composable
fun AccountsScreen(accounts: Accounts, onNavigateUp: () -> Unit) {
    MySmallTitleScaffold(R.string.accounts, onNavigateUp) {
        accounts.list.forEach {
            Column(
                modifier = Modifier
                    .fillMaxWidth().padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(15)).background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SelectionContainer {
                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) {
                        Text(stringResource(R.string.type) + ": " + it.type)
                        Text(stringResource(R.string.name) + ": " + it.name)
                    }
                }
            }
        }
    }
}
