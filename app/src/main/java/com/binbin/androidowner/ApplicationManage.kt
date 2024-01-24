package com.binbin.androidowner

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build.VERSION
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import java.io.IOException
import java.io.InputStream


@Composable
fun ApplicationManage(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    var pkgName by remember { mutableStateOf("") }
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = pkgName,
            onValueChange = {
                pkgName = it
            },
            label = { Text("包名") },
            enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp)
        )
        if(VERSION.SDK_INT>=24){
            val isSuspended = {
                try{
                    myDpm.isPackageSuspended(myComponent,pkgName)
                }catch(e:NameNotFoundException){
                    false
                }
            }
            AppManageItem(R.string.suspend,R.string.place_holder,myDpm, isSuspended,
                {b -> myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName) ,b)})
        }
        AppManageItem(R.string.hide,R.string.isapphidden_desc,myDpm, {myDpm.isApplicationHidden(myComponent,pkgName)},
            {b -> myDpm.setApplicationHidden(myComponent,pkgName,b)})
        if(VERSION.SDK_INT>=30){
            AppManageItem(R.string.user_ctrl_disabled,R.string.user_ctrl_disabled_desc,myDpm, {pkgName in myDpm.getUserControlDisabledPackages(myComponent)},
                {b->myDpm.setUserControlDisabledPackages(myComponent, mutableListOf(if(b){pkgName}else{null}))})
        }
        /*AppManageItem(R.string.block_unins,R.string.sometimes_not_available,myDpm, {myDpm.isUninstallBlocked(myComponent,pkgName)},
            {b -> myDpm.setUninstallBlocked(myComponent,pkgName,b)})*/
        Row(
            modifier = sections(),
            horizontalArrangement = if(!sharedPref.getBoolean("isWear",false)){Arrangement.SpaceAround}else{Arrangement.SpaceBetween}
        ) {
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,false)}, enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm)) {
                Text("允许卸载")
            }
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,true)}, enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm)) {
                Text("防卸载")
            }
        }
        Column(modifier = sections()) {
            Text(text = "许可的输入法", style = MaterialTheme.typography.titleLarge)
            var imeList = mutableListOf<String>()
            var imeListText by remember{ mutableStateOf("") }
            val refreshList = {
                if(isProfileOwner(myDpm) || isDeviceOwner(myDpm)){
                    if(myDpm.getPermittedInputMethods(myComponent)!=null){
                        imeList = myDpm.getPermittedInputMethods(myComponent)!!
                    }
                }
                imeListText = ""
                for(eachIme in imeList){
                    imeListText += "$eachIme \n"
                    Log.e("",eachIme)
                }
            }
            refreshList()
            Text(imeListText)
            Button(
                onClick = {
                    imeList.plus(pkgName)
                    myDpm.setPermittedInputMethods(myComponent, imeList)
                    refreshList()
                }
            ) {
                Text("设为许可的输入法")
            }
            Button(
                onClick = {
                    imeList.remove(pkgName)
                    myDpm.setPermittedInputMethods(myComponent,imeList)
                    refreshList()
                }
            ) {
                Text("从列表中移除")
            }
        }
        /*Button(
            onClick = {
                uninstallPkg(pkgName,myContext)
            }) {
            Text("卸载")
        }*/
        Spacer(Modifier.padding(20.dp))
    }
}

@Composable
private fun AppManageItem(
    itemName:Int,
    itemDesc:Int,
    myDpm: DevicePolicyManager,
    getMethod:()->Boolean,
    setMethod:(b:Boolean)->Unit
){
    var isEnabled by remember{ mutableStateOf(false) }
    if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
        isEnabled = getMethod()
    }
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    if(!sharedPref.getBoolean("isWear",false)){
    Row(
        modifier = sections(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(itemName),
                style = MaterialTheme.typography.titleLarge
            )
            if(itemDesc!=R.string.place_holder){
                Text(stringResource(itemDesc))
            }
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = {
                setMethod(!isEnabled)
                isEnabled = getMethod()
            },
            enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm)
        )
    }}else{
        Column(
            modifier = sections()
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(itemName)
                )
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        setMethod(!isEnabled)
                        isEnabled = getMethod()
                    },
                    enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm)
                )
            }
            if(itemDesc!=R.string.place_holder){
                Text(text = stringResource(itemDesc),
                    style = if(!sharedPref.getBoolean("isWear",false)){MaterialTheme.typography.bodyLarge}else{MaterialTheme.typography.bodyMedium})
            }
        }
    }
}

fun uninstallPkg(pkgName:String,myContext:Context){
    val packageManager = myContext.packageManager
    try {
        val packageInfo = packageManager.getPackageInfo(pkgName, 0)
        val intent = Intent(Intent.ACTION_DELETE)
        intent.setData(Uri.parse("package:" + packageInfo.packageName))
        startActivity(myContext,intent,null)
    } catch (e: NameNotFoundException) {
        Toast.makeText(myContext, "应用未安装", Toast.LENGTH_SHORT).show()
    }
}

private fun uninstallApp(context: Context, packageName: String) {
    val packageUri = Uri.parse("package:$packageName")
    val uninstallIntent = Intent(Intent.ACTION_DELETE, packageUri)
    uninstallIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(uninstallIntent)
}

fun isAppInstalled(context: Context, packageName: String): Boolean {
    return try {
        context.packageManager.getApplicationInfo(packageName, 0)
        true
    } catch (e: NameNotFoundException) {
        false
    }
}

@Throws(IOException::class)
fun installPackage(context: Context, inputStream: InputStream, packageName: String?): Boolean {
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(
        PackageInstaller.SessionParams.MODE_FULL_INSTALL
    )
    params.setAppPackageName(packageName)
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)
    val out = session.openWrite("COSU", 0, -1)
    val buffer = ByteArray(65536)
    var c: Int
    while (inputStream.read(buffer).also { c = it } != -1) {
        out.write(buffer, 0, c)
    }
    session.fsync(out)
    inputStream.close()
    out.close()
    session.commit(createIntentSender(context, sessionId))
    return true
}

private fun createIntentSender(context: Context, sessionId: Int): IntentSender {
    val pendingIntent = PendingIntent.getBroadcast(context, sessionId, Intent(), PendingIntent.FLAG_IMMUTABLE)
    return pendingIntent.intentSender
}
