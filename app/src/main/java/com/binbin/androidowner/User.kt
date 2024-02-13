package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION
import android.os.UserHandle
import android.os.UserManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.os.UserManagerCompat

var affiliationID = mutableSetOf<String>()
@Composable
fun UserManage() {
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        val myContext = LocalContext.current
        val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
        val focusMgr = LocalFocusManager.current
        val userManager = myContext.getSystemService(Context.USER_SERVICE) as UserManager
        val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
        val isWear = sharedPref.getBoolean("isWear",false)
        val bodyTextStyle = if(isWear){ typography.bodyMedium}else{ typography.bodyLarge}
        Column(modifier = sections()) {
            Text(text = "用户信息", style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
            Text("用户已解锁：${UserManagerCompat.isUserUnlocked(myContext)}",style = bodyTextStyle)
            if(VERSION.SDK_INT>=24){ Text("支持多用户：${UserManager.supportsMultipleUsers()}",style = bodyTextStyle) }
            if(VERSION.SDK_INT>=23){ Text(text = "系统用户：${userManager.isSystemUser}", style = bodyTextStyle) }
            if(VERSION.SDK_INT>=34){ Text(text = "管理员用户：${userManager.isAdminUser}", style = bodyTextStyle) }
            if(VERSION.SDK_INT>=31){ Text(text = "无头系统用户: ${UserManager.isHeadlessSystemUserMode()}",style = bodyTextStyle) }
            Spacer(Modifier.padding(vertical = if(isWear){2.dp}else{5.dp}))
            if (VERSION.SDK_INT >= 28) {
                val logoutable = myDpm.isLogoutEnabled
                Text(text = "用户可以退出 : $logoutable",style = bodyTextStyle)
                if(isDeviceOwner(myDpm)|| isProfileOwner(myDpm)){
                    val ephemeralUser = myDpm.isEphemeralUser(myComponent)
                    Text(text = "临时用户： $ephemeralUser",style = bodyTextStyle)
                }
                Text(text = "附属用户: ${myDpm.isAffiliatedUser}",style = bodyTextStyle)
            }
            Spacer(Modifier.padding(vertical = if(isWear){2.dp}else{5.dp}))
            Text(text = "当前UserID：${getCurrentUserId()}",style = bodyTextStyle)
            Text(text = "当前用户序列号：${userManager.getSerialNumberForUser(android.os.Process.myUserHandle())}",style = bodyTextStyle)
        }

        Column(modifier = sections()) {
            Text(text = "用户操作", style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
            var idInput by remember{ mutableStateOf("") }
            var userHandleById:UserHandle by remember{ mutableStateOf(android.os.Process.myUserHandle()) }
            var useUid by remember{ mutableStateOf(false) }
            TextField(
                value = idInput,
                onValueChange = {
                    idInput=it
                    if(useUid){
                        if(idInput!=""&&VERSION.SDK_INT>=24){
                            userHandleById = UserHandle.getUserHandleForUid(idInput.toInt())
                        }
                    }else{
                        val userHandleBySerial = userManager.getUserForSerialNumber(idInput.toLong())
                        userHandleById = userHandleBySerial ?: android.os.Process.myUserHandle()
                    }
                },
                label = {Text(if(useUid){"UID"}else{"序列号"})},
                enabled = isDeviceOwner(myDpm),
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(15.dp))
                    .clickable(enabled = VERSION.SDK_INT >= 24 && isDeviceOwner(myDpm)) { useUid = !useUid }
                    .padding(end = 12.dp)
            ){
                Checkbox(
                    checked = useUid,
                    onCheckedChange = {useUid=it},
                    enabled = VERSION.SDK_INT>=24&& isDeviceOwner(myDpm)
                )
                Text(text = "使用UID(不靠谱)",modifier = Modifier.padding(bottom = 2.dp), style = bodyTextStyle)
            }
            if(VERSION.SDK_INT>28){
                if(isProfileOwner(myDpm)&&myDpm.isAffiliatedUser){
                    Button(
                        onClick = {
                            val result = myDpm.logoutUser(myComponent)
                            Toast.makeText(myContext, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                        },
                        enabled = isProfileOwner(myDpm),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("登出当前用户")
                    }
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        if(VERSION.SDK_INT>=28){
                            val result = myDpm.startUserInBackground(myComponent,userHandleById)
                            Toast.makeText(myContext, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm)&&VERSION.SDK_INT>=28,
                    modifier = Modifier.fillMaxWidth(0.49F)
                ){
                    Text(if(isWear){"启动"}else{"在后台启动"})
                }
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        if(myDpm.switchUser(myComponent,userHandleById)){
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                        }else{
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text("切换")
                }
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        try{
                            if(VERSION.SDK_INT>=28){
                                val result = myDpm.stopUser(myComponent,userHandleById)
                                Toast.makeText(myContext, userOperationResultCode(result), Toast.LENGTH_SHORT).show()
                            }
                        }catch(e:IllegalArgumentException){
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm)&&VERSION.SDK_INT>=28,
                    modifier = Modifier.fillMaxWidth(0.49F)
                ) {
                    Text("停止")
                }
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        if(myDpm.removeUser(myComponent,userHandleById)){
                            Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                            idInput=""
                        }else{
                            Toast.makeText(myContext, "失败", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth(0.96F)
                ) {
                    Text("移除")
                }
            }
            if(VERSION.SDK_INT<28){
                Text(text = "停止用户需API28", style = bodyTextStyle)
            }
        }

        if(VERSION.SDK_INT>=24){
            Column(modifier = sections()) {
                var userName by remember{ mutableStateOf("") }
                Text(text = "创建用户", style = typography.titleLarge,color = colorScheme.onPrimaryContainer)
                TextField(
                    value = userName,
                    onValueChange = {userName=it},
                    label = {Text("用户名")},
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
                )
                var selectedFlag by remember{ mutableIntStateOf(0) }
                RadioButtonItem("无",{selectedFlag==0},{selectedFlag=0})
                RadioButtonItem("跳过创建用户向导",{selectedFlag==DevicePolicyManager.SKIP_SETUP_WIZARD},{selectedFlag=DevicePolicyManager.SKIP_SETUP_WIZARD})
                if(VERSION.SDK_INT>=28){
                    RadioButtonItem("临时用户",{selectedFlag==DevicePolicyManager.MAKE_USER_EPHEMERAL},{selectedFlag=DevicePolicyManager.MAKE_USER_EPHEMERAL})
                    RadioButtonItem("启用所有系统应用",{selectedFlag==DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED},{selectedFlag=DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED})
                }
                var newUserHandle: UserHandle? by remember{ mutableStateOf(null) }
                Button(
                    onClick = {
                        newUserHandle=myDpm.createAndManageUser(myComponent,userName,myComponent,null,selectedFlag)
                        focusMgr.clearFocus()
                        Toast.makeText(myContext, if(newUserHandle!=null){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                    },
                    enabled = isDeviceOwner(myDpm),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("创建(Owner)")
                }
                if(UserManager.supportsMultipleUsers()&&(VERSION.SDK_INT<34||(VERSION.SDK_INT>=34&&userManager.isAdminUser))){
                    Button(
                        onClick = {
                            val intent = UserManager.createUserCreationIntent(userName,null,null,null)
                            createUser.launch(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("创建(Intent)")
                    }
                    Text(text = "尽量用Device owner模式创建，Intent模式可能没有效果", style = bodyTextStyle)
                }
                if(newUserHandle!=null){ Text(text = "新用户的序列号：${userManager.getSerialNumberForUser(newUserHandle)}", style = bodyTextStyle) }
            }
        }
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var input by remember{mutableStateOf("")}
                var list by remember{mutableStateOf("")}
                val refresh = {
                    list = ""
                    var count = affiliationID.size
                    for(item in affiliationID){ count-=1; list+=item; if(count>0){list+="\n"} }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){affiliationID = myDpm.getAffiliationIds(myComponent);refresh();inited=true}
                Text(text = "附属用户ID", style = typography.titleLarge)
                TextField(
                    value = input,
                    onValueChange = {input = it},
                    label = {Text("ID")},
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
                )
                Text(text = if(list==""){"无"}else{list}, style = bodyTextStyle)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = { affiliationID.add(input); refresh() },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ){
                        Text("添加")
                    }
                    Button(
                        onClick = { affiliationID.remove(input); refresh() },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ){
                        Text("移除")
                    }
                }
                Button(
                    onClick = {
                        if("" in affiliationID) {
                            Toast.makeText(myContext, "有空字符串", Toast.LENGTH_SHORT).show()
                        }else if(affiliationID.isEmpty()){
                            Toast.makeText(myContext, "不能为空", Toast.LENGTH_SHORT).show()
                        }else{
                            myDpm.setAffiliationIds(myComponent, affiliationID)
                            affiliationID = myDpm.getAffiliationIds(myComponent)
                            refresh()
                            Toast.makeText(myContext,"成功",Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("应用")
                }
                Text(text = "如果多用户，附属用户ID相同时可以让其他用户附属于主用户", style = bodyTextStyle)
            }
        }
        
        UserSessionMessage("用户名","用户名",true,myDpm,myContext,{null},{msg ->  myDpm.setProfileName(myComponent, msg.toString())})
        if(VERSION.SDK_INT>=28){
            UserSessionMessage("用户会话开始消息","消息",false,myDpm,myContext,{myDpm.getStartUserSessionMessage(myComponent)},{msg ->  myDpm.setStartUserSessionMessage(myComponent,msg)})
            UserSessionMessage("用户会话结束消息","消息",false,myDpm,myContext,{myDpm.getEndUserSessionMessage(myComponent)},{msg ->  myDpm.setEndUserSessionMessage(myComponent,msg)})
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun UserSessionMessage(
    text:String,
    textField:String,
    profileOwner:Boolean,
    myDpm:DevicePolicyManager,
    myContext: Context,
    get:()->CharSequence?,
    setMsg:(msg:CharSequence?)->Unit
){
    Column(
        modifier = sections()
    ) {
        val focusMgr = LocalFocusManager.current
        var msg by remember{ mutableStateOf(if(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner)){ if(get()==null){""}else{get().toString()} }else{""}) }
        val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
        val isWear = sharedPref.getBoolean("isWear",false)
        Text(text = text, style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
        TextField(
            value = msg,
            onValueChange = {msg=it},
            label = {Text(textField)},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
            enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner)
        )
        Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.SpaceBetween) {
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    setMsg(msg)
                    msg = if(get()==null){""}else{get().toString()}
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner),
                modifier = if(isWear){Modifier.fillMaxWidth(0.48F)}else{Modifier.fillMaxWidth(0.65F)}
            ) {
                Text("应用")
            }
            Button(
                onClick = {
                    focusMgr.clearFocus()
                    setMsg(null)
                    msg = if(get()==null){""}else{get().toString()}
                    Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                },
                enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&profileOwner),
                modifier = Modifier.fillMaxWidth(0.92F)
            ) {
                Text("默认")
            }
        }
    }
}

private fun userOperationResultCode(result:Int): String {
    return when(result){
        UserManager.USER_OPERATION_SUCCESS->"成功"
        UserManager.USER_OPERATION_ERROR_UNKNOWN->"未知结果（失败）"
        UserManager.USER_OPERATION_ERROR_MANAGED_PROFILE->"失败：受管理的资料"
        UserManager.USER_OPERATION_ERROR_CURRENT_USER->"失败：当前用户"
        else->"未知"
    }
}

fun getCurrentUserId(): Int {
    try {
        val uh = UserHandle::class.java.getDeclaredMethod("myUserId")
        uh.isAccessible = true
        val userId = uh.invoke(null)
        if (userId is Int) { return userId }
    } catch (ignored: Exception) { }
    return -1
}
