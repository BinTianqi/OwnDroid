package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.WifiSsidPolicy
import android.content.ComponentName
import android.content.Context
import android.net.wifi.WifiSsid
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

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
        
        if(VERSION.SDK_INT>=24){
            val wifimac = try { myDpm.getWifiMacAddress(myComponent).toString() }catch(e:SecurityException){ "没有权限" }
            Text(text = "WiFi MAC: $wifimac",modifier=Modifier.fillMaxWidth(), textAlign = TextAlign.Center,style=bodyTextStyle)
        }
        
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            DeviceCtrlItem(R.string.network_logging,R.string.developing,R.drawable.wifi_fill0,
                {myDpm.isNetworkLoggingEnabled(myComponent)},{b -> myDpm.setNetworkLoggingEnabled(myComponent,b) }
            )
        }
        if(VERSION.SDK_INT>=33&&isDeviceOwner(myDpm)){
            DeviceCtrlItem(R.string.preferential_network_service,R.string.developing,R.drawable.globe_fill0,
                {myDpm.isPreferentialNetworkServiceEnabled},{b ->  myDpm.isPreferentialNetworkServiceEnabled = b}
            )
        }
        if(VERSION.SDK_INT>=30){
            DeviceCtrlItem(R.string.wifi_lockdown,R.string.place_holder,R.drawable.wifi_password_fill0,
                {myDpm.hasLockdownAdminConfiguredNetworks(myComponent)},{b ->  myDpm.setConfiguredNetworksLockdownState(myComponent,b)}
            )
        }
        if(VERSION.SDK_INT>=33){
            Column(modifier = sections()){
                var selectedWifiSecLevel by remember{mutableIntStateOf(myDpm.minimumRequiredWifiSecurityLevel)}
                Text(text = "要求最小WiFi安全等级", style = typography.titleLarge, color = colorScheme.onPrimaryContainer)
                RadioButtonItem("开放", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_OPEN}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_OPEN})
                RadioButtonItem("WEP, WPA(2)-PSK", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_PERSONAL}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_PERSONAL})
                RadioButtonItem("WPA-EAP", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_EAP})
                RadioButtonItem("WPA3-192bit", {selectedWifiSecLevel==DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192}, {selectedWifiSecLevel= DevicePolicyManager.WIFI_SECURITY_ENTERPRISE_192})
                Button(
                    enabled = isDeviceOwner(myDpm)||isProfileOwner(myDpm),
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
        
        if(VERSION.SDK_INT>=33&&isDeviceOwner(myDpm)){
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
                Text(text = "WiFi SSID策略", style = typography.titleLarge)
                RadioButtonItem("白名单",{selectedPolicyType==WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST},{selectedPolicyType= WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST})
                RadioButtonItem("黑名单",{selectedPolicyType==WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST},{selectedPolicyType= WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST})
                Text("SSID列表：")
                Text(text = if(ssidList!=""){ssidList}else{"无"}, style = bodyTextStyle)
                TextField(
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
                Text(text = "私人DNS", style = typography.titleLarge)
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
                Text(text = "状态：$status")
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
                TextField(
                    value = inputHost,
                    onValueChange = {inputHost=it},
                    label = {Text("DNS主机名")},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
                Button(
                    onClick = {
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
        
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            Column(modifier = sections()){
                var keyPair by remember{mutableStateOf("")}
                Text(text = "WiFi密钥对", style = typography.titleLarge)
                TextField(
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
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}
