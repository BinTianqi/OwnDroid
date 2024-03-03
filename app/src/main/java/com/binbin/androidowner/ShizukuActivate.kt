package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.apache.commons.io.IOUtils
import rikka.shizuku.Shizuku
import java.io.*

@Composable
fun ShizukuActivate(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    val sharedPref = LocalContext.current.getSharedPreferences("data", MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium }else{ typography.bodyLarge }
    val filesDir = myContext.filesDir
    var launchExtractRish by remember{mutableStateOf(true)}
    LaunchedEffect(launchExtractRish){ if(launchExtractRish){ extractRish(myContext);launchExtractRish=false } }
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(scrollState)){
        var outputText by remember{mutableStateOf("")}
        if(Binder.getCallingUid()/100000!=0){
            Row(modifier = sections(colorScheme.errorContainer), verticalAlignment = Alignment.CenterVertically){
                Icon(imageVector = Icons.Rounded.Warning, contentDescription = null, tint = colorScheme.onErrorContainer)
                Text(text = stringResource(R.string.not_primary_user_not_support_shizuku), style = bodyTextStyle, color = colorScheme.onErrorContainer)
            }
        }
        
        var launchPermissionCheck by remember{mutableStateOf(false)}
        LaunchedEffect(launchPermissionCheck){
            if(launchPermissionCheck){
                outputText = checkPermission(myContext)
                scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                launchPermissionCheck=false
            }
        }
        Button(
            onClick = {launchPermissionCheck=true},
            enabled = VERSION.SDK_INT>=24, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Text(text = stringResource(R.string.check_permission))
        }
        
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            Column(modifier = sections()){
                Text(text = stringResource(R.string.activate), style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                
                if(!myDpm.isAdminActive(myComponent)){
                    var launchActivateDA by remember{mutableStateOf(false)}
                    LaunchedEffect(launchActivateDA){
                        if(launchActivateDA){
                            outputText = executeCommand("sh rish.sh", "dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver", null, filesDir)
                            scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                            launchActivateDA=false
                        }
                    }
                    Button(onClick = {launchActivateDA=true}, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Device admin")
                    }
                }
                
                var launchActivatePO by remember{mutableStateOf(false)}
                LaunchedEffect(launchActivatePO){
                    if(launchActivatePO){
                        outputText = executeCommand("sh rish.sh", "dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver", null, filesDir)
                        scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                        launchActivatePO=false
                    }
                }
                Button(onClick = {launchActivatePO=true}, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Profile owner")
                }
                
                var launchActivateDO by remember{mutableStateOf(false)}
                LaunchedEffect(launchActivateDO){
                    if(launchActivateDO){
                        outputText = executeCommand("sh rish.sh", "dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver", null, filesDir)
                        scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                        launchActivateDO=false
                    }
                }
                Button(onClick = {launchActivateDO=true}, modifier = Modifier.fillMaxWidth()) {
                    Text(text = "Device owner")
                }
                
            }
        }
        
        if(VERSION.SDK_INT>=30&&!isDeviceOwner(myDpm)&&!myDpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)&&!myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            Column(modifier = sections()){
                Text(text = stringResource(R.string.org_owned_work_profile), style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                Text(text = stringResource(R.string.input_userid_of_work_profile), style = bodyTextStyle)
                var inputUserID by remember{mutableStateOf("")}
                OutlinedTextField(
                    value = inputUserID, onValueChange = {inputUserID=it},
                    label = {Text("UserID")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp)
                )
                var launchActivateOrgProfile by remember{mutableStateOf(false)}
                LaunchedEffect(launchActivateOrgProfile){
                    if(launchActivateOrgProfile){
                        focusMgr.clearFocus()
                        outputText = executeCommand(
                            "sh rish.sh",
                            "dpm mark-profile-owner-on-organization-owned-device --user $inputUserID com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                            null, filesDir
                        )
                        scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                        launchActivateOrgProfile=false
                    }
                }
                Button(
                    onClick = {
                        launchActivateOrgProfile=true
                        if(myDpm.isOrganizationOwnedDeviceWithManagedProfile){
                            Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.activate))
                }
            }
        }
        
        var launchListOwners by remember{mutableStateOf(false)}
        LaunchedEffect(launchListOwners){
            if(launchListOwners){
                outputText=executeCommand("sh rish.sh","dpm list-owners",null,filesDir)
                scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                launchListOwners=false
            }
        }
        Button(
            onClick = {launchListOwners=true}, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ) {
            Text(text = stringResource(R.string.list_owners))
        }
        
        var launchTest by remember{mutableStateOf(false)}
        LaunchedEffect(launchTest){
            if(launchTest){
                outputText= myContext.getString(R.string.should_contain_2000_or_0)
                scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                outputText+=executeCommand("sh rish.sh","id",null,filesDir)
                launchTest=false
            }
        }
        Button(
            onClick = {launchTest=true}, modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = stringResource(R.string.test_rish))
        }
        
        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState())){
            Text(text = outputText, style = bodyTextStyle, softWrap = false, modifier = Modifier.padding(4.dp))
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

fun extractRish(myContext:Context){
    val assetsMgr = myContext.assets
    myContext.deleteFile("rish.sh")
    myContext.deleteFile("rish_shizuku.dex")
    val shInput = assetsMgr.open("rish.sh")
    val shOutput = myContext.openFileOutput("rish.sh",MODE_PRIVATE)
    IOUtils.copy(shInput,shOutput)
    shOutput.close()
    val dexInput = assetsMgr.open("rish_shizuku.dex")
    val dexOutput = myContext.openFileOutput("rish_shizuku.dex",MODE_PRIVATE)
    IOUtils.copy(dexInput,dexOutput)
    dexOutput.close()
    if(VERSION.SDK_INT>=34){ Runtime.getRuntime().exec("chmod 400 rish_shizuku.dex",null,myContext.filesDir) }
}

private fun checkPermission(myContext: Context):String {
    return if(Shizuku.isPreV11()) {
        myContext.getString(R.string.please_update_shizuku)
    }else{
        try{
            if(Shizuku.checkSelfPermission()==PackageManager.PERMISSION_GRANTED) {
                val permission = when(Shizuku.getUid()){ 0->"Root"; 2000->"Shell"; else->myContext.getString(R.string.unknown) }
                myContext.getString(R.string.shizuku_permission_granted, Shizuku.getVersion(), permission)
            }else if(Shizuku.shouldShowRequestPermissionRationale()){ myContext.getString(R.string.denied) }
            else{ Shizuku.requestPermission(0); myContext.getString(R.string.request_permission) }
        }catch(e: Throwable){ myContext.getString(R.string.shizuku_not_started) }
    }
}

fun executeCommand(command: String, subCommand:String, env: Array<String>?, dir:File?): String {
    var result = ""
    val tunnel:ByteArrayInputStream
    val process:Process
    val outputStream:OutputStream
    try {
        tunnel = ByteArrayInputStream(subCommand.toByteArray())
        process = Runtime.getRuntime().exec(command,env,dir)
        outputStream = process.outputStream
        IOUtils.copy(tunnel,outputStream)
        outputStream.close()
        val exitCode = process.waitFor()
        if(exitCode!=0){ result+="Error: $exitCode" }
    }catch(e:Exception){
        e.printStackTrace()
        return e.toString()
    }
    try {
        val outputReader = BufferedReader(InputStreamReader(process.inputStream))
        var outputLine: String
        while(outputReader.readLine().also {outputLine = it}!=null) { result+="$outputLine\n" }
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        var errorLine: String
        while(errorReader.readLine().also {errorLine = it}!=null) { result+="$errorLine\n" }
    } catch(e: NullPointerException) {
        e.printStackTrace()
    }
    return result
}
