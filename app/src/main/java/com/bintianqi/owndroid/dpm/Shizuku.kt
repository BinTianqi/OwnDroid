package com.bintianqi.owndroid.dpm

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.os.Binder
import android.os.Build.VERSION
import android.os.IBinder
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.bintianqi.owndroid.IUserService
import com.bintianqi.owndroid.MyViewModel
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.ui.MyScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Shizuku(vm: MyViewModel, navCtrl: NavHostController) {
    val context = LocalContext.current
    val dpm = context.getDPM()
    val receiver = context.getReceiver()
    val coScope = rememberCoroutineScope()
    val outputTextScrollState = rememberScrollState()
    var outputText by rememberSaveable { mutableStateOf("") }
    var showDeviceAdminButton by remember { mutableStateOf(!context.isDeviceAdmin) }
    var showDeviceOwnerButton by remember { mutableStateOf(!context.isDeviceOwner) }
    var showOrgProfileOwnerButton by remember { mutableStateOf(true) }
    val binder by vm.shizukuBinder.collectAsStateWithLifecycle()
    var service by remember { mutableStateOf<IUserService?>(null) }
    var loading by remember { mutableStateOf(true) }
    LaunchedEffect(binder) {
        if(binder != null && binder!!.pingBinder()) {
            service = IUserService.Stub.asInterface(binder)
            loading = false
        } else {
            service = null
        }
    }
    LaunchedEffect(service) {
        if(service == null && !loading) navCtrl.navigateUp()
    }
    LaunchedEffect(Unit) {
        if(binder == null) bindShizukuService(context, vm.shizukuBinder)
    }
    MyScaffold(R.string.shizuku, 0.dp, navCtrl, false) {
        if(loading) {
            Dialog(onDismissRequest = { navCtrl.navigateUp() }) {
                CircularProgressIndicator()
            }
        }
        
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
                coScope.launch{
                    outputText = service!!.execute("dumpsys account")
                    outputTextScrollState.animateScrollTo(0)
                }
            },
            modifier = Modifier.align(Alignment.CenterHorizontally)
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
                modifier = Modifier.align(Alignment.CenterHorizontally)
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
                modifier = Modifier.align(Alignment.CenterHorizontally)
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

fun bindShizukuService(context: Context, shizukuBinder: MutableStateFlow<IBinder?>) {
    val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            shizukuBinder.value = binder
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            shizukuBinder.value = null
            Toast.makeText(context, R.string.shizuku_service_disconnected, Toast.LENGTH_SHORT).show()
        }
    }
    val userServiceArgs = Shizuku.UserServiceArgs(ComponentName(context, ShizukuService::class.java))
        .daemon(false)
        .processNameSuffix("shizuku-service")
        .debuggable(false)
        .version(26)
    try {
        Shizuku.bindUserService(userServiceArgs, userServiceConnection)
    } catch(e: Exception) {
        e.printStackTrace()
    }
}
