package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity


@Composable
fun ApplicationManage(myDpm:DevicePolicyManager, myComponent:ComponentName,myContext:Context){
    var pkgName by remember { mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("以下功能都需要DeviceOwner权限")
        TextField(
            value = pkgName,
            onValueChange = {
                pkgName = it
            },
            label = { Text("包名") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
                .clip(RoundedCornerShape(15))
                .background(color = MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,false)}) {
                Text("取消防卸载")
            }
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,true)}) {
                Text("防卸载")
            }
        }
        Button(
            onClick = {
                uninstallApp(myContext,pkgName)
            }) {
            Text("卸载")
        }
        Spacer(Modifier.padding(5.dp))
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
    if(myDpm.isDeviceOwnerApp("com.binbin.androidowner")){
        isEnabled = getMethod()
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp)
            .clip(RoundedCornerShape(15))
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .padding(8.dp),
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
            enabled = myDpm.isDeviceOwnerApp("com.binbin.androidowner")
        )
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
