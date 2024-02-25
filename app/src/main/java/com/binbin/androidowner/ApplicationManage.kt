package com.binbin.androidowner

import android.app.PendingIntent
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DEFAULT
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_DENIED
import android.app.admin.DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED
import android.app.admin.PackagePolicy
import android.app.admin.PackagePolicy.PACKAGE_POLICY_ALLOWLIST
import android.app.admin.PackagePolicy.PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM
import android.app.admin.PackagePolicy.PACKAGE_POLICY_BLOCKLIST
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.content.pm.PackageManager.NameNotFoundException
import android.net.Uri
import android.os.Build.VERSION
import android.os.Looper
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.focusable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import kotlinx.coroutines.delay
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.Executors

private var credentialList = mutableSetOf<String>()
private var crossProfilePkg = mutableSetOf<String>()
private var keepUninstallPkg = mutableListOf<String>()
private var permittedIme = mutableListOf<String>()
private var permittedAccessibility = mutableListOf<String>()
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
    val titleColor = colorScheme.onPrimaryContainer
    Column{
        if(!isWear){
            TextField(
                value = pkgName,
                onValueChange = { pkgName = it },
                label = { Text("包名") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
            )
        }
    Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
        if(isWear){
        TextField(
            value = pkgName,
            onValueChange = { pkgName = it },
            label = { Text("包名") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp,vertical = 2.dp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        }else{Spacer(Modifier.padding(vertical = 2.dp))}
        if(VERSION.SDK_INT>=24&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            Text(text = "作用域: 工作资料", style = bodyTextStyle, textAlign = TextAlign.Center,modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp))
        }
        
        Button(
            onClick = {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.setData(Uri.parse("package:$pkgName"))
                startActivity(myContext,intent,null)
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)
        ){
            Text("应用详情")
        }
        
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            AppManageItem(
                R.string.suspend,R.string.place_holder,
                {try{ myDpm.isPackageSuspended(myComponent,pkgName) }
                catch(e:NameNotFoundException){ false }
                catch(e:IllegalArgumentException){ false }}
            ) { b -> myDpm.setPackagesSuspended(myComponent, arrayOf(pkgName), b) }
        }
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            AppManageItem(R.string.hide,R.string.isapphidden_desc, {myDpm.isApplicationHidden(myComponent,pkgName)}) {b-> myDpm.setApplicationHidden(myComponent, pkgName, b)}
        }
        if(VERSION.SDK_INT>=24&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            AppManageItem(
                R.string.always_on_vpn,R.string.place_holder,{pkgName == myDpm.getAlwaysOnVpnPackage(myComponent)}) {b->
                try {
                    myDpm.setAlwaysOnVpnPackage(myComponent, pkgName, b)
                } catch(e: java.lang.UnsupportedOperationException) {
                    Toast.makeText(myContext, "不支持", Toast.LENGTH_SHORT).show()
                } catch(e: NameNotFoundException) {
                    Toast.makeText(myContext, "未安装", Toast.LENGTH_SHORT).show()
                }
            }
        }
        
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            Column(modifier = sections()){
                var state by remember{mutableStateOf(myDpm.isUninstallBlocked(myComponent,pkgName))}
                Text(text = "防卸载", style = typography.titleLarge, color = titleColor)
                Text("当前状态：${if(state){"打开"}else{"关闭"}}")
                Text(text = "有时候无法正确获取防卸载状态", style = bodyTextStyle)
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            myDpm.setUninstallBlocked(myComponent,pkgName,true)
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                            state = myDpm.isUninstallBlocked(myComponent,pkgName)
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("打开")
                    }
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            myDpm.setUninstallBlocked(myComponent,pkgName,false)
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                            state = myDpm.isUninstallBlocked(myComponent,pkgName)
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ){
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
                    for(pkg in pkgList){ count-=1; listText+=pkg; if(count>0){listText+="\n"} }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){refresh();inited=true}
                Text(text = "禁止用户控制", style = typography.titleLarge, color = titleColor)
                Text(text = "用户将无法清除应用的存储空间和缓存", style = bodyTextStyle)
                Text(text = "应用列表：")
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                    Text(text = if(listText==""){"无"}else{listText}, style = bodyTextStyle, color = titleColor)
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
                    onClick = { myDpm.setUserControlDisabledPackages(myComponent, listOf()); refresh() },
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
                Text(text = "权限管理", style = typography.titleLarge, color = titleColor)
                OutlinedTextField(
                    value = inputPermission,
                    label = { Text("权限")},
                    onValueChange = {inputPermission = it},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp)
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
            }
        }
        
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
            Column(modifier = sections()){
                Text(text = "跨资料应用", style = typography.titleLarge, color = titleColor)
                var list by remember{mutableStateOf("")}
                val refresh = {
                    crossProfilePkg = myDpm.getCrossProfilePackages(myComponent)
                    list = ""
                    var count = crossProfilePkg.size
                    for(each in crossProfilePkg){ count-=1; list+=each; if(count>0){list+="\n"} }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){refresh();inited=true}
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                    Text(text = if(list==""){"无"}else{list}, style = bodyTextStyle, color = titleColor)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            if(pkgName!=""){crossProfilePkg.add(pkgName)}
                            myDpm.setCrossProfilePackages(myComponent, crossProfilePkg)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("添加")
                    }
                    Button(
                        onClick = {
                            if(pkgName!=""){crossProfilePkg.remove(pkgName)}
                            myDpm.setCrossProfilePackages(myComponent, crossProfilePkg)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("移除")
                    }
                }
            }
        }
        
        if(isProfileOwner(myDpm)){
            Column(modifier = sections()){
                var pkgList: MutableList<String>
                var list by remember{mutableStateOf("")}
                val refresh = {
                    pkgList = myDpm.getCrossProfileWidgetProviders(myComponent)
                    list = ""
                    var count = pkgList.size
                    for(each in pkgList){ count-=1; list+=each; if(count>0){list+="\n"}}
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){refresh();inited=true}
                Text(text = "跨资料微件", style = typography.titleLarge, color = titleColor)
                Text(text = "(跨资料桌面小部件提供者)", style = bodyTextStyle)
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                    Text(text = if(list==""){"无"}else{list}, style = bodyTextStyle, color = titleColor)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            if(pkgName!=""){myDpm.addCrossProfileWidgetProvider(myComponent,pkgName)}
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ){
                        Text("添加")
                    }
                    Button(
                        onClick = {
                            if(pkgName!=""){myDpm.removeCrossProfileWidgetProvider(myComponent,pkgName)}
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ){
                        Text("移除")
                    }
                }
            }
        }
        
        if(VERSION.SDK_INT>=34&&isDeviceOwner(myDpm)){
            var policy:PackagePolicy?
            var policyType by remember{mutableIntStateOf(-1)}
            var credentialListText by remember{mutableStateOf("")}
            val refreshPolicy = {
                policy = myDpm.credentialManagerPolicy
                policyType = policy?.policyType ?: -1
                credentialList = policy?.packageNames ?: mutableSetOf()
                credentialList = credentialList.toMutableSet()
            }
            val refreshText = {
                credentialListText = ""
                var count = credentialList.size
                for(item in credentialList){ count-=1; credentialListText+=item; if(count>0){credentialListText+="\n"} }
            }
            var inited by remember{mutableStateOf(false)}
            if(!inited){refreshPolicy(); refreshText(); inited = true}
            Column(modifier = sections()){
                Text(text = "凭据管理策略", style = typography.titleLarge, color = titleColor)
                RadioButtonItem("无",{policyType==-1},{policyType=-1})
                RadioButtonItem("黑名单",{policyType==PACKAGE_POLICY_BLOCKLIST},{policyType=PACKAGE_POLICY_BLOCKLIST})
                RadioButtonItem("白名单",{policyType==PACKAGE_POLICY_ALLOWLIST},{policyType=PACKAGE_POLICY_ALLOWLIST})
                RadioButtonItem("白名单和系统应用",{policyType==PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM},{policyType=PACKAGE_POLICY_ALLOWLIST_AND_SYSTEM})
                AnimatedVisibility(policyType!=-1) {
                    Column {
                        Text("应用列表")
                        SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                            Text(text = if(credentialListText!=""){ credentialListText }else{ "无" }, style = bodyTextStyle, color = titleColor)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                            Button(
                                onClick = {
                                    if(pkgName!=""){credentialList.add(pkgName)}
                                    refreshText()
                                },
                                modifier = Modifier.fillMaxWidth(0.49F)
                            ) {
                                Text("添加")
                            }
                            Button(
                                onClick = {
                                    if(pkgName!=""){credentialList.remove(pkgName)}
                                    refreshText()
                                },
                                modifier = Modifier.fillMaxWidth(0.96F)
                            ) {
                                Text("移除")
                            }
                        }
                    }
                }
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        try{
                            if(policyType!=-1&&credentialList.isNotEmpty()){
                                myDpm.credentialManagerPolicy = PackagePolicy(policyType,credentialList)
                            }else{
                                myDpm.credentialManagerPolicy = null
                            }
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }catch(e:java.lang.IllegalArgumentException){
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }finally {
                            refreshPolicy()
                            refreshText()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
                }
            }
        }
        
        if(isProfileOwner(myDpm)||isDeviceOwner(myDpm)){
            Column(modifier = sections()) {
                Text(text = "许可的无障碍应用", style = typography.titleLarge, color = titleColor)
                var listText by remember{ mutableStateOf("") }
                val refreshList = {
                    listText = ""
                    var count = permittedAccessibility.size
                    for(eachAccessibility in permittedAccessibility){ count-=1; listText+=eachAccessibility; if(count>0){listText+="\n"} }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){
                    val getList = myDpm.getPermittedAccessibilityServices(myComponent)
                    if(getList!=null){ permittedAccessibility = getList }
                    refreshList(); inited=true
                }
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                    Text(text = if(listText==""){"无"}else{listText}, style = bodyTextStyle, color = titleColor)
                }
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = { permittedAccessibility.add(pkgName); refreshList()},
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("添加")
                    }
                    Button(
                        onClick = { permittedAccessibility.remove(pkgName); refreshList() },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("移除")
                    }
                }
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        Toast.makeText(myContext, if(myDpm.setPermittedAccessibilityServices(myComponent, permittedAccessibility)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                        val getList = myDpm.getPermittedAccessibilityServices(myComponent)
                        if(getList!=null){ permittedAccessibility = getList }
                        refreshList()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "应用")
                }
            }
        }
        
        if(isDeviceOwner(myDpm)||isProfileOwner(myDpm)){
            Column(modifier = sections()) {
                Text(text = "许可的输入法", style = typography.titleLarge, color = titleColor)
                var imeListText by remember{ mutableStateOf("") }
                val refreshList = {
                    imeListText = ""
                    for(eachIme in permittedIme){ imeListText += "$eachIme \n" }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){
                    val getList = myDpm.getPermittedInputMethods(myComponent)
                    if(getList!=null){ permittedIme = getList }
                    refreshList();inited=true
                }
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                    Text(text = if(imeListText==""){"无"}else{imeListText}, style = bodyTextStyle, color = titleColor)
                }
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = { permittedIme.add(pkgName); refreshList() },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("添加")
                    }
                    Button(
                        onClick = { permittedIme.remove(pkgName); refreshList()},
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("移除")
                    }
                }
                Button(
                    onClick = {
                        Toast.makeText(myContext, if(myDpm.setPermittedInputMethods(myComponent, permittedIme)){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                        val getList = myDpm.getPermittedInputMethods(myComponent)
                        if(getList!=null){ permittedIme = getList }
                        refreshList()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
                }
            }
        }
        
        if(VERSION.SDK_INT>=28&&isDeviceOwner(myDpm)){
            Column(modifier = sections()){
                Text(text = "保持卸载的应用", style = typography.titleLarge, color = titleColor)
                Text(text = "作用未知", style = bodyTextStyle)
                var listText by remember{mutableStateOf("")}
                val refresh = {
                    listText = ""
                    var count = keepUninstallPkg.size
                    for(each in keepUninstallPkg){ count-=1; listText+=each; if(count>0){listText+="\n"} }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){
                    val getList = myDpm.getKeepUninstalledPackages(myComponent)
                    if(getList!=null){ keepUninstallPkg = getList }
                    refresh(); inited=true
                }
                SelectionContainer(modifier = Modifier.horizontalScroll(rememberScrollState()).animateContentSize(scrollAnim())){
                    Text(text = if(listText==""){"无"}else{listText}, style = bodyTextStyle, color = titleColor)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            keepUninstallPkg.add(pkgName)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ){
                        Text("添加")
                    }
                    Button(
                        onClick = {
                            keepUninstallPkg.remove(pkgName)
                            refresh()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ){
                        Text("移除")
                    }
                }
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        myDpm.setKeepUninstalledPackages(myComponent, keepUninstallPkg)
                        val getList = myDpm.getKeepUninstalledPackages(myComponent)
                        if(getList!=null){ keepUninstallPkg = getList }
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("应用")
                }
            }
        }
        
        if(VERSION.SDK_INT>=28){
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
                    myDpm.clearApplicationUserData(myComponent,pkgName,executor,onClear)
                },
                enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp)
            ) {
                Text("清除应用存储")
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
        
        Column(modifier = sections()){
            Text(text = "卸载应用", style = typography.titleLarge, color = titleColor)
            Text(text = "静默卸载需Device owner", style = bodyTextStyle)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                Button(
                    onClick = {
                        val intent = Intent(myContext,PackageInstallerReceiver::class.java)
                        val intentSender = PendingIntent.getBroadcast(myContext, 8, intent, PendingIntent.FLAG_IMMUTABLE).intentSender
                        val pkgInstaller = myContext.packageManager.packageInstaller
                        pkgInstaller.uninstall(pkgName, intentSender)
                    },
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text("静默卸载")
                }
                Button(
                    onClick = {
                        val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE)
                        intent.setData(Uri.parse("package:$pkgName"))
                        myContext.startActivity(intent)
                    },
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text("请求卸载")
                }
            }
        }
        
        Column(modifier = sections()){
            Text(text = "安装应用", style = typography.titleLarge, color = titleColor)
            Text(text = "静默安装需Device owner", style = bodyTextStyle)
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    val installApkIntent = Intent(Intent.ACTION_GET_CONTENT)
                    installApkIntent.setType("application/vnd.android.package-archive")
                    installApkIntent.addCategory(Intent.CATEGORY_OPENABLE)
                    getApk.launch(installApkIntent)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("选择APK...")
            }
            var selected by remember{mutableStateOf(false)}
            LaunchedEffect(selected){apkSelected{selected = apkUri!=null}}
            AnimatedVisibility(selected) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = { uriToStream(myContext, apkUri){stream -> installPackage(myContext,stream)} },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("静默安装")
                    }
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_INSTALL_PACKAGE)
                            intent.setData(apkUri)
                            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            myContext.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("请求安装")
                    }
                }
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
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    Row(
        modifier = sections(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        var enabled by remember{mutableStateOf(getMethod())}
        enabled = getMethod()
        Column(modifier = if(sharedPref.getBoolean("isWear",false)){Modifier.fillMaxWidth(0.65F)}else{Modifier}){
            Text(text = stringResource(itemName), style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
            if(itemDesc!=R.string.place_holder){ Text(stringResource(itemDesc)) }
        }
        Switch(
            checked = enabled,
            onCheckedChange = { setMethod(!enabled); enabled=getMethod() }
        )
    }
}

@Throws(IOException::class)
private fun installPackage(context: Context, inputStream: InputStream){
    val packageInstaller = context.packageManager.packageInstaller
    val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL)
    val sessionId = packageInstaller.createSession(params)
    val session = packageInstaller.openSession(sessionId)
    val out = session.openWrite("COSU", 0, -1)
    val buffer = ByteArray(65536)
    var c: Int
    while(inputStream.read(buffer).also{c = it}!=-1) { out.write(buffer, 0, c) }
    session.fsync(out)
    inputStream.close()
    out.close()
    val pendingIntent = PendingIntent.getBroadcast(context, sessionId, Intent(context,PackageInstallerReceiver::class.java), PendingIntent.FLAG_IMMUTABLE).intentSender
    session.commit(pendingIntent)
}

private suspend fun apkSelected(operation:()->Unit){
    while(true){
        delay(500)
        operation()
    }
}