package com.bintianqi.owndroid.dpm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build.VERSION
import android.os.IBinder
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.IUserService
import com.bintianqi.owndroid.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

private var waitGrantPermission = false

@Composable
fun ShizukuActivate() {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val coScope = rememberCoroutineScope()
    val outputTextScrollState = rememberScrollState()
    var enabled by remember { mutableStateOf(false) }
    var bindShizuku by remember { mutableStateOf(false) }
    var outputText by rememberSaveable { mutableStateOf("") }
    var showDeviceAdminButton by remember { mutableStateOf(!context.isDeviceAdmin) }
    var showDeviceOwnerButton by remember { mutableStateOf(!context.isDeviceOwner) }
    var showOrgProfileOwnerButton by remember { mutableStateOf(true) }
    val service by shizukuService.collectAsState()
    LaunchedEffect(service) {
        if(service == null) {
            enabled = false
            bindShizuku = checkShizukuStatus() == 1
        } else {
            enabled = true
            bindShizuku = false
        }
    }
    LaunchedEffect(Unit) {
        shizukuService.value = null
        userServiceControl(context, true)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedVisibility(bindShizuku) {
            Button(
                onClick = {
                    userServiceControl(context, true)
                    outputText = ""
                }
            ) {
                Text(stringResource(R.string.bind_shizuku))
            }
        }

        Button(
            onClick = {
                outputText = checkPermission(context)
                if(service != null) {
                    enabled = true
                    bindShizuku = false
                } else {
                    enabled = false
                    bindShizuku = checkShizukuStatus() == 1
                }
                coScope.launch {
                    outputTextScrollState.animateScrollTo(0)
                }
            }
        ) {
            Text(text = stringResource(R.string.check_shizuku))
        }
        
        Button(
            onClick = {
                coScope.launch{
                    outputText = service!!.execute("dpm list-owners")
                    outputTextScrollState.animateScrollTo(0)
                }
            },
            enabled = enabled
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
            enabled = enabled
        ) {
            Text(text = stringResource(R.string.list_users))
        }
        Button(
            onClick = {
                coScope.launch{
                    outputText = service!!.execute("dumpsys account")
                    outputTextScrollState.animateScrollTo(0)
                }
            },
            enabled = enabled
        ) {
            Text(text = stringResource(R.string.list_accounts))
        }
        Spacer(Modifier.padding(vertical = 5.dp))

        AnimatedVisibility(showDeviceAdminButton && showDeviceOwnerButton) {
            Button(
                onClick = {
                    coScope.launch{
                        outputText = service!!.execute(context.getString(R.string.dpm_activate_da_command))
                        outputTextScrollState.animateScrollTo(0)
                        delay(500)
                        showDeviceAdminButton = !context.isDeviceAdmin
                    }
                },
                enabled = enabled
            ) {
                Text(text = stringResource(R.string.activate_device_admin))
            }
        }

        AnimatedVisibility(showDeviceOwnerButton) {
            Button(
                onClick = {
                    coScope.launch{
                        outputText = service!!.execute(context.getString(R.string.dpm_activate_do_command))
                        outputTextScrollState.animateScrollTo(0)
                        delay(500)
                        showDeviceOwnerButton = !context.isDeviceOwner
                    }
                },
                enabled = enabled
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
                    enabled = enabled
                ) {
                    Text(text = stringResource(R.string.activate_org_profile))
                }
            }
        }
        
        SelectionContainer(modifier = Modifier.fillMaxWidth().horizontalScroll(outputTextScrollState)) {
            Text(text = outputText, softWrap = false, modifier = Modifier.padding(4.dp))
        }
        
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

private fun checkPermission(context: Context): String {
    if(checkShizukuStatus() == -1) { return context.getString(R.string.shizuku_not_started) }
    val getUid = if(shizukuService.value == null) { return context.getString(R.string.shizuku_not_bind) } else { shizukuService.value!!.uid }
    return when(getUid) {
        "2000"->context.getString(R.string.shizuku_activated_shell)
        "0"->context.getString(R.string.shizuku_activated_root)
        else->context.getString(R.string.unknown_status) + "\nUID: $getUid"
    }
}

fun checkShizukuStatus(): Int {
    val status = try {
        if(Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) { waitGrantPermission = false; 1 }
        else if(Shizuku.shouldShowRequestPermissionRationale()) { 0 }
        else{
            if(!waitGrantPermission) { Shizuku.requestPermission(0) }
            waitGrantPermission = true
            0
        }
    } catch(e:Exception) { -1 }
    return status
}

fun userServiceControl(context:Context, status:Boolean) {
    if(checkShizukuStatus() != 1) { return }
    val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            if (binder.pingBinder()) {
                shizukuService.value = IUserService.Stub.asInterface(binder)
            } else {
                Toast.makeText(context, R.string.invalid_binder, Toast.LENGTH_SHORT).show()
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            shizukuService.value = null
            Toast.makeText(context, R.string.shizuku_service_disconnected, Toast.LENGTH_SHORT).show()
        }
    }
    val userServiceArgs = Shizuku.UserServiceArgs(
        ComponentName(
            context.packageName,ShizukuService::class.java.name
        )
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(true)
        .version(26)
    try {
        if(status) {
            Shizuku.bindUserService(userServiceArgs, userServiceConnection)
        }else{
            Shizuku.unbindUserService(userServiceArgs, userServiceConnection, false)
        }
    } catch(_: Exception) { }
}
