package com.bintianqi.owndroid.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.app.admin.WifiSsidPolicy
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_ALLOWLIST
import android.app.admin.WifiSsidPolicy.WIFI_SSID_POLICY_TYPE_DENYLIST
import android.content.ComponentName
import android.net.wifi.WifiSsid
import android.os.Build.VERSION
import android.telephony.TelephonyManager
import android.telephony.TelephonyManager.UNKNOWN_CARRIER_ID
import android.telephony.data.ApnSetting.*
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.bintianqi.owndroid.R
import com.bintianqi.owndroid.Receiver
import com.bintianqi.owndroid.toText
import com.bintianqi.owndroid.ui.*
import com.bintianqi.owndroid.ui.theme.bgColor

var ssidSet = mutableSetOf<WifiSsid>()
@Composable
fun Network(navCtrl: NavHostController){
    val localNavCtrl = rememberNavController()
    val backStackEntry by localNavCtrl.currentBackStackEntryAsState()
    val scrollState = rememberScrollState()
    /*val titleMap = mapOf(
        "Home" to R.string.network,
        "MinWifiSecurityLevel" to R.string.min_wifi_security_level,
        "WifiSsidPolicy" to R.string.wifi_ssid_policy,
        "PrivateDNS" to R.string.private_dns,
        "NetLog" to R.string.retrieve_net_logs,
        "WifiKeypair" to R.string.wifi_keypair,
        "APN" to R.string.apn_settings
    )*/
    Scaffold(
        topBar = {
            /*TopAppBar(
                title = {Text(text = stringResource(titleMap[backStackEntry?.destination?.route]?:R.string.network))},
                navigationIcon = {NavIcon{if(backStackEntry?.destination?.route=="Home"){navCtrl.navigateUp()}else{localNavCtrl.navigateUp()}}},
                colors = TopAppBarDefaults.topAppBarColors(containerColor = colorScheme.surfaceVariant)
            )*/
            TopBar(backStackEntry,navCtrl,localNavCtrl){
                if(backStackEntry?.destination?.route=="Home"&&scrollState.maxValue>80){
                    Text(
                        text = stringResource(R.string.network),
                        modifier = Modifier.alpha((maxOf(scrollState.value-30,0)).toFloat()/80)
                    )
                }
            }
        }
    ){
        NavHost(
            navController = localNavCtrl, startDestination = "Home",
            enterTransition = Animations.navHostEnterTransition,
            exitTransition = Animations.navHostExitTransition,
            popEnterTransition = Animations.navHostPopEnterTransition,
            popExitTransition = Animations.navHostPopExitTransition,
            modifier = Modifier.background(bgColor).padding(top = it.calculateTopPadding())
        ){
            composable(route = "Home"){Home(localNavCtrl,scrollState)}
            composable(route = "Switches"){Switches()}
            composable(route = "MinWifiSecurityLevel"){WifiSecLevel()}
            composable(route = "WifiSsidPolicy"){WifiSsidPolicy()}
            composable(route = "PrivateDNS"){PrivateDNS()}
            composable(route = "NetLog"){NetLog()}
            composable(route = "WifiKeypair"){WifiKeypair()}
            composable(route = "APN"){APN()}
        }
    }
}

