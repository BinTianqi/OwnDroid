package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.ACTION_PROVISION_MANAGED_PROFILE
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Parcelable
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

@Composable
fun ManagedProfile(navCtrl:NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium}else{ typography.bodyLarge}
    Column(modifier = Modifier.verticalScroll(rememberScrollState())){
        Column(modifier = sections()){
            Text(text = "信息", style = typography.titleLarge)
            if(VERSION.SDK_INT>=30){
                Text(text = "由组织拥有的工作资料：${myDpm.isOrganizationOwnedDeviceWithManagedProfile}", style = bodyTextStyle)
            }
        }
        Column(modifier = sections()) {
            Text(text = "工作资料", style = typography.titleLarge)
            if(VERSION.SDK_INT>=24){
                if(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
                    Text(text = "已是工作资料")
                }else{
                    Text(text = "可以创建工作资料：${myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE)}", style = bodyTextStyle)
                }
            }
            if(isDeviceOwner(myDpm)){
                Text(text = "Device owner不能创建工作资料", style = bodyTextStyle)
            }
            if(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE))){
                var skipEncrypt by remember{mutableStateOf(false)}
                if(VERSION.SDK_INT>=24){CheckBoxItem("跳过加密",{skipEncrypt},{skipEncrypt=!skipEncrypt})}
                Button(
                    onClick = {
                        val intent = Intent(ACTION_PROVISION_MANAGED_PROFILE)
                        if(VERSION.SDK_INT>=23){
                            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,myComponent)
                        }else{
                            intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,"com.binbin.androidowner")
                        }
                        if(VERSION.SDK_INT>=24){intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_SKIP_ENCRYPTION,skipEncrypt)}
                        if(VERSION.SDK_INT>=33){intent.putExtra(DevicePolicyManager.EXTRA_PROVISIONING_ALLOW_OFFLINE,true)}
                        createManagedProfile.launch(intent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("创建")
                }
            }
            if(isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)))){
                Button(
                    onClick = {
                        myDpm.setProfileEnabled(myComponent)
                        navCtrl.navigateUp()
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "激活")
                }
            }
        }
        
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            Column(modifier = sections()){
                var time by remember{mutableStateOf("")}
                time = myDpm.getManagedProfileMaximumTimeOff(myComponent).toString()
                Text(text = "资料关闭时间", style = typography.titleLarge)
                Text(text = "工作资料处于关闭状态的时间达到该限制后会停用个人应用，0为无限制(单位：毫秒)", style = bodyTextStyle)
                TextField(
                    value = time, onValueChange = {time=it}, modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    label = {Text("时间(ms)")},
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
                )
                Text(text = "不能少于72小时")
                Button(
                    onClick = {
                        myDpm.setManagedProfileMaximumTimeOff(myComponent,time.toLong())
                        Toast.makeText(myContext, "成功", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("应用")
                }
            }
        }
        
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}
