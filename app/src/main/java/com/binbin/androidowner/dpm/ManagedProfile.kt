package com.binbin.androidowner.dpm

import android.annotation.SuppressLint
import android.app.admin.DevicePolicyManager
import android.app.admin.DevicePolicyManager.*
import android.content.*
import android.os.Binder
import android.os.Build.VERSION
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.focusable
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.binbin.androidowner.R
import com.binbin.androidowner.ui.CheckBoxItem

@Composable
fun ManagedProfile() {
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val sharedPref = LocalContext.current.getSharedPreferences("data", Context.MODE_PRIVATE)
    val isWear = sharedPref.getBoolean("isWear",false)
    val bodyTextStyle = if(isWear){ typography.bodyMedium}else{ typography.bodyLarge}
    val titleColor = colorScheme.onPrimaryContainer
    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())){
        Column{
            Text(text = stringResource(R.string.info), style = typography.titleLarge, color = titleColor)
            if(VERSION.SDK_INT>=24){
                if(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)){
                    Text(text = stringResource(R.string.is_already_work_profile))
                }else{
                    Text(text = stringResource(R.string.able_to_create_work_profile, myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE)), style = bodyTextStyle)
                    if(isDeviceOwner(myDpm)){
                        Text(text = stringResource(R.string.device_owner_cannot_create_work_profile), style = bodyTextStyle)
                    }
                }
            }
            if(VERSION.SDK_INT>=30){
                Text(text = stringResource(R.string.is_org_owned_profile, myDpm.isOrganizationOwnedDeviceWithManagedProfile), style = bodyTextStyle)
            }
        }
        
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)&&!myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            OrgOwnedProfile()
            
        }
        if(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isProvisioningAllowed(ACTION_PROVISION_MANAGED_PROFILE))){
            CreateWorkProfile()
        }
        
        if(VERSION.SDK_INT>=30&&isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent)&&myDpm.isOrganizationOwnedDeviceWithManagedProfile){
            SuspendPersonalApp()
        }
        
        if(isProfileOwner(myDpm)&&(VERSION.SDK_INT<24||(VERSION.SDK_INT>=24&&myDpm.isManagedProfile(myComponent)))){
            IntentFilter()
        }
        
        if(VERSION.SDK_INT>=31&&(isProfileOwner(myDpm)&&myDpm.isManagedProfile(myComponent))){
            OrgID()
        }
        
        Spacer(Modifier.padding(vertical = 30.dp))
    }
}

@Composable
fun CreateWorkProfile(){
    val myContext = LocalContext.current
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    Column{
        Text(text = stringResource(R.string.work_profile), style = typography.titleLarge)
        var skipEncrypt by remember{mutableStateOf(false)}
        if(VERSION.SDK_INT>=24){CheckBoxItem(stringResource(R.string.skip_encryption),{skipEncrypt},{skipEncrypt=!skipEncrypt})}
        Button(
            onClick = {
                try {
                    val intent = Intent(ACTION_PROVISION_MANAGED_PROFILE)
                    if(VERSION.SDK_INT>=23){
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME,myComponent)
                    }else{
                        intent.putExtra(EXTRA_PROVISIONING_DEVICE_ADMIN_PACKAGE_NAME,"com.binbin.androidowner")
                    }
                    if(VERSION.SDK_INT>=24){intent.putExtra(EXTRA_PROVISIONING_SKIP_ENCRYPTION,skipEncrypt)}
                    if(VERSION.SDK_INT>=33){intent.putExtra(EXTRA_PROVISIONING_ALLOW_OFFLINE,true)}
                    createManagedProfile.launch(intent)
                }catch(e:ActivityNotFoundException){
                    Toast.makeText(myContext,myContext.getString(R.string.unsupported),Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.create))
        }
    }
}

@Composable
fun OrgOwnedProfile(){
    Column{
        Text(text = stringResource(R.string.org_owned_work_profile), color = colorScheme.onTertiaryContainer, style = typography.titleLarge)
        SelectionContainer {
            Text(text = "使用ADB执行以下命令，或者使用Shizuku")
            Text(
                text = stringResource(R.string.activate_org_profile_command, Binder.getCallingUid()/100000),
                color = colorScheme.onTertiaryContainer
            )
        }
    }
}

