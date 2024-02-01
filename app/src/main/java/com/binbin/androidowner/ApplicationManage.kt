package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.os.Build.VERSION
import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
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


@Composable
fun ApplicationManage(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    var pkgName by remember { mutableStateOf("") }
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            value = pkgName,
            onValueChange = { pkgName = it },
            label = { Text("包名") },
            enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
            modifier = Modifier.fillMaxWidth().padding(horizontal = if(isWear){2.dp}else{12.dp},vertical = if(isWear){2.dp}else{6.dp})
        )
        if(VERSION.SDK_INT>=24){
            val isSuspended = { try{ myDpm.isPackageSuspended(myComponent,pkgName) }catch(e:NameNotFoundException){ false } }
            AppManageItem(R.string.suspend,R.string.place_holder,myDpm, isSuspended) { b -> myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName), b) }
        }
        AppManageItem(R.string.hide,R.string.isapphidden_desc,myDpm, {myDpm.isApplicationHidden(myComponent,pkgName)}, {b -> myDpm.setApplicationHidden(myComponent,pkgName,b)})
        if(VERSION.SDK_INT>=30){
            AppManageItem(R.string.user_ctrl_disabled,R.string.user_ctrl_disabled_desc,myDpm, {pkgName in myDpm.getUserControlDisabledPackages(myComponent)},
                {b->myDpm.setUserControlDisabledPackages(myComponent, mutableListOf(if(b){pkgName}else{null}))})
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = if(isWear){sections().horizontalScroll(rememberScrollState())}else{sections()}
        ) {
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,false)}, enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
                modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.48F)}) {
                Text("允许卸载")
            }
            if(isWear){Spacer(Modifier.padding(horizontal = 3.dp))}
            Button(onClick = {myDpm.setUninstallBlocked(myComponent,pkgName,true)}, enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
                modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.92F)}) {
                Text("防卸载")
            }
        }
        Column(modifier = sections()) {
            Text(text = "许可的输入法", style = typography.titleLarge,color = MaterialTheme.colorScheme.onPrimaryContainer)
            var imeList = mutableListOf<String>()
            var imeListText by remember{ mutableStateOf("") }
            val refreshList = {
                if(isProfileOwner(myDpm) || isDeviceOwner(myDpm)){
                    if(myDpm.getPermittedInputMethods(myComponent)!=null){
                        imeList = myDpm.getPermittedInputMethods(myComponent)!!
                    }
                }
                imeListText = ""
                for(eachIme in imeList){ imeListText += "$eachIme \n" }
            }
            refreshList()
            Text(imeListText)
            Row(modifier = if(!isWear){Modifier.fillMaxWidth()}else{Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())},horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    imeList.plus(pkgName)
                    myDpm.setPermittedInputMethods(myComponent, imeList)
                    refreshList()
                },
                modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.48F)},
                enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
            ) {
                Text("加入列表")
            }
            if(isWear){Spacer(Modifier.padding(horizontal = 2.dp))}
            Button(
                onClick = {
                    imeList.remove(pkgName)
                    myDpm.setPermittedInputMethods(myComponent,imeList)
                    refreshList()
                },
                modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.92F)},
                enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
            ) {
                Text("从列表中移除")
            }}
        }
        Spacer(Modifier.padding(30.dp))
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
    if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){ isEnabled = getMethod() }
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
                style = typography.titleLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
                Text(text = stringResource(itemDesc), style = if(!sharedPref.getBoolean("isWear",false)){typography.bodyLarge}else{typography.bodyMedium})
            }
        }
    }
}
