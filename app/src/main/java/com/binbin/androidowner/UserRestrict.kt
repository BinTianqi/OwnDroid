package com.binbin.androidowner

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.os.UserManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp

@Composable
fun UserRestriction(myDpm: DevicePolicyManager, myComponent: ComponentName){
    val verticalScrolling = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(verticalScrolling)) {
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,R.string.config_mobile_network,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_WIFI,R.string.config_wifi,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_BLUETOOTH,R.string.bluetooth,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_AIRPLANE_MODE,R.string.airplane_mode,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_LOCATION,R.string.config_location,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_DEBUGGING_FEATURES,R.string.debug_features,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CREATE_WINDOWS,R.string.create_windows, stringResource(R.string.create_windows_description),myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_ADJUST_VOLUME,R.string.adjust_volume,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_CONFIG_BRIGHTNESS,R.string.config_brightness,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_INSTALL_APPS,R.string.install_apps,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_SMS,R.string.sms,"",myComponent, myDpm)
        UserRestrictionItem(UserManager.DISALLOW_APPS_CONTROL,R.string.apps_ctrl, stringResource(R.string.apps_ctrl_description),myComponent, myDpm)
    }
}

@Composable
private fun UserRestrictionItem(restriction:String, itemName:Int, restrictionDescription:String, myComponent: ComponentName, myDpm: DevicePolicyManager){
    var strictState by remember{ mutableStateOf(myDpm.getUserRestrictions(myComponent).getBoolean(restriction)) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp, horizontal = 8.dp)
            .clip(RoundedCornerShape(10))
            .background(color = MaterialTheme.colorScheme.secondaryContainer)
            .padding(5.dp)
    ){
        Text(
            text = stringResource(itemName),
            style = MaterialTheme.typography.titleLarge
        )
        if(restrictionDescription!=""){Text(restrictionDescription)}
        Text(text = "禁止：$strictState")
        Row {
            Button(
                onClick = {
                    myDpm.clearUserRestriction(myComponent,restriction)
                    strictState = myDpm.getUserRestrictions(myComponent).getBoolean(restriction)
                },
                modifier = Modifier.padding(3.dp)
            ) {
                Text(stringResource(R.string.allow))
            }
            Button(
                onClick = {
                    myDpm.addUserRestriction(myComponent,restriction)
                    strictState = myDpm.getUserRestrictions(myComponent).getBoolean(restriction)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.error
                ) ,
                modifier = Modifier.padding(2.dp)
            ) {
                Text(text = stringResource(R.string.disallow))
            }
        }
    }
}
