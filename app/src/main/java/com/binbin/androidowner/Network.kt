package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.WifiSsidPolicy
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST
import android.content.ComponentName
import android.content.Context
import android.net.wifi.WifiSsid
import android.os.Build.VERSION
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.UNKNOWN_CARRIER_ID
import android.telephony.data.ApnSetting.*
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri

@Composable
fun Network(){
    Column(modifier = Modifier.verticalScroll(rememberScrollState()).fillMaxWidth()){
        val myContext = LocalContext.current
        val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
        val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
        val isWear = sharedPref.getBoolean("isWear",false)
        val bodyTextStyle = if(isWear){ typography.bodyMedium }else{ typography.bodyLarge }
        val focusMgr = LocalFocusManager.current
        val titleColor = colorScheme.onPrimaryContainer
        
        if(VERSION.SDK_INT>=24){
            val wifimac = try { myDpm.getWifiMacAddress(myComponent).toString() }catch(e:SecurityException){ "没有权限" }
            Text(text = "WiFi MAC: $wifimac",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,style=bodyTextStyle)
        }
        
        if(VERSION.SDK_INT>=33&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.preferential_network_service,R.string.developing,R.drawable.globe_fill0,
                {myDpm.isPreferentialNetworkServiceEnabled},{b ->  myDpm.isPreferentialNetworkServiceEnabled = b}
            )
        }
        if(VERSION.SDK_INT>=30&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            DeviceCtrlItem(R.string.wifi_lockdown,R.string.place_holder,R.drawable.wifi_password_fill0,
                {myDpm.hasLockdownAdminConfiguredNetworks(myComponent)},{b ->  myDpm.setConfiguredNetworksLockdownState(myComponent,b)}
            )
        }
        if(VERSION.SDK_INT>=33){
            Column(modifier = sections()){
                var selectedWifiSecLevel by remember{mutableIntStateOf(myDpm.minimumRequiredWifiSecurityLevel)}
                Text(text = "要求最小WiFi安全等级", style = typography.titleLarge, color = titleColor)
                RadioButtonItem("开放", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_OPEN}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_OPEN})
                RadioButtonItem("WEP, WPA(2)-PSK", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_PERSONAL}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_PERSONAL})
                RadioButtonItem("WPA-EAP", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP})
                RadioButtonItem("WPA3-192bit", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192})
                Button(
                    enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile),
                    onClick = {
                        myDpm.minimumRequiredWifiSecurityLevel=selectedWifiSecLevel
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ){
                    Text("应用")
                }
            }
        }else{
            Text(text = "Wifi安全等级需API33", modifier = Modifier.padding(vertical = 3.dp))
        }
        
        if(VERSION.SDK_INT>=33&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            Column(modifier = sections()){
                var policy = myDpm.wifiSsidPolicy
                var selectedPolicyType by remember{mutableIntStateOf(policy?.policyType ?: -1)}
                var inputSsid by remember{mutableStateOf("")}
                var ssidSet = policy?.ssids ?: mutableSetOf<WifiSsid>()
                var ssidList by remember{mutableStateOf("")}
                val refreshList = {
                    policy = myDpm.wifiSsidPolicy
                    selectedPolicyType = policy?.policyType ?: -1
                    ssidSet = policy?.ssids ?: mutableSetOf<WifiSsid>()
                    inputSsid = ""
                    ssidList = ""
                    var count = ssidSet.size
                    for(ssid in ssidSet){
                        count-=1
                        ssidList+=ssid
                        if(count>0){ssidList+="\n"}
                    }
                }
                var inited by remember{mutableStateOf(false)}
                if(!inited){refreshList(); inited=true}
                Text(text = "WiFi SSID策略", style = typography.titleLarge, color = titleColor)
                RadioButtonItem("白名单",{selectedPolicyType==WIFI_SSID_POLICY_TYPE_ALLOWLIST},{selectedPolicyType=WIFI_SSID_POLICY_TYPE_ALLOWLIST})
                RadioButtonItem("黑名单",{selectedPolicyType==WIFI_SSID_POLICY_TYPE_DENYLIST},{selectedPolicyType=WIFI_SSID_POLICY_TYPE_DENYLIST})
                Text("SSID列表：")
                if(ssidList!=""){
                    SelectionContainer {
                        Text(text = ssidList, style = bodyTextStyle)
                    }
                }else{
                    Text(text = "无", style = bodyTextStyle)
                }
                OutlinedTextField(
                    value = inputSsid,
                    label = { Text("SSID")},
                    onValueChange = {inputSsid = it},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            if(selectedPolicyType==-1){
                                Toast.makeText(myContext, "请选择策略", Toast.LENGTH_SHORT).show()
                            }else{
                                ssidSet.add(WifiSsid.fromBytes(inputSsid.toByteArray()))
                                myDpm.wifiSsidPolicy = WifiSsidPolicy(selectedPolicyType, ssidSet)
                                refreshList()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("添加")
                    }
                    Button(
                        onClick = {
                            focusMgr.clearFocus()
                            if(selectedPolicyType==-1){
                                Toast.makeText(myContext, "请选择策略", Toast.LENGTH_SHORT).show()
                            }else{
                                if(WifiSsid.fromBytes(inputSsid.toByteArray()) in ssidSet){
                                    ssidSet.remove(WifiSsid.fromBytes(inputSsid.toByteArray()))
                                    myDpm.wifiSsidPolicy = if(ssidSet.size==0){ null }else{ WifiSsidPolicy(selectedPolicyType, ssidSet) }
                                    refreshList()
                                }else{
                                    Toast.makeText(myContext, "不存在", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("移除")
                    }
                }
            }
        }
        if(VERSION.SDK_INT>=29&&isDeviceOwner(myDpm)){
            Column(modifier = sections()){
                Text(text = "私人DNS", style = typography.titleLarge, color = titleColor)
                val dnsStatus = mapOf(
                    DevicePolicyManager.PRIVATE_DNS_MODE_UNKNOWN to "未知",
                    DevicePolicyManager.PRIVATE_DNS_MODE_OFF to "关闭",
                    DevicePolicyManager.PRIVATE_DNS_MODE_OPPORTUNISTIC to "自动",
                    DevicePolicyManager.PRIVATE_DNS_MODE_PROVIDER_HOSTNAME to "指定主机名"
                )
                val operationResult = mapOf(
                    DevicePolicyManager.PRIVATE_DNS_SET_NO_ERROR to "成功",
                    DevicePolicyManager.PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING to "主机不支持DNS over TLS",
                    DevicePolicyManager.PRIVATE_DNS_SET_ERROR_FAILURE_SETTING to "失败"
                )
                var status by remember{mutableStateOf(dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)])}
                Text(text = "当前状态：$status")
                Button(
                    onClick = {
                        val result = myDpm.setGlobalPrivateDnsModeOpportunistic(myComponent)
                        Toast.makeText(myContext, operationResult[result], Toast.LENGTH_SHORT).show()
                        status = dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)]
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("设为自动")
                }
                Spacer(Modifier.padding(vertical = 3.dp))
                var inputHost by remember{mutableStateOf(myDpm.getGlobalPrivateDnsHost(myComponent) ?: "")}
                OutlinedTextField(
                    value = inputHost,
                    onValueChange = {inputHost=it},
                    label = {Text("DNS主机名")},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                Button(
                    onClick = {
                        focusMgr.clearFocus()
                        val result: Int
                        try{
                            result = myDpm.setGlobalPrivateDnsModeSpecifiedHost(myComponent,inputHost)
                            Toast.makeText(myContext, operationResult[result], Toast.LENGTH_SHORT).show()
                        }catch(e:IllegalArgumentException){
                            Toast.makeText(myContext, "无效主机名", Toast.LENGTH_SHORT).show()
                        }catch(e:SecurityException){
                            Toast.makeText(myContext, "安全错误", Toast.LENGTH_SHORT).show()
                        }finally {
                            status = dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)]
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("设置DNS主机")
                }
            }
        }
        
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)))){
            Column(modifier = sections()){
                Text(text = "收集网络日志", style = typography.titleLarge, color = titleColor)
                Text(text = "功能开发中", style = bodyTextStyle)
                Row(modifier=Modifier.fillMaxWidth().padding(horizontal=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                    var checked by remember{mutableStateOf(myDpm.isNetworkLoggingEnabled(myComponent))}
                    Text(text = "启用", style = typography.titleLarge)
                    Switch(
                        checked = checked,
                        onCheckedChange = {myDpm.setNetworkLoggingEnabled(myComponent,!checked);checked = myDpm.isNetworkLoggingEnabled(myComponent)}
                    )
                }
                Button(
                    onClick = {
                        val log = myDpm.retrieveNetworkLogs(myComponent,1234567890)
                        if(log!=null){
                            for(i in log){ Log.d("NetLog",i.toString()) }
                            Toast.makeText(myContext,"已输出至Log",Toast.LENGTH_SHORT).show()
                        }else{
                            Log.d("NetLog","无")
                            Toast.makeText(myContext,"无",Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("收集")
                }
            }
        }
        
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var keyPair by remember{mutableStateOf("")}
                Text(text = "WiFi密钥对", style = typography.titleLarge, color = titleColor)
                OutlinedTextField(
                    value = keyPair,
                    label = { Text("密钥对")},
                    onValueChange = {keyPair = it},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                val isExist = try{myDpm.isKeyPairGrantedToWifiAuth(keyPair)}catch(e:java.lang.IllegalArgumentException){false}
                Text("已存在：$isExist")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                    Button(
                        onClick = {
                            val result = myDpm.grantKeyPairToWifiAuth(keyPair)
                            Toast.makeText(myContext, if(result){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(0.49F)
                    ) {
                        Text("添加")
                    }
                    Button(
                        onClick = {
                            val result = myDpm.revokeKeyPairFromWifiAuth(keyPair)
                            Toast.makeText(myContext, if(result){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(0.96F)
                    ) {
                        Text("移除")
                    }
                }
            }
        }
        
        if(VERSION.SDK_INT>=28&&isDeviceOwner(myDpm)){
            Column(modifier = sections()){
                val setting = myDpm.getOverrideApns(myComponent)
                var inputNum by remember{mutableStateOf("0")}
                var nextStep by remember{mutableStateOf(false)}
                val builder = Builder()
                Text(text = "APN设置", style = typography.titleLarge, color = titleColor)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                    Text(text = "启用", style = typography.titleLarge)
                    Switch(checked = myDpm.isOverrideApnEnabled(myComponent), onCheckedChange = {myDpm.setOverrideApnsEnabled(myComponent,it)})
                }
                Text(text = "一共有${setting.size}个APN设置", style = bodyTextStyle)
                if(setting.size>0){
                    Text(text = "选择一个你要修改的APN设置(1~${setting.size})或者输入0以新建APN设置", style = bodyTextStyle)
                    TextField(
                        value = inputNum,
                        label = { Text("序号")},
                        onValueChange = {inputNum = it},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        enabled = !nextStep
                    )
                }else{
                    Text(text = "当前没有APN设置，你可以新建一个APN设置", style = bodyTextStyle)
                }
                Button(
                    onClick = {focusMgr.clearFocus(); nextStep=!nextStep},
                    modifier = Modifier.fillMaxWidth(),
                    enabled = inputNum!=""&&(nextStep||inputNum=="0"||setting[inputNum.toInt()-1]!=null)
                ) {
                    Text(if(nextStep){"上一步"}else{"下一步"})
                }
                var result = Builder().build()
                AnimatedVisibility(nextStep) {
                    var carrierEnabled by remember{mutableStateOf(false)}
                    var inputApnName by remember{mutableStateOf("")}
                    var user by remember{mutableStateOf("")}
                    var profileId by remember{mutableStateOf("")}
                    var selectedAuthType by remember{mutableIntStateOf(AUTH_TYPE_NONE)}
                    var carrierId by remember{mutableStateOf("$UNKNOWN_CARRIER_ID")}
                    var apnTypeBitmask by remember{mutableStateOf("")}
                    var entryName by remember{mutableStateOf("")}
                    var mmsProxyAddress by remember{mutableStateOf("")}
                    var mmsProxyPort by remember{mutableStateOf("")}
                    var proxyAddress by remember{mutableStateOf("")}
                    var proxyPort by remember{mutableStateOf("")}
                    var mmsc by remember{mutableStateOf("")}
                    var mtuV4 by remember{mutableStateOf("")}
                    var mtuV6 by remember{mutableStateOf("")}
                    var mvnoType by remember{mutableIntStateOf(-1)}
                    var networkTypeBitmask by remember{mutableStateOf("")}
                    var operatorNumeric by remember{mutableStateOf("")}
                    var password by remember{mutableStateOf("")}
                    var persistent by remember{mutableStateOf(false)}
                    var protocol by remember{mutableIntStateOf(-1)}
                    var roamingProtocol by remember{mutableIntStateOf(-1)}
                    var id by remember{mutableIntStateOf(0)}
                    
                    if(inputNum!="0"){
                        val current = setting[inputNum.toInt()-1]
                        id = current.id
                        carrierEnabled = current.isEnabled
                        inputApnName = current.apnName
                        user = current.user
                        if(VERSION.SDK_INT>=33){profileId = current.profileId.toString()}
                        selectedAuthType = current.authType
                        apnTypeBitmask = current.apnTypeBitmask.toString()
                        entryName = current.entryName
                        if(VERSION.SDK_INT>=29){mmsProxyAddress = current.mmsProxyAddressAsString}
                        mmsProxyPort = current.mmsProxyPort.toString()
                        if(VERSION.SDK_INT>=29){proxyAddress = current.proxyAddressAsString}
                        proxyPort = current.proxyPort.toString()
                        mmsc = current.mmsc.toString()
                        if(VERSION.SDK_INT>=33){ mtuV4 = current.mtuV4.toString(); mtuV6 = current.mtuV6.toString() }
                        mvnoType = current.mvnoType
                        networkTypeBitmask = current.networkTypeBitmask.toString()
                        operatorNumeric = current.operatorNumeric
                        password = current.password
                        if(VERSION.SDK_INT>=33){persistent = current.isPersistent}
                        protocol = current.protocol
                        roamingProtocol = current.roamingProtocol
                    }
                    
                    Column {
                        
                        Text(text = "APN", style = typography.titleLarge)
                        TextField(
                            value = inputApnName,
                            onValueChange = {inputApnName=it},
                            label = {Text("名称")},
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                            Text(text = "启用", style = typography.titleLarge)
                            Switch(checked = carrierEnabled, onCheckedChange = {carrierEnabled=it})
                        }
                        
                        Text(text = "用户名", style = typography.titleLarge)
                        TextField(
                            value = user,
                            onValueChange = {user=it},
                            label = {Text("用户名")},
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        if(VERSION.SDK_INT>=33){
                            Text(text = "资料ID", style = typography.titleLarge)
                            TextField(
                                value = profileId,
                                onValueChange = {profileId=it},
                                label = {Text("ID")},
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                            )
                        }
                        
                        Text(text = "验证类型", style = typography.titleLarge)
                        RadioButtonItem("无",{selectedAuthType==AUTH_TYPE_NONE},{selectedAuthType=AUTH_TYPE_NONE})
                        RadioButtonItem("CHAP",{selectedAuthType==AUTH_TYPE_CHAP},{selectedAuthType=AUTH_TYPE_CHAP})
                        RadioButtonItem("PAP",{selectedAuthType==AUTH_TYPE_PAP},{selectedAuthType=AUTH_TYPE_PAP})
                        RadioButtonItem("PAP/CHAP",{selectedAuthType==AUTH_TYPE_PAP_OR_CHAP},{selectedAuthType=AUTH_TYPE_PAP_OR_CHAP})
                        
                        if(VERSION.SDK_INT>=29){
                            val ts = myContext.getSystemService(ComponentActivity.TELEPHONY_SERVICE) as TelephonyManager
                            carrierId = ts.simCarrierId.toString()
                            Text(text = "CarrierID", style = typography.titleLarge)
                            TextField(
                                value = carrierId,
                                onValueChange = {carrierId=it},
                                label = {Text("ID")},
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                            )
                        }
                        
                        Text(text = "APN类型", style = typography.titleLarge)
                        TextField(
                            value = apnTypeBitmask,
                            onValueChange = {apnTypeBitmask=it},
                            label = {Text("位掩码")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        Text(text = "描述", style = typography.titleLarge)
                        TextField(
                            value = entryName,
                            onValueChange = {entryName=it},
                            label = {Text("文本")},
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        Text(text = "MMS代理", style = typography.titleLarge)
                        if(VERSION.SDK_INT>=29){
                            TextField(
                                value = mmsProxyAddress,
                                onValueChange = {mmsProxyAddress=it},
                                label = {Text("地址")},
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                            )
                        }
                        TextField(
                            value = mmsProxyPort,
                            onValueChange = {mmsProxyPort=it},
                            label = {Text("端口")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        Text(text = "代理", style = typography.titleLarge)
                        if(VERSION.SDK_INT>=29){
                            TextField(
                                value = proxyAddress,
                                onValueChange = {proxyAddress=it},
                                label = {Text("地址")},
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                            )
                        }
                        TextField(
                            value = proxyPort,
                            onValueChange = {proxyPort=it},
                            label = {Text("端口")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        Text(text = "MMSC", style = typography.titleLarge)
                        TextField(
                            value = mmsc,
                            onValueChange = {mmsc=it},
                            label = {Text("Uri")},
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        if(VERSION.SDK_INT>=33){
                            Text(text = "MTU", style = typography.titleLarge)
                            TextField(
                                value = mtuV4,
                                onValueChange = {mtuV4=it},
                                label = {Text("IPV4")},
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                            )
                            TextField(
                                value = mtuV6,
                                onValueChange = {mtuV6=it},
                                label = {Text("IPV6")},
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                                modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                            )
                        }
                        
                        Text(text = "MVNO", style = typography.titleLarge)
                        RadioButtonItem("SPN",{mvnoType==MVNO_TYPE_SPN},{mvnoType=MVNO_TYPE_SPN})
                        RadioButtonItem("IMSI",{mvnoType==MVNO_TYPE_IMSI},{mvnoType=MVNO_TYPE_IMSI})
                        RadioButtonItem("GID",{mvnoType==MVNO_TYPE_GID},{mvnoType=MVNO_TYPE_GID})
                        RadioButtonItem("ICCID",{mvnoType==MVNO_TYPE_ICCID},{mvnoType=MVNO_TYPE_ICCID})
                        
                        Text(text = "网络类型", style = typography.titleLarge)
                        TextField(
                            value = networkTypeBitmask,
                            onValueChange = {networkTypeBitmask=it},
                            label = {Text("位掩码")},
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        Text(text = "OperatorNumeric", style = typography.titleLarge)
                        TextField(
                            value = operatorNumeric,
                            onValueChange = {operatorNumeric=it},
                            label = {Text("ID")},
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        Text(text = "密码", style = typography.titleLarge)
                        TextField(
                            value = password,
                            onValueChange = {password=it},
                            label = {Text("密码")},
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                        )
                        
                        if(VERSION.SDK_INT>=33){
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                                Text(text = "持久的", style = typography.titleLarge)
                                Switch(checked = persistent, onCheckedChange = {persistent=it})
                            }
                        }
                        
                        Text(text = "协议", style = typography.titleLarge)
                        RadioButtonItem("IPV4",{protocol==PROTOCOL_IP},{protocol=PROTOCOL_IP})
                        RadioButtonItem("IPV6",{protocol==PROTOCOL_IPV6},{protocol=PROTOCOL_IPV6})
                        RadioButtonItem("IPV4/IPV6",{protocol==PROTOCOL_IPV4V6},{protocol=PROTOCOL_IPV4V6})
                        RadioButtonItem("PPP",{protocol==PROTOCOL_PPP},{protocol=PROTOCOL_PPP})
                        if(VERSION.SDK_INT>=29){
                            RadioButtonItem("non-IP",{protocol==PROTOCOL_NON_IP},{protocol=PROTOCOL_NON_IP})
                            RadioButtonItem("Unstructured",{protocol==PROTOCOL_UNSTRUCTURED},{protocol=PROTOCOL_UNSTRUCTURED})
                        }
                        
                        Text(text = "漫游协议", style = typography.titleLarge)
                        RadioButtonItem("IPV4",{roamingProtocol==PROTOCOL_IP},{roamingProtocol=PROTOCOL_IP})
                        RadioButtonItem("IPV6",{roamingProtocol==PROTOCOL_IPV6},{roamingProtocol=PROTOCOL_IPV6})
                        RadioButtonItem("IPV4/IPV6",{roamingProtocol==PROTOCOL_IPV4V6},{roamingProtocol=PROTOCOL_IPV4V6})
                        RadioButtonItem("PPP",{roamingProtocol==PROTOCOL_PPP},{roamingProtocol=PROTOCOL_PPP})
                        if(VERSION.SDK_INT>=29){
                            RadioButtonItem("non-IP",{roamingProtocol==PROTOCOL_NON_IP},{roamingProtocol=PROTOCOL_NON_IP})
                            RadioButtonItem("Unstructured",{roamingProtocol==PROTOCOL_UNSTRUCTURED},{roamingProtocol=PROTOCOL_UNSTRUCTURED})
                        }
                        
                        var finalStep by remember{mutableStateOf(false)}
                        Button(
                            onClick = {
                                if(!finalStep){
                                    builder.setCarrierEnabled(carrierEnabled)
                                    builder.setApnName(inputApnName)
                                    builder.setUser(user)
                                    if(VERSION.SDK_INT>=33){builder.setProfileId(profileId.toInt())}
                                    builder.setAuthType(selectedAuthType)
                                    if(VERSION.SDK_INT>=29){builder.setCarrierId(carrierId.toInt())}
                                    builder.setApnTypeBitmask(apnTypeBitmask.toInt())
                                    builder.setEntryName(entryName)
                                    if(VERSION.SDK_INT>=29){builder.setMmsProxyAddress(mmsProxyAddress)}
                                    builder.setMmsProxyPort(mmsProxyPort.toInt())
                                    if(VERSION.SDK_INT>=29){builder.setProxyAddress(proxyAddress)}
                                    builder.setProxyPort(proxyPort.toInt())
                                    builder.setMmsc(mmsc.toUri())
                                    if(VERSION.SDK_INT>=33){ builder.setMtuV4(mtuV4.toInt()); builder.setMtuV6(mtuV6.toInt()) }
                                    builder.setMvnoType(mvnoType)
                                    builder.setNetworkTypeBitmask(networkTypeBitmask.toInt())
                                    builder.setOperatorNumeric(operatorNumeric)
                                    builder.setPassword(password)
                                    if(VERSION.SDK_INT>=33){builder.setPersistent(persistent)}
                                    builder.setProtocol(protocol)
                                    builder.setRoamingProtocol(roamingProtocol)
                                    result = builder.build()
                                }
                                
                                finalStep=!finalStep
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if(finalStep){"上一步"}else{"下一步"})
                        }
                        AnimatedVisibility(finalStep) {
                            if(inputNum=="0"){
                                Button(
                                    onClick = {myDpm.addOverrideApn(myComponent,result)},
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("新建")
                                }
                            }else{
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                                    Button(
                                        onClick = {
                                            val success = myDpm.updateOverrideApn(myComponent,id,result)
                                            Toast.makeText(myContext, if(success){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                                        },
                                        Modifier.fillMaxWidth(0.49F)
                                    ){
                                        Text("更新")
                                    }
                                    Button(
                                        onClick = {
                                            val success = myDpm.removeOverrideApn(myComponent,id)
                                            Toast.makeText(myContext, if(success){"成功"}else{"失败"}, Toast.LENGTH_SHORT).show()
                                        },
                                        Modifier.fillMaxWidth(0.96F)
                                    ){
                                        Text("移除")
                                    }
                                }
                            }
                        }
                    }
                }
                Text(text = stringResource(id = R.string.developing), style = bodyTextStyle)
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}
