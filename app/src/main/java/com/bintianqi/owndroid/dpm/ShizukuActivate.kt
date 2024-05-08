package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build.VERSION
import android.os.IBinder
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.bintianqi.owndroid.IUserService
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Receiver
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import rikka.shizuku.Shizuku

private var waitGrantPermission = false

@Composable
fun ShizukuActivate(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext, Receiver::class.java)
    val coScope = rememberCoroutineScope()
    val outputTextScrollState = rememberScrollState()
    var enabled by remember{ mutableStateOf(false) }
    var bindShizuku by remember{ mutableStateOf(false) }
    var outputText by remember{mutableStateOf("")}
    var showDeviceAdminButton by remember{mutableStateOf(!myDpm.isAdminActive(myComponent))}
    var showProfileOwnerButton by remember{mutableStateOf(!isProfileOwner(myDpm))}
    var showDeviceOwnerButton by remember{mutableStateOf(!isDeviceOwner(myDpm))}
    var showOrgProfileOwnerButton by remember{mutableStateOf(true)}
    LaunchedEffect(Unit){
        if(service==null){userServiceControl(myContext, true)}
        while(true){
            if(service==null){
                enabled = false
                bindShizuku = checkShizukuStatus()==1
            }else{
                enabled = true
                bindShizuku = false
            }
            delay(200)
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        AnimatedVisibility(bindShizuku) {
            Button(
                onClick = {
                    userServiceControl(myContext, true)
                    outputText = ""
                }
            ){
                Text(stringResource(R.string.bind_shizuku))
            }
        }

        Button(
            onClick = {
                outputText = checkPermission(myContext)
                coScope.launch {
                    outputTextScrollState.animateScrollTo(0, scrollAnim())
                }
            }
        ) {
            Text(text = stringResource(R.string.check_shizuku))
        }
        
        Button(
            onClick = {
                coScope.launch{
                    outputText = service!!.execute("dpm list-owners")
                    outputTextScrollState.animateScrollTo(0, scrollAnim())
                }
            },
            enabled = enabled
        ) {
            Text(text = stringResource(R.string.list_owners))
        }
        Spacer(Modifier.padding(vertical = 5.dp))

        AnimatedVisibility(showDeviceAdminButton&&showProfileOwnerButton&&showDeviceOwnerButton) {
            Button(
                onClick = {
                    coScope.launch{
                        outputText = service!!.execute(myContext.getString(R.string.dpm_activate_da_command))
                        outputTextScrollState.animateScrollTo(0, scrollAnim())
                        delay(500)
                        showDeviceAdminButton = !myDpm.isAdminActive(myComponent)
                    }
                },
                enabled = enabled
            ) {
                Text(text = stringResource(R.string.activate_device_admin))
            }
        }

        AnimatedVisibility(showProfileOwnerButton&&showDeviceOwnerButton) {
            Button(
                onClick = {
                    coScope.launch{
                        outputText = service!!.execute(myContext.getString(R.string.dpm_activate_po_command))
                        outputTextScrollState.animateScrollTo(0, scrollAnim())
                        delay(600)
                        showProfileOwnerButton = !isProfileOwner(myDpm)
                    }
                },
                enabled = enabled
            ) {
                Text(text = stringResource(R.string.activate_profile_owner))
            }
        }

        AnimatedVisibility(showDeviceOwnerButton&&showProfileOwnerButton) {
            Button(
                onClick = {
                    coScope.launch{
                        outputText = service!!.execute(myContext.getString(R.string.dpm_activate_do_command))
                        outputTextScrollState.animateScrollTo(0, scrollAnim())
                        delay(500)
                        showDeviceOwnerButton = !isDeviceOwner(myDpm)
                    }
                },
                enabled = enabled
            ) {
                Text(text = stringResource(R.string.activate_device_owner))
            }
        }
        
        if(
            VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)&&!myDpm.isOrganizationOwnedDeviceWithManagedProfile
        ){
            AnimatedVisibility(showOrgProfileOwnerButton) {
                Button(
                    onClick = {
                        coScope.launch{
                            val userID = Binder.getCallingUid() / 100000
                            outputText = service!!.execute(
                                "dpm mark-profile-owner-on-organization-owned-device --user $userID com.bintianqi.owndroid/com.bintianqi.owndroid.Receiver"
                            )
                            outputTextScrollState.animateScrollTo(0, scrollAnim())
                            delay(500)
                            showOrgProfileOwnerButton = !myDpm.isOrganizationOwnedDeviceWithManagedProfile
                        }
                    },
                    enabled = enabled
                ) {
                    Text(text = stringResource(R.string.activate_org_profile))
                }
            }
        }
        
        SelectionContainer(modifier = Modifier
            .align(Alignment.Start)
            .horizontalScroll(outputTextScrollState)){
            Text(text = outputText, softWrap = false, modifier = Modifier.padding(4.dp))
        }
        
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Stable
fun <T> scrollAnim(
    dampingRatio: Float = Spring.DampingRatioNoBouncy,
    stiffness: Float = Spring.StiffnessMedium,
    visibilityThreshold: T? = null
): SpringSpec<T> = SpringSpec(dampingRatio, stiffness, visibilityThreshold)

private fun checkPermission(context: Context):String{
    if(checkShizukuStatus()==-1){return context.getString(R.string.shizuku_not_started)}
    val getUid = if(service==null){return context.getString(R.string.shizuku_not_bind)}else{service!!.uid}
    return when(getUid){
        "2000"->context.getString(R.string.shizuku_activated_shell)
        "0"->context.getString(R.string.shizuku_activated_root)
        else->context.getString(R.string.unknown_status)+"\nUID: $getUid"
    }
}

fun checkShizukuStatus():Int{
    val status = try {
        if(Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) { waitGrantPermission = false; 1 }
        else if(Shizuku.shouldShowRequestPermissionRationale()) { 0 }
        else{
            if(!waitGrantPermission){Shizuku.requestPermission(0)}
            waitGrantPermission = true
            0
        }
    }catch(e:Exception){ -1 }
    return status
}

fun userServiceControl(context:Context, status:Boolean){
    if(checkShizukuStatus()!=1){ return }
    val userServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
            if (binder.pingBinder()) {
                service = IUserService.Stub.asInterface(binder)
            } else {
                Toast.makeText(context, R.string.invalid_binder, Toast.LENGTH_SHORT).show()
            }
        }
        override fun onServiceDisconnected(componentName: ComponentName) {
            service = null
            Toast.makeText(context, R.string.shizuku_service_disconnected, Toast.LENGTH_SHORT).show()
        }
    }
    val userServiceArgs  = Shizuku.UserServiceArgs(
        ComponentName(
            context.packageName,ShizukuService::class.java.name
        )
    )
        .daemon(false)
        .processNameSuffix("service")
        .debuggable(true)
        .version(26)
    if(status){
        Shizuku.bindUserService(userServiceArgs,userServiceConnection)
    }else{
        Shizuku.unbindUserService(userServiceArgs,userServiceConnection,false)
    }
}