@SuppressLint("NewApi")
@Composable
fun OrgID(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val focusMgr = LocalFocusManager.current
    Column{
        var orgId by remember{mutableStateOf("")}
        Text(text = stringResource(R.string.org_id), style = typography.titleLarge)
        OutlinedTextField(
            value = orgId, onValueChange = {orgId=it},
            label = {Text(stringResource(R.string.org_id))},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp)
        )
        AnimatedVisibility(orgId.length !in 6..64) {
            Text(text = stringResource(R.string.length_6_to_64))
        }
        Button(
            onClick = {
                myDpm.setOrganizationId(orgId)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            enabled = orgId.length in 6..64,
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.apply))
        }
        Text(text = stringResource(R.string.get_specific_id_after_set_org_id))
    }
}

@SuppressLint("NewApi")
@Composable
fun SuspendPersonalApp(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically){
            var suspended by remember{mutableStateOf(false)}
            suspended = myDpm.getPersonalAppsSuspendedReasons(myComponent)!=PERSONAL_APPS_NOT_SUSPENDED
            Text(text = stringResource(R.string.suspend_personal_app), style = typography.titleLarge)
            Switch(
                checked = suspended,
                onCheckedChange ={
                    myDpm.setPersonalAppsSuspended(myComponent,!suspended)
                    suspended = myDpm.getPersonalAppsSuspendedReasons(myComponent)!=PERSONAL_APPS_NOT_SUSPENDED
                }
            )
        }
        var time by remember{mutableStateOf("")}
        time = myDpm.getManagedProfileMaximumTimeOff(myComponent).toString()
        Text(text = stringResource(R.string.profile_max_time_off), style = typography.titleLarge)
        Text(text = stringResource(R.string.profile_max_time_out_desc))
        Text(text = stringResource(R.string.personal_app_suspended_because_timeout, myDpm.getPersonalAppsSuspendedReasons(myComponent)==PERSONAL_APPS_SUSPENDED_PROFILE_TIMEOUT))
        OutlinedTextField(
            value = time, onValueChange = {time=it}, modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp),
            label = {Text(stringResource(R.string.time_unit_ms))},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()})
        )
        Text(text = stringResource(R.string.cannot_less_than_72_hours))
        Button(
            onClick = {
                myDpm.setManagedProfileMaximumTimeOff(myComponent,time.toLong())
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.apply))
        }
    }
}

@Composable
fun IntentFilter(){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val focusMgr = LocalFocusManager.current
    Column{
        var action by remember{mutableStateOf("")}
        Text(text = stringResource(R.string.intent_filter), style = typography.titleLarge)
        OutlinedTextField(
            value = action, onValueChange = {action = it},
            label = {Text("Action")},
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Ascii, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
            modifier = Modifier.focusable().fillMaxWidth().padding(vertical = 2.dp)
        )
        Button(
            onClick = {
                myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter(action), FLAG_PARENT_CAN_ACCESS_MANAGED)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_work_to_personal))
        }
        Button(
            onClick = {
                myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter(action), FLAG_MANAGED_CAN_ACCESS_PARENT)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(R.string.add_intent_filter_personal_to_work))
        }
        Button(
            onClick = {
                myDpm.clearCrossProfileIntentFilters(myComponent)
                myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_MANAGED_CAN_ACCESS_PARENT)
                myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_PARENT_CAN_ACCESS_MANAGED)
                Toast.makeText(myContext, myContext.getString(R.string.success),Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth()
        ){
            Text(stringResource(R.string.clear_cross_profile_filters))
        }
    }
}

@Composable
fun ActivateManagedProfile(navCtrl: NavHostController){
    val myContext = LocalContext.current
    val myDpm = myContext.getSystemService(ComponentActivity.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    val myComponent = ComponentName(myContext,MyDeviceAdminReceiver::class.java)
    val sharedPref = myContext.getSharedPreferences("data", Context.MODE_PRIVATE)
    myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_MANAGED_CAN_ACCESS_PARENT)
    myDpm.addCrossProfileIntentFilter(myComponent, IntentFilter("com.binbin.androidowner.MAIN_ACTION"), FLAG_PARENT_CAN_ACCESS_MANAGED)
    Column(modifier = Modifier.verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally){
        Text(text = stringResource(R.string.activate_managed_profile), style = typography.titleLarge)
        Text(text = stringResource(R.string.activate_managed_profile_desc))
        Button(
            onClick = {
                myDpm.setProfileEnabled(myComponent)
                navCtrl.popBackStack("HomePage",false)
                sharedPref.edit().putBoolean("ManagedProfileActivated",true).apply()
                Toast.makeText(myContext, myContext.getString(R.string.success), Toast.LENGTH_SHORT).show()
            },
            modifier = Modifier.fillMaxWidth().padding(8.dp)
        ) {
            Text(stringResource(R.string.activate))
        }
    }
}
