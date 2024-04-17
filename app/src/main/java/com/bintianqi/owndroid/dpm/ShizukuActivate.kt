package com.bintianqi.owndroid.dpm

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
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
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Receiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.IOUtils
import java.io.*

@Composable
fun ShizukuActivate(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext, Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    val filesDir = myContext.filesDir
    LaunchedEffect(Unit){ extractRish(myContext) }
    val coScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val outputTextScrollState = rememberScrollState()
    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        var outputText by remember{mutableStateOf("")}
        if(Binder.getCallingUid()/100000!=0){
            Row{
                Icon(imageVector = Icons.Rounded.Warning, contentDescription = null, tint = colorScheme.onErrorContainer)
                Text(text = stringResource(R.string.not_primary_user_not_support_shizuku), color = colorScheme.onErrorContainer)
            }
        }
        Button(
            onClick = {
                coScope.launch {
                    scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                    outputTextScrollState.animateScrollTo(0, scrollAnim())
                    val getUid = executeCommand(myContext, "sh rish.sh","id -u",null,filesDir)
                    outputText = if(getUid.contains("2000")){
                        myContext.getString(R.string.shizuku_activated_shell)
                    }else if(getUid.contains("0")){
                        myContext.getString(R.string.shizuku_activated_root)
                    }else if(getUid.contains("Error: 1")){
                        myContext.getString(R.string.shizuku_not_started)
                    }else{
                        getUid
                    }
                }
            }
        ) {
            Text(text = stringResource(R.string.check_shizuku))
        }
        
        Button(
            onClick = {
                coScope.launch{
                    outputText= executeCommand(myContext, "sh rish.sh","dpm list-owners",null,filesDir)
                    scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                    outputTextScrollState.animateScrollTo(0, scrollAnim())
                }
            }
        ) {
            Text(text = stringResource(R.string.list_owners))
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        
        if(!isDeviceOwner(myDpm)&&!isProfileOwner(myDpm)){
            Column {
                if(!myDpm.isAdminActive(myComponent)){
                    Button(
                        onClick = {
                            coScope.launch{
                                outputText = executeCommand(myContext, "sh rish.sh", myContext.getString(R.string.dpm_activate_da_command), null, filesDir)
                                scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                                outputTextScrollState.animateScrollTo(0, scrollAnim())
                            }
                        }
                    ) {
                        Text(text = stringResource(R.string.activate_device_admin))
                    }
                }
                
                Button(
                    onClick = {
                        coScope.launch{
                            outputText = executeCommand(myContext, "sh rish.sh", myContext.getString(R.string.dpm_activate_po_command), null, filesDir)
                            scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                            outputTextScrollState.animateScrollTo(0, scrollAnim())
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.activate_profile_owner))
                }
                
                Button(
                    onClick = {
                        coScope.launch{
                            outputText = executeCommand(myContext, "sh rish.sh", myContext.getString(R.string.dpm_activate_do_command), null, filesDir)
                            scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                            outputTextScrollState.animateScrollTo(0, scrollAnim())
                        }
                    }
                ) {
                    Text(text = stringResource(R.string.activate_device_owner))
                }
                
            }
        }
        
        if(
            VERSION.SDK_INT>=30&&!isDeviceOwner(myDpm)&&!myDpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)&&
            !myDpm.isOrganizationOwnedDeviceWithManagedProfile
        ){
            Column {
                Text(text = stringResource(R.string.org_owned_work_profile), style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                Text(text = stringResource(R.string.input_userid_of_work_profile))
                var inputUserID by remember{mutableStateOf("")}
                OutlinedTextField(
                    value = inputUserID, onValueChange = {inputUserID=it},
                    label = {Text("UserID")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp)
                )
                Button(
                    onClick = {
                        coScope.launch{
                            focusMgr.clearFocus()
                            outputText = executeCommand(
                                myContext, "sh rish.sh", myContext.getString(R.string.activate_org_profile_command_with_user_id, inputUserID),
                                null, filesDir
                            )
                            scrollState.animateScrollTo(scrollState.maxValue, scrollAnim())
                            outputTextScrollState.animateScrollTo(0, scrollAnim())
                            if(myDpm.isOrganizationOwnedDeviceWithManagedProfile){
                                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = stringResource(R.string.activate))
                }
            }
        }
        
        SelectionContainer(modifier = Modifier.align(Alignment.Start).horizontalScroll(outputTextScrollState)){
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

suspend fun executeCommand(myContext: Context, command: String, subCommand:String, env: Array<String>?, dir:File?): String {
    var result = ""
    val tunnel:ByteArrayInputStream
    val process:Process
    val outputStream:OutputStream
    try {
        tunnel = ByteArrayInputStream(subCommand.toByteArray())
        process = withContext(Dispatchers.IO){Runtime.getRuntime().exec(command, env, dir)}
        outputStream = process.outputStream
        IOUtils.copy(tunnel,outputStream)
        withContext(Dispatchers.IO){ outputStream.close() }
        val exitCode = withContext(Dispatchers.IO){ process.waitFor() }
        if(exitCode!=0){ result+="Error: $exitCode" }
    }catch(e:Exception){
        e.printStackTrace()
        return e.toString()
    }
    try {
        val outputReader = BufferedReader(InputStreamReader(process.inputStream))
        var outputLine: String
        while(withContext(Dispatchers.IO){ outputReader.readLine() }.also {outputLine = it}!=null) { result+="$outputLine\n" }
        val errorReader = BufferedReader(InputStreamReader(process.errorStream))
        var errorLine: String
        while(withContext(Dispatchers.IO){ errorReader.readLine() }.also {errorLine = it}!=null) { result+="$errorLine\n" }
    } catch(e: NullPointerException) {
        e.printStackTrace()
    }
    if(result==""){ return myContext.getString(R.string.try_again) }
    return result
}
