package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build.VERSION
import android.os.UserManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.apache.commons.io.IOUtils
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStreamReader

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
    Column(modifier = Modifier.verticalScroll(rememberScrollState())){
        var outputText by remember{mutableStateOf("")}
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            Row(modifier = sections(colorScheme.errorContainer), verticalAlignment = Alignment.CenterVertically){
                Icon(imageVector = Icons.Rounded.Warning, contentDescription = null, tint = colorScheme.onErrorContainer)
                Text(text = "暂不支持在工作资料中使用Shizuku", style = bodyTextStyle, color = colorScheme.onErrorContainer)
            }
        }
        Button(onClick = {outputText=checkPermission()}, enabled = VERSION.SDK_INT>=24, modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text(text = "检查权限")
        }
        Column(modifier = sections()){
            Text(text = "激活", style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
            Button(
                onClick = {
                    extractRish(myContext)
                    outputText = executeCommand("sh rish.sh", "dpm set-active-admin com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver", null, filesDir)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Device admin")
            }
            Button(
                onClick = {
                    extractRish(myContext)
                    outputText = executeCommand("sh rish.sh", "dpm set-profile-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver", null, filesDir)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Profile owner")
            }
            Button(
                onClick = {
                    extractRish(myContext)
                    outputText = executeCommand("sh rish.sh", "dpm set-device-owner com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver", null, filesDir)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Device owner")
            }
        }
        
        if(VERSION.SDK_INT>=30&&!myDpm.isProvisioningAllowed(DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE)){
            Column(modifier = sections()){
                Text(text = "组织拥有工作资料", style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                Text(text = "请输入工作资料的UserID", style = bodyTextStyle)
                var inputUserID by remember{mutableStateOf("")}
                OutlinedTextField(
                    value = inputUserID, onValueChange = {inputUserID=it},
                    label = {Text("UserID")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                Button(
                    onClick = {
                        extractRish(myContext)
                        outputText = executeCommand(
                            "sh rish.sh",
                            "dpm mark-profile-owner-on-organization-owned-device --user $inputUserID com.binbin.androidowner/com.binbin.androidowner.MyDeviceAdminReceiver",
                            null, filesDir
                        )
                        if(myDpm.isOrganizationOwnedDeviceWithManagedProfile){
                            Toast.makeText(myContext,"成功",Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "激活")
                }
            }
        }
        Button(
            onClick = {
                extractRish(myContext)
                outputText = executeCommand("sh rish.sh", "pwd", null, filesDir)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "test")
        }
        SelectionContainer(modifier = Modifier.padding(3.dp)){
            Text(text = outputText, style = bodyTextStyle, softWrap = false, modifier = Modifier.horizontalScroll(rememberScrollState()))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

fun extractRish(myContext:Context){
    val assetsMgr = myContext.assets
    val fileList = myContext.fileList()
    if("rish.sh" !in fileList){
        val shInput = assetsMgr.open("rish.sh")
        val shOutput = myContext.openFileOutput("rish.sh",MODE_PRIVATE)
        IOUtils.copy(shInput,shOutput)
        shOutput.close()
    }
    if("rish_shizuku.dex" !in fileList){
        val dexInput = assetsMgr.open("rish_shizuku.dex")
        val dexOutput = myContext.openFileOutput("rish_shizuku.dex",MODE_PRIVATE)
        IOUtils.copy(dexInput,dexOutput)
        dexOutput.close()
    }
}

fun deleteRish(myContext: Context){
    myContext.deleteFile("rish.sh")
    myContext.deleteFile("rish_shizuku.dex")
}

private fun checkPermission():String {
    if(Shizuku.isPreV11()) {
        return "有可能不支持v11以下的Shizuku\n你仍然可以尝试使用这些功能"
    }else{
        try {
            if(Shizuku.checkSelfPermission()==PackageManager.PERMISSION_GRANTED) {
                val permission = when(Shizuku.getUid()){
                    0->"Root"
                    2000->"Shell"
                    else->"未知权限"
                }
                return "Shizuku v${Shizuku.getVersion()}\n已授权($permission)"
            } else if(Shizuku.shouldShowRequestPermissionRationale()) {
                return "用户拒绝"
            } else {
                Shizuku.requestPermission(0)
            }
        } catch(e: Throwable) {
            return "服务未启动"
        }
    }
    return "未知"
}

fun executeCommand(command: String, subCommand:String, env: Array<String>?, dir:File): String {
    val output = StringBuilder()
    try {
        val tunnel = ByteArrayInputStream(subCommand.toByteArray())
        val process = Runtime.getRuntime().exec(command,env,dir)
        val outputStream = process.outputStream
        IOUtils.copy(tunnel,outputStream)
        outputStream.close()
        process.waitFor()
        val reader = BufferedReader(InputStreamReader(process.inputStream))
        var line: String
        while(reader.readLine().also {line = it}!=null) { output.append(line+"\n") }
    } catch(e: Exception) {
        e.printStackTrace()
    }
    return output.toString()
}
