package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
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
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import java.util.concurrent.Executors

@Composable
fun ApplicationManage(){
    val myContext = LocalContext.current
    val focusMgr = LocalFocusManager.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    var pkgName by rememberSaveable{ mutableStateOf("") }
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium }else{ typography.bodyLarge }
    Column{
        if(!isWear){
            TextField(
                value = pkgName,
                onValueChange = { pkgName = it },
                label = { Text("包名") },
                enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
            )
        }
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        if(isWear){
        TextField(
            value = pkgName,
            onValueChange = { pkgName = it },
            label = { Text("包名") },
            enabled = isDeviceOwner(myDpm)|| isProfileOwner(myDpm),
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp,vertical = 2.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        }
        if(VERSION.SDK_INT>=24){
            val isSuspended = { try{ myDpm.isPackageSuspended(myComponent,pkgName) }catch(e:NameNotFoundException){ false } }
            AppManageItem(R.string.suspend,R.string.place_holder, isSuspended) { b -> myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName), b) }
        }
        AppManageItem(R.string.hide,R.string.isapphidden_desc, {myDpm.isApplicationHidden(myComponent,pkgName)}, {b -> myDpm.setApplicationHidden(myComponent,pkgName,b)})
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
        
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            Column(modifier = sections()){
                var state by remember{mutableStateOf(myDpm.isUninstallBlocked(myComponent,pkgName))}
                Text(text = "防卸载", style = typography.titleLarge)
                Text("当前状态：${if(state){"打开"}else{"关闭"}}")
                Text("有时候无法正确获取防卸载状态")
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            myDpm.setUninstallBlocked(myComponent,pkgName,true)
                            state = myDpm.isUninstallBlocked(myComponent,pkgName)
                        },
                        modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.49F)}
                    ) {
                        Text("打开")
                    }
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            myDpm.setUninstallBlocked(myComponent,pkgName,false)
                            state = myDpm.isUninstallBlocked(myComponent,pkgName)
                        },
                        modifier = if(isWear){Modifier}else{Modifier.fillMaxWidth(0.96F)}){
                        Text("关闭")
                    }
                }
            }
        }
        
        if(VERSION.SDK_INT>=30&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var pkgList = myDpm.getUserControlDisabledPackages(myComponent)
                var listText by remember{mutableStateOf("")}
                val refresh = {
                    pkgList = myDpm.getUserControlDisabledPackages(myComponent)
                    listText = ""
                    var count = pkgList.size
                    for(pkg in pkgList){
                        count-=1
                        listText+=pkg
                        if(count>0){listText+="\n"}
                    }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){refresh();inited=true}
                Text(text = "禁止用户控制", style = typography.titleLarge)
                Text(text = "用户将无法清除应用的存储空间和缓存", style = bodyTextStyle)
                Text(text = "应用列表：")
                if(listText!=""){
                    Text(text = listText, style = bodyTextStyle)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            if(pkgName!=""){
                                pkgList.add(pkgName)
                                myDpm.setUserControlDisabledPackages(myComponent,pkgList)
                                refresh()
                            }else{
                                Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ){
                        Text("添加")
                    }
                    Button(
                        onClick = {
                            val result = if(pkgName!=""){pkgList.remove(pkgName)}else{false}
                            if(result){
                                myDpm.setUserControlDisabledPackages(myComponent,pkgList)
                                refresh()
                            }else{
                                Toast.makeText(myContext, "不存在", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ){
                        Text("移除")
                    }
                }
                Button(
                    onClick = {
                        myDpm.setUserControlDisabledPackages(myComponent, listOf())
                        refresh()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("清空列表")
                }
            }
        }
        
        if(VERSION.SDK_INT>=23&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            val grantState = mapOf(
                PERMISSION_GRANT_STATE_DEFAULT to "由用户决定",
                PERMISSION_GRANT_STATE_GRANTED to "允许",
                PERMISSION_GRANT_STATE_DENIED to "拒绝"
            )
            Column(modifier = sections()){
                var inputPermission by remember{mutableStateOf("android.permission.")}
                var currentState by remember{mutableStateOf(grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)])}
                Text(text = "权限管理", style = typography.titleLarge)
                Text(text = "查看系统支持的权限：adb shell pm list permissions", style = bodyTextStyle)
                OutlinedTextField(
                    value = inputPermission,
                    label = { Text("权限")},
                    onValueChange = {inputPermission = it},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                Text("当前状态：$currentState", style = bodyTextStyle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            myDpm.setPermissionGrantState(myComponent,pkgName,inputPermission, PERMISSION_GRANT_STATE_GRANTED)
                            currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("允许")
                    }
                    Button(
                        onClick = {
                            myDpm.setPermissionGrantState(myComponent,pkgName,inputPermission, PERMISSION_GRANT_STATE_DENIED)
                            currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]
                        },
                        Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("拒绝")
                    }
                }
                Button(
                    onClick = {
                        myDpm.setPermissionGrantState(myComponent,pkgName,inputPermission, PERMISSION_GRANT_STATE_DEFAULT)
                        currentState = grantState[myDpm.getPermissionGrantState(myComponent,pkgName,inputPermission)]
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("由用户决定")
                }
                Text(text ="设为允许或拒绝后，用户不能改变状态", style = bodyTextStyle)
            }
        }
        
        if(isProfileOwner(myDpm)||isDeviceOwner(myDpm)){
            Column(modifier = sections()) {
                Text(text = "许可的无障碍应用", style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
                var list = mutableListOf("")
                var listText by remember{ mutableStateOf("") }
                val refreshList = {
                    list = myDpm.getPermittedAccessibilityServices(myComponent) ?: mutableListOf("")
                    listText = ""
                    var count = list.size
                    for(eachAccessibility in list) {
                        count -= 1
                        listText += eachAccessibility
                        if(count>0) { listText += "\n" }
                    }
                }
                refreshList()
                Text(text = listText, style = bodyTextStyle)
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            list.plus(pkgName)
                            Toast.makeText(myContext, if(myDpm.setPermittedAccessibilityServices(myComponent,list)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                            refreshList()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("添加")
                    }
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            list.remove(pkgName)
                            Toast.makeText(myContext, if(myDpm.setPermittedAccessibilityServices(myComponent,list)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                            refreshList()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("移除")
                    }
                }
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
            Text(text = imeListText, style = bodyTextStyle)
            Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
                Button(
                    onClick = {
                        imeList.plus(pkgName)
                        focusMgr.clearFocus()
                        Toast.makeText(myContext, if(myDpm.setPermittedInputMethods(myComponent, imeList)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                        refreshList()
                    },
                    modifier = Modifier.fillMaxWidth(0.49F),
                    enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
                ) {
                    Text("添加")
                }
                Button(
                    onClick = {
                        imeList.remove(pkgName)
                        focusMgr.clearFocus()
                        Toast.makeText(myContext, if(myDpm.setPermittedInputMethods(myComponent, imeList)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                        refreshList()
                    },
                    modifier = Modifier.fillMaxWidth(0.96F),
                    enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm)
                ) {
                    Text("移除")
                }
            }
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
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text("清除")
                }
                Button(
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                        intent.setData(Uri.parse("package:$pkgName"))
                        startActivity(myContext,intent,null)
                    },
                    modifier = Modifier.fillMaxWidth(0.96F)
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