@Composable
private fun Home(navCtrl:NavHostController,scrollState: ScrollState){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext, Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)){
        Text(text = stringResource(R.string.network), style = typography.headlineLarge, modifier = Modifier.padding(top = 8.dp, bottom = 5.dp, start = 15.dp))
        if(VERSION.SDK_INT>=24&&isDeviceOwner(myDpm)){
            val wifimac = myDpm.getWifiMacAddress(myComponent)
            Text(text = "WiFi MAC: $wifimac", modifier = Modifier.padding(start = 15.dp))
        }
        Spacer(Modifier.padding(vertical = 3.dp))
        if(VERSION.SDK_INT>=30){
            SubPageItem(R.string.options,"",R.drawable.tune_fill0){navCtrl.navigate("Switches")}
        }
        if(VERSION.SDK_INT>=33){
            SubPageItem(R.string.min_wifi_security_level,"",R.drawable.wifi_password_fill0){navCtrl.navigate("MinWifiSecurityLevel")}
        }
        if(VERSION.SDK_INT>=33&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            SubPageItem(R.string.wifi_ssid_policy,"",R.drawable.wifi_fill0){navCtrl.navigate("WifiSsidPolicy")}
        }
        if(VERSION.SDK_INT>=29&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.private_dns,"",R.drawable.dns_fill0){navCtrl.navigate("PrivateDNS")}
        }
        if(VERSION.SDK_INT>=26&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)))){
            SubPageItem(R.string.retrieve_net_logs,"",R.drawable.description_fill0){navCtrl.navigate("NetLog")}
        }
        if(VERSION.SDK_INT>=31&&(isDeviceOwner(myDpm)||isProfileOwner(myDpm))){
            SubPageItem(R.string.wifi_keypair,"",R.drawable.key_fill0){navCtrl.navigate("WifiKeypair")}
        }
        if(VERSION.SDK_INT>=28&&isDeviceOwner(myDpm)){
            SubPageItem(R.string.apn_settings,"",R.drawable.cell_tower_fill0){navCtrl.navigate("APN")}
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
private fun Switches(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize()){
        Spacer(Modifier.padding(vertical = 5.dp))
        if(VERSION.SDK_INT>=33&&isDeviceOwner(myDpm)){
            SwitchItem(
                R.string.preferential_network_service, stringResource(R.string.developing),R.drawable.globe_fill0,
                {myDpm.isPreferentialNetworkServiceEnabled},{myDpm.isPreferentialNetworkServiceEnabled = it}
            )
        }
        if(VERSION.SDK_INT>=30&&(isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile))){
            SwitchItem(R.string.wifi_lockdown,"",R.drawable.wifi_password_fill0,
                {myDpm.hasLockdownAdminConfiguredNetworks(myComponent)},{myDpm.setConfiguredNetworksLockdownState(myComponent,it)}
            )
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun WifiSecLevel(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var selectedWifiSecLevel by remember{mutableIntStateOf(myDpm.minimumRequiredWifiSecurityLevel)}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.min_wifi_security_level), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(stringResource(R.string.wifi_security_level_open), {selectedWifiSecLevel==WIFI_SECURITY_OPEN}, {selectedWifiSecLevel= WIFI_SECURITY_OPEN})
        RadioButtonItem("WEP, WPA(2)-PSK", {selectedWifiSecLevel==WIFI_SECURITY_PERSONAL}, {selectedWifiSecLevel= WIFI_SECURITY_PERSONAL})
        RadioButtonItem("WPA-EAP", {selectedWifiSecLevel==WIFI_SECURITY_ENTERPRISE_EAP}, {selectedWifiSecLevel= WIFI_SECURITY_ENTERPRISE_EAP})
        RadioButtonItem("WPA3-192bit", {selectedWifiSecLevel==WIFI_SECURITY_ENTERPRISE_192}, {selectedWifiSecLevel= WIFI_SECURITY_ENTERPRISE_192})
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            enabled = isDeviceOwner(myDpm)||(isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile),
            onClick = {
                myDpm.minimumRequiredWifiSecurityLevel=selectedWifiSecLevel
                Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.apply))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun WifiSsidPolicy(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var policy = myDpm.wifiSsidPolicy
        var selectedPolicyType by remember{mutableIntStateOf(policy?.policyType ?: -1)}
        var inputSsid by remember{mutableStateOf("")}
        var ssidList by remember{mutableStateOf("")}
        val refreshPolicy = {
            policy = myDpm.wifiSsidPolicy
            selectedPolicyType = policy?.policyType ?: -1
            ssidSet = policy?.ssids ?: mutableSetOf()
        }
        LaunchedEffect(Unit){refreshPolicy(); ssidList= ssidSet.toText()}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.wifi_ssid_policy), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        RadioButtonItem(stringResource(R.string.none),{selectedPolicyType==-1},{selectedPolicyType=-1})
        RadioButtonItem(stringResource(R.string.whitelist),{selectedPolicyType==WIFI_SSID_POLICY_TYPE_ALLOWLIST},{selectedPolicyType=WIFI_SSID_POLICY_TYPE_ALLOWLIST})
        RadioButtonItem(stringResource(R.string.blacklist),{selectedPolicyType==WIFI_SSID_POLICY_TYPE_DENYLIST},{selectedPolicyType=WIFI_SSID_POLICY_TYPE_DENYLIST})
        Column(modifier = Modifier.animateContentSize(scrollAnim()).horizontalScroll(rememberScrollState())){
            if(ssidList!=""){
                Spacer(Modifier.padding(vertical = 5.dp))
                Text(stringResource(R.string.ssid_list_is))
                SelectionContainer{
                    Text(text = ssidList, color = colorScheme.onPrimaryContainer)
                }
            }
        }
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = inputSsid,
            label = { Text("SSID")},
            onValueChange = {inputSsid = it},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    if(inputSsid==""){
                        Toast.makeText(myContext, R.string.cannot_be_empty, Toast.LENGTH_SHORT).show()
                    }else if(WifiSsid.fromBytes(inputSsid.toByteArray()) in ssidSet){
                        Toast.makeText(myContext, R.string.already_exist, Toast.LENGTH_SHORT).show()
                    }else{
                        ssidSet.add(WifiSsid.fromBytes(inputSsid.toByteArray()))
                        ssidList = ssidSet.toText()
                    }
                    inputSsid = ""
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    if(inputSsid==""){
                        Toast.makeText(myContext, R.string.cannot_be_empty, Toast.LENGTH_SHORT).show()
                    }else if(WifiSsid.fromBytes(inputSsid.toByteArray()) in ssidSet){
                        ssidSet.remove(WifiSsid.fromBytes(inputSsid.toByteArray()))
                        inputSsid = ""
                        ssidList = ssidSet.toText()
                    }else{
                        Toast.makeText(myContext, R.string.not_exist, Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
        Button(
            onClick = {
                focusMgr.clearFocus()
                if(selectedPolicyType==-1){
                    if(policy==null&&ssidSet.isNotEmpty()){
                        Toast.makeText(myContext, R.string.please_select_a_policy, Toast.LENGTH_SHORT).show()
                    }else{
                        myDpm.wifiSsidPolicy = null
                        refreshPolicy()
                        Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
                    }
                }else{
                    myDpm.wifiSsidPolicy = if(ssidSet.size==0){ null }else{ WifiSsidPolicy(selectedPolicyType, ssidSet) }
                    refreshPolicy()
                    Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@SuppressLint("NewApi")
@Composable
private fun PrivateDNS(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.private_dns), style = typography.headlineLarge)
        val dnsStatus = mapOf(
            PRIVATE_DNS_MODE_UNKNOWN to stringResource(R.string.unknown),
            PRIVATE_DNS_MODE_OFF to stringResource(R.string.disabled),
            PRIVATE_DNS_MODE_OPPORTUNISTIC to stringResource(R.string.auto),
            PRIVATE_DNS_MODE_PROVIDER_HOSTNAME to stringResource(R.string.dns_provide_hostname)
        )
        val operationResult = mapOf(
            PRIVATE_DNS_SET_NO_ERROR to stringResource(R.string.success),
            PRIVATE_DNS_SET_ERROR_HOST_NOT_SERVING to stringResource(R.string.host_not_serving_dns_tls),
            PRIVATE_DNS_SET_ERROR_FAILURE_SETTING to stringResource(R.string.fail)
        )
        var status by remember{mutableStateOf(dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)])}
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.current_state, status?:stringResource(R.string.unknown)))
        AnimatedVisibility(visible = myDpm.getGlobalPrivateDnsMode(myComponent)!=PRIVATE_DNS_MODE_OPPORTUNISTIC) {
            Spacer(Modifier.padding(vertical = 5.dp))
            Button(
                onClick = {
                    val result = myDpm.setGlobalPrivateDnsModeOpportunistic(myComponent)
                    Toast.makeText(myContext, operationResult[result], Toast.LENGTH_SHORT).show()
                    status = dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)]
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.set_to_auto))
            }
        }
        Spacer(Modifier.padding(vertical = 10.dp))
        var inputHost by remember{mutableStateOf(myDpm.getGlobalPrivateDnsHost(myComponent) ?: "")}
        OutlinedTextField(
            value = inputHost,
            onValueChange = {inputHost=it},
            label = {Text(stringResource(R.string.dns_hostname))},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 3.dp))
        Button(
            onClick = {
                focusMgr.clearFocus()
                val result: Int
                try{
                    result = myDpm.setGlobalPrivateDnsModeSpecifiedHost(myComponent,inputHost)
                    Toast.makeText(myContext, operationResult[result], Toast.LENGTH_SHORT).show()
                }catch(e:IllegalArgumentException){
                    Toast.makeText(myContext, R.string.invalid_hostname, Toast.LENGTH_SHORT).show()
                }catch(e:SecurityException){
                    Toast.makeText(myContext, R.string.security_exception, Toast.LENGTH_SHORT).show()
                }finally {
                    status = dnsStatus[myDpm.getGlobalPrivateDnsMode(myComponent)]
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.set_dns_host))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun NetLog(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.retrieve_net_logs), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(R.string.developing))
        Spacer(Modifier.padding(vertical = 5.dp))
        SwitchItem(R.string.enable,"",null,{myDpm.isNetworkLoggingEnabled(myComponent)},{myDpm.setNetworkLoggingEnabled(myComponent,it)})
        Spacer(Modifier.padding(vertical = 5.dp))
        Button(
            onClick = {
                val log = myDpm.retrieveNetworkLogs(myComponent,1234567890)
                if(log!=null){
                    for(i in log){ Log.d("NetLog",i.toString()) }
                    Toast.makeText(myContext, R.string.success, Toast.LENGTH_SHORT).show()
                }else{
                    Log.d("NetLog",myContext.getString(R.string.none))
                    Toast.makeText(myContext, R.string.none, Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.retrieve))
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun WifiKeypair(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        var keyPair by remember{mutableStateOf("")}
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.wifi_keypair), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        OutlinedTextField(
            value = keyPair,
            label = { Text(stringResource(R.string.keypair))},
            onValueChange = {keyPair = it},
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth()
        )
        Spacer(Modifier.padding(vertical = 5.dp))
        val isExist = try{myDpm.isKeyPairGrantedToWifiAuth(keyPair)}catch(e:java.lang.IllegalArgumentException){false}
        Text(stringResource(R.string.already_exist)+"：$isExist")
        Spacer(Modifier.padding(vertical = 5.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
            Button(
                onClick = {
                    val result = myDpm.grantKeyPairToWifiAuth(keyPair)
                    Toast.makeText(myContext, if(result){R.string.success}else{R.string.fail}, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.49F)
            ) {
                Text(stringResource(R.string.add))
            }
            Button(
                onClick = {
                    val result = myDpm.revokeKeyPairFromWifiAuth(keyPair)
                    Toast.makeText(myContext, if(result){R.string.success}else{R.string.fail}, Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.fillMaxWidth(0.96F)
            ) {
                Text(stringResource(R.string.remove))
            }
        }
    }
}

@SuppressLint("NewApi")
@Composable
private fun APN(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,Receiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp).verticalScroll(rememberScrollState())){
        val setting = myDpm.getOverrideApns(myComponent)
        var inputNum by remember{mutableStateOf("0")}
        var nextStep by remember{mutableStateOf(false)}
        val builder = Builder()
        Spacer(Modifier.padding(vertical = 10.dp))
        Text(text = stringResource(R.string.apn_settings), style = typography.headlineLarge)
        Spacer(Modifier.padding(vertical = 5.dp))
        Text(text = stringResource(id = R.string.developing))
        Spacer(Modifier.padding(vertical = 5.dp))
        SwitchItem(R.string.enable,"",null,{myDpm.isOverrideApnEnabled(myComponent)},{myDpm.setOverrideApnsEnabled(myComponent,it)})
        Text(text = stringResource(R.string.total_apn_amount, setting.size))
        if(setting.size>0){
            Text(text = stringResource(R.string.select_a_apn_or_create, setting.size))
            TextField(
                value = inputNum,
                label = { Text("APN")},
                onValueChange = {inputNum = it},
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
                enabled = !nextStep
            )
        }else{
            Text(text = stringResource(R.string.no_apn_you_should_create_one))
        }
        Button(
            onClick = {focusMgr.clearFocus(); nextStep=!nextStep},
            modifier = Modifier.fillMaxWidth(),
            enabled = inputNum!=""&&(nextStep||inputNum=="0"||setting[inputNum.toInt()-1]!=null)
        ) {
            Text(stringResource(if(nextStep){R.string.previous_step}else{R.string.next_step}))
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
                    label = {Text(stringResource(R.string.name))},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                    Text(text = stringResource(R.string.enable), style = typography.titleLarge)
                    Switch(checked = carrierEnabled, onCheckedChange = {carrierEnabled=it})
                }
                
                Text(text = stringResource(R.string.user_name), style = typography.titleLarge)
                TextField(
                    value = user,
                    onValueChange = {user=it},
                    label = {Text(stringResource(R.string.user_name))},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                if(VERSION.SDK_INT>=33){
                    Text(text = stringResource(R.string.profile_id), style = typography.titleLarge)
                    TextField(
                        value = profileId,
                        onValueChange = {profileId=it},
                        label = {Text("ID")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                
                Text(text = stringResource(R.string.auth_type), style = typography.titleLarge)
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
                        modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                
                Text(text = stringResource(R.string.apn_type), style = typography.titleLarge)
                TextField(
                    value = apnTypeBitmask,
                    onValueChange = {apnTypeBitmask=it},
                    label = {Text(stringResource(R.string.bitmask))},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.description), style = typography.titleLarge)
                TextField(
                    value = entryName,
                    onValueChange = {entryName=it},
                    label = {Text(stringResource(R.string.description))},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.mms_proxy), style = typography.titleLarge)
                if(VERSION.SDK_INT>=29){
                    TextField(
                        value = mmsProxyAddress,
                        onValueChange = {mmsProxyAddress=it},
                        label = {Text(stringResource(R.string.address))},
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                TextField(
                    value = mmsProxyPort,
                    onValueChange = {mmsProxyPort=it},
                    label = {Text(stringResource(R.string.port))},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.proxy), style = typography.titleLarge)
                if(VERSION.SDK_INT>=29){
                    TextField(
                        value = proxyAddress,
                        onValueChange = {proxyAddress=it},
                        label = {Text(stringResource(R.string.address))},
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                TextField(
                    value = proxyPort,
                    onValueChange = {proxyPort=it},
                    label = {Text(stringResource(R.string.port))},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = "MMSC", style = typography.titleLarge)
                TextField(
                    value = mmsc,
                    onValueChange = {mmsc=it},
                    label = {Text("Uri")},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                if(VERSION.SDK_INT>=33){
                    Text(text = "MTU", style = typography.titleLarge)
                    TextField(
                        value = mtuV4,
                        onValueChange = {mtuV4=it},
                        label = {Text("IPV4")},
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                    TextField(
                        value = mtuV6,
                        onValueChange = {mtuV6=it},
                        label = {Text("IPV6")},
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                    )
                }
                
                Text(text = "MVNO", style = typography.titleLarge)
                RadioButtonItem("SPN",{mvnoType==MVNO_TYPE_SPN},{mvnoType=MVNO_TYPE_SPN})
                RadioButtonItem("IMSI",{mvnoType==MVNO_TYPE_IMSI},{mvnoType=MVNO_TYPE_IMSI})
                RadioButtonItem("GID",{mvnoType==MVNO_TYPE_GID},{mvnoType=MVNO_TYPE_GID})
                RadioButtonItem("ICCID",{mvnoType==MVNO_TYPE_ICCID},{mvnoType=MVNO_TYPE_ICCID})
                
                Text(text = stringResource(R.string.network_type), style = typography.titleLarge)
                TextField(
                    value = networkTypeBitmask,
                    onValueChange = {networkTypeBitmask=it},
                    label = {Text(stringResource(R.string.bitmask))},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = "OperatorNumeric", style = typography.titleLarge)
                TextField(
                    value = operatorNumeric,
                    onValueChange = {operatorNumeric=it},
                    label = {Text("ID")},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                Text(text = stringResource(R.string.password), style = typography.titleLarge)
                TextField(
                    value = password,
                    onValueChange = {password=it},
                    label = {Text(stringResource(R.string.password))},
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                    modifier = Modifier.focusable().fillMaxWidth().padding(top = 2.dp, bottom = 4.dp)
                )
                
                if(VERSION.SDK_INT>=33){
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
                        Text(text = stringResource(R.string.persistent), style = typography.titleLarge)
                        Switch(checked = persistent, onCheckedChange = {persistent=it})
                    }
                }
                
                Text(text = stringResource(R.string.protocol), style = typography.titleLarge)
                RadioButtonItem("IPV4",{protocol==PROTOCOL_IP},{protocol=PROTOCOL_IP})
                RadioButtonItem("IPV6",{protocol==PROTOCOL_IPV6},{protocol=PROTOCOL_IPV6})
                RadioButtonItem("IPV4/IPV6",{protocol==PROTOCOL_IPV4V6},{protocol=PROTOCOL_IPV4V6})
                RadioButtonItem("PPP",{protocol==PROTOCOL_PPP},{protocol=PROTOCOL_PPP})
                if(VERSION.SDK_INT>=29){
                    RadioButtonItem("non-IP",{protocol==PROTOCOL_NON_IP},{protocol=PROTOCOL_NON_IP})
                    RadioButtonItem("Unstructured",{protocol==PROTOCOL_UNSTRUCTURED},{protocol=PROTOCOL_UNSTRUCTURED})
                }
                
                Text(text = stringResource(R.string.roaming_protocol), style = typography.titleLarge)
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
                    Text(stringResource(if(finalStep){R.string.previous_step}else{R.string.next_step}))
                }
                AnimatedVisibility(finalStep) {
                    if(inputNum=="0"){
                        Button(
                            onClick = {myDpm.addOverrideApn(myComponent,result)},
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.create))
                        }
                    }else{
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween){
                            Button(
                                onClick = {
                                    val success = myDpm.updateOverrideApn(myComponent,id,result)
                                    Toast.makeText(myContext, if(success){R.string.success}else{R.string.fail}, Toast.LENGTH_SHORT).show()
                                },
                                Modifier.fillMaxWidth(0.49F)
                            ){
                                Text(stringResource(R.string.update))
                            }
                            Button(
                                onClick = {
                                    val success = myDpm.removeOverrideApn(myComponent,id)
                                    Toast.makeText(myContext, if(success){R.string.success}else{R.string.fail}, Toast.LENGTH_SHORT).show()
                                },
                                Modifier.fillMaxWidth(0.96F)
                            ){
                                Text(stringResource(R.string.remove))
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}
