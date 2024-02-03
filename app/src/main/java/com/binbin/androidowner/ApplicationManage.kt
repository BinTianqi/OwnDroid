package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build.VERSION
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import java.util.concurrent.Executors


@Composable
fun ApplicationManage(){
    val myContext = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    var pkgName by remember { mutableStateOf("") }
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium }else{ typography.bodyLarge }
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        TextField(
            value = pkgName,
            onValueChange = { pkgName = it },
            label = { Text("包名") },
            enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
            modifier = Modifier.fillMaxWidth().padding(horizontal = if(isWear){2.dp}else{12.dp},vertical = if(isWear){2.dp}else{6.dp}),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        if(VERSION.SDK_INT>=24){
            val isSuspended = { try{ myDpm.isPackageSuspended(myComponent,pkgName) }catch(e:NameNotFoundException){ false } }
            AppManageItem(R.string.suspend,R.string.place_holder, isSuspended) { b -> myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName), b) }
        }
        AppManageItem(R.string.hide,R.string.isapphidden_desc, {myDpm.isApplicationHidden(myComponent,pkgName)}, {b -> myDpm.setApplicationHidden(myComponent,pkgName,b)})
        if(VERSION.SDK_INT>=30){
            AppManageItem(R.string.user_ctrl_disabled,R.string.user_ctrl_disabled_desc,{pkgName in myDpm.getUserControlDisabledPackages(myComponent)},
                {b->myDpm.setUserControlDisabledPackages(myComponent, mutableListOf(if(b){pkgName}else{null}))})
        }
        if(VERSION.SDK_INT>=24){
            AppManageItem(
                R.string.always_on_vpn,R.string.experimental_feature,{pkgName == myDpm.getAlwaysOnVpnPackage(myComponent)},
                {b ->
                    try{ myDpm.setAlwaysOnVpnPackage(myComponent,pkgName,b) }
                    catch(e:java.lang.UnsupportedOperationException){ Toast.makeText(myContext, "不支持", Toast.LENGTH_SHORT).show() }
                    catch(e:NameNotFoundException){ Toast.makeText(myContext, "未安装", Toast.LENGTH_SHORT).show() }
                }
            )
        }
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = if(isWear){sections().horizontalScroll(rememberScrollState())}else{sections()}
        ) {
            Button(onClick = {focusMgr.clearFocus();myDpm.setUninstallBlocked(myComponent,pkgName,false)}, enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
                modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.48F)}) {
                Text("允许卸载")
            }
            if(isWear){Spacer(Modifier.padding(horizontal = 3.dp))}
            Button(onClick = {focusMgr.clearFocus();myDpm.setUninstallBlocked(myComponent,pkgName,true)}, enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
                modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.92F)}) {
                Text("防卸载")
            }
        }
        Column(modifier = sections()) {
            Text(text = "许可的输入法", style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
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
                    focusMgr.clearFocus()
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
                    focusMgr.clearFocus()
                    myDpm.setPermittedInputMethods(myComponent,imeList)
                    refreshList()
                },
                modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.92F)},
                enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
            ) {
                Text("从列表中移除")
            }}
        }
        Column(modifier = sections()){
            Text(text = "清除应用存储", style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                Button(
                    onClick = {
                        val executor = Executors.newCachedThreadPool()
                        val onClear = DevicePolicyManager.OnClearApplicationUserDataListener { pkg: String, succeed: Boolean ->
                            Looper.prepare()
                            focusMgr.clearFocus()
                            val toastText = if(pkg!=""){"$pkg\n"}else{""} + "数据清除" + if(succeed){"成功"}else{"失败"}
                            Toast.makeText(myContext, toastText, Toast.LENGTH_SHORT).show()
                            Looper.loop()
                        }
                        if(VERSION.SDK_INT>=28){
                            myDpm.clearApplicationUserData(myComponent,pkgName,executor,onClear)
                        }
                    },
                    enabled = (isDeviceOwner(myDpm)||isProfileOwner(myDpm))&&VERSION.SDK_INT>=28,
                    modifier = Modifier.fillMaxWidth(0.48F)
                ) {
                    Text("清除")
                }
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.setData(Uri.parse("package:$pkgName"))
                        startActivity(myContext,intent,null)
                    },
                    modifier = Modifier.fillMaxWidth(0.92F)
                ){
                    Text("详情")
                }
            }
            if(VERSION.SDK_INT<28){
                Text(text = "清除存储需API28", style = bodyTextStyle)
            }
        }
        if(VERSION.SDK_INT>=34){
            Button(
                onClick = {
                    try{
                        myDpm.setDefaultDialerApplication(pkgName)
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    }catch(e:IllegalArgumentException){
                        Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            ) {
                Text("设为默认拨号应用")
            }
        }
        Spacer(Modifier.padding(30.dp))
    }
}

@Composable
private fun AppManageItem(
    itemName:Int,
    itemDesc:Int,
    getMethod:()->Boolean,
    setMethod:(b:Boolean)->Unit
){
    val myDpm = LocalContext.current.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
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
            Text(text = stringResource(itemName), style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
            if(itemDesc!=R.string.place_holder){ Text(stringResource(itemDesc)) }
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
                Text(text = stringResource(itemName), fontWeight = FontWeight.SemiBold, style = typography.titleMedium)
                Switch(
                    checked = isEnabled,
                    onCheckedChange = {
                        setMethod(!isEnabled)
                        isEnabled = getMethod()
                        focusMgr.clearFocus()
                    },
                    enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm)
                )
            }
            if(itemDesc!=R.string.place_holder){ Text(text = stringResource(itemDesc), style = typography.bodyMedium) }
        }
    }
}
