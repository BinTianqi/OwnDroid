package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.app.admin.SystemUpdateInfo
import android.app.admin.SystemUpdatePolicy
import android.content.ComponentName
import android.content.Context
import android.os.Build.VERSION
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SysUpdatePolicy(myDpm:DevicePolicyManager,myComponent:ComponentName,myContext: Context){
    val focusMgr = LocalFocusManager.current
    Column {
        if(VERSION.SDK_INT>=26){
            Column(modifier = sections()) {
                val sysUpdateInfo = if(isDeviceOwner(myDpm)){myDpm.getPendingSystemUpdate(myComponent)}else{null}
                if(sysUpdateInfo!=null){
                    Text("Update first available: ${Date(sysUpdateInfo.receivedTime)}")
                    Text("Hash code: ${sysUpdateInfo.hashCode()}")
                    val securityStateDesc = when(sysUpdateInfo.securityPatchState){
                        SystemUpdateInfo.SECURITY_PATCH_STATE_UNKNOWN->"SECURITY_PATCH_STATE_UNKNOWN"
                        SystemUpdateInfo.SECURITY_PATCH_STATE_TRUE->"SECURITY_PATCH_STATE_TRUE"
                        else->"SECURITY_PATCH_STATE_FALSE"
                    }
                    Text("Security patch state: $securityStateDesc")
                }else{
                    Text("暂无更新信息")
                }
            }
        }
        if(VERSION.SDK_INT>=23){
        Column(modifier = sections()) {
            var selectedPolicy by remember{ mutableStateOf(myDpm.systemUpdatePolicy?.policyType) }
            Text(text = "系统更新策略", style = MaterialTheme.typography.titleLarge)
            RadioButtonItem("准备好后立即更新",{selectedPolicy==SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC},{selectedPolicy=SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC})
            RadioButtonItem("在某段时间里更新",{selectedPolicy==SystemUpdatePolicy.TYPE_INSTALL_WINDOWED},{selectedPolicy=SystemUpdatePolicy.TYPE_INSTALL_WINDOWED})
            RadioButtonItem("延迟30天",{selectedPolicy==SystemUpdatePolicy.TYPE_POSTPONE},{selectedPolicy=SystemUpdatePolicy.TYPE_POSTPONE})
            RadioButtonItem("无",{selectedPolicy == null},{selectedPolicy=null})
            var windowedPolicyStart by remember{ mutableStateOf("") }
            var windowedPolicyEnd by remember{ mutableStateOf("") }
            if(selectedPolicy==2){
                Spacer(Modifier.padding(vertical = 3.dp))
                Row(modifier = Modifier.fillMaxWidth(),horizontalArrangement = Arrangement.Center) {
                    TextField(
                        value = windowedPolicyStart,
                        label = { Text("开始时间")},
                        onValueChange = {windowedPolicyStart=it},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.fillMaxWidth(0.5F)
                    )
                    Spacer(Modifier.padding(horizontal = 3.dp))
                    TextField(
                        value = windowedPolicyEnd,
                        onValueChange = {windowedPolicyEnd=it},
                        label = {Text("结束时间")},
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {focusMgr.clearFocus()}),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.padding(vertical = 3.dp))
                Text("请输入一天中的分钟（0~1440）")
            }
            val policy =
                when(selectedPolicy){
                    SystemUpdatePolicy.TYPE_INSTALL_AUTOMATIC->SystemUpdatePolicy.createAutomaticInstallPolicy()
                    SystemUpdatePolicy.TYPE_INSTALL_WINDOWED->SystemUpdatePolicy.createWindowedInstallPolicy(windowedPolicyStart.toInt(),windowedPolicyEnd.toInt())
                    SystemUpdatePolicy.TYPE_POSTPONE->SystemUpdatePolicy.createPostponeInstallPolicy()
                    else->null
                }
            Button(
                onClick = {myDpm.setSystemUpdatePolicy(myComponent,policy);Toast.makeText(myContext, "成功！", Toast.LENGTH_SHORT).show()},
                enabled = isDeviceOwner(myDpm),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("应用")
            }
        }}
        /*val policy = myDpm.systemUpdatePolicy
        if(VERSION.SDK_INT>=28&&policy!=null){
            Column {
                var dateState = rememberDateRangePickerState()
                DateRangePicker(state = dateState)
                val frzPeriod = FreezePeriod(
                    MonthDay.of(6, 1),
                    MonthDay.of(7, 31))
                policy.freezePeriods = listOf(frzPeriod)
                Button(
                    onClick = {
                        try{
                            myDpm.setSystemUpdatePolicy(myComponent, policy)
                        }catch (e:SystemUpdatePolicy.ValidationFailedException){
                            Toast.makeText(myContext, "正在冷却期", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("设置更新冻结期")
                }

            }
        }*/
    }
}
